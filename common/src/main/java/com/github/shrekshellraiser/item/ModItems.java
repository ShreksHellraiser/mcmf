package com.github.shrekshellraiser.item;

import com.github.shrekshellraiser.item.memory.RAMItem;
import com.github.shrekshellraiser.item.memory.SROMItem;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;
import static com.github.shrekshellraiser.ComputerMod.REGISTRIES;

public class ModItems {
    public static final Registrar<Item> ITEMS = REGISTRIES.get().get(Registry.ITEM_REGISTRY);

    public static RegistrySupplier<Item> SROM_ITEM;
    public static RegistrySupplier<Item> RAM_ITEM;
    public static RegistrySupplier<Item> NETWORK_DEBUG_ITEM;

    public static RegistrySupplier<Item> CPU_TIER_COPPER;
    public static RegistrySupplier<Item> CPU_TIER_IRON;
    public static RegistrySupplier<Item> CPU_TIER_GOLD;
    public static RegistrySupplier<Item> CPU_TIER_DIAMOND;

    public static TagKey<Item> UXN_CPU;

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(MOD_ID, name));
    }

    private static RegistrySupplier<Item> registerItem(String name, Supplier<Item> item) {
        return ITEMS.register(new ResourceLocation(MOD_ID, name), item);
    }

    public static void register() {
        SROM_ITEM = registerItem("srom", SROMItem::new);
        RAM_ITEM = registerItem("ram", RAMItem::new);
        NETWORK_DEBUG_ITEM = registerItem("network_debug_stick", NetworkDebugItem::new);

        CPU_TIER_COPPER = registerItem("cpu_copper", () -> new BeetCPUItem(1000));
        CPU_TIER_IRON = registerItem("cpu_iron", () -> new BeetCPUItem(5000));
        CPU_TIER_GOLD = registerItem("cpu_gold", () -> new BeetCPUItem(25000));
        CPU_TIER_DIAMOND = registerItem("cpu_diamond", () -> new BeetCPUItem(100000));

        UXN_CPU = tag("uxn_cpu");
    }
}
