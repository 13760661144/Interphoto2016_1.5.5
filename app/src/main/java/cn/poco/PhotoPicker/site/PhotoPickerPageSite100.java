package cn.poco.PhotoPicker.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.beautify.site.BeautifyPageSite100;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * 第三方调用
 */
public class PhotoPickerPageSite100 extends PhotoPickerPageSite
{
	@Override
	public void OnSelPhoto(Context context, HashMap<String, Object> params)
	{
		HashMap<String, Object> temp = new HashMap<String, Object>();
		temp.put("imgs", MakeRotationImg((String[])params.get("imgs")));
		temp.put("other_call", true);
		MyFramework.CopyExternalCallParams(m_inParams, temp);
		MyFramework.SITE_Open(context, BeautifyPageSite100.class, temp, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
