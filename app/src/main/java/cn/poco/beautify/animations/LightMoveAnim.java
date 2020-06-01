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

public class LightMoveAnim extends View {

	/**
	 * 绘制图片的画笔
	 */
	private Paint mPaint;

	/**
	 * 绘制圆点的画笔
	 */
	private Paint mCirclePaint;

	/**
	 * 背景图
	 */
	private Bitmap mBg;

	/**
	 * 光效图
	 */
	private Bitmap mLight;

	/**
	 * 绘制背景图所在区域
	 */
	private Rect mBgRect;

	/**
	 * 背景图的宽
	 */
	private int mBgWidth;

	/**
	 * 背景图的高
	 */
	private int mBgHeight;

	/**
	 * 绘制光效图的矩阵
	 */
	private Matrix mMatrix;

	/**
	 * 圆点中心x坐标
	 */
	private float mCircleCenterX;

	/**
	 * 圆点中心y坐标
	 */
	private float mCircleCenterY;

	/**
	 * 圆点的半径
	 */
	private int mCircleRadius;

	/**
	 * 旋转角度
	 */
	private int mDegree;

	/**
	 * 旋转半径
	 */
	private float mRotateRadius = 3;

	/**
	 * 用于记录下一个旋转点的位置
	 */
	private float x;
	private float y;

	/**
	 * 绘制类型
	 */
	private int mType = START;

	private static final int START = 1;
	private static final int MOVE = 2;
	private static final int END = 3;
	private static final int STILL = 4;

	private int mCount = 0;

	private Timer mTimer;
	private TimerTask m_task;

	public LightMoveAnim(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LightMoveAnim(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
		mPaint.setDither(true);

		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setColor(Color.WHITE);

		mCircleRadius = PxToDpi_xhdpi(16);

		mBg = BitmapFactory.decodeResource(getResources(), R.drawable.light_move_bg);
		mLight = BitmapFactory.decodeResource(getResources(), R.drawable.light_move);

		mBgWidth = mBg.getWidth();
		mBgHeight = mBg.getHeight();

		mMatrix = new Matrix();
		PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SCREEN);
		mPaint.setXfermode(xfermode);

		m_task = new TimerTask() {
			@Override
			public void run() {
				if (mType == START) {
					mCount++;
					if (mCount == 300) {
						mType = MOVE;
						mCount = 500;
					}

				} else if (mType == MOVE) {
					mDegree++;
					if (mDegree == 359) {
						mType = END;
					}

				} else if (mType == END) {
					mCount--;
					if (mCount == 153 * 3) {
						mType = STILL;
						mCount = 50;
					}

				} else if (mType == STILL) {
					mCount--;
					if (mCount == 0) {
						reset();
					}
				}

				postInvalidate();
			}
		};

		mTimer = new Timer();
		mTimer.schedule(m_task, 0, 3);
	}

	/**
	 * 重置
	 */
	private void reset() {
		mCount = 0;
		mType = START;
		mDegree = 0;
	}

	/**
	 * 停止动画并释放资源
	 */
	public void release() {
		m_task.cancel();
		mTimer.cancel();
		mTimer.purge();
		mTimer = null;

//		mBg.recycle();
//		mLight.recycle();
	}

	/**
	 * 计算位置
	 */
	private void calculatePosition() {
		x = (float) Math.cos(Math.toRadians(mDegree-90)) * mRotateRadius;
		y = (float) Math.sin(Math.toRadians(mDegree-90)) * mRotateRadius;

		mCircleCenterX += x;
		mCircleCenterY += y;
		mMatrix.postTranslate(x, y);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mBgRect = new Rect(0, 0, mBgWidth, mBgHeight);

		mCircleCenterX = mBgWidth / 4.0f;
		mCircleCenterY = mBgHeight / 2.0f;
		mMatrix.setTranslate(-mCircleCenterX*2-80, -mCircleCenterY+30);
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

	/**
	 * 两种方法：
	 * 1.通过旋转角度计算位置进行重绘（calculatePosition）
	 * 2.通过旋转画布重绘
	 * @param canvas 画布
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBg, null, mBgRect, mPaint);

		if (mType == START) {
			mCirclePaint.setAlpha(mCount/2+105);
		} else if (mType == MOVE) {
			canvas.save();
			canvas.rotate(mDegree, mBgRect.left+mBgWidth/2.0f, mBgHeight/2.0f);
			mCirclePaint.setAlpha(255);
		} else if (mType == END) {
			mCirclePaint.setAlpha(mCount/3);
		} else if (mType == STILL) {
			mCirclePaint.setAlpha(153);
		}

		canvas.drawBitmap(mLight, mMatrix, mPaint);
		canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius, mCirclePaint);

		if (mType == MOVE) canvas.restore();
	}

	public int PxToDpi_xhdpi(int size) {
		final float density = getContext().getResources().getDisplayMetrics().density;
		return (int)(size / 2f * density + 0.5f);
	}
}
