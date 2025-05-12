package com.github.shrekshellraiser.devices.api;

import com.github.shrekshellraiser.gui.DevicePianoButtons;
import com.github.shrekshellraiser.gui.PianoButtonGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class GenericDeviceScreen<T extends GenericDeviceMenu> extends AbstractContainerScreen<T> implements PianoButtonGroup.ModifiableScreen {
    protected ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/gui/generic_device.png");
    private final T deviceMenu;
//    private final ToggleSwitchButton[] deviceSelectors = new ToggleSwitchButton[16];
    protected PianoButtonGroup<Integer> deviceSelectors;

    public GenericDeviceScreen(T abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        deviceMenu = abstractContainerMenu;
        this.imageHeight = 107;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        init(true);
    }

    protected void init(boolean buttons) {
        super.init();
        int k = this.leftPos;
        int l = this.topPos;
        assert minecraft != null;
        if (buttons) {
            deviceSelectors = new DevicePianoButtons(20, 26, k, l, 1).setCallback(i -> {
                assert minecraft.gameMode != null;
                minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, i);
            }).build(this, minecraft);
        }
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
        int deviceNumber = deviceMenu.data.get(0);
        deviceSelectors.setSelected(deviceNumber).renderBg(poseStack,i,j);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderBackground(poseStack);
        super.render(poseStack, i, j, f);
        int x = this.leftPos;
        int y = this.topPos;
        assert minecraft != null;

        // Switch labels
        int deviceNumber = deviceMenu.data.get(0);
        deviceSelectors.setSelected(deviceNumber).render(poseStack, i, j, f);
        renderTooltip(poseStack, i, j);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 16777215);
    }

    @Override
    public <T2 extends GuiEventListener & NarratableEntry> @NotNull T2 addWidget(T2 guiEventListener) {
        return super.addWidget(guiEventListener);
    }
}
