package cn.poco.beautify;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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

public class TextTipDlg extends FullScreenDlg
{
	protected ImageView m_closeBtn;
	protected LinearLayout m_addBtn;
	protected Callback m_cb;

	public TextTipDlg(Activity activity, Callback cb)
	{
		super(activity);
		m_cb = cb;
		InitUI(activity);
	}

	public TextTipDlg(Activity activity, int theme, Callback cb)
	{
		super(activity, theme);
		m_cb = cb;
		InitUI(activity);
	}

	public TextTipDlg(Activity activity, boolean cancelable, OnCancelListener cancelListener, Callback cb)
	{
		super(activity, cancelable, cancelListener);
		m_cb = cb;
		InitUI(activity);
	}
	
	protected void InitUI(Activity activity)
	{
		ShareData.InitData(activity);
		m_fr.setOnClickListener(m_btnListener);
		
		FrameLayout.LayoutParams fl;
		LinearLayout.LayoutParams ll;
		m_closeBtn = new ImageView(getContext());
		m_closeBtn.setImageResource(R.drawable.beauty_text_tip_close);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.TOP | Gravity.RIGHT;
		fl.topMargin = ShareData.PxToDpi_xhdpi(34);
		fl.rightMargin = ShareData.PxToDpi_xhdpi(40);
		m_closeBtn.setLayoutParams(fl);
		m_fr.addView(m_closeBtn);
		m_closeBtn.setOnClickListener(m_btnListener);
		
		m_addBtn = new LinearLayout(activity);
		m_addBtn.setBackgroundResource(R.drawable.beauty_text_add_btn);
		m_addBtn.setGravity(Gravity.CENTER);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
		fl.gravity = Gravity.BOTTOM;
		m_addBtn.setLayoutParams(fl);
		m_fr.addView(m_addBtn);
		m_addBtn.setOnClickListener(m_btnListener);
		{
			ImageView img = new ImageView(getContext());
			img.setImageResource(R.drawable.beauty_text_alum_import_icon);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_VERTICAL;
			img.setLayoutParams(ll);
			m_addBtn.addView(img);
			
			TextView text = new TextView(getContext());
			text.setTextColor(Color.WHITE);
			text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			text.setText(getContext().getResources().getString(R.string.addNow));
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_VERTICAL;
			ll.leftMargin = ShareData.PxToDpi_xhdpi(30);
			text.setLayoutParams(ll);
			m_addBtn.addView(text);
		}
		
		LinearLayout cenFr = new LinearLayout(activity);
		cenFr.setOrientation(LinearLayout.VERTICAL);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		fl.topMargin = ShareData.PxToDpi_xhdpi(300);
		cenFr.setLayoutParams(fl);
		m_fr.addView(cenFr);
		{
			ImageView img = new ImageView(getContext());
			img.setImageResource(R.drawable.beauty_text_tip_logo);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			img.setLayoutParams(ll);
			cenFr.addView(img);
			
			TextView text = new TextView(getContext());
			text.setTextColor(Color.WHITE);
			text.setGravity(Gravity.CENTER);
			text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			text.setText(getContext().getResources().getString(R.string.tipsimportPhoto1));
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			ll.topMargin = ShareData.PxToDpi_xhdpi(50);
			text.setLayoutParams(ll);
			cenFr.addView(text);
		}
	}
	
	public void setBg(Bitmap bmp)
	{
		if(bmp != null)
		{
			m_fr.setBackgroundDrawable(new BitmapDrawable(bmp));
		}
	}
	
	protected View.OnClickListener m_btnListener = new View.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			if(v == m_addBtn)
			{
				if(m_cb != null)
				{
					m_cb.onAddBtnClick();
				}
				dismiss();
			}
			else if(v == m_closeBtn)
			{
				dismiss();
			}
			else if(v == m_fr)
			{
				dismiss();
			}
		}
		
	};
	
	public static interface Callback
	{
		public void onAddBtnClick();
	}

}
