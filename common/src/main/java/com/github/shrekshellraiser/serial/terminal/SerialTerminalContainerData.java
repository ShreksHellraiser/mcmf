package com.github.shrekshellraiser.serial.terminal;

import net.minecraft.world.inventory.ContainerData;

public class SerialTerminalContainerData implements ContainerData {
    private final SerialTerminalBlockEntity entity;
    public static final int ECHO_INDEX = 0;
    public static final int WIDTH_INDEX = 1;
    public static final int HEIGHT_INDEX = 2;
    public static final int FONT_INDEX = 3;
    public static final int CURSOR_X_INDEX = 4;
    public static final int CURSOR_Y_INDEX = 5;
    public static final int DATA_SIZE = 6;

    public static final int MAX_SIZE = 2048;

    public SerialTerminalContainerData(SerialTerminalBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public int get(int i) {
        int width = entity.width;
        int height = entity.height;
        if (i == ECHO_INDEX) {
            return entity.echo ? 1 : 0;
        } else if (i == WIDTH_INDEX) {
            return width;
        } else if (i == HEIGHT_INDEX) {
            return height;
        } else if (i == FONT_INDEX) {
            return entity.font.ordinal();
        } else if (i == CURSOR_X_INDEX) {
            return entity.buffer.getX();
        } else if (i == CURSOR_Y_INDEX) {
            return entity.buffer.getY();
        }
        i -= DATA_SIZE;
        return entity.buffer.getChar(i);
    }

    @Override
    public void set(int i, int j) {
        if (i == ECHO_INDEX) {
            entity.echo = j == 1;
        }
    }

    @Override
    public int getCount() {
        int width = entity.width;
        int height = entity.height;
        return width*height+ DATA_SIZE;
    }
}
