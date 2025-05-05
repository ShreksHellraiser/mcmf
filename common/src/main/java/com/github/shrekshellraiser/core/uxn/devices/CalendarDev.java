package com.github.shrekshellraiser.core.uxn.devices;

import java.util.Calendar;
import java.util.Date;

public class CalendarDev extends Device{
    static final Calendar cal = Calendar.getInstance();

    @Override
    public void write(int address) {
        //we don't support setting the clock from java
    }

    @Override
    public void read(int address) {
        int data = bus.readDev(address);
        int port = address & 0x0F;
        if (port >= 0x0B) {return;}
        byte write = switch (port) {
            case 0x00 -> (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
            case 0x01 -> (byte) (cal.get(Calendar.YEAR) & 0xFF);
            case 0x02 -> (byte) cal.get(Calendar.MONTH);
            case 0x03 -> (byte) cal.get(Calendar.DAY_OF_MONTH);
            case 0x04 -> (byte) cal.get(Calendar.HOUR);
            case 0x05 -> (byte) cal.get(Calendar.MINUTE);
            case 0x06 -> (byte) cal.get(Calendar.SECOND);
            case 0x07 -> (byte) cal.get(Calendar.DAY_OF_WEEK);
            case 0x08 -> (byte) ((cal.get(Calendar.DAY_OF_YEAR) >> 8) & 0xFF);
            case 0x09 -> (byte) (cal.get(Calendar.DAY_OF_YEAR) & 0xFF);
            case 0x0A -> {
                byte ret;
                if (cal.getTimeZone().inDaylightTime(new Date())) {ret = 1;} else {ret = 0;}
                yield ret;
            }
            default -> 0x00;
        };
        bus.writeDev(address,write);
    }

    @Override
    public String getLabel() {
        return "Calendar";
    }
}
