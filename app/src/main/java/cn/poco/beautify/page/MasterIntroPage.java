package cn.poco.beautify.page;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseResp;

import java.util.HashMap;

import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.MyButtons;
import cn.poco.beautify.RecomDisplayMgr;
import cn.poco.beautify.ScrollShareFr;
import cn.poco.beautify.site.MasterIntroPageSite;
import cn.poco.blogcore.FacebookBlog;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.LightEffectRes;
import cn.poco.resource.LockRes;
import cn.poco.resource.VideoTextRes;
import cn.poco.setting.SettingPage;
import cn.poco.share.SendWXAPI;
import cn.poco.share.SharePage;
import cn.poco.share.ShareTools;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.MyWebView;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.ImageUtil;
import cn.poco.utils.ScaleEvaluator;
import cn.poco.utils.Utils;

/**
 * 大师介绍页
 */

public class MasterIntroPage extends IPage
{
	public static final String TAG = "大师介绍页";
	private MasterIntroPageSite m_site;
	protected MasterFrameLayout m_mainFr;
	protected LinearLayout m_viewFr;
	protected ImageView m_closeBtn;
	protected ImageView m_backBtn;
	protected ImageView m_shareBtn;

	protected ImageView m_topImg;
	protected ImageView m_headImg;
	protected TextView m_name;
	protected TextView m_detail;
	protected TextView m_introText;
	protected LinearLayout m_bottomBar;
	protected MyWebView m_webView;

	private ImageView m_animView;

	protected ScrollShareFr m_scrollShareFr;
	protected int m_shareFrHeight;
	protected MyButtons m_friendBtn;
	protected MyButtons m_weixinBtn;
	protected MyButtons m_sinaBtn;
	protected MyButtons m_qqBtn;
	protected MyButtons m_facebookBtn;
	protected ImageView m_shareHideBtn;

	protected LinearLayout m_friendUnlockBtn;
	protected TextView m_unLockText;
	private AlertDialog m_shareTip;

	protected ShareTools m_shareTools;
	protected Toast m_toast;
	protected Bitmap m_topBmp;
	protected Bitmap m_headBmp;

	protected int filter_id = -1;
	protected boolean m_lock = false;
	protected String m_unlockTag;
	protected BaseRes m_res;
	protected String m_shareUrl;
	protected Object m_shareImg;
	protected String m_shareTitle;
	protected String m_shareContent;
	protected Bitmap m_bkBmp;
	protected ProgressDialog mProgressDialog;
	private boolean m_uiEnabled;

	private int m_centerX;
	private int m_centerY;
	private int m_viewH;
	private int m_viewW;
	private String m_loadUrl;

	private int m_curTjPageResID;

	public MasterIntroPage(Context context, BaseSite site)
	{
		super(context, site);
		m_site = (MasterIntroPageSite)site;
		init();

		TongJiUtils.onPageStart(getContext(), TAG);
	}

	private void init()
	{
		m_uiEnabled = true;
		m_shareFrHeight = ShareData.PxToDpi_xhdpi(242);
		m_shareTools = new ShareTools(getContext());

		m_scrollShareFr = new ScrollShareFr(this, m_shareFrHeight);
		m_scrollShareFr.setOnCloseListener(new ScrollShareFr.OnCloseListener()
		{
			@Override
			public void onClose()
			{
				m_btnListener.onClick(m_shareHideBtn);
			}
		});
		InitShareFr();

		InitMainFr();

		m_animView = new ImageView(getContext());
		LayoutParams fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.m_screenWidth);
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		m_animView.setLayoutParams(fl);
		this.addView(m_animView);

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

	protected void InitShareFr()
	{
		m_shareTools = new ShareTools(getContext());
		LinearLayout.LayoutParams ll;
		LinearLayout shareFr = new LinearLayout(getContext());
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		ll.topMargin = ShareData.PxToDpi_xhdpi(35);
		ll.bottomMargin = ShareData.PxToDpi_xhdpi(35);
		shareFr.setLayoutParams(ll);
		m_scrollShareFr.AddTopChild(shareFr);
		{
			int width = ShareData.m_screenWidth / 5;

			m_friendBtn = new MyButtons(getContext(), R.drawable.beauty_share_friend, R.drawable.beauty_share_friend);
			m_friendBtn.SetText(getResources().getString(R.string.Moments));
			AddItem(shareFr, m_friendBtn, width);

			m_weixinBtn = new MyButtons(getContext(), R.drawable.beauty_share_weixin, R.drawable.beauty_share_weixin);
			m_weixinBtn.SetText(getResources().getString(R.string.Wechat));
			AddItem(shareFr, m_weixinBtn, width);

			m_sinaBtn = new MyButtons(getContext(), R.drawable.beauty_share_sina, R.drawable.beauty_share_sina);
			m_sinaBtn.SetText(getResources().getString(R.string.Sina));
			AddItem(shareFr, m_sinaBtn, width);

			m_qqBtn = new MyButtons(getContext(), R.drawable.beauty_share_qzone, R.drawable.beauty_share_qzone);
			m_qqBtn.SetText(getResources().getString(R.string.QQZone));
			AddItem(shareFr, m_qqBtn, width);

			m_facebookBtn = new MyButtons(getContext(), R.drawable.beauty_share_facebook, R.drawable.beauty_share_facebook);
			m_facebookBtn.SetText("Facebook");
			AddItem(shareFr, m_facebookBtn, width);
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
		m_scrollShareFr.AddTopChild(line);

		m_shareHideBtn = new ImageView(getContext());
		m_shareHideBtn.setImageResource(R.drawable.beauty_share_hide_btn);
		m_shareHideBtn.setScaleType(ImageView.ScaleType.CENTER);
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(40));
		m_shareHideBtn.setLayoutParams(ll);
		m_scrollShareFr.AddTopChild(m_shareHideBtn);
		m_shareHideBtn.setOnClickListener(m_btnListener);
	}

	private void InitMainFr()
	{
		FrameLayout.LayoutParams fl;
		m_mainFr = new MasterFrameLayout(getContext());
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		m_mainFr.setLayoutParams(fl);
		m_scrollShareFr.AddMainChild(m_mainFr);
		{
			LinearLayout.LayoutParams ll;
			m_viewFr = new LinearLayout(getContext());
			m_viewFr.setOrientation(LinearLayout.VERTICAL);
			m_viewFr.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			m_viewFr.setMinimumHeight(ShareData.m_screenHeight);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			m_viewFr.setLayoutParams(fl);
			m_mainFr.m_scrollView.addView(m_viewFr);
			{
				m_topImg = new ImageView(getContext());
				m_topImg.setScaleType(ImageView.ScaleType.FIT_XY);
				fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth, ShareData.m_screenWidth);
				m_topImg.setLayoutParams(fl);
				m_viewFr.addView(m_topImg);

				int viewSize = ShareData.PxToDpi_xhdpi(160);
				m_headImg = new ImageView(getContext());
				m_headImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
				ll = new LinearLayout.LayoutParams(viewSize, viewSize);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				ll.topMargin = -viewSize / 2;
				m_headImg.setLayoutParams(ll);
				m_viewFr.addView(m_headImg);

				m_name = new TextView(getContext());
				m_name.setTextColor(0xffffffff);
				m_name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.topMargin = ShareData.PxToDpi_xhdpi(20);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				m_name.setLayoutParams(ll);
				m_viewFr.addView(m_name);

				int viewWidth = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40) * 2;
				m_detail = new TextView(getContext());
				m_detail.setGravity(Gravity.CENTER);
				m_detail.setTextColor(0xff666666);
				m_detail.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
				m_detail.setPadding(ShareData.PxToDpi_xhdpi(120), 0, ShareData.PxToDpi_xhdpi(120), 0);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.topMargin = ShareData.PxToDpi_xhdpi(15);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				m_detail.setLayoutParams(ll);
				m_viewFr.addView(m_detail);

				m_introText = new TextView(getContext());
				m_introText.setTextColor(0xffaaaaaa);
				m_introText.setLineSpacing(1.0f, 1.3f);
				m_introText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				ll = new LinearLayout.LayoutParams(viewWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.topMargin = ShareData.PxToDpi_xhdpi(70);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				m_introText.setLayoutParams(ll);
				m_viewFr.addView(m_introText);

				m_bottomBar = new LinearLayout(getContext());
				m_bottomBar.setOrientation(LinearLayout.VERTICAL);
				m_bottomBar.setPadding(0, 0, 0, ShareData.PxToDpi_xhdpi(60));
				ll = new LinearLayout.LayoutParams(viewWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				ll.topMargin = ShareData.PxToDpi_xhdpi(50);
				m_bottomBar.setLayoutParams(ll);
				m_viewFr.addView(m_bottomBar);

				m_webView = new MyWebView(getContext(), null);
				m_webView.setBackgroundColor(0xff0e0e0e);
				m_webView.m_webView.setBackgroundColor(0xff0e0e0e);
				m_webView.m_webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
				m_webView.m_webView.getSettings().setLoadWithOverviewMode(true);
				m_webView.m_webView.getSettings().setUseWideViewPort(true);
				m_webView.m_webView.getSettings().setBuiltInZoomControls(true);
				m_webView.setWebViewClient(new MyWebViewClient());
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				ll.topMargin = ShareData.PxToDpi_xhdpi(50);
				m_webView.setLayoutParams(ll);
				m_viewFr.addView(m_webView);
			}

			m_closeBtn = new ImageView(getContext());
			m_closeBtn.setImageResource(R.drawable.beauty_master_filter_tip_close_btn);
			m_closeBtn.setVisibility(View.GONE);
			m_closeBtn.setOnClickListener(m_btnListener);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.TOP | Gravity.RIGHT;
			fl.topMargin = ShareData.PxToDpi_xhdpi(30);
			fl.rightMargin = fl.topMargin;
			m_closeBtn.setLayoutParams(fl);
			m_mainFr.addView(m_closeBtn);

			m_backBtn = new ImageView(getContext());
			m_backBtn.setImageResource(R.drawable.beauty_master_filter_tip_back_btn);
			m_backBtn.setOnClickListener(m_btnListener);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.TOP | Gravity.LEFT;
			fl.topMargin = ShareData.PxToDpi_xhdpi(30);
			fl.leftMargin = fl.topMargin;
			m_backBtn.setLayoutParams(fl);
			m_mainFr.addView(m_backBtn);

			m_shareBtn = new ImageView(getContext());
			m_shareBtn.setVisibility(View.GONE);
			m_shareBtn.setImageResource(R.drawable.beauty_master_filter_tip_share_btn);
			m_shareBtn.setOnClickListener(m_btnListener);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.TOP | Gravity.RIGHT;
			fl.topMargin = ShareData.PxToDpi_xhdpi(30);
			fl.rightMargin = fl.topMargin;
			m_shareBtn.setLayoutParams(fl);
			m_mainFr.addView(m_shareBtn);

			m_friendUnlockBtn = new LinearLayout(getContext());
			m_friendUnlockBtn.setVisibility(View.GONE);
			m_friendUnlockBtn.setGravity(Gravity.CENTER);
			m_friendUnlockBtn.setBackgroundColor(0xffffc433);
			fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(98));
			fl.gravity = Gravity.BOTTOM;
			m_friendUnlockBtn.setLayoutParams(fl);
			m_friendUnlockBtn.setOnClickListener(m_btnListener);
			m_mainFr.addView(m_friendUnlockBtn);
			{
				ImageView img = new ImageView(getContext());
				img.setImageResource(R.drawable.master_share_friend);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				img.setLayoutParams(ll);
				m_friendUnlockBtn.addView(img);

				m_unLockText = new TextView(getContext());
				m_unLockText.setTextColor(0xffffffff);
				m_unLockText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				TextPaint tp = m_unLockText.getPaint();
				tp.setFakeBoldText(true);
				m_unLockText.setText(getResources().getString(R.string.unlockfilterTips));
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				ll.leftMargin = ShareData.PxToDpi_xhdpi(20);
				m_unLockText.setLayoutParams(ll);
				m_friendUnlockBtn.addView(m_unLockText);
			}
		}
	}

	protected void AddItem(LinearLayout parent, View child, int width)
	{
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_VERTICAL;
		child.setLayoutParams(ll);
		parent.addView(child);
		child.setOnClickListener(m_btnListener);
	}

	private OnClickListener m_btnListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(m_uiEnabled)
			{
				if(v == m_closeBtn || v == m_backBtn)
				{
					onBack();
				}
				else if(v == m_friendUnlockBtn)
				{
					UnLock();
				}
				else if(v == m_shareHideBtn)
				{
					m_scrollShareFr.ShowTopBar(false);
				}
				else if(v == m_shareBtn)
				{
					m_scrollShareFr.ShowTopBar(true);
				}
				else if(v == m_friendBtn)
				{
					shareToWeixinFriend();
				}
				else if(v == m_sinaBtn)
				{
					shareToSina();
				}
				else if(v == m_qqBtn)
				{
					shareToQZone();
				}
				else if(v == m_facebookBtn)
				{
					shareToFacebook();
				}
				else if(v == m_weixinBtn)
				{
					shareToWeixin();
				}
			}
		}
	};

	private void UnLock() {
		if(m_res != null && m_res instanceof VideoTextRes){
			if(((VideoTextRes) m_res).lockType == LockRes.SHARE_TYPE_MARKET){
				OpenMarket();
				m_lock = false;
				TagMgr.SetTag(getContext(), m_unlockTag + filter_id);
				return;
			}
		}
		WeixinUnlock();
	}

	@Override
	public void onResume() {
		if(!m_lock) {
			//市场解锁返回
//			onBack();
		}
	}

	public void OpenMarket()
	{
		try
		{
			Uri uri = Uri.parse("market://details?id=" + getContext().getPackageName());
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getContext().startActivity(intent);
		}
		catch(Throwable e)
		{
			Toast.makeText(getContext(), "还没有安装安卓市场，请先安装", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

	}

	private void WeixinUnlock()
	{
		if(m_res != null)
		{
			String shareContent = null;
			Object shareImg = null;
			String shareLink = null;
			if(m_res instanceof VideoTextRes) {
				shareContent = ((VideoTextRes)m_res).shareTitle;
				shareLink = ((VideoTextRes)m_res).m_shareLink;
				shareImg = ((VideoTextRes)m_res).shareImg;
			}if(m_res instanceof LockRes && m_res.m_type != BaseRes.TYPE_NETWORK_URL)
		{
			shareContent = ((LockRes)m_res).m_shareContent;
			shareLink = ((LockRes)m_res).m_shareLink;
			shareImg = ((LockRes)m_res).m_shareImg;
		}
		else if(m_res instanceof LightEffectRes)
		{
			shareContent = ((LightEffectRes)m_res).m_shareContent;
			shareLink = ((LightEffectRes)m_res).m_shareUrl;
			shareImg = ((LightEffectRes)m_res).m_shareThumb;
		}
			if(shareContent == null && shareImg == null && shareLink == null)
			{
				return;
			}
			SharePage.unlockResourceByWeiXin(getContext(), shareContent, shareContent, shareLink, RecomDisplayMgr.MakeWXLogo(Utils.MakeLogo(getContext(), shareImg)), false, new SendWXAPI.WXCallListener()
			{
				@Override
				public void onCallFinish(int result)
				{
					if(result != BaseResp.ErrCode.ERR_USER_CANCEL)
					{
						TagMgr.SetTag(getContext(), m_unlockTag + filter_id);
						m_lock = false;
						if(m_shareUrl != null && m_shareUrl.length() > 0)
						{
							m_shareBtn.setVisibility(View.VISIBLE);
						}
						m_friendUnlockBtn.setVisibility(View.GONE);
						Toast.makeText(getContext(), getResources().getString(R.string.UnlockSuccessful), Toast.LENGTH_SHORT).show();
						onBack();
					}
				}
			});
		}
		else
		{
			if(m_toast == null)
			{
				m_toast = Toast.makeText(getContext(), getResources().getString(R.string.unlockFailed), Toast.LENGTH_SHORT);
			}
			m_toast.show();
		}
	}

	private void shareToWeixin()
	{
		TongJi2.AddCountByRes(getContext(), R.integer.分享_微信好友);
		MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微信好友, m_curTjPageResID);
		m_shareTools.sendUrlToWeiXin(Utils.MakeLogo(getContext(), m_shareImg), m_shareUrl, m_shareTitle, "", true, new ShareTools.SendCompletedListener()
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

	private void shareToWeixinFriend()
	{
		TongJi2.AddCountByRes(getContext(), R.integer.分享_微信朋友圈);
		MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.朋友圈, m_curTjPageResID);
		m_shareTools.sendUrlToWeiXin(Utils.MakeLogo(getContext(), m_shareImg), m_shareUrl, m_shareTitle, "", false, new ShareTools.SendCompletedListener()
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

	private void shareToSina()
	{
		if(SettingPage.checkSinaBindingStatus(getContext()))
		{
			MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博, m_curTjPageResID);
			TongJi2.AddCountByRes(getContext(), R.integer.分享_新浪微博);
			m_shareTools.sendToSina(Utils.MakeLogo(getContext(), m_shareImg), m_shareTitle + m_shareUrl, new ShareTools.SendCompletedListener()
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
					MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博, m_curTjPageResID);
					m_shareTools.sendToSina(Utils.MakeLogo(getContext(), m_shareImg), m_shareTitle + m_shareUrl, new ShareTools.SendCompletedListener()
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

	private void shareToQZone()
	{
		if(SettingPage.checkQzoneBindingStatus(getContext()))
		{
			TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ空间);
			MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间, m_curTjPageResID);
			m_shareTools.sendUrlToQzone("", Utils.MakeLogo(getContext(), m_shareImg), m_shareTitle, m_shareUrl, new ShareTools.SendCompletedListener()
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
			mProgressDialog = ProgressDialog.show(getContext(), "", getContext().getResources().getString(R.string.Linking));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			m_shareTools.bindQzone(false,new SharePage.BindCompleteListener()
			{
				@Override
				public void success()
				{
					if(mProgressDialog != null)
					{
						mProgressDialog.dismiss();
						mProgressDialog = null;
					}
					TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ空间);
					MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间, m_curTjPageResID);
					m_shareTools.sendUrlToQzone("", Utils.MakeLogo(getContext(), m_shareImg), m_shareTitle, m_shareUrl, new ShareTools.SendCompletedListener()
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
					if(mProgressDialog != null)
					{
						mProgressDialog.dismiss();
						mProgressDialog = null;
					}
				}
			});
		}
	}

	private void shareToFacebook()
	{
		TongJi2.AddCountByRes(getContext(), R.integer.分享_Facebook);
		MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.facebook, m_curTjPageResID);
		String detail = m_name.getText() + " | " + m_detail.getText() + "\n";
		m_shareTools.sendUrlToFacebook(detail + m_shareTitle, m_shareContent, m_shareUrl, new FacebookBlog.FaceBookSendCompleteCallback()
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

	public void setData(HashMap<String, Object> params)
	{
		if (params == null)
			return;
		Object obj;
		obj = params.get("name");
		if (obj != null) {
			m_name.setText((String) obj);
		}

		obj = params.get("detail");
		if (obj != null) {
			m_shareTitle = (String) obj;
			m_detail.setText((String) obj);
		}

		obj = params.get("filter_id");
		if (obj != null)
		{
			filter_id = (Integer) obj;
		}

		obj = params.get("unlock_tag");
		if (obj != null)
		{
			m_unlockTag = (String) obj;
		}
		obj = params.get("intro");
		if (obj != null) {
			m_shareContent = (String) obj;
			m_introText.setText((String) obj);
		}

		obj = params.get("lock");
		if (obj != null) {
			m_lock = (Boolean) obj;
		}

		m_res = (BaseRes) params.get("res");

		if(m_lock)
		{
			m_friendUnlockBtn.setVisibility(View.VISIBLE);
			m_shareBtn.setVisibility(View.GONE);

			if(m_res != null && m_res instanceof VideoTextRes) {
				if(((VideoTextRes) m_res).lockType == LockRes.SHARE_TYPE_MARKET){
					m_unLockText.setText(getResources().getString(R.string.unlockVideoTextTips));
				}else{
					m_unLockText.setText(getResources().getString(R.string.shareToUnlockVideoTextTips));
				}
				m_shareBtn.setVisibility(View.GONE);
				m_webView.setVisibility(View.INVISIBLE);
				if (m_res != null && m_res.m_type == BaseRes.TYPE_NETWORK_URL) {
					//下载资源
					DownloadMgr.getInstance().DownloadRes(m_res, null);
				}
			}else{

			}
		}
		else
		{
			obj = params.get("share_url");
			if(obj != null)
			{
				m_shareUrl = (String)obj;
			}
			obj = params.get("share_title");
			if(obj != null)
			{
				m_shareTitle = (String)obj;
			}
			if(m_shareUrl != null && m_shareUrl.length() > 0 && m_lock == false)
			{
				m_shareBtn.setVisibility(View.VISIBLE);
			}
			else
			{
				m_shareBtn.setVisibility(View.GONE);
			}
		}
		obj = params.get("img_url");
		if(obj != null)
		{
			m_loadUrl = (String)obj;
		}

		final Object top_img = params.get("top_img");
		final Object head_img = params.get("head_img");
		if (top_img != null) {
			m_topBmp = ImageUtil.CreateShowBmp(getContext(), top_img, ShareData.m_screenWidth, ShareData.m_screenWidth);
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (head_img != null) {
					m_shareImg = head_img;
					Bitmap temp = ImageUtil.CreateShowBmp(getContext(), head_img, -1, -1);
					m_headBmp = ImageUtil.makeCircleBmp(temp, 3, 0xffffffff);
				}
				((Activity) getContext()).runOnUiThread(new Runnable() {
					@Override
					public void run() {

						Bitmap tempBmp = ImageUtil.GetScreenBmp((Activity)getContext(), ShareData.m_screenWidth / 6, ShareData.m_screenHeight / 6);

						m_bkBmp = BeautifyResMgr.MakeBkBmp(tempBmp, ShareData.m_screenWidth / 6, ShareData.m_screenHeight / 6, 0xaa000000, 0x26000000);

						m_scrollShareFr.SetMaskBk(m_bkBmp);
					}
				});
			}
		}).start();
	}

	public void show()
	{
		if(m_uiEnabled)
		{
			m_uiEnabled = false;
			m_animView.setImageBitmap(m_topBmp);
			m_animView.setScaleType(ImageView.ScaleType.FIT_XY);

			m_animView.setVisibility(VISIBLE);
			m_animView.clearAnimation();
			m_mainFr.setAlpha(0);

			int w = ShareData.m_screenWidth;
			int endW = w;
			ValueAnimator scale = ValueAnimator.ofObject(new ScaleEvaluator(), new Point(m_viewW, m_viewH),
														 new Point(endW + 50, endW + 50), new Point(endW, endW));
			scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					Point point = (Point)animation.getAnimatedValue();
					FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) m_animView.getLayoutParams();
					params.height = point.y;
					params.width = point.x;
					m_animView.setLayoutParams(params);
				}
			});
			scale.setDuration(550);

			int centerX = m_centerX - (ShareData.m_screenWidth - m_viewW) / 2;
			ValueAnimator trans = ValueAnimator.ofObject(new ScaleEvaluator(),
														 new Point(centerX, m_centerY), new Point(0, -50), new Point(0, 0));
			trans.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					Point point = (Point)animation.getAnimatedValue();
					FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) m_animView.getLayoutParams();
					params.topMargin = point.y;
					params.leftMargin = point.x;
					m_animView.setLayoutParams(params);
				}
			});
			trans.setDuration(350);

			ValueAnimator alpha = ValueAnimator.ofFloat(0, 1);
			alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float animatorValue = Float.valueOf(animation.getAnimatedValue() + "");
					m_mainFr.setAlpha(animatorValue);
				}
			});
			alpha.setStartDelay(200);
			alpha.setDuration(350);

			AnimatorSet as = new AnimatorSet();
			as.setInterpolator(new DecelerateInterpolator());
			as.addListener(new Animator.AnimatorListener()
			{
				@Override
				public void onAnimationStart(Animator animation)
				{

				}

				@Override
				public void onAnimationEnd(Animator animation)
				{
					m_uiEnabled = true;
					m_animView.clearAnimation();
					m_animView.setVisibility(GONE);

					m_headImg.setImageBitmap(m_headBmp);
					if (m_webView != null && !TextUtils.isEmpty(m_loadUrl)) {
						m_webView.loadUrl(m_loadUrl);
					}
					m_topImg.setImageBitmap(m_topBmp);
				}

				@Override
				public void onAnimationCancel(Animator animation)
				{

				}

				@Override
				public void onAnimationRepeat(Animator animation)
				{

				}
			});
			as.play(scale).with(trans);
			as.play(trans).with(alpha);
			as.start();
		}
	}

	public void close()
	{
		if(m_uiEnabled)
		{
			m_uiEnabled = false;
			closeAnim();
		}
	}

	private void closeAnim()
	{
		m_animView.setVisibility(VISIBLE);
		m_animView.clearAnimation();
		m_mainFr.setVisibility(GONE);

		ValueAnimator scale = ValueAnimator.ofObject(new ScaleEvaluator(), new Point(ShareData.m_screenWidth, ShareData.m_screenWidth), new Point(m_viewW, m_viewH));
		scale.setInterpolator(new DecelerateInterpolator());
		scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Point point = (Point)animation.getAnimatedValue();
				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) m_animView.getLayoutParams();
				params.height = point.y;
				params.width = point.x;
				m_animView.setLayoutParams(params);
			}
		});
		scale.setDuration(350);

		int centerX = m_centerX - (ShareData.m_screenWidth - m_viewW) / 2;
		ValueAnimator trans = ValueAnimator.ofObject(new ScaleEvaluator(), new Point(0, m_mainFr.getPaddingTop()),
													 new Point(centerX, m_centerY));
		trans.setInterpolator(new DecelerateInterpolator());
		trans.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Point point = (Point)animation.getAnimatedValue();
				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) m_animView.getLayoutParams();
				params.topMargin = point.y;
				params.leftMargin = point.x;
				m_animView.setLayoutParams(params);
			}
		});
		trans.setDuration(350);

		ValueAnimator alpha = ValueAnimator.ofFloat(1, 0);
		alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float animatorValue = Float.valueOf(animation.getAnimatedValue() + "");
				m_animView.setAlpha(animatorValue);
			}
		});
		alpha.setDuration(100);
		alpha.setStartDelay(250);

		AnimatorSet as = new AnimatorSet();
		as.addListener(new Animator.AnimatorListener()
		{
			@Override
			public void onAnimationStart(Animator animation)
			{
			}

			@Override
			public void onAnimationEnd(Animator animation)
			{
				m_animView.clearAnimation();
				m_mainFr.setVisibility(GONE);
				onBack1();
				clearAll();
			}

			@Override
			public void onAnimationCancel(Animator animation)
			{

			}

			@Override
			public void onAnimationRepeat(Animator animation)
			{

			}
		});
		as.play(scale).with(trans).with(alpha);
		as.start();
	}

	public void clearAll()
	{
		this.removeAllViews();
		this.clearFocus();
		if (m_webView != null) {
			m_webView.onClose();
			m_webView = null;
		}
		m_topBmp = null;
		m_headBmp = null;
		m_bkBmp = null;
		m_scrollShareFr.SetMaskBk(null);
		if(m_shareTip != null)
		{
			m_shareTip.dismiss();
			m_shareTip = null;
		}
		m_shareImg = null;
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
	public void SetData(HashMap<String, Object> params)
	{
		if(params == null)
			return;
		setData(params);
		Object o = params.get("centerX");
		if(o != null)
		{
			m_centerX = (Integer)o;
		}
		o = params.get("centerY");
		if(o != null)
		{
			m_centerY = (Integer)o;
		}
		o = params.get("viewH");
		if(o != null)
		{
			m_viewH = (Integer)o;
		}
		o = params.get("viewW");
		if(o != null)
		{
			m_viewW = (Integer)o;
		}

		show();

		o = params.get("pageId");
		if(o != null)
		{
			m_curTjPageResID = (Integer)o;
		}
		if(m_curTjPageResID == 0){
			m_curTjPageResID = R.string.照片滤镜_大师介绍页;
		}
		MyBeautyStat.onPageStartByRes(m_curTjPageResID);
	}

	@Override
	public void onBack()
	{
		if(m_scrollShareFr.IsTopBarShowing())
		{
			m_scrollShareFr.ShowTopBar(false);
			return;
		}
		close();
	}

	private void onBack1()
	{
		HashMap<String, Object> params = new HashMap<>();
		params.put("id", filter_id);
		params.put("lock", m_lock);
		m_site.onBack(params,getContext());

		MyBeautyStat.onClickByRes(R.string.大师介绍页_退出大师介绍页);
		MyBeautyStat.onPageEndByRes(m_curTjPageResID);
	}

	public class MyWebViewClient extends MyWebView.MyWebViewClient
	{
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

	class MasterFrameLayout extends FrameLayout
	{
		public ScrollView m_scrollView;
		private float m_downY;
		private int m_topPadding;
		private final float m_max = 80f;
		private final int m_maxAlpha = ShareData.m_screenHeight / 2;
		private ValueAnimator m_translateAnim;

		public MasterFrameLayout(Context context)
		{
			this(context, null);
		}

		public MasterFrameLayout(Context context, AttributeSet attrs)
		{
			this(context,  attrs, 0);
		}

		public MasterFrameLayout(Context context, AttributeSet attrs, int defStyleAttr)
		{
			super(context, attrs, defStyleAttr);
			Init();
		}

		private void Init()
		{
			m_scrollView = new ScrollView(getContext());
			m_scrollView.setBackgroundColor(0xff0e0e0e);
			m_scrollView.setVerticalScrollBarEnabled(false);
			FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
			m_scrollView.setLayoutParams(fl);
			MasterFrameLayout.this.addView(m_scrollView, 0);
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent ev)
		{
			boolean handle = super.dispatchTouchEvent(ev);
			switch(ev.getAction())
			{
				case MotionEvent.ACTION_DOWN:
				{
					m_downY = ev.getY();
					m_topPadding = getPaddingTop();
					break;
				}
				case MotionEvent.ACTION_MOVE:
				{
					float deltaY = ev.getY() - m_downY;
					if(deltaY > 1 && m_scrollView.getScrollY() == 0)
					{
						handle = false;
						int padding = (int)(deltaY / 2.5f);
						PullPadding((int)(deltaY / 2.5f));

						float alpha = Math.abs(m_maxAlpha - padding) / (float)m_maxAlpha;
						setAlpha(alpha);
					}
					break;
				}
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
				{
					float deltaY = (ev.getY() - m_downY) / 2.5f;
					if(deltaY > m_max && m_scrollView.getScrollY() == 0 && m_uiEnabled)
					{
						handle = false;
						onBack();
					}
					else
					{
						setAlpha(1.0f);
						if(getPaddingTop() > 0)
						{
							handle = false;
							resetPadding();
						}
					}
					break;
				}
			}
			return handle;
		}

		private void PullPadding(int delta)
		{
			int padding = m_topPadding + delta;

			if(padding < 0)
			{
				padding = 0;
			}
			setPadding(0, padding, 0, 0);
		}

		private void resetPadding()
		{
			if(m_translateAnim != null)
			{
				m_translateAnim.cancel();
				m_translateAnim.removeAllUpdateListeners();
			}
			m_scrollView.scrollTo(0, 0);
			m_translateAnim = ObjectAnimator.ofInt(getPaddingTop(), 0);
			m_translateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(ValueAnimator animation)
				{
					setPadding(0, (int)animation.getAnimatedValue(), 0, 0);
					if((int)animation.getAnimatedValue() == 0)
					{
						m_translateAnim.cancel();
						m_translateAnim.removeAllUpdateListeners();
					}
				}
			});
			m_translateAnim.setDuration(300);
			m_translateAnim.start();
		}
	}
}
