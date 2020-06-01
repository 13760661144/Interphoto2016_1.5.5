package cn.poco.video.render.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

import cn.poco.interphoto2.BuildConfig;

/**
 * Author: Comit
 * Date: 2017/12/2
 * Time: 11:22
 */
public class MediaPlayerWrapper implements IPlayer {

    private Context mContext;
    private MediaPlayer mMediaPlayer;

    private String mDataSource;
    private Surface mSurface;

    private int mState = IDLE;

    private float mVolume = 1;
    private boolean isLooping = false;

    private boolean mPendingStart = false;
    private boolean isPreparedSeek = false;
    private boolean isReadyStart = false;
    private boolean isFinish = false;

    private long mPrepareSeekTo = 0;

    private OnPlayListener mOnPlayListener;

    public MediaPlayerWrapper(Context context) {
        mContext = context;

        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public void setSurface(Surface surface) {
        if (mState != IDLE) {
            throw new IllegalStateException("the state: " + mState);
        }

        if (surface == null) {
            throw new IllegalArgumentException("the surface is null");
        }

        mSurface = surface;
    }

    @Override
    public void setDataSource(String dataSource) {
        if (mState != IDLE) {
            throw new IllegalStateException();
        }

        if (TextUtils.isEmpty(dataSource)) {
            throw new IllegalArgumentException("the dataSource is null");
        }

        mDataSource = dataSource;
    }

    @Override
    public void setVolume(float volume) {
        if (volume != mVolume) {
            mVolume = volume;
            if (mState != IDLE && mState != RELEASE) {
                mMediaPlayer.setVolume(mVolume, mVolume);
            }
        }
    }

    @Override
    public float getVolume() {
        return mVolume;
    }

    @Override
    public boolean isStart() {
        return mState == START;
    }

    @Override
    public boolean isPause() {
        return mState == PAUSE;
    }

    @Override
    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    @Override
    public long getCurrentPosition() {
        if (mState == START || mState == PAUSE || mState == PREPARED) {
            return mMediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    @Override
    public long getDuration() {
        if (mState == IDLE || mState == PREPARING || mState == RELEASE) {
            return 0;
        }
        return mMediaPlayer.getDuration();
    }

    @Override
    public void seekTo(long position) {
        if (mState == PREPARED || mState == START || mState == PAUSE) {

            final long duration = getDuration();

            if (position < 0) {
                position = 0;
            } else if (position > duration) {
                position = duration;
            }

            if (isFinish) {
                mState = PAUSE;
                isFinish = false;
            }

            mMediaPlayer.seekTo((int)position);
        } else {
            mPrepareSeekTo = position;
        }
    }

    @Override
    public void setOnPlayListener(OnPlayListener listener) {
        mOnPlayListener = listener;
    }

    @Override
    public void prepare() {
        if (mState != IDLE) {
            throw new IllegalStateException();
        }

        if (mDataSource == null) {
            throw new RuntimeException("must call setDataSource() before");
        }

        if (mSurface == null) {
            throw new RuntimeException("must call setSurface() before");
        }

        try {
            try {
                mMediaPlayer.setDataSource(mDataSource);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(mDataSource);
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setVolume(mVolume, mVolume);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.prepareAsync();

            mState = PREPARING;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START && mPrepareSeekTo == 0) {
                if (mOnPlayListener != null) {
                    mOnPlayListener.onStart();
                }
            }
            return true;
        }
    };

    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            if (mState == PREPARING) {
                if (mPrepareSeekTo != 0) {
                    mMediaPlayer.seekTo((int)mPrepareSeekTo);
                } else {
                    isPreparedSeek = true;
                    mMediaPlayer.seekTo(0);
                }
                mState = PREPARED;
                if (mPendingStart) {
                    start();
                }
            }
        }
    };

    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {

            if (isPreparedSeek) {
                isPreparedSeek = false;
                return;
            }

            if (isReadyStart) {
                if (mOnPlayListener != null) {
                    mOnPlayListener.onStart();
                }
                isReadyStart = false;
                return;
            }

            if (mOnPlayListener != null) {
                mOnPlayListener.onSeekComplete(MediaPlayerWrapper.this);
            }
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (BuildConfig.DEBUG) {
                Log.d("comit", "onError: " + what + ":" + extra);
            }
            return true;
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {

            if (isLooping) {
                isReadyStart = true;
                mMediaPlayer.seekTo(0);
                mMediaPlayer.start();
            } else {
                isFinish = true;
                if (mOnPlayListener != null) {
                    mOnPlayListener.onFinish();
                }
            }
        }
    };

    @Override
    public void start() {
        if (isFinish && mState == PAUSE) {
            isFinish = false;
            isReadyStart = true;
            mMediaPlayer.seekTo(0);
        }
        if (mState == PREPARING) {
            mPendingStart = true;
        } else if (mState == PREPARED || mState == PAUSE) {
            mPendingStart = false;
            mMediaPlayer.start();
            mState = START;
        }
    }

    @Override
    public void restart() {
        if (isFinish && mState == START) {
            isFinish = false;
            mMediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (mState == START) {
            mMediaPlayer.pause();
            mState = PAUSE;
        }
    }

    @Override
    public void stop() {
        if (mState == START) {
            pause();
        }

        if (mState == PAUSE) {
            mMediaPlayer.stop();
            mState = STOP;
        }
    }

    @Override
    public boolean shouldFinish(long duration) {
        if (isFinish) {
            return true;
        }

        long position = getCurrentPosition();
        return Math.abs(position - duration) < 200;
    }

    @Override
    public void reset() {
        resetField();
        resetListeners();
        mMediaPlayer.reset();
        mState = IDLE;
    }

    @Override
    public void release() {
        mSurface = null;
        resetField();
        if (mState != STOP) {
            stop();
        }

        resetListeners();
        mMediaPlayer.release();
        mState = RELEASE;
    }

    private void resetListeners() {
        mMediaPlayer.setOnInfoListener(null);
        mMediaPlayer.setOnPreparedListener(null);
        mMediaPlayer.setOnSeekCompleteListener(null);
        mMediaPlayer.setOnErrorListener(null);
        mMediaPlayer.setOnCompletionListener(null);
    }

    private void resetField() {
        mPendingStart = false;
        isFinish = false;
        isPreparedSeek = false;
        isReadyStart = false;
        mPrepareSeekTo = 0;
    }
}
