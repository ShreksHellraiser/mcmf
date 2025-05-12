package com.github.shrekshellraiser.devices.datetime;

import com.github.shrekshellraiser.devices.api.GenericDeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class DatetimeDeviceBlock extends GenericDeviceBlock {
    public DatetimeDeviceBlock() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DatetimeDeviceBlockEntity(blockPos, blockState);
    }
}
