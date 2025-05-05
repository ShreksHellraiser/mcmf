package com.github.shrekshellraiser.core.uxn;

public class MemoryRegion {
    private static final int MEMORY_SIZE = 0xFFFF + 1; // 64 KB
    private final byte[] data;
    public MemoryRegion() {
        this.data = new byte[MEMORY_SIZE];
    }

    public byte[] getData() {
        return data;
    }

    public void writeByte(int address, int d) {
        address = checkAddress(address);
        data[address] = (byte) d;
    }

    public void writeShort(int address, short d) {
        address = checkAddress(address);
        writeByte(address, (byte)((d >>> 8) & 0xFF));
        writeByte((address + 1) % MEMORY_SIZE, (byte)(d & 0xFF));
    }

    public void write(boolean isShort, int address, int d) {
        if (isShort) {
            writeShort(address, (short)d);
            return;
        }
        writeByte(address, (byte)d);
    }

    public int readByte(int address) {
        address = checkAddress(address);
        return data[address] & 0xFF;
    }

    public int readShort(int address) {
        address = checkAddress(address);
        return (readByte(address) << 8) | readByte(address + 1);
    }

    public int read(boolean isShort, int address) {
        if (isShort) {
            return readShort(address);
        }
        return readByte(address);
    }

    private int checkAddress(int address) {
        address %= MEMORY_SIZE;
        if (address < 0) {
            address += MEMORY_SIZE;
        }
        return address;
    }

    public int readZP(boolean isShort, int address) {
        if (isShort) {
            int high = readByte(address);
            int low = readByte((address + 1) % 256);
            return (high << 8) | low;
        }
        return readByte(address);
    }

    public void writeZP(boolean isShort, int address, int d) {
        if (isShort) {
            writeByte(address, (byte)((d >>> 8) & 0xFF));
            writeByte((address + 1) % 256, (byte)(d & 0xFF));
        } else {
            writeByte(address, (byte)d);
        }
    }

    public void fill(int address, int length, int value) {
        for (int i = 0; i < length; i++) {
            writeByte(address + i, value);
        }
    }

    public void copyTo(MemoryRegion destination, int saddr, int daddr, int length) {
        for (int i = 0; i < length; i++) {
            int value = readByte(saddr + i);
            destination.writeByte(daddr+i, value);
        }
    }

    public void copyToL(MemoryRegion destination, int saddr, int daddr, int length) {
        for (int i = length - 1; i >= 0; i--) {
            int value = readByte(saddr + i);
            destination.writeByte(daddr + i, value);
        }
    }
}
