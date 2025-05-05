package com.github.shrekshellraiser.item;

import com.github.shrekshellraiser.core.uxn.UXNBus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import static com.github.shrekshellraiser.ModCreativeTab.MOD_TAB;

public class NetworkDebugItem extends Item {
    public NetworkDebugItem() {
        super(new Properties().tab(MOD_TAB));
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockPos pos = useOnContext.getClickedPos();
        Level level = useOnContext.getLevel();
        UXNBus bus = UXNBus.findBus(level, pos);
        if (bus != null && !level.isClientSide) {
            Player player = useOnContext.getPlayer();
            assert player != null;

            player.displayClientMessage(new TextComponent(bus.dumpStatus()), false);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
