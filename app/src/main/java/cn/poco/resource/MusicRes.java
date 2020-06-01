package cn.poco.resource;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import cn.poco.resource.DownloadTaskThread.DownloadItem;
import cn.poco.tianutils.CommonUtils;
import cn.poco.utils.FileUtil;
import cn.poco.zip.Zip;

public class MusicRes extends BaseRes
{
	public String author;
	public String coverColor;
	public String format;
	public String fileName;
	public int duration;
	public String m_res;
	public String url_res;
	public int lockType = LockRes.SHARE_TYPE_NONE;
	public String shareImg;
	public String url_shareImg;
	public String shareTitle;
	public String m_shareLink;
	public String m_resTypeName;
	public String m_auditionURL;

	public boolean m_isHide = false;

	public MusicRes()
	{
		super(ResType.MUSIC.GetValue());
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
					shareImg = item.m_paths[1];
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
					shareImg = item.m_paths[1];
				}
				if(item.m_paths.length > 2 && item.m_paths[2] != null)
				{
					if(item.m_paths[2].endsWith(".zip"))
					{
						int index = item.m_paths[2].lastIndexOf(".zip");
						String path = item.m_paths[2].substring(0, index) + "/.nomedia";
						try{
							CommonUtils.MakeFolder(path);
							Zip.UnZipFolder(item.m_paths[2], path, false);
							FileUtil.deleteSDFile(item.m_paths[2]);
						}catch(Exception e){
							e.printStackTrace();
						}
						item.m_paths[2] = path;
						m_res = item.m_paths[2];
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
			if(!item.m_onlyThumb)
			{
				name = DownloadMgr.GetFileName(url_res);
				if(name != null && !name.equals(""))
				{
					String parentPath = GetSaveParentPath();
					String path = parentPath + File.separator + name;
					//zip包会解压成文件夹，为了不重复下载，如果与zip包同名文件夹存在，我这边就认为素材是下载好了的；
					if(path.endsWith(".zip"))
					{
						String temp = FileUtil.GetImgFilePathNoSuffix(path) + "/.nomeida";
						File file = new File(temp);
						if(file.exists() && file.isDirectory())
						{
							item.m_paths[2] = temp;
							item.m_urls[2] = null;
						}
						else
						{
							item.m_paths[2] = path;
							item.m_urls[2] = url_res;
						}
					}
					else
					{
						item.m_paths[2] = path;
						item.m_urls[2] = url_res;
					}
				}
			}
		}
	}

	@Override
	public String GetSaveParentPath()
	{
		return DownloadMgr.getInstance().MUSIC_PATH;
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
					SQLiteDatabase db = MusicResMgr2.getInstance().GetDB();
					if(db != null)
					{
						MusicResMgr2.getInstance().SaveResByDB(db, this);
						MusicResMgr2.getInstance().CloseDB();

						boolean flag = ResourceUtils.AddIds(MusicResMgr2.getInstance().GetOrderArr(), m_id);
						if(flag)
						{
							MusicResMgr2.getInstance().SaveOrderArr();
						}
					}
				}
				else
				{
					SQLiteDatabase db = MusicResMgr2.getInstance().GetDB();
					if(db != null)
					{
						MusicResMgr2.getInstance().SaveResByDB(db, this);
						MusicResMgr2.getInstance().CloseDB();

						boolean flag = ResourceUtils.AddIds(MusicResMgr2.getInstance().GetOrderArr(), m_id);
						if(flag)
						{
							MusicResMgr2.getInstance().SaveOrderArr();
						}
					}
				}
			}
		}
	}
}
