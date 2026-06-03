package engine.core;

/**
 * Recursos GPU (VAO, VBO, shader, textura) devem implementar esta interface
 * para garantir liberacao deterministica de memoria na GPU.
 */
public interface Cleanable {
    void cleanup();
}
