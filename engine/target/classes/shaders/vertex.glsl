#version 330 core

// ---------------------------------------------------------------
// Atributos de entrada (dados do VBO configurados no VAO)
// ---------------------------------------------------------------
layout (location = 0) in vec3 aPos;       // posicao do vertice (model space)
layout (location = 1) in vec3 aColor;     // cor por vertice
layout (location = 2) in vec2 aTexCoord;  // coordenada UV da textura [0,1]

// ---------------------------------------------------------------
// Saidas para o fragment shader (interpoladas entre vertices)
// ---------------------------------------------------------------
out vec3 vColor;
out vec3 vFragPos;    // posicao no world space (util para iluminacao futura)
out vec2 vTexCoord;   // UV interpolado — rasterizador interpola entre vertices

// ---------------------------------------------------------------
// UBO PerFrame (binding point 0) — dados compartilhados por toda a cena
//
// std140: alinhamento fixo, independente do driver/GPU.
//   mat4  → 64 bytes (4 colunas × vec4)
//   float → 4 bytes (alinhado em 4 apos as matrizes)
//
// Offsets no buffer:
//   uView       → offset   0  (64 bytes)
//   uProjection → offset  64  (64 bytes)
//   uTime       → offset 128  ( 4 bytes)
// ---------------------------------------------------------------
layout(std140) uniform PerFrame {
    mat4  uView;
    mat4  uProjection;
    float uTime;
};

// ---------------------------------------------------------------
// UBO PerObject (binding point 1) — dados por objeto individual
//
//   uModel → offset 0 (64 bytes)
// ---------------------------------------------------------------
layout(std140) uniform PerObject {
    mat4 uModel;
};

void main() {
    vec4 worldPos   = uModel * vec4(aPos, 1.0);
    gl_Position     = uProjection * uView * worldPos;

    vFragPos   = worldPos.xyz;
    vColor     = aColor;
    vTexCoord  = aTexCoord;
}
