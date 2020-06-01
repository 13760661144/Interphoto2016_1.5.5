package cn.poco.video.videoMusic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import cn.poco.draglistview.DragListItemInfo;
import cn.poco.framework.BaseSite;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.resource.AbsDownloadMgr;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.resource.MusicRes;
import cn.poco.resource.MusicResMgr2;
import cn.poco.resource.ResType;
import cn.poco.resource.ResourceUtils;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.tianutils.ShareData;
import cn.poco.video.AudioStore;
import cn.poco.video.VideoResMgr;
import cn.poco.video.frames.VideoPage;
import cn.poco.video.page.VideoModeWrapper;
import cn.poco.video.site.SelectMusicSite;
import cn.poco.video.site.VideoMusicSite;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.utils.VideoUtils;
import cn.poco.video.view.ActionBar;
import cn.poco.video.view.BaseBottomView;

/**
 * Created by lgd on 2017/6/1.
 */

public class VideoMusicPage extends VideoPage
{
//public class MusicFrame extends VideoFrame{
    private static final String TAG= "音乐";
    private VideoModeWrapper mWrapper;
    private int openPageType = 0;
    private static int Edit_MODE = 2;
    private static int SELECT_MODE = 1;
    private FrameLayout mBottomFr;
    private MusicRecycleView mMusicList;
    private int bottomH;
    private TextView mTip;
    private ImageView mCd;
    private LinearLayout mVoiceSwitch;
    private ImageView mSwitchLogo;
    private TextView mVoiceTip;
    private ImageView mEditEntry;
    private boolean isSilence;

    private int musicDuration;
    private int videoDuration;
    private EditMusicView mEditView;
    private boolean isOriginalSilence;
    private String mOriginalMusicPath;
    private int mOriginalStartTime;
    private int mOriginalUri;
    private String mOriginalName;
    private float mOriginalBkVolume;
    private float mOriginalMusicVolume;
    private MusicRes mOriginalRes;
    private MusicSaveInfo mMusicSaveInfo;
    private VideoMusicSite mSite;
    public VideoMusicPage(@NonNull Context context, BaseSite site,VideoModeWrapper wrapper) {
        super(context,site);
        mSite = (VideoMusicSite) site;
        TongJiUtils.onPageStart(getContext(), TAG);
        MyBeautyStat.onPageStartByRes(R.string.视频音乐);
        mWrapper = wrapper;
        mWrapper.mActionBar.setOnActionbarMenuItemClick(new ActionBar.onActionbarMenuItemClick(){
            @Override
            public void onItemClick(int id)
            {
                super.onItemClick(id);
                if(id == ActionBar.LEFT_MENU_ITEM_CLICK){
                    onBack();
                }else if(id == ActionBar.RIGHT_MENU_ITEM_CLICK){
                    isSave = true;
                    openPageType = 0;
                    onBack();
                }
            }
        });
        mMusicSaveInfo = mWrapper.mMusicSaveInfo;
        initData();
        init();
    }

    @Override
    public void SetData(HashMap<String, Object> params) {

    }

    private void initData() {
        bottomH = ShareData.PxToDpi_xhdpi(80);
//        videoDuration = (int) VideoUtils.getDurationFromVideo2(mWrapper.mVideoEntry.mMediaPath) / 1000;
        videoDuration = (int) (mWrapper.getVideosDuration()/1000);
        DownloadMgr.getInstance().AddDownloadListener(mDownloadListener);

        mOriginalMusicPath = mMusicSaveInfo.mMusicPath;
        mOriginalStartTime = mMusicSaveInfo.mMusicStartTime;
        mOriginalUri = mMusicSaveInfo.mMusicUri;
        mOriginalName = mMusicSaveInfo.mMusicName;
        mOriginalBkVolume = mWrapper.mVideoView.getVolume();
        mOriginalMusicVolume = mMusicSaveInfo.mMaxVolume;
        mOriginalRes = mMusicSaveInfo.mMusicRes;
    }

    private void init() {
        LayoutParams fl;
        mMusicList = new MusicRecycleView(getContext());
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mMusicList, fl);

        mVoiceSwitch = new LinearLayout(getContext());
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.TOP;
        fl.topMargin = mWrapper.mVideoView.getSurfaceTop() + mWrapper.mActionBarHeight;
        fl.leftMargin = mWrapper.mVideoView.getSurfaceLeft();
        int padding = ShareData.PxToDpi_xhdpi(15);
        mVoiceSwitch.setPadding(padding, padding, padding, padding);
        mVoiceSwitch.setOrientation(LinearLayout.HORIZONTAL);
        mVoiceSwitch.setGravity(Gravity.CENTER);
        mVoiceSwitch.setLayoutParams(fl);
        mVoiceSwitch.setOnClickListener(mOnClickListener);
        mVoiceSwitch.setVisibility(View.GONE);
//		mMainLayout.AddMainChild(mVoiceSwitch);
        addView(mVoiceSwitch);
        {
            LinearLayout.LayoutParams ll;
            mSwitchLogo = new ImageView(getContext());
            mSwitchLogo.setImageResource(R.drawable.video_music_voice_on);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mSwitchLogo.setLayoutParams(ll);
            mVoiceSwitch.addView(mSwitchLogo);

            mVoiceTip = new TextView(getContext());
            mVoiceTip.setText(R.string.bk_music_off);
            mVoiceTip.setTextColor(Color.WHITE);
            mVoiceTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.leftMargin = ShareData.PxToDpi_xhdpi(5);
            mVoiceTip.setLayoutParams(ll);
            mVoiceSwitch.addView(mVoiceTip);
            isOriginalSilence = mWrapper.mVideoView.getVolume() == 0;
            setSilenceState(isOriginalSilence,mOriginalBkVolume);
        }

        mBottomFr = new FrameLayout(getContext());
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bottomH);
        fl.gravity = Gravity.BOTTOM;
        addView(mBottomFr, fl);
        {
            mCd = new ImageView(getContext()) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    if (enabled) {
                        mCd.setImageResource(R.drawable.video_music_cd_normal);
                    } else {
                        mCd.setImageResource(R.drawable.video_music_cd_disable);
                    }
                }
            };
            mCd.setEnabled(false);
            mCd.setOnClickListener(mOnClickListener);
            fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER_VERTICAL;
            fl.leftMargin = ShareData.PxToDpi_xhdpi(50);
            mBottomFr.addView(mCd, fl);

            mTip = new TextView(getContext()) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    if (enabled) {
                        mTip.setTextColor(Color.WHITE);
                    } else {
                        mTip.setText(R.string.choose_background_music);
                        mTip.setTextColor(0x2fffffff);
                    }
                }
            };
            mTip.setEnabled(false);
            mTip.setEllipsize(TextUtils.TruncateAt.END);
            mTip.setSingleLine();
            mTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
            fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER_VERTICAL;
            fl.leftMargin = ShareData.PxToDpi_xhdpi(110);
            fl.rightMargin = ShareData.PxToDpi_xhdpi(100);
            mBottomFr.addView(mTip, fl);

            mEditEntry = new ImageView(getContext()) {
                @Override
                public void setEnabled(boolean enabled) {
                    super.setEnabled(enabled);
                    if (enabled) {
                        mEditEntry.setVisibility(VISIBLE);
                    } else {
                        mEditEntry.setVisibility(View.GONE);
                    }
                }
            };
            mEditEntry.setImageResource(R.drawable.video_music_edit_entry);
            mEditEntry.setEnabled(false);
            mEditEntry.setOnClickListener(mOnClickListener);
            mEditEntry.setPadding(ShareData.PxToDpi_xhdpi(20), ShareData.PxToDpi_xhdpi(20), ShareData.PxToDpi_xhdpi(50), ShareData.PxToDpi_xhdpi(20));
            fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
//            fl.rightMargin = ShareData.PxToDpi_xhdpi(50);
            mBottomFr.addView(mEditEntry, fl);
        }

        /**
         * 恢复之前选中的音乐
         */
        mMusicList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMusicList.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (mMusicSaveInfo.mMusicPath != null && FileUtils.isFileExist(mMusicSaveInfo.mMusicPath)) {
                    musicDuration = (int) (VideoUtils.getDurationFromVideo2(mMusicSaveInfo.mMusicPath) / 1000);
                    isShowBottomBtn(true);
                    if (mMusicSaveInfo.mMusicUri != 0) {
                        mMusicList.setSelItemByUri(mMusicSaveInfo.mMusicUri,false);
                    }
                    if (mMusicSaveInfo.mMusicName != null) {
                        mTip.setText(mMusicSaveInfo.mMusicName);
                    }
                }
            }
        });

    }

    private void setSilenceState(boolean isSilence,float volume) {
        this.isSilence = isSilence;
        if (isSilence) {
            mWrapper.mVideoView.setVolume(0f);
            mSwitchLogo.setImageResource(R.drawable.video_music_voice_off);
            mVoiceTip.setText(R.string.bk_music_on);
        } else {
            mWrapper.mVideoView.setVolume(volume);
            mSwitchLogo.setImageResource(R.drawable.video_music_voice_on);
            mVoiceTip.setText(R.string.bk_music_off);
        }
    }

    private void setSilenceState(boolean isSilence) {
        setSilenceState(isSilence,1f);
    }

    public void requestSwitchLayout() {
        float x = (mWrapper.mVideoView.getSurfaceWidth() * (1 - mWrapper.mVideoView.getScaleX()) / 2);
        float y = (mWrapper.mVideoView.getSurfaceHeight() * (1 - mWrapper.mVideoView.getScaleX()) / 2);
        y += mWrapper.mVideoView.getTranslationY();
        mVoiceSwitch.setTranslationX(x);
        mVoiceSwitch.setTranslationY(y);
        mVoiceSwitch.setVisibility(View.VISIBLE);
    }


    private View.OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mWrapper.isDragProgress()) {
                if (v == mEditEntry) {
                    openEditView();
//                    if (musicDuration >= videoDuration + 1) {
//                        openEditView();
//                    } else {
//                        LoginOtherUtil.showToast(R.string.short_audio);
//                    }
                } else if (v == mVoiceSwitch) {
                    if (isSilence) {
                        //恢复原声
                        MyBeautyStat.onClickByRes(R.string.视频音乐_视频原音);
                        setSilenceState(false,1f);
                    } else {
                        //关闭原声
                        TongJi2.AddCountByRes(getContext(), R.integer.屏蔽视频音);
                        setSilenceState(true);
                    }
                }
            }
        }
    };
    private boolean isSave = false;

    @Override
    public void onPause() {
        if(mSelectMusicPage != null)
        {
            mSelectMusicPage.onPause();
        }else{
            mWrapper.onPauseAll();
            TongJiUtils.onPagePause(getContext(), TAG);
        }
    }

    @Override
    public void onResume() {
        if(mSelectMusicPage != null)
        {
            mSelectMusicPage.onResume();
        }else
        {
            mWrapper.onResumeAll();
            TongJiUtils.onPageResume(getContext(), TAG);
        }
    }

    @Override
    public void onClose() {
        if(mSelectMusicPage != null)
        {
            mSelectMusicPage.onClose();
        }
        TongJiUtils.onPageEnd(getContext(), TAG);
        MyBeautyStat.onPageEndByRes(R.string.视频音乐);
        mMusicList.releaseMem();
        DownloadMgr.getInstance().RemoveDownloadListener(mDownloadListener);
    }

    @Override
    public void onPageResult(int siteID, HashMap<String, Object> params) {
        if (siteID == SiteID.SELECTMUSIC) {

        } else if (siteID == SiteID.TEXT_UNLOCK) {
            if (params != null) {
                int uri = (int) params.get("resUri");
                mMusicList.setSelItemByUri(uri, true);
                isShowBottomBtn(true);
            }
        }
//        mWrapper.onResumeAll();

    }

    public void clearMusic() {
        TongJi2.AddCountByRes(getContext(), R.integer.取消音乐);
        MyBeautyStat.onClickByRes(R.string.视频音乐_删除音乐);
        mWrapper.mMusicPlayer.pause();
        mWrapper.mMusicPlayer.reset();
        clearMusicInfo();
        isShowBottomBtn(false);
    }


    public void isShowBottomBtn(boolean isShow) {
        if (isShow) {
            mCd.setEnabled(true);
            mTip.setEnabled(true);
            mEditEntry.setEnabled(true);
        } else {
            mCd.setEnabled(false);
            mTip.setEnabled(false);
            mEditEntry.setEnabled(false);
        }
    }

    private EditMusicView.OnCallBack mEditCallBack = new EditMusicView.OnCallBack() {
        @Override
        public void onMusicClip(int second) {
            if(mMusicSaveInfo.mMusicStartTime != second * 1000) {
                mMusicSaveInfo.mMusicStartTime = second * 1000;
                mWrapper.resumeAll(true);
            }
        }

        @Override
        public void onBkVolume(float volume) {
            if(volume <= 0f){
                setSilenceState(true);
            }else {
                setSilenceState(false,volume);
            }
        }

        @Override
        public void onMusicVolume(float volume) {
            mMusicSaveInfo.mMaxVolume = volume;
        }

        @Override
        public void onBack() {
            closeEditView();
        }

        @Override
        public void onMode(int mode) {
            switch (mode){
                case EditMusicView.MODE_CLIP:
                    mWrapper.mActionBar.setUpActionbarTitle(getContext().getString(R.string.music_choose), Color.WHITE, 16);
                    break;
                case EditMusicView.MODE_BK_VOLUME:
                    mWrapper.mActionBar.setUpActionbarTitle(getContext().getString(R.string.video_volume), Color.WHITE, 16);
                    break;
                case EditMusicView.MODE_MUSIC_VOLUME:
                    mWrapper.mActionBar.setUpActionbarTitle(getContext().getString(R.string.music_volume), Color.WHITE, 16);
                    break;
            }
        }
    };

    private void openEditView() {
        TongJi2.AddCountByRes(getContext(), R.integer.修改音乐);
        MyBeautyStat.onClickByRes(R.string.视频音乐_进入音乐调整);
        MyBeautyStat.onPageStartByRes(R.string.音乐调整);

        mMusicList.setVisibility(View.GONE);
        mBottomFr.setVisibility(View.GONE);
        openPageType = Edit_MODE;
        EditMusicView.EditInfo editInfo = new EditMusicView.EditInfo();
        ClipMusicView.FrequencyInfo info = new ClipMusicView.FrequencyInfo();
        info.musicPath = mMusicSaveInfo.mMusicPath;
        info.musicTime = musicDuration;
        info.videoTime = videoDuration;
        info.startTime = mMusicSaveInfo.mMusicStartTime / 1000;
        editInfo.clipInfo = info;
        editInfo.bkVoiceVolume = mWrapper.mVideoView.getVolume();
        editInfo.musicVolume = mMusicSaveInfo.mMaxVolume;
        if (mEditView == null) {
            LayoutParams params;
            mEditView = new EditMusicView(getContext(), editInfo);
            mEditView.setOnCallBack(mEditCallBack);
            params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.BOTTOM;
            addView(mEditView, params);
        }else{
            mEditView.refreshInfo(editInfo);
        }
        mEditView.setTranslationY(ShareData.m_screenHeight);
        mEditView.setClickable(true);
        mEditView.animate().translationY(0).setDuration(150).start();
        mVoiceSwitch.animate().alpha(0).setDuration(150).start();
        mVoiceSwitch.setClickable(false);
    }

    private void closeEditView() {
        MyBeautyStat.onClickByRes(R.string.音乐调整_退出音乐调整);
        MyBeautyStat.onPageEndByRes(R.string.音乐调整);
        TongJi2.AddCountByRes(getContext(), R.integer.音乐_手势_点击_下滑退出音乐选择);
        mWrapper.mActionBar.setUpActionbarTitle(getContext().getString(R.string.Music), Color.WHITE, 16);
        openPageType = 0;
        mEditView.animate().translationY(ShareData.m_screenHeight).setDuration(150).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
//                clearEditView();
            }
        }).start();
        mVoiceSwitch.animate().alpha(1).setDuration(150).start();
        mVoiceSwitch.setClickable(true);

        mMusicList.setVisibility(View.VISIBLE);
        mBottomFr.setVisibility(View.VISIBLE);
    }


    private void openSelectPage() {
        TongJi2.AddCountByRes(getContext(), R.integer.本地音乐);
        MyBeautyStat.onClickByRes(R.string.视频音乐_打开音乐文件夹);
        mWrapper.pauseAll();
        openPageType = SELECT_MODE;
//        mWrapper.mSite.onSelectMusic();
        if(mSelectMusicPage == null){
            mSelectMusicPage = new SelectMusicPage(getContext(),mSelectMusicSite);
            mSelectMusicPage.SetData(null);
            LayoutParams fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mSelectMusicPage,fl);
        }
    }

    private void clearMusicInfo() {
        mMusicSaveInfo.mMusicPath = null;
        mMusicSaveInfo.mMusicStartTime = 0;
        mMusicSaveInfo.mMusicName = null;
        mMusicSaveInfo.mMusicUri = 0;
    }

    private void reStoreMusicInfo() {
        mMusicSaveInfo.mMusicPath = mOriginalMusicPath;
        mMusicSaveInfo.mMusicStartTime = mOriginalStartTime;
        mMusicSaveInfo.mMusicName = mOriginalName;
        mMusicSaveInfo.mMusicUri = mOriginalUri;
        mMusicSaveInfo.mMaxVolume = mOriginalMusicVolume;
        mMusicSaveInfo.mMusicRes = mOriginalRes;
        mWrapper.mVideoView.setVolume(mOriginalBkVolume);
    }

//    @Override
//    public void SetData(HashMap<String, Object> params)
//    {
//
//    }

    public void onBack() {

        if (openPageType != 0) {
            if (openPageType == Edit_MODE) {
                closeEditView();
            }else if(openPageType == SELECT_MODE){
                if(mSelectMusicPage != null)
                {
                    mSelectMusicPage.onBack();
                }
            }
            openPageType = 0;
        } else {
            mEditCallBack = null;
            mVoiceSwitch.setVisibility(View.GONE);
            if (isSave) {
                MyBeautyStat.onClickByRes(R.string.视频音乐_保存音乐);
                if (mMusicSaveInfo.mMusicPath != null || (mOriginalMusicPath != null) || (isSilence != isOriginalSilence)) {
                    mWrapper.isCanSave = true;
                    mWrapper.isModify = true;
                }
                int tongJi = 0;
                if(mMusicSaveInfo.mMusicRes != null){
                    tongJi = mMusicSaveInfo.mMusicRes.m_tjId;
                }
                mWrapper.mVideoView.setMusicPath(tongJi,mMusicSaveInfo.mMusicPath, mMusicSaveInfo.mMusicStartTime,mMusicSaveInfo.mMaxVolume);
//            mWrapper.resumeAll(true);
            } else {
                MyBeautyStat.onClickByRes(R.string.视频音乐_退出视频音乐页);
                boolean isReStart = false;
                if (mOriginalMusicPath == null ||!( new File(mOriginalMusicPath).exists())) {
                    mMusicSaveInfo.mMusicUri = 0;
                    mMusicSaveInfo.mMusicPath = null;
                    mWrapper.mMusicPlayer.pause();
                    mWrapper.mMusicPlayer.reset();
                } else if (mMusicSaveInfo.mMusicPath == null || (mMusicSaveInfo.mMusicPath != null && !mOriginalMusicPath.equals(mMusicSaveInfo.mMusicPath))) {
                    try {
                        mWrapper.mMusicPlayer.reset();
                        mWrapper.mMusicPlayer.setDataSource(mOriginalMusicPath);
                        mWrapper.mMusicPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isReStart = true;
                }
                if (mMusicSaveInfo.mMusicStartTime != mOriginalStartTime) {
                    isReStart = true;
                }
                reStoreMusicInfo();
                if (isReStart) {
                    mWrapper.resumeAll(true);
                }
            }
            mSite.onBack(getContext());
        }
    }



    private DownloadMgr.DownloadListener mDownloadListener = new AbsDownloadMgr.DownloadListener() {
        @Override
        public void OnDataChange(int resType, int downloadId, IDownload[] resArr) {
            if (resArr != null && ((BaseRes) resArr[0]).m_type == BaseRes.TYPE_LOCAL_PATH) {
                if (resType == ResType.MUSIC.GetValue()) {
                    mMusicList.setData(mMusicList.initItemList());
                }
            }
        }
    };


    protected SelectMusicPage mSelectMusicPage;
    protected SelectMusicSite mSelectMusicSite = new SelectMusicSite(){
        @Override
        public void onBack(HashMap<String, Object> params,Context context)
        {
            if(mSelectMusicPage != null){
                removeView(mSelectMusicPage);
                mSelectMusicPage.onClose();
                mSelectMusicPage = null;
            }
            if (params != null && params.get("music") != null) {
                AudioStore.AudioInfo info = (AudioStore.AudioInfo) params.get("music");
                if (info != null && info.getPath() != null) {
                    mMusicList.cancelSelect();
                    musicDuration = (int) (info.getDuration() / 1000);
                    mMusicSaveInfo.mMusicPath = info.getPath();
                    mMusicSaveInfo.mMusicUri = 0;
                    mMusicSaveInfo.mMusicStartTime = 0;
                    mMusicSaveInfo.mMusicName = info.getTitle();
                    mTip.setText(info.getTitle());
                    isShowBottomBtn(true);
                    try {
                        mWrapper.mMusicPlayer.pause();
                        mWrapper.mMusicPlayer.reset();
                        mWrapper.mMusicPlayer.setDataSource(mMusicSaveInfo.mMusicPath);
                        mWrapper.mMusicPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mWrapper.resumeAll(true);
                }
            }else
            {
                mWrapper.resumeAll(false);
            }
        }
    };

    @Override
    public int getBottomPartHeight()
    {
        return ShareData.PxToDpi_xhdpi(300);
    }

    class MusicRecycleView extends BaseBottomView {
        private int m_curUri;
        private List<DragListItemInfo> listdatas;

        public MusicRecycleView(@NonNull Context context) {
            super(context);
            initData();
            initView();
        }

        @Override
        protected List<DragListItemInfo> initItemList() {
            listdatas = VideoResMgr.GetMusicRess(getContext());
            return listdatas;
        }

        protected void cancelSelect() {
            m_listAdapter.SetSelByIndex(-1);
            m_curUri = 0;
        }

        @Override
        protected void OnHideItem(int position) {
            DragListItemInfo itemInfo = m_listDatas.get(position);
            if (itemInfo != null) {
                MusicRes res = (MusicRes) itemInfo.m_ex;
                if(res.m_type != BaseRes.TYPE_LOCAL_RES)
                {
                    TongJi2.AddCountByRes(getContext(), R.integer.音乐_长按隐藏);
                    MyBeautyStat.onClickByRes(R.string.视频音乐_删除音乐);
                    MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.视频音乐);
                    m_listDatas.remove(position);
//                    m_listAdapter.removeItem(position);
                    m_listAdapter.notifyDataSetChanged();
                    res.m_isHide = !res.m_isHide;
                    if (m_curUri == res.m_id)
                    {
//                        setSelItemByUri(DragListItemInfo.URI_ORIGIN, false);
                        cancelSelect();
                        clearMusic();
                    }
                    String path = res.m_res + "/" + res.fileName;
                    if(res.m_id== mOriginalUri || (mOriginalMusicPath != null && mOriginalMusicPath.equals(path))){
                        mOriginalMusicPath = null;
                        mOriginalUri = 0;
                    }

                    MusicResMgr2.getInstance().DeleteRes(getContext(),res);
//                    MusicResMgr.HideRes(getContext(), TableNames.MUSIC, res.m_id, res.m_isHide);
                }
            }
        }

        @Override
        protected void OnChangeItem(int fromPosition, int toPosition) {
			DragListItemInfo itemInfo = m_listDatas.get(fromPosition);
			int fromPos = fromPosition;
			int toPos = toPosition;
			if(itemInfo != null)
			{
				fromPos = ResourceUtils.HasId(MusicResMgr2.getInstance().GetOrderArr(), itemInfo.m_uri);
			}
			itemInfo = m_listDatas.get(toPosition);
			if(itemInfo != null)
			{
				toPos = ResourceUtils.HasId(MusicResMgr2.getInstance().GetOrderArr(), itemInfo.m_uri);
			}
			ResourceUtils.ChangeOrderPosition(MusicResMgr2.getInstance().GetOrderArr(), fromPos, toPos);
            MusicResMgr2.getInstance().SaveOrderArr();
			if(m_listDatas != null && m_listDatas.size() > fromPosition && m_listDatas.size() > toPosition)
			{
				itemInfo = m_listDatas.remove(fromPosition);
				m_listDatas.add(toPosition, itemInfo);
			}
        }

        @Override
        protected void onClickNormalItem(View v, final DragListItemInfo info, final int index) {
            if (mWrapper.isDragProgress()) {
                return;
            }

            if (info.m_isLock) {
//                mWrapper.pauseAll();
//                HashMap<String, Object> data = new HashMap<>();
//                data.put("img", InitGlassBk());
//                data.put("res", clipInfo.m_ex);
//                mWrapper.mSite.onDownLoad(data);
                return;
            }
            if (info.m_uri == DragListItemInfo.URI_MUSIC_NONE) {
                cancelSelect();
                clearMusic();
                return;
            }
            if (info.m_uri == DragListItemInfo.URI_LOCAL_MUSIC) {
                openSelectPage();
                return;
            }
            switch (info.m_style) {
                case NORMAL:
                case NEW: {
                    if (m_curUri != info.m_uri) {
                        m_listAdapter.SetSelByUri(info.m_uri);
                        m_resList.ScrollToCenter(index);
                        if (info.m_ex instanceof MusicRes) {
                            MusicRes res = (MusicRes) info.m_ex;
                            MyBeautyStat.onChooseMaterial(String.valueOf(res.m_tjId),R.string.视频音乐);
                            MyBeautyStat.onClickByRes(R.string.视频音乐_选择音乐);
                            setMusicData(res, info.m_uri);
                        }
                    } else if (m_curUri == info.m_uri) {
                    }
                    m_curUri = info.m_uri;
                    break;
                }
                case NEED_DOWNLOAD: {
                    info.m_style = DragListItemInfo.Style.LOADING;
                    info.m_progress = 0;
                    if(m_listAdapter != null){
                        m_listAdapter.notifyDataSetChanged();
                    }
                    DownloadMgr.getInstance().DownloadRes((IDownload) info.m_ex, new AbsDownloadMgr.Callback() {
                        @Override
                        public void OnProgress(int downloadId, IDownload res, int progress) {
                            if (m_listAdapter != null) {
                                m_listAdapter.SetItemProgress(((MusicRes) res).m_id, progress);
                            }
                        }

                        @Override
                        public void OnComplete(int downloadId, IDownload res) {
                            if (m_listAdapter != null) {
                                if(info != null)
                                {
                                    info.m_canDrag = true;
                                    info.m_canDrop = true;
                                }
                                m_listAdapter.SetItemStyleByUri(((MusicRes) res).m_id, DragListItemInfo.Style.NORMAL);
                                if(openPageType == 0){
                                    setSelItemByUri(((MusicRes) res).m_id, true);
                                }
                            }
                        }

                        @Override
                        public void OnFail(int downloadId, IDownload res) {
                            if (m_listAdapter != null) {
                                Toast.makeText(getContext(), getResources().getString(R.string.Ooops), Toast.LENGTH_SHORT).show();
                                m_listAdapter.SetItemStyleByUri(((MusicRes) res).m_id, DragListItemInfo.Style.NEED_DOWNLOAD);
                            }
                        }
                    });
                    break;
                }
            }
        }

        @Override
        protected void onHeadClick(View v, DragListItemInfo info, int index) {
            super.onHeadClick(v, info, index);
        }

        public void setSelItemByUri(int uri, boolean isSeData) {
            int index = -1;
            if (m_listDatas != null) {
                index = m_listAdapter.GetIndexByUri(uri);
            }
            if (index >= 0) {
                m_curUri = uri;
                m_listAdapter.SetSelByIndex(index);
                m_resList.ScrollToCenter(index);
                if (isSeData) {
                    DragListItemInfo info = listdatas.get(index);
                    if (info.m_ex instanceof MusicRes) {
                        MusicRes res = (MusicRes) info.m_ex;
                        setMusicData(res, info.m_uri);
                    }
                }
            }
        }

        @Override
        protected boolean canDelete(int position) {
            DragListItemInfo itemInfo = m_listDatas.get(position);
            if (itemInfo != null)
            {
                BaseRes res = (BaseRes) itemInfo.m_ex;
                if (res.m_type != BaseRes.TYPE_LOCAL_RES)
                {
                    return true;
                }
            }
            return false;
        }
    }

    private void setMusicData(MusicRes res, int uri) {
        String path = res.m_res + "/" + res.fileName;
        if (!TextUtils.isEmpty(path)) {
            mMusicSaveInfo.mMusicPath = path;
            mMusicSaveInfo.mMusicStartTime = 0;
            mMusicSaveInfo.mMusicUri = uri;
            mMusicSaveInfo.mMusicName = res.m_name;
            mMusicSaveInfo.mMusicRes = res;

            try {
                mWrapper.mMusicPlayer.reset();
                mWrapper.mMusicPlayer.setDataSource(path);
                mWrapper.mMusicPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mTip.setText(res.m_name);
            musicDuration = mWrapper.mMusicPlayer.getDuration() / 1000;
            mWrapper.resumeAll(true);
            isShowBottomBtn(true);
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }
}

