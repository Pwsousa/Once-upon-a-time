package engine.scene;

import engine.renderer.Model;

/**
 * Entidade renderizavel: referencia a um Model compartilhado + Transform proprio.
 *
 * Varios Entity podem apontar para o mesmo Model — a geometria e textura
 * ficam na GPU uma unica vez; o que muda por entidade e apenas o Transform
 * (enviado ao shader via PerObject UBO antes de cada draw call).
 *
 * Uso:
 *   Model  quadModel = new Model(mesh, texture);   // carrega uma vez
 *   Entity e1 = new Entity(quadModel);
 *   Entity e2 = new Entity(quadModel);             // mesmo recurso GPU
 *   e1.transform.position.set(1, 0, 0);
 *   e2.transform.position.set(-1, 0, 0);
 */
public final class Entity {

    public final Model     model;
    public final Transform transform = new Transform();

    public Entity(Model model) {
        this.model = model;
    }
}
