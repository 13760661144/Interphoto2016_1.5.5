package cn.poco.video.videoFilter;

import java.util.ArrayList;
import java.util.List;

import cn.poco.video.VideoResMgr;

/**
 * Created by Simon Meng on 2018/1/2.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class FilterInnerInfo {
    public List<VideoResMgr.FilterData> mFilterDataList = new ArrayList<>();
    public String mVideoMediaPath;
    public long mClipStartTime;
    public boolean mIsPartialFilter;

}
