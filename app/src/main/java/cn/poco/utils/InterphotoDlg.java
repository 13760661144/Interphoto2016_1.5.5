package cn.poco.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.FullScreenDlg;
import cn.poco.tianutils.ShareData;

/**
 * 印象弹框
 */
public class InterphotoDlg extends FullScreenDlg
{
	public static final int POSITIVE = 1;
	public static final int NEGATIVE = 2;
	public static final int ALL = 0;
	public static int text_size = 14;
	protected final int lint_color = 0xff595959;
	protected TextView text;
	protected TextView m_title;
	protected OnDlgClickCallback m_cb;
	protected OnDlgClickCallback1 m_cb1;
	protected LinearLayout viewFr;
	protected LinearLayout btnFr;
	protected TextView m_leftBtn;
	protected TextView m_rightBtn;
	protected ImageView m_line;
	protected int m_btnType = ALL;

	public InterphotoDlg(Activity activity)
	{
		super(activity);
		InitUI(activity);
	}

	public InterphotoDlg(Activity activity, int theme)
	{
		super(activity, theme);
		InitUI(activity);
	}

	public InterphotoDlg(Activity activity, boolean cancelable, OnCancelListener cancelListener)
	{
		super(activity, cancelable, cancelListener);
		InitUI(activity);
	}

	public void setOnDlgClickCallback(OnDlgClickCallback cb)
	{
		m_cb = cb;
	}

	public void setOnDlgClickCallback1(OnDlgClickCallback1 cb)
	{
		m_cb1 = cb;
	}

	public void InitUI(Activity context)
	{
		ShareData.InitData(context);
		FrameLayout.LayoutParams fl;
		m_fr.setBackgroundColor(0x77000000);

		int viewW = ShareData.PxToDpi_xhdpi(620);
		viewFr = new LinearLayout(context);
		viewFr.setBackgroundColor(0xff404040);
		viewFr.setOrientation(LinearLayout.VERTICAL);
		viewFr.setClickable(true);
		fl = new FrameLayout.LayoutParams(viewW, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		AddView(viewFr, fl);
		initTipUI(context);

		setCancelable(false);
	}

	protected void initTipUI(Context context)
	{
		viewFr.removeAllViews();
		LinearLayout.LayoutParams ll;

		m_title = new TextView(context);
		m_title.setVisibility(View.GONE);
		m_title.setPadding(ShareData.PxToDpi_xhdpi(30), ShareData.PxToDpi_xhdpi(50), ShareData.PxToDpi_xhdpi(30), ShareData.PxToDpi_xhdpi(50));
		m_title.setGravity(Gravity.CENTER);
		m_title.setLineSpacing(1.0f, 1.5f);
		m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		m_title.setTextColor(Color.WHITE);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		m_title.setLayoutParams(ll);
		viewFr.addView(m_title);

		text = new TextView(context);
		text.setVisibility(View.GONE);
		text.setGravity(Gravity.CENTER);
		text.setLineSpacing(1.0f, 1.5f);
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
		text.setTextColor(Color.WHITE);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		ll.bottomMargin = ShareData.PxToDpi_xhdpi(50);
		text.setLayoutParams(ll);
		viewFr.addView(text);

		ImageView line = new ImageView(context);
		line.setBackgroundColor(lint_color);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		line.setLayoutParams(ll);
		viewFr.addView(line);

		btnFr = new LinearLayout(context);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		btnFr.setLayoutParams(ll);
		viewFr.addView(btnFr);
		{
			m_leftBtn = new TextView(context);
			m_leftBtn.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20));
			m_leftBtn.setBackgroundColor(0xff272727);
			m_leftBtn.setGravity(Gravity.CENTER);
			m_leftBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
			m_leftBtn.setTextColor(0xffa6a6a6);
			m_leftBtn.setText(context.getResources().getString(R.string.Cancel));
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.weight = 1;
			m_leftBtn.setLayoutParams(ll);
			btnFr.addView(m_leftBtn);
			m_leftBtn.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if(m_cb != null)
					{
						m_cb.onCancel();
					}
					if(m_cb1 != null){
						m_cb1.onLeft();
					}

				}
			});

			m_line = new ImageView(context);
			m_line.setBackgroundColor(lint_color);
			ll = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			m_line.setLayoutParams(ll);
			btnFr.addView(m_line);

			m_rightBtn = new TextView(context);
			m_rightBtn.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20));
			m_rightBtn.setBackgroundColor(0xff272727);
			m_rightBtn.setGravity(Gravity.CENTER);
			m_rightBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
			m_rightBtn.setTextColor(0xffffce54);
			m_rightBtn.setText(context.getResources().getString(R.string.ok));
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.weight = 1;
			m_rightBtn.setLayoutParams(ll);
			btnFr.addView(m_rightBtn);
			m_rightBtn.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if(m_cb != null)
					{
						m_cb.onOK();
					}
					if(m_cb1 != null){
						m_cb1.onRight();
					}
				}
			});
		}
	}

	public void SetTitle(String title)
	{
		if(!TextUtils.isEmpty(title))
		{
			m_title.setVisibility(View.VISIBLE);
			m_title.setText(title);
		}
	}

	public void SetTitle(int title)
	{
		m_title.setVisibility(View.VISIBLE);
		m_title.setText(title);
	}

	public void SetTitle(int title, boolean alignLeft) {
		if (alignLeft) {
			m_title.setGravity(Gravity.START);
		}

		m_title.setVisibility(View.VISIBLE);
		m_title.setText(title);
	}

	public void SetMessage(int msg)
	{
		text.setVisibility(View.VISIBLE);
		text.setText(msg);
	}

	public void SetMessage(String msg)
	{
		if(!TextUtils.isEmpty(msg))
		{
			text.setVisibility(View.VISIBLE);
			text.setText(msg);
		}
	}

	public void SetMessagePadding(int left, int right, int top, int bottom)
	{
		text.setPadding(left, top, right, bottom);
	}

	public void setOnOutsideClickListener(View.OnClickListener outsideClickListener) {
		m_fr.setOnClickListener(outsideClickListener);
	}

	public void SetBtnType(int type)
	{
		m_btnType = type;
		switch(m_btnType)
		{
			case POSITIVE:
			{
				m_rightBtn.setVisibility(View.VISIBLE);
				m_leftBtn.setVisibility(View.GONE);
				m_line.setVisibility(View.GONE);
				break;
			}
			case NEGATIVE:
			{
				m_rightBtn.setVisibility(View.GONE);
				m_leftBtn.setVisibility(View.VISIBLE);
				m_line.setVisibility(View.GONE);
				break;
			}
			case ALL:
			{
				m_rightBtn.setVisibility(View.VISIBLE);
				m_leftBtn.setVisibility(View.VISIBLE);
				m_line.setVisibility(View.VISIBLE);
				break;
			}
		}
	}

	public void SetPositiveBtnText(String text)
	{
		m_rightBtn.setText(text);
	}

	public void SetPositiveBtnText(int text)
	{
		m_rightBtn.setText(text);
	}

	public void SetNegativeBtnText(int text)
	{
		m_leftBtn.setText(text);
	}

	public void SetNegativeBtnText(String text)
	{
		m_leftBtn.setText(text);
	}

	public void SetLeftBtnColor(int color)
	{
		m_leftBtn.setTextColor(color);
	}

	public void SetRightBtnColor(int color)
	{
		m_rightBtn.setTextColor(color);
	}

	public void changeLeftRightBtnColor()
	{
		m_leftBtn.setTextColor(0xffffce54);
		m_rightBtn.setTextColor(0xffa6a6a6);
	}

	public void setLeftRightBtnColor(int leftColor, int rightColor)
	{
		m_leftBtn.setTextColor(leftColor);
		m_rightBtn.setTextColor(rightColor);
	}

	public static interface OnDlgClickCallback
	{
		public void onOK();
		public void onCancel();
	}

	public static interface OnDlgClickCallback1
	{
		public void onLeft();
		public void onRight();
	}

}
