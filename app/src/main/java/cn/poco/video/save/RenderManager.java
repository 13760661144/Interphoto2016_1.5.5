package cn.poco.video.save;

import android.content.Context;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import cn.poco.video.render.adjust.AbstractAdjust;
import cn.poco.video.render.curve.CurveEffect;
import cn.poco.video.render.filter.AbstractFilter;
import cn.poco.video.render.filter.BlurFilter;
import cn.poco.video.render.filter.NoneFilter;
import cn.poco.video.render.gles.BufferPool;
import cn.poco.video.render.gles.OffscreenBuffer;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.save.transition.AbstractTransition;
import cn.poco.video.save.watermark.WatermarkFilter;

/**
 * Created by: fwc
 * Date: 2017/12/27
 */
public class RenderManager {

	private Context mContext;

	private BufferPool mBufferPool;
	private SparseArray<OffscreenBuffer> mDrawBuffers = new SparseArray<>();

	private NoneFilter mRGBFrame;
	private BlurFilter mBlurFilter;

	private CurveEffect mCurveEffect;

	private boolean mBlendEnable;

	public RenderManager(Context context) {
		mContext = context;
	}

	public void setSurfaceSize(int width, int height) {
		if (mBufferPool != null) {
			mBufferPool.release();
			mBufferPool = null;
		}

		mRGBFrame = new NoneFilter(mContext);
		mBlurFilter = new BlurFilter(mContext, width, height);
		mCurveEffect = new CurveEffect(mContext);

		mBufferPool = new BufferPool(width, height, 4);
	}

	public int drawFrame(int textureId, float[] mvpMatrix, float[] texMatrix) {
		OffscreenBuffer buffer = getBuffer();
		buffer.bind();
		mRGBFrame.draw(textureId, mvpMatrix, texMatrix);
		buffer.unbind();

		return buffer.getTextureId();
	}

	public int drawFilter(AbstractFilter filter, int textureId) {
		OffscreenBuffer buffer = getBuffer();
		buffer.bind();
		filter.draw(textureId);
		buffer.unbind();
		resetBuffer(textureId);
		return buffer.getTextureId();
	}

	public int drawAdjust(AbstractAdjust adjust, AbstractAdjust.Params params, int textureId) {
		adjust.setParams(params);
		OffscreenBuffer buffer = getBuffer();
		buffer.bind();
		adjust.draw(textureId);
		buffer.unbind();
		resetBuffer(textureId);
		return buffer.getTextureId();
	}

	public int drawCurve(byte[] data, int textureId) {
		mCurveEffect.setData(data);
		OffscreenBuffer buffer = getBuffer();
		buffer.bind();
		mCurveEffect.draw(textureId);
		buffer.unbind();
		resetBuffer(textureId);
		return buffer.getTextureId();
	}

	public void drawTransition(AbstractTransition transition, int transitionId,
							   int curTextureId, int nextTextureId) {
		if (transition.shouldRenderNext() && transitionId == TransitionItem.BLUR) {
			OffscreenBuffer buffer = getBuffer();
			buffer.bind();
			transition.draw(curTextureId, nextTextureId);
			buffer.unbind();
			int textureId = buffer.getTextureId();

			buffer = getBuffer();
			buffer.bind();
			float progress = transition.getProgress();
			float radius;
			if (progress < 0.5f) {
				radius = progress * 2.4f;
			} else {
				radius = (1 - progress) * 2.4f;
			}
			mBlurFilter.setRadius(radius);
			mBlurFilter.draw(textureId, true);
			buffer.unbind();
			resetBuffer(textureId);
			textureId = buffer.getTextureId();

			mBlurFilter.draw(textureId, false);

			resetBuffer(textureId);
		} else {
			transition.draw(curTextureId, nextTextureId);
			resetBuffer(curTextureId);
			if (nextTextureId != -1) {
				resetBuffer(nextTextureId);
			}
		}
	}

	public void drawWatermark(WatermarkFilter filter) {
		blendEnable(true);
		filter.draw();
		blendEnable(false);
	}

	@NonNull
	private OffscreenBuffer getBuffer() {
		OffscreenBuffer buffer = mBufferPool.obtain();
		mDrawBuffers.put(buffer.getTextureId(), buffer);
		return buffer;
	}

	public void resetBuffer(int textureId) {
		OffscreenBuffer buffer = mDrawBuffers.get(textureId);
		if (buffer != null) {
			buffer.recycle();
			mDrawBuffers.remove(textureId);
		}
	}

	/**
	 * 是否开启混合
	 *
	 * @param enable true: 开启
	 */
	private void blendEnable(boolean enable) {
		if (enable == mBlendEnable) {
			return;
		}
		mBlendEnable = enable;
		if (enable) {
			GLES20.glDepthMask(false);
			GLES20.glEnable(GLES20.GL_BLEND);
			GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
			GLES20.glBlendFuncSeparate(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE, GLES20.GL_ONE);
		} else {
			GLES20.glDisable(GLES20.GL_BLEND);
			GLES20.glDepthMask(true);
		}
	}

	public void release() {
		releaseBuffer();

		if (mRGBFrame != null) {
			mRGBFrame.release();
			mRGBFrame = null;
		}

		if (mBlurFilter != null) {
			mBlurFilter.release();
			mBlurFilter = null;
		}

		if (mCurveEffect != null) {
			mCurveEffect.release();
			mCurveEffect = null;
		}
	}

	private void releaseBuffer() {

		mDrawBuffers.clear();

		if (mBufferPool != null) {
			mBufferPool.release();
			mBufferPool = null;
		}
	}
}
