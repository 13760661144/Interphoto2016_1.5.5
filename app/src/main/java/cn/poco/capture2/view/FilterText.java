package cn.poco.capture2.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.poco.capture2.AnimatorUtils;

/**
 * Created by: fwc
 * Date: 2017/12/28
 */
public class FilterText extends FrameLayout {

	private static final int SHOW_TIME = 1000;

	private Context mContext;

	private TextView mFilterName;

	private boolean isShow = false;

	private ObjectAnimator mShowAnim;
	private ObjectAnimator mHideAnim;

	private Handler mHandler;

	public FilterText(@NonNull Context context) {
		super(context);

		mContext = context;
		mHandler = new Handler();

		initViews();
	}

	private void initViews() {

		LayoutParams params;

		mFilterName = new TextView(mContext);
		mFilterName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		mFilterName.getPaint().setFakeBoldText(true);
		mFilterName.setTextColor(Color.WHITE);
		mFilterName.setIncludeFontPadding(false);

		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mFilterName, params);

		setVisibility(GONE);
	}

	public void setText(String filterName) {
		mFilterName.setText(filterName);
		show();
	}

	private void show() {
		mHandler.removeCallbacks(mHideRunnable);
		float lastAlpha = 0;
//		if (isShow) {
//			if (mHideAnim != null && mHideAnim.isRunning()) {
//				mHideAnim.removeAllListeners();
//				lastAlpha = getAlpha();
//				mHideAnim.cancel();
//				mHideAnim = null;
//			} else {
//				mHandler.postDelayed(mHideRunnable, SHOW_TIME);
//				return;
//			}
//		}

		if (!isShow) {
			setVisibility(VISIBLE);
		}
		isShow = true;
		mShowAnim = ObjectAnimator.ofFloat(this, "alpha", lastAlpha, 1);
		mShowAnim.setDuration((long)((1 - lastAlpha) * AnimatorUtils.DEFAULT_DURATION));
		mShowAnim.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mHandler.postDelayed(mHideRunnable, SHOW_TIME);
				mShowAnim = null;
			}
		});
		mShowAnim.start();
	}

	private Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			if (isShow) {
				mHideAnim = ObjectAnimator.ofFloat(FilterText.this, "alpha", 1, 0);
				mHideAnim.setDuration(AnimatorUtils.DEFAULT_DURATION);
				mHideAnim.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						isShow = false;
						setVisibility(GONE);
						mHideAnim = null;
					}
				});
				mHideAnim.start();
			}
		}
	};

	public void release() {

		mHandler.removeCallbacks(mHideRunnable);

		if (mShowAnim != null) {
			mShowAnim.removeAllListeners();
			mShowAnim.cancel();
			mShowAnim = null;
		}

		if (mHideAnim != null) {
			mHideAnim.removeAllListeners();
			mHideAnim.cancel();
			mHideAnim = null;
		}
	}
}
