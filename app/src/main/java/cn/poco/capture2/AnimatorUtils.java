package cn.poco.capture2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by: fwc
 * Date: 2017/12/28
 */
public class AnimatorUtils {

	public static final int DEFAULT_DURATION = 350;

	private AnimatorUtils() {

	}

	public static void addView(ViewGroup parent, final View view) {
		parent.addView(view);
		ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
		animator.setDuration(DEFAULT_DURATION);
		animator.start();
	}

	public static void removeView(final ViewGroup parent, final View view, long delay) {
		ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1, 0);
		animator.setDuration(DEFAULT_DURATION);
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				parent.removeView(view);
			}
		});
		animator.setStartDelay(delay);
		animator.start();
	}

	public static void showView(View view, long duration) {
		if (view.getVisibility() != View.VISIBLE) {
			view.setVisibility(View.VISIBLE);
			view.setAlpha(0);
			view.animate().alpha(1).setDuration(duration);
		}
	}

	public static void hideView(final View view, long duration) {
		if (view.getVisibility() == View.VISIBLE) {
			view.animate().alpha(0).setDuration(duration).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					view.animate().setListener(null);
					view.setVisibility(View.GONE);
				}
			});
		}
	}
}
