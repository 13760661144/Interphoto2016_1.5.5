package cn.poco.login.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.login.LoginPage;

//首页侧边栏登录进入
public class LoginPageSite extends BaseSite
{
	public LoginPageSite()
	{
		super(SiteID.LOGIN);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new LoginPage(context, this);
	}

	public void ChooseCountry(Context context)
	{
		HashMap<String, Object> params = new HashMap<>();
		if(m_inParams.containsKey("img"))
		{
			params.put("img", m_inParams.get("img"));
		}
		MyFramework.SITE_Popup(context, ChooseCountryAreaCodePageSite.class, params, Framework2.ANIM_NONE);
	}

	public void LosePsw(Context context,HashMap<String, Object> params)
	{
		if(m_inParams.containsKey("img"))
		{
			params.put("img", m_inParams.get("img"));
		}
		MyFramework.SITE_Popup(context, ResetLoginPswPageSite.class, params, Framework2.ANIM_NONE);
	}

	public void createAccount(Context context,HashMap<String, Object> params)
	{
//		MyFramework.SITE_Open(context, RegisterLoginInfoPageSite.class, params, MyFramework.ANIM_NONE);
		if(m_inParams.containsKey("img"))
		{
			params.put("img", m_inParams.get("img"));
		}
		MyFramework.SITE_Popup(context, RegisterLoginPageSite.class, params, Framework2.ANIM_NONE);
	}


	public void onBack(Context context)
	{
//		MyFramework.SITyE_BackTo(context, HomePageSite.class, null, MyFramework.ANIM_TRANSLATION_LEFT);
		MyFramework.SITE_Back(context,null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void loginSuccess(Context context)
	{
//		MyFramework.SITE_BackTo(context, HomePageSite.class, null, MyFramework.ANIM_TRANSLATION_LEFT);
		MyFramework.SITE_Back(context,null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void fillRegisterInfo(Context context,HashMap<String, Object> params) {
		if(m_inParams.containsKey("img"))
		{
			params.put("img", m_inParams.get("img"));
		}
		MyFramework.SITE_Popup(context, RegisterLoginInfoPageSite1.class, params, Framework2.ANIM_NONE);
	}
}
