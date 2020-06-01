package cn.poco.video.videoFilter;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cn.poco.video.VideoResMgr;
import cn.poco.video.sequenceMosaics.VideoInfo;

/**
 * Created by Simon Meng on 2018/2/6.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoFilterManager {
    private Context mContext;

    private static VideoFilterManager sInstance;
    public static VideoFilterManager getInstance(Context context, List<VideoInfo> videoInfoList) {
        if (sInstance == null) {
            sInstance = new VideoFilterManager(context, videoInfoList);
        }
        return sInstance;
    }

    public static VideoFilterManager getsInstance() {
        return sInstance;
    }

    private VideoFilterManager(Context context, List<VideoInfo> videoInfoList) {
        mContext = context;
        mAdjustDataList.clear();
        for (VideoInfo item : videoInfoList) {
            if (videoInfoList.indexOf(item) == 0)  {
                VideoResMgr.FilterData filterData = new VideoResMgr.FilterData();
                filterData.mFilterUrl = -1;
                filterData.mFilterAlpha = 1;
                mFilterDataList.add(filterData);

                mAdjustDataList.add(item.mBrightness.Clone());
                mAdjustDataList.add(item.mContrast.Clone());
                mAdjustDataList.add(item.mCurveData.CloneData());
                mAdjustDataList.add(item.mSaturation.Clone());
                mAdjustDataList.add(item.mSharpen.Clone());
                mAdjustDataList.add(item.mColorTemperatur.Clone());
                mAdjustDataList.add(item.mColorBalance.Clone());
                mAdjustDataList.add(item.mHighLight.Clone());
                mAdjustDataList.add(item.mShade.Clone());
                mAdjustDataList.add(item.mDrakCorner.Clone());
                // 存放最原始的视频数据
            }
        }
    }

    // 全局滤镜的原始滤镜数据
    private List<VideoResMgr.AdjustData> mAdjustDataList = new ArrayList<>();
    public List<VideoResMgr.AdjustData> getGlobalAdjustData() {
        return mAdjustDataList;
    }


    public void setGlobalAdjustData(List<VideoResMgr.AdjustData> savedAdjustList) {
        this.mAdjustDataList = savedAdjustList;
    }

    private List<VideoResMgr.FilterData> mFilterDataList = new ArrayList<>();
    public List<VideoResMgr.FilterData> getGlobalFilterData() {
        return mFilterDataList;
    }

    public void setGlobalFilterData(List<VideoResMgr.FilterData> filterData) {
        this.mFilterDataList = filterData;
    }



    public void clear() {
        this.mAdjustDataList.clear();
        this.mFilterDataList.clear();
        sInstance = null;
    }







}
