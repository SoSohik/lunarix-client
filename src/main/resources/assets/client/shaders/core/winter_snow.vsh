#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform Projection {
    mat4 ProjMat;
};

out vec2 textureCoordinate;
out vec4 vertexColor;

void main() {
    gl_Position = ProjMat * vec4(Position, 1.0);
    textureCoordinate = UV0;
    vertexColor = Color;
}
