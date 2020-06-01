package cn.poco.bootimg;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.adMaster.AdMaster;
import cn.poco.adMaster.BootImgRes;
import cn.poco.banner.BannerCore3;
import cn.poco.bootimg.site.BootImgPageSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.home.GifImageView;
import cn.poco.interphoto2.R;
import cn.poco.system.ConfigIni;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.AnimationView;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.FileUtil;
import cn.poco.utils.Utils;

public class BootImgPage extends IPage
{
	protected BootImgPageSite m_site;

	protected boolean m_uiEnabled;
	protected BootImgRes m_res;
	protected Bitmap m_bmp;
	protected Bitmap m_marketLogo;
	protected Matrix m_marketLogoMatrix;
	protected boolean m_openMyWeb = false;
	protected boolean m_openWeb = false;
	protected boolean m_playVideo = false;
	protected AnimationView mView;
	protected GifImageView m_gifView;
	protected BootVideoView m_videoView;
	private FrameLayout m_bottomBar;
	private SkipBtn m_skipBtn;

	public BootImgPage(Context context, BaseSite site)
	{
		super(context, site);

		m_site = (BootImgPageSite)site;
		InitData();
		InitUI();
	}

	protected void InitData()
	{
		ShareData.InitData((Activity)getContext());

		this.setWillNotDraw(false);

		m_uiEnabled = true;
	}

	protected void InitUI()
	{
		ShareData.InitData((Activity)getContext());

		this.setBackgroundColor(0xFF0E0E0E);

		this.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				try
				{
					if(m_uiEnabled && m_res != null && m_res.mClick != null && m_res.mClick.length() > 0)
					{
						Utils.SendTj(getContext(), m_res.mClickTjs);
						BannerCore3.ExecuteCommand(getContext(), m_res.mClick, new BannerCore3.CmdCallback()
						{
							@Override
							public void OpenPage(Context context, int code, String... args)
							{

							}

							@Override
							public void OpenWebPage(Context context, String... args)
							{
								m_openWeb = true;
								String url;
								if(args != null && args.length > 0 && (url = args[0]) != null)
								{
									String myUrl = Utils.PocoDecodeUrl(context, url);
									CommonUtils.OpenBrowser(context, myUrl);
								}
							}

							@Override
							public void OpenMyWebPage(Context context, String... args)
							{
								m_openMyWeb = true;
								String url;
								if(args != null && args.length > 0 && (url = args[0]) != null)
								{
									String myUrl = Utils.PocoDecodeUrl(context, url);
									m_site.OnMyWeb(myUrl,getContext());
								}
							}

							@Override
							public void OpenPocoCamera(Context context, String... args)
							{

							}

							@Override
							public void OpenPocoMix(Context context, String... args)
							{

							}

							@Override
							public void OpenJane(Context context, String... args)
							{

							}

							@Override
							public void OpenBeautyCamera(Context context, String... args)
							{

							}

							@Override
							public void OpenBusinessPage(Context context, String... args)
							{

							}
						});
					}
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	protected Matrix temp_matrix = new Matrix();
	protected Paint temp_paint = new Paint();

	@Override
	protected void onDraw(Canvas canvas)
	{
		if(m_bmp != null && !m_bmp.isRecycled())
		{
			temp_matrix.reset();
			float scale = (float)this.getWidth() / (float)m_bmp.getWidth();
			//System.out.println("scale : " + scale);
			temp_matrix.postScale(scale, scale);
			temp_paint.reset();
			temp_paint.setFilterBitmap(true);
			temp_paint.setAntiAlias(true);
			canvas.drawBitmap(m_bmp, temp_matrix, temp_paint);
		}
		else if(m_marketLogo != null && !m_marketLogo.isRecycled())
		{
			canvas.drawBitmap(m_marketLogo, m_marketLogoMatrix, temp_paint);
		}
	}

	/**
	 * img:BootImgRes
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		int res = 0;
		String ver = SysConfig.GetAppVer(getContext());
		if(ver != null && ConfigIni.showChannelLogo)
		{
			if(ver.endsWith("_r3"))
			{
				res = R.drawable.main_welcome_logo_r3;
			}
			else if(ver.endsWith("_r10"))
			{
				res = R.drawable.main_welcome_logo_r10;
			}
			else if(ver.endsWith("_r12"))
			{
				res = R.drawable.main_welcome_logo_r12;
			}
			else if(ver.endsWith("_r18"))
			{
				res = R.drawable.main_welcome_logo_union_r18;
			}
			else if(ver.endsWith("_r19"))
			{
				res = R.drawable.main_welcome_logo_r19;
			}
			else if(ver.endsWith("_r20"))
			{
				res = R.drawable.main_welcome_logo_r20;
			}
			else if(ver.endsWith("_r31"))
			{
				res = R.drawable.main_welcome_logo_r31;
			}
			else if(ver.endsWith("_r33"))
			{
				res = R.drawable.main_welcome_logo_r33;
			}
			else if(ver.endsWith("_r34"))
			{
				res = R.drawable.main_welcome_logo_r34;
			}
			else if(ver.endsWith("_r35"))
			{
				res = R.drawable.main_welcome_logo_r35;
			}
			else if(ver.endsWith("_r39"))
			{
				res = R.drawable.main_welcome_logo_r39;
			}
			else if(ver.endsWith("_r40"))
			{
				res = R.drawable.main_welcome_logo_r40;
			}
		}
		if(res != 0)
		{
			m_marketLogo = Utils.DecodeImage(getContext(), res, 0, -1, -1, -1);
			if(m_marketLogo != null)
			{
				float scale = ShareData.m_screenWidth / 1080f;
				//int w = (int)(m_marketLogo.getWidth() * scale + 0.5f);
				//int h = (int)(m_marketLogo.getHeight() * scale + 0.5f);
				int cx = ShareData.m_screenWidth / 2; //logo中心点
				int cy = ShareData.m_screenHeight; //logo中心点
				int ih = ShareData.m_screenWidth * 4 / 3; //图片高
				int lh = ShareData.m_screenHeight - ih;
				if(lh > 0)
				{
					cy = ih + lh / 2;
				}
				m_marketLogoMatrix = new Matrix();
				m_marketLogoMatrix.postTranslate(cx - m_marketLogo.getWidth() / 2, cy - m_marketLogo.getHeight() / 2);
				m_marketLogoMatrix.postScale(scale, scale, cx, cy);
			}
		}

		int showTime = 800;
		boolean flag = false;
		boolean isImg = true;
		if(params != null && (m_res = (BootImgRes)params.get("img")) != null && m_res.mAdm != null)
		{
			showTime = m_res.mShowTime;
			Utils.SendTj(getContext(), m_res.mShowTjs);
			if(m_res.mAdm != null)
			{
				this.setBackgroundColor(0xff000000);
				if(m_res.mAdm.length == 1)
				{
					Object my_res = m_res.mAdm[0];
					if(my_res instanceof String)
					{
						String mine_type = FileUtil.getMimeType((String)my_res);
						if("image/gif".equals(mine_type))
						{
							isImg = false;
							showGif((String)my_res);
						}
						else if(mine_type.contains("video/"))
						{
							isImg = false;
							String key = AdMaster.BuildBootVideoStr(m_res);
							int count = Integer.parseInt(TagMgr.GetTagValue(getContext(), key, "1"));
							if(count <= m_res.mPlayTimes)
							{
								m_playVideo = true;
								showVideo((String)my_res);
								count++;
								TagMgr.SetTagValue(getContext(), key, count + "");
							}
							else
							{
								flag = true;
							}
						}
						else
						{
							decodeImg(my_res);
						}
					}
					else
					{
						decodeImg(my_res);
					}
				}
				else if(m_res.mAdm.length > 1)
				{
					mView = new AnimationView(getContext());
					mView.SetGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP);
					int duration = m_res.mShowTime / m_res.mAdm.length;
					ArrayList<AnimationView.AnimFrameData> data = new ArrayList<>();
					for(Object temp : m_res.mAdm)
					{
						data.add(new AnimationView.AnimFrameData(temp, duration, false));
					}
					mView.SetData_nodpi(data, null);
					FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
					this.addView(mView, fl);
					mView.Start();
				}
			}

			if(!flag)
			{
				FrameLayout.LayoutParams fl;
				m_bottomBar = new FrameLayout(getContext());
				m_bottomBar.setBackgroundColor(0xff000000);
				fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(232));
				fl.gravity = Gravity.LEFT | Gravity.BOTTOM;
				this.addView(m_bottomBar, fl);
				{
					ImageView logo = new ImageView(getContext());
					logo.setImageResource(R.drawable.bootimgpage_my_logo);
					fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					fl.gravity = Gravity.CENTER;
					logo.setLayoutParams(fl);
					m_bottomBar.addView(logo);

					///右上角广告提示
					/*if(!isImg){
					}*/
					ImageView mAdTip = new ImageView(getContext());
					mAdTip.setImageResource(R.drawable.bootimgpage_ad_tip);
					fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xxhdpi(48), ShareData.PxToDpi_xxhdpi(27));
					fl.gravity = Gravity.RIGHT | Gravity.TOP;
					fl.rightMargin = ShareData.PxToDpi_xhdpi(6);
					fl.topMargin = ShareData.PxToDpi_xhdpi(6);
					addView(mAdTip, fl);

					//跳过按钮
					m_skipBtn = new SkipBtn(getContext());
					fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					fl.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
					fl.rightMargin = ShareData.PxToDpi_xhdpi(24);
					m_bottomBar.addView(m_skipBtn, fl);
					m_skipBtn.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							OnHome(true);
						}
					});
					m_skipBtn.SetSkipTime(showTime);
				}
			}
			else
			{
				int frW = ShareData.m_screenWidth;
				int frH = (int)(frW * 4 / 3f + 0.5f);
				int logoY = (int)(200 / 570f * frH - ShareData.PxToDpi_xhdpi(30));
				ImageView logo = new ImageView(getContext());
				logo.setImageResource(R.drawable.homepage_logo);
				FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
				fl.topMargin = logoY;
				this.addView(logo, fl);
				if(android.os.Build.VERSION.SDK_INT >= 17)
				{
					AlphaAnimation aa = new AlphaAnimation(0, 1);
					aa.setDuration(800);
					logo.startAnimation(aa);
				}
			}
		}
		else
		{
			int frW = ShareData.m_screenWidth;
			int frH = (int)(frW * 4 / 3f + 0.5f);
			int logoY = (int)(200 / 570f * frH - ShareData.PxToDpi_xhdpi(30));
			LinearLayout logoFr = new LinearLayout(getContext());
			logoFr.setOrientation(LinearLayout.VERTICAL);
			FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
			fl.topMargin = logoY;
			logoFr.setLayoutParams(fl);
			this.addView(logoFr);
			{
				ImageView logo = new ImageView(getContext());
				logo.setImageResource(R.drawable.homepage_logo);
				LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				logo.setLayoutParams(ll);
				logoFr.addView(logo);

				View tipLine = new View(getContext());
				tipLine.setBackgroundColor(0xffffc433);
				ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(30), ShareData.PxToDpi_xhdpi(6));
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				ll.topMargin = ShareData.PxToDpi_xhdpi(30);
				tipLine.setLayoutParams(ll);
				logoFr.addView(tipLine);

				TextView text = new TextView(getContext());
				text.setText("印象");
				text.setTextColor(Color.WHITE);
				text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
				text.getPaint().setFakeBoldText(true);
				ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				ll.topMargin = ShareData.PxToDpi_xhdpi(20);
				text.setLayoutParams(ll);
				logoFr.addView(text);
			}
			if(android.os.Build.VERSION.SDK_INT >= 17)
			{
				AlphaAnimation aa = new AlphaAnimation(0, 1);
				aa.setDuration(800);
				logoFr.startAnimation(aa);
			}
		}

		this.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if(!m_openMyWeb && !m_playVideo)
				{
					OnHome(true);
				}
			}
		}, showTime);
	}



	private void decodeImg(Object res)
	{
		Bitmap temp = Utils.DecodeImage(getContext(), res, 0, -1, ShareData.m_screenWidth, ShareData.m_screenHeight);
		if(temp != null)
		{
			if(temp.getWidth() > ShareData.m_screenWidth && temp.getHeight() > ShareData.m_screenHeight)
			{
				m_bmp = MakeBmp.CreateBitmap(temp, ShareData.m_screenWidth, ShareData.m_screenHeight, -1, 0, Bitmap.Config.ARGB_8888);
			}
			else
			{
				m_bmp = temp;
			}
			if(m_bmp != temp)
			{
				temp.recycle();
				temp = null;
			}
		}
	}

	protected boolean m_isHideVirtualBar = true;

	/*@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		super.onLayout(changed, left, top, right, bottom);
		if (Build.VERSION.SDK_INT > JELLY_BEAN_MR2) {
			int virtualBarHeight = BootImgPage.this.getRootView().getHeight() - BootImgPage.this.getHeight();
			if (virtualBarHeight >= 50 && !m_isHideVirtualBar) {
				m_isHideVirtualBar = true;
				onLayoutAdViewFr();
			} else if (virtualBarHeight < 50 && m_isHideVirtualBar) {
				m_isHideVirtualBar = false;
				onLayoutAdViewFr();
			}
		}
	}*/

	private void onLayoutAdViewFr()
	{
		int height = BootImgPage.this.getHeight() - ShareData.PxToDpi_xhdpi(232);
		if(m_gifView != null){

			FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams)m_gifView.getLayoutParams();
			fl.height = height;
			m_gifView.setLayoutParams(fl);
		}
		if(m_videoFr != null){
			FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams)m_videoFr.getLayoutParams();
			fl.height = height;
			m_videoFr.setLayoutParams(fl);
		}

	}

	private void showGif(String res)
	{
		m_gifView = new GifImageView(getContext());
		m_gifView.setScaleType(ImageView.ScaleType.FIT_XY);
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.m_screenRealHeight - ShareData.PxToDpi_xhdpi(232));
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		this.addView(m_gifView, fl);

		m_gifView.setAutoPlay(true);
		m_gifView.setCanLoop(false);
		m_gifView.SetImageRes(res);
	}

	private FrameLayout m_videoFr;
	private int showVideo(String res)
	{
		m_videoFr = new FrameLayout(getContext());
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.m_screenRealHeight - ShareData.PxToDpi_xhdpi(232));
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		this.addView(m_videoFr, fl);

		m_videoView = new BootVideoView(getContext());
		fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		fl.gravity = Gravity.CENTER;
		m_videoView.setLayoutParams(fl);
		m_videoFr.addView(m_videoView);

		m_videoView.setVideoPath(res);
		m_videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
		{
			@Override
			public void onPrepared(MediaPlayer mp)
			{
				int height = mp.getVideoHeight();
				int width = mp.getVideoWidth();
				m_videoView.SetRatioSize(width, height);
				m_videoView.start();
			}
		});
		m_videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				OnHome(true);
			}
		});

		return m_videoView.getDuration();
	}

	protected void OnHome(boolean hasAnim)
	{
		if(m_uiEnabled)
		{
			m_site.OnHome(hasAnim,getContext());
			m_uiEnabled = false;
		}
	}

	@Override
	public void onBack()
	{
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(m_playVideo && m_openWeb)
		{
			OnHome(false);
		}
	}

	@Override
	public void onClose()
	{
		if(m_bmp != null)
		{
			m_bmp.recycle();
			m_bmp = null;
		}

		if(mView != null)
		{
			mView.ClearAll();
			mView = null;
		}
		if(m_videoView != null)
		{
			m_videoView.setVisibility(GONE);
			m_videoView.stopPlayback();
			m_videoView = null;
		}
		super.onClose();
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params)
	{
		super.onPageResult(siteID, params);

		if(m_openMyWeb)
		{
			OnHome(false);
		}
	}
}
