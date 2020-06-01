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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;

import cn.poco.MaterialMgr2.MgrUtils;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.FontRes;
import cn.poco.resource.IDownload;
import cn.poco.resource.MyLogoRes;
import cn.poco.resource.TextRes;
import cn.poco.tianutils.FullScreenDlg;
import cn.poco.tianutils.ShareData;

public class FontDownloadDlg extends FullScreenDlg
{
	public static int text_size = 14;
	protected final int lint_color = 0xff595959;
	protected TextView text;
	protected OnDlgClickCallback m_cb;
	protected BaseRes m_res;
	protected String m_size = "0.0";
	protected LinearLayout viewFr;
	protected ProgressBar m_progressBar;
	protected TextView m_cancelBtn;
	protected LinearLayout btnFr;

	public FontDownloadDlg(Activity activity)
	{
		super(activity);
		InitUI(activity);
	}

	public FontDownloadDlg(Activity activity, int theme)
	{
		super(activity, theme);
		InitUI(activity);
	}

	public FontDownloadDlg(Activity activity, boolean cancelable, OnCancelListener cancelListener)
	{
		super(activity, cancelable, cancelListener);
		InitUI(activity);
	}

	public void setData(BaseRes res)
	{
		m_res = res;
		ArrayList<FontRes> fontArr = null;
		boolean flag = true;
		if(m_res instanceof TextRes)
		{
			flag = true;
			fontArr = ((TextRes)m_res).m_resArr;
		}
		else if(m_res instanceof MyLogoRes)
		{
			flag = false;
			fontArr = ((MyLogoRes)m_res).m_resArr;
		}
		if(fontArr != null && fontArr.size() > 0)
		{
			int size = fontArr.size();
			FontRes fontRes;
			float totalSize = 0.0f;
			for(int i = 0; i < size; i ++)
			{
				fontRes = fontArr.get(i);
				if(fontRes != null)
				{
					totalSize += fontRes.m_size;
				}
			}
			NumberFormat num = NumberFormat.getNumberInstance();
			num.setMaximumFractionDigits(2);
			m_size= num.format(totalSize / 1024 / 1024) ;
			if(text != null)
			{
				if(flag)
				{
					text.setText(getContext().getResources().getString(R.string.needDownedTips1)+ m_size + getContext().getResources().getString(R.string.needDownedTips2));
				}
				else
				{
					text.setText(R.string.needFontDownedTips3);
				}
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

		int viewW = ShareData.PxToDpi_xhdpi(620);
		viewFr = new LinearLayout(context);
		viewFr.setBackgroundColor(0xff3f3f3f);
		viewFr.setOrientation(LinearLayout.VERTICAL);
//		viewFr.setBackgroundResource(R.drawable.dlg_corner_shape);
		fl = new FrameLayout.LayoutParams(viewW, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		AddView(viewFr, fl);
		initTipUI(context, false);

		setCancelable(false);
	}

	protected void downloadRes()
	{
		IDownload[] resArr;
		ArrayList<FontRes> fontArr = null;
		if(m_res instanceof TextRes)
		{
			fontArr = ((TextRes)m_res).m_resArr;
		}
		else if(m_res instanceof MyLogoRes)
		{
			fontArr = ((MyLogoRes)m_res).m_resArr;
		}
		if(fontArr != null && fontArr.size() > 0)
		{
			resArr = new IDownload[fontArr.size()];
			resArr = fontArr.toArray(resArr);
			DownloadMgr.getInstance().DownloadRes(resArr, false, m_resDownloadCb);
		}
	}

	protected void initTipUI(Context context, boolean isFailed)
	{
		viewFr.removeAllViews();
		LinearLayout.LayoutParams ll;

		text = new TextView(context);
		text.setGravity(Gravity.LEFT);
		text.setText(getContext().getResources().getString(R.string.needDownedTips1)+ m_size + getContext().getResources().getString(R.string.needDownedTips2));
		text.setLineSpacing(1.0f, 1.5f);
		text.setPadding(ShareData.PxToDpi_xhdpi(30), ShareData.PxToDpi_xhdpi(50), ShareData.PxToDpi_xhdpi(30), ShareData.PxToDpi_xhdpi(50));
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
		text.setTextColor(Color.WHITE);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		text.setLayoutParams(ll);
		viewFr.addView(text);

		/*ImageView line = new ImageView(context);
		line.setBackgroundColor(lint_color);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		ll.topMargin = ShareData.PxToDpi_xhdpi(20);
		line.setLayoutParams(ll);
		viewFr.addView(line);*/

		btnFr = new LinearLayout(context);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		btnFr.setLayoutParams(ll);
		viewFr.addView(btnFr);
		{
			TextView cancelBtn = new TextView(context);
			cancelBtn.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20));
			cancelBtn.setBackgroundColor(0xff272727);
			cancelBtn.setGravity(Gravity.CENTER);
			cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
			cancelBtn.setTextColor(0xffa6a6a6);
			cancelBtn.setText(context.getString(R.string.Cancel));
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
					}

				}
			});

			ImageView line = new ImageView(context);
			line.setBackgroundColor(lint_color);
			ll = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			line.setLayoutParams(ll);
			btnFr.addView(line);

			TextView continueBtn = new TextView(context);
			continueBtn.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20));
			continueBtn.setBackgroundColor(0xff272727);
			continueBtn.setGravity(Gravity.CENTER);
			continueBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
			continueBtn.setTextColor(0xffffce54);
			if(isFailed)
			{
				continueBtn.setText(context.getResources().getString(R.string.continuetext));
			}else
			{
				continueBtn.setText(context.getResources().getString(R.string.download));
			}
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.weight = 1;
			continueBtn.setLayoutParams(ll);
			btnFr.addView(continueBtn);
			continueBtn.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					initProgressUI(getContext());
					downloadRes();
				}
			});
		}
	}

	protected void initProgressUI(Context context)
	{
		LinearLayout.LayoutParams ll;
		FrameLayout.LayoutParams fl;
		viewFr.removeAllViews();
		FrameLayout btnFr = new FrameLayout(context);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		ll.topMargin = ShareData.PxToDpi_xhdpi(20);
		ll.bottomMargin = ll.topMargin;
		ll.leftMargin = ShareData.PxToDpi_xhdpi(30);
		ll.rightMargin = ll.leftMargin;
		btnFr.setLayoutParams(ll);
		viewFr.addView(btnFr);
		{
			text = new TextView(context);
			text.setText(context.getResources().getString(R.string.Downloading));
			text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
			text.setTextColor(Color.WHITE);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL;
			text.setLayoutParams(fl);
			btnFr.addView(text);

			m_cancelBtn = new TextView(context);
			m_cancelBtn.setGravity(Gravity.CENTER);
			m_cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
			m_cancelBtn.setTextColor(0xffa6a6a6);
			m_cancelBtn.setText(context.getString(R.string.Cancel));
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
			m_cancelBtn.setLayoutParams(fl);
			btnFr.addView(m_cancelBtn);
			m_cancelBtn.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if(m_cb != null)
					{
						m_cb.onCancel();
					}
				}
			});
		}

		m_progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
		m_progressBar.setProgressDrawable(getContext().getResources().getDrawable(R.drawable.text_net_import_progressbar));
		m_progressBar.setProgress(0);
		m_progressBar.setMax(100);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(30));
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		m_progressBar.setLayoutParams(ll);
		viewFr.addView(m_progressBar);
	}

	protected MgrUtils.MyDownloadCB m_resDownloadCb = new MgrUtils.MyDownloadCB(new MgrUtils.MyCB()
	{
		@Override
		public void OnFail(int downloadId, IDownload res)
		{
			initTipUI(getContext(), true);
		}

		@Override
		public void OnGroupFail(int downloadId, IDownload[] resArr)
		{

		}

		@Override
		public void OnProgress(int downloadId, IDownload res, int progress)
		{

		}

		@Override
		public void OnComplete(int downloadId, IDownload res)
		{

		}

		@Override
		public void OnGroupProgress(int downloadId, IDownload[] resArr, int progress)
		{
			m_progressBar.setProgress(progress);
		}

		@Override
		public void OnGroupComplete(int downloadId, IDownload[] resArr)
		{
			if(m_cb != null)
			{
				m_cb.onComplete();
			}
		}
	});

	public void clear()
	{
		if(m_resDownloadCb != null)
		{
			m_resDownloadCb.ClearAll();
			m_resDownloadCb = null;
		}
	}

	public static interface OnDlgClickCallback
	{
		public void onComplete();
		public void onCancel();
	}

}
