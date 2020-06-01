package cn.poco.camera.utils;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.ColorInt;

/**
 * Created by: fwc
 * Date: 2017/5/25
 */
public class DrawableUtils {

	/**
	 * 按压切换颜色
	 *
	 * @param normal 普通颜色
	 * @param pressed 按压颜色
	 * @return StateListDrawable对象
	 */
	public static StateListDrawable colorPressedDrawable(@ColorInt int normal, @ColorInt int pressed) {
		StateListDrawable selector = new StateListDrawable();
		selector.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressed));
		selector.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(normal));
		return selector;
	}
}
