package cn.poco.video.clip;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.poco.video.NativeUtils;
import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2018/2/1
 */
public class ClipMediaCore {

	private final String mVideoPath;
	private final String mOutputPath;
	private final long mStartTime;
	private final long mEndTime;

	private final String mMuxerPath;

	private MediaExtractor mExtractor;
	private int mVideoTrack = -1;
	private int mAudioTrack = -1;

	private MediaMuxer mMuxer;
	private int mMuxerVideoTrack = -1;
	private int mMuxerAudioTrack = -1;

	private int mBufferSize = 0;

	public ClipMediaCore(String videoPath, String outputPath, long startTime, long endTime) {
		mVideoPath = videoPath;
		mOutputPath = outputPath;
		mStartTime = startTime * 1000;
		mEndTime = endTime * 1000;

		mMuxerPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
	}

	public void prepare() {
		try {
			mExtractor = new MediaExtractor();
			mExtractor.setDataSource(mVideoPath);

			mMuxer = new MediaMuxer(mMuxerPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

			for (int i = 0; i < mExtractor.getTrackCount(); i++) {
				MediaFormat format = mExtractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				if (mime.startsWith("video/")) {
					mVideoTrack = i;
					if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
						mBufferSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
					} else {
						mBufferSize = NativeUtils.getVideoRGBABufferSize(mVideoPath) / 10;
					}

					mMuxerVideoTrack = mMuxer.addTrack(format);
				} else if (mime.startsWith("audio/")) {
					mAudioTrack = i;
					mMuxerAudioTrack = mMuxer.addTrack(format);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();

			releaseExtractor();
			releaseMuxer();
		}

		if (mVideoTrack == -1 && mAudioTrack == -1) {
			throw new RuntimeException("not found the track of video or audio");
		}
	}

	/**
	 * 开始裁剪
	 * @return 实际裁剪开始的位置
	 */
	public long start() {

		long realStartTime = -1;

		if (mBufferSize == 0) {
			mBufferSize = 4 * 1024;
		}
		ByteBuffer byteBuffer = ByteBuffer.allocate(mBufferSize);
		int sampleSize;
		long sampleTime;
		boolean isFinish = false;
		long lastSampleTime = -1;

		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		mMuxer.start();

		if (mVideoTrack != -1) {
			mExtractor.selectTrack(mVideoTrack);
			mExtractor.seekTo(mStartTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
			realStartTime = mExtractor.getSampleTime() / 1000;
			while (!isFinish) {
				sampleSize = mExtractor.readSampleData(byteBuffer, 0);
				sampleTime = mExtractor.getSampleTime();
				if (sampleSize < 0 || sampleTime >= mEndTime) {
					byteBuffer.clear();
					isFinish = true;
					bufferInfo.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				} else {
					bufferInfo.set(0, sampleSize, sampleTime, mExtractor.getSampleFlags());
				}

				if (lastSampleTime < sampleTime || isFinish) {
					mMuxer.writeSampleData(mMuxerVideoTrack, byteBuffer, bufferInfo);
					lastSampleTime = sampleTime;
				}

				byteBuffer.clear();
				mExtractor.advance();
			}
			mExtractor.unselectTrack(mVideoTrack);
		}

		if (mAudioTrack != -1) {
			mExtractor.selectTrack(mAudioTrack);
			mExtractor.seekTo(mStartTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
			if (realStartTime == -1) {
				realStartTime = mExtractor.getSampleTime() / 1000;
			}
			lastSampleTime = -1;
			isFinish = false;
			while (!isFinish) {
				sampleSize = mExtractor.readSampleData(byteBuffer, 0);
				sampleTime = mExtractor.getSampleTime();
				if (sampleSize < 0 || sampleTime >= mEndTime) {
					byteBuffer.clear();
					isFinish = true;
					bufferInfo.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
				} else {
					bufferInfo.set(0, sampleSize, sampleTime, mExtractor.getSampleFlags());
				}

				if (lastSampleTime < sampleTime || isFinish) {
					mMuxer.writeSampleData(mMuxerAudioTrack, byteBuffer, bufferInfo);
					lastSampleTime = sampleTime;
				}

				byteBuffer.clear();
				mExtractor.advance();
			}
			mExtractor.unselectTrack(mAudioTrack);
		}

		releaseExtractor();

		mMuxer.stop();
		mMuxer.release();

		int rotate = NativeUtils.getRotateAngleFromFile(mVideoPath);
		if (rotate != 0) {
			String audioPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
			int result = NativeUtils.getAACFromVideo(mMuxerPath, audioPath);
			File file = new File(audioPath);
			if (result < 0 || !file.exists() || file.length() < 0) {
				FileUtils.delete(audioPath);
				audioPath = "";
			}
			result = NativeUtils.setMp4Rotation(mMuxerPath, audioPath, mOutputPath, String.valueOf(rotate), 0);
			if (result < 0) {
				FileUtils.renameOrCopy(mMuxerPath, mOutputPath);
			}
		} else {
			FileUtils.renameOrCopy(mMuxerPath, mOutputPath);
		}

		return realStartTime;
	}

	private void releaseExtractor() {
		if (mExtractor != null) {
			mExtractor.release();
			mExtractor = null;
		}
	}

	private void releaseMuxer() {
		if (mMuxer != null) {
			mMuxer.release();
			mMuxer = null;
		}
	}
}
