package cn.poco.webview;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.security.MessageDigest;
import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.interphoto2.R;
import cn.poco.system.SysConfig;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.MyWebView;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.MyNetCore;
import cn.poco.utils.PermissionUtils;
import cn.poco.utils.Utils;
import cn.poco.webview.site.WebViewPageSite;

public class WebViewPage extends MyWebView
{
	public static final int REQUEST_CODE_SELECT_PIC = 0x9001;
	public static final int REQUEST_CODE_SELECT_CAMERA = 0x9002;

	protected WebViewPageSite m_site;

	protected String m_url;

	protected ValueCallback<Uri> m_filePathCallback1;
	protected ValueCallback<Uri[]> m_filePathCallback2;
	protected String m_photoPath;

	protected ImageView m_backBtn;
	protected ImageView m_closeBtn;
	protected TextView m_title;
	protected ProgressBar m_progressBar;
	protected boolean m_showHomeBtn = true;

	public WebViewPage(Context context, BaseSite site)
	{
		super(context, site);

		m_site = (WebViewPageSite)site;
	}

	@Override
	protected void Init()
	{
		ShareData.InitData((Activity)getContext());
		View.OnClickListener btnLst = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(v == m_backBtn)
				{
					onBack();
				}
				else if(v == m_closeBtn)
				{
					m_site.OnClose(getContext());
				}
			}
		};

		FrameLayout.LayoutParams fl;
		this.setBackgroundColor(0xFF000000);

		int topBarH = ShareData.PxToDpi_xhdpi(100);
		FrameLayout topBar = new FrameLayout(getContext());
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, topBarH);
		fl.gravity = Gravity.LEFT | Gravity.TOP;
		this.addView(topBar, fl);
		{
			m_backBtn = new ImageView(getContext());
			m_backBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.framework_back_btn, R.drawable.framework_back_btn));
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
			topBar.addView(m_backBtn, fl);
			m_backBtn.setOnClickListener(btnLst);

			m_closeBtn = new ImageView(getContext());
			m_closeBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.webview_home_btn, R.drawable.webview_home_btn));
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
			topBar.addView(m_closeBtn, fl);
			m_closeBtn.setOnClickListener(btnLst);

			m_title = new TextView(getContext());
			m_title.setTextColor(0xFFFFFFFF);
			m_title.setSingleLine();
			m_title.setEllipsize(TextUtils.TruncateAt.END);
			m_title.setGravity(Gravity.CENTER);
			m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(300), FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER;
			topBar.addView(m_title, fl);
		}

		m_webView = new WebView(getContext());
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.m_screenHeight - topBarH);
		fl.gravity = Gravity.LEFT | Gravity.BOTTOM;
		this.addView(m_webView, fl);

		int pbarH = 5;
		m_progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
		m_progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.web_load_progress));
		m_progressBar.setMax(100);
		m_progressBar.setMinimumHeight(pbarH);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, pbarH);
		fl.gravity = Gravity.LEFT | Gravity.TOP;
		fl.topMargin = topBarH;
		this.addView(m_progressBar, fl);
		m_progressBar.setVisibility(View.GONE);

		InitWebViewSetting(m_webView.getSettings());
		m_webView.getSettings().setUserAgentString(m_webView.getSettings().getUserAgentString() + " interphoto/" + SysConfig.GetAppVer(getContext()));

		m_webView.setWebViewClient(new MyWebViewClient());
		m_webView.setWebChromeClient(new MyWebChromeClient()
		{
			//5.0+
			@Override
			public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams)
			{
				ShowFileChooser(null, filePathCallback);
				return true;
			}

			//4.1.1
			public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture)
			{
				ShowFileChooser(filePathCallback, null);
			}

			//3.0+
			public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType)
			{
				ShowFileChooser(filePathCallback, null);
			}

			//3.0-
			public void openFileChooser(ValueCallback<Uri> filePathCallback)
			{
				ShowFileChooser(filePathCallback, null);
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress)
			{
				if(newProgress < 100)
				{
					m_progressBar.setVisibility(View.VISIBLE);
					m_progressBar.setProgress(newProgress);
				}
				else
				{
					m_progressBar.setVisibility(View.GONE);
				}

				super.onProgressChanged(view, newProgress);
			}

			@Override
			public void onReceivedTitle(WebView view, String title)
			{
				if(title != null)
				{
					m_title.setText(title);
				}

				super.onReceivedTitle(view, title);
			}
		});
	}

	protected void ShowFileChooser(final ValueCallback<Uri> cb1, final ValueCallback<Uri[]> cb2)
	{
		m_filePathCallback1 = cb1;
		m_filePathCallback2 = cb2;
		CharSequence[] items = {getResources().getString(R.string.album), getResources().getString(R.string.xiangji)};
		AlertDialog dlg = new AlertDialog.Builder(getContext()).setTitle(getResources().getString(R.string.selectSources)).setItems(items, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				switch(which)
				{
					case 0:
					{
						m_site.OnSelPhoto(getContext());
						break;
					}

					case 1:
					{
						boolean flag = true;
						if(Build.VERSION.SDK_INT >= 23)
						{
							flag = PermissionUtils.checkCameraPermission(getContext());
						}
						if(flag)
						{
							m_site.OnCamera(getContext());
						}
						else
						{
							PermissionUtils.requestPermissions(getContext(), Manifest.permission.CAMERA);
						}
						break;
					}

					default:
						break;
				}
			}
		}).create();
		dlg.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				if(m_filePathCallback1 != null)
				{
					m_filePathCallback1.onReceiveValue(null);
					m_filePathCallback1 = null;
				}
				if(m_filePathCallback2 != null)
				{
					m_filePathCallback2.onReceiveValue(null);
					m_filePathCallback2 = null;
				}
				m_photoPath = null;
			}
		});
		dlg.show();
	}

	@Override
	public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		String str;
		for(int i = 0; i < permissions.length; i ++)
		{
			str = permissions[i];
			if(Manifest.permission.CAMERA.equals(str))
			{
				if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
				{
					m_site.OnCamera(getContext());
				}
				else
				{
					if(!ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(), str))
					{
						Toast.makeText(getContext(), "请到应用程序管理里面打开相机权限", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			}
		}
		return super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void loadUrl(String url)
	{
		m_url = MyNetCore.GetPocoUrl(getContext(), url);
		m_url = AddMyParams(getContext(), m_url);

		super.loadUrl(m_url);
	}

	public static String AddMyParams(Context context, String url)
	{
		String out = "";

		if(url != null)
		{
			String imei = CommonUtils.GetIMEI(context);
			if(imei != null && imei.length() > 0)
			{
				out += url;
				if(out.contains("?"))
				{
					out += "&";
				}
				else
				{
					out += "?";
				}
				out += "en_str=" + new String(MyEncode(imei, "beautycamera"));
				out += "&ime_str=" + imei;
			}
		}

		return out;
	}

	public static byte[] MyEncode(String key, String data)
	{
		byte[] out = null;

		byte[] keyArr = MD5(key).getBytes();
		byte[] dataArr = data.getBytes();

		int len = dataArr.length;
		int l = keyArr.length;
		int x = 0;
		for(int i = 0; i < len; i++)
		{
			if(x == l)
			{
				x = 0;
			}
			dataArr[i] += keyArr[x];
			x++;
		}

		out = Base64.encode(dataArr, Base64.DEFAULT | Base64.NO_WRAP);

		return out;
	}

	public static byte[] MyDecode(String key, String data)
	{
		byte[] out = null;

		byte[] keyArr = MD5(key).getBytes();
		byte[] dataArr = Base64.decode(data.getBytes(), Base64.DEFAULT | Base64.NO_WRAP);

		int len = dataArr.length;
		int l = keyArr.length;
		int x = 0;
		for(int i = 0; i < len; i++)
		{
			if(x == l)
			{
				x = 0;
			}
			dataArr[i] -= keyArr[x];
			x++;
		}

		out = dataArr;

		return out;
	}

	public static String MD5(String data)
	{
		String out = null;

		try
		{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(data.getBytes("UTF-8"));
			byte[] encryption = md5.digest();

			StringBuffer buf = new StringBuffer();
			String temp;
			for(int i = 0; i < encryption.length; i++)
			{
				temp = Integer.toHexString(0xff & encryption[i]);
				if(temp.length() == 1)
				{
					buf.append("0").append(temp);
				}
				else
				{
					buf.append(temp);
				}
			}

			out = buf.toString();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

		return out;
	}

	public void reloadUrl()
	{
		if(m_url != null)
		{
			loadUrl(m_url);
		}
	}

	@Override
	public void onBack()
	{
		if(m_webView != null && m_webView.getVisibility() == View.GONE)
		{
			if(m_webChromeClient != null)
			{
				m_webChromeClient.onHideCustomView();
				return;
			}
		}

		if(m_webView != null)
		{
			if(m_webView.canGoBack())
			{
				m_webView.goBack();
				return;
			}
		}

		m_site.OnBack(getContext());
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode == Activity.RESULT_OK)
		{
			try
			{
				switch(requestCode)
				{
					case REQUEST_CODE_SELECT_PIC:
					{
						Uri uri = data.getData();
						if(uri != null)
						{
							if(m_filePathCallback2 != null)
							{
								m_filePathCallback2.onReceiveValue(new Uri[]{uri});
								m_filePathCallback2 = null;
							}
							else if(m_filePathCallback1 != null)
							{
								m_filePathCallback1.onReceiveValue(uri);
								m_filePathCallback1 = null;
							}
						}

						if(m_filePathCallback1 != null)
						{
							m_filePathCallback1.onReceiveValue(null);
							m_filePathCallback1 = null;
						}
						if(m_filePathCallback2 != null)
						{
							m_filePathCallback2.onReceiveValue(null);
							m_filePathCallback2 = null;
						}
						return true;
					}

					case REQUEST_CODE_SELECT_CAMERA:
					{
						String path = null;
						if(m_photoPath != null)
						{
							path = m_photoPath;
						}
						else
						{
							Bundle bundle = data.getExtras();
							if(bundle != null)
							{
								Bitmap bitmap = (Bitmap)bundle.get("data");
								if(bitmap != null)
								{
									path = Utils.SaveImg(getContext(), bitmap, null, 90, false);
								}
							}
						}
						if(path != null)
						{
							Uri uri = Uri.fromFile(new File(path));
							if(uri != null)
							{
								if(m_filePathCallback2 != null)
								{
									m_filePathCallback2.onReceiveValue(new Uri[]{uri});
									m_filePathCallback2 = null;
								}
								else if(m_filePathCallback1 != null)
								{
									m_filePathCallback1.onReceiveValue(uri);
									m_filePathCallback1 = null;
								}
							}
						}

						if(m_filePathCallback1 != null)
						{
							m_filePathCallback1.onReceiveValue(null);
							m_filePathCallback1 = null;
						}
						if(m_filePathCallback2 != null)
						{
							m_filePathCallback2.onReceiveValue(null);
							m_filePathCallback2 = null;
						}
						m_photoPath = null;
						return true;
					}

					default:
						break;
				}
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			switch(requestCode)
			{
				case REQUEST_CODE_SELECT_PIC:
				case REQUEST_CODE_SELECT_CAMERA:
				{
					if(m_filePathCallback1 != null)
					{
						m_filePathCallback1.onReceiveValue(null);
						m_filePathCallback1 = null;
					}
					if(m_filePathCallback2 != null)
					{
						m_filePathCallback2.onReceiveValue(null);
						m_filePathCallback2 = null;
					}
					m_photoPath = null;
					return true;
				}
				default:
					break;
			}
		}

		return super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * url:String,打开的URL
	 * show_home: String 是否显示回到首页按钮
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		if(params != null)
		{
			Object obj = params.get("url");
			if(obj instanceof String)
			{
				loadUrl((String)obj);
			}
			obj = params.get("show_home");
			if(obj instanceof Boolean)
			{
				m_showHomeBtn = (Boolean)obj;
			}
			if(m_showHomeBtn)
			{
				m_closeBtn.setVisibility(VISIBLE);
			}
			else
			{
				m_closeBtn.setVisibility(GONE);
			}
		}
	}
}
