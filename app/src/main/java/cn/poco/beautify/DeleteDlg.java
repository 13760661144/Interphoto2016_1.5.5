package cn.poco.beautify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.poco.interphoto2.R;
import cn.poco.resource.MyLogoRes;
import cn.poco.tianutils.FullScreenDlg;
import cn.poco.tianutils.ShareData;

public class DeleteDlg extends FullScreenDlg
{
	public static int text_size = 14;
	protected final int lint_color = 0xff595959;
	public TextView text;
	public TextView m_title;
	protected OnDlgClickCallback m_cb;
	protected Object m_res;
	protected LinearLayout viewFr;
	protected LinearLayout btnFr;
	public TextView cancelBtn, continueBtn;

	public DeleteDlg(Activity activity)
	{
		super(activity);
		InitUI(activity);
	}

	public DeleteDlg(Activity activity, int theme)
	{
		super(activity, theme);
		InitUI(activity);
	}

	public DeleteDlg(Activity activity, boolean cancelable, OnCancelListener cancelListener)
	{
		super(activity, cancelable, cancelListener);
		InitUI(activity);
	}

	@SuppressLint("StringFormatInvalid")
	public void setData(Object res)
	{
		m_res = res;
		if(res != null)
		{
			if(res instanceof MyLogoRes)
			{
				m_title.setText(getContext().getResources().getString(R.string.deletewatermark));
				MyLogoRes lockRes = (MyLogoRes)res;
				String str = getContext().getResources().getString(R.string.isDeletemarkTips);
				str = String.format(str,lockRes.m_name);
				int start = str.indexOf(lockRes.m_name);
				int end = start + lockRes.m_name.length();
				SpannableStringBuilder builder = new SpannableStringBuilder(str);
				builder.setSpan(new ForegroundColorSpan(0xffffc433), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				text.setText(builder);
			} else if (res instanceof List) {
				int count = ((List)res).size();
				m_title.setText(getContext().getResources().getString(R.string.deletePhoto));
				text.setText(getContext().getResources().getQuantityString(R.plurals.ensure_delete, count, count));
			}
			else
			{
				m_title.setText(getContext().getResources().getString(R.string.deletePhoto));
				text.setText(getContext().getResources().getString(R.string.delete));
			}
		}
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
		m_fr.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(m_cb != null)
				{
					m_cb.onPageClick();
				}
			}
		});

		int viewW = ShareData.PxToDpi_xhdpi(620);
		viewFr = new LinearLayout(context);
		viewFr.setBackgroundColor(0xff404040);
		viewFr.setOrientation(LinearLayout.VERTICAL);
		fl = new FrameLayout.LayoutParams(viewW, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		AddView(viewFr, fl);
		viewFr.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

			}
		});
		initTipUI(context, false);

		setCancelable(false);
	}

	protected void initTipUI(Context context, boolean isFailed)
	{
		viewFr.removeAllViews();
		LinearLayout.LayoutParams ll;

		m_title = new TextView(context);
		m_title.setText(getContext().getResources().getString(R.string.deletewatermark));
		m_title.setGravity(Gravity.CENTER);
		m_title.setLineSpacing(1.0f, 1.5f);
		m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		m_title.setTextColor(Color.WHITE);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		ll.topMargin = ShareData.PxToDpi_xhdpi(50);
		m_title.setLayoutParams(ll);
		viewFr.addView(m_title);

		text = new TextView(context);
		text.setGravity(Gravity.CENTER);
		text.setLineSpacing(1.0f, 1.5f);
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
		text.setTextColor(Color.WHITE);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		ll.topMargin = ShareData.PxToDpi_xhdpi(50);
		text.setLayoutParams(ll);
		viewFr.addView(text);

		ImageView line = new ImageView(context);
		line.setBackgroundColor(lint_color);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		ll.topMargin = ShareData.PxToDpi_xhdpi(60);
		line.setLayoutParams(ll);
		viewFr.addView(line);

		btnFr = new LinearLayout(context);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		btnFr.setLayoutParams(ll);
		viewFr.addView(btnFr);
		{
			cancelBtn = new TextView(context);
			cancelBtn.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20));
			cancelBtn.setBackgroundColor(0xff272727);
			cancelBtn.setGravity(Gravity.CENTER);
			cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
			cancelBtn.setTextColor(0xffa6a6a6);
			cancelBtn.setText(context.getResources().getString(R.string.Cancel));
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.weight = 1;
			cancelBtn.setLayoutParams(ll);
			btnFr.addView(cancelBtn);
			cancelBtn.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if(m_cb != null)
					{
						m_cb.onCancel();
						m_cb.onLeftBtnClick(m_res);
					}

				}
			});

			line = new ImageView(context);
			line.setBackgroundColor(lint_color);
			ll = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			line.setLayoutParams(ll);
			btnFr.addView(line);

			continueBtn = new TextView(context);
			continueBtn.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20));
			continueBtn.setBackgroundColor(0xff272727);
			continueBtn.setGravity(Gravity.CENTER);
			continueBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
			continueBtn.setTextColor(0xffffce54);
			continueBtn.setText(context.getResources().getString(R.string.ok));
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.weight = 1;
			continueBtn.setLayoutParams(ll);
			btnFr.addView(continueBtn);
			continueBtn.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if(m_cb != null)
					{
						m_cb.onDelete(m_res);
					}
				}
			});
		}
	}

	public static abstract class OnDlgClickCallback
	{
		public abstract void onDelete(Object res);
		public abstract void onCancel();
		public void onLeftBtnClick(Object... res){};
		public abstract void onPageClick();
	}

}
