package cn.poco.capture2.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v4.widget.Space;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.poco.camera.CameraPage;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.OnAnimationClickListener;

/**
 * Created by: fwc
 * Date: 2017/12/28
 */
public class TopControlView extends LinearLayout {

	private Context mContext;

	private ImageView mHomeView;
	private ImageView mSwitchView;
	private ImageView mFlashView;

	private boolean mUiEnable = false;

	private boolean mSwitchEnable = true;

	private boolean mFlashEnable = true;
	private boolean isOpenFlash = false;

	private OnTopControlListener mControlListener;

	public TopControlView(Context context) {
		super(context);

		mContext = context;

		initViews();
	}

	private void initViews() {
		setClickable(true);
		setOrientation(HORIZONTAL);

		addSpace(1);

		mHomeView = addImageView(R.drawable.camera_home);

		addSpace(2);

		mSwitchView = addImageView(R.drawable.camera_switch);

		addSpace(2);
		mFlashView = addImageView(R.drawable.camera_flash_close);

		addSpace(1);
	}

	@SuppressWarnings("all")
	private ImageView addImageView(@DrawableRes int resId) {
		ImageView imageView = new ImageView(mContext);
		imageView.setPadding(ShareData.PxToDpi_xhdpi(27), 0, ShareData.PxToDpi_xhdpi(27), 0);
		imageView.setScaleType(ImageView.ScaleType.CENTER);
		imageView.setImageResource(resId);
		imageView.setOnTouchListener(mOnTouchListener);

		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		addView(imageView, params);

		return imageView;
	}

	private void addSpace(int weight) {
		Space space = new Space(mContext);

		LayoutParams params = new LayoutParams(0, 0, weight);
		addView(space, params);
	}

	private OnTouchListener mOnTouchListener = new OnAnimationClickListener() {

		@Override
		public void onAnimationClick(View v) {

			if (!mUiEnable || mControlListener == null) {
				return;
			}

			if (v == mHomeView) {
				mControlListener.onClickHome();
			} else if (v == mSwitchView) {
				if (mSwitchEnable) {
					mControlListener.onClickSwitch();
				}
			} else if (v == mFlashView) {
				if (mFlashEnable) {
					isOpenFlash = !isOpenFlash;
					mFlashView.setImageResource(isOpenFlash ? R.drawable.camera_flash_open : R.drawable.camera_flash_close);
					mControlListener.onClickFlash(isOpenFlash);
				}
			}
		}

		@Override
		public void onTouch(View v) {

		}

		@Override
		public void onRelease(View v) {

		}
	};

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

			if (mRotation != mHomeView.getRotation() && mRotation + 360 != mHomeView.getRotation()) {
				createRotationAnim();
			}
		}
	};
	private boolean mAnimatorEnd = true;

	public void setRotate(float rotate) {
		rotate = rotate % 360;
		if (mRotation != rotate) {
			mRotation = rotate;

			if (mAnimatorEnd) {
				createRotationAnim();
			}
		}
	}

	private void createRotationAnim() {
		if (mHomeView.getRotation() == -90 && mRotation != 0) {
			setViewRotation(270);
		} else if (mHomeView.getRotation() == 270 && mRotation == 0) {
			setViewRotation(-90);
		}

		ObjectAnimator animator1, animator2, animator3;
		mAnimatorSet = new AnimatorSet();
		animator1 = ObjectAnimator.ofFloat(mHomeView, "rotation", mHomeView.getRotation(), mRotation);
		animator2 = ObjectAnimator.ofFloat(mSwitchView, "rotation", mSwitchView.getRotation(), mRotation);
		animator3 = ObjectAnimator.ofFloat(mFlashView, "rotation", mFlashView.getRotation(), mRotation);
		mAnimatorSet.playTogether(animator1, animator2, animator3);

		mAnimatorSet.addListener(mAnimatorListener);
		mAnimatorSet.setDuration(CameraPage.ROTATION_ANIM_TIME);
		mAnimatorSet.start();
		mAnimatorEnd = false;
	}

	private void setViewRotation(float degree) {
		mHomeView.setRotation(degree);
		mSwitchView.setRotation(degree);
		mFlashView.setRotation(degree);
	}

	public void setOnTopControlListener(OnTopControlListener listener) {
		mControlListener = listener;
	}

	public void setSwitchEnable(boolean enable) {
		mSwitchEnable = enable;
	}

	public void setFlashEnable(boolean enable) {
		mFlashEnable = enable;
		if (mFlashEnable) {
			mFlashView.setAlpha(1.0f);
		} else {
			mFlashView.setAlpha(0.3f);
		}
	}

	public void setFlashMode(boolean open) {
		if (isOpenFlash != open) {
			isOpenFlash = open;
			mFlashView.setImageResource(isOpenFlash ? R.drawable.camera_flash_open : R.drawable.camera_flash_close);
		}
	}

	public void release() {
		if (mAnimatorSet != null) {
			mAnimatorSet.removeAllListeners();
			mAnimatorSet.end();
			mAnimatorSet = null;
		}
	}

	public void setUiEnable(boolean uiEnable) {
		mUiEnable = uiEnable;
	}

	public interface OnTopControlListener {

		void onClickHome();

		void onClickSwitch();

		void onClickFlash(boolean open);
	}
}
