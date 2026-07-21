package engine.lighting;

import org.joml.Vector3f;

/**
 * Luz direcional (ex: sol) — mesma direcao/intensidade em toda a cena.
 *
 * Dados consumidos pelo fragment shader para Per-Pixel Lighting (diffuse + specular):
 *   direction — de onde a luz vem (aponta da superficie em direcao a luz)
 *   color     — cor/intensidade da luz (RGB, tipicamente 0..1)
 *
 * Ambient e tratado separadamente por [[AmbientLight]] — nao e propriedade
 * do sol, e sim luz de preenchimento global da cena (ex: cor do ceu).
 *
 * Uso:
 *   Light sun = new Light(new Vector3f(1f, 2f, 1.5f), new Vector3f(1f, 1f, 1f));
 */
public final class Light {

    public final Vector3f direction;
    public final Vector3f color;

    public Light(Vector3f direction, Vector3f color) {
        this.direction = new Vector3f(direction).normalize();
        this.color      = color;
    }
}
