package com.github.shrekshellraiser.computer.screen;

import com.github.shrekshellraiser.ModMenus;
import com.github.shrekshellraiser.computer.block.entity.ComputerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public class ComputerMenu extends AbstractContainerMenu {
    public ComputerBlockEntity blockEntity;
    public ContainerData data;

    private String getString(int o) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < ComputerBlockEntity.STRING_LENGTH; i++) {
            s.append((char)data.get(o + i));
        }
        return s.toString();
    }
    public String getRst() {
        return getString(0);
    }
    public String getWst() {
        return getString(ComputerBlockEntity.STRING_LENGTH);
    }

    public boolean isRunning() {
        return data.get(ComputerBlockEntity.DATA_START) == 1;
    }

    public boolean isPaused() {
        return data.get(ComputerBlockEntity.DATA_START + 2) == 1;
    }

    public int getPc() {
        return data.get(ComputerBlockEntity.DATA_START + 1);
    }

    public int getVectors() {
        return data.get(ComputerBlockEntity.DATA_START + 3);
    }

    public ComputerMenu(int i, Inventory inventory) {
        this(i, inventory, null, new SimpleContainerData(ComputerBlockEntity.DATA_LENGTH),
                new SimpleContainer(ComputerBlockEntity.CONTAINER_SIZE));
    }

    public ComputerMenu(int i, Inventory inventory, ComputerBlockEntity computerBlockEntity, ContainerData data, Container container) {
        super(ModMenus.COMPUTER_MENU.get(), i);
        checkContainerSize(container, ComputerBlockEntity.CONTAINER_SIZE);
        blockEntity = computerBlockEntity;
        this.data = data;
        addDataSlots(this.data);
        this.addSlot(new MemorySlot(container, 0, 134, 88 + ComputerScreen.topOffset));
        this.addSlot(new BeetSlot(container, 1, 80, 4));
        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
    }
    protected void addDataSlots(ContainerData containerData) {
        for(int i = 0; i < containerData.getCount(); ++i) {
            this.addDataSlot(DataSlot.forContainer(containerData, i));
        }
    }
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
//        Slot slot = this.slots.get(i);
//        if (slot != null && slot.hasItem()) {
//            ItemStack itemStack2 = slot.getItem();
//            itemStack = itemStack2.copy();
//        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public void setData(int i, int j) {
        super.setData(i, j);
        this.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int i) {
        if (i == 0) {
            blockEntity.togglePower(player);
        } else if (i == 1) {
            blockEntity.togglePause(player);
        } else if (i == 2) {
            // step
            blockEntity.step(player);
        } else if (i == 3) {
            blockEntity.toggleArgumentMode();
        }
        return true;
    }

    private static final int invX = 8;
    private static final int invY = 124 + ComputerScreen.topOffset;
    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, invX + l * 18, invY + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 124+58+ComputerScreen.topOffset));
        }
    }

    public boolean isArgumentMode() {
        return data.get(ComputerBlockEntity.DATA_START + 4) == 1;
    }
}
