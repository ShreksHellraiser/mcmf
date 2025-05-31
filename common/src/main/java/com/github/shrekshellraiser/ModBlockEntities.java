package com.github.shrekshellraiser;

import com.github.shrekshellraiser.computer.block.entity.ComputerBlockEntity;
import com.github.shrekshellraiser.devices.datetime.DatetimeDeviceBlockEntity;
import com.github.shrekshellraiser.devices.flasher.FlasherDeviceBlockEntity;
import com.github.shrekshellraiser.devices.multiplexer.MultiplexerDeviceBlockEntity;
import com.github.shrekshellraiser.devices.redstone.RedstoneDeviceBlockEntity;
import com.github.shrekshellraiser.devices.screen.ScreenDeviceBlockEntity;
import com.github.shrekshellraiser.devices.serial.SerialDeviceBlockEntity;
import com.github.shrekshellraiser.serial.infuser.InfuserBlockEntity;
import com.github.shrekshellraiser.serial.terminal.SerialTerminalBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;
import java.util.function.Supplier;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;
import static com.github.shrekshellraiser.ComputerMod.REGISTRIES;
import static com.github.shrekshellraiser.ModBlocks.*;

public class ModBlockEntities {


    public static final Registrar<BlockEntityType<?>> BLOCK_ENTITIES =
            REGISTRIES.get().get(Registry.BLOCK_ENTITY_TYPE_REGISTRY);
    public static RegistrySupplier<BlockEntityType<ComputerBlockEntity>> COMPUTER_BLOCK_ENTITY;


    // UXN Devices
    public static RegistrySupplier<BlockEntityType<RedstoneDeviceBlockEntity>> REDSTONE_DEVICE_BLOCK_ENTITY;
    public static RegistrySupplier<BlockEntityType<SerialDeviceBlockEntity>> SERIAL_DEVICE_BLOCK_ENTITY;
    public static RegistrySupplier<BlockEntityType<FlasherDeviceBlockEntity>> FLASHER_DEVICE_BLOCK_ENTITY;
    public static RegistrySupplier<BlockEntityType<MultiplexerDeviceBlockEntity>> MULTIPLEXER_DEVICE_BLOCK_ENTITY;
    public static RegistrySupplier<BlockEntityType<DatetimeDeviceBlockEntity>> DATETIME_DEVICE_BLOCK_ENTITY;
    public static RegistrySupplier<BlockEntityType<ScreenDeviceBlockEntity>> SCREEN_DEVICE_BLOCK_ENTITY;


    // Serial Devices
    public static RegistrySupplier<BlockEntityType<SerialTerminalBlockEntity>> SERIAL_TERMINAL_BLOCK_ENTITY;
    public static RegistrySupplier<BlockEntityType<InfuserBlockEntity>> INFUSER_BLOCK_ENTITY;

    private static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String name, Supplier<BlockEntityType<T>> blockEntityType) {
        return BLOCK_ENTITIES.register(new ResourceLocation(MOD_ID, name), blockEntityType);
    }

    public static void register() {
        COMPUTER_BLOCK_ENTITY = register("computer", () ->
                new BlockEntityType<>(ComputerBlockEntity::new, Set.of(COMPUTER_BLOCK.get()), null));


        // UXN Devices
        REDSTONE_DEVICE_BLOCK_ENTITY = register("redstone_device", () ->
                new BlockEntityType<>(RedstoneDeviceBlockEntity::new, Set.of(REDSTONE_DEVICE_BLOCK.get()), null));
        SERIAL_DEVICE_BLOCK_ENTITY = register("serial_device", () ->
                new BlockEntityType<>(SerialDeviceBlockEntity::new, Set.of(SERIAL_DEVICE_BLOCK.get()), null));
        FLASHER_DEVICE_BLOCK_ENTITY = register("flasher_device", () ->
                new BlockEntityType<>(FlasherDeviceBlockEntity::new, Set.of(FLASHER_DEVICE_BLOCK.get()), null));
        MULTIPLEXER_DEVICE_BLOCK_ENTITY = register("multiplexer_device", () ->
                new BlockEntityType<>(MultiplexerDeviceBlockEntity::new, Set.of(MULTIPLEXER_DEVICE_BLOCK.get()), null));
        DATETIME_DEVICE_BLOCK_ENTITY = register("datetime_device", () ->
                new BlockEntityType<>(DatetimeDeviceBlockEntity::new, Set.of(DATETIME_DEVICE_BLOCK.get()), null));
        SCREEN_DEVICE_BLOCK_ENTITY = register("screen_device", () ->
                new BlockEntityType<>(ScreenDeviceBlockEntity::new, Set.of(SCREEN_DEVICE_BLOCK.get()), null));


        // Serial Devices
        SERIAL_TERMINAL_BLOCK_ENTITY = register("serial_terminal", () ->
                new BlockEntityType<>(SerialTerminalBlockEntity::new, Set.of(SERIAL_TERMINAL_BLOCK.get()), null));
        INFUSER_BLOCK_ENTITY = register("infuser", () ->
                new BlockEntityType<>(InfuserBlockEntity::new, Set.of(INFUSER_BLOCK.get()), null));
    }
}
