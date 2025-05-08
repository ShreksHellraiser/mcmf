package com.github.shrekshellraiser.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import static com.github.shrekshellraiser.network.ModPackets.KEY_INPUT_ID;

public class KeyInputPacket {
    public static void send(char ch) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeChar(ch);
        NetworkManager.sendToServer(KEY_INPUT_ID, buf);
    }
    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player.containerMenu instanceof KeyInputHandler receiver) {
            receiver.handleKey(buf.readChar());
        }
    }
}
