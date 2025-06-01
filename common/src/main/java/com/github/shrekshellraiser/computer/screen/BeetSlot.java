package com.github.shrekshellraiser.computer.screen;

import com.github.shrekshellraiser.item.memory.MemoryItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.github.shrekshellraiser.item.ModItems.UXN_CPU;

public class BeetSlot extends Slot {
    public BeetSlot(Container container, int i, int j, int k) {
        super(container, i, j, k);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return itemStack.is(UXN_CPU);
    }
}
