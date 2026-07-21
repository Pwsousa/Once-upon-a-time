package engine.scene;

import engine.renderer.Model;
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
     * @param perObjectUBO UBO usado para o transform (uModel) de cada entidade
     */
    public void render(ShaderProgram shader, UniformBuffer perObjectUBO) {
        shader.bind();

        for (Map.Entry<Model, List<Entity>> batch : batches.entrySet()) {
            Model model = batch.getKey();

            model.texture.bind(0);
            shader.setUniform("uTexture", 0);
            shader.setUniform("uShininess", model.shininess);
            shader.setUniform("uSpecularStrength", model.specularStrength);

            for (Entity entity : batch.getValue()) {
                perObjectUBO.upload(buf -> entity.transform.getMatrix().get(0, buf));
                model.mesh.draw();
            }
        }

        shader.unbind();
    }
}
