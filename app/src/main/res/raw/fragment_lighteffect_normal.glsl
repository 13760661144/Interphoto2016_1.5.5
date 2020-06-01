precision mediump float;

varying mediump vec2 textureCoordinatePort;
varying mediump vec2 secondTextureCoordinatePort;

uniform lowp float alpha;

uniform sampler2D sourceImage;
uniform sampler2D secondSourceImage;

vec4 NormalBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity)
{
    if(sourceColor.a > 0.0){

        mediump vec4 result;

        result.rgb = sourceColor.rgb + secondSourceColor.rgb * (1.0 - sourceColor.a);

        result.rgb = clamp(result.rgb, 0.0, 1.0);

        result.a = max(sourceColor.a, secondSourceColor.a);

        return mix(secondSourceColor, result, opacity);
    }else{
 		return secondSourceColor;
    }
    
}


void main()
{
    lowp vec4 secondSourceColor = texture2D(sourceImage, textureCoordinatePort);
    lowp vec4 sourceColor = texture2D(secondSourceImage, secondTextureCoordinatePort);
    // r,g,b,a
    gl_FragColor = NormalBlend(secondSourceColor, sourceColor, alpha);
}
