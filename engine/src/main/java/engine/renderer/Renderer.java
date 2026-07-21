package engine.renderer;

import java.util.function.Consumer;

import static org.lwjgl.opengl.GL33.*;

/**
 * Servico de renderizacao stateless — sem ownership de recursos GPU.
 *
 * Dois modos de uso:
 *
 * 1) Sem uniforms (ex: debug, geometria estatica):
 *      renderer.render(shader, mesh);
 *
 * 2) Com uniforms por objeto via lambda (padrao para cenas 3D):
 *      renderer.render(shader, s -> {
 *          s.setUniform("uModel", modelMatrix);
 *          s.setUniform("uTime",  time);
 *      }, mesh);
 */
public final class Renderer {

    public void init() {
        glEnable(GL_DEPTH_TEST);

        // alpha blending — pixels opacos (alpha=1) nao sao afetados,
        // entao fica sempre ligado sem custo visual para texturas sem transparencia
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
    }

    /**
     * Liga/desliga escrita no depth buffer.
     *
     * Objetos semi-transparentes (blending real, nao apenas cutout) devem
     * desenhar com depth write desligado — senao um pixel translucido grava
     * profundidade "solida" e bloqueia o que deveria aparecer atras dele.
     * O depth TEST continua ligado (objetos atras de solidos ainda somem).
     */
    public void setDepthWrite(boolean enabled) {
        glDepthMask(enabled);
    }

    /**
     * Renderiza meshes sem configuracao de uniforms adicional.
     */
    public void render(ShaderProgram shader, Mesh... meshes) {
        render(shader, null, meshes);
    }

    /**
     * Renderiza meshes com configuracao de uniforms via lambda.
     * O lambda e chamado com o shader ja vinculado (bind feito antes).
     *
     * @param uniformSetup consumer que recebe o shader e seta os uniforms.
     *                     Pode ser null se nao houver uniforms a configurar.
     */
    public void render(ShaderProgram shader, Consumer<ShaderProgram> uniformSetup, Mesh... meshes) {
        shader.bind();

        if (uniformSetup != null) {
            uniformSetup.accept(shader);
        }

        for (Mesh mesh : meshes) {
            mesh.draw();
        }

        shader.unbind();
    }
}
