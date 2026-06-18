#version 330 core

in vec3 vNormal;
in vec3 vFragPos;
in vec2 vTexCoord;

out vec4 fragColor;

layout(std140) uniform PerFrame {
    mat4  uView;
    mat4  uProjection;
    float uTime;
};

uniform sampler2D uTexture;

void main() {
    // luz direcional fixa no world space (direcao normalizada)
    vec3  lightDir = normalize(vec3(1.0, 2.0, 1.5));

    // Lambertian diffuse: dot(N, L), clampado em [0, 1]
    float diffuse  = max(dot(vNormal, lightDir), 0.0);

    // ambient impede faces traseiras de ficarem completamente pretas
    float ambient  = 0.25;
    float light    = ambient + (1.0 - ambient) * diffuse;

    vec4 texColor = texture(uTexture, vTexCoord);
    fragColor = vec4(texColor.rgb * light, texColor.a);
}
