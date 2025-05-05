package com.github.shrekshellraiser.devices.screen;

import com.github.shrekshellraiser.gui.DevicePianoButtons;
import com.github.shrekshellraiser.gui.PianoButtonGroup;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;
import static com.github.shrekshellraiser.network.ModPackets.FILE_UPLOAD_ID;

public class FlasherDeviceScreen extends GenericDeviceScreen<FlasherDeviceMenu> implements PianoButtonGroup.ModifiableScreen {

    public FlasherDeviceScreen(FlasherDeviceMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        texture = new ResourceLocation(MOD_ID, "textures/gui/flasher_device.png");
        this.imageHeight = 190;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init(false);
        int k = this.leftPos;
        int l = this.topPos;
        assert minecraft != null;
        deviceSelectors = new DevicePianoButtons(20, 26, k, l, 1).setCallback(i -> {
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, i);
        }).build(this, minecraft);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 16777215);
    }

    @Override
    public void onFilesDrop(List<Path> list) {
        if (list.isEmpty()) return;

        assert this.minecraft != null;
        assert this.minecraft.player != null;
        try {
            Path p = list.get(0);
            byte[] data = Files.readAllBytes(p);
            String fn = p.getFileName().toString();
            if (data.length > 0xFFFF) {
                // TOO LARGE
                this.minecraft.player.sendMessage(new TextComponent(String.format("File %s is too large!", fn)), this.minecraft.player.getUUID());
                return;
            }
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            byte[] fnBytes = fn.getBytes(StandardCharsets.UTF_8);
            buf.writeByteArray(fnBytes);
            buf.writeByteArray(data);
            NetworkManager.sendToServer(FILE_UPLOAD_ID, buf);
        } catch (IOException e) {
            this.minecraft.player.sendMessage(new TextComponent("File Upload Failed!"), this.minecraft.player.getUUID());
        }

    }

}
