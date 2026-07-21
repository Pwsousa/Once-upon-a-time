package engine.renderer;

import engine.utils.ResourceLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser de arquivos OBJ Wavefront.
 *
 * Suporta:
 *   v   — posicoes
 *   vt  — coordenadas UV
 *   vn  — normais
 *   f   — faces trianguladas ou quads (triangulacao em fan: 0-1-2, 0-2-3, ...)
 *
 * Ignora: o, g, s, mtllib, usemtl, comentarios (#).
 *
 * Saida (formato interleaved, 8 floats por vertice):
 *   layout 0 — posicao  (vec3)
 *   layout 1 — normal   (vec3)  ← unit vector, pronto para lighting
 *   layout 2 — UV       (vec2)
 *
 * Uso:
 *   ObjLoader.MeshData d = ObjLoader.load("models/cube.obj");
 *   Mesh mesh = Mesh.create(d.vertices, d.indices, 3, 3, 2);
 *
 * Deduplicacao:
 *   OBJ usa indices separados para pos/uv/normal ("1/2/3").
 *   O loader cria um vertice de saida por combinacao unica (pos/uv/normal),
 *   reutilizando indices quando a mesma combinacao aparece em multiplas faces.
 */
public final class ObjLoader {

    /** Container para os dados de vertice/indice prontos para Mesh.create(). */
    public static final class MeshData {
        public final float[] vertices;
        public final int[]   indices;

        MeshData(float[] vertices, int[] indices) {
            this.vertices = vertices;
            this.indices  = indices;
        }
    }

    private ObjLoader() {}

    /**
     * Carrega e converte um arquivo OBJ do classpath.
     *
     * @param resourcePath  caminho relativo ao classpath, ex: "models/cube.obj"
     * @return MeshData com arrays prontos para Mesh.create(data.vertices, data.indices, 3, 3, 2)
     */
    public static MeshData load(String resourcePath) {
        String[] lines = ResourceLoader.loadString(resourcePath).split("\n");

        // dados brutos do OBJ (listas independentes, indices 1-based)
        List<float[]> positions = new ArrayList<>();
        List<float[]> uvs       = new ArrayList<>();
        List<float[]> normals   = new ArrayList<>();

        // saida interleaved (8 floats por vertice)
        List<Float>   verts    = new ArrayList<>();
        List<Integer> indices  = new ArrayList<>();

        // chave "posIdx/uvIdx/normalIdx" → indice de saida (deduplicacao)
        Map<String, Integer> seen = new LinkedHashMap<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] p = line.split("\\s+");

            if ("v".equals(p[0])) {
                positions.add(new float[]{
                    Float.parseFloat(p[1]),
                    Float.parseFloat(p[2]),
                    Float.parseFloat(p[3])
                });
            } else if ("vt".equals(p[0])) {
                uvs.add(new float[]{
                    Float.parseFloat(p[1]),
                    Float.parseFloat(p[2])
                });
            } else if ("vn".equals(p[0])) {
                normals.add(new float[]{
                    Float.parseFloat(p[1]),
                    Float.parseFloat(p[2]),
                    Float.parseFloat(p[3])
                });
            } else if ("f".equals(p[0])) {
                // f pode ter 3 (triangulo) ou 4+ (poligono — triangulacao em fan)
                int faceCount = p.length - 1;
                int[] faceIdx = new int[faceCount];
                for (int i = 0; i < faceCount; i++) {
                    faceIdx[i] = resolve(p[i + 1], positions, uvs, normals, verts, seen);
                }
                // fan: 0-1-2, 0-2-3, 0-3-4, ...
                for (int i = 1; i < faceCount - 1; i++) {
                    indices.add(faceIdx[0]);
                    indices.add(faceIdx[i]);
                    indices.add(faceIdx[i + 1]);
                }
            }
            // ignora: o, g, s, mtllib, usemtl, l
        }

        float[] va = new float[verts.size()];
        for (int i = 0; i < va.length; i++) va[i] = verts.get(i);

        int[] ia = new int[indices.size()];
        for (int i = 0; i < ia.length; i++) ia[i] = indices.get(i);

        System.out.printf("[ObjLoader] %s → %d vertices, %d triangulos%n",
                resourcePath, va.length / 8, ia.length / 3);

        return new MeshData(va, ia);
    }

    /**
     * Resolve um token OBJ ("v/vt/vn", "v//vn", "v/vt", "v") para um indice
     * de saida, adicionando o vertice interleaved se for a primeira vez.
     */
    private static int resolve(String token,
                                List<float[]> positions,
                                List<float[]> uvs,
                                List<float[]> normals,
                                List<Float>   verts,
                                Map<String, Integer> seen) {
        Integer cached = seen.get(token);
        if (cached != null) return cached;

        String[] idx = token.split("/");

        int vi  = resolveIndex(Integer.parseInt(idx[0]), positions.size());                                   // pos (obrigatorio)
        int vti = (idx.length > 1 && !idx[1].isEmpty()) ? resolveIndex(Integer.parseInt(idx[1]), uvs.size())     : -1;
        int vni = (idx.length > 2 && !idx[2].isEmpty()) ? resolveIndex(Integer.parseInt(idx[2]), normals.size()) : -1;

        float[] pos = positions.get(vi);
        float[] uv  = vti >= 0 && vti < uvs.size()     ? uvs.get(vti)     : new float[]{0f, 0f};
        float[] n   = vni >= 0 && vni < normals.size()  ? normals.get(vni) : new float[]{0f, 1f, 0f};

        // interleaved: pos(x,y,z) | normal(x,y,z) | uv(u,v)
        verts.add(pos[0]); verts.add(pos[1]); verts.add(pos[2]);
        verts.add(n[0]);   verts.add(n[1]);   verts.add(n[2]);
        verts.add(uv[0]);  verts.add(uv[1]);

        int newIdx = seen.size();
        seen.put(token, newIdx);
        return newIdx;
    }

    /**
     * Converte indice OBJ (1-based, ou negativo relativo ao total ja declarado)
     * para indice 0-based na lista correspondente.
     */
    private static int resolveIndex(int raw, int countSoFar) {
        return raw < 0 ? countSoFar + raw : raw - 1;
    }
}
