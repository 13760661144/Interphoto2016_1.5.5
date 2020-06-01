package cn.poco.capture2.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

import cn.poco.camera.CameraPage;
import cn.poco.capture2.AnimatorUtils;
import cn.poco.capture2.model.Snippet;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.OnAnimationClickListener;

/**
 * Created by: fwc
 * Date: 2017/12/28
 */
public class BottomControlView extends FrameLayout {

	private Context mContext;

	private ImageView mFilterView;

	private ShutterView mShutterView;

	private SettingsToastView mSettingsToastView;

	private ImageView mNextView;

	private boolean mUiEnable = false;

	private boolean mSettingsEnable = true;

	private OnBottomControlListener mControlListener;

	public BottomControlView(@NonNull Context context) {
		super(context);

		mContext = context;

		initViews();
	}

	@SuppressWarnings("all")
	private void initViews() {
		LayoutParams params;

		mFilterView = new ImageView(mContext);
		mFilterView.setImageResource(R.drawable.camera_filter);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(100), ShareData.PxToDpi_xhdpi(100));
		params.leftMargin = ShareData.PxToDpi_xhdpi(50);
		params.gravity = Gravity.CENTER_VERTICAL;
		addView(mFilterView, params);
		mFilterView.setOnTouchListener(mOnTouchListener);

		mShutterView = new ShutterView(mContext);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(160), ShareData.PxToDpi_xhdpi(160));
		params.gravity = Gravity.CENTER;
		addView(mShutterView, params);
		mShutterView.setOnTouchListener(mOnTouchListener);

		mSettingsToastView = new SettingsToastView(mContext);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(160), ShareData.PxToDpi_xhdpi(60));
		params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
		params.rightMargin = ShareData.PxToDpi_xhdpi(70);
		addView(mSettingsToastView, params);
		mSettingsToastView.setOnTouchListener(mOnTouchListener);

		mNextView = new ImageView(mContext);
		mNextView.setImageResource(R.drawable.camera_next);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
		params.rightMargin = ShareData.PxToDpi_xhdpi(68);
		addView(mNextView, params);
		mNextView.setOnTouchListener(mOnTouchListener);
		mNextView.setVisibility(GONE);
	}

	private OnTouchListener mOnTouchListener = new OnAnimationClickListener() {

		@Override
		public void onAnimationClick(View v) {

			if (v == mShutterView) {
				if (mShutterView.canClick() && mControlListener != null) {
					mControlListener.onClickShutter();
				}

				return;
			}

			if (!mUiEnable || mControlListener == null) {
				return;
			}

			if (v == mFilterView) {
				mControlListener.onClickFilter();
			} else if (v == mSettingsToastView) {
				if (mSettingsEnable) {
					mControlListener.onClickSettings();
				}
			} else if (v == mNextView) {
				mControlListener.onClickNext();
			}
		}
	};

	public void showGuidanceView(boolean show) {
		int visibility = show ? View.GONE : View.VISIBLE;
		mFilterView.setVisibility(visibility);
		mShutterView.setVisibility(visibility);
		mSettingsToastView.setVisibility(visibility);
	}

	public void showSettingsView() {
		if (mSettingsEnable) {
			mSettingsEnable = false;
			mSettingsToastView.animate().alpha(0.2f).setDuration(300);
			mSettingsToastView.setEnabled(false);
		}
	}

	public void hideSettingsView() {
		if (!mSettingsEnable) {
			mSettingsEnable = true;
			mSettingsToastView.animate().alpha(1).setDuration(300);
			mSettingsToastView.setEnabled(true);
		}
	}

	public void hideSettingToast() {
		mSettingsToastView.setVisibility(GONE);
	}

	public void setSize(int size) {
		mSettingsToastView.setSize(size);
	}

	public void setDuration(int duration) {
		mSettingsToastView.setDuration(duration);
	}

	public void startRecord() {
		mShutterView.startRecord();
		AnimatorUtils.hideView(mFilterView, 200);
		AnimatorUtils.hideView(mSettingsToastView, 200);
		AnimatorUtils.hideView(mNextView, 200);
	}

	public void addSnippet(Snippet snippet) {
		mShutterView.addSnippet(snippet);
	}

	public void deleteSnippet(boolean restart, boolean isAddMode) {

		if (restart) {
			AnimatorUtils.hideView(mNextView, 200);
			AnimatorUtils.showView(mFilterView, 200);
			if (!isAddMode) {
				AnimatorUtils.showView(mSettingsToastView, 200);
			}
		} else {
			AnimatorUtils.showView(mFilterView, 200);
		}

		mShutterView.deleteSnippet();
		mShutterView.setEnabled(true);
		AnimatorUtils.showView(mShutterView, 200);
	}

	public void setSnippets(List<Snippet> snippets) {
		mShutterView.setSnippets(snippets);
		mSettingsToastView.setVisibility(GONE);
		mNextView.setAlpha(1f);
		mNextView.setVisibility(VISIBLE);
	}

	public void stopRecord(boolean isFinish, boolean isRestart) {
		mShutterView.stopRecord();
		if (!isFinish) {
			AnimatorUtils.showView(mFilterView, 200);
		} else {
			mShutterView.setEnabled(false);
			AnimatorUtils.hideView(mShutterView, 200);
		}
		if (!isRestart) {
			AnimatorUtils.showView(mNextView, 200);
		}
	}

	/**
	 * 是否显示快门按钮
	 *
	 * @param show 是否显示
	 */
	public void showShutterView(boolean show) {
		if (show) {
			mShutterView.setVisibility(VISIBLE);
		} else {
			postDelayed(new Runnable() {
				@Override
				public void run() {
					mShutterView.setVisibility(GONE);
				}
			}, 50);
		}
	}

	public void setShutterClickEnabled(boolean enabled) {
		mShutterView.setEnabled(enabled);
		if (enabled) {
			AnimatorUtils.showView(mShutterView, 200);
		} else {
			AnimatorUtils.hideView(mShutterView, 200);
		}
	}

	public void refreshShutterView() {
		ViewCompat.postInvalidateOnAnimation(mShutterView);
	}

	public void setUiEnable(boolean uiEnable) {
		mUiEnable = uiEnable;
	}

	public void setOnBottomControlListener(OnBottomControlListener listener) {
		mControlListener = listener;
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

			if (mRotation != mFilterView.getRotation() && mRotation + 360 != mFilterView.getRotation()) {
				createRotationAnim();
			}
		}
	};
	private boolean mAnimatorEnd = true;

	private void createRotationAnim() {
		if (mFilterView.getRotation() == -90 && mRotation != 0) {
			setViewRotation(270);
		} else if (mFilterView.getRotation() == 270 && mRotation == 0) {
			setViewRotation(-90);
		}

		ObjectAnimator animator1, animator2, animator3;
		mAnimatorSet = new AnimatorSet();
		animator1 = ObjectAnimator.ofFloat(mFilterView, "rotation", mFilterView.getRotation(), mRotation);
		animator2 = ObjectAnimator.ofFloat(mNextView, "rotation", mNextView.getRotation(), mRotation);
		animator3 = ObjectAnimator.ofFloat(mSettingsToastView, "rotation", mSettingsToastView.getRotation(), mRotation);
		mAnimatorSet.playTogether(animator1, animator2, animator3);
		mAnimatorSet.addListener(mAnimatorListener);
		mAnimatorSet.setDuration(CameraPage.ROTATION_ANIM_TIME);
		mAnimatorSet.start();
		mAnimatorEnd = false;
	}

	private void setViewRotation(float degree) {
		mFilterView.setRotation(degree);
		mNextView.setRotation(degree);
		mSettingsToastView.setRotation(degree);
	}

	public void release() {
		if (mAnimatorSet != null) {
			mAnimatorSet.removeAllListeners();
			mAnimatorSet.end();
			mAnimatorSet = null;
		}
		mShutterView.release();
	}

	public interface OnBottomControlListener {

		void onClickShutter();

		void onClickFilter();

		void onClickSettings();

		void onClickNext();
	}
}
