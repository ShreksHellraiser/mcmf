package com.github.shrekshellraiser.network;

import com.github.shrekshellraiser.devices.screen.ScreenBuffer;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

public class ScreenUpdatePacket {
    ScreenBuffer buffer;
    FriendlyByteBuf byteBuf;
    public ScreenUpdatePacket(ScreenBuffer buffer) {
        this.buffer = buffer;
        refresh();
    }
    public void refresh() {
        byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        buffer.encode(byteBuf);
    }
    public void send(ArrayList<ServerPlayer> players) {
        NetworkManager.sendToPlayers(players, ModPackets.SCREEN_UPDATE_ID, byteBuf);
    }
    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player.containerMenu instanceof ScreenBufferHandler receiver) {
            receiver.screenBufferUpdate(buf);
        }
    }
}
