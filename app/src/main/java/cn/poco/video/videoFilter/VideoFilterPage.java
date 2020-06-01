package cn.poco.video.videoFilter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.MaterialMgr2.ThemeIntroPage;
import cn.poco.MaterialMgr2.site.ThemeIntroPageSite2;
import cn.poco.MaterialMgr2.site.ThemePageSite2;
import cn.poco.beautify.BeautyAdjustType;
import cn.poco.beautify.BeautyColorType;
import cn.poco.beautify.CurveView;
import cn.poco.beautify.page.MasterIntroPage;
import cn.poco.beautify.site.BightHelpAnimPageSite;
import cn.poco.beautify.site.MasterIntroPageSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.interphoto2.R;
import cn.poco.resource.FilterRes;
import cn.poco.resource.ResType;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.ShareData;
import cn.poco.video.VideoResMgr;
import cn.poco.video.dialog.FilterTipDialog;
import cn.poco.video.frames.VideoPage;
import cn.poco.video.page.VideoModeWrapper;
import cn.poco.video.render.adjust.AdjustItem;
import cn.poco.video.render.curve.CurveEffect;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.view.ActionBar;

/**
 * Created by Simon Meng on 2017/12/29.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoFilterPage extends VideoPage implements EventRouter.EventChain{

    private static final String TAG = "视频滤镜";
    private FilterBottomView mFilterBottmView;
    private Context mContext;
    private ThemeIntroPage mThemeIntroPage;
    private MasterIntroPage mMasterIntroPage;
    private boolean mIsSelfHandle;

    private ActionBar mActionBar;
    private VideoModeWrapper mVideoWrapper;

    private boolean mConfirmSelectedEffect;

    // 目前选择选中的uri
    private FilterInnerInfo mInfo;

    private VideoFilterSite mFilterSite;

    private VideoFilterManager mFilterManager;


    public VideoFilterPage(Context context, BaseSite site, VideoModeWrapper videoWrapper) {
        super(context, site);
        mContext = context;
        mActionBar = videoWrapper.mActionBar;
        mVideoWrapper = videoWrapper;
        mFilterSite = (VideoFilterSite) site;
        mInfo = new FilterInnerInfo();
        mFilterManager = VideoFilterManager.getInstance(context, videoWrapper.mVideoInfos);
        initView();
        initChain();
        TongJiUtils.onPageStart(context, TAG);
    }


    private boolean checkFilterUrlValid() {
        boolean theSameFilter = true;
        if (mVideoWrapper.mVideoInfos.size() > 1) {
            for (int i = 0; i < mVideoWrapper.mVideoInfos.size(); i++) {
                for (int j = i + 1; j < mVideoWrapper.mVideoInfos.size(); j++) {
                    VideoInfo currentInfo = mVideoWrapper.mVideoInfos.get(i);
                    VideoInfo nextInfo = mVideoWrapper.mVideoInfos.get(j);
                    if (currentInfo.mFilterUri != nextInfo.mFilterUri || currentInfo.mFilterAlpha != nextInfo.mFilterAlpha) {
                        theSameFilter = false;
                    }
                }
            }
            mInfo.mFilterDataList.addAll(mFilterManager.getGlobalFilterData());
        } else if (mVideoWrapper.mVideoInfos.size() == 1) {
            mInfo.mFilterDataList.addAll(mFilterManager.getGlobalFilterData());
            theSameFilter = true;
        }
        return theSameFilter;
    }

    private int mTongjiId = -1;
    public boolean mIsPartialFilter;
    private int mCurVideoIndex;
    private VideoInfo mVideoInfo;
    public void setAsPartialFilter(VideoInfo videoInfo, int videoIndex) {
        mIsPartialFilter = true;
        mCurVideoIndex = videoIndex;
        mVideoInfo = videoInfo;
        mInfo.mIsPartialFilter = mIsPartialFilter;
//        List<VideoInfo> tempInfoList = new ArrayList<>();
//        tempInfoList.add(videoInfo);
        initOriginInfo(mVideoWrapper.mVideoInfos);

        // 全局滤镜到局部滤镜
        mFilterBottmView.s_selUri = videoInfo.mFilterUri;
        VideoResMgr.FilterData filterData = new VideoResMgr.FilterData(mFilterBottmView.s_selUri, videoInfo.mFilterAlpha);
        mInfo.mFilterDataList.add(filterData);
        mFilterBottmView.adjustFilterResource();
        shenCePageStartStatistic(R.string.视频滤镜, R.string.单视频滤镜);
    }

    public void setAsGlobalFilter() {
        mIsPartialFilter = false;
        mInfo.mIsPartialFilter = mIsPartialFilter;
        initOriginInfo(mVideoWrapper.mVideoInfos);

        // 局部滤镜到全局滤镜
        boolean isTheSameFilter = checkFilterUrlValid();
        if (isTheSameFilter) {
            FilterBottomView.s_selUri = mVideoWrapper.mVideoInfos.get(0).mFilterUri;
        } else {
            FilterBottomView.s_selUri = -1;
        }
        mFilterBottmView.adjustFilterResource();
        MyBeautyStat.onPageEndByRes(R.string.视频滤镜);
        shenCePageStartStatistic(R.string.视频滤镜, R.string.单视频滤镜);
        if (mVideoWrapper.mIsParticialFilterModify) {
            showFilterTipDialog();
        }
    }

    private FilterTipDialog mTipDialog;

    private void showFilterTipDialog() {
        if (mVideoWrapper.isPlaying()) {
            mVideoWrapper.pauseAll();
        }
        if (TagMgr.CheckTag(mContext, Tags.VIDEO_FILTER_TIP)) {
            mTipDialog = new FilterTipDialog((Activity)mContext);
            mTipDialog.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTipDialog.dismiss();
                    mVideoWrapper.resumeAll(true);
                }
            });
            mTipDialog.show();
        }
    }

    // 进入时的原始数据
    private List<VideoInfo> mOrginVideoInfo = new ArrayList<>();

    private List<VideoInfo> mTempBeautifyVideoInfo = new ArrayList<>();
    private void initOriginInfo(List<VideoInfo> infoList) {
        List<VideoResMgr.AdjustData> adjustItemList = new ArrayList<>();
        for (VideoInfo videoInfo : infoList) {
            VideoInfo temp = new VideoInfo();
            temp.CloneFilterData(videoInfo);
            mOrginVideoInfo.add(temp);
            if (mIsPartialFilter && infoList.indexOf(videoInfo) == mCurVideoIndex) {
                //  局部滤镜读取自己的数据
                adjustItemList.add(temp.mBrightness.Clone());
                adjustItemList.add(temp.mContrast.Clone());
                adjustItemList.add(temp.mCurveData.CloneData());
                adjustItemList.add(temp.mSaturation.Clone());
                adjustItemList.add(temp.mSharpen.Clone());
                adjustItemList.add(temp.mColorTemperatur.Clone());
                adjustItemList.add(temp.mColorBalance.Clone());
                adjustItemList.add(temp.mHighLight.Clone());
                adjustItemList.add(temp.mShade.Clone());
                adjustItemList.add(temp.mDrakCorner.Clone());
            }
        }
        if (!mIsPartialFilter) {
            // 全局滤镜读取单独保存的全局滤镜数据
            for (VideoResMgr.AdjustData item : mFilterManager.getGlobalAdjustData()) {
                if (item instanceof VideoResMgr.CurveData) {
                    VideoResMgr.CurveData curveData = (VideoResMgr.CurveData) item;
                    VideoResMgr.CurveData curveDataClone = curveData.CloneData();
                    adjustItemList.add(curveDataClone);
                } else {
                    VideoResMgr.AdjustData cloneOne = item.Clone();
                    adjustItemList.add(cloneOne);
                }

            }
        }

        mFilterBottmView.setAdjustData(adjustItemList);

        for (VideoInfo videoInfo : infoList) {
            VideoInfo temp = new VideoInfo();
            temp.CloneFilterData(videoInfo);
            mTempBeautifyVideoInfo.add(temp);
        }
    }

    private void initView() {
        mActionBar.setOnActionbarMenuItemClick(new ActionBar.onActionbarMenuItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == ActionBar.LEFT_MENU_ITEM_CLICK) {
                    onBack();
                } else if (id == ActionBar.RIGHT_MENU_ITEM_CLICK) {
                    TongJi2.AddCountByRes(mContext, R.integer.保存);
                    shenCeOnClickRes(R.string.视频滤镜_保存滤镜美化, R.string.单视频滤镜_保存滤镜美化);
                    // 旧页面逻辑
                    mVideoWrapper.isCanSave = true;
                    mVideoWrapper.isModify = true;
                    for (VideoInfo item : mTempBeautifyVideoInfo) {
                        int index = mTempBeautifyVideoInfo.indexOf(item);
                        if (mIsPartialFilter) {
                            // 局部滤镜
                            mVideoWrapper.mIsParticialFilterModify = true;

                            if (index == mCurVideoIndex) {
                                mVideoWrapper.mVideoInfos.get(index).CloneFilterData(item);
                            }
                        } else {
                            // 全局滤镜
                            if (mTempBeautifyVideoInfo.indexOf(item) == 0) {
                                List<VideoResMgr.AdjustData> adjustDataList = new ArrayList<>();
                                adjustDataList.add(item.mBrightness.Clone());
                                adjustDataList.add(item.mContrast.Clone());
                                adjustDataList.add(item.mCurveData.CloneData());
                                adjustDataList.add(item.mSaturation.Clone());
                                adjustDataList.add(item.mSharpen.Clone());
                                adjustDataList.add(item.mColorTemperatur.Clone());
                                adjustDataList.add(item.mColorBalance.Clone());
                                adjustDataList.add(item.mHighLight.Clone());
                                adjustDataList.add(item.mShade.Clone());
                                adjustDataList.add(item.mDrakCorner.Clone());

                                List<VideoResMgr.FilterData> fiterDataList = new ArrayList<>();
                                VideoResMgr.FilterData filterData = new VideoResMgr.FilterData();
                                filterData.mFilterAlpha = item.mFilterAlpha;
                                filterData.mFilterUrl = item.mFilterUri;
                                fiterDataList.add(filterData);

                                mFilterManager.setGlobalAdjustData(adjustDataList);
                                mFilterManager.setGlobalFilterData(fiterDataList);
                            }

                            mVideoWrapper.mVideoInfos.get(index).CloneFilterData(item);
                        }
                    }
                    if (mTongjiId != -1) {
                        MyBeautyStat.onVideoFilter(String.valueOf(mTongjiId));
                    }

                    mFilterBottmView.setHandleEvent(false);
                    mConfirmSelectedEffect = true;
                    mFilterSite.onBack();
                }
            }
        });

        mFilterBottmView = new FilterBottomView(mContext, mInfo);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);
        mFilterBottmView.setLayoutParams(params);
        this.addView(mFilterBottmView);
        mFilterBottmView.setBottomViewCallback(new FilterBottomView.BottomViewCallbackAdapter() {
            @Override
            public void onNeedDownItemClick(HashMap<String, Object> params) {
                shenCeOnClickRes(R.string.视频滤镜_打开推荐位, R.string.单视频滤镜_打开推荐位);
                openRecommend(params);
            }


            @Override
            public void onApplayFilter(FilterRes filterRes, int uri) {
                if (!mVideoWrapper.isPlaying() && (mTipDialog == null || !mTipDialog.isShowing())) {
                    mVideoWrapper.resumeAll(false);
                }
//				mVideoEntry.mVideoFilter.mFilterRes = filterRes;
                boolean filterResDeleted = false;
                if (filterRes != null) {
                    filterResDeleted = false;
                    TongJi2.AddCountById(filterRes.m_tjId + "");
                    shenCeOnClickRes(R.string.视频滤镜_选择滤镜, R.string.单视频滤镜_选择滤镜);
                    MyBeautyStat.onChooseMaterial(String.valueOf(filterRes.m_tjId), R.string.视频滤镜);
                    mTongjiId = filterRes.m_tjId;

                    if (mIsPartialFilter) {
                        mVideoWrapper.changeFilter(mCurVideoIndex, filterRes);
                    } else {
                        mVideoWrapper.changeFilter(filterRes);
                    }
                } else {
                    mTongjiId = -1;
                    filterResDeleted = true;
                    mVideoWrapper.changeFilter(null);
                }

                for (VideoInfo item : mTempBeautifyVideoInfo) {
                    int index = mTempBeautifyVideoInfo.indexOf(item);
                    if (mIsPartialFilter) {
                        if (filterResDeleted) {
                            item.mFilterUri = uri;
                            if (filterRes != null) {
                                item.mFilterAlpha = (int)(filterRes.m_alpha / 100f * 12);
                            }
                        } else {
                            if (index == mCurVideoIndex) {
                                item.mFilterUri = uri;
                                if (filterRes != null) {
                                    item.mFilterAlpha = (int)(filterRes.m_alpha / 100f * 12);
                                }
                            }
                        }
                    } else {
                        item.mFilterUri = uri;
                        if (filterRes != null) {
                            item.mFilterAlpha = (int)(filterRes.m_alpha / 100f * 12);
                        }
                    }
                }
            }

            @Override
            public void onMoreItemClick(View v, int index) {
                shenCeOnClickRes(R.string.视频滤镜_进入更多, R.string.单视频滤镜_进入更多);
                mVideoWrapper.pauseAll();
                HashMap<String, Object> params = new HashMap<>();
                params.put("type", ResType.FILTER);
                params.put("typeOnly", true);
                MyFramework.SITE_Popup(v.getContext(), ThemePageSite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
            }

            @Override
            public void onItemAvatarClick(HashMap<String, Object> params) {
                MyBeautyStat.onClickByRes(R.string.视频滤镜_打开大师介绍页);
                openMasterPage(params);
            }

            @Override
            public void onAdjustModeSelected(BeautyAdjustType type, String title) {
                mActionBar.getActionBarTitleView().setText(title);
            }

            @Override
            public void onFilterTypeChange(int mode) {
                if (mode == 0) {
                    mActionBar.getActionBarTitleView().setText(mContext.getString(R.string.Filters));
                    shenCeOnClickRes(R.string.视频滤镜_退出细节调整, R.string.单视频细节调整_退出细节调整);
                } else if (mode == 1) {
                    TongJi2.AddCountByRes(mContext, R.integer.调整);
                    mActionBar.getActionBarTitleView().setText(mContext.getString(R.string.Adjust));
                    shenCeOnClickRes(R.string.视频滤镜_进入细节调整, R.string.单视频滤镜_进入细节调整);
                }
            }

            @Override
            public void onAdjustbarSeek(BeautyColorType filterType, BeautyAdjustType type, float value, float dataRaw) {
                if (filterType == BeautyColorType.FILTER) {
                    // 添加滤镜透明度
                    mVideoWrapper.changeFilterAlpha(value);
                    for (VideoInfo item : mTempBeautifyVideoInfo) {
                        item.mFilterAlpha = (int)dataRaw;
                    }
                } else if (filterType == BeautyColorType.ADJUST) {
                    AdjustItem adjustItem = null;
                    switch (type) {
                        case BRIGHTNESS: {
                            TongJi2.AddCountByRes(mContext, R.integer.亮度);
                            adjustItem = new AdjustItem(AdjustItem.BRIGHTNESS, value);

                            // 存放临时数据
                            for (VideoInfo item : mTempBeautifyVideoInfo) {
                                item.mBrightness.m_value = value;
                            }
                            break;
                        }
                        case TEMPERATURE: {
                            TongJi2.AddCountByRes(mContext, R.integer.色温);
                            adjustItem = new AdjustItem(AdjustItem.WHITE_BALANCE, value);
                            // 存放临时数据
                            for (VideoInfo item : mTempBeautifyVideoInfo) {
                                item.mColorTemperatur.m_value = value;
                            }
                            break;
                        }

                        case SATURABILITY: {
                            TongJi2.AddCountByRes(mContext, R.integer.饱和度);
                            adjustItem = new AdjustItem(AdjustItem.SATURATION, value);

                            for (VideoInfo item : mTempBeautifyVideoInfo) {
                                item.mSaturation.m_value = value;
                            }
                            break;
                        }

                        case CONTRAST: {
                            TongJi2.AddCountByRes(mContext, R.integer.对比);
                            adjustItem = new AdjustItem(AdjustItem.CONTRAST, value);

                            for (VideoInfo item : mTempBeautifyVideoInfo) {
                                item.mContrast.m_value = value;
                            }
                            break;
                        }

                        case SHARPEN: {
                            TongJi2.AddCountByRes(mContext, R.integer.锐化);
                            adjustItem = new AdjustItem(AdjustItem.SHARPEN, value);

                            for (VideoInfo item : mTempBeautifyVideoInfo) {
                                item.mSharpen.m_value = value;
                            }
                            break;
                        }

                        case HUE: {
                            TongJi2.AddCountByRes(mContext, R.integer.色调);
                            adjustItem = new AdjustItem(AdjustItem.COLOR_BALANCE, value);

                            for (VideoInfo item : mTempBeautifyVideoInfo) {
                                item.mColorBalance.m_value = value;
                            }
                            break;
                        }

                        case DARKCORNER: {
                            TongJi2.AddCountByRes(mContext, R.integer.暗角);
                            adjustItem = new AdjustItem(AdjustItem.DARK_CORNER, value);

                            for (VideoInfo item : mTempBeautifyVideoInfo) {
                                item.mDrakCorner.m_value = value;
                            }
                            break;
                        }

                        case HIGHTLIGHT: {
                            TongJi2.AddCountByRes(mContext, R.integer.高光);
                            float finalValue = (1 - value);
                            finalValue = finalValue < 0 ? 0 : finalValue > 1 ? 1 : finalValue;
                            adjustItem = new AdjustItem(AdjustItem.HIGHLIGHT, finalValue);

                            for (VideoInfo item : mTempBeautifyVideoInfo) {
                                item.mHighLight.m_value = value;
                            }
                            break;
                        }

                        case SHADE: {
                            TongJi2.AddCountByRes(mContext, R.integer.阴影);
                            adjustItem = new AdjustItem(AdjustItem.SHADOW, value);

                            for (VideoInfo item : mTempBeautifyVideoInfo) {
                                item.mShade.m_value = value;
                            }
                            break;
                        }

                        default: {
                            break;
                        }
                    }
                    if (adjustItem != null) {
                        mVideoWrapper.addAdjust(0, adjustItem);
                    }
                }
            }

            @Override
            public void onFilterAlphaBarVisibilityChange(boolean show) {
                if (show) {
                    shenCeOnClickRes(R.string.视频滤镜_进入滤镜不透明度, R.string.单视频滤镜_进入滤镜不透明度);
                } else {
                    MyBeautyStat.onClickByRes(R.string.视频滤镜_滤镜不透明度_收起滤镜不透明度);
                }
            }

            @Override
            public void openHelpPage(HashMap<String, Object> params, Context context) {
                MyFramework.SITE_Popup(context, BightHelpAnimPageSite.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
            }

            @Override
            public void doCurveEffect(int mode, List<Point> controlPointList, CurveView.ControlInfo red, CurveView.ControlInfo green, CurveView.ControlInfo blue, CurveView.ControlInfo rgb) {
                if (mIsPartialFilter) {
                    mVideoWrapper.mVideoView.doCurve(mCurVideoIndex, mode, controlPointList);
                } else {
                    mVideoWrapper.mVideoView.doCurve(mode, controlPointList);
                }

                for (VideoInfo item : mTempBeautifyVideoInfo) {
                    boolean saveCurveData = false;
                    if (mIsPartialFilter) {
                        if (mTempBeautifyVideoInfo.indexOf(item) == mCurVideoIndex) {
                            saveCurveData = true;
                        }
                    } else {
                        saveCurveData = true;
                    }
                    if (saveCurveData) {
                        item.mCurveData.mRed = item.mCurveData.transformToVideoControlInfo(red);
                        item.mCurveData.mGreen = item.mCurveData.transformToVideoControlInfo(green);
                        item.mCurveData.mBlue = item.mCurveData.transformToVideoControlInfo(blue);
                        item.mCurveData.mRGB = item.mCurveData.transformToVideoControlInfo(rgb);
                    }
                }
            }
        });
    }




    private void initChain() {
        List<EventRouter.EventChain> chainList = new ArrayList<>();
        chainList.add(mMasterIntroSite2);
        chainList.add(mThemeIntroSite5);
        chainList.add(mFilterBottmView);
        chainList.add(this);
        EventRouter.getInstance().initEventChain(chainList);
    }


    private void shenCeOnClickRes(int globalData, int partialData) {
        if (!mIsPartialFilter) {
            MyBeautyStat.onClickByRes(globalData);
        } else {
            MyBeautyStat.onClickByRes(partialData);
        }
    }

    private void shenCePageStartStatistic(int globalData, int partialData) {
        if (!mIsPartialFilter) {
            MyBeautyStat.onPageStartByRes(globalData);
        } else {
            MyBeautyStat.onPageStartByRes(partialData);
        }
    }


    private void shenCePageStopStatistic(int globalData, int partialData) {
        if (!mIsPartialFilter) {
            MyBeautyStat.onPageEndByRes(globalData);
        } else {
            MyBeautyStat.onPageEndByRes(partialData);
        }
    }


    @Override
    public void SetData(HashMap<String, Object> params) {
		if (params != null) {
			Object obj = params.get("videos");
			if (obj != null) {
                List<VideoInfo> mVideoList = (List<VideoInfo>)obj;
                if (mIsPartialFilter) {
                    mInfo.mVideoMediaPath = mVideoList.get(mCurVideoIndex).mPath;
                    mInfo.mClipStartTime = mVideoList.get(mCurVideoIndex).mSelectStartTime;
                } else {
                    mInfo.mVideoMediaPath = mVideoList.get(0).mPath;
                    mInfo.mClipStartTime = mVideoList.get(0).mSelectStartTime;
                }

				mFilterBottmView.decodeAndUpdateCoverImage();
			}
		}
    }

    @Override
    public void onBack() {
        EventRouter.getInstance().dispatchEvent(EventRouter.Event.OnBack);
        if (mIsSelfHandle) {
            if (!mConfirmSelectedEffect) {
                if (mOrginVideoInfo.size() > 0) {
                    for (VideoInfo item : mOrginVideoInfo) {
                        int videoIndex = mOrginVideoInfo.indexOf(item);
                        if (mFilterBottmView.isFilterUriValid(item.mFilterUri)) {
                            FilterRes filterRes = mFilterBottmView.getFilterResByUri(item.mFilterUri);
                            if (mIsPartialFilter) {
                                if (mOrginVideoInfo.indexOf(item) == mCurVideoIndex) {
                                    mVideoWrapper.changeFilter(mCurVideoIndex, filterRes);
                                    removeFilterAdjustEffect(videoIndex, item);
                                }
                            } else {
                                mVideoWrapper.changeFilter(mOrginVideoInfo.indexOf(item), filterRes);
                                removeFilterAdjustEffect(videoIndex, item);
                            }
                        } else {
                            mVideoWrapper.changeFilter(mOrginVideoInfo.indexOf(item),null);
                            if (mIsPartialFilter) {
                                if (mCurVideoIndex == mOrginVideoInfo.indexOf(item)) {
                                    removeFilterAdjustEffect(videoIndex, item);
                                }
                            } else {
                                removeFilterAdjustEffect(videoIndex, item);
                            }
                        }
                    }
                }
                mOrginVideoInfo.clear();
                mTempBeautifyVideoInfo.clear();
                mIsSelfHandle = false;
                mFilterSite.onBack();
            }

            shenCeOnClickRes(R.string.视频滤镜_退出视频滤镜, R.string.单视频滤镜_退出视频滤镜);
        }
    }



    private void removeFilterAdjustEffect(int videoIndex, VideoInfo mVideoInfo) {
        // 取消所有细节调整效果
        // 亮度
        AdjustItem adjustItem = new AdjustItem(AdjustItem.BRIGHTNESS, mVideoInfo.mBrightness.m_value);
        mVideoWrapper.addAdjust(AdjustItem.BRIGHTNESS, adjustItem);

        //曲线
        List<Point> controlPointList = new ArrayList<>();
        controlPointList.clear();
        for (Point item : mVideoInfo.mCurveData.mRed.m_ctrlPoints) {
            int[] result = makeValidPoint(item.x, item.y);
            controlPointList.add(new Point(result[0], result[1]));
        }
        mVideoWrapper.mVideoView.doCurve(videoIndex, CurveEffect.CURVE_R, controlPointList);

        controlPointList.clear();
        for (Point item : mVideoInfo.mCurveData.mGreen.m_ctrlPoints) {
            int[] result = makeValidPoint(item.x, item.y);
            controlPointList.add(new Point(result[0], result[1]));
        }
        mVideoWrapper.mVideoView.doCurve(videoIndex, CurveEffect.CURVE_G, controlPointList);

        controlPointList.clear();
        for (Point item : mVideoInfo.mCurveData.mBlue.m_ctrlPoints) {
            int[] result = makeValidPoint(item.x, item.y);
            controlPointList.add(new Point(result[0], result[1]));
        }
        mVideoWrapper.mVideoView.doCurve(videoIndex, CurveEffect.CURVE_B, controlPointList);

        controlPointList.clear();
        for (Point item : mVideoInfo.mCurveData.mRGB.m_ctrlPoints) {
            int[] result = makeValidPoint(item.x, item.y);
            controlPointList.add(new Point(result[0], result[1]));
        }
        mVideoWrapper.mVideoView.doCurve(videoIndex, CurveEffect.CURVE_RGB, controlPointList);

        // 白平衡(色温)
        adjustItem = new AdjustItem(AdjustItem.WHITE_BALANCE, mVideoInfo.mColorTemperatur.m_value);
        mVideoWrapper.addAdjust(AdjustItem.WHITE_BALANCE, adjustItem);

        // 饱和度
        adjustItem = new AdjustItem(AdjustItem.SATURATION, mVideoInfo.mSaturation.m_value);
        mVideoWrapper.addAdjust(AdjustItem.SATURATION, adjustItem);

        // 对比度
        adjustItem = new AdjustItem(AdjustItem.CONTRAST, mVideoInfo.mContrast.m_value);
        mVideoWrapper.addAdjust(AdjustItem.CONTRAST, adjustItem);

        // 锐度
        adjustItem = new AdjustItem(AdjustItem.SHARPEN, mVideoInfo.mSharpen.m_value);
        mVideoWrapper.addAdjust(AdjustItem.SHARPEN, adjustItem);

        // 色调
        adjustItem = new AdjustItem(AdjustItem.COLOR_BALANCE, mVideoInfo.mColorBalance.m_value);
        mVideoWrapper.addAdjust(AdjustItem.COLOR_BALANCE, adjustItem);

        // 暗角
        adjustItem = new AdjustItem(AdjustItem.DARK_CORNER, mVideoInfo.mDrakCorner.m_value);
        mVideoWrapper.addAdjust(AdjustItem.DARK_CORNER, adjustItem);

        // 高光
        adjustItem = new AdjustItem(AdjustItem.HIGHLIGHT, mVideoInfo.mHighLight.m_value);
        mVideoWrapper.addAdjust(AdjustItem.HIGHLIGHT, adjustItem);

        // 阴影
        adjustItem = new AdjustItem(AdjustItem.SHADOW, mVideoInfo.mShade.m_value);
        mVideoWrapper.addAdjust(AdjustItem.SHADOW, adjustItem);
    }

    private int[] makeValidPoint(float x, float y) {
        int[] result = new int[2];
        int size = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(100);
        result[0] = (int)(x / size * 255);
        result[1] = (int)((size - y) * 1.0f / size * 255);
        result[0] = result[0] < 0 ? 0 : result[0] > 255 ? 255 : result[0];
        result[1] = result[1] < 0 ? 0 : result[1] > 255 ? 255 : result[1];
        return result;
    }



    @Override
    public void setNextChain(EventRouter.EventChain chain) {

    }

    @Override
    public boolean handleEvent(EventRouter.Event event) {
        mIsSelfHandle = true;
        return mIsSelfHandle;
    }


    @Override
    public void onPause() {
        EventRouter.getInstance().dispatchEvent(EventRouter.Event.OnPause);
        if (mIsSelfHandle) {
            mVideoWrapper.onPauseAll();
            mIsSelfHandle = false;
        }
        TongJiUtils.onPagePause(mContext, TAG);
    }


    @Override
    public void onResume() {
        EventRouter.getInstance().dispatchEvent(EventRouter.Event.OnResume);
        if (mIsSelfHandle) {
            mVideoWrapper.onResumeAll();
            mIsSelfHandle = false;
        }
        TongJiUtils.onPageResume(mContext, TAG);
    }

    @Override
    public void onPageResult(int siteID, HashMap<String, Object> params) {
        mFilterBottmView.onPageResult(siteID, params);
    }

    @Override
    public void onClose() {
        clear();
        TongJiUtils.onPageEnd(mContext, TAG);
        shenCePageStopStatistic(R.string.视频滤镜, R.string.单视频滤镜);
    }

    @Override
    public int getBottomPartHeight() {
        return ShareData.PxToDpi_xhdpi(300);
    }

    private void clear() {
        EventRouter.getInstance().clear();
        mFilterBottmView.releaseMemButKeepData();
    }


    protected void openMasterPage(HashMap<String, Object> params) {
        mVideoWrapper.pauseAll();

        if (mMasterIntroPage != null) {
            this.removeView(mMasterIntroPage);
            mMasterIntroPage.onClose();
            mMasterIntroPage = null;
        }
        mMasterIntroPage = new MasterIntroPage(getContext(), mMasterIntroSite2);
        params.put("pageId", R.string.视频滤镜);
        mMasterIntroPage.SetData(params);
        this.addView(mMasterIntroPage);
    }

    private void openRecommend(HashMap<String, Object> params) {
        mVideoWrapper.pauseAll();

        if (mThemeIntroPage != null) {
            removeView(mThemeIntroPage);
            mThemeIntroPage.onClose();
            mThemeIntroPage = null;
        }
        mThemeIntroPage = new ThemeIntroPage(getContext(), mThemeIntroSite5);
        mThemeIntroPage.SetData(params);
        this.addView(mThemeIntroPage);
    }



    private ThemeIntroPageSite5 mThemeIntroSite5 = new ThemeIntroPageSite5();

    public class ThemeIntroPageSite5 extends ThemeIntroPageSite2 implements EventRouter.EventChain {
        private EventRouter.EventChain mNextChain;


        @Override
        public void OnResourceUse(HashMap<String, Object> params,Context context) {
            super.OnResourceUse(params,context);
        }

        @Override
        public void OnBack(HashMap<String, Object> params,Context context) {
            VideoFilterPage.this.onPageResult(SiteID.THEME_INTRO_PAGE, params);
            if (mThemeIntroPage != null) {
                VideoFilterPage.this.removeView(mThemeIntroPage);
                mThemeIntroPage.onClose();
                mThemeIntroPage = null;
            }
        }

        @Override
        public void setNextChain(EventRouter.EventChain chain) {
            mNextChain = chain;
        }

        @Override
        public boolean handleEvent(EventRouter.Event event) {
            if (mThemeIntroPage != null) {
                if (event == EventRouter.Event.OnBack) {
                    mThemeIntroPage.onBack();
                    return true;
                } else if (event == EventRouter.Event.OnResume) {
                    mThemeIntroPage.onResume();
                    return true;
                } else if (event == EventRouter.Event.OnPause) {
                    mThemeIntroPage.onPause();
                    return true;
                }
            } else {
                if (mNextChain != null) {
                    mNextChain.handleEvent(event);
                }
            }
            return false;
        }
    }


    private MasterIntroPageSite2 mMasterIntroSite2 = new MasterIntroPageSite2();

    private class MasterIntroPageSite2 extends MasterIntroPageSite implements EventRouter.EventChain {
        private EventRouter.EventChain mNextChain;

        @Override
        public void onBack(HashMap<String, Object> params,Context context) {
            MyBeautyStat.onClickByRes(R.string.视频滤镜_大师介绍页_退出大师介绍页);
            mVideoWrapper.resumeAll(false);
            if (mMasterIntroPage != null) {
                VideoFilterPage.this.removeView(mMasterIntroPage);
                mMasterIntroPage.onClose();
                mMasterIntroPage = null;
            }

            if (params != null) {
                int id = -1;
                boolean lock = true;
                if (params.get("id") != null) {
                    id = (Integer)params.get("id");
                }
                if (params.get("lock") != null) {
                    lock = (Boolean)params.get("lock");
                }
                if (id != -1 && lock == false) {
                    mFilterBottmView.unlockMasterRes(id);
                }
            }

        }

        @Override
        public void setNextChain(EventRouter.EventChain chain) {
            mNextChain = chain;
        }

        @Override
        public boolean handleEvent(EventRouter.Event event) {
            if (mMasterIntroPage != null) {
                if (event == EventRouter.Event.OnBack) {
                    mMasterIntroPage.onBack();
                    return true;
                } else if (event == EventRouter.Event.OnResume) {
                    mMasterIntroPage.onResume();
                    return true;
                } else if (event == EventRouter.Event.OnPause) {
                    mMasterIntroPage.onPause();
                    return true;
                }
            } else {
                if (mNextChain != null) {
                    mNextChain.handleEvent(event);
                }
            }
            return false;
        }
    }



}
