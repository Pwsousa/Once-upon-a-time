package engine.terrain;

import engine.renderer.Mesh;

/**
 * Fabrica de malhas de terreno — gera Mesh, consumido pelo mesmo pipeline
 * de Model/Entity/Scene usado por qualquer outro objeto (OBJ, Shapes, etc).
 * Terreno nao e um sistema de renderizacao a parte; e apenas mais uma malha.
 *
 * Formato de vertice identico ao resto da engine (8 floats):
 *   layout 0 — posicao  (vec3)
 *   layout 1 — normal   (vec3)
 *   layout 2 — UV       (vec2)
 *
 * flat() gera um grid subdividido (nao um unico quad) no plano XZ, y = 0,
 * centrado na origem — a subdivisao existe desde ja para permitir, no
 * futuro, deslocar cada vertice por um heightmap sem trocar o gerador.
 */
public final class Terrain {

    private Terrain() {}

    /**
     * Terreno plano, textura repetida 1x ao longo de toda a extensao.
     *
     * @param size       largura/profundidade total (unidades do mundo)
     * @param resolution numero de celulas por eixo (ex: 32 → grid 32x32, 33x33 vertices)
     */
    public static Mesh flat(float size, int resolution) {
        return flat(size, resolution, 1f);
    }

    /**
     * Terreno plano no plano XZ (y = 0), centrado na origem.
     *
     * @param size          largura/profundidade total (unidades do mundo)
     * @param resolution    numero de celulas por eixo (ex: 32 → grid 32x32, 33x33 vertices)
     * @param textureRepeat quantas vezes a textura se repete ao longo do terreno
     *                      (requer Texture com wrap GL_REPEAT, ja padrao na engine)
     */
    public static Mesh flat(float size, int resolution, float textureRepeat) {
        if (resolution < 1) throw new IllegalArgumentException("resolution deve ser >= 1");

        int   verticesPerSide = resolution + 1;
        float half            = size / 2f;
        float step            = size / resolution;

        float[] verts = new float[verticesPerSide * verticesPerSide * 8];
        int[]   idx   = new int[resolution * resolution * 6];

        int vi = 0;
        for (int z = 0; z <= resolution; z++) {
            for (int x = 0; x <= resolution; x++) {
                float px = -half + x * step;
                float pz = -half + z * step;
                float u  = (float) x / resolution * textureRepeat;
                float v  = (float) z / resolution * textureRepeat;

                verts[vi++] = px;  verts[vi++] = 0f;  verts[vi++] = pz; // posicao
                verts[vi++] = 0f;  verts[vi++] = 1f;  verts[vi++] = 0f; // normal (+Y, terreno plano)
                verts[vi++] = u;   verts[vi++] = v;                     // uv
            }
        }

        int ii = 0;
        for (int z = 0; z < resolution; z++) {
            for (int x = 0; x < resolution; x++) {
                int topLeft     = z * verticesPerSide + x;
                int topRight    = topLeft + 1;
                int bottomLeft  = (z + 1) * verticesPerSide + x;
                int bottomRight = bottomLeft + 1;

                // mesma ordem de winding do Shapes.plane(): (A,B,C) (A,C,D)
                idx[ii++] = topLeft;  idx[ii++] = topRight;    idx[ii++] = bottomRight;
                idx[ii++] = topLeft;  idx[ii++] = bottomRight; idx[ii++] = bottomLeft;
            }
        }

        return Mesh.create(verts, idx, 3, 3, 2);
    }
}
