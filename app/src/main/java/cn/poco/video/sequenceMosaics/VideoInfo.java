package cn.poco.video.sequenceMosaics;

import java.io.File;
import java.util.ArrayList;

import cn.poco.beautify.BeautyAdjustType;
import cn.poco.video.VideoResMgr;
import cn.poco.video.page.ProcessMode;
import cn.poco.video.render.transition.TransitionItem;

/**
 * Created by admin on 2017/10/18.
 */

public class VideoInfo
{
	private static int SOLE_NUM = 10;
	public int mUri = 0;
	public String mPath;	//视频原始路径mParentPath的对应变速路径                 做速率，倒放，会改变这个值。  复制，分割继承这个值，裁剪不改变值
	public String mClipPath;	//裁剪过后视频路径
	public long mSelectStartTime;	//选中的开始时间       做完速率的时长
	public long mSelectEndTime;		//选中区域结束时间     做完速率的时长
	public long mDuration;		//视频总时长  mPath的时长，做完速率的时长
	public int mTransition = TransitionItem.NONE;

	public boolean mHasEdit = false;	//是否重新编辑过时间

	public int mRotation; //视频旋转角度

	public int mTimeType = VideoEditTimeId.TYPE_FREE;

	public int mFramesToLoad;

	public float mCurSpeed = 1.0f;
	public boolean mIsReverse = false;
	public boolean mIsSilenceWhileSpeed = false;
	public String mParentPath;        //最原始的父路径，速率，倒放，分割，复制，裁剪，等操作，此路径不变

	public boolean mIsFrameScale = false;  //画布是否缩放过
	public float mFrameTranX;      //画图移动的X
	public float mFrameTranY;      //画图移动的Y

	// 滤镜uri
    public int mFilterUri = -1;

    // 统一以12为最大
    public int mFilterAlpha = 1;

    // 滤镜统计id
//    public int mFilterTongJi = -1;


    public VideoResMgr.AdjustData mBrightness = new VideoResMgr.AdjustData(BeautyAdjustType.BRIGHTNESS, 0);
    public VideoResMgr.CurveData mCurveData = new VideoResMgr.CurveData();
	public VideoResMgr.AdjustData mContrast = new VideoResMgr.AdjustData(BeautyAdjustType.CONTRAST, 1.05f);
	public VideoResMgr.AdjustData mSaturation = new VideoResMgr.AdjustData(BeautyAdjustType.SATURABILITY, 1);
	public VideoResMgr.AdjustData mSharpen = new VideoResMgr.AdjustData(BeautyAdjustType.SHARPEN, 0);

	public VideoResMgr.AdjustData mColorTemperatur = new VideoResMgr.AdjustData(BeautyAdjustType.TEMPERATURE, 0);
	public VideoResMgr.AdjustData mColorBalance = new VideoResMgr.AdjustData(BeautyAdjustType.HUE, 0);
	public VideoResMgr.AdjustData mHighLight = new VideoResMgr.AdjustData(BeautyAdjustType.HIGHTLIGHT, 0);
	public VideoResMgr.AdjustData mShade = new VideoResMgr.AdjustData(BeautyAdjustType.SHADE, 0);
	public VideoResMgr.AdjustData mDrakCorner = new VideoResMgr.AdjustData(BeautyAdjustType.DARKCORNER, 0);





	/**
	 * 是否被裁剪过，如果裁剪路径和mPath不一样
	 * @return
	 */
	public boolean isHasClipped(){
		if(mClipPath.equals(mPath)){
			return false;
		}else{
			return true;
		}
	}

	public void setClipTime(long startClipTime, long endClipTime)
	{
		mSelectStartTime = startClipTime;
		mSelectEndTime = endClipTime;
	}

	public void deleteClipPath(String newPath)
	{
		if(!mClipPath.equals(mPath)){
			new File(mClipPath).delete();
		}
		mClipPath = newPath;
	}

	public long GetClipTime()
	{
		return mSelectEndTime - mSelectStartTime;
	}

	public void SetStartTime(long time)
	{
		mSelectStartTime = time;
	}

	public void SetEndTime(long time)
	{
		mSelectEndTime = time;
	}

	public long GetStartTime()
	{
		return mSelectStartTime;
	}

	public long GetEndTime()
	{
		return mSelectEndTime;
	}


	// 各功能统一从这里获取视频的时长，判断逻辑在函数里面实现
	public long getVideoTime(ProcessMode videoFeature) {
	    long videoTime = 0;
	    switch (videoFeature) {
			case CLIP: {
			    videoTime = mDuration;
			    break;
			}
			default: {
				videoTime = GetClipTime();
			}
		}
		return videoTime;
	}

	// 各功能模块统一从这里获取视频的路径，判断逻辑在函数里面实现
	public String getVideoPath(ProcessMode videoFeature) {
		String videoPath;
		switch (videoFeature) {
			case CLIP: {
			    videoPath = mPath;
				break;
			}
			default: {
			    videoPath = mClipPath;
			}
		}
		return videoPath;
	}

	public ArrayList<VideoResMgr.AdjustData> m_adjustData = new ArrayList<>();

	protected synchronized static int GetSoleId()
	{
		return ++SOLE_NUM;
	}

	public VideoInfo()
	{
		mUri = GetSoleId();
	}

	public VideoInfo(String videoPath, long duration, int filterUri, int filterAlpha) {
		mUri = GetSoleId();

		mHasEdit = true;
		mSelectStartTime = 0;
		mSelectEndTime = duration;
		mDuration = duration;
		mParentPath = mPath = mClipPath = videoPath;
		mFilterUri = filterUri;
		mFilterAlpha = filterAlpha;

	}

	/**
	 *
	 * @param info
	 * @param clipPath     裁剪区域的视频，如果裁剪过要复制，否则改变删除文件会导致复制的原视频错误
	 */
	public void Copy(VideoInfo info,String clipPath)
	{
		if(info != null)
		{
			mPath = info.mPath;
			mClipPath = clipPath;
			mSelectStartTime = info.mSelectStartTime;
			mSelectEndTime = info.mSelectEndTime;
			mTimeType = info.mTimeType;
			mDuration = info.mDuration;
			for(VideoResMgr.AdjustData data : info.m_adjustData)
			{
				VideoResMgr.AdjustData data1 = new VideoResMgr.AdjustData(data.m_type, data.m_value);
				data1.m_type1 = data.m_type1;
				m_adjustData.add(data1);
			}

			mCurSpeed = info.mCurSpeed;
			mIsReverse = info.mIsReverse;
			mIsSilenceWhileSpeed = info.mIsSilenceWhileSpeed;
			mIsFrameScale = info.mIsFrameScale;
			mFrameTranX = info.mFrameTranX;
			mFrameTranY = info.mFrameTranY;
			mParentPath = info.mParentPath;
			CloneFilterData(info);
		}
	}

	public void CloneFilterData(VideoInfo videoInfo) {
	    this.mFilterUri = videoInfo.mFilterUri;
	    this.mFilterAlpha = videoInfo.mFilterAlpha;
//	    this.mFilterTongJi = videoInfo.mFilterTongJi;

		VideoResMgr.AdjustData brightness = new VideoResMgr.AdjustData(BeautyAdjustType.BRIGHTNESS, videoInfo.mBrightness.m_value);
		VideoResMgr.AdjustData contrast = new VideoResMgr.AdjustData(BeautyAdjustType.CONTRAST, videoInfo.mContrast.m_value);
		VideoResMgr.AdjustData saturation = new VideoResMgr.AdjustData(BeautyAdjustType.SATURABILITY, videoInfo.mSaturation.m_value);
		VideoResMgr.AdjustData shapen = new VideoResMgr.AdjustData(BeautyAdjustType.SHARPEN, videoInfo.mSharpen.m_value);
		VideoResMgr.AdjustData ColorTemperatur = new VideoResMgr.AdjustData(BeautyAdjustType.TEMPERATURE, videoInfo.mColorTemperatur.m_value);
		VideoResMgr.AdjustData ColorBalance = new VideoResMgr.AdjustData(BeautyAdjustType.HUE, videoInfo.mColorBalance.m_value);
		VideoResMgr.AdjustData HighLight = new VideoResMgr.AdjustData(BeautyAdjustType.HIGHTLIGHT, videoInfo.mHighLight.m_value);
		VideoResMgr.AdjustData Shade = new VideoResMgr.AdjustData(BeautyAdjustType.SHADE, videoInfo.mShade.m_value);
		VideoResMgr.AdjustData DrakCorner = new VideoResMgr.AdjustData(BeautyAdjustType.DARKCORNER, videoInfo.mDrakCorner.m_value);

		VideoResMgr.CurveData CurveData = new VideoResMgr.CurveData(videoInfo.mCurveData.mRed, videoInfo.mCurveData.mGreen, videoInfo.mCurveData.mBlue, videoInfo.mCurveData.mRGB);
		this.mBrightness = brightness;
		this.mContrast = contrast;
		this.mSaturation = saturation;
		this.mSharpen = shapen;
		this.mColorTemperatur = ColorTemperatur;
		this.mColorBalance = ColorBalance;
		this.mHighLight = HighLight;
		this.mShade = Shade;
		this.mDrakCorner = DrakCorner;
		this.mCurveData = CurveData;

	}









}
