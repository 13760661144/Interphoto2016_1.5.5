package cn.poco.camera.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by: fwc
 * Date: 2017/5/24
 */
public class BlackMaskView extends View {

	private int mWidth;
	private int mHeight;

	private Paint mPaint;

	private int mTopHeight;
	private int mBottomHeight;

	private AnimatorSet mAnimatorSet;

	private boolean isRotation = false;

	public BlackMaskView(Context context) {
		this(context, null);
	}

	public BlackMaskView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.BLACK);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
		mHeight = h;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (isRotation) {
			canvas.drawRect(0, 0, mTopHeight, mHeight, mPaint);
			canvas.drawRect(mBottomHeight, 0, mWidth, mHeight, mPaint);
		} else {
			// 画上面
			canvas.drawRect(0, 0, mWidth, mTopHeight, mPaint);

			// 画下面
			canvas.drawRect(0, mHeight - mBottomHeight, mWidth, mHeight, mPaint);
		}
	}

	public void setRotation(boolean isRotation) {
		this.isRotation = isRotation;
	}

	public void setTopAndBottom(int top, int bottom) {

		if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
			mAnimatorSet.cancel();
			mAnimatorSet = null;
		}

		if (isRotation) {
			mTopHeight = top;
			mBottomHeight = bottom;
			ViewCompat.postInvalidateOnAnimation(BlackMaskView.this);

		} else {

			mAnimatorSet = new AnimatorSet();

			ValueAnimator valueAnimator1 = ValueAnimator.ofInt(mTopHeight, top);
			valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					mTopHeight = (int)animation.getAnimatedValue();

					ViewCompat.postInvalidateOnAnimation(BlackMaskView.this);
				}
			});

			ValueAnimator valueAnimator2 = ValueAnimator.ofInt(mBottomHeight, bottom);
			valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					mBottomHeight = (int)animation.getAnimatedValue();

					ViewCompat.postInvalidateOnAnimation(BlackMaskView.this);
				}
			});

			mAnimatorSet.setDuration(200);
			mAnimatorSet.setInterpolator(new LinearInterpolator());
			mAnimatorSet.playTogether(valueAnimator1, valueAnimator2);
			mAnimatorSet.start();
		}
	}
}
