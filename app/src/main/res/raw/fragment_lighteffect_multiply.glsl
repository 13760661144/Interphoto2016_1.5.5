precision mediump float;

varying mediump vec2 textureCoordinatePort;
varying mediump vec2 secondTextureCoordinatePort;

uniform lowp float alpha;

uniform sampler2D sourceImage;
uniform sampler2D secondSourceImage;

vec4 MultiplyBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity)
{
    if(sourceColor.a > 0.0){
        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);

        mediump vec3 result = sourceColor.rgb * secondSourceColor.rgb;
        result = clamp(result, 0.0, 1.0);

        return vec4(mix(secondSourceColor.rgb, result, sourceColor.a * opacity), 1.0);
    }else{
    	return secondSourceColor;
    }

}


void main()
{
    lowp vec4 secondSourceColor = texture2D(sourceImage, textureCoordinatePort);
    lowp vec4 sourceColor = texture2D(secondSourceImage, secondTextureCoordinatePort);
    // r,g,b,a
    gl_FragColor = MultiplyBlend(secondSourceColor, sourceColor, alpha);
}
