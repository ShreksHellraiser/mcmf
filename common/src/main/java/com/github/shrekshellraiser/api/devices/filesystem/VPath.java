package com.github.shrekshellraiser.api.devices.filesystem;

import java.util.ArrayList;
import java.util.List;

public class VPath {
    private final List<String> tree = new ArrayList<>();
    private static String removeRoot(String path) {
        if (path.startsWith("./")) {
            return path.substring(2);
        }
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }
    public VPath(String path) {
        path = removeRoot(path);
        String[] parts = path.split("/");
        for (String part : parts) {
            if (part.equals(".") || part.isEmpty()) {
                // skip
                continue;
            } else if (part.equals("..")) {
                if (tree.isEmpty()) {
                    continue;
                }
                tree.remove(tree.size() - 1);
            }
            tree.add(part);
        }
    }
    public String getLevel(int level) {
        if (level < 0 || level >= tree.size()) {
            return "";
        }
        return tree.get(level);
    }
    public boolean isRoot() {
        return tree.isEmpty();
    }
    public String getLast() {
        return getLevel(getHeight() - 1);
    }
    /**
     * Returns the path starting at the given level.
     */
    public String getAfter(int start) {
        if (start >= tree.size()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < tree.size(); i++) {
            if (i > start) sb.append("/");
            sb.append(tree.get(i));
        }
        return sb.toString();
    }
    public int getHeight() {
        return tree.size();
    }
}
