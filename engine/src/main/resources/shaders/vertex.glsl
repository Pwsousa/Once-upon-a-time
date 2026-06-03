#version 330 core

// ---------------------------------------------------------------
// Atributos de entrada (dados do VBO configurados no VAO)
// ---------------------------------------------------------------
layout (location = 0) in vec3 aPos;    // posicao do vertice (model space)
layout (location = 1) in vec3 aColor;  // cor por vertice

// ---------------------------------------------------------------
// Saidas para o fragment shader (interpoladas entre vertices)
// ---------------------------------------------------------------
out vec3 vColor;
out vec3 vFragPos;  // posicao no world space (util para iluminacao futura)

// ---------------------------------------------------------------
// Uniforms — MVP (Model * View * Projection)
//
// Pipeline de transformacao:
//   model space  --(uModel)-->  world space
//   world space  --(uView)-->   camera/view space
//   view space   --(uProj)-->   clip space  -->  NDC  -->  screen
// ---------------------------------------------------------------
uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

void main() {
    vec4 worldPos   = uModel * vec4(aPos, 1.0);
    gl_Position     = uProjection * uView * worldPos;

    vFragPos = worldPos.xyz;
    vColor   = aColor;
}
