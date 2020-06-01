package cn.poco.camera.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.util.SparseArray;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import cn.poco.capture2.encoder.MediaMuxerWrapper;
import cn.poco.capture2.encoder.MediaVideoEncoder;
import cn.poco.capture2.encoder.RecordState;
import cn.poco.video.render.draw.YUVFrame;
import cn.poco.video.render.filter.AbstractFilter;
import cn.poco.video.render.filter.BlendFilter;
import cn.poco.video.render.filter.FilterItem;
import cn.poco.video.render.filter.LookupTableFilter;
import cn.poco.video.render.filter.NoneFilter;
import cn.poco.video.render.gles.BufferPool;
import cn.poco.video.render.gles.GlUtil;
import cn.poco.video.render.gles.OffscreenBuffer;

/**
 * Created by: fwc
 * Date: 2017/5/15
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CameraRenderer implements GLSurfaceView.Renderer {

	private Context mContext;
	private GLCameraView.CameraHandler mCameraHandler;

	private YUVFrame mYUVFrame;

	private int mTextureId;
	private SurfaceTexture mSurfaceTexture;

	private int mSurfaceWidth;
	private int mSurfaceHeight;

	private final float[] mSTMatrix = new float[16];

	private EGLDisplay mSavedEglDisplay;
	private EGLSurface mSavedEglDrawSurface;
	private EGLSurface mSavedEglReadSurface;
	private EGLContext mSavedEglContext;

	private MediaMuxerWrapper mMediaMuxerWrapper;
	private MediaVideoEncoder mMediaVideoEncoder;
	private int mRecordState = RecordState.IDLE;

	private int mTop;
	private int mBottom;

	private int mVideoWidth;
	private int mVideoHeight;

	private int mFilterType = FilterItem.TYPE_NONE;
	private AbstractFilter.Params mParams;
	private SparseArray<AbstractFilter> mFilterArray = new SparseArray<>();

	private BufferPool mBufferPool;

	private boolean isSaveFilter = false;

	private boolean isRotation = false;

//	private OnStopRecordListener mOnStopRecordListener;
//	private Handler mMainHandler;

	public CameraRenderer(Context context, GLCameraView.CameraHandler cameraHandler) {
		mContext = context;
		mCameraHandler = cameraHandler;
	}

	public void setMediaMuxerWrapper(MediaMuxerWrapper mediaMuxerWrapper) {
		mMediaMuxerWrapper = mediaMuxerWrapper;
		if (mMediaMuxerWrapper != null) {
			mMediaVideoEncoder = (MediaVideoEncoder)mMediaMuxerWrapper.getVideoEncoder();
		}
	}

//	public void setOnStopRecordListener(OnStopRecordListener listener, Handler mainHandler) {
//		mOnStopRecordListener = listener;
//		mMainHandler = mainHandler;
//	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// 注意：popup新页面返回和onResume都会回调这个方法

		GLES20.glClearColor(0, 0, 0, 1);

		mYUVFrame = new YUVFrame(mContext);

		if (mTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mTextureId}, 0);
		}

		Matrix.setIdentityM(mSTMatrix, 0);

		mTextureId = GlUtil.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
		mSurfaceTexture = new SurfaceTexture(mTextureId);

		mCameraHandler.obtainMessage(GLCameraView.ON_SURFACE_CREATE, mSurfaceTexture).sendToTarget();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// 注意：popup新页面返回和onResume都会回调这个方法

		mSurfaceWidth = width;
		mSurfaceHeight = height;

		// 由于调用calculateTopAndBottom时onSurfaceChanged还没调用
		// 建议在下面回调中调用
		if (mTop != 0 || mBottom != 0) {
			calculateTopAndBottom();
		}

		GLES20.glViewport(0, 0, width, height);

		initFilters();

		if (mBufferPool != null) {
			mBufferPool.release();
			mBufferPool = null;
		}

		mBufferPool = new BufferPool(width, height, 2);

		mCameraHandler.obtainMessage(GLCameraView.START_CAMERA_PREVIEW, width, height).sendToTarget();

		saveRenderState();
	}

	private void initFilters() {
		mFilterArray.clear();
		mFilterArray.put(FilterItem.TYPE_NONE, new NoneFilter(mContext));
		mFilterArray.put(FilterItem.TYPE_LOOKUP_TABLE, new LookupTableFilter(mContext));
		mFilterArray.put(FilterItem.TYPE_BLEND, new BlendFilter(mContext));
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		mSurfaceTexture.updateTexImage();
		mSurfaceTexture.getTransformMatrix(mSTMatrix);

		GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);

		OffscreenBuffer buffer = mBufferPool.obtain();
		buffer.bind();
		mYUVFrame.drawFrame(mTextureId, GlUtil.IDENTITY_MATRIX, mSTMatrix);
		buffer.unbind();

		drawFilter(buffer.getTextureId(), mFilterType);

		onDrawVideoFrame(buffer.getTextureId());

		buffer.recycle();
	}

	private void drawFilter(int textureId, int type) {
		AbstractFilter filter;
		if (type == FilterItem.TYPE_BOTH) {

			filter = mFilterArray.get(FilterItem.TYPE_LOOKUP_TABLE);

			OffscreenBuffer buffer = mBufferPool.obtain();
			buffer.bind();
			filter.setParams(mParams);
			filter.draw(textureId);
			buffer.unbind();

			filter =  mFilterArray.get(FilterItem.TYPE_BLEND);
			filter.setParams(mParams);
			filter.draw(buffer.getTextureId());

			buffer.recycle();

		} else {
			filter =  mFilterArray.get(type);
			filter.setParams(mParams);
			filter.draw(textureId);
		}
	}

	/**
	 * 绘制视频
	 */
	private void onDrawVideoFrame(int textureId) {

		if (mRecordState == RecordState.IDLE) {
			return;
		}

		if (mMediaMuxerWrapper != null && mMediaVideoEncoder != null) {
			int videoWidth = mMediaVideoEncoder.getVideoWidth();
			int videoHeight = mMediaVideoEncoder.getVideoHeight();
			if (mRecordState == RecordState.PREPARE && !mMediaMuxerWrapper.isPrepared()) {
				boolean prepareSuccess = true;
				try {
					mMediaMuxerWrapper.prepare();
				} catch (Exception e) {
					e.printStackTrace();
					mMediaMuxerWrapper.setPrepared(false);
					prepareSuccess = false;
				}
				if (prepareSuccess) {
//					mMediaMuxerWrapper.setSurfaceSize(mSurfaceWidth, mSurfaceHeight);
					mMediaMuxerWrapper.setViewSize(videoWidth, videoHeight);
					mRecordState = RecordState.WAIT;
				}
			}
			if (mRecordState == RecordState.START) {
				mMediaMuxerWrapper.startRecording();
				mRecordState = RecordState.RECORDING;

			} else if (mRecordState == RecordState.RESUME) {
				mMediaMuxerWrapper.resumeRecording();
				mRecordState = RecordState.RECORDING;

			} else if (mRecordState == RecordState.PAUSE) {
				mMediaMuxerWrapper.pauseRecording();

			} else if (mRecordState == RecordState.STOP) {
//				mMainHandler.removeCallbacks(mCheckStopRunnable);
				mMediaMuxerWrapper.stopRecording();
				mRecordState = RecordState.IDLE;
			}

			if (mRecordState == RecordState.RECORDING && mMediaMuxerWrapper.canRecord() && mMediaVideoEncoder != null) {
//				if (mMainHandler != null) {
//					mMainHandler.removeCallbacks(mCheckStopRunnable);
//					mMainHandler.postDelayed(mCheckStopRunnable, 150);
//				}
				// switch to recorder state
				try {
					mMediaVideoEncoder.makeCurrent();
				} catch (Exception e) {
					e.printStackTrace();
					restoreRenderState();
					return;
				}

				int x, y;
				if (isRotation) {
					x = (mTop + videoWidth) - mVideoWidth;
					y = (videoHeight - mVideoHeight) / 2;
				} else {
					x = (videoWidth - mVideoWidth) / 2;
					y = (mTop + videoHeight) - mVideoHeight;
				}

				GLES20.glViewport(x, y, mVideoWidth, mVideoHeight);

				// render everything again
				if (isSaveFilter) {
					drawFilter(textureId, mFilterType);
				} else {
					drawFilter(textureId, FilterItem.TYPE_NONE);
				}

				mMediaVideoEncoder.swapBuffers();

				restoreRenderState();
			}
		}
	}

	/**
	 * 改变预览比率
	 *
	 * @param top 预览窗口顶部
	 * @param bottom 预览窗口底部
	 */
	public void changeRatio(int top, int bottom) {
		mTop = top;
		mBottom = bottom;

		calculateTopAndBottom();
	}

	/**
	 * 修改视频大小
	 *
	 * @param videoWidth 视频宽
	 * @param videoHeight 视频高
	 */
	public void setVideoSize(int videoWidth, int videoHeight) {
		mVideoWidth = videoWidth;
		mVideoHeight = videoHeight;

		calculateTopAndBottom();
	}

	/**
	 * 根据surface的大小和视频最终大小计算偏移
	 */
	private void calculateTopAndBottom() {
		// 第一次设置时可能mSurfaceHeight还没赋值
		if (mSurfaceHeight != 0) {

			if (isRotation) {
				mTop = (int)((mTop * 1f / mSurfaceWidth) * mVideoWidth);
				mBottom = (int)((mBottom * 1f / mSurfaceWidth) * mVideoWidth);
			} else {
				mTop = (int)((mTop * 1f / mSurfaceHeight) * mVideoHeight);
				mBottom = (int)((mBottom * 1f / mSurfaceHeight) * mVideoHeight);
			}

		}
	}

	/**
	 * 设置录制状态
	 *
	 * @param state 录制状态
	 */
	public void setRecordState(int state) {
		switch (state) {
			case RecordState.IDLE:
				mRecordState = RecordState.IDLE;
				break;
			case RecordState.PREPARE:
				if (mRecordState == RecordState.IDLE) {
					mRecordState = RecordState.PREPARE;
				}
				break;
			case RecordState.WAIT:
				break;
			case RecordState.START:
				if (mRecordState == RecordState.WAIT) {
					mRecordState = RecordState.START;
				}
				break;
			case RecordState.RESUME:
				if (mRecordState == RecordState.PAUSE) {
					mRecordState = RecordState.RESUME;
				}
				break;
			case RecordState.RECORDING:
				break;
			case RecordState.PAUSE:
				if (mRecordState == RecordState.RECORDING) {
					mRecordState = RecordState.PAUSE;
				}
				break;
			case RecordState.STOP:
				if (mRecordState == RecordState.RECORDING) {
					mRecordState = RecordState.STOP;
				}
				break;
			case RecordState.CAPTURE_A_FRAME:
				if (mRecordState == RecordState.IDLE || mRecordState == RecordState.WAIT) {
					mRecordState = RecordState.CAPTURE_A_FRAME;
				}
				break;
		}
	}

	private void saveRenderState() {
		mSavedEglDisplay = EGL14.eglGetCurrentDisplay();
		mSavedEglDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
		mSavedEglReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
		mSavedEglContext = EGL14.eglGetCurrentContext();
	}

	/**
	 * Saves the current projection matrix and EGL state.
	 */
	private void restoreRenderState() {
		// switch back to previous state
		if (!EGL14.eglMakeCurrent(mSavedEglDisplay, mSavedEglDrawSurface, mSavedEglReadSurface, mSavedEglContext)) {
			throw new RuntimeException("eglMakeCurrent failed");
		}
	}

	/**
	 * 录制的视频是否保留滤镜效果
	 */
	public void setSaveFilter(boolean saveFilter) {
		isSaveFilter = saveFilter;
	}

	/**
	 * 修改滤镜
	 *
	 * @param item 滤镜参数item
	 */
	public void changeFilter(FilterItem item) {

		if (item == null) {
			mFilterType = FilterItem.TYPE_NONE;
			mParams = null;
		} else {
			mFilterType = item.type;
			mParams = new AbstractFilter.Params(item);
		}
	}



	/**
	 * 修改滤镜不透明度
	 *
	 * @param alpha 不透明度
	 */
	public void changeFilterAlpha(float alpha) {
		if (mParams != null) {
			mParams.alpha = alpha;
		}
	}

	public void setRotation(boolean isRotation) {
		this.isRotation = isRotation;
	}

	public void onPause() {
		if (mRecordState == RecordState.RECORDING) {
			mRecordState = RecordState.IDLE;
		}
	}

	public void release() {

		if (mTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mTextureId}, 0);
			mTextureId = GlUtil.NO_TEXTURE;
		}

		if (mYUVFrame != null) {
			mYUVFrame.release();
			mYUVFrame = null;
		}

		releaseFilter();
		releaseBuffer();
	}

	private void releaseFilter() {
		for (int i = 0; i < mFilterArray.size(); i++) {
			mFilterArray.get(mFilterArray.keyAt(i)).release();
		}

		mFilterArray.clear();
	}

	private void releaseBuffer() {
		if (mBufferPool != null) {
			mBufferPool.release();
			mBufferPool = null;
		}
	}

//	private Runnable mCheckStopRunnable = new Runnable() {
//
//		@Override
//		public void run() {
//			if (mOnStopRecordListener != null) {
//				mOnStopRecordListener.onStopRecord();
//			}
//		}
//	};
//
//	public interface OnStopRecordListener {
//		void onStopRecord();
//	}
}
