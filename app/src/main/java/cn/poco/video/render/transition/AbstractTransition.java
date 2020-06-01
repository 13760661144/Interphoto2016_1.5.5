package cn.poco.video.render.transition;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RawRes;

import cn.poco.video.render.gles.Drawable2d;
import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/11
 */
public abstract class AbstractTransition {

	private Context mContext;

	private int mProgram = 0;
	private Drawable2d mDrawable2d = new Drawable2d();

	private int aPositionLoc;
	private int aTextureCoordLoc;

	private int uMVPMatrixLoc;
	private int uTexMatrixLoc;

	private int uFirstImageLoc;
	private int uSecondImageLoc;

	protected int mVideoIndex = 0;

	private float mLastProgress = 0;
	private UpdateAnimator mUpdateAnimator;

	private OnTransitionListener mOnTransitionListener;

	protected boolean isStart = false;
	protected boolean isStartAnim = false;
	protected boolean isUpdateEnable = true;

	private Handler mHandler;

	private boolean isReset;

	public AbstractTransition(Context context, Looper looper) {
		mContext = context;

		mUpdateAnimator = new UpdateAnimator(looper);
		mUpdateAnimator.setOnUpdateListener(mOnUpdateListener);

		mHandler = new Handler(Looper.getMainLooper());
	}

	protected void createProgram(@RawRes int vertexShader, @RawRes int fragmentShader) {
		mProgram = GlUtil.createProgram(mContext, vertexShader, fragmentShader);

		onGetUniformLocation(mProgram);
	}

	/**
	 * 获取Uniform变量位置
	 */
	protected void onGetUniformLocation(int program) {
		aPositionLoc = GLES20.glGetAttribLocation(program, "aPosition");
		aTextureCoordLoc = GLES20.glGetAttribLocation(program, "aTextureCoord");
		uMVPMatrixLoc = GLES20.glGetUniformLocation(program, "uMVPMatrix");
		uTexMatrixLoc = GLES20.glGetUniformLocation(program, "uTexMatrix");
		uFirstImageLoc = GLES20.glGetUniformLocation(program, "firstImage");
		uSecondImageLoc = GLES20.glGetUniformLocation(program, "secondImage");
	}

	public boolean update(long position, long duration) {

		if (!isStart && !isReset) {
			if (shouldStart(position, duration)) {
				onStart();
				return true;
			}
		}

		return false;
	}

	protected abstract boolean shouldStart(long position, long duration);

	protected abstract long getDuration();

	protected abstract void onProgressChanged(float progress);

	protected void onStart() {
		isStart = true;
		isStartAnim = false;
		mUpdateAnimator.start(mLastProgress, getDuration());
	}

	protected void onEnd() {
		mLastProgress = 0;
	}

	public void onVideoStart(int index) {
		isStart = false;
		mVideoIndex = index;
		if (canVideoStartAnim() && index != 0) {
			isStartAnim = true;
			mUpdateAnimator.start(0, getDuration());
		}
	}

	public void onChangeSurface() {

	}

	public void onVideoResume() {
		if (mLastProgress > 0 && mLastProgress < 1) {
			long duration = (long)((1 - mLastProgress) * getDuration());
			mUpdateAnimator.start(mLastProgress, duration);
		}
	}

	public void onVideoSeekTo(int index, long position, long duration) {
		mVideoIndex = index;
		if (isUpdateEnable) {
			if (canVideoStartAnim() && position < getDuration()) {
				isStart = false;
				isStartAnim = index != 0;
				if (isStartAnim) {
					mLastProgress = position * 1f / getDuration();
					notifyProgressChanged(mLastProgress);
				}
			} else {
				isStart = position > duration - getDuration();
				isStartAnim = false;
				if (isStart) {
					mLastProgress = (duration - position) * 1f / getDuration();
				} else {
					mLastProgress = 0;
				}
				notifyProgressChanged(mLastProgress);
			}
		}
	}

	private void notifyProgressChanged(float progress) {
		onProgressChanged(progress);
		if (mOnTransitionListener != null) {
			mOnTransitionListener.requestRender();
		}
	}

	public void onVideoPause() {
		mLastProgress = mUpdateAnimator.getCurrentProgress();
		mUpdateAnimator.reset();
	}

	protected boolean canVideoStartAnim() {
		return true;
	}

	public float getProgress() {
		return 0;
	}

	protected void onVideoFinish() {
		mHandler.post(mVideoFinishRunnable);
	}

	private Runnable mVideoFinishRunnable = new Runnable() {
		@Override
		public void run() {
			if (mOnTransitionListener != null) {
				mOnTransitionListener.onVideoFinish();
			}
		}
	};

	protected void onStartNextVideo() {
		mHandler.post(mStartNextVideoRunnable);
	}

	private Runnable mStartNextVideoRunnable = new Runnable() {
		@Override
		public void run() {
			if (mOnTransitionListener != null) {
				mOnTransitionListener.onStartNextVideo();
			}
		}
	};

	protected void onTransitionFinish() {
		mHandler.post(mTransitionFinishRunnable);
	}

	private Runnable mTransitionFinishRunnable = new Runnable() {
		@Override
		public void run() {
			if (mOnTransitionListener != null) {
				mOnTransitionListener.onTransitionFinish();
			}
		}
	};

	public void draw(int textureId1, int textureId2) {
		onUseProgram();
		onSetUniformData();
		onBindTexture(textureId1, textureId2);
		onDraw(GlUtil.IDENTITY_MATRIX, GlUtil.IDENTITY_MATRIX);
	}

	protected void onUseProgram() {
		GLES20.glUseProgram(mProgram);
	}

	/**
	 * 设置Uniform变量数据
	 */
	protected void onSetUniformData() {

	}

	/**
	 * 绑定纹理
	 */
	protected void onBindTexture(int textureId1, int textureId2) {
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId1);
		GLES20.glUniform1i(uFirstImageLoc, 0);

		if (uSecondImageLoc >= 0) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId2);
			GLES20.glUniform1i(uSecondImageLoc, 1);
		}
	}

	protected void onDraw(float[] mvpMatrix, float[] texMatrix) {

		GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0);

		GLES20.glUniformMatrix4fv(uTexMatrixLoc, 1, false, texMatrix, 0);

		GLES20.glEnableVertexAttribArray(aPositionLoc);
		GLES20.glVertexAttribPointer(aPositionLoc, mDrawable2d.getCoordsPerVertex(), GLES20.GL_FLOAT, false, mDrawable2d.getVertexStride(), mDrawable2d.getVertexArray());

		GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
		GLES20.glVertexAttribPointer(aTextureCoordLoc, 2, GLES20.GL_FLOAT, false, mDrawable2d.getTexCoordStride(), mDrawable2d.getTexCoordArray());

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mDrawable2d.getVertexCount());

		GLES20.glDisableVertexAttribArray(aPositionLoc);
		GLES20.glDisableVertexAttribArray(aTextureCoordLoc);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES20.glUseProgram(0);
	}

	public void setOnTransitionListener(OnTransitionListener listener) {
		mOnTransitionListener = listener;
	}

	public void onUse() {
		isReset = false;
		mUpdateAnimator.setOnUpdateListener(mOnUpdateListener);
	}

	public boolean shouldRenderNext() {
		return false;
	}

	public boolean isBlendTransition() {
		return false;
	}

	public void setValue(Object value) {

	}

	public void reset() {
		isReset = true;
		isStart = false;
		mLastProgress = 0;
		notifyProgressChanged(mLastProgress);
		mUpdateAnimator.reset();
		mUpdateAnimator.setOnUpdateListener(null);
	}

	public void release() {

		mUpdateAnimator.reset();
		mUpdateAnimator.setOnUpdateListener(null);

		mOnTransitionListener = null;

		if (mProgram != 0) {
			GLES20.glDeleteProgram(mProgram);
			mProgram = 0;
		}
	}

	private UpdateAnimator.OnUpdateListener mOnUpdateListener = new UpdateAnimator.OnUpdateListener() {

		@Override
		public void onUpdate(float progress) {
			if (isUpdateEnable && !isReset) {
				notifyProgressChanged(progress);
			}
		}

		@Override
		public void onAnimatorEnd() {
			if (!isReset) {
				onEnd();
			}
		}
	};

	public interface OnTransitionListener {

		void requestRender();

		void onVideoFinish();

		void onStartNextVideo();

		void onTransitionFinish();
	}
}
