package cn.poco.album2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.FloatRange;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by: fwc
 * Date: 2016/9/1
 */
public class TabIndicator extends View {

	private Paint mPaint;

	private int mColor;

	private int mWidth;

	private int mHeight;

	private int mCurrent;

	public TabIndicator(Context context) {
		super(context);
		init();
	}

	public TabIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);

		mColor = Color.parseColor("#ffc533");
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mWidth = w;
		mHeight = h;

		mCurrent = 0;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mPaint.setColor(mColor);

		canvas.drawRect(mCurrent, 0, mCurrent + mWidth / 2, mHeight, mPaint);
	}

	public void setProgress(@FloatRange(from = 0.0, to = 1.0) float progress) {
		mCurrent = (int)(mWidth / 2 * progress);
		invalidate();
	}
}
