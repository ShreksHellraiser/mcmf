package com.github.shrekshellraiser.computer.screen;

import com.github.shrekshellraiser.item.memory.MemoryItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FlashableMemorySlot extends Slot {
    public FlashableMemorySlot(Container container, int i, int j, int k) {
        super(container, i, j, k);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MemoryItem item) {
            return item.isFlashable();
        }
        return false;
    }
}
