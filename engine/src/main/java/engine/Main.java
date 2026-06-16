package engine;

import engine.core.WindowConfig;
import engine.renderer.Model;
import engine.renderer.Shapes;
import engine.renderer.ShaderProgram;
import engine.renderer.Texture;
import engine.renderer.UniformBuffer;
import engine.scene.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Main extends Engine {

    private ShaderProgram shader;
    private Model         cubeModel;
    private Entity        cube;
    private UniformBuffer perFrameUBO;
    private UniformBuffer perObjectUBO;

    private static final int PER_FRAME_SIZE  = 144;
    private static final int PER_OBJECT_SIZE = 64;

    private final Matrix4f view = new Matrix4f();
    private final Matrix4f proj = new Matrix4f();
    private float time = 0f;

    public static void main(String[] args) { new Main().start(); }

    @Override
    protected WindowConfig createWindowConfig() {
        return new WindowConfig.Builder("3D Engine — Cubo")
                .width(1280)
                .height(720)
                .vsync(true)
                .build();
    }

    @Override
    protected void onInit() {
        shader    = new ShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");
        cubeModel = new Model(Shapes.cube(), new Texture("textures/test.png"));

        cube = new Entity(cubeModel);
        cube.transform.position.set(0f, 0f, 0f);

        shader.bind();
        shader.bindUniformBlock("PerFrame",  0);
        shader.bindUniformBlock("PerObject", 1);
        shader.unbind();

        perFrameUBO  = new UniformBuffer(0, PER_FRAME_SIZE);
        perObjectUBO = new UniformBuffer(1, PER_OBJECT_SIZE);

        // camera fixa: olhando para a origem a partir de (0, 0, 3)
        view.lookAt(
            new Vector3f(0f, 0f, 3f),   // posicao da camera
            new Vector3f(0f, 0f, 0f),   // ponto alvo (cubo)
            new Vector3f(0f, 1f, 0f)    // up
        );

        proj.perspective((float) Math.toRadians(45f), getAspectRatio(), 0.1f, 100f);
    }

    @Override
    protected void onResize(int w, int h) {
        proj.perspective((float) Math.toRadians(45f), (float) w / h, 0.1f, 100f);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        time += deltaTime;
        cube.transform.rotation.y = time * 45f;  // 45 graus/segundo em Y
        cube.transform.rotation.x = time * 30f;  // 30 graus/segundo em X
    }

    @Override
    protected void onRender() {
        perFrameUBO.upload(buf -> {
            view.get(0, buf);
            proj.get(64, buf);
            buf.putFloat(128, time);
        });

        cube.model.texture.bind(0);
        perObjectUBO.upload(buf -> cube.transform.getMatrix().get(0, buf));
        renderer.render(shader, s -> s.setUniform("uTexture", 0), cube.model.mesh);
    }

    @Override
    protected void onCleanup() {
        shader.cleanup();
        cubeModel.cleanup();
        perFrameUBO.cleanup();
        perObjectUBO.cleanup();
    }
}
