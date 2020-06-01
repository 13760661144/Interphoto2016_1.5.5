package cn.poco.beautify;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.Text.Painter;
import cn.poco.interphoto2.R;
import cn.poco.resource.MyLogoRes;
import cn.poco.statistics.TongJi2;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

public class TextAddDlg extends FrameLayout
{
	protected LinearLayout m_chooseFr;
	protected ImageView m_helpBtn;
	protected TextView m_helpText;
	
	protected LinearLayout m_inputFr;
	protected ImageView m_backBtn;
	protected EditText m_text;
	protected TextView m_completeBtn;

	protected LinearLayout m_progressFr;
	protected ImageView m_clostBtn;
	
	protected Callback m_cb;
	protected Activity m_ac;
	protected MyLogoRes m_res;
	private Bitmap m_bkbmp;
	protected WaitDialog1 m_waitDlg;

	public TextAddDlg(Activity activity, Callback cb)
	{
		super(activity);
		m_cb = cb;
		m_ac = activity;
	}
	
	public void setData(MyLogoRes res)
	{
		m_res = res;
		InitAddLocalUI();

		m_text.setFocusable(true);
		m_text.requestFocus();
		showSoftInput(m_text);
	}
	
	protected void InitAddLocalUI()
	{
		this.removeAllViews();
		this.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

			}
		});
		FrameLayout.LayoutParams fl;
		LinearLayout.LayoutParams ll;
		
		m_inputFr = new LinearLayout(getContext());
		m_inputFr.setOrientation(LinearLayout.VERTICAL);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		fl.topMargin = ShareData.PxToDpi_xhdpi(290);
		m_inputFr.setLayoutParams(fl);
		this.addView(m_inputFr);
		{			
			TextView tip = new TextView(getContext());
			tip.setGravity(Gravity.CENTER);
			tip.setText(getResources().getString(R.string.createwatermarkName));
			tip.setTextColor(0xffa6a6a6);
			tip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			tip.setLayoutParams(ll);
			m_inputFr.addView(tip);

			LayoutInflater inflater = m_ac.getLayoutInflater();
			inflater.inflate(R.layout.edittext1, m_inputFr);
			m_text = (EditText)m_inputFr.findViewById(R.id.edittext1);
			m_text.setCursorVisible(true);
			m_text.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
			m_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
			m_text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
			m_text.setGravity(Gravity.CENTER);
			ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			ll.topMargin = ShareData.PxToDpi_xhdpi(60);
			m_text.setLayoutParams(ll);
			if(m_res != null && m_res.m_name != null)
			{
				m_text.setText(m_res.m_name);
			}
			String str = m_text.getText().toString();
			m_text.setSelection(str.length());
			
			m_completeBtn = new TextView(getContext());
			m_completeBtn.setOnClickListener(m_btnListener);
			m_completeBtn.setBackgroundColor(0xffffc433);
			m_completeBtn.setGravity(Gravity.CENTER);
			m_completeBtn.setText(getResources().getString(R.string.Done));
			m_completeBtn.setTextColor(0xffffffff);
			m_completeBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(400), ShareData.PxToDpi_xhdpi(78));
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			ll.topMargin = ShareData.PxToDpi_xhdpi(150);
			m_completeBtn.setLayoutParams(ll);
			m_inputFr.addView(m_completeBtn);
		}
	}
	
	public void setBk(String bmp)
	{
		this.setBackgroundDrawable(null);
		if(bmp != null)
		{
			m_bkbmp = Utils.DecodeFile(bmp, null);
			this.setBackgroundDrawable(new BitmapDrawable(m_bkbmp));
		}
	}

	public void setBk(Bitmap bmp)
	{
		this.setBackgroundDrawable(null);
		if(bmp != null)
		{
			m_bkbmp = bmp;
			this.setBackgroundDrawable(new BitmapDrawable(m_bkbmp));
		}
	}
	
	public void showSoftInput(View v)
	{
		InputMethodManager manager = (InputMethodManager)getContext().
				getSystemService(Context.INPUT_METHOD_SERVICE);
		if (manager != null && v != null) {
			manager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//			manager.toggleSoftInputFromWindow(v.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
		}
	} 
	
	public void hideSoftInput(View v)
	{
		if(v != null)
		{
			InputMethodManager manager = (InputMethodManager)getContext().
					getSystemService(Context.INPUT_METHOD_SERVICE);
			manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}
	
	protected View.OnClickListener m_btnListener = new View.OnClickListener()
	{
		
		@Override
		public void onClick(View v)
		{
			if(v == m_helpBtn)
			{
				if(m_helpText.getVisibility() == View.GONE)
					m_helpText.setVisibility(View.VISIBLE);
				else
					m_helpText.setVisibility(View.GONE);
			}
			else if(v == m_backBtn)
			{
				m_chooseFr.setVisibility(View.VISIBLE);
				m_backBtn.setVisibility(View.GONE);
				m_inputFr.setVisibility(View.GONE);
				m_progressFr.setVisibility(View.GONE);
				hideSoftInput(m_text);
				TextAddDlg.this.setOnClickListener(m_btnListener);
			}
			else if(v == m_completeBtn)
			{
				final String text = String.valueOf(m_text.getText());
				if(m_cb != null && m_res != null)
				{
					hideSoftInput(m_text);
					if(m_cb != null)
					{
						m_cb.onDismiss();
					}
					m_res.m_name = text.trim();
					if(m_cb != null)
					{
						m_cb.onInputFinished(m_res);
					}
				}
			}
			else if(v == m_clostBtn)
			{
				hideSoftInput(m_text);
				if(m_cb != null)
				{
					m_cb.onDismiss();
				}
			}
			else if(v == TextAddDlg.this)
			{
				hideSoftInput(m_text);
				if(m_cb != null)
				{
					m_cb.onDismiss();
				}
			}
		}
	};
	
	public void clear()
	{
		hideSoftInput(m_text);
		if(m_bkbmp != null)
		{
			m_bkbmp.recycle();
			m_bkbmp = null;
		}
		if (m_waitDlg != null)
		{
			m_waitDlg.dismiss();
			m_waitDlg = null;
		}
		this.removeAllViews();
	}

	private class NoLineClickSpan extends ClickableSpan {
		String text;

		public NoLineClickSpan(String text) {
			super();
			this.text = text;
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(0xffffc433);
			ds.setUnderlineText(false); //去掉下划线
		}

		@Override
		public void onClick(View widget) {
			Intent intent = new Intent(); //点击超链接时调用
			intent.setAction("android.intent.action.VIEW");
			Uri url = Uri.parse("http://" + text);
			intent.setData(url);
			m_ac.startActivity(intent);
		}
	}
	
	public static interface Callback
	{
		public void onDismiss();
		
		public void onInputFinished(MyLogoRes res);
	}

}
