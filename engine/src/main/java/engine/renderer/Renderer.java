package engine.renderer;

import static org.lwjgl.opengl.GL33.*;

/**
 * Servico de renderizacao stateless — nao possui ownership de recursos.
 * Ciclo de vida de Mesh e ShaderProgram e responsabilidade do chamador.
 */
public final class Renderer {

    public void init() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
    }

    public void render(ShaderProgram shader, Mesh... meshes) {
        shader.bind();
        for (Mesh mesh : meshes) {
            mesh.draw();
        }
        shader.unbind();
    }
}
