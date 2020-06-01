package cn.poco.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.album2.PhotoStore;
import cn.poco.album2.model.PhotoInfo;
import cn.poco.album2.utils.PhotoUtils;
import cn.poco.camera.site.CameraPageSite;
import cn.poco.camera.utils.CameraUtils;
import cn.poco.camera.utils.DrawableUtils;
import cn.poco.camera.view.AnimShutterView;
import cn.poco.camera.view.BottomControlView;
import cn.poco.camera.view.CameraLayout;
import cn.poco.camera.view.FilterLayout;
import cn.poco.camera.view.GLCameraView;
import cn.poco.camera.view.GuidanceView;
import cn.poco.camera.view.MoreControl;
import cn.poco.camera.view.MoreControlPanel;
import cn.poco.camera.view.TopControlView;
import cn.poco.capture2.AnimatorUtils;
import cn.poco.capture2.view.FilterText;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
import cn.poco.image.filter;
import cn.poco.interphoto2.R;
import cn.poco.resource.FilterRes;
import cn.poco.resource.ResType;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.ShareData;
import cn.poco.video.render.filter.FilterItem;

/**
 * Created by: fwc
 * Date: 2017/10/9
 */
public class CameraPage extends IPage implements CameraAllCallback, View.OnTouchListener, TopControlView.OnTopControlListener, MoreControlPanel.OnMoreControlListener, BottomControlView.OnBottomControlListener, FilterLayout.OnFilterControlListener, GetFilterTask.OnGetFilterListener {

	private static final String TAG = "相机";

	public static final int ROTATION_ANIM_TIME = 500;

	private static final String CAMERA_PERMISSION_HELPER_URL = "http://www.adnonstop.com/interphoto/android/index.php";

	private static final int MSG_TIMER_COUNT_DOWN = 1;
	private static final int MSG_CANCEL_TAKE_PICTURE = 2;
	private static final int MSG_HANDLE_FOCUS_AND_METERING = 3;
	//	private static final int MSG_HIDE_ZOOM_SEEK_BAR = 4;
	private static final int MSG_ON_VIEW_RESUME = 5;
	private static final int MSG_CAMERA_USING_ERROR = 6;
	private static final int MSG_HIDE_FOCUS_AND_METERING_VIEW = 7;

	private static final int TOUCH_NONE = -1;
	private static final int TOUCH_MOVE = 0;
	private static final int TOUCH_CLICK = 1;
	private static final int TOUCH_SCALE = 2;

	private Context mContext;
	private CameraPageSite mSite;

	private int mTopHeight;
	private int mBottomHeight;

	private boolean mUiEnable = false;

	private CameraLayout mCameraLayout;

	private GLCameraView mGLCameraView;

	/**
	 * 顶部
	 */
	private TopControlView mTopControlView;

	/**
	 * 取消构图
	 */
	private TextView mCancelTeachView;

	/**
	 * 更多控制
	 */
	private MoreControl mMoreControl;

	/**
	 * 底部
	 */
	private BottomControlView mBottomControlView;

	/**
	 * 滤镜文字提示
	 */
	private FilterText mFilterText;

	private Bitmap mAlbumThumb;

	private View mBlackMask;
	private boolean isShowBlack;
	private long mDelayTime = 350;

	/**
	 * 滤镜布局
	 */
	private FilterLayout mFilterLayout;
	private boolean isShowFilter;

	/**
	 * 滤镜
	 */
	private FilterRes mFilterRes;
	private FilterItem mFilterItem;
	private float mAlpha;
	private List<DragListItemInfo> mFilterDatas;
	private int mCurFilterUri;
	/**
	 * 当前选中的滤镜在mFilterDatas列表的下标，用于左滑和右滑改变滤镜
	 */
	private int mFilterIndex = 1;

	/**
	 * 旋转的角度
	 */
	private int mRotation = 0;

	/**
	 * 获取滤镜列表任务，因为可能会卡死，需要放在另外的线程
	 */
	private GetFilterTask mGetFilterTask;

	private AnimShutterView mAnimShutterView;

	/**
	 * 用来标记是否正在拍照，主要用于取消延时拍照
	 */
	private boolean doTakePicture;

	/**
	 * 用于标记调用系统api拍照到系统回调这段时间
	 */
	private boolean isTakingPicture;

	/**
	 * 用于标记调用了系统api拍照多少次
	 */
	private AtomicInteger mTakingPictureCount;

	/**
	 * 触屏拍照
	 */
	private boolean isTouchCapture = false;

	/**
	 * 消息处理
	 */
	private PageHandler mPageHandler;

	/**
	 * 屏幕亮度
	 */
	private boolean mIsKeepScreenOn;

	/**
	 * 第三方调用拍照时保存提示
	 */
	private ProgressDialog mSaveDialog;

	/**
	 * 镜头
	 */
	private CameraWrapper mCameraWrapper;
	private int mCurrentCameraId;
	private boolean isSwitchCamera;

	/**
	 * 预览大小、宽高比
	 */
	private int mPreviewWidth = ShareData.m_screenWidth;
	private int mPreviewHeight = (int)(ShareData.m_screenWidth * 16f / 9);
	private int mSize = CameraConfig.SIZE_9_16;

	/**
	 * 拍照
	 */
	private boolean isTakeOneThenExits; //拍一张然后退出
	private boolean isHideAlbum;
	private boolean isOtherAppCall;
	private ImageFile2 mLastImageFile2;
	private HandlerThread mHandlerThread;
	private Camera2Handler mCamera2Handler;

	/**
	 * 定时拍照
	 */
	private int mCurrentTimer;
	private int mTimerCount = 0;

	/**
	 * 镜头修正
	 */
	private boolean isPatchMode;
	private boolean isShowPatchDialog;
	private boolean mPatchOtherCamera = false; // 校对其它镜头
	private int mPreviewPatchDegree = 90;
	private int mPicturePatchDegree = 90; // 保存照片时的修正角度

	/**
	 * 对焦
	 */
	private int[] mFocusArea; // 可对焦区域
	private int mLastMoveDistance;
	private float mDownX, mDownY;
	private int mTouchType = TOUCH_NONE;
	private int mTouchViewIndex = -1; // 判断是否事件位置是否在对焦和测光区域，0代表对焦，1代表测光
	private boolean mFocusOrMeteringIsMoving;
	private boolean showFocusAndMeteringView;
	@SuppressWarnings("all")
	private Camera.Size mPreviewSize;
	private float mFocusRatio;
	private float mMinFocusDistance; // 改变焦距的最小距离

	/**
	 * 闪关灯
	 */
	private int mFlash;
	private String mCurrentFlashStr;

	/**
	 * 声音
	 */
	private CameraSound mCameraSound;
	private boolean mNoSound;

	/**
	 * 构图/教程辅助线
	 */
	private int mThemeSelected;
	private int mThemeCourseId;
	private long mLastClickTime = -1;
	private int isSelectItem;
	private Object mTeachRes;

	/**
	 * 标记更多菜单动画是否在进行
	 */
	private boolean isAnimationRunning = false;

	/**
	 * 标记是否打开滤镜介绍页或大师介绍页
	 */
	private boolean isPopupPage = false;

	/**
	 * 首次进入
	 */
	private boolean isFirst;
	private GuidanceView mGuidanceView;

	/**
	 * 标记是否调用onPause
	 */
	private boolean isCallPause;

	/**
	 * 用于忽略第三方调用时导致onResume和onPause生命周期的调用
	 */
	private boolean mIgnoreLifecycle;

	/**
	 * 针对全面屏做适配
	 */
	private View mDecorView;
	private int mTopMargin;
	private int mBottomMargin;

	public CameraPage(Context context, BaseSite site) {
		super(context, site);

		mContext = context;
		mSite = (CameraPageSite)site;

		MyBeautyStat.onPageStartByRes(R.string.拍照页);
		TongJiUtils.onPageStart(mContext, TAG);

		init();
	}

	private void init() {
		mTopHeight = ShareData.PxToDpi_xhdpi(100);
		mBottomHeight = ShareData.PxToDpi_xhdpi(220);

		mMinFocusDistance = 50;

		mTakingPictureCount = new AtomicInteger(0);

		mPageHandler = new PageHandler();
		mHandlerThread = new HandlerThread("camera_page");
		mHandlerThread.start();
		mCamera2Handler = new Camera2Handler(mHandlerThread.getLooper(), mPageHandler);

		isFirst = TagMgr.CheckTag(mContext, Tags.CAMERA_FIRST);
		if (isFirst) {
			TagMgr.SetTag(mContext, Tags.CAMERA_FIRST);
		}

		mFilterDatas = new ArrayList<>();
		mGetFilterTask = new GetFilterTask(this);
		mGetFilterTask.execute();

		CameraConfig.initConfig(mContext);

		initViews();

		LocationHelper.getInstance().startLocation(getContext());
	}

	private void initViews() {

		int blackHeight = 0;
		if (ShareData.m_screenRealHeight > mPreviewHeight) {
			blackHeight = ShareData.m_screenRealHeight - mPreviewHeight;
		}

		if (blackHeight > mTopHeight) {
			mTopMargin = mTopHeight;
			mBottomMargin = blackHeight - mTopMargin;
		} else {
			mBottomMargin = blackHeight;
		}

		LayoutParams params;

		mCameraLayout = new CameraLayout(mContext);
		mCameraLayout.setTopAndBottomHeight(mTopHeight, mBottomHeight);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.topMargin = mTopMargin;
		addView(mCameraLayout, params);

		mGLCameraView = mCameraLayout.getGLCameraView();

		mTopControlView = new TopControlView(mContext);
		if (mTopMargin > 0) {
			mTopControlView.setBackgroundColor(Color.BLACK);
		}
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTopHeight);
		addView(mTopControlView, params);
		mTopControlView.setOnTopControlListener(this);

		mMoreControl = new MoreControl(mContext);
		mMoreControl.setOnMoreControlListener(this);

		mBottomControlView = new BottomControlView(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBottomHeight);
		params.gravity = Gravity.BOTTOM;
		params.bottomMargin = mBottomMargin;
		addView(mBottomControlView, params);
		mBottomControlView.setOnBottomControlListener(this);

		mAnimShutterView = new AnimShutterView(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.BOTTOM;
		params.bottomMargin = mBottomMargin;
		addView(mAnimShutterView, params);
		mAnimShutterView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickShutter(true);
			}
		});

		mCancelTeachView = new TextView(mContext);
		mCancelTeachView.setGravity(Gravity.CENTER);
		mCancelTeachView.setText(R.string.canceltutorial);
		mCancelTeachView.setTextColor(Color.WHITE);
		mCancelTeachView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		ViewCompat.setBackground(mCancelTeachView, DrawableUtils.colorPressedDrawable(0xff656d78, 0xff6C737D));
		mCancelTeachView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				removeView(mCancelTeachView);
				mCameraLayout.setImageBitmap(null);
				mTeachRes = null;
				MyBeautyStat.onClickByRes(R.string.拍照页_取消构图);
			}
		});

		mFilterText = new FilterText(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.leftMargin = ShareData.PxToDpi_xhdpi(40);
		addView(mFilterText, params);

		initCamera();

		if (isFirst) {
			setUiEnable(false);
			mBottomControlView.hideFilterView();
			mGuidanceView = new GuidanceView(mContext);
			mGuidanceView.show(this, mBottomMargin);
			mGuidanceView.setOnHideListener(new GuidanceView.OnHideListener() {
				@Override
				public void onHide() {
					AnimatorUtils.removeView(CameraPage.this, mGuidanceView, 0);
					mBottomControlView.showFilterView();
					isFirst = false;
					setUiEnable(true);
					selectFirstFilter();
				}
			});
		} else {
			setUiEnable(true);
		}

		if (mBottomMargin > 0) {
			View view = new View(mContext);
			view.setBackgroundColor(Color.BLACK);
			params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBottomMargin);
			params.gravity = Gravity.BOTTOM;
			addView(view, params);
		}

		// 黑色遮罩放在最后
		mBlackMask = new View(mContext);
		mBlackMask.setBackgroundColor(Color.BLACK);
		mBlackMask.setClickable(true);
		addMask();
	}

	private void addMask() {

		mBlackMask.setAlpha(1);

		if (indexOfChild(mBlackMask) < 0) {
			LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			addView(mBlackMask, params);
		}
		isShowBlack = true;
	}

	private void addCancelTeachView() {
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTopHeight);
		addView(mCancelTeachView, params);
	}

	/**
	 * 初始化Camera
	 */
	@SuppressWarnings("all")
	private void initCamera() {
		mCameraWrapper = mGLCameraView.getCamera();
		mCameraWrapper.setPreviewSize(mPreviewWidth, mPreviewHeight);
		mCameraWrapper.setPictureSize(mPreviewHeight, mPreviewWidth);
		mCameraWrapper.setCameraAllCallback(this);

		mCurrentCameraId = 0;
		mCameraWrapper.openCamera(mCurrentCameraId);
	}

	/**
	 * 初始化所有控件的状态
	 */
	@SuppressWarnings("all")
	private void initAllWidgetState() {

		if (isPatchMode) {
			setPreviewSize(CameraConfig.SIZE_9_16);
		} else {
			mSize = CameraConfig.getImageSize();
			setPreviewSize(mSize);
		}

		// 需要关闭自动对焦功能，因为当闪光灯模式为自动时，对焦完成可能会造成闪关灯闪一下
		mCameraWrapper.setAutoLoopFocus(false);
		mCameraWrapper.setSilenceOnTaken(SettingInfoMgr.GetSettingInfo(getContext()).GetCameraSoundState());

		mPreviewPatchDegree = CameraUtils.getPreviewPatchDegree(mCurrentCameraId); //((360- getPreviewPatchDegree()%360)%360 + 90)%360;//
		mCameraWrapper.setPreviewOrientation(mPreviewPatchDegree); //通过外部获取预览修正角度;

		// 获取镜头闪光灯的配置
		mFlash = CameraConfig.FLASH_AUTO;
		mCurrentFlashStr = CameraConfig.getFlashStr(mFlash);
		mCameraWrapper.setFlashMode(mCurrentFlashStr);

		mMoreControl.setFlashEnable(!mCameraWrapper.isFront());
		mTopControlView.setSwitchEnable(mCameraWrapper.getNumberOfCameras() > 1);

		mCurrentTimer = CameraConfig.getTimer();
		mMoreControl.setTimer(mCurrentTimer);

		// 触屏拍照
		isTouchCapture = CameraConfig.isTouchCapture();
		mMoreControl.setTouchCapture(isTouchCapture);

		mCameraLayout.hideFocusView();
		if (!isPatchMode) {
			mCameraLayout.setOnTouchListener(this);
		}

		mNoSound = SettingInfoMgr.GetSettingInfo(getContext()).GetCameraSoundState();
		mCameraWrapper.setSilenceOnTaken(mNoSound);

		keepScreenWakeUp(true);

		//声音
		initCameraSound();

		initThumbBitmap();
	}

	/**
	 * 保持屏幕常亮
	 *
	 * @param wakeup 是否保持常亮
	 */
	private void keepScreenWakeUp(boolean wakeup) {

		if (wakeup && !mIsKeepScreenOn) {
			((Activity)getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			mIsKeepScreenOn = true;
		} else if (!wakeup && mIsKeepScreenOn) {
			((Activity)getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			mIsKeepScreenOn = false;
		}
	}

	/**
	 * 初始化声音，平均耗时1850ms
	 * 另开线程获取声音
	 */
	private void initCameraSound() {
		if (mCameraSound == null) {
			mCameraSound = new CameraSound();

		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				mCameraSound.initSounds(mContext);//初始化声音 平均耗时1850ms
			}
		}, "initSound").start();
	}

	/**
	 * 初始化缩略图信息
	 */
	private void initThumbBitmap() {
		if (!mBottomControlView.isHideAlbum()) {

			// 获取缩略图
			PhotoInfo photoInfo = PhotoStore.getInstance(mContext).getFirstPhotoInfo();
			if (photoInfo != null) {
				mAlbumThumb = ImageStore.getThumbnail(getContext(), PhotoUtils.change(photoInfo));
				mBottomControlView.setAlbumThumb(mAlbumThumb);
			}
		}
	}

	/**
	 * @param params 参数信息
	 *               <p>
	 *               cameraId 镜头id (int) 0：后置，1：前置
	 *               patchMode 校正模式 (int) 0：不校正，1：校正
	 *               isTakeOneThenExits 是否拍一张然后退出 (boolean)
	 *               isHideAlbum 是否隐藏相册按钮 (boolean)
	 *               isOtherAppCall 第三方应用调用
	 */
	@Override
	@SuppressWarnings("all")
	public void SetData(HashMap<String, Object> params) {
		enterImmersiveMode();
		if (params != null) {
			Object obj;
			int cameraId = 0;

			obj = params.get("cameraId");
			if (obj instanceof Integer) {
				cameraId = (int)obj;
			}

			obj = params.get("patchMode");
			if (obj instanceof Integer) {
				isPatchMode = (int)obj == 1;
			}

			obj = params.get("isTakeOneThenExits");
			if (obj instanceof Boolean) {
				isTakeOneThenExits = (boolean)obj;
			}

			obj = params.get("isHideAlbum");
			if (obj instanceof Boolean) {
				isHideAlbum = (boolean)obj;
			}

			obj = params.get("isOtherAppCall");
			if (obj instanceof Boolean) {
				isOtherAppCall = (boolean)obj;
				mIgnoreLifecycle = isOtherAppCall;
				if (isOtherAppCall) {
					mSaveDialog = new ProgressDialog(mContext);
					mSaveDialog.setCancelable(false);
					String message = getResources().getString(R.string.saving) + "...";
					mSaveDialog.setMessage(message);
				}
			}

			if (isHideAlbum) {
				mBottomControlView.hideAlbumView();
			}

			if (isPatchMode) {
				mPatchOtherCamera = true;
				if (cameraId != mCurrentCameraId) {
					onClickSwitch();
				}

				mTopControlView.setVisibility(INVISIBLE);
				mBottomControlView.setVisibility(INVISIBLE);

				mCameraLayout.setOnTouchListener(null);
				isShowPatchDialog = false;
			}
		}

		initAllWidgetState();
		initThumbBitmap();

		if (!mSite.m_myParams.isEmpty()) {
			setTeachView(mSite.m_myParams);
			mSite.m_myParams.clear();
		}
	}


	@Override
	public void onResume() {

		if (mIgnoreLifecycle || !isCallPause) {
			return;
		}

		isCallPause = false;

		resume();

		TongJiUtils.onPageResume(mContext, TAG);
	}

	@SuppressWarnings("all")
	private void resume() {

		if (isPopupPage) {
			return;
		}

		mDelayTime = 0;
		addMask();

		if (mGLCameraView != null) {
			mGLCameraView.onResume();
		}

		doTakePicture = false;

		keepScreenWakeUp(true);
		initCameraSound();

		if (!isPatchMode) {
			mCameraLayout.setOnTouchListener(this);
		}

		setUiEnable(true);

		if (mPageHandler != null) {
			mPageHandler.sendEmptyMessageDelayed(MSG_ON_VIEW_RESUME, 1000);
		}

		if (mCameraWrapper != null) {
			mCameraWrapper.setCameraAllCallback(this);
		}
	}

	@Override
	public void onPause() {

		if (mIgnoreLifecycle) {
			mIgnoreLifecycle = false;
			return;
		}

		isCallPause = true;

		recycle();

		TongJiUtils.onPagePause(mContext, TAG);
	}

	@Override
	public void onBack() {

		if (isFirst) {
			mGuidanceView.onHide();
			return;
		}

		if (mUiEnable) {
			if (mMoreControl.isOpen()) {
				mMoreControl.close();
			} else if (mFilterLayout == null || !mFilterLayout.onBack()) {
				if (isShowFilter) {
					hideFilterLayout();
				} else {
					MyBeautyStat.onClickByRes(R.string.拍照页_退出拍照页);
					mSite.OnBack(mContext);
				}
			}
		}
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params) {

		if (siteID == SiteID.CAMERA_COMPOSITION_PAGE) {
			if (mPageHandler != null) {
				mPageHandler.sendEmptyMessageDelayed(MSG_ON_VIEW_RESUME, 1000);
			}
			setTeachView(params);

		} else if (siteID == SiteID.WEBVIEW) {
			showCameraPermissionHelper();
		} else if (siteID == SiteID.THEME_PAGE || siteID == SiteID.THEME_INTRO_PAGE) {

			mDelayTime = 0;
			addMask();

			if (mPageHandler != null) {
				mPageHandler.sendEmptyMessageDelayed(MSG_ON_VIEW_RESUME, 1000);
			}

			if (siteID == SiteID.THEME_PAGE && mFilterLayout != null) {
				mFilterLayout.setCurFilterUri(mCurFilterUri);
				mFilterLayout.onPageResult(SiteID.THEME_PAGE, params);
			}
		}
	}

	/**
	 * 设置构图
	 *
	 * @param params 参数
	 */
	@SuppressWarnings("all")
	private void setTeachView(HashMap<String, Object> params) {
		if (params != null) {
			if (params.containsKey("themeSelected")) {
				mThemeSelected = (Integer)params.get("themeSelected");
			}
			if (params.containsKey("themeCourseId")) {
				mThemeCourseId = (Integer)params.get("themeCourseId");
			}
			if (params.containsKey("isSelectItem")) {
				isSelectItem = (int)params.get("isSelectItem");
			}
			if (params.containsKey("themeBmp")) {
				Object res = params.get("themeBmp");
				mTeachRes = res;
				if (res != null) {
					mCameraLayout.setOnTouchListener(this);
					if (res instanceof Integer) {
						mCameraLayout.setImageResource(Integer.parseInt(res.toString()));
						addCancelTeachView();
					} else if (res instanceof Bitmap) {
						Bitmap bitmap = (Bitmap)res;
						float ratio = bitmap.getHeight() * 1f / bitmap.getWidth();
						int previewSize = CameraConfig.SIZE_9_16;
						if (ratio > 1 - 0.1f && ratio < 1 + 0.1f) {
							previewSize = CameraConfig.SIZE_1_1;
						} else if (ratio > 4f / 3 - 0.1f && ratio < 4f / 3 + 0.1f) {
							previewSize = CameraConfig.SIZE_3_4;
						} else if (ratio > 16f / 9 - 0.1f && ratio < 16f / 9 + 0.1f) {
							previewSize = CameraConfig.SIZE_9_16;
						}
						mSize = previewSize;
						setPreviewSize(previewSize);
						mCameraLayout.setImageBitmap(bitmap);
						addCancelTeachView();
					}
				}
			}
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus) {
			enterImmersiveMode();
		}
	}

	/**
	 * 进入沉浸模式
	 */
	private void enterImmersiveMode() {

		if (mDecorView == null) {
			mDecorView = ((Activity)mContext).getWindow().getDecorView();
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mDecorView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	/**
	 * 退出沉浸模式
	 */
	private void exitImmersiveMode() {
		if (mDecorView == null) {
			mDecorView = ((Activity)mContext).getWindow().getDecorView();
		}

		mDecorView.setSystemUiVisibility(0);
	}

	@Override
	public boolean onActivityKeyDown(int keyCode, KeyEvent event) {

		if (isPopupPage) {
			return super.onActivityKeyDown(keyCode, event);
		}

		switch (keyCode) {
			case KeyEvent.KEYCODE_UNKNOWN:
			case KeyEvent.KEYCODE_CAMERA:
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				if (mUiEnable || doTakePicture) {
					onClickShutter(true);
				}
				return true;
			case KeyEvent.KEYCODE_FOCUS:
				return true;
			case KeyEvent.KEYCODE_BACK:
				break;
			case KeyEvent.KEYCODE_ZOOM_IN:
				mCameraWrapper.setCameraZoomInOrOut(1);
				return true;
			case KeyEvent.KEYCODE_ZOOM_OUT:
				mCameraWrapper.setCameraZoomInOrOut(-1);
				return true;
			default:
				break;
		}
		return super.onActivityKeyDown(keyCode, event);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

		if (mFilterLayout != null) {
			mFilterLayout.onActivityResult(requestCode, resultCode, data);
		}

		return super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClose() {

		exitImmersiveMode();

		recycle();

		if (mGLCameraView != null) {
			mGLCameraView.onDestroy();
		}

		if (mFilterLayout != null) {
			mFilterLayout.release();
		}

		if (mHandlerThread != null) {
			mHandlerThread.quit();
			mHandlerThread = null;
		}

		if (mGetFilterTask != null && !mGetFilterTask.isCancelled()) {
			mGetFilterTask.cancel(true);
			mGetFilterTask = null;
		}

		if (mCameraLayout != null) {
			mCameraLayout.release();
		}

		if (mTopControlView != null) {
			mTopControlView.release();
		}

		if (mBottomControlView != null) {
			mBottomControlView.release();
		}

		if (mMoreControl != null) {
			mMoreControl.release();
		}

		if (mFilterText != null) {
			mFilterText.release();
		}

		MyBeautyStat.onPageEndByRes(R.string.拍照页);
		TongJiUtils.onPageEnd(mContext, TAG);
	}

	@SuppressWarnings("all")
	private void recycle() {

		if (mGLCameraView != null) {
			mGLCameraView.onPause();
		}

		if (mPageHandler != null) {
			mPageHandler.removeCallbacksAndMessages(null);
		}

		if (mCamera2Handler != null) {
			mCamera2Handler.removeCallbacksAndMessages(null);
		}

		keepScreenWakeUp(false);

		if (mCameraSound != null) {
			mCameraSound.clearSound();
		}

		if (mCameraWrapper != null) {
			mCameraWrapper.setFlashMode(CameraConfig.getFlashStr(CameraConfig.FLASH_OFF));
			mCameraWrapper.setCameraAllCallback(null);
		}

		mCameraLayout.hideTimerView();
		mCameraLayout.releaseFocusView();
		mCameraLayout.setOnTouchListener(null);

		mPreviewSize = null;
		LocationHelper.getInstance().destroy();
	}

	private void setUiEnable(boolean uiEnable) {
		mUiEnable = uiEnable;
		mTopControlView.setUiEnable(uiEnable);
		mBottomControlView.setUiEnable(uiEnable);
	}

	/**
	 * 设置预览比例
	 *
	 * @param sizeMode 预览比例
	 */
	private void setPreviewSize(int sizeMode) {

		mMoreControl.setPreviewSize(sizeMode);
		changePreviewSize(sizeMode);
	}

	/**
	 * 改变预览比例
	 *
	 * @param sizeMode 预览比例
	 */
	private void changePreviewSize(int sizeMode) {

		float ratio = 16f / 9;
		int tongjiId = R.integer.拍照_比例_16_9;
		int statId = R.string.镜头设置_全屏拍照;
		switch (sizeMode) {
			case CameraConfig.SIZE_1_1:
				ratio = 1;
				tongjiId = R.integer.拍照_比例_1_1;
				statId = R.string.镜头设置_1_1拍照;
				break;
			case CameraConfig.SIZE_3_4:
				ratio = 4f / 3;
				tongjiId = R.integer.拍照_比例_4_3;
				statId = R.string.镜头设置_4_3拍照;
				break;
			case CameraConfig.SIZE_9_16:
				ratio = 16f / 9;
				tongjiId = R.integer.拍照_比例_16_9;
				statId = R.string.镜头设置_全屏拍照;
				break;
		}

		MyBeautyStat.onClickByRes(statId);
		TongJi2.AddCountByRes(mContext, tongjiId);

		if (mCameraLayout != null) {
			int[] touchArea = mCameraLayout.changePreviewRatio(sizeMode, ratio);
			if (touchArea != null) {
				if (mFocusArea == null) {
					mFocusArea = new int[2];
				}
				mFocusArea[0] = touchArea[0];
				mFocusArea[1] = touchArea[1];

				float center = (mFocusArea[1] - mFocusArea[0]) / 2f;
				mFilterText.setTranslationY(mFocusArea[0] + center - mFilterText.getHeight() / 2f + mTopMargin);
			}
		}

		if (!isPatchMode) {
			CameraConfig.setImageSize(sizeMode);
		}
	}

	/**
	 * 判断是否位于可对焦区域
	 */
	private boolean isValidArea(float position) {
		return position > mFocusArea[0] && position < mFocusArea[1];
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float x = event.getX();
		float y = event.getY();

		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				mPageHandler.removeMessages(MSG_HANDLE_FOCUS_AND_METERING);
				if (mTouchType == TOUCH_CLICK) {
					mTouchViewIndex = mCameraLayout.getTouchAreaIndex(x, y);

					if (mTouchViewIndex == 0 && mCameraWrapper != null && mCameraWrapper.isFront()) {
						// 前置没有对焦
						mTouchViewIndex = -1;
					}

					if (mTouchViewIndex >= 0) {
						mPageHandler.removeMessages(MSG_HIDE_FOCUS_AND_METERING_VIEW);
					} else {
						mTouchType = TOUCH_NONE;
					}
				}

				mDownX = x;
				mDownY = y;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				if (!showFocusAndMeteringView) {
					mTouchType = TOUCH_SCALE;
					mLastMoveDistance = (int)CameraUtils.getDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mTouchType == TOUCH_CLICK) {

					if (!isValidArea(y)) {
						mFocusOrMeteringIsMoving = false;
						return true;
					}

					mPageHandler.removeMessages(MSG_HIDE_FOCUS_AND_METERING_VIEW);
					mFocusOrMeteringIsMoving = true;
					if (mTouchViewIndex == 0) {
						mCameraLayout.setFocusLocation(x, y);
					} else if (mTouchViewIndex == 1) {
						mCameraLayout.setMeteringLocation(x, y);
					}
				} else if (mTouchType == TOUCH_SCALE) {
					if (event.getPointerCount() > 1) {
						int distance = (int)CameraUtils.getDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
						int dx = distance - mLastMoveDistance;
						if (Math.abs(dx) > mMinFocusDistance) {
							mLastMoveDistance = distance;
							if (mCameraWrapper != null) {
								int delta = mCameraWrapper.getMaxZoom() / 10;
								if (delta < 1) {
									delta = 1;
								}
								for (int i = 0; i < delta; i++) {
									mCameraWrapper.setCameraZoomInOrOut(dx > 0 ? 1 : -1);
								}
							}
						}
					}
				}

				break;
			case MotionEvent.ACTION_UP:

				if (mTouchType == TOUCH_NONE) {
					if (CameraUtils.getDistance(mDownX, mDownY, x, y) >= ShareData.PxToDpi_xhdpi(50) && Math.abs(y - mDownY) <= ShareData.PxToDpi_xhdpi(200)) {
						mTouchType = TOUCH_MOVE;
					} else {
						mTouchType = TOUCH_CLICK;
					}
				}

				if (mTouchType == TOUCH_MOVE) {
					if (doTakePicture) {
						return true;
					}

					// 如果上一个状态时Move，这里要加判断

					int minDistance = ShareData.PxToDpi_xhdpi(20);
					// 是否横屏
					boolean isLandscape = mRotation % 180 != 0;
					if (!isLandscape && Math.abs(event.getX() - mDownX) > minDistance) {
						changeFilter(event.getX() - mDownX > 0);
					} else if (isLandscape && Math.abs(event.getY() - mDownY) > minDistance) {
						changeFilter(event.getY() - mDownY > 0);
					} else {
						mTouchType = TOUCH_CLICK;
					}
				}

				if (mTouchType == TOUCH_CLICK) {

					if (isTouchCapture) {
						RectF rectF = new RectF(0, mFocusArea[0], ShareData.m_screenWidth, mFocusArea[1]);
						if (rectF.contains(x, y)) {
							mPageHandler.sendEmptyMessage(MSG_HIDE_FOCUS_AND_METERING_VIEW);
							TongJi2.AddCountByRes(mContext, R.integer.相机_触屏拍照);
							onClickShutter(false);
						}
						return true;
					}

					mFocusOrMeteringIsMoving = false;

					if (!isValidArea(y)) {
						return true;
					}

					hideFilterLayout();
					hideMoreControl();

					if (mTouchViewIndex < 0) {
						mCameraLayout.setFocusAndMeteringLocation(x, y, x, y);
					}

					if (showFocusAndMeteringView) {
						mPageHandler.removeMessages(MSG_HIDE_FOCUS_AND_METERING_VIEW);
						mPageHandler.sendEmptyMessage(MSG_HANDLE_FOCUS_AND_METERING);
					} else {
						showFocusAndMeteringView = true;
						mPageHandler.sendEmptyMessage(MSG_HANDLE_FOCUS_AND_METERING);
					}

				} else if (mTouchType == TOUCH_SCALE) {
					mTouchType = TOUCH_NONE;
					mLastMoveDistance = 0;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				mTouchType = TOUCH_NONE;
				mLastMoveDistance = 0;
				break;
		}
		return true;
	}

	/**
	 * 修改滤镜
	 *
	 * @param right 手指是否向右滑
	 */
	private void changeFilter(boolean right) {

		if (mFilterDatas.isEmpty()) {
			return;
		}

		DragListItemInfo info;

		do {
			int filterIndex = calculateIndex(mFilterIndex, right);

			if (filterIndex == mFilterIndex) {
				return;
			}

			mFilterIndex = filterIndex;
			info = mFilterDatas.get(mFilterIndex);

			// 撤销右滑滑动出滤镜素材管理操作
//			if (info.m_uri == DragListItemInfo.URI_MGR) {
//				mFilterIndex = 1;
//				onDownloadMore();
//				hideFilterLayout();
//				return;
//			}
		} while (info.m_isLock || info.m_style != DragListItemInfo.Style.NORMAL);

		if (mFilterLayout != null) {
			mFilterLayout.onItemClick(null, info, mFilterIndex, true);
		}

		hideFilterLayout();
		hideMoreControl();
	}

	/**
	 * 计算下标
	 *
	 * @param right 手指是否向右滑
	 */
	private int calculateIndex(int filterIndex, boolean right) {
		if (right) {
			filterIndex--;
		} else {
			filterIndex++;
		}

		int size = mFilterDatas.size();
		if (filterIndex >= size) {
			filterIndex = size - 1;
		} else if (filterIndex < 1) {
			filterIndex = 1;
		}

		return filterIndex;
	}

	@Override
	public void onScreenOrientationChanged(int orientation, int pictureDegree, float fromDegree, float toDegree) {
		mPicturePatchDegree = pictureDegree;

		mRotation = ((int)toDegree + 360) % 360;

		// 当屏幕旋转时，控件也跟随旋转
		if (mCameraLayout != null) {
			mCameraLayout.setRotate(toDegree);
		}
		if (mTopControlView != null) {
			mTopControlView.setRotate(toDegree);
		}
		if (mBottomControlView != null) {
			mBottomControlView.setRotate(toDegree);
		}
		if (mMoreControl != null) {
			mMoreControl.setRotate(toDegree);
		}
	}

	@Override
	@SuppressWarnings("all")
	public void onAutoFocus(boolean success, Camera camera) {
		if (!mFocusOrMeteringIsMoving) {
			mPageHandler.removeMessages(MSG_HIDE_FOCUS_AND_METERING_VIEW);
			mPageHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS_AND_METERING_VIEW, 2000);
		}
	}

	@Override
	@SuppressWarnings("all")
	public void onError(int error, Camera camera) {
		if (mPageHandler != null) {
			mPageHandler.obtainMessage(MSG_CAMERA_USING_ERROR, error, error).sendToTarget();
		}
	}

	@Override
	@SuppressWarnings("all")
	public void onPictureTaken(byte[] data, Camera camera) {
		isTakingPicture = false;

		float previewRatio = 16f / 9;
		MyBeautyStat.PictureSize size = MyBeautyStat.PictureSize.PictureSize16_9竖;
		if (mSize == CameraConfig.SIZE_1_1) {
			previewRatio = 1;
			size = MyBeautyStat.PictureSize.PictureSize1_1;
		} else if (mSize == CameraConfig.SIZE_3_4) {
			previewRatio = 4f / 3;
			size = MyBeautyStat.PictureSize.PictureSize4_3;
		}

		doTakePicture = false;
		final int degree = CameraUtils.getPictureDegree(mCurrentCameraId, mPicturePatchDegree);
		if (isPatchMode) {
			Bitmap bitmap = Camera2Handler.rotateAndCropPicture(data, mCameraWrapper.isFront(), degree, previewRatio, mFocusArea[0] * 1.0f / mPreviewHeight, 1024);
			showPatchDialog(2, bitmap);
			return;
		}

		mBottomControlView.setOpenAlbumEnable(false);

		Message message = mCamera2Handler.obtainMessage();
		message.what = Camera2Handler.MSG_SAVE_IMAGE;
		Camera2Handler.Params params = new Camera2Handler.Params();
		params.context = mContext;
		params.cameraWrapper = mCameraWrapper;
		params.data = data;
		params.degree = degree;
		params.ratio = previewRatio;
		params.topScale = mFocusArea[0] * 1.0f / mPreviewHeight;
		params.maxSize = -1;
		params.quality = 96;
		params.isSave = !isTakeOneThenExits;
		message.obj = params;
		params.filterRes = mFilterRes;
		params.alpha = (int)(mAlpha * 100);
		params.hideThumb = isHideAlbum;
		mCamera2Handler.sendMessage(message);

		if (isOtherAppCall && mSaveDialog != null) {
			mSaveDialog.show();
		}

		String materialId = "0000";
		int alpha = 0;
		if (mFilterRes != null) {
			materialId = String.valueOf(mFilterRes.m_id);
			alpha = (int)(mAlpha * 12);
		}

		MyBeautyStat.onTakePhoto(materialId, alpha, size, R.string.拍照页);
	}

	@Override
	@SuppressWarnings("all")
	public void onPreviewFrame(byte[] data, Camera camera) {

		if (isShowBlack) {
			isShowBlack = false;
			AnimatorUtils.removeView(this, mBlackMask, mDelayTime);
		}

		if (isPatchMode && !isShowPatchDialog) {
			isShowPatchDialog = true;
			if (mPageHandler != null) {
				mPageHandler.post(new Runnable() {
					@Override
					public void run() {
						showPatchDialog(0, null);
					}
				});
			}
		}
		if (isSwitchCamera) {
			isSwitchCamera = false;
			mPreviewSize = null;
			mFocusRatio = 0;
		}
		if (mPreviewSize == null) {
			int key = (int)(data.length / 1.5f);
			mPreviewSize = mCameraWrapper.getPreviewDataLenghts().get(key);
			if (mPreviewSize == null) {
				mPreviewSize = mCameraWrapper.getCameraParameters().getPreviewSize();
			}
		}
		if (mFocusRatio <= 0) {
			if (mPreviewSize == null) {
				mFocusRatio = 1.0f;
			} else {
				final int previewSizeWidth = mPreviewSize.width > mPreviewSize.height ? mPreviewSize.width : mPreviewSize.height;
				mFocusRatio = previewSizeWidth * 1.0f / mPreviewHeight;
			}
		}
	}

	@Override
	public void onShutter() {

	}

	@Override
	public void onClickHome() {
		MyBeautyStat.onClickByRes(R.string.拍照页_退出拍照页);
		mSite.OnBack(mContext);
	}

	@Override
	public void onClickSwitch() {
		isSwitchCamera = true;
		mCameraWrapper.setPreviewSize(mPreviewWidth, mPreviewHeight);
		mGLCameraView.switchCamera();

		mCurrentCameraId = mCameraWrapper.getCurrentCameraId();

		mCameraWrapper.setFlashMode(mCurrentFlashStr);
		mMoreControl.setFlashEnable(!mCameraWrapper.isFront());

		MyBeautyStat.onClickByRes(R.string.拍照页_切换至前置);
	}

	@Override
	public void onClickTeach() {
		hideFilterLayout();
		hideMoreControl();

		if (System.currentTimeMillis() - mLastClickTime < 1000) {//超过1s才能再次点击
			return;
		}

		TongJi2.AddCountByRes(mContext, R.integer.拍照_教程);

		mLastClickTime = System.currentTimeMillis();
		if (mSite != null) {
			Bitmap bitmap = CameraUtils.takeScreenShot(mCameraLayout);
			if (bitmap != null && !bitmap.isRecycled()) {
				filter.fakeGlass(bitmap, 0x40000000);//0x5fffffff
			}

			if (mGLCameraView != null) {
				mGLCameraView.onPause();
			}

			HashMap<String, Object> params = new HashMap<>();
			params.put("bgBmp", bitmap);
			params.put("themeSelected", mThemeSelected);
			params.put("themeCourseId", mThemeCourseId);
			params.put("isSelectItem", isSelectItem);
			mSite.openCompositionPage(mContext, params);
		}

		MyBeautyStat.onClickByRes(R.string.拍照页_打开构图);
	}

	@Override
	public void onClickMore() {
		hideFilterLayout();
		mMoreControl.openOrClose(this);

		MyBeautyStat.onClickByRes(R.string.拍照页_打开镜头设置);
	}

	private void hideMoreControl() {
		if (mMoreControl.isOpen()) {
			mMoreControl.close();
		}
	}

	@Override
	public void onClickShutter() {
		onClickShutter(true);
	}

	private void onClickShutter(boolean addTongji) {

		if (isPopupPage) {
			return;
		}

		hideMoreControl();
		hideFilterLayout();

		if (doTakePicture) {
			// 取消延时拍照
			mPageHandler.removeMessages(MSG_TIMER_COUNT_DOWN);
			mPageHandler.sendEmptyMessage(MSG_CANCEL_TAKE_PICTURE);
			doTakePicture = false;
			setUiEnable(true);
			return;
		}

		doTakePicture = true;
		setUiEnable(false);
		if (isPatchMode) {
			mTimerCount = 0;
		} else {
			mTimerCount = 0;
			if (mCurrentTimer == CameraConfig.TIMER_3S) {
				mTimerCount = 3;
			} else if (mCurrentTimer == CameraConfig.TIMER_10S) {
				mTimerCount = 10;
			}
		}
		mPageHandler.sendEmptyMessage(MSG_TIMER_COUNT_DOWN);
		if (addTongji) {
			MyBeautyStat.onClickByRes(R.string.拍照页_点击拍照按钮);
			TongJi2.AddCountByRes(mContext, R.integer.拍照_按钮);
		}
	}

	@Override
	public void onClickFilter() {
		if (isShowFilter || mFilterLayout == null || isAnimationRunning) {
			return;
		}

		isAnimationRunning = true;

		hideMoreControl();
		isShowFilter = true;

		mAnimShutterView.setShow(true);

		AnimatorSet animatorSet = new AnimatorSet();
		ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", ShareData.PxToDpi_xhdpi(222) + mBottomMargin, 0);
		animatorSet.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {
				LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				params.bottomMargin = mBottomMargin;
				addView(mFilterLayout, params);

				mBottomControlView.showShutterView(false);

				MyBeautyStat.onPageStartByRes(R.string.滤镜列表页);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mBottomControlView.setVisibility(GONE);
				isAnimationRunning = false;
				mFilterLayout.setUiEnable(true);
			}
		});
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBottomControlView, "alpha", 1, 0);

		animatorSet.playTogether(animator, mAnimShutterView.getScaleAnimator(false), animator1);
		animatorSet.setDuration(350);
		animatorSet.start();

		MyBeautyStat.onClickByRes(R.string.拍照页_打开滤镜列表);
		TongJi2.AddCountByRes(mContext, R.integer.相机_滤镜);
	}

	/**
	 * 收起滤镜弹层
	 */
	private void hideFilterLayout() {
		onClickDown();
	}

	@Override
	public void onClickAlbum() {

		if (mTeachRes != null) {
			mSite.m_myParams.put("themeSelected", mThemeSelected);
			mSite.m_myParams.put("themeCourseId", mThemeCourseId);
			mSite.m_myParams.put("isSelectItem", isSelectItem);
			mSite.m_myParams.put("themeBmp", mTeachRes);
		}

		if (mLastImageFile2 == null) {
			MyBeautyStat.onClickByRes(R.string.拍照页_打开照片相册);
			mSite.OnPickPhoto(mContext, null);
		} else {
			HashMap<String, Object> params = new HashMap<>();
			params.put("imgs", mLastImageFile2);
			mSite.openBeautyPage(mContext, params);
		}
	}

	@Override
	public void changeSize(int size) {
		if (mPageHandler != null) {
			mPageHandler.removeMessages(MSG_TIMER_COUNT_DOWN);
			mPageHandler.sendEmptyMessage(MSG_CANCEL_TAKE_PICTURE);
		}

		mSize = size;
		changePreviewSize(mSize);
	}

	@Override
	public void changeFlash(int flash) {

		mFlash = flash;

		mCurrentFlashStr = CameraConfig.getFlashStr(flash);
		mCameraWrapper.setFlashMode(mCurrentFlashStr);
	}

	@Override
	public void changeTimer(int timer) {
		mCurrentTimer = timer;
		CameraConfig.setTimer(mCurrentTimer);
	}

	@Override
	public void changeTouchCapture(boolean open) {
		isTouchCapture = open;
		CameraConfig.setTouchCapture(isTouchCapture);

		MyBeautyStat.onClickByRes(isTouchCapture ? R.string.镜头设置_开启触屏 : R.string.镜头设置_关闭触屏);
	}

	@Override
	public void onDownloadMore() {
		if (mUiEnable) {
			if (mGLCameraView != null) {
				mGLCameraView.onPause();
			}

			MyBeautyStat.onClickByRes(R.string.滤镜列表页_进入更多);

			HashMap<String, Object> params = new HashMap<>();
			params.put("type", ResType.FILTER);
			params.put("typeOnly", true);
			mSite.onDownloadMore(mContext, params);
		}
	}

	@Override
	public void onClickDown() {
		if (!isShowFilter || isAnimationRunning) {
			return;
		}

		mFilterLayout.setUiEnable(false);
		isAnimationRunning = true;

		AnimatorSet animatorSet = new AnimatorSet();

		ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", 0, ShareData.PxToDpi_xhdpi(222) + mBottomMargin);
		animatorSet.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {
				mBottomControlView.setVisibility(VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mBottomControlView.showShutterView(true);

				mAnimShutterView.setShow(false);
				removeView(mFilterLayout);
				isShowFilter = false;

				if (mFilterLayout != null) {
					mFilterLayout.resetPosition();
				}

				isAnimationRunning = false;

				MyBeautyStat.onPageEndByRes(R.string.滤镜列表页);
			}
		});
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBottomControlView, "alpha", 0, 1);
		animatorSet.playTogether(animator, mAnimShutterView.getScaleAnimator(true), animator1);
		animatorSet.setDuration(350);
		animatorSet.start();

		MyBeautyStat.onClickByRes(R.string.滤镜列表页_收起滤镜列表);
	}

	@Override
	public void onSelectFilter(FilterRes filterRes, int uri, int index) {
		if (mUiEnable && mFilterRes != filterRes) {
			mFilterRes = filterRes;
			mFilterItem = FilterItem.wrap(mContext, filterRes, uri);
			mFilterIndex = index;
			if (mFilterItem != null) {
				mAlpha = mFilterItem.resultAlpha;
			} else {
				mAlpha = 1;
			}
			mCurFilterUri = uri;
			mFilterLayout.setSelUri(mCurFilterUri);
			CameraConfig.setFilterUri(mCurFilterUri);
			mGLCameraView.changeFilter(mFilterItem);

			if (filterRes != null) {
				mFilterText.setText(filterRes.m_name);
				MyBeautyStat.onChooseMaterial(String.valueOf(filterRes.m_id), R.string.滤镜列表页);
			} else {
				mFilterText.setText(getResources().getString(R.string.camera_filter_original));
			}

			MyBeautyStat.onClickByRes(R.string.滤镜列表页_选择滤镜素材);
		}
	}

	@Override
	public void adjustFilterAlpha(float alpha) {
		if (mAlpha != alpha) {
			mAlpha = alpha;
			mGLCameraView.changeFilterAlpha(mAlpha);

			TongJi2.AddCountByRes(mContext, R.integer.相机_滤镜微调);
		}
	}

	@Override
	public void setPopupPage(boolean isPopup) {
		if (isPopupPage != isPopup) {
			isPopupPage = isPopup;

			if (isPopupPage) {
				recycle();
			} else {
				resume();
			}
		}
	}

	@Override
	public Context getPageContext() {
		return mContext;
	}

	@Override
	public void setFilterList(ArrayList<DragListItemInfo> dragListItemInfos) {
		mFilterDatas.addAll(dragListItemInfos);
		initFilterLayout();
	}

	/**
	 * 初始化滤镜信息
	 */
	private void initFilterLayout() {

		mFilterLayout = new FilterLayout(mContext, mFilterDatas);
		mFilterLayout.setOnFilterControlListener(this);

		// 滤镜
		mCurFilterUri = CameraConfig.getFilterUri();

		DragListItemInfo info, tempInfo = null;
		if (mCurFilterUri != DragListItemInfo.URI_ORIGIN) {
			for (int i = 0; i < mFilterDatas.size(); i++) {
				info = mFilterDatas.get(i);
				if (info.m_uri == mCurFilterUri) {
					tempInfo = info;
					mFilterIndex = i;
					break;
				}
			}
		}

		if (!isFirst) {
			ensureFilter(tempInfo);
		}
	}

	/**
	 * 确定选中该滤镜效果
	 */
	private void ensureFilter(DragListItemInfo tempInfo) {
		if (tempInfo == null) {
			mCurFilterUri = DragListItemInfo.URI_ORIGIN;
			mFilterRes = null;
			mFilterText.setText(getResources().getString(R.string.camera_filter_original));
			mFilterItem = null;
			mAlpha = 1;
		} else {
			mFilterLayout.setSelUri(mCurFilterUri);
			mFilterRes = (FilterRes)tempInfo.m_ex;
			mFilterText.setText(mFilterRes.m_name);
			mFilterLayout.setCurFilterRes(mFilterRes);
			mFilterItem = FilterItem.wrap(mContext, mFilterRes, mCurFilterUri);
			if (mFilterItem != null) {
				mAlpha = mFilterItem.resultAlpha;
				mGLCameraView.changeFilter(mFilterItem);
			} else {
				mAlpha = 1;
			}
		}

		CameraConfig.setFilterUri(mCurFilterUri);
	}

	/**
	 * 选中第一个滤镜效果
	 */
	private void selectFirstFilter() {
		DragListItemInfo info, tempInfo = null;
		for (int i = 0; i < mFilterDatas.size(); i++) {
			info = mFilterDatas.get(i);
			if (info.m_uri != DragListItemInfo.URI_MGR && info.m_uri != DragListItemInfo.URI_ORIGIN && !info.m_isLock && info.m_style == DragListItemInfo.Style.NORMAL) {
				tempInfo = info;
				mFilterIndex = i;
				mCurFilterUri = info.m_uri;
				break;
			}
		}

		if (tempInfo != null) {
			ensureFilter(tempInfo);
		}
	}

	@SuppressWarnings("all")
	private class PageHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_TIMER_COUNT_DOWN:
					if (mTimerCount == 0 && !isTakingPicture) {
						mTimerCount = 0;
						isTakingPicture = true;
						mCameraLayout.hideTimerView();
						mBottomControlView.setAlbumThumb(null);
						setUiEnable(true);

						if (!isOtherAppCall) {
							mTakingPictureCount.incrementAndGet();
							mBottomControlView.setLoading(true);
						}
						if (mCameraWrapper != null) {
							if (isTakeOneThenExits) {
								mCameraWrapper.takeOnePicture();
							} else {
								mCameraWrapper.takePicture();
							}
						}
					} else {
						mCameraLayout.changeTimer(mTimerCount);
						mCameraLayout.showTimerView();

						if (mCurrentTimer >= 2 && mCameraSound != null && !mNoSound) {
							if (mTimerCount == 3) {
								mCameraSound.playSound(0, 11);
								mCameraSound.playId = -1;
								mCameraSound.soundIsBusy = false;

							} else if (mTimerCount == 2) {
								mCameraSound.playSound(1, 11);
								mCameraSound.playId = -1;
								mCameraSound.soundIsBusy = false;

							} else if (mTimerCount == 1) {
								mCameraSound.playSound(2, 11);
								mCameraSound.playId = -1;
								mCameraSound.soundIsBusy = false;
							}
						}
						mTimerCount--;
						mPageHandler.sendEmptyMessageDelayed(MSG_TIMER_COUNT_DOWN, 1000);
					}
					break;
				case MSG_CANCEL_TAKE_PICTURE:
					mCameraLayout.hideTimerView();
					doTakePicture = false;
					isTakingPicture = false;
					if (mCameraSound != null) {
						mCameraSound.stopSound();
					}
					break;
				case MSG_HANDLE_FOCUS_AND_METERING:
					if (!mFocusOrMeteringIsMoving && mCameraWrapper != null) {

						float[] positions = mCameraLayout.getFoucuAndMeterPosition();
						if (positions != null && positions.length == 4) {
							mCameraWrapper.setFocusAndMeteringArea(positions[0], positions[1], positions[2], positions[3], mFocusRatio <= 0 ? 1.0f : mFocusRatio);
						}

						mPageHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS_AND_METERING_VIEW, 2000);
					}
					break;
//				case MSG_HIDE_ZOOM_SEEK_BAR:
//					mCameraBottomControl.setSeekBarVisible(false);
//					break;
				case MSG_ON_VIEW_RESUME:
					if (mCameraWrapper != null) {
						mCameraWrapper.setFlashMode(mCurrentFlashStr);
					}
					break;
				case MSG_CAMERA_USING_ERROR:
					if (msg.arg1 == CameraWrapper.CAMERA_ERROR_OCCUPIED_OR_NO_PERMISSION || (msg.arg1 == Camera.CAMERA_ERROR_UNKNOWN && Build.MODEL.toUpperCase(Locale.CHINA).equals("M5 NOTE"))) {//魅蓝note5
//                        errorMsg = "相机权限未开启，\n请在系统权限管理中开启权限";
//                        errorMsg = "1.相机被其他应用占用，请尝试关闭其他应用。\n2.相机权限未开启，请在系统权限管理中开启权限。";
						showCameraPermissionHelper();
//                        Toast.makeText(mContext, "相机权限未开启", Toast.LENGTH_LONG).show();
					} else {
						String errorMsg = getResources().getString(R.string.cameraErrorTips);
//                        Toast.makeText(mContext, "相机出错了error:" + msg.arg1, Toast.LENGTH_LONG).show();
						if (mCameraLayout != null) {
							TextView errorMsgView = new TextView(getContext());
							errorMsgView.setPadding(ShareData.PxToDpi_xhdpi(100), 0, ShareData.PxToDpi_xhdpi(100), 0);
							errorMsgView.setText(errorMsg);
							errorMsgView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
							errorMsgView.setTextColor(Color.WHITE);
							errorMsgView.setGravity(Gravity.CENTER);
							LayoutParams params = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
							params.gravity = Gravity.CENTER;
							mCameraLayout.addView(errorMsgView, params);

							mCameraLayout.setOnTouchListener(null);
							if (mCameraLayout != null) {
								mCameraLayout.postDelayed(new Runnable() {
									@Override
									public void run() {
										mUiEnable = false;
										mTopControlView.setUiEnable(false);
									}
								}, 300);
							}
						}
					}
					break;
				case MSG_HIDE_FOCUS_AND_METERING_VIEW:
					if (showFocusAndMeteringView) {
						if (mCameraLayout != null) {
							mCameraLayout.hideFocusView();
						}
						showFocusAndMeteringView = false;
						mTouchType = TOUCH_NONE;
					}
					break;
				case Camera2Handler.MSG_SAVE_IMAGE: {
					Camera2Handler.Params params = (Camera2Handler.Params)msg.obj;
					msg.obj = null;
					mLastImageFile2 = params.imageFile;
					PhotoStore.getInstance(mContext).addPhotoToAlbums(mLastImageFile2.m_finalOrgPath);
					if (isOtherAppCall || isTakeOneThenExits) {
						if (mSaveDialog != null && mSaveDialog.isShowing()) {
							mSaveDialog.dismiss();
						}
						HashMap<String, Object> params1 = new HashMap<>();
						params1.put("imgs", mLastImageFile2);
						mSite.OnTakePicture(mContext, params1);
						return;
					}

					if (mBottomControlView != null && mTakingPictureCount.decrementAndGet() == 0) {
						mBottomControlView.setLoading(false);
						mBottomControlView.setOpenAlbumEnable(true);
					}

					if (mFlash == CameraConfig.FLASH_ON) {
						mCameraWrapper.setFlashMode(mCurrentFlashStr, true);
					}
					break;
				}
				case Camera2Handler.MSG_IMAGE_THUMB: {
					Camera2Handler.Params params = (Camera2Handler.Params)msg.obj;
					msg.obj = null;
					if (params.thumb != null && !params.thumb.isRecycled()) {
						mAlbumThumb = params.thumb;
						if (mBottomControlView != null) {
							mBottomControlView.setAlbumThumb(mAlbumThumb);
						}
					}
					break;
				}
				default:
					break;
			}
		}
	}

	/**
	 * 弹出相机权限对话框
	 */
	private void showCameraPermissionHelper() {

		final CameraErrorTipsDialog errorTipsDialog = new CameraErrorTipsDialog(getContext());
		errorTipsDialog.setOnClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					if (mSite != null) {
						HashMap<String, Object> params = new HashMap<>();
						params.put("url", CAMERA_PERMISSION_HELPER_URL);
						mSite.openCameraPermissionsHelper(mContext, params);
					}
				} else if (which == 1) {
					//关闭页面
				}
				dialog.dismiss();
			}
		});
		errorTipsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (errorTipsDialog.canClosePage()) {
					onBack();
				}
			}
		});
		errorTipsDialog.show();

	}

	/**
	 * @param type 0: 开始提示，1: 镜头校正，2: 照片校正，3: 完成
	 * @param pic  Bitmap对象
	 */
	private void showPatchDialog(final int type, Bitmap pic) {

		final PatchDialog mDialog = new PatchDialog(getContext(), type);
		Window window = mDialog.getWindow();
		if (type == 1 && window != null) {
			window.setGravity(Gravity.BOTTOM);
			WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
			lp.y = ShareData.PxToDpi_xhdpi(40);
			window.setAttributes(lp);

		} else if (type == 2) {
			mDialog.setPicture(pic);
		}
		mDialog.setOnClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (type == 0) {
					showPatchDialog(1, null);
				} else if (type == 1) {
					if (which == 0) {
						//旋转预览
						if (mCameraWrapper != null) {
							mPreviewPatchDegree = mCameraWrapper.patchPreviewDegree();
						}
						mDialog.setCanQuitPatch(true);
					} else if (which == 1) {
						if (mCurrentCameraId == 0) {
							CameraConfig.setPreviewPatch0(mPreviewPatchDegree);
						} else if (mCurrentCameraId == 1) {
							CameraConfig.setPreviewPatch1(mPreviewPatchDegree);
						}
						Toast.makeText(mContext, getResources().getString(R.string.startPictures), Toast.LENGTH_SHORT).show();
						onClickShutter(true);
					}

				} else if (type == 2) {
					int rotate = mDialog.getRotate();
					rotate = (rotate + CameraUtils.getPicturePatchDegree(mCurrentCameraId)) % 360;
					if (mCurrentCameraId == 0) {
						CameraConfig.setPicturePatch0(rotate);
					} else if (mCurrentCameraId == 1) {
						CameraConfig.setPicturePatch1(rotate);
					}
					showPatchDialog(3, null);

				}
//				else if (type == 3) {
//
//				}
			}
		});

		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mDialog.canQuitPatch()) {
					if (mPatchOtherCamera && type == 3) {
						mPatchOtherCamera = false;
						if (mCurrentCameraId != mCameraWrapper.getNextCameraId()) {
							onClickSwitch();
							showPatchDialog(0, null);
						}
					} else {
						onBack();
					}
				}
			}
		});
		mDialog.show();
	}
}
