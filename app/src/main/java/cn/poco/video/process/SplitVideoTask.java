package cn.poco.video.process;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import cn.poco.video.utils.VideoUtils;

/**
 * Created by: fwc
 * Date: 2018/1/19
 */
public class SplitVideoTask implements Runnable {

	private final Context mContext;
	private final String mVideoPath;
	private final long mDuration;
	private final long mSplitTime;
	private final String[] mOutputPaths;

	private ClipVideoTask mClipVideoTask;

	private OnProcessListener mListener;

	private Handler mMainHandler;

	public SplitVideoTask(Context context, String videoPath, float splitProgress, long duration, String[] outputPaths) {

		if (outputPaths.length < 2 || splitProgress <= 0 || splitProgress >= 1) {
			throw new IllegalArgumentException();
		}

		mContext = context;
		mVideoPath = videoPath;
		mDuration = duration;
		mSplitTime = (long)(splitProgress * duration);
		mOutputPaths = outputPaths;

		mMainHandler = new Handler(Looper.getMainLooper());
	}

	public void setOnProcessListener(OnProcessListener listener) {
		mListener = listener;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("SplitVideoTask");

		onStart();

		VideoUtils.mixVideoSegment(mVideoPath, mOutputPaths[0], 0, mSplitTime / 1000f);
//		if (result < 0) {
//			throw new RuntimeException("call native funtion to clip video");
//		}

//		long startTime = (mSplitTime / 1000 + 1) * 1000;
		mClipVideoTask = new ClipVideoTask(mContext, mVideoPath, mOutputPaths[1], mSplitTime + 50 , mDuration);
		mClipVideoTask.run();

		long duration = VideoUtils.getDurationFromVideo2(mOutputPaths[1]);
		if (duration < 1000) {
			onError("");
		} else {
			onFinish();
		}
	}

	private void onStart() {
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mListener != null) {
					mListener.onStart();
				}
			}
		});
	}

	private void onFinish() {
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mListener != null) {
					mListener.onFinish();
				}
			}
		});
	}

	private void onError(final String message) {
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mListener != null) {
					mListener.onError(message);
				}
			}
		});
	}
}
