package cn.poco.login.widget;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by lgd on 2017/2/6.
 */

public class CommonBtn extends FrameLayout
{

	private TextView textOkLogin;
	private ImageView loginLoading;
	private boolean mUiEnable = true;

	public CommonBtn(Context context)
	{
		super(context);
		init();
	}

	private void init()
	{
		this.setBackgroundResource(R.drawable.login_comfir_btn_bg);

		FrameLayout.LayoutParams fl;
		fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(608), ShareData.PxToDpi_xhdpi(96));
		fl.gravity = Gravity.CENTER;
		textOkLogin = new TextView(getContext());
		textOkLogin.setGravity(Gravity.CENTER);
		textOkLogin.setText(R.string.Done);
		textOkLogin.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		textOkLogin.setTextColor(0xff666666);
		addView(textOkLogin, fl);

		loginLoading = new ImageView(getContext());
		loginLoading.setScaleType(ImageView.ScaleType.CENTER);
		fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(46), ShareData.PxToDpi_xhdpi(46));
		fl.gravity = Gravity.CENTER;
		loginLoading.setImageResource(R.drawable.login_loading_logo);
		loginLoading.setVisibility(GONE);
		addView(loginLoading, fl);
	}

	public void setText(String text)
	{
		textOkLogin.setText(text);
	}

	public void setText(int id)
	{
		textOkLogin.setText(getResources().getString(id));
	}

	public void setLoadingState()
	{
		this.setEnabled(false);
		mUiEnable = false;

		loginLoading.setVisibility(VISIBLE);
		final RotateAnimation animation = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(500);
		animation.setRepeatCount(-1);
		loginLoading.startAnimation(animation);
		textOkLogin.setVisibility(GONE);
	}

	public void setNormalState()
	{
		mUiEnable = true;
		this.setEnabled(true);

		loginLoading.setVisibility(GONE);
		loginLoading.clearAnimation();
		loginLoading.setAnimation(null);
		textOkLogin.setVisibility(VISIBLE);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		if(mUiEnable) {
			super.setEnabled(enabled);
			if (enabled) {
				textOkLogin.setTextColor(0xff111111);
			} else {
//			textOkLogin.setTextColor(0xff646262);
				textOkLogin.setTextColor(0x33d9d9d9);
			}
		}
	}
}
