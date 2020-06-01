package cn.poco.capture2.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;

import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/10/11
 */
public class AnimShutterView extends View {

	private static final int COLOR_RED = 0xffff3e36;
	private static final int COLOR_WHITE = 0x99ffffff;

	/**
	 * 圆中心距离底部的位置
	 */
	private float mCenterY; // 117 -> 280

	private static final int START_CENTERY = ShareData.PxToDpi_xhdpi(117);
	private static final int END_CENTERY = ShareData.PxToDpi_xhdpi(280);

	/**
	 * 圆的半径
	 */
	private float mCircleRadius; // 50 -> 32

	private static final int START_CIRCLE_RADIUS = ShareData.PxToDpi_xhdpi(50);
	private static final int END_CIRCLE_RADIUS = ShareData.PxToDpi_xhdpi(32);

	/**
	 * 圆环的宽度
	 */
	private float mRingWidth; // 12 -> 6

	private static final int START_RING_WIDTH = ShareData.PxToDpi_xhdpi(12);
	private static final int END_RING_WIDTH = ShareData.PxToDpi_xhdpi(6);

	/**
	 * 圆环和圆的间隔
	 */
	private float mInterval; // 4 -> 2

	private static final int START_INTERVAL = ShareData.PxToDpi_xhdpi(4);
	private static final int END_INTERVAL = ShareData.PxToDpi_xhdpi(2);

	private Paint mPaint;
	private RectF mDrawRectF;

	private boolean mShow = false;

	private OnClickListener mOnClickListener;
	private RectF mRectF;


	public AnimShutterView(Context context) {
		super(context);

		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);

		mCenterY = START_CENTERY;
		mCircleRadius = START_CIRCLE_RADIUS;
		mRingWidth = START_RING_WIDTH;
		mInterval = START_INTERVAL;

		mDrawRectF = new RectF();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int size = ShareData.PxToDpi_xhdpi(80);
		float left = (w - size) / 2f;
		float top = h - END_CENTERY - size / 2f;
		mRectF = new RectF(left, top, left + size, top + size);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (mShow) {

			float centerX = getWidth() / 2f;
			float centerY = getHeight() - mCenterY;

			mPaint.setColor(COLOR_RED);
			mPaint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(centerX, centerY, mCircleRadius, mPaint);

			mPaint.setColor(COLOR_WHITE);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(mRingWidth);

			float ringRadius = mCircleRadius + mInterval + (mRingWidth - 1) / 2f;
			mDrawRectF.set(centerX - ringRadius, centerY - ringRadius, centerX + ringRadius, centerY + ringRadius);
			canvas.drawArc(mDrawRectF, 0, 360, false, mPaint);
		}
	}

	public void setShow(boolean show) {
		if (mShow != show) {
			mShow = show;
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	public Animator getScaleAnimator(boolean down) {

		float start = 1;
		float end = 0;
		if (!down) {
			end = 1;
			start = 0;
		}

		ValueAnimator animator = ValueAnimator.ofFloat(start, end);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float value = (float)animation.getAnimatedValue();

				mCenterY = START_CENTERY + (END_CENTERY - START_CENTERY) * value;
				mCircleRadius = START_CIRCLE_RADIUS + (END_CIRCLE_RADIUS - START_CIRCLE_RADIUS) * value;
				mRingWidth = START_RING_WIDTH + (END_RING_WIDTH - START_RING_WIDTH) * value;
				mInterval = START_INTERVAL + (END_INTERVAL - START_INTERVAL) * value;

				ViewCompat.postInvalidateOnAnimation(AnimShutterView.this);
			}
		});
		animator.setDuration(350);

		return animator;
	}

	public void setFinalState() {
		mCenterY = END_CENTERY;
		mCircleRadius = END_CIRCLE_RADIUS;
		mRingWidth = END_RING_WIDTH;
		mInterval = END_INTERVAL;

		ViewCompat.postInvalidateOnAnimation(AnimShutterView.this);
	}

	@Override
	public void setOnClickListener(OnClickListener onClickListener) {
		mOnClickListener = onClickListener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (mShow) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (mRectF.contains(x, y)) {
						return true;
					}

					break;
				case MotionEvent.ACTION_UP:
					if (mRectF.contains(x, y) && mOnClickListener != null) {
						mOnClickListener.onClick(this);
						return true;
					}
					break;
			}
		}

		return super.onTouchEvent(event);
	}
}
