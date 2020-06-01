package cn.poco.video.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.capture2.site.CapturePageSite1;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * Created by: fwc
 * Date: 2017/6/8
 * 视频拼接-添加视频
 */
public class VideoAlbumSite3 extends VideoAlbumSite {

//	@Override
//	public void onVideoBeautify(Context context, ArrayList<VideoEntry> videos, int ratio) {
//		HashMap<String, Object> params = new HashMap<>();
//		params.put("videos",videos);
//		params.put("ratio",ratio);
//		MyFramework.SITE_Back(context, params, Framework2.ANIM_TRANSLATION_LEFT);
//	}

	@Override
	public void onVideoSetting(Context context, HashMap<String, Object> params)
	{
		HashMap<String, Object> date = new HashMap<>();
		params.put("videos",params.get("videos"));
		params.put("ratio",params.get("ratio"));
		MyFramework.SITE_Back(context, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void onCamera(Context context, HashMap<String, Object> params)
	{
		MyFramework.SITE_Open(context, CapturePageSite1.class,params,Framework2.ANIM_TRANSLATION_LEFT);
	}
}
