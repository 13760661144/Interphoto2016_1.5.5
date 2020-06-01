package cn.poco.video.videoFeature;

import android.content.Context;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.video.sequenceMosaics.VideoInfo;

/**
 * Created by Simon Meng on 2018/1/23.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoFeaturePageSite extends BaseSite{
    public VideoFeaturePageSite() {
        super(-1);
    }

    @Override
    public IPage MakePage(Context context) {
        return null;
    }

    public void onBack() {

    }

    public int onClickFeature() {
        return -1;
    };

    public void onClipVideoOk() {


    }

    public void onCanvasAdjustOk() {

    }

    public void onFilterOk() {

    }

    public void okSpeedRateOk(int videoIndex,VideoInfo info) {


    }


    public void onSplitVideoOk(VideoInfo left, VideoInfo right) {

    }

    public void onDeleteVideoOk(int videoIndex) {


    }

    public void onCopyVideoOk(int videoIndex,VideoInfo info) {


    }


}
