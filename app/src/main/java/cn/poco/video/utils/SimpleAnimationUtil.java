package cn.poco.video.utils;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by Shine on 2017/8/31.
 */

public class SimpleAnimationUtil {

    /**
     * 淡入
     */
    public static void alphaIn(View v, int animationDuration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, "alpha", 0, 1);
        animator.setDuration(animationDuration);
        animator.start();
    }

    /**
     * 淡出
     */
    public static void alphaOut(View v, int animationDuration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, "alpha", 1, 0);
        animator.setDuration(animationDuration);
        animator.start();
    }


}
