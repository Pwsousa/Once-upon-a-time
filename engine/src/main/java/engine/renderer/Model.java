package engine.renderer;

import engine.core.Cleanable;

/**
 * Bundle de recursos GPU reutilizavel: Mesh + Texture + propriedades de material.
 *
 * Carregado UMA vez; N entidades referenciam o mesmo Model.
 * cleanup() libera os recursos — chamar apenas quando nenhuma entidade
 * mais precisar (geralmente em onCleanup do engine).
 *
 * shininess/specularStrength controlam o highlight especular (Blinn-Phong) no
 * fragment shader — materiais foscos (madeira, tecido) usam valores baixos,
 * materiais polidos (plastico, metal) usam valores altos.
 */
public final class Model implements Cleanable {

    public final Mesh    mesh;
    public final Texture texture;
    public final float   shininess;
    public final float   specularStrength;

    /** Material padrao (plastico levemente polido): shininess 32, specular 0.5. */
    public Model(Mesh mesh, Texture texture) {
        this(mesh, texture, 32f, 0.5f);
    }

    public Model(Mesh mesh, Texture texture, float shininess, float specularStrength) {
        this.mesh             = mesh;
        this.texture          = texture;
        this.shininess        = shininess;
        this.specularStrength = specularStrength;
    }

    @Override
    public void cleanup() {
        mesh.cleanup();
        texture.cleanup();
    }
}
