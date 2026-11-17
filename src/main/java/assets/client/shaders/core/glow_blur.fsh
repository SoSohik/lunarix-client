#version 150

uniform sampler2D Sampler0;

layout(std140) uniform GlowBlurData {
    vec4 directionRadius;
};

in vec2 texCoord;
out vec4 fragColor;

// Ported from the donor target ESP `core/glow_blur` shader.
// 9-tap separable Gaussian (sigma ~ radius/3) folded into five
// hardware-linear samples. Direction is (1,0) for the horizontal
// pass and (0,1) for the vertical pass.
void main() {
    vec2 texelSize = 1.0 / vec2(textureSize(Sampler0, 0));
    vec2 offset = directionRadius.xy * texelSize * directionRadius.z;

    vec4 sum = texture(Sampler0, texCoord) * 0.227027;
    sum += texture(Sampler0, texCoord + offset * 1.384615) * 0.316216;
    sum += texture(Sampler0, texCoord - offset * 1.384615) * 0.316216;
    sum += texture(Sampler0, texCoord + offset * 3.230769) * 0.070270;
    sum += texture(Sampler0, texCoord - offset * 3.230769) * 0.070270;

    fragColor = sum;
}
