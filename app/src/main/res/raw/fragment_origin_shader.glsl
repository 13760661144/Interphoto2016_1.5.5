precision highp float;

uniform sampler2D sourceImage;
varying vec2 vTextureCoord;

void main() {

    gl_FragColor = texture2D(sourceImage, vTextureCoord);
}