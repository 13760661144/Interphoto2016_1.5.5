package cn.poco.capture2.model;

import cn.poco.video.render.filter.FilterItem;

/**
 * Created by: fwc
 * Date: 2017/12/29
 */
public class Snippet {

	/**
	 * 视频路径
	 */
	public String path;

	/**
	 * 视频时长占的比例
	 */
	public float ratio;

	/**
	 * 滤镜
	 */
	public FilterItem filterItem;

	/**
	 * 滤镜透明度
	 */
	public float alpha;

	/**
	 * 标志是否结束
	 */
	public boolean finish;
}
