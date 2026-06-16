package engine;

import engine.core.WindowConfig;
import engine.renderer.Mesh;
import engine.renderer.ShaderProgram;
import engine.renderer.Texture;
import engine.renderer.UniformBuffer;
import org.joml.Matrix4f;

/**
 * Demo: quad texturizado usando UBOs (Uniform Buffer Objects).
 *
 * Dois UBO blocks substituem os setUniform() de matrices por frame:
 *
 *   PerFrame  (binding 0) — dados compartilhados por toda a cena:
 *     mat4  uView        offset   0  (64 bytes)
 *     mat4  uProjection  offset  64  (64 bytes)
 *     float uTime        offset 128  ( 4 bytes)
 *     [pad]              offset 132  (12 bytes) → total 144 bytes
 *
 *   PerObject (binding 1) — dados por objeto individual:
 *     mat4  uModel       offset   0  (64 bytes)
 *
 * uTexture permanece como uniform classico — samplers nao cabem em UBOs.
 *
 * Vantagem: com N objetos e M shaders, PerFrame e enviado ao GPU apenas
 * uma vez por frame; cada shader le do mesmo buffer sem upload extra.
 */
public final class Main extends Engine {

    private Mesh          quad;
    private ShaderProgram shader;
    private Texture       texture;

    private UniformBuffer perFrameUBO;   // PerFrame  — binding point 0
    private UniformBuffer perObjectUBO;  // PerObject — binding point 1

    // std140 layout: mat4=64 bytes | float=4 bytes | padding ao multiplo de 16
    private static final int PER_FRAME_SIZE  = 144;  // 64 + 64 + 4 + 12 pad
    private static final int PER_OBJECT_SIZE = 64;   // 64

    private final Matrix4f model = new Matrix4f();
    private final Matrix4f view  = new Matrix4f();
    private final Matrix4f proj  = new Matrix4f();

    private float time = 0f;

    //          posicao (x, y, z)       cor (r, g, b)          UV (u, v)
    private static final float[] VERTICES = {
        -0.5f,  0.5f, 0.0f,    1.0f, 0.0f, 0.0f,    0.0f, 1.0f,  // 0 — superior esquerdo  (vermelho)
         0.5f,  0.5f, 0.0f,    0.0f, 1.0f, 0.0f,    1.0f, 1.0f,  // 1 — superior direito   (verde)
         0.5f, -0.5f, 0.0f,    0.0f, 0.0f, 1.0f,    1.0f, 0.0f,  // 2 — inferior direito   (azul)
        -0.5f, -0.5f, 0.0f,    1.0f, 1.0f, 0.0f,    0.0f, 0.0f,  // 3 — inferior esquerdo  (amarelo)
    };

    private static final int[] INDICES = {
        0, 1, 2,
        0, 2, 3,
    };

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    protected WindowConfig createWindowConfig() {
        return new WindowConfig.Builder("3D Engine — UBO")
                .width(1280)
                .height(720)
                .vsync(true)
                .build();
    }

    @Override
    protected void onInit() {
        shader  = new ShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");
        texture = new Texture("textures/test.png");
        quad    = Mesh.create(VERTICES, INDICES, 3, 3, 2);

        // vincula os blocos do shader aos binding points dos UBOs
        // (necessario em GLSL 330 — em GLSL 420 bastaria layout(binding=N) no shader)
        shader.bind();
        shader.bindUniformBlock("PerFrame",  0);
        shader.bindUniformBlock("PerObject", 1);
        shader.unbind();

        perFrameUBO  = new UniformBuffer(0, PER_FRAME_SIZE);
        perObjectUBO = new UniformBuffer(1, PER_OBJECT_SIZE);

        view.identity();
        proj.identity();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        time += deltaTime;
        model.identity().rotateZ(time);
    }

    @Override
    protected void onRender() {
        // upload PerFrame: view (offset 0) | projection (offset 64) | time (offset 128)
        perFrameUBO.upload(buf -> {
            view.get(0, buf);
            proj.get(64, buf);
            buf.putFloat(128, time);
        });

        // upload PerObject: model matrix (offset 0)
        perObjectUBO.upload(buf -> model.get(0, buf));

        texture.bind(0);
        // uTexture e o unico uniform classico restante — samplers nao entram em UBO
        renderer.render(shader, s -> s.setUniform("uTexture", 0), quad);
    }

    @Override
    protected void onCleanup() {
        quad.cleanup();
        shader.cleanup();
        texture.cleanup();
        perFrameUBO.cleanup();
        perObjectUBO.cleanup();
    }
}
