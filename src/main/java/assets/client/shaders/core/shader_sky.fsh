#version 150

uniform sampler2D Sampler0;

layout(std140) uniform ShaderSkyData {
    vec4 meta;          // x: width, y: height, z: time, w: alpha
    vec4 color1;        // rgba
    vec4 color2;        // rgba
    vec4 accentColor;   // rgba
    vec4 params;        // x: modeId, y: speed, z: scale, w: intensity
    vec4 params2;       // x: stars, y: vanillaSky, z: invProjX, w: invProjY
    vec4 cameraRight;   // xyz, 0
    vec4 cameraUp;      // xyz, 0
    vec4 cameraForward; // xyz, 0
};

in vec2 texCoord;
out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 rot = mat2(0.8, -0.6, 0.6, 0.8);
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p = rot * p * 2.0;
        a *= 0.5;
    }
    return v;
}

vec3 getRayDirection(vec2 uv) {
    vec2 ndc = uv * 2.0 - 1.0;
    ndc.x *= params2.z;
    ndc.y *= params2.w;
    return normalize(cameraForward.xyz + cameraRight.xyz * ndc.x + cameraUp.xyz * ndc.y);
}

void main() {
    float time = meta.z * params.y * 0.4;
    float globalAlpha = meta.w;
    int mode = int(params.x);

    vec3 dir = getRayDirection(texCoord);
    vec2 skyUv = vec2(atan(dir.z, dir.x) / 6.2831853 + 0.5, dir.y * 0.5 + 0.5);

    vec3 c1 = color1.rgb;
    vec3 c2 = color2.rgb;
    vec3 cAcc = accentColor.rgb;

    vec3 sky = vec3(0.0);

    if (mode == 0) {
        // Aurora
        float wave = sin(skyUv.x * 12.0 + time * 1.5) * 0.15 + cos(skyUv.x * 24.0 - time * 2.0) * 0.08;
        float band = smoothstep(0.45 + wave, 0.55 + wave, skyUv.y) * (1.0 - smoothstep(0.55 + wave, 0.75 + wave, skyUv.y));
        sky = mix(c1, c2, sin(skyUv.x * 6.0 + time) * 0.5 + 0.5) * band * 1.8;
    } else if (mode == 1) {
        // Night / Deep Cosmos
        float stars = pow(hash(floor(skyUv * 280.0)), 18.0) * params2.x;
        float neb = fbm(skyUv * 6.0 * params.z + vec2(time * 0.05, 0.0));
        sky = mix(c1 * 0.25, c2 * 0.7, neb) + vec3(stars);
    } else if (mode == 2) {
        // Snow / Falling Dust
        vec2 snowUv = skyUv * 40.0;
        snowUv.y += time * 3.0;
        float snow = pow(hash(floor(snowUv)), 16.0) * 1.5;
        sky = mix(c1 * 0.3, c2 * 0.5, skyUv.y) + vec3(snow);
    } else if (mode == 3) {
        // Sky / Gradient
        float g = smoothstep(0.1, 0.9, skyUv.y);
        sky = mix(c1, c2, g);
    } else if (mode == 4) {
        // Starfield
        float s1 = pow(hash(floor(skyUv * 220.0)), 20.0);
        float s2 = pow(hash(floor(skyUv * 400.0 + 17.0)), 24.0) * (sin(time * 5.0 + hash(floor(skyUv * 400.0)) * 6.28) * 0.5 + 0.5);
        sky = mix(c1 * 0.15, c2 * 0.3, skyUv.y) + vec3((s1 + s2) * params2.x * 1.5);
    } else if (mode == 5) {
        // Glow Horizon
        float horizon = exp(-abs(skyUv.y - 0.5) * 8.0 * params.z);
        sky = mix(c1, c2, skyUv.y) + cAcc * horizon * 1.2;
    } else if (mode == 6) {
        // Nebula
        float n1 = fbm(skyUv * 5.0 * params.z + vec2(time * 0.04, time * 0.02));
        float n2 = fbm(skyUv * 10.0 * params.z - vec2(time * 0.03, time * 0.05));
        sky = c1 * n1 + c2 * n2 + cAcc * (n1 * n2 * 1.5);
    } else {
        // WildSky (Signature procedural aesthetic)
        float n = fbm(skyUv * 7.0 * params.z + vec2(time * 0.05, time * 0.02));
        float stars = pow(hash(floor(skyUv * 320.0)), 22.0) * params2.x * 2.0;
        // Godray atmospheric rays
        float ray = sin(skyUv.x * 16.0 + time * 0.8) * 0.5 + 0.5;
        ray = pow(ray, 3.0) * smoothstep(0.3, 0.8, skyUv.y) * 0.45;
        sky = mix(c1 * 0.4, c2, n) + cAcc * ray + vec3(stars);
    }

    sky *= params.w; // intensity
    fragColor = vec4(clamp(sky, 0.0, 1.0), globalAlpha);
}
