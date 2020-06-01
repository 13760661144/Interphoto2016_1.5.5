package cn.poco.camera.view;

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

import cn.poco.capture2.AnimatorUtils;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/11/24
 */
public class GuidanceView extends FrameLayout {

	private Context mContext;

	private ImageView mTeachIcon;
	private TopGuideView mTeachView;

	private ImageView mMoreIcon;
	private TopGuideView mMoreView;

	private ImageView mFilterIcon;
	private BottomGuideView mFilterView;

	private TopGuideView mGestureView;

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

		mTeachIcon = new ImageView(mContext);
		mTeachIcon.setImageResource(R.drawable.camera_teach);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mTeachIcon, params);

		mTeachView = new TopGuideView(mContext);
		mTeachView.setTip(R.string.camera_first_teach);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mTeachView, params);

		mMoreIcon = new ImageView(mContext);
		mMoreIcon.setImageResource(R.drawable.camera_more);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mMoreIcon, params);

		mMoreView = new TopGuideView(mContext);
		mMoreView.setTip(R.string.camera_first_more);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mMoreView, params);

		mFilterIcon = new ImageView(mContext);
		mFilterIcon.setImageResource(R.drawable.camera_filter);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mFilterIcon, params);

		mFilterView = new BottomGuideView(mContext);
		mFilterView.setTip(R.string.camera_first_filter);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mFilterView, params);

		mGestureView = new TopGuideView(mContext);
		mGestureView.setImage(R.drawable.camera_video_gesture);
		mGestureView.setTip(R.string.camera_first_gesture);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mGestureView, params);

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
		float gap = (ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(4 * 48)) / 8f;
		int l = (int)(ShareData.m_screenWidth / 2f + gap + ShareData.PxToDpi_xhdpi(24) - mTeachView.getMeasuredWidth() / 2f);
		int t = ShareData.PxToDpi_xhdpi(94);
		mTeachView.layout(l, t, l + mTeachView.getMeasuredWidth(), t + mTeachView.getMeasuredHeight());

		l = (int)(ShareData.m_screenWidth / 2f + gap);
		mTeachIcon.layout(l, ShareData.PxToDpi_xhdpi(26), l + ShareData.PxToDpi_xhdpi(48), ShareData.PxToDpi_xhdpi(74));

		l = (int)(ShareData.m_screenWidth / 2f + 3 * gap + ShareData.PxToDpi_xhdpi(72) - mMoreView.getMeasuredWidth() / 2f);
		mMoreView.layout(l, t, l + mMoreView.getMeasuredWidth(), t + mMoreView.getMeasuredHeight());

		l = (int)(ShareData.m_screenWidth / 2f + 3 * gap + ShareData.PxToDpi_xhdpi(48));
		mMoreIcon.layout(l, ShareData.PxToDpi_xhdpi(26), l + ShareData.PxToDpi_xhdpi(48), ShareData.PxToDpi_xhdpi(74));

		l = ShareData.PxToDpi_xhdpi(100) - mFilterView.getMeasuredWidth() / 2;
		int b = getHeight() - ShareData.PxToDpi_xhdpi(180);
		mFilterView.layout(l, b - mFilterView.getMeasuredHeight(), l + mFilterView.getMeasuredWidth(), b);

		l = ShareData.PxToDpi_xhdpi(50);
		b = getHeight() - ShareData.PxToDpi_xhdpi(58);
		mFilterIcon.layout(l, b - ShareData.PxToDpi_xhdpi(100), l + ShareData.PxToDpi_xhdpi(100), b);

		l = ShareData.m_screenWidth / 2 - mGestureView.getMeasuredWidth() / 2;
		t = (int)(ShareData.m_screenWidth * 2f / 3) + ShareData.PxToDpi_xhdpi(100) - mGestureView.getMeasuredHeight() / 2;
		mGestureView.layout(l, t, l + mGestureView.getMeasuredWidth(), t + mGestureView.getMeasuredHeight());
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

		mTeachView.setAlpha(0);
		ObjectAnimator animator2 = ObjectAnimator.ofPropertyValuesHolder(mTeachView,
																		 PropertyValuesHolder.ofFloat("translationY", -distance, 0),
																		 PropertyValuesHolder.ofFloat("alpha", 0, 1)
		);
		animator2.setDuration(AnimatorUtils.DEFAULT_DURATION);

		mMoreView.setAlpha(0);
		ObjectAnimator animator3 = ObjectAnimator.ofPropertyValuesHolder(mMoreView,
																		 PropertyValuesHolder.ofFloat("translationY", -distance, 0),
																		 PropertyValuesHolder.ofFloat("alpha", 0, 1)
		);
		animator3.setDuration(AnimatorUtils.DEFAULT_DURATION);
		animator3.setStartDelay(300);

		mFilterView.setAlpha(0);
		ObjectAnimator animator4 = ObjectAnimator.ofPropertyValuesHolder(mFilterView,
																		 PropertyValuesHolder.ofFloat("translationY", distance, 0),
																		 PropertyValuesHolder.ofFloat("alpha", 0, 1)
		);
		animator4.setDuration(AnimatorUtils.DEFAULT_DURATION);
		animator4.setStartDelay(300);

		mGestureView.setAlpha(0);
		ObjectAnimator animator5 = ObjectAnimator.ofFloat(mGestureView, "alpha", 0, 1);
		animator5.setDuration(AnimatorUtils.DEFAULT_DURATION);
		animator5.setStartDelay(300);

		animatorSet.playSequentially(animator1, animator2, animator3, animator5, animator4);
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
