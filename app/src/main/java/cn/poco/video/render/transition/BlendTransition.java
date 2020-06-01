package cn.poco.video.render.transition;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Looper;

import cn.poco.interphoto2.R;

/**
 * Created by: fwc
 * Date: 2017/12/12
 */
public class BlendTransition extends AbstractTransition {

	private long mDuration;

	private float mAlpha;
	private int uAlphaLoc;

	public BlendTransition(Context context, Looper looper) {
		super(context, looper);

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
		boolean start = position > duration - mDuration;
		if (start) {
			onStartNextVideo();
		}
		return start;
	}

	@Override
	protected long getDuration() {
		return mDuration;
	}

	@Override
	public boolean shouldRenderNext() {
		return isStart;
	}

	@Override
	protected void onProgressChanged(float progress) {
		mAlpha = progress;
	}

	@Override
	protected boolean canVideoStartAnim() {
		return false;
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
	protected void onEnd() {
		super.onEnd();
		onVideoFinish();
		onTransitionFinish();
	}

	@Override
	public void onChangeSurface() {
		super.onChangeSurface();
		isStart = false;
		mAlpha = 0;
	}

	@Override
	public void reset() {
		super.reset();
		mAlpha = 0;
	}
}
