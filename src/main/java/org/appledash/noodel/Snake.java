package org.appledash.noodel;

import org.appledash.noodel.util.Vec2;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    public Direction direction = Direction.RIGHT;
    public Direction prevDirection = this.direction;

    public final List<Vec2> path = new ArrayList<>();

    public void reset(Vec2 position) {
        this.direction = Direction.RIGHT;
        this.prevDirection = this.direction;

        this.path.clear();
        this.path.add(position);

        for (int i = 1; i <= 3; i++) {
            this.path.add(position.add(new Vec2(-i, 0)));
        }
    }

    public void move(boolean hasEaten) {
        this.path.add(0, this.path.get(0).add(this.direction.modifier));

        if (!hasEaten) {
            this.path.remove(this.path.size() - 1);
        }

        this.prevDirection = this.direction;
    }

    public boolean isIntersectingWith(Vec2 pos) {
        Vec2 head = this.path.get(0);

        return head.equals(pos);
    }

    public boolean isCollidedWith(Vec2 pos) {
        Vec2 head = this.path.get(0);

        return head.add(this.direction.modifier).equals(pos);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Vec2 getHeadPos() {
        return this.path.get(0);
    }

    public Vec2 getNextPos() {
        return this.getHeadPos().add(this.direction.modifier);
    }

    public enum Direction {
        UP(new Vec2(0, 1)),
        DOWN(new Vec2(0, -1)),
        LEFT(new Vec2(-1, 0)),
        RIGHT(new Vec2(1, 0));

        private final Vec2 modifier;

        Direction(Vec2 modifier) {
            this.modifier = modifier;
        }

        public Direction reverse() {
            return switch (this) {
                case UP -> DOWN;
                case DOWN -> UP;
                case LEFT -> RIGHT;
                case RIGHT -> LEFT;
            };
        }
    }
}
