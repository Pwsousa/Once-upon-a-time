package engine.renderer;

import engine.core.Cleanable;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;

/**
 * EBO — Element Buffer Object (tambem chamado IBO — Index Buffer Object).
 * Armazena indices que referenciam vertices no VBO, permitindo reusar
 * vertices compartilhados entre triangulos.
 *
 * Sem IBO: quad precisa de 6 vertices (2 repetidos).
 * Com IBO: quad usa 4 vertices + 6 indices.
 *
 * Para meshes complexas a economia e significativa:
 *   - Cubo:   8 vertices + 36 indices  (vs 36 vertices sem IBO)
 *   - Esfera: ~500 vertices + ~3000 indices
 *
 * IMPORTANTE: o EBO deve ser criado com o VAO vinculado — o VAO armazena
 * a referencia ao EBO automaticamente.
 *
 * Layout de indices para um quad:
 *
 *   0 ──── 1
 *   │  ╲   │    indices: [0,1,2, 0,2,3]
 *   │   ╲  │
 *   3 ──── 2
 */
public final class IndexBuffer implements Cleanable {

    private final int id;
    private final int count;

    /**
     * @param indices array de indices GL_UNSIGNED_INT referenciando vertices do VBO.
     *                Deve ser criado com o VAO alvo ja vinculado (glBindVertexArray).
     */
    public IndexBuffer(int[] indices) {
        this.count = indices.length;
        id = glGenBuffers();

        IntBuffer buf = MemoryUtil.memAllocInt(indices.length);
        try {
            buf.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        } finally {
            MemoryUtil.memFree(buf);
        }
    }

    /** Numero de indices — usado em glDrawElements. */
    public int getCount() { return count; }

    /** Normalmente nao e necessario — o VAO gerencia o bind do EBO. */
    public void bind()   { glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id); }
    public void unbind() { glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0); }

    @Override
    public void cleanup() {
        glDeleteBuffers(id);
    }
}
