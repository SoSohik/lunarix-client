#version 150

uniform sampler2D Sampler0;

layout(std140) uniform ShaderSkyData {
    vec4 meta;          // x: width, y: height, z: time, w: alpha
    vec4 color1;
    vec4 color2;
    vec4 accentColor;
    vec4 params;
    vec4 params2;
    vec4 cameraRight;
    vec4 cameraUp;
    vec4 cameraForward;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 blurred = texture(Sampler0, texCoord);
    vec3 tint = mix(color1.rgb, color2.rgb, texCoord.y);
    vec3 result = mix(blurred.rgb, tint, 0.35 * params.w);
    fragColor = vec4(result, meta.w);
}
