package com.github.shrekshellraiser.network;

import com.github.shrekshellraiser.devices.screen.FlasherDeviceMenu;
import com.github.shrekshellraiser.item.memory.FileManager;
import com.github.shrekshellraiser.item.memory.MemoryItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class ModPackets {
    public static final ResourceLocation FILE_UPLOAD_ID = new ResourceLocation(MOD_ID, "file_upload");

    public static void register() {
//        if (Platform.getEnv() == EnvType.SERVER) {
            registerServer();
//        }
    }

    private static void registerServer() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, FILE_UPLOAD_ID, (buf, context) -> {
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
                    FileManager.saveFile(data, item.getLabelForDirectory(), uuid);
                    item.setLabel(itemStack, fn);
                    player.displayClientMessage(new TextComponent(String.format("File %s successfully uploaded!", fn)),
                            false);
                    return;
                }
                player.displayClientMessage(new TextComponent("No writable memory inserted!"),false);
            }
            player.displayClientMessage(new TextComponent("Something went wrong with the upload!"),false);
        });

    }
}
