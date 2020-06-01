precision mediump float;

varying mediump vec2 textureCoordinatePort;
varying mediump vec2 secondTextureCoordinatePort;

uniform lowp float alpha;

uniform sampler2D sourceImage;
uniform sampler2D secondSourceImage;

vec4 SoftLightBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity)
{
    if(sourceColor.a > 0.0){
        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);

        mediump float alphaDivisor = secondSourceColor.a + step(secondSourceColor.a, 0.0);

        mediump vec3 result = secondSourceColor.rgb * (sourceColor.a * (secondSourceColor.rgb / alphaDivisor) + (2.0 * sourceColor.rgb * (1.0 - (secondSourceColor.rgb / alphaDivisor)))) + sourceColor.rgb * (1.0 - secondSourceColor.a) + secondSourceColor.rgb * (1.0 - sourceColor.a);
        result = clamp(result, 0.0, 1.0);

        return vec4(mix(secondSourceColor.rgb, result, sourceColor.a * opacity), 1.0);
    }else
    {
    	return secondSourceColor;
    }
}


void main()
{
    lowp vec4 secondSourceColor = texture2D(sourceImage, textureCoordinatePort);
    lowp vec4 sourceColor = texture2D(secondSourceImage, secondTextureCoordinatePort);
    // r,g,b,a
    gl_FragColor = SoftLightBlend(secondSourceColor, sourceColor, alpha);
}
