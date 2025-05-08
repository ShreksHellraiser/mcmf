package com.github.shrekshellraiser.devices.block.entities;

import com.github.shrekshellraiser.computer.block.entity.IBusProvider;
import com.github.shrekshellraiser.core.uxn.UXNBus;
import com.github.shrekshellraiser.devices.block.MultiplexerDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.state.BlockState;

import static com.github.shrekshellraiser.ModBlockEntities.MULTIPLEXER_DEVICE_BLOCK_ENTITY;


public class MultiplexerDeviceBlockEntity extends GenericDeviceBlockEntity implements IBusProvider {
    private final UXNBus bus;

    public MultiplexerDeviceBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(MULTIPLEXER_DEVICE_BLOCK_ENTITY.get(), blockPos, blockState);
        deviceNumber = 15;
        bus = new UXNBus(this);
    }

    @Override
    public void write(int address) {
        UXNBus parent = bus.getParent();
        if (parent == null) return;
        int data = parent.readDev(address);
        int port = address & 0x0F;
        int dev = address & 0xF0;
        switch (port) {
            case 0x00 -> {
                // Set the multiplexed device address (do nothing)
            }
            case 0x01, 0x02 -> {
                // Write to the multiplexed device address
                int mAddress = parent.readDev(dev);
                bus.deo(mAddress + port - 1, (byte)data);
            }
        }

    }

    @Override
    public void read(int address) {
        UXNBus parent = bus.getParent();
        if (parent == null) return;
        int port = address & 0x0F;
        int dev = address & 0xF0;
        switch (port) {
            case 0x01, 0x02 -> {
                int mAddress = parent.readDev(dev);
                int data = bus.dei(mAddress + port - 1);
                parent.writeDev(address, data);
            }
        }
    }

    @Override
    public void attach(UXNBus manager) {
        super.attach(manager);
        bus.setParent(manager);
        bus.refresh();
        bus.erase();
    }

    @Override
    public void detach(UXNBus bus) {
        super.detach(bus);
        bus.setParent(null);
    }

    @Override
    public String getLabel() {
        return "Multiplexer Device";
    }

    @Override
    protected Component getDefaultName() {
        return new TextComponent(getLabel());
    }

    @Override
    public void attemptAttach(UXNBus bus, Direction attachSide) {
        Direction facing = this.getBlockState().getValue(MultiplexerDeviceBlock.FACING);
        if (!facing.getOpposite().equals(attachSide)) {
            return;
        }
        attach(bus);
    }

    @Override
    public UXNBus getBus(Direction dir) {
        Direction facing = this.getBlockState().getValue(MultiplexerDeviceBlock.FACING);
        if (!facing.equals(dir)) {
            return null;
        }
        return bus;
    }

    @Override
    public BlockPos getScanRoot() {
        Direction facing = this.getBlockState().getValue(MultiplexerDeviceBlock.FACING);
        return getBlockPos().relative(facing);
    }

    @Override
    public Direction getScanStartDir() {
        Direction facing = this.getBlockState().getValue(MultiplexerDeviceBlock.FACING);
        return facing.getOpposite();
    }
}
