package cn.poco.video.save.transition;

import android.content.Context;
import android.opengl.GLES20;

import cn.poco.interphoto2.R;
import cn.poco.video.render.transition.TransitionItem;

/**
 * Created by: fwc
 * Date: 2017/12/27
 */
public class LinearTransition extends AbstractTransition {

	public static final float[] MASK_BLACK = new float[] {0, 0, 0, 1};
	public static final float[] MASK_WHITE = new float[] {1, 1, 1, 1};

	private int uMaskLoc;
	private float[] mMask;

	private float mProgress;
	private int uProgressLoc;

	private long mStartPosition;
	private long mDuration;
	private boolean isNext;

	public LinearTransition(Context context) {
		super(context);

		createProgram(R.raw.vertex_shader, R.raw.fragment_linear_transition);
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
		boolean start = position > duration - TransitionItem.DEFAULT_TIME / 2;

		if (start) {
			mStartPosition = position;
			mDuration = duration - position;
		}
		return start;
	}

	@Override
	public void setValue(Object value) {
		mMask = (float[])value;
	}

	@Override
	protected float getProgress(long position, long duration) {
		float progress;
		if (isNext) {
			progress = position * 0.5f / mDuration + 0.5f;
			progress = checkRange(progress, 0.5f, 1);
		} else {
			progress = (position - mStartPosition) * 0.5f / mDuration;
			progress = checkRange(progress, 0, 0.5f);
		}

		return progress;
	}

	@Override
	public void onVideoFinish(int index) {
		super.onVideoFinish(index);

		isNext = true;
		mDuration = TransitionItem.DEFAULT_TIME / 2;
	}

	@Override
	public void reset() {
		super.reset();
		mProgress = 0;
		isNext = false;
	}

	@Override
	protected void onProgressChanged(float progress) {
		if (isNext) {
			mProgress = 2 * (1 - progress);
		} else {
			mProgress = progress * 2;
		}
	}
}
