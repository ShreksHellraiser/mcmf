package com.github.shrekshellraiser.devices.screen;

import com.github.shrekshellraiser.ModBlockEntities;
import com.github.shrekshellraiser.api.devices.GenericDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class ScreenDeviceBlock extends GenericDeviceBlock {
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
}
