package com.github.shrekshellraiser.devices.multiplexer;

import com.github.shrekshellraiser.computer.block.entity.IBusProvider;
import com.github.shrekshellraiser.core.uxn.UXNBus;
import com.github.shrekshellraiser.api.devices.GenericDeviceBlockEntity;
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

    private void writeToMultiplexedDeviceAddress(UXNBus parent, int dev, int port, int data, int offset) {
        int mAddress = parent.readDev(dev + offset - 1);
        bus.deo(mAddress + port - offset, (byte)data);
    }
    @Override
    public void write(int address) {
        UXNBus parent = bus.getParent();
        if (parent == null) return;
        int data = parent.readDev(address);
        int port = address & 0x0F;
        int dev = address & 0xF0;
        switch (port) {
            case 0x00, 0x03, 0x06, 0x09, 0x0c -> {
                // Set the multiplexed device address (do nothing)
            }
            case 0x01, 0x02 -> // Write to the multiplexed device address
                    writeToMultiplexedDeviceAddress(parent, dev, port, data, 1);
            case 0x04, 0x05 -> writeToMultiplexedDeviceAddress(parent, dev, port, data, 4);
            case 0x07, 0x08 -> writeToMultiplexedDeviceAddress(parent, dev, port, data, 7);
            case 0x0a, 0x0b -> writeToMultiplexedDeviceAddress(parent, dev, port, data, 0xa);
            case 0x0d, 0x0e -> writeToMultiplexedDeviceAddress(parent, dev, port, data, 0xd);
        }

    }

    private void readFromMultiplexedDeviceAddress(UXNBus parent, int dev, int port, int offset) {
        int mAddress = parent.readDev(dev + offset - 1);
        int data = bus.dei(mAddress + port - offset);
        parent.writeDev(dev | port, data);
    }
    @Override
    public void read(int address) {
        UXNBus parent = bus.getParent();
        if (parent == null) return;
        int port = address & 0x0F;
        int dev = address & 0xF0;
        switch (port) {
            case 0x01, 0x02 -> readFromMultiplexedDeviceAddress(parent, dev, port, 1);
            case 0x04, 0x05 -> readFromMultiplexedDeviceAddress(parent, dev, port, 4);
            case 0x07, 0x08 -> readFromMultiplexedDeviceAddress(parent, dev, port, 7);
            case 0x0a, 0x0b -> readFromMultiplexedDeviceAddress(parent, dev, port, 0xa);
            case 0x0d, 0x0e -> readFromMultiplexedDeviceAddress(parent, dev, port, 0xd);
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
    public boolean cableAttaches(Direction attachSide) {
        return super.cableAttaches(attachSide) || getBlockState().getValue(MultiplexerDeviceBlock.FACING).equals(attachSide);
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
