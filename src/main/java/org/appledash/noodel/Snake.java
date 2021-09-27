package org.appledash.noodel;

import org.appledash.noodel.util.Vec2;

import java.util.LinkedList;

public class Snake {
    public Direction direction = Direction.RIGHT;
    public Direction prevDirection = this.direction;

    private final LinkedList<Vec2> path = new LinkedList<>();

    public Snake(Vec2 position) {
        this.path.add(position);

        for (int i = 1; i <= 3; i++) {
            this.path.add(position.sub(i, 0));
        }
    }

    public void move(boolean hasEaten) {
        this.path.addFirst(this.getHeadPos().add(this.direction.offset));

        if (!hasEaten) {
            this.path.removeLast();
        }

        this.prevDirection = this.direction;
    }

    public boolean isIntersectingWith(Vec2 pos) {
        return this.getHeadPos().equals(pos);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public LinkedList<Vec2> getPath() {
        return this.path;
    }

    public Vec2 getHeadPos() {
        return this.path.getFirst();
    }

    public Vec2 getNextPos() {
        return this.getHeadPos().add(this.direction.offset);
    }

    public enum Direction {
        UP(new Vec2(0, 1)),
        DOWN(new Vec2(0, -1)),
        LEFT(new Vec2(-1, 0)),
        RIGHT(new Vec2(1, 0));

        private final Vec2 offset;

        Direction(Vec2 offset) {
            this.offset = offset;
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
