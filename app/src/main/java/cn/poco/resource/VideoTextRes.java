package cn.poco.resource;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

import cn.poco.interphoto2.PocoCamera;
import cn.poco.resource.DownloadTaskThread.DownloadItem;
import cn.poco.resource.database.ResourseDatabase;
import cn.poco.tianutils.CommonUtils;
import cn.poco.utils.FileUtil;
import cn.poco.zip.Zip;

public class VideoTextRes extends BaseRes
{
	public String author;
	public String coverColor;
	public String m_coverPic;
	public String url_coverPic;
	public String m_res;
	public String url_res;
	public int lockType = LockRes.SHARE_TYPE_NONE;
	public String shareImg;
	public String url_shareImg;
	public String shareTitle;
	public String shareIntroduce;
	public String m_shareLink;
	public String m_resTypeName;
	public int m_resTypeID;
	public int editType;
	public ArrayList<FontRes> m_resArr;

	public boolean m_isHide = false;

	public VideoTextRes()
	{
		super(ResType.AUDIO_TEXT.GetValue());
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
				if(item.m_paths.length > 2 && item.m_paths[2] != null)
				{
					m_coverPic = item.m_paths[2];
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
					m_coverPic = item.m_paths[2];
				}
				if(item.m_paths.length > 3 && item.m_paths[3] != null)
				{
					if(item.m_paths[3].endsWith(".zip"))
					{
						int index = item.m_paths[3].lastIndexOf(".zip");
						String path = item.m_paths[3].substring(0, index) + "/.nomedia";
						try{
							CommonUtils.MakeFolder(path);
							Zip.UnZipFolder(item.m_paths[3], path, false);
							FileUtil.deleteSDFile(item.m_paths[3]);
						}catch(Exception e){
							e.printStackTrace();
						}
						item.m_paths[3] = path;
						m_res = item.m_paths[3];
					}
				}
				if(m_resArr != null)
				{
					int size = m_resArr.size();
					FontRes res;
					for(int i = 0; i < size; i ++)
					{
						res = m_resArr.get(i);
						if(item.m_paths[4 + i] != null)
						{
							res.m_res = item.m_paths[4 + i];
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
				resLen = 4;
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
			name = DownloadMgr.GetImgFileName(url_shareImg);
			if(name != null && !name.equals(""))
			{
				String parentPath = GetSaveParentPath();
				item.m_paths[1] = parentPath + File.separator + name;
				item.m_urls[1] = url_shareImg;
			}
			name = DownloadMgr.GetImgFileName(url_coverPic);
			if(name != null && !name.equals(""))
			{
				String parentPath = GetSaveParentPath();
				item.m_paths[2] = parentPath + File.separator + name;
				item.m_urls[2] = url_coverPic;
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
						//去掉后缀
						String temp = FileUtil.GetImgFilePathNoSuffix(path) + "/.nomeida";
						File file = new File(temp);
						if(file.exists() && file.isDirectory())
						{
							item.m_paths[3] = temp;
							item.m_urls[3] = null;
						}
						else
						{
							item.m_paths[3] = path;
							item.m_urls[3] = url_res;
						}
					}
					else
					{
						item.m_paths[3] = path;
						item.m_urls[3] = url_res;
					}
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
							item.m_paths[4 + i] = parentPath + File.separator + name;
							item.m_urls[4 + i] = res.url_res;
						}
					}
				}
			}
		}
	}

	@Override
	public String GetSaveParentPath()
	{
		return DownloadMgr.getInstance().VIDEO_TEXT_PATH;
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
					SQLiteDatabase db = VideoTextResMgr2.getInstance().GetDB();
					if(db != null)
					{
						VideoTextResMgr2.getInstance().SaveResByDB(db, this);
						VideoTextResMgr2.getInstance().CloseDB();
						boolean flag = ResourceUtils.AddIds(VideoTextResMgr2.getInstance().GetOrderArr1().get(m_resTypeID), m_id);
						if(flag)
						{
							VideoTextResMgr2.getInstance().SaveOrderArr();
						}
					}
				}
				else
				{
					SQLiteDatabase db = VideoTextResMgr2.getInstance().GetDB();
					if(db != null)
					{
						VideoTextResMgr2.getInstance().SaveResByDB(db, this);
						VideoTextResMgr2.getInstance().CloseDB();
						boolean flag = ResourceUtils.AddIds(VideoTextResMgr2.getInstance().GetOrderArr1().get(m_resTypeID), m_id);
						if(flag)
						{
							VideoTextResMgr2.getInstance().SaveOrderArr();
						}
					}
				}
			}
		}
	}
}
