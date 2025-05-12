package com.github.shrekshellraiser.serial.terminal;

import com.github.shrekshellraiser.network.KeyInputHandler;
import com.github.shrekshellraiser.api.serial.SerialType;
import com.github.shrekshellraiser.api.serial.SerialPeerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import static com.github.shrekshellraiser.ModBlockEntities.SERIAL_TERMINAL_BLOCK_ENTITY;
import static com.github.shrekshellraiser.devices.serial.SerialDeviceBlockEntity.ARGUMENT_MODE_SEQUENCE;

public class SerialTerminalBlockEntity extends SerialPeerBlockEntity implements MenuProvider, KeyInputHandler {
    // Terminal Width = 240
    // Terminal Height = 180
    public final static int TERM_WIDTH = 240;
    public final static int TERM_HEIGHT = 180;
    public int width;
    public int height;
    public int dataSize;
    public TerminalFont font;
    public boolean echo = true;
    private final ContainerData data = new SerialTerminalContainerData(this);
    public TerminalBuffer buffer = new TerminalBuffer();
    private boolean argumentMode = false;

    private int argumentModeIndex = 0;

    public void setFont(TerminalFont font) {
        this.font = font;
        width = TERM_WIDTH / (font.width + font.hpad);
        height = TERM_HEIGHT / font.height;
        dataSize = width * height;
        buffer.setResolution(width, height);
    }
    public void nextFont() {
        setFont(font.next());
    }

    public SerialTerminalBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SERIAL_TERMINAL_BLOCK_ENTITY.get(), blockPos, blockState);
        setFont(TerminalFont.TIMES9K);
    }
    @Override
    protected Component getDefaultName() {
        return new TextComponent("Serial Terminal");
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new SerialTerminalMenu(i, inventory, this, this.data);
    }


    @Override
    public void write(char ch) {
        write(ch, SerialType.STDIN);
    }

    @Override
    public void write(char ch, SerialType type) {
        if (ch == 0x07) {
            // TODO bell
        }
        if (!argumentMode && ch == ARGUMENT_MODE_SEQUENCE.charAt(argumentModeIndex)) {
            argumentModeIndex++;
            if (argumentModeIndex == ARGUMENT_MODE_SEQUENCE.length()) {
                argumentMode = true;
                argumentModeIndex = 0;
            }
        } else {
            argumentMode = false;
            argumentModeIndex = 0;
        }
        buffer.write(ch);
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return null;
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        return null;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int i) {
        return null;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {

    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {

    }

    private void onLineEnd() {
        argumentMode = false;
    }

    public void keyPress(char i) {
    }

    @Override
    public void handleKey(char ch) {
        if (echo || argumentMode) write(ch);
        if (peer != null) peer.write(ch);
        if (ch == '\n') {
            onLineEnd();
        }
    }
}
