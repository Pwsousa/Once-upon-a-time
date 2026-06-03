package engine;

import engine.core.WindowConfig;
import engine.renderer.Mesh;
import engine.renderer.ShaderProgram;

/**
 * Demonstracao minima do motor: triangulo RGB com OpenGL 3.3 core profile.
 * Este e o ponto de entrada — estenda Engine para criar seu jogo.
 */
public final class Main extends Engine {

    private Mesh triangle;
    private ShaderProgram shader;

    // vertices interleaved: posicao (x,y,z) + cor (r,g,b)
    private static final float[] VERTICES = {
         0.0f,  0.5f, 0.0f,   1.0f, 0.0f, 0.0f,
        -0.5f, -0.5f, 0.0f,   0.0f, 1.0f, 0.0f,
         0.5f, -0.5f, 0.0f,   0.0f, 0.0f, 1.0f,
    };

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    protected WindowConfig createWindowConfig() {
        return new WindowConfig.Builder("3D Engine — Demo")
                .width(1280)
                .height(720)
                .vsync(true)
                .build();
    }

    @Override
    protected void onInit() {
        shader   = new ShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");
        triangle = Mesh.create(VERTICES, 3, 3); // atributo 0: vec3 pos, atributo 1: vec3 cor
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // logica de jogo aqui
    }

    @Override
    protected void onRender() {
        renderer.render(shader, triangle);
    }

    @Override
    protected void onCleanup() {
        triangle.cleanup();
        shader.cleanup();
    }
}
