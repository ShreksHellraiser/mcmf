package com.github.shrekshellraiser.serial.infuser;

import com.github.shrekshellraiser.api.serial.SerialPeerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class InfuserBlock extends SerialPeerBlock {
    public InfuserBlock() {
        super(Properties.of(Material.STONE));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new InfuserBlockEntity(blockPos, blockState);
    }
}
