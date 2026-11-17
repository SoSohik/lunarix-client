#version 150
uniform sampler2D Sampler0;
layout(std140) uniform ColorGradeData {
    vec4 grade; // saturation, brightness, contrast, tint strength
    vec4 tintVignette;
    vec4 viewport;
};
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec4 scene = texture(Sampler0, texCoord);
    vec3 color = scene.rgb;
    float luminance = dot(color, vec3(0.2126, 0.7152, 0.0722));
    vec3 chroma = (color - vec3(luminance)) * grade.x;
    // Compress out-of-gamut chroma instead of flattening bright channels.
    vec3 available = mix(vec3(luminance), vec3(1.0 - luminance), greaterThanEqual(chroma, vec3(0.0)));
    vec3 limits = available / max(abs(chroma), vec3(0.00001));
    float gamut = min(1.0, min(limits.r, min(limits.g, limits.b)));
    color = clamp(vec3(luminance) + chroma * gamut, 0.0, 1.0);
    color = clamp((color - 0.5) * grade.z + 0.5, 0.0, 1.0);
    // Gamma brightness leaves black/white endpoints intact, unlike a white overlay.
    color = pow(color, vec3(exp2(-grade.y * 2.0)));
    color *= mix(vec3(1.0), tintVignette.rgb * 2.0, grade.w);
    vec2 centered = (texCoord - 0.5) * vec2(viewport.x, 1.0);
    float radius = length(centered) / length(vec2(viewport.x, 1.0) * 0.5);
    color *= 1.0 - tintVignette.a * smoothstep(0.25, 1.0, radius);
    fragColor = vec4(clamp(color, 0.0, 1.0), scene.a);
}
