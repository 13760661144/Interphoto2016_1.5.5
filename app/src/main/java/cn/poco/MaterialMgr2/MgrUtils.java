package cn.poco.MaterialMgr2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import cn.poco.interphoto2.PocoCamera;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.FilterResMgr2;
import cn.poco.resource.IDownload;
import cn.poco.resource.LightEffectResMgr2;
import cn.poco.resource.LockRes;
import cn.poco.resource.MusicResMgr2;
import cn.poco.resource.ResType;
import cn.poco.resource.ResourceMgr;
import cn.poco.resource.TextResMgr2;
import cn.poco.resource.ThemeRes;
import cn.poco.resource.ThemeResMgr2;
import cn.poco.resource.VideoTextResMgr2;
import cn.poco.resource.database.ResourseDatabase;
import cn.poco.setting.LanguagePage;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;

public class MgrUtils
{
	private static int m_prepareCount = 0; //没有下载的个数
	private static int m_downloadingCount = 0;
	private static int m_completeCount = 0;

	private static final String TAG = "MgrUtils";
	public static ArrayList<GroupInfo> ClassifyThemeInfos(Context context, ResType type, String textType)
	{
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{
			String ver = SysConfig.GetAppVer(context);
			boolean showLock = true;
			if(ver.endsWith("_r1") && !TagMgr.CheckTag(context, LanguagePage.ENGLISH_TAGVALUE))
			{
				showLock = false;
			}

			ArrayList<GroupInfo> out = new ArrayList<>();
			SQLiteDatabase db = ResourseDatabase.getInstance(context).openDatabase();
			ArrayList<ThemeRes> themeResArr = ThemeResMgr2.getInstance().GetAllThemeResArr(db);

			GroupInfo textInfo = new GroupInfo();
			GroupInfo filterInfo = new GroupInfo();
			GroupInfo effectInfo = new GroupInfo();
			GroupInfo watermarkInfo = new GroupInfo();
			ArrayList<ThemeItemInfo> textResArr = new ArrayList<>();
			ArrayList<ThemeItemInfo> filterResArr = new ArrayList<>();
			ArrayList<ThemeItemInfo> effectResArr = new ArrayList<>();
			ArrayList<ThemeItemInfo> watermarkResArr = new ArrayList<>();
			textInfo.m_type = ResType.TEXT;
			textInfo.m_resArr = textResArr;
			filterInfo.m_type = ResType.FILTER;
			filterInfo.m_resArr = filterResArr;
			effectInfo.m_type = ResType.LIGHT_EFFECT;
			effectInfo.m_resArr = effectResArr;
			watermarkInfo.m_type  = ResType.AUDIO_TEXT;
			watermarkInfo.m_resArr = watermarkResArr;

			boolean hasText = true;
			boolean hasFilter = true;
			boolean hasEffect = true;
			if(type == null){
				out.add(filterInfo);
				out.add(textInfo);
				out.add(effectInfo);
			}
			else if(type == ResType.TEXT || type == ResType.AUDIO_TEXT){
				out.add(textInfo);
				hasEffect = hasFilter = false;
			}
			else if(type == ResType.FILTER){
				out.add(filterInfo);
				hasText = hasEffect = false;
			}
			else if(type == ResType.LIGHT_EFFECT){
				out.add(effectInfo);
				hasFilter = hasText = false;
			}
			ThemeItemInfo itemInfo = null;
			if(themeResArr != null && themeResArr.size() > 0)
			{
				for(ThemeRes themeRes : themeResArr)
				{
					Log.i(TAG, "ClassifyThemeInfos: "+ themeRes.m_id);
					if(hasFilter && themeRes.m_filterIDArr != null && themeRes.m_filterIDArr.length > 0)
					{
						itemInfo = new ThemeItemInfo();
						itemInfo.m_idArr = themeRes.m_filterIDArr;
						itemInfo.m_uri = themeRes.m_id;
						itemInfo.m_resArr = new ArrayList<>();
						itemInfo.m_resArr.addAll(FilterResMgr2.getInstance().GetResArr(db, itemInfo.m_idArr));
						itemInfo.m_state = checkGroupDownloadState(itemInfo.m_resArr, itemInfo.m_idArr, null);
						itemInfo.m_progress = 100 * m_completeCount / itemInfo.m_idArr.length;
						itemInfo.m_themeRes = themeRes;
						itemInfo.m_type = ResType.FILTER;
						itemInfo.m_key = "filter";
						filterResArr.add(itemInfo);
						if(itemInfo != null)
						{
							if(itemInfo.m_state != ThemeItemInfo.COMPLETE
									&& themeRes.m_shareType != LockRes.SHARE_TYPE_NONE
									&& TagMgr.CheckTag(context, Tags.THEME_UNLOCK + itemInfo.m_uri) && showLock)
							{
								itemInfo.m_lock = true;
							}
						}
					}
					if(hasText && (themeRes.m_textIDArr != null && themeRes.m_textIDArr.length > 0))
					{
						if(!TextUtils.isEmpty(textType))
						{
							if(themeRes.m_dashiType.equals(textType)){
								itemInfo = new ThemeItemInfo();
								itemInfo.m_idArr = themeRes.m_textIDArr;
								itemInfo.m_uri = themeRes.m_id;
								itemInfo.m_resArr = new ArrayList<>();
								itemInfo.m_resArr.addAll(TextResMgr2.getInstance().GetResArr(db, itemInfo.m_idArr));
								itemInfo.m_state = checkGroupDownloadState(itemInfo.m_resArr, itemInfo.m_idArr, null);
								itemInfo.m_progress = 100 * m_completeCount / itemInfo.m_idArr.length;
								itemInfo.m_themeRes = themeRes;
								itemInfo.m_type = ResType.TEXT;
								itemInfo.m_key = "text";
								textResArr.add(itemInfo);
							}
						}
						else{
							itemInfo = new ThemeItemInfo();
							itemInfo.m_idArr = themeRes.m_textIDArr;
							itemInfo.m_uri = themeRes.m_id;
							itemInfo.m_resArr = new ArrayList<>();
							itemInfo.m_resArr.addAll(TextResMgr2.getInstance().GetResArr(db, itemInfo.m_idArr));
							itemInfo.m_state = checkGroupDownloadState(itemInfo.m_resArr, itemInfo.m_idArr, null);
							itemInfo.m_progress = 100 * m_completeCount / itemInfo.m_idArr.length;
							itemInfo.m_themeRes = themeRes;
							itemInfo.m_type = ResType.TEXT;
							itemInfo.m_key = "text";
							textResArr.add(itemInfo);
						}
						if(itemInfo != null)
						{
							if(itemInfo.m_state != ThemeItemInfo.COMPLETE
									&& themeRes.m_shareType != LockRes.SHARE_TYPE_NONE
									&& TagMgr.CheckTag(context, Tags.THEME_UNLOCK + itemInfo.m_uri) && showLock)
							{
								itemInfo.m_lock = true;
							}
						}
					}

					if(hasText && (themeRes.m_watermarkIDArr != null && themeRes.m_watermarkIDArr.length > 0))
					{
						if(!TextUtils.isEmpty(textType))
						{
							if(themeRes.m_dashiType.equals(textType)){
								itemInfo = new ThemeItemInfo();
								itemInfo.m_idArr = themeRes.m_watermarkIDArr;
								itemInfo.m_uri = themeRes.m_id;
								itemInfo.m_resArr = new ArrayList<>();
								itemInfo.m_resArr.addAll(VideoTextResMgr2.getInstance().GetResArr(db, itemInfo.m_idArr));
								itemInfo.m_state = checkGroupDownloadState(itemInfo.m_resArr, itemInfo.m_idArr, null);
								itemInfo.m_progress = 100 * m_completeCount / itemInfo.m_idArr.length;
								itemInfo.m_themeRes = themeRes;
								itemInfo.m_type = ResType.AUDIO_TEXT;
								itemInfo.m_key = "videoText";
								textResArr.add(itemInfo);
							}
						}
						else{
							itemInfo = new ThemeItemInfo();
							itemInfo.m_idArr = themeRes.m_watermarkIDArr;
							itemInfo.m_uri = themeRes.m_id;
							itemInfo.m_resArr = new ArrayList<>();
							itemInfo.m_resArr.addAll(VideoTextResMgr2.getInstance().GetResArr(db, itemInfo.m_idArr));
							itemInfo.m_state = checkGroupDownloadState(itemInfo.m_resArr, itemInfo.m_idArr, null);
							itemInfo.m_progress = 100 * m_completeCount / itemInfo.m_idArr.length;
							itemInfo.m_themeRes = themeRes;
							itemInfo.m_type = ResType.AUDIO_TEXT;
							itemInfo.m_key = "videoText";
							textResArr.add(itemInfo);
						}
						if(itemInfo != null)
						{
							if(itemInfo.m_state != ThemeItemInfo.COMPLETE
									&& themeRes.m_shareType != LockRes.SHARE_TYPE_NONE
									&& TagMgr.CheckTag(context, Tags.THEME_UNLOCK + itemInfo.m_uri) && showLock)
							{
								itemInfo.m_lock = true;
							}
						}
					}
					if(hasEffect && themeRes.m_lightEffectIDArr != null && themeRes.m_lightEffectIDArr.length > 0)
					{
						itemInfo = new ThemeItemInfo();
						itemInfo.m_idArr = themeRes.m_lightEffectIDArr;
						itemInfo.m_uri = themeRes.m_id;
						itemInfo.m_resArr = new ArrayList<>();
						itemInfo.m_resArr.addAll(LightEffectResMgr2.getInstance().GetResArr(db, itemInfo.m_idArr));
						itemInfo.m_state = checkGroupDownloadState(itemInfo.m_resArr, itemInfo.m_idArr, null);
						itemInfo.m_progress = 100 * m_completeCount / itemInfo.m_idArr.length;
						itemInfo.m_themeRes = themeRes;
						itemInfo.m_type = ResType.LIGHT_EFFECT;
						itemInfo.m_key = "effect";
						effectResArr.add(itemInfo);
						if(itemInfo != null)
						{
							if(itemInfo.m_state != ThemeItemInfo.COMPLETE
									&& themeRes.m_shareType != LockRes.SHARE_TYPE_NONE
									&& TagMgr.CheckTag(context, Tags.THEME_UNLOCK + itemInfo.m_uri) && showLock)
							{
								itemInfo.m_lock = true;
							}
						}
					}
				}
			}
			ResourseDatabase.getInstance(context).closeDatabase();
			return out;
		}
	}

	public static ThemeItemInfo GetThemeItemInfoByUri(Context context, int themeId, ResType type)
	{
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{
			String ver = SysConfig.GetAppVer(context);
			boolean showLock = true;
			if(ver.endsWith("_r1") && !TagMgr.CheckTag(context, LanguagePage.ENGLISH_TAGVALUE))
			{
				showLock = false;
			}

			ThemeItemInfo out = null;
			if(themeId != -1)
			{
				SQLiteDatabase db = ResourseDatabase.getInstance(context).openDatabase();
				ArrayList<ThemeRes> themeResArr = ThemeResMgr2.getInstance().GetAllThemeResArr(db);
				if(themeResArr != null)
				{
					for(ThemeRes themeRes : themeResArr)
					{
						if(themeId == themeRes.m_id)
						{
							switch(type)
							{
								case FILTER:
								{
									out = new ThemeItemInfo();
									out.m_idArr = themeRes.m_filterIDArr;
									out.m_uri = themeRes.m_id;
									out.m_resArr = new ArrayList<>();
									out.m_resArr.addAll(FilterResMgr2.getInstance().GetResArr(db, out.m_idArr));
									out.m_state = checkGroupDownloadState(out.m_resArr, out.m_idArr, null);
									out.m_progress = 100 * m_completeCount / out.m_idArr.length;
									out.m_themeRes = themeRes;
									out.m_type = ResType.FILTER;
									out.m_key = "filter";
									break;
								}
								case TEXT:
								{
									out = new ThemeItemInfo();
									out.m_idArr = themeRes.m_textIDArr;
									out.m_uri = themeRes.m_id;
									out.m_resArr = new ArrayList<>();
									out.m_resArr.addAll(TextResMgr2.getInstance().GetResArr(db, out.m_idArr));
									out.m_state = checkGroupDownloadState(out.m_resArr, out.m_idArr, null);
									out.m_progress = 100 * m_completeCount / out.m_idArr.length;
									out.m_themeRes = themeRes;
									out.m_type = ResType.TEXT;
									out.m_key = "text";
									break;
								}
								case LIGHT_EFFECT:
								{
									out = new ThemeItemInfo();
									out.m_idArr = themeRes.m_lightEffectIDArr;
									out.m_uri = themeRes.m_id;
									out.m_resArr = new ArrayList<>();
									out.m_resArr.addAll(LightEffectResMgr2.getInstance().GetResArr(db, out.m_idArr));
									out.m_state = checkGroupDownloadState(out.m_resArr, out.m_idArr, null);
									out.m_progress = 100 * m_completeCount / out.m_idArr.length;
									out.m_themeRes = themeRes;
									out.m_type = ResType.LIGHT_EFFECT;
									out.m_key = "effect";
									break;
								}
								case AUDIO_TEXT:
								{
									out = new ThemeItemInfo();
									out.m_idArr = themeRes.m_watermarkIDArr;
									out.m_uri = themeRes.m_id;
									out.m_resArr = new ArrayList<>();
									out.m_resArr.addAll(VideoTextResMgr2.getInstance().GetResArr(db, out.m_idArr));
									out.m_state = checkGroupDownloadState(out.m_resArr, out.m_idArr, null);
									out.m_progress = 100 * m_completeCount / out.m_idArr.length;
									out.m_themeRes = themeRes;
									out.m_type = ResType.AUDIO_TEXT;
									out.m_key = "video_text";
									break;
								}
							}
							break;
						}
					}
				}
				ResourseDatabase.getInstance(context).closeDatabase();
			}

			if(out != null)
			{
				if(out.m_state != ThemeItemInfo.COMPLETE
						&& out.m_themeRes.m_shareType != LockRes.SHARE_TYPE_NONE
						&& TagMgr.CheckTag(context, Tags.THEME_UNLOCK + out.m_uri) && showLock)
				{
					out.m_lock = true;
				}
			}

			return out;
		}
	}

	public static ArrayList<GroupInfo> GetManageInfos(ResType type,int tag)
	{
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{
			ArrayList<GroupInfo> out = new ArrayList<>();
			boolean addfilter = true;
			boolean addwatermark = true;
			boolean addattitute = true;
			boolean addeffect = true;
			boolean addvideowatermark = true;
			boolean addvideooriginality = true;
			boolean addmuic = true;
			if(type != null)
			{
				if(type == ResType.FILTER)
				{
					addfilter = true;
					addwatermark = false;
					addattitute = false;
					addeffect = false;
					addvideowatermark = false;
					addvideooriginality = false;
					addmuic = false;

				}
				else if(type == ResType.TEXT_WATERMARK)
				{
					addfilter = false;
					addwatermark = true;
					addattitute = false;
					addeffect = false;
					addvideowatermark = false;
					addvideooriginality = false;
					addmuic = false;
				}
				else if(type == ResType.TEXT_ATTITUTE)
				{
					addfilter = false;
					addwatermark = false;
					addattitute = true;
					addeffect = false;
					addvideowatermark = false;
					addvideooriginality = false;
					addmuic = false;
				}
				else if(type == ResType.LIGHT_EFFECT)
				{
					addfilter = false;
					addwatermark = false;
					addattitute = false;
					addeffect = true;
					addvideowatermark = false;
					addvideooriginality = false;
					addmuic = false;
				}
				else if (type == ResType.VIEDO_WATERMARK){
					addfilter = false;
					addwatermark = false;
					addattitute = false;
					addeffect = false;
					addvideowatermark = true;
					addvideooriginality = false;
					addmuic = false;
				}
				else if (type == ResType.VIEDO_ORIGINALITY){
					addfilter = false;
					addwatermark = false;
					addattitute = false;
					addeffect = false;
					addvideowatermark = false;
					addvideooriginality = true;
					addmuic = false;

				}
				else if(type == ResType.MUSIC){

					addfilter = false;
					addwatermark = false;
					addattitute = false;
					addeffect = false;
					addvideowatermark = true;
					addvideooriginality = false;
					addmuic = true;
				}
			}
			ArrayList<BaseRes> ress;
			GroupInfo info;
			SQLiteDatabase db = ThemeResMgr2.getInstance().GetDB();
			//滤镜
			if(addfilter)
			{
				ress = new ArrayList<>();
				ress.addAll(FilterResMgr2.getInstance().GetLocalResArr(db, false));
				info = new GroupInfo();
				info.m_type = ResType.FILTER;
				info.m_resArr = ress;
				out.add(info);
			}
			//水印
			if(addwatermark || addattitute)
			{
				ress = new ArrayList<>();
				info = new GroupInfo();
				if (addwatermark){
					ress.addAll(TextResMgr2.getInstance().GetLocalResArr(db, tag));
					info.m_type = ResType.TEXT_WATERMARK;
				}else {
					ress.addAll(TextResMgr2.getInstance().GetLocalResArr(db, tag));
					info.m_type = ResType.TEXT_ATTITUTE;
				}

				info.m_resArr = ress;
				out.add(info);
			}

			//视频水印
			if (addvideowatermark || addvideooriginality){
				ress = new ArrayList<>();
				info = new GroupInfo();
				if (addvideowatermark){
					ress.addAll(VideoTextResMgr2.getInstance().GetLocalResArr(db,tag));
					info.m_type = ResType.VIEDO_WATERMARK;
				}else {
					ress.addAll(VideoTextResMgr2.getInstance().GetLocalResArr(db,tag));
					info.m_type = ResType.VIEDO_ORIGINALITY;
				}
				info.m_resArr = ress;
				out.add(info);
			}



			//光效
			if(addeffect)
			{
				ress = new ArrayList<>();
				ress.addAll(LightEffectResMgr2.getInstance().GetLocalResArr(db));
				info = new GroupInfo();
				info.m_type = ResType.LIGHT_EFFECT;
				info.m_resArr = ress;
				out.add(info);
			}

			//态度
			if (addmuic){
				ress = new ArrayList<>();
				ress.addAll(MusicResMgr2.getInstance().GetLocalResArr(db));
				info = new GroupInfo();
				info.m_type = ResType.MUSIC;
				info.m_resArr = ress;
				out.add(info);
			}


			ThemeResMgr2.getInstance().CloseDB();
			return out;
		}
	}
	/**
	 * 检查一组素材的下载状态
	 *
	 * @param ress
	 *            该组素材
	 * @param downloadIds
	 *            下载完成的素材的id, 可以为null
	 * @return 该组素材的下载状态
	 */
	public static int checkGroupDownloadState(ArrayList<? extends BaseRes> ress, int[] resIds, ArrayList<Integer> downloadIds)
	{
		m_prepareCount = 0;
		m_downloadingCount = 0;
		m_completeCount = 0;

		if(null == ress || ress.size() == 0)
			return ThemeItemInfo.PREPARE;
		if(resIds != null)
		{
			if(ress.size() != resIds.length)
			{
				return ThemeItemInfo.PREPARE;
			}
		}
		int len = ress.size();
		for(int i = 0; i < len; i++)
		{
			int flag = checkDownloadState(ress.get(i));
			if(flag != 0)
			{
				m_downloadingCount++;
			}
			else
			{
				if(ress.get(i).m_type == BaseRes.TYPE_NETWORK_URL)
				{
					m_prepareCount++;
				}
				else
				{
					m_completeCount++;
					if(null != downloadIds)
					{
						downloadIds.add(ress.get(i).m_id);
					}
				}
			}
		}

		if(m_downloadingCount != 0)
		{
			return ThemeItemInfo.LOADING;
		}
		else if(m_completeCount == ress.size())
		{
			return ThemeItemInfo.COMPLETE;
		}
		else if(m_prepareCount == ress.size())
		{
			return ThemeItemInfo.PREPARE;
		}
		else
		{
			return ThemeItemInfo.CONTINUE;
		}
	}

	/**
	 * 获取某个资源的下载状态
	 *
	 * @param res
	 * @return
	 * 		0:没有下载
	 *         1:等待中
	 *         2:下载中
	 */
	public static int checkDownloadState(BaseRes res)
	{
		int flag = -1;
		if(res != null)
		{
			if(res.m_type != BaseRes.TYPE_NETWORK_URL)
				return 0;
			flag = DownloadMgr.getInstance().GetStateById(res.m_id, res.getClass());
		}
		return flag;
	}

	public static int getM_prepareCount()
	{
		return m_prepareCount;
	}

	public static int getM_downloadingCount()
	{
		return m_downloadingCount;
	}

	public static int getM_completeCount()
	{
		return m_completeCount;
	}

	public static class MyDownloadCB implements DownloadMgr.Callback2
	{
		MyCB m_cb;

		int m_type;
		int[] m_ids;
		int m_themeId = -1;
		Object m_info;

		public MyDownloadCB(MyCB cb)
		{
			m_cb = cb;
		}

		public void setDatas(int[] ids, int type, int themeId)
		{
			m_ids = ids;
			m_type = type;
			m_themeId = themeId;
		}

		public void setInfos(Object info)
		{
			m_info = info;
		}

		@Override
		public void OnProgress(int downloadId, IDownload res, int progress)
		{
			if(m_cb != null)
			{
				m_cb.OnProgress(downloadId, res, progress);
			}
		}

		@Override
		public void OnComplete(int downloadId, IDownload res)
		{
			if(m_cb != null)
			{
				m_cb.OnComplete(downloadId, res);
			}
		}

		@Override
		public void OnFail(final int downloadId, final IDownload res)
		{
			if(m_cb != null)
			{
				m_cb.OnFail(downloadId, res);
			}
		}

		@Override
		public void OnGroupComplete(int downloadId, IDownload[] resArr)
		{
			// TODO Auto-generated method stub
			if(null != m_ids && m_themeId != -1)
			{
				addToNewMgr();
			}
			if(m_cb != null)
			{
				m_cb.OnGroupComplete(downloadId, resArr);
			}
		}

		@Override
		public void OnGroupFail(int downloadId, IDownload[] resArr)
		{
			if(m_cb != null)
			{
				m_cb.OnGroupFail(downloadId, resArr);
			}
		}

		public void ClearAll()
		{
			m_cb = null;
			m_info = null;
		}

		private void addToNewMgr()
		{
			/*if(m_type == FRAME)
			{
				//将下载完成的边框加到本地new队列中
				ResourceMgr.AddFrameId(m_ids);
				ResourceMgr.AddFrameNewFlag(m_context, m_ids);
			}
			else if(m_type == DECORATE)
			{
				//将下载完成的装饰加到本地new队列中
				//System.out.println("themeId: " + m_themeId);
				ResourceMgr.AddDecorateGroupId(m_themeId);
				ResourceMgr.AddDecorateNewFlag(m_context, m_themeId);
			}
			else if(m_type == MAKEUP)
			{
				//将下载完成的彩妆加到本地new队列中
				//ResourceMgr.AddMakeupComboId(m_ids);
				ResourceMgr.AddMakeupGroupId(m_themeId);
				ResourceMgr.AddMakeupComboNewFlag(m_context, m_ids);
			}*/
		}

		@Override
		public void OnGroupProgress(int downloadId, IDownload[] resArr, int progress)
		{
			// TODO Auto-generated method stub
			if(m_cb != null)
			{
				m_cb.OnGroupProgress(downloadId, resArr, progress);
			}
		}
	}

	/**
	 * 处理当前页面的回调函数{@link MyDownloadCB #OnComplete(int, BaseRes) #OnFail(int,
	 * BaseRes)}}
	 * 使退出的时候能够释放当前页面，防止内存泄露
	 *
	 * @author pocouser
	 *
	 */
	public interface MyCB
	{
		public void OnFail(final int downloadId, final IDownload res);

		public void OnGroupFail(int downloadId, IDownload[] resArr);

		public void OnProgress(int downloadId, IDownload res, int progress);

		public void OnComplete(int downloadId, IDownload res);

		public void OnGroupProgress(int downloadId, IDownload[] resArr, int progress);

		public void OnGroupComplete(int downloadId, IDownload[] resArr);
	}

	public static class MyRefreshCb
	{
		MyCB2 m_cb;

		public MyRefreshCb(MyCB2 cb)
		{
			m_cb = cb;
		}

		public void OnFinish()
		{
			if(m_cb != null)
			{
				m_cb.OnFinish();
			}
		}

		public void ClearAll()
		{
			m_cb = null;
		}
	}

	public interface MyCB2
	{
		public void OnFinish();
	}

}
