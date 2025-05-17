package com.github.shrekshellraiser.devices.screen;

import com.github.shrekshellraiser.ModBlockEntities;
import com.github.shrekshellraiser.api.devices.GenericDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ScreenDeviceBlock extends GenericDeviceBlock {
    public static final VoxelShape NORTH = Block.box(0.0, 0.0, 5.0, 16.0, 15.0, 16.0);
    public static final VoxelShape SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 11.0);
    public static final VoxelShape EAST = Block.box(0.0, 0.0, 0.0, 11.0, 15.0, 16.0);
    public static final VoxelShape WEST = Block.box(5.0, 0.0, 0.0, 16.0, 15.0, 16.0);
    public ScreenDeviceBlock() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ScreenDeviceBlockEntity(blockPos, blockState);
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (blockEntityType == ModBlockEntities.SCREEN_DEVICE_BLOCK_ENTITY.get()) {
            return ScreenDeviceBlockEntity::tick;
        }
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return switch (blockState.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> NORTH;
        };
    }
}
