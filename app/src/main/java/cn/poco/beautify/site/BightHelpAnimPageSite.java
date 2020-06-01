package cn.poco.beautify.site;

import android.content.Context;

import cn.poco.beautify.animations.BightHelpAnimPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * Created by pengdh on 2016/7/13.
 */
public class BightHelpAnimPageSite extends BaseSite {

    public BightHelpAnimPageSite()
    {
        super(SiteID.BIGHT_HELP_ANIM);
    }
    @Override
    public IPage MakePage(Context context) {
        return new BightHelpAnimPage(context,this);
    }

    public void onBack(Context context)
    {
        MyFramework.SITE_ClosePopup(context,null, Framework2.ANIM_TRANSLATION_BOTTOM);
    }
}
