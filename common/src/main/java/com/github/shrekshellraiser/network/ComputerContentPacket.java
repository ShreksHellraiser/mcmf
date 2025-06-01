package com.github.shrekshellraiser.network;

import com.github.shrekshellraiser.computer.block.entity.ComputerBlockEntity;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ComputerContentPacket {
    public static void send(ItemStack stack, BlockPos pos, List<ServerPlayer> players) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeItem(stack);
        buf.writeBlockPos(pos);
        NetworkManager.sendToPlayers(players, ModPackets.COMPUTER_CONTENT_ID, buf);
    }
    public static void receive(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ItemStack stack = buf.readItem();
        BlockPos pos = buf.readBlockPos();
        context.queue(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null && level.getBlockEntity(pos) instanceof ComputerBlockEntity receiver) {
                receiver.setItem(1, stack);
            }
        });
    }
}
