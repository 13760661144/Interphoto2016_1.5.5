precision mediump float;

uniform sampler2D firstImage;
uniform sampler2D secondImage;

varying vec2 vTextureCoord;

uniform float alpha;

void main() {
    vec4 color1 = texture2D(firstImage, vTextureCoord);
    vec4 color2 = texture2D(secondImage, vTextureCoord);

    gl_FragColor = mix(color1, color2, alpha);
}
