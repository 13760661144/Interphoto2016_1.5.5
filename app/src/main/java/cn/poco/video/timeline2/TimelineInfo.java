package cn.poco.video.timeline2;

import java.util.List;

/**
 * Created by: fwc
 * Date: 2017/11/16
 */
public class TimelineInfo {

	public String videoPath;
	public long duration;
	public int frameCount;

	public List<String> frameList;

	public float startProgress = 0;
	public float endProgress = 1;
}
