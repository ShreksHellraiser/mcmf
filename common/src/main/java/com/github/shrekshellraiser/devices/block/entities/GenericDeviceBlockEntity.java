package com.github.shrekshellraiser.devices.block.entities;

import com.github.shrekshellraiser.core.uxn.devices.IAttachableDevice;
import com.github.shrekshellraiser.devices.screen.GenericDeviceMenu;
import com.github.shrekshellraiser.core.uxn.UXNBus;
import com.github.shrekshellraiser.core.uxn.devices.IDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class GenericDeviceBlockEntity extends BaseContainerBlockEntity implements MenuProvider, IAttachableDevice, IDevice {
    public int deviceNumber;
    private final static String DEVICE_NUMBER_TAG = "mcmf.device_number";

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putByte(DEVICE_NUMBER_TAG, (byte)deviceNumber);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.contains(DEVICE_NUMBER_TAG)) {
            deviceNumber = compoundTag.getByte(DEVICE_NUMBER_TAG);
        }
    }

    protected UXNBus bus;
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return deviceNumber;
        }

        @Override
        public void set(int i, int j) {
            setDeviceNumber(j);
        }

        @Override
        public int getCount() {
            return 1;
        }
    };
    protected GenericDeviceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new GenericDeviceMenu(i, inventory, this, this.data, this);
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
    public ItemStack removeItemNoUpdate(int i) {
        return null;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {

    }


    @Override
    public void attach(UXNBus manager) {
        bus = manager;
        manager.setDevice(deviceNumber, this);
    }

    @Override
    public void detach(UXNBus bus) {
        bus.deleteDevice(deviceNumber);
        this.bus = null;
    }

    public void setDeviceNumber(int i) {
        UXNBus bus = this.bus;
        if (bus != null) {
            detach(bus);
        }
        deviceNumber = i;
        if (bus != null) {
            bus.refresh();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {

    }
}
