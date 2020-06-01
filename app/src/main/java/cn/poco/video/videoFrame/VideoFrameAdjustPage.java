package cn.poco.video.videoFrame;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.ShareData;
import cn.poco.video.frames.VideoPage;
import cn.poco.video.page.VideoModeWrapper;
import cn.poco.video.render.GLVideoView;
import cn.poco.video.render.IVideoPlayer;
import cn.poco.video.videoFeature.view.FrameNinePalacesView;
import cn.poco.video.view.ActionBar;

/**
 * Created by lgd on 2018/1/17.
 */

public class VideoFrameAdjustPage extends VideoPage
{
    private TextView mTip;
    private FrameNinePalacesView mNinePalacesView;
    private VideoModeWrapper mWrapper;
    private GLVideoView mVideoView;
    private ImageView mCounterclockwise;
    private ImageView mClockwise;
    private VideoFrameAdjustSite mSite;
    private ObjectAnimator alphaAnimator;
//    private VideoInfo mVideoInfo;
    public VideoFrameAdjustPage(Context context, BaseSite baseSite, VideoModeWrapper wrapper)
    {
        super(context, baseSite);

        MyBeautyStat.onPageStartByRes(R.string.视频画面页);

        mSite = (VideoFrameAdjustSite) baseSite;
        this.mWrapper = wrapper;
        mVideoView = mWrapper.mVideoView;
//        mVideoInfo = mWrapper.mCurrentBeautifiedVideo;
        mWrapper.mActionBar.setOnActionbarMenuItemClick(new ActionBar.onActionbarMenuItemClick(){
            @Override
            public void onItemClick(int id)
            {
                super.onItemClick(id);
                if(id == ActionBar.LEFT_MENU_ITEM_CLICK){
                    onBack();
                }else if(id == ActionBar.RIGHT_MENU_ITEM_CLICK){
                    mWrapper.isModify = true;
                    MyBeautyStat.onClickByRes(R.string.视频画面页_保存画面调整);
                    mSite.onBack(getContext());
                }
            }
        });
//        mVideoView.seekTo(0);
        mVideoView.addOnProgressListener(mOnProgressListener);
//        mWrapper.pauseAll();
        mVideoView.enterFrameAdjust();
        init();
    }

    private void init()
    {
        LayoutParams fl;
//        mTip = new TextView(getContext());
//        mTip.setTextColor(0xff666666);
//        mTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
//        mTip.setText(R.string.video_frame_adjust_tip);
//        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
//        fl.topMargin = ShareData.PxToDpi_xhdpi(74);
//        addView(mTip, fl);

        mCounterclockwise = new ImageView(getContext());
        mCounterclockwise.setImageResource(R.drawable.video_frame_adjust_counterclockwise);
        mCounterclockwise.setPadding(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(20), ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(20));
        mCounterclockwise.setOnClickListener(mOnClickListener);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        fl.leftMargin = ShareData.PxToDpi_xhdpi(160-40);
        addView(mCounterclockwise, fl);

        mClockwise = new ImageView(getContext());
        mClockwise.setImageResource(R.drawable.video_frame_adjust_clockwise);
        mClockwise.setPadding(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(20), ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(20));
        mClockwise.setOnClickListener(mOnClickListener);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.RIGHT);
        fl.rightMargin = ShareData.PxToDpi_xhdpi(160-40);
        addView(mClockwise, fl);


        mNinePalacesView = new FrameNinePalacesView(getContext());
        mNinePalacesView.setAlpha(0f);
        mNinePalacesView.setPadding(mVideoView.getSurfaceLeft(),mVideoView.getSurfaceTop(),mVideoView.getSurfaceLeft(),mVideoView.getSurfaceTop());
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fl.gravity = Gravity.CENTER_HORIZONTAL;

//        fl = new LayoutParams(mVideoView.getSurfaceWidth(), mVideoView.getSurfaceHeight());
//        fl.gravity = Gravity.CENTER_HORIZONTAL;
//        fl.topMargin = mVideoView.getSurfaceTop();

        mVideoView.addView(mNinePalacesView, fl);
        mNinePalacesView.setOnViewDragListener(new FrameNinePalacesView.OnViewListener()
        {
            @Override
            public void onDrag(float dx, float dy)
            {
//                if(mWrapper.isPlaying()){
                    mVideoView.translateFrame(dx, dy);
                alphaAnimator.cancel();
                mNinePalacesView.setAlpha(1f);
//                }
            }

            @Override
            public void onScaleChange(float scale, float focusX, float focusY)
            {
//                mVideoView.scaleFrame();
                mVideoView.scaleFrame(focusX,focusY,scale);
                alphaAnimator.cancel();
                mNinePalacesView.setAlpha(1f);
            }

            @Override
            public void onDoubleClick(float focusX, float focusY)
            {
                mVideoView.doubleScaleFrame(focusX,focusY);
                alphaAnimator.cancel();
                alphaAnimator.start();
            }

            @Override
            public void onClick()
            {
                mNinePalacesView.setAlpha(0f);
                if(mWrapper.isPlaying()){
                    mWrapper.pauseAll();
                }else{
                    mWrapper.resumeAll(false);
                }
            }

            @Override
            public void onUp()
            {
                mNinePalacesView.setAlpha(0f);
            }
        });

        alphaAnimator = ObjectAnimator.ofFloat(mNinePalacesView,View.ALPHA,0,1f,0f);
        alphaAnimator.setDuration(1000);
    }

    private ViewGroup.OnClickListener mOnClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (v == mClockwise)
            {
                MyBeautyStat.onClickByRes(R.string.视频画面页_顺时针翻转);
                alphaAnimator.cancel();
                alphaAnimator.start();
                mVideoView.rotateFrame(true);
            } else if (v == mCounterclockwise)
            {
                MyBeautyStat.onClickByRes(R.string.视频画面页_逆时针翻转);
                alphaAnimator.cancel();
                alphaAnimator.start();
                mVideoView.rotateFrame(false);
            }
        }
    };

    @Override
    public void onClose()
    {
        MyBeautyStat.onPageEndByRes(R.string.视频画面页);

        if (mVideoView != null)
        {
            mVideoView.removeOnProgressListener(mOnProgressListener);
            mVideoView.exitFrameAdjust();
        }
        mWrapper.resumeAll(false);
        if (mNinePalacesView.getParent() == mVideoView)
        {
            mVideoView.removeView(mNinePalacesView);
        }
    }

    @Override
    public void SetData(HashMap<String, Object> params)
    {

    }

    @Override
    public void onBack()
    {
        MyBeautyStat.onClickByRes(R.string.视频画面页_退出画面调整);

        mVideoView.resetFrameAdjust();
        mSite.onBack(getContext());
    }

//    private static final String TAG = "VideoFrameAdjustPage";
    private GLVideoView.OnProgressListener mOnProgressListener = new IVideoPlayer.OnProgressListener()
    {
        @Override
        public void onChanged(float progress, boolean isSeekTo)
        {
            if(progress == 1 ){
                mWrapper.seekTo(0);
                mWrapper.pauseAll();
            }
        }
    };
}
