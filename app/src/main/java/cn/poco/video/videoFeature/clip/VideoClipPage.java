package cn.poco.video.videoFeature.clip;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.album2.utils.T;
import cn.poco.beautify.WaitDialog1;
import cn.poco.framework.BaseSite;
import cn.poco.interphoto2.R;
import cn.poco.video.helper.TimeFormatter;
import cn.poco.video.page.VideoModeWrapper;
import cn.poco.video.process.ClipVideoTask;
import cn.poco.video.process.OnProcessListener;
import cn.poco.video.process.ThreadPool;
import cn.poco.video.render.GLVideoView;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.view.ActionBar;

/**
 * Created by Simon Meng on 2018/1/16.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoClipPage extends VideoFramesPage{
    private Context mContext;
    private VideoModeWrapper mWrapper;


    private ActionBar mActionBar;
    private GLVideoView mVideoView;
    private VideoClipSite mSite;

    private float mLeftProgress;
    private float mRightProgress = 1;

    private ClipVideoTask mClipVideoTask;
    private String mOutputPath;
    private WaitDialog1 mWaitDialog;

    public VideoClipPage(Context context, BaseSite baseSite, VideoModeWrapper wrapper) {
        super(context, baseSite);
        mContext = context;
        mSite = (VideoClipSite) baseSite;
        mWrapper = wrapper;
        mActionBar = wrapper.mActionBar;
        mActionBar.setOnActionbarMenuItemClick(new ActionBar.onActionbarMenuItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == ActionBar.LEFT_MENU_ITEM_CLICK) {
                    mSite.onBack();
                } else if (id == ActionBar.RIGHT_MENU_ITEM_CLICK) {
                    clipVideo();
                }
            }
        });
        mVideoView = mWrapper.mVideoView;

        mWaitDialog = new WaitDialog1(mContext, R.style.waitDialog);
        mWaitDialog.setMessage(getResources().getString(R.string.processing));
    }

    @Override
    public void SetData(HashMap<String, Object> params) {
        if (params != null) {
            ArrayList<cn.poco.video.sequenceMosaics.VideoInfo> videoInfoList = (ArrayList<cn.poco.video.sequenceMosaics.VideoInfo>)params.get("videos");
            setVideoInfoList(videoInfoList);
        }
        mCurrentTime.setText(TimeFormatter.makeVideoTimeDurationText(mWrapper.getCurDuration()));
        mTimeBar.setDuration((int)mWrapper.getCurDuration());
    }

    @Override
    protected void onLeftProgress(float leftProgress) {
        mLeftProgress = leftProgress;
        float diff = mClipTimeLineView.getRightProgress() - leftProgress;
        mCurrentTime.setText(TimeFormatter.makeVideoTimeDurationText((long)(diff * mWrapper.getCurDuration())));

        long currentMoment = (long)(leftProgress * mWrapper.getCurDuration());
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
        mVideoView.seekTo(currentMoment);
    }

    @Override
    protected void onRightProgress(float rightProgress) {
        mRightProgress = rightProgress;
        float diff = rightProgress - mClipTimeLineView.getLeftProgress();
        mCurrentTime.setText(TimeFormatter.makeVideoTimeDurationText((long)(diff * mWrapper.getCurDuration())));

        long currentMoment = (long)(rightProgress * mWrapper.getCurDuration());
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
        mVideoView.seekTo(currentMoment);
    }

    @Override
    protected void onProgressDragEnd() {

    }

    @Override
    protected boolean showVideoDuration() {
        return true;
    }

    @Override
    protected boolean showDragHandle() {
        return true;
    }

    @Override
    protected boolean showTimeIntervalText() {
        return true;
    }

    private void removeListener() {
        mActionBar.setOnActionbarMenuItemClick(null);
    }

    private void clipVideo() {
        mWrapper.pauseAll();

        mWaitDialog.show();
        mOutputPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
        long startTime = (long)(mLeftProgress * mWrapper.getCurDuration());
        long endTime = (long)(mRightProgress * mWrapper.getCurDuration());
        mClipVideoTask = new ClipVideoTask(mContext, mWrapper.mCurrentBeautifiedVideo.mPath,
                                           mOutputPath, startTime, endTime);
        mClipVideoTask.setOnProcessListener(mOnProcessListener);
        ThreadPool.getInstance().execute(mClipVideoTask);
    }

    private OnProcessListener mOnProcessListener = new OnProcessListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void onFinish() {
            mWrapper.mCurrentBeautifiedVideo.mClipPath = mOutputPath;
            mWaitDialog.dismiss();
            mWrapper.mVideoView.changeVideoPath(mOutputPath);

            onBack();
            mSite.onClickOk();
        }

        @Override
        public void onError(String message) {
            mWaitDialog.dismiss();
            T.showShort(mContext, "裁剪失败：" + message);
        }
    };

    @Override
    public void onBack() {
        super.onBack();
        removeListener();
        mSite.onBack();
    }
}
