package cn.poco.video.videotext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import cn.poco.MaterialMgr2.MgrUtils;
import cn.poco.MaterialMgr2.ThemeIntroPage;
import cn.poco.MaterialMgr2.ThemeItemInfo;
import cn.poco.MaterialMgr2.site.ThemeIntroPageSite4;
import cn.poco.Text.JsonParser;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.display.CoreViewV3;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.framework.BaseSite;
import cn.poco.framework.FileCacheMgr;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.resource.AbsDownloadMgr;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.resource.ResType;
import cn.poco.resource.ThemeRes;
import cn.poco.resource.VideoTextRes;
import cn.poco.resource.VideoTextResMgr2;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;
import cn.poco.video.frames.VideoPage;
import cn.poco.video.page.ProcessMode;
import cn.poco.video.page.VideoModeWrapper;
import cn.poco.video.render.GLVideoView;
import cn.poco.video.render.IVideoPlayer;
import cn.poco.video.site.TextEditPageSite;
import cn.poco.video.site.TextUnLockPageSite;
import cn.poco.video.site.VideoTextHelpSite;
import cn.poco.video.site.VideoTextSite;
import cn.poco.video.videotext.text.VideoText;
import cn.poco.video.videotext.text.VideoTextView;
import cn.poco.video.videotext.text.WaterMarkInfo;
import cn.poco.video.view.ActionBar;

import static cn.poco.tianutils.ShareData.PxToDpi_xhdpi;


/**
 * Created by lgd on 2017/6/1.
 */

public class VideoTextPage extends VideoPage
{
    private static final String TAG = "视频水印";
    private TextSaveInfo mCurInfo;
    private VideoTextView mTextView;
    private GLVideoView mVideoView;
    private VideoModeWrapper mWrapper;

    public HashMap<Integer,VideoText> mTextDatas = new HashMap<>();

    private LinearLayout mBottomLL;
    private TextView mIdeaBtn;
    private TextView mWatermarkBtn;
    private TextList mTextList;

    private int selectTextColor = 0xffffc433;
    private int normalTextColor = Color.WHITE;
    private boolean isSave = false;
    public final static int TYPE_START = 0;
    public final static int TYPE_ALL = 1;
    public final static int TYPE_END = 2;

    private int mOriginalUri;
    private long mOriginalStartTime;
    private long mOriginalStayTime;
    private int mOriginalDisplayType;
    private int mOriginalTextRes;
    private VideoTextRes mOriginalRes;
    protected boolean m_uriInClassify = true;
    protected boolean mIsFromTheme = false;  //是否素材商店立即使用

    private IPage mCurModulePage;
    private ObjectAnimator mTextAnim;
    private boolean mUiEnable = true;
    private VideoTextSite mSite;
    public VideoTextPage(@NonNull Context context, BaseSite baseSite,VideoModeWrapper wrapper)
    {
        super(context,baseSite);
        mSite = (VideoTextSite) baseSite;
        mWrapper = wrapper;
        mWrapper.mActionBar.setActionbarTitleIconVisibility(View.VISIBLE);
        mWrapper.mActionBar.setOnActionbarMenuItemClick(new ActionBar.onActionbarMenuItemClick(){
            @Override
            public void onItemClick(int id)
            {
                super.onItemClick(id);
                if(id == ActionBar.LEFT_MENU_ITEM_CLICK){
                    if(!removeModuleView()){
                        onBack();
                    }
                }else if(id == ActionBar.RIGHT_MENU_ITEM_CLICK){
                    if(!removeModuleView()){
                        isSave = true;
                        onBack();
                    }
                }
            }
        });
        mWrapper.mActionBar.setActionbarTitleClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mSite.openTextHelp(getContext(), null);
            }
        });
        mCurInfo = mWrapper.mTextSaveInfo;
        mTextView = mWrapper.mTextView;
        mVideoView = mWrapper.mVideoView;
    }

    @Override
    public void SetData(HashMap<String, Object> params)
    {
        if(params != null){
            if(params.containsKey("watermark_type")){
                mCurInfo.mResType = (int) params.get("watermark_type");
            }
            if(params.containsKey("watermark_id")){
                mIsFromTheme = true;
                mCurInfo.mUri = (int) params.get("watermark_id");
//                mTextList.setData(mTextList.initItemList(mResListType));
            }
        }
        initData();
        initUi();
        mVideoView.addOnPlayListener(mOnPlayListener);
        /**
         * 恢复之前选中的水印
         */
        mTextList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                mTextList.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mTextView.SetOperateMode(CoreViewV3.MODE_PENDANT);
                if (mVideoView.getVideoText() != null || mIsFromTheme)
                {
                    mTextList.setSelItemByUri(mCurInfo.mUri, mIsFromTheme);
                }
                if (mCurInfo.mResType == 1)
                {
                    mIdeaBtn.setSelected(false);
                    mWatermarkBtn.setSelected(true);
                } else
                {
                    mIdeaBtn.setSelected(true);
                    mWatermarkBtn.setSelected(false);
                }
            }
        });
        openTextHelpPage();
    }

    private void initData()
    {
        TongJiUtils.onPageStart(getContext(), TAG);
        int type = VideoTextResMgr2.getInstance().GetClassifyIdByResId(mCurInfo.mUri);
        DownloadMgr.getInstance().AddDownloadListener(mDownloadListener);
        mCurInfo.mResType = type != -1 ? type : mCurInfo.mResType;
        if (mCurInfo.mResType == 1)
        {
            MyBeautyStat.onPageStartByRes(R.string.视频水印);
        } else
        {
            MyBeautyStat.onPageStartByRes(R.string.视频创意);
        }
        //进入水印暂停视频
        mOriginalStartTime = mCurInfo.mStartTime;
        mOriginalStayTime = mCurInfo.mStayTime;
        mOriginalUri = mCurInfo.mUri;
        mOriginalDisplayType = mCurInfo.mDisplayType;
        mOriginalRes = mCurInfo.mTextRes;

        if(mVideoView != null && mVideoView.getVideoText()!= null){
            mTextDatas.put(mCurInfo.mUri,mVideoView.getVideoText());
        }
        mTextAnim = new ObjectAnimator();
        mTextAnim.setTarget(mTextView);
        mTextAnim.setPropertyName("curTime");
        mTextAnim.setRepeatCount(ObjectAnimator.INFINITE);
        mTextAnim.setRepeatMode(ObjectAnimator.RESTART);
        changPageState(true);
    }

    private void initUi()
    {
        LayoutParams params;
        LinearLayout.LayoutParams llParams;
        mBottomLL = new LinearLayout(getContext());
        mBottomLL.setOrientation(LinearLayout.HORIZONTAL);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(80));
        params.gravity = Gravity.BOTTOM;
        {
            mIdeaBtn = new TextView(getContext())
            {
                @Override
                public void setSelected(boolean selected)
                {
                    super.setSelected(selected);
                    if (selected)
                    {
                        mIdeaBtn.setTextColor(selectTextColor);
                    } else
                    {
                        mIdeaBtn.setTextColor(normalTextColor);
                    }
                }
            };
            mIdeaBtn.setText(R.string.video_text_idea);
            mIdeaBtn.setGravity(Gravity.CENTER);
            mIdeaBtn.setSelected(true);
            mIdeaBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
            mIdeaBtn.setOnClickListener(mOnClickListener);
            llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            llParams.weight = 1;
            llParams.gravity = Gravity.CENTER;
            mBottomLL.addView(mIdeaBtn, llParams);

            mWatermarkBtn = new TextView(getContext())
            {
                @Override
                public void setSelected(boolean selected)
                {
                    super.setSelected(selected);
                    if (selected)
                    {
                        mWatermarkBtn.setTextColor(selectTextColor);
                    } else
                    {
                        mWatermarkBtn.setTextColor(normalTextColor);
                    }
                }
            };
            mWatermarkBtn.setText(R.string.video_text_watermark);
            mWatermarkBtn.setGravity(Gravity.CENTER);
            mWatermarkBtn.setTextColor(normalTextColor);
            mWatermarkBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
            mWatermarkBtn.setOnClickListener(mOnClickListener);
            llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            llParams.weight = 1;
            llParams.gravity = Gravity.CENTER;
            mBottomLL.addView(mWatermarkBtn, llParams);
        }
        addView(mBottomLL, params);

        mTextList = new TextList(getContext(),mCurInfo.mResType);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mTextList, params);

    }

    private GLVideoView.OnPlayListener mOnPlayListener = new IVideoPlayer.OnPlayListener()
    {
        @Override
        public void onStart()
        {

        }

        @Override
        public void onResume()
        {
            if(mTextView.getVideText()!= null)
            {
                mTextView.SetSelPendant(-1);
            }
            mTextAnim.cancel();
        }

        @Override
        public void onPause()
        {
            if(mTextView.getVideText()!= null)
            {
                mTextView.SetSelPendant(0);
                startTextAnim();
            }
        }
    };

    private DownloadMgr.DownloadListener mDownloadListener = new AbsDownloadMgr.DownloadListener()
    {
        @Override
        public void OnDataChange(int resType, int downloadId, IDownload[] resArr)
        {
            if (resArr != null && ((BaseRes) resArr[0]).m_type == BaseRes.TYPE_LOCAL_PATH)
            {
                if (resType == ResType.AUDIO_TEXT.GetValue())
                {
                    mTextList.setData(mTextList.initItemList());
                }
            }
        }
    };

    private View.OnClickListener mOnClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (!mWrapper.isDragProgress())
            {
                if (v == mWatermarkBtn)
                {
                    if (mCurInfo.mResType != 1)
                    {
                        m_uriInClassify = false;
                        TongJi2.AddCountByRes(getContext(), R.integer.选择水印);
                        MyBeautyStat.onClickByRes(R.string.视频文字_切换至水印);
                        MyBeautyStat.onPageStartByRes(R.string.视频水印);
                        MyBeautyStat.onPageEndByRes(R.string.视频创意);
                        mIdeaBtn.setSelected(false);
                        mWatermarkBtn.setSelected(true);
                        mCurInfo.mResType = 1;
                        mTextList.setData(mTextList.initItemList(mCurInfo.mResType));
                    }
                } else if (v == mIdeaBtn)
                {
                    if (mCurInfo.mResType != 2)
                    {
                        m_uriInClassify = false;
                        TongJi2.AddCountByRes(getContext(), R.integer.创意);
                        MyBeautyStat.onClickByRes(R.string.视频文字_切换至创意);
                        MyBeautyStat.onPageEndByRes(R.string.视频水印);
                        MyBeautyStat.onPageStartByRes(R.string.视频创意);
                        mIdeaBtn.setSelected(true);
                        mWatermarkBtn.setSelected(false);
                        mCurInfo.mResType = 2;
                        mTextList.setData(mTextList.initItemList(mCurInfo.mResType));
                    }
                }
            }
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if(mUiEnable)
        {
            return super.onInterceptTouchEvent(ev);
        }else{
            return true;
        }
    }

    @Override
    public void onPause()
    {
        if (mCurModulePage != null && !(mCurModulePage instanceof VideoTextDisplayView))
        {
            mCurModulePage.onPause();
        } else
        {
            TongJiUtils.onPagePause(getContext(), TAG);
            mWrapper.onPauseAll();
            mTextAnim.cancel();
        }
    }

    @Override
    public void onResume()
    {
        if (mCurModulePage != null && !(mCurModulePage instanceof VideoTextDisplayView))
        {
            mCurModulePage.onResume();
        } else
        {
            TongJiUtils.onPageResume(getContext(), TAG);
            mWrapper.onResumeAll();
            mTextAnim.cancel();
        }
    }

    @Override
    public void onClose()
    {
        changPageState(false);
        mWrapper.mActionBar.setActionbarTitleIconVisibility(View.GONE);
        mWrapper.mActionBar.getTitleView().setOnClickListener(null);
        removeModuleView();
        TongJiUtils.onPageEnd(getContext(), TAG);
        if (mCurInfo.mResType == 1)
        {
            MyBeautyStat.onPageEndByRes(R.string.视频水印);
        } else
        {
            MyBeautyStat.onPageEndByRes(R.string.视频创意);
        }
        mTextList.releaseMem();
        DownloadMgr.getInstance().RemoveDownloadListener(mDownloadListener);
        mVideoView.removeOnPlayListener(mOnPlayListener);
    }

    public void onBack()
    {
        if (mCurModulePage != null)
        {
            if(mUiEnable)
            {
                mCurModulePage.onBack();
            }
        } else
        {
            if (isSave)
            {
                MyBeautyStat.onClickByRes(R.string.视频文字_保存文字);
                if (!(mTextDatas.get(mCurInfo.mUri) == null && mOriginalUri == 0))
                {
                    mWrapper.isCanSave = true;
                    mWrapper.isModify = true;
                }
                int tongji = 0;
                if(mCurInfo.mTextRes  != null){
                    tongji  = mCurInfo.mTextRes.m_tjId;
                }
                mVideoView.setVideoText(tongji,mTextDatas.get(mCurInfo.mUri), mTextView.m_origin,((int)mCurInfo.mStartTime), (int) mCurInfo.mStayTime);
            } else
            {
                MyBeautyStat.onClickByRes(R.string.视频文字_退出视频文字);
                //恢复原本设置
                if (mTextDatas.get(mCurInfo.mUri) != null)
                {
                    if (mVideoView.getVideoText() == null || mOriginalUri == 0)
                    {
                        mTextView.DeleteWatermark();
                    } else if (mTextDatas.get(mCurInfo.mUri) != mVideoView.getVideoText())
                    {
                        mTextView.DeleteWatermark();
                        mTextView.AddVideoText(mVideoView.getVideoText());
                    }
                } else
                {
                    if (mVideoView.getVideoText() != null)
                    {
                        mTextView.DeleteWatermark();
                        mTextView.AddVideoText(mVideoView.getVideoText());
                    }
                }
                if (mOriginalDisplayType != mCurInfo.mDisplayType)
                {
                    setTextViewType(mOriginalDisplayType);
                }
                mCurInfo.mStartTime = mOriginalStartTime;
                mCurInfo.mStayTime = mOriginalStayTime;
                mCurInfo.mUri = mOriginalUri;
                mCurInfo.mDisplayType = mOriginalDisplayType;
                mCurInfo.mTextRes = mOriginalRes;
            }
            mSite.onBack(getContext());
        }
    }


    @Override
    public void onPageResult(int siteID, HashMap<String, Object> params)
    {
        if(mCurModulePage != null){
            mCurModulePage.onPageResult(siteID,params);
        }else{
            if (siteID == SiteID.TEXTEDIT)
            {
                if (params != null)
                {
                    WaterMarkInfo info = (WaterMarkInfo) params.get("videoText");
                    if (info != null)
                    {
                        if (info.m_fontsInfo != null)
                        {
                            for (int i = 0; i < info.m_fontsInfo.size(); i++)
                            {
                                mTextView.UpdateText(i, info.m_fontsInfo.get(i).m_showText);
                            }
                        }
                    }
                    int type = (int) params.get("showType");
                    if (type != mCurInfo.mDisplayType)
                    {
                        setTextViewType(type);
                    }
                }
            } else if (siteID == SiteID.TEXT_UNLOCK)
            {
//                if (params != null)
//                {
//                    int id = -1;
//                    boolean lock = true;
//                    boolean isUse = false;
//                    if (params.get("id") != null)
//                    {
//                        id = (Integer) params.get("id");
//                    }
//                    if (params.get("lock") != null)
//                    {
//                        lock = (Boolean) params.get("lock");
//                    }
//                    if (params.get("isUse") != null)
//                    {
//                        isUse = (Boolean) params.get("isUse");
//                    }
//                    if (id != -1 && lock == false)
//                    {
//                        mTextList.unLockRes(id, isUse);
//                    }
//                }
            } else if (siteID == SiteID.THEME_INTRO_PAGE || siteID == SiteID.THEME_PAGE)
            {
                if (params != null)
                {
                    int sel_uri = -1;
                    if (params.containsKey("id"))
                    {
                        sel_uri = (Integer) params.get("id");
                        int classify = VideoTextResMgr2.getInstance().GetClassifyIdByResId(sel_uri);
                        mCurInfo.mResType = classify != -1 ? classify : mCurInfo.mResType;
                    }
                    boolean needRefresh = false;
                    if (params.containsKey("need_refresh"))
                    {
                        needRefresh = (Boolean) params.get("need_refresh");
                    }
                    if (needRefresh)
                    {
                        mTextList.UpdateListDatas();
                    }
                    if (m_uriInClassify || sel_uri != -1)
                    {
                        if (sel_uri != -1)
                        {
                            mTextList.setSelItemByUri(sel_uri, true);
                        } else
                        {
                            if (!mTextList.isContainsUri(mCurInfo.mUri))
                            {
                                mTextList.cancelSelect();
                                clearWaterMark();
                            }
                        }

                    }
                }
            }
        }
//        mWrapper.onResumeAll();
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(mCurModulePage != null){
            return mCurModulePage.onActivityResult(requestCode,resultCode,data);
        }
        return false;
    }

    private String glassPath;

    protected String InitGlassBk()
    {
        if (glassPath == null)
        {
            Bitmap bmp = mWrapper.mVideoView.getFrame();
            Bitmap temp = BeautifyResMgr.MakeBkBmp(bmp, ShareData.m_screenWidth, ShareData.m_screenHeight, 0x90000000, 0x28000000);
//            BeautifyResMgr.MakeBkBmp(bmp, ShareData.m_screenWidth, ShareData.m_screenHeight, 0xcc000000, 0x26000000);
            if (temp != null)
            {
                String path;
                path = FileCacheMgr.GetLinePath();
                if (Utils.SaveTempImg(temp, path))
                {
                    glassPath = path;
                }
            }
        }
        return glassPath;
    }


    /**
     * 暂停和播放视频，并改变水印动画和播放按钮状态
     * @param isPlayVideo
     */
    private void changeVideoState(boolean isPlayVideo)
    {
        if(isPlayVideo){
            if(mCurModulePage == null || mCurModulePage instanceof VideoTextDisplayView)
            {
                mWrapper.resumeAll(false);
            }
            mTextAnim.cancel();
        }else{
            mWrapper.pauseAll();
            if(!mTextAnim.isRunning()){
                if(VideoTextResMgr2.getInstance().GetClassifyIdByResId(mCurInfo.mUri) == 1){
                    MyBeautyStat.onClickByRes(R.string.视频水印_选中视频水印);
                }else{
                    MyBeautyStat.onClickByRes(R.string.视频创意_选中视频创意);
                }
            }
            mWrapper.hidePlayBtn();
            mWrapper.hideProgressTip();
            startTextAnim();
        }
    }

    /**
     *  根据页面状态改变按钮显示和水印动画
     * @param isEntryOrExit  进入水印界面还是退出
     */
    private void changPageState(boolean isEntryOrExit){
        if(isEntryOrExit){
            mTextView.setControlCallBack(m_coreCB);
            mTextView.setClickable(true);
            if(!mWrapper.isPlaying() && mTextView.getVideText() != null)
            {
                mWrapper.hideProgressTip();
                mWrapper.hidePlayBtn();
                mTextView.SetSelPendant(0);
                startTextAnim();
            }
        }else{
            if(!mWrapper.isPlaying())
            {
                mWrapper.showPlayBtn();
                mWrapper.showProgressTip(mVideoView.getCurrentPosition(),mVideoView.getTotalDuration(), false);
            }
            mTextAnim.cancel();
            mTextView.SetOperateMode(CoreViewV3.MODE_IMAGE);
            mTextView.setControlCallBack(new VideoTextCallbackAdapter());
            mTextView.setClickable(false);
            mTextView.SetSelPendant(-1);
            mTextView.setCurTime((int) mVideoView.getCurrentPosition());
        }
    }

    private void pauseAll()
    {
//        mTextView.SetSelPendant( -1 );
        mTextAnim.cancel();
        mWrapper.pauseAll();
    }


    protected void openRecommendPage(HashMap<String, Object> params)
    {
        if(mCurInfo.mResType == 1){
            MyBeautyStat.onClickByRes(R.string.视频水印_打开推荐位);
        }else{
            MyBeautyStat.onClickByRes(R.string.视频创意_打开推荐位);
        }
        pauseAll();
        addModuleView(new ThemeIntroPage(getContext(), mThemeIntroPageSite4),params);
    }

    protected void openUnLockPage(HashMap<String, Object> params)
    {
        pauseAll();
        MyBeautyStat.onClickByRes(R.string.视频水印_打开推荐位);
        addModuleView(new TextUnLockPage(getContext(),mTextUnLockPageSite),params);
    }

    protected void openThemePage()
    {
        HashMap<String, Object> params = new HashMap<>();
        params.put("typeOnly", true);
        params.put("type", ResType.AUDIO_TEXT);
        if(mCurInfo.mResType == 1){
            params.put("textType","watermark");
            MyBeautyStat.onClickByRes(R.string.视频水印_点击更多);
        }else{
            params.put("textType","originality");
            MyBeautyStat.onClickByRes(R.string.视频创意_点击更多);
        }
        pauseAll();
//        mWrapper.mSite.openTheme(getContext(), params);
        mSite.openTheme(getContext(),params);
    }

    protected VideoTextDisplayView mVideoTextDisplayView;

    protected void openDisplayView()
    {
        mWrapper.mActionBar.setActionbarTitleIconVisibility(View.GONE);
        changeVideoState(true);
        if(mCurInfo.mResType == 1){
            MyBeautyStat.onClickByRes(R.string.视频水印_点击修改动画);
        }else
        {
            MyBeautyStat.onClickByRes(R.string.视频创意_点击修改动画);
        }
        mVideoTextDisplayView = new VideoTextDisplayView(getContext(),ShareData.PxToDpi_xhdpi(254),new VideoTextDisplayView.DisPlayCallBack()
        {
            @Override
            public void onShowType(int type)
            {
                switch (type)
                {
                    case TYPE_START:
                    case TYPE_ALL:
                    case TYPE_END:
                        setTextViewType(type);
                        break;
                    default:
                        setTextViewType(TYPE_START);
                        break;
                }
                //重新设值
//                mTextAnim.cancel();
//                startTextAnim();
                mWrapper.resumeAll(true);
            }

            @Override
            public void onTimeChang(long startTime, long stayTime)
            {
                mCurInfo.mStartTime = startTime;
                mCurInfo.mStayTime = stayTime;
                mTextView.setStartTime((int) mCurInfo.mStartTime);
                mTextView.setStayTime((int) mCurInfo.mStayTime);
                mWrapper.resumeAll(true);
            }

            @Override
            public void onBack()
            {
                mWrapper.mActionBar.setActionbarTitleIconVisibility(View.VISIBLE);
                mCurModulePage.animate().translationY(ShareData.m_screenHeight).setDuration(150).setListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationStart(Animator animation)
                    {
                        mTextList.setVisibility(View.VISIBLE);
                        mBottomLL.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        removeModuleView();
                        mVideoTextDisplayView = null;
                    }
                }).start();
            }

            @Override
            public void setTitle(String title)
            {
                mWrapper.mActionBar.getActionBarTitleView().setText(title);
            }
        });
        mCurModulePage = mVideoTextDisplayView;
        HashMap<String,Object> data = new HashMap<>();
        data.put("videos", mWrapper.mVideoInfos);
        data.put("videosTime", mWrapper.getVideosDuration());
        data.put("textTime",mTextView.GetTextTotalTimeNoStayTime());
        data.put("type",mCurInfo.mDisplayType);
        data.put("startTime",mCurInfo.mStartTime);
        data.put("stayTime",mCurInfo.mStayTime);
        data.put("showType", mCurInfo.mDisplayType);
        mCurModulePage.SetData(data);
        this.addView(mCurModulePage);
//        mCurModulePage.setTranslationX(ShareData.m_screenWidth);
//        mCurModulePage.animate().translationX(0).setDuration(150).start();
        mCurModulePage.setTranslationY(ShareData.m_screenHeight);
        mCurModulePage.animate().translationY(0).setDuration(150).start();
        mTextList.setVisibility(View.GONE);
        mBottomLL.setVisibility(View.GONE);
//        changeVideoState(true);
//        mPlayBtn.setVisibility(View.GONE);
    }

    private void startTextAnim()
    {
        if(!mTextAnim.isRunning() && !mWrapper.isPlaying())
        {
            mTextAnim.setIntValues(((int)mCurInfo.mStartTime),(int)(mCurInfo.mStartTime+mTextView.GetTextTotalTimeNoStayTime()));
            mTextAnim.setDuration(mTextView.GetTextTotalTimeNoStayTime());
            mTextAnim.start();
        }
    }

    private VideoTextHelpSite mVideoTextHelpSite = new VideoTextHelpSite(){
        @Override
        public void onBack(Context context)
        {
            removeModuleView();
            onPageResult(SiteID.VIDEO_TEXT_HELP, null);
        }
    };

    private ThemeIntroPageSite4 mThemeIntroPageSite4 = new ThemeIntroPageSite4(){

        @Override
        public void OnResourceUse(HashMap<String, Object> params,Context context)
        {
            removeModuleView();
            onPageResult(SiteID.THEME_INTRO_PAGE, params);
        }

        @Override
        public void OnBack(HashMap<String, Object> params,Context context)
        {
            removeModuleView();
            onPageResult(SiteID.THEME_INTRO_PAGE, params);
            changeVideoState(true);
        }
    };

    private TextUnLockPageSite mTextUnLockPageSite = new TextUnLockPageSite()
    {
        @Override
        public void onBack(Context context, HashMap<String, Object> params)
        {
//            mWrapper.resumeAll(false);
            removeModuleView();
            VideoTextPage.this.onPageResult(m_id, params);
            changeVideoState(false);
        }
    };

    private TextEditPageSite mTextEditPageSite = new TextEditPageSite()
    {
        @Override
        public void onBack(Context context)
        {
            OnBack(null);
        }

        @Override
        public void onSave(HashMap<String, Object> data,Context context)
        {
            OnBack(data);
        }

        private void OnBack(final HashMap<String, Object> data){
            postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    removeModuleView();
                    VideoTextPage.this.onPageResult(m_id, data);
                    mTextView.SetSelPendant(0);
                    startTextAnim();

                    //如果是在选择出场类型的页面进入编辑，恢复。
                    if(mVideoTextDisplayView != null){
                        mCurModulePage = mVideoTextDisplayView;
                    }
                }
            },100);
        }
    };

    public void openTextHelpPage(){
        boolean hasShowHelp = TagMgr.CheckTag(getContext(), Tags.VIDEO_TEXT_HELP);
        if(hasShowHelp){
            //商店使用素材来这里会崩
//            mWrapper.onPauseAll();
            if(mIsFromTheme)
            {
                mVideoView.addOnPlayListener(new IVideoPlayer.OnPlayListener()
                {
                    @Override
                    public void onStart()
                    {
                        mVideoView.removeOnPlayListener(this);
                        mWrapper.onPauseAll();
                        TagMgr.SetTag(getContext(), Tags.VIDEO_TEXT_HELP);
                        mSite.openTextHelp(getContext(), null);
                    }

                    @Override
                    public void onResume()
                    {

                    }

                    @Override
                    public void onPause()
                    {

                    }
                });
            }else{
                mWrapper.onPauseAll();
                TagMgr.SetTag(getContext(), Tags.VIDEO_TEXT_HELP);
                mSite.openTextHelp(getContext(), null);
            }
        }
    }

    public void openEditTextPage()
    {
        MyBeautyStat.onClickByRes(R.string.视频水印_打开修改文本);
        TongJi2.AddCountByRes(getContext(), R.integer.修改);
        HashMap<String, Object> params = new HashMap<>();
        params.put("videoText", mTextView.getVideText().getTextInfo());
        params.put("showType", mCurInfo.mDisplayType);
        params.put("img", InitGlassBk());
        pauseAll();
        addModuleView(new TextEditPage(getContext(), mTextEditPageSite),params);
    }

    private IPage addModuleView(IPage page,HashMap<String,Object> params)
    {
        mCurModulePage = page;
        mCurModulePage.setClickable(true);
        mCurModulePage.SetData(params);
        this.addView(mCurModulePage);
        return mCurModulePage;
    }

    private boolean removeModuleView()
    {
        boolean b = false;
        if (mCurModulePage != null)
        {
            if (mCurModulePage.getParent() == this)
            {
                VideoTextPage.this.removeView(mCurModulePage);
            }
            mCurModulePage.onClose();
            mCurModulePage = null;
            return true;
        }
        return b;
    }

    @Override
    public int getBottomPartHeight()
    {
        return ShareData.PxToDpi_xhdpi(300);
    }


    class TextList extends TextListView{

        public TextList(@NonNull Context context,int listType)
        {
            super(context,listType);
        }

        @Override
        protected void onClickNormalItem(View v, DragListItemInfo info, int index)
        {
            if (mWrapper.isDragProgress())
            {
                return;
            }

            if (info.m_uri == DragListItemInfo.URI_MGR) {
                openThemePage();
                return;
            }
            if (info.m_isLock)
            {
                HashMap<String, Object> params = new HashMap<>();
                VideoTextRes data = (VideoTextRes) info.m_ex;
                params.put("res", data);
                params.put("hasAnim", true);
                if (v != null)
                {
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);

                    params.put("centerX", location[0]);
                    params.put("centerY", location[1]);
                    params.put("viewH", v.getHeight());
                    params.put("viewW", v.getWidth());
                }
                openUnLockPage(params);
                return;
            }
            if (info.m_uri == DragListItemInfo.URI_VIDEO_TEXT_NONE)
            {
                cancelSelect();
                clearWaterMark();
                return;
            }

            switch (info.m_style)
            {
                case NORMAL:
                case NEW:
                {
                    if (m_curUri != info.m_uri)
                    {
                        if (info.m_ex instanceof VideoTextRes)
                        {
                            setSelItemByUri(info.m_uri,true);
                        }
                    } else
                    {
                        VideoText videoText = mTextView.getVideText();
                        if (videoText == null)
                        {
                            WaterMarkInfo waterMarkInfo = null;
                            if (info.m_ex instanceof VideoTextRes)
                            {
                                waterMarkInfo = getWaterMarkInfo(((VideoTextRes) info.m_ex).m_res);
                            }
                            if (waterMarkInfo != null)
                            {
                                mTextView.AddWatermark(waterMarkInfo);
                                videoText = mVideoView.getVideoText();
                            }
                        }
                        if (videoText != null && mVideoView.getTotalDuration() >= mTextView.GetTextTotalTimeNoStayTime())
                        {
                            openDisplayView();
                        }
                    }
                    break;
                }
                case NEED_DOWNLOAD:
                {
                    if(info.m_ex instanceof ThemeRes)
                    {
                        HashMap<String, Object> params = new HashMap<>();
                        ThemeItemInfo itemInfo;
                        itemInfo = MgrUtils.GetThemeItemInfoByUri(getContext(), ((ThemeRes) info.m_ex).m_id, ResType.AUDIO_TEXT);
                        params.put("data", itemInfo);
                        if(v != null)
                        {
                            int[] location = new int[2];
                            v.getLocationOnScreen(location);
                            params.put("hasAnim", true);
                            params.put("centerX",location[0]);
                            params.put("centerY", location[1]);
                            params.put("viewH", v.getHeight());
                            params.put("viewW", v.getWidth());
                        }
                        openRecommendPage(params);
                    }
                    break;
                }
            }
        }

        @Override
        public void setData(List<DragListItemInfo> listDatas)
        {
            super.setData(listDatas);
            setSelItemByUri(m_curUri, false);
        }

        @Override
        protected void OnHideItem(int position)
        {
            DragListItemInfo itemInfo = m_listDatas.get(position);
            if (itemInfo != null) {
                VideoTextRes res = (VideoTextRes)itemInfo.m_ex;
                if(res.m_type != BaseRes.TYPE_LOCAL_RES)
                {
                    if (m_curUri == res.m_id) {
                        clearWaterMark();
                    }
                    mTextDatas.remove(res.m_id);
                    if(res.m_id == mOriginalUri){
                        mOriginalUri = 0;
                    }
                }
            }
            super.OnHideItem(position);
        }

        @Override
        protected void cancelSelect()
        {
            super.cancelSelect();
            m_uriInClassify = true;
        }

        public void setSelItemByUri(int uri, boolean isSeData)
        {
            setSelItemByUri(uri);
            DragListItemInfo info = getItemByUri(uri);
            if(info != null)
            {
                if (info.m_ex instanceof VideoTextRes && isSeData)
                {
                    setTextRes((VideoTextRes) info.m_ex);
                }
                if (mVideoView.getTotalDuration() >= mTextView.GetTextTotalTimeNoStayTime())
                {
                    info.m_isHideEditLogo = false;
                } else
                {
                    info.m_isHideEditLogo = true;
                }
                m_uriInClassify = true;
            }
        }
    };
    private void clearWaterMark()
    {
        if (mCurInfo.mResType == 1)
        {
            MyBeautyStat.onClickByRes(R.string.视频水印_删除水印);
        } else
        {
            MyBeautyStat.onClickByRes(R.string.视频创意_删除创意);
        }
        m_uriInClassify = true;
        mTextView.DeleteWatermark();
        mTextView.UpdateUI();
        mCurInfo.mUri = 0;
        if(!mWrapper.isPlaying()){
            mWrapper.showPlayBtn();
            mWrapper.showProgressTip();
        }
    }

    private WaterMarkInfo getWaterMarkInfo(String path)
    {
        WaterMarkInfo info = null;
        String name = path;
        int pos1 = name.lastIndexOf('/');
        if (pos1 > 0)
        {
            name = name.substring(0, pos1);
        }
        name = DownloadMgr.GetFileName(name); //.nom
        info = JsonParser.parseWaterMarkJson(getContext(), name + ".json", path);
        return info;
    }

    private void setTextViewType(int type)
    {
        mCurInfo.mDisplayType = type;
        if (type == TYPE_ALL)
        {
            mCurInfo.mStartTime = 0;
            if(mWrapper.getVideosDuration() > mTextView.GetTextTotalTimeNoStayTime()){
                mCurInfo.mStayTime = (int) (mWrapper.getVideosDuration() - mTextView.GetTextTotalTimeNoStayTime());
            }else{
                mCurInfo.mStayTime = 0;
            }
        } else if (type == TYPE_END)
        {
            mCurInfo.mStayTime = 0;
            if(mWrapper.getVideosDuration() > mTextView.GetTextTotalTimeNoStayTime()){
                mCurInfo.mStartTime = (int) (mWrapper.getVideosDuration() - mTextView.GetTextTotalTimeNoStayTime());
            }else{
                mCurInfo.mStartTime = 0;
            }
        } else
        {
            mCurInfo.mStartTime = 0;
            mCurInfo.mStayTime = 0;
        }
        mTextView.setStartTime((int) mCurInfo.mStartTime);
        mTextView.setStayTime((int) mCurInfo.mStayTime);
    }

    private void setTextRes(VideoTextRes res)
    {
        if (res.m_res != null)
        {
            WaterMarkInfo tempInfo;
            VideoText tempText;
            if (mTextDatas.get(res.m_id) != null)
            {
                tempText = mTextDatas.get(res.m_id);
                mTextView.DeleteWatermark();
                mTextView.AddVideoText(tempText);
            } else
            {
                tempInfo = getWaterMarkInfo(res.m_res);
                mTextView.DeleteWatermark();
                mTextView.AddWatermark(tempInfo);
            }
            if(mCurInfo.mResType == 1)
            {
                MyBeautyStat.onChooseMaterial(String.valueOf(res.m_id),R.string.视频水印);
                MyBeautyStat.onClickByRes(R.string.视频水印_选择视频水印);
            }else{
                MyBeautyStat.onChooseMaterial(String.valueOf(res.m_id),R.string.视频创意);
                MyBeautyStat.onClickByRes(R.string.视频创意_选择视频创意);
            }
            //恢复开始模式
//                setTextViewType(TYPE_START);
            mCurInfo.mTextRes = res;
            //设置水印
            mCurInfo.mUri = res.m_id;
            mTextDatas.put(res.m_id,mTextView.getVideText());
            mTextAnim.cancel();
            setTextViewType(TYPE_START);
            if(mIsFromTheme)
            {
                mIsFromTheme = false;  //视频surfaceview自动创建播放
            }else{
                mTextView.setCurTime(0);
                mWrapper.resumeAll(true);
            }
        }
    }

    private VideoTextCallbackAdapter m_coreCB = new VideoTextCallbackAdapter() {

        @Override
        public void OnViewTouch(boolean haschoose) {

            if (!mWrapper.isDragProgress())
            {
                if(mTextView.getVideText() != null){
                    changeVideoState(!haschoose);
                }else{
                    if (mWrapper.isPlaying())
                    {
                        mWrapper.pauseAll();
                    } else
                    {
                        mWrapper.resumeAll(false);
                    }
                }
            }
        }
        @Override
        public void OnEditBtn()
        {
            openEditTextPage();
        }
    };
}
