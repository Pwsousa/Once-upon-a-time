#version 330 core

in vec3 vNormal;
in vec3 vFragPos;
in vec2 vTexCoord;

out vec4 fragColor;

layout(std140) uniform PerFrame {
    mat4 uView;
    mat4 uProjection;
    vec4 uLightDirTime; // xyz = direcao da luz (world space), w = uTime
    vec4 uLightColor;   // rgb = cor da luz direcional, a nao usado
    vec4 uAmbientColor; // rgb = cor da luz ambiente, a = intensidade
    vec4 uViewPos;      // xyz = posicao da camera (world space), w nao usado
};

uniform sampler2D uTexture;
uniform float     uShininess        = 32.0;      // expoente do brilho especular (mais alto = mais concentrado)
uniform float     uSpecularStrength = 0.5;       // intensidade do highlight especular
uniform vec2      uAtlasOffset      = vec2(0.0); // canto inferior-esquerdo do tile no atlas
uniform vec2      uAtlasScale       = vec2(1.0); // tamanho do tile dentro do atlas (1,1 = textura inteira)

void main() {
    vec3 N        = normalize(vNormal);
    vec3 lightDir = normalize(uLightDirTime.xyz);
    vec3 lightCol = uLightColor.rgb;

    // ambient: luz de preenchimento global, independente da luz direcional
    vec3 ambient = uAmbientColor.rgb * uAmbientColor.a;

    // diffuse (Lambert): dot(N, L), clampado em [0, 1]
    float diff    = max(dot(N, lightDir), 0.0);
    vec3  diffuse = diff * lightCol;

    // specular (Blinn-Phong): highlight baseado no vetor halfway entre luz e camera
    vec3  viewDir    = normalize(uViewPos.xyz - vFragPos);
    vec3  halfwayDir = normalize(lightDir + viewDir);
    float specFactor = pow(max(dot(N, halfwayDir), 0.0), uShininess);
    vec3  specular   = uSpecularStrength * specFactor * lightCol;

    // mosaico: fract() repete o padrao dentro do tile do atlas (uAtlasScale),
    // sem vazar para tiles vizinhos — vTexCoord pode exceder [0,1] (tiling)
    vec2 tiledUV = fract(vTexCoord);
    vec2 atlasUV = uAtlasOffset + tiledUV * uAtlasScale;
    vec4 texColor = texture(uTexture, atlasUV);
    vec3 result   = (ambient + diffuse) * texColor.rgb + specular;
    fragColor     = vec4(result, texColor.a);
}
