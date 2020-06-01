package cn.poco.resource;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import cn.poco.resource.DownloadTaskThread.DownloadItem;

public class LightEffectRes extends BaseRes
{
	public static final int TYPE_SILENTDOWN = 3;
	public static final int TYPE_ACTIONDOWN_HEAD = -1;
	public static final int TYPE_ACTIONDOWN_TAIL = 1;
	public static final int TYPE_DEFAULT = 2;

	public String m_className;
	public int m_size;		//不知道什么作用
	public String m_location;
	public float   m_scale;
	public float m_minScale;
	public float m_maxScale;
	public int m_orderType = TYPE_DEFAULT;
	public int m_compose;	//混合模式
	public String m_color;

	public Object m_res;
	public String url_res;

	public int m_shareType = LockRes.SHARE_TYPE_NONE;
	public String m_lockTypeName = "";
	public String m_showContent;
	public String m_showImg;
	public String url_showImg;

	public String m_shareContent;
	public String m_shareThumb;
	public String url_shareThumb;
	public String m_shareUrl;

	public String m_headTitle;
	public String m_headLink;
	public String m_headImg;
	public String url_headImg;
	public String m_coverImg;
	public String url_coverImg;
	public boolean m_isHide = false;

	public LightEffectRes()
	{
		super(ResType.LIGHT_EFFECT.GetValue());
	}

	@Override
	public void OnBuildPath(DownloadItem item)
	{
		if(item != null)
		{
			/*
			 * thumb
			 * res[]
			 */
			int resLen = 4;
			if(item.m_onlyThumb)
			{
			}
			else
			{
				resLen = 5;
			}
			item.m_paths = new String[resLen];
			item.m_urls = new String[resLen];
			String name = DownloadMgr.GetImgFileName(url_thumb);
			String parentPath = GetSaveParentPath();
			if(name != null && !name.equals(""))
			{
				item.m_paths[0] = parentPath + File.separator + name;
				item.m_urls[0] = url_thumb;
			}

			name = DownloadMgr.GetImgFileName(url_headImg);
			parentPath = GetSaveParentPath();
			if(name != null && !name.equals(""))
			{
				item.m_paths[1] = parentPath + File.separator + name;
				item.m_urls[1] = url_headImg;
			}

			name = DownloadMgr.GetImgFileName(url_shareThumb);
			parentPath = GetSaveParentPath();
			if(name != null && !name.equals(""))
			{
				item.m_paths[2] = parentPath + File.separator + name;
				item.m_urls[2] = url_shareThumb;
			}

//			name = DownloadMgr.GetImgFileName(url_showImg);
//			parentPath = GetSaveParentPath();
//			if(name != null && !name.equals(""))
//			{
//				item.m_paths[3] = parentPath + File.separator + name;
//				item.m_urls[3] = url_showImg;
//			}

			name = DownloadMgr.GetImgFileName(url_coverImg);
			parentPath = GetSaveParentPath();
			if(name != null && !name.equals(""))
			{
				item.m_paths[3] = parentPath + File.separator + name;
				item.m_urls[3] = url_coverImg;
			}
			if(!item.m_onlyThumb)
			{
				name = DownloadMgr.GetImgFileName(url_res);
				parentPath = GetSaveParentPath();
				if(name != null && !name.equals(""))
				{
					item.m_paths[4] = parentPath + File.separator + name;
					item.m_urls[4] = url_res;
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
				if(item.m_paths.length > 1 && item.m_paths[1] != null)
				{
					m_headImg = item.m_paths[1];
				}
				if(item.m_paths.length > 2 && item.m_paths[2] != null)
				{
					m_shareThumb = item.m_paths[2];
				}
				if(item.m_paths.length > 3 && item.m_paths[3] != null)
				{
					m_coverImg = item.m_paths[3];
				}

			}
			else
			{
				if(item.m_paths.length > 0 && item.m_paths[0] != null)
				{
					m_thumb = item.m_paths[0];
				}
				if(item.m_paths.length > 1 && item.m_paths[1] != null)
				{
					m_headImg = item.m_paths[1];
				}
				if(item.m_paths.length > 2 && item.m_paths[2] != null)
				{
					m_shareThumb = item.m_paths[2];
				}
				if(item.m_paths.length > 3 && item.m_paths[3] != null)
				{
					m_coverImg = item.m_paths[3];
				}
				if(item.m_paths.length > 4 && item.m_paths[4] != null)
				{
					m_res = item.m_paths[4];
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
		return DownloadMgr.getInstance().LIGHT_EFFECT_PATH;
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
				if(isNet)
				{
					SQLiteDatabase db = LightEffectResMgr2.getInstance().GetDB();
					if(db != null)
					{
						LightEffectResMgr2.getInstance().SaveResByDB(db, this);
						LightEffectResMgr2.getInstance().CloseDB();

						boolean flag = ResourceUtils.AddIds(LightEffectResMgr2.getInstance().GetOrderArr(), m_id);
						if(flag)
						{
							LightEffectResMgr2.getInstance().SaveOrderArr();
						}
					}
				}
				else
				{
					SQLiteDatabase db = LightEffectResMgr2.getInstance().GetDB();
					if(db != null)
					{
						LightEffectResMgr2.getInstance().SaveResByDB(db, this);
						LightEffectResMgr2.getInstance().CloseDB();

						boolean flag = ResourceUtils.AddIds(LightEffectResMgr2.getInstance().GetOrderArr(), m_id);
						if(flag)
						{
							LightEffectResMgr2.getInstance().SaveOrderArr();
						}
					}
				}
			}
		}
	}
}
