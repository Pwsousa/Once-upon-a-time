package engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Camera first-person controlada por teclado.
 *
 * Controles:
 *   W / S          — avancar / recuar  (ao longo do vetor front)
 *   A / D          — strafe esquerda / direita
 *   SPACE / LSHIFT — subir / descer no eixo Y global
 *   Seta Esq/Dir   — rotacionar yaw   (esquerda/direita)
 *   Seta Cima/Bx   — rotacionar pitch (cima/baixo, clampado em +-89 graus)
 *
 * Pipeline:
 *   (yaw, pitch) → front (vetor direcao unitario)
 *   lookAt(position, position+front, up) → view matrix
 */
public final class Camera {

    private final Vector3f position = new Vector3f(0f, 0f, 5f);
    private float yaw   = -90f;  // -90 = olhando para -Z (padrao OpenGL)
    private float pitch =   0f;

    private static final float SPEED     = 3.0f;  // unidades/segundo
    private static final float TURN_RATE = 60.0f; // graus/segundo

    // pre-alocados — evita GC por frame
    private final Vector3f front = new Vector3f(0f, 0f, -1f);
    private final Vector3f up    = new Vector3f(0f, 1f,  0f);
    private final Vector3f right = new Vector3f();
    private final Vector3f temp  = new Vector3f();
    private final Matrix4f view  = new Matrix4f();

    public void update(float dt, Input input) {
        float vel  = SPEED     * dt;
        float turn = TURN_RATE * dt;

        // strafe precisa do vetor right = normalize(front x up)
        boolean needRight = input.isKeyDown(GLFW_KEY_A) || input.isKeyDown(GLFW_KEY_D);
        if (needRight) front.cross(up, right).normalize();

        if (input.isKeyDown(GLFW_KEY_W)) position.add(front.mul(vel, temp));
        if (input.isKeyDown(GLFW_KEY_S)) position.sub(front.mul(vel, temp));
        if (input.isKeyDown(GLFW_KEY_A)) position.sub(right.mul(vel, temp));
        if (input.isKeyDown(GLFW_KEY_D)) position.add(right.mul(vel, temp));
        if (input.isKeyDown(GLFW_KEY_SPACE))      position.y += vel;
        if (input.isKeyDown(GLFW_KEY_LEFT_SHIFT)) position.y -= vel;

        boolean rotated = false;
        if (input.isKeyDown(GLFW_KEY_LEFT))  { yaw   -= turn; rotated = true; }
        if (input.isKeyDown(GLFW_KEY_RIGHT)) { yaw   += turn; rotated = true; }
        if (input.isKeyDown(GLFW_KEY_UP))    { pitch += turn; rotated = true; }
        if (input.isKeyDown(GLFW_KEY_DOWN))  { pitch -= turn; rotated = true; }
        if (rotated) {
            pitch = Math.max(-89f, Math.min(89f, pitch));
            recomputeFront();
        }
    }

    private void recomputeFront() {
        double yr = Math.toRadians(yaw), pr = Math.toRadians(pitch);
        front.set(
            (float)(Math.cos(pr) * Math.cos(yr)),
            (float) Math.sin(pr),
            (float)(Math.cos(pr) * Math.sin(yr))
        ).normalize();
    }

    /** Retorna a view matrix atualizada (reutiliza campo interno — sem alloc). */
    public Matrix4f getViewMatrix() {
        position.add(front, temp); // temp = ponto alvo (eye + front direction)
        return view.lookAt(position, temp, up);
    }
}
