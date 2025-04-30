package com.github.masongulu.computer.block.entity;

import com.github.masongulu.core.uxn.UXNBus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface IBusProvider {
    UXNBus getBus(Direction dir);
    BlockPos getScanRoot();
    Direction getScanStartDir();
}
