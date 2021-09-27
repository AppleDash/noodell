package org.appledash.noodel.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Vec2(int x, int y) {
    @Contract("_ -> new")
    public @NotNull Vec2 add(@NotNull Vec2 other) {
        return new Vec2(this.x + other.x, this.y + other.y);
    }

    @Contract("_ -> new")
    public @NotNull Vec2 sub(@NotNull Vec2 other) {
        return new Vec2(this.x - other.x, this.y - other.y);
    }

    @Contract("_, _ -> new")
    public @NotNull Vec2 add(int x, int y) {
        return new Vec2(this.x + x, this.y + y);
    }

    @Contract("_, _ -> new")
    public @NotNull Vec2 sub(int x, int y) {
        return new Vec2(this.x - x, this.y - y);
    }

    @Contract("_, _ -> new")
    public static @NotNull Vec2 of(int x, int y) {
        return new Vec2(x, y);
    }
}
