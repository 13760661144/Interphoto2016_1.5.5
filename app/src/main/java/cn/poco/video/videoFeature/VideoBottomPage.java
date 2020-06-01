package cn.poco.video.videoFeature;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.framework.BaseSite;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.resource.FilterRes;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.InterphotoDlg;
import cn.poco.video.VideoConfig;
import cn.poco.video.VideoResMgr;
import cn.poco.video.frames.VideoPage;
import cn.poco.video.page.ProcessMode;
import cn.poco.video.page.VideoModeWrapper;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.sequenceMosaics.TransitionDataInfo;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.videoFeature.layout.TransitionDetailsLayout;
import cn.poco.video.videoFeature.layout.VideoProgressLayout;
import cn.poco.video.view.ActionBar;

/**
 * Created by Simon Meng on 2018/1/3.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoBottomPage extends VideoPage{


    public interface VideoBottomViewCallback {
        boolean canClick();
        void onClickFeature(int index);
    }

    private Context mContext;
    private final int[] mVideoBeautifyFeature = new int[] {R.drawable.video_filter_icon, R.drawable.video_text_icon, R.drawable.video_music_icon};
    private VideoBottomViewCallback mCallback;

    private VideoProgressLayout mProgressLayout;
    private LinearLayout mFeatureBtnContainer;
    private TransitionDetailsLayout mTransitionDetailsLayout;
//    private VideoSelectedLayout mVideoSelectLayout;
//    private FeatureMenuList mFeatureMenuList;

    private VideoBottomSite mSite;

    private VideoModeWrapper mWrapper;

//    private VideoFramesPage mActivePage;
    private ActionBar mActionBar;
    private VideoFeaturePage mVideoFeaturePage;

//    private LinearLayout mViewContainer;
    private FrameLayout mViewContainer;

    public VideoBottomPage(@NonNull Context context, BaseSite baseSite, VideoModeWrapper wrapper) {
        super(context, baseSite);
        mSite = (VideoBottomSite) baseSite;
        mContext = context;
        mWrapper = wrapper;
        initData();
        initView();
    }


    private int mPadding;
    private void initData() {
        mPadding = ShareData.PxToDpi_xhdpi(50);
    }

    private void initView() {
        mActionBar = mWrapper.mActionBar;
        initListener();

        FrameLayout.LayoutParams params;
//        mViewContainer = new LinearLayout(mContext);
//        mViewContainer.setOrientation(LinearLayout.VERTICAL);
//        mViewContainer.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
//        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getBottomPartHeight(), Gravity.BOTTOM);
//        mViewContainer.setLayoutParams(params);
//        this.addView(mViewContainer);
//        {
//            LinearLayout.LayoutParams linearParams;
//            mProgressLayout = new VideoProgressLayout(mContext);
//            mProgressLayout.setBackgroundColor(0x0e0e0e);
////            linearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(300));
//            //在剩下的高度下内部居中
//            linearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            linearParams.weight = 1;
//            mProgressLayout.setLayoutParams(linearParams);
//            mViewContainer.addView(mProgressLayout);
//            mProgressLayout.setVideoProgressLayoutCallback(mVideoProgressLayoutCallback);
//
//            mFeatureBtnContainer = new LinearLayout(mContext);
//            mFeatureBtnContainer.setOrientation(LinearLayout.HORIZONTAL);
//            mFeatureBtnContainer.setGravity(Gravity.CENTER);
//            linearParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,ShareData.PxToDpi_xhdpi(180));
//            linearParams.leftMargin = mPadding;
//            linearParams.rightMargin = mPadding;
//            mFeatureBtnContainer.setLayoutParams(linearParams);
//            mViewContainer.addView(mFeatureBtnContainer);
//        }

        mViewContainer = new FrameLayout(mContext);
//        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getBottomPartHeight(), Gravity.BOTTOM);
        //华为坑爹适配
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.TOP);
        params.topMargin = ShareData.m_screenHeight - getBottomPartHeight();
        mViewContainer.setLayoutParams(params);
        this.addView(mViewContainer);
        {
            int bottomH = ShareData.PxToDpi_xhdpi(180);
            LayoutParams fl;
            mProgressLayout = new VideoProgressLayout(mContext);
            mProgressLayout.setBackgroundColor(0xff0e0e0e);
            //在剩下的高度下内部居中
//            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(300),Gravity.CENTER);
//            fl.bottomMargin =bottomH/2;
            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,Gravity.TOP);
            fl.bottomMargin =bottomH;
            mProgressLayout.setLayoutParams(fl);
            mViewContainer.addView(mProgressLayout);
            mProgressLayout.setVideoProgressLayoutCallback(mVideoProgressLayoutCallback);

            mFeatureBtnContainer = new LinearLayout(mContext);
            mFeatureBtnContainer.setOrientation(LinearLayout.HORIZONTAL);
            mFeatureBtnContainer.setBackgroundColor(Color.BLACK);
            mFeatureBtnContainer.setGravity(Gravity.CENTER);
            fl = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, bottomH, Gravity.BOTTOM);
            mFeatureBtnContainer.setLayoutParams(fl);
            mViewContainer.addView(mFeatureBtnContainer);
        }
        for (int i = 0; i < mVideoBeautifyFeature.length; i++) {
            ImageView btn = new ImageView(mContext);
            btn.setScaleType(ImageView.ScaleType.FIT_CENTER);
            btn.setImageResource(mVideoBeautifyFeature[i]);
            btn.setTag(i);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            btn.setLayoutParams(btnParams);
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null && mCallback.canClick()) {
                        int tag = (int)v.getTag();
                        if (tag == 0) {
                            TongJi2.AddCountByRes(getContext(),R.integer.滤镜);
                            MyBeautyStat.onClickByRes(R.string.视频美化页_打开视频滤镜);
                        } else if (tag == 1) {
                            TongJi2.AddCountByRes(getContext(),R.integer.水印);
                            MyBeautyStat.onClickByRes(R.string.视频美化页_打开视频文字);
                        } else if (tag == 2) {
                            TongJi2.AddCountByRes(getContext(),R.integer.音乐);
                            MyBeautyStat.onClickByRes(R.string.视频美化页_打开视频音乐);
                        }
                        mCallback.onClickFeature(tag);
                    }
                }
            });
            mFeatureBtnContainer.addView(btn);
        }

        mWrapper.mVideoView.addOnItemProgressListener(mProgressLayout.mVideoItemProgressListener);
    }

    public void setIsActivePage(boolean isActive)
    {
        if(isActive){
            mActionBar.setOnActionbarMenuItemClick(mOnItemClick);
        }
    }

    public void setCallback(VideoBottomViewCallback callback) {
        this.mCallback = callback;
    }


    private HashMap<String, Object> mPageParams;
    @Override
    public void SetData(HashMap<String, Object> params) {
        mProgressLayout.setData(mWrapper.mVideoInfos);
        mPageParams = (HashMap<String, Object>) params.clone();
        mPageParams.put("videos", mWrapper.mVideoInfos);
    }


    @Override
    public void onPageResult(int siteID, HashMap<String, Object> params) {
        if (params != null) {
            HashMap<String, Object> videoParams = (HashMap<String, Object>) params.clone();
            if(siteID == SiteID.CAPTURE_VIDEO || siteID == SiteID.VIDEO_ALBUM)
            {
                Object obj = videoParams.get("videos");
                if (obj != null)
                {
                    ArrayList<VideoInfo> videoInfos = (ArrayList<VideoInfo>) obj;
                    String[] paths = new String[videoInfos.size()];
                    boolean hasVideoAdded = false;
                    int firstVideoIndexBeforeAdded = mWrapper.mVideoInfos.size();

                    FilterRes filterRes = null;
                    for (int i = 0; i < videoInfos.size(); i++) {
                        hasVideoAdded = true;
                        mProgressLayout.addVideo(videoInfos.get(i));
                        paths[i] = videoInfos.get(i).mClipPath;
                        filterRes = VideoResMgr.getFilterRes(mContext, videoInfos.get(i).mFilterUri);
                    }
                    mWrapper.mVideoInfos.addAll(videoInfos);
                    mWrapper.mVideoView.addVideos(paths);
                    // 选中新加的视频的第一个
                    if (hasVideoAdded) {
                        mWrapper.isModify = true;
                        mWrapper.adjustMusicAndText();
                        if (siteID == SiteID.CAPTURE_VIDEO) {
                            if (filterRes != null) {
                                mWrapper.mVideoView.changeFilter(mWrapper.mVideoInfos.size() - 1, filterRes);
                            }
                        }
                        if (siteID == SiteID.CAPTURE_VIDEO && firstVideoIndexBeforeAdded != -1) {
                            mProgressLayout.setSelectedVideo(firstVideoIndexBeforeAdded);
                            enterSingleVideoBeautify(firstVideoIndexBeforeAdded);
                        }
                    }
                }
            } else if (siteID == SiteID.THEME_INTRO_PAGE || siteID == SiteID.THEME_PAGE) {
                if (mVideoFeaturePage != null) {
                    mVideoFeaturePage.onPageResult(siteID, params);
                }
            }
        }
    }

    @Override
    public int getBottomPartHeight() {
//        return ShareData.PxToDpi_xhdpi(300) + ShareData.PxToDpi_xhdpi(180);
        // FIXME: 2018/2/5  ShareData.m_screenHeight可能变
        return ShareData.m_screenHeight - mWrapper.mActionBarHeight - mWrapper.mVideoHeight;
    }

    @Override
    public void onPause() {
        if (mVideoFeaturePage != null) {
            mVideoFeaturePage.onPause();
            return;
        }
    }

    @Override
    public void onBack() {
        if (mVideoFeaturePage != null)
        {
            mVideoFeaturePage.onBack();
        } else if (mTransitionDetailsLayout != null)
        {
            //先设置模式再退出
            mWrapper.setCurrentMode(ProcessMode.Normal);
            mWrapper.mVideoView.exitTransition();
            mTransitionDetailsLayout.disapear();
            mProgressLayout.resumeScroll(false);
            initListener();
        } else
        {
            mSite.onBack();
        }
    }

    @Override
    public void onClose()
    {
        mWrapper.mVideoView.removeOnItemProgressListener(mProgressLayout.mVideoItemProgressListener);
        mProgressLayout.clear();

        mProgressLayout.setVideoProgressLayoutCallback(null);
        mVideoProgressLayoutCallback = null;

        if(mTransitionDetailsLayout != null)
        {
            mTransitionDetailsLayout.setTransitionDetailsLayout(null);
        }
        mTransitionLayoutCallback = null;

        if(mActionBar != null)
        {
            mActionBar.setOnActionbarMenuItemClick(null);
        }
        mOnItemClick = null;

        mCallback = null;
        if(mWrapper != null && mWrapper.mVideoView != null)
        {
            mWrapper.mVideoView.release();
            mWrapper.mVideoView = null;
        }
    }

    private void initListener()
    {
        // 更新状态栏
        mActionBar.setUpLeftImageBtn(R.drawable.framework_back_btn);
        mActionBar.setUpRightImageBtn(R.drawable.framework_video_save);
        mActionBar.getLeftImageBtn().setPadding(0, 0, ShareData.PxToDpi_xhdpi(20), 0);
        mActionBar.getRightImageBtn().setPadding(ShareData.PxToDpi_xhdpi(20), 0, 0, 0);
        mActionBar.setUpActionbarTitle("");
        mActionBar.setOnActionbarMenuItemClick(mOnItemClick);
    }

    // 以下为各接口的实现
    private int mSelectedVideoIndex;
    private int mSelectedTransitionIndex;
    private VideoProgressLayout.VideoProgressLayoutCallback mVideoProgressLayoutCallback = new VideoProgressLayout.VideoProgressLayoutCallback() {
        @Override
        public void onTransitionSelected(int transitionIndex, VideoGroupInfo videoGroupInfo) {

            MyBeautyStat.onClickByRes(R.string.视频美化页_打开转场动画);

            mSelectedTransitionIndex = transitionIndex;

            if (mTransitionDetailsLayout == null) {
                mTransitionDetailsLayout = new TransitionDetailsLayout(mContext);
//                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getBottomPartHeight(), Gravity.BOTTOM);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.TOP);
                params.topMargin = ShareData.m_screenHeight - getBottomPartHeight();
                mTransitionDetailsLayout.setLayoutParams(params);
            }
            mTransitionDetailsLayout.show(videoGroupInfo);
            mTransitionDetailsLayout.setTransitionDetailsLayout(mTransitionLayoutCallback);
            if (mTransitionDetailsLayout.getParent() != null) {
                ViewParent parent = mTransitionDetailsLayout.getParent();
                ((ViewGroup)parent).removeView(mTransitionDetailsLayout);

            }
            VideoBottomPage.this.addView(mTransitionDetailsLayout);

            mWrapper.setCurrentMode(ProcessMode.Transition);

            mWrapper.pauseAll();
            mProgressLayout.pauseScroll();
            mActionBar.setLeftImageBtnVisibility(View.GONE);
            mActionBar.setRightImageBtnVisibility(View.GONE);
            mActionBar.setUpActionbarTitle(mContext.getString(R.string.transitionEffect), Color.WHITE, 16);
        }

        @Override
        public boolean canAddTransition(int index)
        {
            if(mWrapper.mVideoInfos != null)
            {
                int size = mWrapper.mVideoInfos.size();
                long preVideoTime = 0;
                long afterVideoTime = 0;
                if(index < size)
                {
                    preVideoTime = mWrapper.mVideoInfos.get(index).GetClipTime();
                }
                if(index + 1 < size)
                {
                    afterVideoTime = mWrapper.mVideoInfos.get(index + 1).GetClipTime();
                }
                if(preVideoTime >= 2000 && afterVideoTime >= 2000)
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onClickVideo(int index) {
            enterSingleVideoBeautify(index);
        }


        @Override
        public void onScrolledByUser(int index, float progress) {
            mWrapper.pauseAll();
            long curDuration = (long)(progress * mWrapper.mVideoInfos.get(index).GetClipTime());
            mWrapper.seekTo(index, curDuration,mWrapper.getBeforeTotalDuration(index) + curDuration);
//            mWrapper.mVideoView.seekTo((long)(progress * mWrapper.getVideosDuration()));
        }

//        private boolean isVideoPlayingBeforeScroll;
        @Override
        public void onStartScroll() {
//            isVideoPlayingBeforeScroll = mWrapper.mVideoView.isPlaying();
            mWrapper.setIsDraggingProgress(true);
            if (mWrapper.mVideoView.isPlaying()) {
                mWrapper.pauseAll();
            }
        }

        @Override
        public void onStopScroll() {
            mWrapper.setIsDraggingProgress(false);
            mWrapper.showPlayBtn();
//            if (isVideoPlayingBeforeScroll) {
//                mWrapper.resumeAll(false);
//            }
        }

        @Override
        public void startDragVideo(int position) {
            mWrapper.pauseAll();
        }

        @Override
        public void onClickAddVideoBtn() {

            MyBeautyStat.onClickByRes(R.string.视频美化页_添加视频);

            int usableTime = (int) (VideoConfig.DURATION_FREE_MODE - mWrapper.getVideosDuration());
            int videoCount = mWrapper.mVideoInfos.size();
            boolean countValid = videoCount < VideoConfig.MAX_NUM;
            if(usableTime > VideoConfig.DURATION_LIMIT && countValid){
                mWrapper.pauseAll();
                int size = mWrapper.mVideoInfos.size();
                HashMap<String, Object> params = new HashMap<>();
                params.put("video_len", size);
                params.put("usable_time", usableTime);
                params.put("ratio", mWrapper.mPlayRatio);
                mSite.onVideoAlbum(getContext(), params);
            } else{
                String dialogText;
                if (!countValid) {
                    dialogText = getResources().getString(R.string.video_beyond_limit);
                } else {
                    dialogText = getResources().getString(R.string.video_exceeded_limit);
                }
                final InterphotoDlg numDialog = new InterphotoDlg((Activity) getContext(), R.style.waitDialog);
                numDialog.getWindow().setWindowAnimations(R.style.PopupAnimation);
                numDialog.SetTitle(dialogText);
                numDialog.SetBtnType(InterphotoDlg.POSITIVE);
                numDialog.setCancelable(true);
                numDialog.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback()
                {
                    @Override
                    public void onOK()
                    {
                        numDialog.dismiss();
                    }

                  ;  @Override
                    public void onCancel()
                    {
                        numDialog.dismiss();
                    }
                });
                numDialog.show();
            }
        }

        @Override
        public void changeVideoOrder(final int fromPosition, final int toPosition, final int index, final float progress) {
			mWrapper.isModify = true;
			mWrapper.changeVideoOrder(fromPosition, toPosition, index, progress);
        }
    };

    private TransitionDetailsLayout.TransitionDetailsLayoutCallback mTransitionLayoutCallback = new TransitionDetailsLayout.TransitionDetailsLayoutCallback() {
        @Override
        public void onApplyEffetToAllTransition(TransitionDataInfo transitionDataInfo) {
            mWrapper.isModify = true;
            mProgressLayout.applyAllTransitionEffectToVideos(transitionDataInfo);
            int size = mWrapper.mVideoInfos.size() - 1;
            if(size > 0)
            {
                int[] tranIds = new int[size];
                for (int i = 0; i < mWrapper.mVideoInfos.size() - 1; i++) {
                    if(transitionDataInfo.mID == TransitionItem.ALPHA || transitionDataInfo.mID == TransitionItem.BLUR)
                    {
                        if(canAddTransition(i))
                            tranIds[i] = transitionDataInfo.mID;
                        else
                        {
                            TransitionDataInfo dataInfo = mProgressLayout.getTransitionDataInfoByIndex(i);
                            if(dataInfo != null)
                            {
                                tranIds[i] = dataInfo.mID;
                            }
                            else
                            {
                                tranIds[i] = TransitionItem.NONE;
                            }
                        }
                    }
                    else
                    {
                        tranIds[i] = transitionDataInfo.mID;
                    }
                }
                mWrapper.mVideoView.setTransitions(tranIds);
            }
//            onBack();
            //下面音乐没恢复 /先设置模式再退出
            mWrapper.setCurrentMode(ProcessMode.Normal);
            mWrapper.resumeAll(true);
            mProgressLayout.restart();
            initListener();
            mTransitionDetailsLayout.disapear();
        }

        @Override
        public boolean canAddTransition(int index)
        {
            if(mWrapper.mVideoInfos != null)
            {
                int size = mWrapper.mVideoInfos.size();
                long preVideoTime = 0;
                long afterVideoTime = 0;
                if(index < size)
                {
                    preVideoTime = mWrapper.mVideoInfos.get(index).GetClipTime();
                }
                if(index + 1 < size)
                {
                    afterVideoTime = mWrapper.mVideoInfos.get(index + 1).GetClipTime();
                }
                if(preVideoTime >= 2000 && afterVideoTime >= 2000)
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onBack() {
            MyBeautyStat.onClickByRes(R.string.转场动画页_退出转场动画);
            VideoBottomPage.this.onBack();
        }

        @Override
        public void onTransitionEffectSelected(VideoGroupInfo videoGroupInfo) {
            if (videoGroupInfo.mTransitionDataInfo.mID != TransitionItem.NONE) {
                mWrapper.isModify = true;
            }
            mProgressLayout.setPositionTransitionType(videoGroupInfo);
            mWrapper.mVideoView.setTransition(videoGroupInfo.mGroupIndex, videoGroupInfo.mTransitionDataInfo.mID);
        }

        @Override
        public void onDispear() {
            VideoBottomPage.this.removeView(mTransitionDetailsLayout);
            mTransitionDetailsLayout = null;
        }
    };

    private ActionBar.onActionbarMenuItemClick mOnItemClick = new ActionBar.onActionbarMenuItemClick() {
        @Override
        public void onItemClick(int id) {
            if (id == ActionBar.LEFT_MENU_ITEM_CLICK) {
                mSite.onBack();
            } else if (id == ActionBar.RIGHT_MENU_ITEM_CLICK) {
                mSite.onShareClick();
            }
        }
    };



    private void startAlphaAnimation(View v, float startAlpha, float endAlpha) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.ALPHA, startAlpha, endAlpha);
        animator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                setUiEnable(true);
            }

            @Override
            public void onAnimationStart(Animator animation)
            {
                setUiEnable(false);
            }
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }


    private VideoFeaturePageSite mVideoFeatureSite = new VideoFeaturePageSite() {
        @Override
        public void onBack() {

            removeView(mVideoFeaturePage);
            if (mVideoFeaturePage != null) {
                mVideoFeaturePage.onClose();
                mVideoFeaturePage = null;
            }
            mProgressLayout.resumeScroll(true);
            initListener();
//            startAlphaAnimation(mFeatureBtnContainer, 0, 1);
            mWrapper.setCurrentMode(ProcessMode.Normal);
        }

        @Override
        public void okSpeedRateOk(int videoIndex,VideoInfo info)
        {
            mProgressLayout.updateVideo(videoIndex,info);
        }


        @Override
        public int onClickFeature() {
            startAlphaAnimation(mFeatureBtnContainer, 1, 0);
            return mProgressLayout.getSelectVideoIndex();
        }

        @Override
        public void onClipVideoOk() {
            mProgressLayout.refreshSelf(mWrapper.mVideoInfos);
        }

        @Override
        public void onSplitVideoOk(VideoInfo left, VideoInfo right) {
            mProgressLayout.splitVideo(left, right);
            mWrapper.resumeAll(true);
        }

        @Override
        public void onDeleteVideoOk(int videoIndex) {
            mProgressLayout.deleteVideo(videoIndex);
        }

        @Override
        public void onCopyVideoOk(int videoIndex,VideoInfo info) {
            mProgressLayout.copyVideo(videoIndex, info);
            mWrapper.resumeAll(true);
        }

    };

    private void enterSingleVideoBeautify(int index) {
        MyBeautyStat.onClickByRes(R.string.视频美化页_打开二级菜单);

        mWrapper.setCurrentMode(ProcessMode.Edit);
        mSelectedVideoIndex = index;
        mWrapper.mCurrentBeautifiedVideo = mWrapper.mVideoInfos.get(mSelectedVideoIndex);
        mPageParams.put("videoIndex", index);

        if (mVideoFeaturePage == null) {
            mVideoFeaturePage = new VideoFeaturePage(mContext, mVideoFeatureSite, mWrapper);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mVideoFeaturePage.setLayoutParams(params);
            mWrapper.mVideoView.enterSingleVideoPlay(mSelectedVideoIndex);
        }
        if (mVideoFeaturePage.getParent() != null) {
            ViewParent parent = mVideoFeaturePage.getParent();
            ((ViewGroup)parent).removeView(mVideoFeaturePage);
        }
        VideoBottomPage.this.addView(mVideoFeaturePage);

        mProgressLayout.pauseScroll();
        // 进入二级菜单，播放一次视频之后暂停
        mWrapper.mVideoView.setLooping(false);
        mVideoFeaturePage.SetData(mPageParams);
        mVideoFeaturePage.show();
    }

    /**
     * 检查裁剪视频的路径是否合法
     */
    private void checkClippedVideoValidity() {
        if(mWrapper.mVideoInfos != null)
        {
            VideoInfo info;
            int size = mWrapper.mVideoInfos.size();
            for(int i = 0; i < size; i ++)
            {
                info = mWrapper.mVideoInfos.get(i);
                if(!FileUtils.isFileExist(info.mPath))
                {
                    mVideoListDeleted.add(i);
                    mWrapper.mVideoInfos.remove(i);
                    i --;
                    size --;
                }
            }
        }
    }

    private List<Integer> mVideoListDeleted = new ArrayList<>();
    private void takeSomeActionWhenVideoInvalid() {
        mVideoListDeleted.clear();
        checkClippedVideoValidity();
        if (!(mWrapper.mVideoInfos.size() > 0)) {
            // 返回视频相册
            mSite.onBackToAlbum(getContext());
        } else if (mVideoListDeleted.size() > 0) {
            // 刷新视频播放的view
            for (Integer index : mVideoListDeleted) {
                mWrapper.mVideoView.deleteVideo(index);
            }
            mVideoListDeleted.clear();
        }
    }

    @Override
    public void onResume()
    {
        takeSomeActionWhenVideoInvalid();
        mWrapper.onResumeAll();
    }
}

