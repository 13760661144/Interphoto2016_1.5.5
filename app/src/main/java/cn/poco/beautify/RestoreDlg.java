package cn.poco.beautify;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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

public class RestoreDlg extends FullScreenDlg
{
	public static int text_size = 14;
	protected final int lint_color = 0xff595959;
	protected TextView m_title;
	protected OnDlgClickCallback m_cb;
	protected LinearLayout viewFr;
	protected LinearLayout btnFr;
	protected TextView m_leftBtn;
	protected TextView m_rightBtn;

	public RestoreDlg(Activity activity)
	{
		super(activity);
		InitUI(activity);
	}

	public RestoreDlg(Activity activity, int theme)
	{
		super(activity, theme);
		InitUI(activity);
	}

	public RestoreDlg(Activity activity, boolean cancelable, OnCancelListener cancelListener)
	{
		super(activity, cancelable, cancelListener);
		InitUI(activity);
	}

	public void setOnDlgClickCallback(OnDlgClickCallback cb)
	{
		m_cb = cb;
	}

	public void InitUI(Activity context)
	{
		ShareData.InitData(context);
		FrameLayout.LayoutParams fl;
		m_fr.setBackgroundColor(0x77000000);
		m_fr.removeAllViews();
		m_fr.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dismiss();
			}
		});

		int viewW = ShareData.PxToDpi_xhdpi(620);
		viewFr = new LinearLayout(context);
		viewFr.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

			}
		});
		viewFr.setBackgroundColor(0xff404040);
		viewFr.setOrientation(LinearLayout.VERTICAL);
		fl = new FrameLayout.LayoutParams(viewW, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		AddView(viewFr, fl);
		initTipUI(context);

		setCancelable(false);
	}

	protected void initTipUI(Context context)
	{
		LinearLayout.LayoutParams ll;

		m_title = new TextView(context);
		m_title.setPadding(ShareData.PxToDpi_xhdpi(30), ShareData.PxToDpi_xhdpi(50), ShareData.PxToDpi_xhdpi(30), ShareData.PxToDpi_xhdpi(50));
		m_title.setText(getContext().getResources().getString(R.string.restore_tip));
		m_title.setGravity(Gravity.CENTER);
		m_title.setLineSpacing(1.0f, 1.5f);
		m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		m_title.setTextColor(Color.WHITE);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		m_title.setLayoutParams(ll);
		viewFr.addView(m_title);

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
			m_leftBtn.setTextColor(0xffffce54);
			m_leftBtn.setText(context.getResources().getString(R.string.restore_last));
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
						m_cb.onUndo();
					}

				}
			});

			line = new ImageView(context);
			line.setBackgroundColor(lint_color);
			ll = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			line.setLayoutParams(ll);
			btnFr.addView(line);

			m_rightBtn = new TextView(context);
			m_rightBtn.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20));
			m_rightBtn.setBackgroundColor(0xff272727);
			m_rightBtn.setGravity(Gravity.CENTER);
			m_rightBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
			m_rightBtn.setTextColor(0xffffce54);
			m_rightBtn.setText(context.getResources().getString(R.string.restore_org));
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
						m_cb.onReset();
					}
				}
			});
		}
	}

	public void SetTitle(String title)
	{
		m_title.setText(title);
	}

	public void SetLeftBtnText(String text)
	{
		m_leftBtn.setText(text);
	}

	public void SetRightBtnText(String text)
	{
		m_rightBtn.setText(text);
	}

	public static interface OnDlgClickCallback
	{
		public void onReset();
		public void onUndo();
	}

}
