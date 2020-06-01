package cn.poco.video.encode;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.poco.video.render.gles.L;
import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2018/1/10
 */
public class AudioEncoderCore {

	private static final String TAG = "AudioEncoderCore";

	private static final int TIMEOUT_USEC = 10000;

//	private static final int SAMPLE_SIZE = 1024;

	private MediaCodec mEncoder;

	private final String mSourcePath;
	private final String mOutputPath;;

	public AudioEncoderCore(EncodeConfig config) throws IOException {

		mSourcePath = config.sourcePath;
		mOutputPath = config.outputPath;

		MediaFormat format = MediaFormat.createAudioFormat(config.mimeType, config.sampleRate, config.channelCount);
		format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
		format.setInteger(MediaFormat.KEY_CHANNEL_MASK, config.channelMode);
		format.setInteger(MediaFormat.KEY_BIT_RATE, config.bitRate);

		mEncoder = MediaCodec.createEncoderByType(config.mimeType);
		mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		mEncoder.start();
	}

	public void start() {

		ByteBuffer[] codecInputBuffers = mEncoder.getInputBuffers();
		ByteBuffer[] codecOutputBuffers = mEncoder.getOutputBuffers();

		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//		byte[] sample = new byte[SAMPLE_SIZE];
		byte[] sample = null;
		int sampleSize;

		boolean inputDone = false;
		boolean outputDone = false;

		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {

			fis = new FileInputStream(mSourcePath);
			fos = new FileOutputStream(mOutputPath);

			while (!outputDone) {

				if (!inputDone) {
					int inputBufIndex = mEncoder.dequeueInputBuffer(TIMEOUT_USEC);
					if (inputBufIndex >= 0) {
						ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];

						if (sample == null) {
							sample = new byte[dstBuf.capacity()];
						}
						sampleSize = fis.read(sample);
						dstBuf.put(sample);
						dstBuf.flip();

						if (sampleSize < 0) {
							L.i(TAG, "saw input EOS.");
							inputDone = true;
							mEncoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						} else {
							mEncoder.queueInputBuffer(inputBufIndex, 0, sampleSize, System.nanoTime() / 1000, 0);
						}
					}
				}

				int decoderStatus = mEncoder.dequeueOutputBuffer(info, TIMEOUT_USEC);

				if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
					// no output available yet
					L.d(TAG, "no output from decoder available");
				} else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					codecOutputBuffers = mEncoder.getOutputBuffers();
					L.i(TAG, "output buffers have changed.");
				} else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					MediaFormat newFormat = mEncoder.getOutputFormat();
					L.i(TAG, "output format has changed to " + newFormat);
				} else if (decoderStatus < 0) {
					throw new RuntimeException("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
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
						byte[] data = new byte[info.size + 7];
						outBuf.get(data, 7, info.size);
						addADTStoPacket(data, data.length);
						fos.write(data);
					}

					mEncoder.releaseOutputBuffer(decoderStatus, false);

					if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
						L.i(TAG, "saw output EOS.");
						outputDone = true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.close(fis);
			FileUtils.close(fos);

			release();
		}
	}

	private void release() {
		if (mEncoder != null) {
			mEncoder.stop();
			mEncoder.release();
			mEncoder = null;
		}
	}

	/**
	 * 给编码出的aac裸流添加adts头字段
	 * @param packet 要空出前7个字节，否则会搞乱数据
	 * @param packetLen 数据长度
	 */
	private void addADTStoPacket(byte[] packet, int packetLen) {
		int profile = 2;  //AAC LC
		int freqIdx = 4;  //44.1KHz
		int chanCfg = 2;  //CPE
		packet[0] = (byte)0xFF;
		packet[1] = (byte)0xF9;
		packet[2] = (byte)(((profile-1)<<6) + (freqIdx<<2) +(chanCfg>>2));
		packet[3] = (byte)(((chanCfg&3)<<6) + (packetLen>>11));
		packet[4] = (byte)((packetLen&0x7FF) >> 3);
		packet[5] = (byte)(((packetLen&7)<<5) + 0x1F);
		packet[6] = (byte)0xFC;
	}

	public static class EncodeConfig {

		private static final String MIME_TYPE = "audio/mp4a-latm";
		private static final int SAMPLE_RATE = 44100;
		private static final int CHANNEL_COUNT = 1;
		private static final int CHANNEL_MODE = AudioFormat.CHANNEL_IN_MONO;
		private static final int BIT_RATE = 128000;

		public final String sourcePath;
		public final String outputPath;

		public String mimeType = MIME_TYPE;
		public int sampleRate = SAMPLE_RATE;
		public int channelCount = CHANNEL_COUNT;
		public int channelMode = CHANNEL_MODE;
		public int bitRate = BIT_RATE;

		public EncodeConfig(String sourcePath, String outputPath) {
			this.sourcePath = sourcePath;
			this.outputPath = outputPath;
		}
	}
}
