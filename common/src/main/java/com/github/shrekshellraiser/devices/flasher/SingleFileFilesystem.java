package com.github.shrekshellraiser.devices.flasher;

import com.github.shrekshellraiser.api.devices.filesystem.IFilesystem;
import com.github.shrekshellraiser.api.devices.filesystem.PathUtils;

import java.util.ArrayList;

public class SingleFileFilesystem implements IFilesystem {
    private String filename = "test.txt";
    private String contents = "This is the contents of the text file.";
    private int deviceNumber = 0xa;

    @Override
    public String getPrefix() {
        return "rom_" + String.format("%1x", deviceNumber);
    }

    public void setDeviceNumber(int deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public void setContents(String contents, String filename) {
        this.contents = contents;
        this.filename = filename;
    }

    @Override
    public ArrayList<String> listContents(String subPath) {
        var list = new ArrayList<String>();
        if (PathUtils.isRoot(subPath)) {
            list.add(filename);
        }
        return list;
    }

    @Override
    public boolean isDirectory(String subPath) {
        return PathUtils.isRoot(subPath);
    }

    @Override
    public int size(String subPath) {
        if (filename.equals(subPath)) {
            return contents.length();
        }
        return 0;
    }

    @Override
    public String readFile(String subPath) {
        if (filename.equals(subPath)) {
            return contents;
        }
        return "";
    }

    @Override
    public void open(String path, boolean append) {

    }

    @Override
    public void closeAll() {

    }

    @Override
    public void close(String path) {

    }

    @Override
    public int write(String content) {
        return 0;
    }
}
