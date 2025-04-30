package com.github.masongulu.core.uxn.devices;

import com.github.masongulu.core.uxn.UXNBus;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface IAttachableDevice {
    void attemptAttach(UXNBus bus, Direction attachSide);
}
