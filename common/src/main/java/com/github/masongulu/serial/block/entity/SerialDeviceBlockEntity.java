package com.github.masongulu.serial.block.entity;

import com.github.masongulu.devices.block.entities.GenericDeviceBlockEntity;
import com.github.masongulu.serial.ISerialPeer;
import com.github.masongulu.core.uxn.UXNBus;
import com.github.masongulu.core.uxn.KeyEvent;
import com.github.masongulu.core.uxn.devices.IDevice;
import com.github.masongulu.serial.SerialType;
import com.github.masongulu.serial.block.SerialDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.state.BlockState;

import static com.github.masongulu.ModBlockEntities.SERIAL_DEVICE_BLOCK_ENTITY;

public class SerialDeviceBlockEntity extends GenericDeviceBlockEntity implements ISerialPeer {
    private ISerialPeer peer;
    private boolean conflicting = false;
    private boolean argumentMode = false;
    public static final String ARGUMENT_MODE_SEQUENCE = "\0\n\nARGS?>\5";
    public SerialDeviceBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SERIAL_DEVICE_BLOCK_ENTITY.get(), blockPos, blockState);
        deviceNumber = 1;
    }

    @Override
    public void attemptAttach(UXNBus bus, Direction attachSide) {
        Direction facing = this.getBlockState().getValue(SerialDeviceBlock.FACING);
        if (!facing.getOpposite().equals(attachSide)) {
            return;
        }
        attach(bus);
    }

    @Override
    protected Component getDefaultName() {
        return new TextComponent("Serial Device");
    }

    @Override
    public void write(int address) {
        int data = bus.readDev(address);
        int port = address & 0x0F;
        switch (port) {
            case 0x08 -> {
                writePeer((char) data);
            }
            case 0x09 -> {
                // TODO fix this
                System.err.write((char) data);
            }
        }
    }

    @Override
    public void read(int address) {
    }

    @Override
    public void attach(UXNBus bus) {
        this.bus = bus;
        this.bus.setDevice(deviceNumber, this);
        SerialPeerBlockEntity.refresh(this, this.getLevel(), this.getBlockPos());
    }

    @Override
    public void detach(UXNBus bus) {
        bus.deleteDevice(deviceNumber);
        this.bus = null;
    }

    @Override
    public String getLabel() {
        return "Serial Adapter";
    }

    @Override
    public void attach(ISerialPeer bus) {
        peer = bus;
    }

    @Override
    public void detach(ISerialPeer bus) {
        peer = null;
    }


    ArgumentParseState parseState;
    private enum ArgumentParseState {
        ARGUMENT,
        QUOTE_SEARCHING,
        WHITESPACE_SKIPPING
    }
    private void setParseState(ArgumentParseState state) {
        if (parseState == ArgumentParseState.WHITESPACE_SKIPPING && state != parseState) {
            rawWrite('\0', SerialType.ARGUMENT_SPACER);
        }
        parseState = state;
    }
    private void parseArguments(char ch) {
        switch (parseState) {
            case ARGUMENT -> {
                if (ch == ' ') {
                    setParseState(ArgumentParseState.WHITESPACE_SKIPPING);
                } else if (ch == '"') {
                    setParseState(ArgumentParseState.QUOTE_SEARCHING);
                } else {
                    rawWrite(ch, SerialType.ARGUMENT);
                }
            }
            case QUOTE_SEARCHING -> {
                if (ch == '"') {
                    setParseState(ArgumentParseState.WHITESPACE_SKIPPING);
                } else {
                    rawWrite(ch, SerialType.ARGUMENT);
                }
            }
            case WHITESPACE_SKIPPING -> {
                if (ch == '"') {
                    setParseState(ArgumentParseState.QUOTE_SEARCHING);
                } else if (ch != ' ') {
                    setParseState(ArgumentParseState.ARGUMENT);
                    rawWrite(ch, SerialType.ARGUMENT);
                }
            }
        }
    }


    private void writePeer(char data) {
        if (this.peer != null) {
            this.peer.write(data);
        }
    }

    private void rawWrite(char ch, SerialType type) {
        if (bus != null) {
            byte typeB = (byte) type.value;
            int deviceB = deviceNumber << 4;
            bus.queueEvent(new KeyEvent(ch, typeB, deviceB));
        }
    }

    @Override
    public void write(char ch) {
        write(ch, SerialType.STDIN);
    }

    @Override
    public void write(char ch, SerialType type) {
        if (argumentMode) {
            if (ch == '\n') {
                argumentMode = false;
                rawWrite('\0', SerialType.ARGUMENT_END);
            } else {
                parseArguments(ch);
            }
        } else {
            rawWrite(ch, type);
        }
    }

    public void requestArgument() {
        argumentMode = true;
        parseState = ArgumentParseState.ARGUMENT;
        if (peer != null) {
            for (char ch : ARGUMENT_MODE_SEQUENCE.toCharArray()) {
                peer.write(ch);
            }
        }
    }

    @Override
    public void setConflicting(boolean b) {
        conflicting = b;
    }

    @Override
    public ISerialPeer getPeer() {
        return peer;
    }

    public BlockPos getPos() {
        return this.getBlockPos();
    }
}

