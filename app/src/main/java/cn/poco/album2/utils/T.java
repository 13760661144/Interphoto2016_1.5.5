package cn.poco.album2.utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Created by: fwc
 * Date: 2016/8/29
 */
public class T {

	/**
	 * 是否需要显示Toast
	 */
	public static boolean isShow = true;

	private static Toast sToast = null;

	private static void check(Context context) {
		if (sToast == null) {
			sToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
		}
	}

	/**
	 * 短时间显示Toast
	 *
	 * @param context 上下文
	 * @param message 消息
	 */
	public static void showShort(Context context, CharSequence message) {
		if (isShow) {
			check(context);
			sToast.setDuration(Toast.LENGTH_SHORT);
			sToast.setText(message);
			sToast.show();
		}
	}

	/**
	 * 短时间显示Toast
	 *
	 * @param context 上下文
	 * @param message 消息
	 */
	public static void showShort(Context context, @StringRes int message) {
		if (isShow) {
			check(context);
			sToast.setDuration(Toast.LENGTH_SHORT);
			sToast.setText(message);
			sToast.show();
		}
	}

	/**
	 * 长时间显示Toast
	 *
	 * @param context 上下文
	 * @param message 消息
	 */
	public static void showLong(Context context, CharSequence message) {
		if (isShow) {
			check(context);
			sToast.setDuration(Toast.LENGTH_LONG);
			sToast.setText(message);
			sToast.show();
		}
	}

	/**
	 * 长时间显示Toast
	 *
	 * @param context 上下文
	 * @param message 消息
	 */
	public static void showLong(Context context, @StringRes int message) {
		if (isShow) {
			check(context);
			sToast.setDuration(Toast.LENGTH_LONG);
			sToast.setText(message);
			sToast.show();
		}
	}

	/**
	 * 自定义显示Toast时间
	 *
	 * @param context 上下文
	 * @param message 消息
	 * @param duration 显示时间
	 */
	public static void show(Context context, CharSequence message, int duration) {
		if (isShow) {
			check(context);
			sToast.setDuration(duration);
			sToast.setText(message);
			sToast.show();
		}
	}

	/**
	 * 自定义显示Toast时间
	 *
	 * @param context 上下文
	 * @param message 消息
	 * @param duration 显示时间
	 */
	public static void show(Context context, @StringRes int message, int duration) {
		if (isShow) {
			check(context);
			sToast.setDuration(duration);
			sToast.setText(message);
			sToast.show();
		}
	}
}
