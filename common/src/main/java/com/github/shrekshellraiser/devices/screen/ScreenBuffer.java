package com.github.shrekshellraiser.devices.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

import static com.github.shrekshellraiser.ComputerMod.LOGGER;

public class ScreenBuffer {
    private static final int BUFFER_PAD = 6;
    public static final int MAX_WIDTH = 640;
    public static final int MAX_HEIGHT = 480;
    private int width = 300;
    private int height = 300;
    public static final int MAX_AREA = MAX_WIDTH * MAX_HEIGHT;
    private static final int BIT_DEPTH = 16; // # of pixels stored per int (compression for network)
    private static final int DISPLAY_BUFFER_SIZE = (MAX_AREA / BIT_DEPTH);
    public static final int BUFFER_SIZE = BUFFER_PAD + DISPLAY_BUFFER_SIZE;
    private final byte[] layer0;
    private final byte[] layer1; // Foreground
    private final byte[][] layers;
    private int[] colors = {0xffffff, 0x000000, 0x77ddbb, 0xff6622};
    public static final float scale = 0.6f;
    private int[] lastPacketBuffer = new int[BUFFER_SIZE];
    private boolean bufferChanged = true;
    private int[] renderPacketBuffer;
    public ScreenBuffer() {
        layer0 = new byte[MAX_AREA];
        layer1 = new byte[MAX_AREA]; // Foreground
        layers = new byte[][]{layer0, layer1};
    }

    public void decode(FriendlyByteBuf buf) {
        width = buf.readInt();
        height = buf.readInt();
        colors = buf.readVarIntArray(4);
        lastPacketBuffer = buf.readVarIntArray(BUFFER_SIZE);
        bufferChanged = true;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        if (width > MAX_WIDTH) {
            width = MAX_WIDTH;
        }
        this.width = width;
    }
    public void setHeight(int height) {
        if (height > MAX_HEIGHT) {
            height = MAX_HEIGHT;
        }
        this.height = height;
    }
    public int getHeight() {
        return height;
    }
    private int getPixelColor(int idx) {
        if (idx < 0 || idx >= MAX_AREA) return 0;
        byte fg = layer1[idx];
        return (fg == 0) ? layer0[idx] : fg;
    }
    private int[] encodeRLE() {
        List<Integer> buffer = new ArrayList<>(BUFFER_SIZE);
        int bufIndex = 0;
        int pixelIndex = 0;
        int area = width * height;
        while (pixelIndex <= area - 15) {  // Check for 15 pixels worth of space
            // Read first 15 pixels
            int[] pixels = new int[15];
            boolean allSame = true;
            pixels[0] = getPixelColor(pixelIndex++);

            for (int i = 1; i < 15; i++) {
                pixels[i] = getPixelColor(pixelIndex++);
                if (pixels[i] != pixels[0]) allSame = false;
            }

            int data;
            if (allSame) {
                // RLE mode
                data = 0x80000000 | (pixels[0] << 28);
                int repeats = 15;
                while (getPixelColor(pixelIndex++) == pixels[0]) {
                    repeats++;
                    if (repeats >= 0x0EFFFFFF) {
                        pixelIndex++;
                        break;
                    };
                    if (pixelIndex >= area) break;
                }
                pixelIndex--;
                data |= repeats;
            } else {
                // Pack 15 pixels, 2 bits each
                data = 0;
                for (int i = 0; i < 15; i++) {
                    data |= (pixels[i] << (28 - i * 2));
                }
            }
            bufIndex++;
            buffer.add(data);
        }

        // Handle remaining pixels if any
        if (pixelIndex < area) {
            int data = 0;
            int remaining = Math.min(15, area - pixelIndex);
            for (int i = 0; i < remaining; i++) {
                data |= (getPixelColor(pixelIndex++) << (30 - i * 2));
            }
            buffer.add(data);
        }
        if (bufIndex >= BUFFER_SIZE) {
            buffer = buffer.subList(0, BUFFER_SIZE);
            LOGGER.warn("Screen buffer size exceeded ({} of allowed {})", bufIndex, BUFFER_SIZE);
        }
        return buffer.stream().mapToInt(Integer::intValue).toArray();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(width);
        buf.writeInt(height);
        buf.writeVarIntArray(colors);
        buf.writeVarIntArray(encodeRLE());
    }


    public void setPixel(int layer, int x, int y, byte val) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        int i = y * width + x;
        layers[layer][i] = val;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }

    private int[] getColors() {
        return colors;
    }

    public int getPixel(int i) {
        return layer0[i];
    }

    public void render(Matrix4f matrix4f, int k, int l) {
        renderBuffer(matrix4f, k, l);
    }

    private void rect(int x0, int y0, int x1, int y1, int color, VertexConsumer bufferBuilder, Matrix4f matrix4f, int k, int l) {
        float minX = k + (x0 * scale);
        float maxX = minX + ((x1 - x0) * scale);
        float minY = l + (y0 * scale);
        float maxY = minY + ((y1 - y0) * scale);
        color |= 0xff000000;
        bufferBuilder.vertex(matrix4f, minX,  maxY, 0.0F).color(color).endVertex();
        bufferBuilder.vertex(matrix4f, maxX,  maxY, 0.0F).color(color).endVertex();
        bufferBuilder.vertex(matrix4f, maxX,  minY, 0.0F).color(color).endVertex();
        bufferBuilder.vertex(matrix4f, minX,  minY, 0.0F).color(color).endVertex();
    }

    private void renderBuffer(Matrix4f matrix4f, int k, int l) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        width = getWidth();
        height = getHeight();
        int area = width * height;
        int[] colorList = this.getColors();
        int pixelIndex = 0;
        int startX, startY, endX = 0, endY = 0;
        if (bufferChanged) {
            renderPacketBuffer = lastPacketBuffer.clone();
            bufferChanged = false;
        }
        for (int i = 0; i < renderPacketBuffer.length && pixelIndex < area; i++) {
            int v = renderPacketBuffer[i];
            startX = pixelIndex % width;
            startY = pixelIndex / width;
            if ((v & 0x80000000) == 0x80000000) {
                // RLE mode
                int color = (v & 0x30000000) >> 28;
                int length = v & 0x0FFFFFFF;
                if (pixelIndex + length > area) {
                    length = area - pixelIndex;
                }
                for (int j = 0; j < length; j++) {
                    endX = pixelIndex % width;
                    endY = pixelIndex / width;
                    if (endY != startY) {
                        rect(startX, startY, width, endY, colorList[color], bufferBuilder, matrix4f, k, l);
                        startX = endX;
                        startY = endY;
                    }
                    pixelIndex++;
                }
                rect(startX, startY, endX + 1, endY + 1, colorList[color], bufferBuilder, matrix4f, k, l);
            } else {
                // Unpack 15 pixels
                int remainingPixels = Math.min(15, area - pixelIndex);
                for (int j = 0; j < remainingPixels; j++) {
                    int pixel = (v >> (28 - j * 2)) & 0b11;
                    int x = pixelIndex % width;
                    int y = pixelIndex / width;
                    pixelIndex++;
                    rect(x, y, x+1, y+1, colorList[pixel], bufferBuilder, matrix4f, k, l);
                }
            }
        }
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
