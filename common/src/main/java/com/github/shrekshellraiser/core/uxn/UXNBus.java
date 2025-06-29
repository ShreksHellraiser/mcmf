package com.github.shrekshellraiser.core.uxn;

import com.github.shrekshellraiser.computer.block.entity.ComputerBlockEntity;
import com.github.shrekshellraiser.computer.block.entity.IBusProvider;
import com.github.shrekshellraiser.api.devices.IAttachableDevice;
import com.github.shrekshellraiser.devices.screen.ScreenDeviceBlockEntity;
import com.github.shrekshellraiser.item.BeetCPUItem;
import com.github.shrekshellraiser.item.memory.MemoryItem;
import com.github.shrekshellraiser.api.devices.IDevice;
import com.github.shrekshellraiser.devices.serial.SerialDeviceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.github.shrekshellraiser.ComputerMod.LOGGER;
import static com.github.shrekshellraiser.ModBlocks.DEVICE_CABLE;
import static com.github.shrekshellraiser.item.ModItems.UXN_CPU;

public class UXNBus {
    private UXN uxn;
    private final IDevice[] devices = new IDevice[16];
    private final Set<IDevice> deviceSet = new HashSet<>();
    private boolean poweredOn = false;
    private boolean conflicting = false;
    private boolean paused = false;
    private boolean argumentMode = false;
    private boolean expectingArgument = false;
    private boolean executing = false;
    private int executionBudget = 1000;
    private int executionSpent = 0;
    private int vectorOverflow = 0;

    private final BlockEntity blockEntity;
    private UXNBus parent;
    private final byte[] deviceMemory = new byte[256];
    private final FileDeviceWrapper fileDeviceWrapper = new FileDeviceWrapper();

    public UXNBus(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void setParent(UXNBus bus) {
        if (bus == this) return;
        parent = bus;
    }

    public FileDeviceWrapper getFileDeviceWrapper() {
        return this.fileDeviceWrapper;
    }

    public UXNBus getParent() {
        return parent;
    }

    /*
    This returns a list of all blocks around the starting position
    plus all blocks around any blocks tagged DEVICE_CABLE
     */
    private static ArrayList<TraversedBlock> traverse(Level level, BlockPos blockPos, Direction start, BlockPos ignore) {
        java.util.Stack<BlockPos> toSearch = new java.util.Stack<>();
        toSearch.push(blockPos);
        ArrayList<BlockPos> visited = new ArrayList<>();
        ArrayList<TraversedBlock> traversed = new ArrayList<>();
        if (start != null) {
            // Allow directly attaching devices to the computer
            traversed.add(new TraversedBlock(blockPos, start));
        }
        while (!toSearch.isEmpty()) {
            BlockPos pos = toSearch.pop();
            // Add surrounding blocks to search
            for (Direction direction : Direction.values()) {
                BlockPos newPos = pos.relative(direction);
                double d = (double)newPos.getX() + 0.5;
                double e = (double)newPos.getY() + 0.5;
                double f = (double)newPos.getZ() + 0.5;
                if (visited.contains(newPos)) continue;
                if (newPos.equals(ignore)) {
                    continue;
                }
                BlockState state = level.getBlockState(newPos);
                if (level.getBlockEntity(newPos) instanceof IAttachableDevice device) {
                    if (!device.cableAttaches(direction.getOpposite())) continue;
                }
                visited.add(newPos);
                if (state.is(DEVICE_CABLE)) {
                    toSearch.push(newPos);
                    continue;
                }
                traversed.add(new TraversedBlock(newPos, direction.getOpposite()));
            }
        }
        return traversed;
    }
    private static ArrayList<TraversedBlock> traverse(Level level, BlockPos blockPos) {
        return traverse(level, blockPos, null, null);
    }

    public static @Nullable UXNBus findBus(Level level, BlockPos blockPos) {
        ArrayList<TraversedBlock> traversed = traverse(level, blockPos);
        for (TraversedBlock block : traversed) {
            BlockEntity entity = level.getBlockEntity(block.pos);
            if (entity == null)
                continue;
            if (entity instanceof IBusProvider IBusProvider) {
                var bus = IBusProvider.getBus(block.dir);
                if (bus != null) return bus;
            }
        }
        return null;
    }
    private void sendParticles(ServerLevel level, ArrayList<TraversedBlock> networkBlocks, ParticleOptions particle) {
        for (TraversedBlock block : networkBlocks) {
            double d = (double)block.pos.getX() + 0.5;
            double e = (double)block.pos.getY() + 0.5;
            double f = (double)block.pos.getZ() + 0.5;
            level.sendParticles(particle, d,e,f, 0, 0.0,0.0,0.0, 0.0);
        }
    }
    private void refresh(Level level, BlockPos blockPos, Direction startDir, BlockPos ignore) {
        ArrayList<TraversedBlock> traversed = traverse(level, blockPos, startDir, ignore);
        this.conflicting = false;
        fileDeviceWrapper.detach(this);
        fileDeviceWrapper.clearDevices();
        for (IDevice device : deviceSet) {
            deviceSet.remove(device);
            device.detach(this);
        }
        // clean up remaining devices
        deviceSet.clear();
        for (int i = 1; i < 16; i++) {
            devices[i] = null;
        }
        this.uxn = null;
        ArrayList<TraversedBlock> networkBlocks = new ArrayList<>();
        for (TraversedBlock block : traversed) {
            BlockEntity entity = level.getBlockEntity(block.pos);
            if (level.getBlockState(block.pos).is(DEVICE_CABLE)) {
                networkBlocks.add(block);
            }
            if (entity == null)
                continue;
            if (entity instanceof IAttachableDevice attachableDevice) {
                attachableDevice.attemptAttach(this, block.dir);
                networkBlocks.add(block);
            } else if (entity instanceof ComputerBlockEntity computer) {
                UXNBus bus = computer.getBus(block.dir);
                if (bus != this) {
                    conflicting = true;
                    bus.conflicting = true;
                }
                networkBlocks.add(block);
            }
        }
        fileDeviceWrapper.attach(this);
        if (!level.isClientSide) sendParticles((ServerLevel)level, networkBlocks, conflicting ? ParticleTypes.LARGE_SMOKE : ParticleTypes.HEART);
    }
    private void refresh(Level level, BlockPos blockPos, Direction startDir) {
        refresh(level, blockPos, startDir, null);
    }
    public void refresh() {
        refresh(null);
    }
    public void refresh(BlockPos ignore) {
        IBusProvider provider = (IBusProvider) this.blockEntity;
        refresh(this.blockEntity.getLevel(), provider.getScanRoot(), provider.getScanStartDir(), ignore);
    }

    public void writeDev(int address, int data) {
        address %= 256;
        deviceMemory[address] = (byte)data;
    }
    public void writeDevWord(int address, int data) {
        writeDev(address, data >> 8);
        writeDev(address + 1, data & 0xFF);
    }

    public int readDev(int address) {
        address %= 256;
        return deviceMemory[address] & 0xFF;
    }
    public int readDevWord(int address) {
        return (readDev(address) << 8) | readDev(address + 1);
    }

    public void setUxn(UXN uxn) {
        this.uxn = uxn;
    }

    public UXN getUxn() {
        if (parent != null) {
            return parent.getUxn();
        }
        return uxn;
    }

    public void updateColors() {
        int red = readDevWord(0x08);
        int green = readDevWord(0x0A);
        int blue = readDevWord(0x0C);

        int[] colors = new int[4];
        for (int i = 3; i >= 0; i--) {
            int r = red & 0b1111;
            int g = green & 0b1111;
            int b = blue & 0b1111;
            r |= r << 4;
            g |= g << 4;
            b |= b << 4;
            red >>= 4;
            green >>= 4;
            blue >>= 4;
            colors[i] = (r << 16) | (g << 8) | b;
        }
        if (devices[0x2] instanceof ScreenDeviceBlockEntity screen) {
            screen.setColors(colors);
        }
    }

    public void erase() {
        for (int i = 0; i < 256; i++) {
            deviceMemory[i] = 0;
        }
    }
    public boolean startup() {
        erase();
        if (parent != null) {
            return parent.startup();
        }
        if (poweredOn) return false;
        if (blockEntity instanceof ComputerBlockEntity ce) {
            ItemStack stack = ce.getItem(0);
            ItemStack beetStack = ce.getItem(1);
            if (!beetStack.is(UXN_CPU)) return false;
            int budget = 10; // Default budget (vanilla beetroot)
            if (beetStack.getItem() instanceof BeetCPUItem item) {
                budget = item.getBudget();
            }
            executionBudget = budget;
            if (stack.getItem() instanceof MemoryItem item) {
                MemoryRegion memory = item.getMemory(stack);
                refresh();
                if (conflicting) return false;
                poweredOn = true;
                new UXN(this, memory);
                uxn.paused = paused || argumentMode;
                uxn.queueEvent(new BootEvent());
                int dev = 0x1;
                IDevice device = devices[dev];
                if (device instanceof SerialDeviceBlockEntity sDevice && sDevice.getPeer() != null) {
                    writeDev(dev << 4 | 7, 0);
                    if (argumentMode) {
                        sDevice.requestArgument();
                        writeDev(dev << 4 | 7, 1);
                        expectingArgument = true;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean run() {
        if (uxn != null && executionSpent < executionBudget) {
            Level level = blockEntity.getLevel();
            if (level == null) return false;
            MinecraftServer server = level.getServer();
            if (server == null) return false;
            if (!server.isSameThread()) return false;
            executing = true;
            executionSpent += uxn.runLimited(executionBudget - executionSpent);
            executing = false;
            return true;
        }
        return false;
    }

    public void tick() {
        executionSpent = 0;
        vectorOverflow = 0;
        if (uxn != null) {
            if (!run()) LOGGER.warn("Main thread skipped execution!");
        }
    }

    public void pause(boolean state) {
        if (parent != null) {
            parent.pause(state);
            return;
        }
        this.paused = state;
        if (uxn != null) {
            uxn.paused = state;
        }
    }
    public void pause() {
        pause(true);
    }

    public void shutdown() {
        if (parent != null) {
            parent.shutdown();
            return;
        }
        poweredOn = false;
        if (uxn != null) uxn.stop();
        uxn = null;
        expectingArgument = false;
    }

    /*
    Set the device for a particular device number.
    This should be called by IDevice::attach
     */
    public void setDevice(int index, IDevice device) {
        if (devices[index] != null) {
            conflicting = true;
        }
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

    private void onVectorDropped() {
        vectorOverflow++;
        if (vectorOverflow > 128) {
            LOGGER.warn("Computer generated excessive vectors!");
            if (blockEntity instanceof ComputerBlockEntity ce) {
                ce.spawnFailureParticle();
            }
            shutdown();
        }
    }
    public void queueEvent(UXNEvent event, UXNBus bus) {
        if (parent != null) {
            parent.queueEvent(event, this);
            return;
        }
        if (uxn != null) {
            if (event instanceof ConsoleEvent ke && expectingArgument) {
                // If this is the last byte of an argument unpause the computer!
                if (ke.type == 0x04) uxn.paused = paused;
                expectingArgument = false;
                // of course as long as the user isn't asking for it to be paused.
            }
            if (blockEntity instanceof ComputerBlockEntity ce) {
                boolean success = uxn.queueEvent(event, this);
                ce.spawnEventParticle(success);
                if (!success) onVectorDropped();
            }
            // run(); // use up our remaining execution budget to run vectors as they are queued.
            // bad idea
        }
    }
    public float getCongestion() {
        if (parent != null) {
            return parent.getCongestion();
        }
        if (uxn != null) {
            return uxn.getCongestion();
        }
        return 0;
    }
    public void queueEvent(UXNEvent event) {
        queueEvent(event, this);
    }

    public boolean isPoweredOn() {
        if (parent != null) {
            return parent.isPoweredOn();
        }
        return poweredOn;
    }

    public boolean isArgumentMode() {
        if (parent != null) {
            return parent.isArgumentMode();
        }
        return argumentMode;
    }

    public boolean isExpectingArgument() {
        if (parent != null) {
            return parent.isExpectingArgument();
        }
        return expectingArgument;
    }

    public void setArgumentMode(boolean state) {
        if (parent != null) {
            parent.setArgumentMode(state);
            return;
        }
        argumentMode = state;
    }

    public boolean isPaused() {
        if (parent != null) {
            return parent.isPaused();
        }
        return paused;
    }

    public int getEventCount() {
        if (parent != null) {
            return parent.getEventCount();
        }
        if (uxn == null) {
            return 0;
        }
        return uxn.getEventCount();
    }

    public String dumpStatus() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("Paused: %s\nExecuting: %s\nDevices:\n", isPaused(), isPoweredOn()));
        for (int i = 0; i < 16; i++) {
            IDevice d = devices[i];
            if (d != null) {
                s.append(String.format("[%X0] %s\n", i, d.getLabel()));
            }
        }
        return s.toString();
    }
}

class BootEvent extends BasicUXNEvent {
    @Override
    public void handle(UXNBus bus) {
        bus.getUxn().pc = 0x100;
    }
}

class TraversedBlock {
    public final BlockPos pos;
    public final Direction dir;

    TraversedBlock(BlockPos pos, Direction dir) {
        this.pos = pos;
        this.dir = dir;
    }
}