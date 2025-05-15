package com.github.shrekshellraiser.item.memory;

import com.github.shrekshellraiser.core.uxn.MemoryRegion;
import net.minecraft.world.item.ItemStack;

public class SROMItem extends MemoryItem {
    @Override
    public MemoryRegion getMemory(ItemStack stack) {
        MemoryRegion region = new MemoryRegion();
        byte[] data = FileManager.readFile("srom", this.getUUID(stack));
        if (data != null) {
            System.arraycopy(data, 0, region.getData(), 0x100, data.length);
        }
        return region;
    }

    @Override
    public String getStorageDirectoryName() {
        return "srom";
    }

    @Override
    public boolean isFlashable() {
        return true;
    }
}
