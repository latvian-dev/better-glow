#version 150

const float TWO_PI = 6.28318530718;
const float zNear = 0.1;
const float zFar = 1000.0;

uniform float NearSize;
uniform float FarSize;
uniform float NoClip;
uniform float Visibility;
uniform float Quality;

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D MCDepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main(){
    if (Visibility <= 0.0) {
        discard;
    }

    vec4 center = texture(DiffuseSampler, texCoord);
    float centerDepth = texture(DiffuseDepthSampler, texCoord).x;
    float mcDepth = texture(MCDepthSampler, texCoord).x;

    if (center.a > 0.0) {
        discard;
    }

    float hstep = TWO_PI / (Quality * 4);
    float vstep = 1.0 / Quality;
    float highestAlpha = 0.0;

    for (float d = 0.0; d < TWO_PI; d += hstep) {
        for (float i = vstep; i <= 1.0; i += vstep) {
            float ha = texture(DiffuseSampler, texCoord + vec2(cos(d) * NearSize * oneTexel.x, sin(d) * NearSize * oneTexel.y) * i).a;

            if (ha > highestAlpha) {
                highestAlpha = ha;
            }
        }
    }

    if (highestAlpha <= 0.0) {
        discard;
    }

    float Size = FarSize + (NearSize - FarSize) * highestAlpha;

    vec4 glow = vec4(0.0);
    float count = 0.0;

    for (float d = 0.0; d < TWO_PI; d += hstep) {
        for (float i = vstep; i <= 1.0; i += vstep) {
            vec4 g = texture(DiffuseSampler, texCoord + vec2(cos(d) * Size * oneTexel.x, sin(d) * Size * oneTexel.y) * i);

            if (g.a > 0.0) {
                glow += vec4(g.rgb, Visibility);
                count++;
            }
        }
    }

    fragColor = glow / count;
}
