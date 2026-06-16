package engine;

import engine.core.WindowConfig;
import engine.renderer.Renderer;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL33.*;

/**
 * Classe base do motor — padrao Template Method.
 *
 * Subclasse define comportamento via:
 *   createWindowConfig() — configuracao da janela
 *   onInit()             — carrega recursos (shaders, meshes, texturas)
 *   onUpdate(dt)         — logica por frame (input, fisica, animacao)
 *   onRender()           — chamadas de renderizacao
 *   onCleanup()          — libera recursos GPU
 *   onResize(w, h)       — opcional; chamado em resize do framebuffer
 */
public abstract class Engine {

    protected Renderer renderer;
    protected Input    input  = new Input();
    private   Window   window;

    public final void start() {
        window = new Window(createWindowConfig(), input);
        window.setResizeCallback(this::onResize);
        window.init();

        GL.createCapabilities();
        printGpuInfo();

        renderer = new Renderer();
        renderer.init();

        onInit();
        gameLoop();
        onCleanup();
        window.destroy();
    }

    protected abstract WindowConfig createWindowConfig();
    protected abstract void onInit();
    protected abstract void onUpdate(float deltaTime);
    protected abstract void onRender();
    protected abstract void onCleanup();

    /** Override para reagir ao resize e atualizar a projection matrix. */
    protected void onResize(int width, int height) {}

    /** Aspect ratio atual da janela (atualiza apos resize). */
    protected float getAspectRatio() { return window.getAspectRatio(); }

    private void gameLoop() {
        long lastTime = System.nanoTime();

        while (!window.shouldClose()) {
            long  now       = System.nanoTime();
            float deltaTime = (now - lastTime) / 1_000_000_000f;
            lastTime = now;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            onUpdate(deltaTime);
            onRender();

            window.swapAndPoll();
        }
    }

    private void printGpuInfo() {
        System.out.println("OpenGL : " + glGetString(GL_VERSION));
        System.out.println("GPU    : " + glGetString(GL_RENDERER));
        System.out.println("GLSL   : " + glGetString(GL_SHADING_LANGUAGE_VERSION));
    }
}
