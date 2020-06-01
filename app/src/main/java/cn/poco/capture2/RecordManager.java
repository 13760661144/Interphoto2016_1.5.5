package cn.poco.capture2;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.poco.camera.CameraConfig;
import cn.poco.capture2.encoder.MediaAudioEncoder;
import cn.poco.capture2.encoder.MediaEncoder;
import cn.poco.capture2.encoder.MediaMuxerWrapper;
import cn.poco.capture2.encoder.MediaVideoEncoder;
import cn.poco.capture2.encoder.RecordState;
import cn.poco.utils.FileUtil;
import cn.poco.video.encode.EncodeUtils;
import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2017/10/9
 */
public class RecordManager implements MediaEncoder.MediaEncoderListener {

	/**
	 * 未准备
	 */
	private static final int STATE_UNPREPARE = -1;

	/**
	 * 准备中
	 */
	private static final int STATE_PREPARING = 0;

	/**
	 * 已准备
	 */
	private static final int STATE_PREPARED = 1;

	/**
	 * 音频最小buffer大小
	 */
	private static final int READ_SIZE = 2048;
	/**
	 * 音频采样率
	 */
	private static final int SAMPLE_RATE = 44100;

	private Context mContext;
	private ExecutorService mService;

	private Handler mHandler;
	private int mVideoWidth;
	private int mVideoHeight;
	private String mVideoFileDir;
	private String mVideoFilePath;

	private MediaMuxerWrapper mMediaMuxerWrapper;
	private boolean mAudioRecordEnable = true;
	private boolean mIsDestroy;

	/**
	 * 准备状态
	 */
	private int mPrepareState = STATE_UNPREPARE;

	/**
	 * 录制状态
	 */
	private int mRecordState;

	private OnRecordListener mOnRecordListener;
	private CountDownTimer mCountDownTimer;

	/**
	 * 进度更新间隔
	 */
	private static final long TICK_TIME = 50;

	/**
	 * 最小录制时长
	 */
	public static final int MIN_RECORD_DURATION = 1000;

	private long mRecordTime = -1;
	private boolean mCountDownIsFinish;
	private long mDuration;

	private boolean isValid;

	private MediaVideoEncoder mMediaVideoEncoder;
	private MediaAudioEncoder mMediaAudioEncoder;

	/**
	 * 录制时间误差
	 */
	private static final int TIME_ERROR = 0;

	/**
	 * 视频时长，默认10秒
	 */
	private int mMaxVideoDuration = 10000 + TIME_ERROR;

	private int mMinVideoDuration = MIN_RECORD_DURATION;

	/**
	 * 剩下可录制时长
	 */
	private int mLeftDuration;

	private MediaVideoEncoder.OnInitListener mOnInitListener;

	public RecordManager(Context context) {
		mContext = context;

		mService = Executors.newFixedThreadPool(2);
	}

	public void setMessageHandler(Handler handler) {
		mHandler = handler;
	}

	public void setOnRecordListener(OnRecordListener listener) {
		mOnRecordListener = listener;
	}

	public void setOnInitListener(MediaVideoEncoder.OnInitListener onInitListener) {
		mOnInitListener = onInitListener;
	}

	public void initDefaultPath() {
		mVideoFileDir = FileUtils.sVideoDir;
	}

	public void setVideoSize(int width, int height) {
		mVideoWidth = EncodeUtils.checkEncodeSize(width);
		mVideoHeight = EncodeUtils.checkEncodeSize(height);
	}

	public void setMaxVideoDuration(int durationMode) {

		if (durationMode == CameraConfig.DURATION_10S) {
			mMaxVideoDuration = 10000 + TIME_ERROR;
		} else if (durationMode == CameraConfig.DURATION_30S) {
			mMaxVideoDuration = 30000 + TIME_ERROR;
		} else if (durationMode == CameraConfig.DURATION_60S) {
			mMaxVideoDuration = 60000 + TIME_ERROR;
		} else if (durationMode == CameraConfig.DURATION_180S) {
			mMaxVideoDuration = 180000 + TIME_ERROR;
		}

		mLeftDuration = mMaxVideoDuration;
	}

	public void setMinVideoDuration(int minVideoDuration) {
		mMinVideoDuration = minVideoDuration;
	}

	public int getMaxVideoDuration() {
		return mMaxVideoDuration;
	}

	public int getLeftDuration() {
		return mLeftDuration;
	}

	public boolean canRecord() {
		return mLeftDuration >= mMinVideoDuration;
	}

	public void setLeftDuration(int duration) {
		mLeftDuration = duration;
	}

	/**
	 * 视频保存路径
	 **/
	public void setVideoPath(String dir) {
		mVideoFileDir = dir;
	}

	/**
	 * 设置音频录制是否可用
	 *
	 * @param enable 是否可用
	 */
	public void setAudioRecordEnable(boolean enable) {
		mAudioRecordEnable = enable;
		if (mPrepareState == STATE_PREPARED && mRecordState == RecordState.PREPARE && mMediaMuxerWrapper != null) {
			mMediaMuxerWrapper.setAudioEncoderEnable(mAudioRecordEnable);
		}
	}

	public void prepare() throws Exception {

		if (mVideoFileDir == null) {
			throw new NullPointerException("video file directory is null");
		}
		if (mPrepareState == STATE_PREPARING) {
			return;
		}

		mPrepareState = STATE_PREPARING;

		File file = Environment.getExternalStorageDirectory();
		if (file == null || !file.canWrite()) {
			return;//没有读写权限
		}

		if (mMediaVideoEncoder != null) {
			mMediaVideoEncoder.releaseAll();
			mMediaVideoEncoder = null;
		}
		if (mMediaAudioEncoder != null) {
			mMediaAudioEncoder.releaseAll();
			mMediaAudioEncoder = null;
		}
		if (mMediaMuxerWrapper != null) {
			mMediaMuxerWrapper = null;
		}

		mMediaMuxerWrapper = new MediaMuxerWrapper(mService, mVideoFileDir, new Date().getTime() + ".mp4");

		mMediaMuxerWrapper.setAudioEncoderEnable(mAudioRecordEnable);
		mVideoFilePath = mMediaMuxerWrapper.getOutputPath();

		// for video capturing
		mMediaVideoEncoder = new MediaVideoEncoder(mMediaMuxerWrapper, this, mVideoWidth, mVideoHeight, mOnInitListener);
		if (mAudioRecordEnable) {
			// for audio capturing
			mMediaAudioEncoder = new MediaAudioEncoder(mMediaMuxerWrapper, null);
		}

		mIsDestroy = false;
		if (mOnRecordListener != null) {
			mOnRecordListener.onPrepare(mMediaMuxerWrapper);
		}
	}

	/**
	 * 设置录制角度
	 * @param degree 录制角度
	 */
	public void setRecordDegree(int degree) {
		if (mMediaMuxerWrapper != null) {
			degree = (360 - degree) % 360;
			mMediaMuxerWrapper.setRecordDegree(degree);
		}
	}

	/**
	 * 开始录制
	 */
	public void startRecord() throws Exception {
		if (mPrepareState == STATE_UNPREPARE) {
			prepare();
//            throw new IllegalStateException("not prepare");
		} else if (mPrepareState == STATE_PREPARING) {
			// 还在准备中...因此需提前准备
			return;
		}

		if (mOnRecordListener != null) {
			mOnRecordListener.onStart(mMediaMuxerWrapper);
		}
	}

	/**
	 * 录制倒计时
	 */
	private void executeCountDownTimer() {

		mCountDownTimer = new CountDownTimer(mLeftDuration, TICK_TIME) {
			@Override
			public void onTick(long millisUntilFinished) {
				float progress = (mLeftDuration - millisUntilFinished) * 1f / mLeftDuration;
				if (mOnRecordListener != null) {
					mOnRecordListener.onProgressChange(progress);
				}
			}

			@Override
			public void onFinish() {
				if (mOnRecordListener != null) {
					mOnRecordListener.onProgressChange(1);
				}
				stopRecord();
				mCountDownIsFinish = true;
			}
		};
		mCountDownTimer.start();
		mRecordTime = System.currentTimeMillis();
		mCountDownIsFinish = false;
		mRecordState = RecordState.START;

	}

//	public void resumeRecord() {
//		if (mOnRecordListener != null) {
//			mOnRecordListener.onResume();
//		}
//	}
//
//	public void pauseRecord() {
//		if (mOnRecordListener != null) {
//			mOnRecordListener.onPause();
//		}
//	}

	/**
	 * 停止录制
	 */
	public void stopRecord() {
		if (mRecordState != RecordState.START || mCountDownIsFinish) {
			return;
		}
		if (mCountDownTimer != null) {
			mCountDownTimer.cancel();
			mCountDownTimer = null;
		}
		isValid = true;
		mDuration = 0;
		if (mRecordTime == -1) {
			isValid = false;
		} else {
			mDuration = System.currentTimeMillis() - mRecordTime;
			if (mDuration < mMinVideoDuration) {
				isValid = false;
			}
		}
		if (isValid) {
			if (mOnRecordListener != null) {
				mOnRecordListener.onStop(true);
			}
		} else {
			releaseAll(true);
			if (mOnRecordListener != null) {
				mOnRecordListener.onStop(false);
			}
		}
	}

	/**
	 * 是否可以停止录制
	 */
	public boolean canStop() {
		long recordTime = System.currentTimeMillis() - mRecordTime;

		return recordTime >= mMinVideoDuration && mLeftDuration - recordTime >= 1000;
	}

	private void releaseAll(boolean deleteFile) {
		if (mPrepareState == STATE_PREPARED) {
			if (mRecordState == RecordState.START && mCountDownTimer != null) {
				mCountDownTimer.cancel();
				mCountDownTimer = null;
			}

			if (mMediaMuxerWrapper != null) {
				mMediaMuxerWrapper.stopRecording();
			}
			mMediaMuxerWrapper = null;
			mPrepareState = STATE_UNPREPARE;
			mRecordState = RecordState.IDLE;
			mRecordTime = -1;

			if (deleteFile && mVideoFilePath != null) {
				deleteInvalidFile(mVideoFilePath);
				mVideoFilePath = null;
			}
		}
	}

	public void destroy() {
		mIsDestroy = true;
		releaseAll(false);
		if (mMediaMuxerWrapper != null) {
			mMediaMuxerWrapper.stopRecording();
		}
		mMediaMuxerWrapper = null;
		if (mMediaVideoEncoder != null) {
			mMediaVideoEncoder.releaseAll();
		}
		mMediaVideoEncoder = null;
		if (mMediaAudioEncoder != null) {
			mMediaAudioEncoder.releaseAll();
		}
		mMediaAudioEncoder = null;
		mPrepareState = STATE_UNPREPARE;
		mContext = null;
	}

	public void close() {
		mIsDestroy = true;
		releaseAll(false);
		if (mMediaMuxerWrapper != null) {
			mMediaMuxerWrapper.stopRecording();
		}
		mMediaMuxerWrapper = null;
		if (mMediaVideoEncoder != null) {
			mMediaVideoEncoder.close();
			mMediaVideoEncoder = null;
		}
		if (mMediaAudioEncoder != null) {
			mMediaAudioEncoder.close();
			mMediaAudioEncoder = null;
		}
		mPrepareState = STATE_UNPREPARE;
		mContext = null;
	}

	public void releaseService() {
		if (mService != null) {
			mService.shutdown();
			mService = null;
		}
	}

	private void deleteInvalidFile(String videoFilePath) {
		FileUtil.deleteSDFile(videoFilePath);
	}

	@Override
	public void onPrepared(MediaEncoder encoder) {
		mPrepareState = STATE_PREPARED;
		mRecordState = RecordState.PREPARE;

		if (mOnRecordListener != null) {
			mOnRecordListener.onPrepared();
		}
	}

	@Override
	public void onStarted(MediaEncoder encoder) {
		mRecordTime = -1;
		if (mHandler != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					executeCountDownTimer();
				}
			});
		}
	}

	@Override
	public void onResumed(MediaEncoder encoder) {

	}

	@Override
	public void onPaused(MediaEncoder encoder) {

	}

	@Override
	public void onStopped(MediaEncoder encoder) {
	}

	@Override
	public void onReleased(MediaEncoder encoder) {
		if (mIsDestroy) {
			return;
		}
		if (isValid) {
			releaseAll(false);
		}

		if (mHandler != null) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (isValid) {
						isValid = false;
						if (mOnRecordListener != null) {
							mOnRecordListener.onCompleted(mDuration, mVideoFilePath);
						}
					}
				}
			}, 100);
		}
	}

	/**
	 * @return false 权限没开，true正常录音
	 */
	public boolean isRecordVoiceEnable() {
		int min = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		if (min < READ_SIZE) {
			min = READ_SIZE;
		}
		boolean enable;
		AudioRecord mRecordInstance = null;
		try {
			mRecordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, min);

			mRecordInstance.startRecording();
			enable = true;
		} catch (Throwable e) {
			e.printStackTrace();
			enable = false;
		}
		if (enable) {

			byte[] tempBuffer = new byte[READ_SIZE];
			int bufferRead = mRecordInstance.read(tempBuffer, 0, READ_SIZE);

			if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
				enable = false;
			}
		} else {
			enable = false;
		}

		try {
			if (mRecordInstance != null) {
				mRecordInstance.release();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return enable;
	}

}
