package cn.poco.login.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.login.ResetLoginPswPage;

/**
 * 修改密码成功 然后 登录成功有返回动画的site
 */
public class ResetLoginPswPageSite extends BaseSite {

    public ResetLoginPswPageSite() {
        super(SiteID.RESETPSW);
    }

    @Override
    public IPage MakePage(Context context) {
        return new ResetLoginPswPage(context, this);
    }

    public void chooseCountry(Context context) {
        HashMap<String, Object> params = new HashMap<>();
        if (m_inParams != null) {
            params.put("img", m_inParams.get("img"));
        }
        MyFramework.SITE_Popup(context, ChooseCountryAreaCodePageSite.class, params, Framework2.ANIM_NONE);
    }

    public void onBack(Context context) {
        MyFramework.SITE_Back(context, null, Framework2.ANIM_NONE);
    }

    public void onBack(Context context,HashMap<String, Object> params) {
        MyFramework.SITE_Back(context, params, Framework2.ANIM_NONE);
    }

    public void loginSucceed(Context context) {
        MyFramework.SITE_ClosePopup2(context, null, 2, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void fillRegisterInfo(Context context,HashMap<String, Object> params) {
        if (m_inParams.containsKey("img")) {
            params.put("img", m_inParams.get("img"));
        }
        MyFramework.SITE_Open(context, RegisterLoginInfoPageSite2.class, params, Framework2.ANIM_NONE);
    }
}
