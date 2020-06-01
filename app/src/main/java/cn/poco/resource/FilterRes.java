package cn.poco.resource;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by admin on 2016/8/12.
 */
public class FilterRes extends BaseRes
{
	public static final int TYPE_LOCAL_HEAD = -1;
	public static final int TYPE_LOCAL_TAIL = 1;
	public static final int MASTER_TYPE1 = 0;	//无大师无头像
	public static final int MASTER_TYPE2 = -1;	//无大师有头像
	public static final int MASTER_TYPE3 = 1;	//有大师有头像
	public int m_alpha;
	public String m_coverColor;
	public String m_coverImg;
	public String url_coverImg;
	public int m_orderType = TYPE_LOCAL_HEAD;
	public int m_order;
	public int m_masterType = MASTER_TYPE1;		//滤镜的大师类型（无大师无头像、无大师有头像、有大师有头像）
	public Object m_authorImg;

	public String m_authorName;
	public String m_authorInfo;
	public String m_filterDetail;
	public String m_filterIntroUrl;
	public String m_filterData;

	public Object m_shareImg;
	public String m_shareTitle;
	public String m_shareUrl;

	public String url_shareImg;
	public String url_authorImg;
	public boolean m_isHide = false;

	/**视频滤镜用到**/
	public String m_tablePic;//查表所需图片路径
	public int m_filterType;//(0 只有查表 1、只有混合 2、查表+混合)

	public FilterComposeInfo[] m_compose;
	public String url_tablePic;

	public FilterRes()
	{
		super(ResType.FILTER.GetValue());
	}

	@Override
	public String GetSaveParentPath()
	{
		return DownloadMgr.getInstance().FILTER_PATH;
	}

	@Override
	public void OnBuildData(DownloadTaskThread.DownloadItem item)
	{
		if(item != null && item.m_paths.length > 0)
		{
			if(item.m_onlyThumb)
			{
				if(item.m_paths.length > 0 && item.m_paths[0] != null)
				{
					m_thumb = item.m_paths[0];
				}
				if(item.m_paths.length > 1 && item.m_paths[1] != null)
				{
					m_shareImg = item.m_paths[1];
				}
				if(item.m_paths.length > 2 && item.m_paths[2] != null)
				{
					m_authorImg = item.m_paths[2];
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
					m_shareImg = item.m_paths[1];
				}
				if(item.m_paths.length > 2 && item.m_paths[2] != null)
				{
					m_authorImg = item.m_paths[2];
				}
				if(item.m_paths.length > 3 && item.m_paths[3] != null)
				{
					m_coverImg = item.m_paths[3];
				}
				if(item.m_paths.length > 4 && item.m_paths[4] != null)
				{
					m_tablePic = item.m_paths[4];
				}
				if(m_compose != null)
				{
					for(int i = 0; i < m_compose.length; i ++)
					{
						m_compose[i].blend_pic = item.m_paths[5 + i];
					}
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
	public void OnBuildPath(DownloadTaskThread.DownloadItem item)
	{
		if(item != null)
		{
			int resLen = 4;
			if(!item.m_onlyThumb)
			{
				resLen ++;
				if(m_compose != null)
				{
					resLen += m_compose.length;
				}
			}
			item.m_paths = new String[resLen];
			item.m_urls = new String[resLen];
			String name = DownloadMgr.GetImgFileName(url_thumb);
			if(name != null && !name.equals(""))
			{
				String parentPath = GetSaveParentPath();
				item.m_paths[0] = parentPath + File.separator + name;
				item.m_urls[0] = url_thumb;
			}
			name = DownloadMgr.GetImgFileName(url_shareImg);
			if(name != null && !name.equals(""))
			{
				String parentPath = GetSaveParentPath();
				item.m_paths[1] = parentPath + File.separator + name;
				item.m_urls[1] = url_shareImg;
			}
			name = DownloadMgr.GetImgFileName(url_authorImg);
			if(name != null && !name.equals(""))
			{
				String parentPath = GetSaveParentPath();
				item.m_paths[2] = parentPath + File.separator + name;
				item.m_urls[2] = url_authorImg;
			}
			name = DownloadMgr.GetImgFileName(url_coverImg);
			if(name != null && !name.equals(""))
			{
				String parentPath = GetSaveParentPath();
				item.m_paths[3] = parentPath + File.separator + name;
				item.m_urls[3] = url_coverImg;
			}
			if(!item.m_onlyThumb)
			{
				name = DownloadMgr.GetImgFileName(url_tablePic);
				if(name != null && !name.equals(""))
				{
					String parentPath = GetSaveParentPath();
					item.m_paths[4] = parentPath + File.separator + name;
					item.m_urls[4] = url_tablePic;
				}
				if(m_compose != null)
				{
					for(int i = 0; i < m_compose.length; i ++)
					{
						name = DownloadMgr.GetImgFileName(m_compose[i].url_blend_pic);
						if(name != null && !name.equals(""))
						{
							String parentPath = GetSaveParentPath();
							item.m_paths[5 + i] = parentPath + File.separator + name;
							item.m_urls[5 + i] = m_compose[i].url_blend_pic;
						}
					}
				}
			}
		}
	}

	@Override
	public void OnDownloadComplete(DownloadTaskThread.DownloadItem item, boolean isNet)
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
					SQLiteDatabase db = FilterResMgr2.getInstance().GetDB();
					if(db != null)
					{
						FilterResMgr2.getInstance().SaveResByDB(FilterResMgr2.getInstance().GetDB(), this);
						FilterResMgr2.getInstance().CloseDB();

						boolean flag = ResourceUtils.AddIds(FilterResMgr2.getInstance().GetOrderArr(), m_id);
						if(flag)
						{
							FilterResMgr2.getInstance().SaveOrderArr();
						}
					}
				}
				else
				{
					SQLiteDatabase db = FilterResMgr2.getInstance().GetDB();
					if(db != null)
					{
						FilterResMgr2.getInstance().SaveResByDB(FilterResMgr2.getInstance().GetDB(), this);
						FilterResMgr2.getInstance().CloseDB();

						boolean flag = ResourceUtils.AddIds(FilterResMgr2.getInstance().GetOrderArr(), m_id);
						if(flag)
						{
							FilterResMgr2.getInstance().SaveOrderArr();
						}
					}
				}
			}
		}
	}
}
