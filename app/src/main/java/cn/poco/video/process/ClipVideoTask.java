package cn.poco.video.process;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import cn.poco.video.clip.ClipVideoCore;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by: fwc
 * Date: 2018/1/16
 * 视频裁剪
 */
public class ClipVideoTask implements Runnable {

	private static final float ERROR = 0.5f;

	private final Context mContext;
	private String mVideoPath;
	private final String mOutputPath;
	private final long mStartTime;
	private final long mEndTime;

	private Handler mMainHandler;
	private OnProcessListener mListener;

	public ClipVideoTask(Context context, String videoPath, String outputPath, long startTime, long endTime) {

		if (startTime >= endTime) {
			throw new IllegalArgumentException();
		}

		mContext = context;
		mVideoPath = videoPath;
		mOutputPath = outputPath;
		mStartTime = startTime;
		mEndTime = endTime;

		mMainHandler = new Handler(Looper.getMainLooper());
	}

	public void setOnProcessListener(OnProcessListener listener) {
		mListener = listener;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("ClipVideoTask");

		onStart();

		float result, offset, start;
		String tempPath;

		start = mStartTime / 1000f;
		tempPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
		result = VideoUtils.mixVideoSegment(mVideoPath, tempPath, start, mEndTime / 1000f);
//		NativeUtils.endMixing();
		if (result < 0) {
			offset = start;
		} else {
			offset = start - result;
		}
		if (offset >= ERROR) {
			mVideoPath = tempPath;

			ClipVideoCore clipVideoCore = new ClipVideoCore(mVideoPath, offset, mOutputPath);
			boolean prepare = clipVideoCore.prepare();
			if (prepare) {
				clipVideoCore.start();
			} else {
				onError("");
				return;
			}
		} else {
			FileUtils.renameOrCopy(tempPath, mOutputPath);
		}

		FileUtils.delete(tempPath);

		onFinish();
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
