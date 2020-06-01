package cn.poco.appmarket;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;

import cn.poco.appmarket.site.MarketPageSite;
import cn.poco.blogcore.Tools;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.home.OpenApp;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.system.SysConfig;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.MyWebView;
import cn.poco.tianutils.ShareData;

public class MarketPage extends IPage
{
	protected static final int ID_FIND = R.id.app_market_find;
	protected static final int ID_BOUTIQUE = R.id.app_market_boutique;
	protected static final int ID_BACK = R.id.app_market_back;

	protected final String MARKET_URL = "http://zt.adnonstop.com/index.php?r=wap/recommend/page/index";
	protected final String MARKET_BETA_URL = "http://tw.adnonstop.com/zt/web/index.php?r=wap/recommend/page/index";

	protected MarketPageSite m_Site;
	protected boolean mQuit;
	protected ImageView m_backBtn;
	protected TextView m_title;

	protected MyWebView m_webView;

	public MarketPage(Context context, BaseSite site)
	{
		super(context, site);

		m_Site = (MarketPageSite)site;
		Init();
	}

	protected void Init()
	{
		ShareData.InitData((Activity)getContext());
		LinearLayout body = new LinearLayout(getContext());
		body.setOrientation(LinearLayout.VERTICAL);
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		this.addView(body, fl);
		{
			FrameLayout topBar = new FrameLayout(getContext());
			this.setBackgroundColor(0xFF000000);
			int topBarH = ShareData.PxToDpi_xhdpi(80);
			fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, topBarH);
			fl.gravity = Gravity.LEFT | Gravity.TOP;

			body.addView(topBar, fl);
			{
				fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
				m_backBtn = new ImageView(getContext());
				m_backBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.framework_back_btn, R.drawable.framework_back_btn));
				m_backBtn.setId(ID_BACK);
				m_backBtn.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						onBack();
					}
				});
				topBar.addView(m_backBtn, fl);

				m_title = new TextView(getContext());
				m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				m_title.setText(getResources().getString(R.string.homepage_tuijian));
				m_title.setTextColor(0xFFFFFFFF);
				m_title.setGravity(Gravity.CENTER);
				fl = new LayoutParams(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(300), LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER;
				topBar.addView(m_title, fl);

			}

			LinearLayout line = new LinearLayout(getContext());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(1));
			line.setBackgroundColor(0xFF333333);
			body.addView(line, params);

			m_webView = new MyWebView(getContext(), null);
			m_webView.setBackgroundColor(0xff0e0e0e);
			m_webView.m_webView.setBackgroundColor(0xff0e0e0e);
			m_webView.m_webView.getSettings().setUserAgentString(m_webView.m_webView.getSettings().getUserAgentString() + " interphoto/" + SysConfig.GetAppVer(getContext()));
			m_webView.m_webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
			m_webView.m_webView.getSettings().setLoadWithOverviewMode(true);
			m_webView.m_webView.getSettings().setUseWideViewPort(true);
			m_webView.m_webView.getSettings().setBuiltInZoomControls(true);
			m_webView.setWebChromeClient(new MyChromeClient());
			m_webView.setWebViewClient(new MyWebViewClient());
			m_webView.loadUrl(getWebUrl());
			fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
			fl.topMargin = topBarH;
			m_webView.setLayoutParams(fl);
			body.addView(m_webView);

		}

	}

	protected String getWebUrl()
	{
		String url;
		if(SysConfig.IsDebug())
		{
			url = MARKET_BETA_URL;
		}
		else
		{
			url = MARKET_URL;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(url);
		buffer.append("&");
		buffer.append("version=");
		buffer.append(SysConfig.GetAppVer(getContext()));
		buffer.append("&");

		buffer.append("os_type=");
		buffer.append("android");
		buffer.append("&");

		buffer.append("come_from=");
		buffer.append("interphoto");
		return buffer.toString();
	}

	public class MyWebViewClient extends MyWebView.MyWebViewClient
	{
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
//			System.out.println("command1: " + url);
			String temp = url.toLowerCase(Locale.ENGLISH);
			if(!temp.startsWith("http") && !temp.startsWith("ftp"))
			{
				ParseCommand(url);
				return true;
			}
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			String temp = url.toLowerCase(Locale.ENGLISH);
			if(!temp.startsWith("http") && !temp.startsWith("ftp"))
			{
				ParseCommand(url);
			}
			else
			{
				super.onPageStarted(view, url, favicon);
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
		{
			super.onReceivedError(view, errorCode, description, failingUrl);
			view.loadUrl("about:blank");
			view.stopLoading();
		}

		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
		{
			super.onReceivedError(view, request, error);
			view.loadUrl("about:blank");
			view.stopLoading();
		}
	}

	private void ParseCommand(String url)
	{
		if(url.startsWith("openapp://"))
		{
			String parseUrl = url.replace("openapp://", "");
			String[] args = parseUrl.split("&");
			if(args != null && args.length >= 1 && !TextUtils.isEmpty(args[0]))
			{
				String[] datas =  args[0].split("=");
				if(datas != null && datas.length == 2 && !TextUtils.isEmpty(datas[1]))
				{
					if(Tools.checkApkExist(getContext(), datas[1]))
					{
						OpenApp.openApp(getContext(), datas[1]);
					}
					else
					{
						if(args.length >= 2 && !TextUtils.isEmpty(args[1]))
						{
							datas =  args[1].split("=");
							OpenApp.downloadAppByUrl(getContext(), URLDecoder.decode(datas[1]));
						}
					}
				}
			}
		}
	}

	public class MyChromeClient extends MyWebView.MyWebChromeClient
	{
		//5.0+
		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams)
		{
			//ShowFileChooser(null, filePathCallback);
//			System.out.println(fileChooserParams);
			return true;
		}
	}

	@Override
	public void SetData(HashMap<String, Object> params)
	{

	}

	@Override
	public void onBack()
	{
		if(m_webView != null)
		{
			if(m_webView.canGoBack())
			{
				m_webView.goBack();
				return;
			}
		}
		mQuit = true;
		m_Site.OnBack(getContext());
		MyBeautyStat.onClickByRes(R.string.侧边栏_打开应用推荐);
	}

	@Override
	public void onClose()
	{
		super.onClose();
		if (m_webView != null) {
			m_webView.onClose();
			m_webView = null;
		}
	}
}
