package cn.poco.video.videoFilter;

/**
 * Created by Shine on 2017/6/1.
 */

//public class VideoFilterFrame extends FrameLayout implements ProcessPageEventDelegate, EventRouter.EventChain
//{
//	private static final String TAG = "视频滤镜";
//	private FilterBottomView mFilterBottmView;
//	private Context mContext;
//	private ThemeIntroPage mThemeIntroPage;
//	private MasterIntroPage mMasterIntroPage;
//	private boolean mIsSelfHandle;
//
//	private VideoBeautifyPage mParentPage;
//	private GLVideoView mVideoView;
//	private ActionBar mActionBar;
//
//	private boolean mConfirmSelectedEffect;
//	private FilterRes mOrginFilterRes;
//	private static int sOriginFilterUri;
//
//	private int mTempSelectedUri;
//
//	private FilterInnerInfo mInfo;
//
//	public VideoFilterFrame(@NonNull Context context, VideoBeautifyPage page) {
//		super(context);
//		mContext = context;
//		mParentPage = page;
//		initInfo();
//		initView();
//		initChain();
//		TongJiUtils.onPageStart(context, TAG);
//		MyBeautyStat.onPageStartByRes(R.string.视频滤镜);
//	}
//
//	@Override
//	public void setData(HashMap<String, Object> params) {
//		int uri = -1, alpha = 0;
////		VideoEntry.VideoFilterRes videoFilter = new VideoEntry.VideoFilterRes();
////		mVideoEntry.mVideoFilter = videoFilter;
//		if (params != null) {
//			Object obj = params.get("filter_url");
//			if (obj instanceof Integer) {
//				uri = (Integer)obj;
//                mInfo.mCurrentFilterUri = uri;
//			}
//
//			obj = params.get("filter_alpha");
//			if (obj instanceof Integer) {
//				alpha = (Integer)obj;
//				mInfo.mCurrentFilterAlpha = (int)((alpha / 12f) * 100);
//			}
//
//			obj = params.get("videos");
//			if (obj != null) {
//                List<VideoInfo> mVideoList = (List<cn.poco.video.sequenceMosaics.VideoInfo>)obj;
//                if (mVideoList.get(0).mHasEdit) {
//					mInfo.mVideoMediaPath = mVideoList.get(0).mClipPath;
//				} else {
//                    mInfo.mVideoMediaPath = mVideoList.get(0).mPath;
//				}
//				mInfo.mClipStartTime = mVideoList.get(0).mSelectStartTime;
//				mFilterBottmView.decodeAndUpdateCoverImage();
//			}
//		}
//		if (uri != -1) {
//			mFilterBottmView.s_selUri = uri;
//			mTempSelectedUri = uri;
//			DragListItemInfo info = mFilterBottmView.getDramItemInfoByUri(uri);
//			mOrginFilterRes = (FilterRes) info.m_ex;
//			sOriginFilterUri = info.m_uri;
//			mFilterBottmView.adjustFilterResource();
//		}
//	}
//
//
//	private void initInfo() {
//		mVideoView = mParentPage.mVideoView;
//		mActionBar = mParentPage.mActionBar;
//		mOrginFilterRes = mVideoView.getCurrentFilterRes();
//        mInfo = new FilterInnerInfo();
//	}
//
////	private OnPlayListener mOnPlayListener = new OnPlayListener() {
////
////		@Override
////		public void onFirstStart() {
////			View topView = null;
////			if (VideoFilterFrame.this.getChildCount() > 0) {
////				topView = VideoFilterFrame.this.getChildAt(VideoFilterFrame.this.getChildCount() - 1);
////			}
////
////			if (!(topView instanceof FilterBottomView)) {
////				// 停止视频的同时还要停止音乐
////				mParentPage.pauseAll();
////			}
////		}
////
////		@Override
////		public void onRestart() {
////
////		}
////	};
//
//	private void initView() {
//		mFilterBottmView = new FilterBottomView(mContext, mInfo);
//		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//		mFilterBottmView.setLayoutParams(params);
//		this.addView(mFilterBottmView);
//		mFilterBottmView.setBottomViewCallback(new FilterBottomView.BottomViewCallbackAdapter() {
//			@Override
//			public void onNeedDownItemClick(HashMap<String, Object> params) {
//				MyBeautyStat.onClickByRes(R.string.视频滤镜_打开推荐位);
//				openRecommend(params);
//			}
//
//
//			@Override
//			public void onApplayFilter(FilterRes filterRes, int uri) {
////				if (mVideoView.isPause()) {
////					mParentPage.resumeAll(false);
////				}
//				mTempSelectedUri = uri;
////				mVideoEntry.mVideoFilter.mFilterRes = filterRes;
//				if (filterRes != null) {
//					TongJi2.AddCountById(filterRes.m_tjId + "");
//                    MyBeautyStat.onClickByRes(R.string.视频滤镜_选择滤镜);
//					MyBeautyStat.onChooseMaterial(String.valueOf(filterRes.m_tjId), R.string.视频滤镜);
//				}
//				mParentPage.mVideoView.changeFilter(filterRes);
//			}
//
//			@Override
//			public void onMoreItemClick(View v, int index) {
//				MyBeautyStat.onClickByRes(R.string.视频滤镜_进入更多);
//				mParentPage.pauseAll();
//				HashMap<String, Object> params = new HashMap<>();
//				params.put("type", ResType.FILTER);
//				params.put("typeOnly", true);
//				MyFramework.SITE_Popup(v.getContext(), ThemePageSite2.class, params, Framework.ANIM_TRANSLATION_LEFT);
//			}
//
//			@Override
//			public void onItemAvatarClick(HashMap<String, Object> params) {
//                MyBeautyStat.onClickByRes(R.string.视频滤镜_打开大师介绍页);
//				openMasterPage(params);
//			}
//
//			@Override
//			public void onAdjustModeSelected(BeautyAdjustType type, String title) {
//				mActionBar.getActionBarTitleView().setText(title);
//			}
//
//			@Override
//			public void onFilterTypeChange(int mode) {
//				if (mode == 0) {
//					mActionBar.getActionBarTitleView().setText(mContext.getString(R.string.Filters));
//				} else if (mode == 1) {
//					TongJi2.AddCountByRes(mContext, R.integer.调整);
//					mActionBar.getActionBarTitleView().setText(mContext.getString(R.string.Adjust));
//				}
//			}
//
//			@Override
//			public void onAdjustbarSeek(BeautyColorType filterType, BeautyAdjustType type, float value) {
////				if (mVideoView.isPause()) {
////					mParentPage.resumeAll(false);
////				}
//
//				if (filterType == BeautyColorType.FILTER) {
//					// 添加滤镜透明度
//					mVideoView.changeFilterAlpha(value / 100);
//				} else if (filterType == BeautyColorType.ADJUST) {
//					AdjustItem adjustItem = null;
//					switch (type) {
//						case BRIGHTNESS: {
//							TongJi2.AddCountByRes(mContext, R.integer.亮度);
//							adjustItem = new AdjustItem(AdjustItem.BRIGHTNESS, value);
//							break;
//						}
//						case TEMPERATURE: {
//							TongJi2.AddCountByRes(mContext, R.integer.色温);
//							adjustItem = new AdjustItem(AdjustItem.WHITE_BALANCE, value);
//							break;
//						}
//
//						case SATURABILITY: {
//							TongJi2.AddCountByRes(mContext, R.integer.饱和度);
//							adjustItem = new AdjustItem(AdjustItem.SATURATION, value);
//							break;
//						}
//
//						case CONTRAST: {
//							TongJi2.AddCountByRes(mContext, R.integer.对比);
//							adjustItem = new AdjustItem(AdjustItem.CONTRAST, value);
//							break;
//						}
//
//						case SHARPEN: {
//							TongJi2.AddCountByRes(mContext, R.integer.锐化);
//							adjustItem = new AdjustItem(AdjustItem.SHARPEN, value);
//							break;
//						}
//
//						case HUE: {
//							TongJi2.AddCountByRes(mContext, R.integer.色调);
//							adjustItem = new AdjustItem(AdjustItem.COLOR_BALANCE, value);
//							break;
//						}
//
//						case DARKCORNER: {
//							TongJi2.AddCountByRes(mContext, R.integer.暗角);
//							adjustItem = new AdjustItem(AdjustItem.DARK_CORNER, value);
//							break;
//						}
//
//						case HIGHTLIGHT: {
//							TongJi2.AddCountByRes(mContext, R.integer.高光);
//                            float finalValue = (1 - value);
//							finalValue = finalValue < 0 ? 0 : finalValue > 1 ? 1 : finalValue;
//							adjustItem = new AdjustItem(AdjustItem.HIGHLIGHT, finalValue);
//							break;
//						}
//
//						case SHADE: {
//							TongJi2.AddCountByRes(mContext, R.integer.阴影);
//							adjustItem = new AdjustItem(AdjustItem.SHADOW, value);
//							break;
//						}
//
//						default: {
//							break;
//						}
//					}
//					if (adjustItem != null) {
//						mVideoView.addAdjust(0, adjustItem);
//					}
//				}
//			}
//
//			@Override
//			public void onFilterAlphaBarVisibilityChange(boolean show) {
//                if (show) {
//                    MyBeautyStat.onClickByRes(R.string.视频滤镜_进入滤镜不透明度);
//				} else {
//                    MyBeautyStat.onClickByRes(R.string.视频滤镜_滤镜不透明度_收起滤镜不透明度);
//				}
//			}
//		});
//	}
//
//	private void initChain() {
//		List<EventRouter.EventChain> chainList = new ArrayList<>();
//		chainList.add(mMasterIntroSite2);
//		chainList.add(mThemeIntroSite5);
//		chainList.add(mFilterBottmView);
//		chainList.add(this);
//		EventRouter.getInstance().initEventChain(chainList);
//	}
//
//	@Override
//	public void onActionbarLeftBtnClick() {
//		if (mParentPage != null) {
//			mParentPage.onBack();
//			MyBeautyStat.onClickByRes(R.string.视频滤镜_退出视频滤镜);
//		}
//	}
//
//	@Override
//	public void onActionBarRigthBtnClick() {
//		if (mParentPage != null) {
//			TongJi2.AddCountByRes(mContext, R.integer.保存);
//			MyBeautyStat.onClickByRes(R.string.视频滤镜_保存滤镜美化);
//			mParentPage.isCanSave = true;
//			mParentPage.isModify = true;
//			mFilterBottmView.s_selUri = mTempSelectedUri;
//			sOriginFilterUri = mTempSelectedUri;
//			mFilterBottmView.setHandleEvent(false);
//			mConfirmSelectedEffect = true;
//			mParentPage.onBack();
//		}
//	}
//
//	@Override
//	public int onBackBtnPressed() {
//		EventRouter.getInstance().dispatchEvent(EventRouter.Event.OnBack);
//		if (mIsSelfHandle) {
//			if (!mConfirmSelectedEffect) {
//				FilterRes filterRes = null;
//                if (sOriginFilterUri != 0 && mFilterBottmView.isFilterUriValid(sOriginFilterUri)) {
//                    filterRes = mOrginFilterRes;
//                }
//                mParentPage.mVideoView.changeFilter(filterRes);
//			}
//			MyBeautyStat.onClickByRes(R.string.视频滤镜_退出视频滤镜);
//			mIsSelfHandle = false;
//			return MODE_OTHER_END;
//		} else {
//			return MODE_NOT_END;
//		}
//	}
//
//	@Override
//	public void setNextChain(EventRouter.EventChain chain) {
//
//	}
//
//	@Override
//	public boolean handleEvent(EventRouter.Event event) {
//		mIsSelfHandle = true;
//		return mIsSelfHandle;
//	}
//
//	@Override
//	public void onPause() {
//	    EventRouter.getInstance().dispatchEvent(EventRouter.Event.OnPause);
//	    if (mIsSelfHandle) {
//			mParentPage.onPauseAll();
//			mIsSelfHandle = false;
//		}
//		TongJiUtils.onPagePause(mContext, TAG);
//	}
//
//	@Override
//	public void onResume() {
//		EventRouter.getInstance().dispatchEvent(EventRouter.Event.OnResume);
//		if (mIsSelfHandle) {
//			mParentPage.onResumeAll();
//			mIsSelfHandle = false;
//		}
//		TongJiUtils.onPageResume(mContext, TAG);
//	}
//
//	@Override
//	public int getDisplayViewHeight() {
//		return ShareData.PxToDpi_xhdpi(300);
//	}
//
//	@Override
//	public boolean onVideoViewClick(boolean isPlaying) {
//        return false;
//	}
//
//	@Override
//	public void onClose() {
//		clear();
//        TongJiUtils.onPageEnd(mContext, TAG);
//		MyBeautyStat.onPageEndByRes(R.string.视频滤镜);
//	}
//
//	@Override
//	public void onPageResult(int siteID, HashMap<String, Object> params) {
//		mParentPage.resumeAll(false);
//		if (siteID == SiteID.THEME_PAGE || siteID == SiteID.THEME_INTRO_PAGE) {
//			if (mFilterBottmView != null) {
//				mFilterBottmView.onPageResult(siteID, params);
//			}
//		}
//	}
//
//	@Override
//	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
//		if (mMasterIntroPage != null) {
//			mMasterIntroPage.onActivityResult(requestCode, resultCode, data);
//		}
//		if (mThemeIntroPage != null) {
//			mThemeIntroPage.onActivityResult(requestCode, resultCode, data);
//		}
//		return false;
//	}
//
//	@Override
//	public void onSurfaceFirstCreate() {
//
//	}
//
//	@Override
//	public void onSurfaceRecreate() {
//
//	}
//
//
//
//	private void clear() {
//		EventRouter.getInstance().clear();
//		mFilterBottmView.releaseMemButKeepData();
////		mVideoView.removeOnPlayListener(mOnPlayListener);
//	}
//
//	protected void openMasterPage(HashMap<String, Object> params) {
//		mParentPage.pauseAll();
//
//		if (mMasterIntroPage != null) {
//			this.removeView(mMasterIntroPage);
//			mMasterIntroPage.onClose();
//			mMasterIntroPage = null;
//		}
//		mMasterIntroPage = new MasterIntroPage(getContext(), mMasterIntroSite2);
//		params.put("pageId", R.string.视频滤镜);
//		mMasterIntroPage.SetData(params);
//		this.addView(mMasterIntroPage);
//	}
//
//	private void openRecommend(HashMap<String, Object> params) {
//		mParentPage.pauseAll();
//
//		if (mThemeIntroPage != null) {
//			removeView(mThemeIntroPage);
//			mThemeIntroPage.onClose();
//			mThemeIntroPage = null;
//		}
//		mThemeIntroPage = new ThemeIntroPage(getContext(), mThemeIntroSite5);
//		mThemeIntroPage.SetData(params);
//		this.addView(mThemeIntroPage);
//	}
//
//
//	private ThemeIntroPageSite5 mThemeIntroSite5 = new ThemeIntroPageSite5();
//
//	public class ThemeIntroPageSite5 extends ThemeIntroPageSite2 implements EventRouter.EventChain {
//		private EventRouter.EventChain mNextChain;
//
//
//		@Override
//		public void OnResourceUse(HashMap<String, Object> params,Context context) {
//			super.OnResourceUse(params,context);
//		}
//
//		@Override
//		public void OnBack(HashMap<String, Object> params,Context context) {
//			VideoFilterFrame.this.onPageResult(SiteID.THEME_INTRO_PAGE, params);
//			if (mThemeIntroPage != null) {
//				VideoFilterFrame.this.removeView(mThemeIntroPage);
//				mThemeIntroPage.onClose();
//				mThemeIntroPage = null;
//			}
//		}
//
//		@Override
//		public void setNextChain(EventRouter.EventChain chain) {
//			mNextChain = chain;
//		}
//
//		@Override
//		public boolean handleEvent(EventRouter.Event event) {
//		    if (mThemeIntroPage != null) {
//		        if (event == EventRouter.Event.OnBack) {
//					mThemeIntroPage.onBack();
//					return true;
//				} else if (event == EventRouter.Event.OnResume) {
//		            mThemeIntroPage.onResume();
//		            return true;
//				} else if (event == EventRouter.Event.OnPause) {
//		            mThemeIntroPage.onPause();
//		            return true;
//				}
//			} else {
//				if (mNextChain != null) {
//					mNextChain.handleEvent(event);
//				}
//			}
//			return false;
//		}
//	}
//
//	private MasterIntroPageSite2 mMasterIntroSite2 = new MasterIntroPageSite2();
//
//	private class MasterIntroPageSite2 extends MasterIntroPageSite implements EventRouter.EventChain {
//		private EventRouter.EventChain mNextChain;
//
//		@Override
//		public void onBack(HashMap<String, Object> params,Context context) {
//            MyBeautyStat.onClickByRes(R.string.视频滤镜_大师介绍页_退出大师介绍页);
//			mParentPage.resumeAll(false);
//			if (mMasterIntroPage != null) {
//				VideoFilterFrame.this.removeView(mMasterIntroPage);
//				mMasterIntroPage.onClose();
//				mMasterIntroPage = null;
//			}
//
//			if (params != null) {
//				int id = -1;
//				boolean lock = true;
//				if (params.get("id") != null) {
//					id = (Integer)params.get("id");
//				}
//				if (params.get("lock") != null) {
//					lock = (Boolean)params.get("lock");
//				}
//				if (id != -1 && lock == false) {
//					mFilterBottmView.unlockMasterRes(id);
//				}
//			}
//
//		}
//
//		@Override
//		public void setNextChain(EventRouter.EventChain chain) {
//			mNextChain = chain;
//		}
//
//		@Override
//		public boolean handleEvent(EventRouter.Event event) {
//			if (mMasterIntroPage != null) {
//				if (event == EventRouter.Event.OnBack) {
//					mMasterIntroPage.onBack();
//					return true;
//				} else if (event == EventRouter.Event.OnResume) {
//					mMasterIntroPage.onResume();
//					return true;
//				} else if (event == EventRouter.Event.OnPause) {
//				    mMasterIntroPage.onPause();
//				    return true;
//				}
//			} else {
//				if (mNextChain != null) {
//					mNextChain.handleEvent(event);
//				}
//			}
//			return false;
//		}
//	}
//}
