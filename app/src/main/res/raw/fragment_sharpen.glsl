precision mediump float;
 
varying vec2 textureCoordinate;
varying vec2 leftTextureCoordinate;
varying vec2 rightTextureCoordinate;
varying vec2 topTextureCoordinate;
varying vec2 bottomTextureCoordinate;
 
varying float centerMultiplier;
varying float edgeMultiplier;

uniform sampler2D sourceImage;
 
void main() {
    mediump vec3 textureColor = texture2D(sourceImage, textureCoordinate).rgb;
    mediump vec3 leftTextureColor = texture2D(sourceImage, leftTextureCoordinate).rgb;
    mediump vec3 rightTextureColor = texture2D(sourceImage, rightTextureCoordinate).rgb;
    mediump vec3 topTextureColor = texture2D(sourceImage, topTextureCoordinate).rgb;
    mediump vec3 bottomTextureColor = texture2D(sourceImage, bottomTextureCoordinate).rgb;

    gl_FragColor = vec4((textureColor * centerMultiplier - (leftTextureColor * edgeMultiplier + rightTextureColor * edgeMultiplier + topTextureColor * edgeMultiplier + bottomTextureColor * edgeMultiplier)), texture2D(sourceImage, bottomTextureCoordinate).w);
}