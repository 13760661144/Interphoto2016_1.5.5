package cn.poco.video.process;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.poco.audio.AudioUtils;
import cn.poco.video.NativeUtils;
import cn.poco.video.utils.AudioHandler;
import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2017/9/28
 * 视频加速
 */
public class SpeedTask implements Runnable {

	private String mVideoPath;
	private String mOutputPath;
	private String mMuxerOutputPath;

	private float mSpeedRatio;
	private long mDuration;

	private MediaExtractor mExtractor;
	private MediaMuxer mMuxer;
	private int mTrackIndex = -1;

	private int mBufferSize;

	private Handler mMainHandler;
	private OnProcessListener mListener;

	private String mErrorMessage;

	public SpeedTask(String videoPath, float speedRatio, String outputPath) {

		if (!FileUtils.isFileExist(videoPath)) {
			throw new IllegalArgumentException("the video is not exist");
		}

		if (speedRatio <= 0) {
			throw new IllegalArgumentException("the speedRatio is not correct");
		}

		mVideoPath = videoPath;
		mSpeedRatio = speedRatio;
		mOutputPath = outputPath;

		mMainHandler = new Handler(Looper.getMainLooper());
	}

	public void setOnProcessListener(OnProcessListener listener) {
		mListener = listener;
	}

	private void prepare() {
		try {
			mExtractor = new MediaExtractor();
			mExtractor.setDataSource(mVideoPath);
			int trackIndex = -1;
			for (int i = 0; i < mExtractor.getTrackCount(); i++) {
				MediaFormat format = mExtractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				if (mime.startsWith("video/")) {
					trackIndex = i;
					break;
				}
			}

			if (trackIndex < 0) {
				releaseExtractor();
				mErrorMessage = "has not found video track";
				return;
			}

			MediaFormat format = mExtractor.getTrackFormat(trackIndex);
			mExtractor.selectTrack(trackIndex);

			if (format.containsKey(MediaFormat.KEY_DURATION)) {
				mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000;
			} else {
				mDuration = (long)(NativeUtils.getDurationFromFile(mVideoPath) * 1000);
			}

			if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
				mBufferSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
			} else {
				mBufferSize = NativeUtils.getVideoRGBABufferSize(mVideoPath) / 10;
			}

			mDuration = (long)(mDuration / mSpeedRatio + 1);

			mMuxerOutputPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
			mMuxer = new MediaMuxer(mMuxerOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

			mTrackIndex = mMuxer.addTrack(format);

		} catch (IOException e) {
			e.printStackTrace();
			mErrorMessage = e.getMessage();

			releaseExtractor();

			if (mMuxer != null) {
				mMuxer.release();
				mMuxer = null;
			}
		}
	}

	@Override
	public void run() {

		Thread.currentThread().setName("SpeedTask");

		onStart();

		prepare();

		if (!TextUtils.isEmpty(mErrorMessage)) {
			onError(mErrorMessage);
			return;
		}

		ByteBuffer byteBuffer = ByteBuffer.allocate(mBufferSize);
		int sampleSize;
		long sampleTime, lastSampleTime = -1;
		boolean isFinish = false;

		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

		mMuxer.start();

		while (!isFinish) {
			sampleSize = mExtractor.readSampleData(byteBuffer, 0);
			sampleTime = mExtractor.getSampleTime();
			if (sampleSize < 0) {
				byteBuffer.clear();
				isFinish = true;
				bufferInfo.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
			} else {
				sampleTime = (long)(sampleTime / mSpeedRatio);
				bufferInfo.set(0, sampleSize, sampleTime, mExtractor.getSampleFlags());
			}

			if (lastSampleTime < sampleTime) {
				mMuxer.writeSampleData(mTrackIndex, byteBuffer, bufferInfo);
				lastSampleTime = sampleTime;
			}

			byteBuffer.clear();
			mExtractor.advance();
		}

		releaseExtractor();

		mMuxer.stop();
		mMuxer.release();

		boolean outputSuccess = false;

		String audioPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
		int result = NativeUtils.getAACFromVideo(mVideoPath, audioPath);
		File file = new File(audioPath);
		if (result >= 0 && file.exists() && file.length() > 0) {
			String speedAudioPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
			boolean speedResult = AudioUtils.changeAacSound(audioPath, speedAudioPath, 1, 0, mSpeedRatio, AudioUtils.AUDIO_TYPE_AAC);
			if (!speedResult) {
				FileUtils.delete(speedAudioPath);
				speedAudioPath = audioPath;
			}

			speedAudioPath = AudioHandler.ensureDuration(speedAudioPath, mDuration);
			int rotate = NativeUtils.getRotateAngleFromFile(mVideoPath);
			if (rotate != 0) {
				result = NativeUtils.setMp4Rotation(mMuxerOutputPath, speedAudioPath, mOutputPath, String.valueOf(rotate), 0);
			} else {
				result = NativeUtils.muxerMp4(mMuxerOutputPath, speedAudioPath, mOutputPath, 0);
			}

			FileUtils.delete(speedAudioPath);

			outputSuccess = result >= 0;
		}

		if (!outputSuccess) {
			saveVideoFile();
		}

		FileUtils.delete(audioPath);
		FileUtils.delete(mMuxerOutputPath);

		onFinish();
	}

	private void saveVideoFile() {
		int rotate = NativeUtils.getRotateAngleFromFile(mVideoPath);
		if (rotate != 0) {
			int result = NativeUtils.setMp4Rotation(mMuxerOutputPath, "", mOutputPath, String.valueOf(rotate), 0);
			if (result >= 0) {
				return;
			}
		}

		FileUtils.delete(mOutputPath);
		FileUtils.renameOrCopy(mMuxerOutputPath, mOutputPath);
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

	private void releaseExtractor() {
		if (mExtractor != null) {
			mExtractor.release();
			mExtractor = null;
		}
	}
}
