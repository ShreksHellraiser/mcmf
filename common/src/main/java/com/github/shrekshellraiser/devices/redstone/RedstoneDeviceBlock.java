package com.github.shrekshellraiser.devices.redstone;

import com.github.shrekshellraiser.ModBlockEntities;
import com.github.shrekshellraiser.devices.api.GenericDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RedstoneDeviceBlock extends GenericDeviceBlock {

    public RedstoneDeviceBlock() {
        super(Properties.of(Material.STONE));
    }

    public BlockEntity newBlockEntity(BlockPos arg, BlockState arg2) {
        return new RedstoneDeviceBlockEntity(arg, arg2);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }


    private Map<Direction, Integer> lastRedstone = new HashMap<>();
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (!level.isClientSide) {
            Direction facing = blockState.getValue(FACING);
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof RedstoneDeviceBlockEntity r) {
                Map<Direction, Integer> newRedstone = new HashMap<>();
                boolean different = false;
                for (Direction direction : Direction.values()) {
                    Direction adjusted = adjustForRotation(facing, direction);
                    int rsLevel = level.getDirectSignal(blockPos.relative(adjusted), adjusted.getOpposite());
                    newRedstone.put(direction, rsLevel);
                    if (rsLevel != lastRedstone.getOrDefault(direction, 0)) {
                        different = true;
                    }
                }
                if (different) {
                    r.updateRedstone(newRedstone);
                    lastRedstone = newRedstone;
                }
            }
        }
    }

    private Direction adjustForRotation(Direction facing, Direction direction) {
        if (direction != Direction.UP && direction != Direction.DOWN && facing != Direction.SOUTH) {
            // There surely is a better way of doing this.
            if (facing == Direction.EAST) {
                return direction.getClockWise();
            } else if (facing == Direction.NORTH) {
                return direction.getOpposite();
            } else if (facing == Direction.WEST) {
                return direction.getCounterClockWise();
            }
        }
        return direction;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.REDSTONE_DEVICE_BLOCK_ENTITY.get(), RedstoneDeviceBlockEntity::tick);
    }


    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof RedstoneDeviceBlockEntity r) {
            Direction facing = blockState.getValue(FACING);
            Direction adjusted = adjustForRotation(facing, direction);

            return r.redstoneOutputs.get(adjusted);
        }
        return super.getSignal(blockState, blockGetter, blockPos, direction);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return getSignal(blockState, blockGetter, blockPos, direction);
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }
}
