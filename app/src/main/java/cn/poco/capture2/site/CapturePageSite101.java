package cn.poco.capture2.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.site.activity.MainActivitySite;

/**
 * Created by: fwc
 * Date: 2017/10/10
 */
public class CapturePageSite101 extends CapturePageSite {

	@Override
	public void openProcessVideo(Context context, HashMap<String, Object> params) {

		//activity级跳转交给activity的site
		if(context instanceof PocoCamera)
		{
			MainActivitySite site = ((PocoCamera)context).getActivitySite();
			if(site != null)
			{
				site.openProcessVideo(context, params);
			}
		}
	}
}
