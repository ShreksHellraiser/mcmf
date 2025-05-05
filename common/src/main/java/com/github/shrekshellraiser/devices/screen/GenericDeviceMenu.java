package com.github.shrekshellraiser.devices.screen;

import com.github.shrekshellraiser.ModMenus;
import com.github.shrekshellraiser.devices.block.entities.GenericDeviceBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public class GenericDeviceMenu extends AbstractContainerMenu {
    public GenericDeviceBlockEntity blockEntity;
    public ContainerData data;

    public GenericDeviceMenu(int i, Inventory inventory) {
        this(i, inventory, null, new SimpleContainerData(1),
                new SimpleContainer(1));
    }

    public GenericDeviceMenu(int i, Inventory inventory, GenericDeviceBlockEntity bentity, ContainerData data, Container container) {
        this(i, inventory, bentity, data, container, ModMenus.GENERIC_DEVICE_MENU.get());
    }

    protected GenericDeviceMenu(int i, Inventory inventory, GenericDeviceBlockEntity bentity, ContainerData data, Container container, MenuType<?> menuType){
        super(menuType, i);
        blockEntity = bentity;
        this.data = data;
        addDataSlots(this.data);
    }

    protected void addDataSlots(ContainerData containerData) {
        for(int i = 0; i < containerData.getCount(); ++i) {
            this.addDataSlot(DataSlot.forContainer(containerData, i));
        }
    }

    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack itemStack = ItemStack.EMPTY;
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
        setData(0, i);
        return true;
    }
}
