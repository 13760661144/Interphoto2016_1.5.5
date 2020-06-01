package cn.poco.beautify.page;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.MaterialMgr2.MgrUtils;
import cn.poco.MaterialMgr2.ThemeIntroPage;
import cn.poco.MaterialMgr2.ThemeItemInfo;
import cn.poco.MaterialMgr2.site.ThemeIntroPageSite2;
import cn.poco.Text.ColorChangeLayout1;
import cn.poco.Text.EditableTextView;
import cn.poco.Text.JsonParser;
import cn.poco.Text.MyTextInfo;
import cn.poco.Text.Painter;
import cn.poco.Text.ShapeEx4;
import cn.poco.Text.ShapeEx5;
import cn.poco.beautify.BeautifyHandler;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.BeautifyViewV3;
import cn.poco.beautify.DeleteDlg;
import cn.poco.beautify.FontDownloadDlg;
import cn.poco.beautify.InputDialog;
import cn.poco.beautify.MySeekBar2;
import cn.poco.beautify.MySimpleBtnList;
import cn.poco.beautify.MySimpleListItem;
import cn.poco.beautify.SimpleListItem;
import cn.poco.beautify.TextAddDlg;
import cn.poco.beautify.site.TextPageSite;
import cn.poco.camera.ImageFile2;
import cn.poco.camera.RotationImg2;
import cn.poco.display.CoreViewV3;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.draglistview.MyDragItemAdapter;
import cn.poco.framework.BaseSite;
import cn.poco.framework.FileCacheMgr;
import cn.poco.framework.SiteID;
import cn.poco.graphics.ShapeEx;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.login.util.UserMgr;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.resource.MyLogoRes;
import cn.poco.resource.MyLogoResMgr;
import cn.poco.resource.ResType;
import cn.poco.resource.ResourceUtils;
import cn.poco.resource.TextRes;
import cn.poco.resource.TextResMgr2;
import cn.poco.resource.ThemeRes;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv100.SimpleBtnList100;
import cn.poco.utils.FileUtil;
import cn.poco.utils.ImageUtil;
import cn.poco.utils.InterphotoDlg;
import cn.poco.utils.MyNetCore;
import cn.poco.utils.Utils;
import cn.poco.watermarksync.manager.WatermarkSyncManager;
import cn.poco.watermarksync.model.EditableWatermark;
import cn.poco.watermarksync.model.NotEditableWatermark;
import cn.poco.watermarksync.model.Watermark;
import cn.poco.zip.Zip;


/**
 * 文字
 */
public class TextPage extends BaseBeautifyPage
{
	private static final String TAG = "文字";
	private static final int TEXT_ALPHA = 2;
	private static final int TEXT_SHADOW_ALPHA = 4;
	protected TextPageSite m_site;
	private int mInputMode;

	protected ArrayList<ShapeEx> m_texts;	//记录上一次文字

	protected int m_curTextClassify = 1;
	protected int m_curTextUri = -1;
	protected String m_curMyTextUri = "";
	protected boolean m_uriInClassify = true;

	protected EditableTextView m_view;

	protected SimpleBtnList100 m_textBtnList;    //文字分类列表
	protected LinearLayout m_textMyBtnFr;
	protected MySimpleBtnList m_myResList;    //文字（我的）
	protected ImageView m_myFrCloseBtn;
	protected boolean m_isMyDeleteShow = false;

	protected LinearLayout m_textOperateFr;
	protected LinearLayout m_operateTitleFr;
	protected ImageView m_colorChooserBtn;
	protected ImageView m_textAlphaBtn;
	protected ImageView m_shadowAlphaBtn;
	protected FrameLayout m_operateContentFr;
	protected ColorChangeLayout1 m_colorChooser;
	protected LinearLayout m_seekBarFr;
	protected MySeekBar2 m_seekBar;
	protected TextView m_seekkBarTip;
	protected InputDialog m_inputFr;
	protected int m_alphaMode = -1;
	protected int m_color;
	protected int m_shadowAlpha = 0;

	protected HandlerThread m_imageThread;
	protected BeautifyHandler m_mainHandler;
	protected UIHandler m_UIHandler;
	protected MyBtnLst m_btnLst = new MyBtnLst();

	protected FontDownloadDlg m_fontDownloadDlg;
	protected TextAddDlg m_textAddDlg;
	protected boolean m_isMyText = false;
	protected boolean m_isInputFrShow;
	protected float m_upY = 0; //手指弹起是y的坐标
	protected DeleteDlg m_deleteTip;
	private InterphotoDlg m_loginTip;

	private ThemeIntroPage m_introPage;

	public TextPage(Context context, BaseSite site)
	{
		super(context, site);
		m_site = (TextPageSite)site;
		mInputMode = ((Activity)getContext()).getWindow().getAttributes().softInputMode;
		((Activity)getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		InitData();
		InitUI();
		TongJiUtils.onPageStart(getContext(), TAG);
	}

	/**
	 *
	 * @param params
	 * imgs RotationImg2[]/ImageFile2
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		super.SetData(params);
		if(params != null && m_view != null)
		{
			if(m_selUri != -1)
			{
				int classify = TextResMgr2.getInstance().GetClassifyIdByResId(m_selUri);
				m_curTextClassify = classify == -1 ? 1 : classify;
			}
			ArrayList<SimpleBtnList100.Item> datas = m_textBtnList.GetDatas();
			if(datas != null)
			{
				int index = -1;
				int size = datas.size();
				for (int i = 0; i < size; i++)
				{
					if (m_curTextClassify == datas.get(i).m_uri)
					{
						index = i;
						break;
					}
				}
				m_textBtnList.SetSelByIndex(index);
			}
			SetTitle();
			InitResList(false);
			m_view.SetLayoutMode(CoreViewV3.LAYOUT_MODE_WRAP_TOP);
			m_view.SetImg(m_orgInfo.image, m_org);
			m_view.UpdateUI();

			if(TagMgr.CheckTag(getContext(), "first_enter_text"))
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
							TagMgr.SetTag(getContext(), "first_enter_text");
						}
					}
				}, 400);
			}
		}
	}

	@Override
	protected void InitShowView()
	{
		m_view = new EditableTextView(getContext(), m_frW, m_frH);
		m_view.setOnClickListener(m_btnLst);
		m_view.setBackgroundColor(0xff0e0e0e);
		m_view.def_rotation_res = R.drawable.photofactory_pendant_scale_btn;
		m_view.def_delete_res = R.drawable.photofactory_pendant_del_btn;
		m_view.def_save_res = R.drawable.photofactory_pendant_save_btn;
		m_view.def_divide_res = R.drawable.photofactory_pendant_divide_btn;
		m_view.def_merge_res = R.drawable.photofactory_pendant_merge_btn;
		m_view.def_absorb_res = R.drawable.beautify_text_color_absorb_tool;
		m_view.InitData(m_coreCallback);
		m_view.SetOperateMode(CoreViewV3.MODE_PENDANT);
		m_view.setOnMotifyListener(m_motifyListener);
		m_view.CreateViewBuffer();
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(m_frW, m_frH);
		fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		m_view.setLayoutParams(fl);
		m_viewFr.addView(m_view, 0);
	}

	@Override
	public void onBack()
	{
		if(m_view != null)
		{
			m_view.SetAbsorbing(false);
		}
		if(m_introPage != null)
		{
			m_introPage.onBack();
			return;
		}
		if (m_textOperateFr != null && m_textOperateFr.getVisibility() == View.VISIBLE)
		{
			if (m_isMyText)
			{
				SetViewState(m_textMyBtnFr, true, true);
			}
			else
			{
				SetViewState(m_bottomBar, true, true);
			}
			SetViewState(m_textOperateFr, false, true);
			SetTitle();
			return;
		}
		if (m_textAddDlg != null)
		{
			TextPage.this.removeView(m_textAddDlg);
			m_textAddDlg.clear();
			m_textAddDlg = null;
			return;
		}
		if (m_isInputFrShow)
		{
			showInputFr(false);
			return;
		}
		if(m_isMyText)
		{
			m_btnLst.onClick(m_myFrCloseBtn);
			return;
		}
		m_params.put("imgh", m_curImgH);
		m_params.put("viewh", m_frH);
		m_site.onBack(m_params,getContext());
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
	public void onPageResult(int siteID, HashMap<String, Object> params)
	{
		if (siteID == SiteID.ALBUM && params != null)
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
					imgPath = (String) infos[0].m_img;
				}
				MyLogoRes res = new MyLogoRes();
				res.m_res = imgPath;
				m_view.DelAllPendant();
				int index = m_view.AddPendant(res, null);
				m_view.SetSelPendant(index);
				m_view.UpdateUI();
				m_curMyTextUri = "";
				m_myResList.SetSelByIndex(-1);
				if(m_curTextUri != -1)
				{
					m_curTextUri = -1;
					m_listAdapter.SetSelByUri(DragListItemInfo.URI_NONE);
				}
			}
		}
		if(siteID == SiteID.TEXT_HELP_ANIM || siteID == SiteID.TEXT_HELP_ANIM1)
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
				if(m_uriInClassify || sel_uri != -1)
				{
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
						int index = m_listAdapter.GetIndexByUri(m_curTextUri);
						m_listAdapter.SetSelByIndex(index);
						if(index == -1)
						{
							if(m_view != null)
							{
								m_view.DelAllPendant();
								m_view.UpdateUI();
							}
						}
					}
				}
			}

		}
		if(siteID == SiteID.LOGIN || siteID == SiteID.REGISTER_DETAIL || siteID == SiteID.RESETPSW)
		{
			if(m_saveTempShape != null && UserMgr.IsLogin(getContext(),null))
			{
				OnSaveWaterMark(m_saveTempShape, m_saveTempBmp);
			}
			m_saveTempShape = null;
			m_saveTempBmp = null;
		}
	}

	@Override
	public void onClose()
	{
		super.onClose();
		((Activity)getContext()).getWindow().setSoftInputMode(mInputMode);
		if(m_view != null)
		{
			m_view.ReleaseMem();
			m_view = null;
		}
		m_UIHandler = null;
		if(m_imageThread != null)
		{
			m_imageThread.quit();
			m_imageThread = null;
		}
		System.gc();
		TongJiUtils.onPageEnd(getContext(), TAG);
		MyBeautyStat.onClickByRes(R.string.照片文字_退出照片文字);
		switch(m_curTextClassify)
		{
			case 1:
				MyBeautyStat.onPageEndByRes(R.string.照片水印);
				break;
			case 2:
				MyBeautyStat.onPageEndByRes(R.string.照片态度);
				break;
		}
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
		if(m_fontDownloadDlg != null)
		{
			m_fontDownloadDlg.clear();
			m_fontDownloadDlg = null;
		}
		if(m_resDownloadCb != null)
		{
			m_resDownloadCb.ClearAll();
			m_resDownloadCb = null;
		}
		if(m_myResList != null)
		{
			m_myResList.ClearAll();
			m_myResList = null;
		}
		if(m_inputFr != null)
		{
			m_inputFr.clearAll();
			m_inputFr = null;
			m_inputCb = null;
		}
		m_colorChooser = null;
		this.removeAllViews();
	}

	@Override
	protected boolean canDelete(int position)
	{
		boolean flag = false;
		DragListItemInfo itemInfo = m_listDatas.get(position);
		if(itemInfo != null)
		{
			TextRes res = (TextRes)itemInfo.m_ex;
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
		m_isMyText = false;

		m_texts = new ArrayList<>();

		m_curTextUri = -1;
		m_curMyTextUri = "";
		m_curTextClassify = 1;

		m_UIHandler = new UIHandler();
		m_imageThread = new HandlerThread("text_thread");
		m_imageThread.start();
		m_mainHandler = new BeautifyHandler(m_imageThread.getLooper(), getContext(), m_UIHandler);
	}

	protected void InitUI()
	{
		super.InitUI();
		FrameLayout.LayoutParams fl;
		m_title.setText(getResources().getString(R.string.Watermark));

		m_textBtnList = new SimpleBtnList100(getContext());
		m_textBtnList.setBackgroundColor(0xff0e0e0e);
		ArrayList<SimpleBtnList100.Item> datas = BeautifyResMgr.getTextClassifyItems(getContext());
		m_textBtnList.SetData(datas, m_textBtnListCB);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_bottomBarHeight);
		fl.gravity = Gravity.BOTTOM;
		m_textBtnList.setLayoutParams(fl);
		m_bottomBar.addView(m_textBtnList);

		m_textMyBtnFr = new LinearLayout(getContext());
		m_textMyBtnFr.setBackgroundColor(0xff000000);
		m_textMyBtnFr.setPadding(0, ShareData.PxToDpi_xhdpi(50), 0, 0);
		m_textMyBtnFr.setOrientation(LinearLayout.VERTICAL);
		m_textMyBtnFr.setVisibility(View.GONE);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM;
		m_textMyBtnFr.setLayoutParams(fl);
		this.addView(m_textMyBtnFr);
		{
			m_myResList = new MySimpleBtnList(getContext());
			ArrayList<SimpleBtnList100.Item> items = BeautifyResMgr.getTextMyItems(getContext());
			m_myResList.SetData(items, m_myResListCB);
			m_myResList.SetSelByIndex(-1);
			LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			m_myResList.setLayoutParams(ll);
			m_textMyBtnFr.addView(m_myResList);

			ImageView Line = new ImageView(getContext());
			Line.setBackgroundColor(0xff272727);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
			ll.topMargin = ShareData.PxToDpi_xhdpi(50);
			Line.setLayoutParams(ll);
			m_textMyBtnFr.addView(Line);

			m_myFrCloseBtn = new ImageView(getContext());
			m_myFrCloseBtn.setBackgroundColor(0xff000000);
			m_myFrCloseBtn.setScaleType(ImageView.ScaleType.CENTER);
			m_myFrCloseBtn.setOnClickListener(m_btnLst);
			m_myFrCloseBtn.setImageResource(R.drawable.beauty_color_adjust_down);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(40));
			m_myFrCloseBtn.setLayoutParams(ll);
			m_textMyBtnFr.addView(m_myFrCloseBtn);
		}

		m_inputFr = new InputDialog(getContext(), m_inputCb);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		fl.gravity = Gravity.BOTTOM;
		m_inputFr.setLayoutParams(fl);
		m_inputFr.hide();
		m_isInputFrShow = false;
		this.addView(m_inputFr);

		LinearLayout.LayoutParams ll;
		m_textOperateFr = new LinearLayout(getContext());
		m_textOperateFr.setVisibility(View.GONE);
		m_textOperateFr.setBackgroundColor(0xff212121);
		m_textOperateFr.setOrientation(LinearLayout.VERTICAL);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(260));
		fl.gravity = Gravity.BOTTOM;
		m_textOperateFr.setLayoutParams(fl);
		this.addView(m_textOperateFr);
		{
			m_operateTitleFr = new LinearLayout(getContext());
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(70));
			m_operateTitleFr.setLayoutParams(ll);
			m_textOperateFr.addView(m_operateTitleFr);
			{
				int width = ShareData.m_screenWidth / 3;
				m_colorChooserBtn = new ImageView(getContext());
				m_colorChooserBtn.setScaleType(ImageView.ScaleType.CENTER);
				m_colorChooserBtn.setBackgroundColor(0xff212121);
				m_colorChooserBtn.setImageResource(R.drawable.beautify_text_color_icon);
				AddItem(m_operateTitleFr, m_colorChooserBtn, width);

				m_textAlphaBtn = new ImageView(getContext());
				m_textAlphaBtn.setScaleType(ImageView.ScaleType.CENTER);
				m_textAlphaBtn.setBackgroundColor(0xff111111);
				m_textAlphaBtn.setImageResource(R.drawable.beautify_text_alpha_icon);
				AddItem(m_operateTitleFr, m_textAlphaBtn, width);

				m_shadowAlphaBtn = new ImageView(getContext());
				m_shadowAlphaBtn.setScaleType(ImageView.ScaleType.CENTER);
				m_shadowAlphaBtn.setBackgroundColor(0xff111111);
				m_shadowAlphaBtn.setImageResource(R.drawable.beautify_text_shadow_alpha_icon);
				AddItem(m_operateTitleFr, m_shadowAlphaBtn, width);
			}

			m_operateContentFr = new FrameLayout(getContext());
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(190));
			m_operateContentFr.setLayoutParams(ll);
			m_textOperateFr.addView(m_operateContentFr);
			{
				m_seekBarFr = new LinearLayout(getContext());
				m_seekBarFr.setPadding(0, ShareData.PxToDpi_xhdpi(15), 0, ShareData.PxToDpi_xhdpi(15));
				m_seekBarFr.setBackgroundColor(0xff222222);
				m_seekBarFr.setVisibility(View.GONE);
				m_seekBarFr.setOrientation(LinearLayout.VERTICAL);
				fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40), FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER;
				m_seekBarFr.setLayoutParams(fl);
				m_operateContentFr.addView(m_seekBarFr);
				{
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

				m_colorChooser = new ColorChangeLayout1(getContext());
				m_colorChooser.SetDatas(BeautifyResMgr.getColorRes(), 6);
				m_colorChooser.setItemOnClickListener(new ColorChangeLayout1.ItemOnClickListener()
				{
					@Override
					public void onObsorbClick(boolean absorbing)
					{
						if(m_view != null)
						{
							if(absorbing)
							{
								TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_取色器);
								MyBeautyStat.onClickByRes(R.string.选中照片水印_吸取颜色);
							}
							m_view.SetAbsorbing(absorbing);
							if(!absorbing)
							{
								m_colorChooser.setSelectedItemByColor(m_view.GetCurShowColor());
							}
						}
					}

					@Override
					public void onColorItemClick(int color, int index)
					{
						if (m_view != null)
						{
							m_view.UpdateColor(color);
						}
					}
				});
				fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER;
				m_colorChooser.setLayoutParams(fl);
				m_operateContentFr.addView(m_colorChooser);
			}

		}

		m_icon.setVisibility(View.VISIBLE);

	}

	protected void AddItem(LinearLayout parent, View child, int width) {
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(width, LayoutParams.MATCH_PARENT);
		ll.gravity = Gravity.CENTER_VERTICAL;
		child.setLayoutParams(ll);
		parent.addView(child);
		child.setOnClickListener(m_btnLst);
	}

	@Override
	protected MyBtnLst GetBtnLst()
	{
		return m_btnLst;
	}

	@Override
	protected ArrayList<DragListItemInfo> InitListDatas()
	{
		ArrayList<DragListItemInfo> listdatas = BeautifyResMgr.getTextRess(getContext(), m_curTextClassify);
		return listdatas;
	}

	protected void SetTitle()
	{
		if (m_textOperateFr != null && m_textOperateFr.getVisibility() == View.VISIBLE)
		{
			if(m_alphaMode == -1)
			{
				m_title.setText(getResources().getString(R.string.Color));
			}
			else if(m_alphaMode == TEXT_ALPHA)
			{
				m_title.setText(getResources().getString(R.string.Opacity));
			}
			else
			{
				m_title.setText(getResources().getString(R.string.Shadow1));
			}
		}
		else if(m_isMyText)
		{
			MyBeautyStat.onClickByRes(R.string.照片文字_打开我的);
			m_title.setText(getResources().getString(R.string.Mine));
		}
		else if(m_curTextClassify == 1)
		{
			MyBeautyStat.onClickByRes(R.string.照片文字_切换至水印);
			m_title.setText(getResources().getString(R.string.Watermark));
		}
		else if(m_curTextClassify == 2)
		{
			MyBeautyStat.onClickByRes(R.string.照片文字_切换至态度);
			m_title.setText(getResources().getString(R.string.Attitude));
		}
	}

	protected void downloadFont(final BaseRes res)
	{
		if (m_fontDownloadDlg != null)
		{
			m_fontDownloadDlg.dismiss();
			m_fontDownloadDlg = null;
		}
		if (res != null)
		{
			m_fontDownloadDlg = new FontDownloadDlg((Activity) getContext(), R.style.waitDialog);
			m_fontDownloadDlg.setData(res);
			m_fontDownloadDlg.setOnDlgClickCallback(new FontDownloadDlg.OnDlgClickCallback()
			{
				@Override
				public void onComplete()
				{
					if (m_fontDownloadDlg != null)
					{
						m_fontDownloadDlg.clear();
						m_fontDownloadDlg.dismiss();
					}
					if(res instanceof TextRes)
					{
						((TextRes)res).m_editable = 1;
						AddText((TextRes)res);
					}
					else if(res instanceof MyLogoRes)
					{
						AddText((MyLogoRes)res, false);
					}
				}

				@Override
				public void onCancel()
				{
					m_fontDownloadDlg.clear();
					m_fontDownloadDlg.dismiss();

					if(res instanceof TextRes)
					{
						((TextRes)res).m_editable = 1;
						AddText((TextRes)res);
					}
					else if(res instanceof MyLogoRes)
					{
						AddText((MyLogoRes)res, false);
					}
				}
			});
			m_fontDownloadDlg.show();
		}
	}

	/**
	 * 自定义文字
	 * @param res
	 * @param checkFont
	 */
	protected void AddText(MyLogoRes res, boolean checkFont)
	{
		if(res == null)
			return;
		if(!res.m_editable)
		{
			m_view.AddPendant(res, null);
			m_view.UpdateUI();
			m_view.SetSelPendant(-1);
		}
		else
		{
			if (res.m_res != null && res.m_res instanceof String)
			{
				String path = (String)res.m_res;
				byte[] bytes = Zip.GetFileStream(getContext(), path, "data.json");
				if (bytes != null && bytes.length > 0)
				{
					String jsonString = new String(bytes);
					MyTextInfo textInfo = JsonParser.parseTextJson(jsonString);
					if (textInfo != null)
					{
						textInfo.m_editable = true;
						textInfo.image_zip = path;
						textInfo.m_ex = res;

						//清除无用的字体
						textInfo.ClearUnusedFont(res.m_resArr);
					}

					if(checkFont)
					{
						boolean exists = Painter.isFontExists(getContext(), res.m_resArr);
						if(!exists)
						{
							downloadFont(res);
							return;
						}
					}
					m_view.AddPendant(textInfo, null);
					m_view.SetSelPendant(-1);
					m_view.UpdateUI();
				}
			}
		}

	}

	/**
	 * 非自定义文字
	 *
	 * @param res
	 */
	protected void AddText(TextRes res)
	{
		if (res == null)
			return;

		switch(m_curTextClassify)
		{
			case 1:
				MyBeautyStat.onClickByRes(R.string.照片水印_选择照片水印);
				MyBeautyStat.onChooseMaterial(res.m_tjId + "", R.string.照片水印);
				break;
			case 2:
				MyBeautyStat.onClickByRes(R.string.照片态度_选择照片态度);
				MyBeautyStat.onChooseMaterial(res.m_tjId + "", R.string.照片态度);
				break;
		}
		ShapeEx shape = GetCacheInfo(res);
		if(shape != null && shape.m_ex != null && shape.m_ex instanceof MyTextInfo &&
				((MyTextInfo)shape.m_ex).m_editable == true)
		{
			ShapeEx cacheShape = m_view.DelPendantByIndex(m_view.GetPendantLen() - 1);
			AddToCacheInfo(cacheShape);
			int index = m_view.AddPendant3(shape);
			m_view.SetSelPendant(-1);
			m_view.UpdateUI();
		}
		else if(shape != null && shape.m_ex != null && shape.m_ex instanceof MyTextInfo && ((MyTextInfo)shape.m_ex).m_editable == false)
		{
			if (res.m_editable == 1 && res.m_imageZip != null && res.m_imageZip.length() > 0)
			{
				MyTextInfo textInfo = null;
				String jsonName;
//				System.out.println("m_imageZip: "  + res.m_imageZip);
				int startIndex = res.m_imageZip.lastIndexOf("/") + 1;
				int endIndex = res.m_imageZip.indexOf(".img");
				if (endIndex == -1)
				{
					endIndex = res.m_imageZip.indexOf(".zip");
				}
				jsonName = res.m_imageZip.substring(startIndex, endIndex);
				jsonName += ".json";
//				Log.i("BeautifyPage", "jsonName: " + jsonName);
				byte[] bytes = Zip.GetFileStream(getContext(), res.m_imageZip, jsonName);
				if (bytes != null && bytes.length > 0)
				{
					String jsonString = new String(bytes);
					textInfo = JsonParser.parseTextJson(jsonString);
					if (textInfo != null)
					{
						textInfo.m_editable = true;
						textInfo.image_zip = res.m_imageZip;
						textInfo.m_ex = res;
					}
				}
				shape.m_ex = textInfo;
			}
			ShapeEx cacheShape = m_view.DelPendantByIndex(m_view.GetPendantLen() - 1);
			AddToCacheInfo(cacheShape);
			int index = m_view.AddPendant3(shape);
			m_view.SetSelPendant(-1);
			m_view.UpdateUI();
		}
		else if(shape == null)
		{
			MyTextInfo textInfo = null;
			String jsonName;
			if (res.m_editable == 1 && res.m_imageZip != null && res.m_imageZip.length() > 0)
			{
//			System.out.println("m_imageZip: "  + res.m_imageZip);
				int startIndex = res.m_imageZip.lastIndexOf("/") + 1;
				int endIndex = res.m_imageZip.indexOf(".img");
				if (endIndex == -1)
				{
					endIndex = res.m_imageZip.indexOf(".zip");
				}
				jsonName = res.m_imageZip.substring(startIndex, endIndex);
				jsonName += ".json";
//			Log.i("BeautifyPage", "jsonName: " + jsonName);
				byte[] bytes = Zip.GetFileStream(getContext(), res.m_imageZip, jsonName);
				if (bytes != null && bytes.length > 0)
				{
					String jsonString = new String(bytes);
					textInfo = JsonParser.parseTextJson(jsonString);
					if (textInfo != null)
					{
						textInfo.m_editable = true;
						textInfo.image_zip = res.m_imageZip;
						textInfo.m_ex = res;
					}
				}
			}
			else if (res.m_pic != null && res.m_pic.length() > 0)
			{
				textInfo = new MyTextInfo();
				textInfo.m_pic = res.m_pic;
				textInfo.align = res.m_align;
				textInfo.offsetX = res.m_offsetX;
				textInfo.offsetY = res.m_offsetY;
				textInfo.m_editable = false;
				textInfo.m_ex = res;
			}
			if (textInfo != null && m_view != null)
			{
				ShapeEx cacheShape = m_view.DelPendantByIndex(m_view.GetPendantLen() - 1);
				AddToCacheInfo(cacheShape);
				int index = m_view.AddPendant(textInfo, null);
				m_view.SetSelPendant(-1);
				m_view.UpdateUI();
			}
		}
	}

	protected synchronized ShapeEx GetCacheInfo(TextRes res)
	{
		ShapeEx out = null;
		if(res != null)
		{
			TextRes res1;
			int size = m_texts.size();
			for(int i = 0; i < size; i ++)
			{
				res1 = (TextRes)(((MyTextInfo)m_texts.get(i).m_ex).m_ex);
				if(res.m_id == res1.m_id)
				{
					out = m_texts.get(i);
					break;
				}
			}
		}
		return out;
	}

	protected synchronized void AddToCacheInfo(ShapeEx shape)
	{
		if(shape != null && shape.m_ex != null && shape.m_ex instanceof MyTextInfo && ((MyTextInfo)shape.m_ex).m_ex instanceof TextRes)
		{
			if(shape instanceof ShapeEx5){
				((ShapeEx5)shape).ClearFontPaint();
			}
			TextRes res = (TextRes)(((MyTextInfo)shape.m_ex).m_ex);
			TextRes res1;
			int size = m_texts.size();
			int index = -1;
			for(int i = 0; i < size; i ++)
			{
				res1 = (TextRes)(((MyTextInfo)m_texts.get(i).m_ex).m_ex);
				if(res.m_id == res1.m_id)
				{
					index = i;
					break;
				}
			}
			if(index == -1)
			{
				m_texts.add(shape);
			}
			else
			{
				m_texts.set(index, shape);
			}
		}
	}

	protected synchronized boolean RemoveFromCacheInfo(ShapeEx shape)
	{
		if(shape != null)
		{
			ShapeEx shapeEx;
			int size = m_texts.size();
			for(int i = 0; i < size; i ++)
			{
				shapeEx = m_texts.get(i);
				if(shape.m_soleId == shapeEx.m_soleId)
				{
					m_texts.remove(i);
					return true;
				}
			}
		}
		return false;
	}

	protected void ReLayoutTextShowView(float upY, final boolean showSoftInput, int softInputHeight, boolean hasAnim)
	{
		if(m_view != null)
		{
			m_view.SetUIEnabled(!showSoftInput);
			m_view.SetLongPressEnabled(!showSoftInput);
		}
		FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams) m_view.getLayoutParams();
		if ((fl == null || (fl != null && fl.topMargin == 0)) && !showSoftInput)
		{
			return;
		}

		m_view.clearAnimation();
		int margin = ShareData.PxToDpi_xhdpi(150);
		float bottomMargin = m_frH - upY + m_resBarHeight + m_bottomBarHeight;
		final float translateDis;

		if (bottomMargin - softInputHeight < margin)
		{
			translateDis = softInputHeight + margin - bottomMargin;
		}
		else
		{
			translateDis = 0;
		}

		int start = 1;
		int end = 1;
		if (showSoftInput)
		{
			if (translateDis > 0)
			{
				start = 0;
				end = -(int) translateDis;
			}
		}
		else
		{
			start = 0;
			end = (int) translateDis;
		}

		if (hasAnim) {
			AnimationSet as;
			TranslateAnimation ta;
			as = new AnimationSet(true);
			ta = new TranslateAnimation(0, 0, start, end);
			ta.setDuration(350);
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
					fl = new FrameLayout.LayoutParams(m_frW, m_frH);
					fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
					if (showSoftInput)
					{
						fl.topMargin = - (int) translateDis;
					}
					if (m_view != null)
					{
						m_view.clearAnimation();
						m_view.setLayoutParams(fl);
					}
				}
			});
			as.addAnimation(ta);
			m_view.startAnimation(as);
		}
	}

	protected void showTextAddDlg(boolean flag, MyLogoRes logoRes, final ShapeEx shape)
	{
		if(flag)
		{
			TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_添加水印);
			if(m_textAddDlg == null)
			{
				m_textAddDlg = new TextAddDlg((Activity)getContext(), new TextAddDlg.Callback()
				{
					@Override
					public void onDismiss()
					{
						if(m_textAddDlg != null)
						{
							TextPage.this.removeView(m_textAddDlg);
							m_textAddDlg.clear();
							m_textAddDlg = null;
						}
					}

					@Override
					public void onInputFinished(MyLogoRes res)
					{
						if(shape != null)
						{
							String userId = SettingInfoMgr.GetSettingInfo(getContext()).GetPoco2Id(true);
							if (!TextUtils.isEmpty(userId)) {
								res.m_userId = Integer.parseInt(userId);
							}
							boolean flag = MyLogoResMgr.getInstance().AddMyLogoResItem(res);
							if(flag)
							{
								if(shape instanceof ShapeEx4)
								{
									((ShapeEx4)shape).m_isLocal = true;

									m_view.UpdateUI();
								}

								if(m_myResList != null)
								{
									m_myResList.SetData(BeautifyResMgr.getTextMyItems(getContext()), m_myResListCB);

									if(shape instanceof ShapeEx4)
									{
										m_myResList.SetSelByIndex(1);
										m_myResList.HideDeleteBtn(m_curMyTextUri);
										m_curMyTextUri = res.m_userId + "_" + res.m_id;
									}
									if(!TextUtils.isEmpty(m_curMyTextUri))
									{
										m_myResList.SetSelByIndex(1);
									}
								}
								Toast.makeText(getContext(), getResources().getString(R.string.added), Toast.LENGTH_SHORT).show();

								// 上传水印
								Watermark watermark;
								if (res.m_editable) {
									watermark = new EditableWatermark(res);
								} else {
									watermark = new NotEditableWatermark(res);
								}
								watermark.setOperateType(Watermark.OperateType.UPLOAD);
								List<Watermark> watermarksList = new ArrayList<>();
								watermarksList.add(watermark);
								WatermarkSyncManager.getInstacne(getContext()).uploadNewWatermark(watermarksList);
							}
							else
							{
								Toast.makeText(getContext(), getResources().getString(R.string.addFailed), Toast.LENGTH_SHORT).show();
							}
						}
						else
						{
							// 先判断是不是可以编辑的水印
							Watermark watermark;
							if (res.m_editable) {
								watermark = new EditableWatermark(res);
							} else {
								watermark = new NotEditableWatermark(res);
							}

							// 只修改水印名字的情况
							if (UserMgr.IsLogin(getContext(),null)) {
								String curUserId = SettingInfoMgr.GetSettingInfo(getContext()).GetPoco2Id(true);
								if (!TextUtils.isEmpty(curUserId)) {
									// 当水印的用户id 和 当前登录用户id不一致,当成上传
									if (!String.valueOf(res.m_userId).equals(curUserId)) {
										res.mUniqueObjectId = -1;
										res.m_userId = Integer.parseInt(curUserId);
										MyLogoResMgr.getInstance().UpdateMyLogoRes(res);
										watermark.setOperateType(Watermark.OperateType.UPLOAD);
										List<Watermark> list = new ArrayList<>();
										list.add(watermark);
										WatermarkSyncManager.getInstacne(getContext()).uploadNewWatermark(list);
									} else {
										// 当水印的用户id 和 当前登录用户id一致,正常修改
										res.mSaveTime = String.valueOf(System.currentTimeMillis() / 1000);
										MyLogoResMgr.getInstance().UpdateMyLogoRes(res);
										WatermarkSyncManager.ModifyWatermarkInfo modifyWatermarkInfo = new WatermarkSyncManager.ModifyWatermarkInfo();
										modifyWatermarkInfo.mObjectId = res.mUniqueObjectId;
										modifyWatermarkInfo.mWatermarkTitle = res.m_name;
										modifyWatermarkInfo.mSaveTime = res.mSaveTime;
										modifyWatermarkInfo.mEditable = res.m_editable;
										if (res.m_res instanceof String) {
											modifyWatermarkInfo.mVolume = cn.poco.watermarksync.util.FileUtil.getFileSize((String)res.m_res);
										}
										modifyWatermarkInfo.mOperateType = Watermark.OperateType.MODIFY;
										WatermarkSyncManager.getInstacne(getContext()).modifyTheWatermark(modifyWatermarkInfo, false);
									}
								}
							} else {
								// 当前没有用户登录，而点击修改名字
								res.mUniqueObjectId = -1;
								res.m_userId = MyLogoRes.USER_NONE_ID;
								MyLogoResMgr.getInstance().UpdateMyLogoRes(res);
							}

							if(m_myResList != null)
							{
								m_myResList.SetData(BeautifyResMgr.getTextMyItems(getContext()), m_myResListCB);
							}
							if(m_isMyDeleteShow)
							{
								m_isMyDeleteShow = false;
								m_myResList.HideDeleteBtn(m_curMyTextUri);
							}
						}

						if(m_textAddDlg != null)
						{
							TextPage.this.removeView(m_textAddDlg);
							m_textAddDlg.clear();
							m_textAddDlg = null;
						}
					}
				});
				m_textAddDlg.setData(logoRes);
				InitBkImg();
				m_textAddDlg.setBk(m_bkBmp);
			}
			if(m_textAddDlg != null)
			{
				this.removeView(m_textAddDlg);
				FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				this.addView(m_textAddDlg, fl);
			}
		}
		else
		{
			if(m_textAddDlg != null)
			{
				this.removeView(m_textAddDlg);
				m_textAddDlg.clear();
				m_textAddDlg = null;
			}
		}
	}

	@Override
	protected void SetShowViewAnim()
	{
		SetShowViewAnim(m_view);
	}

	protected void SetSelItemByUri(int uri)
	{
		m_uriInClassify = true;
		showInputFr(false);
		m_seekBarFr.setVisibility(GONE);
		if (m_fontDownloadDlg != null) {
			m_fontDownloadDlg.dismiss();
			m_fontDownloadDlg = null;
		}
		if (uri == 0)
		{
			m_view.DelAllPendant();
		}
		else
		{
			TextRes res = null;
			if (m_listDatas != null)
			{
				int size = m_listDatas.size();
				for (int i = 0; i < size; i++)
				{
					if (uri == m_listDatas.get(i).m_uri)
					{
						res = (TextRes) m_listDatas.get(i).m_ex;
						break;
					}
				}
			}
			if (res != null)
			{
				if (m_myResList != null)
				{
					m_myResList.SetSelByIndex(-1);
				}
				boolean exists = Painter.isFontExists(getContext(), res.m_resArr);
				if (exists)
				{
					res.m_editable = 1;

					AddText(res);
				}
				else if (res.m_resArr.size() > 0)
				{
					res.m_editable = 0;
					AddText(res);
				}
			}
		}
	}

	protected void showInputFr(boolean show)
	{
		if (show && m_isInputFrShow == false)
		{
			if (m_inputFr != null)
			{
				m_isInputFrShow = true;
				m_inputFr.show();
			}
		}
		else
		{
			if (m_inputFr != null)
			{
				m_isInputFrShow = false;
				m_inputFr.hide();
			}
		}
	}

	@Override
	protected void OnHideItem(int position)
	{
		DragListItemInfo itemInfo = m_listDatas.get(position);
		if(itemInfo != null && itemInfo.m_ex != null)
		{
			TextRes res = (TextRes)itemInfo.m_ex;
			if(res.m_type != BaseRes.TYPE_LOCAL_RES)
			{
				m_listDatas.remove(position);
				m_listAdapter.removeItem(position);

				TextResMgr2.getInstance().DeleteRes(getContext(), res);
				switch(m_curTextClassify)
				{
					case 1:
						MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.照片水印);
						MyBeautyStat.onClickByRes(R.string.照片水印_删除水印);
						TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_水印_隐藏);
						break;
					case 2:
						MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.照片态度);
						MyBeautyStat.onClickByRes(R.string.照片态度_删除态度);
						TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_态度_隐藏);
						break;
				}

				if(m_curTextUri == res.m_id)
				{
					if(m_view != null)
					{
						m_view.DelAllPendant();
						m_view.UpdateUI();
						m_curTextUri = DragListItemInfo.URI_ORIGIN;
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
		ArrayList<Integer> orderArr = TextResMgr2.getInstance().GetOrderArr1().get(m_curTextClassify);
		if(itemInfo != null)
		{
			fromPos = ResourceUtils.HasId(orderArr, itemInfo.m_uri);
		}
		itemInfo = m_listDatas.get(toPosition);
		if(itemInfo != null)
		{
			toPos = ResourceUtils.HasId(orderArr, itemInfo.m_uri);
		}
		ResourceUtils.ChangeOrderPosition(orderArr, fromPos, toPos);
		TextResMgr2.getInstance().SaveOrderArr();
		if (m_listDatas != null && m_listDatas.size() > fromPosition && m_listDatas.size() > toPosition) {
			itemInfo = m_listDatas.remove(fromPosition);
			m_listDatas.add(toPosition, itemInfo);
		}
	}

	@Override
	public void OnItemClick(View view, final DragListItemInfo info, int index)
	{
		if (m_uiEnabled)
		{
			if(info != null)
			{
				if(info.m_uri == DragListItemInfo.URI_MGR)
				{
					HashMap<String, Object> params = new HashMap<>();
					params.put("type", ResType.TEXT);
					if(m_curTextClassify == 1)
					{
						params.put("textType", "water");
						TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_水印_更多按钮);
						MyBeautyStat.onClickByRes(R.string.照片水印_点击更多);
					}
					else if(m_curTextClassify == 2)
					{
						params.put("textType", "attitude");
						TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_态度_更多按钮);
						MyBeautyStat.onClickByRes(R.string.照片态度_点击更多);
					}
					params.put("typeOnly", true);
					m_site.OnDownloadMore(params,getContext());
					return;
				}
				switch(info.m_style)
				{
					case NORMAL:
					{
						if (info.m_uri == DragListItemInfo.URI_MYTEXT)
						{
							TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_水印_我的);
							m_isMyText = true;
							m_title.setText(R.string.Mine);
							MyBeautyStat.onClickByRes(R.string.照片文字_打开我的);
							SetViewState(m_textMyBtnFr, true, true);
							SetViewState(m_bottomBar, false, true);
							String app_ver = CommonUtils.GetAppVer(getContext());
							if(!UserMgr.IsLogin(getContext(),null) && TagMgr.CheckTag(getContext(), "mytext_first_tip_login" + app_ver))
							{
								TagMgr.SetTag(getContext(), "mytext_first_tip_login" + app_ver);
								showLoginTipDlg(false, null, null);
								return;
							}
							if(TagMgr.CheckTag(getContext(), "first_enter_mytext"))
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
											m_site.OpenMyHelpPage(params,getContext());
											TagMgr.SetTag(getContext(), "first_enter_mytext");
										}
									}
								}, 400);
							}
							return;
						}
						if (m_curTextUri != info.m_uri)
						{
							if (m_listAdapter != null)
							{
								m_listAdapter.SetSelByUri(info.m_uri);
							}
							m_resList.ScrollToCenter(index);
							SetSelItemByUri(info.m_uri);

							if(!TextUtils.isEmpty(m_curMyTextUri))
							{
								m_curMyTextUri = "";
								m_myResList.SetSelByIndex(-1);
							}
						}
						m_curTextUri = info.m_uri;
						break;
					}
					case NEED_DOWNLOAD:
					{
						if(info.m_ex instanceof ThemeRes)
						{
							switch(m_curTextClassify)
							{
								case 1:
									MyBeautyStat.onClickByRes(R.string.照片水印_打开推荐位);
									TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_水印_主题推荐位);
									break;
								case 2:
									MyBeautyStat.onClickByRes(R.string.照片态度_打开推荐位);
									TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_态度_主题推荐位);
									break;
							}
							HashMap<String, Object> params = new HashMap<>();
							ThemeItemInfo itemInfo = MgrUtils.GetThemeItemInfoByUri(getContext(), ((ThemeRes)info.m_ex).m_id, ResType.TEXT);
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
						}
						break;
					}
					default:
						break;
				}
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
				TextPage.this.removeView(m_introPage);
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
			if(m_curTextUri != info.m_uri)
			{
				OnItemClick(view, info, index);
				return;
			}
			String headLink = ((TextRes) (info.m_ex)).m_headLink;
			if (headLink != null && headLink.length() > 0)
			{
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("url", MyNetCore.GetPocoUrl(getContext(), headLink));
				params.put("share_app", 1);
				params.put("share_logo", info.m_head);
				m_site.OnHeadLink(params,getContext());
			}
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
	}

	protected SimpleBtnList100.Callback m_textBtnListCB = new SimpleBtnList100.Callback()
	{

		@Override
		public void OnClick(SimpleBtnList100.Item view, int index)
		{
			if (m_uiEnabled && m_curTextClassify != view.m_uri)
			{
				TongJi2.AddCountByRes(getContext(), ((SimpleListItem) view).m_tjID);
				if (m_textBtnList != null)
				{
					m_textBtnList.SetSelByIndex(index);
				}

				if(m_listDatas != null)
				{
					m_listDatas.clear();
					m_listAdapter.ClearAll();
					m_listAdapter = null;
				}
				m_listAdapter = new MyDragItemAdapter(getContext(), true);
				m_listAdapter.showTitle(false);
				m_listDatas = BeautifyResMgr.getTextRess(getContext(), view.m_uri);
				m_listAdapter.setItemList(m_listDatas);
				m_listAdapter.SetOnClickCallback(m_listCallback);
				m_resList.setAdapter(m_listAdapter, true);

				m_listAdapter.SetSelByUri(m_curTextUri);

				m_curTextClassify = view.m_uri;
				switch(m_curTextClassify)
				{
					case 1:
						MyBeautyStat.onPageStartByRes(R.string.照片水印);
						MyBeautyStat.onPageEndByRes(R.string.照片态度);
						break;
					case 2:
						MyBeautyStat.onPageEndByRes(R.string.照片水印);
						MyBeautyStat.onPageStartByRes(R.string.照片态度);
						break;
				}
				m_uriInClassify = false;

				SetViewState(m_resList, true, true);
				SetTitle();
			}
		}

	};

	protected MySimpleBtnList.Callback m_myResListCB = new MySimpleBtnList.Callback() {

		@Override
		public void OnDeleteBtn(SimpleBtnList100.Item view, int index)
		{
			if(m_deleteTip == null)
			{
				m_deleteTip = new DeleteDlg((Activity)getContext(), R.style.waitDialog);
				m_deleteTip.setOnDlgClickCallback(new DeleteDlg.OnDlgClickCallback()
				{
					@Override
					public void onDelete(Object res)
					{
						if(res != null && res instanceof MyLogoRes)
						{
							String uri = ((MyLogoRes)res).m_userId + "_" + ((MyLogoRes)res).m_id;
							m_deleteTip.dismiss();
							if(m_curTextUri == -1 && !TextUtils.isEmpty(m_curMyTextUri)
									&& uri.equals(m_curMyTextUri))
							{
								m_view.DelAllPendant();
								m_view.UpdateUI();
							}

							// 先判断删除的水印是否是属于当前登录用户的
							String curUserId = SettingInfoMgr.GetSettingInfo(getContext()).GetPoco2Id(true);
							if (String.valueOf(((MyLogoRes) res).m_userId).equals(curUserId)) {
								// 删除水印，先判断水印类型
								Watermark watermark;
								((MyLogoRes) res).mShouldDelete = true;
								if (((MyLogoRes) res).m_editable) {
									watermark = new EditableWatermark((MyLogoRes) res);
								} else {
									watermark = new NotEditableWatermark((MyLogoRes) res);
								}
								WatermarkSyncManager.getInstacne(getContext()).deleteWatermark(watermark);
							} else {
								MyLogoRes myLogoRes = (MyLogoRes) res;
								MyLogoResMgr.getInstance().DeleteMyLogoRes(getContext(), myLogoRes);
								if (myLogoRes.m_res instanceof String) {
									String filePath = (String)myLogoRes.m_res;
									FileUtil.deleteSDFile(filePath);
								}
							}
							ArrayList<SimpleBtnList100.Item> items = BeautifyResMgr.getTextMyItems(getContext());
							m_myResList.SetData(items, m_myResListCB);
							m_myResList.SetSelByIndex(-1);
						}
					}

					@Override
					public void onPageClick()
					{

					}

					@Override
					public void onCancel()
					{
						m_deleteTip.dismiss();
					}
				});
			}
			if(m_deleteTip != null)
			{
				m_deleteTip.setData((MyLogoRes)((MySimpleListItem) view).m_ex);
				m_deleteTip.show();
			}
		}

		@Override
		public void OnLongClick(boolean select)
		{
			if(select)
			{
				Vibrator vib = (Vibrator)getContext().getSystemService(Service.VIBRATOR_SERVICE);
				if(vib != null)
				{
					vib.vibrate(30);
				}
				m_myResList.ShowDeleteBtn();
			}
			else
			{
				m_myResList.HideDeleteBtn(m_curMyTextUri);
			}
			m_isMyDeleteShow = select;
		}

		@Override
		public void OnClick(SimpleBtnList100.Item view, int index, boolean showDelete)
		{
			if (m_uiEnabled)
			{
				if (view.m_uri == BeautifyResMgr.ADD_TEXT)
				{
					TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_水印_添加);
					MyBeautyStat.onClickByRes(R.string.我的_点击添加);
					m_site.OnSelMyLogo(getContext());
				}
				else if(showDelete == false)
				{
					if (m_myResList != null && !((MySimpleListItem)view).m_uri.equals(m_curMyTextUri))
					{
						m_myResList.SetSelByIndex(index);
					}
					if (view instanceof MySimpleListItem && ((MySimpleListItem) view).m_ex != null)
					{
						m_view.DelAllPendant();
						MyLogoRes res = (MyLogoRes)((MySimpleListItem) view).m_ex;
						AddText(res, true);
					}
					m_curMyTextUri = ((MySimpleListItem)view).m_uri;
					if(m_curTextUri != -1)
					{
						m_curTextUri = -1;
						m_listAdapter.SetSelByUri(DragListItemInfo.URI_NONE);
					}
				}
				else
				{
					MyLogoRes res = (MyLogoRes)((MySimpleListItem) view).m_ex;
					if (res != null)
						showTextAddDlg(true, res, null);
				}
			}
		}
	};

	private ShapeEx m_saveTempShape = null;
	private Object m_saveTempBmp = null;
	private void showLoginTipDlg(boolean isSave, final ShapeEx shape, final Object saveBmp)
	{
		int title = R.string.watermark_sync_save_tip_title;
		int content = R.string.watermark_sync_save_tip_content;
		if(!isSave)
		{
			title = R.string.watermark_sync_tip_title;
			content = R.string.watermark_sync_tip_content;
		}
		m_saveTempShape = shape;
		m_saveTempBmp = saveBmp;
		if(m_loginTip == null)
		{
			m_loginTip = new InterphotoDlg((Activity)getContext(), R.style.waitDialog);
			m_loginTip.SetNegativeBtnText(R.string.watermark_sync_tip_cancel);
			m_loginTip.SetPositiveBtnText(R.string.watermark_sync_tip_login);
			m_loginTip.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback()
			{
				@Override
				public void onOK()
				{
					m_loginTip.dismiss();
					m_site.OnLogin(getContext());
				}

				@Override
				public void onCancel()
				{
					m_loginTip.dismiss();

					m_saveTempShape = null;
					m_saveTempBmp = null;
				}
			});

		}
		m_loginTip.SetTitle(title);
		m_loginTip.SetMessage(content);
		m_loginTip.show();
	}

	/**
	 * 覆盖/添加提示框
	 * @param info
	 * @param res
	 * @param saveBmp
	 */
	private void showWatermarkNotifyDialog(final MyTextInfo info, final MyLogoRes res, final Object saveBmp, final ShapeEx shape)
	{
		String text = getResources().getString(R.string.modifyWatermarkTitle);
		String leftBtnText = getResources().getString(R.string.createNewWatermark);
		String rightBtnText = getResources().getString(R.string.coverWatermark);
		final DeleteDlg m_deleteTip = new DeleteDlg((Activity)getContext(), R.style.waitDialog);
		m_deleteTip.m_title.setVisibility(View.GONE);
		m_deleteTip.text.setText(text);
		m_deleteTip.cancelBtn.setText(leftBtnText);
		m_deleteTip.cancelBtn.setTextColor(0xffffce54);
		m_deleteTip.continueBtn.setText(rightBtnText);
		m_deleteTip.continueBtn.setTextColor(0xffa6a6a6);
		m_deleteTip.setOnDlgClickCallback(new DeleteDlg.OnDlgClickCallback()
		{
			@Override
			public void onDelete(Object res1)
			{
				//覆盖
				m_deleteTip.dismiss();
				if(res != null)
				{
					MyLogoRes my_res = res;
					String temp_Path = (String)my_res.m_res;
					String savePath = my_res.GetSaveParentPath() + File.separator +
							"." + System.currentTimeMillis() + (int)(Math.random() * 10000) + ".zip";
					saveWatermark(savePath, info, saveBmp);
					my_res.m_res = savePath;
					if(info != null)
					{
						info.image_zip = savePath;
					}
					coverWatermark(my_res);
					if(!savePath.equals(temp_Path))
					{
						FileUtil.deleteSDFile(temp_Path);
					}
				}
			}

			@Override
			public void onCancel()
			{

			}

			@Override
			public void onLeftBtnClick(Object... res1) {
				m_deleteTip.dismiss();
				//新建
				String savePath = FileCacheMgr.GetLinePath();

				saveWatermark(savePath, info, saveBmp);
				MyLogoRes my_res = new MyLogoRes();
				my_res.m_res = savePath;
				my_res.m_editable = true;
				if(res != null)
				{
					my_res.m_resArr = res.m_resArr;
				}
				showTextAddDlg(true, my_res, shape);
			}

			@Override
			public void onPageClick()
			{
				m_deleteTip.dismiss();
			}
		});
		m_deleteTip.show();
	}

	private void saveWatermark(String savePath, MyTextInfo info, Object saveBmp)
	{
		if(info != null && saveBmp != null)
		{
			String name = DownloadMgr.GetFileName(info.image_zip);
			name = name.substring(0, name.lastIndexOf("."));
			name += ".json";

			Zip.MotifyZipPartData(getContext(), savePath, name, "data.json", info.image_zip, saveBmp.toString());
		}
	}

	private void coverWatermark(MyLogoRes res) {
		Watermark watermark;
		if (res.m_editable) {
			watermark = new EditableWatermark(res);
			if(UserMgr.IsLogin(getContext(),null))
			{
				String userId = SettingInfoMgr.GetSettingInfo(getContext()).GetPoco2Id(true);
				String resUserId = res.m_userId + "";
				if(!resUserId.equals(userId))
				{
					res.mUniqueObjectId = -1;
					if (!TextUtils.isEmpty(userId)) {
						res.m_userId = Integer.parseInt(userId);
					}
					MyLogoResMgr.getInstance().UpdateMyLogoRes(res);

					//当成上传
					List<Watermark> watermarkList = new ArrayList<>();
					watermark.setOperateType(Watermark.OperateType.UPLOAD);
					watermarkList.add(watermark);
					WatermarkSyncManager.getInstacne(getContext()).uploadNewWatermark(watermarkList);

				} else {
					MyLogoResMgr.getInstance().UpdateMyLogoRes(res);

					WatermarkSyncManager.ModifyWatermarkInfo modifyWatermarkInfo = new WatermarkSyncManager.ModifyWatermarkInfo();
					modifyWatermarkInfo.mLocalId = res.m_id;
					modifyWatermarkInfo.mLocalPath = (String)res.m_res;
					modifyWatermarkInfo.mSaveTime = String.valueOf(System.currentTimeMillis() / 1000);
					modifyWatermarkInfo.mWatermarkTitle = res.m_name;
					modifyWatermarkInfo.mObjectId = res.mUniqueObjectId;
					modifyWatermarkInfo.mUserId = String.valueOf(res.m_userId);
					modifyWatermarkInfo.mEditable = res.m_editable;
					// 需要上传阿里云的修改
					modifyWatermarkInfo.mOperateType = Watermark.OperateType.MODIFY_UPLOAD;
					WatermarkSyncManager.getInstacne(getContext()).modifyTheWatermark(modifyWatermarkInfo, true);
				}
			} else {
				// 用户没有登录
				res.m_userId = MyLogoRes.USER_NONE_ID;
				res.mUniqueObjectId = -1;
				MyLogoResMgr.getInstance().UpdateMyLogoRes(res);
			}
		}
	}

	/**
	 * 点文字保存按钮的时候做的保存处理
	 * @param shape
	 * @param saveBmp
	 */
	private void OnSaveWaterMark(ShapeEx shape, Object saveBmp)
	{
		MyLogoRes res = null;
		boolean flag = false;
		if(shape.m_ex instanceof MyLogoRes)
		{
			res = (MyLogoRes) shape.m_ex;
			res.m_scale = shape.m_scaleX;
			flag = true;
		}
		else if(shape.m_ex instanceof MyTextInfo)
		{
			MyTextInfo info = (MyTextInfo)shape.m_ex;
			if(saveBmp != null && shape instanceof ShapeEx5)
			{
				if (info.m_ex instanceof MyLogoRes)
				{
					// 修改水印
					res = (MyLogoRes) info.m_ex;
					showWatermarkNotifyDialog(info, res, saveBmp, shape);
					return;
				}
				else
				{
					String savePath = FileCacheMgr.GetLinePath();

					saveWatermark(savePath, info, saveBmp);
					res = new MyLogoRes();
					res.m_res = savePath;
					res.m_editable = true;
					if(info.m_ex instanceof TextRes){
						res.m_resArr = ((TextRes)info.m_ex).m_resArr;
						MyBeautyStat.onSaveToMy(((TextRes)info.m_ex).m_tjId + "");
					}
				}
			}
		}
		if(res != null)
		{
			if(!flag)
			{
				if(m_curTextClassify == 1)
				{
					TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_水印_保存水印);
				}
				else if(m_curTextClassify == 2)
				{
					TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_态度_保存水印);
				}
			}
			showTextAddDlg(true, res, shape);
		}
	}

	protected MgrUtils.MyDownloadCB m_resDownloadCb = new MgrUtils.MyDownloadCB(new MgrUtils.MyCB() {

		@Override
		public void OnGroupComplete(int downloadId, IDownload[] resArr) {
			// TODO Auto-generated method stub

		}

		@Override
		public void OnFail(int downloadId, IDownload res) {
			Toast.makeText(getContext(), getResources().getString(R.string.downloadFailed), Toast.LENGTH_SHORT).show();
			if(m_listAdapter != null)
			{
				m_listAdapter.SetItemStyleByUri(((TextRes) res).m_id, DragListItemInfo.Style.NEED_DOWNLOAD);
			}
		}

		@Override
		public void OnGroupFail(int downloadId, IDownload[] resArr)
		{

		}

		@Override
		public void OnProgress(int downloadId, IDownload res, int progress)
		{
			if(m_listAdapter != null)
			{
				m_listAdapter.SetItemProgress(((TextRes) res).m_id, progress);
			}
		}

		@Override
		public void OnComplete(int downloadId, IDownload res)
		{
			if(m_listAdapter != null)
			{
				m_listAdapter.SetItemStyleByUri(((TextRes) res).m_id, DragListItemInfo.Style.NORMAL);
				if(m_curTextUri == ((TextRes) res).m_id)
				{
					int index = m_listAdapter.SetSelByUri(((TextRes) res).m_id);
					if(m_resList != null && m_resList.getRecyclerView() != null)
					{
						if(m_resList.getRecyclerView().getChildAt(index) == null)
						{
							m_resList.getRecyclerView().scrollToPosition(index);
						}
						View view = m_resList.getRecyclerView().getChildAt(index);
						DragListItemInfo info = m_listAdapter.GetItemInfoByUri(((TextRes) res).m_id);
						m_curTextUri = -1;
						m_listCallback.OnItemClick(view, info, index);
					}
				}
			}
		}

		@Override
		public void OnGroupProgress(int downloadId, IDownload[] resArr, int progress)
		{

		}
	});

	protected BeautifyViewV3.ControlCallback m_coreCallback = new BeautifyViewV3.ControlCallback() {

		@Override
		public Bitmap MakeOutputPendant(Object info, int outW, int outH)
		{
			Bitmap out = null;
			if(info != null && info instanceof MyTextInfo)
			{
				MyTextInfo imgInfo = (MyTextInfo)info;
				if(imgInfo.m_editable == false)
				{
					out = BeautifyHandler.MakeBmp(getContext(), imgInfo.m_pic, outW, outH);
				}
			}
			else if(info != null && info instanceof MyLogoRes)
			{
				out = BeautifyHandler.MakeBmp(getContext(), ((MyLogoRes) info).m_res, outW, outH);
			}
			return out;
		}

		@Override
		public void SelectPendant(int index)
		{
			if (m_view != null && m_view instanceof EditableTextView)
			{
				ShapeEx shape = m_view.GetCurrentSelPendantItem();
				if (shape != null && shape.m_ex instanceof MyTextInfo && !(shape instanceof ShapeEx5))
				{
					MyTextInfo info = (MyTextInfo) shape.m_ex;
					downloadFont((TextRes) info.m_ex);
				}
			}
		}

		@Override
		public Bitmap MakeShowPendant(Object info, int frW, int frH)
		{
			Bitmap out = null;
			if(info != null && info instanceof MyTextInfo)
			{
				MyTextInfo imgInfo = (MyTextInfo)info;
				if(imgInfo.m_editable == false)
				{
					out = BeautifyHandler.MakeBmp(getContext(), imgInfo.m_pic, frW, frH);
				}
			}
			else if(info != null && info instanceof  MyLogoRes)
			{
				out = BeautifyHandler.MakeBmp(getContext(), ((MyLogoRes) info).m_res, frW / 2, frH / 2);
			}
			return out;
		}

		@Override
		public Bitmap MakeShowImg(Object info, int frW, int frH)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bitmap MakeShowFrame(Object info, int frW, int frH) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bitmap MakeShowBK(Object info, int frW, int frH) {
			return MakeOutputBK(info, frW, frH);
		}

		@Override
		public Bitmap MakeOutputImg(Object info, int outW, int outH)
		{
			Bitmap out = BeautifyHandler.MakeBmp2(getContext(), info, -1, -1);

			return out;
		}

		@Override
		public Bitmap MakeOutputFrame(Object info, int outW, int outH)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Bitmap MakeOutputBK(Object info, int outW, int outH)
		{
			Bitmap out;

			Bitmap temp = Utils.DecodeImage(getContext(), info, 0, -1, outW, -1);
			out = MakeBmp.CreateBitmap(temp, outW, -1, -1, 0, Bitmap.Config.ARGB_8888);
			temp.recycle();
			temp = null;

			return out;
		}

		@Override
		public void TouchImage(boolean isTouch)
		{
			if(isTouch && m_isMyText)
			{
				if(m_isMyDeleteShow)
				{
					m_isMyDeleteShow = false;
					m_myResList.HideDeleteBtn(m_curMyTextUri);
				}
			}
		}
	};

	protected EditableTextView.OnMotifyListener m_motifyListener = new EditableTextView.OnMotifyListener() {

		@Override
		public void onClick(String text, float upY)
		{
			m_upY = upY;
			m_inputFr.setDatas(text);
			switch(m_curTextClassify)
			{
				case 1:
					MyBeautyStat.onClickByRes(R.string.照片水印_点击修改文本);
					break;
				case 2:
					MyBeautyStat.onClickByRes(R.string.照片态度_点击修改文本);
					break;
			}
			if (!m_isInputFrShow)
			{
				m_view.SetUIEnabled(false);
				showInputFr(true);
			}
		}

		@Override
		public void onChooseColor(boolean show, int color, int shadowAlpha)
		{
			if (show && m_colorChooser != null)
			{
				m_color = color;
				m_shadowAlpha = shadowAlpha;
				m_colorChooser.setSelectedItemByColor(color);
				int alpha = Painter.GetAlpha(m_color);
				switch(m_curTextClassify)
				{
					case 1:
						MyBeautyStat.onClickByRes(R.string.照片水印_选中照片水印);
						break;
					case 2:
						MyBeautyStat.onClickByRes(R.string.照片态度_选中照片态度);
						break;
				}

				int progress = (int)((alpha / 255f) * m_seekBar.getMax() + 0.5);
				ReLayoutSeekBarTip(progress, m_seekBar.getMax());
				m_seekBar.setProgress(progress);
				m_btnLst.onClick(m_colorChooserBtn);

				if(m_textOperateFr.getVisibility() == View.GONE)
				{
					showInputFr(false);
					if (m_isMyText)
					{
						SetViewState(m_textMyBtnFr, false, true);
					}
					else
					{
						SetViewState(m_bottomBar, false, true);
					}
					SetViewState(m_textOperateFr, true, true);
				}
			}
			if (!show && m_textOperateFr != null && m_textOperateFr.getVisibility() == View.VISIBLE)
			{
				if (m_isMyText)
				{
					SetViewState(m_textMyBtnFr, true, true);
				}
				else
				{
					SetViewState(m_bottomBar, true, true);
				}
				SetViewState(m_textOperateFr, false, true);
			}
			SetTitle();
		}

		@Override
		public void onSave(ShapeEx shape, Object saveBmp) {
			if(UserMgr.IsLogin(getContext(),null))
			{
				MyBeautyStat.onClickByRes(R.string.选中照片水印_保存水印);
				OnSaveWaterMark(shape, saveBmp);
			}
			else
			{
				showLoginTipDlg(true, shape, saveBmp);
			}

		}

		@Override
		public void onDelete(ShapeEx shape, int index) {
			MyBeautyStat.onClickByRes(R.string.选中照片水印_还原按钮);
			showInputFr(false);
			m_view.DelPendantByIndex(index);
			RemoveFromCacheInfo(shape);
			m_view.UpdateUI();
			if (m_colorChooser != null && m_textOperateFr.getVisibility() == View.VISIBLE) {
				if (m_isMyText) {
					SetViewState(m_textMyBtnFr, true, true);
				} else {
					SetViewState(m_bottomBar, true, true);
				}
				SetViewState(m_textOperateFr, false, true);
			}
			m_listAdapter.SetSelByUri(DragListItemInfo.URI_NONE);
			m_myResList.SetSelByIndex(-1);
			m_curTextUri = -1;
			m_curMyTextUri = "";
			SetTitle();
		}

		@Override
		public void onDevide()
		{
			if(m_curTextClassify == 1)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_水印_拆分水印);
			}
			else if(m_curTextClassify == 2)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_态度_拆分水印);
			}
			MyBeautyStat.onClickByRes(R.string.选中照片水印_拆分文字);
		}

		@Override
		public void onMerge()
		{
			MyBeautyStat.onClickByRes(R.string.选中照片水印_组合文字);
			if(m_curTextClassify == 1)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_水印_组合水印);
			}
			else if(m_curTextClassify == 2)
			{
				TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_态度_组合水印);
			}
		}
	};

	private InputDialog.InputCallback m_inputCb = new InputDialog.InputCallback() {

		@Override
		public void onOk()
		{
			showInputFr(false);
		}

		@Override
		public void onShowSoftInput(boolean show, int softHeight)
		{
			if(m_view != null)
			{
				m_isInputFrShow = show;
				ReLayoutTextShowView(m_upY, show, softHeight, true);
			}
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onChange(String lastText, String text)
		{
			if (!text.equals(lastText)) {
				m_view.UpdateText(text);
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
			int alpha = (int)(seekBar.getProgress() / (float)seekBar.getMax() * 255 + 0.5f);
			TongJi2.AddCountByRes(getContext(), R.integer.美化_光效_微调不透明度);
			if(m_alphaMode == TEXT_ALPHA)
			{
				m_color = m_view.SetAlpha(alpha);
			}
			else if(m_alphaMode == TEXT_SHADOW_ALPHA)
			{
				m_shadowAlpha = m_view.SetShadowAlpha(alpha);
			}
			m_view.UpdateUI();
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
				if (m_isInputFrShow)
				{
					showInputFr(false);
					return;
				}
				TongJi2.AddCountByRes(getContext(), R.integer.美化_文字_保存);
				m_uiEnabled = false;
				SetWaitUI(true, getResources().getString(R.string.processing));

				m_params.remove("curBmp");

				TextRes res = null;
				if (m_listDatas != null)
				{
					int size = m_listDatas.size();
					for (int i = 0; i < size; i++)
					{
						if (m_curTextUri == m_listDatas.get(i).m_uri)
						{
							res = (TextRes) m_listDatas.get(i).m_ex;
							break;
						}
					}
				}
				if (res != null)
				{
					MyBeautyStat.onUseText(res.m_tjId + "", m_color, m_shadowAlpha);
					MyBeautyStat.onClickByRes(R.string.照片文字_保存照片文字);
					TongJi2.AddCountById(res.m_tjId + "");
				}
				Bitmap mirror = ImageUtil.GetScreenBmp((Activity)getContext(), ShareData.m_screenWidth, ShareData.m_screenHeight);
				releaseMem();
				showPageMirror(mirror);

				m_org = m_view.GetOutputBmp(DEF_IMG_SIZE);
				m_view.ReleaseMem();
				BeautifyHandler.InitMsg info = new BeautifyHandler.InitMsg();
				info.m_inImgs = m_orgInfo;
				info.m_thumb = m_org;
				Message msg = m_mainHandler.obtainMessage();
				msg.what = BeautifyHandler.MSG_CACHE;
				msg.obj = info;
				m_mainHandler.sendMessage(msg);
			}
			else if(v == m_myFrCloseBtn)
			{
				if(m_isMyDeleteShow)
				{
					m_isMyDeleteShow = false;
					m_myResList.HideDeleteBtn(m_curMyTextUri);
					return;
				}
				m_isMyText = false;
				if(m_deleteTip != null)
				{
					m_deleteTip.dismiss();
				}
				SetTitle();
				MyBeautyStat.onClickByRes(R.string.我的_退出我的);
				SetViewState(m_textMyBtnFr, false, true);
				SetViewState(m_bottomBar, true, true);
			}
			else if(v == m_titleFr && m_helpFlag)
			{
				m_helpFlag = false;
				HashMap<String, Object> params = new HashMap<>();
				InitBkImg();
				params.put("img", m_bkBmp);
				if(m_isMyText)
				{
					m_site.OpenMyHelpPage(params,getContext());
				}
				else
				{
					int title = 0;
					if(m_curTextClassify == 1)
					{
						title = R.string.Watermark;
					}
					else if(m_curTextClassify == 2)
					{
						title = R.string.Attitude;
					}
					params.put("title", title);
					m_site.OpenHelpPage(params,getContext());
				}
			}
			else if(v == m_colorChooserBtn)
			{
				MyBeautyStat.onClickByRes(R.string.选中照片水印_切换至颜色);
				m_alphaMode = -1;
				m_colorChooserBtn.setBackgroundColor(0xff212121);
				m_textAlphaBtn.setBackgroundColor(0xff111111);
				m_shadowAlphaBtn.setBackgroundColor(0xff111111);
				m_colorChooser.setVisibility(VISIBLE);
				m_seekBarFr.setVisibility(GONE);
				SetTitle();
			}
			else if(v == m_textAlphaBtn)
			{
				MyBeautyStat.onClickByRes(R.string.选中照片水印_切换至不透明度);
				m_alphaMode = TEXT_ALPHA;
				m_colorChooserBtn.setBackgroundColor(0xff111111);
				m_textAlphaBtn.setBackgroundColor(0xff212121);
				m_shadowAlphaBtn.setBackgroundColor(0xff111111);
				m_colorChooser.setVisibility(GONE);
				m_colorChooser.cancelAbsorbing();
				m_seekBarFr.setVisibility(VISIBLE);

				int alpha = Painter.GetAlpha(m_color);

				int progress = (int)((alpha / 255f) * m_seekBar.getMax() + 0.5);
				ReLayoutSeekBarTip(progress, m_seekBar.getMax());
				m_seekBar.setProgress(progress);
				SetTitle();
			}
			else if(v == m_shadowAlphaBtn)
			{
				MyBeautyStat.onClickByRes(R.string.选中照片水印_切换至阴影);
				m_alphaMode = TEXT_SHADOW_ALPHA;
				m_colorChooserBtn.setBackgroundColor(0xff111111);
				m_textAlphaBtn.setBackgroundColor(0xff111111);
				m_shadowAlphaBtn.setBackgroundColor(0xff212121);
				m_colorChooser.setVisibility(GONE);
				m_seekBarFr.setVisibility(VISIBLE);
				m_colorChooser.cancelAbsorbing();

				int progress = (int)((m_shadowAlpha / 255f) * m_seekBar.getMax() + 0.5);
				ReLayoutSeekBarTip(progress, m_seekBar.getMax());
				m_seekBar.setProgress(progress);
				SetTitle();
			}
			else if(v == m_view)
			{
				if(m_isInputFrShow)
				{
					showInputFr(false);
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
			}
		}
	}
}
