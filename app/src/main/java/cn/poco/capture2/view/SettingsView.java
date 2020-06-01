package cn.poco.capture2.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.camera.CameraConfig;
import cn.poco.camera.CameraPage;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/12/28
 */
public class SettingsView extends LinearLayout {

	private static final int TEXT_NORMAL_COLOR = 0x99ffffff;
	private static final int TEXT_SELECT_COLOR = 0xffffc433;

	private Context mContext;

	private TextView mSizeTitle;
	private ImageView mSize9_16View;
	private ImageView mSize16_9View;
	private ImageView mSize235_1View;
	private ImageView mSize1_1View;

	private TextView mDurationTitle;
	private TextView mDuration10SView;
	private TextView mDuration30SView;
	private TextView mDuration60SView;
	private TextView mDuration180SView;

	private int mLayoutHeight;
	private int mItemSize;

	private int mSize = CameraConfig.SIZE_16_9;
	private int mDuration = CameraConfig.DURATION_10S;

	private OnSettingsListener mOnSettingsListener;

	public SettingsView(@NonNull Context context) {
		super(context);

		mContext = context;

		mLayoutHeight = ShareData.PxToDpi_xhdpi(100);
		mItemSize = ShareData.PxToDpi_xhdpi(80);

		initViews();
	}

	private void initViews() {
		setBackgroundResource(R.drawable.camera_settings_bg);
		setOrientation(VERTICAL);

		LayoutParams params;

		FrameLayout sizeLayout = new FrameLayout(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mLayoutHeight);
		addView(sizeLayout, params);
		{
			FrameLayout.LayoutParams params1;

			mSizeTitle = new TextView(mContext);
			mSizeTitle.setTextColor(Color.WHITE);
			mSizeTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			mSizeTitle.setIncludeFontPadding(false);
			mSizeTitle.setText(R.string.camera_size);
			params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.gravity = Gravity.CENTER_VERTICAL;
			params1.leftMargin = ShareData.PxToDpi_xhdpi(32);
			sizeLayout.addView(mSizeTitle, params1);

			int padding = ShareData.PxToDpi_xhdpi(24);
			mSize1_1View = getSizeImageView(R.drawable.camera_size_1_1_unselect, padding);
			params1 = new FrameLayout.LayoutParams(mItemSize, mItemSize);
			params1.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(8);
			sizeLayout.addView(mSize1_1View, params1);

			mSize235_1View = getSizeImageView(R.drawable.camera_size_235_1_unselect, padding);
			params1 = new FrameLayout.LayoutParams(mItemSize, mItemSize);
			params1.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(120);
			sizeLayout.addView(mSize235_1View, params1);

			mSize16_9View = getSizeImageView(R.drawable.camera_size_16_9, padding);
			params1 = new FrameLayout.LayoutParams(mItemSize, mItemSize);
			params1.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(232);
			sizeLayout.addView(mSize16_9View, params1);

			mSize9_16View = getSizeImageView(R.drawable.camera_size_9_16_unselect, padding);
			params1 = new FrameLayout.LayoutParams(mItemSize, mItemSize);
			params1.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(344);
			sizeLayout.addView(mSize9_16View, params1);
		}

		View divide = new View(mContext);
		divide.setBackgroundColor(0x1fffffff);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(2));
		params.leftMargin = params.rightMargin = ShareData.PxToDpi_xhdpi(32);
		addView(divide, params);

		FrameLayout durationLayout = new FrameLayout(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mLayoutHeight);
		addView(durationLayout, params);
		{
			FrameLayout.LayoutParams params1;

			mDurationTitle = new TextView(mContext);
			mDurationTitle.setTextColor(Color.WHITE);
			mDurationTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			mDurationTitle.setIncludeFontPadding(false);
			mDurationTitle.setText(R.string.camera_duration);
			params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.gravity = Gravity.CENTER_VERTICAL;
			params1.leftMargin = ShareData.PxToDpi_xhdpi(32);
			durationLayout.addView(mDurationTitle, params1);

			mDuration180SView = getDurationTextView(R.string.camera_duration_180);
			params1 = new FrameLayout.LayoutParams(mItemSize, mItemSize);
			params1.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(8);
			durationLayout.addView(mDuration180SView, params1);

			mDuration60SView = getDurationTextView(R.string.camera_duration_60);
			params1 = new FrameLayout.LayoutParams(mItemSize, mItemSize);
			params1.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(120);
			durationLayout.addView(mDuration60SView, params1);

			mDuration30SView = getDurationTextView(R.string.camera_duration_30);
			params1 = new FrameLayout.LayoutParams(mItemSize, mItemSize);
			params1.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(232);
			durationLayout.addView(mDuration30SView, params1);

			mDuration10SView = getDurationTextView(R.string.camera_duration_10);
			mDuration10SView.setTextColor(TEXT_SELECT_COLOR);
			params1 = new FrameLayout.LayoutParams(mItemSize, mItemSize);
			params1.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(344);
			durationLayout.addView(mDuration10SView, params1);
		}
	}

	private ImageView getSizeImageView(@DrawableRes int resId, int padding) {
		ImageView imageView = new ImageView(mContext);
		imageView.setPadding(padding, padding, padding, padding);
		imageView.setImageResource(resId);
		imageView.setOnClickListener(mOnClickListener);

		return imageView;
	}

	private TextView getDurationTextView(@StringRes int resId) {
		TextView textView = new TextView(mContext);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		textView.setTextColor(TEXT_NORMAL_COLOR);
		textView.setIncludeFontPadding(false);
		textView.setGravity(Gravity.CENTER);
		textView.setText(resId);
		textView.setOnClickListener(mOnClickListener);
		return textView;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == mSize9_16View) {
				setSize(CameraConfig.SIZE_9_16, true);
			} else if (v == mSize16_9View) {
				setSize(CameraConfig.SIZE_16_9, true);
			} else if (v == mSize235_1View) {
				setSize(CameraConfig.SIZE_235_1, true);
			} else if (v == mSize1_1View) {
				setSize(CameraConfig.SIZE_1_1, true);
			} else if (v == mDuration10SView) {
				setDuration(CameraConfig.DURATION_10S, true);
			} else if (v == mDuration30SView) {
				setDuration(CameraConfig.DURATION_30S, true);
			} else if (v == mDuration60SView) {
				setDuration(CameraConfig.DURATION_60S, true);
			} else if (v == mDuration180SView) {
				setDuration(CameraConfig.DURATION_180S, true);
			}
		}
	};

	public void setSize(int size) {
		setSize(size, false);
	}

	private void setSize(int size, boolean click) {
		if (mSize != size) {
			mSize = size;
			mSize9_16View.setImageResource(R.drawable.camera_size_9_16_unselect);
			mSize16_9View.setImageResource(R.drawable.camera_size_16_9_unselect);
			mSize235_1View.setImageResource(R.drawable.camera_size_235_1_unselect);
			mSize1_1View.setImageResource(R.drawable.camera_size_1_1_unselect);

			switch (mSize) {
				case CameraConfig.SIZE_9_16:
					mSize9_16View.setImageResource(R.drawable.camera_size_9_16);
					break;
				case CameraConfig.SIZE_16_9:
					mSize16_9View.setImageResource(R.drawable.camera_size_16_9);
					break;
				case CameraConfig.SIZE_235_1:
					mSize235_1View.setImageResource(R.drawable.camera_size_235_1);
					break;
				case CameraConfig.SIZE_1_1:
					mSize1_1View.setImageResource(R.drawable.camera_size_1_1);
					break;
			}

			if (click && mOnSettingsListener != null) {
				mOnSettingsListener.onClickSize(mSize);
			}
		}
	}

	public void setDuration(int duration) {
		setDuration(duration, false);
	}

	private void setDuration(int duration, boolean click) {
		if (mDuration != duration) {
			mDuration = duration;
			mDuration10SView.setTextColor(TEXT_NORMAL_COLOR);
			mDuration30SView.setTextColor(TEXT_NORMAL_COLOR);
			mDuration60SView.setTextColor(TEXT_NORMAL_COLOR);
			mDuration180SView.setTextColor(TEXT_NORMAL_COLOR);

			switch (mDuration) {
				case CameraConfig.DURATION_10S:
					mDuration10SView.setTextColor(TEXT_SELECT_COLOR);
					break;
				case CameraConfig.DURATION_30S:
					mDuration30SView.setTextColor(TEXT_SELECT_COLOR);
					break;
				case CameraConfig.DURATION_60S:
					mDuration60SView.setTextColor(TEXT_SELECT_COLOR);
					break;
				case CameraConfig.DURATION_180S:
					mDuration180SView.setTextColor(TEXT_SELECT_COLOR);
					break;
			}

			if (click && mOnSettingsListener != null) {
				mOnSettingsListener.onClickDuration(mDuration);
			}
		}
	}

	/**
	 * 设置旋转角度
	 *
	 * @param rotate 旋转角度
	 */
	public void setRotate(float rotate) {
		rotate = rotate % 360;
		if (mRotation != rotate) {
			mRotation = rotate;

			if (mAnimatorEnd) {
				createRotationAnim();
			}
		}
	}

	private float mRotation = 0;
	private AnimatorSet mAnimatorSet;
	private AnimatorListenerAdapter mAnimatorListener = new AnimatorListenerAdapter() {

		@Override
		public void onAnimationEnd(Animator animation) {
			mAnimatorEnd = true;
			if (mAnimatorSet != null) {
				mAnimatorSet.removeAllListeners();
				mAnimatorSet.end();
				mAnimatorSet = null;
			}

			if (mRotation != mSizeTitle.getRotation() && mRotation + 360 != mSizeTitle.getRotation()) {
				createRotationAnim();
			}
		}
	};
	private boolean mAnimatorEnd = true;

	private void createRotationAnim() {
		if (mSizeTitle.getRotation() == -90 && mRotation != 0) {
			setViewRotation(270);
		} else if (mSizeTitle.getRotation() == 270 && mRotation == 0) {
			setViewRotation(-90);
		}

		mAnimatorSet = new AnimatorSet();
		mAnimatorSet.playTogether(ObjectAnimator.ofFloat(mSizeTitle, "rotation", mSizeTitle.getRotation(), mRotation),
								  ObjectAnimator.ofFloat(mSize9_16View, "rotation", mSize9_16View.getRotation(), mRotation),
								  ObjectAnimator.ofFloat(mSize16_9View, "rotation", mSize16_9View.getRotation(), mRotation),
								  ObjectAnimator.ofFloat(mSize235_1View, "rotation", mSize235_1View.getRotation(), mRotation),
								  ObjectAnimator.ofFloat(mSize1_1View, "rotation", mSize1_1View.getRotation(), mRotation),
								  ObjectAnimator.ofFloat(mDurationTitle, "rotation", mDurationTitle.getRotation(), mRotation),
								  ObjectAnimator.ofFloat(mDuration10SView, "rotation", mDuration10SView.getRotation(), mRotation),
								  ObjectAnimator.ofFloat(mDuration30SView, "rotation", mDuration30SView.getRotation(), mRotation),
								  ObjectAnimator.ofFloat(mDuration60SView, "rotation", mDuration60SView.getRotation(), mRotation),
								  ObjectAnimator.ofFloat(mDuration180SView, "rotation", mDuration180SView.getRotation(), mRotation));
		mAnimatorSet.addListener(mAnimatorListener);
		mAnimatorSet.setDuration(CameraPage.ROTATION_ANIM_TIME);
		mAnimatorSet.start();
		mAnimatorEnd = false;
	}

	private void setViewRotation(float degree) {
		mSizeTitle.setRotation(degree);
		mSize9_16View.setRotation(degree);
		mSize16_9View.setRotation(degree);
		mSize235_1View.setRotation(degree);
		mSize1_1View.setRotation(degree);

		mDurationTitle.setRotation(degree);
		mDuration10SView.setRotation(degree);
		mDuration30SView.setRotation(degree);
		mDuration60SView.setRotation(degree);
		mDuration180SView.setRotation(degree);
	}

	public void release() {
		if (mAnimatorSet != null) {
			mAnimatorSet.removeAllListeners();
			mAnimatorSet.end();
			mAnimatorSet = null;
		}
	}

	public void setOnSettingsListener(OnSettingsListener listener) {
		mOnSettingsListener = listener;
	}

	public interface OnSettingsListener {

		void onClickSize(int size);

		void onClickDuration(int duration);
	}
}
