package com.github.shrekshellraiser;

import com.github.shrekshellraiser.computer.block.CableBlock;
import com.github.shrekshellraiser.computer.block.ComputerBlock;
import com.github.shrekshellraiser.devices.datetime.DatetimeDeviceBlock;
import com.github.shrekshellraiser.devices.flasher.FlasherDeviceBlock;
import com.github.shrekshellraiser.devices.multiplexer.MultiplexerDeviceBlock;
import com.github.shrekshellraiser.devices.redstone.RedstoneDeviceBlock;
import com.github.shrekshellraiser.devices.screen.ScreenDeviceBlock;
import com.github.shrekshellraiser.serial.SerialCableBlock;
import com.github.shrekshellraiser.devices.serial.SerialDeviceBlock;
import com.github.shrekshellraiser.serial.terminal.SerialTerminalBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;
import static com.github.shrekshellraiser.ModCreativeTab.MOD_TAB;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registry.BLOCK_REGISTRY);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(MOD_ID, Registry.ITEM_REGISTRY);

    public static RegistrySupplier<Block> COMPUTER_BLOCK;
    public static RegistrySupplier<BlockItem> COMPUTER_ITEM;

    // UXN Devices
    public static RegistrySupplier<Block> CABLE_BLOCK;
    public static RegistrySupplier<Item> CABLE_ITEM;
    public static RegistrySupplier<Block> REDSTONE_DEVICE_BLOCK;
    public static RegistrySupplier<Item> REDSTONE_DEVICE_ITEM;
    public static RegistrySupplier<Block> FLASHER_DEVICE_BLOCK;
    public static RegistrySupplier<Item> FLASHER_DEVICE_ITEM;
    public static RegistrySupplier<Block> SERIAL_DEVICE_BLOCK;
    public static RegistrySupplier<BlockItem> SERIAL_DEVICE_ITEM;
    public static RegistrySupplier<Block> MULTIPLEXER_DEVICE_BLOCK;
    public static RegistrySupplier<BlockItem> MULTIPLEXER_DEVICE_ITEM;
    public static RegistrySupplier<Block> DATETIME_DEVICE_BLOCK;
    public static RegistrySupplier<BlockItem> DATETIME_DEVICE_ITEM;
    public static RegistrySupplier<Block> SCREEN_DEVICE_BLOCK;
    public static RegistrySupplier<BlockItem> SCREEN_DEVICE_ITEM;

    // Serial Devices
    public static RegistrySupplier<Block> SERIAL_CABLE_BLOCK;
    public static RegistrySupplier<Item> SERIAL_CABLE_ITEM;
    public static RegistrySupplier<Block> SERIAL_TERMINAL_BLOCK;
    public static RegistrySupplier<BlockItem> SERIAL_TERMINAL_ITEM;

    public static TagKey<Block> DEVICE_CABLE;
    public static TagKey<Block> SERIAL_CABLE;
    public static TagKey<Block> UXN_DEVICE;
    public static TagKey<Block> SERIAL_DEVICE;

    private static TagKey<Block> tag(String name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, name));
    }

    public static void register() {
        DEVICE_CABLE = tag("device_cable");
        SERIAL_CABLE = tag("serial_cable");
        UXN_DEVICE = tag("uxn_device");
        SERIAL_DEVICE = tag("serial_device");

        COMPUTER_BLOCK = BLOCKS.register("computer", ComputerBlock::new);
        COMPUTER_ITEM = BLOCK_ITEMS.register("computer", () -> new BlockItem(COMPUTER_BLOCK.get(),
                new Item.Properties().tab(MOD_TAB)));


        // UXN Device Registration
        CABLE_BLOCK = BLOCKS.register("cable", CableBlock::new);
        CABLE_ITEM = BLOCK_ITEMS.register("cable", () -> new BlockItem(CABLE_BLOCK.get(),
                new Item.Properties().tab(MOD_TAB)));
        REDSTONE_DEVICE_BLOCK = BLOCKS.register("redstone_device", RedstoneDeviceBlock::new);
        REDSTONE_DEVICE_ITEM = BLOCK_ITEMS.register("redstone_device", () -> new BlockItem(REDSTONE_DEVICE_BLOCK.get(),
                new Item.Properties().tab(MOD_TAB)));
        FLASHER_DEVICE_BLOCK = BLOCKS.register("flasher_device", FlasherDeviceBlock::new);
        FLASHER_DEVICE_ITEM = BLOCK_ITEMS.register("flasher_device", () -> new BlockItem(FLASHER_DEVICE_BLOCK.get(),
                new Item.Properties().tab(MOD_TAB)));
        SERIAL_DEVICE_BLOCK = BLOCKS.register("serial_device", SerialDeviceBlock::new);
        SERIAL_DEVICE_ITEM = BLOCK_ITEMS.register("serial_device", () -> new BlockItem(SERIAL_DEVICE_BLOCK.get(),
                new Item.Properties().tab(MOD_TAB)));
        MULTIPLEXER_DEVICE_BLOCK = BLOCKS.register("multiplexer_device", MultiplexerDeviceBlock::new);
        MULTIPLEXER_DEVICE_ITEM = BLOCK_ITEMS.register("multiplexer_device", () -> new BlockItem(MULTIPLEXER_DEVICE_BLOCK.get(),
                new Item.Properties().tab(MOD_TAB)));
        DATETIME_DEVICE_BLOCK = BLOCKS.register("datetime_device", DatetimeDeviceBlock::new);
        DATETIME_DEVICE_ITEM = BLOCK_ITEMS.register("datetime_device", () -> new BlockItem(DATETIME_DEVICE_BLOCK.get(),
                new Item.Properties().tab(MOD_TAB)));
        SCREEN_DEVICE_BLOCK = BLOCKS.register("screen_device", ScreenDeviceBlock::new);
        SCREEN_DEVICE_ITEM = BLOCK_ITEMS.register("screen_device", () -> new BlockItem(SCREEN_DEVICE_BLOCK.get(),
                new Item.Properties().tab(MOD_TAB)));


        // Serial Device Registration
        SERIAL_CABLE_BLOCK = BLOCKS.register("serial_cable", SerialCableBlock::new);
        SERIAL_CABLE_ITEM = BLOCK_ITEMS.register("serial_cable", () -> new BlockItem(SERIAL_CABLE_BLOCK.get(),
                new Item.Properties().tab(MOD_TAB)));
        SERIAL_TERMINAL_BLOCK = BLOCKS.register("serial_terminal", SerialTerminalBlock::new);
        SERIAL_TERMINAL_ITEM = BLOCK_ITEMS.register("serial_terminal", () -> new BlockItem(SERIAL_TERMINAL_BLOCK.get(),
                new Item.Properties().tab(MOD_TAB)));


        BLOCKS.register();
        BLOCK_ITEMS.register();
    }
}
