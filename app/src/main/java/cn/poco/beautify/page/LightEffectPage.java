package cn.poco.beautify.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.poco.MaterialMgr2.MgrUtils;
import cn.poco.MaterialMgr2.ThemeIntroPage;
import cn.poco.MaterialMgr2.ThemeItemInfo;
import cn.poco.MaterialMgr2.site.ThemeIntroPageSite2;
import cn.poco.beautify.BeautifyHandler;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.MyButtons;
import cn.poco.beautify.MySeekBar2;
import cn.poco.beautify.SimpleListItem;
import cn.poco.beautify.site.LightEffectPageSite;
import cn.poco.beautify.site.MasterIntroPageSite;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.framework.BaseSite;
import cn.poco.framework.FileCacheMgr;
import cn.poco.framework.SiteID;
import cn.poco.graphics.Shape;
import cn.poco.graphics.ShapeEx;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.light.GLLightEffectView;
import cn.poco.light.LightEffectShapeEx;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.LightEffectRes;
import cn.poco.resource.LightEffectResMgr2;
import cn.poco.resource.LockRes;
import cn.poco.resource.ResType;
import cn.poco.resource.ResourceUtils;
import cn.poco.resource.ThemeRes;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv100.SimpleBtnList100;
import cn.poco.utils.ImageUtil;
import cn.poco.utils.Utils;

/**
 * 光效
 */
public class LightEffectPage extends BaseBeautifyPage
{
	public static final int LIGHT_NORMOL = 0;
	public static final int LIGHT_ADJUST = 1;
	private static final String TAG = "光效";
	protected LightEffectPageSite m_site;

	protected ArrayList<ShapeEx> m_effectCaches;	//记录上一次文字

	//	protected LightEffectView m_view;
	protected GLLightEffectView m_view;
	private FrameLayout m_viewContainer;
	private ImageView mAnimView;
	private FrameLayout mAnimViewFr;

	protected LinearLayout m_flipFr;
	//	protected MyButtons m_flipHBtn;
//	protected MyButtons m_flipVBtn;
	protected LinearLayout m_seekBarFr;
	protected MySeekBar2 m_seekBar;
	protected TextView m_seekkBarTip;
	protected int m_curEffectUri = -1;

	protected HandlerThread m_imageThread;
	protected BeautifyHandler m_mainHandler;
	protected UIHandler m_UIHandler;
	protected MyBtnLst m_btnLst = new MyBtnLst();

	protected SimpleBtnList100 m_adjustList;
	protected LinearLayout m_adjustBar;
	protected ArrayList<SimpleBtnList100.Item> m_adjustItems;
	protected ImageView m_adjustDownBtn;

	protected SimpleBtnList100 m_LightTypeList;
	protected int m_curList = LIGHT_NORMOL;
	protected int m_curAdjust = -1;
	protected BeautifyResMgr.LightAdjustData m_curAdjustData;
	protected Bitmap m_curLightBmp;
	protected HashMap<Integer,HashMap<Integer,BeautifyResMgr.LightAdjustData>> m_allLightsAdjusts = new HashMap<>();
	protected HashMap<Integer,BeautifyResMgr.LightAdjustData> m_curAjusts = new HashMap<>();
	protected MyButtons m_norBtn;
	protected MyButtons m_adjustBtn;
	protected Bitmap m_finalLightBmp = null;

	private ThemeIntroPage m_introPage;

	private SimpleBtnList100 m_mixModeList;

	public LightEffectPage(Context context, BaseSite site)
	{
		super(context, site);
		m_site = (LightEffectPageSite)site;
		InitData();
		InitUI();
		TongJiUtils.onPageStart(getContext(), TAG);
		MyBeautyStat.onPageStartByRes(R.string.照片光效);
	}

	@Override
	public void SetData(HashMap<String, Object> params)
	{
		super.SetData(params);
		if(params != null && m_view != null)
		{
			m_view.SetImg(m_orgInfo.image, m_org);
			m_view.UpdateUI();

			if(m_mainImgH <= 1.0f)
			{
				if (mAnimView != null && m_viewFr != null) {
					mAnimView.clearAnimation();
					m_viewFr.removeView(mAnimViewFr);
					mAnimView = null;
				}
			}
			else
			{
				if(mAnimView != null)
				{
					mAnimView.setImageBitmap(m_org);
				}
			}

			InitResList(true);

			if(TagMgr.CheckTag(getContext(), "first_enter_light_effect"))
			{
				postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						if(m_helpFlag)
						{
							m_helpFlag = false;
							HashMap<String, Object> paramsa = new HashMap<>();
							InitBkImg();
							paramsa.put("img", m_bkBmp);
							m_site.OpenHelpPage(paramsa,getContext());
							TagMgr.SetTag(getContext(), "first_enter_light_effect");
						}
					}
				}, 400);
			}
		}
	}

	@Override
	protected BaseBeautifyPage.MyBtnLst GetBtnLst()
	{
		return m_btnLst;
	}

	@Override
	protected void InitShowView()
	{
		m_viewContainer = new FrameLayout(getContext());
		m_viewContainer.setBackgroundColor(0xff0e0e0e);
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(m_frW, m_frH);
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		m_viewContainer.setLayoutParams(fl);
		m_viewFr.addView(m_viewContainer, 0);
		m_viewContainer.setOnClickListener(m_btnLst);

		m_view = new GLLightEffectView(getContext(), m_frW, m_frH);
//		m_view.setBackgroundColor(0xff0e0e0e);
		m_view.setControlCallback(m_coreCallback);
		fl = new FrameLayout.LayoutParams(m_frW, m_frH);
		fl.gravity = Gravity.CENTER;
		m_view.setLayoutParams(fl);
		m_viewContainer.addView(m_view, 0);

		mAnimViewFr = new FrameLayout(getContext());
		mAnimViewFr.setBackgroundColor(0xff0e0e0e);
		fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mAnimViewFr.setLayoutParams(fl);
		m_viewFr.addView(mAnimViewFr);

		mAnimView = new ImageView(getContext());
		mAnimView.setBackgroundColor(0xff0e0e0e);
		mAnimView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		fl = new FrameLayout.LayoutParams(m_frW, m_frH);
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		mAnimView.setLayoutParams(fl);
		mAnimViewFr.addView(mAnimView);
	}

	@Override
	protected void SetShowViewAnim()
	{
		float startScale = m_mainImgH / m_curImgH;
		float endScale = 1;
		AnimationSet as;
		ScaleAnimation sa;
		as = new AnimationSet(true);
		sa = new ScaleAnimation(startScale, endScale, startScale, endScale, m_frW / 2, m_frH / 2);
		sa.setDuration(350);
		as.addAnimation(sa);

		if(m_mainViewH != 0)
		{
			float start = (m_mainViewH - m_frH) / 2f;
			TranslateAnimation ta = new TranslateAnimation(0, 0, start, 0);
			ta.setDuration(350);
			as.addAnimation(ta);
		}

		mAnimView.startAnimation(as);
		as.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mAnimView != null && m_viewFr != null) {
					mAnimView.clearAnimation();
					m_viewFr.removeView(mAnimViewFr);
					mAnimView = null;
				}
			}
		});
	}


	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(m_masterPage != null)
		{
			m_masterPage.onActivityResult(requestCode, resultCode, data);
		}
		if(m_introPage != null)
		{
			m_introPage.onActivityResult(requestCode, resultCode, data);
		}
		return super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBack()
	{
		if(m_masterPage != null)
		{
			m_masterPage.onBack();
			return;
		}
		if(m_introPage != null)
		{
			m_introPage.onBack();
			return;
		}
		if(m_masterPage != null)
		{
			m_masterPage.close();
			return;
		}
		if(m_curList == LIGHT_ADJUST)
		{
			m_btnLst.onClick(m_adjustDownBtn);
			return;
		}
		if (m_seekBarFr.getVisibility() == View.VISIBLE)
		{
			MyBeautyStat.onClickByRes(R.string.光效不透明度_退出光效不透明度);
			LayoutResList(m_bottomBar, true, true);
			return;
		}
		m_params.put("imgh", m_curImgH);
		m_params.put("viewh", m_frH);
		m_site.onBack(m_params,getContext());
	}

	@Override
	public void onClose()
	{
		super.onClose();

		if(m_view != null)
		{
			m_view.releaseMem();
			m_view = null;
		}
		m_UIHandler = null;
		if(m_imageThread != null)
		{
			m_imageThread.quit();
			m_imageThread = null;
		}
		if(m_finalLightBmp != null)
		{
			m_finalLightBmp.recycle();
			m_finalLightBmp = null;
		}
		TongJiUtils.onPageEnd(getContext(), TAG);
		MyBeautyStat.onClickByRes(R.string.照片光效_退出照片光效);
		MyBeautyStat.onPageEndByRes(R.string.照片光效);
	}

	@Override
	public void onResume()
	{
		if(m_view != null)
		{
			m_view.onResume();
		}
		TongJiUtils.onPageResume(getContext(), TAG);
		super.onResume();
	}

	@Override
	public void onPause()
	{
		TongJiUtils.onPagePause(getContext(), TAG);
		super.onPause();
	}

	@Override
	protected void releaseMem()
	{
		super.releaseMem();
		if(m_listAdapter != null)
		{
			m_listAdapter.setItemList(null);
			m_listAdapter.ClearAll();
			m_listAdapter = null;
		}
		if(m_resList != null)
		{
			this.removeView(m_resList);
			m_resList.Clear();
			m_resList.setDragEnabled(false);
			m_resList = null;
			m_listCallback = null;
		}
		m_curLightBmp = null;
		if(m_adjustList != null)
		{
			m_adjustList.ClearAll();
			m_adjustList = null;
		}
		this.removeAllViews();
		System.gc();
	}

	@Override
	protected boolean canDelete(int position)
	{
		boolean flag = false;
		DragListItemInfo itemInfo = m_listDatas.get(position);
		if(itemInfo != null)
		{
			LightEffectRes res = (LightEffectRes)itemInfo.m_ex;
			if(res.m_type != BaseRes.TYPE_LOCAL_RES)
			{
				flag = true;
			}
		}
		return flag;
	}

	protected void InitData()
	{
		super.InitData();

		m_effectCaches = new ArrayList<>();

		m_UIHandler = new UIHandler();
		m_imageThread = new HandlerThread("effect_thread");
		m_imageThread.start();
		m_mainHandler = new BeautifyHandler(m_imageThread.getLooper(), getContext(), m_UIHandler);
		m_curAdjustData = new BeautifyResMgr.LightAdjustData(null, 0,0,0);
	}

	protected void InitUI()
	{
		super.InitUI();
		FrameLayout.LayoutParams fl;
		m_title.setText(getResources().getString(R.string.Light));
		m_icon.setVisibility(VISIBLE);

		{
			m_seekBarFr = new LinearLayout(getContext());
			m_seekBarFr.setVisibility(View.GONE);
			m_seekBarFr.setOrientation(LinearLayout.VERTICAL);
			fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			fl.bottomMargin = m_resBarHeight + ShareData.PxToDpi_xhdpi(20);
			m_seekBarFr.setLayoutParams(fl);
			m_bottomBar.addView(m_seekBarFr);
			{
				LinearLayout.LayoutParams ll;
				m_seekkBarTip = new TextView(getContext());
				m_seekkBarTip.setMaxLines(1);
				m_seekkBarTip.setText("0");
				m_seekkBarTip.setTextColor(Color.WHITE);
				m_seekkBarTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.LEFT;
				ll.leftMargin = ShareData.PxToDpi_xhdpi(21);
				m_seekkBarTip.setLayoutParams(ll);
				m_seekBarFr.addView(m_seekkBarTip);

				m_seekBar = new MySeekBar2(getContext());
				m_seekBar.setMax(12);
				m_seekBar.SetDotNum(13);
				m_seekBar.setOnSeekBarChangeListener(m_seekBarListener);
				m_seekBar.setProgress(0);
				ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40), LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.topMargin = ShareData.PxToDpi_xhdpi(10);
				m_seekBar.setLayoutParams(ll);
				m_seekBarFr.addView(m_seekBar);
			}

			m_resListFr = new FrameLayout(getContext());
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			fl.gravity = Gravity.BOTTOM;
			m_resListFr.setLayoutParams(fl);
			m_bottomBar.addView(m_resListFr);

			m_flipFr = new LinearLayout(getContext());
//			m_flipFr.setBackgroundColor(0xff000000);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_bottomBarHeight);
			fl.gravity = Gravity.BOTTOM;
			m_flipFr.setLayoutParams(fl);
			m_bottomBar.addView(m_flipFr);
			{
				LinearLayout.LayoutParams ll;
				m_norBtn = new MyButtons(getContext(), R.drawable.beauty_light_logo,R.drawable.beauty_light_logo);
				m_norBtn.setOnClickListener(m_btnLst);
				ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth / 2, LinearLayout.LayoutParams.MATCH_PARENT);
				ll.weight = 1;
				m_norBtn.setLayoutParams(ll);
				m_flipFr.addView(m_norBtn);

				m_adjustBtn = new MyButtons(getContext(), R.drawable.beauty_adjust_btn_out, R.drawable.beauty_adjust_btn_out);
				m_adjustBtn.setAlpha(0.2f);
				m_adjustBtn.setOnClickListener(m_btnLst);
				ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth / 2, LinearLayout.LayoutParams.MATCH_PARENT);
				ll.weight = 1;
				m_adjustBtn.setLayoutParams(ll);
				m_flipFr.addView(m_adjustBtn);
			}

//			{
//				m_LightTypeList = new SimpleBtnList100(getContext());
////			m_filterTypeList.setBackgroundColor(0xff000000);
//				ArrayList<SimpleBtnList100.Item> items = BeautifyResMgr.getColorItems(getContext());
//				m_LightTypeList.SetData(BeautifyResMgr.getColorItems(getContext()), m_LightListCB);
//				m_LightTypeList.SetSelByIndex(0);
//				fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_bottomBarHeight);
//				fl.gravity = Gravity.BOTTOM;
//				m_LightTypeList.setLayoutParams(fl);
//				m_bottomBar.addView(m_LightTypeList);
//			}

			{
				int adjustBtnsHeight = ShareData.PxToDpi_xhdpi(120);
				int downHeight = ShareData.PxToDpi_xhdpi(40);
				m_adjustBar = new LinearLayout(getContext());
				m_adjustBar.setVisibility(View.GONE);
				m_adjustBar.setBackgroundColor(0xff000000);
				m_adjustBar.setOrientation(LinearLayout.VERTICAL);
				fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.BOTTOM;
				m_adjustBar.setLayoutParams(fl);
				m_bottomBar.addView(m_adjustBar);
				{
					m_adjustList = new SimpleBtnList100(getContext());
					m_adjustItems = BeautifyResMgr.getLightAdjustItems(getContext());
					m_adjustList.SetData(m_adjustItems, m_adjustBtnListCB);
					LinearLayout.LayoutParams ll;
					ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, adjustBtnsHeight);
					m_adjustList.setLayoutParams(ll);
					m_adjustBar.addView(m_adjustList);

					ImageView Line = new ImageView(getContext());
					Line.setBackgroundColor(0xff272727);
					ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
					Line.setLayoutParams(ll);
					m_adjustBar.addView(Line);

					m_adjustDownBtn = new ImageView(getContext());
					m_adjustDownBtn.setOnClickListener(m_btnLst);
					m_adjustDownBtn.setScaleType(ImageView.ScaleType.CENTER);
					m_adjustDownBtn.setImageResource(R.drawable.beauty_color_adjust_down);
					ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, downHeight);
					m_adjustDownBtn.setLayoutParams(ll);
					m_adjustBar.addView(m_adjustDownBtn);
				}
			}
		}

		if(SysConfig.GetAppVer(getContext()).contains("88.8.8"))
		{
			m_mixModeList = new SimpleBtnList100(getContext());
			ArrayList<SimpleBtnList100.Item> items = BeautifyResMgr.getMixModeList(getContext());
			m_mixModeList.SetData(items, new SimpleBtnList100.Callback()
			{
				@Override
				public void OnClick(SimpleBtnList100.Item view, int index)
				{
					if(m_view != null)
					{
						m_view.changeMixMode((Integer) ((SimpleListItem) view).m_ex);
					}
					m_mixModeList.SetSelByIndex(index);
				}
			});
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_resBarHeight);
			fl.gravity = Gravity.TOP;
			fl.topMargin = m_topBarHeight;
			m_mixModeList.setLayoutParams(fl);
			m_bottomBar.addView(m_mixModeList);
			m_mixModeList.SetSelByIndex(0);
		}
	}

	SimpleBtnList100.Callback m_adjustBtnListCB = new SimpleBtnList100.Callback() {
		@Override
		public void OnClick(SimpleBtnList100.Item view, int index) {
			if(m_curAdjust != index )
			{
				m_adjustList.SetSelByIndex(index);
				m_adjustList.ScrollToCenter(true);
				m_curAdjust = index;
				m_seekBarFr.setVisibility(VISIBLE);
				SimpleListItem item = (SimpleListItem) view;
				m_titleFr.setVisibility(VISIBLE);
				m_title.setText(item.m_title);
				m_icon.setVisibility(GONE);
				TongJi2.AddCountByRes(getContext(), ((SimpleListItem) view).m_tjID);
				MyBeautyStat.onClickByRes(((SimpleListItem) view).m_shenceTjID);
				m_curAdjustData = (BeautifyResMgr.LightAdjustData) item.m_ex;
				if(m_curAdjustData != null && m_curAdjustData.m_type != BeautifyResMgr.LightAdjustData.FLIP_V && m_curAdjustData.m_type != BeautifyResMgr.LightAdjustData.FLIP_H)
				{
					if(m_curAjusts.get(m_curAdjustData.m_type) != null)
					{
						m_curAdjustData = m_curAjusts.get(m_curAdjustData.m_type);
					}
					else
					{
						if(m_curAdjustData.m_type == BeautifyResMgr.LightAdjustData.HUE || m_curAdjustData.m_type == BeautifyResMgr.LightAdjustData.SATURATION)
						{
							m_curAdjustData = new BeautifyResMgr.LightAdjustData(m_curAdjustData.m_tjId, m_curAdjustData.m_type,0,6);
						}
						else
						{
							m_curAdjustData = new BeautifyResMgr.LightAdjustData(m_curAdjustData.m_tjId, m_curAdjustData.m_type,0,0);
						}
					}
					m_curAjusts.put(m_curAdjustData.m_type,m_curAdjustData);
				}
				else if(m_curAdjustData.m_type == BeautifyResMgr.LightAdjustData.FLIP_V)
				{
//					m_curAjusts.remove(BeautifyResMgr.LightAdjustData.FLIP_H);
					m_seekBarFr.setVisibility(GONE);
					m_view.Flip(false);
					m_view.UpdateUI();
					return;
				}
				else if(m_curAdjustData.m_type == BeautifyResMgr.LightAdjustData.FLIP_H)
				{
//					m_curAjusts.remove(BeautifyResMgr.LightAdjustData.FLIP_V);
					m_seekBarFr.setVisibility(GONE);
					m_view.Flip(true);
					m_view.UpdateUI();
					return;
				}
				adjustForCurData();
			}
			else
			{
				if(m_curAdjustData.m_type == BeautifyResMgr.LightAdjustData.FLIP_V)
				{
					m_adjustList.SetSelByIndex(-1);
					m_curAdjust = -1;
					m_titleFr.setVisibility(VISIBLE);
					m_icon.setVisibility(GONE);
					m_title.setText(getResources().getString(R.string.Adjust));
					TongJi2.AddCountByRes(getContext(), R.integer.美化_光效_垂直翻转);
					m_view.Flip(false);
					m_view.UpdateUI();
				}
				else if(m_curAdjustData.m_type == BeautifyResMgr.LightAdjustData.FLIP_H)
				{
					m_adjustList.SetSelByIndex(-1);
					m_titleFr.setVisibility(GONE);
					m_curAdjust = -1;
					m_titleFr.setVisibility(VISIBLE);
					m_icon.setVisibility(GONE);
					m_title.setText(getResources().getString(R.string.Adjust));
					TongJi2.AddCountByRes(getContext(), R.integer.美化_光效_水平翻转);
					m_view.Flip(true);
					m_view.UpdateUI();
				}
				else
				{
					if (m_seekBarFr.getVisibility() == GONE)
					{
						m_titleFr.setVisibility(VISIBLE);
						m_icon.setVisibility(GONE);
						m_title.setText(((SimpleListItem)view).m_title);
						m_seekBarFr.setVisibility(VISIBLE);
						m_adjustList.SetSelByIndex(index);
						m_titleFr.setVisibility(VISIBLE);
					}
					else if(m_seekBarFr.getVisibility() == VISIBLE)
					{
						m_seekBarFr.setVisibility(GONE);
						m_adjustList.SetSelByIndex(-1);
						m_titleFr.setVisibility(GONE);
						m_titleFr.setVisibility(VISIBLE);
						m_icon.setVisibility(GONE);
						m_title.setText(getResources().getString(R.string.Adjust));
					}
				}
			}
		}
	};

	private void adjustForCurData()
	{
		if(m_curAdjustData != null)
		{
			int value = (int) m_curAdjustData.m_progress;
			m_seekBar.setProgress(value);
			int max = m_seekBar.getMax();
			ReLayoutSeekBarTip(value, max);
		}
	}

	@Override
	protected ArrayList<DragListItemInfo> InitListDatas()
	{
		ArrayList<DragListItemInfo> listdatas = BeautifyResMgr.getLightEffectRess(getContext());
		if (listdatas != null)
		{
			DragListItemInfo info = listdatas.get(1);
			if(m_org != null)
			{
				int m_thumbW = ShareData.PxToDpi_xhdpi(140);
				String cachePath = FileCacheMgr.GetLinePath();

				Bitmap out = MakeBmpV2.CreateBitmapV2(m_org, 0, 0, -1, m_thumbW, m_thumbW, Bitmap.Config.ARGB_8888);
				Utils.SaveTempImg(out, cachePath);
				info.m_logo = cachePath;
			}
			else{
				String path = m_orgInfo.image;
				info.m_logo = path;
			}
		}
		return listdatas;
	}

	protected void SetSelItemByUri(int uri)
	{
		if (uri == DragListItemInfo.URI_ORIGIN)
		{
			m_view.DelEffect();
			m_view.UpdateUI();
			m_adjustBtn.setAlpha(0.2f);
			TongJi2.AddCountByRes(getContext(),R.integer.美化_光效_原图);
		}
		else
		{
			LightEffectRes res = null;
			if (m_listDatas != null)
			{
				int size = m_listDatas.size();
				for (int i = 0; i < size; i++)
				{
					if (uri == m_listDatas.get(i).m_uri)
					{
						res = (LightEffectRes) m_listDatas.get(i).m_ex;
						break;
					}
				}
			}
			if (res != null)
			{
				MyBeautyStat.onClickByRes(R.string.照片光效_选择光效);
				MyBeautyStat.onChooseMaterial(res.m_tjId + "", R.string.照片光效);
				m_curAdjust = -1;
				m_adjustList.SetSelByIndex(-1);
//				mSeekBarLayout.setVisibility(GONE);
				if(m_allLightsAdjusts.get(res.m_id) != null)
				{
					m_curAjusts = m_allLightsAdjusts.get(res.m_id);
				}
				else
				{
					m_curAjusts = new HashMap<>();
					m_allLightsAdjusts.put(res.m_id,m_curAjusts);
				}
				m_adjustBtn.setAlpha(1f);
				m_uiEnabled = false;
				Message msg = m_mainHandler.obtainMessage();
				msg.what = BeautifyHandler.DECODE_LIGHTEFFECT_IMG;
				msg.obj = res.m_res;
				msg.arg1 = res.m_id;
				m_mainHandler.sendMessage(msg);
			}
		}
	}

	protected synchronized ShapeEx GetCacheInfo(LightEffectRes res)
	{
		ShapeEx out = null;
		if(res != null)
		{
			LightEffectRes res2;
			int size = m_effectCaches.size();
			for(int i = 0; i < size; i ++)
			{
				res2 = (LightEffectRes)m_effectCaches.get(i).m_ex;
				if(res2.m_id == res.m_id)
				{
					out = m_effectCaches.get(i);
					break;
				}
			}
		}
		return out;
	}

	protected synchronized void AddToCacheInfo(ShapeEx shape)
	{
		if(shape != null)
		{
			int size = m_effectCaches.size();
			ShapeEx shape2;
			int index = -1;
			for(int i = 0; i < size; i ++)
			{
				shape2 = m_effectCaches.get(i);
				if(shape.m_soleId == shape2.m_soleId)
				{
					index = i;
					break;
				}
			}
			if(index == -1)
			{
				m_effectCaches.add(shape);
			}
			else
			{
				m_effectCaches.set(index, shape);
			}
		}
	}

	protected synchronized boolean RemoveFromCacheInfo(ShapeEx shape)
	{
		if(shape != null)
		{
			ShapeEx shapeEx;
			int size = m_listDatas.size();
			for(int i = 0; i < size; i ++)
			{
				shapeEx = m_effectCaches.get(i);
				if(shape.m_soleId == shapeEx.m_soleId)
				{
					m_listDatas.remove(i);
					return true;
				}
			}
		}
		return false;
	}

	protected DragListItemInfo GetResItemInfo(LightEffectRes res)
	{
		DragListItemInfo info = new DragListItemInfo();
		info.m_uri = res.m_id;
		info.m_name = res.m_name;
		info.m_logo = res.m_thumb;
		info.m_head = res.m_headImg;
		info.text_bg_color_over = 0xb2ffc433;
		info.m_ex = res;
		int flag = DownloadMgr.getInstance().GetStateById(res.m_id, res.getClass());
		if (flag == 0 && res.m_type == LightEffectRes.TYPE_NETWORK_URL)
		{
			info.m_style = DragListItemInfo.Style.NEED_DOWNLOAD;
		}
		else if (flag == 1 || flag == 2)
		{
			info.m_style = DragListItemInfo.Style.LOADING;
		}
		else
		{
			info.m_style = DragListItemInfo.Style.NORMAL;
		}
		if(res.m_shareType != LockRes.SHARE_TYPE_NONE)
		{
			info.m_isLock = true;
		}
		return info;
	}

	protected void LayoutResList(View v, final boolean isUp, boolean hasAnim)
	{
		if (v == null)
			return;
		v.clearAnimation();
		final int margin1 = m_bottomBarHeight + ShareData.PxToDpi_xhdpi(82);
		int start = 0;
		int end = 0;
		if (isUp)
		{
			m_seekBarFr.setVisibility(View.GONE);
			FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			fl.bottomMargin = m_resBarHeight + ShareData.PxToDpi_xhdpi(20);
			m_seekBarFr.setLayoutParams(fl);

			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			fl.gravity = Gravity.BOTTOM;
			m_bottomBar.setLayoutParams(fl);

			start = margin1;
			end = 0;
			SetTopbarState(m_topBar, true, true);
		}
		else
		{
			start = 0;
			end = margin1;
			SetTopbarState(m_topBar, false, true);
		}

		if (hasAnim)
		{
			AnimationSet as;
			TranslateAnimation ta;
			as = new AnimationSet(true);
			ta = new TranslateAnimation(0, 0, start, end);
			ta.setDuration(350);
			ta.setFillAfter(true);
			ta.setAnimationListener(new Animation.AnimationListener()
			{

				@Override
				public void onAnimationStart(Animation animation)
				{

				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{

				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					FrameLayout.LayoutParams fl;
					if (!isUp)
					{
						fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
						fl.gravity = Gravity.BOTTOM;
						fl.bottomMargin = -margin1;
						if (m_bottomBar != null)
						{
							m_bottomBar.clearAnimation();
							m_bottomBar.setLayoutParams(fl);
						}

						fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
						fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
						fl.bottomMargin = margin1 + m_resBarHeight - ShareData.PxToDpi_xhdpi(20);
						if (m_seekBarFr != null)
						{
							m_seekBarFr.clearAnimation();
							m_seekBarFr.setVisibility(View.VISIBLE);
							m_seekBarFr.setLayoutParams(fl);
						}
					}
				}
			});
			as.addAnimation(ta);
			v.startAnimation(as);
		}
	}

	protected void ReLayoutSeekBarTip(int progress, int max)
	{
		int w = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40);
		int leftMargin = ShareData.PxToDpi_xhdpi(20);
		int seekBarThumbW = ShareData.PxToDpi_xhdpi(21);
		LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) m_seekkBarTip.getLayoutParams();
		ll.leftMargin = (int) ((w - (seekBarThumbW << 1)) * progress / (float) max + leftMargin + seekBarThumbW - ShareData.PxToDpi_xhdpi(35));
		m_seekkBarTip.setLayoutParams(ll);
		String tip;
		if (progress > 0)
		{
			if(m_curList == LIGHT_NORMOL)
			{
				tip = "+" + progress;
			}
			else
			{
				if(m_curAdjustData.m_type == BeautifyResMgr.LightAdjustData.HUE || m_curAdjustData.m_type == BeautifyResMgr.LightAdjustData.SATURATION)
				{
					if(progress > max/2)
					{
						tip = "+" + (progress - max/2);
					}
					else if(progress < max/2)
					{
						tip = "-" + (max/2 - progress);
					}
					else
					{
						tip = " " + (max/2 - progress);
					}
				}
				else
				{
					tip = "+" + progress;
				}
			}
		}
		else if (progress < 0)
		{
			tip = " " + progress;
		}
		else
		{
			if(m_curList == LIGHT_NORMOL)
			{
				tip = " " + progress;
			}
			else
			{
				if(m_curAdjustData.m_type == BeautifyResMgr.LightAdjustData.HUE || m_curAdjustData.m_type == BeautifyResMgr.LightAdjustData.SATURATION)
				{
					tip = "-" + max/2;
				}
				else
				{
					tip = " " + progress;
				}
			}
		}
		m_seekkBarTip.setText(tip);
	}

	private void SetTopbarState(View v, boolean isOpen, boolean hasAnimation)
	{
		if (v == null)
			return;
		v.clearAnimation();

		int start;
		int end;
		if (isOpen)
		{
			v.setVisibility(View.VISIBLE);

			start = -1;
			end = 0;
		}
		else
		{
			v.setVisibility(View.GONE);

			start = 0;
			end = -1;
		}

		if (hasAnimation)
		{
			AnimationSet as;
			TranslateAnimation ta;
			as = new AnimationSet(true);
			ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, start, Animation.RELATIVE_TO_SELF, end);
			ta.setDuration(350);
			as.addAnimation(ta);
			v.startAnimation(as);
		}
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params)
	{
		super.onPageResult(siteID, params);
		if(siteID == SiteID.MASTER_INTRODUCE && params != null)
		{
			int filter_id = -1;
			boolean lock = true;
			Object obj = params.get("filter_id");
			if(obj != null)
			{
				filter_id = (Integer)obj;
			}
			obj = params.get("lock");
			if(obj != null)
			{
				lock = (Boolean)obj;
			}
			if(filter_id != -1 && lock == false)
			{
				if(m_listAdapter != null)
				{
					m_listAdapter.Unlock(filter_id);
					m_listAdapter.SetSelByUri(filter_id);
					SetSelItemByUri(filter_id);
				}
			}
		}
		if(siteID == SiteID.LIGHT_EFFECT_HELP_ANIM)
		{
			m_helpFlag = true;
		}
		if(siteID == SiteID.THEME_INTRO_PAGE || siteID == SiteID.THEME_PAGE)
		{
			if(params != null)
			{
				int sel_uri = -1;
				if(params.containsKey("id"))
				{
					sel_uri = (Integer)params.get("id");
				}
				boolean needRefresh = false;
				if(params.containsKey("need_refresh"))
				{
					needRefresh = (Boolean)params.get("need_refresh");
				}
				if(needRefresh)
				{
					UpdateListDatas();
				}
				if(sel_uri != -1)
				{
					DragListItemInfo info;
					int index = m_listAdapter.GetIndexByUri(sel_uri);
					if(index != -1)
					{
						info = m_listDatas.get(index);
						OnItemClick(null, info, index);
					}
				}
				if(m_listAdapter != null)
				{
					int index = m_listAdapter.GetIndexByUri(m_curEffectUri);
					m_listAdapter.SetSelByIndex(index);
					if(index == -1)
					{
						m_curEffectUri = DragListItemInfo.URI_ORIGIN;
						if(m_view != null)
						{
							m_view.DelEffect();
							m_view.UpdateUI();
						}
					}
				}
				if (m_curEffectUri == DragListItemInfo.URI_ORIGIN)
				{
					if (m_seekBarFr.getVisibility() == View.VISIBLE)
					{
						MyBeautyStat.onClickByRes(R.string.光效不透明度_退出光效不透明度);
						LayoutResList(m_bottomBar, true, true);
					}
				}
			}
		}
	}

	@Override
	protected void OnHideItem(int position)
	{
		DragListItemInfo itemInfo = m_listDatas.get(position);
		if(itemInfo != null && itemInfo.m_ex != null)
		{
			LightEffectRes res = (LightEffectRes)itemInfo.m_ex;
			if(res.m_type != BaseRes.TYPE_LOCAL_RES)
			{
				MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.照片光效);
				m_listDatas.remove(position);
				m_listAdapter.removeItem(position);

				LightEffectResMgr2.getInstance().DeleteRes(getContext(), res);
				MyBeautyStat.onClickByRes(R.string.照片光效_删除光效);

				if(m_curEffectUri == res.m_id)
				{
					SetSelItemByUri(DragListItemInfo.URI_ORIGIN);
					m_curEffectUri = DragListItemInfo.URI_ORIGIN;
					if (m_seekBarFr.getVisibility() == View.VISIBLE)
					{
						MyBeautyStat.onClickByRes(R.string.光效不透明度_退出光效不透明度);
						LayoutResList(m_bottomBar, true, true);
					}
				}
			}
		}
	}

	@Override
	protected void OnChangeItem(int fromPosition, int toPosition)
	{
		DragListItemInfo itemInfo = m_listDatas.get(fromPosition);
		int fromPos = fromPosition;
		int toPos = toPosition;
		if(itemInfo != null)
		{
			fromPos = ResourceUtils.HasId(LightEffectResMgr2.getInstance().GetOrderArr(), itemInfo.m_uri);
		}
		itemInfo = m_listDatas.get(toPosition);
		if(itemInfo != null)
		{
			toPos = ResourceUtils.HasId(LightEffectResMgr2.getInstance().GetOrderArr(), itemInfo.m_uri);
		}
		ResourceUtils.ChangeOrderPosition(LightEffectResMgr2.getInstance().GetOrderArr(), fromPos, toPos);
		LightEffectResMgr2.getInstance().SaveOrderArr();
		if (m_listDatas != null && m_listDatas.size() > fromPosition && m_listDatas.size() > toPosition) {
			itemInfo = m_listDatas.remove(fromPosition);
			m_listDatas.add(toPosition, itemInfo);
		}
	}

	@Override
	public void OnItemClick(View view, DragListItemInfo info, int index)
	{
		if (m_uiEnabled)
		{
			if (info.m_isLock)
			{
				OnHeadClick(view, info, index);
				return;
			}
			if(info.m_uri == DragListItemInfo.URI_MGR)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_光效_更多按钮);
				MyBeautyStat.onClickByRes(R.string.照片光效_进入更多);
				HashMap<String, Object> params = new HashMap<>();
				params.put("type", ResType.LIGHT_EFFECT);
				params.put("typeOnly", true);
				m_site.OnDownloadMore(params,getContext());
				return;
			}
			switch (info.m_style)
			{
				case NORMAL:
				{
					if (m_curEffectUri != info.m_uri)
					{
						SetSelItemByUri(info.m_uri);
						if (m_listAdapter != null) {
							m_listAdapter.SetSelByUri(info.m_uri);
							m_resList.ScrollToCenter(index);
						}
					}
					else if (info.m_uri != DragListItemInfo.URI_ORIGIN)
					{
						boolean flag = true;
						if (m_seekBarFr.getVisibility() == View.VISIBLE)
						{
							flag = false;
							MyBeautyStat.onClickByRes(R.string.光效不透明度_退出光效不透明度);
							LayoutResList(m_bottomBar, true, true);
						}
						if (flag && m_seekBarFr.getVisibility() == View.GONE)
						{
							MyBeautyStat.onClickByRes(R.string.照片光效_进入光效不透明度);
							LayoutResList(m_bottomBar, false, true);
						}
					}
					if (info.m_uri == DragListItemInfo.URI_ORIGIN)
					{
						if (m_seekBarFr.getVisibility() == View.VISIBLE)
						{
							MyBeautyStat.onClickByRes(R.string.光效不透明度_退出光效不透明度);
							LayoutResList(m_bottomBar, true, true);
						}
					}
					m_curEffectUri = info.m_uri;
					break;
				}
				case NEED_DOWNLOAD:
				{
					if(info.m_ex instanceof ThemeRes)
					{
						TongJi2.AddCountByRes(getContext(), R.integer.美化_光效_主题推荐位);
						HashMap<String, Object> params = new HashMap<>();
						ThemeItemInfo itemInfo = MgrUtils.GetThemeItemInfoByUri(getContext(), ((ThemeRes)info.m_ex).m_id, ResType.LIGHT_EFFECT);
						params.put("data", itemInfo);
						if(view != null)
						{
							int[] location = new int[2];
							view.getLocationOnScreen(location);

							params.put("hasAnim", true);
							params.put("centerX",location[0]);
							params.put("centerY", location[1]);
							params.put("viewH", view.getHeight());
							params.put("viewW", view.getWidth());
						}
						MyBeautyStat.onClickByRes(R.string.照片光效_打开推荐位);
						OpenRecommend(params);
					}
					break;
				}
				default:
					break;
			}

		}
	}

	private void OpenRecommend(HashMap<String, Object> params)
	{
		if(m_introPage != null)
		{
			removeView(m_introPage);
			m_introPage.onClose();
			m_introPage = null;
		}
		m_introPage = new ThemeIntroPage(getContext(), m_introSite);
		m_introPage.SetData(params);
		addView(m_introPage);
	}

	private ThemeIntroPageSite2 m_introSite = new ThemeIntroPageSite2(){
		@Override
		public void OnBack(HashMap<String, Object> params,Context context)
		{
			if(m_introPage != null)
			{
				LightEffectPage.this.removeView(m_introPage);
				m_introPage.onClose();
				m_introPage = null;
			}

			onPageResult(SiteID.THEME_INTRO_PAGE, params);
		}

		@Override
		public void OnResourceUse(HashMap<String, Object> params,Context context)
		{
			super.OnResourceUse(params,context);
		}
	};

	@Override
	public void OnHeadClick(View view, DragListItemInfo info, int index)
	{
		if (m_uiEnabled)
		{
			if (!info.m_isLock)
			{
				if(m_curEffectUri != info.m_uri)
				{
					OnItemClick(view, info, index);
					return;
				}
			}
			else
			{
				if (m_listAdapter != null) {
					m_listAdapter.SetSelByUri(DragListItemInfo.URI_NONE);
				}
			}

			TongJi2.AddCountByRes(getContext(), R.integer.美化_光效_介绍页);

			LightEffectRes data = (LightEffectRes) info.m_ex;
			if(data != null)
			{
				HashMap<String, Object> params = new HashMap<String, Object>();
				Object topImg = data.m_coverImg;
				if(TextUtils.isEmpty(data.m_coverImg))
				{
					topImg = data.m_thumb;
				}
				params.put("top_img", topImg);
				params.put("head_img", data.m_headImg);
				params.put("name", data.m_name);
				params.put("intro", data.m_headTitle);
				params.put("img_url", data.m_headLink);
				params.put("unlock_tag", Tags.UNLOCK_LIGHTEFFECT);
				if (info.m_isLock)
				{
					params.put("lock", true);
					params.put("filter_id", info.m_uri);
				}
//				params.put("share_url", data.m_shareUrl);
				params.put("share_title", data.m_shareContent);

				if(view != null)
				{
					int[] location = new int[2];
					view.getLocationOnScreen(location);

					params.put("centerX",location[0]);
					params.put("centerY", location[1]);
					params.put("viewH", view.getHeight());
					params.put("viewW", view.getWidth());
				}
				OpenMasterPage(params);
			}
			m_curEffectUri = info.m_uri;
		}
	}

	protected void OpenMasterPage(HashMap<String, Object> params)
	{
		if(m_masterPage != null)
		{
			removeView(m_masterPage);
			m_masterPage.onClose();
			m_masterPage = null;
		}
		m_masterPage = new MasterIntroPage(getContext(), m_masterSite);
		m_masterPage.SetData(params);
		addView(m_masterPage);
	}

	private MasterIntroPageSite m_masterSite = new MasterIntroPageSite()
	{
		@Override
		public void onBack(HashMap<String, Object> params,Context context)
		{
			if(m_masterPage != null)
			{
				LightEffectPage.this.removeView(m_masterPage);
				m_masterPage.onClose();
				m_masterPage = null;
			}
			if(params != null)
			{
				int id = -1;
				boolean lock = true;
				if(params.get("id") != null)
				{
					id = (Integer)params.get("id");
				}
				if(params.get("lock") != null)
				{
					lock = (Boolean)params.get("lock");
				}
				if(id != -1 && lock == false)
				{
					if(m_listAdapter != null)
					{
						DragListItemInfo info;
						m_listAdapter.Unlock(id);
						int index = m_listAdapter.GetIndexByUri(id);
						if(index != -1)
						{
							info = m_listDatas.get(index);
							OnItemClick(null, info, index);
						}
					}
				}
			}
		}
	};

	protected SeekBar.OnSeekBarChangeListener m_seekBarListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			ReLayoutSeekBarTip(progress, seekBar.getMax());
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			if(m_curList == LIGHT_ADJUST)
			{
				if(m_curAdjustData != null)
				{
					if(m_curAjusts != null && m_curAjusts.size() > 0)
					{
						float r = seekBar.getProgress()/(seekBar.getMax() + 0.0f);
						switch (m_curAdjustData.m_type)
						{
							case BeautifyResMgr.LightAdjustData.EXPOSURE:
								m_curAdjustData.m_value = (int) (r*35);
								break;
							case BeautifyResMgr.LightAdjustData.SATURATION:
								float tempValue = 0f;
								if(r < 0.5f)
								{
									tempValue = -(0.5f - r)*200;
								}
								else
								{
									tempValue = (r - 0.5f)*120;
								}
								m_curAdjustData.m_value = (int) tempValue;
								break;
							case BeautifyResMgr.LightAdjustData.HUE:
//								m_curAdjustData.m_value = (int) (r*100);
								float temp = 0f;
								if(r < 0.5f)
								{
									temp = -(0.5f - r)*100;
								}
								else
								{
									temp = (r - 0.5f)*100;
								}
								m_curAdjustData.m_value = (int) temp;
								break;
						}
						m_curAdjustData.m_progress = seekBar.getProgress();
						m_curAjusts.put(m_curAdjustData.m_type,m_curAdjustData);

						Message msg = Message.obtain();
						msg.what = BeautifyHandler.MSG_LIGHT_ADJUST;
						BeautifyHandler.LightAdjustMsg adjust = new BeautifyHandler.LightAdjustMsg();
						adjust.m_thumb = m_curLightBmp.copy(Bitmap.Config.ARGB_8888,true);
						adjust.m_adjustArr = m_curAjusts;
						msg.obj = adjust;
						m_mainHandler.sendMessage(msg);
					}
				}
			}
			else
			{
				int alpha = (int)(seekBar.getProgress() / (float)seekBar.getMax() * 120 + 0.5f);
				TongJi2.AddCountByRes(getContext(), R.integer.美化_光效_微调不透明度);
				m_view.SetAlpha(alpha);
			}
		}
	};

	protected GLLightEffectView.ControlCallback m_coreCallback = new GLLightEffectView.ControlCallback() {

		@Override
		public void onTouch(boolean isTouch)
		{
			if(isTouch == false && m_seekBarFr.getVisibility() == View.VISIBLE)
			{
				if(m_curList == LIGHT_NORMOL)
				{
					MyBeautyStat.onClickByRes(R.string.光效不透明度_退出光效不透明度);
					LayoutResList(m_bottomBar, true, true);
				}
			}
//			if(m_uiEnabled && m_view != null)
//			{
//				if(isTouch)
//				{
//					m_view.SetCompareFlag(true);
//				}
//				else
//				{
//					m_view.SetCompareFlag(false);
//				}
//				m_view.UpdateUI();
//			}
		}

		@Override
		public void onGetOutputBmp(Bitmap bmp, boolean finalSaved)
		{
			if(finalSaved)
			{
				m_org = bmp;
				BeautifyHandler.InitMsg info = new BeautifyHandler.InitMsg();
				info.m_inImgs = m_orgInfo;
				info.m_thumb = m_org;
				Message msg = m_mainHandler.obtainMessage();
				msg.what = BeautifyHandler.MSG_CACHE;
				msg.obj = info;
				m_mainHandler.sendMessage(msg);
			}
			else
			{
				m_params.remove("curBmp");
				int[] location = new int[2];
				m_view.getLocationOnScreen(location);
				Bitmap mirror = ImageUtil.GetScreenBmp((Activity)getContext(), ShareData.m_screenWidth, ShareData.m_screenHeight);
				Bitmap out = Bitmap.createBitmap(mirror.getWidth(), mirror.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(out);
				Matrix matrix = new Matrix();
				matrix.setTranslate(location[0], location[1]);
				canvas.drawBitmap(bmp, matrix, null);
				bmp = null;
				matrix.reset();
				canvas.drawBitmap(mirror, matrix, null);
				mirror = out;

				releaseMem();

				FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(m_frW, m_frH);
				fl.gravity = Gravity.CENTER;
				m_viewFr.setLayoutParams(fl);
				addView(m_viewFr);
				m_view.GetOutputBmp(DEF_IMG_SIZE);

				showPageMirror(mirror);
			}
		}

		@Override
		public void SelectPendant(int index)
		{
		}

		@Override
		public Bitmap MakeShowImg(Object info, int frW, int frH)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bitmap MakeOutputImg(Object info, int outW, int outH)
		{
			Bitmap out = BeautifyHandler.MakeBmp2(getContext(), info, outW, outH);

			return out;
		}

		@Override
		public Bitmap MakeShowEffect(Object info, int frW, int frH)
		{
			Bitmap out = null;

			if(info != null && info instanceof LightEffectRes)
			{
				Object res = ((LightEffectRes)info).m_res;
				File file = new File((String)res);
				if(!file.exists())
				{
					try
					{
						InputStream is = getContext().getAssets().open((String)res);
						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						byte[] bytes = new byte[1024];
						while(is.read(bytes) != -1)
						{
							bout.write(bytes, 0, bytes.length);
						}
						res = bout.toByteArray();
						bout.close();
						is.close();
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				Bitmap temp = Utils.DecodeImage(getContext(), res, 0, -1, frW, frH);
				if(temp != null)
				{
					out = MakeBmpV2.CreateBitmapV2(temp, 0, 0, -1, frW, frH, Bitmap.Config.ARGB_8888);
				}
			}

			return out;
		}

		@Override
		public Bitmap MakeOutputEffect(Object info, int outW, int outH)
		{
			if(m_finalLightBmp != null)
			{
				Bitmap out = m_finalLightBmp;
				m_finalLightBmp = null;
				return out;
			}
			return MakeShowEffect(info, outW, outH);
		}
	};

	protected class MyBtnLst extends BaseBeautifyPage.MyBtnLst
	{
		@Override
		public void onClick(View v, boolean fromUser)
		{
			if(m_uiEnabled)
			{
				if(v == m_backBtn)
				{
					onBack();
				}
				else if(v == m_okBtn)
				{
					TongJi2.AddCountByRes(getContext(), R.integer.美化_光效_保存);

					m_uiEnabled = false;
					SetWaitUI(true, getResources().getString(R.string.processing));

					m_params.remove("curBmp");

					String effectTjId = "0000";
					int effectAlpha = 0;
					ArrayList<MyBeautyStat.MgrInfo> tjInfos = new ArrayList<>();
					ShapeEx shapeEx = m_view.GetCurEffect();
					if(shapeEx != null)
					{
						LightEffectRes res = (LightEffectRes)shapeEx.m_ex;
						effectTjId = res.m_tjId + "";
						effectAlpha = (int)((((LightEffectShapeEx)shapeEx).m_alpha / 120f) * m_seekBar.getMax() + 0.5);
						if(m_curAjusts != null)
						{
							for (Iterator it = m_curAjusts.entrySet().iterator(); it.hasNext();) {
								Map.Entry e = (Map.Entry) it.next();
								BeautifyResMgr.LightAdjustData data = (BeautifyResMgr.LightAdjustData)e.getValue();
								MyBeautyStat.MgrInfo info = new MyBeautyStat.MgrInfo();
								info.id = data.m_tjId;
								info.alpha = data.m_progress;
								tjInfos.add(info);
							}
						}
						if (res != null)
						{
							TongJi2.AddCountById(res.m_tjId + "");
						}
						if(((LightEffectShapeEx)shapeEx).m_flip  == Shape.Flip.VERTICAL)
						{
							MyBeautyStat.MgrInfo info = new MyBeautyStat.MgrInfo();
							info.id = "chuizhi";
							info.alpha = 1;
						}
						else if(((LightEffectShapeEx)shapeEx).m_flip  == Shape.Flip.HORIZONTAL)
						{
							MyBeautyStat.MgrInfo info = new MyBeautyStat.MgrInfo();
							info.id = "shuiping";
							info.alpha = 1;
						}
					}
					MyBeautyStat.onClickByRes(R.string.照片光效_保存照片光效);
					MyBeautyStat.onUseEffect(tjInfos, effectTjId, effectAlpha, R.string.照片光效);

					//只是得到当前view的镜像,这个时候只显示镜像
					m_view.GetMirror();
				}
				else if(v == m_titleFr && m_helpFlag)
				{
					if(m_curList == LIGHT_NORMOL)
					{
						m_helpFlag = false;
						HashMap<String, Object> params = new HashMap<>();
						InitBkImg();
						params.put("img", m_bkBmp);
						m_site.OpenHelpPage(params,getContext());
					}
				}
				else if(v == m_adjustDownBtn)
				{
					MyBeautyStat.onClickByRes(R.string.光效细节调整_退出细节调整);
					m_curList = LIGHT_NORMOL;
					m_titleFr.setVisibility(VISIBLE);
					m_title.setText(getResources().getString(R.string.Light));
					m_icon.setVisibility(VISIBLE);
					m_seekBarFr.setVisibility(GONE);
					SetViewState(m_LightTypeList, true, true);
					SetViewState(m_adjustBar, false, false);
					SetViewState(m_resList, true, true);

					ShapeEx item = m_view.GetCurEffect();
					if(item != null)
					{
						int progress = (int)((((LightEffectShapeEx)item).m_alpha / 120f) * m_seekBar.getMax() + 0.5);
						ReLayoutSeekBarTip(progress, m_seekBar.getMax());
						m_seekBar.setProgress(progress);
					}
					m_adjustList.SetSelByIndex(-1);
					m_curAdjust = -1;
				}
				else if(v == m_adjustBtn)
				{
					if(m_adjustBtn.getAlpha() == 1f)
					{
						MyBeautyStat.onClickByRes(R.string.照片光效_进入细节调整);
						m_curList = LIGHT_ADJUST;
						SetViewState(m_LightTypeList, false, false);
						SetViewState(m_adjustBar, true, true);
						SetViewState(m_resList, false, false);
						m_title.setText(getResources().getString(R.string.Adjust));
						m_icon.setVisibility(GONE);
					}
				}
				else if(v == m_norBtn)
				{
					if(m_curList != LIGHT_NORMOL)
					{
						m_curList = LIGHT_NORMOL;
						m_title.setText(getResources().getString(R.string.Light));
						m_icon.setVisibility(VISIBLE);
						SetViewState(m_LightTypeList, true, true);
						MyBeautyStat.onClickByRes(R.string.光效细节调整_退出细节调整);
						SetViewState(m_adjustBar, false, false);
						SetViewState(m_resList, true, true);
					}
				}
				else if(v == m_viewContainer)
				{
					if(m_seekBarFr.getVisibility() == View.VISIBLE)
					{
						if(m_curList == LIGHT_NORMOL)
						{
							MyBeautyStat.onClickByRes(R.string.光效不透明度_退出光效不透明度);
							LayoutResList(m_bottomBar, true, true);
						}
					}
				}
			}
		}
	}

	protected class UIHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case BeautifyHandler.MSG_CACHE:
				{
					BeautifyHandler.InitMsg params = (BeautifyHandler.InitMsg) msg.obj;
					msg.obj = null;

					SetWaitUI(false, "");
					m_uiEnabled = true;

					if(params != null && params.m_outImgs != null)
					{
						m_params.put("imgs", params.m_outImgs);
						m_params.put("curBmp", params.m_thumb);
					}
					m_params.put("on_ok", true);
					m_params.put("imgh", m_curImgH);
					m_site.onBack(m_params,getContext());
					break;
				}
				case BeautifyHandler.DECODE_LIGHTEFFECT_IMG:
				{
					if(m_view != null && msg.obj != null)
					{
						Bitmap bmp = (Bitmap)msg.obj;
						m_curLightBmp = bmp.copy(Bitmap.Config.ARGB_8888,true);
						m_finalLightBmp = null;
						int id = msg.arg1;
						LightEffectRes res = null;
						if (m_listDatas != null)
						{
							int size = m_listDatas.size();
							for (int i = 0; i < size; i++)
							{
								if (id == m_listDatas.get(i).m_uri)
								{
									res = (LightEffectRes) m_listDatas.get(i).m_ex;
									break;
								}
							}
						}
						if(res != null)
						{
							m_view.DelEffect();
							int index = -1;
							ShapeEx shape = GetCacheInfo(res);
							if(shape != null)
							{
								shape.m_bmp = bmp;
								index = m_view.AddEffect(shape);
							}
							else
							{
								index = m_view.AddEffect(res, bmp);
							}
							if(index != -1)
							{
//								m_flipVBtn.setAlpha(1f);
//								m_flipHBtn.setAlpha(1f);
								ShapeEx item = m_view.GetCurEffect();
								AddToCacheInfo(item);
								if(m_curList == LIGHT_NORMOL)
								{
									int progress = (int)((((LightEffectShapeEx)item).m_alpha / 120f) * m_seekBar.getMax() + 0.5);
									ReLayoutSeekBarTip(progress, m_seekBar.getMax());
									m_seekBar.setProgress(progress);
								}
							}

							if(m_curAjusts != null)
							{
								if(m_curAjusts.size() > 0)
								{
									Message adjMsg = Message.obtain();
									adjMsg.what = BeautifyHandler.MSG_LIGHT_ADJUST;
									BeautifyHandler.LightAdjustMsg adjust = new BeautifyHandler.LightAdjustMsg();
									adjust.m_thumb = m_curLightBmp.copy(Bitmap.Config.ARGB_8888,true);
									adjust.m_adjustArr = m_curAjusts;
									adjMsg.obj = adjust;
									m_mainHandler.sendMessage(adjMsg);
								}
								else
								{
									m_view.UpdateUI();
									m_uiEnabled = true;
								}
							}
							else
							{
								m_view.UpdateUI();
								m_uiEnabled = true;
							}

						}
						else
						{
							m_uiEnabled = true;
						}
					}
					else
					{
						m_uiEnabled = true;
					}
					break;
				}
				case BeautifyHandler.MSG_LIGHT_ADJUST:
				{
					if(msg.obj != null)
					{
						Bitmap bmp = (Bitmap) msg.obj;
//						ShapeEx shapeEx = m_view.DelPendant();
						if(m_view != null)
						{
							ShapeEx shapeEx = m_view.GetCurEffect();

							if(shapeEx != null)
							{
								if(shapeEx.m_bmp != null)
								{
									shapeEx.m_bmp.recycle();
									shapeEx.m_bmp = null;
								}
								if(m_finalLightBmp != null)
								{
									m_finalLightBmp.recycle();
									m_finalLightBmp = null;
								}
								m_finalLightBmp = bmp;
								shapeEx.m_bmp = bmp;
								m_view.onChangeEffectBmp();
							}
//						m_view.AddPendant2(shapeEx);
							m_view.UpdateUI();

//						if(m_curAjusts != null && m_curAjusts.size() > 0)
//						{
//							if(m_curAjusts.get(BeautifyResMgr.LightAdjustData.FLIP_H) != null)
//							{
//								m_view.Flip(false);
//							}
//							else if(m_curAjusts.get(BeautifyResMgr.LightAdjustData.FLIP_V) != null)
//							{
//								m_view.Flip(true);
//							}
//						}
						}
						m_uiEnabled = true;
					}
					else
					{
						m_uiEnabled = true;
					}
					break;
				}
			}
		}
	}
}
