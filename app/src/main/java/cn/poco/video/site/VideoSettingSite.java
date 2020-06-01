package cn.poco.video.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.video.videoAlbum.VideoSettingPage;

/**
 * Created by lgd on 2018/1/8.
 */

public class VideoSettingSite extends BaseSite
{
    public VideoSettingSite()
    {
        super(SiteID.VIDEO_SETTING);
    }

    @Override
    public IPage MakePage(Context context)
    {
        return new VideoSettingPage(context,this);
    }

    public void onBack(Context context,HashMap<String,Object> parmas)
    {
        MyFramework.SITE_Back(context,parmas,Framework2.ANIM_TRANSLATION_LEFT);
    }
    public void onVideoBeautify(Context context, HashMap<String,Object> parmas)
    {
        MyFramework.SITE_Open(context, VideoBeautifySite.class, parmas, Framework2.ANIM_TRANSLATION_LEFT);
    }
}
