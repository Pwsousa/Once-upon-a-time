package engine.renderer;

import org.joml.Vector2f;

/**
 * Atlas de texturas — 1 unica imagem dividida em grid (columns x rows) de
 * sub-regioes (tiles). Permite que N tipos de material (grama, terra, pedra...)
 * compartilhem UMA textura na GPU, sem trocar bind por tipo.
 *
 * Combinado com tiling (UV repetido, ver Terrain.flat), o fragment shader
 * usa fract() para repetir o padrao DENTRO da sub-regiao do atlas, sem
 * vazar para tiles vizinhos — mosaico (repeticao) + atlas (selecao de
 * regiao) funcionando juntos, uma unica textura na GPU.
 *
 * Uso:
 *   TextureAtlas atlas = new TextureAtlas(new Texture("textures/terrain_atlas.png"), 4, 4);
 *   Model grass = new Model(mesh, atlas.texture, 2f, 0.05f,
 *                           atlas.offsetOf(0, 0), atlas.tileScale());
 */
public final class TextureAtlas {

    public final Texture texture;
    private final int    columns;
    private final int    rows;

    public TextureAtlas(Texture texture, int columns, int rows) {
        this.texture = texture;
        this.columns = columns;
        this.rows    = rows;
    }

    /** Offset (u,v) do canto inferior-esquerdo do tile (col,row) dentro do atlas. */
    public Vector2f offsetOf(int col, int row) {
        return new Vector2f((float) col / columns, (float) row / rows);
    }

    /** Escala (u,v) de um tile — fracao do atlas ocupada por uma sub-regiao. */
    public Vector2f tileScale() {
        return new Vector2f(1f / columns, 1f / rows);
    }
}
