package com.github.shrekshellraiser.serial;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import static com.github.shrekshellraiser.ComputerMod.MOD_ID;

public enum TerminalFont {
    COMPUTERCRAFT(8, 11, "term_font.png", -2),
    TIMES9K(5, 9, "times9k_font.png"),
    PICO8(5, 7, "pico8_font.png"),
    UNIFONT(8, 16, "unifont_font.png"),
    CGA(8,19,"cga_font.png"),
    TERMINUS(7,13,"terminus_font.png"),
    SEG7(7, 11, "7seg_font.png");
    public final int width;
    public final int height;
    public final int hpad;
    public final String path;
    public final ResourceLocation texture;

    TerminalFont(int width, int height, String path, int hpad) {
        this.width = width;
        this.height = height;
        this.path = path;
        this.hpad = hpad;
        this.texture = new ResourceLocation(MOD_ID, "textures/gui/fonts/" + path);
    }
    TerminalFont(int width, int height, String path) {
        this(width, height, path, 0);
    }

    public void render(Screen screen, PoseStack poseStack, int x, int y, int ch) {
        int charX = ch % 16;
        int charY = ch / 16;
        int charFromX = charX * this.width;
        int charFromY = charY * this.height;
        screen.blit(poseStack, x, y, charFromX, charFromY, this.width, this.height);
    }
    public void renderCursor(Screen screen, PoseStack poseStack, int sx, int sy, int cx, int cy) {
        int cpx = cx * (this.width + this.hpad);
        int cpy = cy * this.height;
        render(screen, poseStack, sx + cpx - this.width / 2, sy + cpy, '|');
    }

    public void render(Screen screen, PoseStack poseStack, int sx, int sy, int cx, int cy, int ch) {
        int cpx = cx * (this.width + this.hpad);
        int cpy = cy * this.height;
        render(screen, poseStack, sx + cpx, sy + cpy, ch);
    }
    public void renderString(Screen screen, PoseStack poseStack, int sx, int sy, String s) {
        for (int i = 0; i < s.length(); i++) {
            render(screen, poseStack, sx, sy, i, 0, s.charAt(i));
        }
    }

    public static final TerminalFont[] vals = values();

    public TerminalFont next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }
}
