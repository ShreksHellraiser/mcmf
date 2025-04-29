package com.github.masongulu.core.uxn;


public class KeyEvent implements UXNEvent {
    char ch;
    byte type;
    int device;

    public KeyEvent(char ch, byte type, int device) {
        this.ch = ch;
        this.type = type;
        this.device = device;
    }
    public KeyEvent(char ch) {
        this.ch = ch;
        this.type = 0x01; //stdin key
        this.device = 0x10; // this is where the device is on varvara
    }

    @Override
    public void handle(UXNBus bus) {
        bus.uxn.pc = (bus.readDev(device) << 8) | bus.readDev(device+1); //get the vector for PC at the time the event is handled
        bus.writeDev(device + 0x02, (byte) ch);
        bus.writeDev(device + 0x07, type);
    }

    @Override
    public String toString() {
        return "key: '%s' type: %02X".formatted(ch,type);
    }
}