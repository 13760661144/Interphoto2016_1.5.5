package cn.poco.video.render.player;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;

/**
 * Created by: fwc
 * Date: 2017/12/7
 */
public class VideoMonitor {

	private static final int IDLE = 0;
	private static final int START = 1;
	private static final int END = 2;

	private static final long INTERVAL = 40;

	private HandlerThread mHandlerThread;
	private Handler mHandler;

	private int mState = IDLE;

	private long mTimestamp = 0;

	private OnMonitorListener mOnMonitorListener;

	public VideoMonitor(OnMonitorListener listener) {
		mHandlerThread = new HandlerThread("video-monitor");
		mHandlerThread.start();

		mHandler = new Handler(mHandlerThread.getLooper());

		mOnMonitorListener = listener;
	}

	public Looper getLooper() {
		return mHandlerThread.getLooper();
	}

	public synchronized void start() {
		if (mState == START) {
			return;
		}

		mTimestamp = 0;
		mState = START;
	}

	public synchronized void update(long timestamp) {

		mHandler.removeCallbacks(mMonitorRunnable);
		mTimestamp = timestamp;
		mOnMonitorListener.onUpdate(mTimestamp);

		if (mState == START) {
			mHandler.postAtTime(mMonitorRunnable, SystemClock.uptimeMillis() + INTERVAL);
		}
	}


	private Runnable mMonitorRunnable = new Runnable() {
		@Override
		public void run() {
			mHandler.removeCallbacks(this);

			synchronized (VideoMonitor.this) {
				if (mState == START) {
					mTimestamp += INTERVAL;
					mOnMonitorListener.onUpdate(mTimestamp);
					mHandler.postAtTime(mMonitorRunnable, SystemClock.uptimeMillis() + INTERVAL);
				}
			}
		}
	};

	public synchronized void reset() {
		mHandler.removeCallbacks(mMonitorRunnable);
		mTimestamp = 0;
		mState = IDLE;
	}

	public synchronized void release() {
		mState = END;
		mHandler.removeCallbacksAndMessages(null);
		if (mHandlerThread != null) {
			mHandlerThread.quit();
			mHandlerThread = null;
		}
	}

	public interface OnMonitorListener {
		void onUpdate(long timestamp);
	}
}
