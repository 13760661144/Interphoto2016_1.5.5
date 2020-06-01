package cn.poco.beautify.page;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.PhotoPicker.ImageViewer;
import cn.poco.PhotoPicker.MyImageViewer;
import cn.poco.PhotoPicker.site.PhotoPickerPageSite;
import cn.poco.album2.AlbumPage;
import cn.poco.album2.LocalStore;
import cn.poco.album2.PhotoStore;
import cn.poco.album2.model.PhotoInfo;
import cn.poco.album2.utils.AlbumUtils;
import cn.poco.album2.utils.PhotoUtils;
import cn.poco.beautify.BeautifyHandler;
import cn.poco.beautify.BeautifyModuleType;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.DeleteDlg;
import cn.poco.beautify.MyButtons;
import cn.poco.beautify.RestoreDlg;
import cn.poco.beautify.ScrollShareFr;
import cn.poco.beautify.WaitDialog1;
import cn.poco.beautify.site.BeautifyPageSite;
import cn.poco.beautify.site.ClipPageSite;
import cn.poco.beautify.site.FilterPageSite;
import cn.poco.beautify.site.LightEffectPageSite;
import cn.poco.beautify.site.TextPageSite;
import cn.poco.blogcore.FacebookBlog;
import cn.poco.camera.RotationImg2;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.setting.SettingPage;
import cn.poco.share.SharePage;
import cn.poco.share.ShareTools;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

/**
 * 美化首页
 */
public class BeautifyPage extends IPage {
	private static final String TAG = "美化首页";
	protected BeautifyPageSite m_site;

	protected int m_topBarHeight;
	protected int m_bottomBarHeight;
	protected int DEF_IMG_SIZE;
	protected int m_shareFrHeight;
	protected int m_frW;
	protected int m_frH;
	protected boolean m_isExit;

	protected ScrollShareFr m_mainFr;
	protected FrameLayout m_topBar;
	protected ImageView m_backBtn;
	protected ImageView m_shareBtn;

	protected HorizontalScrollView m_btnBarFr; // 最底下的分类按钮列
	protected MyButtons m_clipBtn; //剪切
	protected MyButtons m_effectBtn; //光效
	protected MyButtons m_filterBtn; //滤镜
	protected MyButtons m_textBtn; //文字

	protected MyButtons m_saveBtn;
	protected MyButtons m_restoreBtn;
	protected MyButtons m_copyBtn;
	protected MyButtons m_pasteBtn;
	protected MyButtons m_deleteBtn;
	protected MyButtons m_friendBtn;
	protected MyButtons m_weixinBtn;
	protected MyButtons m_sinaBtn;
	protected MyButtons m_qzoneBtn;
	protected MyButtons m_qqBtn;
	protected MyButtons m_facebookBtn;
	protected MyButtons m_instagramBtn;
	protected MyButtons m_twitterBtn;
	protected MyButtons m_circleBtn;
	protected ImageView m_shareHideBtn;
	protected ShareTools m_shareTools;

	protected boolean m_otherCall = false;    //是否第三方調用
	protected BeautifyModuleType m_selModule = BeautifyModuleType.NONE;
	protected int m_selUri = -1;
	protected WaitDialog1 m_waitDlg;
	protected DeleteDlg m_deleteTip;
	protected RestoreDlg m_restoreDlg;
	private AlertDialog m_shareTip;

	protected HandlerThread m_imageThread;
	protected BeautifyHandler m_mainHandler;
	protected UIHandler m_UIHandler;
	protected MyBtnLst m_btnLst = new MyBtnLst();
	protected boolean m_uiEnabled; // 界面控制总开关

	protected ImageStore.ImageInfo m_orgInfo;
	protected ArrayList<ImageStore.ImageInfo> m_allImages;
	protected int m_curImgIndex = -1;
	protected MyImageViewer m_view;
	protected Bitmap m_bkBmp;
	protected boolean m_canSave;
	protected boolean m_canRestore;
	protected boolean m_canCopy;
	protected String m_pasteStr;
//	protected HashMap<Integer, ArrayList<ImageStore.ImageInfo>> m_histories;	//历史记录

	private float m_childImgH = 1.0f;    //做动画用
	private float m_curImgH = 1.0f;
	private float m_childViewH = 0.0f;
	private ProgressDialog mProgressDialog;

	private PhotoStore mPhotoStore;
	private boolean isFromAlbum = false;
	private boolean mIsLocalAlbum = false;

	private String mFolderName;

	public BeautifyPage(Context context, BaseSite site) {
		super(context, site);
		m_site = (BeautifyPageSite)site;
		InitData();
		InitUI();
	}

	/**
	 * imgs:RotationImg2[]/ImageFile2/ImageInfo</br>
	 * def_page: Integer 默认打开的页面,可为null
	 * other_call : Boolean 是否第三方调用,可为null
	 * def_uri: Integer 默认选中的item
	 * is_local: Boolean 是否是app相册
	 * <p>
	 * is_child: Boolean 是否从子页面打开
	 * on_ok: Boolean 是否是美化子页面点勾保存
	 * all_imgs: ArrayList<ImageStore.ImageInfo>
	 * index: Integer
	 * from_album: boolean 是否相册跳转
	 */
	@Override
	public void SetData(HashMap<String, Object> params) {
		Object v;
		params = (HashMap<String, Object>)params.clone();

		v = params.get("other_call");
		if (v != null) {
			m_otherCall = (Boolean)v;
		}
		v = params.get("imgs");
		if (v != null && v instanceof RotationImg2[] && ((RotationImg2[])v).length > 0) {
			params.put("imgs", ((RotationImg2[])v).clone());
		}
		v = params.get("is_child");
		if (v == null || (Boolean)v == false) {
			MySetImages(params);
		} else {
			MySetImages2(params);
		}
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params) {
		super.onPageResult(siteID, params);
		if (siteID == SiteID.BEAUTY_CLIP || siteID == SiteID.BEAUTY_FILTER || siteID == SiteID.BEAUTY_TEXT || siteID == SiteID.BEAUTY_LIGHTEFFECT) {
			m_selModule = BeautifyModuleType.NONE;
			if (params != null) {
				InitData();
				InitUI();
				params = (HashMap<String, Object>)params.clone();
				Object v;
				v = params.get("other_call");
				if (v != null) {
					m_otherCall = (Boolean)v;
				}
				v = params.get("imgs");
				if (v != null && v instanceof RotationImg2[] && ((RotationImg2[])v).length > 0) {
					params.put("imgs", ((RotationImg2[])v).clone());
				}
				MySetImages2(params);
			}
		}
		else
		{
			switch(m_selModule)
			{
				case TEXT:
					if(m_textPage != null)
					{
						m_textPage.onPageResult(siteID, params);
					}
					break;
				case FILTER:
					if(m_filterPage != null)
					{
						m_filterPage.onPageResult(siteID, params);
					}
					break;
				case CLIP:
					if(m_clipPage != null)
					{
						m_clipPage.onPageResult(siteID, params);
					}
					break;
				case EFFECT:
					if(m_effectPage != null)
					{
						m_effectPage.onPageResult(siteID, params);
					}
					break;
				default:
					break;
			}
		}
	}

	/**
	 * 正常流程进来
	 *
	 * @param params
	 */
	protected void MySetImages(HashMap<String, Object> params) {
		m_site.m_myParams.clear();

		Object obj;
		obj = params.get("other_call");
		if (obj != null) {
			m_otherCall = (Boolean)obj;
		}
		obj = params.get("def_page");
		if (obj != null) {
			m_selModule = BeautifyModuleType.GetType((Integer)obj);
		}
		obj = params.get("def_uri");
		if (obj != null) {
			m_selUri = (Integer)obj;
		}

		obj = params.get("folder_name");
		if (obj instanceof String) {
			mFolderName = (String)obj;
		}

		m_uiEnabled = false;
		m_view.setUIEnabled(m_uiEnabled);
		Object orgInfo = params.get("imgs");
		BeautifyHandler.InitMsg info = new BeautifyHandler.InitMsg();
		info.m_inImgs = orgInfo;
		obj = params.get("is_local");
		if (obj != null) {
			mIsLocalAlbum = (Boolean)obj;
		}
		obj = params.get("from_album");
		if (obj instanceof Boolean) {
			isFromAlbum = (boolean)obj;
		}
		PhotoStore.getInstance(getContext()).clearCache();
		Message msg = m_mainHandler.obtainMessage();
		msg.what = BeautifyHandler.MSG_INIT;
		msg.obj = info;
		m_mainHandler.sendMessage(msg);
	}

	/**
	 * 从子页面返回
	 *
	 * @param params
	 */
	protected void MySetImages2(HashMap<String, Object> params) {
		m_site.m_myParams.clear();

		Object obj;
		m_uiEnabled = true;
		if(m_view != null)
		{
			m_view.setUIEnabled(m_uiEnabled);
		}
		m_selModule = BeautifyModuleType.NONE;
		obj = params.get("other_call");
		if (obj != null) {
			m_otherCall = (Boolean)obj;
		}
		obj = params.get("all_imgs");
		if (obj != null) {
			m_allImages = (ArrayList<ImageStore.ImageInfo>)obj;
		}
		int index = 0;
		obj = params.get("index");
		if (obj != null) {
			index = (Integer)obj;
		}

		obj = params.get("folder_name");
		if (obj instanceof String) {
			mFolderName = (String)obj;
		}

		obj = params.get("imgs");
		if (obj != null) {
			m_orgInfo = (ImageStore.ImageInfo)obj;
		}

		obj = params.get("curBmp");
		Bitmap bmp = null;
		if (obj != null) {
			bmp = (Bitmap)obj;
		}

		if (m_allImages != null && m_allImages.size() > 0 && m_orgInfo != null) {
			obj = params.get("on_ok");
			if (obj != null && (Boolean)obj == true) {

				if (index < m_allImages.size()) {
					m_allImages.set(index, m_orgInfo);
				} else {
					int realIndex = mPhotoStore.mapFromCacheIndex(index);
					m_allImages.clear();
					m_allImages.addAll(PhotoUtils.change(mPhotoStore.getPhotoInfos(mFolderName, realIndex)));
					index = mPhotoStore.mapToCacheIndex(realIndex);
					m_allImages.set(index, m_orgInfo);
				}

				if (isFromAlbum) {
					AlbumPage.sLocalAlbum = true;
				}
			}

			if (m_view.getImageCount() <= 0) {
				m_view.setImages(m_allImages);
			}
			if(bmp == null)
			{
				bmp = BeautifyHandler.MakeBmp(getContext(), m_orgInfo.image, m_frW, m_frH);
			}
			else
			{
				bmp = MakeBmpV2.CreateBitmapV2(bmp, 0, 0, -1, m_frW, m_frH, Bitmap.Config.ARGB_8888);
			}
			if (bmp != null) {
				float scale1 = (float)m_frW / (float)bmp.getWidth();
				float scale2 = (float)m_frH / (float)bmp.getHeight();
				m_curImgH = bmp.getHeight() * ((scale1 > scale2) ? scale2 : scale1);
				SetViewState(m_btnBarFr, true, true);

				obj = params.get("imgh");
				if (obj != null) {
					m_childImgH = (Float)obj;
				}
				obj = params.get("viewh");
				if (obj != null) {
					m_childViewH = (Integer)obj;
				}

				m_view.leave();
				m_view.enter(index, bmp);
				SetShowViewAnim();
			} else {
				Toast.makeText(getContext(), "保存失败", Toast.LENGTH_SHORT).show();
			}
			Utils.AlphaAnim(m_shareBtn, true, 400);

			SetBtnState(m_orgInfo);
		}
	}

	private void SetShowViewAnim() {
		float startScale = m_childImgH / m_curImgH;
		float endScale = 1;
		AnimationSet as;
		ScaleAnimation sa;
		as = new AnimationSet(true);
		sa = new ScaleAnimation(startScale, endScale, startScale, endScale, m_frW / 2, m_frH / 2);
		sa.setDuration(350);
		as.addAnimation(sa);

		if (m_childViewH != 0) {
			float start = (m_childViewH - m_frH) / 2f;
			TranslateAnimation ta = new TranslateAnimation(0, 0, start, 0);
			ta.setDuration(350);
			as.addAnimation(ta);
		}

		m_view.startAnimation(as);
		as.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (m_view != null) {
					m_view.clearAnimation();
				}
			}
		});
	}

	@Override
	public void onBack() {
		switch(m_selModule)
		{
			case TEXT:
				if(m_textPage != null)
				{
					m_textPage.onBack();
				}
				break;
			case FILTER:
				if(m_filterPage != null)
				{
					m_filterPage.onBack();
				}
				break;
			case CLIP:
				if(m_clipPage != null)
				{
					m_clipPage.onBack();
				}
				break;
			case EFFECT:
				if(m_effectPage != null)
				{
					m_effectPage.onBack();
				}
				break;
			default:
				if(m_mainFr != null && m_mainFr.IsTopBarShowing())
				{
					MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_退出保存与分享);
					m_mainFr.ShowTopBar(false);
					return;
				}

				mPhotoStore.clearCache();
				m_site.OnBack(getContext());
				break;
		}
	}

	@Override
	public void onClose() {
		if(m_textPage != null)
		{
			m_selModule = BeautifyModuleType.NONE;
			removeView(m_textPage);
			m_textPage.onClose();
			m_textPage = null;
			return;
		}
		if(m_filterPage != null)
		{
			m_selModule = BeautifyModuleType.NONE;
			removeView(m_filterPage);
			m_filterPage.onClose();
			m_filterPage = null;
			return;
		}
		if(m_clipPage != null)
		{
			m_selModule = BeautifyModuleType.NONE;
			removeView(m_clipPage);
			m_clipPage.onClose();
			m_clipPage = null;
			return;
		}
		if(m_effectPage != null)
		{
			m_selModule = BeautifyModuleType.NONE;
			removeView(m_effectPage);
			m_effectPage.onClose();
			m_effectPage = null;
			return;
		}
		m_uiEnabled = false;
		m_isExit = true;
		if (m_imageThread != null) {
			m_imageThread.quit();
			m_imageThread = null;
		}
		m_topBar.setBackgroundColor(0xff000000);
		m_btnBarFr.setBackgroundColor(0xff000000);
		if (m_bkBmp != null) {
			m_bkBmp.recycle();
			m_bkBmp = null;
		}
		if (m_waitDlg != null) {
			m_waitDlg.dismiss();
			m_waitDlg = null;
		}
		if (m_view != null) {
			m_view.clearAnimation();
			m_view.clear();
			m_view.setSwitchListener(null);
			m_view = null;
		}
		if (m_restoreDlg != null) {
			m_restoreDlg.dismiss();
			m_restoreDlg = null;
		}
		if (m_deleteTip != null) {
			m_deleteTip.dismiss();
			m_deleteTip = null;
		}
		this.removeAllViews();
		MyBeautyStat.onPageEndByRes(R.string.照片美化页);
		TongJiUtils.onPageEnd(getContext(), TAG);
	}

	@Override
	public void onResume()
	{
		switch(m_selModule)
		{
			case TEXT:
				if(m_textPage != null)
				{
					m_textPage.onResume();
				}
				break;
			case FILTER:
				if(m_filterPage != null)
				{
					m_filterPage.onResume();
				}
				break;
			case CLIP:
				if(m_clipPage != null)
				{
					m_clipPage.onResume();
				}
				break;
			case EFFECT:
				if(m_effectPage != null)
				{
					m_effectPage.onResume();
				}
				break;
		}
		TongJiUtils.onPageResume(getContext(), TAG);
		super.onResume();
	}

	@Override
	public void onPause()
	{
		switch(m_selModule)
		{
			case TEXT:
				if(m_textPage != null)
				{
					m_textPage.onPause();
				}
				break;
			case FILTER:
				if(m_filterPage != null)
				{
					m_filterPage.onPause();
				}
				break;
			case CLIP:
				if(m_clipPage != null)
				{
					m_clipPage.onPause();
				}
				break;
			case EFFECT:
				if(m_effectPage != null)
				{
					m_effectPage.onPause();
				}
				break;
		}
		TongJiUtils.onPagePause(getContext(), TAG);
		super.onPause();
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(m_selModule)
		{
			case TEXT:
				if(m_textPage != null)
				{
					m_textPage.onActivityResult(requestCode, resultCode, data);
				}
				break;
			case FILTER:
				if(m_filterPage != null)
				{
					m_filterPage.onActivityResult(requestCode, resultCode, data);
				}
				break;
			case CLIP:
				if(m_clipPage != null)
				{
					m_clipPage.onActivityResult(requestCode, resultCode, data);
				}
				break;
			case EFFECT:
				if(m_effectPage != null)
				{
					m_effectPage.onActivityResult(requestCode, resultCode, data);
				}
				break;
			default:
				if (m_shareTools != null) {
					m_shareTools.onActivityResult(requestCode, resultCode, data);
				}
				break;
		}
		return super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	protected void SetBtnState(ImageStore.ImageInfo info) {
		if (info != null) {
			if (AlbumUtils.canRestore(getContext(), info.id)) {
				m_canRestore = true;
				m_restoreBtn.setAlpha(1f);
			} else {
				m_canRestore = false;
				m_restoreBtn.setAlpha(0.2f);
			}
			if (!info.localAlbum) {
				m_canSave = false;
				m_saveBtn.setAlpha(0.2f);
			} else {
				m_canSave = true;
				m_saveBtn.setAlpha(1f);
			}
			if (info.effect != null && info.effect.length() > 0) {
				m_canCopy = true;
				m_copyBtn.setAlpha(1f);
			} else {
				m_canCopy = false;
				m_copyBtn.setAlpha(0.2f);
			}
		}
	}

	protected void InitData() {
		TongJiUtils.onPageStart(getContext(), TAG);
		MyBeautyStat.onPageStartByRes(R.string.照片美化页);
		ShareData.InitData(getContext());

		mPhotoStore = PhotoStore.getInstance(getContext());

		m_isExit = false;
		m_canSave = false;

		m_topBarHeight = ShareData.PxToDpi_xhdpi(80);
		m_bottomBarHeight = ShareData.PxToDpi_xhdpi(180);
		m_shareFrHeight = ShareData.PxToDpi_xhdpi(655);
		DEF_IMG_SIZE = SysConfig.GetPhotoSize(getContext());
		m_frW = ShareData.m_screenWidth;
		m_frH = ShareData.m_screenHeight - m_topBarHeight - m_bottomBarHeight;
		m_shareTools = new ShareTools(getContext());
		InitBmpRemScale();

		m_UIHandler = new UIHandler();
		m_imageThread = new HandlerThread("beauty_thread");
		m_imageThread.start();
		m_mainHandler = new BeautifyHandler(m_imageThread.getLooper(), getContext(), m_UIHandler);

	}

	/**
	 * 缩小压缩系数
	 */
	protected void InitBmpRemScale()
	{
		MakeBmpV2.MEM_SCALE *= 2;
		float mem = Runtime.getRuntime().maxMemory() / 1048576f;
//		System.out.println("mem : " + mem);
		float minScale = 1f / 12f;
		if(mem * MakeBmpV2.MEM_SCALE > 32)
		{
			MakeBmpV2.MEM_SCALE = 32f / mem;
		}
		if(MakeBmpV2.MEM_SCALE < minScale)
		{
			MakeBmpV2.MEM_SCALE = minScale;
		}
//		System.out.println("MEM_SCALE1111 : " + MakeBmpV2.MEM_SCALE);
	}

	protected void InitUI() {
		FrameLayout.LayoutParams fl;
		this.setBackgroundColor(0xff0e0e0e);

		m_mainFr = new ScrollShareFr(this, m_shareFrHeight);
		m_mainFr.setOnCloseListener(new ScrollShareFr.OnCloseListener()
		{
			@Override
			public void onClose()
			{
				m_btnLst.onClick(m_shareHideBtn);
			}
		});
		m_mainFr.setAnimTime(300);

		InitShareFr();

		m_topBar = new FrameLayout(getContext());
		m_topBar.setBackgroundColor(0xff0e0e0e);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_topBarHeight);
		fl.gravity = Gravity.TOP;
		m_topBar.setLayoutParams(fl);
		m_mainFr.AddMainChild(m_topBar);
		{
			m_backBtn = new ImageView(getContext());
			m_backBtn.setImageResource(R.drawable.framework_back_btn);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
			m_backBtn.setLayoutParams(fl);
			m_topBar.addView(m_backBtn);
			m_backBtn.setOnClickListener(m_btnLst);

			m_shareBtn = new ImageView(getContext());
			m_shareBtn.setImageResource(R.drawable.framework_share_btn);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
			m_shareBtn.setLayoutParams(fl);
			m_topBar.addView(m_shareBtn);
			m_shareBtn.setOnClickListener(m_btnLst);
		}

		m_view = new MyImageViewer(getContext());
		m_view.setMaxSize((int)(ShareData.m_screenWidth * 1.5f));
//		m_view.setVisibility(View.GONE);
		m_view.setBackgroundColor(0xff0e0e0e);
		m_view.setSwitchListener(new ImageViewer.OnSwitchListener() {
			@Override
			public void onSwitch(ImageStore.ImageInfo img, int index) {
				if (img != null) {
					m_orgInfo = img;
					m_curImgIndex = index;

					SetBtnState(m_orgInfo);

					if (!mIsLocalAlbum && !m_otherCall) {
						index = mPhotoStore.mapFromCacheIndex(index);
						if(mPhotoStore.shouldReloadData(mFolderName, index))
						{
							String currentPath = m_allImages.get(m_curImgIndex).image;

							m_allImages.clear();
							List<PhotoInfo> temp = mPhotoStore.getPhotoInfos(mFolderName, index);
							m_allImages.addAll(PhotoUtils.change(temp));

//							m_curImgIndex = mPhotoStore.mapToCacheIndex(index);

							int size = m_allImages.size();
							ImageStore.ImageInfo imageInfo;
							for (int i = size / 2, j = i + 1; i >= 0 || j < size; i--, j++) {
								if (i >= 0) {
									imageInfo = m_allImages.get(i);
									if (imageInfo.image.equals(currentPath)) {
										m_curImgIndex = i;
										break;
									}
								}

								if (j < size) {
									imageInfo = m_allImages.get(j);
									if (imageInfo.image.equals(currentPath)) {
										m_curImgIndex = j;
										break;
									}
								}
							}
							m_view.updateImages(m_allImages, m_curImgIndex);
						}
						else
						{
							m_curImgIndex = mPhotoStore.mapToCacheIndex(index);
						}
					}
				}
			}
		});
		fl = new FrameLayout.LayoutParams(m_frW, m_frH);
		fl.gravity = Gravity.BOTTOM;
		fl.bottomMargin = m_bottomBarHeight;
		m_view.setLayoutParams(fl);
		m_mainFr.AddMainChild(m_view, 0);

		m_btnBarFr = new HorizontalScrollView(getContext());
		m_btnBarFr.setBackgroundColor(0xff0e0e0e);
		m_btnBarFr.setHorizontalScrollBarEnabled(false);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_bottomBarHeight);
		fl.gravity = Gravity.BOTTOM | Gravity.LEFT;
		m_btnBarFr.setLayoutParams(fl);
		m_mainFr.AddMainChild(m_btnBarFr);
		{
			LinearLayout btnList = new LinearLayout(getContext());
			btnList.setGravity(Gravity.CENTER_VERTICAL);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
			btnList.setLayoutParams(fl);
			m_btnBarFr.addView(btnList);
			{
				LinearLayout.LayoutParams ll;
				m_clipBtn = new MyButtons(getContext(), R.drawable.beauty_clip_btn, R.drawable.beauty_clip_btn);
				m_clipBtn.setOnClickListener(m_btnLst);
				ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth / 4, LinearLayout.LayoutParams.MATCH_PARENT);
				ll.weight = 1;
				m_clipBtn.setLayoutParams(ll);
				btnList.addView(m_clipBtn);

				m_filterBtn = new MyButtons(getContext(), R.drawable.beauty_color_btn, R.drawable.beauty_color_btn);
				m_filterBtn.setOnClickListener(m_btnLst);
				String filterId = TagMgr.GetTagValue(getContext(), Tags.FILTER_RECOMMENT_ID);
				if (!TextUtils.isEmpty(filterId) && TagMgr.CheckTag(getContext(), Tags.FILTER_RECOMMENT_NEW + filterId)) {
					m_filterBtn.SetNew(true);
				}
				ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth / 4, LinearLayout.LayoutParams.MATCH_PARENT);
				ll.weight = 1;
				m_filterBtn.setLayoutParams(ll);
				btnList.addView(m_filterBtn);

				m_effectBtn = new MyButtons(getContext(), R.drawable.beauty_lighteffect_btn, R.drawable.beauty_lighteffect_btn);
				m_effectBtn.setOnClickListener(m_btnLst);
				if (TagMgr.CheckTag(getContext(), "light_effect_new")) {
					m_effectBtn.SetNew(true);
				}
				String effectId = TagMgr.GetTagValue(getContext(), Tags.EFFECT_RECOMMENT_ID);
				if (!TextUtils.isEmpty(effectId) && TagMgr.CheckTag(getContext(), Tags.EFFECT_RECOMMENT_NEW + effectId)) {
					m_effectBtn.SetNew(true);
				}
				ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth / 4, LinearLayout.LayoutParams.MATCH_PARENT);
				ll.weight = 1;
				m_effectBtn.setLayoutParams(ll);
				btnList.addView(m_effectBtn);

				m_textBtn = new MyButtons(getContext(), R.drawable.beauty_text_btn, R.drawable.beauty_text_btn);
				m_textBtn.setOnClickListener(m_btnLst);
				String textId = TagMgr.GetTagValue(getContext(), Tags.TEXT_RECOMMENT_ID);
				if (!TextUtils.isEmpty(textId) && TagMgr.CheckTag(getContext(), Tags.TEXT_RECOMMENT_NEW + textId)) {
					m_textBtn.SetNew(true);
				}
				ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth / 4, LinearLayout.LayoutParams.MATCH_PARENT);
				ll.weight = 1;
				m_textBtn.setLayoutParams(ll);
				btnList.addView(m_textBtn);
			}
		}

		m_waitDlg = new WaitDialog1(getContext(), R.style.waitDialog);

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

	protected void InitShareFr() {
		LinearLayout.LayoutParams ll;
		int width = ShareData.m_screenWidth / 5;
		LinearLayout shareFr = new LinearLayout(getContext());
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		ll.topMargin = ShareData.PxToDpi_xhdpi(35);
		shareFr.setLayoutParams(ll);
		m_mainFr.AddTopChild(shareFr);
		{
			m_saveBtn = new MyButtons(getContext(), R.drawable.beauty_save_btn, R.drawable.beauty_save_btn);
			m_saveBtn.SetText(getResources().getString(R.string.Save));
			m_saveBtn.setAlpha(0.2f);
			AddItem(shareFr, m_saveBtn, width);

			m_restoreBtn = new MyButtons(getContext(), R.drawable.beauty_restore_btn, R.drawable.beauty_restore_btn);
			m_restoreBtn.SetText(getResources().getString(R.string.Restore));
			m_restoreBtn.setAlpha(0.2f);
			AddItem(shareFr, m_restoreBtn, width);

			m_copyBtn = new MyButtons(getContext(), R.drawable.beauty_copy_btn, R.drawable.beauty_copy_btn);
			m_copyBtn.SetText(getResources().getString(R.string.Copy));
			m_copyBtn.setAlpha(0.2f);
			AddItem(shareFr, m_copyBtn, width);

			m_pasteBtn = new MyButtons(getContext(), R.drawable.beauty_paste_btn, R.drawable.beauty_paste_btn);
			m_pasteBtn.SetText(getResources().getString(R.string.Paste));
			if (AlbumPage.sCopy) {
				m_pasteBtn.setAlpha(1f);
			} else {
				m_pasteBtn.setAlpha(0.2f);
			}
			AddItem(shareFr, m_pasteBtn, width);

			m_deleteBtn = new MyButtons(getContext(), R.drawable.beauty_share_delete, R.drawable.beauty_share_delete);
			m_deleteBtn.SetText(getResources().getString(R.string.Delete));
			AddItem(shareFr, m_deleteBtn, width);
		}

		shareFr = new LinearLayout(getContext());
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		ll.topMargin = ShareData.PxToDpi_xhdpi(50);
		ll.bottomMargin = ShareData.PxToDpi_xhdpi(40);
		shareFr.setLayoutParams(ll);
		m_mainFr.AddTopChild(shareFr);
		{
			TextView text = new TextView(getContext());
			text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			text.setTextColor(0xff555555);
			text.setText(getResources().getString(R.string.ShareTo));
			ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			ll.leftMargin = ShareData.PxToDpi_xhdpi(30);
			ll.rightMargin = ShareData.PxToDpi_xhdpi(30);
			text.setLayoutParams(ll);
			shareFr.addView(text);

			View view = new View(getContext());
			view.setBackgroundColor(0xff555555);
			ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 2);
			ll.gravity = Gravity.CENTER_VERTICAL;
			ll.rightMargin = ShareData.PxToDpi_xhdpi(30);
			view.setLayoutParams(ll);
			shareFr.addView(view);

		}

		shareFr = new LinearLayout(getContext());
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		ll.bottomMargin = ShareData.PxToDpi_xhdpi(40);
		shareFr.setLayoutParams(ll);
		m_mainFr.AddTopChild(shareFr);
		{
			m_circleBtn = new MyButtons(getContext(), R.drawable.beauty_share_circle, R.drawable.beauty_share_circle);
			m_circleBtn.SetText(getResources().getString(R.string.Circle));
			AddItem(shareFr, m_circleBtn, width);

			m_friendBtn = new MyButtons(getContext(), R.drawable.beauty_share_friend, R.drawable.beauty_share_friend);
			m_friendBtn.SetText(getResources().getString(R.string.Moments));
			AddItem(shareFr, m_friendBtn, width);

			m_weixinBtn = new MyButtons(getContext(), R.drawable.beauty_share_weixin, R.drawable.beauty_share_weixin);
			m_weixinBtn.SetText(getResources().getString(R.string.Wechat));
			AddItem(shareFr, m_weixinBtn, width);

			m_sinaBtn = new MyButtons(getContext(), R.drawable.beauty_share_sina, R.drawable.beauty_share_sina);
			m_sinaBtn.SetText(getResources().getString(R.string.Sina));
			AddItem(shareFr, m_sinaBtn, width);

			m_qzoneBtn = new MyButtons(getContext(), R.drawable.beauty_share_qzone, R.drawable.beauty_share_qzone);
			m_qzoneBtn.SetText(getResources().getString(R.string.QQZone));
			AddItem(shareFr, m_qzoneBtn, width);
		}

		shareFr = new LinearLayout(getContext());
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		ll.bottomMargin = ShareData.PxToDpi_xhdpi(40);
		shareFr.setLayoutParams(ll);
		m_mainFr.AddTopChild(shareFr);
		{
			m_qqBtn = new MyButtons(getContext(), R.drawable.beauty_share_qq, R.drawable.beauty_share_qq);
			m_qqBtn.SetText(getResources().getString(R.string.QQ));
			AddItem(shareFr, m_qqBtn, width);

			m_facebookBtn = new MyButtons(getContext(), R.drawable.beauty_share_facebook, R.drawable.beauty_share_facebook);
			m_facebookBtn.SetText("Facebook");
			AddItem(shareFr, m_facebookBtn, width);

			m_instagramBtn = new MyButtons(getContext(), R.drawable.beauty_share_ins, R.drawable.beauty_share_ins);
			m_instagramBtn.SetText("Instagram");
			AddItem(shareFr, m_instagramBtn, width);
			String ver = SysConfig.GetAppVer(getContext());
			if(ver != null && ver.endsWith("_r3"))
			{
				m_instagramBtn.setVisibility(GONE);
				m_facebookBtn.setVisibility(GONE);
			}

			m_twitterBtn = new MyButtons(getContext(), R.drawable.beauty_share_twitter, R.drawable.beauty_share_twitter);
			m_twitterBtn.SetText("Twitter");
			AddItem(shareFr, m_twitterBtn, width);
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

	protected void AddItem(LinearLayout parent, View child, int width) {
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_VERTICAL;
		child.setLayoutParams(ll);
		parent.addView(child);
		child.setOnClickListener(m_btnLst);
	}

	private void SetViewState(View v, boolean isOpen, boolean hasAnimation) {
		if (v == null) return;
		v.clearAnimation();

		int start;
		int end;
		if (isOpen) {
			v.setVisibility(View.VISIBLE);

			start = 1;
			end = 0;
		} else {
			v.setVisibility(View.GONE);

			start = 0;
			end = 1;
		}

		if (hasAnimation) {
			AnimationSet as;
			TranslateAnimation ta;
			as = new AnimationSet(true);
			ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, start, Animation.RELATIVE_TO_SELF, end);
			ta.setDuration(200);
			as.addAnimation(ta);
			v.startAnimation(as);
		}
	}

	protected void SetWaitUI(boolean flag, String str) {
		if (flag) {
			if (str == null) {
				str = "";
			}
			if (m_waitDlg != null) {
				m_waitDlg.show();
				m_waitDlg.setText(str);
			}
		} else {
			if (m_waitDlg != null) {
				m_waitDlg.hide();
			}
		}
	}

	protected int getImgIndex(Object res) {
		String path = "";
		if (res instanceof RotationImg2[]) {
			path = ((RotationImg2[])res)[0].m_orgPath;
		} else if (res instanceof String) {
			path = (String)res;
		}
		if (m_allImages != null && m_allImages.size() > 0 && path != null && path.length() > 0) {
			int length = m_allImages.size();
			for (int i = 0; i < length; i++) {
				if (m_allImages.get(i).image != null && m_allImages.get(i).image.equals(path)) {
					return i;
				}
			}
		}
		return 0;
	}

	protected class MyBtnLst implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (m_uiEnabled) {
				onClick(v, true);
			}
		}

		public void onClick(View v, boolean fromUser) {
			if (v == m_shareBtn) {
				if (m_otherCall) {
					HashMap<String, Object> params = new HashMap<String, Object>();
					if (m_orgInfo != null) {
						params.put("img", PhotoPickerPageSite.MakeRotationImg(new String[] {m_orgInfo.image})[0]);
					}
					m_site.OnSave(getContext(),params);
					return;
				}
				MyBeautyStat.onClickByRes(R.string.照片美化页_打开保存与分享);
				if (!m_mainFr.IsTopBarShowing()) {
					if (m_bkBmp == null) {
						Bitmap bmp = Bitmap.createBitmap(m_view.getWidth(), m_view.getHeight(), Bitmap.Config.ARGB_8888);
						Canvas canvas = new Canvas(bmp);
						m_view.draw(canvas);
						m_bkBmp = BeautifyResMgr.MakeBkBmp(bmp, ShareData.m_screenWidth, ShareData.m_screenHeight, 0x99000000, 0x28000000);
						if (bmp != null && bmp != m_bkBmp) {
							bmp.recycle();
						}
					}
					m_mainFr.SetMaskBk(m_bkBmp);
					m_mainFr.ShowTopBar(true);
				} else {
					m_mainFr.ShowTopBar(false);
				}
			} else if (v == m_saveBtn) {
				if (m_canSave) {
					TongJi2.AddCountByRes(getContext(), R.integer.分享_保存至本地);
					MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_保存);
					m_uiEnabled = false;
					if(m_view != null)
					{
						m_view.setUIEnabled(m_uiEnabled);
					}
					SetWaitUI(true, getResources().getString(R.string.saving));

					HashMap<String, Object> params = new HashMap<String, Object>();
					if (m_orgInfo != null) {
						params.put("img", m_orgInfo);
					}
					params.put("add_date", SettingInfoMgr.GetSettingInfo(getContext()).GetAddDateState());

					Message msg = m_mainHandler.obtainMessage();
					msg.what = BeautifyHandler.MSG_SAVE;
					msg.obj = params;
					m_mainHandler.sendMessage(msg);
				}
			} else if (v == m_restoreBtn) {
				if (m_canRestore) {
					TongJi2.AddCountByRes(getContext(), R.integer.分享_恢复);
					MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_恢复);
					if (m_restoreDlg == null) {
						m_restoreDlg = new RestoreDlg((Activity)getContext(), R.style.waitDialog);
						m_restoreDlg.setOnDlgClickCallback(new RestoreDlg.OnDlgClickCallback() {
							@Override
							public void onReset() {
								m_restoreDlg.dismiss();
								if (m_view != null && m_orgInfo != null) {
									MyBeautyStat.onClickByRes(R.string.恢复页_恢复原图);
									TongJi2.AddCountByRes(getContext(), R.integer.恢复原图);
									int index = m_view.getCurSel();
									if (AlbumUtils.canRestore(getContext(), m_orgInfo.id)) {
										ImageStore.ImageInfo info = AlbumUtils.restore(getContext(), m_orgInfo.id);
										if (info != null) {
											Bitmap bmp = BeautifyHandler.MakeBmp(getContext(), info.image, m_frW, m_frH);
											m_view.changeImage(info, index, bmp);
											m_allImages.set(index, info);
											m_orgInfo = info;
										}
									}

									onClick(m_shareHideBtn);
									SetBtnState(m_orgInfo);
								}
							}

							@Override
							public void onUndo() {
								m_restoreDlg.dismiss();
								if (m_view != null && m_orgInfo != null) {
									MyBeautyStat.onClickByRes(R.string.恢复页_恢复上一步);
									TongJi2.AddCountByRes(getContext(), R.integer.恢复上一步);
									int index = m_view.getCurSel();
									if (AlbumUtils.canUndo(getContext(), m_orgInfo.id)) {
										ImageStore.ImageInfo info = AlbumUtils.undo(getContext(), m_orgInfo.id);
										if (info != null) {
											Bitmap bmp = BeautifyHandler.MakeBmp(getContext(), info.image, m_frW, m_frH);
											m_view.changeImage(info, index, bmp);
											m_allImages.set(index, info);
											m_orgInfo = info;
										}
									}
								}

								onClick(m_shareHideBtn);
								SetBtnState(m_orgInfo);
							}
						});
					}
					m_restoreDlg.show();
				}
			} else if (v == m_copyBtn) {
				if (m_canCopy) {
					TongJi2.AddCountByRes(getContext(), R.integer.复制效果);
					MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_复制效果);
					m_pasteStr = m_orgInfo.effect;
					if (m_pasteStr != null && m_pasteStr.length() > 0) {
						m_pasteBtn.setAlpha(1f);

						AlbumPage.sCopy = true;
						if (AlbumPage.sCopyEffect == null) {
							AlbumPage.sCopyEffect = new AlbumPage.CopyEffect();
						}
						AlbumPage.sCopyEffect.effect = m_orgInfo.effect;
						if (AlbumPage.sCopyEffect.image != null && !AlbumPage.sCopyEffect.image.isRecycled()) {
							AlbumPage.sCopyEffect.image.recycle();
						}
						AlbumPage.sCopyEffect.image = Utils.DecodeImage(getContext(), m_orgInfo.image, 0, 0, ShareData.PxToDpi_xhdpi(56), ShareData.PxToDpi_xhdpi(56));
					} else {
						m_pasteBtn.setAlpha(0.2f);
					}
					onClick(m_shareHideBtn);
				}
			} else if (v == m_pasteBtn) {
				if (AlbumPage.sCopy && AlbumPage.sCopyEffect != null && m_orgInfo != null) {
					TongJi2.AddCountByRes(getContext(), R.integer.粘贴效果);
					MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_粘贴效果);
					m_uiEnabled = false;
					if(m_view != null)
					{
						m_view.setUIEnabled(m_uiEnabled);
					}
					SetWaitUI(true, getResources().getString(R.string.pasting));
					HashMap<String, Object> params = new HashMap<String, Object>();
					params.put("img", m_orgInfo);
					params.put("copy_effect", AlbumPage.sCopyEffect);

					Message msg = m_mainHandler.obtainMessage();
					msg.what = BeautifyHandler.MSG_PASTE;
					msg.obj = params;
					m_mainHandler.sendMessage(msg);
				}
			} else if (v == m_deleteBtn) {
				if (m_deleteTip == null) {
					m_deleteTip = new DeleteDlg((Activity)getContext(), R.style.waitDialog);
					m_deleteTip.setOnDlgClickCallback(new DeleteDlg.OnDlgClickCallback() {
						@Override
						public void onDelete(Object res) {
							if (res != null) {
								MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_删除);
								TongJi2.AddCountByRes(getContext(), R.integer.分享_删除);
								m_deleteTip.dismiss();
								if (m_orgInfo != null) {
									if (m_orgInfo.localAlbum) {
										AlbumUtils.deleteImage(getContext(), m_orgInfo);
									} else {
										PhotoUtils.deletePhoto(getContext(), m_orgInfo);
									}
								}
								m_allImages.remove(m_view.getCurSel());
								m_view.delImage(m_view.getCurSel());
								m_btnLst.onClick(m_shareHideBtn);
								Toast.makeText(getContext(), getResources().getString(R.string.deletedSuccess), Toast.LENGTH_SHORT).show();
								if (m_allImages != null && m_allImages.size() == 0) {
									BeautifyHandler.InitMsg info = new BeautifyHandler.InitMsg();
									mIsLocalAlbum = false;
									m_uiEnabled = false;
									if(m_view != null)
									{
										m_view.setUIEnabled(m_uiEnabled);
									}
									AlbumPage.sLocalAlbum = false;
									Message msg = m_mainHandler.obtainMessage();
									msg.what = BeautifyHandler.MSG_INIT;
									msg.obj = info;
									m_mainHandler.sendMessage(msg);
								}
							}
						}

						@Override
						public void onCancel() {
							m_deleteTip.dismiss();
						}

						@Override
						public void onPageClick()
						{

						}
					});
				}
				if (m_deleteTip != null) {
					m_deleteTip.setData(m_orgInfo);
					m_deleteTip.show();
				}
			} else if (v == m_friendBtn) {
				if (m_orgInfo != null) {
					TongJi2.AddCountByRes(getContext(), R.integer.分享_微信朋友圈);
					MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
					MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.朋友圈, R.string.照片美化页);
					m_shareTools.sendToWeiXin(m_orgInfo.image, false, new ShareTools.SendCompletedListener() {
						@Override
						public void result(int result) {
							if (result == ShareTools.SUCCESS) {
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
			} else if (v == m_weixinBtn) {
				if (m_orgInfo != null) {
					TongJi2.AddCountByRes(getContext(), R.integer.分享_微信好友);
					MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
					MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微信好友, R.string.照片美化页);
					m_shareTools.sendToWeiXin(m_orgInfo.image, true, new ShareTools.SendCompletedListener() {
						@Override
						public void result(int result) {
							if (result == ShareTools.SUCCESS) {
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
			} else if (v == m_sinaBtn) {
				if (SettingPage.checkSinaBindingStatus(getContext())) {
					if (m_orgInfo != null) {
						TongJi2.AddCountByRes(getContext(), R.integer.分享_新浪微博);
						MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
						MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博, R.string.照片美化页);
						m_shareTools.sendToSina(m_orgInfo.image, new ShareTools.SendCompletedListener() {
							@Override
							public void result(int result) {
								if (result == ShareTools.SUCCESS) {
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
				} else {
					m_shareTools.bindSina(new SharePage.BindCompleteListener() {
						@Override
						public void success() {
							Toast.makeText(getContext(), getResources().getString(R.string.Linked), Toast.LENGTH_SHORT).show();
							if (m_orgInfo != null) {
								TongJi2.AddCountByRes(getContext(), R.integer.分享_新浪微博);
								MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
								MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博, R.string.照片美化页);
								m_shareTools.sendToSina(m_orgInfo.image, new ShareTools.SendCompletedListener() {
									@Override
									public void result(int result) {
										if (result == ShareTools.SUCCESS) {
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
						}

						@Override
						public void fail() {
						}
					});
				}
			} else if (v == m_shareHideBtn) {
				MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_退出保存与分享);
				m_mainFr.ShowTopBar(false);
			} else if (v == m_qzoneBtn) {
				if (SettingPage.checkQzoneBindingStatus(getContext())) {
					if (m_orgInfo != null) {
						TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ空间);
						MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
						MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间, R.string.照片美化页);
						m_shareTools.sendToQzone(m_orgInfo.image, null);
					}
				} else {
					mProgressDialog = ProgressDialog.show(getContext(), "", getContext().getResources().getString(R.string.Linking));
					mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					m_shareTools.bindQzone(false, new SharePage.BindCompleteListener() {
						@Override
						public void success() {
							Toast.makeText(getContext(), getResources().getString(R.string.Linked), Toast.LENGTH_SHORT).show();
							if (m_orgInfo != null) {
								if (mProgressDialog != null) {
									mProgressDialog.dismiss();
									mProgressDialog = null;
								}
								TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ空间);
								MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
								MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间, R.string.照片美化页);
								m_shareTools.sendToQzone(m_orgInfo.image, null);
							}
						}

						@Override
						public void fail() {
							if (mProgressDialog != null) {
								mProgressDialog.dismiss();
								mProgressDialog = null;
							}
						}
					});
				}
			} else if (v == m_qqBtn) {
				if (SettingPage.checkQzoneBindingStatus(getContext())) {
					if (m_orgInfo != null) {
						TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ好友);
						MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
						MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ好友, R.string.照片美化页);
						m_shareTools.sendToQQ(m_orgInfo.image, null);
					}
				} else {
					mProgressDialog = ProgressDialog.show(getContext(), "", getContext().getResources().getString(R.string.Linking));
					mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					m_shareTools.bindQzone(false, new SharePage.BindCompleteListener() {
						@Override
						public void success() {
							Toast.makeText(getContext(), getResources().getString(R.string.Linked), Toast.LENGTH_SHORT).show();
							if (m_orgInfo != null) {
								if (mProgressDialog != null) {
									mProgressDialog.dismiss();
									mProgressDialog = null;
								}
								TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ好友);
								MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
								MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ好友, R.string.照片美化页);
								m_shareTools.sendToQQ(m_orgInfo.image, null);
							}
						}

						@Override
						public void fail() {
							if (mProgressDialog != null) {
								mProgressDialog.dismiss();
								mProgressDialog = null;
							}
						}
					});
				}
			} else if (v == m_facebookBtn) {
				TongJi2.AddCountByRes(getContext(), R.integer.分享_Facebook);
				MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
				MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.facebook, R.string.照片美化页);
				m_shareTools.sendToFacebook(m_orgInfo.image, new FacebookBlog.FaceBookSendCompleteCallback() {
					@Override
					public void sendComplete(int result, String error_info) {

					}
				});

			} else if (v == m_instagramBtn) {
				TongJi2.AddCountByRes(getContext(), R.integer.分享_Instagram);
				MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
				MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.instagram, R.string.照片美化页);
				m_shareTools.sendToInstagram(m_orgInfo.image);
			} else if (v == m_twitterBtn) {
				TongJi2.AddCountByRes(getContext(), R.integer.分享_Twitter);
				MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
				MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.twitter, R.string.照片美化页);
				m_shareTools.sendToTwitter(m_orgInfo.image, getResources().getString(R.string.viaInterPhoto));
			}else if (v == m_circleBtn){
				TongJi2.AddCountByRes(getContext(), R.integer.分享_在一起);
				MyBeautyStat.onClickByRes(R.string.图片美化_保存与分享_分享);
				MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.在一起, R.string.照片美化页);
				m_shareTools.sendToCircle(m_orgInfo.image, getResources().getString(R.string.viaInterPhoto), true, new ShareTools.SendCompletedListener()
				{
					@Override
					public void result(int result)
					{
						if (result == ShareTools.SUCCESS) {
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
			} else if (v == m_backBtn) {
				onBack();
			} else if (v == m_effectBtn) {
				TongJi2.AddCountByRes(getContext(), R.integer.美化_剪裁);
				HashMap<String, Object> params = MakeChildParams();
				if (params == null) {
					Toast.makeText(getContext(), getResources().getString(R.string.selectPhotoAgain), Toast.LENGTH_SHORT).show();
					return;
				}
				m_effectBtn.SetNew(false);
				TagMgr.SetTag(getContext(), "light_effect_new");
				String id = TagMgr.GetTagValue(getContext(), Tags.EFFECT_RECOMMENT_ID);
				if (!TextUtils.isEmpty(id)) {
					TagMgr.SetTag(getContext(), Tags.EFFECT_RECOMMENT_NEW + id);
				}
				onClose();
				OpenEffectPage(params);
			} else if (v == m_filterBtn) {
				TongJi2.AddCountByRes(getContext(), R.integer.美化_滤镜);
				HashMap<String, Object> params = MakeChildParams();
				if (params == null) {
					Toast.makeText(getContext(), getResources().getString(R.string.selectPhotoAgain), Toast.LENGTH_SHORT).show();
					return;
				}
				String filterId = TagMgr.GetTagValue(getContext(), Tags.FILTER_RECOMMENT_ID);
				if (!TextUtils.isEmpty(filterId)) {
					TagMgr.SetTag(getContext(), Tags.FILTER_RECOMMENT_NEW + filterId);
				}
				onClose();
				OpenFilterPage(params);
			} else if (v == m_textBtn) {
				TongJi2.AddCountByRes(getContext(), R.integer.美化_文字);

				HashMap<String, Object> params = MakeChildParams();
				if (params == null) {
					Toast.makeText(getContext(), getResources().getString(R.string.selectPhotoAgain), Toast.LENGTH_SHORT).show();
					return;
				}
				String id = TagMgr.GetTagValue(getContext(), Tags.TEXT_RECOMMENT_ID);
				if (!TextUtils.isEmpty(id)) {
					TagMgr.SetTag(getContext(), Tags.TEXT_RECOMMENT_NEW + id);
				}
				onClose();
				OpenTextPage(params);
			} else if (v == m_clipBtn) {
				TongJi2.AddCountByRes(getContext(), R.integer.美化_光效);
				HashMap<String, Object> params = MakeChildParams();
				if (params == null) {
					Toast.makeText(getContext(), getResources().getString(R.string.selectPhotoAgain), Toast.LENGTH_SHORT).show();
					return;
				}
				onClose();
				OpenClipPage(params);
			}
		}
	}

	private LightEffectPage m_effectPage;
	private LightEffectPageSite m_effectSite = new LightEffectPageSite()
	{
		@Override
		public void onBack(HashMap<String, Object> params,Context context)
		{
			if(m_effectPage != null)
			{
				BeautifyPage.this.removeView(m_effectPage);
				m_effectPage.onClose();
				m_effectPage = null;
			}

			onPageResult(SiteID.BEAUTY_LIGHTEFFECT, params);
		}
	};

	private void OpenEffectPage(HashMap<String, Object> params)
	{
		MyBeautyStat.onClickByRes(R.string.照片美化页_打开照片光效);
		m_selModule = BeautifyModuleType.EFFECT;
		if(m_effectPage != null)
		{
			removeView(m_effectPage);
			m_effectPage.onClose();
			m_effectPage = null;
		}

		m_effectPage = new LightEffectPage(getContext(), m_effectSite);
		m_effectPage.SetData(params);
		addView(m_effectPage);
	}

	private FilterPage m_filterPage;
	private FilterPageSite m_filterPageSite = new FilterPageSite()
	{
		@Override
		public void onBack(HashMap<String, Object> params,Context context)
		{
			if(m_filterPage != null)
			{
				BeautifyPage.this.removeView(m_filterPage);
				m_filterPage.onClose();
				m_filterPage = null;
			}

			onPageResult(SiteID.BEAUTY_FILTER, params);
		}
	};

	private void OpenFilterPage(HashMap<String, Object> params)
	{
		MyBeautyStat.onClickByRes(R.string.照片美化页_打开照片滤镜);
		if(m_filterPage != null)
		{
			removeView(m_filterPage);
			m_filterPage.onClose();
			m_filterPage = null;
		}

		m_selModule = BeautifyModuleType.FILTER;

		m_filterPage = new FilterPage(getContext(), m_filterPageSite);
		m_filterPage.SetData(params);
		addView(m_filterPage);
	}

	private TextPage m_textPage;
	private TextPageSite m_textPageSite = new TextPageSite()
	{
		@Override
		public void onBack(HashMap<String, Object> params,Context context)
		{
			if(m_textPage != null)
			{
				BeautifyPage.this.removeView(m_textPage);
				m_textPage.onClose();
				m_textPage = null;
			}

			onPageResult(SiteID.BEAUTY_TEXT, params);
		}
	};
	private void OpenTextPage(HashMap<String, Object> params)
	{
		if(m_textPage != null)
		{
			removeView(m_textPage);
			m_textPage.onClose();
			m_textPage = null;
		}
		MyBeautyStat.onClickByRes(R.string.照片美化页_打开照片文字);
		m_selModule = BeautifyModuleType.TEXT;

		m_textPage = new TextPage(getContext(), m_textPageSite);
		m_textPage.SetData(params);
		addView(m_textPage);
	}

	private ClipPage m_clipPage;
	private ClipPageSite m_clipPageSite = new ClipPageSite()
	{
		@Override
		public void onBack(HashMap<String, Object> params,Context context)
		{
			if(m_clipPage != null)
			{
				BeautifyPage.this.removeView(m_clipPage);
				m_clipPage.onClose();
				m_clipPage = null;
			}

			onPageResult(SiteID.BEAUTY_CLIP, params);
		}
	};
	private void OpenClipPage(HashMap<String, Object> params)
	{
		if(m_clipPage != null)
		{
			removeView(m_clipPage);
			m_clipPage.onClose();
			m_clipPage = null;
		}
		MyBeautyStat.onClickByRes(R.string.照片美化页_打开照片裁剪);

		m_selModule = BeautifyModuleType.CLIP;

		m_clipPage = new ClipPage(getContext(), m_clipPageSite);
		m_clipPage.SetData(params);
		addView(m_clipPage);
	}

	protected HashMap<String, Object> MakeChildParams() {
		boolean flag = CheckImgExist();
		if (flag) {
			HashMap<String, Object> out = new HashMap<>();
			out.put("imgs", m_orgInfo);
			out.put("other_call", m_otherCall);
			out.put("is_child", true);
			out.put("all_imgs", m_allImages);
			out.put("index", m_curImgIndex);
			if (m_selUri != -1) {
				out.put("sel_uri", m_selUri);
				m_selUri = -1;
			}
			if (m_view != null) {
				RectF rect = m_view.getCurCache();
				if (rect != null) {
					float height = rect.bottom - rect.top;
					float width = rect.right - rect.left;
					if (height > 0 && width > 0) {
						out.put("imgh", height);
						out.put("viewh", m_frH);
					}
				}
				out.put("curBmp", m_view.getCurBitmap());
			}
			return out;
		}
		return null;
	}

	protected boolean CheckImgExist() {
		if (m_orgInfo != null && m_orgInfo.image != null && new File(m_orgInfo.image).exists()) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(m_orgInfo.image, opts);
			if (opts.outWidth > 0 && opts.outHeight > 0) return true;
		}
		return false;
	}

	protected class UIHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BeautifyHandler.MSG_INIT: {
					BeautifyHandler.InitMsg params = (BeautifyHandler.InitMsg)msg.obj;
					msg.obj = null;
					RotationImg2[] tempInfo = (RotationImg2[])params.m_inImgs;

					SetWaitUI(false, "");
					m_uiEnabled = true;

					if (mIsLocalAlbum) {
						m_allImages = new ArrayList<>();
						m_allImages.addAll(LocalStore.getLocalAlbumList());
					} else {
						if (m_otherCall) {
							String imagePath = (tempInfo)[0].m_orgPath;
							m_allImages = new ArrayList<>(1);
							m_allImages.add(PhotoUtils.change(imagePath));

						} else {
							int index = 0;
							if (tempInfo != null) {
								String imagePath = (tempInfo)[0].m_orgPath;
								index = mPhotoStore.getPhotoInfoIndex(mFolderName, imagePath);
							}
							List<PhotoInfo> photoInfos = mPhotoStore.getPhotoInfos(mFolderName, index);
							m_allImages = (ArrayList<ImageStore.ImageInfo>) PhotoUtils.change(photoInfos);
						}
					}
					if(m_view != null)
					{
						m_view.setUIEnabled(m_uiEnabled);
					}

					int index = getImgIndex(tempInfo);
					if (index != -1 && index < m_allImages.size() &&
							m_allImages != null && m_allImages.size() > 0) {
						m_orgInfo = m_allImages.get(index);
						m_curImgIndex = mPhotoStore.mapToCacheIndex(index);
					}

					switch (m_selModule) {
						case TEXT: {
							m_btnLst.onClick(m_textBtn);
							break;
						}
						case FILTER: {
							m_btnLst.onClick(m_filterBtn);
							break;
						}
						case EFFECT: {
							m_btnLst.onClick(m_effectBtn);
							break;
						}
						default: {
							if (m_view != null) {
								if (m_view.getImageCount() <= 0) {
									m_view.setImages(m_allImages);
								}
								m_view.leave();
								m_view.enter(index, null);
							}
						}
					}

					break;
				}
				case BeautifyHandler.MSG_SAVE: {
					m_uiEnabled = true;
					if(m_view != null)
					{
						m_view.setUIEnabled(m_uiEnabled);
					}
					SetWaitUI(false, "");
					Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.Saved), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();

					if (m_orgInfo != null && m_view != null) {
						m_orgInfo.isSaved = true;
						m_canSave = false;
					}
					SetBtnState(m_orgInfo);
					m_btnLst.onClick(m_shareHideBtn);
					break;
				}
				case BeautifyHandler.MSG_PASTE: {
					HashMap<String, Object> params = (HashMap<String, Object>)msg.obj;
					msg.obj = null;
					if (params != null) {
						Object o = params.get("outInfo");
						if (o != null) {
							ImageStore.ImageInfo info = (ImageStore.ImageInfo)o;
							int index = m_view.getCurSel();

							if (info != null && m_view != null) {
								Bitmap bmp = BeautifyHandler.MakeBmp(getContext(), info.image, m_frW, m_frH);
								if (bmp != null) {
									m_view.changeImage(info, index, bmp);
									m_allImages.set(index, info);
								}
								m_orgInfo = info;
							}

							SetBtnState(m_orgInfo);
						}
					}
					m_uiEnabled = true;
					SetWaitUI(false, "");
					if(m_view != null)
					{
						m_view.setUIEnabled(m_uiEnabled);
					}
					m_btnLst.onClick(m_shareHideBtn);
					break;
				}
			}
		}
	}
}
