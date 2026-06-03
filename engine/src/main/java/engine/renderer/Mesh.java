package engine.renderer;

import engine.core.Cleanable;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

/**
 * Encapsula VAO + VBO (+ EBO opcional) para geometria estatica.
 *
 * Sem indices — usa glDrawArrays:
 *   Mesh mesh = Mesh.create(vertices, 3, 3);
 *
 * Com indices — usa glDrawElements (recomendado para quads e meshes complexas):
 *   Mesh mesh = Mesh.create(vertices, indices, 3, 3);
 *
 * attributeSizes descreve quantos floats cada atributo ocupa, na ordem
 * de layout location = 0, 1, 2...
 *
 * Estrutura de buffers OpenGL:
 *
 *   VAO ──┬── VBO  (dados dos vertices: posicao, cor, uv, normal...)
 *         └── EBO  (indices de vertices para reusar vertices compartilhados)
 *
 * Exemplo de quad (4 vertices, 6 indices = 2 triangulos):
 *
 *   0 ──── 1        indices: [0,1,2]  triangulo superior
 *   │  ╲   │                 [0,2,3]  triangulo inferior
 *   │   ╲  │
 *   3 ──── 2
 */
public final class Mesh implements Cleanable {

    private final int vao;
    private final int vbo;
    private final int ebo;          // 0 = sem indices
    private final int drawCount;    // vertices (sem EBO) ou indices (com EBO)

    private Mesh(int vao, int vbo, int ebo, int drawCount) {
        this.vao       = vao;
        this.vbo       = vbo;
        this.ebo       = ebo;
        this.drawCount = drawCount;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /**
     * Cria Mesh sem indices. Usa glDrawArrays.
     *
     * @param vertices       floats interleaved (posicao, cor, uv...)
     * @param attributeSizes numero de floats por atributo (ex: 3, 3 = vec3 pos + vec3 cor)
     */
    public static Mesh create(float[] vertices, int... attributeSizes) {
        validate(attributeSizes);

        int stride      = stride(attributeSizes);
        int vertexCount = vertices.length / (stride / Float.BYTES);

        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);
        uploadVertices(vbo, vertices);
        configureAttributes(stride, attributeSizes);
        glBindVertexArray(0);

        return new Mesh(vao, vbo, 0, vertexCount);
    }

    /**
     * Cria Mesh com EBO (indexed rendering). Usa glDrawElements.
     * Permite reusar vertices — essencial para quads, cubos e meshes complexas.
     *
     * @param vertices       floats interleaved
     * @param indices        indices que referenciam vertices no VBO
     * @param attributeSizes numero de floats por atributo
     */
    public static Mesh create(float[] vertices, int[] indices, int... attributeSizes) {
        validate(attributeSizes);

        int stride = stride(attributeSizes);

        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();
        int ebo = glGenBuffers();

        glBindVertexArray(vao);
        uploadVertices(vbo, vertices);
        uploadIndices(ebo, indices);          // EBO fica vinculado ao VAO
        configureAttributes(stride, attributeSizes);
        glBindVertexArray(0);

        return new Mesh(vao, vbo, ebo, indices.length);
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------

    public void draw() {
        glBindVertexArray(vao);
        if (ebo != 0) {
            glDrawElements(GL_TRIANGLES, drawCount, GL_UNSIGNED_INT, 0L);
        } else {
            glDrawArrays(GL_TRIANGLES, 0, drawCount);
        }
        glBindVertexArray(0);
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    @Override
    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        if (ebo != 0) glDeleteBuffers(ebo);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private static void uploadVertices(int vbo, float[] vertices) {
        FloatBuffer buf = MemoryUtil.memAllocFloat(vertices.length);
        try {
            buf.put(vertices).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        } finally {
            MemoryUtil.memFree(buf);
        }
    }

    private static void uploadIndices(int ebo, int[] indices) {
        IntBuffer buf = MemoryUtil.memAllocInt(indices.length);
        try {
            buf.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        } finally {
            MemoryUtil.memFree(buf);
        }
    }

    private static void configureAttributes(int stride, int[] attributeSizes) {
        int offset = 0;
        for (int i = 0; i < attributeSizes.length; i++) {
            glVertexAttribPointer(i, attributeSizes[i], GL_FLOAT, false, stride, (long) offset * Float.BYTES);
            glEnableVertexAttribArray(i);
            offset += attributeSizes[i];
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private static int stride(int[] attributeSizes) {
        int total = 0;
        for (int s : attributeSizes) total += s;
        return total * Float.BYTES;
    }

    private static void validate(int[] attributeSizes) {
        if (attributeSizes.length == 0) {
            throw new IllegalArgumentException("attributeSizes nao pode ser vazio");
        }
    }
}
