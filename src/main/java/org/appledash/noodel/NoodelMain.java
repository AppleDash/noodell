package org.appledash.noodel;

import org.appledash.noodel.render.TexturedQuadRenderer;
import org.appledash.noodel.texture.Terrain;
import org.appledash.noodel.texture.Texture2D;
import org.appledash.noodel.util.FrameCounter;
import org.appledash.noodel.util.Vec2;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import javax.swing.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public final class NoodelMain {
    /* the actual window size */
    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 1200;

    /* correspond to the values mapped in the shader */
    private static final int SCALED_WIDTH = 800;
    private static final int SCALED_HEIGHT = 600;

    private static final int TILE_SIZE = 10;
    private static final int TILES_X = SCALED_WIDTH / TILE_SIZE;
    private static final int TILES_Y = SCALED_HEIGHT / TILE_SIZE;
    private static final int UPDATE_INTERVAL = 100; /* in milliseconds */

    private final World world = new World(TILES_X, TILES_Y);
    private final FrameCounter frameCounter = new FrameCounter();

    private GameWindow window;
    private TexturedQuadRenderer quadRenderer;

    private boolean paused;
    private long lastUpdate = -1;

    private void init() {
        this.window = new GameWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        this.window.setKeyCallback(this::keyCallback);
        this.window.setSizeCallback((window, width, height) ->
                glViewport(0, 0, width, height)
        );

        this.window.centerOnScreen();
        this.window.makeContextCurrent();
        glfwSwapInterval(1); // vsync
        this.window.show();

        GL.createCapabilities();

        glBindVertexArray(glGenVertexArrays());

        this.quadRenderer = new TexturedQuadRenderer(Texture2D.fromResource("textures/terrain.png"), 16, 16);
        this.world.reset();
    }



    private void mainLoop() {
        glClearColor(0.0f, 0.0f, 0.4f, 0.0f);

        while (!this.window.shouldClose()) {
            long frameStart = System.currentTimeMillis();

            if (((frameStart - this.lastUpdate) >= UPDATE_INTERVAL) && !this.paused) {
                this.world.update();
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

            this.drawTiles(this.world.getApples(), Terrain.RED_WOOL);
            this.drawTiles(this.world.getSnake().getPath(), Terrain.LIME_WOOL);

            this.quadRenderer.draw();

            glfwSwapBuffers(this.window.getWindowId());
            glfwPollEvents();

            this.frameCounter.update(System.currentTimeMillis() - frameStart);
        }

        this.quadRenderer.delete();
    }

    private void drawTile(int tileX, int tileY, int blockID) {
        this.quadRenderer.putQuad(tileX * TILE_SIZE, tileY * TILE_SIZE, TILE_SIZE, TILE_SIZE, blockID);
    }

    private void drawTiles(Iterable<Vec2> tiles, int blockId) {
        for (Vec2 pos : tiles) {
            this.drawTile(pos.x(), pos.y(), blockId);
        }
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            this.paused = !this.paused;
            // this.window.setShouldClose(true);
        }

        if (action != GLFW_PRESS) {
            return;
        }

        if (this.paused) {
            return;
        }

        Snake snake = this.world.getSnake();

        Snake.Direction desiredDir = switch (key) {
            case GLFW_KEY_W -> Snake.Direction.UP;
            case GLFW_KEY_A -> Snake.Direction.LEFT;
            case GLFW_KEY_S -> Snake.Direction.DOWN;
            case GLFW_KEY_D -> Snake.Direction.RIGHT;
            default -> snake.direction;
        };

        if (desiredDir != snake.direction && desiredDir != snake.prevDirection.reverse()) {
            snake.setDirection(desiredDir);
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
