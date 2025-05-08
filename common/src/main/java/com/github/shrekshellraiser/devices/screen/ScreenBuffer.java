package com.github.shrekshellraiser.devices.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.inventory.ContainerData;

public class ScreenBuffer implements ContainerData {
    private static final int BUFFER_PAD = 4;
    public static final int width = 300;
    public static final int height = 300;
    public static final int AREA = width * height;
    private static final int BIT_DEPTH = 16; // # of pixels stored per int (compression for network)
    private static final int DISPLAY_BUFFER_SIZE = (AREA / BIT_DEPTH);
    public static final int BUFFER_SIZE = BUFFER_PAD + DISPLAY_BUFFER_SIZE;
    private final byte[] layer0;
    private final byte[] layer1; // Foreground
    private final byte[][] layers;
    private int[] colors = {0xffffff, 0x000000, 0x77ddbb, 0xff6622};
    public static final float scale = 0.6f;
    private ContainerData data = this;
    public ScreenBuffer(ContainerData data) {
        this.data = data;
        layer0 = null;
        layer1 = null;
        layers = null;
    }
    public ScreenBuffer() {
        layer0 = new byte[AREA];
        layer1 = new byte[AREA]; // Foreground
        layers = new byte[][]{layer0, layer1};
    }
    @Override
    public int get(int i) {
        if (i < BUFFER_PAD) {
            return colors[i];
        }
        i -= BUFFER_PAD;
        i *= BIT_DEPTH;
        int v = 0;
        assert layer1 != null;
        assert layer0 != null;
        for (int d = 0; d < BIT_DEPTH; d++) {
            byte fg = layer1[i+d];
            v = (v << 2) | ((fg == 0) ? layer0[i+d] : fg);
        }
        return v;
    }

    @Override
    public void set(int i, int j) {
    }

    public void setPixel(int layer, int x, int y, byte val) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        int i = y * width + x;
        assert layers != null;
        layers[layer][i] = val;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }

    private int[] getColors() {
        return new int[]{data.get(0), data.get(1), data.get(2), data.get(3)};
    }

    @Override
    public int getCount() {
        return BUFFER_SIZE;
    }

    public int getPixel(int i) {
        return data.get(i + BUFFER_PAD);
    }

    public void render(Matrix4f matrix4f, int k, int l) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int[] colorList = this.getColors();
        for (int i = 0; i < DISPLAY_BUFFER_SIZE; i++) {
            int pixels = this.getPixel(i);
            for (int di = BIT_DEPTH-1; di >= 0; di--) {
                int pixelIdx = i * BIT_DEPTH + di;
                int x = pixelIdx % width;
                int y = pixelIdx / width;
                int pixel = pixels & 0b11;
                pixels >>= 2;
                int color = colorList[pixel] | 0xff000000;
                float minX = k + (x * scale);
                float maxX = minX + scale;
                float minY = l + (y * scale);
                float maxY = minY + scale;
                bufferBuilder.vertex(matrix4f, minX,  maxY, 0.0F).color(color).endVertex();
                bufferBuilder.vertex(matrix4f, maxX,  maxY, 0.0F).color(color).endVertex();
                bufferBuilder.vertex(matrix4f, maxX,  minY, 0.0F).color(color).endVertex();
                bufferBuilder.vertex(matrix4f, minX,  minY, 0.0F).color(color).endVertex();
            }
        }
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
