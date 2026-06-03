package engine;

import engine.core.WindowConfig;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Responsabilidade unica: gerenciar janela GLFW e contexto OpenGL.
 * Nao contem logica de jogo ou renderizacao.
 */
public final class Window {

    private final WindowConfig config;
    private long handle;

    public Window(WindowConfig config) {
        this.config = config;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Falha ao inicializar GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE,   GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, config.resizable ? GLFW_TRUE : GLFW_FALSE);

        handle = glfwCreateWindow(config.width, config.height, config.title, NULL, NULL);
        if (handle == NULL) {
            throw new RuntimeException("Falha ao criar janela GLFW");
        }

        glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        glfwSetFramebufferSizeCallback(handle, (window, w, h) -> glViewport(0, 0, w, h));

        centerOnScreen();

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(config.vsync ? 1 : 0);
        glfwShowWindow(handle);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void swapAndPoll() {
        glfwSwapBuffers(handle);
        glfwPollEvents();
    }

    public void destroy() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public long getHandle() {
        return handle;
    }

    private void centerOnScreen() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth  = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(handle, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vidmode != null) {
                glfwSetWindowPos(handle,
                        (vidmode.width()  - pWidth.get(0))  / 2,
                        (vidmode.height() - pHeight.get(0)) / 2);
            }
        }
    }
}
