package com.github.shrekshellraiser.item;

import com.github.shrekshellraiser.item.memory.RAMItem;
import com.github.shrekshellraiser.item.memory.SROMItem;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;
import static com.github.shrekshellraiser.ComputerMod.REGISTRIES;

public class ModItems {
    public static final Registrar<Item> ITEMS = REGISTRIES.get().get(Registry.ITEM_REGISTRY);

    public static RegistrySupplier<Item> SROM_ITEM;
    public static RegistrySupplier<Item> RAM_ITEM;
    public static RegistrySupplier<Item> NETWORK_DEBUG_ITEM;

    public static final List<RegistrySupplier<Item>> CPU_ITEMS = new ArrayList<>();
    public static final Map<String,Integer> CPU_TIERS = new HashMap<>();

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

        UXN_CPU = tag("uxn_cpu");
    }

    static {
        CPU_TIERS.put("beet_copper", 1000);
        CPU_TIERS.put("beet_iron", 5000);
        CPU_TIERS.put("beet_gold", 25000);
        CPU_TIERS.put("beet_diamond", 100000);

        for (String tier : CPU_TIERS.keySet()) {
            CPU_ITEMS.add(registerItem(tier, () -> new BeetCPUItem(CPU_TIERS.get(tier))));
        }
    }
}
