attribute vec4 aPosition;
attribute vec4 aTextureCoord;

uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;
uniform float imageWidthFactor;
uniform float imageHeightFactor;
uniform float values[1];

varying vec2 textureCoordinate;
varying vec2 leftTextureCoordinate;
varying vec2 rightTextureCoordinate;
varying vec2 topTextureCoordinate;
varying vec2 bottomTextureCoordinate;

varying float centerMultiplier;
varying float edgeMultiplier;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    
    vec2 widthStep = vec2(imageWidthFactor, 0.0);
    vec2 heightStep = vec2(0.0, imageHeightFactor);

    vec2 textureCoord = (uTexMatrix * aTextureCoord).xy;
    textureCoordinate = textureCoord;
    leftTextureCoordinate = textureCoord - widthStep;
    rightTextureCoordinate = textureCoord + widthStep;
    topTextureCoordinate = textureCoord + heightStep;
    bottomTextureCoordinate = textureCoord - heightStep;
    
    centerMultiplier = 1.0 + 4.0 * values[0];
    edgeMultiplier = values[0];
}
