package org.appledash.noodel;

import java.awt.image.BufferedImage;

public class SpriteSheet {
    private final Texture2D texture;
    private final int spriteWidth;
    private final int spriteHeight;
    private final int xCount;
    private final int yCount;

    public SpriteSheet(BufferedImage bufferedImage, int spriteWidth, int spriteHeight) {
        this(new Texture2D(bufferedImage), spriteWidth, spriteHeight);
    }

    public SpriteSheet(Texture2D texture, int spriteWidth, int spriteHeight) {
        this.texture = texture;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.xCount = texture.getWidth() / spriteWidth;
        this.yCount = texture.getHeight() / spriteHeight;
    }

    // upper 16 bits = U, lower 16 bits = V
    public int getSpriteUV(int spriteIndex) {
        int row = spriteIndex / this.xCount;
        int col = spriteIndex % this.yCount;

        return ((row * this.spriteWidth) << Short.SIZE) | (col * this.spriteHeight);
    }

    public Texture2D getTexture() {
        return this.texture;
    }

    public int getSpriteWidth() {
        return this.spriteWidth;
    }

    public int getSpriteHeight() {
        return this.spriteHeight;
    }

    public void delete() {
        this.texture.delete();
    }

    public static SpriteSheet fromResource(String resourcePath, int spriteWidth, int spriteHeight) {
        return new SpriteSheet(
                Texture2D.fromResource(resourcePath),
                spriteWidth,
                spriteHeight
        );
    }
}
