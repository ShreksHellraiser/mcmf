package com.github.shrekshellraiser.serial.infuser;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class InfuserScreen extends AbstractContainerScreen<InfuserMenu> {
    protected ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/gui/infuser.png");
    public InfuserScreen(InfuserMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.texture);
        int k = this.leftPos;
        int l = this.topPos;
        assert minecraft != null;
        this.blit(poseStack, k, l, 0, 0, this.imageWidth, this.imageHeight);

        renderProgressArrow(poseStack, k, l);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderBackground(poseStack);
        super.render(poseStack, i, j, f);

        renderTooltip(poseStack, i, j);
    }

    private void renderProgressArrow(PoseStack poseStack, int x, int y) {
        this.blit(poseStack, x + 85, y + 30, 176, 0, 8, menu.getScaledProgress());
    }
}
