package cn.poco.video.videoAlbum;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.album2.model.FolderInfo;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.PermissionUtils;
import cn.poco.video.VideoConfig;
import cn.poco.video.helper.GridSpacingItemDecoration;
import cn.poco.video.helper.controller.MediaController;
import cn.poco.video.model.VideoEntry;
import cn.poco.video.render.PlayRatio;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.site.VideoAlbumSite;
import cn.poco.video.site.VideoAlbumSite3;
import cn.poco.video.utils.AndroidUtil;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.utils.VideoUtils;
import cn.poco.video.view.ActionBar;


/**
 * Created by Shine on 2017/5/12.
 */

public class VideoAlbumPage extends IPage {
	private final static String SCROLL_POSITION = "Scroll_Position";
	private final static String TAG = "视频相册页";

	//UI布局数据
	private int mActionBarHeight;

	private VideoAlbumSite mSite;

	private Context mContext;
	private ActionBar mActionBar;
	private RecyclerView mRecyclerView;
	private TutorialView mTutorialLayer;
	private VideoAdapter mVideoAdapter;
	private VideoFolderView mFolderView;
	private VideoSelectedView mSelectedTipView;
	private EmptyAlbumView mEmptyAlbumView;

	private static boolean sFirstCache = true;
//    private static List<VideoEntry> sVideoList = new ArrayList<>();

	private boolean mFirstUse;

	private boolean mUiEnable = true;
	private ArrayList<VideoEntry> mJointVideos = new ArrayList<>();  //拼接的视频集合
	private int mUsableTime = VideoConfig.DURATION_FREE_MODE - 100; //加100误差
	private int mHasUsedNum;
	private int mCurFolderIndex = 0;
	private int mBottomH;
	private int mCurRatio = PlayRatio.RATIO_1_1;
	private boolean mIs10sMode = true;
//    private boolean mIsUseRatio;

	public VideoAlbumPage(Context context, BaseSite site) {
		super(context, site);
		mSite = (VideoAlbumSite)site;
		mContext = context;
		AndroidUtil.init();
		initData();
		initView();
		initListener();
		TongJiUtils.onPageStart(context, TAG);
		MyBeautyStat.onPageStartByRes(R.string.视频相册页);
	}

	private void initData() {
		this.setClickable(true);
		mActionBarHeight = ShareData.PxToDpi_xhdpi(80);
		mFirstUse = TagMgr.CheckTag(mContext, Tags.VIDEOALBUM_TUTORIAL_NEW);
		mBottomH = ShareData.PxToDpi_xhdpi(80);
	}

	private void initView() {
		this.setBackgroundColor(0xff0e0e0e);

		mEmptyAlbumView = new EmptyAlbumView(mContext);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
		mEmptyAlbumView.setLayoutParams(params);
		this.addView(mEmptyAlbumView);
		mEmptyAlbumView.setVisibility(View.GONE);

		mActionBar = new ActionBar(mContext, 0xff0e0e0e);
		mActionBar.setUpLeftImageBtn(R.drawable.framework_back_btn);
		mActionBar.getLeftImageBtn().setPadding(0, 0, ShareData.PxToDpi_xhdpi(20), 0);
//        mActionBar.setUpActionbarTitle(mContext.getString(R.string.all_video), Color.WHITE, 16);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight);
		mActionBar.setLayoutParams(params);
		this.addView(mActionBar);
		mActionBar.setOnActionbarMenuItemClick(new ActionBar.onActionbarMenuItemClick() {
			@Override
			public void onItemClick(int id) {
				if (id == ActionBar.LEFT_MENU_ITEM_CLICK) {
					onBack();
				} else if (id == ActionBar.RIGHT_MENU_ITEM_CLICK) {

				}
			}
		});
		mActionBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mRecyclerView.smoothScrollToPosition(0);
			}
		});
		mRecyclerView = new RecyclerView(mContext);
		final GridLayoutManager layoutManager = new GridLayoutManager(mContext, 3) {
			@Override
			public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
				super.onLayoutChildren(recycler, state);
                mUiEnable = true;
				if (mFirstUse && !isFromVideoBeauty() && mTutorialLayer.getVisibility() != View.VISIBLE) {
					View child = mRecyclerView.getChildAt(0);
					if (child != null && child.getWidth() > 0) {
						mTutorialLayer.setVisibility(View.VISIBLE);
						TagMgr.SetTag(mContext, Tags.VIDEOALBUM_TUTORIAL_NEW);
						mFirstUse = false;
					}
				}
//                if (mFirstUse && !mTutorailShow) {
//                    View child = mRecyclerView.getChildAt(0);
//                    if (child != null) {
//                        int cellWidth = child.getWidth();
//                        if (cellWidth > 0) {
////                            mTutorialLayer.setSelectPosition(cellWidth / 2, cellWidth + mActionBarHeight - ShareData.PxToDpi_xhdpi(30));
//                            mTutorialLayer.show();
//                            mUiEnable = true;
//                            TagMgr.SetTag(mContext, Tags.VIDEOALBUM_TUTORIAL);
//                            mFirstUse = false;
//                            mTutorailShow = true;
//                        }
//                    }
//                }
			}
		};
		((SimpleItemAnimator)mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, ShareData.PxToDpi_xhdpi(2)));
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL);
		params.topMargin = mActionBarHeight;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(80);
		mRecyclerView.setLayoutParams(params);
		this.addView(mRecyclerView);

		mVideoAdapter = new VideoAdapter(mContext, null);
		mRecyclerView.setAdapter(mVideoAdapter);
		mVideoAdapter.setOnItemListener(mOnItemListener);

		mSelectedTipView = new VideoSelectedView(getContext());
		mSelectedTipView.setVisibility(View.GONE);
		mSelectedTipView.setClickable(true);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(640), ShareData.PxToDpi_xhdpi(120));
		params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(120);
		addView(mSelectedTipView, params);
		mSelectedTipView.setOnNextClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mJointVideos.size() > 0) {
					MyBeautyStat.onClickByRes(R.string.视频相册页_下一步);
					saveDate();
					final HashMap<String, Object> params = new HashMap<>();
					params.put("ratio", mCurRatio);
					params.put("10s_mode", mIs10sMode);
					if (isFromVideoBeauty()) {
						List<VideoInfo> videoInfos = VideoAlbumUtils.transformVideoInfo(mJointVideos);
						params.put("videos", videoInfos);
						mSite.onVideoSetting(getContext(), params);
					} else {
						params.put("videos", mJointVideos);
						//缓存数据
						mSite.onVideoSetting(getContext(), params);
					}

				}
			}
		});

		mFolderView = new VideoFolderView(getContext());
		mFolderView.setOnFolderCallBack(new VideoFolderView.OnFolderCallBack() {
			@Override
			public void onChange(int folderIndex) {
				mCurFolderIndex = folderIndex;
				mVideoAdapter.updateList(MediaController.getInstance(getContext()).getVideoEntrys(mCurFolderIndex));
			}
		});
		params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
		addView(mFolderView, params);

		mTutorialLayer = new TutorialView(mContext);
		params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		mTutorialLayer.setLayoutParams(params);
		this.addView(mTutorialLayer);
		mTutorialLayer.setVisibility(View.GONE);
		mTutorialLayer.setOnClickListener(mOnClickListener);
	}

	private Bitmap mMaskBp;
	private long lastMaskTime;//上一次做毛玻璃的时间

	private Bitmap getMaskBp() {
		if (System.currentTimeMillis() - lastMaskTime >= 5000) {
			mMaskBp = null;
		}
		if (mMaskBp == null) {
			lastMaskTime = System.currentTimeMillis();
			Bitmap bitmap = Bitmap.createBitmap(ShareData.m_screenWidth, ShareData.m_screenHeight, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			this.draw(canvas);
			if (bitmap != null) {
				mMaskBp = BeautifyResMgr.MakeBkBmp(bitmap, bitmap.getWidth(), bitmap.getHeight(), 0x90000000, 0x28000000);
			}
		}
		return mMaskBp;
	}


	private int getTotalSecond() {
		int total = 0;
		for (int i = 0; i < mJointVideos.size(); i++) {
//            total += (mJointVideos.get(i).mDuration+500) /1000;
			total += mJointVideos.get(i).mDuration;
		}
		return total;
	}

	private void onNumChange(int curNum, boolean isSelect) {
		if (curNum + mHasUsedNum > 0) {
			mSelectedTipView.setNextBtnClickable(curNum > 0);
			mSelectedTipView.setVisibility(View.VISIBLE);
			mSelectedTipView.setDate(curNum + mHasUsedNum, getTotalSecond() + (VideoConfig.DURATION_FREE_MODE - mUsableTime));
		} else {
			mSelectedTipView.setVisibility(View.GONE);
			mCurRatio = PlayRatio.RATIO_1_1;
		}
//        mActionBar.setUpActionbarTitle(title,Color.WHITE, 16);
	}

	private void initListener() {
		mContext.getContentResolver().registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, mContentObserver);
	}

	public void saveDate() {
		mSite.m_myParams.put("folderIndex", mCurFolderIndex);
//        mSite.m_myParams.put("bk",getMaskBp());
		mSite.m_myParams.put("videos", mJointVideos);
		mSite.m_myParams.put("10s_mode", mIs10sMode);
//        Parcelable parcelable = (mRecyclerView.getLayoutManager()).onSaveInstanceState();
//        mSite.m_myParams.put(SCROLL_POSITION, parcelable);
		GridLayoutManager gridLayoutManager = (GridLayoutManager)mRecyclerView.getLayoutManager();
		int position = gridLayoutManager.findFirstVisibleItemPosition();
		if (position >= 0) {
			View firstVisiableChildView = gridLayoutManager.findViewByPosition(position);
			int offset = firstVisiableChildView.getTop();
			mSite.m_myParams.put("position", position);
			mSite.m_myParams.put("offset", offset);
		}
	}

	public void restoreDate() {
		if (mSite.m_myParams.containsKey("videos")) {
			mJointVideos = (ArrayList<VideoEntry>)mSite.m_myParams.get("videos");
			checkAndShowVideosOrder();
			onNumChange(mJointVideos.size(), true);
		}
		if (mSite.m_myParams.containsKey("ratio")) {
			mCurRatio = (int)mSite.m_myParams.get("ratio");
		}
		if (mSite.m_myParams.containsKey("10s_mode")) {
			mIs10sMode = (boolean)mSite.m_myParams.get("10s_mode");
		}
//        if(mSite.m_myParams.containsKey("bk")){
//            mMaskPath = (String) mSite.m_myParams.get("bk");
//        }
		if (mSite.m_myParams.containsKey("folderIndex")) {
			mCurFolderIndex = (int)mSite.m_myParams.get("folderIndex");
		}
		if (mSite.m_myParams.get(SCROLL_POSITION) != null) {
			final Parcelable parcelable = (Parcelable)mSite.m_myParams.get(SCROLL_POSITION);
			mRecyclerView.getLayoutManager().onRestoreInstanceState(parcelable);
		}
	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (!mUiEnable) {
			return true;
		} else {
			return super.onInterceptTouchEvent(ev);
		}
	}

	private boolean isFromVideoBeauty() {
		return mHasUsedNum > 0 && mUsableTime > 0;
//        return true;
	}

	@Override
	public void SetData(HashMap<String, Object> params) {
		if (params != null) {
			if (params.containsKey("video_len")) {
				mHasUsedNum = (int)params.get("video_len");
			}
			if (params.containsKey("usable_time")) {
				mUsableTime = (int)params.get("usable_time");
				onNumChange(0, false);
			}
			if (params.containsKey("ratio")) {
				mCurRatio = (int)params.get("ratio");
			}
		}
		mVideoAdapter.setShowCamera(isFromVideoBeauty());
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				MediaController.getInstance(getContext()).initVideoInfo(true);
			}
		}).start();
		MediaController.getInstance(getContext()).addLoadCompleteListener(mIVideoLoadComplete);

		FileUtils.clearHiddenSrcVideoFile();
	}

	private void checkAndShowVideosOrder() {
		if (mJointVideos != null) {
			for (int i = mJointVideos.size() - 1; i >= 0; i--) {
				if (!new File(mJointVideos.get(i).mMediaPath).exists()) {
					mJointVideos.remove(i);
				}
			}
			if (mVideoAdapter != null) {
				mVideoAdapter.clearSelectedItem();
				mVideoAdapter.setSelectVideos(mJointVideos);
			}
		}
	}

	@Override
	public void onResume() {
		TongJiUtils.onPageResume(mContext, TAG);
		checkAndShowVideosOrder();
	}

	@Override
	public void onPause() {
//        VideoPreviewer.getInstance().closePreview();
		TongJiUtils.onPagePause(mContext, TAG);
	}

	@Override
	public void onBack() {
		if (mTutorialLayer.getVisibility() == View.VISIBLE) {
			mTutorialLayer.setVisibility(View.GONE);
			return;
		}
		if (!mFolderView.onBack()) {
			MyBeautyStat.onClickByRes(R.string.视频相册页_退出视频相册);
			mSite.onBack(getContext());
		}
	}

	@Override
	public void onClose() {
		TongJiUtils.onPageEnd(mContext, TAG);
		MyBeautyStat.onPageEndByRes(R.string.视频相册页);
		mContext.getContentResolver().unregisterContentObserver(mContentObserver);
		Glide.get(mContext).clearMemory();
		mOnClickListener = null;
		mContentObserver = null;
		AndroidUtil.clear();
		MediaController.getInstance(getContext()).removeLoadCompleteListener(mIVideoLoadComplete);
	}

	private void removeVideoInfo(String path) {
		for (int i = 0; i < mJointVideos.size(); i++) {
			if (mJointVideos.get(i).mMediaPath.equals(path)) {
				mJointVideos.remove(i);
				break;
			}
		}
	}

	private boolean isContainsVideoInfo(String path) {
		boolean has = false;
		for (int i = 0; i < mJointVideos.size(); i++) {
			if (mJointVideos.get(i).mMediaPath.equals(path)) {
				has = true;
				break;
			}
		}
		return has;
	}

	private void openCamera() {
		MyBeautyStat.onClickByRes(R.string.视频相册页_开启镜头);
		HashMap<String, Object> params = new HashMap<>();
		params.put("addVideo", true);
		params.put("usable_time", mUsableTime);
		params.put("ratio", mCurRatio);
		((VideoAlbumSite3)mSite).onCamera(getContext(), params);
	}

	private VideoAdapter.OnItemListener mOnItemListener = new VideoAdapter.OnItemListener() {
		@Override
		public void onCamera() {
			if (mSite instanceof VideoAlbumSite3) {
				boolean flag = true;
				boolean flag1 = true;
				if (Build.VERSION.SDK_INT >= 23) {
					flag = PermissionUtils.checkCameraPermission(getContext());
					flag1 = PermissionUtils.checkAudioPermission(getContext());
				}
				if (flag && flag1) {
					openCamera();
				} else {
					PermissionUtils.requestPermissions(getContext(), new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
				}
			}
		}

		@Override
		public void onItemClick(VideoEntry videoEntry, int position) {
			MyBeautyStat.onClickByRes(R.string.视频相册页_进入视频预览);

			HashMap<String, Object> params = new HashMap<>();
			params.put("video", videoEntry);
//            params.put("bk",initMaskPath());
			params.put("bk", getMaskBp());
			params.put("cur_num", mJointVideos.size() + mHasUsedNum);
			params.put("usable_time", mUsableTime - getTotalSecond());
//            params.put("is_selected",mJointVideos.contains(videoEntry));
			params.put("is_selected", isContainsVideoInfo(videoEntry.mMediaPath));
			saveDate();
			mSite.onClickVideo(getContext(), params);
		}

		@Override
		public boolean onItemSelected(VideoEntry params, int position) {
			boolean isSelectedItem = !isContainsVideoInfo(params.mMediaPath);
			if (!isSelectedItem) {
				removeVideoInfo(params.mMediaPath);
//                mJointVideos.remove(params.mMediaPath);
				onNumChange(mJointVideos.size(), false);
			} else {
				isSelectedItem = false;
				if (VideoAlbumUtils.isVideoValid(getContext(), params, mJointVideos.size() + mHasUsedNum, mUsableTime - getTotalSecond())) {
					mJointVideos.add(params);
					//检查比例
					if (mJointVideos.size() == 1) {
						mCurRatio = VideoUtils.getVideoRatio(params.mWidth, params.mHeight);
					}
					onNumChange(mJointVideos.size(), true);
					isSelectedItem = true;
				}
			}
			return isSelectedItem;
		}
	};

	private boolean mHasLoadData = false;
	private MediaController.IVideoLoadComplete mIVideoLoadComplete = new MediaController.IVideoLoadComplete() {
		@Override
		public void onCompleted(List<FolderInfo> folderInfos, boolean update) {
			if (MediaController.getInstance(getContext()).getAllVideoEntrys().size() == 0) {
				if(!isFromVideoBeauty()){
					mRecyclerView.setVisibility(View.GONE);
					mEmptyAlbumView.setVisibility(View.VISIBLE);
				}
				mFolderView.setVisibility(View.GONE);
			} else {
				if (mFirstUse) {
					mUiEnable = false;
				}
				if (mCurFolderIndex > folderInfos.size()) {
					mCurFolderIndex = 0;
				}
				mEmptyAlbumView.setVisibility(View.GONE);
				mRecyclerView.setVisibility(View.VISIBLE);
				mFolderView.setVisibility(View.VISIBLE);
				mFolderView.setFolderInfos(folderInfos, mCurFolderIndex);
				mVideoAdapter.updateList(MediaController.getInstance(getContext()).getVideoEntrys(mCurFolderIndex));
				if(!mHasLoadData)
				{
					//放置刷新数据时回到旧的位置
					int position = 0;
					int offset = 0;
					if (mSite.m_myParams.containsKey("position"))
					{
						position = (int) mSite.m_myParams.get("position");
					}
					if (mSite.m_myParams.containsKey("offset"))
					{
						offset = (int) mSite.m_myParams.get("offset");
					}
					if (position != 0 || offset != 0)
					{
						((GridLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, offset);
					}
				}
				mHasLoadData = true;
			}
		}
	};

	private View.OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v == mTutorialLayer) {
				mTutorialLayer.setVisibility(View.GONE);
			}
		}
	};

	private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {

			if (!MediaController.getInstance(getContext()).isLoading()) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						MediaController.getInstance(mContext).initVideoInfo(true);
					}
				}).run();
			}
		}
	};

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params) {
		super.onPageResult(siteID, params);
//        if(params != null && siteID == SiteID.VIDEO_PREVIEW)
//        {
//            if(params.containsKey("video")){
//                final VideoEntry videoEntry = (VideoEntry) params.get("video");
//                if(mJointVideos.contains(videoEntry))
//                {
//                    mJointVideos.remove(videoEntry);
//                    mVideoAdapter.cancelSelectItem(videoEntry.mMediaPath);
//                }else{
//                    if(mOnItemListener.onItemSelected(videoEntry,-1)){
//                        mVideoAdapter.setSelectItem(videoEntry.mMediaPath);
//                    }
//                }
//            }
//        }
	}

	@Override
	public void onBackResult(int siteID, HashMap<String, Object> params) {
		super.onBackResult(siteID, params);
		if (siteID == SiteID.VIDEO_SETTING || siteID == SiteID.VIDEO_PREVIEW | siteID == SiteID.CAPTURE_VIDEO) {
			restoreDate();
			if (params != null) {
				if (siteID == SiteID.VIDEO_PREVIEW) {
					if (params.containsKey("video")) {
						final VideoEntry videoEntry = (VideoEntry)params.get("video");
						if(videoEntry != null)
						{
							if (mOnItemListener.onItemSelected(videoEntry, -1))
							{
								mVideoAdapter.setSelectItem(videoEntry.mMediaPath);
							} else
							{
								mVideoAdapter.cancelSelectItem(videoEntry.mMediaPath);
							}
						}
					}
				} else if (siteID == SiteID.VIDEO_SETTING) {
					if (params.containsKey("ratio")) {
						mCurRatio = (int)params.get("ratio");
					}
					if (params.containsKey("10s_mode")) {
						mIs10sMode = (boolean)params.get("10s_mode");
					}
				} else if (siteID == SiteID.CAPTURE_VIDEO) {

				}
				saveDate();
			}
		} else {
			mSite.m_myParams.clear();
		}
	}

	@Override
	public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (Build.VERSION.SDK_INT >= 23) {
			if (permissions != null && grantResults != null && permissions.length == grantResults.length) {
				String str;
				boolean flag1 = false;
				boolean flag2 = false;
				for (int i = 0; i < permissions.length; i++) {
					str = permissions[i];

					if (Manifest.permission.CAMERA.equals(str)) {
						if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
							flag1 = true;
						} else {
							flag1 = false;
							if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(), str)) {
								Toast.makeText(getContext(), "请到应用程序管理里面打开相机权限", Toast.LENGTH_SHORT).show();
							}
						}
					}

					if (Manifest.permission.RECORD_AUDIO.equals(str)) {
						if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
							flag2 = true;
						} else {
							flag2 = false;
							if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(), str)) {
								Toast.makeText(getContext(), "请到应用程序管理里面打开录音权限", Toast.LENGTH_SHORT).show();
							}
						}
					}
				}


				if (flag1 && flag2) {
					openCamera();
				}
			}
		}
		return super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}

