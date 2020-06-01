package cn.poco.video.render.base;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by: fwc
 * Date: 2017/12/19
 */
public class GLTextureView extends TextureView implements TextureView.SurfaceTextureListener {

	Renderer mRenderer;
	private RenderThread mRenderThread;

	private boolean mDetached;

	public GLTextureView(Context context) {
		this(context, null);
	}

	public GLTextureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setSurfaceTextureListener(this);
	}

	public void setRenderer(Renderer renderer) {
		if (renderer == null) {
			throw new IllegalArgumentException("renderer must not be null");
		}

		if (mRenderThread != null) {
			throw new IllegalStateException("setRenderer has already been called for this instance.");
		}

		mRenderer = renderer;
		mRenderThread = new RenderThread(this);
		mRenderThread.start();
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		mRenderThread.onSurfaceCreated(width, height);
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		mRenderThread.onSurfaceChanged(width, height);
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		mRenderThread.onSurfaceDestroy();
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		if (mDetached && mRenderer != null) {
			mRenderThread = new RenderThread(this);
			mRenderThread.start();
		}
		mDetached = false;
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mRenderThread != null) {
			mRenderThread.requestExitAndWait();
		}
		mDetached = true;
		super.onDetachedFromWindow();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (mRenderThread != null) {
				mRenderThread.requestExitAndWait();
			}
		} finally {
			super.finalize();
		}
	}

	public void requestRender() {
		mRenderThread.requestRender();
	}

	public void queueEvent(Runnable r) {
		mRenderThread.queueEvent(r);
	}

	public interface Renderer {

		void onSurfaceCreated();

		void onSurfaceChanged(int width, int height);

		void onDrawFrame();

		void onSurfaceDestroyed();
	}
}
