package com.github.shrekshellraiser;

import dev.architectury.registry.CreativeTabRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class ModCreativeTab {
    public static final CreativeModeTab MOD_TAB = CreativeTabRegistry.create(
            new ResourceLocation(MOD_ID, "mcmf_tab"),
            () -> new ItemStack(ModBlocks.COMPUTER_ITEM.get())
    );
}
