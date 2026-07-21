package engine.lighting;

import org.joml.Vector3f;

/**
 * Luz direcional (ex: sol) — mesma direcao/intensidade em toda a cena.
 *
 * Dados consumidos pelo fragment shader para Per-Pixel Lighting (Blinn-Phong):
 *   direction       — de onde a luz vem (aponta da superficie em direcao a luz)
 *   color           — cor/intensidade da luz (RGB, tipicamente 0..1)
 *   ambientStrength — piso de luz que evita faces traseiras 100% pretas
 *
 * Uso:
 *   Light sun = new Light(new Vector3f(1f, 2f, 1.5f), new Vector3f(1f, 1f, 1f), 0.25f);
 *   sun.direction.normalize();
 */
public final class Light {

    public final Vector3f direction;
    public final Vector3f color;
    public float           ambientStrength;

    public Light(Vector3f direction, Vector3f color, float ambientStrength) {
        this.direction       = new Vector3f(direction).normalize();
        this.color           = color;
        this.ambientStrength = ambientStrength;
    }
}
