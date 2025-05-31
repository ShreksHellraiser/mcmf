package com.github.shrekshellraiser.api.devices.filesystem;

public class PathUtils {
    public static boolean isRoot(String path) {
        return path.isEmpty() || path.equals("/") || path.equals(".") || path.equals("./");
    }

    public static String removeRoot(String path) {
        if (path.startsWith("./")) {
            return path.substring(2);
        }
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    public static String removePrefix(String path) {
        int index = path.indexOf('/');
        if (index == path.length() - 1) return "";
        return index > 0 ? path.substring(index + 1) : "";
    }

    public static String getPrefix(String path) {
        int index = path.indexOf('/');
        return index > 0 ? path.substring(0, index) : path;
    }
}
