package cn.poco.bootimg.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.banner.BannerCore3;
import cn.poco.bootimg.BootImgPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.home.site.HomePageSite;
import cn.poco.tianutils.CommonUtils;
import cn.poco.utils.MyNetCore;
import cn.poco.webview.site.WebViewPageSite4;

public class BootImgPageSite extends BaseSite
{
	public HomePageSite.CmdProc m_cmdProc;
	public BootImgPageSite()
	{
		super(SiteID.BOOT_IMG);

		MakeCmdProc();
	}

	/**
	 * 注意构造函数调用
	 */
	protected void MakeCmdProc()
	{
		m_cmdProc = new HomePageSite.CmdProc();
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new BootImgPage(context, this);
	}

	public void OnHome(boolean anim,Context context)
	{
		/*if(SysConfig.IsFirstRun())
		{
			MyFramework.SITE_Open(context, true, IntroPage2Site.class, null, Framework.ANIM_NONE);
		}
		else*/
		{
			if(anim)
			{
				MyFramework.SITE_Open(context, true, HomePageSite.class, null, Framework2.ANIM_TRANSITION);
			}
			else
			{
				MyFramework.SITE_Open(context, true, HomePageSite.class, null, Framework2.ANIM_NONE);
			}
		}
	}

	public void OnMyWeb(String url,Context context)
	{
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("url", url);
		MyFramework.SITE_Popup(context, WebViewPageSite4.class, params, Framework2.ANIM_NONE);
	}

	public void OnSystemWeb(Context context, String url)
	{
		CommonUtils.OpenBrowser(context, MyNetCore.GetPocoUrl(context, url));
	}

	public void OnImg(String url,Context context)
	{
		BannerCore3.ExecuteCommand(context, url, m_cmdProc);
	}
}
