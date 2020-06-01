package cn.poco.video.render.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import cn.poco.video.render.transition.TransitionItem;

/**
 * Author: Comit
 * Date: 2017/12/2
 * Time: 15:03
 */
public class MultiPlayerImpl implements IMultiPlayer {

	private Context mContext;

	private IPlayer mPlayer1;
	private IPlayer mPlayer2;

	private MultiSurface mSurface;

	private String[] mDataSources;
	private int mCurrentIndex = 0;

	private int mState = IPlayer.IDLE;

	private float mVolume = 1;

	private boolean isLooping = true;

	private boolean isSingleVideo = false;

	private OnMultiPlayListener mOnMultiPlayListener;

	/**
	 * 用于标记正在执行视频结束清理工作，避免多线程出现问题
	 */
	private boolean isVideoFinishing = false;

	/**
	 * 调用seekTo时用于标记mPlayer2是否也调用了seekTo
	 */
	private boolean isNextVideoSeeked = false;

	/**
	 * 解决红米手机resume的时候会回调onStart方法的问题
	 */
	private boolean isCallResume;

	/**
	 * 解决金立手机第二个以后的视频开始播放不回调onStart方法的问题
	 */
	private boolean isCallStart = false;

	private Handler mHandler;

	public MultiPlayerImpl(Context context) {
		mContext = context;

		mHandler = new Handler(Looper.getMainLooper());

		initPlayer();
	}

	private void initPlayer() {
		mPlayer1 = new MediaPlayerWrapper(mContext);
		mPlayer1.setLooping(false);

		mPlayer2 = new MediaPlayerWrapper(mContext);
		mPlayer2.setLooping(false);
	}

	@Override
	public void setSurface(MultiSurface surface) {
		if (mState != IPlayer.IDLE) {
			throw new IllegalStateException();
		}

		if (surface == null) {
			throw new IllegalArgumentException("the surface is null");
		}

		mSurface = surface;
	}

	@Override
	public void setDataSources(String... dataSources) {
		if (mState != IPlayer.IDLE) {
			throw new IllegalStateException();
		}

		if (dataSources == null || dataSources.length == 0) {
			throw new IllegalArgumentException("the dataSources are empty");
		}

		isSingleVideo = dataSources.length == 1;

		mDataSources = new String[dataSources.length];
		System.arraycopy(dataSources, 0, mDataSources, 0, mDataSources.length);
	}

	@Override
	public void setCurrentIndex(int currentIndex) {
		if (mState != IPlayer.IDLE) {
			throw new IllegalStateException();
		}

		if (mDataSources == null) {
			throw new RuntimeException("must call setDataSources() before");
		}

		if (currentIndex >= 0 && currentIndex < mDataSources.length) {
			mCurrentIndex = currentIndex;
		}
	}

	@Override
	public int getCurrentIndex() {
		return mCurrentIndex;
	}

	@Override
	public long getCurrentPlayPosition() {
		if (mState == IPlayer.IDLE || mState == IPlayer.RELEASE || isVideoFinishing) {
			return 0;
		}

		return mPlayer1.getCurrentPosition();
	}

	@Override
	public void setVolume(float volume) {
		if (mVolume != volume) {
			mVolume = volume;
			mPlayer1.setVolume(mVolume);
			mPlayer2.setVolume(mVolume);
		}
	}

	@Override
	public float getVolume() {
		return mVolume;
	}

	@Override
	public void setLooping(boolean looping) {
		isLooping = looping;
	}

	@Override
	public void setOnMultiPlayListener(OnMultiPlayListener listener) {
		mOnMultiPlayListener = listener;
	}

	@Override
	public void updateFrame() {

	}

	@Override
	public void prepare() {
		if (mState != IPlayer.IDLE) {
			throw new IllegalStateException();
		}

		if (mSurface == null) {
			throw new RuntimeException("the surface is null");
		}

		if (mDataSources == null || mDataSources.length == 0) {
			throw new RuntimeException("the dataSources are empty");
		}

		boolean isMute = false;
		if (mOnMultiPlayListener != null) {
			isMute = mOnMultiPlayListener.isMute(mCurrentIndex);
		}
		preparePlayer(mPlayer1, mSurface.getCurrentSurface(), mDataSources[mCurrentIndex], isMute, mOnPlayListener);

		prepardNextVideo();
		mState = IPlayer.PREPARED;
	}

	private void preparePlayer(IPlayer player, Surface surface, String dataSource, boolean isMute, IPlayer.OnPlayListener listener) {
		player.setSurface(surface);
		player.setDataSource(dataSource);
		player.setOnPlayListener(listener);
		if (isMute) {
			player.setVolume(0);
		} else {
			player.setVolume(mVolume);
		}
		player.prepare();
	}

	private void prepardNextVideo() {
		if (isSingleVideo) {
			return;
		}

		int next = getNextIndex();
		if (next < mDataSources.length) {
			boolean isMute = false;
			if (mOnMultiPlayListener != null) {
				isMute = mOnMultiPlayListener.isMute(next);
			}
			preparePlayer(mPlayer2, mSurface.getNextSurface(), mDataSources[next], isMute, null);
		}
	}

	private int getNextIndex() {
		int next = mCurrentIndex + 1;
		if (isLooping) {
			next = next % mDataSources.length;
		}

		return next;
	}

	@Override
	public void start() {
		if (mState == IPlayer.PREPARED || mState == IPlayer.PAUSE) {

			isCallResume = mState == IPlayer.PAUSE;
			isCallStart = false;
			mPlayer1.start();

			if (mPlayer2.isPause() || isNextVideoSeeked) {
				mPlayer2.start();
				isNextVideoSeeked = false;
			}
			mState = IPlayer.START;
		}
	}

	@Override
	public void startNext() {
		if (mState == IPlayer.START) {
			mPlayer2.start();
		}
	}

	@Override
	public void seekTo(int index, long position) {

		if (index >= mDataSources.length) {
			return;
		}

		isNextVideoSeeked = false;

		if (isSingleVideo) {
			mPlayer1.seekTo(position);
		} else if (mOnMultiPlayListener != null) {
			long duration = mOnMultiPlayListener.getDuration(index);
			if (position >= duration) {
				position = duration - 1;
			} else if (position < 0) {
				position = 0;
			}

			if (index > 0 && position < TransitionItem.DEFAULT_TIME) {
				boolean isBlend = mOnMultiPlayListener.isBlendTransition(index - 1);
				if (isBlend) {
					mCurrentIndex = index - 1;
					boolean isMute = false;
					if (mOnMultiPlayListener != null) {
						isMute = mOnMultiPlayListener.isMute(mCurrentIndex);
					}

					mPlayer1.reset();
					mPlayer2.reset();
					preparePlayer(mPlayer1, mSurface.getCurrentSurface(), mDataSources[mCurrentIndex], isMute, mOnPlayListener);
					prepardNextVideo();

					duration = mOnMultiPlayListener.getDuration(index - 1);
					mPlayer1.seekTo(duration - position);
					mPlayer2.seekTo(position);

					isNextVideoSeeked = true;
					return;
				}
			}

			boolean isBlend = mOnMultiPlayListener.isBlendTransition(index);
			if (mCurrentIndex == index) {
				seekToInner(position, duration, isBlend);
			} else {
				mCurrentIndex = index;
				boolean isMute = false;
				if (mOnMultiPlayListener != null) {
					isMute = mOnMultiPlayListener.isMute(mCurrentIndex);
				}
				mPlayer1.reset();
				mPlayer2.reset();
				preparePlayer(mPlayer1, mSurface.getCurrentSurface(), mDataSources[mCurrentIndex], isMute, mOnPlayListener);
				prepardNextVideo();

				seekToInner(position, duration, isBlend);
			}
		}
	}

	@Override
	public void forceSeekTo(int index, long position) {

	}

	@Override
	public void changeVideoOrder(int index, long position, String... dataSources) {
		if (dataSources == null || dataSources.length == 0) {
			throw new IllegalArgumentException("the dataSources are empty");
		}
		mDataSources = new String[dataSources.length];
		System.arraycopy(dataSources, 0, mDataSources, 0, mDataSources.length);

		mPlayer1.reset();
		mPlayer2.reset();

		mCurrentIndex = index;
		boolean isMute = false;
		if (mOnMultiPlayListener != null) {
			isMute = mOnMultiPlayListener.isMute(mCurrentIndex);
		}
		preparePlayer(mPlayer1, mSurface.getCurrentSurface(), mDataSources[mCurrentIndex], isMute, mOnPlayListener);
		prepardNextVideo();

		boolean isBlend = mOnMultiPlayListener.isBlendTransition(index);
		final long duration = mOnMultiPlayListener.getDuration(index);
		position = Math.min(position, duration);
		seekToInner(position, duration, isBlend);
	}

	private void seekToInner(long position, long duration, boolean isBlend) {
		mPlayer1.seekTo(position);
		boolean isInTransition = position > duration - TransitionItem.DEFAULT_TIME;
		if (isBlend && isInTransition) {
			mPlayer2.seekTo(duration - position);
			isNextVideoSeeked = true;
		}
	}

	@Override
	public void pause() {
		if (mState == IPlayer.START) {
			mPlayer1.pause();
			mPlayer2.pause();

			isCallStart = false;
			mState = IPlayer.PAUSE;
		}
	}

	@Override
	public void finishVideo() {
		if (!isSingleVideo) {
			onVideoFinish();
		}
	}

	@Override
	public void reset() {
		mHandler.removeCallbacksAndMessages(null);
		mCurrentIndex = 0;
//		isSingleVideo = false;
		isNextVideoSeeked = false;
		isCallResume = false;
		isCallStart = false;
		mPlayer1.setOnPlayListener(null);
		mPlayer1.reset();
		mPlayer2.setOnPlayListener(null);
		mPlayer2.reset();

		mState = IPlayer.IDLE;
	}

	@Override
	public void release() {
		mHandler.removeCallbacksAndMessages(null);
		mCurrentIndex = 0;
		mPlayer1.setOnPlayListener(null);
		mPlayer1.release();
		mPlayer2.setOnPlayListener(null);
		mPlayer2.release();

		mState = IPlayer.RELEASE;
	}

	@Override
	public void post(Runnable r) {

	}

	private IPlayer.OnPlayListener mOnPlayListener = new IPlayer.OnPlayListener() {

		@Override
		public void onStart() {
			if (!isCallStart) {
				isCallStart = true;
				mSurface.onVideoStart(mCurrentIndex);
				if (!isCallResume) {
					final int startIndex = mCurrentIndex;
					if (mOnMultiPlayListener != null) {
						mOnMultiPlayListener.onStart(startIndex);
					}
				}

				isCallResume = false;
			}
		}

		@Override
		public void onFinish() {
			isCallStart = false;
			mSurface.onVideoFinish(mCurrentIndex);
			if (isSingleVideo) {
				if (isLooping) {
					mPlayer1.restart();
					final int startIndex = mCurrentIndex;
					if (mOnMultiPlayListener != null) {
						mOnMultiPlayListener.onStart(startIndex);
					}
				}
				else {
					final int finishIndex = mCurrentIndex;
					if (mOnMultiPlayListener != null) {
						mOnMultiPlayListener.onFinish(finishIndex);
					}
				}
			}
		}

		@Override
		public void onSeekComplete(IPlayer player) {
			if (mOnMultiPlayListener != null) {
				mOnMultiPlayListener.onSeekComplete(mCurrentIndex, player.getCurrentPosition());
			}
		}
	};

	/**
	 * 视频结束，由转场动画回调通知
	 */
	private void onVideoFinish() {

		if (mState != IPlayer.START || mOnMultiPlayListener == null) {
			return;
		}

		long duration = mOnMultiPlayListener.getDuration(mCurrentIndex);
		if (!mPlayer1.shouldFinish(duration)) {
			return;
		}

		isVideoFinishing = true;
		isCallResume = false;
		isCallStart = false;
		mSurface.onVideoFinish(mCurrentIndex);
		final int finishIndex = mCurrentIndex;
		if (mOnMultiPlayListener != null) {
			mOnMultiPlayListener.onFinish(finishIndex);
		}

		mPlayer1.setOnPlayListener(null);
		mPlayer1.reset();
		mCurrentIndex = getNextIndex();
		if (mCurrentIndex < mDataSources.length) {
			final IPlayer player = mPlayer1;
			mPlayer1 = mPlayer2;
			mPlayer2 = player;
			mSurface.onChangeSurface();

			mPlayer1.setOnPlayListener(mOnPlayListener);
			mPlayer1.start();
			mHandler.postDelayed(mCallStartRunnable, 100);

			prepardNextVideo();
		}
		isVideoFinishing = false;
	}

	private Runnable mCallStartRunnable = new Runnable() {
		@Override
		public void run() {
			if (!isCallStart && mOnPlayListener != null) {
				mOnPlayListener.onStart();
			}
		}
	};
}
