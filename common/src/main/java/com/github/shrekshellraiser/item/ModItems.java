package com.github.shrekshellraiser.item;

import com.github.shrekshellraiser.item.memory.RAMItem;
import com.github.shrekshellraiser.item.memory.SROMItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, Registry.ITEM_REGISTRY);

    public static RegistrySupplier<Item> SROM_ITEM;
    public static RegistrySupplier<Item> RAM_ITEM;
    public static RegistrySupplier<Item> NETWORK_DEBUG_ITEM;

    public static void register() {
        SROM_ITEM = ITEMS.register("srom", SROMItem::new);
        RAM_ITEM = ITEMS.register("ram", RAMItem::new);
        NETWORK_DEBUG_ITEM = ITEMS.register("network_debug_stick", NetworkDebugItem::new);
        ITEMS.register();
    }
}
