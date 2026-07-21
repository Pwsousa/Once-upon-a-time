package engine.scene;

import engine.renderer.Model;
import engine.renderer.Renderer;
import engine.renderer.ShaderProgram;
import engine.renderer.UniformBuffer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agrupa entidades por Model (HashMap) para minimizar troca de estado GPU.
 *
 * Sem batching: cada Entity gera 1 bind de textura + 3 setUniform, mesmo
 * quando N entidades compartilham o mesmo Model (mesh+textura+material).
 * Com batching: textura e uniforms de material sao setados UMA vez por
 * grupo de Model — apenas o transform (uModel) muda por entidade dentro
 * do grupo. Beneficio cresce com o numero de instancias do mesmo modelo
 * (ex: floresta de arvores, multiplicos stalls).
 *
 * Uso:
 *   Scene scene = new Scene();
 *   scene.add(cube);
 *   scene.add(stall);
 *   scene.render(shader, perObjectUBO);
 */
public final class Scene {

    private final Map<Model, List<Entity>> batches = new LinkedHashMap<>();

    public void add(Entity entity) {
        batches.computeIfAbsent(entity.model, m -> new ArrayList<>()).add(entity);
    }

    public void remove(Entity entity) {
        List<Entity> group = batches.get(entity.model);
        if (group == null) return;
        group.remove(entity);
        if (group.isEmpty()) batches.remove(entity.model);
    }

    /**
     * Desenha todas as entidades, agrupadas por Model.
     *
     * Ordem: opacos e cutout primeiro, depois transparentes (blending real)
     * por ultimo e com depth write desligado — assim o transparente nunca
     * esconde algo atras dele por causa da propria profundidade escrita
     * (nao ha sorting entre transparentes entre si; ok para poucos objetos
     * translucidos que nao se sobrepoem, ver limitacao na doc da classe).
     *
     * @param perObjectUBO UBO usado para o transform (uModel) de cada entidade
     */
    public void render(Renderer renderer, ShaderProgram shader, UniformBuffer perObjectUBO) {
        shader.bind();

        for (Map.Entry<Model, List<Entity>> batch : batches.entrySet()) {
            if (!batch.getKey().transparent) drawBatch(shader, perObjectUBO, batch);
        }

        renderer.setDepthWrite(false);
        for (Map.Entry<Model, List<Entity>> batch : batches.entrySet()) {
            if (batch.getKey().transparent) drawBatch(shader, perObjectUBO, batch);
        }
        renderer.setDepthWrite(true);

        shader.unbind();
    }

    private void drawBatch(ShaderProgram shader, UniformBuffer perObjectUBO, Map.Entry<Model, List<Entity>> batch) {
        Model model = batch.getKey();

        model.texture.bind(0);
        shader.setUniform("uTexture", 0);
        shader.setUniform("uShininess", model.shininess);
        shader.setUniform("uSpecularStrength", model.specularStrength);
        shader.setUniform("uAtlasOffset", model.atlasOffset);
        shader.setUniform("uAtlasScale", model.atlasScale);
        shader.setUniform("uAlphaCutoff", model.alphaCutoff);

        for (Entity entity : batch.getValue()) {
            perObjectUBO.upload(buf -> entity.transform.getMatrix().get(0, buf));
            model.mesh.draw();
        }
    }
}
