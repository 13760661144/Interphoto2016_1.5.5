package cn.poco.MaterialMgr2;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tencent.mm.opensdk.modelbase.BaseResp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.MaterialMgr2.site.ThemeIntroPageSite;
import cn.poco.Text.Painter;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.MyButtons;
import cn.poco.beautify.RecomDisplayMgr;
import cn.poco.beautify.ScrollShareFr;
import cn.poco.blogcore.FacebookBlog;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.FilterRes;
import cn.poco.resource.FilterResMgr2;
import cn.poco.resource.FontRes;
import cn.poco.resource.IDownload;
import cn.poco.resource.LightEffectRes;
import cn.poco.resource.LightEffectResMgr2;
import cn.poco.resource.ResType;
import cn.poco.resource.ResourceMgr;
import cn.poco.resource.TextRes;
import cn.poco.resource.TextResMgr2;
import cn.poco.resource.ThemeRes;
import cn.poco.resource.VideoTextRes;
import cn.poco.resource.VideoTextResMgr2;
import cn.poco.resource.database.ResourseDatabase;
import cn.poco.resource.database.TableNames;
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
import cn.poco.tianutils.ShareData;
import cn.poco.utils.FileUtil;
import cn.poco.utils.GlideImageLoader;
import cn.poco.utils.InterphotoDlg;
import cn.poco.utils.MyImageLoader;
import cn.poco.utils.ScaleEvaluator;
import cn.poco.utils.Utils;

/**
 * Created by admin on 2016/9/9.
 */
public class ThemeIntroPage extends IPage
{
	private static final String TAG = "主题详情页";
	private ThemeIntroPageSite m_site;
	private ThemeItemInfo m_info;
	private ResType m_curPageType;
	private HashMap<Integer, Integer> m_resDownloadIds;	//用来下载素材显示Img的时候，判断该素材是否正在下载
	private ArrayList<BaseRes> m_listData;
	private ScrollShareFr m_mainFr;
	protected MyButtons m_friendBtn;
	protected MyButtons m_weixinBtn;
	protected MyButtons m_sinaBtn;
	protected MyButtons m_qqBtn;
	protected MyButtons m_facebookBtn;
	protected ImageView m_shareHideBtn;
	protected ProgressBar m_progressBar;
	protected LinearLayout m_stateBtn;
	protected ImageView m_stateImg;
	protected TextView m_stateText;

	protected ShareTools m_shareTools;
	protected ProgressDialog mProgressDialogQQ;
	protected String m_shareUrl;
	protected Object m_shareImg;
	protected String m_shareTitle;
	protected String m_shareContent;

	private ListView m_list;
	private ListAdapter m_adapter;
	private ImageView m_backBtn;
	private ImageView m_shareBtn;
	private int m_headW;
	private int m_headH;
	protected int m_shareFrHeight;
	private AlertDialog m_checkDlg;
	private AlertDialog m_shareTip;
	private InterphotoDlg m_wifiTip;
	private boolean m_gprsContinue = false;

	protected Toast m_toast;
	protected boolean m_exit = false;
	private boolean m_needRefresh = false;
	protected boolean m_showOnKey = false;
	private Bitmap m_bkBmp;
	private InterphotoDlg m_restoreDlg;

	private boolean m_hasAnim = false;
	private int m_centerX;
	private int m_centerY;
	private int m_viewH;
	private int m_viewW;
	private IntroPageCallback m_pageCB;
	private ImageView m_animView;
	private boolean m_uiEnabled = true;
	private ArrayList<MyBeautyStat.MgrInfo1> m_mgrInfo;
	int video_typeID = 1;
	public ThemeIntroPage(Context context, BaseSite site)
	{
		super(context, site);
		m_site = (ThemeIntroPageSite)site;

		m_shareFrHeight = ShareData.PxToDpi_xhdpi(242);
		m_headW = ShareData.PxToDpi_xhdpi(120);
		m_headH = m_headW;
		m_resDownloadIds = new HashMap<>();

		InitUI();

		TongJiUtils.onPageStart(getContext(), TAG);
	}

	/**
	 * data ThemeItemInfo
	 * @param params
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		if(params != null)
		{
			Object o = params.get("data");
			if(o != null)
			{
				m_info = (ThemeItemInfo)o;
			}
			o = params.get("hasAnim");
			if(o != null)
			{
				m_hasAnim = (boolean)o;
			}
			o = params.get("centerX");
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
			m_viewW = ShareData.m_screenWidth;
			o = params.get("viewW");
			if(o != null)
			{
				m_viewW = (Integer)o;
			}
		}

		if(m_info != null)
		{
			switch(m_info.m_type)
			{
				case FILTER:
					TongJi2.AddCountByRes(getContext(), R.integer.素材商店_滤镜主题详情页);


					MyBeautyStat.onClickByRes(R.string.滤镜主题_打开滤镜主题详情页);
					break;
				case LIGHT_EFFECT:
					TongJi2.AddCountByRes(getContext(), R.integer.素材商店_光效主题详情页);
					MyBeautyStat.onClickByRes(R.string.光效主题_打开光效主题详情页);
					break;
			}
			m_curPageType = m_info.m_type;
			if(m_info.m_resArr == null || m_info.m_resArr.size() == 0)
			{
				m_info = MgrUtils.GetThemeItemInfoByUri(getContext(), m_info.m_uri, m_info.m_type);
			}
			if(m_info.m_resArr != null && m_info.m_resArr.size() > 0)
			{
				BaseRes temp = m_info.m_resArr.get(0);

				if(temp instanceof TextRes)
				{
					if(((TextRes) temp).m_resTypeID == 1)
					{

						m_curPageType = ResType.TEXT_WATERMARK;
						MyBeautyStat.onClickByRes(R.string.照片水印主题_打开照片水印主题详情页);
					}
					else
					{
						Log.i(TAG, "SetData: " + ((TextRes) temp).m_resTypeName);
						m_curPageType = ResType.TEXT_ATTITUTE;
						MyBeautyStat.onClickByRes(R.string.照片态度主题_打开照片态度主题详情页);
					}
				}
				if(temp instanceof VideoTextRes)
				{

					if (((VideoTextRes) temp).m_resTypeID == 1){
						m_curPageType = ResType.VIEDO_WATERMARK;
						Log.i(TAG, "SetData: " + m_curPageType);
						MyBeautyStat.onClickByRes(R.string.视频水印主题_打开视频水印主题详情页);
					}else {
						m_curPageType = ResType.VIEDO_ORIGINALITY;
						Log.i(TAG, "SetData: " + m_curPageType);
						MyBeautyStat.onClickByRes(R.string.视频创意主题_打开视频创意主题详情页);
					}
				}

			}
			m_listData = new ArrayList<>();
			m_listData.add(m_info.m_themeRes);
			m_listData.add(m_info.m_themeRes);
			m_listData.addAll(m_info.m_resArr);

			m_adapter = new ListAdapter();
			m_list.setAdapter(m_adapter);
			m_adapter.notifyDataSetChanged();

			int orgState = m_info.m_state;
			m_info.m_state = MgrUtils.checkGroupDownloadState(m_info.m_resArr, m_info.m_idArr, null);
			m_info.m_progress = 100 * MgrUtils.getM_completeCount() / m_info.m_idArr.length;
			if(m_info.m_state == ThemeItemInfo.LOADING)
			{
				DownloadRes();
			}
			if(orgState != m_info.m_state && m_info.m_state == ThemeItemInfo.COMPLETE)
			{
				m_needRefresh = true;
			}

			setBtnState(m_info);

			MakeShareInfos();

			if(m_hasAnim)
			{
				m_uiEnabled = true;
				startAnim();
			}
		}
	}

	private void startAnim()
	{
		if(!m_uiEnabled)
			return;
		m_uiEnabled = false;
		Bitmap bmp = MyImageLoader.MakeBmp2(getContext(), m_info.m_themeRes.m_icon,
										   ShareData.m_screenWidth, ShareData.m_screenWidth);
		m_animView.setImageBitmap(bmp);
		m_animView.setScaleType(ImageView.ScaleType.CENTER);

		m_animView.setVisibility(VISIBLE);
		m_animView.clearAnimation();
		m_mainFr.GetMainFr().setAlpha(0);

		int w = ShareData.m_screenWidth;
		int endW = w;
		if(m_viewW < w)
		{
			endW = w + 50;
			m_animView.setScaleType(ImageView.ScaleType.FIT_XY);
		}
		ValueAnimator scale;
		if(endW > w)
		{
			scale = ValueAnimator.ofObject(new ScaleEvaluator(), new Point(m_viewW, m_viewH),
										   new Point(endW, endW), new Point(w, w));
			scale.setDuration(550);
		}
		else
		{
			scale = ValueAnimator.ofObject(new ScaleEvaluator(), new Point(m_viewW, m_viewH),
										   new Point(endW, endW));
			scale.setDuration(350);
		}
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

		int centerX = m_centerX - (ShareData.m_screenWidth - m_viewW) / 2;
		ValueAnimator trans;
		if(endW > w)
		{
			trans = ValueAnimator.ofObject(new ScaleEvaluator(),
										   new Point(centerX, m_centerY), new Point(0, -50), new Point(0, 0));
		}
		else
		{
			trans = ValueAnimator.ofObject(new ScaleEvaluator(),
										   new Point(centerX, m_centerY), new Point(0, 0));
		}
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
				m_mainFr.GetMainFr().setAlpha(animatorValue);
			}
		});
		alpha.setStartDelay(200);
		alpha.setDuration(150);

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
				m_animView.clearAnimation();
				m_animView.setVisibility(GONE);
				m_uiEnabled = true;
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

	private void closeAnim()
	{
		if(!m_uiEnabled)
			return;
		m_uiEnabled = false;
		m_animView.setVisibility(VISIBLE);
		m_animView.clearAnimation();
		m_mainFr.GetMainFr().setVisibility(GONE);

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
		ValueAnimator trans = ValueAnimator.ofObject(new ScaleEvaluator(), new Point(0, 0),
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
				onBack1();
				ThemeIntroPage.this.setVisibility(View.GONE);
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

	private void InitUI()
	{
		FrameLayout.LayoutParams fl;
		m_mainFr = new ScrollShareFr(this, m_shareFrHeight);
		m_mainFr.setOnCloseListener(new ScrollShareFr.OnCloseListener()
		{
			@Override
			public void onClose()
			{
				if(m_btnListener != null)
				{
					m_btnListener.onClick(m_shareHideBtn);
				}
			}
		});

		InitShareFr();

		m_list = new ListView(getContext());
		m_list.setBackgroundColor(0xff0e0e0e);
		m_list.setDividerHeight(0);
		m_list.setVerticalScrollBarEnabled(false);
		m_list.setCacheColorHint(0x00000000);
		fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		fl.bottomMargin = ShareData.PxToDpi_xhdpi(98);
		m_list.setLayoutParams(fl);
		m_mainFr.AddMainChild(m_list);

		m_backBtn = new ImageView(getContext());
		m_backBtn.setImageResource(R.drawable.beauty_master_filter_tip_back_btn);
		m_backBtn.setOnClickListener(m_btnListener);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.TOP | Gravity.LEFT;
		fl.topMargin = ShareData.PxToDpi_xhdpi(30);
		fl.leftMargin = fl.topMargin;
		m_backBtn.setLayoutParams(fl);
		m_mainFr.AddMainChild(m_backBtn);

		m_shareBtn = new ImageView(getContext());
		m_shareBtn.setVisibility(View.GONE);
		m_shareBtn.setImageResource(R.drawable.beauty_master_filter_tip_share_btn);
		m_shareBtn.setOnClickListener(m_btnListener);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.TOP | Gravity.RIGHT;
		fl.topMargin = ShareData.PxToDpi_xhdpi(30);
		fl.rightMargin = fl.topMargin;
		m_shareBtn.setLayoutParams(fl);
		m_mainFr.AddMainChild(m_shareBtn);

		m_progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
		m_progressBar.setProgressDrawable(getContext().getResources().getDrawable(R.drawable.themeintro_progress));
		m_progressBar.setProgress(100);
		m_progressBar.setMax(100);
		fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(98));
		fl.gravity = Gravity.BOTTOM;
		m_progressBar.setLayoutParams(fl);
		m_mainFr.AddMainChild(m_progressBar);

		m_stateBtn = new LinearLayout(getContext());
//		m_stateBtn.setVisibility(View.GONE);
		m_stateBtn.setGravity(Gravity.CENTER);
//		m_stateBtn.setBackgroundColor(0xffffc433);
		fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(98));
		fl.gravity = Gravity.BOTTOM;
		m_stateBtn.setLayoutParams(fl);
		m_stateBtn.setOnClickListener(m_btnListener);
		m_mainFr.AddMainChild(m_stateBtn);
		{
			m_stateImg = new ImageView(getContext());
			m_stateImg.setImageResource(R.drawable.master_share_friend);
			LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_stateImg.setLayoutParams(ll);
			m_stateBtn.addView(m_stateImg);

			m_stateText = new TextView(getContext());
			m_stateText.setTextColor(0xffffffff);
			m_stateText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			TextPaint tp = m_stateText.getPaint();
			tp.setFakeBoldText(true);
			m_stateText.setText(getResources().getString(R.string.mgr_unlock));
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			ll.leftMargin = ShareData.PxToDpi_xhdpi(20);
			m_stateText.setLayoutParams(ll);
			m_stateBtn.addView(m_stateText);
		}

		m_animView = new ImageView(getContext());
		fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.m_screenWidth);
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		m_animView.setLayoutParams(fl);
		this.addView(m_animView);

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(getResources().getString(R.string.downloadFailed));
		builder.setMessage(getResources().getString(R.string.Ooops));
		builder.setPositiveButton(getResources().getString(R.string.Isee), new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				if(m_checkDlg != null)
				{
					m_checkDlg.dismiss();
				}
			}
		});
		m_checkDlg = builder.create();

		builder = new AlertDialog.Builder(getContext());
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

		m_wifiTip = new InterphotoDlg((Activity)getContext(), R.style.waitDialog);
		m_wifiTip.SetTitle(getResources().getString(R.string.wifi_msg));
		m_wifiTip.SetNegativeBtnText(getResources().getString(R.string.Cancel));
		m_wifiTip.SetPositiveBtnText(getResources().getString(R.string.download_continue));
		m_wifiTip.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback()
		{
			@Override
			public void onOK()
			{
				m_gprsContinue = true;
				if(m_wifiTip != null)
				{
					m_wifiTip.dismiss();
				}
				m_btnListener.onClick(m_stateBtn);
			}

			@Override
			public void onCancel()
			{
				m_gprsContinue = false;
				if(m_wifiTip != null)
				{
					m_wifiTip.dismiss();
				}
			}
		});
	}

	private void MakeShareInfos()
	{
		if(m_info != null && m_info.m_themeRes != null)
		{
			m_shareImg = m_info.m_themeRes.m_icon;
			m_shareUrl = m_info.m_themeRes.m_shareUrl;
			m_shareTitle = m_info.m_themeRes.m_shareTitle;
		}
	}

	private View.OnClickListener m_btnListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v == m_backBtn)
			{
				onBack();
			}
			else if(v == m_shareBtn)
			{
				if (m_bkBmp == null)
				{
					Bitmap bmp = Bitmap.createBitmap(ThemeIntroPage.this.getWidth(), ThemeIntroPage.this.getHeight(), Bitmap.Config.ARGB_8888);
					Canvas canvas = new Canvas(bmp);
					ThemeIntroPage.this.draw(canvas);
					m_bkBmp = BeautifyResMgr.MakeBkBmp(bmp, ShareData.m_screenWidth, ShareData.m_screenHeight, 0x99000000, 0x28000000);
					if(bmp != null && bmp != m_bkBmp)
					{
						bmp.recycle();
					}
				}
				m_mainFr.SetMaskBk(m_bkBmp);
				m_mainFr.ShowTopBar(true);

				Log.i(TAG, "onClick: " + m_info.m_type);
				switch(m_info.m_type)
				{
					case FILTER:
						TongJi2.AddCountByRes(getContext(), R.integer.素材商店_滤镜主题详情页_分享按钮);
						MyBeautyStat.onClickByRes(R.string.滤镜主题详情_分享);
						break;
					case TEXT:
					case AUDIO_TEXT:
						TongJi2.AddCountByRes(getContext(), R.integer.素材商店_文字主题详情页_分享按钮);
						MyBeautyStat.onClickByRes(R.string.文字主题详情_分享);

						break;
					case LIGHT_EFFECT:
						TongJi2.AddCountByRes(getContext(), R.integer.素材商店_光效主题详情页_分享按钮);
						MyBeautyStat.onClickByRes(R.string.光效主题详情_分享);
						break;
				}
			}else if(v == m_shareHideBtn)
			{
				m_mainFr.ShowTopBar(false);
			}
			else if(v == m_shareBtn)
			{
				m_mainFr.ShowTopBar(true);
			}
			else if(v == m_friendBtn)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.分享_微信朋友圈);
				m_shareTools.sendUrlToWeiXin(Utils.MakeLogo(getContext(), m_shareImg), m_shareUrl, m_shareTitle, "", false, new ShareTools.SendCompletedListener()
				{
					@Override
					public void result(int result)
					{
						if(result == ShareTools.SUCCESS)
						{
							switch(m_info.m_type)
							{
								case FILTER:

									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.朋友圈,R.string.滤镜主题详情);
									break;
								case LIGHT_EFFECT:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.朋友圈,R.string.光效主题详情);

									break;
							}
							Log.i(TAG, "result: " + m_curPageType);
							switch (m_curPageType){
								case TEXT_WATERMARK:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.朋友圈,R.string.照片水印主题详情);
									break;
								case  TEXT_ATTITUTE:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.朋友圈,R.string.照片态度主题详情);
									break;
								case VIEDO_WATERMARK:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.朋友圈,R.string.视频水印主题详情);
									break;
								case VIEDO_ORIGINALITY:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.朋友圈,R.string.视频创意主题详情);
									break;
							}
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
			else if(v == m_sinaBtn)
			{
				if(SettingPage.checkSinaBindingStatus(getContext()))
				{
					TongJi2.AddCountByRes(getContext(),R.integer.分享_新浪微博);
					m_shareTools.sendToSina(Utils.MakeLogo(getContext(), m_shareImg), m_shareTitle + m_shareUrl, new ShareTools.SendCompletedListener()
					{
						@Override
						public void result(int result)
						{
							if(result == ShareTools.SUCCESS)
							{
								ShareTools.ToastSuccess(getContext());
								switch(m_info.m_type)
								{
									case FILTER:

										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.滤镜主题详情);
										break;
									case LIGHT_EFFECT:
										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.光效主题详情);

										break;
								}
								switch (m_curPageType){
									case TEXT_WATERMARK:
										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.照片水印主题详情);
										break;
									case  TEXT_ATTITUTE:
										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.照片态度主题详情);
										break;
									case VIEDO_WATERMARK:
										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.视频水印主题详情);
										break;
									case VIEDO_ORIGINALITY:
										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.视频创意主题详情);
										break;
								}
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
							m_shareTools.sendToSina(Utils.MakeLogo(getContext(), m_shareImg), m_shareTitle + m_shareUrl, new ShareTools.SendCompletedListener()
							{
								@Override
								public void result(int result)
								{
									if(result == ShareTools.SUCCESS)
									{
										ShareTools.ToastSuccess(getContext());
										switch(m_info.m_type)
										{
											case FILTER:

												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.滤镜主题详情);
												break;
											case LIGHT_EFFECT:
												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.光效主题详情);

												break;
										}
										switch (m_curPageType){
											case TEXT_WATERMARK:
												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.照片水印主题详情);
												break;
											case  TEXT_ATTITUTE:
												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.照片态度主题详情);
												break;
											case VIEDO_WATERMARK:
												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.视频水印主题详情);
												break;
											case VIEDO_ORIGINALITY:
												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博,R.string.视频创意主题详情);
												break;
										}
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
			else if(v == m_qqBtn)
			{
				if(SettingPage.checkQzoneBindingStatus(getContext()))
				{
					TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ空间);
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
							if (result == ShareTools.SUCCESS){
								switch(m_info.m_type)
								{
									case FILTER:

										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.滤镜主题详情);
										break;
									case LIGHT_EFFECT:
										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.光效主题详情);

										break;
								}
								switch (m_curPageType){
									case TEXT_WATERMARK:
										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.照片水印主题详情);
										break;
									case  TEXT_ATTITUTE:
										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.照片态度主题详情);
										break;
									case VIEDO_WATERMARK:
										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.视频水印主题详情);
										break;
									case VIEDO_ORIGINALITY:
										MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.视频创意主题详情);
										break;
								}
							}
						}
					});
				}
				else
				{
					mProgressDialogQQ = ProgressDialog.show(getContext(), "", getContext().getResources().getString(R.string.Linking));
					mProgressDialogQQ.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					m_shareTools.bindQzone(false, new SharePage.BindCompleteListener()
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
									if (result ==  ShareTools.SUCCESS){
										switch(m_info.m_type)
										{
											case FILTER:

												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.滤镜主题详情);
												break;
											case LIGHT_EFFECT:
												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.光效主题详情);

												break;
										}
										switch (m_curPageType){
											case TEXT_WATERMARK:
												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.照片水印主题详情);
												break;
											case  TEXT_ATTITUTE:
												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.照片态度主题详情);
												break;
											case VIEDO_WATERMARK:
												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.视频水印主题详情);
												break;
											case VIEDO_ORIGINALITY:
												MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间,R.string.视频创意主题详情);
												break;
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
			else if(v == m_facebookBtn)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.分享_Facebook);
				String detail = m_info.m_themeRes.m_name + " | " + ((ThemeRes)m_info.m_themeRes).m_detail + "\n";
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
						if (result == ShareTools.SUCCESS){
							switch(m_info.m_type)
							{
								case FILTER:

									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.facebook,R.string.滤镜主题详情);
									break;
								case LIGHT_EFFECT:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.facebook,R.string.光效主题详情);

									break;
							}
							switch (m_curPageType){
								case TEXT_WATERMARK:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.facebook,R.string.照片水印主题详情);
									break;
								case  TEXT_ATTITUTE:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.facebook,R.string.照片态度主题详情);
									break;
								case VIEDO_WATERMARK:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.facebook,R.string.视频水印主题详情);
									break;
								case VIEDO_ORIGINALITY:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.facebook,R.string.视频创意主题详情);
									break;
							}
						}
					}
				});

			}
			else if(v == m_weixinBtn)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.分享_微信好友);
				m_shareTools.sendUrlToWeiXin(Utils.MakeLogo(getContext(), m_shareImg), m_shareUrl, m_shareTitle, "", true, new ShareTools.SendCompletedListener()
				{
					@Override
					public void result(int result)
					{
						if(result == ShareTools.SUCCESS)
						{
							ShareTools.ToastSuccess(getContext());
							switch(m_info.m_type)
							{
								case FILTER:

									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微信好友,R.string.滤镜主题详情);
									break;
								case LIGHT_EFFECT:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微信好友,R.string.光效主题详情);

									break;
							}
							switch (m_curPageType){
								case TEXT_WATERMARK:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微信好友,R.string.照片水印主题详情);
									break;
								case  TEXT_ATTITUTE:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微信好友,R.string.照片态度主题详情);
									break;
								case VIEDO_WATERMARK:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微信好友,R.string.视频水印主题详情);
									break;
								case VIEDO_ORIGINALITY:
									MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微信好友,R.string.视频创意主题详情);
									break;
							}


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
			else if(v == m_stateBtn)
			{
				if(m_info != null)
				{
					final ThemeRes res = m_info.m_themeRes;

					if(m_info.m_lock)
					{
						if(res != null)
						{
							SharePage.unlockResourceByWeiXin(getContext(), m_shareTitle, m_shareContent, m_shareUrl,
															 RecomDisplayMgr.MakeWXLogo(Utils.MakeLogo(getContext(), m_shareImg)), false, new SendWXAPI.WXCallListener()
							{
								@Override
								public void onCallFinish(int result)
								{
									if(!m_exit && result != BaseResp.ErrCode.ERR_USER_CANCEL)
									{
										m_needRefresh = true;
										TagMgr.SetTag(getContext(), Tags.THEME_UNLOCK + m_info.m_uri);
										if(m_shareUrl != null && m_shareUrl.length() > 0)
										{
											m_shareBtn.setVisibility(View.VISIBLE);
										}
										if(m_info != null)
										{
											m_info.m_lock = false;
										}
										setBtnState(m_info);
										if(m_toast == null)
										{
											m_toast = Toast.makeText(getContext(), getResources().getString(R.string.UnlockSuccessful), Toast.LENGTH_SHORT);

											//统计分享素材ID
											switch(m_info.m_type)
											{
												case FILTER:
													MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.滤镜, MyBeautyStat.ButtonType.朋友圈解锁,true, String.valueOf(res.m_tjId));
													break;
												case LIGHT_EFFECT:
													MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.光效, MyBeautyStat.ButtonType.朋友圈解锁,true, String.valueOf(res.m_tjId));
													break;
											}
											switch (m_curPageType){
												case TEXT_WATERMARK:
													MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.照片水印, MyBeautyStat.ButtonType.朋友圈解锁,true, String.valueOf(res.m_tjId));
													break;
												case  TEXT_ATTITUTE:
													MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.照片态度, MyBeautyStat.ButtonType.朋友圈解锁,true, String.valueOf(res.m_tjId));
													break;
												case VIEDO_WATERMARK:
													MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.视频水印, MyBeautyStat.ButtonType.朋友圈解锁,true, String.valueOf(res.m_tjId));
													break;
												case VIEDO_ORIGINALITY:

													MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.视频创意, MyBeautyStat.ButtonType.朋友圈解锁,true, String.valueOf(res.m_tjId));

													break;
											}
										}
										else
										{
											m_toast.setText(getResources().getString(R.string.UnlockSuccessful));
										}
										m_toast.show();
										onClick(m_stateBtn);
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
							else
							{
								m_toast.setText(getResources().getString(R.string.unlockFailed));
							}
							m_toast.show();
						}

					}
					else
					{
						switch(m_info.m_state)
						{
							case ThemeItemInfo.PREPARE:
							case ThemeItemInfo.CONTINUE:
							{
								TongJi2.AddCountById(m_info.m_themeRes.m_tjId + "");
								if(!Utils.isNetConnected(getContext()))
								{
									if(m_checkDlg != null && !m_checkDlg.isShowing())
									{
										m_checkDlg.show();
									}
									return;
								}
								else
								{
									if(!Utils.isWifiConnected(getContext()) && !m_gprsContinue && GetCurDownloadSize() >= 10)
									{
										if(m_wifiTip != null && !m_wifiTip.isShowing())
										{
											m_wifiTip.show();
										}
										return;
									}
								}
								if (m_info.m_state == ThemeItemInfo.PREPARE){
									switch(m_info.m_type)
									{
										case FILTER:
											TongJi2.AddCountByRes(getContext(), R.integer.素材商店_滤镜主题详情页_立即下载);
											MyBeautyStat.onClickByRes(R.string.滤镜主题详情_立即下载);
											break;
										case LIGHT_EFFECT:
											TongJi2.AddCountByRes(getContext(), R.integer.素材商店_光效主题详情页_立即下载);
											MyBeautyStat.onClickByRes(R.string.光效主题详情_立即下载);
											break;
									}
									switch (m_curPageType){
										case TEXT_WATERMARK:
											MyBeautyStat.onClickByRes(R.string.照片水印主题详情_立即下载);
											break;
										case  TEXT_ATTITUTE:
											MyBeautyStat.onClickByRes(R.string.照片态度主题详情_立即下载);
											break;
										case VIEDO_WATERMARK:
											MyBeautyStat.onClickByRes(R.string.视频水印主题详情_立即下载);
											break;
										case VIEDO_ORIGINALITY:
											MyBeautyStat.onClickByRes(R.string.视频创意主题详情_立即下载);
											break;
									}

								}



								if(m_info.m_state == ThemeItemInfo.CONTINUE){
									switch(m_info.m_type)
									{
										case FILTER:

											MyBeautyStat.onClickByRes(R.string.滤镜主题详情_继续下载);
											break;
										case LIGHT_EFFECT:

											MyBeautyStat.onClickByRes(R.string.光效主题详情_继续下载);
											break;
									}
									switch (m_curPageType){
										case TEXT_WATERMARK:
											MyBeautyStat.onClickByRes(R.string.照片水印主题详情_继续下载);
											break;
										case  TEXT_ATTITUTE:
											MyBeautyStat.onClickByRes(R.string.照片态度主题详情_继续下载);
											break;
										case VIEDO_WATERMARK:
											MyBeautyStat.onClickByRes(R.string.视频水印主题详情_继续下载);
											break;
										case VIEDO_ORIGINALITY:
											MyBeautyStat.onClickByRes(R.string.视频创意主题详情_继续下载);
											break;
									}
								}

								DownloadRes();
								m_info.m_state = ThemeItemInfo.LOADING;
								setBtnState(m_info);
								break;
							}
							case ThemeItemInfo.COMPLETE:
							{
								ThemeRes  info = null;

								if(m_info.m_idArr != null && m_info.m_idArr.length > 0)
								{
									Log.i(TAG, "onClick: " + m_info.m_type);
									int id = GetFirstShowId(m_info.m_idArr, m_info.m_type, m_info.m_themeRes.m_dashiType);
									if(id == -1)
									{
										if(m_restoreDlg != null)
										{
											m_restoreDlg.show();
										}
									}
									else
									{

										switch(m_info.m_type)
										{
											case FILTER:
												TongJi2.AddCountByRes(getContext(), R.integer.素材商店_滤镜主题详情页_立即体验);
												MyBeautyStat.onClickByRes(R.string.滤镜主题详情_立即体验);
												info = m_info.m_themeRes;
												MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.滤镜, MyBeautyStat.ButtonType.立即使用,info.m_isHide, String.valueOf(info.m_tjId));

												break;
											case TEXT:
												TongJi2.AddCountByRes(getContext(), R.integer.素材商店_文字主题详情页_立即体验);
												break;
											case LIGHT_EFFECT:
												TongJi2.AddCountByRes(getContext(), R.integer.素材商店_光效主题详情页_立即体验);
												MyBeautyStat.onClickByRes(R.string.光效主题详情_立即体验);
												info = m_info.m_themeRes;
												MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.光效, MyBeautyStat.ButtonType.立即使用,info.m_isHide, String.valueOf(info.m_tjId));
												break;

										}

										switch (m_curPageType){
											case TEXT_WATERMARK:
												MyBeautyStat.onClickByRes(R.string.照片水印主题详情_立即体验);
												info = m_info.m_themeRes;
												MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.照片水印, MyBeautyStat.ButtonType.立即使用,info.m_isHide, String.valueOf(info.m_tjId));
												break;
											case  TEXT_ATTITUTE:
												MyBeautyStat.onClickByRes(R.string.照片态度主题详情_立即体验);
												info = m_info.m_themeRes;
												MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.照片态度, MyBeautyStat.ButtonType.立即使用,info.m_isHide, String.valueOf(info.m_tjId));
												break;
											case VIEDO_WATERMARK:
												MyBeautyStat.onClickByRes(R.string.视频水印主题详情_立即体验);
												info = m_info.m_themeRes;
												MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.视频水印, MyBeautyStat.ButtonType.立即使用,info.m_isHide, String.valueOf(info.m_tjId));
												break;
											case VIEDO_ORIGINALITY:
												MyBeautyStat.onClickByRes(R.string.视频创意主题详情_立即体验);
												info = m_info.m_themeRes;
												MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.视频创意, MyBeautyStat.ButtonType.立即使用,info.m_isHide, String.valueOf(info.m_tjId));
												break;
										}

										HashMap<String, Object> params = new HashMap<>();
										params.put("id", id);
										params.put("type", m_info.m_type);
										params.put("watermark",video_typeID);

										if(m_showOnKey)
										{
											params.put("need_refresh", true);
										}
										m_site.OnResourceUse(params,getContext());
									}
								}
							}
						}
					}
				}
			}
		}
	};

	private float GetCurDownloadSize()
	{
		int len = 0;
		if(m_info != null)
		{
			if(m_info.m_resArr != null)
			{
				int size = m_info.m_resArr.size();
				BaseRes res;
				for(int i = 0; i < size; i ++)
				{
					res = m_info.m_resArr.get(i);
					if(res.m_type == BaseRes.TYPE_NETWORK_URL && res instanceof TextRes)
					{
						if(((TextRes)res).m_resArr != null)
						{
							int arrSize = ((TextRes)res).m_resArr.size();
							FontRes font ;
							for(int k = 0; k < arrSize; k ++)
							{
								font = ((TextRes)res).m_resArr.get(k);
								if(font != null)
								{
									String filePath = res.GetSaveParentPath() + File.separator + DownloadMgr.GetFileName(font.url_res);
									if(!FileUtil.isFileExists(filePath))
									{
										len += font.m_size;
									}
								}
							}
						}
					}
				}
			}
		}
		len = len / 1024 / 1024;
		return len;
	}

	public int GetFirstShowId(int[] ids, ResType type, String classify)
	{
		int id = -1;
		if(ids != null)
		{
			ArrayList<FilterRes> filterResArr = null;
			ArrayList<TextRes> textResArr = null;
			ArrayList<LightEffectRes> effectResArr = null;
			ArrayList<VideoTextRes> videoTextResArr = null;
			synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
			{
				SQLiteDatabase db = ResourseDatabase.getInstance(getContext()).openDatabase();
				switch(type)
				{
					case FILTER:

						filterResArr = FilterResMgr2.getInstance().GetLocalResArr(db, false);
						break;
					case TEXT:
						int typeID = 1;
						if("water".equals(classify))
						{
							typeID = 1;
						}
						else if("attitude".equals(classify))
						{
							typeID = 2;
						}
						textResArr = TextResMgr2.getInstance().GetLocalResArr(db, typeID);
						break;
					case LIGHT_EFFECT:
						effectResArr = LightEffectResMgr2.getInstance().GetLocalResArr(db);
						break;
					case AUDIO_TEXT:

						if ("watermark".equals(classify)){
							video_typeID = 1;
						}else if ("originality".equals(classify)){
							video_typeID = 2;
						}
						videoTextResArr = VideoTextResMgr2.getInstance().GetLocalResArr(db, video_typeID);

						break;
				}
				ResourseDatabase.getInstance(getContext()).closeDatabase();
			}
			for(int i = 0; i < ids.length; i ++)
			{
				switch(type)
				{
					case FILTER:

						for(FilterRes res : filterResArr)
						{
							if(res.m_id == ids[i])
							{
								return ids[i];
							}
						}
						break;
					case TEXT:

						for(TextRes res : textResArr)
						{
							if(res.m_id == ids[i])
							{
								return ids[i];
							}
						}
						break;
					case LIGHT_EFFECT:
						for(LightEffectRes res : effectResArr)
						{
							if(res.m_id == ids[i])
							{
								return ids[i];
							}
						}
						break;
					case AUDIO_TEXT:
						for (VideoTextRes res : videoTextResArr){

							if (res.m_id == ids[i]){

								return  ids[i];

							}
						}
						break;

				}
			}
		}
		return id;
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
		m_mainFr.AddTopChild(shareFr);
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
		m_mainFr.AddTopChild(line);

		m_shareHideBtn = new ImageView(getContext());
		m_shareHideBtn.setImageResource(R.drawable.beauty_share_hide_btn);
		m_shareHideBtn.setScaleType(ImageView.ScaleType.CENTER);
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(40));
		m_shareHideBtn.setLayoutParams(ll);
		m_mainFr.AddTopChild(m_shareHideBtn);
		m_shareHideBtn.setOnClickListener(m_btnListener);
	}

	protected void AddItem(LinearLayout parent, View child, int width)
	{
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_VERTICAL;
		child.setLayoutParams(ll);
		parent.addView(child);
		child.setOnClickListener(m_btnListener);
	}

	private void setBtnState(ThemeItemInfo info)
	{
		if(info != null)
		{
			if(info.m_lock)
			{
				m_progressBar.setProgress(100);
				m_shareBtn.setVisibility(View.GONE);
				m_stateImg.setVisibility(VISIBLE);
				m_stateImg.setImageResource(R.drawable.master_share_friend);
				m_stateText.setText(getResources().getString(R.string.themeUnlockFriend));
				return;
			}
			else
			{
				m_shareBtn.setVisibility(View.VISIBLE);
				switch(info.m_state)
				{
					case ThemeItemInfo.PREPARE:
					{
						m_progressBar.setProgress(100);
						m_stateImg.setVisibility(VISIBLE);
						m_stateImg.setImageResource(R.drawable.mgr_download_icon);
						m_stateText.setText(getResources().getString(R.string.download_now));
						break;
					}
					case ThemeItemInfo.LOADING:
					{
						m_progressBar.setProgress(m_info.m_progress);
						m_stateImg.setVisibility(VISIBLE);
						m_stateImg.setImageResource(R.drawable.camera_take_picture_loading);
						m_stateText.setText(getResources().getString(R.string.downloading));
						break;
					}
					case ThemeItemInfo.CONTINUE:
					{
						m_progressBar.setProgress(100);
						m_stateImg.setVisibility(VISIBLE);
						m_stateImg.setImageResource(R.drawable.mgr_download_icon);
						m_stateText.setText(getResources().getString(R.string.download_continue));
						break;
					}
					case ThemeItemInfo.COMPLETE:
					{
						m_progressBar.setProgress(100);
						m_stateImg.setVisibility(GONE);
						m_stateText.setText(getResources().getString(R.string.use_now));
						break;
					}
				}

			}
		}
	}

	public void DownloadRes()
	{
		if(m_info != null)
		{
			ArrayList<IDownload> tempResArr = new ArrayList<>();

			if(m_info.m_resArr == null || m_info.m_resArr.size() < m_info.m_idArr.length)
			{
				for(int i = 0; i < m_info.m_idArr.length; i++)
				{
					BaseRes baseRes = new FilterRes();
					baseRes.m_id =  m_info.m_idArr[i];
					baseRes.m_type = BaseRes.TYPE_NETWORK_URL;
					tempResArr.add(baseRes);
				}
				tempResArr.add(m_info.m_themeRes);
				IDownload[] resArr = new IDownload[tempResArr.size()];
				tempResArr.toArray(resArr);
				DownloadMgr.getInstance().DownloadRes(resArr, false, m_downloadCB);
			}
			else
			{
				int size = m_info.m_resArr.size();
				BaseRes res;
				for(int i = 0; i < size; i ++)
				{
					res = m_info.m_resArr.get(i);
					if(res.m_type == BaseRes.TYPE_NETWORK_URL)
					{
						tempResArr.add(res);
					}
				}


				//每次下载的时候一定要把主题放在最后下载
				tempResArr.add(m_info.m_themeRes);
				IDownload[] resArr = new IDownload[tempResArr.size()];
				tempResArr.toArray(resArr);
				DownloadMgr.getInstance().DownloadRes(resArr, false, m_downloadCB);
			}
			if(m_info != null) {
				ThemeRes  info = null;
				switch (m_info.m_type) {
					case FILTER:
						info = m_info.m_themeRes;
						MyBeautyStat.onClickByRes(R.string.滤镜主题详情_立即下载);
						MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.滤镜, MyBeautyStat.ButtonType.立即下载,info.m_isHide, String.valueOf(info.m_tjId));
						break;
					case LIGHT_EFFECT:
						info = m_info.m_themeRes;
						MyBeautyStat.onClickByRes(R.string.光效主题详情_立即下载);
						MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.光效, MyBeautyStat.ButtonType.立即下载,info.m_isHide, String.valueOf(info.m_tjId));
						break;
				}
				switch (m_curPageType){
					case TEXT_WATERMARK:
						info = m_info.m_themeRes;
						MyBeautyStat.onClickByRes(R.string.照片水印主题详情_立即下载);
						MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.照片水印, MyBeautyStat.ButtonType.立即下载,info.m_isHide, String.valueOf(info.m_tjId));

						break;
					case  TEXT_ATTITUTE:
						info = m_info.m_themeRes;
						MyBeautyStat.onClickByRes(R.string.照片态度主题详情_立即下载);
						MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.照片态度, MyBeautyStat.ButtonType.立即下载,info.m_isHide, String.valueOf(info.m_tjId));
						break;
					case VIEDO_WATERMARK:
						info = m_info.m_themeRes;
						MyBeautyStat.onClickByRes(R.string.视频水印主题详情_立即下载);
						MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.视频水印, MyBeautyStat.ButtonType.立即下载,info.m_isHide, String.valueOf(info.m_tjId));
						break;
					case VIEDO_ORIGINALITY:
						info = m_info.m_themeRes;
						MyBeautyStat.onClickByRes(R.string.视频创意主题详情_立即下载);
						MyBeautyStat.onDownloadRes(MyBeautyStat.DownloadType.视频创意, MyBeautyStat.ButtonType.立即下载,info.m_isHide, String.valueOf(info.m_tjId));
						break;
				}
			}



		}
	}

	private MgrUtils.MyDownloadCB m_downloadCB = new MgrUtils.MyDownloadCB(new MgrUtils.MyCB()
	{
		@Override
		public void OnFail(int downloadId, IDownload res)
		{

		}

		@Override
		public void OnGroupFail(int downloadId, IDownload[] resArr)
		{
			m_needRefresh = false;
			if(m_info != null)
			{
				m_info.m_state = ThemeItemInfo.PREPARE;
			}
			setBtnState(m_info);
			if(m_checkDlg != null && !m_checkDlg.isShowing())
			{
				m_checkDlg.show();
			}
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
			if(m_info != null)
			{
				m_info.m_state = ThemeItemInfo.LOADING;
				m_info.m_progress = progress;
			}
			setBtnState(m_info);
		}

		@Override
		public void OnGroupComplete(int downloadId, IDownload[] resArr)
		{
			boolean flag = true;
			if(resArr != null)
			{
				for(int i = 0; i < resArr.length; i ++)
				{
					if(resArr[i] instanceof FilterRes && TextUtils.isEmpty(((FilterRes)resArr[i]).m_filterData))
					{
						((FilterRes)resArr[i]).m_type = BaseRes.TYPE_NETWORK_URL;
						flag = false;
					}
				}
			}
			if(!flag)
			{
				OnGroupFail(downloadId, resArr);
			}
			else
			{
				m_needRefresh = true;
				if(m_info != null)
				{
					m_info.m_state = ThemeItemInfo.COMPLETE;
				}
				setBtnState(m_info);
			}
		}
	});

	private MgrUtils.MyDownloadCB m_downloadThumbCB = new MgrUtils.MyDownloadCB(new MgrUtils.MyCB()
	{
		@Override
		public void OnFail(int downloadId, IDownload res)
		{

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
			if(m_adapter != null)
			{
				m_adapter.notifyDataSetChanged();
			}
			if(res instanceof ThemeRes)
			{
				MakeShareInfos();
			}
		}

		@Override
		public void OnGroupProgress(int downloadId, IDownload[] resArr, int progress)
		{
		}

		@Override
		public void OnGroupComplete(int downloadId, IDownload[] resArr)
		{
		}
	});

	@Override
	public void onBack()
	{
		if(m_mainFr != null && m_mainFr.IsTopBarShowing())
		{
			m_mainFr.ShowTopBar(false);
			return;
		}
		if(m_hasAnim)
		{
			closeAnim();
		}
		else
		{
			onBack1();
		}


		if(m_info != null) {
			switch (m_info.m_type) {
				case FILTER:
					MyBeautyStat.onClickByRes(R.string.滤镜主题详情_退出滤镜主题详情页);
					break;
				case LIGHT_EFFECT:
					MyBeautyStat.onClickByRes(R.string.光效主题详情_退出光效主题详情页);
					break;
			}
		}

		switch (m_curPageType){
			case TEXT_WATERMARK:
				MyBeautyStat.onClickByRes(R.string.文字水印主题_退出文字水印主题详情页);
				break;
			case  TEXT_ATTITUTE:
				MyBeautyStat.onClickByRes(R.string.文字态度主题_退出文字态度主题详情页);
				break;
			case VIEDO_WATERMARK:
				MyBeautyStat.onClickByRes(R.string.视频水印主题_退出视频水印主题详情页);
				break;
			case VIEDO_ORIGINALITY:
				MyBeautyStat.onClickByRes(R.string.视频创意主题_退出视频创意主题详情页);
				break;
		}

	}

	private void onBack1()
	{
		HashMap<String, Object> params = new HashMap<>();
		params.put("need_refresh", m_needRefresh);
		if(m_pageCB != null)
		{
			m_pageCB.onClose(params);
		}
		else
		{
			m_site.OnBack(params,getContext());
		}
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
	public void onClose()
	{
		m_exit = true;
		if(m_list != null)
		{
			View child;
			int childCount = m_list.getChildCount();
			for(int i = 0; i < childCount; i ++)
			{
				child = m_list.getChildAt(i);
				if(child instanceof ImageItem)
				{
					Glide.clear(((ImageItem)child).m_img);
				}
				else if(child instanceof InfoItem)
				{
					Glide.clear(((InfoItem)child).m_head);
				}
				else if(child instanceof ImageView)
				{
					Glide.clear(child);
				}
			}
		}
		m_adapter = null;
		if(m_listData != null)
		{
			m_listData.clear();
			m_listData = null;
		}
		if(m_downloadCB != null)
		{
			m_downloadCB.ClearAll();
			m_downloadCB = null;
		}
		if(m_downloadThumbCB != null)
		{
			m_downloadThumbCB.ClearAll();
			m_downloadThumbCB = null;
		}
		this.setBackgroundColor(Color.BLACK);
		if(m_bkBmp != null)
		{
			m_bkBmp.recycle();
			m_bkBmp = null;
		}
		if(m_checkDlg != null)
		{
			m_checkDlg.dismiss();
			m_checkDlg = null;
		}
		if(m_shareTip != null)
		{
			m_shareTip.dismiss();
			m_shareTip = null;
		}

		super.onClose();
		TongJiUtils.onPageEnd(getContext(), TAG);
	}

	@Override
	public void onResume()
	{
		TongJiUtils.onPageResume(getContext(), TAG);
		super.onResume();
	}

	@Override
	public void onPause()
	{
		TongJiUtils.onPagePause(getContext(), TAG);
		super.onPause();
	}

	class ImageItem extends FrameLayout
	{
		private ImageView m_img;
		private TextView m_text;
		public ImageItem(Context context)
		{
			super(context);
			FrameLayout.LayoutParams fl;

			m_img = new ImageView(context);
//			m_img.setScaleType(ImageView.ScaleType.FIT_XY);
			fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_img.setLayoutParams(fl);
			this.addView(m_img);

			m_text = new TextView(getContext());
			int topPadding = ShareData.PxToDpi_xhdpi(20);
			int leftPadding = ShareData.PxToDpi_xhdpi(40);
			m_text.setPadding(leftPadding, topPadding, leftPadding, topPadding);
			m_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			m_text.setTextColor(Color.WHITE);
			m_text.setGravity(Gravity.CENTER);
			fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.RIGHT | Gravity.TOP;
			fl.topMargin = ShareData.PxToDpi_xhdpi(20);
			fl.rightMargin = fl.topMargin;
			m_text.setLayoutParams(fl);
			this.addView(m_text);
		}
	}

	class InfoItem extends LinearLayout
	{
		public ImageView m_head;
		public TextView m_text1;
		public TextView m_text2;
		public TextView m_text3;
		public InfoItem(Context context)
		{
			super(context);

			this.setOrientation(VERTICAL);
			LinearLayout.LayoutParams ll;
			this.setPadding(0, ShareData.PxToDpi_xhdpi(50), 0, ShareData.PxToDpi_xhdpi(50));

			m_head = new ImageView(context);
			m_head.setScaleType(ImageView.ScaleType.CENTER);
			ll = new LayoutParams(m_headW, m_headH);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			ll.bottomMargin = ShareData.PxToDpi_xhdpi(30);
			m_head.setLayoutParams(ll);
			this.addView(m_head);

			m_text1 = new TextView(context);
			m_text1.setTextColor(0xffffffff);
			m_text1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			ll = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			ll.bottomMargin = ShareData.PxToDpi_xhdpi(20);
			m_text1.setLayoutParams(ll);
			this.addView(m_text1);

			m_text2 = new TextView(context);
			m_text2.setTextColor(0xff666666);
			m_text2.setPadding(ShareData.PxToDpi_xhdpi(120), 0, ShareData.PxToDpi_xhdpi(120), 0);
			m_text2.setGravity(Gravity.CENTER);
			m_text2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			ll = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			ll.bottomMargin = ShareData.PxToDpi_xhdpi(50);
			m_text2.setLayoutParams(ll);
			this.addView(m_text2);

			m_text3 = new TextView(context);
			m_text3.setPadding(ShareData.PxToDpi_xhdpi(50), 0, ShareData.PxToDpi_xhdpi(50), 0);
			m_text3.setTextColor(0xffaaaaaa);
			m_text3.setLineSpacing(1.0f, 1.3f);
			m_text3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			ll = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_text3.setLayoutParams(ll);
			this.addView(m_text3);

		}
	}

	class ListAdapter extends BaseAdapter
	{

		@Override
		public int getCount()
		{
			return m_listData == null ? 0: m_listData.size();
		}

		@Override
		public Object getItem(int position)
		{
			return m_listData == null? null : m_listData.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			int type = getItemViewType(position);
			if(type == 0)
			{
				if(convertView == null)
				{
					convertView = new ImageView(getContext());
					convertView.setMinimumHeight(ShareData.m_screenWidth);
					convertView.setMinimumWidth(ShareData.m_screenWidth);
				}
				if(m_listData != null)
					LoadImg(convertView, m_listData.get(position), position);
			}
			else if(type == 1)
			{
				if(convertView == null)
				{
					convertView = new InfoItem(getContext());
				}
				if(m_listData != null)
					LoadInfos((InfoItem)convertView, m_listData.get(position));
			}
			else if(type == 2)
			{
				if(convertView == null)
				{
					convertView = new ImageItem(getContext());
					convertView.setMinimumHeight(ShareData.m_screenWidth);
				}
				if(m_listData != null)
					LoadImg(convertView, m_listData.get(position), position);
			}
			return convertView;
		}

		private void LoadImg(final View view, BaseRes res, int position)
		{
			String url = null;
			if(res instanceof TextRes)
			{
				url = ((TextRes)res).m_coverImg;
				/*if(TextUtils.isEmpty(url))
				{
					url = ((TextRes)res).url_coverImg;
				}*/
			}
			else if(res instanceof FilterRes)
			{
				url = ((FilterRes)res).m_coverImg;
				/*if(TextUtils.isEmpty(url))
				{
					url = ((FilterRes)res).url_coverImg;
				}*/
			}
			else if(res instanceof LightEffectRes)
			{
				if(!TextUtils.isEmpty(((LightEffectRes)res).m_coverImg))
					url = ((LightEffectRes)res).m_coverImg;
				else{
					url = (String)((LightEffectRes)res).m_thumb;
				}
				/*if(TextUtils.isEmpty(url))
				{
					url = ((LightEffectRes)res).url_coverImg;
				}*/
			}
			else if (res instanceof VideoTextRes){
				url = ((VideoTextRes)res).m_coverPic;
				/*if(TextUtils.isEmpty(url))
				{
					url = ((VideoTextRes)res).url_coverPic;
				}*/
			}
			else if(res instanceof ThemeRes)
			{
				url = ((ThemeRes)res).m_icon;
			}

			if(url == null || url.length() == 0)
			{
				if(res instanceof ThemeRes && m_info.m_downloadId == -1)
				{
					m_info.m_downloadId = DownloadMgr.getInstance().DownloadRes(res, m_downloadThumbCB);
				}
				else
				{
					if(!m_resDownloadIds.containsKey(res.m_id) || m_resDownloadIds.get(res.m_id) == -1)
					{
						int downloadID = DownloadMgr.getInstance().DownloadResThumb(res, m_downloadThumbCB);
						m_resDownloadIds.put(res.m_id, downloadID);
					}
				}
				return;
			}
			if(view instanceof ImageView)
			{
				GlideImageLoader.LoadImg((ImageView)view, getContext(), url, ShareData.m_screenWidth, ShareData.m_screenWidth, false);
			}
			else if(view instanceof ImageItem)
			{
				GlideImageLoader.LoadImg(((ImageItem)view).m_img, getContext(), url, ShareData.m_screenWidth, ShareData.m_screenWidth, false);
				if(res instanceof TextRes)
				{
//					if(!TextUtils.isEmpty(((TextRes)res).m_titleColor))
//					{
//						((ImageItem)view).m_text.setBackgroundColor(Painter.GetColor(((TextRes)res).m_titleColor, "ff"));
//					}
//					((ImageItem)view).m_text.setText(((TextRes)res).m_name);
				}
				else if(res instanceof FilterRes)
				{
					if(!TextUtils.isEmpty(((FilterRes)res).m_coverColor))
					{
						((ImageItem)view).m_text.setBackgroundColor(Painter.GetColor(((FilterRes)res).m_coverColor, 0xff));
					}
					((ImageItem)view).m_text.setText(((FilterRes)res).m_name);
				}
				else if(res instanceof LightEffectRes)
				{
					if(!TextUtils.isEmpty(((LightEffectRes)res).m_color))
					{
						((ImageItem)view).m_text.setBackgroundColor(Painter.GetColor(((LightEffectRes)res).m_color, 0xff));
					}
					((ImageItem)view).m_text.setText(((LightEffectRes)res).m_name);
				}
			}
		}

		private void LoadInfos(final InfoItem view, BaseRes res)
		{
			if(view == null)
				return;
			ThemeRes themeRes = (ThemeRes)res;
			String detail = themeRes.m_detail.replaceAll("&lt;br rel=auto&gt;", "\n");
			if(m_info.m_type == ResType.TEXT && !m_info.m_themeRes.m_dashiType.equals("attitude"))
			{
				view.m_head.setVisibility(GONE);
				view.m_text2.setVisibility(GONE);
				view.m_text1.setText(themeRes.m_name);
				view.m_text3.setText(detail);
				LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.LEFT;
				ll.bottomMargin = ShareData.PxToDpi_xhdpi(40);
				ll.leftMargin = ShareData.PxToDpi_xhdpi(50);
				view.m_text1.setLayoutParams(ll);
			}
			else if(m_info.m_type == ResType.LIGHT_EFFECT)
			{
				view.m_head.setVisibility(GONE);
				view.m_text2.setVisibility(GONE);
				view.m_text1.setText(themeRes.m_name);
				view.m_text3.setText(detail);
				LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.LEFT;
				ll.bottomMargin = ShareData.PxToDpi_xhdpi(40);
				ll.leftMargin = ShareData.PxToDpi_xhdpi(50);
				view.m_text1.setLayoutParams(ll);
			}else if (m_curPageType  ==  ResType.VIEDO_ORIGINALITY || m_curPageType == ResType.VIEDO_WATERMARK){
				view.m_head.setVisibility(GONE);
				view.m_text2.setVisibility(GONE);
				view.m_text1.setText(themeRes.m_name);
				view.m_text3.setText(detail);
				LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.LEFT;
				ll.bottomMargin = ShareData.PxToDpi_xhdpi(40);
				ll.leftMargin = ShareData.PxToDpi_xhdpi(50);
				view.m_text1.setLayoutParams(ll);
			}
			else
			{
				GlideImageLoader.LoadCircleImg(view.m_head, getContext(), ((ThemeRes)res).m_dashiIcon, 0, false);
				view.m_text1.setText(themeRes.m_dashiName);
				view.m_text2.setText(themeRes.m_dashiRank);
				view.m_text3.setText(detail);
			}

		}

		@Override
		public int getItemViewType(int position)
		{
			if(position >= 2)
				return 2;
			return position;
		}

		@Override
		public int getViewTypeCount()
		{
			return 3;
		}
	}

	public static interface IntroPageCallback
	{
		public void onClose(HashMap<String, Object> params);
	}
}
