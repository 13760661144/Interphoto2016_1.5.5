package cn.poco.PhotoPicker.site;

import android.content.Context;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;

import cn.poco.PhotoPicker.PhotoPickerPage;
import cn.poco.beautify.site.BeautifyPageSite;
import cn.poco.camera.RotationImg2;
import cn.poco.framework.BaseSite;
import cn.poco.framework.FileCacheMgr;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.utils.Utils;

public class PhotoPickerPageSite extends BaseSite
{
	public PhotoPickerPageSite()
	{
		super(SiteID.PICK_PIC);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new PhotoPickerPage(context, this);
	}

	/**
	 * 
	 * @param params
	 *            imgs:String[]选择的图片数组
	 */
	public void OnSelPhoto(Context context, HashMap<String, Object> params)
	{
		HashMap<String, Object> temp = new HashMap<String, Object>();
		temp.put("imgs", MakeRotationImg((String[])params.get("imgs")));
		MyFramework.SITE_Open(context, BeautifyPageSite.class, temp, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public static RotationImg2[] MakeRotationImg(String[] arr)
	{
		RotationImg2[] out = null;

		if(arr != null && arr.length > 0)
		{
			out = new RotationImg2[arr.length];
			for(int i = 0; i < arr.length; i++)
			{
				RotationImg2 temp = Utils.Path2ImgObj(arr[i]);
				temp.m_img = FileCacheMgr.GetLinePath();
				try
				{
					File destFile = new File((String)temp.m_img);
					File tempFile = destFile.getParentFile();
					if(tempFile != null && !tempFile.exists())
					{
						tempFile.mkdirs();
					}
					FileUtils.copyFile(new File(arr[i]), destFile);
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
				out[i] = temp;
			}
		}

		return out;
	}
}
