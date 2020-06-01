package cn.poco.video.utils;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by Shine on 2017/7/11.
 */

public class VideoAnimationUtil {

    public static void backgroundAlphaIn(View view, long duration, AnimatorListenerAdapter adapter) {
        AnimatorSet animatorSet = new AnimatorSet();
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        animatorSet.playTogether(alphaAnimator);
        animatorSet.setDuration(duration);
        animatorSet.start();
        animatorSet.addListener(adapter);
    }

    public static void backgroundAlphaOut(View view, long duration) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        animatorSet.playTogether(alphaAnimator);
        animatorSet.setDuration(duration);
        animatorSet.start();

        if (view.getVisibility() != View.GONE) {
            view.setVisibility(View.GONE);
        }

    }


}
