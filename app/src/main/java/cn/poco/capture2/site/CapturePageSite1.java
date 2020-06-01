package cn.poco.capture2.site;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.capture2.model.Snippet;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by lgd on 2018/1/9.
 * 视频处理->相册->
 */

public class CapturePageSite1 extends CapturePageSite
{
    /**
     * 拍完视频后处理
     * @param params 参数
     * data 视频路径 String
     */
    @Override
    public void openProcessVideo(Context context, HashMap<String, Object> params) {

        Snippet snippet = (Snippet)params.get("snippet");
        params.remove("snippet");

        int filterUri = -1;
        int filterAlpha = 1;
        if (snippet.filterItem != null) {
            filterUri = snippet.filterItem.filterUri;
            filterAlpha = (int)(snippet.alpha * 12);
        }
        long duration = VideoUtils.getDurationFromVideo2(snippet.path);
        VideoInfo videoInfo = new VideoInfo(snippet.path, duration, filterUri, filterAlpha);
        List<VideoInfo> list = new ArrayList<>();
        list.add(videoInfo);
        params.put("videos", list);
        MyFramework.SITE_ClosePopup(context, params, Framework2.ANIM_TRANSLATION_LEFT);
    }
}
