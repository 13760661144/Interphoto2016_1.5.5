package cn.poco.video.save.player;

import android.content.Context;

import cn.poco.video.render.PlayVideoInfo;

/**
 * Created by: fwc
 * Date: 2017/12/27
 */
public class MultiSoftPlayer {

	private static final int IDLE = 0;
	private static final int PREPARED = 1;
	private static final int START = 2;
	private static final int RELEASE = 4;

	private Context mContext;

	private SoftPlayer mCurPlayer;
	private SoftPlayer mNextPlayer;

	private SoftTexture mCurTexture;
	private SoftTexture mNextTexture;

	private PlayVideoInfo[] mVideoInfos;
	private int mCurIndex = 0;

	private int mState = IDLE;

	private boolean isFinish = false;

	private boolean mCurStart;

	private OnPlayListener mOnPlayListener;

	private boolean mShouldExit;

	public MultiSoftPlayer(Context context) {
		mContext = context;

		init();
	}

	private void init() {

		mCurPlayer = new SoftPlayer();
		mNextPlayer = new SoftPlayer();
	}

	public void setVideoPaths(PlayVideoInfo... videoInfos) {

		if (mState != IDLE) {
			throw new IllegalStateException();
		}

		if (videoInfos == null || videoInfos.length == 0) {
			throw new IllegalArgumentException("the videoPaths are null or empty");
		}

		mVideoInfos = new PlayVideoInfo[videoInfos.length];
		System.arraycopy(videoInfos, 0, mVideoInfos, 0, videoInfos.length);
	}

	public void setSurface(SoftTexture curTexture, SoftTexture nextTexture) {

		if (mState != IDLE) {
			throw new IllegalStateException();
		}

		if (curTexture == null || nextTexture == null) {
			throw new IllegalArgumentException("the surface is null");
		}

		mCurTexture = curTexture;
		mNextTexture = nextTexture;
	}

	public void setOnPlayListener(OnPlayListener listener) {
		mOnPlayListener = listener;
	}

	public void prepare() {

		if (mState != IDLE) {
			throw new IllegalStateException();
		}

		if (mVideoInfos == null) {
			throw new RuntimeException("must call setVideoInfos() before");
		}

		if (mCurTexture == null) {
			throw new RuntimeException("must call setSurface() before");
		}

		if (mCurIndex < mVideoInfos.length) {
			mCurPlayer.setSoftTexture(mCurTexture);
			PlayVideoInfo info = mVideoInfos[mCurIndex];
			mCurPlayer.setDataSource(info.path, 0);
			mCurPlayer.setRotation(info.data.rotation);
			mCurPlayer.setDuration(info.data.duration);

			mCurPlayer.prepare();

			// 提前准备下一个视频
			if (mNextTexture != null) {
				mNextPlayer.setSoftTexture(mNextTexture);
				prepareNextPlayer();
			}
		}

		mState = PREPARED;
	}

	private void prepareNextPlayer() {

		if (mNextPlayer != null && mCurIndex + 1 < mVideoInfos.length) {
			int nextIndex = mCurIndex + 1;
			PlayVideoInfo info = mVideoInfos[nextIndex];
			mNextPlayer.setDataSource(info.path, 0);
			mNextPlayer.setDuration(info.data.duration);
			mNextPlayer.setRotation(info.data.rotation);

			mNextPlayer.prepare();
		}
	}

	public void startCurrent() {

		if (mState != PREPARED) {
			throw new IllegalStateException();
		}

		mState = START;
		mCurPlayer.startDecoder();

		long timestamp;
		try {
			while (!mShouldExit) {

				timestamp = mCurPlayer.getFrame();
				if (timestamp == 0 && !mCurStart) {
					onVideoStart();
				}

				if (mNextPlayer.isPlaying()) {
					mNextPlayer.getFrame();
				}
				mCurPlayer.notifyFrameAvailable();

				if (timestamp == -1) {
					onVideoFinish();
				}

				if (isFinish) {
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void requestExit() {
		mShouldExit = true;
	}

	private void onVideoStart() {
		mCurStart = true;
		if (mOnPlayListener != null) {
			mOnPlayListener.onStart(mCurIndex);
		}
	}

	private void onVideoFinish() {

		if (mOnPlayListener != null) {
			mOnPlayListener.onFinish(mCurIndex);
		}

		mCurStart = false;

		if (mCurIndex + 1 < mVideoInfos.length) {
			resetCurPlayer();

			SoftPlayer temp = mCurPlayer;
			mCurPlayer = mNextPlayer;
			mNextPlayer = temp;

			mCurIndex++;
			if (!mCurPlayer.isPlaying()) {
				mCurPlayer.startDecoder();
			}

			prepareNextPlayer();
		} else {
			isFinish = true;
		}
	}

	private void resetCurPlayer() {
		mCurPlayer.release();
		mCurPlayer = new SoftPlayer();
		mCurPlayer.setSoftTexture(mCurIndex % 2 == 0 ? mCurTexture : mNextTexture);
	}

	public int getCurrentIndex() {
		return mCurIndex;
	}

	public void startNext() {

		if (mState != START) {
			throw new IllegalStateException();
		}

		if (mNextPlayer != null) {
			mNextPlayer.startDecoder();
		}
	}

	public void release() {
		if (mCurPlayer != null) {
			mCurPlayer.release();
			mCurPlayer = null;
		}

		if (mNextPlayer != null) {
			mNextPlayer.release();
			mNextPlayer = null;
		}

		mState = RELEASE;
	}

	public interface OnPlayListener {
		void onStart(int index);
		void onFinish(int index);
	}
}
