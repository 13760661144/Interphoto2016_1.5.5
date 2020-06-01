package cn.poco.capture2.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v4.view.ViewCompat;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.poco.capture2.AnimatorUtils;
import cn.poco.capture2.model.Snippet;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/12/29
 */
public class ShutterView extends View {

	private static final int COLOR_WHITE = 0x99ffffff;
	private static final int COLOR_RED = 0xffff3e36;
	private static final int COLOR_START_RED = 0xfffe4f4f;
	private static final int COLOR_END_RED = 0xffff2929;

	private static final int START_RED_CIRCLE_SIZE = ShareData.PxToDpi_xhdpi(100);
	private static final int END_RED_CIRCLE_SIZE = ShareData.PxToDpi_xhdpi(80);

	private static final int START_WHITE_RING_STROKE = ShareData.PxToDpi_xhdpi(12);
	private static final int END_WHITE_RING_STROKE = ShareData.PxToDpi_xhdpi(40);

	private Paint mPaint;

	private float mCenterX;
	private float mCenterY;

	private float mRedCircleSize = START_RED_CIRCLE_SIZE;

	private float mWhiteRingStroke = START_WHITE_RING_STROKE;
	private int mWhiteRingRadius;

	private float mProgressStroke;

	private ValueAnimator mValueAnimator;

	private Shader mLinearShader;

	private boolean isRecording;

	private boolean mDrawProgress;

	private RectF mRectF;

	private static final float INTERVAL = 5f;

	private List<Snippet> mSnippets = new ArrayList<>();

	private int mStopLineStroke;
	private int mStopLineHeight;

	private boolean isEnabled = true;

	public ShutterView(Context context) {
		super(context);

		init();
	}

	private void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

		mWhiteRingRadius = ShareData.PxToDpi_xhdpi(60) - 1;
		mProgressStroke = ShareData.PxToDpi_xhdpi(5);
		mStopLineStroke = ShareData.PxToDpi_xhdpi(4);
		mStopLineHeight = ShareData.PxToDpi_xhdpi(18);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCenterX = w / 2f;
		mCenterY = h / 2f;

		mLinearShader = getLinearShader(mRedCircleSize);

		int gap = ShareData.PxToDpi_xhdpi(20);
		mRectF = new RectF(gap, gap, w - gap, h - gap);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		mPaint.setColor(COLOR_WHITE);
		mPaint.setStrokeWidth(mWhiteRingStroke);
		mPaint.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(mCenterX, mCenterY, mWhiteRingRadius, mPaint);

		mPaint.setAlpha(255);
		mPaint.setShader(mLinearShader);
		if (!isEnabled) {
			mPaint.setAlpha((int)(0.7f * 255));
		}
		mPaint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(mCenterX, mCenterY, mRedCircleSize / 2f, mPaint);
		mPaint.setShader(null);

		if (mDrawProgress && !mSnippets.isEmpty()) {
			drawProgress(canvas);

			drawStopLine(canvas);
		}
	}

	private void drawProgress(Canvas canvas) {
		mPaint.setColor(COLOR_RED);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mProgressStroke);

		float startAngle = -90;
		float sweepAngle;
		if (mSnippets.size() == 1) {
			sweepAngle = 360 * mSnippets.get(0).ratio;
			canvas.drawArc(mRectF, startAngle, sweepAngle, false, mPaint);
		} else {
			int size = mSnippets.size();
			float totalAngel = 360 - size * INTERVAL;
			for (Snippet snippet : mSnippets) {
				sweepAngle = totalAngel * snippet.ratio;
				if (startAngle + sweepAngle + INTERVAL > 270) {
					sweepAngle = 270 - startAngle - INTERVAL;
				}
				canvas.drawArc(mRectF, startAngle, sweepAngle, false, mPaint);
				startAngle = startAngle + sweepAngle + INTERVAL;
			}
		}
	}

	private void drawStopLine(Canvas canvas) {
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.WHITE);
		if (!isEnabled) {
			mPaint.setAlpha((int)(0.7f * 255));
		}
		mPaint.setStrokeWidth(mStopLineStroke);
		float startX = mCenterX - mStopLineStroke * 1.5f;
		float startY = mCenterY - mStopLineHeight / 2f;
		float endY = mCenterY + mStopLineHeight / 2f;

		canvas.drawLine(startX, startY, startX, endY, mPaint);

		startX = mCenterX + mStopLineStroke * 1.5f;
		canvas.drawLine(startX, startY, startX, endY, mPaint);
	}

	private Shader getLinearShader(float redCircleSize) {
		return new LinearGradient(0, mCenterY - redCircleSize / 2f,
						   0, mCenterY + redCircleSize / 2f,
						   COLOR_START_RED, COLOR_END_RED, Shader.TileMode.CLAMP);
	}

	public void startRecord() {

		if (isRecording) {
			return;
		}

		isRecording = true;
		mValueAnimator = getAnimator(0, 1);
		mValueAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mDrawProgress = true;
				isEnabled = false;
			}
		});
		mValueAnimator.start();
	}

	public void addSnippet(Snippet snippet) {
		mSnippets.add(snippet);
	}

	public void deleteSnippet() {
		mSnippets.remove(mSnippets.size() - 1);
	}

	public void setSnippets(List<Snippet> snippets) {
		if (snippets != null) {
			mSnippets.addAll(snippets);
		}
	}

	public void stopRecord() {
		if (!isRecording) {
			return;
		}

		isRecording = false;
		mDrawProgress = false;

		mValueAnimator = getAnimator(1, 0);
		mValueAnimator.start();
	}

	public void setEnabled(boolean isEnabled) {
		if (this.isEnabled != isEnabled) {
			this.isEnabled = isEnabled;
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	public boolean canClick() {
		return isEnabled;
	}

	private ValueAnimator getAnimator(float start, float end) {
		ValueAnimator valueAnimator = ValueAnimator.ofFloat(start, end);
		valueAnimator.setDuration(AnimatorUtils.DEFAULT_DURATION);
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float value = (float)animation.getAnimatedValue();
				mRedCircleSize = (END_RED_CIRCLE_SIZE - START_RED_CIRCLE_SIZE) * value + START_RED_CIRCLE_SIZE;
				mWhiteRingStroke = (END_WHITE_RING_STROKE - START_WHITE_RING_STROKE) * value + START_WHITE_RING_STROKE;

				mLinearShader = getLinearShader(mRedCircleSize);

				ViewCompat.postInvalidateOnAnimation(ShutterView.this);
			}
		});

		return valueAnimator;
	}

	public void release() {
		if (mValueAnimator != null) {
			mValueAnimator.removeAllUpdateListeners();
			mValueAnimator.removeAllListeners();
			mValueAnimator.cancel();
			mValueAnimator = null;
		}
	}
}
