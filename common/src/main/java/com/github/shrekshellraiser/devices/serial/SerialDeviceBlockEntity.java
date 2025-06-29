package com.github.shrekshellraiser.devices.serial;

import com.github.shrekshellraiser.api.devices.GenericDeviceBlockEntity;
import com.github.shrekshellraiser.api.serial.ISerialPeer;
import com.github.shrekshellraiser.core.uxn.UXNBus;
import com.github.shrekshellraiser.core.uxn.ConsoleEvent;
import com.github.shrekshellraiser.api.serial.SerialType;
import com.github.shrekshellraiser.api.serial.SerialPeerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.state.BlockState;

import static com.github.shrekshellraiser.ModBlockEntities.SERIAL_DEVICE_BLOCK_ENTITY;

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

    @Override
    public void writePeer(char data) {
        if (this.peer != null) {
            this.peer.write(data);
        }
    }

    private void rawWrite(char ch, SerialType type) {
        if (bus != null) {
            byte typeB = (byte) type.value;
            int deviceB = deviceNumber << 4;
            bus.queueEvent(new ConsoleEvent(ch, typeB, deviceB));
        }
    }

    @Override
    public void write(char ch) {
        if (argumentMode) {
            if (ch == '\n') {
                argumentMode = false;
                rawWrite('\0', SerialType.ARGUMENT_END);
            } else {
                parseArguments(ch);
            }
        } else {
            rawWrite(ch, SerialType.STDIN);
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

