package org.appledash.noodel;

import org.appledash.noodel.render.TexturedQuadRenderer;
import org.appledash.noodel.texture.Terrain;
import org.appledash.noodel.texture.Texture2D;
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

public class NoodelMain {
    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 1200;
    private static final int SCALED_WIDTH = 800;
    private static final int SCALED_HEIGHT = 600;
    private static final int TILE_SIZE = 10;
    private static final int TILES_X = SCALED_WIDTH / TILE_SIZE;
    private static final int TILES_Y = SCALED_HEIGHT / TILE_SIZE;
    private static final int UPDATE_FREQUENCY = 100;

    private long window;
    private TexturedQuadRenderer quadRenderer;
    private long lastUpdate = -1;
    private final Snake snake = new Snake();
    private final Set<Vec2> apples = new HashSet<>();
    private final Random random = new Random();
    private boolean wantReset;


    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        this.window = glfwCreateWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT, "Noodell", NULL, NULL);
        if (this.window == NULL) {
            throw new IllegalStateException("Failed to create window");
        }

        glfwSetKeyCallback(this.window, (window, key, scancode, action, modifiers) -> {
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
        });

        glfwSetWindowSizeCallback(this.window, (window, width, height) -> {
            glViewport(0, 0, width, height);
        });

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
        glfwSwapInterval(0); // vsync
        glfwShowWindow(this.window);

        GL.createCapabilities();

        try (MemoryStack stack = stackPush()) {
            IntBuffer intBuffer = stack.mallocInt(1);
            glGenVertexArrays(intBuffer);
            glBindVertexArray(intBuffer.get());
        }


        this.quadRenderer = new TexturedQuadRenderer(Texture2D.fromResource("textures/terrain.png"), 16, 16);

        this.reset();
    }

    private Vec2 findApplePos() {
        Vec2 pos;

        do {
            pos = new Vec2((1 + this.random.nextInt(TILES_X - 2)), (1 + this.random.nextInt(TILES_Y - 2)));
        } while (this.apples.contains(pos) && this.snake.path.contains(pos));

        return pos;
    }

    private void spawnApple() {
        this.apples.add(this.findApplePos());
    }

    private void mainLoop() {
        glClearColor(0.0f, 0.0f, 0.4f, 0.0f);

        int i = 0;
        float n = 0;
        while (!glfwWindowShouldClose(this.window)) {
            long now = System.currentTimeMillis();

            if ((now - this.lastUpdate) >= UPDATE_FREQUENCY) {
                this.update();
                this.lastUpdate = now;
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

            for (Vec2 pathComponent : this.snake.path) {
                this.drawTile(pathComponent.x(), pathComponent.y(), Terrain.LIME_WOOL);
            }

            for (Vec2 appleLocation : this.apples) {
                this.drawTile(appleLocation.x(), appleLocation.y(), Terrain.RED_WOOL);
            }

            this.quadRenderer.draw();

            glfwSwapBuffers(this.window);
            glfwPollEvents();

            if (this.wantReset) {
                this.reset();
                this.wantReset = false;
            }

            long end = System.currentTimeMillis();

            n += end - now;

            if (i == 10) {
                // System.out.printf("ms per frame = %.2f\n", (n / (float) i));
                // System.out.printf("frames per second = %.2f\n", 1000.0f / (n / (float) i));

                i = 0;
                n = 0;
            }

            i++;
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

        for (Vec2 snakeSegment : this.snake.path.subList(0, this.snake.path.size() - 2)) {
            if (nextPos.equals(snakeSegment)) {
                this.wantReset = true;
                break;
            }
        }
    }

    private void reset() {
        this.apples.clear();
        this.snake.reset(new Vec2(TILES_X / 2, TILES_Y / 2));

        for (int i = 0; i < 5; i++) {
            this.spawnApple();
        }
    }

    private void drawTile(int tileX, int tileY, int blockID) {
        int startX = tileX * TILE_SIZE;
        int startY = tileY * TILE_SIZE;
        this.quadRenderer.putQuad(startX, startY, TILE_SIZE, TILE_SIZE, blockID);
        // this.quadRenderer.draw();
    }

    public static void main(String[] args) {
        NoodelMain noodelMain = new NoodelMain();
        try {
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
