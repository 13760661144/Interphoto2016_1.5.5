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

public class LightRotateAnim extends View {

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

	private float mCenterX;

	private float mCenterY;

	private float mDegree = 0;

	private int mType = START;

	private static final int START = 1;
	private static final int ROTATE = 2;
	private static final int ROTATE_BACK = 3;
	private static final int END = 4;
	private static final int STILL = 5;

	private Timer mTimer;

	private int mCount;

	public LightRotateAnim(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LightRotateAnim(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		mBg = BitmapFactory.decodeResource(getResources(), R.drawable.light_rotate_bg);
		mLight = BitmapFactory.decodeResource(getResources(), R.drawable.light_rotate);

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

		mCircleRadius = PxToDpi_xhdpi(16);

		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (mType == START) {
					mCount++;
					if (mCount == 150) {
						mType = ROTATE;
					}
				} else if (mType == ROTATE) {
					mDegree++;
					if (mDegree > 120) {
						mType = ROTATE_BACK;
					}
				} else if (mType == ROTATE_BACK) {
					mDegree--;
					if (mDegree <= 0) {
						mType = END;
					}
				} else if (mType == END) {
					mCount--;
					if (mCount <= 0) {
						mType = STILL;
						mCount = 8;
					}
				} else if (mType == STILL) {
					mCount--;
					if (mCount <= 0) {
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
		mDegree = 0;
		mCount = 0;
	}

	/**
	 * 停止动画并释放资源
	 */
	public void release() {
		mTimer.cancel();
		mTimer = null;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mBgRect = new Rect(0, 0, mBgWidth, mBgHeight);

		mTopCircleCenterX = mBgWidth / 4.0f + 30;
		mTopCircleCenterY = mBgHeight / 2.0f + 60;

		mBottomCircleCenterX = mBgWidth / 5.0f - 50;
		mBottomCircleCenterY = mBgHeight * 3.0f / 4;

		mMatrix.postScale(0.8f, 0.8f);
		mMatrix.postTranslate(-mBgWidth / 2, 50);

		mCenterX = (mTopCircleCenterX + mBottomCircleCenterX) / 2;
		mCenterY = (mTopCircleCenterY + mBottomCircleCenterY) / 2;
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
			mCirclePaint.setAlpha(mCount + 105);
		} else if (mType == END) {
			mCirclePaint.setAlpha(mCount + 105);
		} else if (mType == STILL) {
			mCirclePaint.setAlpha(mCount + 105);
		}
		canvas.save();
		canvas.rotate(mDegree, mCenterX, mCenterY);
		canvas.drawBitmap(mLight, mMatrix, mPaint);
		canvas.drawCircle(mTopCircleCenterX, mTopCircleCenterY, mCircleRadius, mCirclePaint);
		canvas.drawCircle(mBottomCircleCenterX, mBottomCircleCenterY, mCircleRadius, mCirclePaint);

		canvas.restore();
	}

	public int PxToDpi_xhdpi(int size) {
		final float density = getContext().getResources().getDisplayMetrics().density;
		return (int)(size / 2f * density + 0.5f);
	}
}
