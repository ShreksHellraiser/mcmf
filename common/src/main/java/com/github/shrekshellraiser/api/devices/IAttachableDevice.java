package com.github.shrekshellraiser.api.devices;

import com.github.shrekshellraiser.core.uxn.UXNBus;
import net.minecraft.core.Direction;

public interface IAttachableDevice {
    void attemptAttach(UXNBus bus, Direction attachSide);
    boolean cableAttaches(Direction attachSide);
}
