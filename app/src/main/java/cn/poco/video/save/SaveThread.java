package cn.poco.video.save;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

import cn.poco.setting.SettingInfoMgr;
import cn.poco.video.encode.EncodeThread;
import cn.poco.video.encode.EncodeUtils;
import cn.poco.video.encode.VideoEncoderCore;
import cn.poco.video.process.ThreadPool;
import cn.poco.video.render.PlayRatio;
import cn.poco.video.render.PlayVideoInfo;
import cn.poco.video.save.audio.AudioThread;
import cn.poco.video.save.player.MultiSoftPlayer;
import cn.poco.video.save.player.SoftTexture;
import cn.poco.video.save.transition.AbstractTransition;
import cn.poco.video.save.watermark.BitmapInfo;
import cn.poco.video.utils.AudioHandler;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by: fwc
 * Date: 2017/12/27
 */
public class SaveThread implements Runnable, SoftTexture.OnFrameAvailableListener {

	private static final String TAG = "SaveThread";

	private Context mContext;
	private SaveParams mSaveParams;

	private Handler mMainHandler;

	private OnSaveListener mOnSaveListener;

	private int mWidth;
	private int mHeight;

	private MultiSoftPlayer mMultiSoftPlayer;
	private SaveRenderer mSaveRenderer;

	private EGLHelper mEGLHelper;
	private WatermarkHelper mWatermarkHelper;
	private AudioThread mAudioThread;

	private EncodeThread mEncodeThread;

	private String mVideoOutputPath;

	private boolean mShouldExit;

	public SaveThread(Context context, SaveParams saveParams) {
		mContext = context;
		mSaveParams = saveParams;

		mMainHandler = new Handler(Looper.getMainLooper());

		prepare();
	}

	public static SaveThread start(Context context, SaveParams params, OnSaveListener listener) {
		SaveThread thread = new SaveThread(context, params);
		thread.setOnSaveListener(listener);
		new Thread(thread).start();

		return thread;
	}

	public void setOnSaveListener(OnSaveListener listener) {
		mOnSaveListener = listener;
	}

	private void prepare() {
		mMultiSoftPlayer = new MultiSoftPlayer(mContext);
		mMultiSoftPlayer.setOnPlayListener(mOnPlayListener);

		mSaveRenderer = new SaveRenderer(mContext, mSaveParams, mOnRenderListener);
		mSaveRenderer.setOnTransitionListener(mOnTransitionListener);

		mEGLHelper = new EGLHelper(mSaveRenderer);

		initWatermarkInfo();

		boolean shouldHandleAudio = mSaveParams.videoVolume != 0 || (FileUtils.isFileExist(mSaveParams.musicPath) && mSaveParams.musicVolume != 0);

		if (shouldHandleAudio) {
			mAudioThread = new AudioThread(mSaveParams);
		}
	}

	/**
	 * 初始化文字水印信息
	 */
	private void initWatermarkInfo() {
		if (mSaveParams.videoText != null) {
			mWatermarkHelper = new WatermarkHelper(mContext, mSaveParams.videoText, mSaveParams.shapeEx, mSaveParams.startTime, mSaveParams.stayTime);
		}
	}

	/**
	 * 请求退出保存操作
	 */
	public void requestExit() {
		mShouldExit = true;
		if (mMultiSoftPlayer != null) {
			mMultiSoftPlayer.requestExit();
		}
		if (mAudioThread != null) {
			mAudioThread.interrupt();
			mAudioThread = null;
		}
	}

	@Override
	public void run() {

		Thread.currentThread().setName("SaveThread");

		onStart();

		if (mAudioThread != null) {
			mAudioThread.start();
		}

		try {
			guardedRun();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ensureFinish();
		}
	}

	private void guardedRun() throws IOException, InterruptedException {

		int width;
		int height;
		if (SettingInfoMgr.GetSettingInfo(mContext).getVideoQualityState()) {
			width = 1080;
			height = 1920;
		} else {
			width = 720;
			height = 1280;
		}

		switch (mSaveParams.playRatio) {
			case PlayRatio.RATIO_16_9:
				height = (int)(width * 9f / 16);
				break;
			case PlayRatio.RATIO_235_1:
				height = (int)(width / 2.35f);
				break;
			case PlayRatio.RATIO_1_1:
				height = width;
				break;
		}
		int[] size = EncodeUtils.limitVideoWidthAndHeight(VideoEncoderCore.EncodeConfig.MIME_TYPE, width, height, 0);

		mWidth = size[0];
		mHeight = size[1];

		mVideoOutputPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);

		VideoEncoderCore.EncodeConfig config = new VideoEncoderCore.EncodeConfig(mWidth, mHeight, VideoEncoderCore.EncodeConfig.FRAME_RATE, mVideoOutputPath);

		mEncodeThread = new EncodeThread(config);
		ThreadPool.getInstance().execute(mEncodeThread);

		if (mWatermarkHelper != null) {
			mWatermarkHelper.prepareWatermark(mWidth, mHeight);
		}

		mEGLHelper.initEGLContext(mEncodeThread.getEncodeSurface());
		mEGLHelper.onSurfaceCreated();
		mEGLHelper.onSurfaceChanged(mWidth, mHeight);

		mMultiSoftPlayer.startCurrent();

		mEGLHelper.onSurfaceDestroy();
	}

	private void ensureFinish() {

		if (mEncodeThread != null) {
			mEncodeThread.waitForFinish();
		}
		mMultiSoftPlayer.release();
		mEGLHelper.release();

		if (!mShouldExit) {
			String audioPath = null;
			if (mAudioThread != null) {
				audioPath = mAudioThread.getAudioPath();
			}

			boolean success = false;
			if (!TextUtils.isEmpty(audioPath) && new File(audioPath).exists() && new File(audioPath).length() > 0) {
				long videoDuration = VideoUtils.getDurationFromVideo2(mVideoOutputPath);
				audioPath = AudioHandler.ensureDuration(audioPath, videoDuration);
				success = VideoUtils.replaceAudio(mVideoOutputPath, audioPath, mSaveParams.outputPath);
				FileUtils.delete(audioPath);
			}

			if (!success) {
				FileUtils.renameOrCopy(mVideoOutputPath, mSaveParams.outputPath);
			}
		} else {
			if (mAudioThread != null) {
				mAudioThread.interrupt();
				mAudioThread = null;
			}
		}

		FileUtils.delete(mVideoOutputPath);

		mSaveParams = null;

		if (mShouldExit) {
			onCancel();
		} else {
			onFinish();
		}
	}

	@Override
	public void onFrameAvailable(SoftTexture softTexture) {
		mEGLHelper.onDrawFrame();
	}

	private void onStart() {
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mOnSaveListener != null) {
					mOnSaveListener.onStart();
				}
			}
		});
	}

	private void onProgress(final float progress) {
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mOnSaveListener != null) {
					mOnSaveListener.onProgress(progress);
				}
			}
		});
	}

	private void onCancel() {
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mOnSaveListener != null) {
					mOnSaveListener.onCancel();
				}
			}
		});
	}

	private void onFinish() {
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mOnSaveListener != null) {
					mOnSaveListener.onFinish();
				}
			}
		});
	}

//	private void release() {
//
//	}

	private MultiSoftPlayer.OnPlayListener mOnPlayListener = new MultiSoftPlayer.OnPlayListener() {

		@Override
		public void onStart(int index) {
			mSaveRenderer.onVideoStart(index);
		}

		@Override
		public void onFinish(int index) {
			mSaveRenderer.onVideoFinish(index);
		}
	};

	private SaveRenderer.OnRenderListener mOnRenderListener = new SaveRenderer.OnRenderListener() {

		@Override
		public void onCreateSurface(SoftTexture softTexture1, SoftTexture softTexture2) {
			softTexture1.setOnFrameAvailableListener(SaveThread.this);
			softTexture2.setOnFrameAvailableListener(SaveThread.this);

			mMultiSoftPlayer.setVideoPaths(mSaveParams.videoInfos.toArray(new PlayVideoInfo[] {}));
			mMultiSoftPlayer.setSurface(softTexture1, softTexture2);
			mMultiSoftPlayer.prepare();
		}

		@Override
		public int getCurrentIndex() {
			return mMultiSoftPlayer.getCurrentIndex();
		}

		@Override
		public void onDrawFrame(long timestamp) {
			mEGLHelper.onSwapBuffers(timestamp);
			mEncodeThread.encode();
			onProgress(timestamp / 10f / mSaveParams.duration);
		}

		@Override
		public BitmapInfo getWatermarkInfo(long timestamp) {
			if (mWatermarkHelper != null) {
				return mWatermarkHelper.getWatermarkInfo(mWidth, mHeight, timestamp);
			}

			return null;
		}
	};

	private AbstractTransition.OnTransitionListener mOnTransitionListener = new AbstractTransition.OnTransitionListener() {
		@Override
		public void onStartNextVideo() {
			if (mMultiSoftPlayer != null) {
				mMultiSoftPlayer.startNext();
			}
		}
	};

	public interface OnSaveListener {

		void onStart();

		void onProgress(float progress);

		void onCancel();

		void onFinish();
	}
}
