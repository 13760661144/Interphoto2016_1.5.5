package cn.poco.video.render.transition;

import android.content.Context;
import android.os.Looper;

/**
 * Created by: fwc
 * Date: 2017/12/12
 */
public class NoneTransition extends LinearTransition {

	public NoneTransition(Context context, Looper looper) {
		super(context, looper);

		isUpdateEnable = false;
	}

	@Override
	protected boolean canVideoStartAnim() {
		return false;
	}

	@Override
	protected void onEnd() {
		super.onEnd();
		onTransitionFinish();
	}
}
