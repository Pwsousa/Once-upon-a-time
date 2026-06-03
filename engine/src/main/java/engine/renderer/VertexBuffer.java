package engine.renderer;

import engine.core.Cleanable;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.*;

/**
 * VBO — Vertex Buffer Object.
 * Armazena dados dos vertices (posicao, cor, uv, normal...) na memoria da GPU.
 *
 * Uso direto (geralmente via Mesh):
 *   VertexBuffer vbo = new VertexBuffer(vertices, GL_STATIC_DRAW);
 *   vbo.bind();
 *   // configura atributos...
 *   vbo.unbind();
 */
public final class VertexBuffer implements Cleanable {

    private final int id;

    public VertexBuffer(float[] data, int usage) {
        id = glGenBuffers();

        FloatBuffer buf = MemoryUtil.memAllocFloat(data.length);
        try {
            buf.put(data).flip();
            glBindBuffer(GL_ARRAY_BUFFER, id);
            glBufferData(GL_ARRAY_BUFFER, buf, usage);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        } finally {
            MemoryUtil.memFree(buf);
        }
    }

    /** Cria VBO com uso estatico (geometria que nao muda por frame). */
    public static VertexBuffer staticDraw(float[] data) {
        return new VertexBuffer(data, GL_STATIC_DRAW);
    }

    /** Cria VBO com uso dinamico (geometria atualizada frequentemente). */
    public static VertexBuffer dynamicDraw(float[] data) {
        return new VertexBuffer(data, GL_DYNAMIC_DRAW);
    }

    public void bind()   { glBindBuffer(GL_ARRAY_BUFFER, id); }
    public void unbind() { glBindBuffer(GL_ARRAY_BUFFER, 0); }

    /** Atualiza parte dos dados sem realocar o buffer (eficiente para geometria dinamica). */
    public void update(float[] data, int offsetFloats) {
        FloatBuffer buf = MemoryUtil.memAllocFloat(data.length);
        try {
            buf.put(data).flip();
            glBindBuffer(GL_ARRAY_BUFFER, id);
            glBufferSubData(GL_ARRAY_BUFFER, (long) offsetFloats * Float.BYTES, buf);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        } finally {
            MemoryUtil.memFree(buf);
        }
    }

    @Override
    public void cleanup() {
        glDeleteBuffers(id);
    }
}
