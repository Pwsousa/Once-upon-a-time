#version 330 core

// ---------------------------------------------------------------
// Entradas interpoladas do vertex shader
// ---------------------------------------------------------------
in vec3 vColor;
in vec3 vFragPos;

// ---------------------------------------------------------------
// Saida — cor final do fragmento (pixel)
// ---------------------------------------------------------------
out vec4 fragColor;

// ---------------------------------------------------------------
// Uniforms
// ---------------------------------------------------------------
uniform float uTime;  // tempo em segundos desde o inicio

void main() {
    // Pulso de brilho baseado no tempo — demonstra uso de uTime
    // sin() retorna [-1,1], remapeamos para [0.4, 1.0] para nao apagar
    float pulse = 0.4 + 0.6 * abs(sin(uTime * 1.5));

    fragColor = vec4(vColor * pulse, 1.0);
}
