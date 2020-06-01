precision mediump float;

varying mediump vec2 textureCoordinatePort;
varying mediump vec2 secondTextureCoordinatePort;

uniform lowp float alpha;

uniform sampler2D sourceImage;
uniform sampler2D secondSourceImage;

vec4 VividLightBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity)
{
    if(sourceColor.a > 0.0){
        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);

        mediump float ra;
        if(sourceColor.r > 0.5){

            ra = secondSourceColor.r / (2.0 * (1.0 - sourceColor.r));
        }
        else
        {
            ra = 1.0 - 0.5 * (1.0 - secondSourceColor.r) / sourceColor.r;
        }
        mediump float ga;
        if(sourceColor.g > 0.5){

            ga = secondSourceColor.g / (2.0 * (1.0 - sourceColor.g));
        }
        else
        {
            ga = 1.0 - 0.5 * (1.0 - secondSourceColor.g) / sourceColor.g;
        }
        mediump float ba;
        if(sourceColor.b > 0.5){

            ba = secondSourceColor.b / (2.0 * (1.0 - sourceColor.b));
        }
        else
        {
            ba = 1.0 - 0.5 * (1.0 - secondSourceColor.b) / sourceColor.b;
        }

        mediump vec3 result = vec3(ra, ga, ba);
        result = clamp(result, 0.0, 1.0);

        return vec4(mix(secondSourceColor.rgb, result, sourceColor.a * opacity), 1.0);
    }
    else{
    	return secondSourceColor;
    }
}


void main()
{
    lowp vec4 secondSourceColor = texture2D(sourceImage, textureCoordinatePort);
    lowp vec4 sourceColor = texture2D(secondSourceImage, secondTextureCoordinatePort);
    // r,g,b,a
    gl_FragColor = VividLightBlend(secondSourceColor, sourceColor, alpha);
}
