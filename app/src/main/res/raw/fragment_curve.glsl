precision mediump float;

uniform sampler2D sourceImage;
uniform sampler2D curveImage;

varying vec2 vTextureCoord;

void main() {

	vec4 source = texture2D(sourceImage, vTextureCoord);

	float red = texture2D(curveImage, vec2(source.r, 0.0)).r;
	float green = texture2D(curveImage, vec2(source.g, 0.0)).g;
	float blue = texture2D(curveImage, vec2(source.b, 0.0)).b;

	gl_FragColor = vec4(red, green, blue, source.a);
}
