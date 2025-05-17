package com.github.shrekshellraiser.item;

import com.github.shrekshellraiser.item.memory.RAMItem;
import com.github.shrekshellraiser.item.memory.SROMItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;
import static com.github.shrekshellraiser.ComputerMod.REGISTRIES;

public class ModItems {
    public static final Registrar<Item> ITEMS = REGISTRIES.get().get(Registry.ITEM_REGISTRY);

    public static RegistrySupplier<Item> SROM_ITEM;
    public static RegistrySupplier<Item> RAM_ITEM;
    public static RegistrySupplier<Item> NETWORK_DEBUG_ITEM;

    private static RegistrySupplier<Item> registerItem(String name, Supplier<Item> item) {
        return ITEMS.register(new ResourceLocation(MOD_ID, name), item);
    }

    public static void register() {
        SROM_ITEM = registerItem("srom", SROMItem::new);
        RAM_ITEM = registerItem("ram", RAMItem::new);
        NETWORK_DEBUG_ITEM = registerItem("network_debug_stick", NetworkDebugItem::new);
    }
}
