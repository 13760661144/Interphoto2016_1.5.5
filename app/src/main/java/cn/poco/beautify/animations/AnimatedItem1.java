package cn.poco.beautify.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by shine on 16/7/16.
 */
public class AnimatedItem1 extends FrameLayout{
    protected ImageView textAnimation;
    protected ImageView deleteButton;
    protected TargetView targetView;

    private float translationX;
    private float translationY;

    public AnimatedItem1(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        textAnimation = new ImageView(context);
        LayoutParams layoutParams1 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textAnimation = new ImageView(context);

        textAnimation.setImageResource(R.drawable.selected_item_selector);
        textAnimation.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.addView(textAnimation, layoutParams1);

        deleteButton = new ImageView(context);
        LayoutParams layoutParams2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteButton.setImageResource(R.drawable.delete_btn);
        deleteButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.addView(deleteButton, layoutParams2);
        deleteButton.setScaleX(0.0f);
        deleteButton.setScaleY(0.0f);

        targetView = new TargetView(context);
        LayoutParams layoutParams3 = new LayoutParams(ShareData.PxToDpi_xhdpi(76), ShareData.PxToDpi_xhdpi(76));
        this.addView(targetView, layoutParams3);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (textAnimation != null) {
            int leftPostion = 0;
            int topPosition = (this.getMeasuredHeight() - textAnimation.getMeasuredHeight()) / 2 + ShareData.PxToDpi_xhdpi(5);
            int rightPosition = leftPostion + textAnimation.getMeasuredWidth();
            int bottomPostion = topPosition + textAnimation.getMeasuredHeight();
            textAnimation.layout(leftPostion, topPosition, rightPosition, bottomPostion);
        }

        if (deleteButton != null) {
            int leftPosition = this.getMeasuredWidth() - deleteButton.getMeasuredWidth();
            int topPosition = this.getMeasuredHeight() / 2  - textAnimation.getMeasuredHeight() / 2 - ShareData.PxToDpi_xhdpi(8);
            int rightPosition = leftPosition + deleteButton.getMeasuredWidth();
            int bottomPosition = topPosition + deleteButton.getMeasuredHeight();
            deleteButton.layout(leftPosition, topPosition, rightPosition, bottomPosition);
        }

        if (targetView != null) {
            int leftPosition = this.getMeasuredWidth() / 2 - targetView.getMeasuredWidth() / 2;
            int topPosition = this.getMeasuredHeight() - targetView.getMeasuredHeight();
            int rightPosition = leftPosition + targetView.getMeasuredWidth();
            int bottomPosition = topPosition + targetView.getMeasuredHeight();
            targetView.layout(leftPosition, topPosition, rightPosition, bottomPosition);
        }
    }


    public Animator getAlphaInAnimator() {
        if (this.targetView.circleView != null) {
            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this.targetView.circleView, "Alpha", 0.0f, 1.0f);
            alphaAnimator.setDuration(1000);
            alphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    alphaAnimator.removeAllListeners();
                    if(AnimatedItem1.this.targetView.outerCircleView != null)
                    {
                        AnimatedItem1.this.targetView.outerCircleView.setVisibility(VISIBLE);
                        AnimatedItem1.this.targetView.outerCircleView.startAlphaAnimation(AnimatedItem1.this.textAnimation);
                        AnimatedItem1.this.targetView.outerCircleView.startdraw(ShareData.PxToDpi_xhdpi(26), ShareData.PxToDpi_xhdpi(26));
                    }
                }
            });
            return alphaAnimator;
        }
        return null;
    }

    public Animator getDeleteBtnScaleAnimator() {
        AnimatorSet scaleAnimatorSet = null;
        if (this.deleteButton != null) {
            this.deleteButton.setVisibility(VISIBLE);
            ObjectAnimator scaleAnimatorX = ObjectAnimator.ofFloat(this.deleteButton, "ScaleX", 0.0f, 1.0f);
            ObjectAnimator scaleAnimatorY = ObjectAnimator.ofFloat(this.deleteButton, "ScaleY", 0.0f, 1.0f);

            scaleAnimatorSet = new AnimatorSet();
            scaleAnimatorSet.playTogether(scaleAnimatorX, scaleAnimatorY);
            scaleAnimatorSet.setDuration(300);
        }
        return scaleAnimatorSet;
    }

    public Animator getAlphaOutAnimator() {
        ObjectAnimator alphaAnimator = null;
        if (this.targetView.circleView != null) {
            alphaAnimator = ObjectAnimator.ofFloat(this.targetView.circleView, "Alpha", 1.0f, 0.6f);
            alphaAnimator.setDuration(1300);
        }
        return alphaAnimator;
    }

    public Animator getTranslatedAnimator() {
        AnimatorSet translatedAnimatorSet = null;
        if (deleteButton != null && targetView != null) {
            translationX = deleteButton.getX() - this.targetView.getX() + (deleteButton.getMeasuredWidth() - this.targetView.getMeasuredWidth()) / 2;
            ObjectAnimator translatedAnimatorX = ObjectAnimator.ofFloat(this.targetView, "TranslationX", 0.0f, translationX);

            translationY = -(this.targetView.getY() - deleteButton.getY() - (deleteButton.getMeasuredHeight() - this.targetView.getMeasuredHeight()) / 2);
            ObjectAnimator translatedAnimatorY = ObjectAnimator.ofFloat(this.targetView, "TranslationY", 0.0f, translationY);

            translatedAnimatorSet = new AnimatorSet();
            translatedAnimatorSet.playTogether(translatedAnimatorX, translatedAnimatorY);
            translatedAnimatorSet.setDuration(500);
        }
        return translatedAnimatorSet;
    }

    public Animator getAlphaToInvisibleAnimator() {
        Animator alphaAnimator = null;
        if (targetView.circleView != null) {
            alphaAnimator = ObjectAnimator.ofFloat(this.targetView.circleView, "Alpha", 0.6f, 0.0f);
            alphaAnimator.setDuration(400);
        }
        return alphaAnimator;
    }


    public Animator getAnimatedItemScaleDownAnimator() {
        AnimatorSet scaleAnimatorSet = null;
        if (this != null) {
            Animator scaleDownAnimatorX = ObjectAnimator.ofFloat(this, "ScaleX", 1.0f, 0.0f);
            Animator scaleDownAnimatorY = ObjectAnimator.ofFloat(this, "ScaleY", 1.0f, 0.0f);

            this.setPivotX(0);
            this.setPivotY(this.getMeasuredHeight() / 2);
            scaleAnimatorSet = new AnimatorSet();
            scaleAnimatorSet.playTogether(scaleDownAnimatorX, scaleDownAnimatorY);
        }
        return scaleAnimatorSet;
    }

    public void resetAnimation() {
        if (textAnimation != null && targetView != null && deleteButton != null) {
            this.textAnimation.setSelected(false);
            this.targetView.setTranslationX(this.targetView.getTranslationX() - translationX);
            this.targetView.setTranslationY(this.targetView.getTranslationY() - translationY);

            this.setScaleX(1.0f);
            this.setScaleY(1.0f);

            this.deleteButton.setScaleX(0.0f);
            this.deleteButton.setScaleY(0.0f);
        }
    }


    public static class TargetView extends RelativeLayout {
        protected WhiteDotView circleView;
        protected OuterCicleView outerCircleView;

        public TargetView(Context context) {
            super(context);
            init(context);
        }

        private void init(Context context) {
            circleView = new WhiteDotView(context);
            LayoutParams layoutParams1 = new LayoutParams(ShareData.PxToDpi_xhdpi(26), ShareData.PxToDpi_xhdpi(26));
            layoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            addView(circleView, layoutParams1);
            circleView.setAlpha(0.0f);
            outerCircleView = new OuterCicleView(context);
            LayoutParams layoutParams2 = new LayoutParams(ShareData.PxToDpi_xhdpi(76), ShareData.PxToDpi_xhdpi(76));
            layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            addView(outerCircleView, layoutParams2);
            outerCircleView.setVisibility(INVISIBLE);
        }
    }


    public static class WhiteDotView extends View {
        private Paint mPaint;

        public WhiteDotView(Context context) {
            super(context);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.WHITE);
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(this.getMeasuredWidth() / 2, this.getMeasuredHeight() / 2, this.getMeasuredWidth() / 2, mPaint);
        }
    }

    public static class OuterCicleView extends View {
        public interface NotifyAnimation {
            void keepAnimation();
        }

        public int originWidth;
        public int originHeight;
        public int currentWidth = ShareData.PxToDpi_xhdpi(26);
        public int currentHeight = ShareData.PxToDpi_xhdpi(26);
        private int maxWidth = ShareData.PxToDpi_xhdpi(76);
        private int maxHeight = ShareData.PxToDpi_xhdpi(76);

        private Paint mPaint;
        private Paint wavePaint;
        private boolean startWavePaint;
        private long animationDuration = 1000;
        private long animationEndTime;
        private long startOrigin;

        private NotifyAnimation mCallBack;



        public OuterCicleView(Context context) {
            super(context);
            init();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(currentWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(currentHeight, MeasureSpec.EXACTLY));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (!startWavePaint) {
                canvas.drawCircle(this.getMeasuredWidth() / 2, this.getMeasuredHeight() / 2, this.getMeasuredWidth() / 2, mPaint);
            } else {
                canvas.drawCircle(currentWidth / 2, currentWidth / 2, currentWidth / 2, wavePaint);
            }

            long currentTime = System.currentTimeMillis();

            if (currentTime <= animationEndTime) {
                long elapsedTime = currentTime - startOrigin;
                double rate = (double)elapsedTime / (double)animationDuration;

                currentWidth = (int)(originWidth + (rate * (maxWidth - originWidth)));
                currentHeight = (int)(originHeight + (rate * (maxHeight - originHeight)));

                if (currentWidth >= ShareData.PxToDpi_xhdpi(56)) {
                    startWavePaint = true;
                }
                invalidate();
                requestLayout();
            }

        }

        private void init () {
            mPaint = new Paint();
            mPaint.setColor(Color.WHITE);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);

            wavePaint  = new Paint();
            wavePaint.setColor(Color.WHITE);
            wavePaint.setAntiAlias(true);
            wavePaint.setStyle(Paint.Style.STROKE);
            wavePaint.setStrokeWidth(4);
            wavePaint.setStrokeJoin(Paint.Join.ROUND);
        }

        public void startdraw(int width, int height) {
            originWidth = width;
            originHeight = height;

            currentWidth = width;
            currentHeight = height;

            startOrigin = System.currentTimeMillis();
            animationEndTime = startOrigin + animationDuration;
            requestLayout();
            invalidate();
        }

        public void setmCallBack(NotifyAnimation mCallBack) {
            this.mCallBack = mCallBack;
        }

        public void removeCallBack() {
            this.mCallBack = null;
        }

        public void startAlphaAnimation(final View v) {
            if (OuterCicleView.this != null) {
                final ObjectAnimator animator = ObjectAnimator.ofFloat(this, "Alpha", 1.0f, 0.0f);
                animator.setDuration(animationDuration);
                animator.start();
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animator.removeAllListeners();
                        if (v != null) {
                            v.setSelected(true);
                        }
                        if (OuterCicleView.this != null) {
                            OuterCicleView.this.setVisibility(GONE);
                        }
                        startWavePaint = false;
                        if (mCallBack != null) {
                            mCallBack.keepAnimation();
                        }
                    }
                });
            }
        }
    }

}
