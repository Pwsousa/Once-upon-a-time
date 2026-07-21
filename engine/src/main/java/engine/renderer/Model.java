package engine.renderer;

import engine.core.Cleanable;
import org.joml.Vector2f;

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
 *
 * atlasOffset/atlasScale selecionam uma sub-regiao da textura (ver TextureAtlas)
 * — default (0,0)/(1,1) usa a textura inteira, sem custo extra para Models
 * que nao usam atlas.
 */
public final class Model implements Cleanable {

    private static final Vector2f ATLAS_IDENTITY_OFFSET = new Vector2f(0f, 0f);
    private static final Vector2f ATLAS_IDENTITY_SCALE   = new Vector2f(1f, 1f);

    public final Mesh    mesh;
    public final Texture texture;
    public final float   shininess;
    public final float   specularStrength;
    public final Vector2f atlasOffset;
    public final Vector2f atlasScale;

    /** Material padrao (plastico levemente polido): shininess 32, specular 0.5. */
    public Model(Mesh mesh, Texture texture) {
        this(mesh, texture, 32f, 0.5f);
    }

    /** Sem atlas — usa a textura inteira (atlasOffset (0,0), atlasScale (1,1)). */
    public Model(Mesh mesh, Texture texture, float shininess, float specularStrength) {
        this(mesh, texture, shininess, specularStrength, ATLAS_IDENTITY_OFFSET, ATLAS_IDENTITY_SCALE);
    }

    /** Com atlas — atlasOffset/atlasScale normalmente vem de TextureAtlas.offsetOf()/tileScale(). */
    public Model(Mesh mesh, Texture texture, float shininess, float specularStrength,
                 Vector2f atlasOffset, Vector2f atlasScale) {
        this.mesh             = mesh;
        this.texture          = texture;
        this.shininess        = shininess;
        this.specularStrength = specularStrength;
        this.atlasOffset      = atlasOffset;
        this.atlasScale       = atlasScale;
    }

    @Override
    public void cleanup() {
        mesh.cleanup();
        texture.cleanup();
    }
}
