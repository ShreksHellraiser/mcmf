package com.github.shrekshellraiser.devices.multiplexer;

import com.github.shrekshellraiser.api.devices.GenericDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MultiplexerDeviceBlock extends GenericDeviceBlock {
    public static final VoxelShape BOTTOM_SLAB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    public MultiplexerDeviceBlock() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MultiplexerDeviceBlockEntity(blockPos, blockState);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return BOTTOM_SLAB;
    }
}
