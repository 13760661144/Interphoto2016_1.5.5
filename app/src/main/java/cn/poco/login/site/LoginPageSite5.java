package cn.poco.login.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

//首页-月赛-登录
public class LoginPageSite5 extends LoginPageSite {
    //	@Override
//	public void onBack()
//	{
//		MyFramework.SITE_BackTo(PocoCamera.main, WebViewPageSite3.class, null, MyFramework.ANIM_TRANSLATION_LEFT);
//	}
//
//	@Override
//	public void loginSuccess()
//	{
//		HashMap<String, Object> params = new HashMap<>();
//		params.put("url", m_inParams.get("url"));
//		MyFramework.SITE_BackTo(PocoCamera.main, WebViewPageSite3.class, params, MyFramework.ANIM_TRANSLATION_LEFT);
//	}
    @Override
    public void loginSuccess(Context context) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("url", m_inParams.get("url"));
        MyFramework.SITE_ClosePopup(context, params, Framework2.ANIM_TRANSLATION_LEFT);
    }
}
