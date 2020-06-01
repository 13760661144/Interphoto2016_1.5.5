package cn.poco.Text;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.poco.resource.DownloadMgr;
import cn.poco.resource.FontRes;
import cn.poco.video.videotext.text.CharInfo;

/**
 *辅助类，用于生成字体、画笔、位置布局等信息
 */
public class Painter {
	private static final String TAG = "Painter";

	public static Paint getFontPaint(Context c, FontInfo fontInfo, Paint paint)
	{
		paint.reset();
		if(fontInfo.m_paint != null)
		{
			paint.setTypeface(fontInfo.m_paint.getTypeface());
			paint.setColor(fontInfo.m_fontColor);
			paint.setTextSize(fontInfo.m_paint.getTextSize());
			paint.setAntiAlias(true);
			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			paint.setDither(true);
			// 设定阴影(柔边, X 轴位移, Y 轴位移, 阴影颜色)
			paint.setShadowLayer(fontInfo.m_shadowRadius, fontInfo.m_shadowX,
								 fontInfo.m_shadowY, fontInfo.m_shadowColor);
			paint.setTextAlign(fontInfo.m_paint.getTextAlign());
			return paint;
		}
		String fontName = fontInfo.m_font;
		fontName = fontName.trim();
		String fontPath = DownloadMgr.getInstance().FONT_PATH + "/" + fontName;
		File file = new File(fontPath);
		Typeface typeface;
		if(file.exists())
		{
			typeface = readFont(fontPath, fontName);
		}
		else
		{
			fontPath = "fonts/" + fontName;
			typeface = readFont(c, fontPath);
		}
		

		if(typeface != null)
		{
			Log.i(TAG, "typeface:" + typeface);
			paint.setTypeface(typeface);
		}
		String fontSize = fontInfo.m_fontSize + "";
		paint.setColor(fontInfo.m_fontColor);
		float size = Integer.valueOf(fontSize);
		paint.setTextSize(size);
		paint.setAntiAlias(true);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		String align = fontInfo.m_pos;
		if(align.equals(MyTextInfo.ALIGN_TOP_LEFT)
				|| align.equals(MyTextInfo.ALIGN_CENTER_LEFT)
				|| align.equals(MyTextInfo.ALIGN_BOTTOM_LEFT))
		{
			paint.setTextAlign(Paint.Align.LEFT);
		}
		else if(align.equals(MyTextInfo.ALIGN_TOP_CENTER)
				|| align.equals(MyTextInfo.ALIGN_CENTER)
				|| align.equals(MyTextInfo.ALIGN_BOTTOM_CENTER))
		{
			paint.setTextAlign(Paint.Align.CENTER);
		}
		else
		{
			paint.setTextAlign(Paint.Align.RIGHT);
		}
		paint.setShadowLayer(fontInfo.m_shadowRadius, fontInfo.m_shadowX,
							 fontInfo.m_shadowY, fontInfo.m_shadowColor);
		fontInfo.m_paint = new Paint();
		fontInfo.m_paint.setTypeface(paint.getTypeface());
		fontInfo.m_paint.setColor(paint.getColor());
		fontInfo.m_paint.setTextSize(paint.getTextSize());
		fontInfo.m_paint.setAntiAlias(true);
		fontInfo.m_paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		fontInfo.m_paint.setTextAlign(paint.getTextAlign());
		return paint;
	}

	public static Paint getFontPaint(Context c, CharInfo fontInfo, Paint paint)
	{
		paint.reset();
		if(fontInfo.m_paint != null)
		{
			paint.setTypeface(fontInfo.m_paint.getTypeface());
			int alpha = Painter.GetAlpha(fontInfo.m_fontColor);
			int color = Painter.SetColorAlpha(fontInfo.m_animTextAlpha, alpha, fontInfo.m_fontColor);
			paint.setColor(color);
//			Log.i(TAG, "color:" + Integer.toString(Painter.GetAlpha(color), 16));
//			Log.i(TAG, "fontInfo.m_animTextAlpha:" + Integer.toString(fontInfo.m_animTextAlpha, 16));
			paint.setTextSize(fontInfo.m_paint.getTextSize());
			paint.setAntiAlias(true);
			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			paint.setDither(true);
			// 设定阴影(柔边, X 轴位移, Y 轴位移, 阴影颜色)
			int shadow = Painter.SetColorAlpha(fontInfo.m_animTextAlpha, fontInfo.m_shadowAlpha, fontInfo.m_shadowColor);
			paint.setShadowLayer(fontInfo.m_shadowRadius, fontInfo.m_shadowX,
								 fontInfo.m_shadowY, shadow);
			paint.setTextAlign(fontInfo.m_paint.getTextAlign());
			return paint;
		}
		String fontName = fontInfo.m_font;
		fontName = fontName.trim();
		String fontPath = DownloadMgr.getInstance().FONT_PATH + "/" + fontName;
		File file = new File(fontPath);
		Typeface typeface;
		if(file.exists())
		{
			typeface = readFont(fontPath, fontName);
		}
		else
		{
			fontPath = "fonts/" + fontName;
			typeface = readFont(c, fontPath);
		}


		if(typeface != null)
		{
			Log.i(TAG, "typeface:" + typeface);
			paint.setTypeface(typeface);
		}
		String fontSize = fontInfo.m_fontSize + "";
		int alpha = Painter.GetAlpha(fontInfo.m_fontColor);
		int color = Painter.SetColorAlpha(fontInfo.m_animTextAlpha, alpha, fontInfo.m_fontColor);
		paint.setColor(color);
		float size = Integer.valueOf(fontSize);
		paint.setTextSize(size);
		paint.setAntiAlias(true);
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		String align = fontInfo.m_pos;
		if(align.equals(MyTextInfo.ALIGN_TOP_LEFT)
				|| align.equals(MyTextInfo.ALIGN_CENTER_LEFT)
				|| align.equals(MyTextInfo.ALIGN_BOTTOM_LEFT))
		{
			paint.setTextAlign(Paint.Align.LEFT);
		}
		else if(align.equals(MyTextInfo.ALIGN_TOP_CENTER)
				|| align.equals(MyTextInfo.ALIGN_CENTER)
				|| align.equals(MyTextInfo.ALIGN_BOTTOM_CENTER))
		{
			paint.setTextAlign(Paint.Align.CENTER);
		}
		else
		{
			paint.setTextAlign(Paint.Align.RIGHT);
		}
		int shadow = Painter.SetColorAlpha(fontInfo.m_animTextAlpha, fontInfo.m_shadowAlpha, fontInfo.m_shadowColor);
		paint.setShadowLayer(fontInfo.m_shadowRadius, fontInfo.m_shadowX,
							 fontInfo.m_shadowY, shadow);
		fontInfo.m_paint = new Paint();
		fontInfo.m_paint.setTypeface(paint.getTypeface());
		fontInfo.m_paint.setColor(paint.getColor());
		fontInfo.m_paint.setTextSize(paint.getTextSize());
		fontInfo.m_paint.setAntiAlias(true);
		fontInfo.m_paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		fontInfo.m_paint.setTextAlign(paint.getTextAlign());
		return paint;
	}

	// 工具类：在代码中使用dp的方法（因为代码中直接用数字表示的是像素）
	public static int dip2px(Context context, float dip) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}

	//工具类：判断是否是字母或者数字
	public static boolean isNumOrLetters(String str)
	{
		String regEx="^[A-Za-z0-9_]+$";
		Pattern p=Pattern.compile(regEx);
		Matcher m=p.matcher(str);
		return m.matches();
	}
	
	public static boolean isNum(String str)
	{
		String regEx="^[0-9_]+$";
		Pattern p=Pattern.compile(regEx);
		Matcher m=p.matcher(str);
		return m.matches();
	}

	/**
	 *
	 * @param alpha
	 * @param baseAlpha	在文字初始化的透明度基础上改变透明度
	 * @param color
	 * @return
	 */
	public static int SetColorAlpha(int alpha, int baseAlpha, int color)
	{
		int tempAlpha = (int)(alpha / 255f * baseAlpha);
		int out = Color.argb(tempAlpha, Color.red(color), Color.green(color), Color.blue(color));
		return out;
	}

	public static int SetColorAlpha(int alpha, int color)
	{
		return SetColorAlpha(alpha, 255, color);
	}

	public static int GetColorWithAlphaUnchanged(int colorOrg, int changeColor)
	{
		int alpha = Color.alpha(colorOrg);
		int out = Color.argb(alpha, Color.red(changeColor),
							 Color.green(changeColor), Color.blue(changeColor));
		return out;
	}

	public static int GetAlpha(String str_color)
	{
		int out = 0xff;
		if(!TextUtils.isEmpty(str_color))
		{
			int color = GetColor(str_color);
			out = Color.alpha(color);
		}
		return out;
	}

	public static int GetAlpha(int str_color)
	{
		int out = Color.alpha(str_color);
		return out;
	}
	
	public static int GetColor(String str_color, int alpha)
	{
		int color = 0;
		if(!TextUtils.isEmpty(str_color) && str_color.length() >= 6){
			if(str_color.length() != 7 && str_color.length() != 9)
			{
				if(str_color.length() > 8)
				{
					str_color = str_color.substring(str_color.length() - 8, str_color.length());
				}
				str_color = "#" + str_color;
			}
			int tempcolor = Color.parseColor(str_color);
			if(alpha == -1){
				alpha = 0xff;
			}
			color = Color.argb(alpha, Color.red(tempcolor),
							   Color.green(tempcolor), Color.blue(tempcolor));
		}
		return color;
	}

	public static int GetColor(String str_color)
	{
		int color = 0;
		if(!TextUtils.isEmpty(str_color) && str_color.length() >= 6){
			if(str_color.length() != 7 && str_color.length() != 9)
			{
				if(str_color.length() > 8)
				{
					str_color = str_color.substring(str_color.length() - 8, str_color.length());
				}
				str_color = "#" + str_color;
			}
			int tempcolor = Color.parseColor(str_color);
			color = Color.argb(Color.alpha(tempcolor), Color.red(tempcolor),
							   Color.green(tempcolor), Color.blue(tempcolor));
		}
		return color;
	}
	
	/**
	 * 
	 * @param fontPath
	 * @return
	 */
	private static Typeface readFont(String fontPath, String fontName)
	{
		if(fontPath == null || fontName == null || fontName.equals("") || fontName.equals(""))
			return Typeface.DEFAULT;
		if (fontName.equals("LiHei Pro") || fontName.equals("Heiti SC"))
		{
			return Typeface.DEFAULT;
		}
		try
		{
			Log.i(TAG, "read sdcard:" + fontPath);
			File file = new File(fontPath);
			Typeface typeface = Typeface.createFromFile(file);
			return typeface;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private static Typeface readFont(Context context, String fontType)
	{
		if (fontType.equals("LiHei Pro") || fontType.equals("Heiti SC"))
		{
			return Typeface.DEFAULT;
		}
		String fileName = fontType;
		Log.i(TAG, "read assets:"+fileName);
		try
		{
			Typeface typeface = Typeface.createFromAsset(context.getAssets(), fileName);
			return typeface;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean isFontExists(Context context, ArrayList<FontRes> resArr)
	{
		if(resArr == null || resArr.size() == 0)
			return true;
		int size = resArr.size();
		FontRes res;
		Typeface typeface;
		for(int i = 0; i < size; i ++)
		{
			typeface = null;
			res = resArr.get(i);
			String name = DownloadMgr.GetFileName(res.url_res);
			String fontPath = DownloadMgr.getInstance().FONT_PATH + "/" + name;
			File file = new File(fontPath);
//			Log.i(TAG, "fontPath:" + fontPath);
//			Log.i(TAG, "res.url_res:" + res.url_res);
//			Log.i(TAG, "name:" + name);
			if(file.exists())
			{
				typeface = readFont(fontPath, name);
			}
			else
			{
				fontPath = "fonts/" + name;
				typeface = readFont(context, fontPath);
			}
			if(typeface == null)
				return false;
		}
		return true;
	}

}
