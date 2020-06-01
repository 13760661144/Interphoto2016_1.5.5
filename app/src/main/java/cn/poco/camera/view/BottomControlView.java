package cn.poco.camera.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import cn.poco.camera.CameraPage;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.OnAnimationClickListener;

/**
 * Created by: fwc
 * Date: 2017/10/9
 */
public class BottomControlView extends FrameLayout {

	private Context mContext;

	private ShutterView mShutterView;

	private ImageView mFilterView;

	private ImageView mAlbumView;

	private FrameLayout mLoadingLayout;
	private ImageView mLoadingView;
	private boolean isLoading = false;
	private ObjectAnimator mLoadingAnimator;

	private boolean mUiEnable = false;

	private boolean mOpenAlbumEnable = true;

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
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(132), ShareData.PxToDpi_xhdpi(132));
		params.gravity = Gravity.CENTER;
		addView(mShutterView, params);
		mShutterView.setOnTouchListener(mOnTouchListener);

		mAlbumView = new ImageView(mContext);
		mAlbumView.setImageResource(R.drawable.camera_open_album);
		mAlbumView.setScaleType(ImageView.ScaleType.CENTER);
		mAlbumView.setBackgroundColor(0xff262626);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(100), ShareData.PxToDpi_xhdpi(100));
		params.rightMargin = ShareData.PxToDpi_xhdpi(50);
		params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
		addView(mAlbumView, params);
		mAlbumView.setOnTouchListener(mOnTouchListener);

		mLoadingLayout = new FrameLayout(mContext);
		mLoadingLayout.setBackgroundColor(0xff262626);
		mLoadingLayout.setClickable(true);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(100), ShareData.PxToDpi_xhdpi(100));
		params.rightMargin = ShareData.PxToDpi_xhdpi(50);
		params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
		addView(mLoadingLayout, params);
		{
			mLoadingView = new ImageView(mContext);
			mLoadingView.setImageResource(R.drawable.camera_take_picture_loading);
			mLoadingView.setScaleType(ImageView.ScaleType.CENTER);
			params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			mLoadingLayout.addView(mLoadingView, params);
		}
		mLoadingLayout.setVisibility(GONE);
	}

	public void setUiEnable(boolean uiEnable) {
		mUiEnable = uiEnable;
	}

	public void setOpenAlbumEnable(boolean enable) {
		mOpenAlbumEnable = enable;
	}

	/**
	 * 设置相册缩略图
	 *
	 * @param bitmap Bitmap对象
	 */
	public void setAlbumThumb(Bitmap bitmap) {
		if (bitmap != null) {
			mAlbumView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			mAlbumView.setImageBitmap(bitmap);
		} else {
			mAlbumView.setImageResource(R.drawable.camera_open_album);
			mAlbumView.setScaleType(ImageView.ScaleType.CENTER);
			mAlbumView.setBackgroundColor(0xff262626);
		}
	}

	/**
	 * 隐藏相册入口
	 */
	public void hideAlbumView() {
		mAlbumView.setVisibility(INVISIBLE);
	}

	public void hideFilterView() {
		mFilterView.setVisibility(GONE);
	}

	public void showFilterView() {
		mFilterView.setVisibility(VISIBLE);
	}

	public boolean isHideAlbum() {
		return mAlbumView.getVisibility() != VISIBLE;
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

	public void setOnBottomControlListener(OnBottomControlListener controlListener) {
		mControlListener = controlListener;
	}

	public void setLoading(boolean loading) {
		if (isLoading != loading) {
			isLoading = loading;

			if (isLoading) {
				mAlbumView.setVisibility(GONE);
				mLoadingLayout.setVisibility(VISIBLE);
				mLoadingAnimator = ObjectAnimator.ofFloat(mLoadingView, "rotation", 0, 360);
				mLoadingAnimator.setDuration(500);
				mLoadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
				mLoadingAnimator.setRepeatMode(ValueAnimator.RESTART);
				mLoadingAnimator.setInterpolator(new LinearInterpolator());
				mLoadingAnimator.start();
			} else {
				if (mLoadingAnimator != null) {
					mLoadingAnimator.cancel();
					mLoadingAnimator = null;
				}

				mLoadingLayout.setVisibility(GONE);
				mAlbumView.setVisibility(VISIBLE);
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

			if (mRotation != mFilterView.getRotation() && mRotation + 360 != mFilterView.getRotation()) {
				createRotationAnim();
			}
		}
	};
	private boolean mAnimatorEnd = true;

	private void createRotationAnim() {
		if (mFilterView.getRotation() == -90 && mRotation != 0) {
			mFilterView.setRotation(270);
		} else if (mFilterView.getRotation() == 270 && mRotation == 0) {
			mFilterView.setRotation(-90);
		}

		ObjectAnimator animator1, animator2;
		mAnimatorSet = new AnimatorSet();
		animator1 = ObjectAnimator.ofFloat(mFilterView, "rotation", mFilterView.getRotation(), mRotation);
		animator2 = ObjectAnimator.ofFloat(mAlbumView, "rotation", mAlbumView.getRotation(), mRotation);
		mAnimatorSet.playTogether(animator1, animator2);
		mAnimatorSet.addListener(mAnimatorListener);
		mAnimatorSet.setDuration(CameraPage.ROTATION_ANIM_TIME);
		mAnimatorSet.start();
		mAnimatorEnd = false;
	}

	private OnTouchListener mOnTouchListener = new OnAnimationClickListener() {
		@Override
		public void onAnimationClick(View v) {

			// 不受mUiEnable控制
			if (v == mShutterView && mControlListener != null) {
				mControlListener.onClickShutter();
				return;
			}

			if (!mUiEnable || mControlListener == null) {
				return;
			}

			if (v == mFilterView) {
				mControlListener.onClickFilter();
			} else if (v == mAlbumView) {
				if (mOpenAlbumEnable) {
					mControlListener.onClickAlbum();
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

	public void release() {
		if (mAnimatorSet != null) {
			mAnimatorSet.removeAllListeners();
			mAnimatorSet.end();
			mAnimatorSet = null;
		}

		if (mLoadingAnimator != null) {
			mLoadingAnimator.removeAllListeners();
			mLoadingAnimator.cancel();
			mLoadingAnimator = null;
		}
	}

	public interface OnBottomControlListener {

		void onClickShutter();
		void onClickFilter();
		void onClickAlbum();
	}
}
