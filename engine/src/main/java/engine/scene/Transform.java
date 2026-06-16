package engine.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Transformacao de um objeto no espaco 3D.
 *
 * Campos publicos mutaveis — atualize diretamente em onUpdate():
 *   position — translacao (x, y, z)
 *   rotation — rotacao Euler em GRAUS (pitch=X, yaw=Y, roll=Z)
 *   scale    — escala por eixo (padrao: 1, 1, 1)
 *
 * Ordem de aplicacao: T * Ry * Rx * Rz * S
 *   (translate → yaw → pitch → roll → scale)
 */
public final class Transform {

    public final Vector3f position = new Vector3f(0f, 0f, 0f);
    public final Vector3f rotation = new Vector3f(0f, 0f, 0f); // graus
    public final Vector3f scale    = new Vector3f(1f, 1f, 1f);

    private final Matrix4f matrix = new Matrix4f(); // reutilizado — sem GC

    /** Reconstroi e retorna a model matrix TRS. Sem alocacao por chamada. */
    public Matrix4f getMatrix() {
        return matrix.identity()
                .translate(position)
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateZ((float) Math.toRadians(rotation.z))
                .scale(scale);
    }
}
