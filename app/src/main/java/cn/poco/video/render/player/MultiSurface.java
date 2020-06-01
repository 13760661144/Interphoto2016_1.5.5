package cn.poco.video.render.player;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;

import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/5
 */
public class MultiSurface implements SurfaceTexture.OnFrameAvailableListener {

	private int mCurrentTextureId = GlUtil.NO_TEXTURE;
	private int mNextTextureId = GlUtil.NO_TEXTURE;

	private SurfaceTexture mCurrentTexture;
	private SurfaceTexture mNextTexture;

	private Surface mCurrentSurface;
	private Surface mNextSurface;

	private OnFrameAvailableListener mOnFrameAvailableListener;

	private OnSurfaceChangeListener mOnSurfaceChangeListener;

	private boolean isVideoStart = false;

	private float[] mSTMatrix1 = new float[16];
	private float[] mSTMatrix2 = new float[16];

	private Handler mHandler;

	public MultiSurface(OnSurfaceChangeListener listener) {

		mOnSurfaceChangeListener = listener;

		mCurrentTextureId = GlUtil.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
		mNextTextureId = GlUtil.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);

		mCurrentTexture = new SurfaceTexture(mCurrentTextureId);
		mCurrentTexture.setOnFrameAvailableListener(this);

		mNextTexture = new SurfaceTexture(mNextTextureId);
		mNextTexture.setOnFrameAvailableListener(this);

		mCurrentSurface = new Surface(mCurrentTexture);
		mNextSurface = new Surface(mNextTexture);

		mHandler = new Handler(Looper.getMainLooper());
	}

	public Surface getCurrentSurface() {
		return mCurrentSurface;
	}

	public Surface getNextSurface() {
		return mNextSurface;
	}

	public void onChangeSurface() {

		final int textureId = mCurrentTextureId;
		mCurrentTextureId = mNextTextureId;
		mNextTextureId = textureId;

		final SurfaceTexture surfaceTexture = mCurrentTexture;
		mCurrentTexture = mNextTexture;
		mNextTexture = surfaceTexture;

		final Surface surface = mCurrentSurface;
		mCurrentSurface = mNextSurface;
		mNextSurface = surface;

		mOnSurfaceChangeListener.onChangeSurface();
	}

	void onVideoStart(int index) {

	}

	void onVideoFinish(int index) {

	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		if (mOnFrameAvailableListener != null) {
			mOnFrameAvailableListener.onFrameAvailable(MultiSurface.this);
		}
	}

	public void updateTexImage() {
		mCurrentTexture.updateTexImage();
		mNextTexture.updateTexImage();
	}

	public float[] getCurrentSTMatrix() {
		mCurrentTexture.getTransformMatrix(mSTMatrix1);
		return mSTMatrix1;
	}

	public float[] getNextSTMatrix() {
		mNextTexture.getTransformMatrix(mSTMatrix2);
		return mSTMatrix2;
	}

	public int getCurrentTextureId() {
		return mCurrentTextureId;
	}

	public int getNextTextureId() {
		return mNextTextureId;
	}

	public void requestRender() {

		mHandler.post(mRenderRunnable);
	}

	private Runnable mRenderRunnable = new Runnable() {
		@Override
		public void run() {
			if (mOnFrameAvailableListener != null) {
				mOnFrameAvailableListener.onFrameAvailable(MultiSurface.this);
			}
		}
	};

	public void release() {

		mOnSurfaceChangeListener = null;
		mOnFrameAvailableListener = null;

		if (mCurrentTexture != null) {
			mCurrentTexture.setOnFrameAvailableListener(null);
			mCurrentTexture.release();
			mCurrentTexture = null;
		}

		if (mNextTexture != null) {
			mNextTexture.setOnFrameAvailableListener(this);
			mNextTexture.release();
			mNextTexture = null;
		}

		if (mCurrentSurface != null) {
			mCurrentSurface.release();
			mCurrentSurface = null;
		}

		if (mNextSurface != null) {
			mNextSurface.release();
			mNextSurface = null;
		}

		if (mCurrentTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mCurrentTextureId}, 0);
			mCurrentTextureId = GlUtil.NO_TEXTURE;
		}

		if (mNextTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mNextTextureId}, 0);
			mNextTextureId = GlUtil.NO_TEXTURE;
		}
	}

	public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
		mOnFrameAvailableListener = listener;
	}

	public interface OnFrameAvailableListener {
		void onFrameAvailable(MultiSurface multiSurface);
	}

	public interface OnSurfaceChangeListener {
		void onChangeSurface();
	}
}
