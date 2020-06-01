package cn.poco.resource;

import java.io.File;
import java.util.ArrayList;

import cn.poco.resource.DownloadTaskThread.DownloadItem;

public class MyLogoRes extends BaseRes
{
	public static int USER_NONE_ID = -1;
	public int m_userId = USER_NONE_ID;
	public Object m_res;

	public String url_res;
	public boolean m_editable = false;
	public ArrayList<FontRes> m_resArr;

	public String mSaveTime;
	public int mUniqueObjectId = -1;
	public boolean mShouldDelete = false;
	public boolean mShouldModify = false;

	public float m_scale = 1;

	public MyLogoRes()
	{
		super(ResType.MY_LOGO.GetValue());
	}

	@Override
	public void OnBuildPath(DownloadItem item)
	{
		if(item != null)
		{
			/*
			 * thumb
			 * res
			 */
			int resLen = 1;
			if(item.m_onlyThumb)
			{
			}
			else
			{
				resLen = 2;
			}
			item.m_paths = new String[resLen];
			item.m_urls = new String[resLen];
			String name = DownloadMgr.GetImgFileName(url_thumb);
			String parentPath = GetSaveParentPath();
			if(name != null && name.length() > 0)
			{
				item.m_paths[0] = parentPath + File.separator + name;
				item.m_urls[0] = url_thumb;
			}
			if(!item.m_onlyThumb)
			{
				name = DownloadMgr.GetImgFileName(url_res);
				if(name != null && name.length() > 0)
				{
					item.m_paths[1] = parentPath + File.separator + name;
					item.m_urls[1] = url_res;
				}
			}
		}
	}

	@Override
	public void OnBuildData(DownloadItem item)
	{
		if(item != null && item.m_urls.length > 0)
		{
			if(item.m_onlyThumb)
			{
				if(item.m_paths.length > 0 && item.m_paths[0] != null)
				{
					m_thumb = item.m_paths[0];
				}
			}
			else
			{
				if(item.m_paths[0] != null)
				{
					m_thumb = item.m_paths[0];
				}
				if(item.m_paths[1] != null)
				{
					m_res = item.m_paths[1];
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
		return DownloadMgr.getInstance().MY_LOGO_PATH;
	}

	protected boolean MyEquals(String str1, String str2)
	{
		boolean out = false;

		if((str1 == null && str2 == null) || (str1 != null && str2 != null && str1.equals(str2)))
		{
			out = true;
		}

		return out;
	}

	protected int MyHasItem(ArrayList<MyLogoRes> arr, MyLogoRes res)
	{
		int out = -1;

		if(arr != null && res != null)
		{
			int len = arr.size();
			MyLogoRes temp;
			for(int i = 0; i < len; i++)
			{
				temp = arr.get(i);
				if(MyEquals(temp.m_name, res.m_name) && MyEquals((String)temp.m_res, (String)res.m_res))
				{
					out = i;
					break;
				}
			}
		}

		return out;
	}

	@Override
	public void OnDownloadComplete(DownloadItem item, boolean isNet)
	{
		if(item == null || !item.m_onlyThumb)
		{
			if(MyLogoResMgr.m_sdcardArr != null)
			{
				//BaseResMgr.DeleteItem(MyLogoResMgr.m_sdcardArr, m_id);
				if(MyHasItem(MyLogoResMgr.m_sdcardArr, this) < 0)
				{
					MyLogoResMgr.m_sdcardArr.add(0, this);
					MyLogoResMgr.getInstance().WriteSDCardResArr(MyLogoResMgr.m_sdcardArr);
				}
			}
		}
	}
}
