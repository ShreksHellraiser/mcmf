package com.github.shrekshellraiser.network;

import com.github.shrekshellraiser.devices.flasher.FlasherDeviceMenu;
import com.github.shrekshellraiser.item.memory.FileManager;
import com.github.shrekshellraiser.item.memory.MemoryItem;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;

import static com.github.shrekshellraiser.network.ModPackets.FILE_UPLOAD_ID;

public class FileUploadPacket {
    public static boolean send(String fn, byte[] data) {
        if (data.length > 0xFFFF) {
            // TOO LARGE
            return false;
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        byte[] fnBytes = fn.getBytes(StandardCharsets.UTF_8);
        buf.writeByteArray(fnBytes);
        buf.writeByteArray(data);
        NetworkManager.sendToServer(FILE_UPLOAD_ID, buf);
        return true;
    }
    public static void receive(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player.containerMenu instanceof FlasherDeviceMenu flasher) {
            String fn = new String(buf.readByteArray(), StandardCharsets.UTF_8);
            ItemStack itemStack = flasher.blockEntity.getItem(0);
            if (itemStack.getItem() instanceof MemoryItem item) {
                if (!item.isFlashable()) {
                    return;
                }
                String uuid = item.getUUID(itemStack);
                byte[] data = buf.readByteArray();
                FileManager.saveFile(data, item.getStorageDirectoryName(), uuid);
                item.setLabel(itemStack, fn);
                player.displayClientMessage(new TextComponent(String.format("File %s successfully uploaded!", fn)),
                        false);
                return;
            }
            player.displayClientMessage(new TextComponent("No writable memory inserted!"),false);
        }
        player.displayClientMessage(new TextComponent("Something went wrong with the upload!"),false);
    }
}
