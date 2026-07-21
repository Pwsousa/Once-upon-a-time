#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;     // normal em model space
layout (location = 2) in vec2 aTexCoord;

out vec3 vNormal;    // normal em world space (para lighting no fragment)
out vec3 vFragPos;   // posicao em world space
out vec2 vTexCoord;

layout(std140) uniform PerFrame {
    mat4 uView;
    mat4 uProjection;
    vec4 uLightDirTime;      // xyz = direcao da luz (world space), w = uTime
    vec4 uLightColorAmbient; // rgb = cor da luz, a = ambient strength
    vec4 uViewPos;           // xyz = posicao da camera (world space), w nao usado
};

layout(std140) uniform PerObject {
    mat4 uModel;
};

void main() {
    vec4 worldPos = uModel * vec4(aPos, 1.0);
    gl_Position   = uProjection * uView * worldPos;

    // transforma normal para world space
    // mat3(uModel) funciona corretamente para rotacao + escala uniforme
    // para escala nao-uniforme: usar transpose(inverse(mat3(uModel)))
    vNormal   = normalize(mat3(uModel) * aNormal);
    vFragPos  = worldPos.xyz;
    vTexCoord = aTexCoord;
}
