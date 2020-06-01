package cn.poco.login.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.login.area.ChooseCountryAreaCodePage;

/**
 * 登录，注册和忘记密码验证进入
 */
public class ChooseCountryAreaCodePageSite extends BaseSite{

    public ChooseCountryAreaCodePageSite()
    {
        super(SiteID.CHOOSE_COUNTRY);
    }

    @Override
    public IPage MakePage(Context context) {
        return new ChooseCountryAreaCodePage(context, this);
    }

    public void onSel(Context context,HashMap<String,Object> datas)
    {
        MyFramework.SITE_ClosePopup(context, datas, Framework2.ANIM_NONE);
    }

    public void backToLastPage(Context context)
    {
        MyFramework.SITE_ClosePopup(context, null, Framework2.ANIM_NONE);
    }

    public void onBack(Context context)
    {
        MyFramework.SITE_ClosePopup(context, null, Framework2.ANIM_NONE);
    }

}
