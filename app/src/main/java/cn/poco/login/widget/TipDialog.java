package cn.poco.login.widget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.FullScreenDlg;
import cn.poco.tianutils.ShareData;

/**
 * Created lgd on 2017/3/13.
 */

public class TipDialog extends FullScreenDlg
{

	private LinearLayout m_centerLl;
	private TextView m_message;
	private LinearLayout m_btnsLl;

	private int bkColor1 = 0xff404040;
	private int bkColor2 = 0xff262626;

	public TipDialog(Activity activity)
	{
		super(activity, R.style.waitDialog);
		initView();
	}

	public TipDialog(Activity activity, int theme)
	{
		super(activity, theme);
	}

	public TipDialog(Activity activity, boolean cancelable, OnCancelListener cancelListener)
	{
		super(activity, cancelable, cancelListener);
	}

	private void initView()
	{
		m_fr.setBackgroundColor(0x7f000000);

		m_centerLl = new LinearLayout(getContext());
		m_centerLl.setGravity(Gravity.CENTER);
		m_centerLl.setOrientation(LinearLayout.VERTICAL);
//		m_centerLl.setPadding(ShareData.PxToDpi_xhdpi(50), 0, ShareData.PxToDpi_xhdpi(50), 0);
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(620), ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		AddView(m_centerLl,fl);
		{
			LinearLayout.LayoutParams ll;
			m_message = new TextView(getContext());
			m_message.setBackgroundColor(bkColor1);
			m_message.setTextColor(Color.WHITE);
			m_message.setGravity(Gravity.CENTER);
			m_message.setMinHeight(ShareData.PxToDpi_xhdpi(210));
			m_message.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f);
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_message.setLayoutParams(ll);
			m_centerLl.addView(m_message);

			m_btnsLl = new LinearLayout(getContext());
			m_btnsLl.setGravity(Gravity.CENTER);
			m_btnsLl.setBackgroundColor(bkColor2);
			m_btnsLl.setOrientation(LinearLayout.HORIZONTAL);
			m_btnsLl.setMinimumHeight(ShareData.PxToDpi_xhdpi(90));
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_btnsLl.setLayoutParams(ll);
			m_centerLl.addView(m_btnsLl);
		}

	}


	public void setBackGround(Bitmap bitmap)
	{
		m_fr.setBackgroundDrawable(new BitmapDrawable(bitmap));
	}

	public void setMessage(String msg)
	{
		m_message.setText(msg);
	}

	public void setBtnText(String text, final OnCallBack onCallBack)
	{
		m_btnsLl.removeAllViews();
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		TextView btn = new TextView(getContext());
		btn.setBackgroundColor(bkColor2);
//		btn.setTextColor(Color.YELLOW);
		btn.setTextColor(0xffFFC433);
		btn.setGravity(Gravity.CENTER);
		btn.setText(text);
		btn.setLayoutParams(ll);
		m_btnsLl.addView(btn);
		btn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(onCallBack != null)
				{
					onCallBack.onBtnClick();
				}
			}
		});
	}

	public void setTwoBtnText(String leftText, String rightText, final OnCallBack2 onCallBack)
	{
		LinearLayout.LayoutParams ll;
		m_btnsLl.removeAllViews();
		TextView leftBtn = new TextView(getContext());
		leftBtn.setGravity(Gravity.CENTER);
		leftBtn.setTextColor(Color.YELLOW);
		leftBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f);
		leftBtn.setText(leftText);
		ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		ll.weight = 1;
		leftBtn.setLayoutParams(ll);
		m_btnsLl.addView(leftBtn);
		leftBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(onCallBack != null)
				{
					onCallBack.onLeftClick();
				}
			}
		});

		View line = new ImageView(getContext());
		line.setBackgroundColor(0xff595959);
		ll = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		line.setLayoutParams(ll);
		m_btnsLl.addView(line);

		TextView rightBtn = new TextView(getContext());
		rightBtn.setGravity(Gravity.CENTER);
		rightBtn.setText(rightText);
		rightBtn.setTextColor(Color.YELLOW);
		rightBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f);
		ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		ll.weight = 1;
		rightBtn.setLayoutParams(ll);
		m_btnsLl.addView(rightBtn);
		rightBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(onCallBack != null)
				{
					onCallBack.onRightClick();
				}
			}
		});
	}

	public interface OnCallBack
	{
		void onBtnClick();
	}

	public interface OnCallBack2
	{
		void onLeftClick();
		void onRightClick();
	}
}
