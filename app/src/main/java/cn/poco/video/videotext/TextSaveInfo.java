package cn.poco.video.videotext;

import cn.poco.resource.VideoTextRes;

/**
 * Created by lgd on 2017/11/1.
 * 存储水印模块的数据
 */

public class TextSaveInfo
{
    public int mUri;       //水印的uri
    public int mResType = 2;  //默认创意     // m如果没有uri，  记录和使用上一次用户点击的类型
    public int mDisplayType = VideoTextPage.TYPE_START;
    public long mStartTime = 0;
    public long mStayTime = 0;
    public VideoTextRes mTextRes;
}
