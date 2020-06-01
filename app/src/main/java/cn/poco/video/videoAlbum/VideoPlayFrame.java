package cn.poco.video.videoAlbum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import cn.poco.video.model.VideoEntry;
import cn.poco.video.view.IVideoDelegate;
import cn.poco.video.view.PreviewVideo;

/**
 * Created by Shine on 2017/5/17.
 */

public class VideoPlayFrame extends FrameLayout implements IVideoDelegate
{
    private float mProgress;
    private IVideoDelegate mVideoDelegate;
    protected PreviewVideo mVideoView;
    private Context mContext;
    private VideoEntry mVideoEntry;

    public VideoPlayFrame(@NonNull Context context) {
        super(context);
        mContext = context;
        setWillNotDraw(false);
    }

    private void initView() {
        mVideoView = new PreviewVideo(mContext, mVideoEntry);
        mVideoDelegate = mVideoView;
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mVideoView.setLayoutParams(params);
        this.addView(mVideoView);
        mVideoView.setAlpha(0);
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        invalidate();
    }


    @Override
    public void setVideoModel(VideoEntry videoEntry) {
        mVideoEntry = videoEntry;
        initView();
        mVideoDelegate.setVideoModel(videoEntry);
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void playVideo() {
        if (mVideoView != null) {
            mVideoView.requestFocus();
        }

        if (mVideoDelegate != null) {
            mVideoDelegate.playVideo();
        }
    }

    @Override
    public void pauseVideo() {
        if (mVideoDelegate != null) {
            mVideoDelegate.pauseVideo();
        }
    }

    @Override
    public void resumeVideo() {
        if (mVideoDelegate != null) {
            mVideoDelegate.resumeVideo();
        }
    }

    @Override
    public void stopPlay() {
        if (mVideoDelegate != null) {
            mVideoDelegate.stopPlay();
        }
    }

    @Override
    public void clear() {
        if (mVideoDelegate != null) {
            mVideoDelegate.clear();
            mVideoDelegate = null;
        }
        mVideoView = null;
    }

    @Override
    public void seekTo(int msec) {
        if (mVideoDelegate != null) {
            mVideoDelegate.seekTo(msec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int color = Color.argb((int)Math.ceil(mProgress * 255), 0, 0, 0);
        canvas.drawColor(color);
    }
}
