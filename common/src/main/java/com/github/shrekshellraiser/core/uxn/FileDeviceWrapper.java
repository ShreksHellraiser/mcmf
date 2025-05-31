package com.github.shrekshellraiser.core.uxn;

import com.github.shrekshellraiser.api.devices.IDevice;
import com.github.shrekshellraiser.api.devices.filesystem.IFilesystem;
import com.github.shrekshellraiser.api.devices.filesystem.PathUtils;
import com.github.shrekshellraiser.api.devices.filesystem.VPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FileDeviceWrapper implements IDevice {
    private UXNBus bus;
    private final Map<Integer, FileDeviceProvider> devices = new HashMap<>();

    private final Map<String, IFilesystem> registeredFilesystems = new HashMap<>();

    public void registerFilesystem(IFilesystem filesystem) {
        registeredFilesystems.put(filesystem.getPrefix(), filesystem);
    }
    public void registerDevice(int deviceId) {
        if (devices.containsKey(deviceId)) {
            return;
        }
        FileDeviceProvider device = new FileDeviceProvider(deviceId);
        devices.put(deviceId, device);
    }
    public void clearDevices() {
        devices.clear();
    }

    private IFilesystem getFilesystem(String prefix) {
        return registeredFilesystems.get(prefix);
    }

    public FileDeviceWrapper() {
    }

    @Override
    public void write(int address) {
        int deviceId = (address & 0xf0) >> 4;
        FileDeviceProvider device = devices.get(deviceId);
        assert device != null;
        device.write(address);
    }

    private ArrayList<String> listRoot() {
        return new ArrayList<>(registeredFilesystems.keySet());
    }

    private ArrayList<String> listContents(VPath vPath) {
        if (vPath.isRoot()) {
            return listRoot();
        }
        IFilesystem filesystem = getFilesystem(vPath.getLevel(0));
        if (filesystem != null) {
            return filesystem.listContents(vPath.getAfter(1));
        }
        return new ArrayList<>();
    }

    private boolean isDirectory(VPath vPath) {
        if (vPath.isRoot()) {
            return true;
        }
        IFilesystem filesystem = getFilesystem(vPath.getLevel(0));
        if (filesystem != null) {
            return filesystem.isDirectory(vPath.getAfter(1));
        }
        return false;
    }

    private String stat(VPath vPath) {
        if (vPath.isRoot()) {
            return "---- /\n";
        }
        IFilesystem filesystem = getFilesystem(vPath.getLevel(0));
        String subPath = vPath.getAfter(1);
        if (filesystem != null) {
            boolean isDir = filesystem.isDirectory(subPath);
            if (isDir) {
                return String.format("---- %s\n", vPath.getLast());
            }
            return String.format("%04x %s\n", filesystem.size(subPath), subPath);
        }
        return String.format("!!!! %s\n", vPath.getAfter(0));
    }

    private String readFile(VPath vPath) {
        String prefix = vPath.getLevel(0);
        if (!isDirectory(vPath)) {
            IFilesystem filesystem = getFilesystem(prefix);
            if (filesystem == null) return "";
            return filesystem.readFile(vPath.getAfter(1));
        }
        StringBuilder sb = new StringBuilder();
        for (String s : listContents(vPath)) {
            sb.append(stat(new VPath(prefix + '/' + s)));
        }
        return sb.toString();
    }

    @Override
    public void read(int address) {

    }

    @Override
    public void attach(UXNBus bus) {
        this.bus = bus;
        for (Integer deviceId : devices.keySet()) {
            bus.setDevice(deviceId, this);
        }
    }

    @Override
    public void detach(UXNBus bus) {
        for (Integer deviceId : devices.keySet()) {
            bus.deleteDevice(deviceId);
        }
        this.bus = null;
    }

    @Override
    public String getLabel() {
        return "File Device";
    }

    private class FileDeviceProvider {
        int device;
        VPath vPath = new VPath("");
        String path = "";
        IFilesystem activeFilesystem = null;
        String readingData = "";
        int ptr = 0;
        FileDeviceState state = FileDeviceState.IDLE;
        public FileDeviceProvider(int deviceId) {
            this.device = deviceId << 4;
        }
        private void writeLength(int length) {
            bus.writeDevWord(device | 0x0a, length);
        }
        private void writeSuccess(int success) {
            bus.writeDevWord(device | 0x02, success);
        }
        private int writeDataIntoBuffer(int length, int address) {
            int i0 = Math.min(readingData.length(), ptr);
            int i1 = Math.min(readingData.length(), i0 + length);
            String sub = readingData.substring(i0, i1);
            bus.getUxn().memory.writeString(address, sub);
            int read = i1 - i0;
            ptr += read;
            return read;
        }
        private String readDataFromBuffer(int length, int address) {
            return bus.getUxn().memory.readString(address, length, false);
        }
        private int readLength() {
            return bus.readDevWord(device | 0x0a);
        }
        private void prepareRead(FileDeviceState newState, String data) {
            if (!state.equals(newState)) {
                readingData = data;
                state = newState;
                ptr = 0;
            }
        }
        private void prepareWrite() {
            if (!state.equals(FileDeviceState.WRITING)) {
                state = FileDeviceState.WRITING;
                ptr = 0; // ptr is irrelevant for writing
                if (activeFilesystem != null) {
                    activeFilesystem.open(vPath.getAfter(1), false); // TODO
                }
            }
        }
        public void write(int address) {
            int port = address & 0x0F;
            switch (port) {
                case 0x05 -> { // File/stat* - second byte
                    prepareRead(FileDeviceState.STAT, stat(new VPath(vPath.getAfter(1))));
                    writeSuccess(writeDataIntoBuffer(readLength(), bus.readDevWord(address - 1)));
                } case 0x09 -> { // File/name* - second byte
                    if (state == FileDeviceState.WRITING && activeFilesystem != null) {
                        // file was open, close it
                        activeFilesystem.close(vPath.getAfter(1));
                    }
                    path = bus.getUxn().memory.readString(bus.readDevWord(address - 1));
                    vPath = new VPath(path);
                    state = FileDeviceState.OPEN;
                    activeFilesystem = getFilesystem(vPath.getLevel(0));
                } case 0x0d -> { // File/read* - second byte
                    prepareRead(FileDeviceState.READING, readFile(vPath));
                    writeSuccess(writeDataIntoBuffer(readLength(), bus.readDevWord(address - 1)));
                } case 0x0f -> { // File/write* - second byte
                    if (activeFilesystem == null) {
                        writeSuccess(0x0000);
                        return;
                    }
                    prepareWrite();
                    activeFilesystem.write(readDataFromBuffer(readLength(), bus.readDevWord(address - 1)));
                }
            }
        }
    }
    private enum FileDeviceState {
        READING,
        WRITING,
        STAT,
        OPEN,
        IDLE
    }
}