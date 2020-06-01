package cn.poco.capture2;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.PermissionChecker;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import cn.poco.album2.utils.T;
import cn.poco.camera.CameraAllCallback;
import cn.poco.camera.CameraConfig;
import cn.poco.camera.CameraErrorTipsDialog;
import cn.poco.camera.CameraWrapper;
import cn.poco.camera.GetFilterTask;
import cn.poco.camera.utils.CameraUtils;
import cn.poco.camera.view.CameraLayout;
import cn.poco.camera.view.CameraRenderer;
import cn.poco.camera.view.FilterLayout;
import cn.poco.camera.view.GLCameraView;
import cn.poco.capture2.encoder.MediaMuxerWrapper;
import cn.poco.capture2.encoder.MediaVideoEncoder;
import cn.poco.capture2.encoder.RecordState;
import cn.poco.capture2.model.Snippet;
import cn.poco.capture2.site.CapturePageSite;
import cn.poco.capture2.view.AnimShutterView;
import cn.poco.capture2.view.BottomControlView;
import cn.poco.capture2.view.FilterText;
import cn.poco.capture2.view.GuidanceView;
import cn.poco.capture2.view.ProgressView;
import cn.poco.capture2.view.RecordTip;
import cn.poco.capture2.view.SettingsView;
import cn.poco.capture2.view.TopControlView;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
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
import cn.poco.utils.InterphotoDlg;
import cn.poco.video.encode.EncodeUtils;
import cn.poco.video.render.PlayRatio;
import cn.poco.video.render.filter.FilterItem;
import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2017/12/28
 */
public class CapturePage extends IPage implements CameraAllCallback, TopControlView.OnTopControlListener, BottomControlView.OnBottomControlListener, SettingsView.OnSettingsListener, ProgressView.OnClickDeleteListener, GetFilterTask.OnGetFilterListener, FilterLayout.OnFilterControlListener, View.OnTouchListener {

	private static final String TAG = "录像";

	private static final String CAMERA_PERMISSION_HELPER_URL = "http://www.adnonstop.com/interphoto/android/index.php";

	private static final int MSG_HANDLE_FOCUS_AND_METERING = 1;
	private static final int MSG_ON_VIEW_RESUME = 2;
	private static final int MSG_CAMERA_USING_ERROR = 3;
	private static final int MSG_HIDE_FOCUS_AND_METERING_VIEW = 4;

	private static final int TOUCH_NONE = -1;
	private static final int TOUCH_MOVE = 0;
	private static final int TOUCH_CLICK = 1;
	private static final int TOUCH_SCALE = 2;

	private Context mContext;
	private CapturePageSite mSite;

	private int mTopHeight;
	private int mBottomHeight;

	private CameraLayout mCameraLayout;
	private GLCameraView mGLCameraView;
	private CameraRenderer mCameraRenderer;

	private boolean mUiEnable;

	private TopControlView mTopControlView;
	private BottomControlView mBottomControlView;

	private SettingsView mSettingsView;

	private FilterText mFilterText;

	private RecordTip mRecordTip;

	private ProgressView mProgressView;

	private AnimShutterView mAnimShutterView;

	private View mBlackMask;
	private boolean isShowBlack;
	private long mDelayTime = 350;

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
	private int mSize;

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
	 * 视频
	 */
	private boolean isRecording;
	private int mSnippetIndex = -1;
	private List<Snippet> mSnippets = new ArrayList<>();
	private boolean isOtherAppCall;
	private Uri mVideoUri;
	/**
	 * 用于标记第一个视频的旋转角度
	 */
	private int mFirstVideoRotation = 0;

	/**
	 * 视频录制
	 */
	private RecordManager mRecordManager;
	private int mRecordState = RecordState.IDLE;
	private int mVideoWidth = ShareData.m_screenWidth;
	private int mVideoHeight = ShareData.m_screenHeight;
	private int mRotation = 0;
	private int mRecordRotation = 0;
	private boolean isSupportHD; // 是否高清模式
	private int mMinVideoDuration = RecordManager.MIN_RECORD_DURATION;

	/**
	 * 闪关灯
	 */
	private int mFlash;
	private String mCurrentFlashStr;

	/**
	 * 视频时长
	 */
	private int mDuration;

	/**
	 * 屏幕亮度
	 */
	private boolean mIsKeepScreenOn;

	/**
	 * 消息处理
	 */
	private PageHandler mPageHandler;

	/**
	 * 音频焦点
	 */
	private AudioManager mAudioManager;
	private boolean isMusicActive;
	private MyOnAudioFocusChangeListener mListener;
	private boolean isToBeautify;

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
	 * 标记是否打开滤镜介绍页或大师介绍页
	 */
	private boolean isPopupPage = false;

	/**
	 * 获取滤镜列表任务，因为可能会卡死，需要放在另外的线程
	 */
	private GetFilterTask mGetFilterTask;

	/**
	 * 第三方调用录像保存提示
	 */
	private ProgressDialog mSaveDialog;

	/**
	 * 返回提示框
	 */
	private InterphotoDlg mBackDialog;

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

	private boolean isRestore;

	/**
	 * 添加视频模式
	 */
	private boolean isAddVideoMode;

	/**
	 *  添加视频时的画幅大小
	 */
	private int mAddVideoSize = 0;

	/**
	 * 添加视频时剩余拍摄时间，不小于一秒
	 */
	private int mLeftTime;

	public CapturePage(Context context, BaseSite site) {
		super(context, site);

		mContext = context;
		mSite = (CapturePageSite)site;

		MyBeautyStat.onPageStartByRes(R.string.录像页);
		TongJiUtils.onPageStart(mContext, TAG);

		init();
	}

	private void init() {

		mTopHeight = ShareData.PxToDpi_xhdpi(100);
		mBottomHeight = ShareData.PxToDpi_xhdpi(236);

		mMinFocusDistance = 50;

		mPageHandler = new PageHandler();

		isFirst = TagMgr.CheckTag(mContext, Tags.RECORD_FIRST);
		if (isFirst) {
			TagMgr.SetTag(mContext, Tags.RECORD_FIRST);
		}

		mFilterDatas = new ArrayList<>();
		mGetFilterTask = new GetFilterTask(this);
		mGetFilterTask.execute();

		CameraConfig.initConfig(mContext);

		if (SettingInfoMgr.GetSettingInfo(getContext()).getVideoQualityState() && EncodeUtils.isSupportHideMode()) {
			// 高清模式
			mVideoWidth = 1080;
			mVideoHeight = 1920;
			isSupportHD = true;
		} else {
			mVideoWidth = 720;
			mVideoHeight = 1280;
			isSupportHD = false;
		}

		initViews();

		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		mListener = new MyOnAudioFocusChangeListener();
		requestAudioFocus();
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
		mCameraLayout.removeTimerView();

		mGLCameraView = mCameraLayout.getGLCameraView();
		mGLCameraView.setVideoSize(mVideoWidth, mVideoHeight);
		mCameraRenderer = mGLCameraView.getRenderer();

		mTopControlView = new TopControlView(mContext);
		if (mTopMargin > 0) {
			mTopControlView.setBackgroundColor(Color.BLACK);
		}
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTopHeight);
		addView(mTopControlView, params);
		mTopControlView.setOnTopControlListener(this);

		mBottomControlView = new BottomControlView(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBottomHeight);
		params.gravity = Gravity.BOTTOM;
		params.bottomMargin = mBottomMargin;
		addView(mBottomControlView, params);
		mBottomControlView.setOnBottomControlListener(this);
		mBottomControlView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isShowSettings()) {
					hideSettingsView();
				}
			}
		});

		mSettingsView = new SettingsView(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.leftMargin = params.rightMargin = ShareData.PxToDpi_xhdpi(50);
		params.gravity = Gravity.BOTTOM;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(326) + mBottomMargin;
		mSettingsView.setLayoutParams(params);
		mSettingsView.setOnSettingsListener(this);

		mFilterText = new FilterText(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.leftMargin = ShareData.PxToDpi_xhdpi(40);
		addView(mFilterText, params);

		mRecordTip = new RecordTip(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.topMargin = ShareData.PxToDpi_xhdpi(40) + mTopMargin;
		addView(mRecordTip, params);

		mProgressView = new ProgressView(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(64));
		params.gravity = Gravity.BOTTOM;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(272) + mBottomMargin;
		addView(mProgressView, params);
		mProgressView.setOnClickDeleteListener(this);

		mAnimShutterView = new AnimShutterView(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.BOTTOM;
		params.bottomMargin = mBottomMargin;
		addView(mAnimShutterView, params);
		mAnimShutterView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isRecording) {
					onClickShutter();
				}
			}
		});

		initCamera();

		initBackDialog();

		if (isFirst) {
			setUiEnable(false);
			mBottomControlView.showGuidanceView(true);
			mGuidanceView = new GuidanceView(mContext);
			mGuidanceView.show(this, mBottomMargin);
			mGuidanceView.setOnHideListener(new GuidanceView.OnHideListener() {
				@Override
				public void onHide() {
					AnimatorUtils.removeView(CapturePage.this, mGuidanceView, 0);
					mBottomControlView.showGuidanceView(false);
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

	private void initBackDialog() {

		mBackDialog = new InterphotoDlg((Activity)mContext, R.style.backDialog);
		mBackDialog.SetTitle(R.string.camera_back_message, false);
		mBackDialog.SetPositiveBtnText(R.string.Cancel);
		mBackDialog.SetNegativeBtnText(R.string.ok);
		mBackDialog.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback() {
			@Override
			public void onOK() {
				mBackDialog.dismiss();
			}

			@Override
			public void onCancel() {
				mBackDialog.dismiss();
				mSite.onBack(mContext);
			}
		});
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

	private void setUiEnable(boolean uiEnable) {
		mUiEnable = uiEnable;
		mTopControlView.setUiEnable(uiEnable);
		mBottomControlView.setUiEnable(uiEnable);
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
				if (mUiEnable || isRecording) {
					onClickShutter();
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

	/**
	 * @param params 参数信息
	 *               <p>
	 *               	isOtherAppCall(boolean) 第三方应用调用
	 *               	minDuration(long) 最小录制时间，单位毫秒
	 *               	saveUri(String) 视频保存路径
	 *               	addVideo(boolean) 添加视频模式
	 *               	usable_time(int) 可用拍摄时间
	 *               	ratio(int) 视频画幅
	 *               <p>>
	 */
	@Override
	public void SetData(HashMap<String, Object> params) {
		enterImmersiveMode();

		if (isRestore) {
			return;
		}

		if (params != null) {

			Object obj = params.get("addVideo");
			if (obj instanceof Boolean && (boolean)obj) {

				isAddVideoMode = true;
				obj = params.get("usable_time");
				if(obj != null){
					mLeftTime = (int)obj;
				}

				obj = params.get("ratio");
				if (obj instanceof Integer) {

					int playRatio = (int)obj;
					switch (playRatio) {
						case PlayRatio.RATIO_9_16:
							mAddVideoSize = CameraConfig.SIZE_9_16;
							break;
						case PlayRatio.RATIO_16_9:
							mAddVideoSize = CameraConfig.SIZE_16_9;
							break;
						case PlayRatio.RATIO_235_1:
							mAddVideoSize = CameraConfig.SIZE_235_1;
							break;
						case PlayRatio.RATIO_1_1:
							mAddVideoSize = CameraConfig.SIZE_1_1;
							break;
						default:
							mAddVideoSize = CameraConfig.SIZE_9_16;
							break;
					}
				}

				initAllWidgetState();
				if (isFirst) {
					isFirst = false;
					setUiEnable(true);
					removeView(mGuidanceView);
					mBottomControlView.showGuidanceView(false);
				}
				mBottomControlView.hideSettingToast();
				mRecordManager.setMaxVideoDuration(CameraConfig.DURATION_180S);
				mRecordManager.setLeftDuration(mLeftTime);
				mRecordTip.setInitTime(mLeftTime / 1000);
				return;
			}

			obj = params.get("isOtherAppCall");
			if (obj instanceof Boolean) {
				isOtherAppCall = (boolean)obj;
				mIgnoreLifecycle = isOtherAppCall;
				if (isOtherAppCall) {
					mGLCameraView.setSaveFilter(true);

					mSaveDialog = new ProgressDialog(mContext);
					mSaveDialog.setCancelable(false);
					String message = getResources().getString(R.string.saving) + "...";
					mSaveDialog.setMessage(message);
				}

				obj = params.get("minDuration");
				if (obj instanceof Integer) {
					int minDuration = (Integer)obj;

					if (minDuration > RecordManager.MIN_RECORD_DURATION) {
						mMinVideoDuration = minDuration;
					}
				}

				obj = params.get("saveUri");
				if (obj instanceof Uri) {
					mVideoUri = (Uri)obj;
				}
			}
		}

		if (!isOtherAppCall) {
			FileUtils.clearVideoFiles();
		}

		mSite.m_myParams.clear();

		initAllWidgetState();
	}

	/**
	 *
	 * @param siteID Site ID
	 * @param params 参数信息
	 *               <p>
	 *               	restore(boolean) 是否恢复录制状态
	 *               </p>
	 */
	@Override
	public void onBackResult(int siteID, HashMap<String, Object> params) {
		if (siteID == SiteID.VIDEO_PROCESS && params != null) {
			Object obj = params.get("restore");
			if (obj instanceof Boolean && (boolean)obj) {
				isRestore = true;
				@SuppressWarnings("all")
				List<Snippet> snippets = (List<Snippet>)mSite.m_myParams.get("snippet_list");
				int duration = (int)mSite.m_myParams.get("left_duration");
				mSite.m_myParams.clear();
				mSnippets.addAll(snippets);
				mSnippetIndex = mSnippets.size()-1;
				initAllWidgetState();
				mBottomControlView.setSnippets(snippets);
				mProgressView.setSnippets(snippets);
				mRecordManager.setLeftDuration(duration);
				if (!mRecordManager.canRecord()) {
					mBottomControlView.setShutterClickEnabled(false);
				}
			}
		}
	}

	/**
	 * 初始化所有控件的状态
	 */
	@SuppressWarnings("all")
	private void initAllWidgetState() {

		initRecord();

		// 画幅
		if (isAddVideoMode) {
			mSize = mAddVideoSize;
		} else {
			mSize = CameraConfig.getVideoSize();
		}
		mBottomControlView.setSize(mSize);
		mSettingsView.setSize(mSize);
		setPreviewSize(mSize);

		// 视频拍摄需要开启自动对焦
		mCameraWrapper.setAutoLoopFocus(true);
		mCameraWrapper.setSilenceOnTaken(SettingInfoMgr.GetSettingInfo(getContext()).GetCameraSoundState());

		int previewPatchDegree = CameraUtils.getPreviewPatchDegree(mCurrentCameraId); //((360- getPreviewPatchDegree()%360)%360 + 90)%360;//
		mCameraWrapper.setPreviewOrientation(previewPatchDegree); //通过外部获取预览修正角度;

		// 获取镜头闪光灯的配置
		mFlash = CameraConfig.FLASH_OFF;
		mCurrentFlashStr = CameraConfig.getFlashStr(mFlash);
		mCameraWrapper.setFlashMode(mCurrentFlashStr);

		mTopControlView.setFlashMode(mFlash == CameraConfig.FLASH_ON);
		mTopControlView.setFlashEnable(!mCameraWrapper.isFront());
		mTopControlView.setSwitchEnable(mCameraWrapper.getNumberOfCameras() > 1);

		// 视频时长
		mDuration = CameraConfig.getDuration();
		mRecordManager.setMaxVideoDuration(mDuration);
		mBottomControlView.setDuration(mDuration);
		mSettingsView.setDuration(mDuration);

		mCameraLayout.hideFocusView();
		mCameraLayout.setOnTouchListener(this);

		keepScreenWakeUp(true);
	}

	/**
	 * 初始化视频录制
	 */
	private void initRecord() {

		mRecordManager = new RecordManager(getContext());

		int granted = PermissionChecker.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO);
		boolean audioEnable = granted == PermissionChecker.PERMISSION_GRANTED && mRecordManager.isRecordVoiceEnable();
		if (!audioEnable) {
			Toast.makeText(mContext, R.string.camerapage_check_record_permission, Toast.LENGTH_SHORT).show();
		}
		mRecordManager.setVideoSize(mVideoWidth, mVideoHeight);
		mRecordManager.initDefaultPath();
		mRecordManager.setMinVideoDuration(mMinVideoDuration);
		if (mDuration != 0) {
			mRecordManager.setMaxVideoDuration(mDuration);
		}
		mRecordManager.setMessageHandler(mPageHandler);
		mRecordManager.setAudioRecordEnable(audioEnable);
		mRecordManager.setOnInitListener(mOnInitListener);
		mRecordManager.setOnRecordListener(new OnRecordListener() {
			@Override
			public void onPrepare(final MediaMuxerWrapper mediaMuxerWrapper) {
				if (mGLCameraView != null) {
					mGLCameraView.queueEvent(new Runnable() {
						@Override
						public void run() {
							if (mCameraRenderer != null) {
								mCameraRenderer.setMediaMuxerWrapper(mediaMuxerWrapper);
								mCameraRenderer.setRecordState(RecordState.IDLE);
								mCameraRenderer.setRecordState(RecordState.PREPARE);
							}
						}
					});
				}
			}

			@Override
			public void onPrepared() {
				// 不是主线程
				mRecordState = RecordState.PREPARE;
			}

			@Override
			public void onStart(MediaMuxerWrapper mediaMuxerWrapper) {
				if (mGLCameraView != null) {
					mGLCameraView.queueEvent(new Runnable() {
						@Override
						public void run() {
							if (mCameraRenderer != null) {
								mCameraRenderer.setRecordState(RecordState.START);
							}
						}
					});
				}

				mRecordTip.startRecord();
			}

			@Override
			public void onResume() {
				if (mGLCameraView != null) {
					mGLCameraView.queueEvent(new Runnable() {
						@Override
						public void run() {
							if (mCameraRenderer != null) {
								mCameraRenderer.setRecordState(RecordState.RESUME);
							}
						}
					});
				}
			}

			@Override
			public void onProgressChange(float progress) {
				if (mRecordManager != null) {
					int duration = mRecordManager.getLeftDuration();
					float time = progress * duration;
					if (time >= 1000) {
						mBottomControlView.setShutterClickEnabled(true);
					}
					if (isAddVideoMode) {
						mRecordTip.setTime(Math.max(0, (Math.round(mLeftTime - time) / 1000)));
						progress = progress * mRecordManager.getLeftDuration() / mLeftTime;
					} else {
						mRecordTip.setTime(Math.round(time) / 1000);
						progress = progress * mRecordManager.getLeftDuration() / mRecordManager.getMaxVideoDuration();
					}

					mSnippets.get(mSnippetIndex).ratio = progress;
					mBottomControlView.refreshShutterView();
				}
			}

			@Override
			public void onPause() {
				if (mGLCameraView != null) {
					mGLCameraView.queueEvent(new Runnable() {
						@Override
						public void run() {
							if (mCameraRenderer != null) {
								mCameraRenderer.setRecordState(RecordState.PAUSE);
							}
						}
					});
				}
			}

			@Override
			public void onStop(boolean isValid) {

				if (isValid) {
					if (mGLCameraView != null) {
						mGLCameraView.queueEvent(new Runnable() {
							@Override
							public void run() {
								if (mCameraRenderer != null) {
									mCameraRenderer.setRecordState(RecordState.STOP);
								}
							}
						});
					}
				} else {
					deleteSnippet();
					finishRecord();
				}
			}

			@Override
			public void onCompleted(long duration, String filePath) {
				Snippet snippet = mSnippets.get(mSnippetIndex);
				snippet.path = filePath;
				snippet.finish = true;

				finishRecord();
				prepareNextState();
			}
		});
	}

	/**
	 * 准备视频录制
	 *
	 * @param delay 延迟时间
	 */
	private void prepareRecord(final int delay) {
		mPageHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mRecordManager != null) {
					try {
						mRecordManager.prepare();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}, delay);
	}

	/**
	 * 删除片段
	 */
	private void deleteSnippet() {
		mSnippets.remove(mSnippetIndex);
		mSnippetIndex--;
		mBottomControlView.deleteSnippet(mSnippets.isEmpty(), isAddVideoMode);

		float leftDuration;
		if (isAddVideoMode) {
			leftDuration = mLeftTime;
		} else {
			leftDuration = mRecordManager.getMaxVideoDuration() * (1 - getTotalRatio());
		}
		mRecordManager.setLeftDuration(Math.round(leftDuration));
	}

	/**
	 * 结束录制
	 */
	private void finishRecord() {

		isRecording = false;
		setUiEnable(true);

		mProgressView.hideDeleteIcon();
		mProgressView.setAlpha(1);
		mProgressView.setVisibility(VISIBLE);
		mProgressView.addSnippet(mSnippets.get(mSnippetIndex));
		mTopControlView.setVisibility(VISIBLE);
		mRecordTip.stopRecord();

		mRecordState = RecordState.IDLE;

		if (isAddVideoMode && !mSnippets.isEmpty() && mSnippets.get(0).finish) {
			mBottomControlView.setShutterClickEnabled(false);
		}
	}

	/**
	 * 准备下一个状态，继续录制或者录制完成
	 */
	private void prepareNextState() {
		float leftDuration = mRecordManager.getMaxVideoDuration() * (1 - getTotalRatio());
		mRecordManager.setLeftDuration(Math.round(leftDuration));

		if (isAddVideoMode) {
			mBottomControlView.stopRecord(true, false);
		} else {
			mBottomControlView.stopRecord(!mRecordManager.canRecord(), false);
		}

		prepareRecord(100);
	}

	private float getTotalRatio() {
		float ratio = 0;
		for (Snippet snippet : mSnippets) {
			ratio += snippet.ratio;
		}

		return ratio;
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

	@Override
	public void onBack() {

		if (isFirst) {
			mGuidanceView.onHide();
			return;
		}

		if (mUiEnable) {
			if (isShowSettings()) {
				hideSettingsView();
			} else if (mFilterLayout == null || !mFilterLayout.onBack()) {
				if (isShowFilter) {
					hideFilterLayout();
				} else {
					sureBack();
				}
			}
		}
	}

	/**
	 * 确定返回
	 */
	private void sureBack() {

		MyBeautyStat.onClickByRes(R.string.录像页_退出录像页);

		if (mSnippetIndex >= 0) {
			mBackDialog.show();
		} else {
			mSite.onBack(mContext);
		}
	}

	@Override
	public void onResume() {

		if (mIgnoreLifecycle || !isCallPause) {
			return;
		}

		isCallPause = false;

		requestAudioFocus();

		if (!isShowFilter) {
			mProgressView.setAlpha(1);
			mProgressView.setVisibility(VISIBLE);
		}

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

		keepScreenWakeUp(true);

		mCameraLayout.setOnTouchListener(this);

		setUiEnable(true);

		if (mPageHandler != null) {
			mPageHandler.sendEmptyMessageDelayed(MSG_ON_VIEW_RESUME, 1000);
		}

		if (mCameraWrapper != null) {
			mCameraWrapper.setCameraAllCallback(this);
		}

		if (mRecordManager != null) {
			mRecordState = RecordState.IDLE;
			prepareRecord(0);
		}
	}

	@Override
	public void onPause() {

		if (mIgnoreLifecycle) {
			mIgnoreLifecycle = false;
			return;
		}

		isCallPause = true;

		if (mRecordManager != null) {
			mRecordManager.destroy();
		}

		if (isRecording) {
			deleteSnippet();
			mBottomControlView.stopRecord(false, mSnippetIndex == -1);
			mTopControlView.setVisibility(View.VISIBLE);
			mRecordTip.setVisibility(GONE);
			mGLCameraView.queueEvent(new Runnable() {
				@Override
				public void run() {
					mCameraRenderer.onPause();
				}
			});
			isRecording = false;
			setUiEnable(true);
		}

		recycle();

		TongJiUtils.onPagePause(mContext, TAG);
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params) {

		if (siteID == SiteID.WEBVIEW) {
			showCameraPermissionHelper();
		} else if (siteID == SiteID.THEME_PAGE || siteID == SiteID.THEME_INTRO_PAGE) {
			int leftDuration = mRecordManager.getLeftDuration();
			releaseRecordManager();
			initRecord();
			if (isAddVideoMode) {
				mRecordManager.setMaxVideoDuration(CameraConfig.DURATION_180S);
				mRecordManager.setLeftDuration(mLeftTime);
			} else {
				mRecordManager.setMaxVideoDuration(mDuration);
				mRecordManager.setLeftDuration(leftDuration);
			}

			prepareRecord(0);

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

	@Override
	public void onClose() {

		exitImmersiveMode();

		recycle();

		if (mRecordManager != null) {
			mRecordManager.releaseService();
		}

		closeRecordManager();

		if (mGLCameraView != null) {
			mGLCameraView.onDestroy();
			mGLCameraView = null;
		}

		if (mGetFilterTask != null && !mGetFilterTask.isCancelled()) {
			mGetFilterTask.cancel(true);
			mGetFilterTask = null;
		}

		if (mFilterLayout != null) {
			mFilterLayout.release();
			mFilterLayout = null;
		}

		mCameraLayout.release();
		mTopControlView.release();
		mBottomControlView.release();
		mSettingsView.release();
		mRecordTip.release();
		mFilterText.release();
		mProgressView.release();
		mSnippets.clear();

		MyBeautyStat.onPageEndByRes(R.string.录像页);
		TongJiUtils.onPageEnd(mContext, TAG);
	}

	/**
	 * 释放RecordManager
	 */
	private void releaseRecordManager() {
		if (mRecordManager != null) {
			mRecordManager.setMessageHandler(null);
			mRecordManager.setOnRecordListener(null);
			mRecordManager.setOnInitListener(null);
			mRecordManager.destroy();
		}
	}

	private void closeRecordManager() {
		if (mRecordManager != null) {
			mRecordManager.setMessageHandler(null);
			mRecordManager.setOnRecordListener(null);
			mRecordManager.setOnInitListener(null);
			mRecordManager.close();
		}
	}

	@SuppressWarnings("all")
	private void recycle() {

		if (mGLCameraView != null) {
			mGLCameraView.onPause();
		}

		if (mPageHandler != null) {
			mPageHandler.removeCallbacksAndMessages(null);
		}

		keepScreenWakeUp(false);

		if (mCameraWrapper != null) {
			mCameraWrapper.setFlashMode(CameraConfig.getFlashStr(CameraConfig.FLASH_OFF));
			mCameraWrapper.setCameraAllCallback(null);
		}

		mCameraLayout.releaseFocusView();
		mCameraLayout.setOnTouchListener(null);

		mPreviewSize = null;

		if (!isToBeautify) {
			abandonAudioFocus();
		}
	}

	// ------------------------------------ CameraAllCallback -------------------------------------------

	@Override
	public void onScreenOrientationChanged(int orientation, int pictureDegree, float fromDegree, float toDegree) {
		if (!isRecording) {
			mRotation = ((int)toDegree + 360) % 360;
		} else {
			if (toDegree % 180 != 0) {
				if (toDegree == 90) {
					mRecordTip.setRotation(90);
					mRecordTip.setTranslationX(ShareData.m_screenWidth / 2 - mRecordTip.getHeight());
				} else {
					mRecordTip.setRotation(-90);
					mRecordTip.setTranslationX(-ShareData.m_screenWidth / 2 + mRecordTip.getHeight());
				}
				float center = (mFocusArea[1] - mFocusArea[0]) / 2f;
				mRecordTip.setTranslationY(mFocusArea[0] + center - mRecordTip.getWidth() / 2);
			} else {
				mRecordTip.setRotation(0);
				mRecordTip.setTranslationX(0);
				mRecordTip.setTranslationY(0);
			}
		}

		// 当屏幕旋转时，控件也跟随旋转
		if (mTopControlView != null) {
			mTopControlView.setRotate(toDegree);
		}
		if (mBottomControlView != null) {
			mBottomControlView.setRotate(toDegree);
		}
		if (mSettingsView != null) {
			mSettingsView.setRotate(toDegree);
		}
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (!mFocusOrMeteringIsMoving) {
			mPageHandler.removeMessages(MSG_HIDE_FOCUS_AND_METERING_VIEW);
			mPageHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS_AND_METERING_VIEW, 2000);
		}
	}

	@Override
	public void onError(int error, Camera camera) {
		if (mPageHandler != null) {
			mPageHandler.obtainMessage(MSG_CAMERA_USING_ERROR, error, error).sendToTarget();
		}
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {

	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		if (isShowBlack) {
			isShowBlack = false;
			AnimatorUtils.removeView(this, mBlackMask, mDelayTime);
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

	// -------------------------- TopControlView.OnTopControlListener ---------------------------------

	@Override
	public void onClickHome() {
		sureBack();
	}

	@Override
	public void onClickSwitch() {
		isSwitchCamera = true;
		mCameraWrapper.setPreviewSize(mPreviewWidth, mPreviewHeight);
		mGLCameraView.switchCamera();

		mCurrentCameraId = mCameraWrapper.getCurrentCameraId();

		mCameraWrapper.setFlashMode(mCurrentFlashStr);
		mTopControlView.setFlashEnable(!mCameraWrapper.isFront());

		MyBeautyStat.onClickByRes(R.string.录像页_切换至前置);
	}

	@Override
	public void onClickFlash(boolean open) {
		mFlash = open ? CameraConfig.FLASH_ON : CameraConfig.FLASH_OFF;
		mCurrentFlashStr = CameraConfig.getFlashStr(mFlash);
		mCameraWrapper.setFlashMode(mCurrentFlashStr);
	}

	// -------------------------- BottomControlView.OnBottomControlListener ----------------------------

	@Override
	public void onClickShutter() {
		if (isPopupPage) {
			return;
		}

		hideFilterLayout();
		if (isShowSettings()) {
			hideSettingsView();
		}

		if (mRecordManager == null || (!isRecording && (mRecordState != RecordState.PREPARE || !mRecordManager.canRecord()))) {
			return;
		}

		if (!isRecording) {
			mRecordRotation = mRotation;
			if (mSnippetIndex == -1) {
				mFirstVideoRotation = mRecordRotation;
			}
			startRecord();
			setUiEnable(false);
			isRecording = true;

			MyBeautyStat.onClickByRes(R.string.录像页_点击录像按钮);
		} else {
			if (mRecordManager.canStop() && mSnippetIndex < 9) {
				// 结束录制
				stopRecord();
				setUiEnable(true);
				isRecording = false;

				MyBeautyStat.onClickByRes(R.string.录像中状态_停止录像_自由模式);
			}
		}

		TongJi2.AddCountByRes(mContext, R.integer.录像_录像按钮);
	}

	/**
	 * 开始录制视频
	 */
	private void startRecord() {
		if (mRecordState == RecordState.PREPARE) {
			mTopControlView.setVisibility(GONE);

			AnimatorUtils.hideView(mProgressView, 200);
			Snippet snippet = new Snippet();
			snippet.filterItem = mFilterItem;
			snippet.alpha = mAlpha;
			mSnippets.add(snippet);
			mSnippetIndex++;
			mBottomControlView.startRecord();
			mBottomControlView.addSnippet(snippet);
			try {
				mRecordManager.setRecordDegree(mRecordRotation);
				mRecordManager.startRecord();
				mRecordState = RecordState.RECORDING;
			} catch (Exception e) {
				e.printStackTrace();
				mRecordState = RecordState.IDLE;
			}

			if (mRotation % 180 != 0) {
				if (mRotation == 90) {
					mRecordTip.setRotation(90);
					mRecordTip.setTranslationX(ShareData.m_screenWidth / 2 - mRecordTip.getHeight());
				} else {
					mRecordTip.setRotation(-90);
					mRecordTip.setTranslationX(-ShareData.m_screenWidth / 2 + mRecordTip.getHeight());
				}
				float center = (mFocusArea[1] - mFocusArea[0]) / 2f;
				mRecordTip.setTranslationY(mFocusArea[0] + center - mRecordTip.getWidth() / 2);
			} else {
				mRecordTip.setRotation(0);
				mRecordTip.setTranslationX(0);
				mRecordTip.setTranslationY(0);
			}
		}
	}

	/**
	 * 停止录制视频
	 */
	private void stopRecord() {
		mRecordState = RecordState.STOP;
		mRecordManager.stopRecord();
	}

	@Override
	public void onClickFilter() {

		if (isShowFilter || mFilterLayout == null) {
			return;
		}

		if (isShowSettings()) {
			hideSettingsView();
		}

		isShowFilter = true;

		mAnimShutterView.setShow(true);

		AnimatorSet animatorSet = new AnimatorSet();
		ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", ShareData.PxToDpi_xhdpi(222) + mBottomMargin, 0);
		animatorSet.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {

				AnimatorUtils.hideView(mProgressView, 200);

				LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				params.bottomMargin = mBottomMargin;
				addView(mFilterLayout, params);

				mBottomControlView.showShutterView(false);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mBottomControlView.setVisibility(GONE);
				mFilterLayout.setUiEnable(true);
			}
		});
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBottomControlView, "alpha", 1, 0);

		animatorSet.playTogether(animator, mAnimShutterView.getScaleAnimator(false), animator1);
		animatorSet.setDuration(350);
		animatorSet.start();

		MyBeautyStat.onClickByRes(R.string.录像页_打开滤镜列表);
		TongJi2.AddCountByRes(mContext, R.integer.相机_滤镜);
	}

	@Override
	public void onClickSettings() {
		if (!isShowSettings()) {
			AnimatorUtils.addView(this, mSettingsView);
			mBottomControlView.showSettingsView();

			MyBeautyStat.onClickByRes(R.string.录像页_打开镜头设置);
		}
	}

	private boolean isShowSettings() {
		return indexOfChild(mSettingsView) >= 0;
	}

	private void hideSettingsView() {
		AnimatorUtils.removeView(this, mSettingsView, 0);
		mBottomControlView.hideSettingsView();
	}

	@Override
	public void onClickNext() {
		if (isOtherAppCall) {
			saveVideo();
			return;
		}

		if (isAddVideoMode) {
			// 录制成功
			HashMap<String, Object> params = new HashMap<>();
			params.put("snippet", mSnippets.get(0));
			mSite.openProcessVideo(mContext, params);
			return;
		}

		List<Snippet> snippets = new ArrayList<>(mSnippets.size());
		snippets.addAll(mSnippets);
		mSite.m_myParams.put("snippet_list", snippets);
		mSite.m_myParams.put("left_duration", mRecordManager.getLeftDuration());

		HashMap<String, Object> params = new HashMap<>();
		params.put("snippet_list", mSnippets);
		params.put("from_camera", true);

		MyBeautyStat.onClickByRes(R.string.停止录像_确认并进入美化);

		String materialId = "0000";
		int alpha = 0;
		if (mFilterRes != null) {
			materialId = String.valueOf(mFilterRes.m_id);
			alpha = (int)(mAlpha * 12);
		}

		int playRatio = mFirstVideoRotation % 180 != 0 ? PlayRatio.RATIO_16_9 : PlayRatio.RATIO_9_16;

		MyBeautyStat.PictureSize size = MyBeautyStat.PictureSize.PictureSize16_9竖;
		switch (mSize) {
			case CameraConfig.SIZE_1_1:
				size = MyBeautyStat.PictureSize.PictureSize1_1;
				playRatio = PlayRatio.RATIO_1_1;
				break;
			case CameraConfig.SIZE_235_1:
				size = MyBeautyStat.PictureSize.PictureSize235_1;
				playRatio = PlayRatio.RATIO_235_1;
				break;
			case CameraConfig.SIZE_16_9:
				size = MyBeautyStat.PictureSize.PictureSize16_9横;
				playRatio = PlayRatio.RATIO_16_9;
				if (mFirstVideoRotation % 180 != 0) {
					playRatio = PlayRatio.RATIO_9_16;
				}
				break;
		}

		params.put("ratio", playRatio);

		MyBeautyStat.Time time = MyBeautyStat.Time.values()[mDuration - 1];
		MyBeautyStat.onTakeVideo(materialId, alpha, size, MyBeautyStat.Segment.Segmentfree, time, R.string.录像页);

		isToBeautify = true;

		mSite.openProcessVideo(mContext, params);
	}

	private void saveVideo() {
		SaveVideoTask saveVideoTask = new SaveVideoTask(mVideoUri, mOnSaveListener);
		saveVideoTask.execute(mSnippets.toArray(new Snippet[] {}));
	}

	private SaveVideoTask.OnSaveListener mOnSaveListener = new SaveVideoTask.OnSaveListener() {
		@Override
		public void onStart() {
			mSaveDialog.show();
		}

		@Override
		public void onFinish(String path) {

			mSaveDialog.dismiss();

			if (path != null) {
				HashMap<String, Object> params = new HashMap<>();
				params.put("data", path);
				mSite.openProcessVideo(mContext, params);
			} else {
				T.showShort(mContext, "保存视频出错");
			}
		}
	};

	// ---------------------------- SettingsView.OnSettingsListener --------------------------------

	@Override
	public void onClickSize(int size) {
		mSize = size;
		mBottomControlView.setSize(size);
		changePreviewSize(size);
	}

	/**
	 * 设置预览比例
	 *
	 * @param sizeMode 预览比例
	 */
	private void setPreviewSize(int sizeMode) {
		changePreviewSize(sizeMode);
	}

	/**
	 * 改变预览比例
	 *
	 * @param sizeMode 预览比例
	 */
	private void changePreviewSize(int sizeMode) {

		float ratio = 16f / 9;
		int tongjiId = R.integer.录像_画幅_9_16;
		int statId = R.string.镜头设置_16_9竖录像;
		switch (sizeMode) {
			case CameraConfig.SIZE_1_1:
				ratio = 1;
				tongjiId = R.integer.录像_画幅_1_1;
				statId = R.string.镜头设置_1_1录像;
				break;
			case CameraConfig.SIZE_235_1:
				ratio = 1 / 2.35f;
				tongjiId = R.integer.录像_画幅_235_1;
				statId = R.string.镜头设置_235_1录像;
				break;
			case CameraConfig.SIZE_16_9:
				ratio = 9f / 16;
				tongjiId = R.integer.录像_画幅_16_9;
				statId = R.string.镜头设置_16_9横录像;
				break;
			case CameraConfig.SIZE_9_16:
				ratio = 16f / 9;
				tongjiId = R.integer.录像_画幅_9_16;
				statId = R.string.镜头设置_16_9竖录像;
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

				if (mFilterText.getHeight() == 0) {
					mFilterText.post(new Runnable() {
						@Override
						public void run() {
							float center = (mFocusArea[1] - mFocusArea[0]) / 2f;
							mFilterText.setTranslationY(mFocusArea[0] + center - mFilterText.getHeight() / 2f + mTopMargin);
						}
					});
				} else {
					float center = (mFocusArea[1] - mFocusArea[0]) / 2f;
					mFilterText.setTranslationY(mFocusArea[0] + center - mFilterText.getHeight() / 2f + mTopMargin);
				}

				changeVideoSizeRatio(ratio);
			}
		}

		CameraConfig.setVideoSize(mSize);
	}

	/**
	 * 改变视频预览比例
	 *
	 * @param ratio 比例
	 */
	public void changeVideoSizeRatio(float ratio) {
		if (mRecordManager != null) {
			mRecordManager.destroy();
			if (isSupportHD) {
				mVideoWidth = 1080;
				mVideoHeight = 1920;
			} else {
				mVideoWidth = 720;
				mVideoHeight = 1280;
			}

			mVideoHeight = (int)(ratio * mVideoWidth + 0.5f);
			mRecordManager.setVideoSize(mVideoWidth, mVideoHeight);

			prepareRecord(0);

			if (mGLCameraView != null && mFocusArea != null && mFocusArea.length > 1) {
				mGLCameraView.changeRatio(mFocusArea[0], mFocusArea[1]);
			}
		}
	}

	@Override
	public void onClickDuration(int duration) {
		mDuration = duration;

		if (mRecordManager != null) {
			mRecordManager.setMaxVideoDuration(mDuration);
		}
		mBottomControlView.setDuration(mDuration);

		switch (mDuration) {
			case CameraConfig.DURATION_10S:
				MyBeautyStat.onClickByRes(R.string.镜头设置_10s);
				TongJi2.AddCountByRes(mContext, R.integer.录像_时长_10);
				break;
			case CameraConfig.DURATION_30S:
				MyBeautyStat.onClickByRes(R.string.镜头设置_30s);
				TongJi2.AddCountByRes(mContext, R.integer.录像_时长_30);
				break;
			case CameraConfig.DURATION_60S:
				MyBeautyStat.onClickByRes(R.string.镜头设置_60s);
				TongJi2.AddCountByRes(mContext, R.integer.录像_时长_60);
				break;
			case CameraConfig.DURATION_180S:
				MyBeautyStat.onClickByRes(R.string.镜头设置_180s);
				break;
		}

		CameraConfig.setDuration(duration);
	}

	// ----------------------------- ProgressView.OnClickDeleteListener ----------------------------

	@Override
	public void onClickDelete() {
		deleteSnippet();
	}

	// ----------------------------- GetFilterTask.OnGetFilterListener -----------------------------

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
		} else {
			for (int i = 0; i < mFilterDatas.size(); i++) {
				info = mFilterDatas.get(i);
				if (info.m_uri != DragListItemInfo.URI_MGR && info.m_uri != DragListItemInfo.URI_ORIGIN && !info.m_isLock && info.m_style == DragListItemInfo.Style.NORMAL) {
					tempInfo = info;
					mFilterIndex = i;
					mCurFilterUri = info.m_uri;
					break;
				}
			}
		}

		if (tempInfo != null) {
			ensureFilter(tempInfo);
		}
	}

	// ------------------------------ FilterLayout.OnFilterControlListener -------------------------

	@Override
	public void onDownloadMore() {
		if (mUiEnable) {
			if (mGLCameraView != null) {
				mGLCameraView.onPause();
			}

			HashMap<String, Object> params = new HashMap<>();
			params.put("type", ResType.FILTER);
			params.put("typeOnly", true);
			mSite.onDownloadMore(mContext, params);
		}
	}

	/**
	 * 收起滤镜弹层
	 */
	private void hideFilterLayout() {

		onClickDown();
	}

	@Override
	public void onClickDown() {
		if (!isShowFilter) {
			return;
		}

		mFilterLayout.setUiEnable(false);

		AnimatorSet animatorSet = new AnimatorSet();

		ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", 0, ShareData.PxToDpi_xhdpi(222) + mBottomMargin);
		animatorSet.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {
				mBottomControlView.setVisibility(VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (!isRecording) {
					AnimatorUtils.showView(mProgressView, 200);
				}
				mBottomControlView.showShutterView(true);

				mAnimShutterView.setShow(false);
				removeView(mFilterLayout);
				isShowFilter = false;

				if (mFilterLayout != null) {
					mFilterLayout.resetPosition();
				}
			}
		});
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBottomControlView, "alpha", 0, 1);
		animatorSet.playTogether(animator, mAnimShutterView.getScaleAnimator(true), animator1);
		animatorSet.setDuration(350);
		animatorSet.start();
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

	// -------------------------------------- View.OnTouchListener ---------------------------------

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

					mFocusOrMeteringIsMoving = false;

					if (!isValidArea(y)) {
						return true;
					}

					hideFilterLayout();
					if (isShowSettings()) {
						hideSettingsView();
					}

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
	 * 判断是否位于可对焦区域
	 */
	private boolean isValidArea(float position) {
		return position > mFocusArea[0] && position < mFocusArea[1];
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
		if (isShowSettings()) {
			hideSettingsView();
		}
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

	@SuppressWarnings("all")
	private class PageHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_HANDLE_FOCUS_AND_METERING:
					if (!mFocusOrMeteringIsMoving && mCameraWrapper != null) {

						float[] positions = mCameraLayout.getFoucuAndMeterPosition();
						if (positions != null && positions.length == 4) {
							mCameraWrapper.setFocusAndMeteringArea(positions[0], positions[1], positions[2], positions[3], mFocusRatio <= 0 ? 1.0f : mFocusRatio);
						}

						mPageHandler.sendEmptyMessageDelayed(MSG_HIDE_FOCUS_AND_METERING_VIEW, 2000);
					}
					break;
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
										setUiEnable(false);
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
//						mTouchType = TOUCH_NONE;
					}
					break;
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

	private MediaVideoEncoder.OnInitListener mOnInitListener = new MediaVideoEncoder.OnInitListener() {

		@Override
		public void onError(Throwable e) {
			if (!isOtherAppCall) {
				if (isSupportHD) {
					T.showShort(mContext, "不支持该比例的高清模式");

					releaseRecordManager();

					mVideoWidth = 720;
					mVideoHeight = 1280;

					isSupportHD = false;
				} else {

					if (mSize != CameraConfig.SIZE_9_16) {
						T.showShort(mContext, "不支持该比例");

						releaseRecordManager();

						mSize = CameraConfig.SIZE_9_16;
					} else {
						T.showShort(mContext, "镜头发生异常");
						mSite.onBack(mContext);
					}
				}

				initRecord();

				setPreviewSize(mSize);
			}
		}
	};

	/**
	 * 请求音频焦点
	 */
	@SuppressWarnings("all")
	private void requestAudioFocus() {
		isMusicActive = mAudioManager.isMusicActive();
		if (isMusicActive) {
			int result = mAudioManager.requestAudioFocus(mListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

			if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
				// 请求成功
			} else {
				// 请求失败
			}
		}
	}

	/**
	 * 放弃音频焦点
	 */
	private void abandonAudioFocus() {
		if (isMusicActive) {
			mAudioManager.abandonAudioFocus(mListener);
		}
	}

	private static class MyOnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

		@Override
		public void onAudioFocusChange(int focusChange) {
			// 被其他App切换时，把当前自己的音乐停止
		}
	}
}
