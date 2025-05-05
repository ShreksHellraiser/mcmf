package com.github.shrekshellraiser.serial.screen;

import com.github.shrekshellraiser.gui.ToggleSwitchButton;
import com.github.shrekshellraiser.gui.ToggleSwitchType;
import com.github.shrekshellraiser.serial.TerminalFont;
import com.github.shrekshellraiser.serial.block.entity.SerialTerminalContainerData;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class SerialTerminalScreen extends AbstractContainerScreen<SerialTerminalMenu> {
    private final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/gui/serial_terminal.png");
    private final SerialTerminalMenu termMenu;
    private ToggleSwitchButton echoToggle;
    private Button switchFontButton;
    private ToggleSwitchButton resetButton;
    private boolean blinkVisible = true;
    private long blinkTime = System.currentTimeMillis();
    private final static int BLINK_INTERVAL = 500;

    public SerialTerminalScreen(SerialTerminalMenu termMenu, Inventory inventory, Component component) {
        super(termMenu, inventory, component);
        this.termMenu = termMenu;
        this.imageHeight = 211;
        this.imageWidth = 251;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int k = this.leftPos;
        int l = this.topPos;

        assert minecraft != null;
        echoToggle = new ToggleSwitchButton(k + 21, l + 200, "Echo", button -> {
            assert minecraft != null;
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }, minecraft.font, ToggleSwitchType.TOGGLE, ToggleSwitchButton.LabelPosition.RIGHT);

        switchFontButton = new Button(k + 205, l + 192, 30, 17, new TextComponent("Font"), button -> {
            assert minecraft != null;
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
            onClose();
        });

        resetButton = new ToggleSwitchButton(k + 77, l + 200, "Reset", button -> {
            assert minecraft != null;
            assert minecraft.gameMode != null;
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
            onClose();
        }, minecraft.font, ToggleSwitchType.TOGGLE, ToggleSwitchButton.LabelPosition.RIGHT);

        addWidget(echoToggle);
        addWidget(switchFontButton);
        addWidget(resetButton);
    }

    private void sendKey(char ch) {
        assert minecraft != null;
        assert minecraft.gameMode != null;
        minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 3 + ch);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
//        return GuiEventListener.super.keyPressed(i, j, k);
        if (Screen.isCopy(i)) {
//            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            return true;
        } else if (Screen.isPaste(i)) {
//                this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());

            return true;
        } else if (Screen.isCut(i)) {
//            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());

            return true;
        }
        if (i == InputConstants.KEY_BACKSPACE) {
            sendKey((char)127);
            return true;
        } else if (i == InputConstants.KEY_RETURN) {
            sendKey('\n');
            return true;
        } else if (i == InputConstants.KEY_ESCAPE) { // Might need to change this to not be hardcoded in the future
            this.onClose();
            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(char ch, int i) {
        if (ch >= 32 && ch <= 126 || ch >= 160 && ch <= 255) {
            sendKey(ch);
            return true;
        }
        return false;
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

        int swidth = termMenu.data.get(SerialTerminalContainerData.WIDTH_INDEX);
        int sheight = termMenu.data.get(SerialTerminalContainerData.HEIGHT_INDEX);
        int fontIndex = termMenu.data.get(SerialTerminalContainerData.FONT_INDEX);
        TerminalFont font = TerminalFont.vals[fontIndex];
        RenderSystem.setShaderTexture(0, font.texture);
        for (int c = 0; c < swidth * sheight; c++ ) {
            int cx = c % swidth;
            int cy = c / swidth;
            int ch = termMenu.data.get(c + SerialTerminalContainerData.DATA_SIZE);
            font.render(this, poseStack, k + 5, l + 5, cx, cy, ch);
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - blinkTime >= BLINK_INTERVAL) {
            blinkTime = currentTime;
            blinkVisible = !blinkVisible;
        }
        if (blinkVisible) {
            int cx = termMenu.data.get(SerialTerminalContainerData.CURSOR_X_INDEX);
            int cy = termMenu.data.get(SerialTerminalContainerData.CURSOR_Y_INDEX);
            font.renderCursor(this, poseStack, k + 5, l + 5, cx, cy);
        }
        echoToggle.renderBg(poseStack, k, l, menu.data.get(SerialTerminalContainerData.ECHO_INDEX) == 1);
        resetButton.renderBg(poseStack, k, l, false);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        renderBackground(poseStack);
        super.render(poseStack, i, j, f);
        int x = this.leftPos;
        int y = this.topPos;
        assert minecraft != null;
        echoToggle.render(poseStack, i, j, f);
        switchFontButton.render(poseStack, i, j, f);
        resetButton.render(poseStack, i, j, f);

        // Switch labels
        renderTooltip(poseStack, i, j);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
    }
}

