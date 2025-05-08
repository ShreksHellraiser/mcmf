package com.github.shrekshellraiser.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import static com.github.shrekshellraiser.network.ModPackets.*;

public class MouseInputPacket {
    public static void sendClick(int x, int y, int i, boolean click) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(i);
        buf.writeBoolean(click);
        NetworkManager.sendToServer(MOUSE_CLICK_ID, buf);
    }
    public static void sendMove(int x, int y) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(x);
        buf.writeInt(y);
        NetworkManager.sendToServer(MOUSE_MOVE_ID, buf);
    }
    public static void handleClick(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player.containerMenu instanceof MouseInputHandler receiver) {
            int x = buf.readInt(), y = buf.readInt(), i = buf.readInt();
            boolean click = buf.readBoolean();
            if (click) {
                receiver.handleMouseClick(x, y, i);
            } else {
                receiver.handleMouseRelease(x, y, i);
            }
        }
    }
    public static void handleMove(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player.containerMenu instanceof MouseInputHandler receiver) {
            receiver.handleMouseMove(buf.readInt(), buf.readInt());
        }
    }

}
