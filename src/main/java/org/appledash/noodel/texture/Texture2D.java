package org.appledash.noodel.texture;

import org.appledash.noodel.util.ResourceHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture2D {
    private final int textureId;
    private final int width;
    private final int height;
    private final @NotNull ByteBuffer buffer;

    public Texture2D(@NotNull BufferedImage bufferedImage) {
        this.textureId = glGenTextures();
        this.width = bufferedImage.getWidth();
        this.height = bufferedImage.getHeight();
        this.buffer = swizzleBufferedImage(bufferedImage);

        glBindTexture(GL_TEXTURE_2D, this.textureId);

        glTexImage2D(
                GL_TEXTURE_2D, // target
                0, // level
                GL_RGBA8, // internalformat
                this.width, // width
                this.height, // height
                0, // border
                GL_RGBA, // format
                GL_UNSIGNED_BYTE, // type
                this.buffer // buffer
        );

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.textureId);
    }

    public void delete() {
        glDeleteTextures(this.textureId);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Contract("_ -> new")
    public static @NotNull Texture2D fromResource(@NotNull String resourcePath) {
        return new Texture2D(ResourceHelper.getImage(resourcePath));
    }

    @SuppressWarnings("MagicNumber")
    private static @NotNull ByteBuffer swizzleBufferedImage(@NotNull BufferedImage bufferedImage) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(bufferedImage.getWidth() * bufferedImage.getHeight() * 4);

        int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());

        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                int pixel = pixels[y * bufferedImage.getWidth() + x];

                // RGBA
                // @formatter:off
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >>  8) & 0xFF));
                buffer.put((byte) ((pixel      ) & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
                // @formatter:on
            }
        }

        buffer.flip();

        return buffer;
    }


}
