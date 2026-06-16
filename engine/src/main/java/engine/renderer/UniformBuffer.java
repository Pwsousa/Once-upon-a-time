package engine.renderer;

import engine.core.Cleanable;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL33.*;

/**
 * UBO (Uniform Buffer Object) — buffer GPU compartilhado entre shaders.
 *
 * Vantagem sobre setUniform(): um upload por frame independente de quantos
 * shaders ou draw calls consomem o mesmo bloco (ex: PerFrame compartilha
 * uView/uProjection/uTime com todos os shaders da cena).
 *
 * Layout std140 — regras de alinhamento que o GLSL e o Java precisam concordar:
 *   float  → 4 bytes, alinhado em 4
 *   vec2   → 8 bytes, alinhado em 8
 *   vec3   → 12 bytes, alinhado em 16
 *   vec4   → 16 bytes, alinhado em 16
 *   mat4   → 4 colunas de vec4 = 64 bytes, alinhado em 16
 *
 * Uso:
 *   UniformBuffer ubo = new UniformBuffer(0, 144);   // binding point 0, 144 bytes
 *   shader.bindUniformBlock("PerFrame", 0);
 *
 *   // por frame:
 *   ubo.upload(buf -> {
 *       view.get(0,   buf);        // mat4 offset 0   (64 bytes)
 *       proj.get(64,  buf);        // mat4 offset 64  (64 bytes)
 *       buf.putFloat(128, time);   // float offset 128 (4 bytes)
 *   });
 */
public final class UniformBuffer implements Cleanable {

    private final int        id;
    private final int        bindingPoint;
    private final ByteBuffer staging;

    /**
     * @param bindingPoint ponto de binding (layout binding=N no shader)
     * @param sizeBytes    tamanho total do bloco em bytes (seguir std140)
     */
    public UniformBuffer(int bindingPoint, int sizeBytes) {
        this.bindingPoint = bindingPoint;
        staging = MemoryUtil.memAlloc(sizeBytes);

        id = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, id);
        glBufferData(GL_UNIFORM_BUFFER, sizeBytes, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, bindingPoint, id);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    /**
     * Envia dados ao GPU. O consumer recebe o staging ByteBuffer limpo;
     * use escrita por posicao absoluta para evitar gerenciar position manualmente:
     *   matrix.get(byteOffset, buf)
     *   buf.putFloat(byteOffset, value)
     */
    public void upload(Consumer<ByteBuffer> writer) {
        staging.clear();
        writer.accept(staging);
        glBindBuffer(GL_UNIFORM_BUFFER, id);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, staging);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
    }

    public int getBindingPoint() { return bindingPoint; }

    @Override
    public void cleanup() {
        glDeleteBuffers(id);
        MemoryUtil.memFree(staging);
    }
}
