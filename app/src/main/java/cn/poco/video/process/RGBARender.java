package cn.poco.video.process;

import android.content.Context;
import android.opengl.GLES20;

import cn.poco.video.render.filter.NoneFilter;
import cn.poco.video.render.gles.GlUtil;
import cn.poco.video.save.EGLHelper;
import cn.poco.video.save.player.SoftTexture;

/**
 * Created by: fwc
 * Date: 2018/1/22
 */
public class RGBARender implements EGLHelper.Renderer {

	private Context mContext;

	private OnRenderListener mOnRenderListener;

	private int mTextureId = GlUtil.NO_TEXTURE;
	private SoftTexture mSoftTexture;
	private NoneFilter mNoneFilter;

	private float[] mSTMatrix = new float[16];

	public RGBARender(Context context, OnRenderListener listener) {
		mContext = context;
		mOnRenderListener = listener;
	}

	@Override
	public void onSurfaceCreated() {
		GLES20.glClearColor(0, 0, 0, 1);

		mTextureId = GlUtil.createTexture(GLES20.GL_TEXTURE_2D);
		mSoftTexture = new SoftTexture(mTextureId);

		if (mOnRenderListener != null) {
			mOnRenderListener.onCreateSurface(mSoftTexture);
		}
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		mNoneFilter = new NoneFilter(mContext);
	}

	@Override
	public void onDrawFrame() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		mSoftTexture.updateTexImage();
		mSoftTexture.getTransformMatrix(mSTMatrix);
		long timestamp = mSoftTexture.getTimestamp();
		mNoneFilter.draw(mTextureId, GlUtil.IDENTITY_MATRIX, mSTMatrix);
		onDrawFinish(timestamp);
	}

	@Override
	public void onSurfaceDestroy() {
		release();
	}

	private void release() {
		if (mNoneFilter != null) {
			mNoneFilter.release();
			mNoneFilter = null;
		}

		if (mSoftTexture != null) {
			mSoftTexture.release();
			mSoftTexture = null;
		}

		if (mTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mTextureId}, 0);
			mTextureId = GlUtil.NO_TEXTURE;
		}
	}

	private void onDrawFinish(long timestamp) {
		if (mOnRenderListener != null) {
			mOnRenderListener.onDrawFrame(timestamp);
		}
	}

	public interface OnRenderListener {

		void onCreateSurface(SoftTexture softTexture);

		void onDrawFrame(long timestamp);
	}
}
