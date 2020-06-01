package cn.poco.video.clip;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.poco.video.NativeUtils;
import cn.poco.video.encode.EncodeUtils;
import cn.poco.video.render.gles.L;
import cn.poco.video.utils.AudioHandler;
import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2018/2/5
 */
public class ClipVideoCore {

	private static final String TAG = "ClipVideoCore";

	private static final int TIMEOUT_USEC = 10000;

	private final String mVideoPath;
	private final String mOutputPath;

	private final long mOffset;

	private MediaExtractor mExtractor;
	private MediaCodec mDecoder;

	private MediaMuxer mMuxer;
	private int mTrackIndex = -1;
	private boolean mMuxerStarted = false;

	private MediaCodec mEncoder;
	private String mMimeType;
	private long mDuration;
	private int mColorFormat;
	private int mEncodeWidth;
	private int mEncodeHeight;
	private int mFrameRate;
	private int mBitRate;
	private boolean isInitEncoder;

	private String mMuxerOutputPath;

	public ClipVideoCore(String videoPath, float offset, String outputPath) {
		mVideoPath = videoPath;
		mOutputPath = outputPath;

		mOffset = (long)(offset * 1000 * 1000 + 0.5f);
	}

	public boolean prepare() {
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
				return false;
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

			if (format.containsKey(MediaFormat.KEY_DURATION)) {
				mDuration = format.getLong(MediaFormat.KEY_DURATION);
			} else {
				mDuration = (long)(NativeUtils.getDurationFromFile(mVideoPath) * 1000000);
			}

			int frameRate;
			if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
				try {
					frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
				} catch (Exception e) {
					e.printStackTrace();
					frameRate = (int)format.getFloat(MediaFormat.KEY_FRAME_RATE);
				}
			} else {
				frameRate = (int)NativeUtils.getFPSFromFile(mVideoPath);
			}

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
			mFrameRate = frameRate;
			mBitRate = bitRate;

		} catch (IOException e) {
			e.printStackTrace();

			if (mDecoder != null) {
				mDecoder.release();
				mDecoder = null;
			}

			return false;
		}

		return true;
	}

	public void start() {

		mDecoder.start();

		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

		boolean isDecodeInputDone = false;
		boolean isDecodeOutputDone = false;

//		boolean isEncodeInputDone = false;
		boolean isEncodeOutputDone = false;

		boolean isStartEncode = false;

		try {

			while (!isEncodeOutputDone) {
				if (!isDecodeInputDone) {
					int inputBufIndex = mDecoder.dequeueInputBuffer(TIMEOUT_USEC);
					if (inputBufIndex >= 0) {
						ByteBuffer dstBuf = mDecoder.getInputBuffers()[inputBufIndex];
						int sampleSize = mExtractor.readSampleData(dstBuf, 0);

						if (sampleSize < 0) {
							L.i(TAG, "saw input EOS.");
							isDecodeInputDone = true;
							mDecoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						} else {
							mDecoder.queueInputBuffer(inputBufIndex, 0, sampleSize, mExtractor.getSampleTime(), 0);
							mExtractor.advance();
						}
					}
				}

				if (!isDecodeOutputDone) {
					int decoderStatus = mDecoder.dequeueOutputBuffer(info, TIMEOUT_USEC);

					if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
						// no saveVideoFile available yet
						L.d(TAG, "no saveVideoFile from decoder available");
					} else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
						L.i(TAG, "saveVideoFile buffers have changed.");
					} else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
						MediaFormat newFormat = mDecoder.getOutputFormat();
						L.i(TAG, "saveVideoFile format has changed to " + newFormat);
					} else if (decoderStatus < 0) {
						throw new RuntimeException("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
					} else { // decoderStatus >= 0

						// Simply ignore codec config buffers.
						if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
							L.i(TAG, "audio decoder: codec config buffer");
							info.size = 0;
						}

						if (info.size != 0) {

							ByteBuffer outBuf = mDecoder.getOutputBuffers()[decoderStatus];

							outBuf.position(info.offset);
							outBuf.limit(info.offset + info.size);

							if (!isInitEncoder) {
								isInitEncoder = true;
								MediaFormat decodeFormat = mDecoder.getOutputFormat();
								if (decodeFormat.containsKey("stride")) {
									mEncodeWidth = decodeFormat.getInteger("stride");
								}
								if (decodeFormat.containsKey("slice-height")) {
									mEncodeHeight = decodeFormat.getInteger("slice-height");
								}
								mColorFormat = decodeFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT);
								initEncoder();
							}

							if (info.presentationTimeUs >= mOffset) {
								int inputBufIndex = mEncoder.dequeueInputBuffer(TIMEOUT_USEC);
								if (inputBufIndex >= 0) {
									ByteBuffer dstBuf = mEncoder.getInputBuffers()[inputBufIndex];

									dstBuf.put(outBuf);
									dstBuf.flip();
									mEncoder.queueInputBuffer(inputBufIndex, 0, info.size, info.presentationTimeUs - mOffset, 0);
									isStartEncode = true;
								}
							}
						}

						mDecoder.releaseOutputBuffer(decoderStatus, false);

						if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
							L.i(TAG, "saw saveVideoFile EOS.");
							isDecodeOutputDone = true;
							int inputBufIndex = mEncoder.dequeueInputBuffer(TIMEOUT_USEC);
							mEncoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						}
					}
				}

				if (isInitEncoder && isStartEncode) {
					int encoderStatus = mEncoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
					if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
						// no saveVideoFile available yet
						L.d(TAG, "no saveVideoFile available, spinning to await EOS");
					} else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
						// not expected for an encoder
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
						ByteBuffer outBuf = mEncoder.getOutputBuffers()[encoderStatus];
						if (outBuf == null) {
							throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
						}

						if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
							// The codec config data was pulled out and fed to the muxer when we got
							// the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
							L.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
							info.size = 0;
						}

						if (info.size != 0) {
							if (!mMuxerStarted) {
								throw new RuntimeException("muxer hasn't started");
							}

							// adjust the ByteBuffer values to match BufferInfo (not needed?)
							outBuf.position(info.offset);
							outBuf.limit(info.offset + info.size);

							mMuxer.writeSampleData(mTrackIndex, outBuf, info);

							L.d(TAG, "sent " + info.size + " bytes to muxer, ts=" + info.presentationTimeUs);
						}

						mEncoder.releaseOutputBuffer(encoderStatus, false);

						if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {

							L.d(TAG, "end of stream reached");
							isEncodeOutputDone = true;
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			releaseDecoder();
			releaseExtractor();

			releaseEncoder();
			try {
				releaseMuxer();
			} catch (Exception e) {
				e.printStackTrace();
				FileUtils.delete(mMuxerOutputPath);
			}
		}

		if (mMuxerOutputPath == null) {
			cn.poco.album2.utils.FileUtils.copyFile(mVideoPath, mOutputPath);
			return;
		}

		File file = new File(mMuxerOutputPath);
		if (!file.exists() || file.length() <= 0) {
			cn.poco.album2.utils.FileUtils.copyFile(mVideoPath, mOutputPath);
		} else {
			boolean outputSuccess = false;

			String audioPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
			int result = NativeUtils.getAACFromVideo(mVideoPath, audioPath);
			file = new File(audioPath);
			if (result >= 0 && file.exists() && file.length() > 0) {
				audioPath = AudioHandler.clipAudio(audioPath, mOffset / 1000, mDuration / 1000);
				int rotate = NativeUtils.getRotateAngleFromFile(mVideoPath);
				if (rotate != 0) {
					result = NativeUtils.setMp4Rotation(mMuxerOutputPath, audioPath, mOutputPath, String.valueOf(rotate), 0);
				} else {
					result = NativeUtils.muxerMp4(mMuxerOutputPath, audioPath, mOutputPath, 0);
				}

				FileUtils.delete(audioPath);

				outputSuccess = result >= 0;
			}

			if (!outputSuccess) {
				saveVideoFile();
			}
		}

		FileUtils.delete(mMuxerOutputPath);
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

	private void initEncoder() throws IOException {
		MediaCodecInfo info = EncodeUtils.selectCodec(mMimeType, mColorFormat);
		if (info != null) {
			mEncoder = MediaCodec.createByCodecName(info.getName());
		} else {
			mEncoder = MediaCodec.createEncoderByType(mMimeType);
		}

		MediaFormat format = MediaFormat.createVideoFormat(mMimeType, mEncodeWidth, mEncodeHeight);
		format.setInteger(MediaFormat.KEY_COLOR_FORMAT, mColorFormat);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
		format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
		mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		mMuxerOutputPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
		mMuxer = new MediaMuxer(mMuxerOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

		mEncoder.start();
	}

	private void releaseExtractor() {
		if (mExtractor != null) {
			mExtractor.release();
			mExtractor = null;
		}
	}

	private void releaseDecoder() {
		if (mDecoder != null) {
			mDecoder.stop();
			mDecoder.release();
			mDecoder = null;
		}
	}

	private void releaseMuxer() {
		if (mMuxer != null) {
			mMuxer.stop();
			mMuxer.release();
			mMuxer = null;
		}
	}

	private void releaseEncoder() {
		if (mEncoder != null) {
			mEncoder.stop();
			mEncoder.release();
			mEncoder = null;
		}
	}
}
