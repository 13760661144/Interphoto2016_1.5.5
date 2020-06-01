package cn.poco.adMaster;

import android.text.TextUtils;

import com.adnonstop.admasterlibs.AdUtils;
import com.adnonstop.admasterlibs.data.AbsClickAdRes;

import org.json.JSONObject;

import java.io.File;

import cn.poco.resource.AbsDownloadMgr;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.DownloadTaskThread;
import cn.poco.resource.ResType;

/**
 * Created by admin on 2017/8/23.
 */

public class ClickAdRes extends AbsClickAdRes
{
	public boolean m_isShowLogo = true; //是否显示logo(interphoto)
	public int m_insertIndex; //插入的index,-1往前插入,0替换第一张,>max后面插入
	public int m_showTime = 5000;
	public ClickAdRes()
	{
		super(ResType.BUSINESS.GetValue());
	}

	@Override
	public String GetSaveParentPath()
	{
		return DownloadMgr.getInstance().BUSINESS_PATH;
	}

	@Override
	public boolean Decode(JSONObject json)
	{
		DecodeAdType(json);
		if(CanDecodeAdType(mAdType))
		{
			DecodeBaseData(json);
			if(json != null)
			{
				try{
					JSONObject json1 = json.getJSONObject("data");
					if(json1 != null){
						String temp = json1.getString("show_time");
						if(!TextUtils.isEmpty(temp))
						{
							m_showTime = Integer.parseInt(temp);
						}
						url_adm = AdUtils.JsonArrToStrArr(json1.getJSONArray("adm"));
						mClick = json1.getString("click");
						url_thumb = json1.getString("cover");
						if (json1.has("order_num")){
							temp = json1.getString("order_num");
							if(!TextUtils.isEmpty(temp)){
								m_insertIndex = Integer.parseInt(temp);
							}
						}
						if(json1.has("show_logo"))
						{
							m_isShowLogo = json1.getString("show_logo").equals("1");
						}
						return true;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public void OnBuildPath(DownloadTaskThread.DownloadItem item)
	{
		if(item != null)
		{
			int resLen = 1;
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
				item.m_paths[0] = parentPath + File.separator + name;
				item.m_urls[0] = url_thumb;
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
						item.m_paths[1 + i] = parentPath + File.separator + name;
						item.m_urls[1 + i] = url_adm[i];
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
	public boolean CanDecodeAdType(String type)
	{
		return "img-channel".equals(type) || "video-channel".equals(type);
	}

	@Override
	public void OnDownloadComplete(DownloadTaskThread.DownloadItem item, boolean isNet)
	{

	}
}
