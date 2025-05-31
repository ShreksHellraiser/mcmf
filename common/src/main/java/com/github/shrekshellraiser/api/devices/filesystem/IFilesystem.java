package com.github.shrekshellraiser.api.devices.filesystem;

import java.util.ArrayList;

public interface IFilesystem {
    String getPrefix();

    ArrayList<String> listContents(String subPath);

    boolean isDirectory(String subPath);

    int size(String subPath);

    String readFile(String subPath);

    void open(String path, boolean append);

    void close(String path);

    int write(String content);

    void closeAll();
}
