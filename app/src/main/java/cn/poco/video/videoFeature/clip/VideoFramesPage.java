package cn.poco.video.videoFeature.clip;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import cn.poco.framework.BaseSite;
import cn.poco.tianutils.ShareData;
import cn.poco.video.frames.VideoPage;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.videoFeature.view.ClipTimeLineView;
import cn.poco.video.videoFeature.view.TimeBar;
import cn.poco.video.videoFeature.view.VideoSeekBar;

/**
 * Created by Simon Meng on 2018/1/15.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public abstract class VideoFramesPage extends VideoPage{
    protected ClipTimeLineView mClipTimeLineView;
    protected TimeBar mTimeBar;
    protected TextView mCurrentTime, mIntroduceText;

    private Context mContext;

    protected int mEdgePadding = ShareData.PxToDpi_xhdpi(20);

    public VideoFramesPage(Context context, BaseSite baseSite) {
        super(context, baseSite);
        this.setBackgroundColor(Color.BLACK);
        this.setClickable(true);
        mContext = context;
        initData();
        initView();
    }

    @Override
    public void SetData(HashMap<String, Object> params) {

    }

    private boolean mShowIntroduceText;
    private boolean mShowTimeDuration;
    private boolean mShowDragHandle;
    private boolean mShowTimeInterval;
    private boolean mShowDragSeekbar;
    private boolean mShowProcessLine;
    private void initData() {
        mShowIntroduceText = showIntroduceText();
        mShowTimeDuration = showVideoDuration();
        mShowDragHandle = showDragHandle();
        mShowTimeInterval = showTimeIntervalText();
        mShowDragSeekbar = showDragSeekbar();
        mShowProcessLine = showProcessLine();
    }

    private void initView() {
        FrameLayout.LayoutParams params;
        mIntroduceText = new TextView(mContext);
        mIntroduceText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        params.topMargin = ShareData.PxToDpi_xhdpi(50);
        mIntroduceText.setLayoutParams(params);
        this.addView(mIntroduceText);

        mCurrentTime = new TextView(mContext);
        mCurrentTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = ShareData.PxToDpi_xhdpi(121);
        mCurrentTime.setLayoutParams(params);
        mCurrentTime.setVisibility(View.INVISIBLE);
        this.addView(mCurrentTime);

        mClipTimeLineView = new ClipTimeLineView(mContext);
        params = new FrameLayout.LayoutParams(ShareData.m_screenWidth - mEdgePadding * 2, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mClipTimeLineView.setLayoutParams(params);
        this.addView(mClipTimeLineView);
        mClipTimeLineView.setHandleDragVisibility(mShowDragHandle == true ? View.VISIBLE : View.GONE);
        mClipTimeLineView.setDragSeekbarVisibility(mShowDragSeekbar == true ? View.VISIBLE : View.GONE);
        mClipTimeLineView.setProgressLineVisibility(mShowProcessLine == true ? View.VISIBLE : View.GONE);

        mTimeBar = new TimeBar(mContext);
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        params.topMargin = ShareData.PxToDpi_xhdpi(340);
        mTimeBar.setLayoutParams(params);

    }

    protected void setVideoInfoList(List<VideoInfo> videoInfoList) {
        mClipTimeLineView.setVideoList(videoInfoList);
    }

    private void resetTimeTextView() {
        RectF rectF = mClipTimeLineView.getSelectRectF();
        int leftMargin = (int)((rectF.width() - mCurrentTime.getWidth()) / 2 + rectF.left) + ShareData.PxToDpi_xhdpi(20);
        FrameLayout.LayoutParams layoutParams = (LayoutParams) mCurrentTime.getLayoutParams();
        layoutParams.leftMargin = leftMargin;
        if (mCurrentTime.getVisibility() != View.VISIBLE) {
            mCurrentTime.setVisibility(View.VISIBLE);
        }
        mCurrentTime.requestLayout();
    }


    public void clear() {
        mClipTimeLineView.clear();
    }


    protected void onLeftProgress(float leftProgress) {

    };

    protected void onRightProgress(float rightProgress) {

    };

    protected void onProgressDragEnd() {

    };




    protected boolean showIntroduceText() {
        return false;
    }

    protected boolean showVideoDuration() {
        return false;
    }

    protected boolean showDragHandle() {
        return false;
    }

    protected boolean showTimeIntervalText() {
        return false;
    }

    protected boolean showDragSeekbar() {
        return false;
    }

    protected boolean showProcessLine() {
        return false;
    }



    protected void onProcessChange(float process)
    {

    }

    protected void onSeekbarProgressChange(VideoSeekBar seekBar, int progress, boolean fromUser) {

    }


    @Override
    public void onBack() {
        mClipTimeLineView.clear();
    }
}
