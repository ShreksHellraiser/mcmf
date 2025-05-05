package com.github.shrekshellraiser.computer.screen;

import com.github.shrekshellraiser.item.memory.MemoryItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MemorySlot extends Slot {
    public MemorySlot(Container container, int i, int j, int k) {
        super(container, i, j, k);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return itemStack.getItem() instanceof MemoryItem;
    }
}
