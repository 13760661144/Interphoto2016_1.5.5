package cn.poco.video.process;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;

import cn.poco.interphoto2.BuildConfig;
import cn.poco.video.NativeUtils;
import cn.poco.video.decode.DecodeUtils;
import cn.poco.video.decode.VideoInfo;
import cn.poco.video.encode.EncodeThread;
import cn.poco.video.encode.VideoEncoderCore;
import cn.poco.video.save.EGLHelper;
import cn.poco.video.save.player.SoftPlayer;
import cn.poco.video.save.player.SoftTexture;
import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2018/1/9
 * 视频压缩
 */
public class CompressTask implements Runnable, SoftTexture.OnFrameAvailableListener {

	private Context mContext;

	private final String mVideoPath;
	private float mOffset;
	private final int mWidth;
	private final int mHeight;
	private final String mOutputPath;

	private long mDuration;
	private int mRotation;

	private String mVideoOutputPath;

	private EGLHelper mEGLHelper;

	private SoftPlayer mSoftPlayer;
	private EncodeThread mEncodeThread;

	private Handler mMainHandler;

	private OnProcessListener mListener;

	public CompressTask(Context context, String videoPath, float offset,
						int width, int height, String outputPath) {
		mContext = context;

		mVideoPath = videoPath;
		mOutputPath = outputPath;

		mWidth = width;
		mHeight = height;

		mOffset = offset;

		VideoInfo info = DecodeUtils.getVideoInfo(mVideoPath);

		if (info != null) {
			mDuration = info.duration;
			if (mDuration / 1000 < mOffset) {
				mOffset = 0f;
			}
			mRotation = info.rotation;
		} else  {
			if (BuildConfig.DEBUG) {
				throw new RuntimeException("the VideoInfo is null");
			}

			onError("the VideoInfo is null");
		}

		mMainHandler = new Handler(Looper.getMainLooper());
	}

	public void setOnProcessListener(OnProcessListener listener) {
		mListener = listener;
	}

	private void prepare() throws IOException {

		RGBARender render = new RGBARender(mContext, new RGBARender.OnRenderListener() {
			@Override
			public void onCreateSurface(SoftTexture softTexture) {
				softTexture.setOnFrameAvailableListener(CompressTask.this);
				mSoftPlayer = new SoftPlayer();
				mSoftPlayer.setDataSource(mVideoPath, mOffset);
				mSoftPlayer.setRotation(mRotation);
				mSoftPlayer.setDuration(mDuration);
				mSoftPlayer.setSoftTexture(softTexture);
				mSoftPlayer.prepare();
			}

			@Override
			public void onDrawFrame(long timestamp) {
				mEGLHelper.onSwapBuffers(timestamp);
				mEncodeThread.encode();
			}
		});
		mEGLHelper = new EGLHelper(render);

		mVideoOutputPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);

		VideoEncoderCore.EncodeConfig config = new VideoEncoderCore.EncodeConfig(mWidth, mHeight, 25, mVideoOutputPath);
		mEncodeThread = new EncodeThread(config);
		ThreadPool.getInstance().execute(mEncodeThread);
	}

	@Override
	public void run() {
		Thread.currentThread().setName("CompressTask");

		onStart();

		try {
			guardedRun();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			finish();
		}
	}

	private void finish() {
		if (mEncodeThread != null) {
			mEncodeThread.waitForFinish();
		}

		mSoftPlayer.release();
		mEGLHelper.release();

		File file = new File(mVideoOutputPath);
		if (!file.exists() || file.length() <= 0) {
			cn.poco.album2.utils.FileUtils.copyFile(mVideoPath, mOutputPath);
		} else {
			boolean outputSuccess = false;

			String audioPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
			int result = NativeUtils.getAACFromVideo(mVideoPath, audioPath);
			file = new File(audioPath);
			if (result >= 0 && file.exists() && file.length() > 0) {
				int rotate = NativeUtils.getRotateAngleFromFile(mVideoPath);
				if (rotate != 0) {
					result = NativeUtils.setMp4Rotation(mVideoOutputPath, audioPath, mOutputPath, String.valueOf(rotate), 0);
				} else {
					result = NativeUtils.muxerMp4(mVideoOutputPath, audioPath, mOutputPath, 0);
				}

				FileUtils.delete(audioPath);

				outputSuccess = result >= 0;
			}

			if (!outputSuccess) {
				saveVideoFile();
			}
		}

		FileUtils.delete(mVideoOutputPath);

		onFinish();
	}

	private void saveVideoFile() {
		int rotate = NativeUtils.getRotateAngleFromFile(mVideoPath);
		if (rotate != 0) {
			int result = NativeUtils.setMp4Rotation(mVideoOutputPath, "", mOutputPath, String.valueOf(rotate), 0);
			if (result >= 0) {
				return;
			}
		}

		FileUtils.delete(mOutputPath);
		FileUtils.renameOrCopy(mVideoOutputPath, mOutputPath);
	}

	private void guardedRun() throws IOException {
		prepare();

		mEGLHelper.initEGLContext(mEncodeThread.getEncodeSurface());
		mEGLHelper.onSurfaceCreated();
		mEGLHelper.onSurfaceChanged(mWidth, mHeight);

		mSoftPlayer.start();

		mEGLHelper.onSurfaceDestroy();
	}

	@Override
	public void onFrameAvailable(SoftTexture softTexture) {
		mEGLHelper.onDrawFrame();
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
