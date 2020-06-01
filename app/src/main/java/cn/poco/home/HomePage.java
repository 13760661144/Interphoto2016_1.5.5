package cn.poco.home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cn.poco.adMaster.AdMaster;
import cn.poco.adMaster.ClickAdRes;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.framework.BaseSite;
import cn.poco.framework.EventCenter;
import cn.poco.framework.EventID;
import cn.poco.framework.FileCacheMgr;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.home.site.HomePageSite;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.login.util.UserMgr;
import cn.poco.loginlibs.info.UserInfo;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.resource.ResourceMgr;
import cn.poco.resource.SwitchRes;
import cn.poco.resource.SwitchResMgr2;
import cn.poco.statistics.CheckUpdate;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.ConfigIni;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.LoopViewPager;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.ShareData;
import cn.poco.transitions.viewpager.ZoomOutTransformer3;
import cn.poco.utils.FileUtil;
import cn.poco.utils.ImageUtil;
import cn.poco.utils.MyNetCore;
import cn.poco.utils.PermissionUtils;
import cn.poco.utils.Utils;
import cn.poco.video.page.ReminderPage;
import cn.poco.webview.WebViewPage1;
import poco.photedatabaselib2016.PhotoDatabaseHelper;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static cn.poco.tianutils.ShareData.PxToDpi_xhdpi;

public class HomePage extends IPage {
	private static final String TAG = "首页";
	protected HomePageSite m_site;

	private boolean m_firstLayout = true;
	protected boolean m_uiEnabled; // 界面控制总开关
	protected boolean m_cmdEnabled; // 指令控制开关
	protected int m_imgFrW;
	protected int m_imgFrH;
	protected int m_bottomFrW;
	protected int m_bottomFrH;
	protected int m_logoY;
	protected boolean m_isFirst = true;
	protected static boolean m_isFirstTurning = true;
	protected static boolean m_hasCheckUpdate = false;    //首次启动的时候检查是否有更新
	protected static boolean m_hasCheckMem = false;        //首次启动检查内存
	protected boolean m_canChangeTurning = false;
	protected int m_currentBkIndex = -1;

	protected HandlerThread m_imageThread;
	protected HomeHandler m_mainHandler;
	protected UIHandler m_UIHandler;

	protected ArrayList<HomeImgInfo> m_imgs;
	protected MyBtnLst m_btnLst = new MyBtnLst();
	protected LoopViewPager<HomeImgInfo> m_viewPager;
	protected static int VIEWPAGER_TURNING_TIME = 5000;
	protected static int VIEWPAGER_FIRST_TURNING_TIME = 1000;
	protected DrawerLayout m_drawer;
	protected boolean m_drawerOpen;
	protected FrameLayout m_left;
	protected FrameLayout m_body;
	protected ImageView m_propertiesBtn; //属性按钮
	protected FrameLayout m_logoLayer; //LOGO的黑色层
	protected ImageView m_logo;
	protected LinearLayout m_videoLogo;
	protected LinearLayout m_headLayer;
	protected ImageView m_headImg;
	private TextView m_userName;
	protected BottomBar m_bottomFr;
	protected LinearLayout m_cameraBtn;
	protected LinearLayout m_captureBtn;
	protected LinearLayout m_beautifyBtn;
	protected LinearLayout m_videoBtn;
	protected PageNumComponent m_pageNum;
	protected TextView m_test;
	protected ImageView m_businessIcon;
	protected String m_businessUrl = "http://www1.poco.cn/topic/qing_special/interphoto_rotate/201608/index.php";
	protected ArrayList<LeftItemData> left_datas;
	protected boolean m_isHideVirtualBar = true;
	protected AlertDialog m_checkDlg;
	private ReminderPage reminderPage;

	protected VideoViewFr m_videoFr;
	protected boolean m_showVideo = false;

	protected boolean mCameraBtnClick = false;
	protected boolean mCaptureBtnClick = false;

	public HomePage(Context context, BaseSite site) {
		super(context, site);

		m_site = (HomePageSite)site;
		InitData();
		InitUI();

		TongJiUtils.onPageStart(getContext(), TAG);
		MyBeautyStat.onPageStartByRes(R.string.首页);
	}

	/**
	 * @param params isLanguageBack 更换语言设置刷新页面时恢复页面原来状态用到。
	 */
	@Override
	public void SetData(HashMap<String, Object> params) {
		if (params != null) {
			if (params.get("isLanguageBack") != null && (boolean)params.get("isLanguageBack")) {
				m_btnLst.onClick(m_propertiesBtn);
				params.remove("isLanguageBack");
			}
		}

		//浏览器启动调用
		Intent intent = ((Activity)getContext()).getIntent();
		if (intent != null) {
			Uri uri = intent.getData();
			if (uri != null && "intercamera".equals(uri.getScheme()) && "goto".equals(uri.getHost())) {
				String str = uri.toString();
				MyFramework.startGoTo(getContext(),str);
				((Activity)getContext()).setIntent(null);
			}
		}

		if (m_isFirstTurning) {
			m_viewPager.StartTurning(VIEWPAGER_FIRST_TURNING_TIME);
			UserMgr.reFreshToken(getContext());
//					UserMgr.updateUserInfo(getContext());
			m_isFirstTurning = false;
			m_canChangeTurning = true;
		} else {
			m_viewPager.StartTurning(VIEWPAGER_TURNING_TIME);
		}

	}

	protected final int QUIT_DELAY = 3000;
	protected long quit_time = 0;
	protected Toast quit_toast;

	@Override
	public void onBack() {
		if (m_showVideo && m_videoFr != null) {
			m_videoFr.close();
			return;
		}
		if (m_drawer != null && m_drawerOpen) {
			m_drawer.closeDrawers();
			return;
		}
		long time = System.currentTimeMillis();
		if (time - quit_time > QUIT_DELAY) {
			//第一次点击提示
			if (quit_toast == null) {
				quit_toast = Toast.makeText(getContext(), getResources().getString(R.string.pressAgainToExit), Toast.LENGTH_SHORT);
				TextView view = new TextView(getContext());
				view.setText(getResources().getString(R.string.pressAgainToExit));
				view.setGravity(Gravity.CENTER);
				ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				view.setLayoutParams(vl);
				quit_toast.setDuration(Toast.LENGTH_SHORT);
				View layout = quit_toast.getView();
				if (layout instanceof RelativeLayout) {
					((RelativeLayout)layout).removeAllViews();
					((RelativeLayout)layout).addView(view);
				} else if (layout instanceof LinearLayout) {
					((LinearLayout)layout).removeAllViews();
					((LinearLayout)layout).addView(view);
				} else if (layout instanceof FrameLayout) {
					((FrameLayout)layout).removeAllViews();
					((FrameLayout)layout).addView(view);
				}
				quit_toast.show();
			}
			quit_toast.show();
		} else {
			if (quit_toast != null) {
				quit_toast.cancel();
				quit_toast = null;
			}
			m_site.OnBack(getContext());
		}
		quit_time = time;
	}

	@Override
	public void onResume() {
		if (m_videoFr == null && m_viewPager != null && !m_drawerOpen) {
			m_viewPager.StartTurning(VIEWPAGER_TURNING_TIME);
		}
		if (m_videoFr != null) {
			m_videoFr.onResume();
		}
		TongJiUtils.onPageResume(getContext(), TAG);
		super.onResume();
	}

	@Override
	public void onPause() {
		if (m_videoFr == null && m_viewPager != null) {
			m_viewPager.StopTurning();
		}
		if (m_videoFr != null) {
			m_videoFr.onPause();
		}
		TongJiUtils.onPagePause(getContext(), TAG);
		super.onPause();
	}

	@Override
	public void onClose() {
		m_uiEnabled = false;
		m_cmdEnabled = false;
		if (m_imageThread != null) {
			m_imageThread.quit();
			m_imageThread = null;
		}
		if (quit_toast != null) {
			quit_toast.cancel();
			quit_toast = null;
		}
		if (m_viewPager != null) {
			m_viewPager.StopTurning();
			m_viewPager.ClearAll();
		}
		if (m_checkDlg != null) {
			m_checkDlg.dismiss();
			m_checkDlg = null;
		}
		EventCenter.removeListener(m_onEventListener);
		DownloadMgr.getInstance().RemoveDownloadListener(m_downloadLst);
		super.onClose();
		TongJiUtils.onPageEnd(getContext(), TAG);
		MyBeautyStat.onPageEndByRes(R.string.首页);
	}

	private void layoutBottomBarFr()
	{
		FrameLayout.LayoutParams fl = (LayoutParams)m_bottomFr.getLayoutParams();
		fl.height = m_bottomFrH;
		m_bottomFr.setLayoutParams(fl);
		fl = (LayoutParams)m_pageNum.getLayoutParams();
		fl.bottomMargin = m_bottomFrH + PxToDpi_xhdpi(30);
		m_pageNum.setLayoutParams(fl);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		//适配虚拟键盘手机
		int height = HomePage.this.getHeight();
		if(m_firstLayout && height > 0)
		{
			m_firstLayout = false;
			m_bottomFrH = PxToDpi_xhdpi(200);
			if (height - m_imgFrH > m_bottomFrH) {
				m_bottomFrH = height - m_imgFrH;
			}
			layoutBottomBarFr();
		}

		//在4.2版本下，会影响viewpager的切换
		//小米平板上会看不到底下的美化拍照
		if (Build.VERSION.SDK_INT > JELLY_BEAN_MR2) {
			int virtualBarHeight = HomePage.this.getRootView().getHeight() - HomePage.this.getHeight();
			if (virtualBarHeight >= 50 && !m_isHideVirtualBar) {
				m_bottomFrH = ShareData.m_screenRealHeight - virtualBarHeight - m_imgFrH;
				m_isHideVirtualBar = true;
				if (m_bottomFrH < ShareData.PxToDpi_xhdpi(200)) {
					m_bottomFrH = ShareData.PxToDpi_xhdpi(200);
				}
				layoutBottomBarFr();
			} else if (virtualBarHeight < 50 && m_isHideVirtualBar) {
				m_bottomFrH = HomePage.this.getRootView().getHeight() - m_imgFrH;
				m_isHideVirtualBar = false;
				if (m_bottomFrH < PxToDpi_xhdpi(200)) {
					m_bottomFrH = ShareData.PxToDpi_xhdpi(200);
				}
				layoutBottomBarFr();
			}
		}
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params) {
		if (!m_drawerOpen) {
			if (m_viewPager != null) {
				m_viewPager.StartTurning(VIEWPAGER_TURNING_TIME);
			}
		}
		if(siteID == SiteID.LOGIN || siteID == SiteID.REGISTER_DETAIL || siteID == SiteID.RESETPSW)
		{
			if(params != null)
			{
				Object o = params.get("url");
				if(o != null)
				{
					String url = (String)o;
					if(!TextUtils.isEmpty(url) && UserMgr.IsLogin(getContext(),null))
					{
						m_site.OnImg(WebViewPage1.GetBeautyUrl(getContext(), url),getContext());
					}
				}
			}
		}

		super.onPageResult(siteID, params);
	}

	protected void InitData() {
		EventCenter.addListener(m_onEventListener);
		ShareData.InitData((Activity)getContext());

		m_uiEnabled = true;
		m_cmdEnabled = true;
		m_imgFrW = ShareData.m_screenWidth;
		int height = ShareData.m_screenRealHeight;
		m_imgFrH = (int)(height * 0.75 + 0.5f);
//		m_imgFrH = (int)(m_imgFrW * 4 / 3f + 0.5f);
		m_bottomFrW = ShareData.m_screenWidth;
		m_bottomFrH = PxToDpi_xhdpi(200);
		if (height - m_imgFrH > m_bottomFrH) {
			m_bottomFrH = height - m_imgFrH;
		}
		m_logoY = (int)(200 / 570f * m_imgFrH - PxToDpi_xhdpi(30));

		m_imgs = GetHomeImgArr();

		m_UIHandler = new UIHandler();
		m_imageThread = new HandlerThread("home_handler_thread");
		m_imageThread.start();
		m_mainHandler = new HomeHandler(m_imageThread.getLooper(), getContext(), m_UIHandler);

		DownloadMgr.getInstance().AddDownloadListener(m_downloadLst);

		left_datas = new ArrayList<>();

		LeftItemData data;
		boolean showMagezine = false;
		ArrayList<SwitchRes> resArr = SwitchResMgr2.getInstance().GetAllResArr();
		if(resArr != null)
		{
			for(SwitchRes res : resArr)
			{
				if("magazine".equals(res.mId) && res.mUnlock)
				{
					showMagezine = true;
					break;
				}
			}
		}
		if(showMagezine)
		{
			data = new LeftItemData();
			data.mListLogo = R.drawable.homepage_left_logo_interphoto;
			data.mListName = getResources().getString(R.string.homepage_amazing);
			data.mListId = LeftItemType.MAGAZINE;
			left_datas.add(data);
		}
		data = new LeftItemData();
		data.mListLogo = R.drawable.homepage_left_logo_material;
		data.mListName = getResources().getString(R.string.homepage_material);
		data.mListId = LeftItemType.MERTIRAL;
		left_datas.add(data);

		if (!ConfigIni.hideAppMarket)
		{
			data = new LeftItemData();
			data.mListLogo = R.drawable.homepage_left_logo_recommend;
			data.mListName = getResources().getString(R.string.homepage_apps);
			data.mListId = LeftItemType.RECOMMENT;
			left_datas.add(data);
		}

		data = new LeftItemData();
		data.mListLogo = R.drawable.homepage_left_logo_setting;
		data.mListName = getResources().getString(R.string.homepage_setting);
		data.mListId = LeftItemType.SETTING;
		left_datas.add(data);
	}

	protected DownloadMgr.DownloadListener m_downloadLst = new DownloadMgr.DownloadListener() {
		@Override
		public void OnDataChange(int resType, int downloadId, IDownload[] resArr) {
			/*if(resType == ResType.BUSINESS.GetValue())
			{
				m_imgs = GetHomeImgArr();
				if(m_viewPager != null)
				{
					m_viewPager.SetPageData(m_imgs);

					if(m_pageNum != null)
					{
						int index = m_currentBkIndex;
						if(index < 0)
						{
							index = 0;
						}
						else if(index >= m_imgs.size())
						{
							index = m_imgs.size() - 1;
						}
						m_pageNum.UpdatePageNum(index, m_imgs.size());
					}
				}
			}*/
		}
	};

	protected void InitUI() {
		FrameLayout.LayoutParams fl;

		m_drawer = new DrawerLayout(getContext());
		//m_drawer.setScrimColor(0);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		m_drawer.setLayoutParams(fl);
		m_drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				if (m_viewPager != null) {
					m_viewPager.StopTurning();
				}
				m_drawerOpen = true;
				MyBeautyStat.onClickByRes(R.string.首页_打开侧边栏);
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				if (m_viewPager != null) {
					m_viewPager.StartTurning(VIEWPAGER_TURNING_TIME);
				}
				MyBeautyStat.onClickByRes(R.string.侧边栏_退出侧边栏);
				m_drawerOpen = false;
			}

			@Override
			public void onDrawerStateChanged(int newState) {
				// TODO Auto-generated method stub
			}
		});
		this.addView(m_drawer);
		{
			DrawerLayout.LayoutParams dl;

			m_body = new FrameLayout(getContext());
			dl = new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.MATCH_PARENT, DrawerLayout.LayoutParams.MATCH_PARENT);
			m_body.setLayoutParams(dl);
			m_drawer.addView(m_body);
			{
				m_viewPager = new LoopViewPager<HomePage.HomeImgInfo>(getContext());
				m_viewPager.setOnPageChangeListener(new MyPagerLst());
				fl = new FrameLayout.LayoutParams(m_imgFrW, m_imgFrH);
				fl.gravity = Gravity.LEFT | Gravity.TOP;
				m_viewPager.setLayoutParams(fl);
				m_body.addView(m_viewPager);
				m_viewPager.setPageTransformer(true, new ZoomOutTransformer3());
				m_viewPager.SetPageData(m_imgs);
				m_viewPager.setOnPageClickListener(m_btnLst);


				m_logoLayer = new FrameLayout(getContext());
				m_logoLayer.setBackgroundColor(0x1A000000);
				fl = new FrameLayout.LayoutParams(m_imgFrW, m_imgFrH);
				fl.gravity = Gravity.LEFT | Gravity.TOP;
				m_logoLayer.setLayoutParams(fl);
				m_body.addView(m_logoLayer);
				{
					m_logo = new ImageView(getContext());
					m_logo.setImageResource(R.drawable.homepage_logo);
					fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
					fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
					fl.topMargin = m_logoY;
					m_logo.setLayoutParams(fl);
					m_logoLayer.addView(m_logo);

					m_videoLogo = new LinearLayout(getContext());
					m_videoLogo.setOrientation(LinearLayout.VERTICAL);
					m_videoLogo.setGravity(Gravity.CENTER);
					m_videoLogo.setVisibility(GONE);
					fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, m_imgFrH - m_logoY - ShareData.PxToDpi_xhdpi(60));
					fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
					fl.topMargin = m_logoY + ShareData.PxToDpi_xhdpi(60);
					m_videoLogo.setLayoutParams(fl);
					m_logoLayer.addView(m_videoLogo);
					{
						ImageView logo = new ImageView(getContext());
						logo.setImageResource(R.drawable.homepage_video_logo);
						LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
						logo.setLayoutParams(ll);
						m_videoLogo.addView(logo);

						TextView text = new TextView(getContext());
						text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
						text.setTextColor(Color.WHITE);
						text.setText(R.string.homepage_play);
						ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
						ll.topMargin = ShareData.PxToDpi_xhdpi(20);
						text.setLayoutParams(ll);
						m_videoLogo.addView(text);
					}
				}

				m_propertiesBtn = new ImageView(getContext());
				m_propertiesBtn.setImageResource(R.drawable.homepage_properties_btn_out);
				fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.LEFT | Gravity.TOP;
				fl.leftMargin = PxToDpi_xhdpi(30);
				fl.topMargin = PxToDpi_xhdpi(30);
				m_propertiesBtn.setLayoutParams(fl);
				m_body.addView(m_propertiesBtn);
				m_propertiesBtn.setOnClickListener(m_btnLst);

				m_bottomFr = new BottomBar(getContext());
				fl = new FrameLayout.LayoutParams(m_bottomFrW, m_bottomFrH);
				fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
				m_bottomFr.setLayoutParams(fl);
				m_body.addView(m_bottomFr);
				{
					int w = PxToDpi_xhdpi(100);
					int num = 4;
					float d = (float)(m_bottomFrW - w * num) / (float)(num * 2);

					LinearLayout container = new LinearLayout(getContext());
					container.setOrientation(LinearLayout.VERTICAL);
					container.setGravity(Gravity.CENTER);
					fl = new FrameLayout.LayoutParams(m_bottomFrW / 2, LayoutParams.MATCH_PARENT);
					fl.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
					container.setLayoutParams(fl);
					m_bottomFr.addView(container);
					{
						TextView title = new TextView(getContext());
						title.setTextColor(Color.WHITE);
						title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
						title.setText(R.string.homepage_photoes);
						TextPaint tp = title.getPaint();
						tp.setFakeBoldText(true);
						LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
						ll.gravity = Gravity.CENTER_HORIZONTAL;
						ll.bottomMargin = ShareData.PxToDpi_xhdpi(25);
						title.setLayoutParams(ll);
						container.addView(title);

						LinearLayout btnFr = new LinearLayout(getContext());
						btnFr.setGravity(Gravity.CENTER);
						ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
						btnFr.setLayoutParams(ll);
						container.addView(btnFr);
						{
							m_cameraBtn = MakeItem(R.drawable.homepage_camera_btn_out, getResources().getString(R.string.homepage_camera));
							ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
							ll.gravity = Gravity.CENTER_VERTICAL;
							ll.rightMargin = ShareData.PxToDpi_xhdpi(60);
							m_cameraBtn.setLayoutParams(ll);
							btnFr.addView(m_cameraBtn);
							m_cameraBtn.setOnClickListener(m_btnLst);

							m_beautifyBtn = MakeItem(R.drawable.homepage_photoes_btn, getResources().getString(R.string.homepage_retouch));
							ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
							ll.gravity = Gravity.CENTER_VERTICAL;
							m_beautifyBtn.setLayoutParams(ll);
							btnFr.addView(m_beautifyBtn);
							m_beautifyBtn.setOnClickListener(m_btnLst);
						}
					}

					container = new LinearLayout(getContext());
					container.setOrientation(LinearLayout.VERTICAL);
					container.setGravity(Gravity.CENTER);
					fl = new FrameLayout.LayoutParams(m_bottomFrW / 2 - ShareData.PxToDpi_xhdpi(1), LayoutParams.MATCH_PARENT);
					fl.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
					fl.leftMargin = m_bottomFrW / 2 + ShareData.PxToDpi_xhdpi(1);
					container.setLayoutParams(fl);
					m_bottomFr.addView(container);
					{
						TextView title = new TextView(getContext());
						title.setTextColor(Color.WHITE);
						title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
						title.setText(R.string.homepage_video);
						TextPaint tp = title.getPaint();
						tp.setFakeBoldText(true);
						LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
						ll.gravity = Gravity.CENTER_HORIZONTAL;
						ll.bottomMargin = ShareData.PxToDpi_xhdpi(25);
						title.setLayoutParams(ll);
						container.addView(title);

						FrameLayout frCon = new FrameLayout(getContext());
						ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
						frCon.setLayoutParams(ll);
						container.addView(frCon);
						{
							View line = new View(getContext());
							line.setBackgroundColor(Color.WHITE);
							fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(1), ShareData.PxToDpi_xxhdpi(86));
							fl.gravity = Gravity.LEFT | Gravity.TOP;
							fl.topMargin = ShareData.PxToDpi_xxhdpi(32);
							line.setLayoutParams(fl);
							frCon.addView(line);

							LinearLayout btnFr = new LinearLayout(getContext());
							btnFr.setGravity(Gravity.CENTER);
							fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
							btnFr.setLayoutParams(fl);
							frCon.addView(btnFr);
							{
								m_captureBtn = MakeItem(R.drawable.homepage_capture_btn, getResources().getString(R.string.homepage_capture));
								ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
								ll.gravity = Gravity.CENTER_VERTICAL;
								ll.rightMargin = ShareData.PxToDpi_xhdpi(60);
								m_captureBtn.setLayoutParams(ll);
								btnFr.addView(m_captureBtn);
								m_captureBtn.setOnClickListener(m_btnLst);

								m_videoBtn = MakeItem(R.drawable.homepage_video_btn, getResources().getString(R.string.homepage_edit));
								ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
								ll.gravity = Gravity.CENTER_VERTICAL;
								m_videoBtn.setLayoutParams(ll);
								btnFr.addView(m_videoBtn);
								m_videoBtn.setOnClickListener(m_btnLst);
							}
						}
					}
				}

				m_pageNum = new PageNumComponent(getContext());
				m_pageNum.page_num_out = R.drawable.homepage_dot_out;
				m_pageNum.page_num_over = R.drawable.homepage_dot_over;
				fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
				fl.bottomMargin = m_bottomFrH + PxToDpi_xhdpi(30);
				m_body.addView(m_pageNum, fl);
				m_pageNum.UpdatePageNum(0, m_imgs.size());
				Log.i(TAG, "InitUI: " +  Tags.HOME_CAMERA_TIP + SysConfig.GetAppVerNoSuffix(getContext()));
				if(TagMgr.CheckTag(getContext(), Tags.HOME_CAMERA_TIP + SysConfig.GetAppVerNoSuffix(getContext())))
				{
					fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					fl.gravity = Gravity.CENTER;
					reminderPage =  new ReminderPage(getContext());
					reminderPage.setOnClickListener(new MyBtnLst());
					m_body.addView(reminderPage,fl);
				}
			}

			m_left = new FrameLayout(getContext());
			m_left.setBackgroundColor(0xFF0E0E0E);
			m_left.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
				}
			});
			dl = new DrawerLayout.LayoutParams(PxToDpi_xhdpi(350), DrawerLayout.LayoutParams.MATCH_PARENT);
			dl.gravity = Gravity.LEFT | Gravity.TOP;
			m_left.setLayoutParams(dl);
			m_drawer.addView(m_left);
			{
				m_headLayer = new LinearLayout(getContext());
				m_headLayer.setOrientation(LinearLayout.VERTICAL);
				m_headLayer.setGravity(Gravity.CENTER);
				m_headLayer.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						MyBeautyStat.onClickByRes(R.string.侧边栏_打开登录注册);
						if (UserMgr.IsLogin(getContext(),null)) {
							UserInfo info = UserMgr.ReadCache(getContext());
							if (info != null) {
								m_site.OnUserInfo(info.mUserId, InitGlassBk(0xcc000000),getContext());
							}
						} else {
							m_site.OnLogin(InitGlassBk(0xbf000000),getContext());
						}
					}
				});
				fl = new FrameLayout.LayoutParams(PxToDpi_xhdpi(350), PxToDpi_xhdpi(350));
				fl.gravity = Gravity.LEFT | Gravity.TOP;
				m_left.addView(m_headLayer, fl);
				{
					LinearLayout.LayoutParams ll;
					m_headImg = new ImageView(getContext());
					m_headImg.setScaleType(ScaleType.FIT_XY);
					ll = new LinearLayout.LayoutParams(PxToDpi_xhdpi(180), PxToDpi_xhdpi(180));
					m_headLayer.addView(m_headImg, ll);

					m_userName = new TextView(getContext());
					m_userName.setSingleLine();
					m_userName.setEllipsize(TextUtils.TruncateAt.END);
					if(LoginOtherUtil.isChineseLanguage(getContext())){
						m_userName.setMaxEms(10);
					}
					else{
						m_userName.setMaxEms(20);
					}
					m_userName.setText(R.string.log_in_sign_up);
					m_userName.setTextColor(Color.WHITE);
					m_userName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
					ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					ll.topMargin = PxToDpi_xhdpi(30);
					m_headLayer.addView(m_userName, ll);
					updateAvatar();

				}
				ScrollView list = new ScrollView(getContext());

//				ListView list = new ListView(getContext());
				//list.setBackgroundColor(0xFFFF0000);
//				list.setDivider(new ColorDrawable(0xFF333333));
//				list.setDividerHeight(2);
				int h = ShareData.m_screenHeight - PxToDpi_xhdpi(350);
				final int itemH = (h - (left_datas.size() - 1) * 1) / left_datas.size();
				fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, h);
				fl.gravity = Gravity.LEFT | Gravity.BOTTOM;
				list.setLayoutParams(fl);
				m_left.addView(list);
				{
					LinearLayout lin = new LinearLayout(getContext());
					lin.setOrientation(LinearLayout.VERTICAL);
					fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, h);
					fl.gravity = Gravity.LEFT | Gravity.BOTTOM;
					lin.setLayoutParams(fl);
					list.addView(lin);
					{
						for(final LeftItemData data : left_datas)
						{
							LeftItem item = new LeftItem(getContext());
							item.SetData(0, data.mListLogo, data.mListName, itemH);
							item.setOnClickListener(new OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									leftItemClick(data.mListId);
								}
							});
							LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemH);
							item.setLayoutParams(ll);
							lin.addView(item);
						}
					}
				}
			}

			//调试模式显示文字
			try {
				if (SysConfig.GetAppVer(getContext()).contains("88.8.8")) {
					m_test = new TextView(getContext());
					m_test.setTextColor(0xFFFF0000);
					m_test.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
					m_test.setText("调试模式");
					fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					fl.gravity = Gravity.LEFT | Gravity.TOP;
					this.addView(m_test, fl);
					m_test.setOnClickListener(m_btnLst);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		try {
			String ver = SysConfig.GetAppVer(getContext());
			if (ver != null && ver.endsWith("_r20")) {
				long time = System.currentTimeMillis();
				String date1Str = "2016-08-26 00:00";
				String date2Str = "2016-09-02 00:00";
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
				Date date1 = df.parse(date1Str);
				Date date2 = df.parse(date2Str);
				if (time >= date1.getTime() && time <= date2.getTime()) {
					m_businessIcon = new ImageView(getContext());
					m_businessIcon.setImageResource(R.drawable.ad_xiaomi_enter_icon);
					fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					fl.gravity = Gravity.RIGHT | Gravity.TOP;
					m_businessIcon.setLayoutParams(fl);
					this.addView(m_businessIcon);
					m_businessIcon.setOnClickListener(m_btnLst);
				}
			}
		} catch (Exception e) {

		}

		//google市场不显示推荐更新的popup
		String ver = SysConfig.GetAppVer(getContext());
		if (!m_hasCheckUpdate && !ver.endsWith("_r1")) {
			m_hasCheckUpdate = true;
			CheckUpdate update = new CheckUpdate(getContext(), HomePage.this);
			update.CheckForUpdate();
		}

		if (!m_hasCheckMem) {
			m_hasCheckMem = true;
			if (FileUtil.getSDFreeMemory() < 100 * 1024 * 1024) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setTitle(getResources().getString(R.string.tip));
				builder.setMessage(getResources().getString(R.string.check_sd_capacity));
				builder.setPositiveButton(getResources().getString(R.string.Isee), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (m_checkDlg != null) {
							m_checkDlg.dismiss();
						}
					}
				});
				m_checkDlg = builder.create();
				m_checkDlg.show();
			}
		}
	}


	protected class UIHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (m_cmdEnabled) {
				switch (msg.what) {
					case HomeHandler.MSG_UPDATE_UI:
						HomeHandler.QueueItem item = (HomeHandler.QueueItem)msg.obj;
						if (item != null && item.m_bmp != null) {
							if (m_currentBkIndex == item.m_index) {
								if (m_bottomFr != null) {
									m_bottomFr.ChangeBk(item.m_bmp, true, true);
								}
							} else {
								item.m_bmp.recycle();
								item.m_bmp = null;
							}
						}
						break;

					default:
						break;
				}
			}
		}
	}

	private void leftItemClick(LeftItemType type)
	{
		if(type != null)
		{
			switch(type)
			{
				case MAGAZINE:
					MyBeautyStat.onClickByRes(R.string.侧边栏_打开印象杂志);
					m_site.OnInterphoto(MyNetCore.GetPocoUrl(getContext(), "http://img-m-ip.poco.cn/mypoco/mtmpfile/API/interphoto/weixin_wap/index.php"),getContext());
					break;
				case MERTIRAL:
					MyBeautyStat.onClickByRes(R.string.侧边栏_打开素材商店);
					m_site.OnMaterial(getContext());
					break;
				case RECOMMENT:
					MyBeautyStat.onClickByRes(R.string.侧边栏_打开应用推荐);
					m_site.OnRecommendApp(getContext());
					break;
				case SETTING:
					MyBeautyStat.onClickByRes(R.string.侧边栏_打开设置);
					m_site.OnSetting(getContext());
					break;
			}
		}
	}

	@Override
	public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if(Build.VERSION.SDK_INT >= 23){
			if(permissions != null && grantResults != null && permissions.length == grantResults.length)
			{
				String str;
				boolean flag1 = false;
				boolean flag2 = false;
				for(int i = 0; i < permissions.length; i ++)
				{
					str = permissions[i];
					if(mCameraBtnClick)
					{
						if(Manifest.permission.CAMERA.equals(str))
						{
							if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
							{
								m_btnLst.onClick(m_cameraBtn);
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
					if(mCaptureBtnClick)
					{
						if(Manifest.permission.CAMERA.equals(str))
						{
							if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
							{
								flag1 = true;
							}
							else
							{
								flag1 = false;
								if(!ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(), str))
								{
									Toast.makeText(getContext(), "请到应用程序管理里面打开相机权限", Toast.LENGTH_SHORT).show();
								}
							}
						}

						if(Manifest.permission.RECORD_AUDIO.equals(str))
						{
							if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
							{
								flag2 = true;
							}
							else
							{
								flag2 = false;
								if(!ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(), str))
								{
									Toast.makeText(getContext(), "请到应用程序管理里面打开录音权限", Toast.LENGTH_SHORT).show();
								}
							}
						}
					}
				}

				if(mCaptureBtnClick && flag1 && flag2)
				{
					m_btnLst.onClick(m_captureBtn);
				}
			}
		}
		return super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	protected LinearLayout MakeItem(int icon, String name) {
		LinearLayout out = null;

		out = new LinearLayout(getContext());
		out.setOrientation(LinearLayout.VERTICAL);
		{
			LinearLayout.LayoutParams ll;

			ImageView img = new ImageView(getContext());
			img.setImageResource(icon);
			img.setScaleType(ScaleType.CENTER);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER;
			img.setLayoutParams(ll);
			out.addView(img);

			TextView tex = new TextView(getContext());
			tex.setTextColor(0xFFFFFFFF);
			tex.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			tex.setGravity(Gravity.CENTER);
			tex.setSingleLine();
			tex.setText(name);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER;
			tex.setLayoutParams(ll);
			out.addView(tex);
		}

		return out;
	}

	/**
	 * 生成和底部BAR同一比例的bitmap
	 *
	 * @param bmp
	 * @return
	 */
	public Bitmap MakeButtonBk(Bitmap bmp) {
		Bitmap out = null;
		if (bmp != null && !bmp.isRecycled() && bmp.getWidth() > 0 && bmp.getHeight() > 0) {
			float scale = (float)m_bottomFrW / (float)m_bottomFrH;
			int h = (int)(bmp.getWidth() / scale + 0.5f);
			h = h == 0 ? bmp.getHeight() : h;
			out = MakeBmp.CreateFixBitmap(bmp, bmp.getWidth(), h, MakeBmp.POS_END, 0, Config.ARGB_8888);
		}
		return out;
	}

	protected class MyPagerLst implements ViewPager.OnPageChangeListener {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			int len;
			if (m_imgs != null && (len = m_imgs.size()) > 0) {
				int pos2 = (position + 1) % len;
				if (positionOffset < 0.25f) {
					HomeImgInfo info = m_imgs.get(position);
					if (info.m_isShowLogo) {
						m_logo.setAlpha(1f);
					} else {
						m_logo.setAlpha(0f);
					}

				} else if (positionOffset > 0.75f) {
					HomeImgInfo info = m_imgs.get(pos2);
					if (info.m_isShowLogo) {
						m_logo.setAlpha(1f);
					} else {
						m_logo.setAlpha(0f);
					}
				} else {
					HomeImgInfo info = m_imgs.get(position);
					HomeImgInfo info2 = m_imgs.get(pos2);
					if (info.m_isShowLogo != info2.m_isShowLogo) {
						float s = (positionOffset - 0.25f) / 0.5f;
						if (info.m_isShowLogo) {
							s = 1 - s;
						}
						m_logo.setAlpha(s);
					} else {
						if (info.m_isShowLogo) {
							m_logo.setAlpha(1f);
						} else {
							m_logo.setAlpha(0f);
						}
					}
				}
			} else {
				if (m_logo != null) {
					m_logo.setAlpha(1f);
				}
			}

			//System.out.println(position + "," + positionOffset);
		}

		@Override
		public void onPageSelected(int position) {
			if (m_canChangeTurning) {
				m_canChangeTurning = false;
				if (m_viewPager != null) {
					m_viewPager.ClearRunnable();
					m_viewPager.InitRunnable();
					m_viewPager.StartTurning(VIEWPAGER_TURNING_TIME);
				}
			}
			if (m_pageNum != null) {
				//System.out.println("UpdatePageNum : " + position + "," + m_imgs.size());
				m_pageNum.UpdatePageNum(position, m_imgs.size());
			}

			if (position != m_currentBkIndex) {
				m_currentBkIndex = position;
				if (m_viewPager != null) {
					View view = m_viewPager.GetPrimaryView();
					if (view instanceof HomeImgItem) {
						Bitmap bmp = MakeButtonBk(((HomeImgItem)view).m_bmp);
						if (bmp != null) {
							HomeHandler.QueueItem item = new HomeHandler.QueueItem();
							item.m_bmp = bmp;
							item.m_index = m_currentBkIndex;
							m_mainHandler.AddItem(item);

							Message msg = m_mainHandler.obtainMessage();
							msg.what = HomeHandler.MSG_CYC_QUEUE;
							m_mainHandler.sendMessage(msg);
						}
					}
				}
			}
			if (m_imgs != null && position >= 0 && position < m_imgs.size() && m_videoLogo != null) {
				Object obj = m_imgs.get(position).m_data;
				/*if (m_viewPager != null) {
					m_viewPager.SetTurningTime(m_imgs.get(position).m_showTime);
				}*/
				String url = null;
				if (obj instanceof String) {
					url = (String)obj;
				} else if (obj instanceof ClickAdRes) {
					url = ((ClickAdRes)obj).mClick;
				}
				if (!TextUtils.isEmpty(url) && url.startsWith("interphoto://openvideo")) {
					m_videoLogo.setVisibility(VISIBLE);
				} else {
					m_videoLogo.setVisibility(GONE);
				}
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
		}
	}

	protected class MyBtnLst implements View.OnClickListener, LoopViewPager.OnPageClickListener {
		@Override
		public void onClick(View v) {
			if (m_uiEnabled) {
				TagMgr.SetTag(getContext(), Tags.HOME_CAMERA_TIP + SysConfig.GetAppVerNoSuffix(getContext()));
				if(reminderPage != null)
				{
					//reminderPage.setVisibility(View.GONE);
				}
				onClick(v, true);
			}
		}

		public void onClick(View v, boolean fromUser) {
			if (s_glassPath == null) {
				s_glassPath = InitGlassBk(0xbf000000, true);
			}
			if (v == m_cameraBtn) {
				mCameraBtnClick = true;
				mCaptureBtnClick = false;
				boolean flag = true;
				if(Build.VERSION.SDK_INT >= 23)
				{
					flag = PermissionUtils.checkCameraPermission(getContext());
				}
				if(flag)
				{
					MyBeautyStat.onClickByRes(R.string.首页_打开相机);
					TagMgr.SetTag(getContext(), Tags.HOME_CAMERA_TIP + SysConfig.GetAppVerNoSuffix(getContext()));
					TongJi2.AddCountByRes(getContext(), R.integer.拍照);
					m_site.OnCamera(getContext());
				}
				else
				{
					PermissionUtils.requestPermissions(getContext(), Manifest.permission.CAMERA);
				}
			} else if (v == m_captureBtn) {
				mCaptureBtnClick = true;
				mCameraBtnClick = false;
				boolean flag = true;
				boolean flag1 = true;
				if(Build.VERSION.SDK_INT >= 23)
				{
					flag = PermissionUtils.checkCameraPermission(getContext());
					flag1 = PermissionUtils.checkAudioPermission(getContext());
				}
				if(flag && flag1)
				{
					MyBeautyStat.onClickByRes(R.string.首页_打开录像);
					m_site.OnCapture(getContext());
				}
				else
				{
					PermissionUtils.requestPermissions(getContext(), new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
				}
			} else if (v == m_beautifyBtn) {
				if (FileUtil.isFileExists(PhotoDatabaseHelper.DB_PATH) || FileUtil.getSDFreeMemory() / 1024 > 100) {
					TongJi2.AddCountByRes(getContext(), R.integer.美化);
					MyBeautyStat.onClickByRes(R.string.首页_打开照片);
					m_site.OnBeautify(getContext());
				} else {
					Toast.makeText(getContext(), "存储空间不足，请及时清理", Toast.LENGTH_SHORT).show();
				}
			} else if (v == m_videoBtn) {
				MyBeautyStat.onClickByRes(R.string.首页_打开视频);
				TongJi2.AddCountByRes(getContext(), R.integer.视频);
				m_site.onVideo(getContext());
			} else if (v == m_propertiesBtn) {
				TongJi2.AddCountByRes(getContext(), R.integer.首页_侧栏);
//				MyBeautyStat.onClickByRes(R.string.首页_打开侧边栏);
				m_drawer.openDrawer(Gravity.LEFT);
			} else if (v == m_businessIcon) {
				m_site.OnInterphoto(m_businessUrl,getContext());
			}
			else if(v == m_test)
			{
				ResourceMgr.clearDatabaseData(getContext());
			}
		}

		@Override
		public void onPageClick(int position) {
			if (m_imgs != null && position >= 0 && position < m_imgs.size()) {
				TongJi2.AddCountByRes(getContext(), R.integer.首页_轮播图);
				MyBeautyStat.onClickByRes(R.string.首页_打开轮播图);
				Object obj = m_imgs.get(position).m_data;
				String url = null;
				if (obj instanceof String) {
					url = (String)obj;
				} else if (obj instanceof ClickAdRes) {
					url = ((ClickAdRes)obj).mClick;
					if (url != null && url.length() > 0) {
						Utils.SendTj(getContext(), ((ClickAdRes)obj).mClickTjs);
					}
				}
				if (url != null && url.length() > 0) {
					if (url.startsWith("interphoto://openvideo")) {
						int len = "interphoto://openvideo".length();
						url = url.substring(len + 1);
						openVideoView(url);
					} else {
						url += "&share_logo=" + ((ClickAdRes)obj).m_thumb;
						m_viewPager.StopTurning();
						m_site.OnImg(url,getContext());
					}
				}
			}
		}
	}

//	private HashMap<String, String> mAdTj = new HashMap<>();

	public class HomeImgInfo implements LoopViewPager.ItemCreator {
		public Object m_img; //Integer/String
		public Object m_data; //String/BusinessRes
		public boolean m_isShowLogo;
		public int m_showTime;

		@Override
		public View CreateView(Context context, View view, int position) {
			if (view == null) {
				view = new HomeImgItem(context);
			}
			Bitmap bmp = null;
			Bitmap temp = Utils.DecodeImage(context, m_img, 0, (float)m_imgFrW / (float)m_imgFrH, m_imgFrW, m_imgFrH);
			if (temp != null) {
				if (Math.abs(temp.getWidth() - m_imgFrW) > 200) {
					bmp = MakeBmp.CreateBitmap(temp, m_imgFrW, m_imgFrH, -1, 0, Config.ARGB_8888);
					temp.recycle();
					temp = null;
				} else {
					bmp = temp;
				}
			}
			if (m_data instanceof ClickAdRes) {
				Utils.SendTj(getContext(), ((ClickAdRes)m_data).mShowTjs);
			}

			//第一次马上显示
			if (m_isFirst) {
				m_isFirst = false;
				m_currentBkIndex = position;

				if (m_bottomFr != null) {
					Bitmap bk = MakeButtonBk(bmp);
					HomeHandler.MakeGlassBk(bk);
					m_bottomFr.ChangeBk(bk, true, false);
				}
			}
			((HomeImgItem)view).SetBmp(m_img, bmp, position);

			return view;
		}

		@Override
		public int GetViewPosition(View view) {
			int out = -1;

			if (view instanceof HomeImgItem) {
				out = ((HomeImgItem)view).m_position;
			}

			return out;
		}

		@Override
		public void DestroyView(View view, int position) {
			if (view instanceof HomeImgItem) {
				Bitmap bmp = ((HomeImgItem)view).m_bmp;
				((HomeImgItem)view).m_bmp = null;
				((HomeImgItem)view).m_imgView.ClearAll();
				((HomeImgItem)view).m_imgView.setImageBitmap(null);
				if (bmp != null) {
					bmp.recycle();
					bmp = null;
				}
			}
		}
	}

	public static class HomeImgItem extends FrameLayout {
		protected Bitmap m_bmp;
		protected int m_position;
		protected GifImageView m_imgView;

		public HomeImgItem(Context context) {
			super(context);
			m_imgView = new GifImageView(context);
			LayoutParams fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			m_imgView.setLayoutParams(fl);
			addView(m_imgView);

			m_imgView.setScaleType(ScaleType.CENTER_CROP);
		}

		public void SetBmp(Bitmap bmp, int position) {
			m_bmp = bmp;
			m_position = position;

			m_imgView.setImageBitmap(bmp);
		}

		public void SetBmp(Object res, Bitmap bmp, int position) {
			m_bmp = bmp;
			m_position = position;
			if (res instanceof String && ((String)res).length() > 0) {
				String mine_type = FileUtil.getMimeType((String)res);
				if ("image/gif".equals(mine_type)) {
					m_imgView.setAutoPlay(true);
					m_imgView.SetImageRes(res);
				} else {
					m_imgView.setImageBitmap(bmp);
				}
			} else {
				m_imgView.setImageBitmap(bmp);
			}

		}
	}

	public ArrayList<HomeImgInfo> GetHomeImgArr() {
		ArrayList<HomeImgInfo> out = new ArrayList<HomeImgInfo>();

		HomeImgInfo info;
		//内置
		info = new HomeImgInfo();
		info.m_img = R.drawable.homepage_img1;
		info.m_isShowLogo = true;
		info.m_showTime = 5000;
		out.add(info);

		info = new HomeImgInfo();
		info.m_img = R.drawable.homepage_img2;
		info.m_isShowLogo = true;
		info.m_showTime = 10000;
		out.add(info);

		info = new HomeImgInfo();
		info.m_img = R.drawable.homepage_img3;
		info.m_isShowLogo = true;
		info.m_showTime = 10000;
		out.add(info);

		info = new HomeImgInfo();
		info.m_img = R.drawable.homepage_img4;
		info.m_isShowLogo = true;
		info.m_showTime = 5000;
		out.add(info);

		//网络配置
		ArrayList<ClickAdRes> resArr = AdMaster.getInstance(getContext()).getClickResArr();
		if (resArr != null) {
			int len = resArr.size();
			ClickAdRes temp;
			for (int i = 0; i < len; i++) {
				temp = resArr.get(i);
				if (temp.mShowOk) {
					InsertHomeImg(out, temp);
				}
			}
		}

		return out;
	}

	protected void openVideoView(String url) {
		if (!m_showVideo) {
			if (m_videoFr != null) {
				this.removeView(m_videoFr);
				m_videoFr.releaseMem();
				m_videoFr = null;
			}
			m_videoFr = new VideoViewFr(getContext());
			FrameLayout.LayoutParams fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			m_videoFr.setLayoutParams(fl);
			this.addView(m_videoFr);
			m_videoFr.SetPageCB(new VideoViewFr.VideoViewCB() {
				@Override
				public void onClose() {
					m_showVideo = false;
					if (m_videoFr != null) {
						HomePage.this.removeView(m_videoFr);
						m_videoFr.releaseMem();
						m_videoFr = null;
					}
					if (m_viewPager != null) {
						m_viewPager.StartTurning(VIEWPAGER_TURNING_TIME);
					}
				}
			});
			m_showVideo = true;
			if (m_viewPager != null) {
				m_viewPager.StopTurning();
			}
			m_videoFr.show(url);
		}
	}

	protected void InsertHomeImg(ArrayList<HomeImgInfo> arr, ClickAdRes res) {
		if (arr != null && res != null && res.mAdm != null && res.mAdm.length > 0) {
			HomeImgInfo info = new HomeImgInfo();
			info.m_img = res.mAdm[0];
			info.m_isShowLogo = res.m_isShowLogo;
			info.m_data = res;
			info.m_showTime = res.m_showTime;

			int len = arr.size();
			if (res.m_insertIndex < 0) {
				arr.add(0, info);
			} else if (res.m_insertIndex >= len) {
				arr.add(info);
			} else {
				arr.remove(res.m_insertIndex);
				arr.add(res.m_insertIndex, info);
			}
		}
	}

	protected static class LeftItemData{
		public int mListLogo;
		public String mListName;
		public LeftItemType mListId = LeftItemType.NONE;
	}

	protected static class LeftItem extends FrameLayout {
		public int m_pos;
		public ImageView m_logo;
		public TextView m_title;

		public LeftItem(Context context) {
			super(context);

			Init();
		}

		public LeftItem(Context context, AttributeSet attrs) {
			super(context, attrs);

			Init();
		}

		public LeftItem(Context context, AttributeSet attrs, int defStyleAttr) {
			super(context, attrs, defStyleAttr);

			Init();
		}

		public void Init() {
			this.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));

			FrameLayout.LayoutParams fl;
			LinearLayout.LayoutParams ll;

			LinearLayout fr = new LinearLayout(getContext());
			fr.setOrientation(LinearLayout.VERTICAL);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER;
			this.addView(fr, fl);
			{
				m_logo = new ImageView(getContext());
				m_logo.setScaleType(ScaleType.CENTER);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER;
				ll.bottomMargin = PxToDpi_xhdpi(20);
				fr.addView(m_logo, ll);

				m_title = new TextView(getContext());
				m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				m_title.setTextColor(0xFF999999);
				m_title.setSingleLine();
				m_title.setGravity(Gravity.CENTER);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER;
				m_title.setLayoutParams(ll);
				fr.addView(m_title);
			}
		}

		public void SetData(int pos, int logo, String name, int h) {
			m_pos = pos;
			m_logo.setImageResource(logo);
			m_title.setText(name);
			ViewGroup.LayoutParams lp = this.getLayoutParams();
			if (lp != null) {
				lp.height = h;
			}
		}
	}

	private EventCenter.OnEventListener m_onEventListener = new EventCenter.OnEventListener() {
		@Override
		public void onEvent(int eventId, Object[] params) {
			if (eventId == EventID.UPDATE_USER_INFO) {
				updateAvatar();
			}
		}
	};

	protected String InitGlassBk(int fillColor) {
		return InitGlassBk(fillColor, false);
	}

	public static String s_glassPath;

	protected String InitGlassBk(int fillColor, boolean isAppPath) {
		String imgPath = null;
		Bitmap bitmap = Bitmap.createBitmap(ShareData.m_screenWidth, ShareData.m_screenHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		m_body.draw(canvas);
		if (bitmap != null) {
			bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
			bitmap = BeautifyResMgr.MakeBkBmp(bitmap, bitmap.getWidth(), bitmap.getHeight(), fillColor, 0x90FFFFFF);
			String path;
			if (isAppPath) {
				path = FileCacheMgr.GetAppPath();
			} else {
				path = FileCacheMgr.GetLinePath();
			}
			if (Utils.SaveTempImg(bitmap, path)) {
				imgPath = path;
			}
		}
		return imgPath;
	}

	private void updateAvatar() {
		if (UserMgr.IsLogin(getContext(),null)) {
			final UserInfo info = UserMgr.ReadCache(getContext());
			if (info != null && m_userName != null && m_headImg != null) {
				m_userName.setText(info.mNickname);
				Bitmap head = BitmapFactory.decodeFile(UserMgr.HEAD_PATH);
				if (head != null) {
					head = ImageUtil.makeCircleBmp(head, 0, 0);
					m_headImg.setImageBitmap(head);
				} else {
					m_headImg.setImageResource(R.drawable.login_head_logo);
					new Thread(new Runnable() {
						@Override
						public void run() {
							String path = UserMgr.DownloadHeadImg(getContext(), info.mUserIcon);
							if (path != null) {
								EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
							}
						}
					}).start();
				}
			} else {
				setUnLoginState();
			}
		} else {
			setUnLoginState();
		}
	}

	private void setUnLoginState() {
		if (m_headImg != null && m_userName != null) {
			Bitmap headBp = BitmapFactory.decodeResource(getResources(), R.drawable.login_default_head);
			headBp = ImageUtil.makeCircleBmp(headBp, 0, 0);
			m_headImg.setImageBitmap(headBp);
			m_userName.setText(R.string.log_in_sign_up);
		}
	}

}
