package engine.renderer;

/**
 * Fabrica de primitivas geometricas — cada metodo retorna um Mesh pronto para GPU.
 *
 * Formato de vertice (8 floats):
 *   layout 0 — posicao  (vec3)
 *   layout 1 — normal   (vec3)  ← unit vector, usado para lighting
 *   layout 2 — UV       (vec2)
 */
public final class Shapes {

    private Shapes() {}

    // -------------------------------------------------------------------------
    // Cubo
    // -------------------------------------------------------------------------

    /**
     * Cubo unitario centrado na origem, lado 1.
     * 24 vertices (4 por face, normais de face perpendiculares) + 36 indices.
     */
    public static Mesh cube() {
        //  pos (x,y,z)               normal (nx,ny,nz)        uv (u,v)
        float[] v = {
            // frente (+Z)  normal: (0, 0, 1)
            -0.5f, +0.5f, +0.5f,   0f, 0f, 1f,   0.0f, 1.0f,
            +0.5f, +0.5f, +0.5f,   0f, 0f, 1f,   1.0f, 1.0f,
            +0.5f, -0.5f, +0.5f,   0f, 0f, 1f,   1.0f, 0.0f,
            -0.5f, -0.5f, +0.5f,   0f, 0f, 1f,   0.0f, 0.0f,
            // fundo (-Z)  normal: (0, 0, -1)
            +0.5f, +0.5f, -0.5f,   0f, 0f, -1f,  0.0f, 1.0f,
            -0.5f, +0.5f, -0.5f,   0f, 0f, -1f,  1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,   0f, 0f, -1f,  1.0f, 0.0f,
            +0.5f, -0.5f, -0.5f,   0f, 0f, -1f,  0.0f, 0.0f,
            // esquerda (-X)  normal: (-1, 0, 0)
            -0.5f, +0.5f, +0.5f,  -1f, 0f, 0f,   0.0f, 1.0f,
            -0.5f, +0.5f, -0.5f,  -1f, 0f, 0f,   1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,  -1f, 0f, 0f,   1.0f, 0.0f,
            -0.5f, -0.5f, +0.5f,  -1f, 0f, 0f,   0.0f, 0.0f,
            // direita (+X)  normal: (1, 0, 0)
            +0.5f, +0.5f, -0.5f,   1f, 0f, 0f,   0.0f, 1.0f,
            +0.5f, +0.5f, +0.5f,   1f, 0f, 0f,   1.0f, 1.0f,
            +0.5f, -0.5f, +0.5f,   1f, 0f, 0f,   1.0f, 0.0f,
            +0.5f, -0.5f, -0.5f,   1f, 0f, 0f,   0.0f, 0.0f,
            // topo (+Y)  normal: (0, 1, 0)
            -0.5f, +0.5f, -0.5f,   0f, 1f, 0f,   0.0f, 1.0f,
            +0.5f, +0.5f, -0.5f,   0f, 1f, 0f,   1.0f, 1.0f,
            +0.5f, +0.5f, +0.5f,   0f, 1f, 0f,   1.0f, 0.0f,
            -0.5f, +0.5f, +0.5f,   0f, 1f, 0f,   0.0f, 0.0f,
            // base (-Y)  normal: (0, -1, 0)
            -0.5f, -0.5f, +0.5f,   0f, -1f, 0f,  0.0f, 1.0f,
            +0.5f, -0.5f, +0.5f,   0f, -1f, 0f,  1.0f, 1.0f,
            +0.5f, -0.5f, -0.5f,   0f, -1f, 0f,  1.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,   0f, -1f, 0f,  0.0f, 0.0f,
        };

        int[] idx = new int[36];
        for (int f = 0; f < 6; f++) {
            int b = f * 4, o = f * 6;
            idx[o]   = b;   idx[o+1] = b+1; idx[o+2] = b+2;
            idx[o+3] = b;   idx[o+4] = b+2; idx[o+5] = b+3;
        }

        return Mesh.create(v, idx, 3, 3, 2);
    }

    // -------------------------------------------------------------------------
    // Plano
    // -------------------------------------------------------------------------

    /**
     * Plano unitario no plano XZ (y = 0), de -0.5 a +0.5 em X e Z.
     * Normal aponta para cima (+Y).
     */
    public static Mesh plane() {
        //  pos (x,y,z)               normal (nx,ny,nz)        uv (u,v)
        float[] v = {
            -0.5f, 0.0f, -0.5f,    0f, 1f, 0f,    0.0f, 0.0f,
            +0.5f, 0.0f, -0.5f,    0f, 1f, 0f,    1.0f, 0.0f,
            +0.5f, 0.0f, +0.5f,    0f, 1f, 0f,    1.0f, 1.0f,
            -0.5f, 0.0f, +0.5f,    0f, 1f, 0f,    0.0f, 1.0f,
        };
        int[] idx = { 0, 1, 2, 0, 2, 3 };
        return Mesh.create(v, idx, 3, 3, 2);
    }

    // -------------------------------------------------------------------------
    // Esfera UV
    // -------------------------------------------------------------------------

    /**
     * Esfera UV unitaria (raio 0.5).
     * Normal = posicao normalizada (outward, perfeita para esfera sem escala).
     *
     * @param stacks divisoes de latitude  (recomendado: 16–32)
     * @param slices divisoes de longitude (recomendado: 32–64)
     */
    public static Mesh sphere(int stacks, int slices) {
        int vertexCount = (stacks + 1) * (slices + 1);
        float[] verts   = new float[vertexCount * 8];
        int[]   idx     = new int[stacks * slices * 6];

        int vi = 0, ii = 0;

        for (int i = 0; i <= stacks; i++) {
            double theta = Math.PI * i / stacks;
            float  y     = (float)  Math.cos(theta);
            float  r     = (float)  Math.sin(theta);

            for (int j = 0; j <= slices; j++) {
                double phi = 2.0 * Math.PI * j / slices;
                float  x   = r * (float) Math.cos(phi);
                float  z   = r * (float) Math.sin(phi);

                float u = (float) j / slices;
                float v = (float) i / stacks;

                verts[vi++] = x * 0.5f; verts[vi++] = y * 0.5f; verts[vi++] = z * 0.5f; // posicao
                verts[vi++] = x;        verts[vi++] = y;         verts[vi++] = z;         // normal (unit)
                verts[vi++] = u;        verts[vi++] = v;                                   // UV
            }
        }

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int r1 = i       * (slices + 1) + j;
                int r2 = (i + 1) * (slices + 1) + j;
                idx[ii++] = r1;     idx[ii++] = r2;     idx[ii++] = r1 + 1;
                idx[ii++] = r1 + 1; idx[ii++] = r2;     idx[ii++] = r2 + 1;
            }
        }

        return Mesh.create(verts, idx, 3, 3, 2);
    }
}
