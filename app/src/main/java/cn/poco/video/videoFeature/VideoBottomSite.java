package cn.poco.video.videoFeature;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.video.site.VideoAlbumSite3;

/**
 * Created by Simon Meng on 2018/1/4.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoBottomSite extends BaseSite{

    public VideoBottomSite() {
        super(-1);
    }

    @Override
    public IPage MakePage(Context context) {
        return null;
    }

    public void onBack() {

    }

    public void onShareClick() {

    }


    public void onVideoAlbum(Context context, HashMap<String, Object> data)
    {
        MyFramework.SITE_Popup(context,VideoAlbumSite3.class,data, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void onBackToAlbum(Context context)
    {

    }

}
