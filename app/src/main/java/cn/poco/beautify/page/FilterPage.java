package cn.poco.beautify.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.MaterialMgr2.MgrUtils;
import cn.poco.MaterialMgr2.ThemeIntroPage;
import cn.poco.MaterialMgr2.ThemeItemInfo;
import cn.poco.MaterialMgr2.site.ThemeIntroPageSite2;
import cn.poco.beautify.BeautifyHandler;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.BeautyAdjustType;
import cn.poco.beautify.BeautyColorType;
import cn.poco.beautify.CurveView;
import cn.poco.beautify.CurveView2;
import cn.poco.beautify.MyButtons2;
import cn.poco.beautify.MySeekBar2;
import cn.poco.beautify.SimpleListItem;
import cn.poco.beautify.site.FilterPageSite;
import cn.poco.beautify.site.MasterIntroPageSite;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.framework.BaseSite;
import cn.poco.framework.FileCacheMgr;
import cn.poco.framework.SiteID;
import cn.poco.image.filter;
import cn.poco.interphoto2.R;
import cn.poco.resource.BaseRes;
import cn.poco.resource.FilterRes;
import cn.poco.resource.FilterResMgr2;
import cn.poco.resource.ResType;
import cn.poco.resource.ResourceUtils;
import cn.poco.resource.ThemeRes;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv100.SimpleBtnList100;
import cn.poco.utils.ImageUtil;
import cn.poco.utils.Utils;

/**
 * 滤镜
 */
public class FilterPage extends BaseBeautifyPage
{
	private static final String TAG = "滤镜";
	protected FilterPageSite m_site;
	protected int m_curViewSize;

	protected ImageView m_view;
	protected CurveView2 m_curveView;

	protected SimpleBtnList100 m_filterTypeList;    //颜色列表
	protected LinearLayout m_seekBarFr;
	protected MySeekBar2 m_colorSeekBar;
	protected TextView m_seekkBarTip;
	protected LinearLayout m_adjustBar;
	protected ImageView m_adjustDownBtn;
	protected SimpleBtnList100 m_adjustList;    //调整列表
	protected ArrayList<SimpleBtnList100.Item> m_adjustItems;

	protected RelativeLayout m_rgbFr;
	protected MyButtons2 m_btnR;
	protected MyButtons2 m_btnG;
	protected MyButtons2 m_btnB;
	protected MyButtons2 m_btnRGB;
	protected ImageView m_scanBtn;

	protected BeautifyHandler.ColorMsg m_colorMsg;
	protected String m_adjustTip = getResources().getString(R.string.Adjust);

	protected BeautyColorType m_filterType = BeautyColorType.FILTER;
	protected BeautifyResMgr.AdjustData m_curAdjustData;
	protected int m_curFilterUri = -1;    //颜色
	protected ArrayList<CurveView2.CurveInfo> m_curInfo;

	protected HandlerThread m_imageThread;
	protected BeautifyHandler m_mainHandler;
	protected UIHandler m_UIHandler;
	protected MyBtnLst m_btnLst = new MyBtnLst();
	protected boolean m_curveShow;
	protected boolean m_seekBarShow;

	protected Bitmap m_showImg;		//显示用的Bitmap
	protected Bitmap m_curveBmp;	//曲线显示用的Bitmap
	protected Bitmap m_filterBmp;	//用于临时保存颜色的Bitmap
	private ThemeIntroPage m_introPage;
	public FilterPage(Context context, BaseSite site)
	{
		super(context, site);
		m_site = (FilterPageSite)site;
		InitData();
		InitUI();
		TongJiUtils.onPageStart(getContext(), TAG);
		MyBeautyStat.onPageStartByRes(R.string.照片滤镜);
	}

	/**
	 *
	 * @param params
	 * img RotationImg2[]/ImageFile2
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		super.SetData(params);
		if(params != null && m_view != null)
		{
			m_view.setImageBitmap(m_org);

			InitResList(true);

			if(TagMgr.CheckTag(getContext(), "first_enter_filter_1.2.0"))
			{
				postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						if(m_helpFlag)
						{
							m_helpFlag = false;
							HashMap<String, Object> params = new HashMap<>();
							InitBkImg();
							params.put("img", m_bkBmp);
							m_site.OpenFilterHelpPage(params,getContext());
							TagMgr.SetTag(getContext(), "first_enter_filter_1.2.0");
						}
					}
				}, 400);
			}
		}
	}

	@Override
	protected ArrayList<DragListItemInfo> InitListDatas()
	{
		ArrayList<DragListItemInfo> listdatas = BeautifyResMgr.GetFilterRess(getContext(), false);
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

	@Override
	protected void SetShowViewAnim()
	{
		SetShowViewAnim(m_view);
	}

	@Override
	protected void InitShowView()
	{
		m_view = new ImageView(getContext());
		m_view.setScaleType(ImageView.ScaleType.FIT_CENTER);
		m_view.setOnTouchListener(new View.OnTouchListener()
		{
			private float m_downY;
			private final float m_max = 200f;
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
					{
						MyBeautyStat.onClickByRes(R.string.照片滤镜_长按查看原图);
						m_downY = event.getY();
						m_view.setImageBitmap(m_org);
						break;
					}
					case MotionEvent.ACTION_CANCEL:
					case MotionEvent.ACTION_UP:
					{
						if (m_seekBarFr != null && m_seekBarFr.getVisibility() == View.VISIBLE && m_filterType == BeautyColorType.FILTER)
						{
							MyBeautyStat.onClickByRes(R.string.滤镜不透明度_收起滤镜不透明度);
							LayoutColorList(m_bottomBar, true, true);
						}
						float curY = event.getY();
						if(curY - m_downY > m_max && m_filterType == BeautyColorType.ADJUST)
						{
							onBack();
						}
						if(m_curveBmp != null && !m_curveBmp.isRecycled())
						{
							m_view.setImageBitmap(m_curveBmp);
						}
						else if(m_showImg != null && !m_showImg.isRecycled())
						{
							m_view.setImageBitmap(m_showImg);
						}
						break;
					}
				}
				if (m_seekBarFr != null && m_seekBarFr.getVisibility() == View.VISIBLE && m_filterType == BeautyColorType.FILTER)
				{
					if (event.getAction() == MotionEvent.ACTION_UP)
					{
						LayoutColorList(m_bottomBar, true, true);
					}
				}
				return true;
			}
		});
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(m_frW, m_frH);
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		m_view.setLayoutParams(fl);
		m_viewFr.addView(m_view, 0);
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
		if(m_filterType == BeautyColorType.ADJUST)
		{
			m_btnLst.onClick(m_adjustDownBtn);
			return;
		}
		if (m_filterType == BeautyColorType.FILTER && m_seekBarFr.getVisibility() == View.VISIBLE)
		{
			MyBeautyStat.onClickByRes(R.string.滤镜不透明度_收起滤镜不透明度);
			LayoutColorList(m_bottomBar, true, true);
			return;
		}
		m_params.put("imgh", m_curImgH);
		m_params.put("viewh", m_frH);
		m_site.onBack(m_params,getContext());
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params)
	{
		super.onPageResult(siteID, params);
		if(siteID == SiteID.BIGHT_HELP_ANIM || siteID == SiteID.FILTER_HELP_ANIM)
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
					int index = m_listAdapter.GetIndexByUri(m_curFilterUri);
					m_listAdapter.SetSelByIndex(index);
					if(index == -1)
					{
						SetSelItemByUri(DragListItemInfo.URI_ORIGIN);
					}
				}

				if (m_curFilterUri == DragListItemInfo.URI_ORIGIN)
				{
					if (m_seekBarFr.getVisibility() == View.VISIBLE)
					{
						MyBeautyStat.onClickByRes(R.string.滤镜不透明度_收起滤镜不透明度);
						LayoutColorList(m_bottomBar, true, true);
					}
				}
			}
		}
	}

	@Override
	public void onClose()
	{
		super.onClose();
		m_UIHandler = null;
		if(m_imageThread != null)
		{
			m_imageThread.quit();
			m_imageThread = null;
		}
		TongJiUtils.onPageEnd(getContext(), TAG);
		MyBeautyStat.onClickByRes(R.string.照片滤镜_退出照片滤镜);
		MyBeautyStat.onPageEndByRes(R.string.照片滤镜);
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

	@Override
	protected void releaseMem()
	{
		super.releaseMem();
		m_uiEnabled = false;
		m_curveBmp = null;
		m_showImg = null;
		m_filterBmp = null;
		if(m_listAdapter != null)
		{
			m_listAdapter.ClearAll();
			m_listAdapter = null;
		}
		if(m_resList != null)
		{
			m_resList.Clear();
			m_listCallback = null;
			m_resList.removeAllViews();
			m_resList = null;
		}
		m_curveCB = null;
	}

	@Override
	protected boolean canDelete(int position)
	{
		boolean flag = false;
		DragListItemInfo itemInfo = m_listDatas.get(position);
		if(itemInfo != null)
		{
			FilterRes res = (FilterRes)itemInfo.m_ex;
			if(res.m_type != BaseRes.TYPE_LOCAL_RES)
			{
				flag = true;
			}
		}
		return flag;
	}

	@Override
	protected void InitData()
	{
		super.InitData();
		m_curViewSize = ShareData.PxToDpi_xhdpi(500);

		m_colorMsg = new BeautifyHandler.ColorMsg();
		m_filterType = BeautyColorType.FILTER;
		m_curAdjustData = new BeautifyResMgr.AdjustData(BeautyAdjustType.NONE, 0);

		m_UIHandler = new UIHandler();
		m_imageThread = new HandlerThread("filter_thread");
		m_imageThread.start();
		m_mainHandler = new BeautifyHandler(m_imageThread.getLooper(), getContext(), m_UIHandler);
	}

	@Override
	protected BaseBeautifyPage.MyBtnLst GetBtnLst()
	{
		return m_btnLst;
	}

	protected void InitUI()
	{
		super.InitUI();
		FrameLayout.LayoutParams fl;
		m_title.setText(getContext().getResources().getString(R.string.Filters));
		m_icon.setVisibility(VISIBLE);

		m_seekBarFr = new LinearLayout(getContext());
		m_seekBarFr.setVisibility(View.GONE);
		m_seekBarFr.setOrientation(LinearLayout.VERTICAL);
		fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		fl.bottomMargin = m_resBarHeight;
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

			m_colorSeekBar = new MySeekBar2(getContext());
			m_colorSeekBar.setMax(12);
			m_colorSeekBar.SetDotNum(13);
			m_colorSeekBar.setOnSeekBarChangeListener(m_seekBarListener);
			m_colorSeekBar.setProgress(0);
			ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40), LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.topMargin = ShareData.PxToDpi_xhdpi(10);
			m_colorSeekBar.setLayoutParams(ll);
			m_seekBarFr.addView(m_colorSeekBar);
		}

		m_resListFr = new FrameLayout(getContext());
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		fl.gravity = Gravity.BOTTOM;
		m_resListFr.setLayoutParams(fl);
		m_bottomBar.addView(m_resListFr);

		m_rgbFr = new RelativeLayout(getContext());
		m_rgbFr.setVisibility(View.GONE);
		fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(120));
		fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		fl.bottomMargin = ShareData.PxToDpi_xhdpi(160);
		fl.leftMargin = fl.rightMargin = ShareData.PxToDpi_xhdpi(40);
		m_rgbFr.setLayoutParams(fl);
		m_bottomBar.addView(m_rgbFr);
		{
			RelativeLayout.LayoutParams rl;
			m_btnRGB = new MyButtons2(getContext(), R.drawable.beauty_curve_rgb, "RGB", ShareData.PxToDpi_xhdpi(17), 12);
			m_btnRGB.setId(R.id.btn_rgb);
			m_btnRGB.setOnClickListener(m_btnLst);
			m_btnRGB.OnChoose(true);
			rl= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			rl.addRule(RelativeLayout.CENTER_VERTICAL);
			m_btnRGB.setLayoutParams(rl);
			m_rgbFr.addView(m_btnRGB);

			m_btnR = new MyButtons2(getContext(), R.drawable.beauty_curve_r, "R", ShareData.PxToDpi_xhdpi(17), 12);
			m_btnR.setId(R.id.btn_r);
			m_btnR.setOnClickListener(m_btnLst);
			m_btnR.OnChoose(false);
			rl= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			rl.addRule(RelativeLayout.CENTER_VERTICAL);
			rl.addRule(RelativeLayout.RIGHT_OF, R.id.btn_rgb);
			rl.leftMargin = ShareData.PxToDpi_xhdpi(65);
			m_btnR.setLayoutParams(rl);
			m_rgbFr.addView(m_btnR);

			m_btnG = new MyButtons2(getContext(), R.drawable.beauty_curve_g, "G", ShareData.PxToDpi_xhdpi(17), 12);
			m_btnG.setId(R.id.btn_g);
			m_btnG.setOnClickListener(m_btnLst);
			m_btnG.OnChoose(false);
			rl= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			rl.addRule(RelativeLayout.CENTER_VERTICAL);
			rl.addRule(RelativeLayout.RIGHT_OF, R.id.btn_r);
			rl.leftMargin = ShareData.PxToDpi_xhdpi(65);
			m_btnG.setLayoutParams(rl);
			m_rgbFr.addView(m_btnG);

			m_btnB = new MyButtons2(getContext(), R.drawable.beauty_curve_b, "B", ShareData.PxToDpi_xhdpi(17), 12);
			m_btnB.setId(R.id.btn_b);
			m_btnB.setOnClickListener(m_btnLst);
			m_btnB.OnChoose(false);
			rl= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			rl.addRule(RelativeLayout.CENTER_VERTICAL);
			rl.addRule(RelativeLayout.RIGHT_OF, R.id.btn_g);
			rl.leftMargin = ShareData.PxToDpi_xhdpi(65);
			m_btnB.setLayoutParams(rl);
			m_rgbFr.addView(m_btnB);

			m_scanBtn = new ImageView(getContext());
			m_scanBtn.setOnClickListener(m_btnLst);
			m_scanBtn.setImageResource(R.drawable.beauty_curve_show);
			rl= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			rl.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			rl.topMargin = ShareData.PxToDpi_xhdpi(15);
			m_scanBtn.setLayoutParams(rl);
			m_rgbFr.addView(m_scanBtn);
		}

		m_filterTypeList = new SimpleBtnList100(getContext());
//			m_filterTypeList.setBackgroundColor(0xff000000);
		ArrayList<SimpleBtnList100.Item> items = BeautifyResMgr.getColorItems(getContext());
		m_filterTypeList.SetData(BeautifyResMgr.getColorItems(getContext()), m_filterListCB);
		int len = items.size();
		int index = -1;
		for (int i = 0; i < len; i++)
		{
			BeautyColorType type = (BeautyColorType) (((SimpleListItem) items.get(i)).m_ex);
			if (m_filterType == type)
			{
				index = i;
				break;
			}
		}
		m_filterTypeList.SetSelByIndex(index);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_bottomBarHeight);
		fl.gravity = Gravity.BOTTOM;
		m_filterTypeList.setLayoutParams(fl);
		m_bottomBar.addView(m_filterTypeList);

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
			m_adjustItems = BeautifyResMgr.getColorAdjustItems(getContext());
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
			m_adjustDownBtn.setScaleType(ImageView.ScaleType.CENTER);
			m_adjustDownBtn.setOnClickListener(m_btnLst);
			m_adjustDownBtn.setImageResource(R.drawable.beauty_color_adjust_down);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, downHeight);
			m_adjustDownBtn.setLayoutParams(ll);
			m_adjustBar.addView(m_adjustDownBtn);
		}

		m_curveView = new CurveView2(getContext());
		m_curveView.SetOnChangeListener(m_curveCB);
		m_curveView.setVisibility(View.GONE);
		fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		fl.bottomMargin = ShareData.PxToDpi_xhdpi(280) + ShareData.PxToDpi_xhdpi(40);
		m_curveView.setLayoutParams(fl);
		this.addView(m_curveView);

		m_curViewSize = m_curveView.GetCurveViewSize();

	}

	protected void InsertToAdjustList(BeautifyResMgr.AdjustData data)
	{
		if(m_colorMsg != null)
		{
			BeautifyResMgr.AdjustData tempData;

			//清理掉效果为0的数据
			for(int i = 0; i < m_colorMsg.m_adjustData.size(); i ++)
			{
				tempData = m_colorMsg.m_adjustData.get(i);
				if(tempData.m_type != BeautyAdjustType.NONE && tempData.m_value == 0)
				{
					m_colorMsg.m_adjustData.remove(i);
					i --;
				}
				if(tempData.m_type == BeautyAdjustType.NONE && data.m_type == BeautyAdjustType.NONE)
				{
					m_colorMsg.m_adjustData.remove(i);
					i --;
				}
			}

			boolean flag = true;
			for(int i = 0; i < m_colorMsg.m_adjustData.size(); i ++)
			{
				tempData = m_colorMsg.m_adjustData.get(i);
				if(data.m_type == tempData.m_type)
				{
					m_colorMsg.m_adjustData.set(i, data);
					flag = false;
					break;
				}
			}
			if(flag)
			{
				m_colorMsg.m_adjustData.add(data);
			}
		}
	}

	protected void adjustBtnClick(BeautifyResMgr.AdjustData data)
	{
		m_titleFr.setVisibility(View.VISIBLE);
		String text = TextUtils.isEmpty(m_adjustTip)? getResources().getString(R.string.Adjust) : m_adjustTip;
		m_title.setText(text);
		m_curAdjustData = data;
		if(m_curAdjustData == null)
		{
			return;
		}
		InsertToAdjustList(data);

		if(m_curAdjustData.m_type == BeautyAdjustType.CURVE)
		{
			if(TagMgr.CheckTag(getContext(), "first_enter_curve"))
			{
				postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						if(m_helpFlag)
						{
							m_helpFlag = false;
							HashMap<String, Object> params = new HashMap<>();
							InitBkImg();
							params.put("img", m_bkBmp);
							m_site.OpenHelpPage(params,getContext());
							TagMgr.SetTag(getContext(), "first_enter_curve");
						}
					}
				}, 400);
			}
			if(m_curInfo == null)
			{
				m_curInfo = m_curveView.GetCurveInfos();
			}
			m_btnLst.onClick(m_btnRGB);
			m_seekBarFr.setVisibility(View.GONE);
			m_curveShow = true;
			m_scanBtn.setImageResource(R.drawable.beauty_curve_show);
			m_curveView.setVisibility(View.VISIBLE);
			m_rgbFr.setVisibility(View.VISIBLE);
			m_icon.setVisibility(View.VISIBLE);
			return;
		}
		else if(m_curAdjustData.m_type != BeautyAdjustType.NONE)
		{
			m_seekBarShow = true;
			m_curveView.setVisibility(View.GONE);
			m_rgbFr.setVisibility(View.GONE);
			m_seekBarFr.setVisibility(View.VISIBLE);
			m_icon.setVisibility(View.GONE);
		}
		else
		{
			m_seekBarShow = false;
			m_curveView.setVisibility(View.GONE);
			m_rgbFr.setVisibility(View.GONE);
			m_seekBarFr.setVisibility(View.GONE);
			m_icon.setVisibility(View.GONE);
		}
		m_colorSeekBar.setProgress(0);
		int progress = 0;
		if (m_seekBarFr != null && m_curAdjustData.m_type != BeautyAdjustType.NONE)
		{
			if (m_seekBarFr.getVisibility() == View.GONE)
			{
				m_seekBarFr.setVisibility(View.VISIBLE);
			}
			int max = m_colorSeekBar.getMax();
			progress = GetShowProgress(m_curAdjustData.m_type, m_curAdjustData.m_value);
			int lastProgress = m_colorSeekBar.getProgress();
			int newProgress = progress;
			if (lastProgress == newProgress)
			{
				ReLayoutSeekBarTip(newProgress, max);
			}
			m_colorSeekBar.setProgress(progress);
		}
	}

	protected int GetShowProgress(BeautyAdjustType type, float value)
	{
		int max = m_colorSeekBar.getMax();
		int progress = 0;
		if(type != null)
		{
			switch (type)
			{
				case BRIGHTNESS:
				case CONTRAST:
				case SATURABILITY:
				case BEAUTY:
				{
					progress = (int) (((value + 100) / 200f) * max + 0.5f);
					break;
				}
				case TEMPERATURE:
				{
					progress = (int) (((value + 1) / 2f) * max + 0.5f);
					break;
				}
				case SHARPEN:
				case BETTER:
				{
					progress = (int) (value / 100f * max + 0.5f);
					break;
				}
				case HUE:
				{
					progress = (int) ((value + 10) / 20f * max + 0.5f);
					break;
				}
				case HIGHTLIGHT:
				case SHADE:
				case DARKCORNER:
				case PARTICAL:
				case FADE:
				{
					progress = (int) (Math.abs(value) * 10 / 100f * max + 0.5f);
					break;
				}
				default:
					break;
			}
		}
		else{
			progress = (int)((value / 100f) * max + 0.5);
		}
		return progress;
	}

	protected int GetShowTipValue(int progress, BeautyAdjustType type)
	{
		int tip = progress;
		if(type != null)
		{
			switch (type)
			{
				case BRIGHTNESS:
				case CONTRAST:
				case SATURABILITY:
				case TEMPERATURE:
				case HUE:
				case BEAUTY:
				{
					tip = progress - 6;
					break;
				}
				default:
					break;
			}
		}
		return tip;
	}

	protected void ReLayoutSeekBarTip(int progress, int max)
	{
		int w = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40);
		int leftMargin = ShareData.PxToDpi_xhdpi(20);
		int seekBarThumbW = ShareData.PxToDpi_xhdpi(21);
		LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) m_seekkBarTip.getLayoutParams();
		ll.leftMargin = (int) ((w - (seekBarThumbW << 1)) * progress / (float) max + leftMargin + seekBarThumbW - ShareData.PxToDpi_xhdpi(35));
		m_seekkBarTip.setLayoutParams(ll);
		switch (m_filterType)
		{
			case FILTER:
			{
				String tip;
				if (progress > 0)
				{
					tip = "+" + progress;
				}
				else if (progress < 0)
				{
					tip = "" + progress;
				}
				else
				{
					tip = " " + progress;
				}
				m_seekkBarTip.setText(tip);
				break;
			}
			case ADJUST:
			{
				switch (m_curAdjustData.m_type)
				{
					case BRIGHTNESS:
					case CONTRAST:
					case SATURABILITY:
					case TEMPERATURE:
					case HUE:
					case BEAUTY:
					{
						int progress1 = progress - 6;
						String tip;
						if (progress1 > 0)
						{
							tip = "+" + progress1;
						}
						else if (progress1 < 0)
						{
							tip = "" + progress1;
						}
						else
						{
							tip = "  " + progress1;
						}
						m_seekkBarTip.setText(tip);
						break;
					}
					case SHARPEN:
					case BETTER:
					case DARKCORNER:
					case PARTICAL:
					case FADE:
					case HIGHTLIGHT:
					case SHADE:
					{
						String tip;
						if (progress > 0)
						{
							tip = "+" + progress;
						}
						else if (progress < 0)
						{
							tip = "" + progress;
						}
						else
						{
							tip = " " + progress;
						}
						m_seekkBarTip.setText(tip);
						break;
					}
					default:
						break;
				}
				break;
			}
		}
	}

	protected void LayoutColorList(View v, final boolean isUp, boolean hasAnim)
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
			fl.bottomMargin = m_resBarHeight;
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

	protected void SetSelItemByUri(int uri)
	{
		if(m_filterType == BeautyColorType.FILTER)
		{
			FilterRes filterRes = null;
			if(m_listDatas != null)
			{
				int size = m_listDatas.size();
				for(int i = 0; i < size; i++)
				{
					if(uri == m_listDatas.get(i).m_uri)
					{
						filterRes = (FilterRes)m_listDatas.get(i).m_ex;
						break;
					}
				}
			}
			if(m_colorMsg != null)
			{
				m_uiEnabled = false;
				m_colorMsg.m_filterData = filterRes;
				if(filterRes != null)
				{
					m_colorMsg.m_filterAlpha = filterRes.m_alpha;
				}
				m_colorMsg.m_thumb = m_org.copy(Bitmap.Config.ARGB_8888, true);
				Message msg = m_mainHandler.obtainMessage();
				msg.what = BeautifyHandler.MSG_COLOR_FILTER;
				msg.obj = m_colorMsg;
				m_mainHandler.sendMessage(msg);

				if(filterRes != null)
				{
					MyBeautyStat.onClickByRes(R.string.照片滤镜_选择滤镜);
					MyBeautyStat.onChooseMaterial(filterRes.m_tjId + "", R.string.照片滤镜);
					int progress = (int)((filterRes.m_alpha / 100f) * m_colorSeekBar.getMax() + 0.5);
					ReLayoutSeekBarTip(progress, m_colorSeekBar.getMax());
					m_colorSeekBar.setProgress(progress);
				}
			}
			m_curFilterUri = uri;
		}
	}

	protected void colorBtnClick(BeautyColorType type)
	{
		m_filterType = type;
		FrameLayout.LayoutParams fl;
		if (m_filterType == BeautyColorType.ADJUST)
		{
			if(m_filterBmp == null)
			{
				if(m_showImg != null)
				{
					m_filterBmp = m_showImg.copy(Bitmap.Config.ARGB_8888, true);
				}
				else
				{
					m_filterBmp = m_org.copy(Bitmap.Config.ARGB_8888, true);
				}
			}
			TongJi2.AddCountByRes(getContext(), R.integer.美化_滤镜_调整);
			MyBeautyStat.onClickByRes(R.string.照片滤镜_进入细节调整);
			adjustBtnClick(m_curAdjustData);
			int len = m_adjustItems.size();
			for(int i = 0; i < len; i ++)
			{
				if(m_curAdjustData.m_type.GetValue() == m_adjustItems.get(i).m_uri)
				{
					m_adjustList.SetSelByIndex(i);
					break;
				}
			}

			fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			fl.bottomMargin = m_resBarHeight + ShareData.PxToDpi_xhdpi(20);
			m_seekBarFr.setLayoutParams(fl);

			SetViewState(m_filterTypeList, false, false);
			SetViewState(m_adjustBar, true, true);
			SetViewState(m_resList, false, false);
		}
		else if (m_filterType == BeautyColorType.FILTER)
		{
			MyBeautyStat.onClickByRes(R.string.滤镜细节调整_退出细节调整);
			m_titleFr.setVisibility(View.VISIBLE);
			m_title.setText(getResources().getString(R.string.Filters));
			m_icon.setVisibility(View.VISIBLE);
			m_curveView.setVisibility(View.GONE);
			m_rgbFr.setVisibility(View.GONE);

			fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			fl.bottomMargin = m_resBarHeight;
			m_seekBarFr.setLayoutParams(fl);

			SetViewState(m_adjustBar, false, false);
			SetViewState(m_filterTypeList, true, false);
			SetViewState(m_resList, true, true);
			m_seekBarShow = false;
			m_seekBarFr.setVisibility(View.GONE);

			FilterRes colorData = m_colorMsg.m_filterData;
			if(colorData != null)
			{
				int progress = (int)((colorData.m_alpha / 100f) * m_colorSeekBar.getMax() + 0.5);
				ReLayoutSeekBarTip(progress, m_colorSeekBar.getMax());
				m_colorSeekBar.setProgress(progress);
			}
		}
	}

	protected Bitmap DoCurve()
	{
		Bitmap out = null;
		if(m_curInfo != null)
		{
			int infoSize = m_curInfo.size();
			ArrayList<Float> allPoints = new ArrayList<>();
			int count[] = new int[infoSize];
			for(int s = 0; s < infoSize; s ++)
			{
				CurveView2.CurveInfo info = m_curInfo.get(s);
				int size = info.m_ctrlPoints.size();
				for(int i = 0; i < size; i ++)
				{
					allPoints.add(info.m_ctrlPoints.get(i).x / m_curViewSize);
					allPoints.add((m_curViewSize - info.m_ctrlPoints.get(i).y) / m_curViewSize);
				}
				count[s] = size;
			}
			int size = allPoints.size();
			float[] controlPoints = new float[size];
			for(int i = 0; i < size; i ++)
			{
				controlPoints[i] = allPoints.get(i);
			}
//			long lasttime = System.currentTimeMillis();
			if(m_curveBmp == null && m_org != null)
			{
				m_curveBmp = Bitmap.createBitmap(m_org.getWidth(), m_org.getHeight(), Bitmap.Config.ARGB_8888);
			}
			if(m_showImg == null)
			{
				m_showImg = m_org.copy(Bitmap.Config.ARGB_8888, true);
			}
			out = filter.AdjustCurveAll(m_curveBmp, m_showImg, controlPoints, count);
//			long curtime = System.currentTimeMillis();
//			System.out.println("time: " + (curtime - lasttime));
		}
		else
		{
			out = m_showImg.copy(Bitmap.Config.ARGB_8888, true);
		}
		return out;
	}

	@Override
	protected void OnHideItem(int position)
	{
		DragListItemInfo itemInfo = m_listDatas.get(position);
		if(itemInfo != null)
		{
			FilterRes res = (FilterRes)itemInfo.m_ex;
			if(res.m_type != BaseRes.TYPE_LOCAL_RES)
			{
				MyBeautyStat.onClickByRes(R.string.照片滤镜_删除滤镜);
				MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.照片滤镜);
				m_listDatas.remove(position);
				m_listAdapter.removeItem(position);

				FilterResMgr2.getInstance().DeleteRes(getContext(), res);

				if(m_curFilterUri == res.m_id)
				{
					SetSelItemByUri(DragListItemInfo.URI_ORIGIN);
					if (m_seekBarFr.getVisibility() == View.VISIBLE)
					{
						LayoutColorList(m_bottomBar, true, true);
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
			fromPos = ResourceUtils.HasId(FilterResMgr2.getInstance().GetOrderArr(), itemInfo.m_uri);
		}
		itemInfo = m_listDatas.get(toPosition);
		if(itemInfo != null)
		{
			toPos = ResourceUtils.HasId(FilterResMgr2.getInstance().GetOrderArr(), itemInfo.m_uri);
		}
		ResourceUtils.ChangeOrderPosition(FilterResMgr2.getInstance().GetOrderArr(), fromPos, toPos);
		FilterResMgr2.getInstance().SaveOrderArr();
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
				if (m_curFilterUri != info.m_uri && m_curFilterUri != DragListItemInfo.URI_ORIGIN)
				{
					SetSelItemByUri(DragListItemInfo.URI_ORIGIN);
				}

				OnHeadClick(view, info, index);
				return;
			}
			if(info.m_uri == DragListItemInfo.URI_MGR)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_滤镜_更多按钮);
				MyBeautyStat.onClickByRes(R.string.照片滤镜_进入更多);
				HashMap<String, Object> params = new HashMap<>();
				params.put("type", ResType.FILTER);
				params.put("typeOnly", true);
				m_site.OnDownloadMore(params,getContext());
				return;
			}
			switch (info.m_style)
			{
				case NORMAL:
				{
					if (m_filterType == BeautyColorType.FILTER)
					{
						if (m_curFilterUri != info.m_uri)
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
								MyBeautyStat.onClickByRes(R.string.滤镜不透明度_收起滤镜不透明度);
								LayoutColorList(m_bottomBar, true, true);
							}
							if (flag && m_seekBarFr.getVisibility() == View.GONE)
							{
								LayoutColorList(m_bottomBar, false, true);
								MyBeautyStat.onClickByRes(R.string.照片滤镜_进入滤镜不透明度);
							}
						}
						if (info.m_uri == DragListItemInfo.URI_ORIGIN)
						{
							if (m_seekBarFr.getVisibility() == View.VISIBLE)
							{
								MyBeautyStat.onClickByRes(R.string.滤镜不透明度_收起滤镜不透明度);
								LayoutColorList(m_bottomBar, true, true);
							}
						}
						m_curFilterUri = info.m_uri;
					}
					break;
				}
				case NEED_DOWNLOAD:
				{
					if(info.m_ex instanceof ThemeRes)
					{
						TongJi2.AddCountByRes(getContext(), R.integer.美化_滤镜_主题推荐位);
						MyBeautyStat.onClickByRes(R.string.照片滤镜_打开推荐位);
						HashMap<String, Object> params = new HashMap<>();
						ThemeItemInfo itemInfo = MgrUtils.GetThemeItemInfoByUri(getContext(), ((ThemeRes)info.m_ex).m_id, ResType.FILTER);
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
						OpenRecommend(params);
//						m_site.OnRecommend(params);
					}
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
				FilterPage.this.removeView(m_introPage);
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
				if(m_curFilterUri != info.m_uri)
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

			TongJi2.AddCountByRes(getContext(), R.integer.美化_滤镜_大师介绍);
			MyBeautyStat.onClickByRes(R.string.照片滤镜_打开大师介绍页);

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("head_img", info.m_head);
			FilterRes data = (FilterRes) info.m_ex;
			params.put("top_img", data.m_coverImg);
			params.put("name", data.m_authorName);
			params.put("detail", data.m_authorInfo);
			params.put("intro", data.m_filterDetail);
			params.put("img_url", data.m_filterIntroUrl);
			if (info.m_isLock)
			{
				params.put("unlock_tag", Tags.UNLOCK_FILTER);
				params.put("lock", true);
				params.put("filter_id", info.m_uri);
			}
			params.put("share_url", data.m_shareUrl);
			params.put("share_title", data.m_shareTitle);

			if(view != null)
			{
				int[] location = new int[2];
				view.getLocationOnScreen(location);

				params.put("centerX",location[0]);
				params.put("centerY", location[1]);
				params.put("viewH", view.getHeight());
				params.put("viewW", view.getWidth());
			}
			params.put("pageId", R.string.照片滤镜_大师介绍页);
			OpenMasterPage(params);

			m_curFilterUri = info.m_uri;
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
				FilterPage.this.removeView(m_masterPage);
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

	protected CurveView.Callback m_curveCB = new CurveView.Callback()
	{

		@Override
		public void OnDown(PointF point, ArrayList<PointF> m_ctrlPoints)
		{
			m_curveView.ShowCoord(true);
			OnMove(point, m_ctrlPoints);
		}

		@Override
		public void OnMove(PointF point, ArrayList<PointF> m_ctrlPoints)
		{
			int x = 0, y = 0;

			if(point != null)
			{
				x = (int)(point.x / m_curViewSize * 255);
				y = (int)((m_curViewSize - point.y) / m_curViewSize * 255);
			}
			m_curveView.SetCoord(x, y);

			m_curveBmp = DoCurve();
			m_view.setImageBitmap(m_curveBmp);
		}

		@Override
		public void OnUp(PointF point, ArrayList<PointF> m_ctrlPoints)
		{
//			m_curveView.ShowCoord(false);
			int x = 0, y = 0;

			if(point != null)
			{
				x = (int)(point.x / m_curViewSize * 255);
				y = (int)((m_curViewSize - point.y) / m_curViewSize * 255);
			}
			m_curveView.SetCoord(x, y);
			m_curveBmp = DoCurve();
			m_view.setImageBitmap(m_curveBmp);
		}
	};

	protected SimpleBtnList100.Callback m_filterListCB = new SimpleBtnList100.Callback()
	{

		@Override
		public void OnClick(SimpleBtnList100.Item view, int index)
		{
			if (m_uiEnabled)
			{
				BeautyColorType type = (BeautyColorType) (((SimpleListItem) view).m_ex);
				if (m_filterTypeList != null)
				{
					m_filterTypeList.SetSelByIndex(index);
				}
				if (m_filterType != type || m_filterType != BeautyColorType.FILTER)
				{
					colorBtnClick(type);
				}
			}
		}

	};

	protected SimpleBtnList100.Callback m_adjustBtnListCB = new SimpleBtnList100.Callback()
	{

		@Override
		public void OnClick(SimpleBtnList100.Item view, int index)
		{
			if (m_uiEnabled)
			{
				BeautifyResMgr.AdjustData data = (BeautifyResMgr.AdjustData) (((SimpleListItem) view).m_ex);

				if(m_curAdjustData.m_type != data.m_type)
				{
					TongJi2.AddCountByRes(getContext(), ((SimpleListItem) view).m_tjID);
					MyBeautyStat.onClickByRes(((SimpleListItem) view).m_shenceTjID);
					if (m_adjustList != null)
					{
						m_adjustList.SetSelByIndex(index);
						m_adjustList.ScrollToCenter(true);
					}
					m_adjustTip = ((SimpleListItem) view).m_title;
					adjustBtnClick(data);
				}
				else
				{
					if(data.m_type != BeautyAdjustType.CURVE)
					{
						if(m_seekBarShow == true)
						{
							m_seekBarShow = false;
							m_seekBarFr.setVisibility(View.GONE);
							String text = getResources().getString(R.string.Adjust);
							m_title.setText(text);
							m_adjustList.SetSelByIndex(-1);
						}
						else
						{
							m_seekBarShow = true;
							m_titleFr.setVisibility(View.VISIBLE);
							m_title.setText(m_adjustTip);
							m_seekBarFr.setVisibility(View.VISIBLE);
							m_adjustList.SetSelByIndex(index);
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
			if(m_uiEnabled)
			{
				int progress = seekBar.getProgress();
				int maxP = seekBar.getMax();
				if (m_colorMsg != null)
				{
					switch (m_filterType)
					{
						case FILTER:
						{
							if (m_colorMsg.m_filterData != null)
							{
								m_colorMsg.m_filterAlpha = (int) ((progress / (float) maxP) * 100 + 0.5f);
							}
							m_uiEnabled = false;
//							SetWaitUI(true, "正在处理");
							m_colorMsg.m_thumb = m_org.copy(Bitmap.Config.ARGB_8888, true);
							Message msg = m_mainHandler.obtainMessage();
							msg.what = BeautifyHandler.MSG_COLOR_FILTER;
							msg.obj = m_colorMsg;
							m_mainHandler.sendMessage(msg);
							break;
						}
						case ADJUST:
						{
							switch (m_curAdjustData.m_type)
							{
								case BRIGHTNESS:
								{
									m_curAdjustData.m_value = -100 + (progress * 2 / (float) maxP) * 100;
									break;
								}
								case CONTRAST:
								{
									m_curAdjustData.m_value = -100 + (int) ((progress * 2 / (float) maxP) * 100 + 0.5f);
									break;
								}
								case SATURABILITY:
								{
									m_curAdjustData.m_value = -100 + (int) ((progress * 2 / (float) maxP) * 100 + 0.5f);
									break;
								}
								case TEMPERATURE:
								{
									m_curAdjustData.m_value = -1 + progress / (float) maxP * 2;
									break;
								}
								case SHARPEN:
								{
									m_curAdjustData.m_value = (int) ((progress / (float) maxP) * 100 + 0.5f);
									break;
								}
								case BETTER:
								{
									m_curAdjustData.m_value = (int) ((progress / (float) maxP) * 100 + 0.5f);
									break;
								}
								case HIGHTLIGHT:
								{
									m_curAdjustData.m_value = (progress / (float) maxP) * -10;
									break;
								}
								case SHADE:
								{
									m_curAdjustData.m_value = (progress / (float) maxP) * 10;
									break;
								}
								case HUE:
								{
									m_curAdjustData.m_value = -10 + (progress * 2 / (float) maxP) * 10;
									break;
								}
								case DARKCORNER:
								{
									m_curAdjustData.m_value = (progress / (float) maxP) * 10;
									break;
								}
								case PARTICAL:
								{
									m_curAdjustData.m_value = (progress / (float) maxP) * 10;
									break;
								}
								case FADE:
								{
									m_curAdjustData.m_value = (progress / (float) maxP) * 10;
									break;
								}
								case BEAUTY:
								{
									m_curAdjustData.m_value = -50 + (int) ((progress * 2 / (float) maxP) * 50 + 0.5f);
									break;
								}
								default:
									break;
							}

							if (m_uiEnabled)
							{
								m_uiEnabled = false;
//							SetWaitUI(true, "正在处理");
								m_colorMsg.m_thumb = m_filterBmp.copy(Bitmap.Config.ARGB_8888, true);
								Message msg = m_mainHandler.obtainMessage();
								msg.what = BeautifyHandler.MSG_COLOR_ADJUST;
								msg.obj = m_colorMsg;
								m_mainHandler.sendMessage(msg);
							}
							break;
						}
						default:
							break;
					}
				}
			}

		}
	};

	protected class MyBtnLst extends BaseBeautifyPage.MyBtnLst
	{
		@Override
		public void onClick(View v, boolean fromUser)
		{
			if(v == m_backBtn)
			{
				onBack();
			}
			else if(v == m_okBtn)
			{
				m_uiEnabled = false;
				SetWaitUI(true, getResources().getString(R.string.processing));

				m_params.remove("curBmp");

				TongJi2.AddCountByRes(getContext(), R.integer.美化_滤镜_保存);
				MyBeautyStat.onClickByRes(R.string.照片滤镜_保存滤镜美化);
				String filterTjId = "0000";
				int filterAlpha = 0;
				ArrayList<MyBeautyStat.MgrInfo> tjInfos = new ArrayList<>();
				boolean curve = false;
				if(m_colorMsg != null)
				{
					if(m_colorMsg.m_filterData != null)
					{
						TongJi2.AddCountById(m_colorMsg.m_filterData.m_tjId + "");
						filterTjId = m_colorMsg.m_filterData.m_tjId + "";
						filterAlpha = GetShowProgress(null, m_colorMsg.m_filterAlpha);
					}
					if(m_colorMsg.m_adjustData != null)
					{
   						for(BeautifyResMgr.AdjustData data : m_colorMsg.m_adjustData)
						{
							if(data.m_type.GetValue() != BeautyAdjustType.CURVE.GetValue() && data.m_type.GetValue() != BeautyAdjustType.NONE.GetValue())
							{
								MyBeautyStat.MgrInfo info = new MyBeautyStat.MgrInfo();
								int temp = GetShowProgress(data.m_type, data.m_value);
								info.alpha = GetShowTipValue(temp, data.m_type);
								info.id = data.m_tjId;
								tjInfos.add(info);
							}
						}
					}
					if(m_curInfo != null){
						curve = true;
					}
				}

				MyBeautyStat.onUseFilter(tjInfos, curve, filterTjId, filterAlpha, R.string.照片滤镜);

				Bitmap mirror = ImageUtil.GetScreenBmp((Activity)getContext(), ShareData.m_screenWidth, ShareData.m_screenHeight);
				releaseMem();
				showPageMirror(mirror);

				BeautifyHandler.ColorMsg info = m_colorMsg;
				info.m_curInfo = m_curInfo;
				info.m_inImgs = m_orgInfo;
				String path = m_orgInfo.image;
				info.m_orgThumbPath = path;

				Message msg = m_mainHandler.obtainMessage();
				msg.what = BeautifyHandler.MSG_COLOR_CACHE;
				msg.obj = info;
				m_mainHandler.sendMessage(msg);
			}
			else if(v == m_adjustDownBtn)
			{
				SetViewState(m_seekBarFr, false, false);
				if (m_filterTypeList != null)
				{
					m_filterTypeList.SetSelByIndex(0);
				}
				colorBtnClick(BeautyColorType.FILTER);
			}
			else if(v == m_btnRGB)
			{
				m_btnRGB.OnChoose(true);
				m_btnR.OnChoose(false);
				m_btnG.OnChoose(false);
				m_btnB.OnChoose(false);
				m_curveView.SetMode(CurveView.MODE_RGB);
			}
			else if(v == m_btnR)
			{
				m_btnRGB.OnChoose(false);
				m_btnR.OnChoose(true);
				m_btnG.OnChoose(false);
				m_btnB.OnChoose(false);
				m_curveView.SetMode(CurveView.MODE_RED);
			}
			else if(v == m_btnG)
			{
				m_btnRGB.OnChoose(false);
				m_btnR.OnChoose(false);
				m_btnG.OnChoose(true);
				m_btnB.OnChoose(false);
				m_curveView.SetMode(CurveView.MODE_GREEN);
			}
			else if(v == m_btnB)
			{
				m_btnRGB.OnChoose(false);
				m_btnR.OnChoose(false);
				m_btnG.OnChoose(false);
				m_btnB.OnChoose(true);
				m_curveView.SetMode(CurveView.MODE_BLUE);
			}
			else if(v == m_scanBtn)
			{
				if(m_curveShow)
				{
					m_curveShow = false;
					m_scanBtn.setImageResource(R.drawable.beauty_curve_hide);
					m_curveView.setVisibility(View.GONE);
				}
				else
				{
					m_curveShow = true;
					m_scanBtn.setImageResource(R.drawable.beauty_curve_show);
					m_curveView.setVisibility(View.VISIBLE);
				}
			}
			else if(v == m_titleFr && m_helpFlag)
			{
				m_helpFlag = false;
				if(m_filterType == BeautyColorType.ADJUST && m_curAdjustData != null && m_curAdjustData.m_type == BeautyAdjustType.CURVE)
				{
					HashMap<String, Object> params = new HashMap<>();
					InitBkImg();
					params.put("img", m_bkBmp);
					m_site.OpenHelpPage(params,getContext());
				}
				else if(m_filterType == BeautyColorType.FILTER)
				{
					HashMap<String, Object> params = new HashMap<>();
					InitBkImg();
					params.put("img", m_bkBmp);
					m_site.OpenFilterHelpPage(params,getContext());
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
				case BeautifyHandler.MSG_COLOR_CACHE:
				{
					BeautifyHandler.ColorMsg params = (BeautifyHandler.ColorMsg) msg.obj;
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
					m_params.put("viewh", m_frH);
					m_site.onBack(m_params,getContext());
					break;
				}
				case BeautifyHandler.MSG_COLOR_ADJUST:
				{
					BeautifyHandler.ColorMsg params = (BeautifyHandler.ColorMsg) msg.obj;
					msg.obj = null;
					if (m_showImg != null && m_showImg != params.m_thumb)
					{
						m_showImg.recycle();
						m_showImg = null;
					}
					m_showImg = params.m_thumb;
					m_uiEnabled = true;
					SetWaitUI(false, "");
					if(m_view != null)
					{
						m_curveBmp = DoCurve();
						m_view.setImageBitmap(m_curveBmp);
					}

					break;
				}
				case BeautifyHandler.MSG_COLOR_FILTER:
				{
					BeautifyHandler.ColorMsg params = (BeautifyHandler.ColorMsg) msg.obj;
					msg.obj = null;

					if(m_filterBmp != null)
					{
						m_filterBmp.recycle();
						m_filterBmp = null;
					}
					m_filterBmp = params.m_thumb.copy(Bitmap.Config.ARGB_8888, true);
					m_colorMsg.m_thumb = params.m_thumb;
					Message msg1 = m_mainHandler.obtainMessage();
					msg1.what = BeautifyHandler.MSG_COLOR_ADJUST;
					msg1.obj = m_colorMsg;
					m_mainHandler.sendMessage(msg1);
				}
			}
		}
	}
}
