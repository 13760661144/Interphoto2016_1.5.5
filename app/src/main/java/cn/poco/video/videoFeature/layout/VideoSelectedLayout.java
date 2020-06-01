package cn.poco.video.videoFeature.layout;

import android.content.Context;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.poco.tianutils.ShareData;
import cn.poco.video.helper.TimeFormatter;
import cn.poco.video.page.ProcessMode;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.videoFeature.view.ClipTimeLineView;
import cn.poco.video.videoFeature.view.ProcessView;
import cn.poco.video.videoFeature.view.TimeBar;
import cn.poco.video.videoFeature.view.VideoSeekBar;
import cn.poco.video.videoFeature.view.VideoSelectView;

/**
 * Created by Simon Meng on 2018/1/23.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoSelectedLayout extends FrameLayout{
    public interface VideoSelectLayoutCallback {
        void onLeftProgress(float leftProgress, float rightProgress);
        void onRightProgress(float leftProgress, float rightProgress);
        void onConfirmProgress(float leftProgress,float rightProgress);
        boolean canMoveLeft();
        boolean canMoveRight();

        void onSeekbarProgressChange(float progress);
        void onSeekbarDown(float progress);
        void onSeekbarUp(float progress);
        void onProcessChange(float process);
        void onProcessDown(float process);
        void onProcessUp(float process);
    }


    private Context mContext;
//    private TextView mIntroduceText; //这page有动画，放去外层
    public TextView mCurrentTime;
    public ClipTimeLineView mClipTimeLineView;
    private TimeBar mTimeBar;
    protected int mEdgePadding = ShareData.PxToDpi_xhdpi(20);
    protected int mCenterBpH = ShareData.PxToDpi_xhdpi(162);
    private VideoSelectLayoutCallback mCallback;


    public VideoSelectedLayout(Context context) {
        super(context);
//        this.setOrientation(VERTICAL);
//        this.setGravity(Gravity.CENTER);
        mContext = context;
        initView();
    }

    private void initView() {
        LayoutParams params;
        // 顶部文案
//        mIntroduceText = new TextView(mContext);
//        mIntroduceText.setText("顶部文案");
//        mIntroduceText.setBackgroundColor(0x4fff0000);
//        mIntroduceText.setVisibility(View.INVISIBLE);
//        mIntroduceText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
//        mIntroduceText.setTextColor(0xff666666);
//        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.CENTER);
//        params.bottomMargin = mCenterBpH/2 + ShareData.PxToDpi_xhdpi(86);
//        mIntroduceText.setVisibility(View.VISIBLE);
//        mIntroduceText.setLayoutParams(params);
//        this.addView(mIntroduceText);

        mCurrentTime = new TextView(mContext);
        mCurrentTime.setText("10S");
        mCurrentTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.CENTER);
        params.bottomMargin = mCenterBpH/2 +ShareData.PxToDpi_xhdpi(40/2 + 20);//分割图标上面占的地方和自身高度
//        params.topMargin = ShareData.PxToDpi_xhdpi(5);
        mCurrentTime.setLayoutParams(params);
        mCurrentTime.setVisibility(View.INVISIBLE);
        this.addView(mCurrentTime);


        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.CENTER);
        params.bottomMargin = ShareData.PxToDpi_xhdpi(40/2);    //分割图标上面占的地方
        addView(linearLayout,params);
        {
            LinearLayout.LayoutParams ll;

            mClipTimeLineView = new ClipTimeLineView(mContext);
//            params = new LayoutParams(ShareData.m_screenWidth - mEdgePadding * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
//            params.gravity = Gravity.CENTER;
            ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth - mEdgePadding * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
            mClipTimeLineView.setLayoutParams(ll);
            linearLayout.addView(mClipTimeLineView);
            mClipTimeLineView.setHandleDragVisibility(View.INVISIBLE);
            mClipTimeLineView.setDragSeekbarVisibility(View.INVISIBLE);
            mClipTimeLineView.setProgressLineVisibility(View.VISIBLE);
            mClipTimeLineView.setTimeLineProgressCallback(new VideoSelectView.ProgressChangeListener()
            {
                @Override
                public void onLeftProgressChange(float leftProgress, float rightProgress)
                {
                    System.out.println("leftProgress: " + leftProgress);
                    if (mShowTimeDuration)
                    {
                        resetTimeTextView();
                    }
                    if (mCallback != null)
                    {
                        mCallback.onLeftProgress(leftProgress, rightProgress);
                    }
                }

                @Override
                public void onRightProgressChange(float leftProgress, float rightProgress)
                {
                    if (mShowTimeDuration)
                    {
                        resetTimeTextView();
                    }

                    if (mCallback != null)
                    {
                        mCallback.onRightProgress(leftProgress, rightProgress);
                    }
                }


                @Override
                public void onConfirmProgress()
                {
                    if (mCallback != null)
                    {
                        mCallback.onConfirmProgress(mClipTimeLineView.getLeftProgress(), mClipTimeLineView.getRightProgress());
                    }

                }

                @Override
                public void onSizeChange()
                {
                    if (mShowTimeDuration)
                    {
                        resetTimeTextView();
                    }
                }

                @Override
                public boolean canMoveLeft()
                {
                    if (mCallback != null)
                    {
                        return mCallback.canMoveLeft();
                    }
                    return false;
                }

                @Override
                public boolean canMoveRight()
                {
                    if (mCallback != null)
                    {
                        return mCallback.canMoveRight();
                    }
                    return false;
                }
            });
            mClipTimeLineView.setProcessListener(new ProcessView.ProcessLister()
            {
                @Override
                public void onProgress(float process)
                {
                    if (mCallback != null)
                    {
                        mCallback.onProcessChange(process);
                    }
                }

                @Override
                public void onDown(float process)
                {
                    if (mCallback != null)
                    {
                        mCallback.onProcessDown(process);
                    }
                }

                @Override
                public void onUp(float process)
                {
                    if (mCallback != null)
                    {
                        mCallback.onProcessUp(process);
                    }
                }
            });
            mClipTimeLineView.setOnSeekBarProgressListener(new VideoSeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(VideoSeekBar seekBar, float progress, boolean fromUser)
                {
                    if (mCallback != null)
                    {
                        mCallback.onSeekbarProgressChange(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(VideoSeekBar seekBar)
                {
                    if (mCallback != null)
                    {
                        mCallback.onSeekbarDown(seekBar.getProgress());
                    }
                }

                @Override
                public void onStopTrackingTouch(VideoSeekBar seekBar)
                {
                    if (mCallback != null)
                    {
                        mCallback.onSeekbarUp(seekBar.getProgress());
                    }
                }
            });

            mTimeBar = new TimeBar(mContext);
            mTimeBar = new TimeBar(mContext);
//            params = new LayoutParams(ShareData.m_screenWidth - mEdgePadding * 2 - ShareData.PxToDpi_xhdpi(40) * 2, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
//            params.topMargin = mCenterBpH / 2 + ShareData.PxToDpi_xhdpi(20);
            ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth - mEdgePadding * 2 - ShareData.PxToDpi_xhdpi(40) * 2, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            mTimeBar.setLayoutParams(ll);
            linearLayout.addView(mTimeBar);
        }
    }

    private VideoInfo mCurInfo;
    public void setVideoInfo(VideoInfo videoInfo, ProcessMode videoFeature) {
        mCurInfo = videoInfo;
        List<VideoInfo> videoList = new ArrayList<>();
        videoList.add(videoInfo);
        mClipTimeLineView.setVideoList(videoList);
        mTimeBar.setDuration((int)mCurInfo.getVideoTime(videoFeature));
    }

    public void refreshBitmapList(boolean refreshFrame, VideoInfo videoInfo, ProcessMode videoFeature) {
        List<VideoInfo> videoList = new ArrayList<>();
        videoList.add(videoInfo);
        mClipTimeLineView.refreshBitmapList(refreshFrame, videoList, videoFeature);
        mTimeBar.setDuration((int)videoInfo.getVideoTime(videoFeature));
    }


    public void setVideoSelectedCallback(VideoSelectLayoutCallback callback) {
        this.mCallback = callback;
    }

//    private boolean mShowIntroduceText;
    private boolean mShowTimeDuration;
    private boolean mShowDragHandle;
    private boolean mShowTimeInterval;
    private boolean mShowDragSeekbar;
    private boolean mShowProcessLine;

    private ProcessMode mActiveFeature;
    public void setFeature(ProcessMode feature) {
        mClipTimeLineView.setProcessClickable(true);
        mActiveFeature = feature;
        switch (feature) {
            case CLIP: {
                mClipTimeLineView.setProcessClickable(false);
//                mShowIntroduceText = false;
                mShowTimeDuration = true;
                mShowDragHandle = true;
                mShowTimeInterval = true;
                mShowProcessLine = true;
                mShowDragSeekbar = false;
                mClipTimeLineView.setProgressLineRangeProgress();
                if(mCallback != null){
                    mCallback.onConfirmProgress(mClipTimeLineView.getLeftProgress(),mClipTimeLineView.getRightProgress());
                }
                break;
            }
            case CANVASADJUST: {
//                mShowIntroduceText = true;
                mShowTimeDuration = false;
                mShowDragHandle = false;
                mShowTimeInterval = false;
                mShowProcessLine = true;
                mShowDragSeekbar = false;
//                mIntroduceText.setText(mContext.getResources().getString(R.string.video_frame_adjust_tip));
                break;
            }

            case FILTER: {

                break;
            }

            case SPEEDRATE: {


                break;
            }

            case SEGENTATION: {
//                mShowIntroduceText = true;
                mShowTimeDuration = false;
                mShowDragHandle = false;

                mShowTimeInterval = true;
                mShowProcessLine = false;
                mShowDragSeekbar = true;

//                mIntroduceText.setText(mContext.getResources().getString(R.string.video_segmentation_introduce));

                break;
            }

            case COPY:
            case DELETE:
            case Edit: {
//                mShowIntroduceText = false;
                mShowTimeDuration = false;
                mShowDragHandle = false;
                mShowTimeInterval = true;
                mShowDragSeekbar = false;
                mShowProcessLine = true;
                mClipTimeLineView.setProgressLineRangeProgress(0,1);
                break;
            }

            default: {

            }
        }
        setLayoutStyle();
    }


    private void setLayoutStyle() {
//        if (mShowIntroduceText) {
//            mIntroduceText.setVisibility(View.VISIBLE);
//        } else {
//            mIntroduceText.setVisibility(View.INVISIBLE);
//        }

        if (mShowTimeDuration) {
            mCurrentTime.setVisibility(View.VISIBLE);
            resetTimeTextView();
            //裁剪完再进去，这时间不是裁剪的时间
//            mCurrentTime.setText(TimeFormatter.makeVideoTimeDurationText(mCurInfo.getVideoTime(mActiveFeature)));
            mCurrentTime.setText(TimeFormatter.makeVideoTimeDurationText(mCurInfo.GetClipTime()));
        } else {
            mCurrentTime.setVisibility(View.INVISIBLE);
        }

        if (mShowTimeInterval) {
            mTimeBar.setVisibility(View.VISIBLE);
        } else {
            mTimeBar.setVisibility(View.INVISIBLE);
        }


        if (mShowDragHandle) {
            mClipTimeLineView.setHandleDragVisibility(View.VISIBLE);
        } else {
            mClipTimeLineView.setHandleDragVisibility(View.INVISIBLE);
        }

        if (mShowDragSeekbar) {
            mClipTimeLineView.setDragSeekbarVisibility(View.VISIBLE);
        } else {
            mClipTimeLineView.setDragSeekbarVisibility(View.INVISIBLE);
        }

        if (mShowProcessLine) {
           mClipTimeLineView.setProgressLineVisibility(View.VISIBLE);
        } else {
            mClipTimeLineView.setProgressLineVisibility(View.INVISIBLE);
        }
    }

    private void resetTimeTextView() {
        RectF rectF = mClipTimeLineView.getSelectRectF();
        int centerX = (int)((rectF.width() - mCurrentTime.getWidth()) / 2 + rectF.left) + ShareData.PxToDpi_xhdpi(20);
        int originX = (ShareData.m_screenWidth - mCurrentTime.getWidth()) / 2;
        int leftMargin = centerX - originX;
        LayoutParams layoutParams = (LayoutParams) mCurrentTime.getLayoutParams();
        layoutParams.leftMargin = leftMargin;
        if (mCurrentTime.getVisibility() != View.VISIBLE) {
            mCurrentTime.setVisibility(View.VISIBLE);
        }
        mCurrentTime.requestLayout();
    }

    public float getLeftProgress() {
        return mClipTimeLineView.getLeftProgress();
    }

    public float getRightProgress() {
        return mClipTimeLineView.getRightProgress();
    }

    public void setProgress(float process) {
        if(mClipTimeLineView != null){
            mClipTimeLineView.setProgress(process);
        }
    }

//    public void setProgressRangeTime(int start, int end) {
//        if (mClipTimeLineView != null) {
//            mClipTimeLineView.setProgressLineRangeProgress(start ,end);
//        }
//    }


    public void clear() {
        mClipTimeLineView.clear();
    }

    public void setMinClipTime(long duration)
    {
        mClipTimeLineView.setMinClipTime(duration);
    }
}
