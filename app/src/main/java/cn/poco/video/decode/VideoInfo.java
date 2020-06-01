package cn.poco.video.decode;

/**
 * Created by: fwc
 * Date: 2017/9/20
 */
public class VideoInfo {

	public String mimeType;
	public int width;
	public int height;
	public long duration;
	public int rotation;
	public int frameRate;
	public int bitRate;

	public VideoInfo Clone() {
		VideoInfo info = new VideoInfo();

		info.mimeType = mimeType;
		info.width = width;
		info.height = height;
		info.duration = duration;
		info.rotation = rotation;
		info.frameRate = frameRate;
		info.bitRate = bitRate;

		return info;
	}
}
