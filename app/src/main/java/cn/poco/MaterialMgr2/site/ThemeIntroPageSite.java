package cn.poco.MaterialMgr2.site;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;

import cn.poco.MaterialMgr2.ThemeIntroPage;
import cn.poco.PhotoPicker.PhotoPickerPage;
import cn.poco.album2.site.AlbumSite3;
import cn.poco.beautify.BeautifyModuleType;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.home.site.HomePageSite;
import cn.poco.resource.ResType;
import cn.poco.video.site.VideoAlbumSite;

/**
 * Created by admin on 2016/9/9.
 */
public class ThemeIntroPageSite extends BaseSite
{
	public static final String TYPE = "material_type"; //打开哪个页面(int)
	public static final String ID = "material_id"; //打开哪个id的素材(int)
	public static  final String VIDEO_WATERMARK_TYPE = "watermark_type";//视频水印类型
	private static final String TAG = "ThemeIntroPageSite";

	public ThemeIntroPageSite()
	{
		super(SiteID.THEME_INTRO_PAGE);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new ThemeIntroPage(context, this);
	}

	public void OnBack(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Back(context, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	/**
	 * @param params type ResType
	 *               id int
	 */
	public void OnResourceUse(HashMap<String, Object> params,Context context)
	{
		ResType type = (ResType)params.get("type");
		int id = (Integer)params.get("id");
		int video_watermark_type = 0 ;
		if (params.get("watermark") != null){
			 video_watermark_type = (int) params.get("watermark");
		}

		HashMap<String, Object> temp;
		switch(type)
		{
			case FILTER:
				temp = new HashMap<>();
				temp.put("mode", PhotoPickerPage.MODE_SINGLE);
				temp.put(TYPE, BeautifyModuleType.FILTER.GetValue());
				temp.put(ID, id);
				Log.i(TAG, "OnResourceUse: "  + temp.put(ID, id) );
				temp.put("hide_multi_choose", true);
				MyFramework.SITE_BackAndOpen(context, HomePageSite.class, AlbumSite3.class, temp, Framework2.ANIM_TRANSLATION_LEFT);
				break;
			case TEXT:
				temp = new HashMap<>();
				temp.put("mode", PhotoPickerPage.MODE_SINGLE);
				temp.put(TYPE, BeautifyModuleType.TEXT.GetValue());
				temp.put(ID, id);
				temp.put("hide_multi_choose", true);
				MyFramework.SITE_BackAndOpen(context, HomePageSite.class, AlbumSite3.class, temp, Framework2.ANIM_TRANSLATION_LEFT);
				break;
			case LIGHT_EFFECT:
				temp = new HashMap<>();
				temp.put("mode", PhotoPickerPage.MODE_SINGLE);
				temp.put(TYPE, BeautifyModuleType.EFFECT.GetValue());
				temp.put(ID, id);
				temp.put("hide_multi_choose", true);
				MyFramework.SITE_BackAndOpen(context, HomePageSite.class, AlbumSite3.class, temp, Framework2.ANIM_TRANSLATION_LEFT);
				break;
			case AUDIO_TEXT:
				temp  = new HashMap<>();
				temp.put(ID,id);
				temp.put(VIDEO_WATERMARK_TYPE,video_watermark_type);
				temp.put("watermark_id",id);
				MyFramework.SITE_BackAndOpen(context,HomePageSite.class, VideoAlbumSite.class, temp, Framework2.ANIM_TRANSLATION_LEFT);
				break;
			default:
				break;
		}
	}
}
