package cn.poco.resource;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

import cn.poco.framework.MyFramework2App;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.resource.DownloadTaskThread.DownloadItem;
import cn.poco.resource.database.ResourseDatabase;

public class ThemeRes extends BaseRes
{
	public String m_detail; //主题介绍
	public String m_subTitle;	//主题副标题
	public String m_version;		//主题版本
	public String m_icon;
	public String url_icon;
	public int m_shareType = LockRes.SHARE_TYPE_NONE;
	public String m_shareTitle;
	public String m_dashiType;	//类型（光效/滤镜/水印/态度）
	public String m_dashiName;	//大师名字
	public String m_dashiRank;	//大师头衔
	public String m_dashiIcon;	//大师头像
	public String url_dashiIcon;
	public String m_shareUrl;
	public String m_titleColor;

	public int m_tjShowId; //主题显示统计id
	public String m_tjLink; //商业统计链接
	public int m_order; //主题排序，可能不连续，按照数值的大小从小到大排列
	public boolean m_isHide; //是否显示在推荐位
	public boolean m_isBusiness; //是否商业

	public int[] m_filterIDArr;
	public int[] m_textIDArr;
	public int[] m_lightEffectIDArr;
	public int[] m_musicIDArr;
	public int[] m_watermarkIDArr;

	public int m_recommend;

	public ThemeRes()
	{
		super(ResType.THEME.GetValue());
	}

	@Override
	public void OnBuildPath(DownloadItem item)
	{
		if(item != null)
		{
			/*
			 * thumb
			 * pic
			 */
			int resLen = 2;
			if(item.m_onlyThumb)
			{
			}
			else
			{
				resLen = 3;
			}
			item.m_paths = new String[resLen];
			item.m_urls = new String[resLen];
			String name = DownloadMgr.GetImgFileName(url_icon);
			String parentPath = GetSaveParentPath();
			if(name != null && !name.equals(""))
			{
				item.m_paths[0] = parentPath + File.separator + name;
				item.m_urls[0] = url_icon;
			}
			name = DownloadMgr.GetImgFileName(url_thumb);
			if(name != null && !name.equals(""))
			{
				item.m_paths[1] = parentPath + File.separator + name;
				item.m_urls[1] = url_thumb;
			}
			if(!item.m_onlyThumb)
			{
				name = DownloadMgr.GetImgFileName(url_dashiIcon);
				if(name != null && !name.equals(""))
				{
					item.m_paths[2] = parentPath + File.separator + name;
					item.m_urls[2] = url_dashiIcon;
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
					m_icon = item.m_paths[0];
				}
				if(item.m_paths.length > 1 && item.m_paths[1] != null)
				{
					m_thumb = item.m_paths[1];
				}
			}
			else
			{
				if(item.m_paths[0] != null)
				{
					m_icon = item.m_paths[0];
				}
				if(item.m_paths[1] != null)
				{
					m_thumb = item.m_paths[1];
				}
				if(item.m_paths[2] != null)
				{
					m_dashiIcon = item.m_paths[2];
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
		return DownloadMgr.getInstance().THEME_PATH;
	}

	@Override
	public void OnDownloadComplete(DownloadItem item, boolean isNet)
	{
		if(item.m_onlyThumb)
		{
		}
		else
		{
			synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
			{
				SQLiteDatabase db = ThemeResMgr2.getInstance().GetDB();
				if(db != null)
				{
					ThemeResMgr2.getInstance().SaveResByDB(db, this);
					ThemeResMgr2.getInstance().CloseDB();
				}

			}

		}
	}

}
