package engine;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * Estado instantaneo do teclado — atualizado pelos callbacks GLFW.
 *
 * Controles registrados em Main:
 *   WASD           — mover camera (frente/tras/esquerda/direita)
 *   SPACE / LSHIFT — subir / descer
 *   Setas          — rotacionar camera (yaw / pitch)
 *   ESC            — fechar janela (tratado em Window)
 */
public final class Input {

    private static final int KEY_COUNT = 350; // cobre todos os GLFW_KEY_* (max ~348)
    private final boolean[]  keys      = new boolean[KEY_COUNT];

    /** Chamado pelo callback de teclado do GLFW (package-private). */
    void onKey(int key, int action) {
        if (key >= 0 && key < KEY_COUNT) {
            keys[key] = action != GLFW_RELEASE;
        }
    }

    /** Retorna true enquanto a tecla estiver pressionada. */
    public boolean isKeyDown(int key) {
        return key >= 0 && key < KEY_COUNT && keys[key];
    }
}
