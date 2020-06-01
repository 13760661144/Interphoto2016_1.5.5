package cn.poco.resource;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

import cn.poco.interphoto2.PocoCamera;
import cn.poco.resource.DownloadTaskThread.DownloadItem;
import cn.poco.resource.database.ResourseDatabase;

public class TextRes extends BaseRes
{
	public static final int TYPE_SILENTDOWN = 3;
	public static final int TYPE_ACTIONDOWN_HEAD = -1;
	public static final int TYPE_ACTIONDOWN_TAIL = 1;
	public static final int TYPE_DEFAULT = 2;

	public int m_editable;
	public String m_align;
	public float m_offsetX;
	public float m_offsetY;
	public String m_titleColor;
	public String m_resTypeName;
	public int m_resTypeID;
	public int m_order;
	public int m_orderType = TYPE_DEFAULT;

	public String m_pic;
	public String m_imageZip;

	public String url_pic;
	public String url_imageZip;

	public String m_headImg;
	public String url_headImg;
	public String m_headLink;

	public String m_coverImg;
	public String url_coverImg;

	public ArrayList<FontRes> m_resArr;
	public boolean m_isHide = false;

	public TextRes()
	{
		super(ResType.TEXT.GetValue());
	}

	@Override
	public void OnBuildData(DownloadItem item)
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
					m_headImg = item.m_paths[1];
				}
				if(item.m_paths.length > 2 && item.m_paths[2] != null)
				{
					m_coverImg = item.m_paths[2];
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
					m_headImg = item.m_paths[1];
				}
				if(item.m_paths[2] != null)
				{
					m_coverImg = item.m_paths[2];
				}
				if(item.m_paths[3] != null)
				{
					m_pic = item.m_paths[3];
				}
				if(item.m_paths[4] != null)
				{
					m_imageZip = item.m_paths[4];
				}

				if(m_resArr != null)
				{
					int size = m_resArr.size();
					FontRes res;
					for(int i = 0; i < size; i ++)
					{
						res = m_resArr.get(i);
						if(item.m_paths[5 + i] != null)
						{
							res.m_res = item.m_paths[5 + i];
						}
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
	public void OnBuildPath(DownloadItem item)
	{
		if(item != null)
		{
			int resLen = 3;
			if(item.m_onlyThumb)
			{
			}
			else
			{
				resLen = 5;
				if(m_resArr != null)
				{
					resLen += m_resArr.size();
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
			name = DownloadMgr.GetImgFileName(url_headImg);
			if(name != null && !name.equals(""))
			{
				String parentPath = GetSaveParentPath();
				item.m_paths[1] = parentPath + File.separator + name;
				item.m_urls[1] = url_headImg;
			}
			name = DownloadMgr.GetImgFileName(url_coverImg);
			if(name != null && !name.equals(""))
			{
				String parentPath = GetSaveParentPath();
				item.m_paths[2] = parentPath + File.separator + name;
				item.m_urls[2] = url_coverImg;
			}
			if(!item.m_onlyThumb)
			{
				name = DownloadMgr.GetImgFileName(url_pic);
				if(name != null && !name.equals(""))
				{
					String parentPath = GetSaveParentPath();
					item.m_paths[3] = parentPath + File.separator + name;
					item.m_urls[3] = url_pic;
				}

				name = DownloadMgr.GetImgFileName(url_imageZip);
				if(name != null && !name.equals(""))
				{
					String parentPath = GetSaveParentPath();
					item.m_paths[4] = parentPath + File.separator + name;
					item.m_urls[4] = url_imageZip;
				}

				if(m_resArr != null)
				{
					int size = m_resArr.size();
					FontRes res;
					for(int i = 0; i < size; i ++)
					{
						res = m_resArr.get(i);
						name = DownloadMgr.GetFileName(res.url_res);
						if(name != null && !name.equals(""))
						{
							String parentPath = res.GetSaveParentPath();
							item.m_paths[5 + i] = parentPath + File.separator + name;
							item.m_urls[5 + i] = res.url_res;
						}
					}
				}
			}
		}
	}

	@Override
	public String GetSaveParentPath()
	{
		return DownloadMgr.getInstance().TEXT_PATH;
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
					SQLiteDatabase db = TextResMgr2.getInstance().GetDB();
					if(db != null)
					{
						TextResMgr2.getInstance().SaveResByDB(db, this);
						TextResMgr2.getInstance().CloseDB();

						boolean flag = ResourceUtils.AddIds(TextResMgr2.getInstance().GetOrderArr1().get(m_resTypeID), m_id);
						if(flag)
						{
							TextResMgr2.getInstance().SaveOrderArr();
						}
					}
				}
				else
				{
					SQLiteDatabase db = TextResMgr2.getInstance().GetDB();
					if(db != null)
					{
						TextResMgr2.getInstance().SaveResByDB(db, this);
						TextResMgr2.getInstance().CloseDB();

						boolean flag = ResourceUtils.AddIds(TextResMgr2.getInstance().GetOrderArr1().get(m_resTypeID), m_id);
						if(flag)
						{
							TextResMgr2.getInstance().SaveOrderArr();
						}
					}
				}
			}
		}
	}
}
