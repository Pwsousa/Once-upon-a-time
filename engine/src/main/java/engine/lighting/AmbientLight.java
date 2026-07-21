package engine.lighting;

import org.joml.Vector3f;

/**
 * Luz ambiente — piso de iluminacao uniforme aplicado a cena inteira,
 * independente da direcao da luz principal ([[Light]]).
 *
 * Representa luz indireta/de preenchimento (ex: cor do ceu refletindo em
 * tudo) — sem ela, faces de costas para a luz direcional ficam 100% pretas.
 *
 * Uso:
 *   AmbientLight ambient = new AmbientLight(new Vector3f(0.5f, 0.6f, 0.75f), 0.2f);
 */
public final class AmbientLight {

    public final Vector3f color;
    public float           intensity;

    public AmbientLight(Vector3f color, float intensity) {
        this.color     = color;
        this.intensity = intensity;
    }
}
