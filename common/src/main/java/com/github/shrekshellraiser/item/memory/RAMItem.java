package com.github.shrekshellraiser.item.memory;

import com.github.shrekshellraiser.core.uxn.MemoryRegion;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RAMItem extends MemoryItem {
    @Override
    public String getUUID(ItemStack stack) {
        return "";
    }

    @Override
    public void setLabel(ItemStack stack, String label) {
    }

    @Override
    public boolean isFlashable() {
        return false;
    }

    @Override
    public @Nullable String getLabel(ItemStack pStack) {
        return null;
    }

    @Override
    public MemoryRegion getMemory(ItemStack stack) {
        return new MemoryRegion();
    }

    @Override
    public String getLabelForDirectory() {
        return "ram";
    }
}
