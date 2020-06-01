package cn.poco.video.view;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;

import cn.poco.tianutils.ShareData;
import cn.poco.video.model.VideoEntry;

/**
 * Created by Shine on 2017/6/2.
 */

public class PreviewVideo extends FrameLayout implements IVideoDelegate{
    private Context mContext;
    private RatioKeptSurfaceView mSurfaceView;
    private MediaPlayer mMediaPlayer;

    private VideoEntry mVideoEntry;
    private PreviewVideoDelegate mVideoDelegate = new PreviewVideoDelegate();
    private MediaPlayerCallbackAdapter mCallbackAdapter = new MediaPlayerCallbackAdapter();

    private boolean mAutoPlay;
    private int mVideoWidth = -1, mVideoHeight = -1;
    private int mDisplayHeight;

    public PreviewVideo(@NonNull Context context, VideoEntry videoEntry) {
        super(context);
        mContext = context;
        mVideoEntry = videoEntry;
        initData();
        initView();
    }

    private void initData() {
        mDisplayHeight = ShareData.m_screenHeight - ShareData.PxToDpi_xhdpi(80) - ShareData.PxToDpi_xhdpi(300);
        mVideoWidth = mVideoEntry.mWidth;
        mVideoHeight = mVideoEntry.mHeight;
    }

    private void initView() {
        mSurfaceView = new RatioKeptSurfaceView(mContext);
        FrameLayout.LayoutParams layoutParams;
        if (mVideoWidth != -1 && mVideoHeight != -1) {
            mSurfaceView.setVideoDimension(mVideoWidth, mVideoHeight);
            layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mDisplayHeight, Gravity.CENTER);
        } else {
            layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        }
        mSurfaceView.setLayoutParams(layoutParams);
        this.addView(mSurfaceView);
        mSurfaceView.getHolder().addCallback(mVideoDelegate);
    }

    @Override
    public void seekTo(int msec) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(msec);
        }
    }

    @Override
    public void playVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        } else {
            mAutoPlay = true;
            initView();
        }
    }

    @Override
    public void setVideoModel(VideoEntry videoEntry) {

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
    public void pauseVideo() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }

    @Override
    public void resumeVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void stopPlay() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mSurfaceView != null) {
            this.removeView(mSurfaceView);
            mVideoWidth = -1;
            mVideoHeight = -1;
            mSurfaceView.getHolder().removeCallback(mVideoDelegate);
        }
    }

    @Override
    public void clear() {
        stopPlay();
    }

    private void initMediaPlayer(SurfaceHolder holder) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDisplay(holder);
        try {
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(mCallbackAdapter);
            mMediaPlayer.setOnCompletionListener(mCallbackAdapter);
            mMediaPlayer.setOnErrorListener(mCallbackAdapter);
            mMediaPlayer.setOnVideoSizeChangedListener(mCallbackAdapter);
            mMediaPlayer.setDataSource(mVideoEntry.mMediaPath);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private class PreviewVideoDelegate implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceView.setVideoDimension(mVideoWidth, mVideoHeight);
            mSurfaceView.requestLayout();
            initMediaPlayer(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }


    private class MediaPlayerCallbackAdapter implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
            MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener{
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mp != null) {
               mp.start();
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (mp != null) {
                mp.reset();
                mp.setScreenOnWhilePlaying(true);
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setOnPreparedListener(mCallbackAdapter);
                mp.setOnCompletionListener(mCallbackAdapter);
                mp.setOnErrorListener(mCallbackAdapter);
                mp.setOnVideoSizeChangedListener(mCallbackAdapter);
                try {
                    mp.setDataSource(mVideoEntry.mMediaPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mp.prepareAsync();
            }
            return true;
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            if (mAutoPlay) {
                mp.start();
                mAutoPlay = false;
            }
        }


        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoHeight = width;
            mVideoWidth = height;
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                if (mSurfaceView != null) {
                    mSurfaceView.getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                    mSurfaceView.setVisibility(View.VISIBLE);
                    mSurfaceView.requestLayout();
                }
            }
        }
    }
}
