package cn.poco.beautify;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.MaterialMgr2.MgrUtils;
import cn.poco.MaterialMgr2.ThemeItemInfo;
import cn.poco.Text.ColorChangeLayout1;
import cn.poco.Text.ColorItemInfo;
import cn.poco.Text.Painter;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.image.filter;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.FilterRes;
import cn.poco.resource.FilterResMgr2;
import cn.poco.resource.LightEffectRes;
import cn.poco.resource.LightEffectResMgr2;
import cn.poco.resource.MyLogoRes;
import cn.poco.resource.MyLogoResMgr;
import cn.poco.resource.ResourceMgr;
import cn.poco.resource.TextRes;
import cn.poco.resource.TextResMgr2;
import cn.poco.resource.ThemeRes;
import cn.poco.resource.ThemeResMgr2;
import cn.poco.resource.database.ResourseDatabase;
import cn.poco.setting.LanguagePage;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv100.SimpleBtnList100;

import static cn.poco.interphoto2.R.string.Exposure;
import static cn.poco.interphoto2.R.string.Saturation;
import static cn.poco.interphoto2.R.string.hue;
import static cn.poco.interphoto2.R.string.idea;

public class BeautifyResMgr
{
	public static final int ADD_TEXT = -0xff0001;
	public static final int MY_TEXT = -0xff0002;
	private static String TAG  = "BeautifyResMgr";

	public static ArrayList<DragListItemInfo> getTextRess(Context context, int typeID)
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
			ArrayList<TextRes> textRes = TextResMgr2.getInstance().GetLocalResArr(db, typeID);
			HashMap<Integer, Boolean> ids = new HashMap<>();
			String classifyFlag = "";
			if(typeID == 1)
			{
				classifyFlag = "water";
			}
			else if(typeID == 2)
			{
				classifyFlag = "attitude";
			}
			if(textRes != null)
			{
				int size = textRes.size();
				for(int i = 0; i < size; i++)
				{
					TextRes res = textRes.get(i);
					info = new DragListItemInfo();
					info.m_uri = res.m_id;
					info.m_name = res.m_name;
					info.m_logo = res.m_thumb;
					info.m_head = res.m_headImg;
					info.text_bg_color_over = 0xb2ffc433;
					info.m_ex = res;
					int flag = MgrUtils.checkDownloadState(res);
					if(flag == 0 && res.m_type == TextRes.TYPE_NETWORK_URL)
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
					if(!themeRes.m_isHide && themeRes.m_textIDArr != null && themeRes.m_textIDArr.length > 0)
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
						ArrayList<TextRes> textRes1 = TextResMgr2.getInstance().GetResArr(db, themeRes.m_textIDArr);
						int state = MgrUtils.checkGroupDownloadState(textRes1, themeRes.m_textIDArr, null);
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
								Log.i(TAG, "getTextRess: " + themeRes.m_recommend);
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



			//我的
			info = new DragListItemInfo();
			info.m_uri = DragListItemInfo.URI_MYTEXT;
			info.m_logo = "mytext";
			out.add(0, info);
			out.add(0, GetDownloadMoreItem(context));

			ResourseDatabase.getInstance(context).closeDatabase();
			return out;
		}
	}

	public static ArrayList<SimpleBtnList100.Item> getTextMyItems(Context context)
	{
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<SimpleBtnList100.Item>();
		int left_margin = ShareData.PxToDpi_xhdpi(30);
		int viewSize = ShareData.PxToDpi_xhdpi(128);

		//添加水印
		SimpleListItem item = new SimpleListItem(context);
		item.m_uri = ADD_TEXT;
		item.img_res_out = item.img_res_over = R.drawable.beauty_text_my_add_icon;
		item.title_color_out = item.title_color_over = 0xff000000;
		item.def_title_size = 10;
		item.isVertical = false;
		item.title_bg_res_out = item.title_bg_res_over = R.drawable.beauty_text_my_add_bg;
		item.m_title = context.getResources().getString(R.string.Add);
		item.m_showTitle = true;
		item.item_width = item.item_height = viewSize;
		item.left_margin = left_margin;
		item.top_margin = ShareData.PxToDpi_xhdpi(8);
		item.InitDatas();
		out.add(item);

		MySimpleListItem item1;
		ArrayList<MyLogoRes> logoRess = MyLogoResMgr.getInstance().GetMyLogoResArr();
		if(logoRess != null)
		{
			int size = logoRess.size();
			for(int i = 0; i < size; i++)
			{
				MyLogoRes res = logoRess.get(i);
				if (res.mShouldDelete) {
					continue;
				}

				item1 = new MySimpleListItem(context);
				item1.m_uri = res.m_userId + "_" + res.m_id;
				item1.title_color_out = 0xffffffff;
				item1.title_color_over = 0xff000000;
				item1.m_title = res.m_name;
				item1.def_title_size = 10;
				item1.item_width = item.item_height = viewSize;
				item1.left_margin = left_margin;
				if(i == 0)
				{
					item1.left_margin = left_margin + ShareData.PxToDpi_xhdpi(8);
				}
				item1.title_bg_res_out = R.drawable.beauty_text_my_item_bg_out;
				item1.title_bg_res_over = R.drawable.beauty_text_my_item_bg_over;
				item1.delete_res = R.drawable.text_my_delete_btn;
				item1.m_ex = res;
				item1.InitDatas();
				out.add(item1);
			}
		}
		return out;
	}

	public static ArrayList<DragListItemInfo> getLightEffectRess(Context context)
	{
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{
			String ver = SysConfig.GetAppVer(context);
			boolean showLock = true;
			if(ver.endsWith("_r1") && !TagMgr.CheckTag(context, LanguagePage.ENGLISH_TAGVALUE))
			{
				showLock = false;
			}
			ArrayList<DragListItemInfo> out = new ArrayList<DragListItemInfo>();
			DragListItemInfo info;
			LightEffectRes res;
			HashMap<Integer, Boolean> ids = new HashMap<>();
			SQLiteDatabase db = ResourseDatabase.getInstance(context).openDatabase();
			ArrayList<LightEffectRes> lightRess = LightEffectResMgr2.getInstance().GetLocalResArr(db);
			if(lightRess != null && lightRess.size() > 0)
			{
				int size = lightRess.size();
				for(int i = 0; i < size; i ++)
				{
					info = new DragListItemInfo();
					res = lightRess.get(i);
					info.m_uri = res.m_id;
					info.m_name = res.m_name;
					info.m_logo = res.m_thumb;
					info.m_head = res.m_headImg;
					if(res.m_color != null && res.m_color.length() > 0)
					{
						info.text_bg_color_out = Painter.GetColor(res.m_color, 0x99);
					}
					else
					{
						info.text_bg_color_out = 0x99000000;
					}
					info.text_bg_color_over = info.text_bg_color_out;
					info.m_ex = res;
					int flag = MgrUtils.checkDownloadState(res);
					if (flag == 0 && res.m_type == LightEffectRes.TYPE_NETWORK_URL)
					{
						info.m_style = DragListItemInfo.Style.NEED_DOWNLOAD;
					}
					else if (flag == 1 || flag == 2)
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
					if(!themeRes.m_isHide && themeRes.m_lightEffectIDArr != null && themeRes.m_lightEffectIDArr.length > 0)
					{
						if(themeRes.m_type == BaseRes.TYPE_NETWORK_URL)
						{
							DownloadMgr.getInstance().DownloadRes(themeRes, null);
							continue;
						}
						ArrayList<LightEffectRes> effectRes = LightEffectResMgr2.getInstance().GetResArr(db, themeRes.m_lightEffectIDArr);
						int state = MgrUtils.checkGroupDownloadState(effectRes, themeRes.m_lightEffectIDArr, null);
						if(state != ThemeItemInfo.COMPLETE)
						{
							info = new DragListItemInfo();
							info.m_uri = uri;
							info.m_name = themeRes.m_name;
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
							uri ++;
						}
					}

				}
			}

			info = new DragListItemInfo();
			info.m_name = context.getResources().getString(R.string.oriphoto);
			info.m_uri = DragListItemInfo.URI_ORIGIN;
			info.text_bg_color_out = 0x99000000;
			info.text_bg_color_over = 0x99000000;
			out.add(0, info);

			out.add(0, GetDownloadMoreItem(context));

			ResourseDatabase.getInstance(context).closeDatabase();
			return out;
		}
	}

	public static ArrayList<DragListItemInfo> GetFilterRess(Context context, boolean isVideo)
	{
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{

			ArrayList<DragListItemInfo> out = new ArrayList<DragListItemInfo>();
			DragListItemInfo info;
			FilterRes res;
			SQLiteDatabase db = ResourseDatabase.getInstance(context).openDatabase();
			ArrayList<FilterRes> resArr = FilterResMgr2.getInstance().GetLocalResArr(db, isVideo);
			HashMap<Integer, Boolean> ids = new HashMap<>();
			if(resArr != null && resArr.size() > 0)
			{
				int size = resArr.size();
				for(int i = 0; i < size; i ++)
				{
					info = MakeFilterDragItemInfo(resArr.get(i));
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
							uri ++;
						}
					}

				}
			}


			info = new DragListItemInfo();
			info.m_name = context.getResources().getString(R.string.oriphoto);
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

	public static DragListItemInfo MakeFilterDragItemInfo(FilterRes res)
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
		if (flag == 0 && res.m_type == LightEffectRes.TYPE_NETWORK_URL)
		{
			info.m_style = DragListItemInfo.Style.NEED_DOWNLOAD;
		}
		else if (flag == 1 || flag == 2)
		{
			info.m_style = DragListItemInfo.Style.LOADING;
		}
		else
		{
			info.m_style = DragListItemInfo.Style.NORMAL;
		}
		if(info.m_style == DragListItemInfo.Style.NORMAL) {
			info.m_canDrag = true;
			info.m_canDrop = true;
		}
		return info;
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
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<SimpleBtnList100.Item>();
		int width = ShareData.m_screenWidth / 2;

		SimpleListItem item = new SimpleListItem(context);
		item.m_uri = 0;
		item.m_ex = BeautyColorType.FILTER;
		item.img_res_out = R.drawable.beauty_filter_btn_over;
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

	public static ArrayList<SimpleBtnList100.Item> getColorAdjustItems(Context context)
	{
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<SimpleBtnList100.Item>();
		int width = (int)(ShareData.m_screenWidth / 4.5);
		AdjustData data;

		SimpleListItem item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.BRIGHTNESS.GetValue();
		item.m_title = context.getResources().getString(Exposure);
		item.m_tjID = R.integer.美化_滤镜_调整_亮度;
		item.m_shenceTjID = R.string.滤镜细节调整_亮度;
		item.m_shenceTjStr = "exposure";
		item.img_res_over = R.drawable.beauty_color_brightness_over;
		item.img_res_out = R.drawable.beauty_color_brightness_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.BRIGHTNESS, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.CONTRAST.GetValue();
		item.m_title = context.getResources().getString(R.string.Contrast);
		item.m_tjID = R.integer.美化_滤镜_调整_对比度;
		item.m_shenceTjID = R.string.滤镜细节调整_对比度;
		item.m_shenceTjStr = "contrast";
		item.img_res_over = R.drawable.beauty_color_contrast_over;
		item.img_res_out = R.drawable.beauty_color_contrast_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.CONTRAST, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.BETTER.GetValue();
		item.m_title = context.getResources().getString(R.string.Clarity);
		item.m_tjID = R.integer.美化_滤镜_调整_增强;
		item.m_shenceTjStr = "clarity";
		item.m_shenceTjID = R.string.滤镜细节调整_增强;
		item.img_res_over = R.drawable.beauty_color_better_over;
		item.img_res_out = R.drawable.beauty_color_better_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.BETTER, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
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
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.SATURABILITY.GetValue();
		item.m_title = context.getResources().getString(Saturation);
		item.m_tjID = R.integer.美化_滤镜_调整_饱和度;
		item.m_shenceTjStr = "saturation";
		item.m_shenceTjID = R.string.滤镜细节调整_饱和度;
		item.img_res_over = R.drawable.beauty_color_saturability_over;
		item.img_res_out = R.drawable.beauty_color_saturability_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.SATURABILITY, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.SHARPEN.GetValue();
		item.m_title = context.getResources().getString(R.string.Sharpness);
		item.m_tjID = R.integer.美化_滤镜_调整_锐化;
		item.m_shenceTjStr = "sharpness";
		item.m_shenceTjID = R.string.滤镜细节调整_锐度;
		item.img_res_over = R.drawable.beauty_color_sharpen_over;
		item.img_res_out = R.drawable.beauty_color_sharpen_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.SHARPEN, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.TEMPERATURE.GetValue();
		item.m_title = context.getResources().getString(R.string.Temperature);
		item.m_tjID = R.integer.美化_滤镜_调整_色温;
		item.m_shenceTjStr = "temperature";
		item.m_shenceTjID = R.string.滤镜细节调整_色温;
		item.img_res_over = R.drawable.beauty_color_temperature_over;
		item.img_res_out = R.drawable.beauty_color_temperature_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.TEMPERATURE, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.HUE.GetValue();
		item.m_title = context.getResources().getString(R.string.Tone);
		item.m_tjID = R.integer.美化_滤镜_调整_色调;
		item.m_shenceTjStr = "tone";
		item.m_shenceTjID = R.string.滤镜细节调整_色调;
		item.img_res_over = R.drawable.beauty_color_hue_over;
		item.img_res_out = R.drawable.beauty_color_hue_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.HUE, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.HIGHTLIGHT.GetValue();
		item.m_title = context.getResources().getString(R.string.Highlight);
		item.m_tjID = R.integer.美化_滤镜_调整_高光;
		item.m_shenceTjStr = "highlight";
		item.m_shenceTjID = R.string.滤镜细节调整_高光减淡;
		item.img_res_over = R.drawable.beauty_color_hightlight_over;
		item.img_res_out = R.drawable.beauty_color_hightlight_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.HIGHTLIGHT, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.SHADE.GetValue();
		item.m_title = context.getResources().getString(R.string.Shadow);
		item.m_tjID = R.integer.美化_滤镜_调整_阴影;
		item.m_shenceTjStr = "shadow";
		item.m_shenceTjID = R.string.滤镜细节调整_阴影补偿;
		item.img_res_over = R.drawable.beauty_color_shade_over;
		item.img_res_out = R.drawable.beauty_color_shade_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.SHADE, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.BEAUTY.GetValue();
		item.m_title = context.getResources().getString(R.string.Beauty);
		item.m_tjID = R.integer.美化_滤镜_调整_肤色;
		item.m_shenceTjStr = "skin";
		item.m_shenceTjID = R.string.滤镜细节调整_肤色调整;
		item.img_res_over = R.drawable.beauty_color_beauty_over;
		item.img_res_out = R.drawable.beauty_color_beauty_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.BEAUTY, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.DARKCORNER.GetValue();
		item.m_title = context.getResources().getString(R.string.Vignette);
		item.m_tjID = R.integer.美化_滤镜_调整_暗角;
		item.m_shenceTjStr = "vignette";
		item.m_shenceTjID = R.string.滤镜细节调整_暗角;
		item.img_res_over = R.drawable.beauty_color_vignetting_over;
		item.img_res_out = R.drawable.beauty_color_vignetting_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.DARKCORNER, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.PARTICAL.GetValue();
		item.m_title = context.getResources().getString(R.string.Grain);
		item.m_tjID = R.integer.美化_滤镜_调整_颗粒;
		item.m_shenceTjStr = "grain";
		item.m_shenceTjID = R.string.滤镜细节调整_颗粒;
		item.img_res_over = R.drawable.beauty_color_partical_over;
		item.img_res_out = R.drawable.beauty_color_partical_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.PARTICAL, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.FADE.GetValue();
		item.m_title = context.getResources().getString(R.string.Fade);
		item.m_tjID = R.integer.美化_滤镜_调整_褪色;
		item.m_shenceTjStr = "fade";
		item.m_shenceTjID = R.string.滤镜细节调整_褪色;
		item.img_res_over = R.drawable.beauty_color_fade_over;
		item.img_res_out = R.drawable.beauty_color_fade_out;
		item.item_width = width;
		item.InitDatas();
		data = new AdjustData(BeautyAdjustType.FADE, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);


		return out;
	}

	public static ArrayList<SimpleBtnList100.Item> getLightAdjustItems(Context context)
	{
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<SimpleBtnList100.Item>();
		int width = (int)(ShareData.m_screenWidth / 5f);
		LightAdjustData data;

		SimpleListItem item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.BRIGHTNESS.GetValue();
		item.m_title = context.getResources().getString(Exposure);
		item.m_tjID = R.integer.美化_光效_调整_亮度;
		item.m_shenceTjID = R.string.光效细节调整_亮度;
		item.m_shenceTjStr = "lexposure";
		item.img_res_over = R.drawable.beauty_color_brightness_over;
		item.img_res_out = R.drawable.beauty_color_brightness_out;
		item.item_width = width;
		item.InitDatas();
		data = new LightAdjustData(item.m_shenceTjStr, LightAdjustData.EXPOSURE, 0, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.CONTRAST.GetValue();
		item.m_title = context.getResources().getString(Saturation);
		item.m_tjID = R.integer.美化_光效_调整_饱和度;
		item.m_shenceTjID = R.string.光效细节调整_饱和度;
		item.m_shenceTjStr = "lsaturation";
		item.img_res_over = R.drawable.beauty_color_saturability_over;
		item.img_res_out = R.drawable.beauty_color_saturability_out;
		item.item_width = width;
		item.InitDatas();
		data = new LightAdjustData(item.m_shenceTjStr, LightAdjustData.SATURATION, 0, 6);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.BETTER.GetValue();
		item.m_title = context.getResources().getString(hue);
		item.m_tjID = R.integer.美化_光效_调整_色相;
		item.m_shenceTjID = R.string.光效细节调整_色相;
		item.m_shenceTjStr = "lhue";
		item.img_res_over = R.drawable.beauty_light_adjust_sexiang_hover;
		item.img_res_out = R.drawable.beauty_light_adjust_sexiang_normol;
		item.item_width = width;
		item.InitDatas();
		data = new LightAdjustData(item.m_shenceTjStr, LightAdjustData.HUE, 0, 6);
		data.m_tjId = item.m_shenceTjStr;
		item.m_ex = data;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = BeautyAdjustType.CURVE.GetValue();
		item.m_title = context.getResources().getString(R.string.flip_h);
		item.m_tjID = R.integer.美化_光效_水平翻转;
		item.m_shenceTjStr = "shuiping";
		item.m_shenceTjID = R.string.光效细节调整_水平翻转;
		item.img_res_over = R.drawable.beauty_reversal_h_logo_hover;
		item.img_res_out = R.drawable.beauty_reversal_h_logo;
		item.item_width = width;
		item.InitDatas();
		data = new LightAdjustData(item.m_shenceTjStr, LightAdjustData.FLIP_H, 0, 0);
		item.m_ex = data;
		data.m_tjId = item.m_shenceTjStr;
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = 6;
		item.m_title = context.getResources().getString(R.string.flip_v);
		item.m_tjID = R.integer.美化_光效_垂直翻转;
		item.m_shenceTjStr = "chuizhi";
		item.m_shenceTjID = R.string.光效细节调整_垂直翻转;
		item.img_res_over = R.drawable.beauty_reversal_v_logo_hover;
		item.img_res_out = R.drawable.beauty_reversal_v_logo;
		item.item_width = width;
		item.InitDatas();
		data = new LightAdjustData(item.m_shenceTjStr, LightAdjustData.FLIP_V, 0, 0);
		data.m_tjId = item.m_shenceTjStr;
		item.m_ex = data;
		out.add(item);

		return out;
	}

	public static ArrayList<SimpleBtnList100.Item> getClipBtnList(Context context)
	{
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<SimpleBtnList100.Item>();
		int[] outRess = new int[]{R.drawable.beauty_clip_free_out, R.drawable.beauty_clip_3_2_out, R.drawable.beauty_clip_4_3_out, R.drawable.beauty_clip_16_9_out, R.drawable.beauty_clip_1_1_out, R.drawable.beauty_clip_2_3_out, R.drawable.beauty_clip_3_4_out, R.drawable.beauty_clip_9_16_out};
		int[] overRess = new int[]{R.drawable.beauty_clip_free_over, R.drawable.beauty_clip_3_2_over, R.drawable.beauty_clip_4_3_over, R.drawable.beauty_clip_16_9_over, R.drawable.beauty_clip_1_1_over, R.drawable.beauty_clip_2_3_over, R.drawable.beauty_clip_3_4_over, R.drawable.beauty_clip_9_16_over};
		float[] scale = new float[]{-1, 3 / 2f, 4 / 3f, 16 / 9f,  1.0f, 2 / 3f, 3 / 4f, 9 / 16f};
		int[] tjIds = new int[]{R.integer.美化_剪裁_free, R.integer.美化_剪裁_3_2, R.integer.美化_剪裁_4_3, R.integer.美化_剪裁_16_9, R.integer.美化_剪裁_1_1, R.integer.美化_剪裁_2_3, R.integer.美化_剪裁_3_4, R.integer.美化_剪裁_9_16};
		int[] shenceTjs = new int[]{R.string.裁剪_free, R.string.照片裁剪_3_2, R.string.照片裁剪_4_3, R.string.照片裁剪_16_9, R.string.照片裁剪_1_1, R.string.照片裁剪_2_3, R.string.照片裁剪_3_4, R.string.照片裁剪_9_16};
		String[] shenceTjStr = new String[]{"free", "3_2", "4_3", "16_9", "1_1", "2_3", "3_4", "9_16"};
		String[] titles = new String[]{"free", "3:2", "4:3", "16:9", "1:1", "2:3", "3:4", "9:16"};
		int width = ShareData.m_screenWidth / overRess.length;
		SimpleListItem item;
		for(int i = 0; i < outRess.length; i++)
		{
			item = new SimpleListItem(context);
			item.m_uri = i;
			item.m_ex = scale[i];
			item.m_tjID = tjIds[i];
			item.m_shenceTjID = shenceTjs[i];
			item.m_shenceTjStr = shenceTjStr[i];
			item.img_res_over = overRess[i];
			item.img_res_out = outRess[i];
			item.item_width = width;
			item.isVertical  = true;
			item.m_showTitle = true;
			item.def_title_size = 12;
			item.title_color_out = 0xff999999;
			item.title_color_over = 0xffffc433;
			item.m_title = titles[i];
			item.InitDatas();
			out.add(item);
		}

		return out;
	}

	public static ArrayList<SimpleBtnList100.Item> getMixModeList(Context context)
	{
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<SimpleBtnList100.Item>();
		int outRes = R.drawable.beauty_clip_free_out;
		int overRes = R.drawable.beauty_clip_free_over;
		int[] ids = new int[]{8, 9, 20, 30, 33, 38, 41, 45, 46, 59, 61, 26, 29, 34};
		String[] titles = new String[]{"颜色加深", "颜色减淡", "变暗", "高光", "变亮", "正片叠底", "叠加", "滤色"
		,"柔光", "亮光", "线性减淡", "差值", "排除", "线性光"};
		int width = ShareData.m_screenWidth / 5;
		SimpleListItem item;
		for(int i = 0; i < ids.length; i++)
		{
			item = new SimpleListItem(context);
			item.m_uri = i;
			item.m_ex = ids[i];
			item.img_res_over = overRes;
			item.img_res_out = outRes;
			item.item_width = width;
			item.isVertical  = true;
			item.m_showTitle = true;
			item.def_title_size = 12;
			item.title_color_out = 0xff999999;
			item.title_color_over = 0xffffc433;
			item.m_title = titles[i];
			item.InitDatas();
			out.add(item);
		}

		return out;
	}

	public static ArrayList<SimpleBtnList100.Item> getTextClassifyItems(Context context)
	{
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<SimpleBtnList100.Item>();
		int width = ShareData.m_screenWidth / 2;
		int height = ShareData.PxToDpi_xhdpi(180);
		//添加水印
		SimpleListItem item = new SimpleListItem(context);
		item.m_uri = 1;
		item.m_showTitle = true;
		item.title_color_out = 0xffffffff;
		item.title_color_over = 0xffffc433;
		item.m_title = context.getResources().getString(R.string.Watermark);
		item.item_width = width;
		item.def_title_size = 14;
		item.item_height = height;
		item.m_ex = null;
		item.m_tjID = R.integer.美化_文字_水印;
		item.InitDatas();
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = 2;
		item.m_showTitle = true;
		item.title_color_out = 0xffffffff;
		item.title_color_over = 0xffffc433;
		item.m_title = context.getResources().getString(R.string.Attitude);
		item.def_title_size = 14;
		item.item_width = width;
		item.item_height = height;
		item.m_ex = null;
		item.m_tjID = R.integer.美化_文字_态度;
		item.InitDatas();
		out.add(item);

		return out;
	}

	public static ArrayList<ColorItemInfo> getColorRes1()
	{
		ArrayList<ColorItemInfo> out = new ArrayList<ColorItemInfo>();
		int id = 0;
		ColorItemInfo info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "000000";
		info.m_color = new String[]{"000000", "000000"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_000000;
		out.add(info);

		info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "ffffff";
		info.m_color = new String[]{"ffffff", "ffffff"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_ffffff;
		out.add(info);

		info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "808080";
		info.m_color = new String[]{"808080", "808080"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_808080;
		out.add(info);

		info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "65514a";
		info.m_color = new String[]{"65514a", "65514a"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_65514a;
		out.add(info);

		info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "133159";
		info.m_color = new String[]{"133159", "133159"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_133159;
		out.add(info);

		info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "5a9498";
		info.m_color = new String[]{"5a9498", "5a9498"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_5a9498;
		out.add(info);

		info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "8e3b4f";
		info.m_color = new String[]{"8e3b4f", "8e3b4f"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_8e3b4f;
		out.add(info);

		info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "e67976";
		info.m_color = new String[]{"e67976", "e67976"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_e67976;
		out.add(info);

		info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "93818d";
		info.m_color = new String[]{"93818d", "93818d"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_93818d;
		out.add(info);

		info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "c07c2b";
		info.m_color = new String[]{"c07c2b", "c07c2b"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_c07c2b;
		out.add(info);

		info = new ColorItemInfo();
		info.m_id = id++;
		info.m_showColor = "cbb480";
		info.m_color = new String[]{"cbb480", "cbb480"};
		info.m_cids = new int[]{1000, 0};
		info.m_normalRes = R.drawable.photofactory_color_chooser_out;
		info.m_selectedRes = R.drawable.photofactory_color_chooser_over;
		info.m_transitionRes = R.drawable.photofactory_color_chooser_transition_cbb480;
		out.add(info);

		return out;
	}

	public static ArrayList<ColorChangeLayout1.ItemInfo> getColorRes()
	{
		ArrayList<ColorChangeLayout1.ItemInfo> out = new ArrayList<ColorChangeLayout1.ItemInfo>();
		int id = 0;
		ColorChangeLayout1.ItemInfo info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xff000000;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xffffffff;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xff808080;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xff65514a;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xff93818d;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xffcbb480;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xff133159;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xff5a9498;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xff852026;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xff8e3b4f;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xffe67976;
		out.add(info);

		info = new ColorChangeLayout1.ItemInfo();
		info.m_uri = id++;
		info.m_color = 0xffc07c2b;
		out.add(info);

		return out;
	}

	public static Bitmap MakeBkBmp(Bitmap bmp, int outW, int outH)
	{
		return MakeBkBmp(bmp, outW, outH, 0x60FFFFFF, 0x90FFFFFF);
	}

	public static Bitmap MakeBkBmp(Bitmap bmp, int outW, int outH, int fillColor, int glassColor)
	{
		Bitmap out = null;
		if(bmp != null)
		{
			out = MakeBmp.CreateBitmap(bmp, outW / 2, outH / 2, (float)outW / (float)outH, 0, Config.ARGB_8888);
			//out = MakeBmp.CreateFixBitmap(bmp, outW, outH, MakeBmp.POS_CENTER, 0, Config.ARGB_8888);
			//filter.largeRblurOpacity(out, 100, 0);
			filter.fakeGlass(out, glassColor);
			Canvas canvas = new Canvas(out);
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			canvas.drawColor(fillColor);
		}

		return out;
	}

	public static class ColorData
	{
		public int m_uri;
		public int m_alpha = 100;

		public int m_tjID = -1;
		public Object m_thumb;
		public String m_author = "创建者";	//滤镜作者
		public String m_authorInfo = "POCO特约摄影师";	//滤镜作者介绍
		public String m_colorIntro = "承载着拟真胶片的精髓，拥有自然的色调、微妙的色偏和轻微的暗调，非常实用于室内、肖像和食物摄影。该滤镜免费使用";	//滤镜介绍
		public int m_sampleId = 1;
		public String m_shareUrl;
		public String m_shareTitle;

		public ColorData(int uri, int alpha)
		{
			m_uri = uri;
			m_alpha = alpha;
		}

		public Object Clone()
		{
			return new ColorData(m_uri, m_alpha);
		}
	}

	public static class AdjustData
	{
		public String m_tjId;
		public BeautyAdjustType m_type;
		public float m_value = 0;

		public AdjustData(BeautyAdjustType type, float value)
		{
			m_type = type;
			m_value = value;
		}
	}

	public static class LightAdjustData
	{
		public final static int EXPOSURE = 0;
		public final static int SATURATION = 1;
		public final static int HUE = 2;
		public final static int FLIP_V = 3;
		public final static int FLIP_H = 4;

		public int m_type= 0;
		public int m_value = 0;
		public int m_progress = 0;
		public String m_tjId;

		public LightAdjustData(String tjId, int type,int value,int progress)
		{
			this.m_tjId = tjId;
			this.m_type = type;
			this.m_value = value;
			this.m_progress = progress;
		}
	}

}
