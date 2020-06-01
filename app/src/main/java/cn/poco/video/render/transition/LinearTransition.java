package cn.poco.video.render.transition;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Looper;

import cn.poco.interphoto2.R;

/**
 * Created by: fwc
 * Date: 2017/12/11
 */
public class LinearTransition extends AbstractTransition {

	public static final float[] MASK_BLACK = new float[] {0, 0, 0, 1};
	public static final float[] MASK_WHITE = new float[] {1, 1, 1, 1};

	private long mDuration;

	private int uMaskLoc;
	private float[] mMask;

	private float mProgress;
	private int uProgressLoc;

	public LinearTransition(Context context, Looper looper) {
		super(context, looper);

		mDuration = TransitionItem.DEFAULT_TIME / 2;

		createProgram(R.raw.vertex_shader, R.raw.fragment_linear_transition);
	}

	@Override
	public void setValue(Object value) {
		mMask = (float[])value;
	}

	@Override
	protected void onGetUniformLocation(int program) {
		super.onGetUniformLocation(program);
		uMaskLoc = GLES20.glGetUniformLocation(program, "mask");
		uProgressLoc = GLES20.glGetUniformLocation(program, "progress");
	}

	@Override
	protected void onSetUniformData() {
		super.onSetUniformData();
		if (mMask != null) {
			GLES20.glUniform4fv(uMaskLoc, 1, mMask, 0);
		}
		GLES20.glUniform1f(uProgressLoc, mProgress);
	}

	@Override
	protected boolean shouldStart(long position, long duration) {
		return position > duration - mDuration;
	}

	@Override
	protected long getDuration() {
		return mDuration;
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		if (!isStartAnim) {
			onVideoFinish();
		} else {
			onTransitionFinish();
		}
	}

	@Override
	protected void onProgressChanged(float progress) {
		if (isStartAnim) {
			mProgress = 1 - progress;
		} else {
			mProgress = progress;
		}
	}

	@Override
	public void reset() {
		super.reset();
		mProgress = 0;
	}
}
