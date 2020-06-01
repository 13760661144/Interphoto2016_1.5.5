package cn.poco.beautify.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by shine on 16/7/3.
 */

public class TextAnim2 extends LinearLayout {
    private ImageView sceneView;
    private LinearLayout subContainer;
    private ImageView addButton;

    private AnimatedItem1 animatedItem1;
    private ImageView animatedItem2;
    private ImageView animatedItem3;

    private float anim2TranslationX;
    private float anim3TranslationX;


    public TextAnim2(Context context) {
        super(context);
        this.setOrientation(VERTICAL);
        initView(context);
    }

    private void initView(Context context) {
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sceneView = new ImageView(context);
        sceneView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        sceneView.setImageResource(R.drawable.background);
        this.addView(sceneView, params1);

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(204));
        subContainer = new LinearLayout(context);
        subContainer.setOrientation(HORIZONTAL);
        subContainer.setGravity(Gravity.CENTER_VERTICAL);
        subContainer.setBackgroundColor(Color.BLACK);
        this.addView(subContainer, params2);

        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params3.leftMargin = ShareData.PxToDpi_xhdpi(30);
        params3.topMargin = ShareData.PxToDpi_xhdpi(4);
        addButton = new ImageView(context);
        addButton.setImageResource(R.drawable.animation_add_btn);
        addButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        subContainer.addView(addButton, params3);

        LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(216), ShareData.PxToDpi_xhdpi(146));
        params4.leftMargin = ShareData.PxToDpi_xhdpi(35);
        animatedItem1 = new AnimatedItem1(context);
        subContainer.addView(animatedItem1, params4);

        animatedItem2 = new ImageView(context);
        LinearLayout.LayoutParams params5 = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(216), ViewGroup.LayoutParams.WRAP_CONTENT);
        params5.leftMargin = ShareData.PxToDpi_xhdpi(30);
        animatedItem2.setImageResource(R.drawable.item2);
        animatedItem2.setScaleType(ImageView.ScaleType.CENTER_CROP);
        subContainer.addView(animatedItem2, params5);

        animatedItem3 = new ImageView(context);
        LinearLayout.LayoutParams params6 = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(189), ViewGroup.LayoutParams.WRAP_CONTENT);
        params6.leftMargin = ShareData.PxToDpi_xhdpi(30);
        animatedItem3.setImageResource(R.drawable.item3);
        animatedItem3.setScaleType(ImageView.ScaleType.CENTER_CROP);
        subContainer.addView(animatedItem3, params6);
    }

    public void startFirstAnimation() {
        if (animatedItem1 != null) {
            Animator whiteDotAlpha = animatedItem1.getAlphaInAnimator();
            if (whiteDotAlpha != null) {
                whiteDotAlpha.start();
                final AnimatedItem1.OuterCicleView outerCicleView = animatedItem1.targetView.outerCircleView;
                if (outerCicleView != null) {
                    outerCicleView.setmCallBack(new AnimatedItem1.OuterCicleView.NotifyAnimation() {
                        @Override
                        public void keepAnimation() {
                            startRestAnimation();
                            outerCicleView.removeCallBack();
                        }
                    });
                }
            }
        }
    }

    public void release() {
        if (animatedItem1 != null) {
            animatedItem1.clearAnimation();
            animatedItem1.targetView.outerCircleView.removeCallBack();
            animatedItem1.targetView.outerCircleView = null;
            animatedItem1 = null;
        }

        if (animatedItem2 != null) {
            animatedItem2.clearAnimation();
        }

        if (animatedItem3 != null) {
            animatedItem3.clearAnimation();
        }

    }

    private Animator getAnimatedItem2Animator() {
        ObjectAnimator animator = null;
        if (animatedItem1 != null) {
            anim2TranslationX = animatedItem1.getWidth() + ((LayoutParams)animatedItem2.getLayoutParams()).leftMargin;
            animator = ObjectAnimator.ofFloat(this.animatedItem2, "TranslationX", 0.0f, -anim2TranslationX);
            animator.setDuration(1000);
        }
        return animator;

    }

    private Animator getAnimatedItem3Animator() {
        ObjectAnimator animator = null;
        if (animatedItem2 != null) {
            anim3TranslationX = animatedItem2.getWidth() + ((LayoutParams)animatedItem3.getLayoutParams()).leftMargin;
            animator = ObjectAnimator.ofFloat(this.animatedItem3, "TranslationX", 0.0f, -anim3TranslationX);
            animator.setDuration(1000);
        }
        return animator;
    }


    private void resetAnimatedItem() {
        if (animatedItem2 != null && animatedItem3 != null) {
            animatedItem2.setTranslationX(animatedItem2.getTranslationX() + anim2TranslationX);
            animatedItem3.setTranslationX(animatedItem3.getTranslationX() + anim3TranslationX);
        }
    }

    public void reset() {
        if (animatedItem1 != null) {
            animatedItem1.resetAnimation();
            resetAnimatedItem();
        }
    }

    public void startRestAnimation() {
        if (animatedItem1 != null) {
            List<Animator> animatorList = new ArrayList<>();

            final AnimatorSet animatorSet = new AnimatorSet();

            Animator scaleDelete = animatedItem1.getDeleteBtnScaleAnimator();
            Animator whiteDotAlphaOut = animatedItem1.getAlphaOutAnimator();
            Animator translatedSet = animatedItem1.getTranslatedAnimator();
            Animator alphaToInvisible = animatedItem1.getAlphaToInvisibleAnimator();

            Animator scaleDown = animatedItem1.getAnimatedItemScaleDownAnimator();
            Animator item2Translation = getAnimatedItem2Animator();
            Animator item3Translation = getAnimatedItem3Animator();

            AnimatorSet animatorSet2 = new AnimatorSet();
            animatorSet2.playTogether(scaleDown, item2Translation, item3Translation);
            animatorSet2.setDuration(500);

            if (scaleDelete != null && whiteDotAlphaOut != null && translatedSet != null && alphaToInvisible != null) {
                animatorList.add(scaleDelete);
                animatorList.add(whiteDotAlphaOut);
                animatorList.add(translatedSet);
                animatorList.add(alphaToInvisible);
                animatorList.add(animatorSet2);
                animatorSet.playSequentially(animatorList);
                animatorSet.start();

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animatorSet.removeAllListeners();
                            reset();
                            startFirstAnimation();
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        animatorSet.removeAllListeners();
                    }
                });
            }
        }
    }
}

