package cn.poco.adMaster;

import android.text.TextUtils;

import com.adnonstop.admasterlibs.data.AbsBootAdRes;

import java.io.File;

import cn.poco.resource.AbsDownloadMgr;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.DownloadTaskThread;
import cn.poco.resource.ResType;

/**
 * Created by admin on 2017/8/23.
 */

public class BootImgRes extends AbsBootAdRes
{
	public BootImgRes()
	{
		super(ResType.BOOT_IMG.GetValue());
	}

	@Override
	public String GetSaveParentPath()
	{
		return DownloadMgr.getInstance().BUSINESS_PATH;
	}

	@Override
	public void OnBuildData(DownloadTaskThread.DownloadItem item)
	{
		super.OnBuildData(item);
	}

	@Override
	public void OnBuildPath(DownloadTaskThread.DownloadItem item)
	{
		if(item != null)
		{
			int resLen = 2;
			if(url_adm != null)
			{
				resLen += url_adm.length;
			}
			item.m_paths = new String[resLen];
			item.m_urls = new String[resLen];
			String name = AbsDownloadMgr.GetImgFileName(url_thumb);
			if(name != null && !name.equals(""))
			{
				String parentPath = GetSaveParentPath();
				name = changeName(name, url_thumb);
				item.m_paths[0] = parentPath + File.separator + name;
				item.m_urls[0] = url_thumb;
			}
			name = AbsDownloadMgr.GetImgFileName(url_replayBtn);
			if(name != null && !name.equals(""))
			{
				String parentPath = GetSaveParentPath();
				item.m_paths[1] = parentPath + File.separator + name;
				item.m_urls[1] = url_replayBtn;
			}
			if(url_adm != null)
			{
				int size = url_adm.length;
				for(int i = 0; i < size; i ++)
				{
					name = AbsDownloadMgr.GetImgFileName(url_adm[i]);
					if(name != null && !name.equals(""))
					{
						String parentPath = GetSaveParentPath();
						name = changeName(name, url_adm[i]);
						item.m_paths[2 + i] = parentPath + File.separator + name;
						item.m_urls[2 + i] = url_adm[i];
					}
				}
			}

		}
	}

	private String changeName(String name, String orgPath)
	{
		String replaceStr = null;
		if(orgPath.endsWith(".gif") || orgPath.endsWith(".GIF"))
		{
			replaceStr = ".gif";
		}
		else if(orgPath.endsWith(".mp4"))
		{
			replaceStr = ".mp4";
		}
		if(!TextUtils.isEmpty(replaceStr))
		{
			int index = name.lastIndexOf(".img");
			name = "." + name.substring(0, index) + replaceStr;
		}
		return name;
	}

	@Override
	public void OnDownloadComplete(DownloadTaskThread.DownloadItem item, boolean isNet)
	{

	}
}
