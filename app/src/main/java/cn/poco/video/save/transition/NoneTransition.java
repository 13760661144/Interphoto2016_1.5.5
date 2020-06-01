package cn.poco.video.save.transition;

import android.content.Context;

/**
 * Created by: fwc
 * Date: 2017/12/28
 */
public class NoneTransition extends LinearTransition {

	public NoneTransition(Context context) {
		super(context);
	}

	@Override
	protected boolean shouldStart(long position, long duration) {
		return false;
	}
}
