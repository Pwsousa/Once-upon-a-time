package engine;

import engine.core.WindowConfig;
import engine.renderer.Mesh;
import engine.renderer.ShaderProgram;

/**
 * Demo: quad renderizado com VAO + VBO + EBO.
 *
 * Layout dos 4 vertices (NDC):
 *
 *   0(-0.5, 0.5) ──── 1(0.5, 0.5)
 *        │       ╲         │
 *        │         ╲       │
 *   3(-0.5,-0.5) ──── 2(0.5,-0.5)
 *
 * EBO reutiliza vertices — 4 vertices definem 2 triangulos via 6 indices:
 *   triangulo A: 0, 1, 2
 *   triangulo B: 0, 2, 3
 */
public final class Main extends Engine {

    private Mesh quad;
    private ShaderProgram shader;

    //          posicao (x, y, z)     cor (r, g, b)
    private static final float[] VERTICES = {
        -0.5f,  0.5f, 0.0f,   1.0f, 0.0f, 0.0f,   // 0 — canto superior esquerdo  (vermelho)
         0.5f,  0.5f, 0.0f,   0.0f, 1.0f, 0.0f,   // 1 — canto superior direito   (verde)
         0.5f, -0.5f, 0.0f,   0.0f, 0.0f, 1.0f,   // 2 — canto inferior direito   (azul)
        -0.5f, -0.5f, 0.0f,   1.0f, 1.0f, 0.0f,   // 3 — canto inferior esquerdo  (amarelo)
    };

    // 2 triangulos formando o quad — EBO evita duplicar vertices
    private static final int[] INDICES = {
        0, 1, 2,   // triangulo superior
        0, 2, 3,   // triangulo inferior
    };

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    protected WindowConfig createWindowConfig() {
        return new WindowConfig.Builder("3D Engine — Quad VAO/VBO/EBO")
                .width(1280)
                .height(720)
                .vsync(true)
                .build();
    }

    @Override
    protected void onInit() {
        shader = new ShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");

        // atributo 0: vec3 posicao (3 floats)
        // atributo 1: vec3 cor     (3 floats)
        quad = Mesh.create(VERTICES, INDICES, 3, 3);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // logica de jogo aqui
    }

    @Override
    protected void onRender() {
        renderer.render(shader, quad);
    }

    @Override
    protected void onCleanup() {
        quad.cleanup();
        shader.cleanup();
    }
}
