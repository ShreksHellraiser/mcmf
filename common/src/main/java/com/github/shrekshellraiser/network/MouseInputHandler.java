package com.github.shrekshellraiser.network;

public interface MouseInputHandler {
    void handleMouseClick(int x, int y, int i);
    void handleMouseRelease(int x, int y, int i);
    void handleMouseMove(int x, int y);
}
