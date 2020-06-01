precision mediump float;

uniform sampler2D sourceImage;
uniform sampler2D maskImage1;
uniform sampler2D maskImage2;
uniform sampler2D maskImage3;

varying vec2 vTextureCoord;

uniform int comOp[3];
uniform float alpha[3];

uniform float resultAlpha;

vec4 DarkenBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity) {

    if (sourceColor.a > 0.0) {
        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);

        mediump vec3 result = min(sourceColor.rgb, secondSourceColor.rgb);

        result = clamp(result, 0.0, 1.0);

        return vec4(mix(secondSourceColor.rgb, result, sourceColor.a * opacity), 1.0);
    } else {
    	return secondSourceColor;
    }
}

vec4 LightenBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity) {

    if (sourceColor.a > 0.0) {
        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);

        mediump vec3 result = max(sourceColor.rgb, secondSourceColor.rgb);

        result = clamp(result, 0.0, 1.0);

        return vec4(mix(secondSourceColor.rgb, result, sourceColor.a * opacity), 1.0);
    } else {
    	return secondSourceColor;
    }
}

vec4 MultiplyBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity) {

    if (sourceColor.a > 0.0) {
        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);

        mediump vec3 result = sourceColor.rgb * secondSourceColor.rgb;
        result = clamp(result, 0.0, 1.0);

        return vec4(mix(secondSourceColor.rgb, result, sourceColor.a * opacity), 1.0);
    } else {
    	return secondSourceColor;
    }
}

vec4 OverlayBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity) {

    if (sourceColor.a > 0.0) {
        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);

        mediump float ra;
        if(secondSourceColor.r <= 0.5) {
            ra = 2.0 * sourceColor.r * secondSourceColor.r;
        } else {
            ra = 1.0 - 2.0 * (1.0 - sourceColor.r) * (1.0 - secondSourceColor.r);
        }
        mediump float ga;
        if (secondSourceColor.g <= 0.5) {
            ga = 2.0 * sourceColor.g * secondSourceColor.g;
        } else {
            ga = 1.0 - 2.0 * (1.0 - sourceColor.g) * (1.0 - secondSourceColor.g);
        }
        mediump float ba;
        if (secondSourceColor.b <= 0.5) {
            ba = 2.0 * sourceColor.b * secondSourceColor.b;
        } else {
            ba = 1.0 - 2.0 * (1.0 - sourceColor.b) * (1.0 - secondSourceColor.b);
        }

        mediump vec3 result = vec3(ra, ga, ba);
        result = clamp(result, 0.0, 1.0);

        return vec4(mix(secondSourceColor.rgb, result, sourceColor.a * opacity), 1.0);
    } else {
    	 return secondSourceColor;
    }
}

vec4 ScreenBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity) {

    if (sourceColor.a > 0.0) {
        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);

        mediump vec3 whiteColor = vec3(1.0);

        mediump vec3 result = whiteColor - ((whiteColor - sourceColor.rgb) * (whiteColor - secondSourceColor.rgb));
        result = clamp(result, 0.0, 1.0);

        return vec4(mix(secondSourceColor.rgb, result, sourceColor.a * opacity), 1.0);
    } else {
    	  return secondSourceColor;
    }
}

vec4 blendColor(vec4 base, vec4 overlay, int com, float opa) {

    vec4 result;

    if (com == 33) {
        result = LightenBlend(base, overlay, opa);
    } else if(com == 38) {
        result = MultiplyBlend(base, overlay, opa);
    } else if(com == 41) {
        result = OverlayBlend(base, overlay, opa);
    } else if(com == 45) {
        result = ScreenBlend(base, overlay, opa);
    } else if(com == 20) {
        result = DarkenBlend(base, overlay, opa);
    } else {
        result = base;
    }

    return result;
}

void main() {

    vec4 textureColor= texture2D(sourceImage, vTextureCoord);

    vec4 result = textureColor;

    if (alpha[0] > 0.0) {
        vec4 maskColor = texture2D(maskImage1, vTextureCoord);
        result = blendColor(result, maskColor, comOp[0], alpha[0]);
    }

    if (alpha[1] > 0.0) {
         vec4 maskColor1 = texture2D(maskImage2, vTextureCoord);
         result = blendColor(result, maskColor1, comOp[1], alpha[1]);
    }

    if (alpha[2] > 0.0) {
         vec4 maskColor2 = texture2D(maskImage3, vTextureCoord);
         result = blendColor(result, maskColor2, comOp[2], alpha[2]);
    }

    gl_FragColor = mix(textureColor, result, resultAlpha); // x*(1-a)+y*a
 }