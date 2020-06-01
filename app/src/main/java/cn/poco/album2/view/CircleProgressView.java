package cn.poco.album2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.View;

import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/3/6
 */
public class CircleProgressView extends View {

	private int mBackgroundColor = 0xff4d4d4d;
	private int mProgressColor = 0xffffc433;

	private float mProgress = 0;

	private Paint mPaint;
	private float mPaintWidth;

	private Path mPath;
	private Path mProgressPath;
	private PathMeasure mPathMeasure;
	private float mPathLength;

	private State mState = State.NONE;
	private static final float SCAN_LENGTH_RATIO = 0.1f;
	private float mStart = 0;

	public CircleProgressView(Context context) {
		super(context);

		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaintWidth = ShareData.PxToDpi_xhdpi(7);
		mPaint.setStrokeWidth(mPaintWidth);
		mPaint.setStyle(Paint.Style.STROKE);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		final float radius = (w - mPaintWidth) / 2f;
		mPath = new Path();
		mPath.addCircle(w / 2f, h / 2f, radius, Path.Direction.CW);

		mPathMeasure = new PathMeasure(mPath, true);
		mPathLength = mPathMeasure.getLength();

		mProgressPath = new Path();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();

		mPaint.setColor(mBackgroundColor);
		canvas.drawPath(mPath, mPaint);
		mProgressPath.reset();

		if (mState == State.PROGRESS) {
			canvas.rotate(-90, getWidth() / 2f, getHeight() / 2f);

			mPathMeasure.getSegment(0, mProgress / 100f * mPathLength, mProgressPath, true);
			mPaint.setColor(mProgressColor);
			canvas.drawPath(mProgressPath, mPaint);
		} else if (mState == State.SCAN){
			canvas.rotate((mStart-90) % 360, getWidth() / 2f, getHeight() / 2f);
			mPathMeasure.getSegment(0, SCAN_LENGTH_RATIO * mPathLength, mProgressPath, true);
			mPaint.setColor(mProgressColor);
			canvas.drawPath(mProgressPath, mPaint);
		}

		canvas.restore();
	}


	private Runnable mRotateRunnable = new Runnable() {
		@Override
		public void run() {
			if (mState == State.SCAN) {
				mStart += 30;
				invalidate();
				postDelayed(mRotateRunnable, 50);
			}
		}
	};

	public void setProgress(float progress) {
		mProgress = progress;
		invalidate();
	}

	public float getProgress() {
		return mProgress;
	}

	public void startScan() {
		mState = State.SCAN;
		mStart = -30;
		mRotateRunnable.run();
	}

	public void stopScan() {
		mStart = 0;
		mState = State.PROGRESS;
	}

	public enum State {
		NONE, SCAN, PROGRESS
	}
}
