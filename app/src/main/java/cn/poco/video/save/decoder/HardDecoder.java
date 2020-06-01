//package cn.poco.video.save.decoder;
//
//import android.media.MediaCodec;
//import android.media.MediaExtractor;
//import android.media.MediaFormat;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.concurrent.ArrayBlockingQueue;
//
///**
// * Created by: fwc
// * Date: 2018/1/11
// */
//public class HardDecoder implements IDecoder, Runnable {
//
//	private static final int TIMEOUT_USEC = 10000;
//
//	/**
//	 * 误差范围
//	 */
//	private static final float ERROR = 0.1f;
//
//	private final String mVideoPath;
//	private final float mOffset;
//	private boolean isNeedSkip;
//
//	private final ArrayBlockingQueue<BufferInfo> mQueue;
//	private final ArrayBlockingQueue<BufferInfo> mPool;
//
//	private MediaExtractor mExtractor;
//	private MediaCodec mDecoder;
//
//	private int mWidth = -1;
//	private int mHeight = -1;
//
//	private boolean isError;
//
//	private long mLastTimestamp = -1;
//
//	private byte[] mBytes;
//	private int mBufferSize;
//
//	private byte[] mLastBytes;
//
//	private boolean isDecodeFinish;
//
//	public HardDecoder(String videoPath, float offset, int bufferSize) {
//		File file = new File(videoPath);
//		if (!file.exists() || !file.isFile()) {
//			throw new IllegalArgumentException("the file is not exist");
//		}
//
//		if (!file.canRead()) {
//			throw new RuntimeException("the file can not read");
//		}
//
//		mVideoPath = videoPath;
//		mOffset = offset;
//
//		isNeedSkip = mOffset > ERROR;
//
//		mQueue = new ArrayBlockingQueue<>(bufferSize);
//		mPool = new ArrayBlockingQueue<>(bufferSize);
//	}
//
//	private void prepare() throws IOException {
//
//		mExtractor = new MediaExtractor();
//		mExtractor.setDataSource(mVideoPath);
//		int trackIndex = -1;
//		for (int i = 0; i < mExtractor.getTrackCount(); i++) {
//			MediaFormat format = mExtractor.getTrackFormat(i);
//			String mime = format.getString(MediaFormat.KEY_MIME);
//			if (mime.startsWith("video/")) {
//				trackIndex = i;
//				break;
//			}
//		}
//
//		if (trackIndex < 0) {
//			releaseExtractor();
//			isError = true;
//			return;
//		}
//
//		MediaFormat format = mExtractor.getTrackFormat(trackIndex);
//		mExtractor.selectTrack(trackIndex);
//		String mimeType = format.getString(MediaFormat.KEY_MIME);
//		int width = format.getInteger(MediaFormat.KEY_WIDTH);
//		if (format.containsKey("crop-left") && format.containsKey("crop-right")) {
//			width = format.getInteger("crop-right") + 1 - format.getInteger("crop-left");
//		}
//		int height = format.getInteger(MediaFormat.KEY_HEIGHT);
//		if (format.containsKey("crop-top") && format.containsKey("crop-bottom")) {
//			height = format.getInteger("crop-bottom") + 1 - format.getInteger("crop-top");
//		}
//
//		mWidth = width;
//		mHeight = height;
//
//		mDecoder = MediaCodec.createDecoderByType(mimeType);
//		mDecoder.configure(format, null, null, 0);
//		mDecoder.start();
//	}
//
//	@Override
//	public void run() {
//
//		Thread.currentThread().setName("HardDecoder");
//
//		try {
//			prepare();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
//
//		if (isError) {
//			return;
//		}
//
//		try {
//			guardedRun();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} finally {
//			releaseExtractor();
//			releaseDecoder();
//		}
//	}
//
//	private void guardedRun() throws InterruptedException {
//
//		ByteBuffer[] codecInputBuffers = mDecoder.getInputBuffers();
//		ByteBuffer[] codecOutputBuffers = mDecoder.getOutputBuffers();
//
//		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//
//		boolean inputDone = false;
//		boolean outputDone = false;
//
//		long offset = 0;
//
//		while (!outputDone) {
//
//			if (!inputDone) {
//				int inputBufIndex = mDecoder.dequeueInputBuffer(TIMEOUT_USEC);
//				if (inputBufIndex >= 0) {
//					ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
//					int sampleSize = mExtractor.readSampleData(dstBuf, 0);
//
//					if (sampleSize < 0) {
//						inputDone = true;
//						mDecoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//					} else {
//						long presentationTimeUs = mExtractor.getSampleTime();
//						mDecoder.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, 0);
//						mExtractor.advance();
//					}
//				}
//			}
//
//			int decoderStatus = mDecoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
//
//			if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//
//			} else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//
//			} else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//
//			} else if (decoderStatus < 0) {
//				throw new RuntimeException("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
//			} else { // decoderStatus >= 0
//
//				// Simply ignore codec config buffers.
//				if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//					info.size = 0;
//				}
//
//				if (info.size != 0) {
//
//					ByteBuffer outBuf = codecOutputBuffers[decoderStatus];
//
//					outBuf.position(info.offset);
//					outBuf.limit(info.offset + info.size);
//					if (mBytes == null) {
//						// TODO: 2018/1/11 不确定每帧的大小都一样
//						mBufferSize = info.size;
//						mBytes = new byte[info.size];
//					}
//					outBuf.get(mBytes);
//
//					if (isNeedSkip) {
//						if (mOffset - info.presentationTimeUs / 1000000f > ERROR) {
//							mLastTimestamp = info.presentationTimeUs;
//							if (mLastBytes == null) {
//								mLastBytes = new byte[mBufferSize];
//							}
//							System.arraycopy(mBytes, 0, mLastBytes, 0, mBufferSize);
//							continue;
//						} else if (mLastTimestamp != -1) {
//							// 考虑到低帧率情况
//							putBuffer(mLastBytes, 0);
//							mLastBytes = null;
//							mLastTimestamp = -1;
//							offset = (long)(mOffset * 1000000);
//						} else {
//							offset = 0;
//						}
//					}
//
//					isNeedSkip = false;
//					putBuffer(mBytes, info.presentationTimeUs - offset);
//				}
//
//				mDecoder.releaseOutputBuffer(decoderStatus, false);
//
//				if (isDecodeFinish || (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//					outputDone = true;
//				}
//			}
//		}
//
//		isDecodeFinish = true;
//	}
//
//	private void putBuffer(byte[] bytes, long timestamp) throws InterruptedException {
//
//		BufferInfo bufferInfo = mPool.poll();
//		if (bufferInfo == null) {
//			bufferInfo = new BufferInfo();
//			bufferInfo.byteBuffer = ByteBuffer.allocate(bytes.length);
//		}
//
//		bufferInfo.byteBuffer.put(bytes);
//		bufferInfo.byteBuffer.flip();
//		bufferInfo.width = mWidth;
//		bufferInfo.height = mHeight;
//		bufferInfo.timestamp = timestamp;
//
//		mQueue.put(bufferInfo);
//	}
//
//	@Override
//	public BufferInfo getBufferInfo() throws InterruptedException {
//		if (isDecodeFinish && mQueue.isEmpty()) {
//			return null;
//		}
//
//		return mQueue.take();
//	}
//
//	@Override
//	public void recycle(BufferInfo info) {
//		if (!isDecodeFinish && info != null && info.byteBuffer != null
//				&& info.byteBuffer.capacity() == mBufferSize) {
//			info.byteBuffer.clear();
//			mPool.offer(info);
//		}
//	}
//
//	@Override
//	public void release() {
//		isDecodeFinish = true;
//		mQueue.clear();
//		mPool.clear();
//	}
//
//	private void releaseExtractor() {
//		if (mExtractor != null) {
//			mExtractor.release();
//			mExtractor = null;
//		}
//	}
//
//	private void releaseDecoder() {
//		if (mDecoder != null) {
//			mDecoder.stop();
//			mDecoder.release();
//			mDecoder = null;
//		}
//	}
//}
