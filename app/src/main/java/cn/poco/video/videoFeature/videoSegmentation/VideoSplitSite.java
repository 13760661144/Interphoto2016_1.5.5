package cn.poco.video.videoFeature.videoSegmentation;

import android.content.Context;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.video.sequenceMosaics.VideoInfo;

/**
 * Created by Simon Meng on 2018/1/17.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoSplitSite extends BaseSite{

    public VideoSplitSite() {
        super(-1);
    }

    @Override
    public IPage MakePage(Context context) {
        return null;
    }

    public void onClickOk(VideoInfo originInfo, VideoInfo splitInfo) {

    }

    public void onBack() {

    }

}
