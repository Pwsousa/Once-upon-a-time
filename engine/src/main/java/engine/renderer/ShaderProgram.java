package engine.renderer;

import engine.core.Cleanable;
import engine.utils.ResourceLoader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33.*;

/**
 * Encapsula compilacao, link e uniformes de um shader program OpenGL 3.3.
 *
 * Uniform locations sao cacheadas na primeira consulta — evita chamar
 * glGetUniformLocation toda frame (custo de roundtrip driver/GPU).
 *
 * Uso:
 *   ShaderProgram s = new ShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");
 *   s.bind();
 *   s.setUniform("uModel", modelMatrix);
 *   s.setUniform("uTime",  time);
 *   mesh.draw();
 *   s.unbind();
 */
public final class ShaderProgram implements Cleanable {

    private final int                id;
    private final Map<String, Integer> uniformCache = new HashMap<>();

    public ShaderProgram(String vertexPath, String fragmentPath) {
        int vs = compile(GL_VERTEX_SHADER,   ResourceLoader.loadString(vertexPath),   vertexPath);
        int fs = compile(GL_FRAGMENT_SHADER, ResourceLoader.loadString(fragmentPath), fragmentPath);
        id = link(vs, fs);
    }

    // -------------------------------------------------------------------------
    // Bind / Unbind
    // -------------------------------------------------------------------------

    public void bind()   { glUseProgram(id); }
    public void unbind() { glUseProgram(0);  }

    // -------------------------------------------------------------------------
    // Setters de uniform — todos usam cache de location
    // -------------------------------------------------------------------------

    public void setUniform(String name, boolean value) {
        glUniform1i(location(name), value ? 1 : 0);
    }

    public void setUniform(String name, int value) {
        glUniform1i(location(name), value);
    }

    public void setUniform(String name, float value) {
        glUniform1f(location(name), value);
    }

    public void setUniform(String name, Vector2f v) {
        glUniform2f(location(name), v.x, v.y);
    }

    public void setUniform(String name, Vector3f v) {
        glUniform3f(location(name), v.x, v.y, v.z);
    }

    public void setUniform(String name, Vector4f v) {
        glUniform4f(location(name), v.x, v.y, v.z, v.w);
    }

    public void setUniform(String name, Matrix4f m) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(location(name), false, m.get(stack.mallocFloat(16)));
        }
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    @Override
    public void cleanup() {
        unbind();
        uniformCache.clear();
        glDeleteProgram(id);
    }

    // -------------------------------------------------------------------------
    // Internos
    // -------------------------------------------------------------------------

    /** Retorna location cacheada — consulta o driver apenas na primeira chamada. */
    private int location(String name) {
        return uniformCache.computeIfAbsent(name, n -> {
            int loc = glGetUniformLocation(id, n);
            if (loc == -1) {
                System.err.println("[ShaderProgram] uniform nao encontrado (ou otimizado pelo driver): '" + n + "'");
            }
            return loc;
        });
    }

    private static int compile(int type, String source, String path) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new RuntimeException("Erro ao compilar [" + path + "]:\n" + annotateErrors(source, log));
        }
        return shader;
    }

    private static int link(int vs, int fs) {
        int program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        glDeleteShader(vs);
        glDeleteShader(fs);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            glDeleteProgram(program);
            throw new RuntimeException("Erro ao linkar shader program:\n" + log);
        }
        return program;
    }

    /**
     * Anota o log de erros do GLSL com as linhas do codigo fonte.
     * Ex: "ERROR: 0:5: ..." mostra a linha 5 do shader para facilitar debug.
     */
    private static String annotateErrors(String source, String log) {
        String[] lines = source.split("\n");
        StringBuilder sb = new StringBuilder(log).append("\n--- fonte ---\n");
        for (int i = 0; i < lines.length; i++) {
            sb.append(String.format("%3d | %s%n", i + 1, lines[i]));
        }
        return sb.toString();
    }
}
