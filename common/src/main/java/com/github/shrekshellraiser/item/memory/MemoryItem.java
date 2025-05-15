package com.github.shrekshellraiser.item.memory;

import com.github.shrekshellraiser.core.uxn.MemoryRegion;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static com.github.shrekshellraiser.ModCreativeTab.MOD_TAB;

public abstract class MemoryItem extends Item {
    private final static String UUID_TAG = "mcmf.uuid";
    private final static String LABEL_TAG = "mcmf.label";
    public MemoryItem() {
        super(new Properties().tab(MOD_TAB));
    }

    public String getUUID(ItemStack stack, boolean createIfMissing) {
        String uuid = null;
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            assert tag != null;
            if (tag.contains(UUID_TAG))
                uuid = tag.getString(UUID_TAG);
            else {
                uuid = UUID.randomUUID().toString();
                tag.putString(UUID_TAG, uuid);
                stack.setTag(tag);
            }
        } else if (createIfMissing) {
            CompoundTag tag = new CompoundTag();
            uuid = UUID.randomUUID().toString();
            tag.putString(UUID_TAG, uuid);
            stack.setTag(tag);
        }
        return uuid;
    }
    public String getUUID(ItemStack stack) {
        return getUUID(stack, true);
    }

    public @Nullable String getLabel(ItemStack pStack) {
        if (pStack.hasTag() && pStack.getTag().contains(LABEL_TAG)) {
            return pStack.getTag().getString(LABEL_TAG);
        }
        return null;
    }

    public void setLabel(ItemStack stack, String label) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            assert tag != null;
            tag.putString(LABEL_TAG, label);
            stack.setTag(tag);
        } else {
            CompoundTag tag = new CompoundTag();
            tag.putString(LABEL_TAG, label);
            stack.setTag(tag);
        }
    }

    public abstract boolean isFlashable();

    public abstract String getStorageDirectoryName();

    public abstract MemoryRegion getMemory(ItemStack stack);

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        String label = getLabel(pStack);
        if (label != null) {
            list.add(new TextComponent(label));
        }
        String uuid = getUUID(pStack, false);
        if (Screen.hasShiftDown() && uuid != null) {
            list.add(new TextComponent(uuid));
        }
        super.appendHoverText(pStack, level, list, tooltipFlag);
    }
}
