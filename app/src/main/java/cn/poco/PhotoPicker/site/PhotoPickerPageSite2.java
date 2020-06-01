package cn.poco.PhotoPicker.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * 文字水印-我的-从相册添加
 */
public class PhotoPickerPageSite2 extends PhotoPickerPageSite
{

	public void OnSelPhoto(Context context, HashMap<String, Object> params)
	{
		HashMap<String, Object> temp = new HashMap<String, Object>();
		temp.put("imgs", MakeRotationImg((String[])params.get("imgs")));
		MyFramework.SITE_ClosePopup(context, temp, Framework2.ANIM_TRANSLATION_LEFT);
	}

}
