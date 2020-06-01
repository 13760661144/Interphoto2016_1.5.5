package cn.poco.beautify.animations;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import cn.poco.tianutils.ShareData;

public class Circle extends View {

	private Paint mPaint;

	private float mRadius;

	private int mDefaultSize;

	public Circle(Context context) {
		super(context);
		init();
	}

	public Circle(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Circle(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {

		mDefaultSize = ShareData.PxToDpi_xhdpi(32);

		mRadius = ShareData.PxToDpi_xhdpi(15);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.WHITE);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = measure(widthMeasureSpec);
		int height = measure(heightMeasureSpec);
		int size = Math.min(width, height);
		setMeasuredDimension(size, size);
	}

	/**
	 * 测量大小
	 *
	 * @param measureSpec 测量参数
	 * @return 最终大小
	 */
	private int measure(int measureSpec) {
		int result;
		int mode = MeasureSpec.getMode(measureSpec);
		int size = MeasureSpec.getSize(measureSpec);
		if (mode == MeasureSpec.EXACTLY) {
			result = size;
		} else {
			result = mDefaultSize;
			if(mode == MeasureSpec.AT_MOST){
				result = Math.min(result, size);
			}
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(getWidth()/2, getHeight()/2, mRadius, mPaint);
	}
}
