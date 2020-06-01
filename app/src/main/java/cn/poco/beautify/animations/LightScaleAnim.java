package cn.poco.beautify.animations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import cn.poco.interphoto2.R;

public class LightScaleAnim extends View {

	private Paint mPaint;
	private Paint mCirclePaint;

	private Bitmap mBg;
	private Bitmap mLight;

	private int mBgWidth;
	private int mBgHeight;

	private Rect mBgRect;

	private float mTopCircleCenterX;
	private float mTopCircleCenterY;

	private float mBottomCircleCenterX;
	private float mBottomCircleCenterY;

	private int mCircleRadius;

	private Matrix mMatrix;

	private double mDegree;

	private double mDistance;

	private int mRatio = 1;

	private double mInitialDistance;

	private int mType = START;

	private static final int START = 1;
	private static final int LARGE = 2;
	private static final int SMALL = 3;
	private static final int END = 4;
	private static final int STILL = 5;

	private Timer mTimer;

	private int mCount = 0;

	public LightScaleAnim(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LightScaleAnim(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {

		mBg = BitmapFactory.decodeResource(getResources(), R.drawable.light_scale_bg);
		mLight = BitmapFactory.decodeResource(getResources(), R.drawable.light_scale);

		mBgWidth = mBg.getWidth();
		mBgHeight = mBg.getHeight();

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
		mPaint.setDither(true);
		PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SCREEN);
		mPaint.setXfermode(xfermode);

		mMatrix = new Matrix();

		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setColor(Color.WHITE);

		mCircleRadius = (int)PxToDpi_xhdpi(16);

		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {

				if (mType == START) {
					mCount++;
					if (mCount == 200) {
						mType = LARGE;
					}
				} else if (mType == LARGE) {
					calculatePosition();
					if (mDistance > PxToDpi_xhdpi(300)) {
						mType = SMALL;
						mRatio = -1;
					}

				} else if (mType == SMALL) {
					calculatePosition();
					if (mDistance < mInitialDistance) {
						mType = END;
						mCount = 255;
					}

				} else if (mType == END) {
					mCount--;
					if (mCount == 105) {
						mType = STILL;
						mCount = 15;
					}
				} else if (mType == STILL) {
					mCount--;
					if (mCount == 0) {
						reset();
					}
				}

				postInvalidate();

			}
		}, 0, 8);
	}

	/**
	 * 重置
	 */
	private void reset() {
		mType = START;
		mCount = 0;
		mDistance = mInitialDistance;
		mRatio = 1;
	}

	/**
	 * 停止动画并释放资源
	 */
	public void release() {
		mTimer.cancel();
		mTimer = null;
	}

	private void calculatePosition() {
		double x = mRatio * Math.cos(mDegree);
		double y = mRatio * Math.sin(mDegree);

		mTopCircleCenterX += x;
		mTopCircleCenterY += y;

		mBottomCircleCenterX -= x;
		mBottomCircleCenterY -= y;
		mDistance = calculateDistance();

		mMatrix.setTranslate(-mBgWidth/2-60, -mBgHeight/2+110);

		mMatrix.postScale((float)mDistance/180, (float)mDistance/180,
						  (mTopCircleCenterX+mBottomCircleCenterX) / 2.0f,
						  (mTopCircleCenterY+mBottomCircleCenterY) / 2.0f);
	}

	private double calculateDistance() {
		double x = mTopCircleCenterX - mBottomCircleCenterX;
		double y = mTopCircleCenterY - mBottomCircleCenterY;
		return Math.sqrt(x * x + y * y);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mBgRect = new Rect(0, 0, mBgWidth, mBgHeight);
		mTopCircleCenterX = mBgWidth / 3 + 30;
		mTopCircleCenterY = mBgHeight / 2.0f;

		mBottomCircleCenterX = mBgWidth / 4.0f - 30;
		mBottomCircleCenterY = mBgHeight * 2.0f / 3;

		mInitialDistance = calculateDistance();
		mDistance = mInitialDistance = PxToDpi_xhdpi(mInitialDistance);

		mMatrix.setTranslate(-mBgWidth/2-60, -mBgHeight/2+110);
		mMatrix.postScale((float)mDistance/180, (float)mDistance/180,
						  (mTopCircleCenterX+mBottomCircleCenterX) / 2.0f,
						  (mTopCircleCenterY+mBottomCircleCenterY) / 2.0f);

		double k = (mTopCircleCenterY - mBottomCircleCenterY) / (mTopCircleCenterX - mBottomCircleCenterX);
		mDegree = Math.atan(k);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = measure(widthMeasureSpec);
		int height = measure(heightMeasureSpec);
		if (width == 0) width = mBgWidth;
		if (height == 0) height = mBgHeight;
		setMeasuredDimension(width, height);
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
			result = 0;
			if(mode == MeasureSpec.AT_MOST){
				result = Math.min(result, size);
			}
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBg, null, mBgRect, mPaint);

		if (mType == START) {
			mCirclePaint.setAlpha(mCount * 3/4 + 105);
		} else if (mType == END) {
			mCirclePaint.setAlpha(mCount);
		} else if (mType == STILL) {
			mCirclePaint.setAlpha(103);
		}

		canvas.drawBitmap(mLight, mMatrix, mPaint);
		canvas.drawCircle(mTopCircleCenterX, mTopCircleCenterY, mCircleRadius, mCirclePaint);
		canvas.drawCircle(mBottomCircleCenterX, mBottomCircleCenterY, mCircleRadius, mCirclePaint);
	}

	public double PxToDpi_xhdpi(double size) {
		final float density = getContext().getResources().getDisplayMetrics().density;
		return size / 2f * density + 0.5f;
	}
}
