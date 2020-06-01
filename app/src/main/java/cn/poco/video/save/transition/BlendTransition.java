package cn.poco.video.save.transition;

import android.content.Context;
import android.opengl.GLES20;

import cn.poco.interphoto2.R;
import cn.poco.video.render.transition.TransitionItem;

/**
 * Created by: fwc
 * Date: 2017/12/28
 */
public class BlendTransition extends AbstractTransition {

	private long mDuration;
	private long mStartPosition;

	private float mAlpha;
	private int uAlphaLoc;

	public BlendTransition(Context context) {
		super(context);

		mDuration = TransitionItem.DEFAULT_TIME;
		createProgram(R.raw.vertex_shader, R.raw.fragment_blend_transition);
	}

	@Override
	protected void onGetUniformLocation(int program) {
		super.onGetUniformLocation(program);
		uAlphaLoc = GLES20.glGetUniformLocation(program, "alpha");
	}

	@Override
	protected void onSetUniformData() {
		super.onSetUniformData();
		GLES20.glUniform1f(uAlphaLoc, mAlpha);
	}

	@Override
	protected boolean shouldStart(long position, long duration) {
		boolean start = position > duration - TransitionItem.DEFAULT_TIME;
		if (start) {
			mStartPosition = position;
			mDuration = duration - position;
			onStartNextVideo();
		}
		return start;
	}

	@Override
	public boolean shouldRenderNext() {
		return isStart;
	}

	@Override
	protected float getProgress(long position, long duration) {
		float progress = (position - mStartPosition) * 1f / mDuration;
		return checkRange(progress, 0, 0.999f);
	}

	@Override
	protected void onProgressChanged(float progress) {
		mAlpha = progress;
	}

	@Override
	public boolean isBlendTransition() {
		return true;
	}

	@Override
	public float getProgress() {
		return mAlpha;
	}

	@Override
	public void onVideoFinish(int index) {
		super.onVideoFinish(index);
		onEnd();
	}

	@Override
	public void reset() {
		super.reset();
		mAlpha = 0;
	}
}
