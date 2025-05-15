package com.github.shrekshellraiser.api.devices;

import com.github.shrekshellraiser.core.uxn.UXNBus;

public interface IDevice {
    void write(int address);
    void read(int address);
    void attach(UXNBus bus);
    void detach(UXNBus bus);
    String getLabel();
}
