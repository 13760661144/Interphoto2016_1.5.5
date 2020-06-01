package cn.poco.video.render.player;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.WorkerThread;

/**
 * Created by: fwc
 * Date: 2018/2/7
 */
public class MultiPlayer implements IMultiPlayer {

	private final Context mContext;

	private MultiPlayerImpl mMultiPlayerImpl;

	private HandlerThread mHandlerThread;
	private Handler mThread;

	private long mLastSeekIndex = -1;
	private long mLastSeekPosition = 0;
	private static final long MAX_RANGE = 300;

	public MultiPlayer(Context context) {
		mContext = context;

		mMultiPlayerImpl = new MultiPlayerImpl(context);

		mHandlerThread = new HandlerThread("MultiPlayer");
		mHandlerThread.start();
		mThread = new Handler(mHandlerThread.getLooper());
	}

	@Override
	public void setSurface(final MultiSurface surface) {
		mMultiPlayerImpl.setSurface(surface);
	}

	@Override
	public void setDataSources(final String... dataSources) {
		mMultiPlayerImpl.setDataSources(dataSources);
	}

	@Override
	public void setCurrentIndex(final int currentIndex) {
		mMultiPlayerImpl.setCurrentIndex(currentIndex);
	}

	@Override
	public int getCurrentIndex() {
		return mMultiPlayerImpl.getCurrentIndex();
	}

	@Override
	public long getCurrentPlayPosition() {
		return mMultiPlayerImpl.getCurrentPlayPosition();
	}

	@Override
	public void setVolume(final float volume) {
		mMultiPlayerImpl.setVolume(volume);
	}

	@Override
	public float getVolume() {
		return mMultiPlayerImpl.getVolume();
	}

	@Override
	public void setLooping(final boolean looping) {
		mMultiPlayerImpl.setLooping(looping);
	}

	@Override
	public void setOnMultiPlayListener(OnMultiPlayListener listener) {
		mMultiPlayerImpl.setOnMultiPlayListener(listener);
	}

	@Override
	public void updateFrame() {
		mMultiPlayerImpl.updateFrame();
	}

	@Override
	public void prepare() {
		mMultiPlayerImpl.prepare();
	}

	@Override
	public void start() {
		mThread.post(new Runnable() {
			@Override
			public void run() {
				mMultiPlayerImpl.start();
			}
		});

	}

	@Override
	@WorkerThread
	public void startNext() {
		mMultiPlayerImpl.startNext();
	}

	@Override
	public void seekTo(final int index, final long position) {
		if (index != mLastSeekIndex || Math.abs(position - mLastSeekPosition) >= MAX_RANGE) {
			mThread.post(new Runnable() {
				@Override
				public void run() {
					mMultiPlayerImpl.seekTo(index, position);
				}
			});
			mLastSeekIndex = index;
			mLastSeekPosition = position;
		}
	}

	@Override
	public void forceSeekTo(final int index, final long position) {

		mMultiPlayerImpl.seekTo(index, position);

		mLastSeekIndex = -1;
		mLastSeekPosition = 0;
	}

	@Override
	@WorkerThread
	public void changeVideoOrder(final int index, final long position, final String... dataSources) {
		mMultiPlayerImpl.changeVideoOrder(index, position, dataSources);
	}

	@Override
	public void pause() {
		mMultiPlayerImpl.pause();
	}

	@Override
	@WorkerThread
	public void finishVideo() {

		mMultiPlayerImpl.finishVideo();
	}

	@Override
	public void reset() {
		mLastSeekIndex = -1;
		mLastSeekPosition = 0;
//		mThread.removeCallbacksAndMessages(null);
		mMultiPlayerImpl.reset();
	}

	@Override
	public void release() {
		mLastSeekIndex = -1;
		mLastSeekPosition = 0;
		mThread.removeCallbacksAndMessages(null);
		if (mHandlerThread != null) {
			mHandlerThread.quit();
			mHandlerThread = null;
		}

		mMultiPlayerImpl.release();
	}

	@Override
	public void post(Runnable r) {
		mThread.post(r);
	}
}
