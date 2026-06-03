package engine;

import engine.core.WindowConfig;
import engine.renderer.Mesh;
import engine.renderer.ShaderProgram;
import org.joml.Matrix4f;

/**
 * Demo: quad com pipeline GLSL MVP completo.
 *
 * Shaders:
 *   vertex.glsl   — transforma vertices com uModel * uView * uProjection
 *   fragment.glsl — aplica cor interpolada com pulso animado via uTime
 *
 * Uniforms enviados por frame:
 *   uModel      — rotacao do quad no eixo Z ao longo do tempo
 *   uView       — camera fixa (identity por enquanto)
 *   uProjection — projecao ortografica (identity por enquanto)
 *   uTime       — tempo acumulado em segundos
 */
public final class Main extends Engine {

    private Mesh          quad;
    private ShaderProgram shader;

    // matrizes MVP — alocadas uma vez, reutilizadas toda frame
    private final Matrix4f model = new Matrix4f();
    private final Matrix4f view  = new Matrix4f();
    private final Matrix4f proj  = new Matrix4f();

    private float time = 0f;

    //          posicao (x, y, z)       cor (r, g, b)
    private static final float[] VERTICES = {
        -0.5f,  0.5f, 0.0f,    1.0f, 0.0f, 0.0f,   // 0 — superior esquerdo  (vermelho)
         0.5f,  0.5f, 0.0f,    0.0f, 1.0f, 0.0f,   // 1 — superior direito   (verde)
         0.5f, -0.5f, 0.0f,    0.0f, 0.0f, 1.0f,   // 2 — inferior direito   (azul)
        -0.5f, -0.5f, 0.0f,    1.0f, 1.0f, 0.0f,   // 3 — inferior esquerdo  (amarelo)
    };

    private static final int[] INDICES = {
        0, 1, 2,   // triangulo superior
        0, 2, 3,   // triangulo inferior
    };

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    protected WindowConfig createWindowConfig() {
        return new WindowConfig.Builder("3D Engine — Shaders GLSL")
                .width(1280)
                .height(720)
                .vsync(true)
                .build();
    }

    @Override
    protected void onInit() {
        shader = new ShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");
        quad   = Mesh.create(VERTICES, INDICES, 3, 3);

        // view e projecao fixas por enquanto (camera e projecao serao implementadas depois)
        // identity = sem transformacao de camera / sem perspectiva
        view.identity();
        proj.identity();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        time += deltaTime;

        // rotaciona o quad 90 graus por segundo no eixo Z
        model.identity().rotateZ(time);
    }

    @Override
    protected void onRender() {
        renderer.render(shader, s -> {
            s.setUniform("uModel",      model);
            s.setUniform("uView",       view);
            s.setUniform("uProjection", proj);
            s.setUniform("uTime",       time);
        }, quad);
    }

    @Override
    protected void onCleanup() {
        quad.cleanup();
        shader.cleanup();
    }
}
