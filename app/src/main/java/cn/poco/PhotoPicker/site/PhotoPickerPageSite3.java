package cn.poco.PhotoPicker.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.beautify.site.BeautifyPageSite;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * 美化-（滤镜、文字。。。）
 */
public class PhotoPickerPageSite3 extends PhotoPickerPageSite
{

	public void OnSelPhoto(Context context, HashMap<String, Object> params)
	{
		HashMap<String, Object> temp = new HashMap<String, Object>();
		temp.putAll(params);
		temp.put("imgs", MakeRotationImg((String[])params.get("imgs")));
		if(m_inParams != null)
		{
			m_inParams.put("is_back", true);
		}
		MyFramework.SITE_Open(context, BeautifyPageSite.class, temp, Framework2.ANIM_TRANSLATION_LEFT);
	}

}
