package cn.poco.video.save;

import android.util.SparseArray;

import java.util.List;

import cn.poco.graphics.ShapeEx;
import cn.poco.video.render.PlayVideoInfo;
import cn.poco.video.render.draw.PlayRenderer;
import cn.poco.video.render.filter.AbstractFilter;
import cn.poco.video.videotext.text.VideoText;

/**
 * Created by: fwc
 * Date: 2017/6/15
 */
public class SaveParams {

	/**
	 * 视频列表
	 */
	public List<PlayVideoInfo> videoInfos;

	/**
	 * 转场动画id
	 */
	public int[] transitions;

	/**
	 * 滤镜
	 */
	public SparseArray<AbstractFilter.Params> filterParamArray;

	/**
	 * 调整
	 */
	public SparseArray<PlayRenderer.AdjustInfo> adjustInfoArray;

	/**
	 * 曲线
	 */
	public SparseArray<byte[]> curveArray;

	/**
	 * 文字
	 */
	public String textId = "0000";
	public VideoText videoText;
	public ShapeEx shapeEx;
	public int startTime;
	public int stayTime;

	/**
	 * logo
	 */
	public int videoLogo;
	public String logoPath;

	/**
	 * 音乐
	 */
	public String musicId = "0000";
	public String musicPath;
	public int musicStart;
	public float musicVolume = 0.6f;
	public float videoVolume = 1;

	/**
	 * 比例
	 */
	public int playRatio;

	public long duration;

	/**
	 * 保存路径
	 */
	public String outputPath;
}
