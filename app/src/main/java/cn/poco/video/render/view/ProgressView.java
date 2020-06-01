package cn.poco.video.render.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2018/1/5
 */
public class ProgressView extends View {

	private static final int COLOR_GRAY = 0xff595959;
	private static final int COLOR_YELLOW = 0xffffcf56;

	private Paint mPaint;

	private float mProgress;

	private int mWidth;

	private int mHeight;

	private int mNormalColor = COLOR_GRAY;
	private int mProgressColor = COLOR_YELLOW;

	private boolean isDown = false;

	private Bitmap mIndicator;

	private OnSeekBarChangeListener mOnSeekBarChangeListener;

	private int mProgressWidth;
	private int mProgressHeight;
	private float mProgressCenterH;

	private float mLeftRightSpace;

	public ProgressView(Context context) {
		super(context);
		init();
	}

	public ProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {

		mProgressHeight = ShareData.PxToDpi_xhdpi(6);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(mProgressHeight);

		mIndicator = BitmapFactory.decodeResource(getResources(), R.drawable.video_progress_indicator);

		mLeftRightSpace = mIndicator.getWidth() / 2f;
	}

	public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
		mOnSeekBarChangeListener = onSeekBarChangeListener;
	}

	public void setProgress(float progress) {
		if (progress < 0) {
			progress = 0;
		} else if (progress > 1) {
			progress = 1;
		}
		mProgress = progress;
		ViewCompat.postInvalidateOnAnimation(this);
	}

	public float getProgress() {
		return mProgress;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
		mHeight = h;

		mProgressWidth = mWidth - mIndicator.getWidth();

		mProgressCenterH = mHeight / 2f;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		float left = mProgress * mProgressWidth;

		mPaint.setColor(mNormalColor);
		canvas.drawLine(mLeftRightSpace, mProgressCenterH, mWidth - mLeftRightSpace, mProgressCenterH, mPaint);

		mPaint.setColor(mProgressColor);
		canvas.drawLine(mLeftRightSpace, mProgressCenterH, left + mLeftRightSpace + 1, mProgressCenterH, mPaint);

		canvas.drawBitmap(mIndicator, left, 0, null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isDown = true;
				mProgress = calculateProgress(event.getX());
				if (mOnSeekBarChangeListener != null) {
					mOnSeekBarChangeListener.onStartTrackingTouch(this);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				mProgress = calculateProgress(event.getX());
				if (mOnSeekBarChangeListener != null) {
					mOnSeekBarChangeListener.onProgressChanged(this, mProgress);
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				isDown = false;
				if (mOnSeekBarChangeListener != null) {
					mOnSeekBarChangeListener.onStopTrackingTouch(this);
				}
				break;
		}

		ViewCompat.postInvalidateOnAnimation(this);

		return true;
	}

	private float calculateProgress(float x) {
		float progress = (x - mLeftRightSpace) / mProgressWidth;
		if (progress < 0) {
			progress = 0;
		}

		if (progress > 1) {
			progress = 1;
		}

		return progress;
	}

	public interface OnSeekBarChangeListener {

		void onProgressChanged(ProgressView view, float progress);

		void onStartTrackingTouch(ProgressView view);

		void onStopTrackingTouch(ProgressView view);
	}
}
