package org.appledash.noodel;

import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GameWindow {
    private final long windowId;

    public GameWindow(int width, int height) {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        this.windowId = glfwCreateWindow(width, height, "Noodell", NULL, NULL);

        if (this.windowId == NULL) {
            throw new IllegalStateException("Failed to create window");
        }
    }

    public void setKeyCallback(GLFWKeyCallbackI callback) {
        glfwSetKeyCallback(this.windowId, callback);
    }

    public void setSizeCallback(GLFWWindowSizeCallbackI callback) {
        glfwSetWindowSizeCallback(this.windowId, callback);
    }

    public void centerOnScreen() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(this.windowId, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            assert vidMode != null;
            glfwSetWindowPos(
                    this.windowId,
                    (vidMode.width() - pWidth.get()) / 2,
                    (vidMode.height() - pHeight.get()) / 2
            );
        }
    }

    public void makeContextCurrent() {
        glfwMakeContextCurrent(this.windowId);
    }

    public void show() {
        glfwShowWindow(this.windowId);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(this.windowId);
    }

    public void setShouldClose(boolean shouldClose) {
        glfwSetWindowShouldClose(this.windowId, shouldClose);
    }

    public long getWindowId() {
        return windowId;
    }
}
