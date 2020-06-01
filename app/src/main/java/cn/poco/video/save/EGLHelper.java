package cn.poco.video.save;

import android.view.Surface;

import cn.poco.video.render.gles.EglCore;
import cn.poco.video.render.gles.WindowSurface;


/**
 * Created by: fwc
 * Date: 2017/9/22
 */
public class EGLHelper {

	private EglCore mEglCore;
	private WindowSurface mWindowSurface;

	private Renderer mRenderer;

	public EGLHelper(Renderer renderer) {
		mRenderer = renderer;
	}

	public void initEGLContext(Surface surface) {
		mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE | EglCore.FLAG_TRY_GLES3);
		mWindowSurface = new WindowSurface(mEglCore, surface, false);
		mWindowSurface.makeCurrent();
	}

	public void onSurfaceCreated() {
		if (mRenderer != null) {
			mRenderer.onSurfaceCreated();
		}
	}

	public void onSurfaceChanged(int width, int height) {
		if (mRenderer != null) {
			mRenderer.onSurfaceChanged(width, height);
		}
	}

	public void onDrawFrame() {
		if (mRenderer != null) {
			mRenderer.onDrawFrame();
		}
	}

	public void onSurfaceDestroy() {
		if (mRenderer != null) {
			mRenderer.onSurfaceDestroy();
		}
	}

	public void onSwapBuffers(long timestamp) {
		mWindowSurface.setPresentationTime(timestamp * 1000);
		mWindowSurface.swapBuffers();
	}

	public void release() {
		if (mWindowSurface != null) {
			mWindowSurface.release();
		}
		if (mEglCore != null) {
			mEglCore.release();
		}
	}

	public interface Renderer {

		void onSurfaceCreated();

		void onSurfaceChanged(int width, int height);

		void onDrawFrame();

		void onSurfaceDestroy();
	}
}
