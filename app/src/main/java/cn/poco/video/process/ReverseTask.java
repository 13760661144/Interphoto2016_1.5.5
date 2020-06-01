package cn.poco.video.process;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.UUID;

import cn.poco.album2.utils.SDCardUtils;
import cn.poco.tianutils.CommonUtils;
import cn.poco.video.NativeUtils;
import cn.poco.video.decode.DecodeUtils;
import cn.poco.video.encode.EncodeUtils;
import cn.poco.video.render.gles.L;
import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2017/9/28
 * 视频倒放
 */
public class ReverseTask implements Runnable {

	private static final String TAG = "ReverseTask";

	private static final int TIMEOUT_USEC = 10000;

	private String mVideoPath;
	private String mOutputPath;
	private String mMuxerOutputPath;

	private MediaCodec mDecoder;
	private MediaExtractor mExtractor;

	private MediaFormat mEncodeFormat;
	private String mMimeType;
	private int mColorFormat;
	private int mEncodeWidth;
	private int mEncodeHeight;
	private int mBitRate;
	private boolean isSetColorFormat;
	private MediaCodec mEncoder;
	private MediaMuxer mMuxer;
	private int mTrackIndex = -1;
	private boolean mMuxerStarted = false;

	private long mDuration;

	private String mTempDir;
	private LinkedList<DecodeData> mStack = new LinkedList<>();

	private Handler mMainHandler;
	private OnProcessListener mListener;

	private String mErrorMessage;

	public ReverseTask(String videoPath, String outputPath) {

		if (!FileUtils.isFileExist(videoPath)) {
			throw new IllegalArgumentException("the video is not exist");
		}

		mVideoPath = videoPath;
		mOutputPath = outputPath;

		mMainHandler = new Handler(Looper.getMainLooper());
	}

	public void setOnProcessListener(OnProcessListener listener) {
		mListener = listener;
	}

	private void prepare() {

		// 检查SD卡剩余空间
		long size = SDCardUtils.getSDCardAvailableSize();
		if (size < SDCardUtils.DEFAULT_SIZE) {
			mErrorMessage = "has no more space";
			return;
		}
		mTempDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "reverse";
		CommonUtils.MakeFolder(mTempDir);

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
			String mimeType = format.getString(MediaFormat.KEY_MIME);
			int width = format.getInteger(MediaFormat.KEY_WIDTH);
			if (format.containsKey("crop-left") && format.containsKey("crop-right")) {
				width = format.getInteger("crop-right") + 1 - format.getInteger("crop-left");
			}
			int height = format.getInteger(MediaFormat.KEY_HEIGHT);
			if (format.containsKey("crop-top") && format.containsKey("crop-bottom")) {
				height = format.getInteger("crop-bottom") + 1 - format.getInteger("crop-top");
			}

//			if (format.containsKey(MediaFormat.KEY_DURATION)) {
//				mDuration = format.getLong(MediaFormat.KEY_DURATION);
//			} else {
//				mDuration = (long)(NativeUtils.getDurationFromFile(mVideoPath) * 1000000);
//			}

//			mDuration = VideoUtils.getDurationFromVideo2(mVideoPath) * 1000;

			int bitRate;
			if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
				bitRate = format.getInteger(MediaFormat.KEY_BIT_RATE);
			} else {
				bitRate = (int)(0.25f * 25 * width * height);
			}

			mDecoder = MediaCodec.createDecoderByType(mimeType);
			mDecoder.configure(format, null, null, 0);

			mMimeType = mimeType;
			mEncodeWidth = width;
			mEncodeHeight = height;
			mBitRate = bitRate;

		} catch (IOException e) {
			e.printStackTrace();
			mErrorMessage = e.getMessage();

			if (mDecoder != null) {
				mDecoder.release();
				mDecoder = null;
			}

			releaseExtractor();
		}
	}

	@Override
	public void run() {

		Thread.currentThread().setName("ReverseTask");

		onStart();

		prepare();

		if (!TextUtils.isEmpty(mErrorMessage)) {
			onError(mErrorMessage);
			return;
		}

		decode();
		try {
			encode();
		} catch (IOException e) {
			e.printStackTrace();
			onError(e.getMessage());
			return;
		}

		saveVideoFile();

		FileUtils.delete(mMuxerOutputPath);
		FileUtils.deleteFiles(mTempDir, false);

		if (DecodeUtils.checkVideo(mOutputPath)) {
			onFinish();
		} else {
			onError("finish check error");
		}
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

	/**
	 * 解码
	 */
	private void decode() {

		mDecoder.start();

		ByteBuffer[] codecInputBuffers = mDecoder.getInputBuffers();
		ByteBuffer[] codecOutputBuffers = mDecoder.getOutputBuffers();

		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

		boolean inputDone = false;
		boolean outputDone = false;

		DecodeData data;

		try {

			while (!outputDone) {

				if (!inputDone) {
					int inputBufIndex = mDecoder.dequeueInputBuffer(TIMEOUT_USEC);
					if (inputBufIndex >= 0) {
						ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
						int sampleSize = mExtractor.readSampleData(dstBuf, 0);

						if (sampleSize < 0) {
							L.i(TAG, "saw input EOS.");
							inputDone = true;
							mDecoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						} else {
							long presentationTimeUs = mExtractor.getSampleTime();
							mDecoder.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, 0);
							mExtractor.advance();
						}
					}
				}

				int decoderStatus = mDecoder.dequeueOutputBuffer(info, TIMEOUT_USEC);

				if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
					// no saveVideoFile available yet
					L.d(TAG, "no saveVideoFile from decoder available");
				} else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					codecOutputBuffers = mDecoder.getOutputBuffers();
					L.i(TAG, "saveVideoFile buffers have changed.");
				} else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					MediaFormat newFormat = mDecoder.getOutputFormat();
					L.i(TAG, "saveVideoFile format has changed to " + newFormat);
				} else if (decoderStatus < 0) {
					throw new RuntimeException(
							"unexpected result from decoder.dequeueOutputBuffer: " +
									decoderStatus);
				} else { // decoderStatus >= 0

					// Simply ignore codec config buffers.
					if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
						L.i(TAG, "audio decoder: codec config buffer");
						info.size = 0;
					}

					if (info.size != 0) {

						ByteBuffer outBuf = codecOutputBuffers[decoderStatus];

						outBuf.position(info.offset);
						outBuf.limit(info.offset + info.size);

						if (!isSetColorFormat) {
							isSetColorFormat = true;
							MediaFormat decodeFormat = mDecoder.getOutputFormat();
							if (decodeFormat.containsKey("stride")) {
								mEncodeWidth = decodeFormat.getInteger("stride");
							}
							if (decodeFormat.containsKey("slice-height")) {
								mEncodeHeight = decodeFormat.getInteger("slice-height");
							}
							mColorFormat = decodeFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
						}

						data = new DecodeData();
						data.path = saveFrame(outBuf);
						mDuration = data.timestamp = info.presentationTimeUs;
						mStack.push(data);
					}

					mDecoder.releaseOutputBuffer(decoderStatus, false);

					if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
						L.i(TAG, "saw saveVideoFile EOS.");
						outputDone = true;
					}
				}
			}

		} finally {
			if (mDecoder != null) {
				mDecoder.stop();
				mDecoder.release();
				mDecoder = null;
			}

			releaseExtractor();
		}
	}

	private String saveFrame(ByteBuffer byteBuffer) {
		FileOutputStream fos = null;
		FileChannel fileChannel = null;
		String path = mTempDir + File.separator + UUID.randomUUID();

		try {
			fos = new FileOutputStream(path);
			fileChannel = fos.getChannel();
			fileChannel.write(byteBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.close(fileChannel);
			FileUtils.close(fos);
		}

		return path;
	}

	/**
	 * 编码
	 */
	private void encode() throws IOException {

		MediaCodecInfo info = EncodeUtils.selectCodec(mMimeType, mColorFormat);
		if (info != null) {
			mEncoder = MediaCodec.createByCodecName(info.getName());
		} else {
			mEncoder = MediaCodec.createEncoderByType(mMimeType);
		}

		mEncodeFormat = MediaFormat.createVideoFormat(mMimeType, mEncodeWidth, mEncodeHeight);
		mEncodeFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
		mEncodeFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
		mEncodeFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
		mEncodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
		mEncoder.configure(mEncodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		mMuxerOutputPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
		mMuxer = new MediaMuxer(mMuxerOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

		mEncoder.start();

		ByteBuffer[] codecInputBuffers = mEncoder.getInputBuffers();
		ByteBuffer[] codecOutputBuffers = mEncoder.getOutputBuffers();

		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

		boolean inputDone = false;
		boolean outputDone = false;

		DecodeData data;
		long presentationTimeUs = 0;
		long lastTimestamp = mDuration;

		try {

			while (!outputDone) {

				if (!inputDone) {
					int inputBufIndex = mEncoder.dequeueInputBuffer(TIMEOUT_USEC);
					if (inputBufIndex >= 0) {
						ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];

						if (mStack.isEmpty()) {
							inputDone = true;
							mEncoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						} else {
							data = mStack.pop();
							readFrame(data.path, dstBuf);
							mEncoder.queueInputBuffer(inputBufIndex, 0, dstBuf.limit(), presentationTimeUs, 0);
							presentationTimeUs += (lastTimestamp - data.timestamp);
							lastTimestamp = data.timestamp;
						}
					}
				}

				int encoderStatus = mEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
				if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
					// no saveVideoFile available yet
					L.d(TAG, "no saveVideoFile available, spinning to await EOS");
				} else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					// not expected for an encoder
					codecOutputBuffers = mEncoder.getOutputBuffers();
				} else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					// should happen before receiving buffers, and should only happen once
					if (mMuxerStarted) {
						throw new RuntimeException("format changed twice");
					}
					MediaFormat newFormat = mEncoder.getOutputFormat();
					L.d(TAG, "encoder saveVideoFile format changed: " + newFormat);

					// now that we have the Magic Goodies, start the muxer
					mTrackIndex = mMuxer.addTrack(newFormat);
					mMuxer.start();
					mMuxerStarted = true;
				} else if (encoderStatus < 0) {
					L.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
					// let's ignore it
				} else {
					ByteBuffer outBuf = codecOutputBuffers[encoderStatus];
					if (outBuf == null) {
						throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
					}

					if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
						// The codec config data was pulled out and fed to the muxer when we got
						// the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
						L.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
						bufferInfo.size = 0;
					}

					if (bufferInfo.size > 0 && bufferInfo.offset >= 0 && bufferInfo.presentationTimeUs >= 0) {
						if (!mMuxerStarted) {
							throw new RuntimeException("muxer hasn't started");
						}

						// adjust the ByteBuffer values to match BufferInfo (not needed?)
						outBuf.position(bufferInfo.offset);
						outBuf.limit(bufferInfo.offset + bufferInfo.size);

						mMuxer.writeSampleData(mTrackIndex, outBuf, bufferInfo);

						L.d(TAG, "sent " + bufferInfo.size + " bytes to muxer, ts=" + bufferInfo.presentationTimeUs);
					}

					mEncoder.releaseOutputBuffer(encoderStatus, false);

					if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {

						L.d(TAG, "end of stream reached");
						outputDone = true;
					}
				}
			}

		} finally {
			if (mEncoder != null) {
				mEncoder.stop();
				mEncoder.release();
				mEncoder = null;
			}

			if (mMuxer != null) {
				mMuxer.stop();
				mMuxer.release();
				mMuxer = null;
			}
		}
	}

	private void readFrame(String path, ByteBuffer byteBuffer) {
		FileInputStream fis = null;
		FileChannel fileChannel = null;

		try {
			fis = new FileInputStream(path);
			fileChannel = fis.getChannel();
			fileChannel.read(byteBuffer);
			byteBuffer.flip();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.close(fileChannel);
			FileUtils.close(fis);
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

	private void releaseExtractor() {
		if (mExtractor != null) {
			mExtractor.release();
			mExtractor = null;
		}
	}

	private static class DecodeData {
		public long timestamp;
		public String path;
	}
}
