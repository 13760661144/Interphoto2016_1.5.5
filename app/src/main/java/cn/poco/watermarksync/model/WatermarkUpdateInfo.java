package cn.poco.watermarksync.model;

import cn.poco.storagesystemlibs.UpdateInfo;

/**
 * Created by Shine on 2017/3/2.
 */

public class WatermarkUpdateInfo extends UpdateInfo{
    public String mTitle;
    public String mCoverImgUrl;
    public String mContent;
    public String mSaveTime;
    // 水印字体信息
    public String mFontInfo;

    public long mFileVolume;
}
