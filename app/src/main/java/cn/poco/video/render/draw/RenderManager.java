package cn.poco.video.render.draw;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import cn.poco.video.render.adjust.AbstractAdjust;
import cn.poco.video.render.curve.CurveEffect;
import cn.poco.video.render.filter.AbstractFilter;
import cn.poco.video.render.filter.BlurFilter;
import cn.poco.video.render.gles.BufferPool;
import cn.poco.video.render.gles.OffscreenBuffer;
import cn.poco.video.render.transition.AbstractTransition;
import cn.poco.video.render.transition.TransitionItem;

/**
 * Created by: fwc
 * Date: 2017/12/13
 */
public class RenderManager {

	private Context mContext;

	private BufferPool mBufferPool;
	private SparseArray<OffscreenBuffer> mDrawBuffers = new SparseArray<>();

	private int mWidth;
	private int mHeight;

	private YUVFrame mYUVFrame;

	private BlurFilter mBlurFilter;

	private CurveEffect mCurveEffect;

	public RenderManager(Context context) {
		mContext = context;

		mYUVFrame = new YUVFrame(mContext);
		mCurveEffect = new CurveEffect(mContext);
	}

	public void setSurfaceSize(int width, int height) {
		mWidth = width;
		mHeight = height;

		if (mBufferPool != null) {
			mBufferPool.release();
			mBufferPool = null;
		}

		mBlurFilter = new BlurFilter(mContext, width, height);

		mBufferPool = new BufferPool(width, height, 3);
	}

	public int drawFrame(int textureId, float[] mvpMatrix, float[] texMatrix) {
		OffscreenBuffer buffer = getBuffer();

		buffer.bind();
		mYUVFrame.drawFrame(textureId, mvpMatrix, texMatrix);
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
			resetBuffer(curTextureId);
			resetBuffer(nextTextureId);
			curTextureId = buffer.getTextureId();

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
			mBlurFilter.draw(curTextureId, true);
			buffer.unbind();
			resetBuffer(curTextureId);
			curTextureId = buffer.getTextureId();

			mBlurFilter.draw(curTextureId, false);

			resetBuffer(curTextureId);
		} else {
			transition.draw(curTextureId, nextTextureId);
			resetBuffer(curTextureId);
			if (nextTextureId != -1) {
				resetBuffer(nextTextureId);
			}
		}
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

	public void release() {

		releaseBuffer();

		if (mYUVFrame != null) {
			mYUVFrame.release();
			mYUVFrame = null;
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
