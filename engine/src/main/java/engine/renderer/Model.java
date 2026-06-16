package engine.renderer;

import engine.core.Cleanable;

/**
 * Bundle de recursos GPU reutilizavel: Mesh + Texture.
 *
 * Carregado UMA vez; N entidades referenciam o mesmo Model.
 * cleanup() libera os recursos — chamar apenas quando nenhuma entidade
 * mais precisar (geralmente em onCleanup do engine).
 */
public final class Model implements Cleanable {

    public final Mesh    mesh;
    public final Texture texture;

    public Model(Mesh mesh, Texture texture) {
        this.mesh    = mesh;
        this.texture = texture;
    }

    @Override
    public void cleanup() {
        mesh.cleanup();
        texture.cleanup();
    }
}
