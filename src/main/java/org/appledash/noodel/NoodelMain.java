package org.appledash.noodel;

import org.appledash.noodel.render.TexturedQuadRenderer;
import org.appledash.noodel.texture.Terrain;
import org.appledash.noodel.texture.Texture2D;
import org.appledash.noodel.util.FrameCounter;
import org.appledash.noodel.util.Vec2;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import javax.swing.*;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class NoodelMain {
    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 1200;
    private static final int SCALED_WIDTH = 800;
    private static final int SCALED_HEIGHT = 600;
    private static final int TILE_SIZE = 10;
    private static final int TILES_X = SCALED_WIDTH / TILE_SIZE;
    private static final int TILES_Y = SCALED_HEIGHT / TILE_SIZE;
    private static final int UPDATE_FREQUENCY = 100;

    private final Random random = new Random();
    private final Set<Vec2> apples = new HashSet<>();
    private final FrameCounter frameCounter = new FrameCounter();

    private long window;
    private TexturedQuadRenderer quadRenderer;

    private long lastUpdate = -1;
    private Snake snake;
    private boolean wantReset;

    private void init() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        this.window = glfwCreateWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT, "Noodell", NULL, NULL);

        if (this.window == NULL) {
            throw new IllegalStateException("Failed to create window");
        }

        glfwSetKeyCallback(this.window, this::keyCallback);
        glfwSetWindowSizeCallback(this.window, (window, width, height) ->
                glViewport(0, 0, width, height)
        );

        /* Center the window */
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(this.window, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            assert vidMode != null;
            glfwSetWindowPos(
                    this.window,
                    (vidMode.width() - pWidth.get()) / 2,
                    (vidMode.height() - pHeight.get()) / 2
            );
        }

        glfwMakeContextCurrent(this.window);
        glfwSwapInterval(1); // vsync
        glfwShowWindow(this.window);

        GL.createCapabilities();

        glBindVertexArray(glGenVertexArrays());

        this.quadRenderer = new TexturedQuadRenderer(Texture2D.fromResource("textures/terrain.png"), 16, 16);

        this.reset();
    }

    private Vec2 findApplePos() {
        Vec2 pos;

        do {
            pos = new Vec2((1 + this.random.nextInt(TILES_X - 2)), (1 + this.random.nextInt(TILES_Y - 2)));
        } while (this.apples.contains(pos) && this.snake.getPath().contains(pos)); // FIXME - Slow, have to walk the whole linked list for the snek

        return pos;
    }

    private void spawnApple() {
        this.apples.add(this.findApplePos());
    }

    private void mainLoop() {
        glClearColor(0.0f, 0.0f, 0.4f, 0.0f);

        while (!glfwWindowShouldClose(this.window)) {
            long frameStart = System.currentTimeMillis();

            if ((frameStart - this.lastUpdate) >= UPDATE_FREQUENCY) {
                this.update();
                this.lastUpdate = frameStart;
            }

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Top and bottom border
            for (int x = 0; x < TILES_X; x++) {
                this.drawTile(x, 0, Terrain.OBSIDIAN);
                this.drawTile(x, TILES_Y - 1, Terrain.OBSIDIAN);
            }

            // Left and right border
            for (int y = 1; y < TILES_Y - 1; y++) {
                this.drawTile(0, y, Terrain.OBSIDIAN);
                this.drawTile(TILES_X - 1, y, Terrain.OBSIDIAN);
            }

            this.drawTiles(this.apples, Terrain.RED_WOOL);
            this.drawTiles(this.snake.getPath(), Terrain.LIME_WOOL);

            this.quadRenderer.draw();

            glfwSwapBuffers(this.window);
            glfwPollEvents();

            if (this.wantReset) {
                this.reset();
                this.wantReset = false;
            }

            this.frameCounter.update(System.currentTimeMillis() - frameStart);
        }

        this.quadRenderer.delete();
    }

    private void update() {
        boolean hasEaten = false;
        for (Iterator<Vec2> iter = this.apples.iterator(); iter.hasNext();) {
            Vec2 applePos = iter.next();
            if (this.snake.isIntersectingWith(applePos)) {
                iter.remove();
                hasEaten = true;
                break;
            }
        }

        this.snake.move(hasEaten);

        if (hasEaten) {
            this.spawnApple();
        }

        Vec2 nextPos = this.snake.getNextPos();

        if (nextPos.x() == 0 || nextPos.x() == TILES_X || nextPos.y() == 0 || nextPos.y() == TILES_Y) {
            this.wantReset = true;
        }

        /* this is a little weird, but the reason I do it this way is that the snake's path is a linked list,
         * and iterating like this is faster. */
        int pathSize = this.snake.getPath().size();
        Iterator<Vec2> iter = this.snake.getPath().iterator();

        for (int i = 0; iter.hasNext(); i++) {
            /* don't care about colliding with our own head or our tail (said tail is about to disappear) */
            if (i == 0 || i == pathSize - 2) {
                continue;
            }

            if (nextPos.equals(iter.next())) {
                this.wantReset = true;
                break;
            }
        }
    }

    private void drawTile(int tileX, int tileY, int blockID) {
        this.quadRenderer.putQuad(tileX * TILE_SIZE, tileY * TILE_SIZE, TILE_SIZE, TILE_SIZE, blockID);
    }

    private void drawTiles(Iterable<Vec2> tiles, int blockId) {
        for (Vec2 pos : tiles) {
            this.drawTile(pos.x(), pos.y(), blockId);
        }
    }

    private void reset() {
        this.apples.clear();
        this.snake = new Snake(new Vec2(TILES_X / 2, TILES_Y / 2));

        for (int i = 0; i < 5; i++) {
            this.spawnApple();
        }
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(this.window, true);
        }

        if (action != GLFW_PRESS) {
            return;
        }

        Snake.Direction whereIWantToGo = switch (key) {
            case GLFW_KEY_W -> Snake.Direction.UP;
            case GLFW_KEY_A -> Snake.Direction.LEFT;
            case GLFW_KEY_S -> Snake.Direction.DOWN;
            case GLFW_KEY_D -> Snake.Direction.RIGHT;
            default -> this.snake.direction;
        };

        if (whereIWantToGo != this.snake.direction && whereIWantToGo != this.snake.prevDirection.reverse()) {
            this.snake.setDirection(whereIWantToGo);
        }
    }

    public static void main(String[] args) {
        NoodelMain noodelMain = new NoodelMain();
        try {
            GLFWErrorCallback.createPrint(System.err).set();

            if (!glfwInit()) {
                throw new IllegalStateException("Failed to initialize GLFW");
            }

            noodelMain.init();
            noodelMain.mainLoop();
        } catch (Exception e) {
            e.printStackTrace();
            alertError("Error!", e.getMessage());
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private static void alertError(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private static void alertInfo(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
        System.out.println(title + " - " + message);
    }


}
