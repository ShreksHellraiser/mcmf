package com.github.shrekshellraiser.devices.flasher;

import com.github.shrekshellraiser.core.uxn.FileDeviceWrapper;
import com.github.shrekshellraiser.core.uxn.UXNBus;
import com.github.shrekshellraiser.api.devices.GenericDeviceBlockEntity;
import com.github.shrekshellraiser.item.memory.FileManager;
import com.github.shrekshellraiser.item.memory.MemoryItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.charset.StandardCharsets;

import static com.github.shrekshellraiser.ModBlockEntities.FLASHER_DEVICE_BLOCK_ENTITY;

public class FlasherDeviceBlockEntity extends GenericDeviceBlockEntity {
    private NonNullList<ItemStack> items;
    public static final int INVENTORY_SIZE = 1;
    private final SingleFileFilesystem filesystem = new SingleFileFilesystem();

    public FlasherDeviceBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(FLASHER_DEVICE_BLOCK_ENTITY.get(), blockPos, blockState);
        deviceNumber = 13;
        this.items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    private void tick(Level level, BlockPos pos, BlockState state) {
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((FlasherDeviceBlockEntity)t).tick(level, blockPos, blockState);
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new FlasherDeviceMenu(i, inventory, this, this.data, this);
    }

    @Override
    public void write(int address) {
        int port = address & 0x0F;
        switch(port) {
        }
    }

    @Override
    public void read(int address) {
        int port = address & 0x0F;
        switch(port) {
        }
    }

    @Override
    public String getLabel() {
        return "Flasher Device";
    }

    @Override
    protected Component getDefaultName() {
        return new TextComponent("Flasher Device");
    }

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemStack = ContainerHelper.removeItem(this.items, i, j);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }

        return itemStack;
    }

    @Override
    public void attach(UXNBus bus) {
        bus.getFileDeviceWrapper().registerDevice(deviceNumber);
        bus.getFileDeviceWrapper().registerFilesystem(filesystem);
    }

    @Override
    public void detach(UXNBus bus) {
    }

    @Override
    public void setDeviceNumber(int i) {
        super.setDeviceNumber(i);
        filesystem.setDeviceNumber(i);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(this.items, i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.items.set(i, itemStack);
        if (itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }

        if (itemStack.getItem() instanceof MemoryItem item && item.isFlashable()) {
            String fn = item.getLabel(itemStack);
            byte[] data = FileManager.readFile("srom", item.getUUID(itemStack));
            assert data != null;
            filesystem.setContents(new String(data, StandardCharsets.UTF_8), fn);
        }

        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        assert this.level != null;
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(player.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5) > 64.0);
        }
    }


    @Override
    public void clearContent() {
        this.items.clear();
    }


    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.items);
    }

    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        ContainerHelper.saveAllItems(compoundTag, this.items);
    }
}