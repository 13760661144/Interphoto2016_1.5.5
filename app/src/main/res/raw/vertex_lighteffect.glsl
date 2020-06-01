attribute vec4 aPosition;

attribute vec4 aTextureCoord;
attribute vec4 bTextureCoord;
attribute vec4 cTextureCoord;

varying mediump vec2 textureCoordinatePort;
varying mediump vec2 secondTextureCoordinatePort;

void main () {
    gl_Position = aPosition;

    textureCoordinatePort = aTextureCoord.xy;
    secondTextureCoordinatePort = bTextureCoord.xy;
}