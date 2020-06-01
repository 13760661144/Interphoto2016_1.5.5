package cn.poco.beautify.page;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.beautify.BeautifyClipType;
import cn.poco.beautify.BeautifyHandler;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.MyButtons;
import cn.poco.beautify.MyClipView;
import cn.poco.beautify.ScaleAttached;
import cn.poco.beautify.SimpleListItem;
import cn.poco.beautify.site.ClipPageSite;
import cn.poco.display.ClipView;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.framework.BaseSite;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv100.SimpleBtnList100;

/**
 * 裁剪
 */
public class ClipPage extends BaseBeautifyPage
{
	private static final String TAG = "裁剪";
	private ClipPageSite m_site;

	protected MyClipView m_view;
	protected ImageView m_rot90Btn;

	protected HorizontalScrollView m_clipTypeFr; //裁剪模式选择列
	protected MyButtons m_clipBtn; //裁剪
	protected MyButtons m_rotateBtn; //缩放
	protected MyButtons m_adjustH; //水平调整
	protected MyButtons m_adjustV; //垂直调整

	protected SimpleBtnList100 m_clipBtnList;    //裁剪列表
	protected FrameLayout m_seekBarFr;
	protected ScaleAttached m_seekBar;

	protected MyBtnLst m_btnLst;
	protected HandlerThread m_imageThread;
	protected BeautifyHandler m_mainHandler;
	protected UIHandler m_UIHandler;
	protected float m_rotatePro;
	protected float m_adjustHPro;
	protected float m_adjustVPro;

	protected int m_curClipIndex;
	protected String m_curClipRatioStr;
	protected BeautifyClipType m_mode;

	public ClipPage(Context context, BaseSite site)
	{
		super(context, site);
		m_site = (ClipPageSite)site;
		InitData();
		InitUI();
		TongJiUtils.onPageStart(getContext(), TAG);
		MyBeautyStat.onPageStartByRes(R.string.照片裁剪);
	}

	@Override
	public void SetData(HashMap<String, Object> params)
	{
		super.SetData(params);
		if(params != null && m_org != null)
		{
			m_view.SetImg(m_orgInfo.image, m_org);
			m_view.invalidate();
		}
	}

	@Override
	protected ArrayList<DragListItemInfo> InitListDatas()
	{
		return null;
	}

	@Override
	protected void SetShowViewAnim()
	{
		SetShowViewAnim(m_view);
	}

	@Override
	protected void InitShowView()
	{
		m_view = new MyClipView((Activity) getContext(), m_frW, m_frH, m_clipCallback);
		m_view.setAreaCount(6);
		m_view.def_mask_color = 0x99000000;
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(m_frW, m_frH);
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		fl.topMargin = m_topBarHeight;
		m_view.setLayoutParams(fl);
		this.addView(m_view, 0);
	}

	@Override
	public void onBack()
	{
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
			m_view.ClearAll();
			m_view = null;
		}
		if(m_clipBtnList != null)
		{
			m_clipBtnList.ClearAll();
			m_clipBtnList = null;
		}
		if (m_imageThread != null)
		{
			m_imageThread.quit();
			m_imageThread = null;
		}
		this.removeAllViews();
		TongJiUtils.onPageEnd(getContext(), TAG);
		MyBeautyStat.onClickByRes(R.string.照片裁剪_退出照片裁剪);
		MyBeautyStat.onPageEndByRes(R.string.照片裁剪);
	}

	@Override
	protected boolean canDelete(int position)
	{
		return false;
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

	protected void InitData()
	{
		super.InitData();
		m_btnLst = new MyBtnLst();
		m_uiEnabled = true;
		m_curClipIndex = 0;
		m_mode = BeautifyClipType.CLIP;

		m_resBarHeight = ShareData.PxToDpi_xhdpi(150);

		m_rotatePro = 45;
		m_adjustHPro = 45;
		m_adjustVPro = 45;

		m_UIHandler = new UIHandler();
		m_imageThread = new HandlerThread("clip_thread");
		m_imageThread.start();
		m_mainHandler = new BeautifyHandler(m_imageThread.getLooper(), getContext(), m_UIHandler);
	}

	protected void InitUI()
	{
		FrameLayout.LayoutParams fl;
		super.InitUI();
		fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth, LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM;
		m_bottomBar.setLayoutParams(fl);

		m_clipBtnList = new SimpleBtnList100(getContext());
//		m_clipBtnList.setBackgroundColor(0xff000000);
		ArrayList<SimpleBtnList100.Item> items = BeautifyResMgr.getClipBtnList(getContext());
		m_clipBtnList.SetData(items, m_clipBtnListCB);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_resBarHeight);
		fl.gravity = Gravity.BOTTOM;
		fl.bottomMargin = m_bottomBarHeight;
		m_clipBtnList.setLayoutParams(fl);
		m_bottomBar.addView(m_clipBtnList);
		m_clipBtnList.SetSelByIndex(m_curClipIndex);
		m_clipBtnListCB.OnClick(items.get(m_curClipIndex), m_curClipIndex);

		m_clipTypeFr = new HorizontalScrollView(getContext());
//		m_clipTypeFr.setBackgroundColor(0xff000000);
		m_clipTypeFr.setHorizontalScrollBarEnabled(false);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_bottomBarHeight);
		fl.gravity = Gravity.BOTTOM | Gravity.LEFT;
		m_clipTypeFr.setLayoutParams(fl);
		m_bottomBar.addView(m_clipTypeFr);
		{
			LinearLayout btnList = new LinearLayout(getContext());
			btnList.setGravity(Gravity.CENTER_VERTICAL);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
			btnList.setLayoutParams(fl);
			m_clipTypeFr.addView(btnList);
			{
				LinearLayout.LayoutParams ll;
				int width = ShareData.m_screenWidth / 4;
				m_clipBtn = new MyButtons(getContext(), R.drawable.beautify_clip_clip_out, R.drawable.beautify_clip_clip_over);
				m_clipBtn.setOnClickListener(m_btnLst);
				m_clipBtn.SetSelect(true);
				ll = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
				ll.weight = 1;
				m_clipBtn.setLayoutParams(ll);
				btnList.addView(m_clipBtn);

				m_rotateBtn = new MyButtons(getContext(), R.drawable.beautify_clip_rotation_out, R.drawable.beautify_clip_rotation_over);
				m_rotateBtn.setOnClickListener(m_btnLst);
				ll = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
				ll.weight = 1;
				m_rotateBtn.setLayoutParams(ll);
				btnList.addView(m_rotateBtn);

				m_adjustH = new MyButtons(getContext(), R.drawable.beautify_clip_adhjust_h_out, R.drawable.beautify_clip_adhjust_h_over);
				m_adjustH.setOnClickListener(m_btnLst);
				ll = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
				ll.weight = 1;
				m_adjustH.setLayoutParams(ll);
				btnList.addView(m_adjustH);

				m_adjustV = new MyButtons(getContext(), R.drawable.beautify_clip_adhjust_v_out, R.drawable.beautify_clip_adhjust_v_over);
				m_adjustV.setOnClickListener(m_btnLst);
				ll = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
				ll.weight = 1;
				m_adjustV.setLayoutParams(ll);
				btnList.addView(m_adjustV);
			}
		}

		m_seekBarFr = new FrameLayout(getContext());
		m_seekBarFr.setVisibility(View.GONE);
		fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, m_resBarHeight);
		fl.gravity = Gravity.BOTTOM;
		fl.bottomMargin = m_bottomBarHeight;
		m_seekBarFr.setLayoutParams(fl);
		this.addView(m_seekBarFr);
		{
			int w = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(180);
			m_seekBar = new ScaleAttached(getContext());
			m_seekBar.SetMax(90);
			m_seekBar.SetProgress(45);
			m_seekBar.SetShowDotNum(27);
			m_seekBar.SetDivide(10);
			m_seekBar.setTextSize(10);
			m_seekBar.setTextColor(0xffffffff);
			m_seekBar.SetTextBottomMargin(ShareData.PxToDpi_xhdpi(25));
			m_seekBar.SetOnAttachedChangeListener(m_seekbarListener);
			fl = new FrameLayout.LayoutParams(w, LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
			fl.leftMargin = ShareData.PxToDpi_xhdpi(40);
			m_seekBar.setLayoutParams(fl);
			m_seekBarFr.addView(m_seekBar);

			m_rot90Btn = new ImageView(getContext());
			m_rot90Btn.setImageResource(R.drawable.framework_img_rotation_btn);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
			fl.topMargin = ShareData.PxToDpi_xhdpi(25);
			fl.rightMargin = ShareData.PxToDpi_xhdpi(20);
			m_rot90Btn.setLayoutParams(fl);
			m_seekBarFr.addView(m_rot90Btn);
			m_rot90Btn.setOnClickListener(m_btnLst);
		}
	}

	protected void ReLayoutSeekBarFr()
	{
		int w = 0;
		int leftMargin = 0;
		FrameLayout.LayoutParams fl;
		switch(m_mode)
		{
			case ROTATION:
			{
				m_seekBar.SetMax(90);
				m_seekBar.SetDivide(10);
				m_seekBar.SetShowDotNum(32);
				m_seekBar.SetProgress(m_rotatePro);
				w = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(180);
				leftMargin = ShareData.PxToDpi_xhdpi(40);
				break;
			}
			case ADJUST_H:
			{
				m_seekBar.SetMax(90);
				m_seekBar.SetDivide(5);
				m_seekBar.SetShowDotNum(40);
				m_seekBar.SetProgress(m_adjustHPro);
				w = ShareData.m_screenWidth;
				leftMargin = 0;
				break;
			}
			case ADJUST_V:
			{
				m_seekBar.SetMax(90);
				m_seekBar.SetDivide(5);
				m_seekBar.SetShowDotNum(40);
				m_seekBar.SetProgress(m_adjustVPro);
				w = ShareData.m_screenWidth;
				leftMargin = 0;
				break;
			}
		}

		fl = new FrameLayout.LayoutParams(w, ShareData.PxToDpi_xhdpi(20));
		fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
		fl.leftMargin = leftMargin;
		m_seekBar.setLayoutParams(fl);
	}

	@Override
	protected void OnHideItem(int position)
	{

	}

	@Override
	protected void OnChangeItem(int fromPosition, int toPosition)
	{

	}

	@Override
	public void OnItemClick(View view, DragListItemInfo info, int index)
	{

	}

	@Override
	public void OnHeadClick(View view, DragListItemInfo info, int index)
	{

	}

	protected ScaleAttached.OnAttachedChangeListener m_seekbarListener = new ScaleAttached.OnAttachedChangeListener()
	{
		@Override
		public void OnProgress(float progress, int max)
		{
			if(m_mode == BeautifyClipType.ROTATION)
			{
				m_rotatePro = progress;
				int mid = (int)(max / 2f + 0.5f);
				float degree = progress - mid;
				m_view.SetMicroDegree(degree);
//				System.out.println("degree: " + degree);
				m_view.invalidate();
			}
			else if(m_mode == BeautifyClipType.ADJUST_H)
			{
				m_adjustHPro = progress;
				int mid = (int)(max / 2f + 0.5f);
				float degree = (progress - mid) / 2f;
				m_view.SetDegreeH(degree);
				m_view.invalidate();
			}
			else if(m_mode == BeautifyClipType.ADJUST_V)
			{
				m_adjustVPro = progress;
				int mid = (int)(max / 2f + 0.5f);
				float degree = (progress - mid) / 2f;
				m_view.SetDegreeV(degree);
				m_view.invalidate();
			}
		}
	};

	protected SimpleBtnList100.Callback m_clipBtnListCB = new SimpleBtnList100.Callback()
	{

		@Override
		public void OnClick(SimpleBtnList100.Item view, int index)
		{
			if (m_uiEnabled && m_view != null)
			{
				if (m_clipBtnList != null) {
					m_clipBtnList.SetSelByIndex(index);
				}
				if (((SimpleListItem) view).m_tjID > 0) {
					TongJi2.AddCountByRes(getContext(), ((SimpleListItem) view).m_tjID);
				}
				if(((SimpleListItem) view).m_shenceTjID > 0){
					MyBeautyStat.onClickByRes(((SimpleListItem) view).m_shenceTjID);
				}
				m_view.SetClipWHScale((Float) ((SimpleListItem) view).m_ex);
				m_view.invalidate();
				m_curClipIndex = index;
				m_curClipRatioStr = ((SimpleListItem) view).m_shenceTjStr;
			}
		}
	};

	protected ClipView.Callback m_clipCallback = new ClipView.Callback()
	{

		@Override
		public Bitmap MakeShowImg(Object info, int frW, int frH)
		{
			return MakeOutputImg(info, frW, frH);
		}

		@Override
		public Bitmap MakeOutputImg(Object info, int outW, int outH)
		{
			Bitmap out = BeautifyHandler.MakeBmp(getContext(), info, outW, outH);

			return out;
		}
	};

	@Override
	protected MyBtnLst GetBtnLst()
	{
		return m_btnLst;
	}

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
				MyBeautyStat.onClickByRes(R.string.照片裁剪_保存裁剪);
				boolean rotate = m_rotatePro == 45;
				boolean adjustH = m_adjustHPro == 45;
				boolean adjustV = m_adjustVPro == 45;
				MyBeautyStat.onUseClip(m_curClipRatioStr, !rotate, !adjustH, !adjustV);
				m_org = m_view.GetClipBmp(DEF_IMG_SIZE);
				m_view.ClearAll();
				m_params.remove("curBmp");

				m_uiEnabled = false;
				SetWaitUI(true, getResources().getString(R.string.processing));
				BeautifyHandler.InitMsg info = new BeautifyHandler.InitMsg();
				info.m_inImgs = m_orgInfo;
				info.m_thumb = m_org;
				Message msg = m_mainHandler.obtainMessage();
				msg.what = BeautifyHandler.MSG_CACHE;
				msg.obj = info;
				m_mainHandler.sendMessage(msg);
			}
			else if(v == m_clipBtn)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_剪裁_剪裁比例);
				MyBeautyStat.onClickByRes(R.string.照片裁剪_切换裁剪);
				m_mode = BeautifyClipType.CLIP;
				m_clipBtn.SetSelect(true);
				m_rotateBtn.SetSelect(false);
				m_adjustH.SetSelect(false);
				m_adjustV.SetSelect(false);
				m_title.setText(getResources().getString(R.string.Crop));

				m_clipBtnList.setVisibility(View.VISIBLE);
				m_seekBarFr.setVisibility(View.GONE);
			}
			else if(v == m_rotateBtn)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_剪裁_旋转);
				MyBeautyStat.onClickByRes(R.string.照片裁剪_切换旋转);
				m_mode = BeautifyClipType.ROTATION;
				m_clipBtn.SetSelect(false);
				m_rotateBtn.SetSelect(true);
				m_adjustH.SetSelect(false);
				m_adjustV.SetSelect(false);
				m_rot90Btn.setVisibility(View.VISIBLE);
				m_clipBtnList.setVisibility(View.GONE);
				m_seekBarFr.setVisibility(View.VISIBLE);
				m_title.setText(getResources().getString(R.string.Rotate));

				ReLayoutSeekBarFr();
			}
			else if(v == m_adjustH)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_剪裁_水平矫正);
				MyBeautyStat.onClickByRes(R.string.照片裁剪_切换水平校正);
				m_mode = BeautifyClipType.ADJUST_H;
				m_clipBtn.SetSelect(false);
				m_rotateBtn.SetSelect(false);
				m_adjustH.SetSelect(true);
				m_adjustV.SetSelect(false);
				m_rot90Btn.setVisibility(View.GONE);
				m_clipBtnList.setVisibility(View.GONE);
				m_seekBarFr.setVisibility(View.VISIBLE);
				m_title.setText(getResources().getString(R.string.HorizontalCorrection));

				ReLayoutSeekBarFr();
			}
			else if(v == m_adjustV)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_剪裁_垂直矫正);
				MyBeautyStat.onClickByRes(R.string.照片裁剪_切换垂直校正);
				m_mode = BeautifyClipType.ADJUST_V;
				m_clipBtn.SetSelect(false);
				m_rotateBtn.SetSelect(false);
				m_adjustH.SetSelect(false);
				m_adjustV.SetSelect(true);
				m_rot90Btn.setVisibility(View.GONE);
				m_clipBtnList.setVisibility(View.GONE);
				m_seekBarFr.setVisibility(View.VISIBLE);
				m_title.setText(getResources().getString(R.string.VerticalCorrection));

				ReLayoutSeekBarFr();
			}
			else if(v == m_rot90Btn)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_剪裁_90度旋转);
				MyBeautyStat.onClickByRes(R.string.旋转_旋转90度);
				m_view.AnimRotate(90);
				m_view.invalidate();
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
					m_params.put("viewh", m_frH);
					m_site.onBack(m_params,getContext());
					break;
				}
			}
		}
	}
}
