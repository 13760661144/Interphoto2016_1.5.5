package cn.poco.camera.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/10/9
 */
public class ShutterView extends View {

	private Paint mPaint;

	private int mCircleRadius;
	private int mRingWidth;

	private RectF mRectF;

	public ShutterView(Context context) {
		super(context);

		init();
	}

	private void init() {

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.WHITE);

		mCircleRadius = ShareData.PxToDpi_xhdpi(50);
		mRingWidth = ShareData.PxToDpi_xhdpi(12);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int gap = ShareData.PxToDpi_xhdpi(6);
		mRectF = new RectF(gap, gap, w - gap, h - gap);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		int width = getWidth();
		int height = getHeight();

		mPaint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(width / 2f, height / 2f, mCircleRadius, mPaint);

		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mRingWidth);
		canvas.drawArc(mRectF, 0, 360, false, mPaint);
	}
}
