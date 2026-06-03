package engine.renderer;

import engine.core.Cleanable;
import engine.utils.ResourceLoader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL33.*;

/**
 * Encapsula compilacao, link e uniformes de um shader program OpenGL.
 * Uso:
 *   ShaderProgram s = new ShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");
 *   s.bind();
 *   s.setUniform("uModel", modelMatrix);
 *   mesh.draw();
 *   s.unbind();
 */
public final class ShaderProgram implements Cleanable {

    private final int id;

    public ShaderProgram(String vertexPath, String fragmentPath) {
        int vs = compile(GL_VERTEX_SHADER,   ResourceLoader.loadString(vertexPath));
        int fs = compile(GL_FRAGMENT_SHADER, ResourceLoader.loadString(fragmentPath));
        id = link(vs, fs);
    }

    public void bind()   { glUseProgram(id); }
    public void unbind() { glUseProgram(0); }

    public void setUniform(String name, int value) {
        glUniform1i(location(name), value);
    }

    public void setUniform(String name, float value) {
        glUniform1f(location(name), value);
    }

    public void setUniform(String name, Vector3f v) {
        glUniform3f(location(name), v.x, v.y, v.z);
    }

    public void setUniform(String name, Matrix4f m) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(location(name), false, m.get(stack.mallocFloat(16)));
        }
    }

    @Override
    public void cleanup() {
        glDeleteProgram(id);
    }

    private int location(String name) {
        return glGetUniformLocation(id, name);
    }

    private static int compile(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new RuntimeException("Erro ao compilar shader:\n" + log);
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
}
