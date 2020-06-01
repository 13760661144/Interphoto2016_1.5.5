package cn.poco.video.save.transition;

import android.content.Context;
import android.opengl.GLES20;
import android.support.annotation.RawRes;

import cn.poco.video.render.gles.Drawable2d;
import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/27
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

	private OnTransitionListener mOnTransitionListener;

	protected boolean isStart = false;
	private float mProgress = 0;

	public AbstractTransition(Context context) {
		mContext = context;
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

	/**
	 * 更新转场进度
	 * @param position 当前视频的进度
	 * @param duration 当前视频的时长
	 * @return 转场动画是否将要结束
	 */
	public boolean update(long position, long duration) {

		if (!isStart) {
			if (shouldStart(position, duration)) {
				onStart();
			}
		} else {
			mProgress = getProgress(position, duration);
			if (mProgress > 0 && mProgress < 1) {
				onProgressChanged(mProgress);
			} else {
				onEnd();
				return true;
			}
		}

		return false;
	}

	protected void onStart() {
		isStart = true;
		mProgress = 0;
		onProgressChanged(mProgress);
	}

	protected void onEnd() {
		isStart = false;
		mProgress = 1;
		onProgressChanged(mProgress);
	}

	protected void onStartNextVideo() {
		if (mOnTransitionListener != null) {
			mOnTransitionListener.onStartNextVideo();
		}
	}

	public void setValue(Object value) {

	}

	public void onVideoFinish(int index) {
	}

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

	public boolean shouldRenderNext() {
		return false;
	}

	public boolean isBlendTransition() {
		return false;
	}

	public float getProgress() {
		return 0;
	}

	protected abstract boolean shouldStart(long position, long duration);

	protected abstract float getProgress(long position, long duration);

	protected abstract void onProgressChanged(float progress);

	public void reset() {
		isStart = false;
		mProgress = 0;
	}

	public void release() {
		mOnTransitionListener = null;
		if (mProgram != 0) {
			GLES20.glDeleteProgram(mProgram);
			mProgram = 0;
		}
	}

	protected float checkRange(float value, float min, float max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		}

		return value;
	}

	public interface OnTransitionListener {

		void onStartNextVideo();
	}
}
