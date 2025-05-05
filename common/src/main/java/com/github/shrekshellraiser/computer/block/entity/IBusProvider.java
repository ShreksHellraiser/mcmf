package com.github.shrekshellraiser.computer.block.entity;

import com.github.shrekshellraiser.core.uxn.UXNBus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface IBusProvider {
    UXNBus getBus(Direction dir);
    BlockPos getScanRoot();
    Direction getScanStartDir();
}
