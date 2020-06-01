package cn.poco.video.videoFeature;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import cn.poco.beautify.WaitDialog1;
import cn.poco.framework.BaseSite;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.FileUtil;
import cn.poco.utils.InterphotoDlg;
import cn.poco.video.VideoConfig;
import cn.poco.video.frames.VideoPage;
import cn.poco.video.helper.TimeFormatter;
import cn.poco.video.page.ProcessMode;
import cn.poco.video.page.VideoModeWrapper;
import cn.poco.video.process.ClipVideoTask;
import cn.poco.video.process.OnProcessListener;
import cn.poco.video.process.SplitVideoTask;
import cn.poco.video.process.ThreadPool;
import cn.poco.video.render.GLVideoView;
import cn.poco.video.render.IVideoPlayer;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.utils.AndroidUtil;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.videoFeature.layout.VideoSelectedLayout;
import cn.poco.video.videoFeature.view.FeatureMenuList;
import cn.poco.video.videoFilter.VideoFilterPage;
import cn.poco.video.videoFilter.VideoFilterSite;
import cn.poco.video.videoFrame.VideoFrameAdjustPage;
import cn.poco.video.videoFrame.VideoFrameAdjustSite;
import cn.poco.video.videoSpeed.VideoSpeedPage;
import cn.poco.video.videoSpeed.VideoSpeedSite;
import cn.poco.video.view.ActionBar;

/**
 * Created by Simon Meng on 2018/1/23.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoFeaturePage extends VideoPage{

    private long mMaxTime = 3 * 60 * 1000 - 1000;
    private Context mContext;
    private VideoFeaturePageSite mSite;
    private VideoModeWrapper mWrapper;

    private ActionBar mActionBar;
    private VideoInfo mCurrentInfo;


    public VideoSelectedLayout mVideoSelectLayout;
    private FeatureMenuList mFeatureMenuList;
    private WaitDialog1 mWaitDialog;

    private float mLeftProgress, mRightProgress = 1;
    private FrameLayout mViewContainer;
    private int mBottomH = ShareData.PxToDpi_xhdpi(180);   //底部高度
    private int mMoveOffset = mBottomH /2;    // mFeatureMenuList隐藏后 mVideoSelectLayout向下居中
    private TextView mIntroduceText;

    private Dialog mCurDialog;  //退出页面时清一清对话框，防止显示

    public VideoFeaturePage(Context context, BaseSite baseSite, VideoModeWrapper wrapper) {
        super(context, baseSite);
        mContext = context;
        mWrapper = wrapper;

        mActionBar = mWrapper.mActionBar;
        mActionBar.setUpActionbarTitle(getContext().getString(R.string.video_edit), Color.WHITE, 16);
        mActionBar.setLeftImageBtnVisibility(View.GONE);
        mActionBar.setRightImageBtnVisibility(View.GONE);
        mCurrentInfo = mWrapper.mCurrentBeautifiedVideo;

        mSite = (VideoFeaturePageSite) baseSite;
        initView();
        initListener();

        mFeatureMenuList.setCopyFeatureVisibility(mWrapper.showCopyFeature());
        mFeatureMenuList.setDeleteFeatureVisibility(mWrapper.showDeleteFeature());
        mFeatureMenuList.setSplitFeatureVisibility(mWrapper.showSplitFeature());
        MyBeautyStat.onPageStartByRes(R.string.视频二级菜单页);
    }



    private void initView() {
        LayoutParams params;
//        LinearLayout.LayoutParams linearParams;
//        mViewContainer = new LinearLayout(mContext);
//        mViewContainer.setBackgroundColor(0x0e0e0e);
//        mViewContainer.setOrientation(LinearLayout.VERTICAL);
//        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getBottomPartHeight(), Gravity.BOTTOM);
//        mViewContainer.setLayoutParams(params);
//        mViewContainer.setBackgroundColor(Color.BLACK);
//        mViewContainer.setClickable(true);
//        this.addView(mViewContainer);
//
//        mVideoSelectLayout = new VideoSelectedLayout(mContext);
//        linearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        mVideoSelectLayout.setLayoutParams(linearParams);
//        mViewContainer.addView(mVideoSelectLayout);
//        mLeftProgress = mVideoSelectLayout.getLeftProgress();
//        mRightProgress = mVideoSelectLayout.getRightProgress();
//
//        linearParams = new LinearLayout.LayoutParams(ShareData.m_screenWidth, mBottomH);
//        linearParams.leftMargin = -ShareData.m_screenWidth;
//        mFeatureMenuList = new FeatureMenuList(mContext);
//        mFeatureMenuList.setLayoutParams(linearParams);
//        mViewContainer.addView(mFeatureMenuList);
//        mFeatureMenuList.setVisibility(View.GONE);
//        mFeatureMenuList.setFeatureMenuCallback(mFeatureListCallback);


        mViewContainer = new FrameLayout(mContext);
        mViewContainer.setBackgroundColor(0xff0e0e0e);
//        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getBottomPartHeight(), Gravity.BOTTOM);
        //华为坑爹适配
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.TOP);
        params.topMargin = ShareData.m_screenHeight - getBottomPartHeight();
        mViewContainer.setLayoutParams(params);
        mViewContainer.setClickable(true);
        this.addView(mViewContainer);
        {
            mIntroduceText = new TextView(mContext);
            mIntroduceText.setText("顶部文案");
            mIntroduceText.setVisibility(View.INVISIBLE);
            mIntroduceText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            mIntroduceText.setTextColor(0xff666666);
//            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL);
//            params.topMargin = ShareData.PxToDpi_xhdpi(76);
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            params.topMargin = ShareData.PxToDpi_xhdpi(60);
            mIntroduceText.setVisibility(View.INVISIBLE);
            mIntroduceText.setLayoutParams(params);
            mViewContainer.addView(mIntroduceText);

            mVideoSelectLayout = new VideoSelectedLayout(mContext);
            //在剩下的高度下内部居中
            params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.bottomMargin =mBottomH;
            //下面这种也可以
//            params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,Gravity.CENTER);
//            params.bottomMargin =mBottomH/2;
            mVideoSelectLayout.setLayoutParams(params);
            mViewContainer.addView(mVideoSelectLayout);
            mLeftProgress = mVideoSelectLayout.getLeftProgress();
            mRightProgress = mVideoSelectLayout.getRightProgress();

            params = new LayoutParams(ShareData.m_screenWidth, mBottomH,Gravity.BOTTOM);
            params.leftMargin = -ShareData.m_screenWidth;
            mFeatureMenuList = new FeatureMenuList(mContext);
            mFeatureMenuList.setLayoutParams(params);
            mViewContainer.addView(mFeatureMenuList);
            mFeatureMenuList.setVisibility(View.GONE);
            mFeatureMenuList.setFeatureMenuCallback(mFeatureListCallback);
        }
        mWaitDialog = new WaitDialog1(mContext, R.style.waitDialog);
        mWaitDialog.setMessage(getResources().getString(R.string.processing));
    }

    private void initListener() {
        mWrapper.mVideoView.addOnProgressListener(mOnProgressListener);
        mActionBar.setOnActionbarMenuItemClick(new ActionBar.onActionbarMenuItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == ActionBar.LEFT_MENU_ITEM_CLICK) {
                    onBack();
                } else if (id == ActionBar.RIGHT_MENU_ITEM_CLICK) {
                    carryDifferentFeatureAction();
                }
            }
        });

        mVideoSelectLayout.setVideoSelectedCallback(new VideoSelectedLayout.VideoSelectLayoutCallback() {
            @Override
            public void onLeftProgress(float leftProgress, float rightProgress) {
                mWrapper.setIsDraggingProgress(true);
                pausePlay();
                mLeftProgress = leftProgress;
                mRightProgress = rightProgress;
                float diff = mRightProgress - mLeftProgress;
                mVideoSelectLayout.mCurrentTime.setText(TimeFormatter.makeVideoTimeDurationText((long)(diff * mCurrentInfo.mDuration)));

                long currentMoment = (long)(leftProgress * mCurrentInfo.mDuration);
                if (mWrapper.isPlaying()) {
                    mWrapper.pauseAll();
                }
                mWrapper.seekTo(currentMoment);
                // 改变显示标签时间
                mWrapper.showProgressTip((long)(mLeftProgress * mCurrentInfo.getVideoTime(mWrapper.getCurrentMode())), (long)(mRightProgress * mCurrentInfo.getVideoTime(mWrapper.getCurrentMode())), true);
                mVideoSelectLayout.setProgress(leftProgress);
            }

            @Override
            public void onRightProgress(float leftProgress, float rightProgress) {
                mWrapper.setIsDraggingProgress(true);
                pausePlay();
                mLeftProgress = leftProgress;
                mRightProgress = rightProgress;
                float diff = mRightProgress - mLeftProgress;
                mVideoSelectLayout.mCurrentTime.setText(TimeFormatter.makeVideoTimeDurationText((long)(diff * mCurrentInfo.mDuration)));

                long currentMoment = (long)(rightProgress * mCurrentInfo.mDuration);
                if (mWrapper.isPlaying()) {
                    mWrapper.pauseAll();
                }
                mWrapper.seekTo(currentMoment);
                mWrapper.showProgressTip((long)(mLeftProgress * mCurrentInfo.getVideoTime(mWrapper.getCurrentMode())), (long)(mRightProgress * mCurrentInfo.getVideoTime(mWrapper.getCurrentMode())), true);
            }

            @Override
            public void onConfirmProgress(float leftProgress,float rightProgress) {
                mLastProgress = leftProgress;
                mRightProgress = rightProgress;
                reCalculateProgress();
                onRightProgress(leftProgress, mRightProgress);
                mWrapper.setIsDraggingProgress(false);
                mWrapper.resumeAll(false);
                startPlay();
                mWrapper.mVideoView.addOnPlayListener(mOnPlayListener);
            }

            @Override
            public boolean canMoveLeft()
            {
                return canAddTime();
            }

            @Override
            public boolean canMoveRight()
            {
                return canAddTime();
            }

            @Override
            public void onSeekbarProgressChange(float progress) {
                mSplitProgress = progress;
                if (!mWrapper.isPlaying()) {
                    mWrapper.pauseAll();
                }
                mWrapper.seekTo((long)(mSplitProgress * mWrapper.mCurrentBeautifiedVideo.getVideoTime(ProcessMode.SEGENTATION)));
            }

            @Override
            public void onSeekbarDown(float progress)
            {
                mWrapper.setIsDraggingProgress(true);
                mWrapper.pauseAll();
            }

            @Override
            public void onSeekbarUp(float progress)
            {
                mWrapper.setIsDraggingProgress(false);
                mWrapper.resumeAll(false);
            }

            @Override
            public void onProcessChange(float process)
            {
                long curDuration = (long) ((mWrapper.mCurrentBeautifiedVideo.mSelectEndTime-mWrapper.mCurrentBeautifiedVideo.mSelectStartTime)/100 * 100 * process);
                mWrapper.seekTo(curDuration);
            }

            boolean isPlaying;
            @Override
            public void onProcessDown(float process)
            {
                mWrapper.setIsDraggingProgress(true);
                isPlaying = mWrapper.isPlaying();
                mWrapper.pauseAll();
                long curDuration = (long) ((mWrapper.mCurrentBeautifiedVideo.mSelectEndTime-mWrapper.mCurrentBeautifiedVideo.mSelectStartTime)/100 * 100 * process);
                mWrapper.seekTo(curDuration);
                mWrapper.mVideoView.removeOnPlayListener(mOnPlayListener);
            }

            @Override
            public void onProcessUp(float process)
            {
                mWrapper.setIsDraggingProgress(false);
                long curDuration = (long) ((mWrapper.mCurrentBeautifiedVideo.mSelectEndTime-mWrapper.mCurrentBeautifiedVideo.mSelectStartTime)/100 * 100 * process);
                mWrapper.seekTo(curDuration);
                if(isPlaying || (mWrapper.getCurrentMode() != null && mWrapper.getCurrentMode() == ProcessMode.CANVASADJUST)){
                    mWrapper.resumeAll(false);
                }else{
                    mWrapper.showPlayBtn();
                }
                if(mWrapper.getCurrentMode() == ProcessMode.CLIP)
                {
                    mWrapper.mVideoView.removeOnPlayListener(mOnPlayListener);
                }
            }
        });

    }

    public void reCalculateProgress()
    {
        long totalTime = mWrapper.getVideosDuration();
        long curDur = mCurrentInfo.GetClipTime();
        float right = (mMaxTime - totalTime + curDur) / (float)mCurrentInfo.mDuration + mLeftProgress;;
        if(mRightProgress >= right)
        {
            mRightProgress = right;
        }
    }

    public boolean canAddTime()
    {
        long totalTime = mWrapper.getVideosDuration();
        long curDur = mCurrentInfo.GetClipTime();
        float diff = mRightProgress - mLeftProgress;
        long curClipTime = (long)(diff * mCurrentInfo.mDuration);
        long d = mMaxTime - (totalTime - curDur + curClipTime);
        if(d >= 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private int mCurSelectedVideoIndex = -1;
    @Override
    public void SetData(HashMap<String, Object> params) {
        HashMap<String, Object> pageParams = (HashMap<String, Object>) params.clone();
        if (pageParams != null) {
            Object oj = pageParams.get("videoIndex");
            if (oj instanceof Integer) {
                mCurSelectedVideoIndex = (Integer) oj;
            }
        }
    }
    ;
    @Override
    public int getBottomPartHeight() {
        // FIXME: 2018/2/5  ShareData.m_screenHeight可能变
        return ShareData.m_screenHeight - mWrapper.mActionBarHeight - mWrapper.mVideoHeight;
    }

    @Override
    public void onPause() {
        pausePlay();
    }

    @Override
    public void onPageResult(int siteID, HashMap<String, Object> params) {
        if (siteID == SiteID.THEME_PAGE || siteID == SiteID.THEME_INTRO_PAGE) {
            mVideoFitlerPage.onPageResult(siteID, params);
        }
    }

    private void carryDifferentFeatureAction() {
        if(mWrapper.getCurrentMode() == null){
            onBack();
            return;
        }
        // 右上角打勾保存，视为做出了修改
        mWrapper.isModify = true;
        switch (mWrapper.getCurrentMode()) {
            case CLIP: {
                MyBeautyStat.onClickByRes(R.string.视频裁剪页_保存视频裁剪);
                clipVideo();
                break;
            }

            case CANVASADJUST: {
                // 有内部页面添加神策统计

                break;
            }

            case FILTER: {
                // 有内部页面添加神策统计

                break;
            }

            case SPEEDRATE: {
                // 有内部页面添加神策统计

                break;
            }

            case SEGENTATION: {
                MyBeautyStat.onClickByRes(R.string.视频分割_保存视频分割);
                splitVideo();
                break;
            }

        }
    }

    @Override
    public void onBack() {
        if (mWrapper.getCurrentMode() != ProcessMode.Edit)
        {
            onBack(false);
        } else
        {
            removeListener();
            mWrapper.mVideoView.exitSingleVideoPlay();
            closeHorizontalTransition(mFeatureMenuList, ShareData.m_screenWidth, 0);
            startAlphaAnimation(VideoFeaturePage.this, 1, 0, View.GONE);
            mVideoSelectLayout.clear();

            // 二级菜单返回到美化主页, 重新循环播放视频
            // mWrapper要先恢复模式，否则音乐素材seekTo不正确
            mWrapper.setCurrentMode(ProcessMode.Normal);
            mWrapper.resumeAll(true);
            mWrapper.mVideoView.setLooping(true);
            MyBeautyStat.onClickByRes(R.string.二级菜单列表_收起二级菜单);
        }
    }

    private void onBack(boolean isSave)
    {
        switch (mWrapper.getCurrentMode())
        {
            case CLIP:
            {
                if(!isSave)
                {
                    MyBeautyStat.onClickByRes(R.string.视频裁剪页_退出视频裁剪);
                }
                MyBeautyStat.onPageEndByRes(R.string.视频裁剪页);
                mWrapper.mVideoView.changeVideoPath(mCurrentInfo.mClipPath);
                mWrapper.mVideoView.removeOnPlayListener(mOnPlayListener);
                pausePlay();
                break;
            }
            case SEGENTATION:
            {
                if(!isSave)
                {
                    MyBeautyStat.onClickByRes(R.string.视频分割_退出视频分割);
                }
                MyBeautyStat.onPageEndByRes(R.string.视频分割);
                startAlphaAnimation(mIntroduceText,1,0, View.GONE);
                break;
            }
            case CANVASADJUST:
                mWrapper.showProgressTip();
                break;
        }
        if (mCurVideoPage != null)
        {
            mCurVideoPage.onBack();
        } else
        {
            onBackFeaturePage();
            startVerticalTransition(mVideoSelectLayout, mMoveOffset, 0);
            startAlphaAnimation(mFeatureMenuList, 0, 1, View.VISIBLE);
        }
        boolean hasClip = mCurrentInfo.isHasClipped();
        mVideoSelectLayout.refreshBitmapList(hasClip, mCurrentInfo, ProcessMode.Edit);
        // 三级菜单返回到二级菜单，重新播放一次视频然后暂停
        mWrapper.resumeAll(true);
    }

    private void onBackFeaturePage() {
        pausePlay();
        mCurVideoPage = null;
        mVideoFitlerPage = null;
        mVideoSpeedPage = null;
        mFrameAdjustPage = null;
        mWrapper.setCurrentMode(ProcessMode.Edit);
        mVideoSelectLayout.setFeature(ProcessMode.Edit);
        mFeatureMenuList.setClickable(true);
        // 更新状态栏
        mActionBar.setUpActionbarTitle(getContext().getString(R.string.video_edit), Color.WHITE, 16);
        mActionBar.setLeftImageBtnVisibility(View.GONE);
        mActionBar.setRightImageBtnVisibility(View.GONE);
        initListener();
    }




    private void removeListener() {
        pausePlay();
        mActionBar.setOnActionbarMenuItemClick(null);
        mWrapper.mVideoView.removeOnProgressListener(mOnProgressListener);
        mWrapper.mVideoView.removeOnPlayListener(mOnPlayListener);

    }

    public void show() {
        mVideoSelectLayout.setVideoInfo(mCurrentInfo, ProcessMode.Edit);
        if (mFeatureMenuList.getVisibility() != View.VISIBLE) {
            startHorizontalTransition(mFeatureMenuList, 0, ShareData.m_screenWidth);
        }
    }

    private void openModulePage(ProcessMode videoFeature) {
        if(videoFeature != ProcessMode.COPY && videoFeature != ProcessMode.DELETE)
        {
//            mMoveOffset = (mViewContainer.getHeight() - mVideoSelectLayout.getHeight()) / 2;
            mFeatureMenuList.setClickable(false);
            startAlphaAnimation(mFeatureMenuList, 1, 0, View.GONE);
            mWrapper.setCurrentMode(videoFeature);
            mActionBar.setUpLeftImageBtn(R.drawable.framework_back_btn);
            mActionBar.setUpRightImageBtn(R.drawable.framework_ok_btn);
        }

        switch (videoFeature) {
            case CLIP: {
//                mWrapper.mVideoView.addOnPlayListener(mOnPlayListener);
                MyBeautyStat.onClickByRes(R.string.二级菜单列表_进入裁剪);
                MyBeautyStat.onPageStartByRes(R.string.视频裁剪页);
                // 播放一次视频后在视频起点暂停
                mWrapper.mVideoView.changeVideoPath(mCurrentInfo.getVideoPath(ProcessMode.CLIP));
                mWrapper.showProgressTip((long)(mLeftProgress * mCurrentInfo.getVideoTime(mWrapper.getCurrentMode())), (long)(mRightProgress * mCurrentInfo.getVideoTime(mWrapper.getCurrentMode())), true);
                startVerticalTransition(mVideoSelectLayout, 0, mMoveOffset);
                mActionBar.setUpActionbarTitle(mContext.getString(R.string.clipVideo), Color.WHITE, 16);
                boolean hasClip  = mCurrentInfo.isHasClipped();
                mVideoSelectLayout.setMinClipTime(mWrapper.mVideoView.checkTransition() + VideoConfig.DURATION_LIMIT);
                mVideoSelectLayout.refreshBitmapList(hasClip, mCurrentInfo, ProcessMode.CLIP);
                mLeftProgress = mVideoSelectLayout.getLeftProgress();
                mRightProgress = mVideoSelectLayout.getRightProgress();
                startPlay();
                break;
            }

            case CANVASADJUST: {
                MyBeautyStat.onClickByRes(R.string.二级菜单列表_进入画面);
                // 顺延时间点播放状态，播放一次
                mWrapper.resumeAll(false);

                mFrameAdjustPage = new VideoFrameAdjustPage(getContext(),mFrameAdjustSite,mWrapper);
//                mFrameAdjustPage.setClickable(true);
                mFrameAdjustPage.SetData(null);
                mCurVideoPage = mFrameAdjustPage;
                mActionBar.setUpActionbarTitle(mContext.getString(R.string.canvasAdjust), Color.WHITE, 16);
                LayoutParams fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getBottomPartHeight(),Gravity.BOTTOM);
                addView(mCurVideoPage,fl);
                startAlphaAnimation(mFrameAdjustPage,0,1, View.VISIBLE);
                startVerticalTransition(mVideoSelectLayout, 0, mMoveOffset);
                mIntroduceText.setText(mContext.getResources().getString(R.string.video_frame_adjust_tip));
                startAlphaAnimation(mIntroduceText,0,1, View.VISIBLE);
                break;
            }

            case FILTER: {
                // 顺延时间点播放状态，播放一次
                mWrapper.resumeAll(false);
                MyBeautyStat.onClickByRes(R.string.二级菜单列表_进入滤镜);
                mActionBar.setUpActionbarTitle(mContext.getString(R.string.filter), Color.WHITE, 16);
                mWrapper.mVideoView.setLooping(true);

                mVideoFitlerPage = new VideoFilterPage(getContext(), mFilterSite, mWrapper);
                if (mCurSelectedVideoIndex != -1) {
                    mVideoFitlerPage.setAsPartialFilter(mCurrentInfo, mCurSelectedVideoIndex);
                }
                startAlphaAnimation(mVideoSelectLayout,1,0, View.GONE);
                startAlphaAnimation(mFeatureMenuList,1,0, View.GONE);
                startAlphaAnimation(mVideoFitlerPage,0,1, View.VISIBLE);
                HashMap<String, Object> params = new HashMap<>();
                params.put("videos", mWrapper.mVideoInfos);
                mVideoFitlerPage.SetData(params);

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
                mVideoFitlerPage.setLayoutParams(layoutParams);
                this.addView(mVideoFitlerPage);
                startAlphaAnimation(mVideoFitlerPage,0,1, View.VISIBLE);
                mCurVideoPage = mVideoFitlerPage;
                break;
            }

            case SPEEDRATE: {
                // 顺延时间点播放状态，播放一次
                mWrapper.resumeAll(false);
                MyBeautyStat.onClickByRes(R.string.二级菜单列表_进入速率);

                mVideoSpeedPage = new VideoSpeedPage(getContext(),mVideoSpeedSite,mWrapper);
                mVideoSpeedPage.setClickable(true);
                mVideoSpeedPage.SetData(null);
                mCurVideoPage = mVideoSpeedPage;
//                LayoutParams fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getBottomPartHeight(),Gravity.BOTTOM);
                //坑爹适配
                LayoutParams fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,Gravity.TOP);
                fl.topMargin = ShareData.m_screenHeight - getBottomPartHeight();
                addView(mCurVideoPage,fl);
                startAlphaAnimation(mVideoSpeedPage,0,1, View.VISIBLE);
                mActionBar.setUpActionbarTitle(mContext.getString(R.string.videoSpeed), Color.WHITE, 16);
                break;
            }

            case SEGENTATION: {
                // 顺延时间点播放状态，播放一次
                mWrapper.resumeAll(false);
                MyBeautyStat.onClickByRes(R.string.二级菜单列表_进入分割);
                MyBeautyStat.onPageStartByRes(R.string.视频分割);
                startVerticalTransition(mVideoSelectLayout, 0, mMoveOffset);
                mActionBar.setUpActionbarTitle(mContext.getString(R.string.videoSegmentation), Color.WHITE, 16);
                startAlphaAnimation(mIntroduceText,0,1, View.VISIBLE);
                mIntroduceText.setText(mContext.getResources().getString(R.string.video_segmentation_introduce));
                break;
            }

            case COPY: {
                MyBeautyStat.onClickByRes(R.string.二级菜单列表_复制视频);
//                //从头循环播放视频
//                mWrapper.mVideoView.setLooping(true);
//                mWrapper.resumeAll(true);

                if (mWrapper.isPlaying()) {
                    mWrapper.pauseAll();
                }
//                startVerticalTransition(mVideoSelectLayout, 0, mProgressLayoutOffset);
//                mActionBar.setUpActionbarTitle(mContext.getString(R.string.videoCopy), Color.WHITE, 16);
//                final boolean isPlaying = mWrapper.isPlaying();
//                mWrapper.pauseAll();
                final InterphotoDlg interphotoDlg = new InterphotoDlg((Activity) getContext());
                final boolean canCopy = mWrapper.canAddVideo(mCurrentInfo);
                interphotoDlg.setCancelable(true);
                interphotoDlg.SetPositiveBtnText(getResources().getString(R.string.yes));
                if(canCopy)
                {
                    interphotoDlg.SetNegativeBtnText(getResources().getString(R.string.no));
                    interphotoDlg.SetTitle(getResources().getString(R.string.whetherToCopy));
                }else{
                    interphotoDlg.SetBtnType(InterphotoDlg.NEGATIVE);
                    interphotoDlg.SetTitle(getResources().getString(R.string.video_copy_limit));
                }
                interphotoDlg.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback()
                {
                    @Override
                    public void onOK()
                    {
                        mWrapper.isModify = true;
                        interphotoDlg.dismiss();
                        if(canCopy)
                        {
                            onBackFeaturePage();
                            copyVideo();
                        }
                    }

                    @Override
                    public void onCancel()
                    {
                        interphotoDlg.dismiss();
//                        if(isPlaying){
//                            mWrapper.resumeAll(false);
//                        }
                    }
                });
                interphotoDlg.show();
                mCurDialog = interphotoDlg;
                break;
            }

            case DELETE: {
                MyBeautyStat.onClickByRes(R.string.二级菜单列表_删除视频);
//                //从头循环播放视频
//                mWrapper.mVideoView.setLooping(true);
//                mWrapper.resumeAll(true);
                if (mWrapper.isPlaying()) {
                    mWrapper.pauseAll();
                }

//                mActionBar.setUpActionbarTitle(mContext.getString(R.string.videoDelete), Color.WHITE, 16);
//                final boolean isPlaying = mWrapper.isPlaying();
//                mWrapper.pauseAll();
                final InterphotoDlg interphotoDlg = new InterphotoDlg((Activity) getContext());
                interphotoDlg.SetNegativeBtnText(getResources().getString(R.string.yes));
                interphotoDlg.SetTitle(getResources().getString(R.string.whetherDelete));
                interphotoDlg.SetNegativeBtnText(getResources().getString(R.string.no));
                interphotoDlg.setCancelable(true);
                    interphotoDlg.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback()
                    {
                        @Override
                        public void onOK()
                        {
                            mWrapper.isModify = true;
                            interphotoDlg.dismiss();
                            onBackFeaturePage();
                            deleteVideo();
                        }

                        @Override
                        public void onCancel()
                        {
                            interphotoDlg.dismiss();
//                            if(isPlaying){
//                                mWrapper.resumeAll(false);
//                            }
                        }
                    });
                interphotoDlg.show();
                mCurDialog = interphotoDlg;
                break;
            }
        }
        mVideoSelectLayout.setFeature(videoFeature);
    }

    //裁剪模式使用的监听，用完移除
    private GLVideoView.OnPlayListener mOnPlayListener = new IVideoPlayer.OnPlayListener()
    {
        @Override
        public void onStart()
        {
            if(!mProcessAnimator.isRunning())
            {
                startPlay();
            }
        }

        @Override
        public void onResume()
        {
            //不加这条件会循环调用
            if(!mProcessAnimator.isRunning())
            {
                resumePlay();
            }
        }

        @Override
        public void onPause()
        {
            pausePlay();
        }
    };

    private GLVideoView.OnProgressListener mOnProgressListener = new IVideoPlayer.OnProgressListener()
    {
        @Override
        public void onChanged(float progress, boolean isSeekTo)
        {
            if(!isSeekTo)
            {
                if (mWrapper.getCurrentMode() == ProcessMode.CLIP) {
                    progress = progress > mVideoSelectLayout.getRightProgress() ? mVideoSelectLayout.getRightProgress() : progress;
                    mWrapper.showProgressTip(mWrapper.mVideoView.getCurrentPosition(), mWrapper.mVideoView.getTotalDuration(),false);
                }else
                {
                    mVideoSelectLayout.setProgress(progress);
                }
            } else {
                if (mWrapper.getCurrentMode() == ProcessMode.CLIP)
                {
                    mWrapper.showProgressTip((long) (mLeftProgress * mCurrentInfo.getVideoTime(mWrapper.getCurrentMode())), (long) (mRightProgress * mCurrentInfo.getVideoTime(mWrapper.getCurrentMode())), true);
                }
            }

            //画布那里手动seekTo 0
            if(progress == 0){
                mVideoSelectLayout.setProgress(progress);
            }
        }
    };


    private FeatureMenuList.FeatureMenuListCallback mFeatureListCallback = new FeatureMenuList.FeatureMenuListCallback() {
        @Override
        public void onClickMenuFeature(ProcessMode videoFeature) {
            openModulePage(videoFeature);
        }

        @Override
        public void onBack() {
            VideoFeaturePage.this.onBack();
        }
    };

    // 过渡动画函数
    private void startHorizontalTransition(View v, float startPosition, float endPosition) {
        if (v.getVisibility() != VISIBLE) {
            v.setVisibility(View.VISIBLE);
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.TRANSLATION_X, startPosition, endPosition);
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

    private void closeHorizontalTransition (final View v, float startPosition, float endPosition) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.TRANSLATION_X, startPosition, endPosition);
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
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                VideoFeaturePage.this.setVisibility(View.GONE);
                mSite.onBack();
            }
        });
    }

    private void startVerticalTransition(View v, float startPosition, float endPosition) {
        if (v.getVisibility() != VISIBLE) {
            v.setVisibility(View.VISIBLE);
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, startPosition, endPosition);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    private Animator startAlphaAnimation(final View v, float startAlpha, float endAlpha, final int visibility) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.ALPHA, startAlpha, endAlpha);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(visibility);
                setUiEnable(true);
            }

            @Override
            public void onAnimationStart(Animator animation)
            {
                setUiEnable(false);
            }

        });
        return animator;
    }



    private String mOutputPath;
    private ClipVideoTask mClipVideoTask;
    private void clipVideo() {
        long startTime = (long)(mLeftProgress * mCurrentInfo.mDuration);
        long endTime = (long)(mRightProgress * mCurrentInfo.mDuration);
        if (isTimeExceedLimit(startTime, endTime)) {
            Toast.makeText(getContext(), R.string.exceedTimeLimit, Toast.LENGTH_SHORT).show();
            return;
        }
        mWrapper.mVideoView.removeOnPlayListener(mOnPlayListener);
        pausePlay();
        mWrapper.pauseAll();
        mWaitDialog.show();
        mOutputPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
        mCurrentInfo.mSelectStartTime = startTime;
        mCurrentInfo.mSelectEndTime = endTime;

        mClipVideoTask = new ClipVideoTask(mContext, mWrapper.mCurrentBeautifiedVideo.mPath,
                                           mOutputPath, startTime, endTime);
        mClipVideoTask.setOnProcessListener(mClipProcessListener);
        ThreadPool.getInstance().execute(mClipVideoTask);
    }

    private boolean isTimeExceedLimit(long startTime, long endTime) {
        long total = 0;
        for (VideoInfo videoInfo : mWrapper.mVideoInfos) {
            if (videoInfo != mCurrentInfo) {
                total += videoInfo.GetClipTime();
            }
        }
        total += (endTime - startTime);
        boolean result = total > VideoConfig.DURATION_FREE_MODE ? true : false;
        return result;
    }

    private String mVideo1Path, mVideo2Path;
    private SplitVideoTask mSplitVideoTask;
    private float mSplitProgress;
    private void splitVideo() {

        long duration = mWrapper.mCurrentBeautifiedVideo.GetClipTime();
        long duration1 = (long) (mSplitProgress * duration);
        long duration2 = (long) ((1-mSplitProgress) * duration);
        if(duration1 < VideoConfig.DURATION_LIMIT || duration2 < VideoConfig.DURATION_LIMIT)
        {
            final InterphotoDlg numDialog = new InterphotoDlg((Activity) getContext(), R.style.waitDialog);
            numDialog.getWindow().setWindowAnimations(R.style.PopupAnimation);
            numDialog.SetTitle(getContext().getString(R.string.video_too_short));
            numDialog.SetBtnType(InterphotoDlg.POSITIVE);
            numDialog.setCancelable(true);
            numDialog.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback()
            {
                @Override
                public void onOK()
                {
                    numDialog.dismiss();
                }

                @Override
                public void onCancel()
                {
                    numDialog.dismiss();
                }
            });
            numDialog.show();
            mCurDialog = numDialog;
        }else
        {
            mWrapper.pauseAll();
            mWaitDialog.show();
            mVideo1Path = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
            mVideo2Path = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
            mSplitVideoTask = new SplitVideoTask(mContext, mWrapper.mCurrentBeautifiedVideo.mClipPath,
                                                 mSplitProgress, duration, new String[]{mVideo1Path, mVideo2Path});
            mSplitVideoTask.setOnProcessListener(mSplitProcessListener);

            ThreadPool.getInstance().execute(mSplitVideoTask);
        }
    }

    private void deleteVideo() {
        int index = mWrapper.mVideoInfos.indexOf(mCurrentInfo);
        if (index != -1) {
            mWrapper.deleteVideo(index);
            mWrapper.mVideoView.deleteVideo(index);
            mSite.onDeleteVideoOk(index);
            onBack();
        }
    }


    private void copyVideo() {
        final int index = mWrapper.mVideoInfos.indexOf(mCurrentInfo);
        if (index != -1) {
            if(!mCurrentInfo.isHasClipped()){
                copyFinish(index,mCurrentInfo.mClipPath);
            }else{
                final WaitDialog1 dialog1 = new WaitDialog1(getContext());
                dialog1.setMessage(getResources().getString(R.string.processing));
                dialog1.show();
                ThreadPool.getInstance().execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final String path = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
                        final boolean isSucceed = FileUtil.copySDFile(mCurrentInfo.mClipPath,path);
                        AndroidUtil.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                dialog1.dismiss();
                                if(isSucceed){
                                    copyFinish(index,path);
                                }
                            }
                        });
                    }
                });
            }
        }
    }
    private void copyFinish(int index,String clipPath)
    {
        VideoInfo dstInfo = new VideoInfo();
        dstInfo.Copy(mCurrentInfo,clipPath);
        mWrapper.mVideoInfos.add(index+1,dstInfo);
        mWrapper.mVideoView.copyVideo(index,clipPath);
        mWrapper.refreshCurVideoDuration();
        mSite.onCopyVideoOk(index,dstInfo);
        onBack();
    }


    private OnProcessListener mSplitProcessListener = new OnProcessListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void onFinish() {
            mWaitDialog.dismiss();
            mWrapper.mVideoView.splitVideoPath(mVideo1Path, mVideo2Path);
            VideoInfo splitLeftOne = mWrapper.mCurrentBeautifiedVideo;
            splitLeftOne.deleteClipPath(mVideo1Path);
            VideoInfo splitRightOne = new VideoInfo();
            mWrapper.mVideoInfos.add(mWrapper.mVideoInfos.indexOf(splitLeftOne)+1,splitRightOne);
            splitRightOne.Copy(splitLeftOne,mVideo2Path);
            long start = splitLeftOne.GetStartTime();
            long end = splitLeftOne.GetEndTime();
            long centerSplit = (long) (start+splitLeftOne.GetClipTime() * mSplitProgress);
            splitLeftOne.setClipTime(start,centerSplit);
            splitRightOne.setClipTime(centerSplit,end);
            //重新刷一下时间
            mWrapper.refreshCurVideoDuration(splitLeftOne);
            mWrapper.refreshCurVideoDuration(splitRightOne);
            onBack(true);
            onBack();
//            onBackFeaturePage();
            mSite.onSplitVideoOk(splitLeftOne, splitRightOne);
        }

        @Override
        public void onError(String message) {
            mWaitDialog.dismiss();
            Toast.makeText(getContext(), R.string.video_modify_failed, Toast.LENGTH_SHORT).show();
        }
    };

    private OnProcessListener mClipProcessListener = new OnProcessListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void onFinish() {
            mCurrentInfo.mClipPath = mOutputPath;
//            mCurrentInfo.mDuration = (long)((mRightProgress - mLeftProgress) * mCurrentInfo.mDuration);
            mWaitDialog.dismiss();
            mWrapper.mVideoView.changeVideoPath(mOutputPath);
            mWrapper.refreshCurVideoDuration();
            mFeatureMenuList.setSplitFeatureVisibility(mWrapper.showSplitFeature());
            onBack(true);
            mSite.onClipVideoOk();
        }

        @Override
        public void onError(String message) {
            mWaitDialog.dismiss();
            Toast.makeText(getContext(), R.string.video_modify_failed, Toast.LENGTH_SHORT).show();
//            T.showShort(mContext, "裁剪失败：" + message);
        }
    };

    private VideoFrameAdjustPage mFrameAdjustPage;
    private VideoFrameAdjustSite mFrameAdjustSite = new VideoFrameAdjustSite(){
        @Override
        public void onBack(Context context)
        {
            startAlphaAnimation(mIntroduceText,1,0, View.GONE);
            startVerticalTransition(mVideoSelectLayout, mMoveOffset,0 );
            onCurPageBack(mFrameAdjustPage);
        }
    };

    private VideoPage mCurVideoPage;
    private VideoSpeedPage mVideoSpeedPage;
    private VideoSpeedSite mVideoSpeedSite = new VideoSpeedSite(){
        @Override
        public void onBack(Context context,boolean isRefresh)
        {
//            mVideoSelectLayout.refreshBitmapList();
            if(isRefresh)
            {
                mFeatureMenuList.setSplitFeatureVisibility(mWrapper.showSplitFeature());
                mVideoSelectLayout.refreshBitmapList(true, mCurrentInfo, ProcessMode.Edit);
                mSite.okSpeedRateOk(mWrapper.mVideoInfos.indexOf(mCurrentInfo),mCurrentInfo);
            }
            startAlphaAnimation(mVideoSelectLayout,0,1, View.VISIBLE);
            onCurPageBack(mVideoSpeedPage);
        }
    };

    private VideoFilterPage mVideoFitlerPage;
    private VideoFilterSite mFilterSite = new VideoFilterSite() {
        @Override
        public void onBack() {
            startAlphaAnimation(mVideoSelectLayout,0,1, View.VISIBLE);
            VideoFeaturePage.this.mWrapper.mVideoView.setLooping(false);
            onCurPageBack(mVideoFitlerPage);
        }
    };

    private void onCurPageBack(final VideoPage videoPage)
    {
        mIntroduceText.setVisibility(View.GONE);
        startAlphaAnimation(mFeatureMenuList,0,1, View.VISIBLE);
        Animator animator = startAlphaAnimation(videoPage,1,0, View.GONE);
        animator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                videoPage.onClose();
                removeView(videoPage);
            }
        });
        onBackFeaturePage();
        mWrapper.resumeAll(false);
    }

//    private Handler mRepeatPlayHandler = new Handler();
//    private Runnable mRepeatPlayRunnable = new Runnable() {
//        @Override
//        public void run() {
//            long interval = 0;
//            try {
//                mWrapper.seekTo((long)(mCurrentInfo.getVideoTime(mWrapper.getCurrentMode()) * mLeftProgress));
//                interval = (long)((mRightProgress - mLeftProgress) * mCurrentInfo.getVideoTime(mWrapper.getCurrentMode())) ;
//            } finally {
//                mRepeatPlayHandler.postDelayed(mRepeatPlayRunnable, interval);
//            }
//        }
//    };
//
//    private void startPlay() {
//        mRepeatPlayRunnable.run();
//    }
//
//    private void cancelPlay() {
//        mRepeatPlayHandler.removeCallbacks(mRepeatPlayRunnable);
//    }

    private Handler mRepeatPlayHandler = new Handler();
    private long mInterval;
    private ValueAnimator mProcessAnimator = new ValueAnimator();
    private Runnable mRepeatPlayRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mLastProgress = mLeftProgress;
                long duration = mCurrentInfo.getVideoTime(mWrapper.getCurrentMode());
                mWrapper.seekTo((long) (duration * mLeftProgress),true);
                mWrapper.resumeAll(false);
                mInterval = (long)((mRightProgress - mLastProgress) * duration) ;
                startAnim((int) duration);
            } finally {
                mRepeatPlayHandler.postDelayed(mRepeatPlayRunnable, mInterval);
            }
        }
    };
    private float mLastProgress;
    private void pausePlay()
    {
        if(mProcessAnimator.isRunning()){
            mProcessAnimator.removeAllUpdateListeners();
            mProcessAnimator.removeAllListeners();
            mLastProgress = (float) mProcessAnimator.getAnimatedValue();
            mProcessAnimator.cancel();
        }
        mRepeatPlayHandler.removeCallbacks(mRepeatPlayRunnable);
        mProcessAnimator.cancel();
    }

    private void startAnim(int duration)
    {
        mInterval = (long)((mRightProgress - mLastProgress) * duration) ;
        mInterval = Math.abs(mInterval);
        mProcessAnimator.cancel();
        mProcessAnimator.setFloatValues(mLastProgress,mRightProgress);
        mProcessAnimator.setDuration(mInterval);
        mProcessAnimator.setInterpolator(new LinearInterpolator());
        mProcessAnimator.removeAllUpdateListeners();
        mProcessAnimator.removeAllListeners();
        mProcessAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                float f = (float) animation.getAnimatedValue();
                if(mVideoSelectLayout != null){
                    mVideoSelectLayout.setProgress(f);
                }
            }
        });
        mProcessAnimator.start();
    }
    private void resumePlay()
    {
        long duration = mCurrentInfo.getVideoTime(mWrapper.getCurrentMode());
        startAnim((int) duration);
        long leaveDuration = (long)((mRightProgress * duration) - (mLastProgress * duration));
        mInterval = leaveDuration;
        mRepeatPlayHandler.removeCallbacks(mRepeatPlayRunnable);
        mRepeatPlayHandler.postDelayed(mRepeatPlayRunnable,mInterval);
//        startPlay();
    }

    private void startPlay() {
        mRepeatPlayRunnable.run();
    }


    @Override
    public void onClose()
    {
        if(mCurDialog != null){
            mCurDialog.dismiss();
        }
        MyBeautyStat.onPageEndByRes(R.string.视频二级菜单页);
    }
}

