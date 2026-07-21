package engine;

import engine.core.WindowConfig;
import engine.lighting.AmbientLight;
import engine.lighting.Light;
import engine.renderer.Mesh;
import engine.renderer.Model;
import engine.renderer.ObjLoader;
import engine.renderer.ShaderProgram;
import engine.renderer.Texture;
import engine.renderer.UniformBuffer;
import engine.scene.Entity;
import engine.scene.Scene;
import engine.terrain.Terrain;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Carrega "models/cube.obj" e renderiza o cubo rotacionando.
 *
 * Para trocar o modelo: substitua "models/cube.obj" pelo caminho do seu arquivo.
 * O arquivo deve estar em src/main/resources/ e exportado do Blender/Maya como OBJ.
 *
 * Exportacao recomendada no Blender:
 *   File → Export → Wavefront (.obj)
 *   [x] Include Normals
 *   [x] Include UVs
 *   [x] Triangulate Faces
 *   Forward Axis: -Z,  Up Axis: Y
 */
public final class Main extends Engine {

    private ShaderProgram shader;
    private Model         cubeModel;
    private Model         stallModel;
    private Model         terrainModel;
    private Entity        cube;
    private Entity        stall;
    private Entity        terrain;
    private UniformBuffer perFrameUBO;
    private UniformBuffer perObjectUBO;
    private Light         sun;
    private AmbientLight  ambient;
    private Scene         scene;

    private static final int PER_FRAME_SIZE  = 192;
    private static final int PER_OBJECT_SIZE = 64;

    private final Matrix4f view      = new Matrix4f();
    private final Matrix4f proj      = new Matrix4f();
    private final Vector3f cameraPos = new Vector3f(0f, 4f, 14f);
    private float time = 0f;

    public static void main(String[] args) { new Main().start(); }

    @Override
    protected WindowConfig createWindowConfig() {
        return new WindowConfig.Builder("3D Engine — OBJ Loader")
                .width(1280)
                .height(720)
                .vsync(true)
                .build();
    }

    @Override
    protected void onInit() {
        shader = new ShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");

        // carrega OBJ do classpath (src/main/resources/models/cube.obj)
        ObjLoader.MeshData cubeData = ObjLoader.load("models/cube.obj");
        Mesh cubeMesh = Mesh.create(cubeData.vertices, cubeData.indices, 3, 3, 2);
        cubeModel = new Model(cubeMesh, new Texture("textures/test.png"));

        cube = new Entity(cubeModel);
        cube.transform.position.set(-5f, 0f, 0f);

        // segundo modelo, lado a lado com o cubo
        ObjLoader.MeshData stallData = ObjLoader.load("models/stall.obj");
        Mesh stallMesh = Mesh.create(stallData.vertices, stallData.indices, 3, 3, 2);
        // madeira fosca: highlight fraco e espalhado (shininess baixo)
        stallModel = new Model(stallMesh, new Texture("textures/stallTexture.png"), 8f, 0.15f);

        stall = new Entity(stallModel);
        stall.transform.position.set(5f, 0f, 0f);

        // terreno: mesh gerada (grid 20x20, 20x20 unidades), tratado como
        // Model comum — mesmo pipeline de Entity/Scene do resto da cena
        Mesh terrainMesh = Terrain.flat(20f, 20, 8f);
        terrainModel = new Model(terrainMesh, new Texture("textures/test.png"), 2f, 0.05f);

        terrain = new Entity(terrainModel);
        terrain.transform.position.set(0f, -1f, 0f);

        // agrupa entidades por Model — bind de textura/uniforms de material
        // acontece 1x por grupo, nao por entidade (ver engine.scene.Scene)
        scene = new Scene();
        scene.add(terrain);
        scene.add(cube);
        scene.add(stall);

        shader.bind();
        shader.bindUniformBlock("PerFrame",  0);
        shader.bindUniformBlock("PerObject", 1);
        shader.unbind();

        perFrameUBO  = new UniformBuffer(0, PER_FRAME_SIZE);
        perObjectUBO = new UniformBuffer(1, PER_OBJECT_SIZE);

        // camera afastada para enquadrar os dois modelos lado a lado
        view.lookAt(
            cameraPos,                  // posicao camera
            new Vector3f(0f, 0f,  0f),  // alvo (entre os dois modelos)
            new Vector3f(0f, 1f,  0f)   // up
        );

        proj.perspective((float) Math.toRadians(45f), getAspectRatio(), 0.1f, 100f);

        // luz direcional (tipo sol), branca
        sun = new Light(new Vector3f(1f, 2f, 1.5f), new Vector3f(1f, 1f, 1f));

        // luz ambiente (tipo ceu), azulada, 20% de intensidade
        ambient = new AmbientLight(new Vector3f(0.5f, 0.6f, 0.75f), 0.2f);
    }

    @Override
    protected void onResize(int w, int h) {
        proj.perspective((float) Math.toRadians(45f), (float) w / h, 0.1f, 100f);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        time += deltaTime;
        cube.transform.rotation.y = time * 45f;
        cube.transform.rotation.x = time * 20f;
        stall.transform.rotation.y = time * 45f;
    }

    @Override
    protected void onRender() {
        perFrameUBO.upload(buf -> {
            view.get(0, buf);
            proj.get(64, buf);
            buf.putFloat(128, sun.direction.x);
            buf.putFloat(132, sun.direction.y);
            buf.putFloat(136, sun.direction.z);
            buf.putFloat(140, time);
            buf.putFloat(144, sun.color.x);
            buf.putFloat(148, sun.color.y);
            buf.putFloat(152, sun.color.z);
            buf.putFloat(156, 0f);
            buf.putFloat(160, ambient.color.x);
            buf.putFloat(164, ambient.color.y);
            buf.putFloat(168, ambient.color.z);
            buf.putFloat(172, ambient.intensity);
            buf.putFloat(176, cameraPos.x);
            buf.putFloat(180, cameraPos.y);
            buf.putFloat(184, cameraPos.z);
            buf.putFloat(188, 0f);
        });

        scene.render(renderer, shader, perObjectUBO);
    }

    @Override
    protected void onCleanup() {
        shader.cleanup();
        cubeModel.cleanup();
        stallModel.cleanup();
        terrainModel.cleanup();
        perFrameUBO.cleanup();
        perObjectUBO.cleanup();
    }
}
