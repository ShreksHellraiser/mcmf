package com.github.masongulu.core.uxn;

import com.github.masongulu.ComputerMod;
import com.github.masongulu.computer.block.entity.ComputerBlockEntity;
import com.github.masongulu.item.memory.MemoryItem;
import com.github.masongulu.core.uxn.devices.IDevice;
import com.github.masongulu.core.uxn.devices.IDeviceProvider;
import com.github.masongulu.serial.ISerialPeer;
import com.github.masongulu.serial.block.entity.SerialDeviceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.github.masongulu.ModBlocks.DEVICE_CABLE;

public class UXNBus {
    public UXN uxn;
    private final IDevice[] devices = new IDevice[16];
    private final Set<IDevice> deviceSet = new HashSet<>();
    private boolean executing = false;
    private boolean conflicting = false;
    private boolean paused = false;
    private boolean argumentMode = false;
    private boolean expectingArgument = false;

    private final ComputerBlockEntity computerEntity;
    private final byte[] deviceMemory = new byte[256];

    public UXNBus(ComputerBlockEntity computerEntity) {
        this.computerEntity = computerEntity;
    }

    /*
    This returns a list of all blocks around the starting position
    plus all blocks around any blocks tagged DEVICE_CABLE
     */
    public static ArrayList<BlockPos> traverse(Level level, BlockPos blockPos, BlockPos ignore) {
        java.util.Stack<BlockPos> toSearch = new java.util.Stack<>();
        toSearch.push(blockPos);
        ArrayList<BlockPos> visited = new ArrayList<>();
        while (!toSearch.isEmpty()) {
            BlockPos pos = toSearch.pop();
            // Add surrounding blocks to search
            for (Direction direction : Direction.values()) {
                BlockPos newPos = pos.relative(direction);
                if (visited.contains(newPos)) continue;
                if (newPos.equals(ignore)) continue;
                visited.add(newPos);
                BlockState state = level.getBlockState(newPos);
                if (state.is(DEVICE_CABLE)) {
                    toSearch.push(newPos);
                }
            }
        }
        return visited;
    }
    public static ArrayList<BlockPos> traverse(Level level, BlockPos blockPos) {
        return traverse(level, blockPos, null);
    }

    public static UXNBus findBus(Level level, BlockPos blockPos) {
        ArrayList<BlockPos> traversed = traverse(level, blockPos);
        for (BlockPos pos : traversed) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity == null)
                continue;
            if (entity instanceof ComputerBlockEntity computerBlockEntity) {
                return computerBlockEntity.getBus();
            }
        }
        return null;
    }

    public void refresh(Level level, BlockPos blockPos, BlockPos ignore) {
        ArrayList<BlockPos> traversed = traverse(level, blockPos, ignore);
        Set<IDevice> foundDevices = new HashSet<>();
        this.conflicting = false;
        for (BlockPos pos : traversed) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity == null)
                continue;
            if (entity instanceof ComputerBlockEntity computerBlockEntity) {
                if (computerEntity != computerBlockEntity) {
                    this.conflicting = true;
                    // TODO flesh out this conflicting thing more
                }
            } else if (entity instanceof IDeviceProvider deviceProvider) {
                IDevice device = deviceProvider.getDevice(null); // TODO figure out how to get direction here
                if (device != null) {
                    foundDevices.add(device);
                }
            }
        }
        var newDevices = new HashSet<>(foundDevices);
        newDevices.removeAll(this.deviceSet);
        var oldDevices = new HashSet<>(this.deviceSet);
        oldDevices.removeAll(foundDevices);
        for (IDevice device : newDevices) {
            this.deviceSet.add(device);
            device.attach(this);
        }
        for (IDevice device : oldDevices) {
            this.deviceSet.remove(device);
            device.detach(this);
        }
    }
    public void refresh(Level level, BlockPos blockPos) {
        refresh(level, blockPos, null);
    }
    public void refresh() {
        refresh(this.computerEntity.getLevel(), this.computerEntity.getBlockPos());
    }
    public void refresh(BlockPos ignore) {
        refresh(this.computerEntity.getLevel(), this.computerEntity.getBlockPos(), ignore);
    }

    public void writeDev(int address, int data) {
        address %= 256;
        deviceMemory[address] = (byte)data;
    }

    public int readDev(int address) {
        address %= 256;
        return deviceMemory[address] & 0xFF;
    }

    public void setUxn(UXN uxn) {
        this.uxn = uxn;
    }

    public void startup() {
        if (executing || conflicting) {
            return;
        }
        ItemStack stack = this.computerEntity.getItem(0);
        if (stack.getItem() instanceof MemoryItem item) {
            MemoryRegion memory = item.getMemory(stack);
            refresh(this.computerEntity.getLevel(), this.computerEntity.getBlockPos());
            executing = true;
            new UXN(this, memory);
            uxn.paused = paused || argumentMode;    
            uxn.queueEvent(new BootEvent());
            if (argumentMode) {
                for (int dev = 0x1; dev <= 0xf; dev++) {
                    IDevice device = devices[dev];
                    if (device instanceof SerialDeviceBlockEntity sDevice && sDevice.getPeer() != null) {
                        sDevice.requestArgument();
                        expectingArgument = true;
                        break;
                    }
                }
            }
            ComputerMod.UXN_EXECUTOR.addUXN(uxn);
        }
    }

    public void pause(boolean state) {
        this.paused = state;
        if (uxn != null) {
            uxn.paused = state;
        }
    }
    public void pause() {
        pause(true);
    }

    public void shutdown() {
        ComputerMod.UXN_EXECUTOR.removeUXN(uxn);
        executing = false;
        uxn = null;
    }

    /*
    Set the device for a particular device number.
    This should be called by IDevice::attach
     */
    public void setDevice(int index, IDevice device) {
        devices[index] = device;
    }
    /*
    Delete the device for a particular device number.
    This should be called by IDevice::detach
     */
    public void deleteDevice(int index) {
        devices[index] = null;
    }

    public void deo(int address, byte data) {
        deviceMemory[address] = data;
        int dev = (address & 0xF0) >> 4;
        IDevice device = this.devices[dev];
        if (device != null) {
            device.write(address);
        }
    }

    public byte dei(int address) {
        int dev = (address & 0xF0) >> 4;
        IDevice device = this.devices[dev];
        if (device != null) {
            device.read(address);
        }
        return deviceMemory[address];
    }

    public IDevice getDevice(int index) {
        return devices[index];
    }

    public void invalidate() {
//        stop();
//        for (IDevice device : deviceEntities) {
//            device.detach(this);
//        }
//        deviceEntities.clear();
    }

    public void queueEvent(UXNEvent event) {
        if (uxn != null) {
            if (event instanceof KeyEvent ke && expectingArgument) {
                // If this is the last byte of an argument unpause the computer!
                if (ke.type == 0x04) uxn.paused = paused;
                // of course as long as the user isn't asking for it to be paused.
            }
            uxn.queueEvent(event);
        }
    }

    public boolean isExecuting() {
        return executing;
    }

    public boolean isArgumentMode() {
        return argumentMode;
    }

    public void setArgumentMode(boolean state) {
        argumentMode = state;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getEventCount() {
        if (uxn == null) {
            return 0;
        }
        return uxn.getEventCount();
    }

    public String dumpStatus() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("Paused: %s\nExecuting: %s\nDevices:\n", isPaused(), isExecuting()));
        for (int i = 0; i < 16; i++) {
            IDevice d = devices[i];
            if (d != null) {
                s.append(String.format("[%X0] %s\n", i, d.getLabel()));
            }
        }
        return s.toString();
    }
}

class BootEvent implements UXNEvent {
    @Override
    public void handle(UXNBus bus) {
        bus.uxn.pc = 0x100;
    }
}
