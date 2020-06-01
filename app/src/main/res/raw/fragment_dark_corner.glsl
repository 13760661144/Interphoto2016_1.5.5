precision mediump float;
 
uniform sampler2D sourceImage;
uniform sampler2D darkenCornerImage;
uniform float values[1];

varying vec2 vTextureCoord;
 
vec4 MultiplyBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity) {
    if (sourceColor.a > 0.0) {
        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);
        
        vec3 result = sourceColor.rgb * secondSourceColor.rgb;
        result = clamp(result, 0.0, 1.0);
        
        return vec4(mix(secondSourceColor.rgb, result, sourceColor.a * opacity), 1.0);
    } else {
    	// 针对小米 Android4.4手机，if 和 else 要一起
    	return secondSourceColor;
    }
}
 
void main() {
    vec4 textureColor= texture2D(sourceImage, vTextureCoord);
    vec4 darkenColor = texture2D(darkenCornerImage, vTextureCoord);
    gl_FragColor = MultiplyBlend(textureColor, darkenColor, values[0]);
}