package cn.poco.video.site;

import android.content.Context;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.video.page.VideoEditHelpAnim;

/**
 * Created by Shine on 2017/5/27.
 */

public class VideoMosaicHelpSite extends BaseSite {

    public VideoMosaicHelpSite() {
        super(SiteID.VIDEO_MOSAIC_HELP);
    }

    @Override
    public IPage MakePage(Context context) {
        return new VideoEditHelpAnim(context, this);
    }

    public void onBack(Context context) {
        MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_BOTTOM);
    }
}
