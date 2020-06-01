precision mediump float;

varying mediump vec2 textureCoordinatePort;
varying mediump vec2 secondTextureCoordinatePort;

uniform lowp float alpha;

uniform sampler2D sourceImage;
uniform sampler2D secondSourceImage;

vec4 OverlayBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity)
{
    if(sourceColor.a > 0.0){
        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);

        mediump float ra;
        if(secondSourceColor.r <= 0.5){

            ra = 2.0 * sourceColor.r * secondSourceColor.r;
        }
        else
        {
            ra = 1.0 - 2.0 * (1.0 - sourceColor.r) * (1.0 - secondSourceColor.r);
        }
        mediump float ga;
        if(secondSourceColor.g <= 0.5){

            ga = 2.0 * sourceColor.g * secondSourceColor.g;
        }
        else
        {
            ga = 1.0 - 2.0 * (1.0 - sourceColor.g) * (1.0 - secondSourceColor.g);
        }
        mediump float ba;
        if(secondSourceColor.b <= 0.5){

            ba = 2.0 * sourceColor.b * secondSourceColor.b;
        }
        else
        {
            ba = 1.0 - 2.0 * (1.0 - sourceColor.b) * (1.0 - secondSourceColor.b);
        }

        mediump vec3 result = vec3(ra, ga, ba);
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
    gl_FragColor = OverlayBlend(secondSourceColor, sourceColor, alpha);
}
