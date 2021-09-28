package org.appledash.noodel.util;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceHelper {
    private ResourceHelper() {
    }

    public static @NotNull String getText(@NotNull String resourcePath) {
        try {
            return new String(getResource(resourcePath).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load text at resource " + resourcePath, e);
        }
    }

    public static @NotNull BufferedImage getImage(@NotNull String resourcePath) {
        try {
            return ImageIO.read(getResource(resourcePath));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load image at resource " + resourcePath, e);
        }
    }

    public static @NotNull InputStream getResource(@NotNull String resourcePath) {
        InputStream localInputStream = ResourceHelper.class.getResourceAsStream(resourcePath);

        if (localInputStream != null) {
            return localInputStream;
        }

        InputStream globalInputStream = ResourceHelper.class.getClassLoader().getResourceAsStream(resourcePath);

        if (globalInputStream != null) {
            return globalInputStream;
        }

        throw new IllegalStateException("Missing resource " + resourcePath);
    }
}
