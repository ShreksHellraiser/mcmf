package com.github.shrekshellraiser.item;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.shrekshellraiser.ModCreativeTab.MOD_TAB;

public class BeetCPUItem extends Item {
    private final int budget;
    public BeetCPUItem(int budget) {
        super(new Properties().tab(MOD_TAB));
        this.budget = budget;
    }

    public int getBudget() {
        return budget;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(new TextComponent(String.format("Budget (IPT): %d", budget)));
        super.appendHoverText(pStack, level, list, tooltipFlag);
    }
}
