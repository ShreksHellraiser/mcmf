package com.github.masongulu;

import com.github.masongulu.item.ModItems;
import com.github.masongulu.network.ModPackets;
import com.github.masongulu.serial.ISerialPeer;
import com.github.masongulu.serial.block.entity.SerialPeerBlockEntity;
import com.github.masongulu.core.uxn.UXNBus;
import com.github.masongulu.core.uxn.UXNExecutor;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.TickEvent;

import static com.github.masongulu.ModBlocks.*;

public final class ComputerMod {
    public static final String MOD_ID = "mcmf";

    public static UXNExecutor UXN_EXECUTOR = new UXNExecutor();

    public static void registerEvents() {
        BlockEvent.BREAK.register(((level, pos, state, player, xp) -> {
            if (state.is(DEVICE_CABLE) || state.is(UXN_DEVICE)) {
                UXNBus bus = UXNBus.findBus(level, pos);
                if (bus != null) {
                    bus.refresh(pos);
                }
            }
            if (state.is(SERIAL_CABLE) || state.is(SERIAL_DEVICE)) {
                ISerialPeer sbus = SerialPeerBlockEntity.findSerialDevice(level, pos);
                if (sbus != null) {
                    SerialPeerBlockEntity.refresh(sbus, level, sbus.getPos(), pos);
                }
            }
            return EventResult.pass();
        }));
        BlockEvent.PLACE.register(((level, pos, state, placer) -> {
            if (state.is(DEVICE_CABLE) || state.is(UXN_DEVICE)) {
                UXNBus bus = UXNBus.findBus(level, pos);
                if (bus != null) {
                    bus.refresh();
                }
            }
            if (state.is(SERIAL_CABLE) || state.is(SERIAL_DEVICE)) {
                ISerialPeer sbus = SerialPeerBlockEntity.findSerialDevice(level, pos);
                if (sbus != null) {
                    SerialPeerBlockEntity.refresh(sbus, level, pos);
                }
            }
            return EventResult.pass();
        }));
        TickEvent.SERVER_POST.register((level) -> UXN_EXECUTOR.tick());
    }

    public static void init() {
        // Write common init code here.;
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        Commands.register();
        ModMenus.register();
        ModPackets.register();
        registerEvents();
    }
}
