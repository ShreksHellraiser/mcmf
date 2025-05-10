package com.github.shrekshellraiser.network;

import com.github.shrekshellraiser.devices.screen.ScreenBuffer;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

public class ScreenUpdatePacket {
    public static void send(ArrayList<ServerPlayer> players, ScreenBuffer buffer) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buffer.encode(buf);
        NetworkManager.sendToPlayers(players, ModPackets.SCREEN_UPDATE_ID, buf);
    }
    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player.containerMenu instanceof ScreenBufferHandler receiver) {
            receiver.screenBufferUpdate(buf);
        }
    }
}
