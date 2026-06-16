package engine.renderer;

/**
 * Fabrica de primitivas geometricas — cada metodo retorna um Mesh pronto para GPU.
 *
 * Formato de vertice fixo (compartilhado com os shaders):
 *   layout 0 — posicao  (vec3, 3 floats)
 *   layout 1 — cor      (vec3, 3 floats)
 *   layout 2 — UV       (vec2, 2 floats)
 *
 * Primitivas disponíveis:
 *   cube()              — cubo unitario, 6 faces coloridas (24 verts / 36 idx)
 *   plane()             — plano XZ unitario, Y=0 (4 verts / 6 idx)
 *   sphere(stk, slc)    — esfera UV unitaria, cor mapeada da normal (procedural)
 */
public final class Shapes {

    private Shapes() {}

    // -------------------------------------------------------------------------
    // Cubo
    // -------------------------------------------------------------------------

    /**
     * Cubo unitario centrado na origem, lado 1.
     * Cada face tem cor distinta — red, green, blue, yellow, cyan, magenta.
     * 24 vertices (4 por face) + 36 indices (6 por face).
     */
    public static Mesh cube() {
        //  pos (x, y, z)               cor (r, g, b)            uv (u, v)
        float[] v = {
            // --- frente (+Z) vermelho ---
            -0.5f, +0.5f, +0.5f,    1.0f, 0.3f, 0.3f,    0.0f, 1.0f,
            +0.5f, +0.5f, +0.5f,    1.0f, 0.3f, 0.3f,    1.0f, 1.0f,
            +0.5f, -0.5f, +0.5f,    1.0f, 0.3f, 0.3f,    1.0f, 0.0f,
            -0.5f, -0.5f, +0.5f,    1.0f, 0.3f, 0.3f,    0.0f, 0.0f,
            // --- fundo (-Z) verde ---
            +0.5f, +0.5f, -0.5f,    0.3f, 1.0f, 0.3f,    0.0f, 1.0f,
            -0.5f, +0.5f, -0.5f,    0.3f, 1.0f, 0.3f,    1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,    0.3f, 1.0f, 0.3f,    1.0f, 0.0f,
            +0.5f, -0.5f, -0.5f,    0.3f, 1.0f, 0.3f,    0.0f, 0.0f,
            // --- esquerda (-X) azul ---
            -0.5f, +0.5f, +0.5f,    0.3f, 0.3f, 1.0f,    0.0f, 1.0f,
            -0.5f, +0.5f, -0.5f,    0.3f, 0.3f, 1.0f,    1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,    0.3f, 0.3f, 1.0f,    1.0f, 0.0f,
            -0.5f, -0.5f, +0.5f,    0.3f, 0.3f, 1.0f,    0.0f, 0.0f,
            // --- direita (+X) amarelo ---
            +0.5f, +0.5f, -0.5f,    1.0f, 1.0f, 0.3f,    0.0f, 1.0f,
            +0.5f, +0.5f, +0.5f,    1.0f, 1.0f, 0.3f,    1.0f, 1.0f,
            +0.5f, -0.5f, +0.5f,    1.0f, 1.0f, 0.3f,    1.0f, 0.0f,
            +0.5f, -0.5f, -0.5f,    1.0f, 1.0f, 0.3f,    0.0f, 0.0f,
            // --- topo (+Y) ciano ---
            -0.5f, +0.5f, -0.5f,    0.3f, 1.0f, 1.0f,    0.0f, 1.0f,
            +0.5f, +0.5f, -0.5f,    0.3f, 1.0f, 1.0f,    1.0f, 1.0f,
            +0.5f, +0.5f, +0.5f,    0.3f, 1.0f, 1.0f,    1.0f, 0.0f,
            -0.5f, +0.5f, +0.5f,    0.3f, 1.0f, 1.0f,    0.0f, 0.0f,
            // --- base (-Y) magenta ---
            -0.5f, -0.5f, +0.5f,    1.0f, 0.3f, 1.0f,    0.0f, 1.0f,
            +0.5f, -0.5f, +0.5f,    1.0f, 0.3f, 1.0f,    1.0f, 1.0f,
            +0.5f, -0.5f, -0.5f,    1.0f, 0.3f, 1.0f,    1.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,    1.0f, 0.3f, 1.0f,    0.0f, 0.0f,
        };

        // 2 triangulos por face, 6 faces
        int[] idx = new int[36];
        for (int f = 0; f < 6; f++) {
            int b = f * 4, o = f * 6;
            idx[o]   = b;     idx[o+1] = b+1;  idx[o+2] = b+2;
            idx[o+3] = b;     idx[o+4] = b+2;  idx[o+5] = b+3;
        }

        return Mesh.create(v, idx, 3, 3, 2);
    }

    // -------------------------------------------------------------------------
    // Plano
    // -------------------------------------------------------------------------

    /**
     * Plano unitario no plano XZ (y = 0), de -0.5 a +0.5 em X e Z.
     * Util como chao ou superficie horizontal. Scale via Transform para ajustar.
     */
    public static Mesh plane() {
        //  pos (x, y, z)               cor (r, g, b)            uv (u, v)
        float[] v = {
            -0.5f, 0.0f, -0.5f,    0.75f, 0.75f, 0.75f,    0.0f, 0.0f,
            +0.5f, 0.0f, -0.5f,    0.75f, 0.75f, 0.75f,    1.0f, 0.0f,
            +0.5f, 0.0f, +0.5f,    0.75f, 0.75f, 0.75f,    1.0f, 1.0f,
            -0.5f, 0.0f, +0.5f,    0.75f, 0.75f, 0.75f,    0.0f, 1.0f,
        };
        int[] idx = { 0, 1, 2, 0, 2, 3 };
        return Mesh.create(v, idx, 3, 3, 2);
    }

    // -------------------------------------------------------------------------
    // Esfera UV
    // -------------------------------------------------------------------------

    /**
     * Esfera UV unitaria centrada na origem, raio 0.5.
     *
     * Cor mapeada da normal (posicao na esfera unitaria):
     *   r = 0.5 + 0.5*x,  g = 0.5 + 0.5*y,  b = 0.5 + 0.5*z
     * Isso cria um gradiente de cores suave que realca a curvatura mesmo sem lighting.
     *
     * @param stacks divisoes de latitude  (recomendado: 16–32)
     * @param slices divisoes de longitude (recomendado: 32–64)
     */
    public static Mesh sphere(int stacks, int slices) {
        int vertexCount = (stacks + 1) * (slices + 1);
        float[] verts   = new float[vertexCount * 8];          // 8 floats por vertice
        int[]   idx     = new int  [stacks * slices * 6];      // 2 triangulos por quad

        int vi = 0, ii = 0;

        for (int i = 0; i <= stacks; i++) {
            double theta = Math.PI * i / stacks;               // 0 (topo) → PI (base)
            float  y     = (float)  Math.cos(theta);           // +1 no topo, -1 na base
            float  r     = (float)  Math.sin(theta);           // raio do anel

            for (int j = 0; j <= slices; j++) {
                double phi = 2.0 * Math.PI * j / slices;
                float  x   = r * (float) Math.cos(phi);
                float  z   = r * (float) Math.sin(phi);

                // cor a partir da normal — realca curvatura sem iluminacao
                float cr = 0.5f + 0.5f * x;
                float cg = 0.5f + 0.5f * y;
                float cb = 0.5f + 0.5f * z;

                float u = (float) j / slices;
                float v = (float) i / stacks;

                verts[vi++] = x * 0.5f; verts[vi++] = y * 0.5f; verts[vi++] = z * 0.5f;
                verts[vi++] = cr;        verts[vi++] = cg;        verts[vi++] = cb;
                verts[vi++] = u;         verts[vi++] = v;
            }
        }

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int r1 = i       * (slices + 1) + j;
                int r2 = (i + 1) * (slices + 1) + j;
                // triangulo superior do quad
                idx[ii++] = r1;     idx[ii++] = r2;     idx[ii++] = r1 + 1;
                // triangulo inferior do quad
                idx[ii++] = r1 + 1; idx[ii++] = r2;     idx[ii++] = r2 + 1;
            }
        }

        return Mesh.create(verts, idx, 3, 3, 2);
    }
}
