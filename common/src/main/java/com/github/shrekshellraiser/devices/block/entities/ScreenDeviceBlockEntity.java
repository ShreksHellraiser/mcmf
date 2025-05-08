package com.github.shrekshellraiser.devices.block.entities;

import com.github.shrekshellraiser.computer.block.entity.ComputerBlockEntity;
import com.github.shrekshellraiser.core.uxn.KeyEvent;
import com.github.shrekshellraiser.core.uxn.MemoryRegion;
import com.github.shrekshellraiser.core.uxn.UXNBus;
import com.github.shrekshellraiser.core.uxn.UXNEvent;
import com.github.shrekshellraiser.core.uxn.devices.IAttachableDevice;
import com.github.shrekshellraiser.core.uxn.devices.IDevice;
import com.github.shrekshellraiser.devices.screen.ScreenBuffer;
import com.github.shrekshellraiser.devices.screen.ScreenDeviceMenu;
import com.github.shrekshellraiser.network.KeyInputHandler;
import com.github.shrekshellraiser.network.MouseInputHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.shrekshellraiser.ModBlockEntities.SCREEN_DEVICE_BLOCK_ENTITY;

public class ScreenDeviceBlockEntity extends BlockEntity implements MenuProvider, KeyInputHandler, IAttachableDevice, IDevice, MouseInputHandler {

    private final static int SCREEN_PORT = 0x2;
    private final static int MOUSE_PORT = 0x9;
    private final static byte[][] blending = {
            {0, 0, 1, 2}, {0, 1, 2, 3}, {0, 2, 3, 1}, {0, 3, 1, 2},
            {1, 0, 1, 2}, {0, 1, 2, 3}, {1, 2, 3, 1}, {1, 3, 1, 2},
            {2, 0, 1, 2}, {2, 1, 2, 3}, {0, 3, 1, 2}, {2, 3, 1, 2},
            {3, 0, 1, 2}, {3, 1, 2, 3}, {3, 2, 3, 1}, {0, 3, 1, 2}
    };


    private int x = 0;
    private int y = 0;
    private int spriteAddr = 0;
    private boolean autoX = false;
    private boolean autoY = false;
    private boolean autoAddr = false;

    private void writeWord(int address, int data) {
        bus.writeDev(address, data >> 8);
        bus.writeDev(address+1, data & 0xff);
    }
    private int readWord(int address) {
        return (bus.readDev(address) << 8) | bus.readDev(address + 1);
    }

    private void readState() {
        x = readWord(0x28);
        y = readWord(0x2A);
        x %= ScreenBuffer.width;
        y %= ScreenBuffer.height;
        spriteAddr = readWord(0x2c);
        int autoData = bus.readDev(0x26);
        autoX = (autoData & 0b1) > 0;
        autoY = (autoData & 0b10) > 0;
        autoAddr = (autoData & 0b100) > 0;
    }

    private void auto(boolean pixel, boolean twoBpp, boolean flipX, boolean flipY) {

        if (pixel) {
            if (autoX) {
                writeWord(0x28, x + 1);
            }
            if (autoY) {
                writeWord(0x2A, y + 1);
            }

            readState();
            return;
        }
        if (autoX) {
            int sign = flipX ? -8 : 8;
            writeWord(0x28, x + sign);
        }
        if (autoY) {
            int sign = flipY ? -8 : 8;
            writeWord(0x2A, y + sign);
        }
        if (autoAddr) {
            writeWord(0x2C, spriteAddr + (twoBpp ? 16 : 8));
        }
        readState();
    }

    private UXNBus bus;
    private final ScreenBuffer buffer = new ScreenBuffer();

    public ScreenDeviceBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SCREEN_DEVICE_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    public void handleKey(char ch) {
        if (bus != null) {
            bus.queueEvent(new KeyEvent(ch));
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TextComponent(getLabel());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ScreenDeviceMenu(i, inventory, this, buffer);
    }

    @Override
    public void attemptAttach(UXNBus bus, Direction attachSide) {
        attach(bus);
    }

    public void setColors(int[] colors) {
        buffer.setColors(colors);
    }

    private void writePixel(boolean fill, int layer, boolean flipY, boolean flipX, byte color) {
        readState();
        if (fill) {
            int minX = flipX ? 0 : this.x;
            int maxX = flipX ? this.x : ScreenBuffer.width;
            int minY = flipY ? 0 : this.y;
            int maxY = flipY ? this.y : ScreenBuffer.height;
            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    buffer.setPixel(layer, x, y, color);
                }
            }
        } else {
            buffer.setPixel(layer, x, y, color);
            auto(true, false, flipX, flipY);
        }
    }
    private void writePixel(int v) {
        boolean fill  = (v & 0b10000000) > 0;
        int layer     = (v & 0b01000000) > 0 ? 1 : 0;
        boolean flipY = (v & 0b00100000) > 0;
        boolean flipX = (v & 0b00010000) > 0;
        byte color = (byte) (v & 0x3);
        writePixel(fill, layer, flipY, flipX, color);
    }
    private byte[] readSprite(boolean twoBpp, int address) {
        MemoryRegion memory = bus.getUxn().memory;
        if (twoBpp) {
            byte[] sprite = new byte[64];
            for (int b = 0; b < 8; b++) {
                int line1 = memory.readByte(address + b);
                int line2 = memory.readByte(address + b + 8);
                for (int col = 0; col < 8; col++) {
                    sprite[b * 8 + (7 - col)] = (byte) ((line1 & 1) | ((line2 & 1) << 1));
                    line1 >>= 1;
                    line2 >>= 1;
                }
            }
            return sprite;
        }
        byte[] sprite = new byte[64];
        for (int b = 0; b < 8; b++) {
            int line = memory.readByte(address + b);
            for (int col = 0; col < 8; col++) {
                sprite[b * 8 + (7 - col)] = (byte) (line & 1);
                line >>= 1;
            }
        }
        return sprite;
    }
    private void writeSpriteRaw(byte[] sprite, byte[] colMap, int layer, int x, int y, boolean flipX, boolean flipY) {
        for (int dy = 0; dy < 8; dy++) {
            for (int dx = 0; dx < 8; dx++) {
                int sourceX = flipX ? 7 - dx : dx;
                int sourceY = flipY ? 7 - dy : dy;
                int i = sourceY * 8 + sourceX;
                byte color = colMap[sprite[i]];
                buffer.setPixel(layer, x + dx, y + dy, color);
            }
        }
    }
    private void writeSprite(boolean twoBpp, int layer, boolean flipY, boolean flipX, int colorIdx) {
        int data = bus.readDev(0x26);
        readState();
        int length = data >> 4;
        int dx = autoX ? 8 : 0;
        int dy = autoY ? 8 : 0;
        int fx = flipX ? -1 : 1;
        int fy = flipY ? -1 : 1;
        int dyx = dy * fx;
        int dxy = dx * fy;
        int address = spriteAddr;
        int addressInc = autoAddr ? (twoBpp ? 16 : 8) : 0;
        byte[] colMap = blending[colorIdx];

        for (int i = 0; i <= length; i++) {
            byte[] sprite = readSprite(twoBpp, address);
            writeSpriteRaw(sprite, colMap, layer, x + dyx * i, y + dxy * i, flipX, flipY);
            address += addressInc;
        }
        if (autoX) writeWord(0x28, x + dx * fx);
        if (autoY) writeWord(0x2A, y + dy * fy);
        if (autoAddr) writeWord(0x2C, address);
    }
    private void writeSprite(int v) {
        boolean twoBpp    = (v & 0b10000000) > 0;
        int layer         = (v & 0b01000000) > 0 ? 1 : 0;
        boolean flipY     = (v & 0b00100000) > 0;
        boolean flipX     = (v & 0b00010000) > 0;
        byte color = (byte) (v & 0b00001111);
        writeSprite(twoBpp, layer, flipY, flipX, color);
    }
    private void writeScreen(int address) {
        int v = bus.readDev(address);
        switch (address) {
            case 0x2e -> writePixel(v);
            case 0x2f -> writeSprite(v);
        }
    }

    private void writeMouse(int address) {

    }
    @Override
    public void write(int address) {
        int port = (address & 0xF0) >> 4;
        if (port == SCREEN_PORT) writeScreen(address);
        else if (port == MOUSE_PORT) writeMouse(address);
    }
    private void readScreen(int address) {
        switch (address) {
            case 0x22, 0x23 -> {
                writeWord(0x22, ScreenBuffer.width);
            }
            case 0x24, 0x25 -> {
                writeWord(0x24, ScreenBuffer.height);
            }
        }
    }
    private void readMouse(int address) {

    }
    @Override
    public void read(int address) {
        int port = (address & 0xF0) >> 4;
        if (port == SCREEN_PORT) readScreen(address);
        else if (port == MOUSE_PORT) readMouse(address);
    }

    @Override
    public void attach(UXNBus bus) {
        this.bus = bus;
        bus.setDevice(MOUSE_PORT, this);
        bus.setDevice(SCREEN_PORT, this);
    }

    @Override
    public void detach(UXNBus bus) {
        this.bus = null;
        bus.setDevice(MOUSE_PORT, null);
        bus.setDevice(SCREEN_PORT, null);
    }

    @Override
    public String getLabel() {
        return "Screen Device";
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((ScreenDeviceBlockEntity)t).tick(level, blockPos, blockState);
    }
    private void tick(Level level, BlockPos pos, BlockState state) {
        if (this.bus != null && !this.bus.isPaused()) {
            for (int i = 0; i < 3; i++) {
                this.bus.queueEvent(new ScreenEvent(readWord(0x20)));
            }
        }
    }

    @Override
    public void handleMouseClick(int x, int y, int i) {
        if (this.bus == null) return;
        this.bus.queueEvent(new MouseClickEvent(x, y, i, true));
    }

    @Override
    public void handleMouseMove(int x, int y) {
        if (this.bus == null) return;
        this.bus.queueEvent(new MouseMoveEvent(x, y));
    }

    @Override
    public void handleMouseRelease(int x, int y, int i) {
        if (this.bus == null) return;
        this.bus.queueEvent(new MouseClickEvent(x, y, i, false));
    }
}

class ScreenEvent implements UXNEvent {
    private final int address;
    public ScreenEvent(int address) {
        this.address = address;
    }
    @Override
    public void handle(UXNBus bus) {
        bus.getUxn().pc = address;
    }
}

class MouseMoveEvent implements UXNEvent {
    private final int x;
    private final int y;

    MouseMoveEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void handle(UXNBus bus) {
        bus.writeDevWord(0x92, x);
        bus.writeDevWord(0x94, y);
        bus.getUxn().pc = bus.readDevWord(0x90);
    }
}

class MouseClickEvent implements UXNEvent {
    private final int x;
    private final int y;
    private final int button;
    private final boolean press;

    MouseClickEvent(int x, int y, int button, boolean press) {
        this.x = x;
        this.y = y;
        this.button = button;
        this.press = press;
    }

    @Override
    public void handle(UXNBus bus) {
        bus.writeDevWord(0x92, x);
        bus.writeDevWord(0x94, y);
        int state = bus.readDev(0x96);
        if (press) {
            state |= 1 << button;
        } else {
            state &= ~(1 << button);
        }
        bus.writeDev(0x96, state);
        bus.getUxn().pc = bus.readDevWord(0x90);
    }
}