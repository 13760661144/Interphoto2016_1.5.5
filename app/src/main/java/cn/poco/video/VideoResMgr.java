package cn.poco.video;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.MaterialMgr2.MgrUtils;
import cn.poco.MaterialMgr2.ThemeItemInfo;
import cn.poco.Text.Painter;
import cn.poco.beautify.BeautyAdjustType;
import cn.poco.beautify.BeautyColorType;
import cn.poco.beautify.CurveView;
import cn.poco.beautify.SimpleListItem;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.interphoto2.R;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.FilterRes;
import cn.poco.resource.FilterResMgr2;
import cn.poco.resource.LightEffectRes;
import cn.poco.resource.LockRes;
import cn.poco.resource.MusicRes;
import cn.poco.resource.MusicResMgr2;
import cn.poco.resource.ResourceMgr;
import cn.poco.resource.ThemeRes;
import cn.poco.resource.ThemeResMgr2;
import cn.poco.resource.VideoTextRes;
import cn.poco.resource.VideoTextResMgr2;
import cn.poco.resource.database.ResourseDatabase;
import cn.poco.setting.LanguagePage;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv100.SimpleBtnList100;
import cn.poco.video.render.adjust.AdjustItem;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.videoMusic.Mp3Info;

/**
 * Created by lgd on 2017/6/8.
 */

public class VideoResMgr
{
	public static ArrayList<DragListItemInfo> GetMusicRess(Context context)
	{
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{
			ArrayList<DragListItemInfo> out = new ArrayList<DragListItemInfo>();
			DragListItemInfo info;
			MusicRes res;
			SQLiteDatabase db = ResourseDatabase.getInstance(context).openDatabase();
			ArrayList<MusicRes> resArr = MusicResMgr2.getInstance().GetAllResArr(context, db);
			HashMap<Integer, Boolean> ids = new HashMap<>();
			if(resArr != null && resArr.size() > 0)
			{
				int size = resArr.size();
				for(int i = 0; i < size; i++)
				{
					res = resArr.get(i);
					if(res.m_thumb == null || (res.m_thumb instanceof String && res.m_thumb.equals(""))){
						continue;
					}
					info = new DragListItemInfo();
					info.m_uri = res.m_id;
					info.m_name = res.m_name;
					info.m_logo = res.m_thumb;
					if(TextUtils.isEmpty(res.coverColor)){
						res.coverColor = "000000";
					}
					info.text_bg_color_out = Painter.GetColor(res.coverColor, 0x99);
					info.text_bg_color_over = info.text_bg_color_out;
					info.m_ex = res;
					int flag = MgrUtils.checkDownloadState(res);
					if(flag == 0 && res.m_type == LightEffectRes.TYPE_NETWORK_URL)
					{
						info.m_style = DragListItemInfo.Style.NEED_DOWNLOAD;
						info.m_canDrop = true;
					}
					else if(flag == 1 || flag == 2)
					{
						info.m_style = DragListItemInfo.Style.LOADING;
					}
					else
					{
						info.m_style = DragListItemInfo.Style.NORMAL;
					}
					if(info.m_style == DragListItemInfo.Style.NORMAL || info.m_style == DragListItemInfo.Style.NEW)
					{
						info.m_canDrag = true;
						info.m_canDrop = true;
					}
					if(res.lockType != LockRes.SHARE_TYPE_NONE){
                        info.m_isLock = true;
                    }
					out.add(info);
					ids.put(info.m_uri, true);
				}
			}

			//本地音乐
			info = new DragListItemInfo();
			info.m_name = "music";
			info.m_uri = DragListItemInfo.URI_LOCAL_MUSIC;
//			info.text_bg_color_out = 0x99000000;
//			info.text_bg_color_over = 0x99000000;
            info.m_logo = R.drawable.video_music_itunes_logo;
			info.m_ex = null;
			out.add(0, info);

			//删除音乐
			info = new DragListItemInfo();
			info.m_uri = DragListItemInfo.URI_MUSIC_NONE;
            info.m_logo = R.drawable.video_text_none;
			info.m_ex = null;
			out.add(0, info);

//			out.add(GetDownloadMoreItem(context));
			ResourseDatabase.getInstance(context).closeDatabase();
			return out;
		}
	}


	public static ArrayList<DragListItemInfo> getVedioTextRess(Context context, int typeID)
	{
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{
			String ver = SysConfig.GetAppVer(context);
			boolean showLock = true;
			if(ver.endsWith("_r1") && !TagMgr.CheckTag(context, LanguagePage.ENGLISH_TAGVALUE))
			{
				showLock = false;
			}
			ArrayList<DragListItemInfo> out = new ArrayList<>();
			DragListItemInfo info;
			SQLiteDatabase db = ResourseDatabase.getInstance(context).openDatabase();
			ArrayList<VideoTextRes> textRes = VideoTextResMgr2.getInstance().GetLocalResArr(db, typeID);
			HashMap<Integer, Boolean> ids = new HashMap<>();
			String classifyFlag = "";
			if(typeID == 1)
			{
				classifyFlag = "watermark";
			}
			else if(typeID == 2)
			{
				classifyFlag = "originality";
			}
			if(textRes != null)
			{
				int size = textRes.size();
				for(int i = 0; i < size; i++)
				{
					VideoTextRes res = textRes.get(i);
					if(res.m_thumb == null || (res.m_thumb instanceof String && res.m_thumb.equals(""))){
						continue;
					}
					info = new DragListItemInfo();
					info.m_uri = res.m_id;
					info.m_name = res.m_name;
					info.m_logo = res.m_thumb;
//					info.m_head = res.shareImg;
					info.text_bg_color_over = 0xb2ffc433;
					info.m_ex = res;
					int flag = MgrUtils.checkDownloadState(res);
					if(flag == 0 && res.m_type == VideoTextRes.TYPE_NETWORK_URL)
					{
						info.m_style = DragListItemInfo.Style.NEED_DOWNLOAD;
						info.m_canDrop = true;
					}
					else if(flag == 1 || flag == 2)
					{
						info.m_style = DragListItemInfo.Style.LOADING;
					}
					else
					{
						info.m_style = DragListItemInfo.Style.NORMAL;
					}
					if(info.m_style == DragListItemInfo.Style.NORMAL || info.m_style == DragListItemInfo.Style.NEW)
					{
						info.m_canDrag = true;
						info.m_canDrop = true;
					}
//                    if(res.lockType != LockRes.SHARE_TYPE_NONE && res.m_type == BaseRes.TYPE_NETWORK_URL){
//						int lock = TagMgr.GetTagIntValue(PocoCamera.main, Tags.VIDEOTEXT_UNLOCK + info.m_uri);
//						if(lock == 0) {
//                        	info.m_isLock = true;
//						}
//                    }
					out.add(info);
					ids.put(info.m_uri, true);
				}
			}
			//推荐位
			ArrayList<ThemeRes> themeResArr = ThemeResMgr2.getInstance().GetAllThemeResArr(db);
			if(themeResArr != null)
			{
				int uri = DragListItemInfo.URI_RECOMMENT;
				for(ThemeRes themeRes : themeResArr)
				{
					if(!themeRes.m_isHide && themeRes.m_watermarkIDArr != null && themeRes.m_watermarkIDArr.length > 0)
					{
						if(themeRes.m_type == BaseRes.TYPE_NETWORK_URL)
						{
							DownloadMgr.getInstance().DownloadRes(themeRes, null);
							continue;
						}
						if(!themeRes.m_dashiType.equals(classifyFlag))
						{
							continue;
						}
						ArrayList<VideoTextRes> textRes1 = VideoTextResMgr2.getInstance().GetResArr(db, themeRes.m_watermarkIDArr);
						int state = MgrUtils.checkGroupDownloadState(textRes1, themeRes.m_watermarkIDArr, null);
						if(state != ThemeItemInfo.COMPLETE)
						{
							info = new DragListItemInfo();
							info.m_uri = uri;
							info.m_name = themeRes.m_name;
							info.m_logo = themeRes.m_thumb;
							info.m_isRecomment = true;
							info.text_bg_color_out = 0x00000000;
							info.text_bg_color_over = info.text_bg_color_out;
							info.m_ex = themeRes;
							info.m_style = DragListItemInfo.Style.NEED_DOWNLOAD;
							info.m_canDrag = false;
							info.m_canDrop = true;
							if(themeRes.m_recommend < out.size()){
								out.add(themeRes.m_recommend, info);
							}
							else
							{
								out.add(info);
							}
							uri++;
						}
					}

				}
			}
			//原位
			info = new DragListItemInfo();
			info.m_name = context.getResources().getString(R.string.oriphoto);
			info.m_uri = DragListItemInfo.URI_VIDEO_TEXT_NONE;
            info.m_logo = R.drawable.video_text_none;
			info.m_ex = null;
			out.add(0, info);
//			out.add(GetDownloadMoreItem(context));

			out.add(0,GetDownloadMoreItem(context));
			ResourseDatabase.getInstance(context).closeDatabase();
			return out;
		}
	}

	public static ArrayList<DragListItemInfo> GetFilterRess(Context context)
	{
			synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
			{
				ArrayList<DragListItemInfo> out = new ArrayList<>();
				DragListItemInfo info;
				FilterRes res;
				SQLiteDatabase db = ResourseDatabase.getInstance(context).openDatabase();
				ArrayList<FilterRes> resArray = FilterResMgr2.getInstance().GetLocalResArr(db, true);
				HashMap<Integer, Boolean> ids = new HashMap<>();
				if(resArray != null && resArray.size() > 0)
				{
					int size = resArray.size();
					for(int i = 0; i < size; i++)
					{
						info = MakeFilterDragItemInfo(resArray.get(i));
						out.add(info);
						ids.put(info.m_uri, true);
					}
				}

				//推荐位
				ArrayList<ThemeRes> themeResArr = ThemeResMgr2.getInstance().GetAllThemeResArr(db);
				if(themeResArr != null)
				{
					int uri = DragListItemInfo.URI_RECOMMENT;
					for(ThemeRes themeRes : themeResArr)
					{
						if(!themeRes.m_isHide && themeRes.m_filterIDArr != null && themeRes.m_filterIDArr.length > 0)
						{
							if(themeRes.m_type == BaseRes.TYPE_NETWORK_URL)
							{
								DownloadMgr.getInstance().DownloadRes(themeRes, null);
								continue;
							}
							ArrayList<FilterRes> filterRes = FilterResMgr2.getInstance().GetResArr(db, themeRes.m_filterIDArr);
							int state = MgrUtils.checkGroupDownloadState(filterRes, themeRes.m_filterIDArr, null);
							if(state != ThemeItemInfo.COMPLETE)
							{
								info = new DragListItemInfo();
								info.m_uri = uri;
								info.m_name = themeRes.m_subTitle;
								info.m_author = themeRes.m_dashiName;
								info.m_logo = themeRes.m_thumb;
								info.m_isRecomment = true;
								info.text_bg_color_out = Painter.GetColor(themeRes.m_titleColor, 0x99);
								info.text_bg_color_over = info.text_bg_color_out;
								info.m_ex = themeRes;
								info.m_style = DragListItemInfo.Style.NEED_DOWNLOAD;
								info.m_canDrag = false;
								info.m_canDrop = true;
								if(themeRes.m_recommend < out.size()){
									out.add(themeRes.m_recommend, info);
								}
								else
								{
									out.add(info);
								}
								uri++;
							}
						}

					}
				}

				info = new DragListItemInfo();
				info.m_name = context.getResources().getString(R.string.orignVideo);
				info.m_uri = DragListItemInfo.URI_ORIGIN;
				info.text_bg_color_out = 0x99000000;
				info.text_bg_color_over = 0x99000000;
				info.m_ex = null;
				out.add(0, info);

				out.add(0, GetDownloadMoreItem(context));
				ResourseDatabase.getInstance(context).closeDatabase();
				return out;
			}
	}





	private static DragListItemInfo MakeFilterDragItemInfo(FilterRes res)
	{
		DragListItemInfo info = new DragListItemInfo();
		info.m_uri = res.m_id;
		info.m_name = res.m_name;
		info.m_logo = res.m_thumb;
		info.m_head = res.m_authorImg;
		if(res.m_coverColor != null && res.m_coverColor.length() > 0)
		{
			info.text_bg_color_out = Painter.GetColor(res.m_coverColor, 0x99);
		}
		else
		{
			info.text_bg_color_out = 0x99000000;
		}
		info.text_bg_color_over = info.text_bg_color_out;
		if(res.m_masterType == FilterRes.MASTER_TYPE3)
		{
			info.m_author = res.m_authorName;
		}
		info.m_ex = res;
		int flag = MgrUtils.checkDownloadState(res);
		if(flag == 0 && res.m_type == LightEffectRes.TYPE_NETWORK_URL)
		{
			info.m_style = DragListItemInfo.Style.NEED_DOWNLOAD;
		}
		else if(flag == 1 || flag == 2)
		{
			info.m_style = DragListItemInfo.Style.LOADING;
		}
		else
		{
			info.m_style = DragListItemInfo.Style.NORMAL;
		}
		if(info.m_style == DragListItemInfo.Style.NORMAL)
		{
			info.m_canDrag = true;
			info.m_canDrop = true;
		}
		return info;
	}

	public static FilterRes getFilterRes(Context context, int filterUri) {
		SQLiteDatabase db = ResourseDatabase.getInstance(context).openDatabase();
		ArrayList<FilterRes> resArray = FilterResMgr2.getInstance().GetLocalResArr(db, true);
		for (FilterRes item : resArray) {
			if (item.m_id == filterUri) {
			    return item;
			}
		}
		return null;
	}


	public static DragListItemInfo GetDownloadMoreItem(Context context)
	{
		DragListItemInfo info = new DragListItemInfo();
//		info.m_name = context.getResources().getString(R.string.oriphoto);
		info.m_uri = DragListItemInfo.URI_MGR;
		info.text_bg_color_out = 0x0000000f;
		info.text_bg_color_over = 0x0000000f;
		return info;
	}

	public static ArrayList<SimpleBtnList100.Item> getColorItems(Context context)
	{
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<>();
		int width = ShareData.m_screenWidth / 2;

		SimpleListItem item = new SimpleListItem(context);
		item.m_uri = 0;
		item.m_ex = BeautyColorType.FILTER;
		item.img_res_out = R.drawable.beauty_filter_btn_out;
		item.img_res_over = R.drawable.beauty_filter_btn_over;
		item.item_width = width;
		item.InitDatas();
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = 1;
		item.m_ex = BeautyColorType.ADJUST;
		item.img_res_over = R.drawable.beauty_adjust_btn_out;
		item.img_res_out = R.drawable.beauty_adjust_btn_out;
		item.item_width = width;
		item.InitDatas();
		out.add(item);

		return out;
	}

//	public static ArrayList<SimpleBtnList100.Item> sAdjustItemList;
	public static ArrayList<SimpleBtnList100.Item> getColorAdjustItems(Context context)
	{
//        if (sAdjustItemList != null) {
//            for (SimpleBtnList100.Item item : sAdjustItemList) {
//				ViewParent parent = item.getParent();
//				ViewGroup parentGroup = (ViewGroup) parent;
//				parentGroup.removeView(item);
//			}
//			return sAdjustItemList;
//		} else {
//        	sAdjustItemList = new ArrayList<>();
			ArrayList<SimpleBtnList100.Item> out = new ArrayList<>();
			int width = (int)(ShareData.m_screenWidth / 4.5);
			AdjustData data;

			SimpleListItem item = new SimpleListItem(context);
			item.m_uri = BeautyAdjustType.BRIGHTNESS.GetValue();
			item.m_title = context.getResources().getString(R.string.Exposure);
			item.m_tjID = R.integer.美化_滤镜_调整_亮度;
			item.img_res_over = R.drawable.beauty_color_brightness_over;
			item.img_res_out = R.drawable.beauty_color_brightness_out;
			item.item_width = width;
			item.InitDatas();
			data = new AdjustData(AdjustItem.BRIGHTNESS, 0);
			data.m_type = BeautyAdjustType.BRIGHTNESS;
			item.m_ex = data;
			out.add(item);

			item = new SimpleListItem(context);
			item.m_uri = BeautyAdjustType.CONTRAST.GetValue();
			item.m_title = context.getResources().getString(R.string.Contrast);
			item.m_tjID = R.integer.美化_滤镜_调整_对比度;
			item.img_res_over = R.drawable.beauty_color_contrast_over;
			item.img_res_out = R.drawable.beauty_color_contrast_out;
			item.item_width = width;
			item.InitDatas();
			data = new AdjustData(AdjustItem.CONTRAST, 1.05f);
			data.m_type = BeautyAdjustType.CONTRAST;
			item.m_ex = data;
			out.add(item);


			item = new SimpleListItem(context);
			item.m_uri = BeautyAdjustType.CURVE.GetValue();
			item.m_title = context.getResources().getString(R.string.Curve);
			item.m_tjID = R.integer.美化_滤镜_调整_曲线;
			item.m_shenceTjStr = "curve";
			item.m_shenceTjID = R.string.滤镜细节调整_曲线;
			item.img_res_over = R.drawable.beauty_color_curve_over;
			item.img_res_out = R.drawable.beauty_color_curve_out;
			item.item_width = width;
			item.InitDatas();
			data = new AdjustData(BeautyAdjustType.CURVE, 0);
			data.m_type = BeautyAdjustType.CURVE;
			item.m_ex = data;
			data.m_tjId = item.m_shenceTjStr;
			out.add(item);

			item = new SimpleListItem(context);
			item.m_uri = BeautyAdjustType.SATURABILITY.GetValue();
			item.m_title = context.getResources().getString(R.string.Saturation);
			item.m_tjID = R.integer.美化_滤镜_调整_饱和度;
			item.img_res_over = R.drawable.beauty_color_saturability_over;
			item.img_res_out = R.drawable.beauty_color_saturability_out;
			item.item_width = width;
			item.InitDatas();
			data = new AdjustData(AdjustItem.SATURATION, 1);
			data.m_type = BeautyAdjustType.SATURABILITY;
			item.m_ex = data;
			out.add(item);

			item = new SimpleListItem(context);
			item.m_uri = BeautyAdjustType.SHARPEN.GetValue();
			item.m_title = context.getResources().getString(R.string.Sharpness);
			item.m_tjID = R.integer.美化_滤镜_调整_锐化;
			item.img_res_over = R.drawable.beauty_color_sharpen_over;
			item.img_res_out = R.drawable.beauty_color_sharpen_out;
			item.item_width = width;
			item.InitDatas();
			data = new AdjustData(AdjustItem.SHARPEN, 0f);
			data.m_type = BeautyAdjustType.SHARPEN;
			item.m_ex = data;
			out.add(item);

			item = new SimpleListItem(context);
			item.m_uri = BeautyAdjustType.TEMPERATURE.GetValue();
			item.m_title = context.getResources().getString(R.string.Temperature);
			item.m_tjID = R.integer.美化_滤镜_调整_色温;
			item.img_res_over = R.drawable.beauty_color_temperature_over;
			item.img_res_out = R.drawable.beauty_color_temperature_out;
			item.item_width = width;
			item.InitDatas();
			data = new AdjustData(AdjustItem.WHITE_BALANCE, 0);
			data.m_type = BeautyAdjustType.TEMPERATURE;
			item.m_ex = data;
			out.add(item);

			item = new SimpleListItem(context);
			item.m_uri = BeautyAdjustType.HUE.GetValue();
			item.m_title = context.getResources().getString(R.string.Tone);
			item.m_tjID = R.integer.美化_滤镜_调整_色调;
			item.img_res_over = R.drawable.beauty_color_hue_over;
			item.img_res_out = R.drawable.beauty_color_hue_out;
			item.item_width = width;
			item.InitDatas();
			data = new AdjustData(AdjustItem.COLOR_BALANCE, 0);
			data.m_type = BeautyAdjustType.HUE;
			item.m_ex = data;
			out.add(item);

			item = new SimpleListItem(context);
			item.m_uri = BeautyAdjustType.HIGHTLIGHT.GetValue();
			item.m_title = context.getResources().getString(R.string.Highlight);
			item.m_tjID = R.integer.美化_滤镜_调整_高光;
			item.img_res_over = R.drawable.beauty_color_hightlight_over;
			item.img_res_out = R.drawable.beauty_color_hightlight_out;
			item.item_width = width;
			item.InitDatas();
			data = new AdjustData(AdjustItem.HIGHLIGHT, 0f);
			data.m_type = BeautyAdjustType.HIGHTLIGHT;
			item.m_ex = data;
			out.add(item);

			item = new SimpleListItem(context);
			item.m_uri = BeautyAdjustType.SHADE.GetValue();
			item.m_title = context.getResources().getString(R.string.Shadow);
			item.m_tjID = R.integer.美化_滤镜_调整_阴影;
			item.img_res_over = R.drawable.beauty_color_shade_over;
			item.img_res_out = R.drawable.beauty_color_shade_out;
			item.item_width = width;
			item.InitDatas();
			data = new AdjustData(AdjustItem.SHADOW, 0f);
			data.m_type = BeautyAdjustType.SHADE;
			item.m_ex = data;
			out.add(item);

			item = new SimpleListItem(context);
			item.m_uri = BeautyAdjustType.DARKCORNER.GetValue();
			item.m_title = context.getResources().getString(R.string.Vignette);
			item.m_tjID = R.integer.美化_滤镜_调整_暗角;
			item.img_res_over = R.drawable.beauty_color_vignetting_over;
			item.img_res_out = R.drawable.beauty_color_vignetting_out;
			item.item_width = width;
			item.InitDatas();
			data = new AdjustData(AdjustItem.DARK_CORNER, 0f);
			data.m_type = BeautyAdjustType.DARKCORNER;
			item.m_ex = data;
			out.add(item);
//			sAdjustItemList.addAll(out);
			return out;
//		}
	}


	/**
	 * 用于从数据库中查询歌曲的信息，保存在List当中
	 * http://blog.csdn.net/little_shengsheng/article/details/51384984
	 *
	 * @return
	 */
	public static List<Mp3Info> getMusicInfo(Context context)
	{
		List<Mp3Info> mp3Infos = new ArrayList<>();

		Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

		if (cursor != null) {
			for(int i = 0; i < cursor.getCount(); i++)
			{
				cursor.moveToNext();
				Mp3Info mp3Info = new Mp3Info();
				long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));   //音乐id
				String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))); // 音乐标题
				String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 艺术家
				String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)); //专辑
				String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
				long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
				long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
				long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 文件大小
				String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)); // 文件路径
				int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)); // 是否为音乐
				if(isMusic != 0 && FileUtils.isFileExist(url))
				{ // 只把音乐添加到集合当中
					mp3Info.setId(id);
					mp3Info.setTitle(title);
					mp3Info.setArtist(artist);
					mp3Info.setAlbum(album);
					mp3Info.setDisplayName(displayName);
					mp3Info.setAlbumId(albumId);
					mp3Info.setDuration(duration);
					mp3Info.setSize(size);
					mp3Info.setUrl(url);
					mp3Infos.add(mp3Info);
				}
			}

			cursor.close();
		}

		return mp3Infos;
	}

	/**
	 * 清除不需要的数据
	 */
//	public static void clearData() {
//        if (VideoResMgr.sAdjustItemList != null) {
//			VideoResMgr.sAdjustItemList.clear();
//			VideoResMgr.sAdjustItemList = null;
//		}
//	}

	public static class AdjustData
	{
		public BeautyAdjustType m_type;
		public int m_type1;		//视频拼接调整用到
		public float m_value = 0;

		public String m_tjId; //视频曲线

		public AdjustData(BeautyAdjustType type, float value)
		{
			m_type = type;
			m_value = value;
		}

		public AdjustData(int type, float value)
		{
			m_type1 = type;
			m_value = value;
		}

		public AdjustData() {

		}


		public AdjustData Clone()
		{
			AdjustData out = new AdjustData(m_type1, m_value);
			out.m_tjId = m_tjId;
			out.m_type = m_type;
			return out;
		}
	}

	public static class CurveData extends AdjustData{
	    public VideoControlInfo mRed, mGreen, mBlue, mRGB;

	    public CurveData(VideoControlInfo red, VideoControlInfo green, VideoControlInfo blue,VideoControlInfo rgb) {
			this.m_type = BeautyAdjustType.CURVE;
	        this.mRed = red;
	        this.mGreen = green;
	        this.mBlue = blue;
	        this.mRGB = rgb;
		}

		public CurveData() {
			this.m_type = BeautyAdjustType.CURVE;

			int size = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(100);
			mRed = new VideoControlInfo();
			mRed.line_color = Color.RED;
			mRed.m_orgPoints.add(new PointF(0, size));
			mRed.m_orgPoints.add(new PointF(size, 0));

			for (int i = 0; i < mRed.m_orgPoints.size(); i++) {
				int x = (int)(mRed.m_orgPoints.get(i).x + 0.5);
				int y = (int)(mRed.m_orgPoints.get(i).y + 0.5);
				mRed.m_ctrlPoints.add(new Point(x, y));
			}

			mGreen = new VideoControlInfo();
			mGreen.line_color = Color.GREEN;
			mGreen.m_orgPoints.add(new PointF(0, size));
			mGreen.m_orgPoints.add(new PointF(size, 0));
			for (int i = 0; i < mGreen.m_orgPoints.size(); i++) {
				int x = (int)(mGreen.m_orgPoints.get(i).x + 0.5);
				int y = (int)(mGreen.m_orgPoints.get(i).y + 0.5);
				mGreen.m_ctrlPoints.add(new Point(x, y));
			}

			mBlue = new VideoControlInfo();
			mBlue.line_color = Color.BLUE;
			mBlue.m_orgPoints.add(new PointF(0, size));
			mBlue.m_orgPoints.add(new PointF(size, 0));

			for (int i = 0; i < mBlue.m_orgPoints.size(); i++) {
				int x = (int)(mBlue.m_orgPoints.get(i).x + 0.5);
				int y = (int)(mBlue.m_orgPoints.get(i).y + 0.5);
				mBlue.m_ctrlPoints.add(new Point(x, y));
			}

			mRGB = new VideoControlInfo();
			mRGB.line_color = Color.WHITE;
			mRGB.m_orgPoints.add(new PointF(0, size));
			mRGB.m_orgPoints.add(new PointF(size, 0));

			for (int i = 0; i < mRGB.m_orgPoints.size(); i++) {
				int x = (int)(mRGB.m_orgPoints.get(i).x + 0.5);
				int y = (int)(mRGB.m_orgPoints.get(i).y + 0.5);
				mRGB.m_ctrlPoints.add(new Point(x, y));
			}
		}

		public VideoControlInfo transformToVideoControlInfo(CurveView.ControlInfo controlInfo) {
	    	VideoControlInfo videoControlInfo = new VideoControlInfo();
	    	videoControlInfo.line_color = controlInfo.line_color;
	    	for (int i = 0; i < controlInfo.m_ctrlPoints.size(); i++) {
	    	    int x = (int)(controlInfo.m_ctrlPoints.get(i).x + 0.5);

				int y = (int)(controlInfo.m_ctrlPoints.get(i).y + 0.5);
				videoControlInfo.m_ctrlPoints.add(new Point(x, y));
			}
			videoControlInfo.m_id = controlInfo.m_id;
	    	videoControlInfo.m_orgPoints = controlInfo.m_orgPoints;
	    	videoControlInfo.m_path = controlInfo.m_path;
	    	videoControlInfo.m_pathPoints = controlInfo.m_pathPoints;
	    	return videoControlInfo;
		}

		public CurveView.ControlInfo transformBackToControlInfo(VideoControlInfo videoControlInfo) {
			CurveView.ControlInfo controlInfo = new CurveView.ControlInfo();
			controlInfo.line_color = videoControlInfo.line_color;
			controlInfo.m_id = videoControlInfo.m_id;
			controlInfo.m_orgPoints = videoControlInfo.m_orgPoints;
			controlInfo.m_path = videoControlInfo.m_path;
			controlInfo.m_pathPoints = videoControlInfo.m_pathPoints;
			for (int i = 0; i < videoControlInfo.m_ctrlPoints.size(); i++) {
				float x = (videoControlInfo.m_ctrlPoints.get(i).x);
				float y = (videoControlInfo.m_ctrlPoints.get(i).y);
				controlInfo.m_ctrlPoints.add(new PointF(x, y));
			}
			return controlInfo;
		}


		public CurveData CloneData()
		{
			CurveData curveData = new CurveData();
			curveData.mRed = mRed;
			curveData.mGreen = mGreen;
			curveData.mBlue = mBlue;
			curveData.mRGB = mRGB;

			curveData.m_type = this.m_type;
			curveData.m_tjId = this.m_tjId;
			curveData.m_type = this.m_type;
			curveData.m_type1 = this.m_type1;
			return curveData;
		}
	}


	public static class VideoControlInfo {
		public int m_id;
		public int line_color;
		public Path m_path = new Path();
		protected ArrayList<PointF> m_pathPoints = new ArrayList<>();
		protected ArrayList<PointF> m_orgPoints = new ArrayList<>();
		public ArrayList<Point> m_ctrlPoints = new ArrayList<>();
	}





	public static class FilterData {
	    public int mFilterUrl; // 0 - 12
	    public int mFilterAlpha;
	    public boolean mIsValid;
	    public FilterRes mFilterRes;

	    public FilterData() {

		}

		public FilterData(int filterUrl, int filterAlpha) {
	    	this.mFilterUrl = filterUrl;
	    	this.mFilterAlpha = filterAlpha;
		}

	}




	public static class ColorMsg
	{
		public FilterRes m_filterData;
		public int m_filterAlpha = 100;
		public ArrayList<VideoResMgr.AdjustData> m_adjustData = new ArrayList<>();
	}



}
