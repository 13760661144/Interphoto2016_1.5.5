package cn.poco.video.save.decoder;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import cn.poco.video.NativeUtils;

/**
 * Created by: fwc
 * Date: 2017/12/27
 */
public class SoftDecoder implements IDecoder {

	private static final BufferInfo FINISH_INFO = new BufferInfo();

	/**
	 * 误差范围
	 */
	private static final float ERROR = 0.1f;

	private static int sVideoIndex = 0;

	private final String mVideoPath;
	private final float mOffset;
	private boolean isNeedSkip;

	private final int mVideoIndex;

	private final ArrayBlockingQueue<BufferInfo> mQueue;
	private final ArrayBlockingQueue<BufferInfo> mPool;

	private int mWidth = -1;
	private int mHeight = -1;

	private int mFrameCount = -1;
	private int mCurrentFrame = -1;

	private int mBufferSize;
	private byte[] mBytes;

	private byte[] mLastBytes;

	private volatile boolean isDecodeFinish;

	private float mLastTimestamp = -1;

	public SoftDecoder(String videoPath, float offset, int bufferSize) {
		File file = new File(videoPath);
		if (!file.exists() || !file.isFile()) {
			throw new IllegalArgumentException("the file is not exist");
		}

		if (!file.canRead()) {
			throw new RuntimeException("the file can not read");
		}

		mVideoPath = videoPath;
		mOffset = offset;

		isNeedSkip = mOffset > ERROR;

		mVideoIndex = (sVideoIndex++) % 3;

		mQueue = new ArrayBlockingQueue<>(bufferSize);
		mPool = new ArrayBlockingQueue<>(bufferSize);
	}

	private void prepare() {

		mFrameCount = NativeUtils.getFrameNumFromFile2(mVideoPath);
		if (mFrameCount <= 0) {
			throw new RuntimeException("the video is error");
		}
		mCurrentFrame = -1;

		mBufferSize = NativeUtils.getVideoRGBABufferSize(mVideoPath);
		if (mBufferSize <= 0) {
			throw new RuntimeException("the size of Buffer is error");
		}

		mBytes = new byte[mBufferSize];
		if (isNeedSkip) {
			mLastBytes = new byte[mBufferSize];
		}
		isDecodeFinish = false;
	}

	@Override
	public BufferInfo getBufferInfo() throws InterruptedException {
		if (isDecodeFinish && mQueue.isEmpty()) {
			return null;
		}

		BufferInfo info = mQueue.take();
		return info == FINISH_INFO ? null : info;
	}

	@Override
	public void recycle(BufferInfo info) {
		if (!isDecodeFinish && info != null && info.byteBuffer != null
				&& info.byteBuffer.capacity() == mBufferSize) {
			info.byteBuffer.clear();
			mPool.offer(info);
		}
	}

	@Override
	public void release() {
		isDecodeFinish = true;
		mQueue.clear();
		mPool.clear();
	}

	@Override
	public void run() {

		Thread.currentThread().setName("SoftDecoder");

		prepare();

		try {
			guardedRun();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			// 释放资源
			NativeUtils.cleanVideoGroupByIndex(mVideoIndex);
			mBufferSize = 0;
			mBytes = null;
		}
	}

	@SuppressWarnings("all")
	private void guardedRun() throws InterruptedException {

		Integer width = new Integer(-1);
		Integer height = new Integer(-1);
		float timestamp, offset = 0;

		while (!isDecodeFinish) {

			if (mCurrentFrame + 1 < mFrameCount) {
				timestamp = NativeUtils.getNextFrameRGBAWithTimeFromFile(mVideoPath, mVideoIndex, width, height, mBytes);
				if (timestamp == -1) {
					isDecodeFinish = true;
					break;
				}
				if (mWidth == -1 || mHeight == -1) {
					mWidth = width;
					mHeight = height;
				}

				mCurrentFrame++;

				if (isNeedSkip) {
					if (mOffset - timestamp > ERROR) {
						mLastTimestamp = timestamp;
						System.arraycopy(mBytes, 0, mLastBytes, 0, mBufferSize);
						continue;
					} else if (mLastTimestamp != -1) {
						// 考虑到低帧率情况
						putBuffer(mLastBytes, 0);
						mLastBytes = null;
						mLastTimestamp = -1;
						offset = mOffset;
					} else {
						offset = 0;
					}
				}

				isNeedSkip = false;
				timestamp -= offset;
				putBuffer(mBytes, timestamp);
			} else {
				isDecodeFinish = true;
				// 防止卡死
				mQueue.put(FINISH_INFO);
			}
		}
	}

	private void putBuffer(byte[] bytes, float timestamp) throws InterruptedException {

		BufferInfo bufferInfo = mPool.poll();
		if (bufferInfo == null) {
			bufferInfo = new BufferInfo();
			bufferInfo.byteBuffer = ByteBuffer.allocate(mBufferSize);
		}

		bufferInfo.byteBuffer.put(bytes);
		bufferInfo.byteBuffer.flip();
		bufferInfo.width = mWidth;
		bufferInfo.height = mHeight;
		bufferInfo.timestamp = (long) (timestamp * 1000000);

		mQueue.put(bufferInfo);
	}
}
