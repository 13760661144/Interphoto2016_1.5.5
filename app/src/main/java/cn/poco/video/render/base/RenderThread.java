package cn.poco.video.render.base;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.poco.video.render.gles.EglCore;
import cn.poco.video.render.gles.WindowSurface;

/**
 * Created by: fwc
 * Date: 2017/12/19
 */
public class RenderThread extends Thread {

	private WeakReference<GLTextureView> mTextureView;

	private EglCore mEglCore;
	private WindowSurface mWindowSurface;
	private boolean mShouldCallDestroy;

	private boolean mHaveSurface;

	private int mWidth;
	private int mHeight;
	private boolean mSizeChanged;

	private boolean mRequestRender;
	private boolean mDestroySurface;
	private boolean mWaittingSurface;

	private boolean mShouldExit = false;
	private boolean mExited = false;

	private List<Runnable> mEventQueue = new ArrayList<>();

	private final Object mLock = new Object();

	public RenderThread(GLTextureView textureView) {
		mTextureView = new WeakReference<>(textureView);
	}

	@Override
	public void run() {
		setName("Render Thread " + getId());

		try {
			guardedRun();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			synchronized (mLock) {
				mExited = true;
				mLock.notifyAll();
			}
		}
	}

	private void guardedRun() throws InterruptedException {
		mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);

		try {
			while (true) {

				boolean createEglSurface = false;
				boolean sizeChanged = false;
				boolean doRender = false;
				Runnable event = null;

				synchronized (mLock) {
					while (true) {
						if (mShouldExit) {
							return;
						}

						if (!mEventQueue.isEmpty()) {
							event = mEventQueue.remove(0);
							break;
						}

						if (mDestroySurface) {
							releaseSurface();
							mDestroySurface = false;
							mWaittingSurface = true;
						}

						if (mWindowSurface == null && mHaveSurface) {
							createEglSurface = true;
							break;
						}

						if (mSizeChanged) {
							sizeChanged = true;
							mSizeChanged = false;
							break;
						}

						if (mRequestRender && mWidth > 0 && mHeight > 0) {
							doRender = true;
							mRequestRender = false;
							break;
						}

						mLock.wait();
					}
				}

				if (event != null) {
					event.run();
					continue;
				}

				if (createEglSurface) {
					GLTextureView view = mTextureView.get();
					if (view != null) {
						mWindowSurface = new WindowSurface(mEglCore, view.getSurfaceTexture());
					}

					mWindowSurface.makeCurrent();
					if (view != null && !mWaittingSurface) {
						view.mRenderer.onSurfaceCreated();
						mShouldCallDestroy = true;
						if (mWidth > 0 && mHeight > 0) {
							view.mRenderer.onSurfaceChanged(mWidth, mHeight);
						}
					}
					doRender = mWaittingSurface;
					mWaittingSurface = false;
				}

				if (sizeChanged) {
					GLTextureView view = mTextureView.get();
					if (view != null) {
						view.mRenderer.onSurfaceChanged(mWidth, mHeight);
					}
				}

				if (doRender) {
					GLTextureView view = mTextureView.get();
					if (view != null) {
						view.mRenderer.onDrawFrame();
						mWindowSurface.swapBuffers();
					}
				}
			}
		} finally {
			GLTextureView view = mTextureView.get();
			if (view != null && mShouldCallDestroy) {
				view.mRenderer.onSurfaceDestroyed();
			}
			releaseSurface();
			releaseGL();
		}
	}

	private void releaseSurface() {
		if (mWindowSurface != null) {
			mWindowSurface.release();
			mWindowSurface = null;
		}
		mWidth = 0;
		mHeight = 0;
		mHaveSurface = false;
	}

	private void releaseGL() {

		if (mEglCore != null) {
			mEglCore.release();
			mEglCore = null;
		}
	}

	public void requestExitAndWait() {
		synchronized(mLock) {
			if (!mExited) {
				mShouldExit = true;
				mLock.notifyAll();
				while (!mExited) {
					try {
						mLock.wait();
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	public void onSurfaceCreated(int width, int height) {
		synchronized (mLock) {
			if (!mExited) {
				mHaveSurface = true;
				mWidth = width;
				mHeight = height;
				mLock.notifyAll();
			}
		}
	}

	public void onSurfaceChanged(int width, int height) {
		synchronized (mLock) {
			if (!mExited) {
				mWidth = width;
				mHeight = height;
				mSizeChanged = true;
				mLock.notifyAll();
			}
		}
	}

	public void requestRender() {
		synchronized (mLock) {
			if (!mExited) {
				mRequestRender = true;
				mLock.notifyAll();
			}
		}
	}

	public void queueEvent(Runnable r) {
		if (r == null) {
			throw new IllegalArgumentException("r must not be null");
		}
		synchronized(mLock) {
			if (!mExited) {
				mEventQueue.add(r);
				mLock.notifyAll();
			}
		}
	}

	public void onSurfaceDestroy() {
		synchronized (mLock) {
			if (!mExited) {
				mDestroySurface = true;
				mLock.notifyAll();
			}
		}
	}
}
