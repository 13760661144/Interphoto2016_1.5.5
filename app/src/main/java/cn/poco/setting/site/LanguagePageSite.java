package cn.poco.setting.site;

import android.content.Context;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.setting.LanguagePage;

/**
 * Created by pengdh on 2016/9/9.
 */
public class LanguagePageSite extends BaseSite {

    public LanguagePageSite()
    {
        super(SiteID.SETTING);
    }

    @Override
    public IPage MakePage(Context context) {
        return new LanguagePage(context,this);
    }

    public void onBack(Context context)
    {
        MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
    }
}
