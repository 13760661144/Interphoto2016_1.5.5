package cn.poco.video.page;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.beautify.BeautifyResMgr;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.resource.FilterRes;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.InterphotoDlg;
import cn.poco.video.NativeUtils;
import cn.poco.video.VideoResMgr;
import cn.poco.video.frames.VideoPage;
import cn.poco.video.process.ThreadPool;
import cn.poco.video.render.GLVideoView;
import cn.poco.video.render.view.ProgressView;
import cn.poco.video.save.SaveParams;
import cn.poco.video.sequenceMosaics.TransitionDataInfo;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.site.VideoBeautifySite;
import cn.poco.video.site.VideoMusicSite;
import cn.poco.video.site.VideoTextSite;
import cn.poco.video.utils.AndroidUtil;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.videoAlbum.VideoAlbumUtils;
import cn.poco.video.videoFeature.VideoBottomPage;
import cn.poco.video.videoFeature.VideoBottomSite;
import cn.poco.video.videoFilter.FilterBottomView;
import cn.poco.video.videoFilter.VideoFilterManager;
import cn.poco.video.videoFilter.VideoFilterPage;
import cn.poco.video.videoFilter.VideoFilterSite;
import cn.poco.video.videoMusic.VideoMusicPage;
import cn.poco.video.videotext.VideoTextCallbackAdapter;
import cn.poco.video.videotext.VideoTextPage;
import cn.poco.video.videotext.text.VideoTextView;
import cn.poco.video.view.ActionBar;


/**
 * Created by Shine on 2017/5/27.
 */

public class VideoBeautifyPage extends IPage {
	private static final String TAG = "视频美化页";

	private VideoModeWrapper mVideoModeWrapper = new VideoModeWrapper();
	private VideoPage mActivePage;

	// UI布局
	protected ActionBar mActionBar;
	protected GLVideoView mVideoView;
	protected LinearLayout mBottomView;
	private View mCurrentBottomView;
	private VideoBottomPage mVideoBottomPage;
	private FrameLayout mShowFrame;
	private InterphotoDlg mBackDialog;
	private ShareVideoView shareVideoView;
	private ProgressView mProgressView;

	protected Context mContext;
	private ArrayList<TransitionDataInfo> mTrans;

	protected VideoBeautifySite mSite;

	//水印页面
	protected VideoTextView mTextView;

	//音乐
	protected MediaPlayer mMusicPlayer;

    protected ImageView mPlayBtn;

	// 视频帧画面的毛玻璃
	protected Bitmap mBitmap;

//	/**
//	 * 分享到在一起会触发 onPause -> onResume -> onPause
//	 */
//	private boolean isSharingToCircle = false;


	private AudioManager mAudioManager;
	private boolean isMusicActive;
	private MyOnAudioFocusChangeListener mListener;

	/**
	 * 是否退出了应用
	 */
	private boolean isExitApp = false;

	private TextView mProgressTip;

	// 标记来自素材商店的流程
	private boolean mOpenFromResourceShop;
	// 标记来自摄像头的流程
	private boolean isFromCamera = false;

	/**
	 * 屏幕亮度
	 */
	private boolean mIsKeepScreenOn;

	public VideoBeautifyPage(Context context, BaseSite site) {
		super(context, site);
		mSite = (VideoBeautifySite)site;
		mContext = context;
		// 初始化数据
		initData();
        // 初始化页面布局
		initView();
		// 添加相关统计
		TongJiUtils.onPageStart(context, TAG);
		MyBeautyStat.onPageStartByRes(R.string.视频美化页);
		// 清理视频底层接口残留的数据
		NativeUtils.cleanVideoGroupByIndex(0);
	}



	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mVideoModeWrapper.mUiEnable) {
			return true;
		} else {
			return super.onInterceptTouchEvent(ev);
		}
	}

	protected int mVideoHeight;
	/**
	 * 初始化相关布局数据
	 */
	private void initData() {
		mVideoModeWrapper.mActionBarHeight = ShareData.PxToDpi_xhdpi(80);
        mVideoHeight = mVideoModeWrapper.mVideoHeight = ShareData.m_screenWidth;

//        mVideoHeight = ShareData.m_screenHeight - mVideoModeWrapper.mActionBarHeight - ShareData.PxToDpi_xhdpi(480);
		//设计要一比一区域
		mVideoModeWrapper.mVideoInfos = new ArrayList<>();

		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mListener = new MyOnAudioFocusChangeListener();
        AndroidUtil.init();
	}

	/**
	 * 开始页面布局
	 */
	protected void initView() {
		this.setBackgroundColor(0xff0e0e0e);

		LayoutParams params;
		mVideoView = new GLVideoView(mContext);
		mVideoView.setBackgroundColor(Color.BLACK);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mVideoHeight);
		params.topMargin = mVideoModeWrapper.mActionBarHeight;
		addView(mVideoView, params);
		mVideoView.setOnClickListener(mOnClickListener);


		mShowFrame = new FrameLayout(getContext());
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mShowFrame, params);
		this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				VideoBeautifyPage.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				initTextAndProcessView();
				setModeWrapper();
				if (mOpenFromResourceShop) {
					openWatermarkFrame(mWatermarkType, mWatermarkId);
					mOpenFromResourceShop = false;
				}
			}
		});

        // 添加播放按钮
        mPlayBtn = new ImageView(mContext);
        mPlayBtn.setImageResource(R.drawable.video_play);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
//        params.topMargin = mVideoHeight / 2;
        params.bottomMargin = ShareData.m_screenHeight/2 - mVideoModeWrapper.mActionBarHeight - mVideoModeWrapper.mVideoHeight/2;
		addView(mPlayBtn, params);
		mPlayBtn.setVisibility(View.INVISIBLE);

		mActionBar = new ActionBar(mContext, 0xff0e0e0e);
		mActionBar.setClickable(true);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mVideoModeWrapper.mActionBarHeight);
		addView(mActionBar, params);

		mProgressTip = new TextView(mContext);
		mProgressTip.setTextColor(Color.WHITE);
//		mProgressTip.setBackgroundColor(0x4c000000);
		mProgressTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		mProgressTip.setPadding(ShareData.PxToDpi_xhdpi(30), 0, ShareData.PxToDpi_xhdpi(30), 0);
//		mProgressTip.setLineSpacing(ShareData.PxToDpi_xhdpi(1),1f);
		mProgressTip.setGravity(Gravity.CENTER);
		mProgressTip.setVisibility(INVISIBLE);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL);
		params.topMargin = mVideoModeWrapper.mActionBarHeight + mVideoModeWrapper.mVideoHeight - ShareData.PxToDpi_xhdpi(60);
//		params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
//		params.bottomMargin = ShareData.PxToDpi_xhdpi(509);

		addView(mProgressTip,params);

//		mVideoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//			@Override
//			public void onGlobalLayout() {
//				mVideoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//
//				int height = mVideoView.getSurfaceHeight();
//				int top = mVideoView.getSurfaceTop();
//
//				LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(64));
//				params.topMargin = mVideoModeWrapper.mActionBarHeight + top + height - ShareData.PxToDpi_xhdpi(124);
//				params.gravity = Gravity.CENTER_HORIZONTAL;
//				mProgressTip.setLayoutParams(params);
//			}
//		});

		setModeWrapper();

		mBottomView = new LinearLayout(mContext);
		mBottomView.setGravity(Gravity.BOTTOM);
		mBottomView.setOrientation(LinearLayout.VERTICAL);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.LEFT | Gravity.BOTTOM);
		addView(mBottomView, params);

		mVideoBottomPage = new VideoBottomPage(mContext, mVideoBottomSite, mVideoModeWrapper);
		mVideoBottomPage.setCallback(new VideoBottomPage.VideoBottomViewCallback() {
			@Override
			public boolean canClick() {
				if (mVideoModeWrapper.isDragProgress) {
					return false;
				} else {
					return true;
				}
			}

			@Override
			public void onClickFeature(int index) {
				ProcessMode mode = ProcessMode.Normal;
				if (index == 0) {
					mode = ProcessMode.Fileter;
				} else if (index == 1) {
					mode = ProcessMode.Watermark;
				} else if (index == 2) {
					mode = ProcessMode.Music;
					initMusicPlayer();
				}
				openSpecifyModule(mode);

			}
		});

		mActivePage = mVideoBottomPage;
//		LinearLayout.LayoutParams paramsBottomView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mActivePage.getBottomPartHeight());
		LinearLayout.LayoutParams paramsBottomView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mVideoBottomPage.setLayoutParams(paramsBottomView);
		mBottomView.addView(mVideoBottomPage);
		mCurrentBottomView = mVideoBottomPage;

        mVideoModeWrapper.initResource(mContext);
		initBackDialog();
	}
	private ObjectAnimator objectAnimator;

	private void shareVideoViewAnim(View view){
		int translationY = ShareData.PxToDpi_xhdpi(444);
		objectAnimator = ObjectAnimator.ofFloat(view, "translationY", translationY, 0);
		objectAnimator.setDuration(1000);

	objectAnimator.addListener(new AnimatorListenerAdapter() {
		@Override
		public void onAnimationStart(Animator animation) {
			shareVideoView.setVisibility(VISIBLE);
		}
	});
		objectAnimator.start();

	}

	/**
	 * 设置logo
	 */
	private void setVideoLogo(SaveParams params) {
		params.logoPath = null;
		String videoLogo = SettingInfoMgr.GetSettingInfo(mContext).getVideoLogo();
		if (videoLogo != null) {
			File file = new File(videoLogo);
			if (file.exists()) {
				params.logoPath = videoLogo;
			}
		}

		if (params.logoPath == null) {
			params.videoLogo = getVideoLogo(videoLogo);
		}
	}

	/**
	 * 获取水印logo的资源id
	 */
	private int getVideoLogo(String logoString) {
		int logoIndex = logoString == null ? 0 : -1;
		try {
			logoIndex = Integer.valueOf(logoString);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		if (logoIndex == -1) {
			return 0;
		}

		int resId = 0;
		switch (logoIndex) {
			case 0:
				resId = R.drawable.interphpoto_logo_1;
				break;
			case 1:
				resId = R.drawable.interphpoto_logo_2;
				break;
			case 2:
				resId = R.drawable.interphpoto_logo_3;
				break;
			case 3:
				resId = R.drawable.interphpoto_logo_4;
				break;
			case 4:
				resId = R.drawable.interphpoto_logo_5;
				break;
			case 5:
				resId = R.drawable.interphpoto_logo_6;
				break;
			case 6:
				resId = R.drawable.interphpoto_logo_7;
				break;
		}

		return resId;
	}

	private void setModeWrapper()
	{
		mVideoModeWrapper.mActionBar = mActionBar;
		mVideoModeWrapper.mVideoView = mVideoView;
		mVideoModeWrapper.mTextView = mTextView;
		mVideoModeWrapper.mPlayBtn = mPlayBtn;
		mVideoModeWrapper.mProgressTip = mProgressTip;
		mVideoModeWrapper.mProgressView = mProgressView;
		if(mVideoView != null){
			mVideoView.addOnPlayListener(mVideoModeWrapper.mOnPlayListener);
			mVideoView.addOnProgressListener(mVideoModeWrapper.mOnProgressListener);
		}
		if(mProgressView != null){
			mProgressView.setOnSeekBarChangeListener(mVideoModeWrapper.mOnSeekBarChangeListener);

		}
	}

	protected void initTextAndProcessView() {
		if (mTextView == null) {
			LayoutParams params;
			mTextView = new VideoTextView(getContext(), mVideoView.getSurfaceWidth(), mVideoView.getSurfaceHeight()) {
				@Override
				public boolean onTouchEvent(MotionEvent event) {
				    // 移除进度条点击逻辑
//                    float touchX = event.getRawX();
//					float touchY = event.getRawY();
//					Rect progressBarPosition = mVideoView.getProgressBarLoc();
//					boolean isTouchProgressView = progressBarPosition.contains((int)touchX, (int)touchY);
//					if (isTouchProgressView) {
//						return false;
//					} else {
//						return super.onTouchEvent(event);
//					}
					return super.onTouchEvent(event);
				}
			};
			mTextView.def_edit_res = R.drawable.video_text_edit;
            mTextView.InitData(new VideoTextCallbackAdapter());
            mTextView.setClickable(false);
            mTextView.SetOperateMode(VideoTextView.MODE_IMAGE);
            mTextView.CreateViewBuffer();
            params = new FrameLayout.LayoutParams(mVideoView.getSurfaceWidth(), mVideoView.getSurfaceHeight());
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.topMargin = mVideoView.getSurfaceTop() + mVideoModeWrapper.mActionBarHeight;
            mTextView.setLayoutParams(params);
            mShowFrame.addView(mTextView);
		}
		if(mProgressView == null){
			int h = ShareData.PxToDpi_xhdpi(48);
			LayoutParams params;
			mProgressView = new ProgressView(mContext);
			mProgressView.setVisibility(View.GONE);
			mProgressView.setEnabled(false);
			params = new LayoutParams(mVideoView.getSurfaceWidth()+h, h);
			params.topMargin = mVideoView.getSurfaceTop()+mVideoView.getSurfaceHeight() + mVideoModeWrapper.mActionBarHeight - h/2;
			params.gravity = Gravity.CENTER_HORIZONTAL;
			mShowFrame.addView(mProgressView, params);
		}
	}

	/**
	 * 初始化音乐播放器
	 */
	protected void initMusicPlayer()
	{
		if(mMusicPlayer == null){
			mMusicPlayer = new MediaPlayer();
			mVideoModeWrapper.mMusicPlayer = mMusicPlayer;
			mMusicPlayer.setVolume(mVideoModeWrapper.mMusicSaveInfo.mMaxVolume,mVideoModeWrapper.mMusicSaveInfo.mMaxVolume);
			mMusicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.seekTo(mVideoModeWrapper.mMusicSaveInfo.mMusicStartTime);
					if(!mp.isPlaying()) {
						mp.start();
					}
				}
			});
		}
	}
//	//缩小视频画面
//	private void shrinkVideoView(View view) {
//
//		int viewW = view.getWidth();
//		int viewH = view.getHeight();
//		int fixed = ShareData.PxToDpi_xhdpi(640);
//		float ratio = 0;
//		if (viewH > viewW) {
//			ratio = fixed * 1f / viewH;
//		} else {
//			ratio = fixed * 1f / viewW;
//		}
//		int translationY = ShareData.PxToDpi_xhdpi(20);
//		AnimatorSet animatorSet = new AnimatorSet();
//		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, ratio);
//		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, ratio);
//		ObjectAnimator translaY = ObjectAnimator.ofFloat(view, "translationY", 0, -translationY);
//
//		animatorSet.setDuration(500);
//		//animatorSet.setInterpolator(new DecelerateInterpolator());
//		//animatorSet.play(scaleX).with(scaleY);
//		animatorSet.play(scaleX).with(scaleY).with(translaY);
//		animatorSet.start();
//
//	}

	/**
	 * 更新顶部bar的标题和按钮
	 * @param actionbarResId 右边按钮的资源文件id
	 */
	private void updateActionbarInfo(int actionbarResId) {
		if (mVideoModeWrapper.getCurrentMode() == ProcessMode.Edit) {
			mActionBar.setUpActionbarTitle(mContext.getString(R.string.footage), Color.WHITE, 16);
		} else if (mVideoModeWrapper.getCurrentMode() == ProcessMode.Fileter) {
			mActionBar.setUpActionbarTitle(mContext.getString(R.string.Filters), Color.WHITE, 16);
		} else if (mVideoModeWrapper.getCurrentMode() == ProcessMode.Music) {
			mActionBar.setUpActionbarTitle(mContext.getString(R.string.Music), Color.WHITE, 16);
		} else if (mVideoModeWrapper.getCurrentMode() == ProcessMode.Watermark) {
			mActionBar.setUpActionbarTitle(mContext.getString(R.string.video_text), Color.WHITE, 16);
		} else {
			mActionBar.setUpActionbarTitle("");
		}
		mActionBar.setUpRightImageBtn(actionbarResId);
	}

	private void setActivePage(VideoPage activePage) {
		this.mActivePage = activePage;
	}


	/**
	 * 设置参数
	 *
	 * @param params videos ArrayList<VideoEntry>
	 *               ratio (int) 视频比例
	 *               from_camera (boolean) 是否来自镜头
	 */
	@Override
	public void SetData(HashMap<String, Object> params) {
		VideoAlbumUtils.enterImmersiveMode(getContext());
		if (params != null) {
			// 从视频相册跳转过来的流程
			Object obj;
			ArrayList<String> pathList = new ArrayList<>();
			obj = params.get("videos");
			List<VideoInfo> videoInfos = null;
			if(obj != null)
			{
				videoInfos = (List<VideoInfo>)obj;
				mVideoModeWrapper.mVideoInfos.clear();
				mVideoModeWrapper.mVideoInfos.addAll(videoInfos);
				for(VideoInfo videoInfo : videoInfos)
				{
					pathList.add(videoInfo.mClipPath);
				}
			}
			obj = params.get("ratio");
			if(obj != null){
				mVideoModeWrapper.mPlayRatio = (int) obj;
			}
			mVideoView.setPlayRatio(mVideoModeWrapper.mPlayRatio);
			mVideoView.setVideoPaths(pathList.toArray(new String[] {}));

			// 从镜头跳转过来的流程
			obj = params.get("from_camera");
			if (obj instanceof Boolean) {
				isFromCamera = (boolean) obj;
				mVideoModeWrapper.isCanSave = isFromCamera;
				boolean theSameFilter = true;
				if (videoInfos != null) {
					for (int i = 0; i < videoInfos.size(); i++) {
					    if (videoInfos.get(i).mFilterUri != -1) {
							FilterRes filterRes = VideoResMgr.getFilterRes(mContext, videoInfos.get(i).mFilterUri);
							if (filterRes != null) {
								mVideoModeWrapper.mVideoView.changeFilter(i, filterRes);
							}
						}
						for (int j = i + 1; j < videoInfos.size(); j++) {
							int uri = videoInfos.get(i).mFilterUri;
							int nextUri = videoInfos.get(j).mFilterUri;
							if (uri != nextUri) {
								theSameFilter = false;
							}
						}
					}
				}
				mVideoModeWrapper.mIsParticialFilterModify = !theSameFilter;
			}

			if (!isFromCamera) {
				obj = params.get("watermark_type");
				if (obj instanceof Integer) {
					mWatermarkType = (Integer) obj;
				}

				obj = params.get("watermark_id");
				if (obj instanceof Integer) {
					mWatermarkId  = (Integer) obj;
					mOpenFromResourceShop = true;
				}
			}
			requestAudioFocus();
			mActivePage.SetData(params);
		}
		keepScreenWakeUp(true);
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
	public void onResume() {
		requestAudioFocus();
		TongJiUtils.onPageResume(mContext, TAG);
		if (mActivePage != null) {
			mActivePage.onResume();
		}

		keepScreenWakeUp(true);
	}

	@Override
	public void onBack() {
		mActivePage.onBack();
	}

	/**
	 * 从功能子页面返回到主页面
	 */
	private void backFromSubPage() {
		mVideoModeWrapper.setCurrentMode(ProcessMode.Normal);
		int visibleHeight = mActivePage.getBottomPartHeight();
		int oldHeight = ShareData.m_screenHeight - mVideoModeWrapper.mActionBarHeight - visibleHeight;
		int animationStart = visibleHeight;

		final VideoPage oldActivePage = mActivePage;
		setActivePage(mVideoBottomPage);
		mVideoBottomPage.setIsActivePage(true);
		int newHeight = ShareData.m_screenHeight - mVideoModeWrapper.mActionBarHeight - mActivePage.getBottomPartHeight();

		startTransitionAnimtion(false, oldHeight, newHeight, 0, animationStart, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mBottomView.removeView(mCurrentBottomView);
                oldActivePage.onClose();
				mCurrentBottomView = mVideoBottomPage;
				ViewParent parent = mVideoBottomPage.getParent();
				if (parent != null) {
					ViewGroup viewGroup = (ViewGroup)parent;
					viewGroup.removeView(mVideoBottomPage);
				}
				mBottomView.addView(mVideoBottomPage);
				updateActionbarInfo(R.drawable.framework_video_save);
			}
		});
	}

	private void backToAlbumPage()
	{
		mSite.onBack(getContext(),mVideoModeWrapper.hasSave | mVideoModeWrapper.isModify, !mVideoModeWrapper.isModify);
	}

	/**
	 * 从主页面退出
	 */
	private void backFromMainPage() {
		TongJi2.AddCountByRes(mContext, R.integer.视频_返回);
		MyBeautyStat.onClickByRes(R.string.视频美化页_退出视频美化页);

		if (mVideoModeWrapper.isModify) {
            mVideoModeWrapper.pauseAll();
			mBackDialog.show();
		} else {
//			HashMap<String ,Object> params = new HashMap<>();
//			if(!mVideoModeWrapper.hasSave)
//			{
//				params.put("restore",true);
//			}
			backToAlbumPage();
		}
	}

	@Override
	public void onPause() {
		abandonAudioFocus();
		TongJiUtils.onPagePause(mContext, TAG);
		if (mActivePage != null) {
		    if (mActivePage != mVideoBottomPage) {
				mActivePage.onPause();
			} else {
				mVideoModeWrapper.onPauseAll();
			}
		}

		keepScreenWakeUp(false);
	}


	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return mActivePage.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClose() {
		VideoAlbumUtils.exitImmersiveMode(getContext());
		keepScreenWakeUp(false);

		TongJiUtils.onPageEnd(mContext, TAG);
		MyBeautyStat.onPageEndByRes(R.string.视频美化页);
		mVideoModeWrapper.onClose();
		ThreadPool.getInstance().release();

		if (mActivePage != null) {
			mActivePage.onClose();
		} else{
			abandonAudioFocus();
			if (mVideoView != null) {
				isExitApp = true;
				mVideoView.release();
			}
			FileUtils.clearTempFiles();
		}
		mOnClickListener = null;
		FileUtils.clearHiddenSrcVideoFile();

		if (VideoFilterManager.getsInstance() != null) {
		    VideoFilterManager.getsInstance().clear();
		}

		// 去除记住的滤镜
		FilterBottomView.s_selUri = -1;
//        VideoResMgr.clearData();
		AndroidUtil.clear();
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params) {
		mActivePage.onPageResult(siteID, params);
	}


	private int mVideoStartPosition;
	private int mVideoEndPosition;
	private float mLastStartScale = 1.0f;
	private float mLastEndScale;
    private float mAlphaStart = 0.0f;
	private float mAlphaEnd = 1.0f;
	private float mConstantAlpha = 1.0f;
    private int mMoveDistance;
	private boolean mHasScale;

	private void startTransitionAnimtion(boolean isAnimationIn, int oldHeight, int newHeight, int bottomStartPosition, int bottomEndPosition, AnimatorListenerAdapter callback) {
		int videoHeightOld = oldHeight;
		int videoNewHeight = newHeight;
        int surfaceHeight = mVideoView.getSurfaceHeight();
		int surfaceTopAfterAnimation = (videoNewHeight - surfaceHeight) / 2 ;
		int progressTipStart, progressTipEnd;

        ObjectAnimator scaleX = null, scaleY = null;
		ObjectAnimator playBtnScaleX = null, playBtnScaleY = null;

		// 进入裁剪、滤镜时
        if (isAnimationIn) {
			mMoveDistance = (videoNewHeight - videoHeightOld) / 2 ;
			boolean noScale = surfaceTopAfterAnimation > 0;

			if (noScale) {
				// 1.不用缩放，可以直接平移
				mHasScale = false;
			} else {
				// 既要缩放，也要平移
				float scaleRate = (videoNewHeight) / (float)videoHeightOld;
				mHasScale = true;
				mLastStartScale = 1;
				mLastEndScale = scaleRate;

				scaleX = ObjectAnimator.ofFloat(mVideoView, "scaleX", mLastStartScale, mLastEndScale);
				scaleY = ObjectAnimator.ofFloat(mVideoView, "scaleY", mLastStartScale, mLastEndScale);

				playBtnScaleX = ObjectAnimator.ofFloat(mPlayBtn, "scaleX", mLastStartScale, mLastEndScale);
				playBtnScaleY = ObjectAnimator.ofFloat(mPlayBtn, "scaleY", mLastStartScale, mLastEndScale);
                mLastStartScale = mLastEndScale;
				mMoveDistance = (int)(mMoveDistance * scaleRate);
			}
			progressTipStart = 0;
			progressTipEnd = mVideoView.getSurfaceWidth() == ShareData.m_screenWidth ? mMoveDistance : mMoveDistance * 2;
		} else {
            // 退出裁剪、滤镜等
			progressTipEnd = 0;
			progressTipStart = mVideoView.getSurfaceWidth() == ShareData.m_screenWidth ? mMoveDistance : mMoveDistance * 2;

			if (mHasScale) {
				mLastEndScale = 1;
				scaleX = ObjectAnimator.ofFloat(mVideoView, "scaleX", mLastStartScale, mLastEndScale);
				scaleY = ObjectAnimator.ofFloat(mVideoView, "scaleY", mLastStartScale, mLastEndScale);

				playBtnScaleX = ObjectAnimator.ofFloat(mVideoView, "scaleX", mLastStartScale, mLastEndScale);
				playBtnScaleY = ObjectAnimator.ofFloat(mVideoView, "scaleY", mLastStartScale, mLastEndScale);
			}
		}

		mVideoStartPosition = isAnimationIn ? 0 : mMoveDistance;
		mVideoEndPosition = isAnimationIn ? mMoveDistance : 0;

		ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(mVideoView, "translationY", mVideoStartPosition, mVideoEndPosition);
		ObjectAnimator playBtnTranslationAnimator = ObjectAnimator.ofFloat(mPlayBtn, "translationY", mVideoStartPosition, mVideoEndPosition);
		ObjectAnimator progressTipAnimator = ObjectAnimator.ofFloat(mProgressTip, "translationY", mVideoStartPosition, mVideoEndPosition);
		ObjectAnimator progressViewAnimator = ObjectAnimator.ofFloat(mProgressView, "translationY", mVideoStartPosition, mVideoEndPosition);

		ObjectAnimator bottomViewTranslation = ObjectAnimator.ofFloat(mCurrentBottomView, "translationY", bottomStartPosition, bottomEndPosition);
		ObjectAnimator bottomViewTranslation2 = ObjectAnimator.ofFloat(mVideoBottomPage, "translationY", bottomEndPosition, bottomStartPosition);
		ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mActionBar.getLeftImageBtn(), "alpha", mAlphaStart, mAlphaEnd, mConstantAlpha);
		ObjectAnimator alphaAnimator2 = ObjectAnimator.ofFloat(mActionBar.getRightImageBtn(), "alpha", mAlphaStart, mAlphaEnd, mConstantAlpha);
		ObjectAnimator alphaAnimator3 = ObjectAnimator.ofFloat(mActionBar.getTitleView(), "alpha", mAlphaStart, mAlphaEnd);

		AnimatorSet animatorSet = new AnimatorSet();
        // 播放所有动画
        if (mHasScale) {
			animatorSet.playTogether(translationAnimator, playBtnTranslationAnimator, scaleX, scaleY, playBtnScaleX, playBtnScaleY, bottomViewTranslation, bottomViewTranslation2, alphaAnimator, alphaAnimator2, alphaAnimator3, progressTipAnimator);
			scaleY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					float s = (float) animation.getAnimatedValue();
					if (mTextView != null) {
						mTextView.setViewScale(s);
					}
				}
			});
		} else {
			//播放所有动画,除去缩放动画
			animatorSet.playTogether(translationAnimator, playBtnTranslationAnimator, bottomViewTranslation, bottomViewTranslation2, alphaAnimator, alphaAnimator2, alphaAnimator3, progressTipAnimator,progressViewAnimator);
		}
		translationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float y = (float) animation.getAnimatedValue();
				if (mTextView != null ) {
					mTextView.setTranslationY(y);
				}
			}
		});

		animatorSet.setDuration(200);
		animatorSet.setInterpolator(new LinearInterpolator());
		animatorSet.removeAllListeners();
		animatorSet.addListener(callback);
		animatorSet.start();

		float temp = mAlphaStart;
		mAlphaStart = mAlphaEnd;
		mAlphaEnd = temp;

        if (!isAnimationIn) {
            mHasScale = false;
		}
	}


	/**
	 * 请求音频焦点
	 */
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

	/**
	 * 初始化返回对话框
	 */
	private void initBackDialog() {
		mBackDialog = new InterphotoDlg((Activity)mContext, R.style.waitDialog);
		mBackDialog.SetTitle(R.string.video_edit_back_title);
		mBackDialog.SetMessage(R.string.video_edit_back_message);
		mBackDialog.setLeftRightBtnColor(0xffa6a6a6, 0xffa6a6a6);
		mBackDialog.SetPositiveBtnText(R.string.Cancel);
		mBackDialog.SetNegativeBtnText(R.string.ok);
		mBackDialog.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback() {
			@Override
			public void onOK() {
				mBackDialog.dismiss();
				mVideoModeWrapper.resumeAll(false);
			}

			@Override
			public void onCancel() {
				mBackDialog.dismiss();
//				mSite.onBack(getContext());
				backToAlbumPage();
			}
		});
	}

	private int mWatermarkType, mWatermarkId;
	private void openWatermarkFrame(int watermarkType, int watermarkId) {
		mWatermarkType = watermarkType;
		mWatermarkId = watermarkId;
		mVideoModeWrapper.setCurrentMode(ProcessMode.Watermark);
		openSpecifyModule(ProcessMode.Watermark);
	}

	/**
	 *  根据需要打开指定的模式
	 * @param mode 对应的模式
	 */
	private void openSpecifyModule(final ProcessMode mode) {
		int oldHeight = ShareData.m_screenHeight - mVideoModeWrapper.mActionBarHeight - mActivePage.getBottomPartHeight();
		setActiveFeature(mode);
		updateActionbarInfo(R.drawable.framework_ok_btn);
		int newHeight = ShareData.m_screenHeight - mVideoModeWrapper.mActionBarHeight - mActivePage.getBottomPartHeight();
		int animationStart = mActivePage.getBottomPartHeight();
		mVideoHeight = ShareData.m_screenHeight - mVideoModeWrapper.mActionBarHeight - animationStart;
		mVideoView.setPivotX(mVideoView.getWidth() / 2);
		mVideoView.setPivotY(mVideoView.getHeight() / 2);

		startTransitionAnimtion(true, oldHeight, newHeight, animationStart, 0, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if(mode == ProcessMode.Music){
					if(mActivePage instanceof VideoMusicPage){
						((VideoMusicPage) mActivePage).requestSwitchLayout();
					}
				}
			}
		});
	}

	private LinearLayout.LayoutParams viewParams = null;
	private void setActiveFeature(ProcessMode mode) {
		mVideoModeWrapper.setCurrentMode(mode);
		int moduleIndex = -1;
		if (mode == ProcessMode.Fileter) {
			moduleIndex = 1;
			mCurrentBottomView = new VideoFilterPage(mContext, mFilterSite, mVideoModeWrapper);
			VideoFilterPage filterPage = (VideoFilterPage)mCurrentBottomView;
			filterPage.setAsGlobalFilter();
			setActivePage(filterPage);
			viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		} else if (mode == ProcessMode.Music) {
			moduleIndex = 2;
			VideoMusicPage musicView = new VideoMusicPage(mContext, mVideoMusicSite,mVideoModeWrapper);
			setActivePage(musicView);
			viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			mCurrentBottomView = musicView;
		} else if (mode == ProcessMode.Watermark) {
			moduleIndex = 3;
			VideoTextPage waterMarkView = new VideoTextPage(mContext,mVideoTextSite,mVideoModeWrapper);
			setActivePage(waterMarkView);
			viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			mCurrentBottomView = waterMarkView;
		}
		if (viewParams != null) {
			mCurrentBottomView.setLayoutParams(viewParams);
		}
		ViewParent parent = mCurrentBottomView.getParent();
		if (parent instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup)parent;
			viewGroup.removeView(mCurrentBottomView);
		}

		mBottomView.removeAllViews();
		mBottomView.addView(mCurrentBottomView);

		//设置页面参数
		if (moduleIndex != -1) {
			HashMap<String, Object> params = new HashMap<>();
			params.put("videos", mVideoModeWrapper.mVideoInfos);
			params.put("trans", mTrans);
			if (moduleIndex == 1) {
//			    if (isFromCamera) {
//					params.put("filter_url", mCurFilterUri);
//					params.put("filter_alpha", mCurFilterAlpha);
//					isFromCamera = false;
//				}
			} else if (moduleIndex == 3) {
				if (mOpenFromResourceShop) {
					params.put("watermark_type", mWatermarkType);
					params.put("watermark_id", mWatermarkId);
				}
			}
			mActivePage.SetData(params);
		}
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (view == mVideoView) {
			    if (mVideoModeWrapper.isPlaying()) {
			        mVideoModeWrapper.pauseAll();
				} else {
			        mVideoModeWrapper.resumeAll(false);
				}
			}
		}
	};

    private VideoFilterSite mFilterSite = new VideoFilterSite() {
		@Override
		public void onBack() {
            backFromSubPage();
		}
	};

	private VideoMusicSite mVideoMusicSite = new VideoMusicSite(){
		@Override
		public void onBack(Context context)
		{
			backFromSubPage();
		}
	};

	private VideoTextSite mVideoTextSite = new VideoTextSite(){
		@Override
		public void onBack(Context context)
		{
			backFromSubPage();
		}
	};

	private VideoBottomSite mVideoBottomSite = new VideoBottomSite() {

		@Override
		public void onBack() {
			backFromMainPage();
		}

		@Override
		public void onShareClick() {
			if (mBitmap == null) {
				Bitmap bmp = mVideoView.getFrame();
				mBitmap = BeautifyResMgr.MakeBkBmp(bmp, ShareData.m_screenWidth, ShareData.m_screenHeight, 0x99000000, 0x28000000);
				if (bmp != mBitmap) {
					bmp.recycle();
				}
			}
			mVideoModeWrapper.pauseAll();

			Bitmap bitmap = mVideoView.getFrame();
			HashMap<String, Object> params = new HashMap<>();
			params.put("maskBitmap", mBitmap);
			params.put("coverBitmap", bitmap);
			params.put("width", mVideoView.getWidth());
			params.put("height", mVideoView.getHeight());
			int[] location = new int[2];
			mVideoView.getLocationOnScreen(location);
			params.put("coordinations", location);

			params.put("isFromCamera",isFromCamera);
			params.put("params", mVideoView.getOutputParams());
			params.put("videoSaveDuration", mVideoModeWrapper.getVideosDuration());
			MyBeautyStat.onClickByRes(R.string.视频美化_保存与分享_保存);

			mSite.onOpenShare(mContext, params);

		}

		@Override
		public void onVideoAlbum(Context context, HashMap<String, Object> data) {
		    mSite.onVideoAlumb(context, data);
		}

		@Override
		public void onBackToAlbum(Context context)
		{
			mSite.onBack(getContext(),true, false);
		}
	};

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus) {
			VideoAlbumUtils.enterImmersiveMode(getContext());
		}
	}


}

