package com.github.shrekshellraiser;

import com.github.shrekshellraiser.item.ModItems;
import com.github.shrekshellraiser.network.ModPackets;
import com.github.shrekshellraiser.api.serial.ISerialPeer;
import com.github.shrekshellraiser.api.serial.SerialPeerBlockEntity;
import com.github.shrekshellraiser.core.uxn.UXNBus;
import com.google.common.base.Suppliers;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.Registries;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static com.github.shrekshellraiser.ModBlocks.*;

public final class ComputerMod {
    public static final String MOD_ID = "mcmf";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Supplier<Registries> REGISTRIES = Suppliers.memoize(() -> Registries.get(MOD_ID));

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
        ClientLifecycleEvent.CLIENT_SETUP.register(ComputerMod::initClient);
    }

    public static void initClient(Minecraft client) {
        ModBlockEntityRenderers.register();
        ModMenus.registerClient();
    }

    public static void init() {
        // Write common init code here.;
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        Commands.register();
        ModMenus.registerServer();
        ModPackets.register();
        registerEvents();
    }
}
