#version 150

uniform sampler2D Sampler0;

in vec2 textureCoordinate;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, textureCoordinate) * vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color;
}
