package cn.poco.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import cn.poco.banner.BannerCore3;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.MyButtons;
import cn.poco.beautify.ScrollShareFr;
import cn.poco.blogcore.FacebookBlog;
import cn.poco.camera.ImageFile2;
import cn.poco.camera.RotationImg2;
import cn.poco.framework.BaseSite;
import cn.poco.framework.FileCacheMgr;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.login.util.UserMgr;
import cn.poco.loginlibs.info.UserInfo;
import cn.poco.resource.DownloadMgr;
import cn.poco.setting.SettingInfo;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.setting.SettingPage;
import cn.poco.share.SharePage;
import cn.poco.share.ShareTools;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.FolderMgr;
import cn.poco.system.SysConfig;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.MyWebView;
import cn.poco.tianutils.NetCore2;
import cn.poco.tianutils.ShareData;
import cn.poco.transitions.TweenLite;
import cn.poco.utils.FileUtil;
import cn.poco.utils.Utils;

/**
 * Created by admin on 2016/5/19.
 */
public class WebViewPage1 extends WebViewPage
{
	protected static final int WEIXIN = 1;
	protected static final int FRIEND = 2;
	protected static final int SINA = 3;
	protected static final int QZONE = 4;
	protected static final int FACEBOOK = 5;
	protected static final int INSTANGRAM = 6;
	protected static final int TWITTER = 7;
	private static final String TAG = "印象杂志";

	protected ScrollShareFr m_mainFr;
	protected ImageView m_shareBtn;
	protected MyButtons m_friendBtn;
	protected MyButtons m_weixinBtn;
	protected MyButtons m_sinaBtn;
	protected MyButtons m_qqBtn;
	protected MyButtons m_facebookBtn;
	protected MyButtons m_instagramBtn;
	protected MyButtons m_twitterBtn;
	protected ImageView m_shareHideBtn;
	protected FrameLayout m_topFr;
	protected int m_shareFrHeight;
	protected ShareTools m_shareTools;
	protected int m_shareType = -1;
	protected Bitmap m_bk;

	protected String m_shareTitle;
	protected String m_shareUrl;
	protected String m_shareLogo;
	protected int m_shareApp = 0;
	private boolean m_isMagazine = false;
	private AlertDialog m_shareTip;

	protected boolean m_isJaneKe = false;
	protected boolean m_outShare = false;	//外部分享
	protected boolean m_uiEnabled = true;
	protected boolean m_topBarShow = false;
	protected SmoothScrollRunnable m_scrollRunnable;

	protected MyWebViewClient m_client;
	protected ProgressDialog mProgressDialog;
	protected ProgressDialog mProgressDialogQQ;

	protected boolean title_by_url = false;

	public WebViewPage1(Context context, BaseSite site)
	{
		super(context, site);
		TongJiUtils.onPageStart(getContext(), TAG);
	}

	private View.OnClickListener m_btnLst;

	@Override
	protected void Init()
	{
		m_btnLst = new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(v == m_backBtn)
				{
					onBack();
					MyBeautyStat.onClickByRes(R.string.印象杂志页_退出印象杂志);
				}
				else if(v == m_closeBtn)
				{
					MyBeautyStat.onClickByRes(R.string.印象杂志页_返回首页);
					m_site.OnClose(getContext());
				}
				else if(v == m_shareBtn)
				{
					m_mainFr.ShowTopBar(true);
					pauseMusic();
				}
				else if(v == m_friendBtn && m_uiEnabled)
				{
					m_shareType = FRIEND;
					share();
				}
				else if(v == m_weixinBtn && m_uiEnabled)
				{
					m_shareType = WEIXIN;
					share();
				}
				else if(v == m_sinaBtn && m_uiEnabled)
				{
					m_shareType = SINA;
					share();
				}
				else if(v == m_qqBtn && m_uiEnabled)
				{
					m_shareType = QZONE;
					share();
				}
				else if(v == m_facebookBtn && m_uiEnabled)
				{
					m_shareType = FACEBOOK;
					share();
				}
				else if(v == m_instagramBtn && m_uiEnabled)
				{
					m_shareType = INSTANGRAM;
					share();
				}
				else if(v == m_twitterBtn && m_uiEnabled)
				{
					m_shareType = TWITTER;
					share();
				}
				else if(v == m_shareHideBtn)
				{
					m_mainFr.ShowTopBar(false);
					playMusic();
				}
				else if(v == m_topFr)
				{
					StartMove(m_webView.getScrollY(), 0, 300);
				}
			}
		};
		ShareData.InitData(getContext());
		m_shareFrHeight = ShareData.PxToDpi_xhdpi(200) + ShareData.PxToDpi_xhdpi(42);

		FrameLayout.LayoutParams fl;
		this.setBackgroundColor(0xFF000000);

		m_mainFr = new ScrollShareFr(this, m_shareFrHeight);
		m_mainFr.setOnCloseListener(new ScrollShareFr.OnCloseListener()
		{
			@Override
			public void onClose()
			{
				m_btnLst.onClick(m_shareHideBtn);
			}
		});
		InitShareFr(m_btnLst);

		m_webView = new WebView(getContext());
		m_webView.setOnTouchListener(new OnTouchListener()
		{
			protected float m_downY;
			protected boolean showTopFr;
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if(m_isJaneKe)
				{
					switch(event.getAction())
					{
						case MotionEvent.ACTION_DOWN:
						{
							m_downY = event.getY();
							showTopFr = true;
							break;
						}
						case MotionEvent.ACTION_MOVE:
						{
							float curY = event.getY();
							if(Math.abs(curY - m_downY) >= 100)
							{
								showTopFr = false;
								m_topFr.setVisibility(View.GONE);
								m_topBarShow = false;
							}
							break;
						}
						case MotionEvent.ACTION_UP:
						{
							boolean click2 = true;
							if(m_shareApp == 1 && event.getY() <= ShareData.PxToDpi_xhdpi(100) && event.getX() >= ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(100))
							{
								click2 = false;
							}
							if(click2)
							{
								if(m_topBarShow == false && m_topFr != null && showTopFr)
								{
									m_topFr.setVisibility(View.VISIBLE);
									m_topBarShow = true;
								}
								else
								{
									m_topFr.setVisibility(View.GONE);
									m_topBarShow = false;
								}
							}
							break;
						}
					}
				}
				return false;
			}
		});
//		m_webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.m_screenHeight);
		fl.gravity = Gravity.LEFT | Gravity.BOTTOM;
		m_webView.setLayoutParams(fl);
		m_mainFr.AddMainChild(m_webView, 0);

		int pbarH = 5;
		m_progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
		m_progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.web_load_progress));
		m_progressBar.setMax(100);
		m_progressBar.setMinimumHeight(pbarH);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, pbarH);
		fl.gravity = Gravity.LEFT | Gravity.TOP;
		m_progressBar.setLayoutParams(fl);
		m_mainFr.AddMainChild(m_progressBar);
		m_progressBar.setVisibility(View.GONE);

		InitWebViewSetting(m_webView.getSettings());
		m_webView.getSettings().setUserAgentString(m_webView.getSettings().getUserAgentString() + " interphoto/" + SysConfig.GetAppVer(getContext()));
		m_webView.requestFocus();
		m_client = new MyWebViewClient();
		m_webView.setWebViewClient(m_client);
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
				super.onReceivedTitle(view, title);
				if(m_outShare)
				{
					m_shareTitle = title;
					if(TextUtils.isEmpty(m_shareTitle))
					{
						m_title.setText(getResources().getString(R.string.fb_app_name));
					}
					else
					{
						m_title.setText(m_shareTitle);
					}
				}
				if(title_by_url)
				{
					m_title.setText(title);
				}
			}
		});

		int topBarH = ShareData.PxToDpi_xhdpi(80);

		m_topFr = new FrameLayout(getContext());
		m_topFr.setOnClickListener(m_btnLst);
		m_topFr.setBackgroundColor(0xFF000000);
		m_topFr.setVisibility(GONE);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, topBarH);
		fl.gravity = Gravity.LEFT | Gravity.TOP;
		m_topFr.setLayoutParams(fl);
		m_mainFr.AddMainChild(m_topFr);
		{
			m_backBtn = new ImageView(getContext());
			m_backBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.framework_back_btn, R.drawable.framework_back_btn));
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
			m_topFr.addView(m_backBtn, fl);
			m_backBtn.setOnClickListener(m_btnLst);

			m_closeBtn = new ImageView(getContext());
			m_closeBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.webview_home_btn, R.drawable.webview_home_btn));
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
			m_topFr.addView(m_closeBtn, fl);
			m_closeBtn.setOnClickListener(m_btnLst);

			m_shareBtn = new ImageView(getContext());
			m_shareBtn.setVisibility(View.GONE);
			m_shareBtn.setImageResource(R.drawable.framework_share_btn);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
			m_topFr.addView(m_shareBtn, fl);
			m_shareBtn.setOnClickListener(m_btnLst);

			m_title = new TextView(getContext());
			m_title.setText(getResources().getString(R.string.fb_app_name));
			m_title.setTextColor(0xFFFFFFFF);
			m_title.setSingleLine();
			m_title.setEllipsize(TextUtils.TruncateAt.END);
			m_title.setGravity(Gravity.CENTER);
			m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(300), FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER;
			m_topFr.addView(m_title, fl);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(getResources().getString(R.string.tip));
		builder.setMessage(getResources().getString(R.string.cancelshare));
		builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				if(m_shareTip != null)
				{
					m_shareTip.dismiss();
				}
			}
		});
		m_shareTip = builder.create();
	}

	protected void share()
	{
		MyBeautyStat.onClickByRes(R.string.印象杂志页_分享);
		switch(m_shareType)
		{
			case WEIXIN:
			{
				if(isFileExist(m_shareLogo))
				{
					TongJi2.AddCountByRes(getContext(), R.integer.分享_微信好友);
					MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微信好友, R.string.印象杂志页);
					m_shareTools.sendUrlToWeiXin(Utils.MakeLogo(getContext(), m_shareLogo), m_shareUrl, m_shareTitle, "", true, new ShareTools.SendCompletedListener()
					{
						@Override
						public void result(int result)
						{
							if(result == ShareTools.SUCCESS)
							{
								ShareTools.ToastSuccess(getContext());
							}
							if(result == ShareTools.CANCEL)
							{
								if(m_shareTip != null)
								{
									m_shareTip.show();
								}
							}
						}
					});
				}
				else
				{
					downloadLogo(m_shareLogo);
				}
				break;
			}
			case FRIEND:
			{
				if(isFileExist(m_shareLogo))
				{
					TongJi2.AddCountByRes(getContext(), R.integer.分享_微信朋友圈);
					MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.朋友圈, R.string.印象杂志页);
					m_shareTools.sendUrlToWeiXin(Utils.MakeLogo(getContext(), m_shareLogo), m_shareUrl, m_shareTitle, "", false, new ShareTools.SendCompletedListener()
					{
						@Override
						public void result(int result)
						{
							if(result == ShareTools.SUCCESS)
							{
								ShareTools.ToastSuccess(getContext());
							}
							if(result == ShareTools.CANCEL)
							{
								if(m_shareTip != null)
								{
									m_shareTip.show();
								}
							}
						}
					});
				}
				else
				{
					downloadLogo(m_shareLogo);
				}
				break;
			}
			case SINA:
			{
				if(isFileExist(m_shareLogo))
				{
					if(SettingPage.checkSinaBindingStatus(getContext()))
					{
						TongJi2.AddCountByRes(getContext(), R.integer.分享_新浪微博);
						MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博, R.string.印象杂志页);
						m_shareTools.sendToSina(Utils.MakeLogo(getContext(), m_shareLogo), m_shareTitle + m_shareUrl, new ShareTools.SendCompletedListener()
						{
							@Override
							public void result(int result)
							{
								if(result == ShareTools.SUCCESS)
								{
									ShareTools.ToastSuccess(getContext());
								}
								if(result == ShareTools.CANCEL)
								{
									if(m_shareTip != null)
									{
										m_shareTip.show();
									}
								}
							}
						});
					}
					else
					{
						m_shareTools.bindSina(new SharePage.BindCompleteListener()
						{
							@Override
							public void success()
							{
								TongJi2.AddCountByRes(getContext(), R.integer.分享_新浪微博);
								MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博, R.string.印象杂志页);
								m_shareTools.sendToSina(Utils.MakeLogo(getContext(), m_shareLogo), m_shareTitle + m_shareUrl, new ShareTools.SendCompletedListener()
								{
									@Override
									public void result(int result)
									{
										if(result == ShareTools.SUCCESS)
										{
											ShareTools.ToastSuccess(getContext());
										}
										if(result == ShareTools.CANCEL)
										{
											if(m_shareTip != null)
											{
												m_shareTip.show();
											}
										}
									}
								});
							}

							@Override
							public void fail()
							{
							}
						});
					}
				}
				else
				{
					downloadLogo(m_shareLogo);
				}
				break;
			}
			case QZONE:
			{
				if(isFileExist(m_shareLogo))
				{
					if(SettingPage.checkQzoneBindingStatus(getContext()))
					{
						TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ空间);
						MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间, R.string.印象杂志页);
						m_shareTools.sendUrlToQzone("", Utils.MakeLogo(getContext(), m_shareLogo), m_shareTitle, m_shareUrl, new ShareTools.SendCompletedListener()
						{
							@Override
							public void result(int result)
							{
								if(result == ShareTools.CANCEL)
								{
									if(m_shareTip != null)
									{
										m_shareTip.show();
									}
								}
							}
						});
					}
					else
					{
						mProgressDialogQQ = ProgressDialog.show(getContext(), "", getContext().getResources().getString(R.string.Linking));
						mProgressDialogQQ.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						m_shareTools.bindQzone(false,new SharePage.BindCompleteListener()
						{
							@Override
							public void success()
							{
								if(mProgressDialogQQ != null)
								{
									mProgressDialogQQ.dismiss();
									mProgressDialogQQ = null;
								}
								TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ空间);
								MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间, R.string.印象杂志页);
								m_shareTools.sendUrlToQzone("", Utils.MakeLogo(getContext(), m_shareLogo), m_shareTitle, m_shareUrl, new ShareTools.SendCompletedListener()
								{
									@Override
									public void result(int result)
									{
										if(result == ShareTools.CANCEL)
										{
											if(m_shareTip != null)
											{
												m_shareTip.show();
											}
										}
									}
								});
							}

							@Override
							public void fail()
							{
								if(mProgressDialogQQ != null)
								{
									mProgressDialogQQ.dismiss();
									mProgressDialogQQ = null;
								}
							}
						});
					}
				}
				else
				{
					downloadLogo(m_shareLogo);
				}
				break;
			}
			case FACEBOOK:
			{
				if(isFileExist(m_shareLogo))
				{
					TongJi2.AddCountByRes(getContext(), R.integer.分享_Facebook);
					MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.facebook, R.string.印象杂志页);
					m_shareTools.sendUrlToFacebook(m_shareTitle, "", m_shareUrl, new FacebookBlog.FaceBookSendCompleteCallback()
					{
						@Override
						public void sendComplete(int result, String error_info)
						{
							if(result == ShareTools.CANCEL)
							{
								if(m_shareTip != null)
								{
									m_shareTip.show();
								}
							}
						}
					});
				}
				else
				{
					downloadLogo(m_shareLogo);
				}
				break;
			}
			case TWITTER:
			{
				if(isFileExist(m_shareLogo))
				{
					TongJi2.AddCountByRes(getContext(), R.integer.分享_Twitter);
					MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.twitter, R.string.印象杂志页);
					m_shareTools.sendToTwitter(Utils.MakeLogo(getContext(), m_shareLogo), m_shareTitle + m_shareUrl);
				}
				else
				{
					downloadLogo(m_shareLogo);
				}
				break;
			}
		}
	}

	protected boolean isFileExist(String url)
	{
		if(FileUtil.isFileExists(url))
			return true;
		else if(FileUtil.getAssetsBitmap(getContext(), url) != null)
		{
			return true;
		}
		return false;
	}

	protected boolean downloadLogo(final String url)
	{
		final String path = FolderMgr.getInstance().IMAGE_CACHE_PATH + "/" + DownloadMgr.GetImgFileName(url);
		File file = new File(path);
		if(file.exists())
		{
			m_shareLogo = path;
			share();
			return true;
		}
		else
		{
			m_uiEnabled = false;
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						NetCore2 net = new NetCore2();
						NetCore2.NetMsg msg = net.HttpGet(url, null);
						if(msg != null && msg.m_stateCode == HttpURLConnection.HTTP_OK && msg.m_data != null)
						{
							WriteSDCardRes(path, msg.m_data);
//							System.out.println("path: " + path);
							m_shareLogo = path;
							m_uiEnabled = true;
							share();
						}
						else
						{
							m_uiEnabled = true;
							((Activity)WebViewPage1.this.getContext()).runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									Toast.makeText(WebViewPage1.this.getContext(), getResources().getString(R.string.shareFailed), Toast.LENGTH_SHORT).show();
								}
							});
						}
					}catch(Exception e){}
				}
			}).start();
		}

		return false;
	}

	public void WriteSDCardRes(String path, byte[] data)
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(path);
			fos.write(data);
			fos.flush();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(fos != null)
			{
				try
				{
					fos.close();
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	protected void InitShareFr(View.OnClickListener m_btnLst)
	{
		m_shareTools = new ShareTools(getContext());
		LinearLayout.LayoutParams ll;
		LinearLayout shareFr = new LinearLayout(getContext());
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ll.topMargin = ShareData.PxToDpi_xhdpi(35);
		ll.bottomMargin = ShareData.PxToDpi_xhdpi(35);
		shareFr.setLayoutParams(ll);
		m_mainFr.AddTopChild(shareFr);
		{
			int width = ShareData.m_screenWidth / 5;

			m_friendBtn = new MyButtons(getContext(), R.drawable.beauty_share_friend, R.drawable.beauty_share_friend);
			m_friendBtn.SetText(getResources().getString(R.string.Moments));
			AddItem(shareFr, m_friendBtn, width, m_btnLst);

			m_weixinBtn = new MyButtons(getContext(), R.drawable.beauty_share_weixin, R.drawable.beauty_share_weixin);
			m_weixinBtn.SetText(getResources().getString(R.string.Wechat));
			AddItem(shareFr, m_weixinBtn, width, m_btnLst);

			m_sinaBtn = new MyButtons(getContext(), R.drawable.beauty_share_sina, R.drawable.beauty_share_sina);
			m_sinaBtn.SetText(getResources().getString(R.string.Sina));
			AddItem(shareFr, m_sinaBtn, width, m_btnLst);

			m_qqBtn = new MyButtons(getContext(), R.drawable.beauty_share_qzone, R.drawable.beauty_share_qzone);
			m_qqBtn.SetText(getResources().getString(R.string.QQZone));
			AddItem(shareFr, m_qqBtn, width, m_btnLst);

			m_facebookBtn = new MyButtons(getContext(), R.drawable.beauty_share_facebook, R.drawable.beauty_share_facebook);
			m_facebookBtn.SetText("Facebook");
			AddItem(shareFr, m_facebookBtn, width, m_btnLst);
			String ver = SysConfig.GetAppVer(getContext());
			if(ver != null && ver.endsWith("_r3"))
			{
				m_facebookBtn.setVisibility(GONE);
			}
		}

		ImageView line = new ImageView(getContext());
		line.setBackgroundColor(0xff1a1a1a);
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
		line.setLayoutParams(ll);
		m_mainFr.AddTopChild(line);

		m_shareHideBtn = new ImageView(getContext());
		m_shareHideBtn.setImageResource(R.drawable.beauty_share_hide_btn);
		m_shareHideBtn.setScaleType(ImageView.ScaleType.CENTER);
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(40));
		m_shareHideBtn.setLayoutParams(ll);
		m_mainFr.AddTopChild(m_shareHideBtn);
		m_shareHideBtn.setOnClickListener(m_btnLst);
	}

	protected void AddItem(LinearLayout parent, View child, int width, OnClickListener lst)
	{
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_VERTICAL;
		child.setLayoutParams(ll);
		parent.addView(child);
		child.setOnClickListener(lst);
	}

	/**
	 * url:String,打开的URL
	 * share_app: int 是否分享
	 * share_logo: String 分享图片的路径
	 * title_by_url: boolean 标题是否根据url的标题来
	 * is_magazine: boolean 是否是杂志页
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		super.SetData(params);
		if(params != null)
		{
			Object obj = params.get("share_app");
			if(obj != null)
			{
				m_shareApp = (Integer)obj;
			}
			obj = params.get("is_magazine");
			if(obj != null)
			{
				m_isMagazine = (Boolean)obj;
			}
			if(m_showHomeBtn)
			{
				if(m_shareApp == 1)
				{
					m_outShare = true;
					m_shareBtn.setVisibility(View.VISIBLE);
					m_closeBtn.setVisibility(View.GONE);
				}
				else
				{
					m_outShare = false;
					m_shareBtn.setVisibility(View.GONE);
					m_closeBtn.setVisibility(View.VISIBLE);
				}
			}
			obj = params.get("share_logo");
			if(obj != null)
			{
				m_shareLogo = (String)obj;
			}
			obj = params.get("title_by_url");
			if(obj != null)
			{
				title_by_url = (Boolean)obj;
			}
			if(title_by_url)
			{
				m_title.setText("");
			}
			obj = params.get("url");
			if(obj != null)
			{
				m_shareUrl = (String)obj;
			}
			Bitmap temp = Utils.DecodeImage(getContext(), R.drawable.homepage_img1, 0, -1, ShareData.m_screenWidth, ShareData.getScreenH());
			Bitmap bmp = MakeBmp.CreateBitmap(temp, ShareData.m_screenWidth, ShareData.getScreenH(), -1, 0, Bitmap.Config.ARGB_8888);
			temp.recycle();
			temp = null;
			m_bk = BeautifyResMgr.MakeBkBmp(bmp, ShareData.m_screenWidth, ShareData.m_screenHeight, 0xaa000000, 0x26000000);
			bmp.recycle();
			m_mainFr.SetMaskBk(m_bk);
		}
		if(m_isMagazine){
			MyBeautyStat.onPageStartByRes(R.string.印象杂志页);
		}
	}

	@Override
	public void onBack()
	{
		if(m_mainFr.IsTopBarShowing())
		{
			m_mainFr.ShowTopBar(false);
			playMusic();
		}
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
				if(m_showHomeBtn)
				{
					if(m_outShare)
					{
						m_shareBtn.setVisibility(View.VISIBLE);
						m_closeBtn.setVisibility(View.GONE);
					}
					else
					{
						m_shareBtn.setVisibility(View.GONE);
						m_closeBtn.setVisibility(View.VISIBLE);
					}
				}
				return;
			}
		}

		m_site.OnBack(getContext());

	}

	@Override
	public void onClose()
	{
		super.onClose();
		m_webView = null;
		StopMove();
		m_client = null;
		if(m_shareTip != null)
		{
			m_shareTip.dismiss();
			m_shareTip = null;
		}
		TongJiUtils.onPageEnd(getContext(), TAG);

		if(m_isMagazine){
			MyBeautyStat.onClickByRes(R.string.印象杂志页_退出印象杂志);
		}
		MyBeautyStat.onPageEndByRes(R.string.印象杂志页);
	}

	private void pauseMusic()
	{
		try
		{
			if(m_webView != null)
			{
				m_webView.loadUrl("javascript:pauseMusic()");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void playMusic()
	{
		try
		{
			if(m_webView != null)
			{
				m_webView.loadUrl("javascript:playMusic()");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		pauseMusic();
		TongJiUtils.onPagePause(getContext(), TAG);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(!m_mainFr.IsTopBarShowing())
		{
			playMusic();
		}
		TongJiUtils.onPageResume(getContext(), TAG);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(m_shareTools != null)
		{
			m_shareTools.onActivityResult(requestCode, resultCode, data);
		}
		return super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params)
	{
		super.onPageResult(siteID, params);
		if(siteID == SiteID.LOGIN || siteID == SiteID.REGISTER_DETAIL || siteID == SiteID.RESETPSW)
		{
			if(!TextUtils.isEmpty(m_loginUrl) && UserMgr.IsLogin(getContext(),null))
			{
				String url = GetBeautyUrl(getContext(), m_loginUrl);
				loadUrl(url);
			}
		}
		if((siteID == SiteID.ALBUM || siteID == SiteID.CAMERA))
		{
			if(params != null)
			{
				Object obj = params.get("imgs");
				if (obj != null)
				{
					String imgPath = null;
					RotationImg2[] infos = null;
					if (obj instanceof RotationImg2[])
					{
						infos = (RotationImg2[]) obj;
					}
					else if (obj instanceof ImageFile2)
					{
						infos = ((ImageFile2) obj).SaveImg2(getContext());
					}
					if (infos != null)
					{
						imgPath = infos[0].m_orgPath;
					}
					if(!TextUtils.isEmpty(imgPath))
					{
						Uri uri = Uri.fromFile(new File(imgPath));
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

		}
	}

	public void StartMove(int startY, int endY, int delay)
	{
		StopMove();
		if(startY != endY)
		{
			m_scrollRunnable = new SmoothScrollRunnable(startY, endY, delay);
			post(m_scrollRunnable);
		}
	}

	public void StopMove()
	{
		if(m_scrollRunnable != null)
		{
			m_scrollRunnable.stop();
			m_scrollRunnable = null;
		}
	}

	public class SmoothScrollRunnable implements Runnable
	{
		private boolean m_canScroll = true;
		private int startY;
		private int endY;
		private int m_curY = -1;
		private int m_delay;
		private TweenLite m_lite;
		private boolean m_started = false;
		public SmoothScrollRunnable(int startY, int endY, int delay)
		{
			this.startY = startY;
			this.endY = endY;
			this.m_delay = delay;
			m_lite = new TweenLite(this.startY, this.endY, this.m_delay);
		}

		@Override
		public void run()
		{
			if(m_delay <= 0)
			{
				m_webView.scrollTo(0, endY);
				return;
			}
			if(!m_started)
			{
				m_lite.M1Start(TweenLite.EASE_IN_OUT | TweenLite.EASING_QUART);
				m_started = true;
				WebViewPage1.this.postDelayed(this, 16);
			}
			else
			{
				if(!m_lite.M1IsFinish() && m_canScroll)
				{
					m_curY = (int)m_lite.M1GetPos();
					m_webView.scrollTo(0, m_curY);
					WebViewPage1.this.postDelayed(this, 16);
				}
			}
		}

		public void stop()
		{
			m_canScroll = false;
			m_lite.M1End();
			removeCallbacks(this);
		}
	}

	protected void ParseShareUrl(String url)
	{
		if(!m_outShare)
		{
			int index = url.indexOf("share_app");
			if(index >= 0)
			{
				String datas = url.substring(index, url.length());
				if(datas != null && datas.length() > 0)
				{
					String[] infos = datas.split("&");
					if(infos != null)
					{
						if(infos.length >= 4)
						{
							String[] temp = infos[0].split("=");
							if(temp.length >= 2)
							{
								m_shareApp = Integer.parseInt(temp[1]);
							}
							temp = infos[1].split("=");
							if(temp.length >= 2)
							{
								m_shareTitle = Uri.decode(temp[1]);
							}
							temp = infos[2].split("=");
							if(temp.length >= 2)
							{
								m_shareLogo = Uri.decode(temp[1]);
							}
							index = infos[3].indexOf("=");
							if(index >= 0)
							{
								m_shareUrl = infos[3].substring(index + 1, infos[3].length());
								m_shareUrl = Uri.decode(m_shareUrl);
							}
//							temp = infos[3].split("=");
//							if(temp.length >= 2)
//							{
//								m_shareUrl = temp[1];
//							}
							if(m_showHomeBtn)
							{
								if(m_shareApp == 1)
								{
									m_shareBtn.setVisibility(View.VISIBLE);
									m_closeBtn.setVisibility(View.GONE);
								}
								else
								{
									m_shareBtn.setVisibility(View.GONE);
									m_closeBtn.setVisibility(View.VISIBLE);
								}
							}
						}
					}
				}
			}
			else
			{
				m_shareApp = 0;
				if(m_showHomeBtn)
				{
					if(m_shareApp == 1)
					{
						m_shareBtn.setVisibility(View.VISIBLE);
						m_closeBtn.setVisibility(View.GONE);
					}
					else
					{
						m_shareBtn.setVisibility(View.GONE);
						m_closeBtn.setVisibility(View.VISIBLE);
					}
				}
			}
		}
	}

	public class MyWebViewClient extends MyWebView.MyWebViewClient
	{
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			String temp1 = url.toLowerCase(Locale.ENGLISH);
//			System.out.println("command1: " + url);
			if(!temp1.startsWith("http") && !temp1.startsWith("ftp"))
			{
				ParseCommand(url);
				return true;
			}
			if(url.contains("jk.adnonstop.com")|| url.contains("jkzp.adnonstop.com") || url.contains("janeplus.html"))
			{
				m_isJaneKe = true;
			}
			else
			{
				m_isJaneKe = false;
			}
			ReLayoutUI();
			ParseShareUrl(url);
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			if(url.contains("jk.adnonstop.com")|| url.contains("jkzp.adnonstop.com") || url.contains("janeplus.html"))
			{
				m_isJaneKe = true;
			}
			else
			{
				m_isJaneKe = false;
			}
			ReLayoutUI();

//			System.out.println("command: " + url);
			if(url.startsWith("InterPhoto://") || url.startsWith("interPhoto://"))
			{
				ParseCommand(url);
			}
			else
			{
				ParseShareUrl(url);
				super.onPageStarted(view, url, favicon);
			}
		}

		@Override
		public void onPageFinished(WebView view, String url)
		{
			//自动播放网页音乐
			view.loadUrl("javascript:(" +
								 "function() { " +
								 "var audio1 = document.getElementById('audio');" +
								 " audio1.play();" +
								 " }" +
								 ")()");
		}
	}

	protected void ReLayoutUI()
	{
		FrameLayout.LayoutParams fl;
		if(!m_isJaneKe)
		{
			m_topFr.setVisibility(VISIBLE);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.m_screenHeight - ShareData.PxToDpi_xhdpi(80));
			fl.gravity = Gravity.LEFT | Gravity.BOTTOM;
			m_webView.setLayoutParams(fl);
			fl = (FrameLayout.LayoutParams)m_progressBar.getLayoutParams();
			fl.topMargin = ShareData.PxToDpi_xhdpi(80);
		}
		else
		{
			m_topFr.setVisibility(GONE);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.m_screenHeight);
			fl.gravity = Gravity.LEFT | Gravity.BOTTOM;
			m_webView.setLayoutParams(fl);

			fl = (FrameLayout.LayoutParams)m_progressBar.getLayoutParams();
			fl.topMargin = 0;
		}
	}

	private String m_loginUrl;

	protected void ParseCommand(String command)
	{
		BannerCore3.CmdStruct struct = BannerCore3.GetCmdStruct(command);
		if(struct != null)
		{
			if(struct.m_cmd != null && struct.m_cmd.equals("action_share"))
			{
				ArrayList<String> args = new ArrayList<String>();
				if(struct.m_params != null)
				{
					for(int i = 0; i < struct.m_params.length; i++)
					{
						args.add(struct.m_params[i]);
					}
				}

				if(args.size() > 0)
				{
					//shareplatform=(sina,qzone,qq,weixin)&sharetxt=xxxx&shareimg=xxxx&sharelink=xxxx&weixinuser=1
					String content = "";
					String platform = "";
					String callbackUrl = "";
					String imgUrl = "";
					String weixinUser = "";
					String tjId = null;
					for(int i = 0; i < args.size(); i++)
					{
						String[] pair = args.get(i).split("=");
						if(pair.length == 2)
						{
							if(pair[0].equals("shareplatform"))
							{
								platform = pair[1];
							}
							else if(pair[0].equals("sharetxt"))
							{
								content = pair[1];
							}
							else if(pair[0].equals("sharelink"))
							{
								callbackUrl = pair[1];
							}
							else if(pair[0].equals("shareimg"))
							{
								imgUrl = pair[1];
							}
							else if(pair[0].equals("weixinuser"))
							{
								weixinUser = pair[1];
							}
							else if(pair[0].equals("tj_id"))
							{
								tjId = pair[1];
							}
						}
					}
					if(platform.equals("weixin") && weixinUser.equals("1"))
					{
						platform = "weixinuser";
					}
					shareTo(platform, content, imgUrl, callbackUrl, tjId);
				}
				return;
			}
			else if(struct.m_cmd != null && struct.m_cmd.equals("interphoto"))
			{
				if(struct.m_params != null && struct.m_params.length > 0)
				{
					String[] pair = struct.m_params[0].split("=");
					if(pair.length == 2)
					{
						if(pair[0].equals("openweb"))
						{
							String url = URLDecoder.decode(pair[1]);
							if(!TextUtils.isEmpty(url))
							{
								CommonUtils.OpenBrowser(getContext(), url);
							}
						}
						if(pair[0].equals("open") && struct.m_params.length >= 2)
						{
							if(pair[1].equals("login"))
							{
								pair = struct.m_params[1].split("=");
								m_loginUrl = URLDecoder.decode(pair[1]);
								if(UserMgr.IsLogin(getContext(),null))
								{
									String url = GetBeautyUrl(getContext(), m_loginUrl);
									loadUrl(url);
								}
								else
								{
									m_site.OnLogin(getContext());
								}
							}
							else if(pair[1].equals("month"))
							{
								pair = struct.m_params[1].split("=");
								m_loginUrl = URLDecoder.decode(pair[1]);
								String url = GetBeautyUrl(getContext(), m_loginUrl);
								loadUrl(url);
							}
						}
					}
				}
			}

		}
	}

	public static String GetBeautyUrl(Context context, String url)
	{
		JSONObject postJson = new JSONObject();
		try
		{
			JSONObject paramJson = new JSONObject();
			UserInfo userInfo = UserMgr.ReadCache(context);
			SettingInfo settingInfo = SettingInfoMgr.GetSettingInfo(context);
			if(userInfo != null && settingInfo != null)
			{
				paramJson.put("user_id", userInfo.mUserId);
				paramJson.put("access_token", settingInfo.GetPoco2Token(false));
				paramJson.put("expire_time", settingInfo.GetPoco2ExpiresIn());
			}

			String param = new StringBuilder().append("poco_").append(paramJson.toString()).append("_app").toString();
			String signStr = CommonUtils.Encrypt("MD5", param);
			String signCode = signStr.substring(5, (signStr.length() - 8));

			postJson.put("version",SysConfig.GetAppVerNoSuffix(context));
			postJson.put("os_type", "android");
			postJson.put("ctime", System.currentTimeMillis());
			postJson.put("app_name", "interphoto_app_android");
			postJson.put("is_enc", 0);
			postJson.put("imei", CommonUtils.GetIMEI(context));
			postJson.put("sign_code", signCode);
			postJson.put("param", paramJson);
			postJson.put("come_from", "interphoto");
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		StringBuilder builder = new StringBuilder(url);
		builder.append("&req=");
		builder.append(new String(Base64.encode(postJson.toString().getBytes(), Base64.NO_WRAP)));
		return builder.toString();
	}

	private void shareTo(final String platform, final String text, final String orgImgUrl, final String orgCallbackUrl, String tjId)
	{
		TongJi2.AddCountById(tjId);

		final String content = text != null ? URLDecoder.decode(text) : null;
		final String imgUrl = URLDecoder.decode(orgImgUrl);
		final String callbackUrl = URLDecoder.decode(orgCallbackUrl);

		if(imgUrl != null && imgUrl.length() > 0)
		{
			ShowWaitDlg();

			final Handler uiHandler = new Handler();
			final String imgPath = FileCacheMgr.GetAppPath(".jpg");
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					NetCore2 net = new NetCore2();
					final NetCore2.NetMsg msg = net.HttpGet(imgUrl, null, imgPath, null);
//					System.out.println("msg: " + msg);
					uiHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							if(msg != null && msg.m_stateCode == HttpURLConnection.HTTP_OK)
							{
								if(platform.equals("sina"))
								{
									CloseWaitDlg();
									if(SettingPage.checkSinaBindingStatus(getContext()))
									{
										m_shareTools.sendToSina(imgPath, content, new ShareTools.SendCompletedListener()
										{
											@Override
											public void result(int result)
											{
												if(result == ShareTools.SUCCESS)
												{
													ShareTools.ToastSuccess(getContext());
												}
											}
										});
									}
									else
									{
										m_shareTools.bindSina(new SharePage.BindCompleteListener()
										{
											@Override
											public void success()
											{
												m_shareTools.sendToSina(imgPath, content, new ShareTools.SendCompletedListener()
												{
													@Override
													public void result(int result)
													{
														if(result == ShareTools.SUCCESS)
														{
															ShareTools.ToastSuccess(getContext());
														}
													}
												});
											}

											@Override
											public void fail(){}
										});
									}
								}
								else if(platform.equals("qqzone"))
								{
									if(SettingPage.checkQzoneBindingStatus(getContext()))
									{
										CloseWaitDlg();
										m_shareTools.sendUrlToQzone(content, imgPath, getResources().getString(R.string.FromInterphoto), callbackUrl, null);
									}
									else
									{
										CloseWaitDlg();
										mProgressDialogQQ = ProgressDialog.show(getContext(), "", getContext().getResources().getString(R.string.Linking));
										mProgressDialogQQ.setProgressStyle(ProgressDialog.STYLE_SPINNER);
										m_shareTools.bindQzone(false,new SharePage.BindCompleteListener()
										{
											@Override
											public void success()
											{
												if(mProgressDialogQQ != null)
												{
													mProgressDialogQQ.dismiss();
													mProgressDialogQQ = null;
												}
												CloseWaitDlg();
												m_shareTools.sendUrlToQzone(content, imgPath, getResources().getString(R.string.FromInterphoto), callbackUrl, null);
											}

											@Override
											public void fail()
											{
												if(mProgressDialogQQ != null)
												{
													mProgressDialogQQ.dismiss();
													mProgressDialogQQ = null;
												}
											}
										});
									}
								}
								else if(platform.equals("qq"))
								{
									if(SettingPage.checkQzoneBindingStatus(getContext()))
									{
										CloseWaitDlg();
										m_shareTools.sendToQQ(imgPath, new ShareTools.SendCompletedListener()
										{
											@Override
											public void result(int result)
											{
												if(result == ShareTools.SUCCESS)
												{
													ShareTools.ToastSuccess(getContext());
												}
											}
										});
									}
									else
									{
										CloseWaitDlg();
										mProgressDialogQQ = ProgressDialog.show(getContext(), "", getContext().getResources().getString(R.string.Linking));
										mProgressDialogQQ.setProgressStyle(ProgressDialog.STYLE_SPINNER);
										m_shareTools.bindQzone(false,new SharePage.BindCompleteListener()
										{
											@Override
											public void success()
											{
												CloseWaitDlg();
												if(mProgressDialogQQ != null)
												{
													mProgressDialogQQ.dismiss();
													mProgressDialogQQ = null;
												}
												m_shareTools.sendToQQ(imgPath, new ShareTools.SendCompletedListener()
												{
													@Override
													public void result(int result)
													{
														if(result == ShareTools.SUCCESS)
														{
															ShareTools.ToastSuccess(getContext());
														}
													}
												});
											}

											@Override
											public void fail()
											{
												if(mProgressDialogQQ != null)
												{
													mProgressDialogQQ.dismiss();
													mProgressDialogQQ = null;
												}
											}
										});
									}
								}
								else if(platform.equals("weixin"))
								{
									CloseWaitDlg();
									m_shareTools.sendUrlToWeiXin(Utils.MakeLogo(getContext(), imgPath), callbackUrl, content, "", false, new ShareTools.SendCompletedListener()
									{
										@Override
										public void result(int result)
										{
											if(result == ShareTools.SUCCESS)
											{
												ShareTools.ToastSuccess(getContext());
											}
										}
									});
								}
								else if(platform.equals("weixinuser"))
								{
									CloseWaitDlg();
									m_shareTools.sendUrlToWeiXin(Utils.MakeLogo(getContext(), imgPath), callbackUrl, content, "", true, new ShareTools.SendCompletedListener()
									{
										@Override
										public void result(int result)
										{
											if(result == ShareTools.SUCCESS)
											{
												ShareTools.ToastSuccess(getContext());
											}
										}
									});
								}
								else
								{
									CloseWaitDlg();
									Toast.makeText(getContext(), getResources().getString(R.string.accessDataFailed), Toast.LENGTH_LONG).show();
								}
							}
							else
							{
								CloseWaitDlg();
								Toast.makeText(getContext(), getResources().getString(R.string.accessDataFailed), Toast.LENGTH_LONG).show();
							}
						}
					});
				}
			}).start();
		}
	}

	protected void ShowWaitDlg()
	{
		if(mProgressDialog == null)
		{
			mProgressDialog = ProgressDialog.show(getContext(), "", getResources().getString(R.string.sending));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}
		mProgressDialog.show();
	}

	protected void CloseWaitDlg()
	{
		if(mProgressDialog != null)
		{
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
}
