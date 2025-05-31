package com.github.shrekshellraiser.api.serial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Stack;

import static com.github.shrekshellraiser.ModBlocks.SERIAL_CABLE;

public abstract class SerialPeerBlockEntity extends BaseContainerBlockEntity implements ISerialPeer {
    ISerialPeer peer;
    boolean conflicting = false;

    protected SerialPeerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
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
    public void clearContent() {

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void writePeer(char ch) {
        if (peer != null) {
            peer.write(ch);
        }
    }

    /*
        This returns a list of all blocks around the starting position
        plus all blocks around any blocks tagged DEVICE_CABLE
         */
    public static ArrayList<BlockPos> traverse(Level level, BlockPos blockPos, BlockPos ignore) {
        Stack<BlockPos> toSearch = new Stack<>();
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
                if (state.is(SERIAL_CABLE)) {
                    toSearch.push(newPos);
                }
            }
        }
        return visited;
    }
    public static ArrayList<BlockPos> traverse(Level level, BlockPos blockPos) {
        return traverse(level, blockPos, null);
    }

    public static ISerialPeer findSerialDevice(Level level, BlockPos blockPos) {
        ArrayList<BlockPos> traversed = traverse(level, blockPos);
        for (BlockPos pos : traversed) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity == null)
                continue;
            if (entity instanceof ISerialPeer device) {
                return device;
            }
        }
        return null;
    }

    public static void refresh(ISerialPeer host, Level level, BlockPos blockPos, BlockPos ignore) {
        ArrayList<BlockPos> traversed = traverse(level, blockPos, ignore);
        ArrayList<ISerialPeer> foundDevices = new ArrayList<>();
        host.setConflicting(false);
        System.out.println("Refreshing Serial Bus");
        for (BlockPos pos : traversed) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity == null)
                continue;
            if (entity instanceof ISerialPeer device) {
                if (entity != host) {
                    foundDevices.add(device);
                }
            }
        }
        if (foundDevices.size() > 1) {
            System.out.println("Found multiple serial devices");
            host.setConflicting(true);
            return;
        }
        ISerialPeer peer = host.getPeer();
        if (peer != null) {
            peer.detach(host);
            host.detach(peer);
        }
        if (foundDevices.size() == 1) {
            System.out.println("Found a single serial device");
            peer = foundDevices.get(0);
            host.attach(peer);
            peer.attach(host);
            return;
        }
        System.out.println("Found no devices");
    }

    public static void refresh(ISerialPeer host, Level level, BlockPos blockPos) {
        refresh(host, level, blockPos, null);
    }

    @Override
    public void attach(ISerialPeer device) {
        peer = device;
    }

    @Override
    public void detach(ISerialPeer device) {
        peer = null;
    }

    @Override
    public void setConflicting(boolean b) {
        conflicting = b;
    }

    @Override
    public ISerialPeer getPeer() {
        return peer;
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }
}
