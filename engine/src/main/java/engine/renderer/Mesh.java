package engine.renderer;

import engine.core.Cleanable;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.*;

/**
 * Encapsula VAO + VBO para geometria estatica.
 *
 * Use o factory method estatico:
 *   // vertices interleaved: posicao (3) + cor (3) = attributeSizes {3, 3}
 *   Mesh mesh = Mesh.create(vertices, 3, 3);
 *
 * attributeSizes descreve quantos floats cada atributo ocupa, na ordem
 * dos layout location = 0, 1, 2...
 */
public final class Mesh implements Cleanable {

    private final int vao;
    private final int vbo;
    private final int vertexCount;

    private Mesh(int vao, int vbo, int vertexCount) {
        this.vao = vao;
        this.vbo = vbo;
        this.vertexCount = vertexCount;
    }

    /**
     * Cria um Mesh a partir de vertices interleaved e tamanhos de atributos.
     *
     * @param vertices      array de floats interleaved
     * @param attributeSizes tamanhos de cada atributo em numero de floats
     */
    public static Mesh create(float[] vertices, int... attributeSizes) {
        if (attributeSizes.length == 0) throw new IllegalArgumentException("attributeSizes nao pode ser vazio");

        int totalComponents = 0;
        for (int size : attributeSizes) totalComponents += size;

        int stride = totalComponents * Float.BYTES;
        int vertexCount = vertices.length / totalComponents;

        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        FloatBuffer buf = MemoryUtil.memAllocFloat(vertices.length);
        try {
            buf.put(vertices).flip();
            glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        } finally {
            MemoryUtil.memFree(buf);
        }

        int offset = 0;
        for (int i = 0; i < attributeSizes.length; i++) {
            glVertexAttribPointer(i, attributeSizes[i], GL_FLOAT, false, stride, (long) offset * Float.BYTES);
            glEnableVertexAttribArray(i);
            offset += attributeSizes[i];
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return new Mesh(vao, vbo, vertexCount);
    }

    public void draw() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    @Override
    public void cleanup() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
    }
}
