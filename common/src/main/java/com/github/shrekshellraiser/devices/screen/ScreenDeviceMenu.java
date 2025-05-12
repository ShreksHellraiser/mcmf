package com.github.shrekshellraiser.devices.screen;

import com.github.shrekshellraiser.ModMenus;
import com.github.shrekshellraiser.network.KeyInputHandler;
import com.github.shrekshellraiser.network.MouseInputHandler;
import com.github.shrekshellraiser.network.ScreenBufferHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;

public class ScreenDeviceMenu extends AbstractContainerMenu implements KeyInputHandler, MouseInputHandler, ScreenBufferHandler {
    private final ScreenDeviceBlockEntity blockEntity;
    public ScreenBuffer buffer;
    public ContainerData data;
    public ScreenDeviceMenu(int i, Inventory inventory) {
        this(i, inventory, null, null);
    }
    public ScreenDeviceMenu(int i, Inventory inventory, ScreenDeviceBlockEntity termBlockEntity, ContainerData data) {
        super(ModMenus.SCREEN_DEVICE_MENU.get(), i);
        blockEntity = termBlockEntity;
        this.buffer = new ScreenBuffer();
    }
    public ScreenDeviceBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public void setData(int i, int j) {
        super.setData(i, j);
        this.broadcastChanges();
    }

    @Override
    public void handleKey(char ch) {
        blockEntity.handleKey(ch);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }


    @Override
    public void handleMouseClick(int x, int y, int i) {
        blockEntity.handleMouseClick(x, y, i);
    }

    @Override
    public void handleMouseRelease(int x, int y, int i) {
        blockEntity.handleMouseRelease(x, y, i);
    }

    @Override
    public void handleMouseMove(int x, int y) {
        blockEntity.handleMouseMove(x, y);
    }

    @Override
    public void screenBufferUpdate(FriendlyByteBuf buffer) {
        this.buffer.decode(buffer);
    }
}
