package cn.poco.video.site;

import android.content.Context;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.video.videotext.VideoTextHelpPage;

/**
 * Created by lgd on 2018/1/18.
 */

public class VideoTextHelpSite extends BaseSite
{

    public VideoTextHelpSite()
    {
        super(SiteID.VIDEO_TEXT_HELP);
    }

    @Override
    public IPage MakePage(Context context)
    {
        return new VideoTextHelpPage(context,this);
    }
    public void onBack(Context context)
    {
        MyFramework.SITE_Back(context,null, Framework2.ANIM_TRANSLATION_BOTTOM);
    }
}
