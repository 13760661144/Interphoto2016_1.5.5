package cn.poco.video.render;

import android.graphics.Point;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;

import java.util.List;

import cn.poco.graphics.ShapeEx;
import cn.poco.resource.FilterRes;
import cn.poco.video.render.adjust.AdjustItem;
import cn.poco.video.save.SaveParams;
import cn.poco.video.videotext.text.VideoText;

/**
 * Created by: fwc
 * Date: 2017/12/13
 */
public interface IVideoPlayer {

	void setVideoPaths(String... paths);

	void addVideos(String... paths);

	void deleteVideo(int index);

	void copyVideo(int index, String path);

	void setPlayRatio(int playRatio);

	void prepare();

	void start();

	void enterSingleVideoPlay(int index);

	void exitSingleVideoPlay();

	void restart();

	void pause();

	boolean isPlaying();

	void seekTo(long position);

	void seekTo(int index, long position);

	void forceSeekTo(int index, long position);

	void onResume();

	void onPause();

	void reset();

	void release();

	void setLooping(boolean looping);

	void setAutoPlay(boolean isAutoPlay);

	void setVolume(float volume);

	float getVolume();

	void openVolume();

	void closeVolume();

	void setTransition(int index, int transitionId);

	void setTransitions(int... transitionIds);

	void exitTransition();

	long checkTransition();

	void changeVideoOrder(int fromPosition, int toPosition,  int index, float progress);

	long getTotalDuration();

	long getBlendTime(int index);

	long getCurrentPosition();

	void changeVideoPath(String videoPath);

	void splitVideoPath(String videoPath1, String videoPath2);

	/**
	 * 设置全局滤镜
	 *
	 * @param filterRes 滤镜资源对象
	 */
	void changeFilter(FilterRes filterRes);

	/**
	 * 设置全局滤镜的透明度
	 *
	 * @param alpha 透明度，0~1
	 */
	void changeFilterAlpha(float alpha);

	/**
	 * 设置局部滤镜
	 *
	 * @param index     视频下标
	 * @param filterRes 滤镜资源对象
	 */
	void changeFilter(int index, FilterRes filterRes);

	/**
	 * 设置局部滤镜的透明度
	 *
	 * @param index 视频下标
	 * @param alpha 透明度，0~1
	 */
	void changeFilterAlpha(int index, float alpha);

	/**
	 * 整体做曲线效果
	 *
	 * @param curveType     曲线类型：CURVE_RGB、CURVE_R、CURVE_G、CURVE_B
	 * @param controlPoints 控制点，包括起点（0,0）和终点（255,255），坐标范围0~255
	 *                      设置为null，表示还原
	 */
	void doCurve(int curveType, List<Point> controlPoints);

	/**
	 * 局部做曲线效果
	 *
	 * @param index         视频下标
	 * @param curveType     曲线类型：CURVE_RGB、CURVE_R、CURVE_G、CURVE_B
	 * @param controlPoints 控制点，包括起点（0,0）和终点（255,255），坐标范围0~255
	 *                      设置为null，表示还原
	 */
	void doCurve(int index, int curveType, List<Point> controlPoints);

	/**
	 * 添加全局视频调整
	 *
	 * @param item 视频调整对象
	 */
	void addAdjust(AdjustItem item);

	/**
	 * 添加局部视频调整
	 *
	 * @param index 视频下标
	 * @param item  视频调整对象
	 */
	void addAdjust(int index, AdjustItem item);

	void setVideoText(int textId, VideoText videoText, ShapeEx shapeEx, int startTime, int stayTime);

	@Nullable
	public VideoText getVideoText();

	public SaveParams getOutputParams();

	void setMusicPath(int musicId, String musicPath, int musicStart, @FloatRange(from = 0, to = 1) float musicVolume);

	void addOnPlayListener(OnPlayListener listener);

	void removeOnPlayListener(OnPlayListener listener);

	void addOnProgressListener(OnProgressListener listener);

	void removeOnProgressListener(OnProgressListener listener);

	void addOnItemProgressListener(OnItemProgressListener listener);

	void removeOnItemProgressListener(OnItemProgressListener listener);

	interface OnPlayListener {

		void onStart();

		void onResume();

		void onPause();
	}

	interface OnProgressListener {

		/**
		 * 进度回调
		 *
		 * @param progress 范围：0~1
		 * @param isSeekTo true: seekTo导致的进度变化
		 */
		void onChanged(float progress, boolean isSeekTo);
	}

	interface OnItemProgressListener {

		/**
		 * 每个Item的进度回调
		 *
		 * @param index Item的下标
		 * @param progress 范围：0~1
		 */
		void onItemChanged(int index, float progress, boolean isSeekTo);
	}
}
