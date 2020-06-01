package my.Gif;

public class GifFrame {
	public byte[] srcBytes = null;
	public byte[] bytes = null;
	public int time = 50;
	public int width = 0;
	public int height = 0;

	public GifFrame() {
	}

	public GifFrame(byte[] bytes, int time) {
		this.bytes = bytes;
		this.time = time;
	}
}
