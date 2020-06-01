precision mediump float;

uniform sampler2D sourceImage;
uniform sampler2D lookupTexture;
uniform sampler2D basicBlackWhite;

varying vec2 vTextureCoord;

uniform float alpha;
uniform int isBlackWhite; // 0表示非黑白滤镜，1为黑白滤镜

vec4 getLookUpColor(vec4 textureColor, sampler2D lookUpTable) {

    float blueColor = textureColor.b * 15.0;

    vec2 quad1;
    quad1.y = floor(floor(blueColor) / 4.0);
    quad1.x = floor(blueColor) - (quad1.y * 4.0);

    vec2 quad2;
    quad2.y = floor(ceil(blueColor) / 4.0);
    quad2.x = ceil(blueColor) - (quad2.y * 4.0);

    vec2 texPos1;
    texPos1.x = (quad1.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);
    texPos1.y = (quad1.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);

    vec2 texPos2;
    texPos2.x = (quad2.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);
    texPos2.y = (quad2.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);

    vec4 newColor1 = texture2D(lookUpTable, texPos1);
    vec4 newColor2 = texture2D(lookUpTable, texPos2);

    return mix(newColor1, newColor2, fract(blueColor));
}

void main() {

	vec4 textureColor = texture2D(sourceImage, vTextureCoord);
    vec4 newColor = getLookUpColor(textureColor, lookupTexture);

    if (isBlackWhite == 1) {
        vec4 basicBlackTextureColor = getLookUpColor(textureColor, basicBlackWhite);
        gl_FragColor = mix(basicBlackTextureColor, vec4(newColor.rgb, textureColor.w), alpha);
    } else {
        gl_FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), alpha);
    }
}