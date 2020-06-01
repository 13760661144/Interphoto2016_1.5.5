package cn.poco.capture2.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import cn.poco.camera.view.BottomGuideView;
import cn.poco.capture2.AnimatorUtils;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2018/1/4
 */
public class GuidanceView extends FrameLayout {

	private Context mContext;

	private ImageView mFilterIcon;
	private BottomGuideView mFilterView;

	private ShutterView mShutterIcon;
	private BottomGuideView mShutterView;

	private SettingsToastView mSettingsIcon;
	private BottomGuideView mSettingsView;

	private boolean isCanClick;

	private OnHideListener mOnHideListener;

	public GuidanceView(@NonNull Context context) {
		super(context);

		mContext = context;

		initViews();
	}

	private void initViews() {
		setBackgroundColor(0x90000000);
		LayoutParams params;

		mFilterIcon = new ImageView(mContext);
		mFilterIcon.setImageResource(R.drawable.camera_filter);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mFilterIcon, params);

		mFilterView = new BottomGuideView(mContext);
		mFilterView.setTip(R.string.camera_first_filter);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mFilterView, params);

		mShutterIcon = new ShutterView(mContext);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(160), ShareData.PxToDpi_xhdpi(160));
		addView(mShutterIcon, params);

		mShutterView = new BottomGuideView(mContext);
		mShutterView.setTip(R.string.camera_first_shutter);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mShutterView, params);

		mSettingsIcon = new SettingsToastView(mContext);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(160), ShareData.PxToDpi_xhdpi(60));
		addView(mSettingsIcon, params);

		mSettingsView = new BottomGuideView(mContext);
		mSettingsView.setTip(R.string.camera_first_settings);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mSettingsView, params);

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onHide();
			}
		});
	}

	public void onHide() {
		if (isCanClick && mOnHideListener != null) {
			isCanClick = false;
			mOnHideListener.onHide();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int l = ShareData.PxToDpi_xhdpi(100) - mFilterView.getMeasuredWidth() / 2;
		int b = getHeight() - ShareData.PxToDpi_xhdpi(180);
		mFilterView.layout(l, b - mFilterView.getMeasuredHeight(), l + mFilterView.getMeasuredWidth(), b);

		l = ShareData.PxToDpi_xhdpi(50);
		b = getHeight() - ShareData.PxToDpi_xhdpi(67);
		mFilterIcon.layout(l, b - ShareData.PxToDpi_xhdpi(100), l + ShareData.PxToDpi_xhdpi(100), b);

		l = (ShareData.m_screenWidth - mShutterView.getMeasuredWidth()) / 2;
		b = getHeight() - ShareData.PxToDpi_xhdpi(200);
		mShutterView.layout(l, b - mShutterView.getMeasuredHeight(), l + mShutterView.getMeasuredWidth(), b);

		l = (ShareData.m_screenWidth - mShutterIcon.getMeasuredWidth()) / 2;
		b = getHeight() - ShareData.PxToDpi_xhdpi(38);
		mShutterIcon.layout(l, b - mShutterIcon.getMeasuredHeight(), l + mShutterIcon.getMeasuredWidth(), b);

		int r = ShareData.m_screenWidth - (ShareData.PxToDpi_xhdpi(150) - mSettingsView.getMeasuredWidth() / 2);
		b = getHeight() - ShareData.PxToDpi_xhdpi(180);
		mSettingsView.layout(r - mSettingsView.getMeasuredWidth(), b - mSettingsView.getMeasuredHeight(), r, b);

		r = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(70);
		b = getHeight() - ShareData.PxToDpi_xhdpi(88);
		mSettingsIcon.layout(r - mSettingsIcon.getMeasuredWidth(), b - mSettingsIcon.getMeasuredHeight(), r, b);
	}

	public void show(ViewGroup parent, int bottomMargin) {

		int distance = ShareData.PxToDpi_xhdpi(50);

		setAlpha(0);
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.bottomMargin = bottomMargin;
		parent.addView(this, params);

		AnimatorSet animatorSet = new AnimatorSet();

		ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, "alpha", 0, 1);
		animator1.setDuration(AnimatorUtils.DEFAULT_DURATION);

		mFilterView.setAlpha(0);
		ObjectAnimator animator2 = ObjectAnimator.ofPropertyValuesHolder(mFilterView,
				PropertyValuesHolder.ofFloat("translationY", distance, 0),
				PropertyValuesHolder.ofFloat("alpha", 0, 1)
		);
		animator2.setDuration(AnimatorUtils.DEFAULT_DURATION);
		animator2.setStartDelay(300);

		mShutterView.setAlpha(0);
		ObjectAnimator animator3 = ObjectAnimator.ofPropertyValuesHolder(mShutterView,
				PropertyValuesHolder.ofFloat("translationY", distance, 0),
				PropertyValuesHolder.ofFloat("alpha", 0, 1)
		);
		animator3.setDuration(AnimatorUtils.DEFAULT_DURATION);

		mSettingsView.setAlpha(0);
		ObjectAnimator animator4 = ObjectAnimator.ofPropertyValuesHolder(mSettingsView,
				PropertyValuesHolder.ofFloat("translationY", distance, 0),
				PropertyValuesHolder.ofFloat("alpha", 0, 1)
		);
		animator4.setDuration(AnimatorUtils.DEFAULT_DURATION);
		animator4.setStartDelay(300);

		animatorSet.playSequentially(animator1, animator3, animator2, animator4);
		animatorSet.setStartDelay(AnimatorUtils.DEFAULT_DURATION);
		animatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				isCanClick = true;
			}
		});
		animatorSet.start();
	}

	public void setOnHideListener(OnHideListener listener) {
		mOnHideListener = listener;
	}

	public interface OnHideListener {
		void onHide();
	}
}
