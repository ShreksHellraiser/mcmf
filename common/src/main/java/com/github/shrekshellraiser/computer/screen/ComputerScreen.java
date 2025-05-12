package com.github.shrekshellraiser.computer.screen;

import com.github.shrekshellraiser.gui.ToggleSwitchButton;
import com.github.shrekshellraiser.gui.ToggleSwitchType;
import com.github.shrekshellraiser.serial.terminal.TerminalFont;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {
    public ToggleSwitchButton powerButton;
    public ToggleSwitchButton pauseButton;
    public ToggleSwitchButton stepButton;
    public ToggleSwitchButton argumentButton;
    private final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/gui/computer.png");
    private final ComputerMenu computerMenu;
    private final int powerSwitchX = 25;
    private final int pauseSwitchX = 60;
    private final int stepButtonX = 90;
    private final int argumentButtonX = 120;
    private final int switchY = 102;
    private final TerminalFont segFont = TerminalFont.SEG7;

    public ComputerScreen(ComputerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
        computerMenu = abstractContainerMenu;
        this.imageHeight = 206;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int k = this.leftPos;
        int l = this.topPos;
        assert minecraft != null;
        powerButton = new ToggleSwitchButton(k + powerSwitchX, l + switchY - 5, "", button -> {
            assert minecraft != null;
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }, minecraft.font, ToggleSwitchType.ROCKER);
        pauseButton = new ToggleSwitchButton(k + pauseSwitchX, l + switchY, "Pause", button -> {
            assert minecraft != null;
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
        }, minecraft.font, ToggleSwitchType.TOGGLE);
        stepButton = new ToggleSwitchButton(k + stepButtonX, l + switchY, "Step", button -> {
            assert minecraft != null;
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
        }, minecraft.font, ToggleSwitchType.TOGGLE);
        argumentButton = new ToggleSwitchButton(k + argumentButtonX, l + switchY, "Arg", button -> {
            assert minecraft != null;
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 3);
        }, minecraft.font, ToggleSwitchType.TOGGLE);

        addWidget(powerButton);
        addWidget(pauseButton);
        addWidget(stepButton);
        addWidget(argumentButton);
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
        powerButton.renderBg(poseStack, k, l, computerMenu.isRunning());
        pauseButton.renderBg(poseStack, k, l, computerMenu.isPaused());
        stepButton.renderBg(poseStack, k, l, false);
        argumentButton.renderBg(poseStack, k, l, computerMenu.isArgumentMode());

//        RenderSystem.setShaderTexture(0, segFont.texture);
//        String wst = computerMenu.getWst();
//        String rst = computerMenu.getRst();
//        for (int g = 0; g < 8; g ++) {
//            int idx = 4 + g * 3;
//            int sx = k + 32
//                    + (g * (segFont.width * 2 + 3));
//            segFont.renderString(this, poseStack, sx, l + 43, wst.substring(idx, idx+2));
//            segFont.renderString(this, poseStack, sx, l + 61, rst.substring(idx, idx+2));
//        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderBackground(poseStack);
        super.render(poseStack, i, j, f);
        int x = this.leftPos;
        int y = this.topPos;
        assert minecraft != null;
        powerButton.render(poseStack, i, j, f);
        pauseButton.render(poseStack, i, j, f);
        stepButton.render(poseStack, i, j, f);
        argumentButton.render(poseStack, i, j, f);
        drawString(poseStack, minecraft.font, computerMenu.getWst(), x + 8, y + 43, 16777215);
        drawString(poseStack, minecraft.font, computerMenu.getRst(), x + 8, y + 61, 16777215);
//        drawString(poseStack, minecraft.font, "WST", x + 8, y + 44, 16777215);
//        drawString(poseStack, minecraft.font, "RST", x + 8, y + 62, 16777215);
        drawString(poseStack, minecraft.font, String.format("Vectors %d", computerMenu.getVectors()),
                x + 69, y + 25, 16777215);
        drawString(poseStack, minecraft.font, String.format("PC %04x", computerMenu.getPc()),
                x + 8, y + 26, 16777215);

        renderTooltip(poseStack, i, j);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 16777215);
        this.font.draw(poseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 16777215);
    }
}
