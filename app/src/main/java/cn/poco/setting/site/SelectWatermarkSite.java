package cn.poco.setting.site;

import android.content.Context;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.setting.SelectWatermarkPage;

/**
 * Created by admin on 2017/6/13.
 */

public class SelectWatermarkSite extends BaseSite {


    public SelectWatermarkSite() {
        super(SiteID.SELECTWATERMARK);
    }


    @Override
    public IPage MakePage(Context context) {

        return new SelectWatermarkPage(context, this);
    }

    public void onBack(Context context) {
        MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);

    }
}