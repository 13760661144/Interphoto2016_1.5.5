package cn.poco.camera.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.poco.camera.CameraConfig;
import cn.poco.camera.CameraPage;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/5/24
 */
public class CameraLayout extends FrameLayout {

	private Context mContext;

	private FrameLayout mCameraPreview;

	private GLCameraView mGLCameraView;
	private CameraRenderer mCameraRenderer;

	/**
	 * Camera预览高度
	 */
	private int mCameraViewHeight;

	private int mDefaultTopHeight;
	private int mDefaultBottomHeight;

	/**
	 * 是否显示构图
	 */
	private boolean isShowTeachView;

	/**
	 * 构图/教程辅助线
	 */
	private ImageView mCameraTeachMaskView;

	/**
	 * 倒计时
	 */
	private TextView mTimerView;

	/**
	 * 对焦和测光指示View
	 */
	private FocusRectView mFocusRectView;

	/**
	 * 黑色遮罩，放在最后添加
	 */
	private BlackMaskView mBlackMaskView;

	private boolean isRotation;

	public CameraLayout(@NonNull Context context) {
		this(context, null);
	}

	public CameraLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		init();
	}

	private void init() {
		mCameraViewHeight = (int) (ShareData.m_screenWidth * (16.0f / 9));

		initViews();
	}

	private void initViews() {
		LayoutParams params;

		mCameraPreview = new FrameLayout(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mCameraViewHeight);
		addView(mCameraPreview, params);
		{
			mGLCameraView = new GLCameraView(mContext);
			params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			mCameraPreview.addView(mGLCameraView, params);

			mCameraRenderer = mGLCameraView.getRenderer();
		}

		mCameraTeachMaskView = new ImageView(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mCameraTeachMaskView, params);

		mTimerView = new TextView(mContext);
		mTimerView.setGravity(Gravity.CENTER);
		mTimerView.setTextColor(Color.WHITE);
		mTimerView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 96);
		mTimerView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/CodeLight.otf"));
		mTimerView.setVisibility(View.INVISIBLE);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(200), ShareData.PxToDpi_xhdpi(200));
		params.gravity = Gravity.CENTER;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(50);
		addView(mTimerView, params);

		mFocusRectView = new FocusRectView(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mFocusRectView, params);

		// 放到最后
		mBlackMaskView = new BlackMaskView(mContext);
//		mBlackMaskView.setClickable(true);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mCameraViewHeight);
		addView(mBlackMaskView, params);
	}

	/**
	 * 设置默认的top和bottom高度
	 *
	 * @param topHeight top高度
	 * @param bottomHeight bottom高度
	 */
	public void setTopAndBottomHeight(int topHeight, int bottomHeight) {
		mDefaultTopHeight = topHeight;
		mDefaultBottomHeight = bottomHeight;
	}

	/**
	 * 改变预览比率
	 *
	 * @param ratioMode 预览模式
	 * @param HWratio 高宽预览比率
	 * @return 可触控范围大小
	 */
	public int[] changePreviewRatio(int ratioMode, float HWratio) {

		int targetHeight = (int) (ShareData.m_screenWidth * HWratio + 0.5f) / 10 * 10;
		int blackHeight = mCameraViewHeight - targetHeight;
		int topBH = mDefaultTopHeight;
		int bottomBH = mDefaultBottomHeight;

		if (topBH + bottomBH > blackHeight) {
			topBH = 0;
		}

		int[] touchArea = new int[2];
		if (ratioMode == CameraConfig.SIZE_1_1) {
			int avg = (blackHeight - topBH - bottomBH) / 2;
			touchArea[0] = topBH + avg;
			touchArea[1] = touchArea[0] + targetHeight;

			mBlackMaskView.setTopAndBottom(topBH + avg, bottomBH + avg);

		} else if (ratioMode == CameraConfig.SIZE_3_4) {
			touchArea[0] = topBH;
			touchArea[1] = touchArea[0] + targetHeight;

			mBlackMaskView.setTopAndBottom(topBH, blackHeight - topBH);
		} else if (ratioMode == CameraConfig.SIZE_9_16) {
			touchArea[0] = topBH;
			touchArea[1] = mCameraViewHeight - bottomBH;

			mBlackMaskView.setTopAndBottom(0, 0);
		} else if (ratioMode == CameraConfig.SIZE_235_1) {

			if (isRotation) {

				int height = (int)(ShareData.m_screenWidth * 16f / 9) / 10 * 10;
				int width = (int)(height / 2.35f) / 10 * 10;

				int avg = (ShareData.m_screenWidth - width) / 2;

				touchArea[0] = avg;
				touchArea[1] = avg + width;

				mBlackMaskView.setTopAndBottom(touchArea[0], touchArea[1]);

				return touchArea;
			} else {
				int avg = (blackHeight - topBH - bottomBH) / 2;
				touchArea[0] = topBH + avg;
				touchArea[1] = touchArea[0] + targetHeight;

				mBlackMaskView.setTopAndBottom(topBH + avg, bottomBH + avg);
			}
		} else if (ratioMode == CameraConfig.SIZE_16_9) {
			int avg = (blackHeight - topBH - bottomBH) / 2;
			touchArea[0] = topBH + avg;
			touchArea[1] = touchArea[0] + targetHeight;

			mBlackMaskView.setTopAndBottom(topBH + avg, bottomBH + avg);
		}

		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, targetHeight);
		params.topMargin = touchArea[0];
		mCameraTeachMaskView.setLayoutParams(params);

		return touchArea;
	}

	public void setRotation(boolean isRotation) {
		this.isRotation = isRotation;
		mBlackMaskView.setRotation(isRotation);
	}

	private float mRotation = 0;
	private ObjectAnimator mRotationAnimator;
	private AnimatorListenerAdapter mAnimatorListener = new AnimatorListenerAdapter() {

		@Override
		public void onAnimationEnd(Animator animation) {
			mAnimatorEnd = true;
			if (mRotationAnimator != null) {
				mRotationAnimator.removeAllListeners();
				mRotationAnimator.end();
				mRotationAnimator = null;
			}

			if (mRotation != mTimerView.getRotation() && mRotation + 360 != mTimerView.getRotation()) {
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
		if (mTimerView.getRotation() == -90 && mRotation != 0) {
			mTimerView.setRotation(270);
		} else if (mTimerView.getRotation() == 270 && mRotation == 0) {
			mTimerView.setRotation(-90);
		}
		mRotationAnimator = ObjectAnimator.ofFloat(mTimerView, "rotation", mTimerView.getRotation(), mRotation);
		mRotationAnimator.addListener(mAnimatorListener);
		mRotationAnimator.setDuration(CameraPage.ROTATION_ANIM_TIME);
		mRotationAnimator.start();
		mAnimatorEnd = false;
	}

	public void showTimerView() {
		if (mTimerView.getVisibility() != VISIBLE) {
			mTimerView.setVisibility(VISIBLE);
		}
	}

	public void hideTimerView() {
		if (mTimerView.getVisibility() == VISIBLE) {
			mTimerView.setText("");
			mTimerView.setVisibility(INVISIBLE);
		}
	}

	public void removeTimerView() {
		removeView(mTimerView);
	}

	public void changeTimer(int time) {
		mTimerView.setText(String.valueOf(time));
	}

	public void hideFocusView() {
		mFocusRectView.clearAll();
	}

	public void releaseFocusView() {
		mFocusRectView.clearAll();
		mFocusRectView.release();
	}

	public int getTouchAreaIndex(float x, float y) {
		return mFocusRectView.getTouchAreaIndex(x, y);
	}

	public void setFocusAndMeteringLocation(float fx, float fy, float mx, float my) {
		mFocusRectView.setFocusAndMeteringLocation(fx, fy, mx, my);
	}

	public void setFocusLocation(float x, float y) {
		mFocusRectView.setFocusLocation(x, y);
	}

	public void setMeteringLocation(float x, float y) {
		mFocusRectView.setMeteringLocation(x, y);
	}

	@Deprecated
	public RectF[] getViewLocation() {
		return mFocusRectView.getViewLocation();
	}

	public float[] getFoucuAndMeterPosition() {
		return mFocusRectView.getFoucuAndMeterPosition();
	}

	public void setImageResource(@DrawableRes int resId) {
		mCameraTeachMaskView.setImageResource(resId);
		isShowTeachView = true;
	}

	public void setImageBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			isShowTeachView = false;
		} else {
			isShowTeachView = true;
		}
		mCameraTeachMaskView.setImageBitmap(bitmap);
	}

	public boolean isShowTeachView() {
		return isShowTeachView;
	}

	public GLCameraView getGLCameraView() {
		return mGLCameraView;
	}

	public void release() {
		if (mRotationAnimator != null) {
			mRotationAnimator.removeAllListeners();
			mRotationAnimator.end();
			mRotationAnimator = null;
		}
	}
}
