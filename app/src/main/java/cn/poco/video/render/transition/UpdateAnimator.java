package cn.poco.video.render.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.LinearInterpolator;

/**
 * Created by: fwc
 * Date: 2017/12/7
 */
public class UpdateAnimator {

	private static final int IDLE = 0;
	private static final int START = 1;
	private static final int END = 2;

	private int mState = IDLE;

	private float mStartProgress;
	private long mDuration;

	private Handler mHandler;

	private OnUpdateListener mOnUpdateListener;

	private ValueAnimator mValueAnimator;

	private float mCurrentProgress;

	public UpdateAnimator(Looper looper) {
		mHandler = new Handler(looper == null ? Looper.getMainLooper() : looper);
	}

	public void setStartProgress(float startProgress) {
		if (mState != IDLE) {
			throw new IllegalStateException();
		}

		if (startProgress < 0) {
			startProgress = 0;
		} else if (startProgress > 1) {
			startProgress = 1;
		}

		mStartProgress = startProgress;
	}

	public void setDuration(long duration) {
		if (mState != IDLE) {
			throw new IllegalStateException();
		}

		if (duration <= 0) {
			throw new RuntimeException("the duration: " + duration);
		}

		mDuration = duration;
	}

	public void start() {
		if (mState != IDLE) {
			throw new IllegalStateException();
		}

		if (mDuration <= 0) {
			throw new RuntimeException("the duration is not correct.");
		}

		mCurrentProgress = mStartProgress;

		mState = START;
		mHandler.post(mStartRunnable);
	}

	public void start(float startProgress, long duration) {
		reset();

		if (duration <= 0) {
			return;
		}

		setStartProgress(startProgress);
		setDuration(duration);
		start();
	}

	public float getCurrentProgress() {
		if (mState == START) {
			return mCurrentProgress;
		}

		return 0;
	}

	public void reset() {
		mHandler.removeCallbacks(mStartRunnable);
		if (mState != IDLE) {
			mState = IDLE;
			mHandler.post(mResetRunnable);
		}
	}

	private ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			if (mState == START) {
				mCurrentProgress = (float)animation.getAnimatedValue();
				if (mOnUpdateListener != null) {
					mOnUpdateListener.onUpdate(mCurrentProgress);
				}
			}
		}
	};

	private ValueAnimator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {

			if (mState == START) {
				mState = END;

				if (mOnUpdateListener != null) {
					mOnUpdateListener.onAnimatorEnd();
				}
			}
		}
	};

	private Runnable mStartRunnable = new Runnable() {
		@Override
		public void run() {
			mValueAnimator = ValueAnimator.ofFloat(mStartProgress, 1);
			mValueAnimator.setDuration(mDuration);
			mValueAnimator.setInterpolator(new LinearInterpolator());
			mValueAnimator.addUpdateListener(mUpdateListener);
			mValueAnimator.addListener(mAnimatorListener);
			mValueAnimator.start();
		}
	};

	private Runnable mResetRunnable = new Runnable() {
		@Override
		public void run() {
			if (mValueAnimator != null) {
				mValueAnimator.removeUpdateListener(mUpdateListener);
				mValueAnimator.removeListener(mAnimatorListener);
				mValueAnimator.cancel();
			}
		}
	};

	public void setOnUpdateListener(OnUpdateListener listener) {
		mOnUpdateListener = listener;
	}

	public interface OnUpdateListener {
		void onUpdate(float progress);

		void onAnimatorEnd();
	}
}
