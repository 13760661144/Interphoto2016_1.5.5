package cn.poco.camera.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import cn.poco.camera.CameraPage;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/10/9
 */
public class MoreControl extends FrameLayout {

	private Context mContext;

	private FrameLayout mLayout;
	private MoreControlPanel mMoreControlPanel;

	private boolean isOpen = false;

	private boolean isRunningAnimation = false;

	public MoreControl(@NonNull Context context) {
		super(context);

		mContext = context;

		initViews();
	}

	private void initViews() {

		setPadding(0, ShareData.PxToDpi_xhdpi(122), 0, 0);
		setClipToPadding(false);

		mLayout = new FrameLayout(mContext);
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.leftMargin = params.rightMargin = ShareData.PxToDpi_xhdpi(20);
		addView(mLayout, params);
		{
			mMoreControlPanel = new MoreControlPanel(mContext);
			params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			mLayout.addView(mMoreControlPanel, params);
		}
	}

	public void setOnMoreControlListener(MoreControlPanel.OnMoreControlListener listener) {
		mMoreControlPanel.setOnMoreControlListener(listener);
	}

	public void setFlashEnable(boolean flashEnable) {
		mMoreControlPanel.setFlashEnable(flashEnable);
	}

	public void setFlash(int flashMode) {
		mMoreControlPanel.setFlash(flashMode);
	}

	public void setPreviewSize(int previewSize) {
		mMoreControlPanel.setSize(previewSize);
	}

	public void setTimer(int timer) {
		mMoreControlPanel.setTimer(timer);
	}

	public void setTouchCapture(boolean isTouchCapture) {
		mMoreControlPanel.setTouchCapture(isTouchCapture);
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

			if (mRotation != mLayout.getRotation() && mRotation + 360 != mLayout.getRotation()) {
				createRotationAnim();
			}
		}
	};
	private boolean mAnimatorEnd = true;

	private void createRotationAnim() {
		if (mLayout.getRotation() == -90 && mRotation != 0) {
			mLayout.setRotation(270);
		} else if (mLayout.getRotation() == 270 && mRotation == 0) {
			mLayout.setRotation(-90);
		}

		int offsetY = 0;
		if ((mRotation + 360) % 180 != 0) {
			offsetY = ShareData.PxToDpi_xhdpi(122);
			int count = mMoreControlPanel.isFlashEnable() ? 0 : 1;
			offsetY += count * ShareData.PxToDpi_xhdpi(55);
		}

		mAnimatorSet = new AnimatorSet();
		ObjectAnimator animator1, animator2;
		animator1 = ObjectAnimator.ofFloat(mLayout, "rotation", mLayout.getRotation(), mRotation);
		animator2 = ObjectAnimator.ofFloat(mLayout, "translationY", mLayout.getTranslationY(), offsetY);
		mAnimatorSet.playTogether(animator1, animator2);
		mAnimatorSet.addListener(mAnimatorListener);
		mAnimatorSet.setDuration(CameraPage.ROTATION_ANIM_TIME);
		mAnimatorSet.start();
		mAnimatorEnd = false;
	}

	public void openOrClose(FrameLayout container) {
		if (isOpen) {
			close();
		} else {
			open(container);
		}
	}

	public void open(FrameLayout container) {
		if (!isOpen && !isRunningAnimation) {
			isRunningAnimation = true;
			LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			container.addView(this, params);

			Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.cloudalbum_slide_in_top);
			animation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					isRunningAnimation = false;
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
			mMoreControlPanel.startAnimation(animation);

			isOpen = true;

			MyBeautyStat.onPageStartByRes(R.string.拍照_镜头设置);
		}
	}

	public void close() {
		if (isOpen && !isRunningAnimation) {
			isRunningAnimation = true;
			Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.cloudalbum_slide_out_top);
			animation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					ViewParent viewParent = getParent();
					if (viewParent instanceof ViewGroup) {
						((ViewGroup)viewParent).removeView(MoreControl.this);
					}
					isOpen = false;
					isRunningAnimation = false;
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});

			mMoreControlPanel.startAnimation(animation);

			MyBeautyStat.onPageEndByRes(R.string.拍照_镜头设置);
		}
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void release() {
		if (mAnimatorSet != null) {
			mAnimatorSet.removeAllListeners();
			mAnimatorSet.end();
			mAnimatorSet = null;
		}
	}
}
