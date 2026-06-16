package engine;

import engine.core.WindowConfig;
import engine.renderer.Mesh;
import engine.renderer.ShaderProgram;
import engine.renderer.Texture;
import engine.renderer.UniformBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Demo: camera first-person + 5 quads em posicoes 3D distintas.
 *
 * Padrao UBO por frame:
 *
 *   PerFrame  (binding 0) — upload UMA vez por frame, independente do numero de objetos
 *     mat4  uView        offset   0  (64 bytes)  ← camera.getViewMatrix()
 *     mat4  uProjection  offset  64  (64 bytes)  ← projecao perspectiva
 *     float uTime        offset 128  ( 4 bytes)  ← tempo acumulado
 *     [pad]              offset 132  (12 bytes)
 *
 *   PerObject (binding 1) — upload POR OBJETO antes de cada draw call
 *     mat4  uModel       offset   0  (64 bytes)  ← translate + rotate por objeto
 *
 * Este e o padrao de engines reais:
 *   N objetos × M shaders → PerFrame enviado M vezes, PerObject N×M vezes.
 *   Motores maiores combinam isso com instanced rendering e indirect draw.
 *
 * Controles:
 *   WASD           — mover camera
 *   SPACE / LSHIFT — subir / descer
 *   Setas          — olhar (yaw / pitch)
 *   ESC            — fechar
 */
public final class Main extends Engine {

    private Mesh          quad;
    private ShaderProgram shader;
    private Texture       texture;
    private Camera        camera;

    private UniformBuffer perFrameUBO;
    private UniformBuffer perObjectUBO;

    private static final int PER_FRAME_SIZE  = 144; // view(64) + proj(64) + time(4) + pad(12)
    private static final int PER_OBJECT_SIZE = 64;  // model(64)

    private final Matrix4f model = new Matrix4f();  // reutilizado por objeto
    private final Matrix4f proj  = new Matrix4f();

    private float time = 0f;

    // 5 quads posicionados em diferentes pontos do espaco 3D
    private static final Vector3f[] POSITIONS = {
        new Vector3f( 0.0f,  0.0f,  0.0f),
        new Vector3f( 2.5f,  1.5f, -3.0f),
        new Vector3f(-2.0f,  0.5f, -2.5f),
        new Vector3f( 1.0f, -1.5f, -4.0f),
        new Vector3f(-0.5f,  2.0f, -1.5f),
    };

    //          posicao (x, y, z)       cor (r, g, b)          UV (u, v)
    private static final float[] VERTICES = {
        -0.5f,  0.5f, 0.0f,    1.0f, 0.0f, 0.0f,    0.0f, 1.0f,  // superior esquerdo  (vermelho)
         0.5f,  0.5f, 0.0f,    0.0f, 1.0f, 0.0f,    1.0f, 1.0f,  // superior direito   (verde)
         0.5f, -0.5f, 0.0f,    0.0f, 0.0f, 1.0f,    1.0f, 0.0f,  // inferior direito   (azul)
        -0.5f, -0.5f, 0.0f,    1.0f, 1.0f, 0.0f,    0.0f, 0.0f,  // inferior esquerdo  (amarelo)
    };

    private static final int[] INDICES = { 0, 1, 2, 0, 2, 3 };

    public static void main(String[] args) { new Main().start(); }

    @Override
    protected WindowConfig createWindowConfig() {
        return new WindowConfig.Builder("3D Engine — Camera + Multi-Object")
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

        shader.bind();
        shader.bindUniformBlock("PerFrame",  0);
        shader.bindUniformBlock("PerObject", 1);
        shader.unbind();

        perFrameUBO  = new UniformBuffer(0, PER_FRAME_SIZE);
        perObjectUBO = new UniformBuffer(1, PER_OBJECT_SIZE);

        camera = new Camera();

        // projecao perspectiva: FOV 45 graus, aspect ratio atual, near=0.1, far=100
        proj.perspective((float) Math.toRadians(45f), getAspectRatio(), 0.1f, 100f);
    }

    @Override
    protected void onResize(int w, int h) {
        // recalcula projecao para manter proporcoes corretas apos resize
        proj.perspective((float) Math.toRadians(45f), (float) w / h, 0.1f, 100f);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        time += deltaTime;
        camera.update(deltaTime, input);
    }

    @Override
    protected void onRender() {
        // --- PerFrame: UMA vez por frame ---
        perFrameUBO.upload(buf -> {
            camera.getViewMatrix().get(0, buf);  // view       offset   0
            proj.get(64, buf);                   // projection offset  64
            buf.putFloat(128, time);             // time       offset 128
        });

        texture.bind(0);

        // --- PerObject: POR OBJETO — PerFrame ja esta no GPU, nao re-envia ---
        for (int i = 0; i < POSITIONS.length; i++) {
            float angle = time * (0.5f + i * 0.2f);
            model.identity()
                 .translate(POSITIONS[i])
                 .rotateY(angle)
                 .rotateX(angle * 0.3f);

            perObjectUBO.upload(buf -> model.get(0, buf));

            // unico setUniform restante: slot do sampler (nao cabe em UBO)
            renderer.render(shader, s -> s.setUniform("uTexture", 0), quad);
        }
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
