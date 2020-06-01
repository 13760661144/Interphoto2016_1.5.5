package cn.poco.video.save.player;

import cn.poco.video.process.ThreadPool;
import cn.poco.video.save.decoder.IDecoder;
import cn.poco.video.save.decoder.SoftDecoder;

/**
 * Created by: fwc
 * Date: 2017/12/27
 */
public class SoftPlayer {

	private static final int IDLE = 0;
	private static final int PREPARED = 1;
	private static final int START = 2;
	private static final int RELEASE = 4;

	private String mDataSource;
	private float mOffset;
	private SoftTexture mSoftTexture;

	private SoftDecoder mDecoder;

	private long mDuration;
	private int mRotation;

	private int mState = IDLE;

	private ThreadPool mThreadPool;

	public SoftPlayer() {
		mThreadPool = ThreadPool.getInstance();
	}

	public void setDataSource(String dataSource, float offset) {

		if (mState != IDLE) {
			throw new IllegalStateException();
		}

		mDataSource = dataSource;
		mOffset = offset;
	}

	public void setDuration(long duration) {
		mDuration = duration - (long)(mOffset * 1000);
	}

	public void setRotation(int rotation) {
		mRotation = rotation;
	}

	public void setSoftTexture(SoftTexture softTexture) {

		if (mState != IDLE) {
			throw new IllegalStateException();
		}

		if (softTexture == null) {
			throw new IllegalArgumentException("the softTexture is null");
		}

		mSoftTexture = softTexture;
	}

	public void prepare() {

		if (mState != IDLE) {
			throw new IllegalStateException();
		}

		if (mDataSource == null) {
			throw new RuntimeException("the mDataSource is null");
		}

		if (mSoftTexture == null) {
			throw new RuntimeException("the mSoftTexture is null");
		}

		mDecoder = new SoftDecoder(mDataSource, mOffset, 2);

		mState = PREPARED;
	}

	public void start() {

		if (mState != PREPARED) {
			throw new IllegalStateException();
		}

		mState = START;

		mThreadPool.execute(mDecoder);

		boolean finish = false;

		try {
			while (!finish) {
				finish = getFrame() == -1;
				notifyFrameAvailable();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isPlaying() {
		return mState == START;
	}

	void startDecoder() {

		if (mState != PREPARED) {
			throw new IllegalStateException();
		}
		mState = START;

		mThreadPool.execute(mDecoder);
	}

	/**
	 * 获取一帧数据
	 * @return 当前帧的时间戳，返回-1表示视频结束
	 * @throws InterruptedException 等待解码一帧完成
	 */
	long getFrame() throws InterruptedException {

		if (mState != START) {
			throw new IllegalStateException();
		}

		IDecoder.BufferInfo bufferInfo = mDecoder.getBufferInfo();
		if (bufferInfo != null) {
			mSoftTexture.setByteBuffer(bufferInfo.byteBuffer, bufferInfo.width,
									   bufferInfo.height, mRotation, bufferInfo.timestamp);
			mDecoder.recycle(bufferInfo);
			return bufferInfo.timestamp;
		} else {
			mSoftTexture.setTimestamp(mDuration * 1000);
		}

		return -1;
	}

	public void notifyFrameAvailable() {
		mSoftTexture.notifyFrameAvailable();
	}

	public void release() {
		if (mDecoder != null) {
			mDecoder.release();
		}

		mState = RELEASE;
	}
}
