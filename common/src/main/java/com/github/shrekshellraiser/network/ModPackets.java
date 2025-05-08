package com.github.shrekshellraiser.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.resources.ResourceLocation;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class ModPackets {
    public static final ResourceLocation FILE_UPLOAD_ID = new ResourceLocation(MOD_ID, "file_upload");
    public static final ResourceLocation KEY_INPUT_ID = new ResourceLocation(MOD_ID, "key_input");
    public static final ResourceLocation MOUSE_CLICK_ID = new ResourceLocation(MOD_ID, "mouse_click");
    public static final ResourceLocation MOUSE_MOVE_ID = new ResourceLocation(MOD_ID, "mouse_move");

    public static void register() {
//        if (Platform.getEnv() == EnvType.SERVER) {
            registerServer();
//        }
    }

    private static void registerServer() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, FILE_UPLOAD_ID, FileUploadPacket::receive);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, KEY_INPUT_ID, KeyInputPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, MOUSE_CLICK_ID, MouseInputPacket::handleClick);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, MOUSE_MOVE_ID, MouseInputPacket::handleMove);
    }
}
