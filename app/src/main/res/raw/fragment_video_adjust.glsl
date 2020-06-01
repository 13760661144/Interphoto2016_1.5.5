precision mediump float;

uniform sampler2D sourceImage;
varying vec2 vTextureCoord;

uniform float values[7];

// 饱和度
const vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

// 色温
const vec3 warmFilter = vec3(0.93, 0.54, 0.0);
const mat3 RGBtoYIQ = mat3(0.299, 0.587, 0.114, 0.596, -0.274, -0.322, 0.212, -0.523, 0.311);
const mat3 YIQtoRGB = mat3(1.0, 0.956, 0.621, 1.0, -0.272, -0.647, 1.0, -1.105, 1.702);

// 高光和阴影
const vec3 luminanceWeighting2 = vec3(0.3, 0.3, 0.3);

float brightness(float color, float bvalue) {
    float tvalue = clamp(bvalue, -150.0, 150.0);

    if ((tvalue > 0.0) && (tvalue < 150.0)) {
        float bv = 150.0 / (150.0 - tvalue) - 1.0;
        bv = clamp(color + color * bv, 0.0, 255.0);
        return bv;
    } else {
        return clamp(color + color * tvalue / 150.0, 0.0, 255.0);
    }
}

// 亮度调整
vec4 brightnessAdjust(vec4 color, float value) {

	vec3 brightnessColor = color.rgb * 255.0;
	brightnessColor.r = brightness(brightnessColor.r, value);
    brightnessColor.g = brightness(brightnessColor.g, value);
    brightnessColor.b = brightness(brightnessColor.b, value);

    brightnessColor /= 255.0;

   	return vec4(brightnessColor, color.a);
}

// 对比度调整
vec4 contrastAdjust(vec4 color, float value) {
    return vec4(((color.rgb - vec3(0.5)) * value + vec3(0.5)), color.w);
}

// 饱和度调整
vec4 saturationAdjust(vec4 color, float value) {

    float luminance = dot(color.rgb, luminanceWeighting);
    vec3 greyScaleColor = vec3(luminance);

   return vec4(mix(greyScaleColor, color.rgb, value), color.w);
}

// 色温调整
vec4 whiteBalanceAdjust(vec4 color, float value) {

    vec3 yiq = RGBtoYIQ * color.rgb;
    yiq.b = clamp(yiq.b, -0.5226, 0.5226);
    vec3 rgb = YIQtoRGB * yiq;

    vec3 processed = vec3(
    	(rgb.r < 0.5 ? (2.0 * rgb.r * warmFilter.r) : (1.0 - 2.0 * (1.0 - rgb.r) * (1.0 - warmFilter.r))),
    	(rgb.g < 0.5 ? (2.0 * rgb.g * warmFilter.g) : (1.0 - 2.0 * (1.0 - rgb.g) * (1.0 - warmFilter.g))),
    	(rgb.b < 0.5 ? (2.0 * rgb.b * warmFilter.b) : (1.0 - 2.0 * (1.0 - rgb.b) * (1.0 - warmFilter.b))));

    return vec4(mix(rgb, processed, value), color.a);
}

// 色调调整
vec4 colorBalanceAdjust(vec4 color, float value) {
    vec3 lightness = color.rgb;

    const float a = 0.25;
    const float b = 0.333;
    const float scale = 0.7;

    vec3 midtonesShift = vec3(0.0, value, 0.0);
    vec3 midtones = midtonesShift * (clamp((lightness - b) / a + 0.5, 0.0, 1.0) *
                                              clamp((lightness + b - 1.0) / -a + 0.5, 0.0, 1.0) * scale);

    vec3 newColor = color.rgb + midtones;
    newColor = clamp(newColor, 0.0, 1.0);

    return vec4(newColor, 1.0);
}

// 高光和阴影调整
vec4 highlightAdjust(vec4 color, float shadows, float highlights) {
   	float luminance = dot(color.rgb, luminanceWeighting2);

   	float shadow = clamp((pow(luminance, 1.0/(shadows+1.0)) + (-0.76)*pow(luminance, 2.0/(shadows+1.0))) - luminance, 0.0, 1.0);
   	float highlight = clamp((1.0 - (pow(1.0-luminance, 1.0/(2.0-highlights)) + (-0.8)*pow(1.0-luminance, 2.0/(2.0-highlights)))) - luminance, -1.0, 0.0);
   	vec3 result = vec3(0.0, 0.0, 0.0) + ((luminance + shadow + highlight) - 0.0) * ((color.rgb - vec3(0.0, 0.0, 0.0))/(luminance - 0.0));

   	return vec4(result.rgb, color.a);
}

void main() {
	vec4 textureColor = texture2D(sourceImage, vTextureCoord);

	textureColor = brightnessAdjust(textureColor, values[0]);
	textureColor = contrastAdjust(textureColor, values[1]);
	textureColor = saturationAdjust(textureColor, values[2]);
	textureColor = whiteBalanceAdjust(textureColor, values[3]);
	textureColor = colorBalanceAdjust(textureColor, values[4]);
	textureColor = highlightAdjust(textureColor, values[5], values[6]);

	gl_FragColor = textureColor;
}
