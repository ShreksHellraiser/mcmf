package com.github.shrekshellraiser.core.uxn;

import com.github.shrekshellraiser.computer.block.ComputerBlock;
import com.github.shrekshellraiser.computer.block.entity.ComputerBlockEntity;
import com.github.shrekshellraiser.computer.block.entity.IBusProvider;
import com.github.shrekshellraiser.core.uxn.devices.IAttachableDevice;
import com.github.shrekshellraiser.devices.block.entities.ScreenDeviceBlockEntity;
import com.github.shrekshellraiser.item.memory.MemoryItem;
import com.github.shrekshellraiser.core.uxn.devices.IDevice;
import com.github.shrekshellraiser.serial.block.entity.SerialDeviceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.github.shrekshellraiser.ModBlocks.DEVICE_CABLE;

public class UXNBus {
    private UXN uxn;
    private final IDevice[] devices = new IDevice[16];
    private final Set<IDevice> deviceSet = new HashSet<>();
    private boolean executing = false;
    private boolean conflicting = false;
    private boolean paused = false;
    private boolean argumentMode = false;
    private boolean expectingArgument = false;

    private final BlockEntity blockEntity;
    private UXNBus parent;
    private final byte[] deviceMemory = new byte[256];

    public UXNBus(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public void setParent(UXNBus bus) {
        if (bus == this) return;
        parent = bus;
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
                    if (level instanceof ServerLevel sLevel) {
                        sLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, d,e,f, 0, 0.0,0.0,0.0, 0.0);
                    }
                    level.addAlwaysVisibleParticle(ParticleTypes.ANGRY_VILLAGER, d,e,f, 0.0,0.0,0.0);
                    continue;
                }
                visited.add(newPos);
                BlockState state = level.getBlockState(newPos);
                if (state.is(DEVICE_CABLE)) {
                    toSearch.push(newPos);
                    continue;
                }
                if (level instanceof ServerLevel sLevel) {
                    sLevel.sendParticles(ParticleTypes.HEART, d,e,f, 0, 0.0,0.0,0.0, 0.0);
                }
                level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, d,e,f, 0.0,0.0,0.0);
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

    private void refresh(Level level, BlockPos blockPos, Direction startDir, BlockPos ignore) {
        ArrayList<TraversedBlock> traversed = traverse(level, blockPos, startDir, ignore);
        this.conflicting = false;
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
        for (TraversedBlock block : traversed) {
            BlockEntity entity = level.getBlockEntity(block.pos);
            if (entity == null)
                continue;
            if (entity instanceof IAttachableDevice attachableDevice) {
                attachableDevice.attemptAttach(this, block.dir);
            }
        }
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
    public void startup() {
        erase();
        if (parent != null) {
            parent.startup();
            return;
        }
        if (executing || conflicting) {
            return;
        }
        if (blockEntity instanceof ComputerBlockEntity ce) {
            ItemStack stack = ce.getItem(0);
            if (stack.getItem() instanceof MemoryItem item) {
                MemoryRegion memory = item.getMemory(stack);
                refresh();
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
            }
        }
    }

    public void tick() {
        if (uxn != null) {
            uxn.runLimited(256000);
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

    private void spawnEventParticle(boolean success) {
        SimpleParticleType particleType = success ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.SMOKE;
        if (blockEntity.getLevel() instanceof ServerLevel sLevel) {
            BlockPos pos = blockEntity.getBlockPos();
            Direction facing = blockEntity.getBlockState().getValue(ComputerBlock.FACING);
            float offset = 0.51f;
            float horizontalOffset = (float) ((Math.random() - 0.5f) * 0.8f);
            int stepX = facing.getStepX();
            int stepY = facing.getStepY();
            int stepZ = facing.getStepZ();
            float x = pos.getX() + 0.5f + stepX * offset + stepZ * horizontalOffset;
            float y = pos.getY() + 0.5f + stepY * offset;
            float z = pos.getZ() + 0.5f + stepZ * offset + stepX * horizontalOffset;
            float x1 = (1 - stepX) * 0.0f;
            float y1 = (1 - stepY) * 0.0f;
            float z2 = (1 - stepZ) * 0.0f;
            float speed = 0.5f;
            sLevel.sendParticles(particleType, x, y, z, 0, x1,y1,z2, speed);
        }
    }

    public void queueEvent(UXNEvent event, UXNBus bus) {
        if (parent != null) {
            parent.queueEvent(event, this);
            return;
        }
        if (uxn != null) {
            if (event instanceof KeyEvent ke && expectingArgument) {
                // If this is the last byte of an argument unpause the computer!
                if (ke.type == 0x04) uxn.paused = paused;
                // of course as long as the user isn't asking for it to be paused.
            }
            spawnEventParticle(uxn.queueEvent(event, this));
        }
    }
    public void queueEvent(UXNEvent event) {
        queueEvent(event, this);
    }

    public boolean isExecuting() {
        if (parent != null) {
            return parent.isExecuting();
        }
        return executing;
    }

    public boolean isArgumentMode() {
        if (parent != null) {
            return parent.isArgumentMode();
        }
        return argumentMode;
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