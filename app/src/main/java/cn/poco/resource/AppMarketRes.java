package cn.poco.resource;

import cn.poco.resource.DownloadTaskThread.DownloadItem;

public class AppMarketRes extends BaseRes
{
	public int m_classID;
	public String m_className;
	public String m_downloadUrl;
	public String m_info;

	public AppMarketRes()
	{
		super(ResType.APP.GetValue());
	}

	@Override
	public String GetSaveParentPath()
	{
		return DownloadMgr.getInstance().APP_MARKET_PATH;
	}

	@Override
	public void OnDownloadComplete(DownloadItem item, boolean isNet)
	{
		if(item.m_onlyThumb)
		{
		}
		else
		{
		}
	}
}
