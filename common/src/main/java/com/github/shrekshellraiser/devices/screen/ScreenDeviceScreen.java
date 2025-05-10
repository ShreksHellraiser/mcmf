package com.github.shrekshellraiser.devices.screen;

import com.github.shrekshellraiser.network.KeyInputPacket;
import com.github.shrekshellraiser.network.MouseInputPacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class ScreenDeviceScreen extends AbstractContainerScreen<ScreenDeviceMenu> {
    private final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/gui/serial_terminal.png");
    ScreenDeviceMenu menu;
    public ScreenDeviceScreen(ScreenDeviceMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        menu = abstractContainerMenu;
        this.imageWidth = (int) (menu.buffer.getWidth() * ScreenBuffer.scale);
        this.imageHeight = (int) (menu.buffer.getHeight() * ScreenBuffer.scale);
        init();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        RenderSystem.setShaderTexture(0, this.texture);
        this.imageWidth = (int) (menu.buffer.getWidth() * ScreenBuffer.scale);
        this.imageHeight = (int) (menu.buffer.getHeight() * ScreenBuffer.scale);
        init();
        int k = this.leftPos;
        int l = this.topPos;
//        assert minecraft != null;
//        this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);
        menu.buffer.render(poseStack.last().pose(), k, l);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderBg(poseStack, f, i, j);
//        super.render(poseStack, i, j, f);
//        renderTooltip(poseStack, i, j);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == InputConstants.KEY_ESCAPE) { // Might need to change this to not be hardcoded in the future
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int i) {
        KeyInputPacket.send(c);
        return false;
    }

    @Override
    public void mouseMoved(double d, double e) {
        int x = (int) ((d - this.leftPos) / ScreenBuffer.scale);
        int y = (int) ((e - this.topPos) / ScreenBuffer.scale);
        if (x < 0 || y < 0 || x >= menu.buffer.getWidth() || y >= menu.buffer.getHeight()) return;
        MouseInputPacket.sendMove(x, y);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        int x = (int) ((d - this.leftPos) / ScreenBuffer.scale);
        int y = (int) ((e - this.topPos) / ScreenBuffer.scale);
        if (x < 0 || y < 0 || x >= menu.buffer.getWidth() || y >= menu.buffer.getHeight()) return false;
        MouseInputPacket.sendClick(x, y, i, true);
        return true;
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        int x = (int) ((d - this.leftPos) / ScreenBuffer.scale);
        int y = (int) ((e - this.topPos) / ScreenBuffer.scale);
        if (x < 0 || y < 0 || x >= menu.buffer.getWidth() || y >= menu.buffer.getHeight()) return false;
        MouseInputPacket.sendClick(x, y, i, false);
        return true;
    }
}
