package org.appledash.noodel;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;

public class Texture2D {
    private final int textureId;
    private final int width;
    private final int height;
    private final ByteBuffer buffer;

    public Texture2D(BufferedImage bufferedImage) {
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

    public static Texture2D fromResource(String resourcePath) {
        try {
            return new Texture2D(ImageIO.read(Objects.requireNonNull(Texture2D.class.getClassLoader().getResourceAsStream(resourcePath))));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load resource " + resourcePath, e);
        }
    }

    private static ByteBuffer swizzleBufferedImage(BufferedImage bufferedImage) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(bufferedImage.getWidth() * bufferedImage.getHeight() * 4);

        int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());

        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                int pixel = pixels[y * bufferedImage.getWidth() + x];

                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) ((pixel) & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        buffer.flip();

        return buffer;
    }


}
