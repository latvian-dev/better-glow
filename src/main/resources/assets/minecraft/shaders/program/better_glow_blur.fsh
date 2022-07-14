#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Radius;

out vec4 fragColor;

void main() {
    vec4 blurred = vec4(0.0);

    for (float x = -1.0; x <= 1.0; x += 1.0) {
        for (float y = -1.0; y <= 1.0; y += 1.0) {
            blurred += texture(DiffuseSampler, texCoord + oneTexel * vec2(x, y));
        }
    }

    if (blurred.a == 0.0) {
        discard;
    }

    fragColor = blurred / 9.0;
}
