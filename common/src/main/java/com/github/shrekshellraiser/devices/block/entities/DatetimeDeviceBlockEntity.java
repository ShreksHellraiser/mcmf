package com.github.shrekshellraiser.devices.block.entities;

import com.github.shrekshellraiser.core.uxn.UXNBus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

import static com.github.shrekshellraiser.ModBlockEntities.DATETIME_DEVICE_BLOCK_ENTITY;

public class DatetimeDeviceBlockEntity extends GenericDeviceBlockEntity {
    static final Calendar cal = Calendar.getInstance();
    public DatetimeDeviceBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DATETIME_DEVICE_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    public void attemptAttach(UXNBus bus, Direction attachSide) {
        attach(bus);
    }

    @Override
    public void write(int address) {
    }

    @Override
    public void read(int address) {
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
        return "Datetime Device";
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return new TextComponent(getLabel());
    }
}
