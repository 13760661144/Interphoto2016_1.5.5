package cn.poco.Text;

import java.util.ArrayList;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;

import cn.poco.graphics.ShapeEx;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.FontRes;
import cn.poco.tianutils.NetCore2;

public class MyTextInfo
{
	public static final String ALIGN_TOP_LEFT = "a";
    public static final String ALIGN_TOP_CENTER = "b";
    public static final String ALIGN_TOP_RIGHT = "c";
    public static final String ALIGN_CENTER_LEFT = "d";
    public static final String ALIGN_CENTER = "e";
    public static final String ALIGN_CENTER_RIGHT = "f";
    public static final String ALIGN_BOTTOM_LEFT = "g";
    public static final String ALIGN_BOTTOM_CENTER = "h";
    public static final String ALIGN_BOTTOM_RIGHT = "i";
    public static final String NETWORK_DATA = "NetWork_Data";

    public static final String VERTICAL = "竖排";
    public static final String HORIZONTAL = "横排";

	public ArrayList<FontInfo> m_fontsInfo;
	public ArrayList<ImgInfo> m_imgInfo;

	public boolean m_editable = false;
	public String m_pic;	//图片路径
	public String image_zip;	//zip包路径

	public String align;
	public float offsetX;
	public float offsetY;
	public float[] m_matrixValue;
	public int m_alpha = 255;

	public Object m_ex;

	public Object Clone()
	{
		MyTextInfo item = null;

		try
		{
			item = this.getClass().getConstructor().newInstance();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

		if(item != null)
		{
			item.Set(this);
		}

		return item;
	}

	public void Set(MyTextInfo item)
	{
		this.m_fontsInfo = item.m_fontsInfo;
		this.m_imgInfo = item.m_imgInfo;
		this.align = item.align;
		this.offsetX = item.offsetX;
		this.offsetY = item.offsetY;
		this.image_zip = item.image_zip;
		this.m_pic = item.m_pic;
		this.m_editable = item.m_editable;
		this.m_ex = item.m_ex;
	}

	public void ClearUnusedFont(ArrayList<FontRes> fontArr)
	{
		if(fontArr == null)
			return;
		FontRes font;
		String fontName = "";
		int len = fontArr.size();
		for(int i = 0; i < len; i ++)
		{
			int count = 0;
			font = fontArr.get(i);
			if(font.m_res != null && font.m_res instanceof String)
			{
				fontName = DownloadMgr.GetFileName((String)font.m_res);
			}
			else if(!TextUtils.isEmpty(font.url_res))
			{
				fontName = DownloadMgr.GetFileName(font.url_res);
			}
			if(TextUtils.isEmpty(fontName))
			{
				fontArr.remove(i);
				len --;
				i --;
				continue;
			}
			if(m_fontsInfo != null)
			{
				int len1 = m_fontsInfo.size();
				FontInfo info;
				for(int k = 0; k < len1; k ++)
				{
					info = m_fontsInfo.get(k);
					String fontName1 =  NetCore2.FileNameFilter(info.m_font);
					if(!TextUtils.isEmpty(fontName1) && fontName1.equals(fontName))
					{
						count ++;
					}
				}
			}
			if(count == 0)
			{
				fontArr.remove(i);
				len --;
				i --;
				continue;
			}
		}
	}
}

class ImgInfo extends BaseInfo
{
	public Object m_pic;
	public String m_imgFile;
	public int paint_color;
}

class FontInfo extends BaseInfo
{
	public String m_font;	//字体
	public String m_fontSize;	//字体大小
	public int m_fontColor;
	public int m_verticalspacing = 0;	//行间距
	public int m_wordspace = 2;	//列间距
	public String m_typeSet;	//横排、竖排
	public int m_maxLine = -1;	//最大行数
	public int m_maxNum = -1;	//最大显示文字个数
	public String m_align;
	public Map<String, String> m_wenan;	//默认文案
	public String m_showText;	//显示文案

	public Paint m_paint;	//画笔缓存
}

class BaseInfo
{
	public int m_cid;
	public String m_con;	//常规、天气、时间
	public String m_pos;	//对齐方式
	public float m_offsetX;
	public float m_offsetY;
	public int baseAlpha;	//文字的初始alpha，

	public float m_shadowX = 3;
	public float m_shadowY = 3;
	public int m_shadowColor;
	public float m_shadowRadius = 1f;
	public int m_shadowAlpha;
	public int m_ncColor = 0;		//1不变色   0 变色
	public float[] m_matrixValue;
}
