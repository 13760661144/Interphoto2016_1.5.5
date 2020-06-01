package cn.poco.video.timeline2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/11/16
 */
public class ThumbView extends View {

	public static final int MOVE_LEFT = 1;
	public static final int MOVE_RIGHT = 2;
	public static final int MOVE_OVERALL = 3;

	private static final int COLOR_YELLOW = 0xffffc433;
	private static final int COLOR_MASK = 0x66000000;

	private Paint mPaint;

	private long mDuration;

	private float mStartProgress = 0;
	private float mEndProgress = 1;

	private float mMinProgress = 0;

	private int mFrameWidth;
	private int mFrameHeight;
	private int mFrameCount = 0;

	private int mThumbWidth;
	private int mThumbMargin;
	private int mThumbGap;

	private int mLineMargin;
	private int mLineGap;
	private int mShortLineHeight;
	private int mLineHeight;

	private int mThumbLineWidth;
	private int mPlayLineWidth;

	private float mLastX;
	private RectF mTouchRectF;
	private boolean isMoveStart;
	private boolean isMoveEnd;

	private boolean isTenSecondMode = false;

	private OnDragListener mOnDragListener;

	private float mPlayProgress = 0;
	private boolean isHidePlayLine = false;
	private ValueAnimator mValueAnimator;
	private ValueAnimator mTempAnimator;
	private boolean isPause;

	private boolean isMoveOverall;

	private boolean canAutoScroll;
	private boolean isLeftStartScroll;
	private boolean isRightStartScroll;
	private RectF mLeftScrollRange;
	private RectF mRightScrollRange;

	private int mMoveDistance;
	private float mMoveOverallMaxWidth;

	private static final long LONG_TIME = 300;
	private int mTouchSlop;
	private boolean shouldDispatchEvent = false;

	private Vibrator mVibrator;
	private long[] mPattern = {50, 200};

	private Runnable mCheckLongPressRunnable = new Runnable() {
		@Override
		public void run() {
			isMoveOverall = true;
			isHidePlayLine = true;
			getParent().requestDisallowInterceptTouchEvent(true);

			mVibrator.vibrate(mPattern, -1);
			if (mOnDragListener != null) {
				mOnDragListener.onDragStart(MOVE_OVERALL);
			}
		}
	};

	public ThumbView(Context context) {
		this(context, null);
	}

	public ThumbView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	private void init() {

		mVibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);

		mFrameWidth = ShareData.PxToDpi_xhdpi(40);
		mFrameHeight = ShareData.PxToDpi_xhdpi(128);

		mThumbWidth = ShareData.PxToDpi_xhdpi(40);
		mThumbMargin = ShareData.PxToDpi_xhdpi(7);
		mThumbGap = mFrameWidth - mThumbWidth;

		mLineMargin = ShareData.PxToDpi_xhdpi(15);
		mLineGap = ShareData.PxToDpi_xhdpi(8);
		mShortLineHeight = ShareData.PxToDpi_xhdpi(20);
		mLineHeight =  ShareData.PxToDpi_xhdpi(30);

		mThumbLineWidth = ShareData.PxToDpi_xhdpi(2);
		mPlayLineWidth = ShareData.PxToDpi_xhdpi(4);

		mTouchRectF = new RectF();

		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

		int temp = ShareData.PxToDpi_xhdpi(60);
		mLeftScrollRange = new RectF(0, 0, temp, mFrameHeight);
		mRightScrollRange = new RectF(ShareData.m_screenWidth - temp, 0, ShareData.m_screenWidth, mFrameHeight);
		mMoveOverallMaxWidth = ShareData.m_screenWidth * 4f / 5;
	}

	public void setFrameCount(int frameCount) {
		if (mFrameCount != frameCount) {
			mFrameCount = frameCount;
			requestLayout();
		}
	}

	public void setDuration(long duration) {
		if (mDuration != duration) {
			mDuration = duration;

			mMinProgress = 1000f / mDuration;
		}
	}

	public void initAnimator() {
		long duration = (long) (mDuration * (mEndProgress - mStartProgress));
		if (duration > 0) {
			mValueAnimator = ValueAnimator.ofFloat(mStartProgress, mEndProgress);
			mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					mPlayProgress = (float)animation.getAnimatedValue();
					ViewCompat.postInvalidateOnAnimation(ThumbView.this);
				}
			});
			mValueAnimator.setInterpolator(new LinearInterpolator());
			mValueAnimator.setDuration(duration);
		}
	}

	public void start() {
		if (mValueAnimator != null) {
			mValueAnimator.start();
		}
	}

	public void resume() {
		if (isPause) {
			long duration = (long) (mDuration * (mEndProgress - mPlayProgress));
			if (duration > 0) {
				mTempAnimator = ValueAnimator.ofFloat(mPlayProgress, mEndProgress);
				mTempAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						mPlayProgress = (float)animation.getAnimatedValue();
						ViewCompat.postInvalidateOnAnimation(ThumbView.this);
					}
				});
				mTempAnimator.setInterpolator(new LinearInterpolator());
				mTempAnimator.setDuration(duration);
				mTempAnimator.start();
			}

			initAnimator();

			isPause = false;
		}
	}

	public void pause() {
		if (!isPause) {
			isPause = true;
			if (mTempAnimator != null && mTempAnimator.isRunning()) {
				mTempAnimator.removeAllUpdateListeners();
				mTempAnimator.cancel();
				mTempAnimator = null;
			}

			if (mValueAnimator != null && mValueAnimator.isRunning()) {
				mValueAnimator.removeAllUpdateListeners();
				mValueAnimator.cancel();
				mValueAnimator = null;
			}
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	public void release() {
		mPlayProgress = 0;
		pause();
		isPause = false;
		mVibrator.cancel();
	}

	public void setTenSecondMode(boolean isTenSecondMode) {
		this.isTenSecondMode = isTenSecondMode;
	}

	public void setOnDragListener(OnDragListener listener) {
		mOnDragListener = listener;
	}

	public void updateProgress(float startProgress, float endProgress) {
		mStartProgress = startProgress;
		mEndProgress = endProgress;
		ViewCompat.postInvalidateOnAnimation(this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(getMeasuredWidth(widthMeasureSpec), getMeasuredHeight(heightMeasureSpec));
	}

	private int getMeasuredWidth(int measureSpec) {
		int size = MeasureSpec.getSize(measureSpec);
		int mode = MeasureSpec.getMode(measureSpec);

		int result = size;
		if (mode != MeasureSpec.EXACTLY) {
			result = mFrameCount * mFrameWidth + mThumbWidth * 2;

			if (mode == MeasureSpec.AT_MOST) {
				result = Math.min(size, result);
			}
		}

		return result;
	}

	public void updateSize() {
		mMoveDistance = mFrameCount * mFrameWidth;
		canAutoScroll = mMoveDistance > ShareData.m_screenWidth;
	}

	private int getMeasuredHeight(int measureSpec) {

		int size = MeasureSpec.getSize(measureSpec);
		int mode = MeasureSpec.getMode(measureSpec);

		int result = size;
		if (mode != MeasureSpec.EXACTLY) {
			result = mFrameHeight;
			if (mode == MeasureSpec.AT_MOST) {
				result = Math.min(size, result);
			}
		}

		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float startThumbX = mStartProgress * mMoveDistance;
		float endThumbX = mEndProgress * mMoveDistance + mThumbWidth;

		if (mPlayProgress > 0 && !isHidePlayLine) {
			float position = mPlayProgress * mMoveDistance + mThumbWidth;
			mPaint.setColor(COLOR_YELLOW);
			mPaint.setStrokeWidth(mPlayLineWidth);
			canvas.drawLine(position, 0, position, mFrameHeight, mPaint);
		}

		mPaint.setColor(COLOR_MASK);
		canvas.drawRect(0, 0, startThumbX, mFrameHeight, mPaint);
		canvas.drawRect(endThumbX + mThumbWidth, 0, mMoveDistance + 2 * mThumbWidth, mFrameHeight, mPaint);

		mPaint.setColor(Color.WHITE);
		canvas.drawRect(startThumbX, 0, startThumbX + mThumbWidth, mFrameHeight, mPaint);
		canvas.drawRect(endThumbX, 0, endThumbX + mThumbWidth, mFrameHeight, mPaint);
		canvas.drawRect(startThumbX + mThumbWidth, 0, endThumbX, mThumbMargin, mPaint);
		canvas.drawRect(startThumbX + mThumbWidth,  mFrameHeight - mThumbMargin, endThumbX, mFrameHeight, mPaint);

		mPaint.setColor(COLOR_YELLOW);
		mPaint.setStrokeWidth(mThumbLineWidth);
		float lineStart = mLineMargin + startThumbX;
		float lineTop = (mFrameHeight - mShortLineHeight) / 2f;
		canvas.drawLine(lineStart, lineTop, lineStart, lineTop + mShortLineHeight, mPaint);

		lineStart = lineStart + mLineGap;
		lineTop = (mFrameHeight - mLineHeight) / 2f;
		canvas.drawLine(lineStart, lineTop, lineStart, lineTop + mLineHeight, mPaint);

		lineStart = endThumbX + mThumbWidth - mLineMargin;
		lineTop = (mFrameHeight - mShortLineHeight) / 2f;
		canvas.drawLine(lineStart, lineTop, lineStart, lineTop + mShortLineHeight, mPaint);

		lineStart = lineStart - mLineGap;
		lineTop = (mFrameHeight - mLineHeight) / 2f;
		canvas.drawLine(lineStart, lineTop, lineStart, lineTop + mLineHeight, mPaint);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			shouldDispatchEvent = false;
		}
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (shouldDispatchEvent) {
			if (mOnDragListener != null) {
				mOnDragListener.onDispatchTouchEvent(event);
			}
			return true;
		}

		boolean result = false;

		final float x = event.getX();
		final float y = event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				float startX = mStartProgress * mMoveDistance - getScrollX();
				float endX = startX + mThumbWidth;
				mTouchRectF.set(startX - mThumbGap, 0, endX + mThumbGap, mFrameHeight);
				if (mTouchRectF.contains(x, y)) {
					isMoveStart = true;
					isHidePlayLine = true;
					getParent().requestDisallowInterceptTouchEvent(true);
					result = true;
					if (mOnDragListener != null) {
						mOnDragListener.onDragStart(MOVE_LEFT);
					}
					break;
				}

				startX = mEndProgress * mMoveDistance + mThumbWidth - getScrollX();
				endX = startX + mThumbWidth;
				mTouchRectF.set(startX - mThumbGap, 0, endX + mThumbGap, mFrameHeight);
				if (mTouchRectF.contains(x, y)) {
					isMoveEnd = true;
					isHidePlayLine = true;
					getParent().requestDisallowInterceptTouchEvent(true);
					result = true;
					if (mOnDragListener != null) {
						mOnDragListener.onDragStart(MOVE_RIGHT);
					}
					break;
				}

				startX = mStartProgress * mMoveDistance + mThumbWidth + mThumbGap - getScrollX();
				endX = mEndProgress * mMoveDistance + mThumbWidth - mThumbGap - getScrollX();
				mTouchRectF.set(startX, 0, endX, mFrameHeight);
//				float thumbWidth = 0;
//				if (canAutoScroll) {
//					thumbWidth = (mEndProgress - mStartProgress) * mMoveDistance;
//				}
				if (mTouchRectF.contains(x, y) && (mStartProgress > 0 || mEndProgress < 1) /*&& thumbWidth < mMoveOverallMaxWidth*/) {
					ViewCompat.postOnAnimationDelayed(this, mCheckLongPressRunnable, LONG_TIME);
					result = true;
					break;
				}
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				final float deltaX = x - mLastX;
				float progress = deltaX / mMoveDistance;

				if (isMoveStart) {

					final float originProgress = mStartProgress;

					if (isTenSecondMode) {
						if (deltaX < 0 && TimelineLayout.sTenLeftDuration > 0) {
							long tempDuration = calculateStartLeft(progress, TimelineLayout.sTenLeftDuration * -1f / mDuration);
							TimelineLayout.sTenLeftDuration = Math.max(0, TimelineLayout.sTenLeftDuration - tempDuration);
						} else if (deltaX > 0) {
							long tempDuration = calculateStartRight(progress);
							TimelineLayout.sTenLeftDuration += tempDuration;
						}
					} else {
						if (deltaX < 0 && TimelineLayout.s3MLeftDuration > 0) {
							long tempDuration = calculateStartLeft(progress, TimelineLayout.s3MLeftDuration * -1f / mDuration);
							TimelineLayout.s3MLeftDuration = Math.max(0, TimelineLayout.s3MLeftDuration - tempDuration);
						} else if (deltaX > 0) {
							long tempDuration = calculateStartRight(progress);
							TimelineLayout.s3MLeftDuration += tempDuration;
						}
					}

					if (Math.abs(mStartProgress - originProgress) > 0.0001) {

						handleStartAutoScroll();

						final float distance = (mStartProgress - originProgress) * mMoveDistance;
						if (mOnDragListener != null) {
							pause();
							long position = (int)(mStartProgress * mDuration);
							mOnDragListener.onDragLeft(position, distance);
						}

						mPlayProgress = mStartProgress;
						ViewCompat.postInvalidateOnAnimation(this);
					}
					result = true;
				} else if (isMoveEnd) {

					final float originProgress = mEndProgress;

					if (isTenSecondMode) {
						if (deltaX < 0) {
							long tempDuration = calculateEndStart(progress);
							TimelineLayout.sTenLeftDuration += tempDuration;
						} else if (deltaX > 0 && TimelineLayout.sTenLeftDuration > 0) {
							long tempDuration = calculateEndRight(progress, TimelineLayout.sTenLeftDuration * 1f / mDuration);
							TimelineLayout.sTenLeftDuration = Math.max(0, TimelineLayout.sTenLeftDuration - tempDuration);
						}
					} else {

						if (deltaX < 0) {
							long tempDuration = calculateEndStart(progress);
							TimelineLayout.s3MLeftDuration += tempDuration;
						} else if (deltaX > 0 && TimelineLayout.s3MLeftDuration > 0) {
							long tempDuration = calculateEndRight(progress, TimelineLayout.s3MLeftDuration * 1f / mDuration);
							TimelineLayout.s3MLeftDuration = Math.max(0, TimelineLayout.s3MLeftDuration - tempDuration);
						}
					}

					if (Math.abs(mEndProgress - originProgress) > 0.0001) {

						handleEndAutoScroll();

						final float distance = (mEndProgress - originProgress) * mMoveDistance;
						if (mOnDragListener != null) {
							pause();
							long position = (int)(mEndProgress * mDuration);
							mOnDragListener.onDragRight(position, distance);
						}

						mPlayProgress = mEndProgress;
						ViewCompat.postInvalidateOnAnimation(this);
					}
					result = true;
				} else if (isMoveOverall) {

					handleBothAutoScroll(deltaX > 0);

					if (deltaX > 0 && mEndProgress < 1) {
						final float originProgress = mEndProgress;
						mEndProgress = Math.min(mEndProgress + progress, 1);
						final float changeProgress = mEndProgress - originProgress;
						mStartProgress += changeProgress;
						pause();
						if (mOnDragListener != null) {
							mOnDragListener.onDragOverall((int)(mStartProgress * mDuration),
														  (int)(mEndProgress * mDuration));
						}
						mPlayProgress = mStartProgress;
						ViewCompat.postInvalidateOnAnimation(this);
					} else if (deltaX < 0 && mStartProgress > 0) {
						final float originProgress = mStartProgress;
						mStartProgress = Math.max(mStartProgress + progress, 0);
						final float changeProgress = mStartProgress - originProgress;
						mEndProgress += changeProgress;
						pause();
						if (mOnDragListener != null) {
							mOnDragListener.onDragOverall((int)(mStartProgress * mDuration),
														  (int)(mEndProgress * mDuration));
						}
						mPlayProgress = mStartProgress;
						ViewCompat.postInvalidateOnAnimation(this);
					}

				} else if (canAutoScroll) {
					if (Math.abs(deltaX) >= mTouchSlop) {
						removeCallbacks(mCheckLongPressRunnable);
						shouldDispatchEvent = true;
						MotionEvent e = MotionEvent.obtain(event.getDownTime(), event.getEventTime(),
														   MotionEvent.ACTION_DOWN, event.getX(), event.getY(), 0);
						if (mOnDragListener != null) {
							mOnDragListener.onDispatchTouchEvent(e);
						}

						e.recycle();
					}
				}
				break;
			}
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:

				if (isLeftStartScroll || isRightStartScroll) {
					onStopScroll();
				}

				isHidePlayLine = false;

				if (mOnDragListener != null) {
					if (isMoveStart) {
						mOnDragListener.onDragStop(MOVE_LEFT);
					} else if (isMoveEnd) {
						mOnDragListener.onDragStop(MOVE_RIGHT);
					} else if (isMoveOverall) {
						mOnDragListener.onDragStop(MOVE_OVERALL);
					}
				}

				isMoveStart = false;
				isMoveEnd = false;
				isMoveOverall = false;
				removeCallbacks(mCheckLongPressRunnable);
				result = true;
				getParent().requestDisallowInterceptTouchEvent(false);
				break;
		}

		mLastX = x;

		return result;
	}

	public void updateProgress(float dx) {
		final float progress = dx / mMoveDistance;
		if (isMoveStart) {
			mStartProgress = checkProgress(mStartProgress + progress, 0, mEndProgress - mMinProgress);
			ViewCompat.postInvalidateOnAnimation(this);
		} else if (isMoveEnd) {
			mEndProgress = checkProgress(mEndProgress + progress, mStartProgress + mMinProgress, 1);
			ViewCompat.postInvalidateOnAnimation(this);
		} else if (isMoveOverall) {
			if (isLeftStartScroll) {
				final float originProgress = mStartProgress;
				mStartProgress = checkProgress(mStartProgress + progress, 0, mEndProgress - mMinProgress);
				final float changeProgress = mStartProgress - originProgress;
				if (Math.abs(changeProgress) > 0) {
					mEndProgress += changeProgress;
					ViewCompat.postInvalidateOnAnimation(this);
				}
			} else if (isRightStartScroll) {
				final float originProgress = mEndProgress;
				mEndProgress = checkProgress(mEndProgress + progress, mStartProgress + mMinProgress, 1);
				final float changeProgress = mEndProgress - originProgress;
				if (Math.abs(changeProgress) > 0) {
					mStartProgress += changeProgress;
					ViewCompat.postInvalidateOnAnimation(this);
				}
			}
		}
	}

	private float checkProgress(float progress, float min, float max) {
		if (progress < min) {
			progress = min;
		} else if (progress > max) {
			progress = max;
		}

		return progress;
	}

	private void handleStartAutoScroll() {
		if (canAutoScroll) {
			float startX = mMoveDistance * mStartProgress - getScrollX();
			mTouchRectF.set(startX, 0, startX + mFrameWidth, mFrameHeight);
			if (RectF.intersects(mLeftScrollRange, mTouchRectF)) {
				if (!isLeftStartScroll) {
					isLeftStartScroll = true;
					onStartScroll(MOVE_LEFT);
				}
			} else if (RectF.intersects(mRightScrollRange, mTouchRectF)) {
				if (!isRightStartScroll) {
					isRightStartScroll = true;
					onStartScroll(MOVE_RIGHT);
				}
			} else {
				onStopScroll();
			}
		}
	}

	private void handleEndAutoScroll() {

		if (canAutoScroll) {
			float startX = mMoveDistance * mEndProgress + mFrameWidth - getScrollX();
			mTouchRectF.set(startX, 0, startX + mFrameWidth, mFrameHeight);
			if (RectF.intersects(mLeftScrollRange, mTouchRectF)) {
				if (!isLeftStartScroll) {
					isLeftStartScroll = true;
					onStartScroll(MOVE_LEFT);
				}
			} else if (RectF.intersects(mRightScrollRange, mTouchRectF)) {
				if (!isRightStartScroll) {
					isRightStartScroll = true;
					onStartScroll(MOVE_RIGHT);
				}
			} else {
				onStopScroll();
			}
		}
	}

	private void handleBothAutoScroll(boolean right) {

		if (canAutoScroll) {
			if (right) {
				float startX = mMoveDistance * mEndProgress + mFrameWidth - getScrollX();
				mTouchRectF.set(startX, 0, startX + mFrameWidth, mFrameHeight);
				if (RectF.intersects(mRightScrollRange, mTouchRectF)) {
					if (!isRightStartScroll) {
						isRightStartScroll = true;
						onStartScroll(MOVE_RIGHT);
					}
				} else {
					onStopScroll();
				}
			} else {
				float startX = mMoveDistance * mStartProgress - getScrollX();
				mTouchRectF.set(startX, 0, startX + mFrameWidth, mFrameHeight);
				if (RectF.intersects(mLeftScrollRange, mTouchRectF)) {
					if (!isLeftStartScroll) {
						isLeftStartScroll = true;
						onStartScroll(MOVE_LEFT);
					}
				} else {
					onStopScroll();
				}
			}
		}
	}

	private void onStartScroll(int move) {
		if (mOnDragListener != null) {
			mOnDragListener.onStartScroll(move);
		}
	}

	private void onStopScroll() {
		isLeftStartScroll = false;
		isRightStartScroll = false;
		if (mOnDragListener != null) {
			mOnDragListener.onStopScroll();
		}
	}

	private long calculateStartRight(float progress) {
		float consume = progress;
		final float originStartProgress = mStartProgress;
		mStartProgress += progress;
		if (mStartProgress > mEndProgress - mMinProgress) {
			mStartProgress = mEndProgress - mMinProgress;
			consume = mStartProgress - originStartProgress;
		}
		return (long)(mDuration * consume);
	}

	private long calculateStartLeft(float progress, float tempProgress) {
		float consume = Math.max(progress, tempProgress);
		mStartProgress += consume;
		if (mStartProgress < 0) {
			consume -= mStartProgress;
			mStartProgress = 0;
		}
		return (long)(mDuration * -consume + 0.5f);
	}

	private long calculateEndRight(float progress, float tempProgress) {
		float consume = Math.min(progress, tempProgress);
		mEndProgress += consume;
		if (mEndProgress > 1) {
			consume = 1 - mEndProgress + consume;
			mEndProgress = 1;
		}
		return (long)(mDuration * consume + 0.5f);
	}

	private long calculateEndStart(float progress) {
		float consume = progress;
		final float originEndProgress = mEndProgress;
		mEndProgress += progress;
		if (mEndProgress < mStartProgress + mMinProgress) {
			mEndProgress = mStartProgress + mMinProgress;
			consume = mEndProgress - originEndProgress;
		}
		return (long)(mDuration * -consume);
	}

	public interface OnDragListener {

		void onDragLeft(long position, float distance);

		void onDragRight(long position, float distance);

		void onDragOverall(long leftPosition, long rightPosition);

		void onDragStart(int move);

		void onDragStop(int move);

		void onStartScroll(int move);

		void onStopScroll();

		void onDispatchTouchEvent(MotionEvent event);
	}
}
