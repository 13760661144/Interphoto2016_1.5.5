package cn.poco.camera.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import java.lang.ref.WeakReference;

import cn.poco.camera.CameraWrapper;
import cn.poco.camera.ICameraView;
import cn.poco.video.render.filter.FilterItem;

/**
 * Created by: fwc
 * Date: 2017/5/15
 */
public class GLCameraView extends GLSurfaceView implements ICameraView, SurfaceTexture.OnFrameAvailableListener {

	public static final int ON_SURFACE_CREATE = 1001;
	public static final int START_CAMERA_PREVIEW = 1002;

	private CameraHandler mCameraHandler;
	private CameraRenderer mCameraRenderer;
	private CameraWrapper mCameraWrapper;
	private boolean mSurfaceCreated;
	private SurfaceTexture mSurfaceTexture;

	public GLCameraView(Context context) {
		this(context, null);
	}

	public GLCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	private void init() {
		setEGLContextClientVersion(2);

		mCameraHandler = new CameraHandler(Looper.getMainLooper(), this);
		mCameraRenderer = new CameraRenderer(getContext(), mCameraHandler);
		setRenderer(mCameraRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		mCameraWrapper = new CameraWrapper(getContext(), new CameraWrapper.CameraSurfaceView() {
			@Override
			public SurfaceHolder getSurfaceHolder() {
				return null;
			}

			@Override
			public SurfaceTexture getSurfaceTexture() {
				return mSurfaceTexture;
			}

			@Override
			public void requestView() {
				if (mSurfaceCreated) {
					requestLayout();
				}
			}
		});
	}

	public CameraRenderer getRenderer() {
		return mCameraRenderer;
	}

	/**
	 * 录制的视频是否保留滤镜效果
	 */
	public void setSaveFilter(final boolean saveFilter) {
		mCameraRenderer.setSaveFilter(saveFilter);
	}

	/**
	 * 修改滤镜
	 *
	 * @param item 滤镜参数item
	 */
	public void changeFilter(final FilterItem item) {
		mCameraRenderer.changeFilter(item);
	}

	/**
	 * 修改滤镜不透明度
	 *
	 * @param alpha 不透明度，0~1
	 */
	public void changeFilterAlpha(final float alpha) {
		mCameraRenderer.changeFilterAlpha(alpha);
	}

	/**
	 * 修改预览比率
	 *
	 * @param top 预览窗口顶部
	 * @param bottom 预览窗口底部
	 */
	public void changeRatio(final int top, final int bottom) {
		queueEvent(new Runnable() {
			@Override
			public void run() {
				mCameraRenderer.changeRatio(top, bottom);
			}
		});
	}

	public void setRotation(final boolean isRotation) {
		queueEvent(new Runnable() {
			@Override
			public void run() {
				mCameraRenderer.setRotation(isRotation);
			}
		});
	}

	/**
	 * 修改视频大小
	 *
	 * @param videoWidth 视频宽
	 * @param videoHeight 视频高
	 */
	public void setVideoSize(final int videoWidth, final int videoHeight) {
		queueEvent(new Runnable() {
			@Override
			public void run() {
				mCameraRenderer.setVideoSize(videoWidth, videoHeight);
			}
		});
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		requestRender();
	}

	@Override
	public CameraWrapper getCamera() {
		return mCameraWrapper;
	}

	@Override
	public void switchCamera() {
		if (mCameraWrapper != null) {
			mCameraWrapper.switchCamera();
		}
	}

	@Override
	public void setPreviewRatio(float ratio) {

	}

	@Override
	public void onPreviewSuccess() {

	}

	@Override
	public void setFaceData(Object... objects) {

	}

	@Override
	public void setPreviewDegree(int patchDegree) {

	}

	@Override
	public void setPatchMode(boolean patchMode) {

	}

	@Override
	public int patchPreviewDegree() {
		return 0;
	}

	@Override
	public void setBeautyEnable(boolean enable) {

	}

	@Override
	public void setFilterEnable(boolean enable) {

	}

	@Override
	public void setFilterId(int filterId) {

	}

	@Override
	public void setDelayDestroyTime(int delay) {

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mCameraHandler.removeCallbacksAndMessages(null);
		if (mCameraWrapper != null) {
			mCameraWrapper.onSurfaceViewDestory();
			mSurfaceCreated = false;
		}

		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (mCameraRenderer != null) {
					mCameraRenderer.release();
				}
			}
		});
	}

	@Override
	public void onDestroy() {

		if (mCameraHandler != null) {
			mCameraHandler.removeCallbacksAndMessages(null);
			mCameraHandler = null;
		}

		if (mSurfaceTexture != null) {
			mSurfaceTexture.release();
			mSurfaceTexture = null;
		}
	}

	@Override
	public void recycleAll() {

	}

	public static class CameraHandler extends Handler {
		private WeakReference<GLCameraView> mCameraView;

		public CameraHandler(Looper looper, GLCameraView GLCameraView) {
			super(looper);
			mCameraView = new WeakReference<>(GLCameraView);
		}

		@Override
		public void handleMessage(Message msg) {
			GLCameraView GLCameraView = mCameraView.get();
			if (GLCameraView != null) {
				GLCameraView.handleAllMessage(msg);
			}
		}
	}

	private void handleAllMessage(final Message msg) {
		switch (msg.what) {
			case ON_SURFACE_CREATE:
				mSurfaceTexture = (SurfaceTexture) msg.obj;
				if (mSurfaceTexture != null) {
					mSurfaceTexture.setOnFrameAvailableListener(this);
				}
				if (mCameraWrapper != null) {
					mCameraWrapper.onSurfaceViewCreate();
					mCameraWrapper.setSilenceOnTaken(mCameraWrapper.isSilenceOnTaken());
					mSurfaceCreated = true;
				}
				break;
			case START_CAMERA_PREVIEW:
//				final int width = msg.arg1;
//				final int height = msg.arg2;
				if (mCameraWrapper != null) {
					mCameraWrapper.onSurfaceViewChange();
				}
				break;
			default:
				break;
		}
	}
}
