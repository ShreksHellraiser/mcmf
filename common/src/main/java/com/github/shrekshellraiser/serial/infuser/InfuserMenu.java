package com.github.shrekshellraiser.serial.infuser;

import com.github.shrekshellraiser.ModMenus;
import com.github.shrekshellraiser.computer.screen.FlashableMemorySlot;
import com.github.shrekshellraiser.devices.flasher.FlasherDeviceBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;

public class InfuserMenu extends AbstractContainerMenu {
    public InfuserBlockEntity blockEntity;
    public ContainerData data;

    public InfuserMenu(int i, Inventory inventory) {
        this(i, inventory, null, new SimpleContainerData(2));
    }

    public InfuserMenu(int i, Inventory inventory, InfuserBlockEntity bentity, ContainerData data) {
        super(ModMenus.INFUSER_MENU.get(), i);
//        checkContainerSize(bentity, 1);
        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
        blockEntity = bentity;
        this.data = data;
        addDataSlots(this.data);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private static final int INV_Y = 84;
    private static final int INV_X = 8;
    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, INV_X + l * 18, INV_Y + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, INV_X + i * 18, INV_Y+58));
        }
    }

    public int getScaledProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1);  // Max Progress
        int progressArrowSize = 26; // This is the width in pixels of your arrow

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }
}
