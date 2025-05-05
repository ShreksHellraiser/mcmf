package com.github.shrekshellraiser.core.uxn.devices;

import com.github.shrekshellraiser.core.uxn.UXNBus;

public abstract class Device implements IDevice {
    protected UXNBus bus;
    protected int deviceId;
    @Override
    public void attach(UXNBus bus) {
        this.bus = bus;
        bus.setDevice(deviceId, this);
    }

    @Override
    public void detach(UXNBus bus) {
        bus.deleteDevice(deviceId);
        this.bus = null;
    }

    public abstract void write(int address);
    public abstract void read(int address);
}
