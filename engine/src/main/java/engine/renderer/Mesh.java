package engine.renderer;

import engine.core.Cleanable;

import static org.lwjgl.opengl.GL33.*;

/**
 * VAO — Vertex Array Object.
 * Agrupa VBO + atributos de vertices + EBO (opcional) em um unico objeto.
 *
 * O VAO memoriza:
 *   - qual VBO esta vinculado
 *   - como os atributos estao dispostos (glVertexAttribPointer)
 *   - qual EBO (IndexBuffer) esta vinculado
 *
 * Apos configurar, basta fazer glBindVertexArray(vao) para restaurar
 * todo o estado — sem reconfigurar atributos a cada frame.
 *
 * Factory methods:
 *
 *   // sem indices — glDrawArrays
 *   Mesh mesh = Mesh.create(vertices, 3, 3);
 *
 *   // com indices — glDrawElements (recomendado para quads e meshes)
 *   Mesh mesh = Mesh.create(vertices, indices, 3, 3);
 *
 * attributeSizes: numero de floats por atributo na ordem dos location:
 *   {3, 3} = location 0 tem vec3, location 1 tem vec3
 *   {3, 2} = location 0 tem vec3 posicao, location 1 tem vec2 uv
 */
public final class Mesh implements Cleanable {

    private final int            vao;
    private final VertexBuffer   vbo;
    private final IndexBuffer    ebo;    // null = sem indices
    private final int            vertexCount;

    private Mesh(int vao, VertexBuffer vbo, IndexBuffer ebo, int vertexCount) {
        this.vao         = vao;
        this.vbo         = vbo;
        this.ebo         = ebo;
        this.vertexCount = vertexCount;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /**
     * Cria Mesh sem Index Buffer. Usa glDrawArrays.
     */
    public static Mesh create(float[] vertices, int... attributeSizes) {
        validate(attributeSizes);

        int stride      = computeStride(attributeSizes);
        int vertexCount = vertices.length / (stride / Float.BYTES);

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        VertexBuffer vbo = VertexBuffer.staticDraw(vertices);
        vbo.bind();
        configureAttributes(stride, attributeSizes);
        vbo.unbind();

        glBindVertexArray(0);
        return new Mesh(vao, vbo, null, vertexCount);
    }

    /**
     * Cria Mesh com Index Buffer (EBO). Usa glDrawElements.
     * Vertices do VBO sao reutilizados pelos indices — sem duplicatas na GPU.
     *
     * @param vertices       floats interleaved dos vertices
     * @param indices        indices referenciando posicoes no array de vertices
     * @param attributeSizes numero de floats por atributo
     */
    public static Mesh create(float[] vertices, int[] indices, int... attributeSizes) {
        validate(attributeSizes);

        int stride = computeStride(attributeSizes);

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        VertexBuffer vbo = VertexBuffer.staticDraw(vertices);
        vbo.bind();
        configureAttributes(stride, attributeSizes);
        vbo.unbind();

        // EBO criado com VAO vinculado — VAO armazena a referencia automaticamente
        IndexBuffer ebo = new IndexBuffer(indices);

        glBindVertexArray(0);
        return new Mesh(vao, vbo, ebo, indices.length);
    }

    // -------------------------------------------------------------------------
    // Render
    // -------------------------------------------------------------------------

    public void draw() {
        glBindVertexArray(vao);
        if (ebo != null) {
            // glDrawElements usa os indices do EBO para buscar vertices no VBO
            glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0L);
        } else {
            glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        }
        glBindVertexArray(0);
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    @Override
    public void cleanup() {
        glDeleteVertexArrays(vao);
        vbo.cleanup();
        if (ebo != null) ebo.cleanup();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void configureAttributes(int stride, int[] attributeSizes) {
        int offset = 0;
        for (int i = 0; i < attributeSizes.length; i++) {
            glVertexAttribPointer(i, attributeSizes[i], GL_FLOAT, false, stride, (long) offset * Float.BYTES);
            glEnableVertexAttribArray(i);
            offset += attributeSizes[i];
        }
    }

    private static int computeStride(int[] sizes) {
        int total = 0;
        for (int s : sizes) total += s;
        return total * Float.BYTES;
    }

    private static void validate(int[] sizes) {
        if (sizes.length == 0) throw new IllegalArgumentException("attributeSizes nao pode ser vazio");
    }
}
