package cn.poco.video.save.player;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;

/**
 * Created by: fwc
 * Date: 2017/12/27
 */
public class SoftTexture {

	private final int mTextureId;

	private ByteBuffer mByteBuffer;
	private int mWidth;
	private int mHeight;
	private int mRotation;

	private long mTimestamp;

	private OnFrameAvailableListener mOnFrameAvailableListener;

	public SoftTexture(int textureId) {

		if (textureId <= 0) {
			throw new IllegalArgumentException("the texture id is error");
		}

		mTextureId = textureId;
	}

	public void setByteBuffer(ByteBuffer byteBuffer, int width, int height, int rotation, long timestamp) {

		if (byteBuffer == null || width <= 0 || height <= 0) {
			throw new IllegalArgumentException();
		}

		if (mByteBuffer == null) {
			mByteBuffer = ByteBuffer.allocateDirect(byteBuffer.capacity());
		} else if (mByteBuffer.capacity() < byteBuffer.capacity()) {
			mByteBuffer.clear();
			mByteBuffer = ByteBuffer.allocateDirect(byteBuffer.capacity());
		}

		mByteBuffer.clear();
		mByteBuffer.put(byteBuffer);
		mByteBuffer.flip();

		mWidth = width;
		mHeight = height;
		mRotation = rotation;

		mTimestamp = timestamp;
	}

	void setTimestamp(long timestamp) {
		mTimestamp = timestamp;
	}

	void notifyFrameAvailable() {
		if (mOnFrameAvailableListener != null) {
			mOnFrameAvailableListener.onFrameAvailable(this);
		}
	}

	public void updateTexImage() {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mByteBuffer);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	public void getTransformMatrix(float[] mtx) {
		if (mtx.length != 16) {
			throw new IllegalArgumentException();
		}

		Matrix.setIdentityM(mtx, 0);

		// 翻转纹理
		mtx[5] = -1;
		mtx[13] = 1;

		MatrixUtils.rotateM(mtx, 0, mRotation);
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
		mOnFrameAvailableListener = listener;
	}

	public void release() {
		if (mByteBuffer != null) {
			mByteBuffer.clear();
		}
	}

	public interface OnFrameAvailableListener {
		void onFrameAvailable(SoftTexture softTexture);
	}
}
