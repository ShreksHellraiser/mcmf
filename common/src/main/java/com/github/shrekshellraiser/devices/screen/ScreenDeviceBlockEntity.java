package com.github.shrekshellraiser.devices.screen;

import com.github.shrekshellraiser.api.devices.GenericDeviceBlock;
import com.github.shrekshellraiser.core.uxn.*;
import com.github.shrekshellraiser.api.devices.IAttachableDevice;
import com.github.shrekshellraiser.api.devices.IDevice;
import com.github.shrekshellraiser.network.KeyInputHandler;
import com.github.shrekshellraiser.network.MouseInputHandler;
import com.github.shrekshellraiser.network.ScreenUpdatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

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
    protected boolean runningScreenVector = false;

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
        spriteAddr = readWord(0x2c);
        int autoData = bus.readDev(0x26);
        autoX = (autoData & 0b1) > 0;
        autoY = (autoData & 0b10) > 0;
        autoAddr = (autoData & 0b100) > 0;
    }

    private void auto(boolean flipX, boolean flipY) {
        if (autoX) {
            writeWord(0x28, x + 1);
        }
        if (autoY) {
            writeWord(0x2A, y + 1);
        }

        readState();
    }

    private UXNBus bus;
    private final ScreenBuffer buffer = new ScreenBuffer();
    private final ScreenUpdatePacket packet = new ScreenUpdatePacket(buffer);

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
        screenRefreshed = true; // send packet to newly opened screen
        return new ScreenDeviceMenu(i, inventory, this, null);
    }

    @Override
    public boolean cableAttaches(Direction attachSide) {
        Direction facing = this.getBlockState().getValue(ScreenDeviceBlock.FACING);
        return facing.getOpposite().equals(attachSide);
    }

    @Override
    public void attemptAttach(UXNBus bus, Direction attachSide) {
        if (cableAttaches(attachSide)) {
            attach(bus);
        }
    }

    public void setColors(int[] colors) {
        buffer.setColors(colors);
    }

    private boolean screenDirty = false;
    private void writePixel(boolean fill, int layer, boolean flipY, boolean flipX, byte color) {
        readState();
        if (fill) {
            int minX = flipX ? 0 : this.x;
            int maxX = flipX ? this.x : buffer.getWidth();
            int minY = flipY ? 0 : this.y;
            int maxY = flipY ? this.y : buffer.getHeight();
            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    buffer.setPixel(layer, x, y, color);
                }
            }
        } else {
            buffer.setPixel(layer, x, y, color);
            auto(flipX, flipY);
        }
        screenDirty = true;
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
        screenDirty = true;
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
            case 0x23 -> buffer.setWidth(bus.readDevWord(0x22));
            case 0x25 -> buffer.setHeight(bus.readDevWord(0x24));
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
                writeWord(0x22, buffer.getWidth());
            }
            case 0x24, 0x25 -> {
                writeWord(0x24, buffer.getHeight());
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

    private boolean screenRefreshed = false;
    public void refresh() {
        packet.refresh();
        screenDirty = false;
        screenRefreshed = true;
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        if (!(level instanceof ServerLevel)) return;
        ((ScreenDeviceBlockEntity)t).tick((ServerLevel) level, blockPos, blockState);
    }
    private boolean mouseActive = false;
    private int mouseX = 0;
    private int lastMouseX = -100;
    private int lastMouseY = -100;
    private int mouseY = 0;
    private int mouseButton = 0;
    private void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (this.bus != null && !this.bus.isPaused()) {
            if (mouseActive) {
                this.bus.queueEvent(new MouseEvent(mouseX, mouseY, mouseButton));
                mouseActive = false;
            }
            for (int i = 0; i < 3; i++) {
                this.bus.queueEvent(new ScreenEvent(readWord(0x20), this));
            }
            if (screenDirty & !runningScreenVector) refresh();
            if (screenRefreshed) {
                ArrayList<ServerPlayer> players = new ArrayList<>();
                for (ServerPlayer p : level.players()) {
                    if (p.containerMenu instanceof ScreenDeviceMenu m && m.getBlockEntity() == this) {
                        players.add(p);
                    }
                }
                packet.send(players);
                screenRefreshed = false;
            }
        }
    }

    private static final int MIN_THINNING_THRESHOLD = 5;
    private static final int MAX_THINNING_THRESHOLD = 50;
    private static final int THINNING_DIFF = MAX_THINNING_THRESHOLD - MIN_THINNING_THRESHOLD;
    private void queueMouseEvent(int x, int y, int state) {
        if (mouseX == x && mouseY == y && mouseButton == state) return;
        mouseX = x;
        mouseY = y;
        mouseButton = state;
        mouseActive = true;
        float congestion = this.bus.getCongestion();
        int THINNING_THRESHOLD = MIN_THINNING_THRESHOLD + (int) (THINNING_DIFF * congestion);
        if (congestion > 0.7f) return;
        if (Math.abs(mouseX - lastMouseX) > THINNING_THRESHOLD || Math.abs(lastMouseY - mouseY) > THINNING_THRESHOLD) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            this.bus.queueEvent(new MouseEvent(x, y, mouseButton));
        }
    }

    @Override
    public void handleMouseClick(int x, int y, int i) {
        if (this.bus == null) return;
        queueMouseEvent(x, y, mouseButton | 1 << i);
    }

    @Override
    public void handleMouseMove(int x, int y) {
        if (this.bus == null) return;
        queueMouseEvent(x, y, mouseButton);
    }

    @Override
    public void handleMouseRelease(int x, int y, int i) {
        if (this.bus == null) return;
        queueMouseEvent(x, y, mouseButton & ~(1 << i));
    }

}

class ScreenEvent implements UXNEvent {
    private final int address;
    private final ScreenDeviceBlockEntity blockEntity;
    public ScreenEvent(int address, ScreenDeviceBlockEntity blockEntity) {
        this.address = address;
        this.blockEntity = blockEntity;
    }
    @Override
    public void handle(UXNBus bus) {
        bus.getUxn().pc = address;
        this.blockEntity.runningScreenVector = true;
    }

    @Override
    public void post(UXNBus bus) {
        this.blockEntity.runningScreenVector = false;
        this.blockEntity.refresh();
    }
}

class MouseEvent extends BasicUXNEvent {
    private final int x;
    private final int y;
    private final int state;

    MouseEvent(int x, int y, int state) {
        this.x = x;
        this.y = y;
        this.state = state;
    }

    @Override
    public void handle(UXNBus bus) {
        bus.writeDevWord(0x92, x);
        bus.writeDevWord(0x94, y);
        bus.writeDev(0x96, state);
        bus.getUxn().pc = bus.readDevWord(0x90);
    }
}