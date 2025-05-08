package com.github.shrekshellraiser.devices.screen;

import com.github.shrekshellraiser.ModMenus;
import com.github.shrekshellraiser.devices.block.entities.ScreenDeviceBlockEntity;
import com.github.shrekshellraiser.network.KeyInputHandler;
import com.github.shrekshellraiser.network.MouseInputHandler;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class ScreenDeviceMenu extends AbstractContainerMenu implements KeyInputHandler, MouseInputHandler {
    private ScreenDeviceBlockEntity blockEntity;
    public ScreenBuffer buffer;
    public ContainerData data;
    public ScreenDeviceMenu(int i, Inventory inventory) {
        this(i, inventory, null, new SimpleContainerData(ScreenBuffer.BUFFER_SIZE));
    }
    public ScreenDeviceMenu(int i, Inventory inventory, ScreenDeviceBlockEntity termBlockEntity, ContainerData data) {
        super(ModMenus.SCREEN_DEVICE_MENU.get(), i);
        blockEntity = termBlockEntity;
        this.data = data;
        if (data instanceof ScreenBuffer buf) {
            this.buffer = buf;
        } else {
            this.buffer = new ScreenBuffer(data);
        }
        addDataSlots(this.data);
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
}
