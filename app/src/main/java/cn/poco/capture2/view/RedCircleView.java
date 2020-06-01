package cn.poco.capture2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by: fwc
 * Date: 2017/12/29
 */
public class RedCircleView extends View {

	private static final int COLOR_RED = 0xffff433c;

	private Paint mPaint;

	private float mCenterX;
	private float mCenterY;

	private float mRadius;

	public RedCircleView(Context context) {
		super(context);

		init();
	}

	private void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		mPaint.setColor(COLOR_RED);
		mPaint.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mCenterX = w / 2f;
		mCenterY = h / 2f;

		mRadius = mCenterX;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
	}
}
