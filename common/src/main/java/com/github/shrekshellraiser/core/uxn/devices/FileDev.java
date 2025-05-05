package com.github.shrekshellraiser.core.uxn.devices;

import com.github.shrekshellraiser.core.uxn.UXN;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Objects;

public class FileDev extends Device {
    private File file;
    private long bytes_read = 0;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void write(int address) {
        int data = bus.readDev(address);
        int devid = address & 0xF0;
        int port = address & 0x0F;
        UXN uxn = bus.getUxn();
        switch (port) {
            case 0x06 -> { // File/delete
                if (file != null) {
                    file.delete();
                }
            }
            case 0x09 -> { // File/name
                short addr = (short) ((bus.readDev(devid + 0x08) << 8) | (data & 0xFF));
                byte last = (byte)uxn.memory.readByte(addr);
                short offset = 1;
                StringBuilder builder = new StringBuilder();
                while (last != 0x00) {
                    builder.append((char)last);
                    last = (byte) (uxn.memory.readByte(addr + offset) & 0xff);
                    offset++;
                }
                String result = builder.toString();
                file = new File(result);
//                if (/* replace with security check here */) {
//                    uxn.writeDev(0x03, (byte) 0x00);
//                    uxn.writeDev(0x04, (byte) 0x00); //success = 0x0000
//                    uxn.writeDev(0x0A, (byte) 0x00);
//                    uxn.writeDev(0x0B, (byte) 0x00); //length = 0x0000
//                    return;
//                }
                short length = (short) Math.min(file.length(), 0xffff);
                bus.writeDev(0x03, (byte) 0x00);
                bus.writeDev(0x04, (byte) 0x01); //success = 0x001
                bus.writeDev(0x0A, (byte) ((length >> 8) & 0xFF));
                bus.writeDev(0x0B, (byte) (length & 0xFF)); //write Length to the file
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        // failed to create file... probably should log this somewhere
                        bus.writeDev(0x03, (byte) 0x00);
                        bus.writeDev(0x04, (byte) 0x00); //success = 0x0000
                        bus.writeDev(0x0A, (byte) 0x00);
                        bus.writeDev(0x0B, (byte) 0x00); //length = 0x0000
                        file = null; //nullify the file since we were unable to create it (cant read from a empty file. and cant write to it)
                    }
                }
                bytes_read = 0;
            }
            case 0x0D -> { // File/read
                if (file == null) {
                    bus.writeDev(devid + 0x03, (byte) 0x00);
                    bus.writeDev(devid + 0x04, (byte) 0x00); //success = 0x0000
                    return;
                }
                int addr = (short) ((bus.readDev(devid + 0x0C) << 8) | (data & 0xFF));
                int length = (short) ((bus.readDev(devid + 0x0A << 8) | (devid + 0x0B & 0xFF)));
                if (file.isDirectory()) {
                    short success = 0x0000;
                    for (File child : Objects.requireNonNull(file.listFiles())) {
                        if (length <= 0) {break;}
                        String size;
                        if (file.isDirectory()) {
                            size = "----";
                        } else {
                            if(file.length() > 0xFFFF) {
                                size = "????";
                            } else {
                                size = String.format("%04X",file.length());
                            }
                        }
                        String fmt = "%s %s\n".formatted(
                                size,
                            file.getName()
                        );
                        if (length - fmt.length() < 0) {break;}
                        int idx = 0;
                        for (byte ch: fmt.getBytes(Charset.forName("IBM437"))) {
                            uxn.memory.writeByte(addr+idx,ch);
                            idx++;
                            success++;
                        }
                    }
                    bus.writeDev(0x03, (byte) ((success >> 8) & 0xFF));
                    bus.writeDev(0x04, (byte) (success & 0xFF)); //write number of bytes read to success
                } else {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        fis.skip(bytes_read);
                        byte[] bytes = new byte[length];
                        int read = fis.read(bytes);
                        for (int i = 0; i < read; i++) {
                            uxn.memory.writeByte(addr + i, bytes[i]);
                        }
                        bytes_read += read;
                        bus.writeDev(0x03, (byte) ((read >> 8) & 0xFF));
                        bus.writeDev(0x04, (byte) (read & 0xFF)); //write number of bytes read to success
                    } catch (IOException e) {
                        bus.writeDev(0x03, (byte) 0x00);
                        bus.writeDev(0x04, (byte) 0x00); //success = 0x0000
                    }
                }

            }
            case 0x0F -> { //File/write
                if (file == null || !file.exists()) {
                    bus.writeDev(0x03, (byte) 0x00);
                    bus.writeDev(0x04, (byte) 0x00); //success = 0x0000
                    return;
                }
                int addr = (short) ((bus.readDev(devid + 0x0E) << 8) | (data & 0xFF));
                int length = (short) ((bus.readDev(devid + 0x0A << 8) | 0x0B & 0xFF));
                int append = bus.readDev(devid + 0x07);
                byte[] bytes = new byte[length];
                for (int i = 0; i < length; i++) {
                    bytes[i] = (byte) uxn.memory.readByte(addr + i);
                }
                try (FileOutputStream fw = new FileOutputStream(file,append != 0x00)) {
                    fw.write(bytes);
                } catch (IOException e) {
                    bus.writeDev(devid + 0x03, (byte) 0x00);
                    bus.writeDev(devid + 0x04, (byte) 0x00); //success = 0x0000
                    return;
                }
                bus.writeDev(devid + 0x03, bus.readDev(devid + 0x0A));
                bus.writeDev(devid + 0x04, bus.readDev(devid + 0x0B));
            }
            default -> {}
        }
    }

    @Override
    public void read(int address) {
        //useless as `write` does all the memory writing stuffs
    }

    @Override
    public String getLabel() {
        return "Java File I/O";
    }
}
