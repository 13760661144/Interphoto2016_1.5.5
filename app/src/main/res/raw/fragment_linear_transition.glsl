precision mediump float;

uniform sampler2D firstImage;
varying vec2 vTextureCoord;

uniform float progress;
uniform vec4 mask;

void main() {

    vec4 color = texture2D(firstImage, vTextureCoord);
    gl_FragColor = mix(color, mask, progress);
}
