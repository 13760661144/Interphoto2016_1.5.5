package cn.poco.resource;

import java.io.File;

import cn.poco.resource.DownloadTaskThread.DownloadItem;

public class FontRes extends BaseRes
{
	public float m_size;
	public Object m_res;

	public String url_res;

	public FontRes()
	{
		super(ResType.FONT.GetValue());
	}

	@Override
	public void OnBuildPath(DownloadItem item)
	{
		if(item != null)
		{
			/*
			 * res
			 */
			int resLen = 0;
			if(item.m_onlyThumb)
			{
			}
			else
			{
				resLen = 1;
			}
			item.m_paths = new String[resLen];
			item.m_urls = new String[resLen];
			String name;
			String parentPath = GetSaveParentPath();
			if(!item.m_onlyThumb)
			{
				name = DownloadMgr.GetFileName(url_res);
				if(name != null && !name.equals(""))
				{
					item.m_paths[0] = parentPath + File.separator + name;
					item.m_urls[0] = url_res;
				}
			}
		}
	}

	@Override
	public void OnBuildData(DownloadItem item)
	{
		if(item != null && item.m_urls.length > 0)
		{
			if(!item.m_onlyThumb)
			{
				if(item.m_paths[0] != null)
				{
					m_res = item.m_paths[0];
				}

				//放最后避免同步问题
				if(m_type == BaseRes.TYPE_NETWORK_URL)
				{
					m_type = BaseRes.TYPE_LOCAL_PATH;
				}
			}
		}
	}

	@Override
	public String GetSaveParentPath()
	{
		return DownloadMgr.getInstance().FONT_PATH;
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
