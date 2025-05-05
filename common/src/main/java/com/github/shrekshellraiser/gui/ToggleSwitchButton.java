package com.github.shrekshellraiser.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public class ToggleSwitchButton extends Button {
    static private final ResourceLocation texture = new ResourceLocation(MOD_ID, "textures/gui/switches.png");

    private ToggleSwitchType type;
    private String label;
    private Font font;
    private boolean enabled;
    private LabelPosition labelPosition;
    public ToggleSwitchButton(int x, int y, String label, OnPress onPress, Font font, ToggleSwitchType type, LabelPosition position) {
        super(x - type.hw, y - type.hh, type.w, type.w, new TextComponent(label), onPress);
        this.type = type;
        this.label = label;
        this.font = font;
        this.labelPosition = position;
        this.enabled = true;
    }
    public ToggleSwitchButton(int x, int y, String label, OnPress onPress, Font font, ToggleSwitchType type) {
        this(x, y, label, onPress, font, type, LabelPosition.ABOVE);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void onPress() {
        if (this.enabled) {
            super.onPress();
        }
    }

    public void renderBg(PoseStack poseStack, int i, int j, boolean state) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        int ty = enabled ? (state ? type.onTy : type.offTy) : type.disabledTY;
        this.blit(poseStack, x, y, type.tx, ty, type.w, type.h);
    }

    public void render(PoseStack poseStack, int i, int j, float f, boolean offset) {
        switch (labelPosition) {
            case ABOVE -> drawCenteredString(poseStack, font, label, x + type.hw, y - type.hh + 2, 16777215);
            case RIGHT -> drawString(poseStack, font, label, x + type.w, y + type.hh / 2, 16777215);
            case ON -> {
                int sx = x + type.hw;
                int sy = y + type.hh / 2;
                if (offset) {
                    sx += 1;
                    sy += 1;
                }
                drawCenteredString(poseStack, font, label, sx, sy, 16777215);
            }
        }
    }

    @Override
    public void render(PoseStack stack, int i, int j, float f) {
        render(stack, i, j, f, false);
    }

    public enum LabelPosition {
        ABOVE,
        RIGHT,
        ON;
    }
}
