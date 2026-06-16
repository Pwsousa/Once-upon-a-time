package engine.renderer;

import engine.core.Cleanable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.*;

/**
 * Textura 2D OpenGL carregada via STB Image.
 *
 * Convencoes:
 *   - Imagem flipada verticalmente no load (origem OpenGL = canto inferior-esquerdo)
 *   - Formato interno: GL_RGBA8
 *   - Mipmaps gerados automaticamente
 *   - Wrap: GL_REPEAT, Filter: GL_LINEAR / GL_LINEAR_MIPMAP_LINEAR
 *
 * Uso:
 *   Texture tex = new Texture("textures/wall.png");
 *
 *   // antes de desenhar, ativa a unidade e passa o slot pro shader
 *   tex.bind(0);
 *   shader.setUniform("uTexture", 0);
 *   mesh.draw();
 *
 *   tex.cleanup(); // ao finalizar
 *
 * Slots de textura (unidades):
 *   Cada chamada bind(slot) ativa GL_TEXTURE0 + slot.
 *   O shader usa sampler2D com valor igual ao slot.
 *   Multiplas texturas: tex0.bind(0), tex1.bind(1), shader.setUniform("uNormal", 1).
 */
public final class Texture implements Cleanable {

    private final int id;
    private final int width;
    private final int height;

    /**
     * Carrega textura de arquivo no classpath.
     *
     * @param classpathPath caminho relativo ao classpath, ex: "textures/wall.png"
     */
    public Texture(String classpathPath) {
        ByteBuffer raw = readFromClasspath(classpathPath);

        int texId = 0, imgW = 0, imgH = 0;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pw = stack.mallocInt(1);
            IntBuffer ph = stack.mallocInt(1);
            IntBuffer pc = stack.mallocInt(1);

            // OpenGL espera UV com origem no canto inferior-esquerdo;
            // a maioria dos formatos de imagem tem origem no topo — inverte aqui.
            stbi_set_flip_vertically_on_load(true);

            ByteBuffer pixels = stbi_load_from_memory(raw, pw, ph, pc, STBI_rgb_alpha);
            if (pixels == null) {
                throw new RuntimeException(
                    "Falha ao decodificar textura [" + classpathPath + "]: " + stbi_failure_reason());
            }

            imgW  = pw.get(0);
            imgH  = ph.get(0);
            texId = glGenTextures();

            glBindTexture(GL_TEXTURE_2D, texId);

            // como a textura e repetida quando UV ultrapassa [0,1]
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            // filtragem ao diminuir (usa mipmap) e ao ampliar
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            // envia pixels para a GPU e gera cadeia de mipmaps
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, imgW, imgH,
                         0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            glGenerateMipmap(GL_TEXTURE_2D);

            glBindTexture(GL_TEXTURE_2D, 0);
            stbi_image_free(pixels);

        } finally {
            MemoryUtil.memFree(raw);
        }

        this.id     = texId;
        this.width  = imgW;
        this.height = imgH;
    }

    // -------------------------------------------------------------------------
    // Bind / Unbind
    // -------------------------------------------------------------------------

    /**
     * Ativa a unidade de textura {@code slot} e vincula esta textura.
     * Passar slot = 0 para a primeira textura, slot = 1 para a segunda, etc.
     */
    public void bind(int slot) {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int getWidth()  { return width;  }
    public int getHeight() { return height; }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    @Override
    public void cleanup() {
        glDeleteTextures(id);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Le bytes brutos do classpath para um ByteBuffer nativo (MemoryUtil).
     * O caller e responsavel por chamar MemoryUtil.memFree() no buffer retornado.
     */
    private static ByteBuffer readFromClasspath(String path) {
        try (InputStream is = Texture.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Textura nao encontrada no classpath: " + path);
            }
            byte[] bytes = is.readAllBytes();
            ByteBuffer buf = MemoryUtil.memAlloc(bytes.length);
            buf.put(bytes).flip();
            return buf;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler textura: " + path, e);
        }
    }
}
