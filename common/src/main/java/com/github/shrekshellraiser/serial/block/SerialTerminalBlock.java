package com.github.shrekshellraiser.serial.block;

import com.github.shrekshellraiser.serial.block.entity.SerialTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class SerialTerminalBlock extends SerialPeerBlock {
    public SerialTerminalBlock() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SerialTerminalBlockEntity(blockPos, blockState);
    }
}
