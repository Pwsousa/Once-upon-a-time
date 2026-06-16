#version 330 core

// ---------------------------------------------------------------
// Entradas interpoladas do vertex shader
// ---------------------------------------------------------------
in vec3 vColor;
in vec3 vFragPos;
in vec2 vTexCoord;  // UV interpolado pelo rasterizador

// ---------------------------------------------------------------
// Saida — cor final do fragmento (pixel)
// ---------------------------------------------------------------
out vec4 fragColor;

// ---------------------------------------------------------------
// UBO PerFrame (binding point 0) — mesma declaracao do vertex shader
//
// Samplers nao podem entrar em UBOs (restricao OpenGL) —
// uTexture permanece como uniform classico.
// ---------------------------------------------------------------
layout(std140) uniform PerFrame {
    mat4  uView;
    mat4  uProjection;
    float uTime;
};

uniform sampler2D uTexture;  // slot de textura passado via setUniform

void main() {
    // Pulso de brilho baseado no tempo (vem do UBO PerFrame)
    float pulse = 0.4 + 0.6 * abs(sin(uTime * 1.5));

    vec4 texColor = texture(uTexture, vTexCoord);

    fragColor = texColor * vec4(vColor * pulse, 1.0);
}
