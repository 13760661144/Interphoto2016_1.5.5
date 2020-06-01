package cn.poco.Text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.poco.graphics.ShapeEx;
import cn.poco.svg.SVG;
import cn.poco.svg.SVGParser;
import cn.poco.zip.Zip;

/**
 * 可编辑文字
 */
public class ShapeEx3 extends ShapeEx
{
	private static final String TAG = "shapeEx3";
	protected int m_cid;
	public ShapeEx5 m_parent;
	protected MyTextInfo m_textInfo;
	protected Map<Integer, RectF> m_textRects = new HashMap<>();
	protected Map<Integer, RectF> m_imageRects = new HashMap<>();
	protected Paint m_paint;
	protected Context m_context;
	public RectF m_bmpRect = new RectF(0, 0, 0, 0);
	protected PorterDuffColorFilter temp_color_filter;

	protected int m_curSelIndex = -1;
	protected String m_updateText;
	private int m_color = -1;
	private int m_shadowAlpha = -1;

	public ShapeEx3(Context context, ShapeEx5 parent)
	{
		m_context = context;
		m_parent = parent;
		m_paint = new Paint();
	}

	public void draw(Canvas canvas)
	{
		drawBmps(canvas);
		drawTexts(canvas);
	}

	public void SetTextInfo(MyTextInfo info)
	{
		m_textInfo = info;
	}

	public MyTextInfo GetTextInfo()
	{
		return m_textInfo;
	}

	public void SetCID(int cid)
	{
		m_cid = cid;
	}

	public int getCID()
	{
		return m_cid;
	}

	protected void drawBmps(Canvas canvas)
	{
		if(m_textInfo != null)
		{
			ArrayList<ImgInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String zipPath = m_textInfo.image_zip;
				int size = imgInfos.size();
				for(int i = 0; i < size; i ++)
				{
					ImgInfo info = imgInfos.get(i);
					Object bmp;
					if(info != null && info.m_pic != null)
					{
						bmp = info.m_pic;
					}
					else
					{
						bmp = GetBmpByInfo(info, zipPath);
					}
					int w = 0, h = 0;
					if(bmp instanceof Bitmap)
					{
						w = ((Bitmap)bmp).getWidth();
						h = ((Bitmap)bmp).getHeight();
					}
					else if(bmp instanceof Picture)
					{
						w = ((Picture)bmp).getWidth();
						h = ((Picture)bmp).getHeight();
					}
					if(bmp != null)
					{
						RectF rect = GetBmpRect(info, w, h);
						if(bmp instanceof Bitmap)
						{
							m_paint.reset();
							m_paint.setAntiAlias(true);
							m_paint.setFilterBitmap(true);
							if(info.paint_color > 0)
							{
								int color = info.paint_color;
								//版本兼容
								if(android.os.Build.VERSION.SDK_INT >= 17)
								{
									temp_color_filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
								}
								m_paint.setColorFilter(temp_color_filter);
							}
							canvas.drawBitmap((Bitmap)bmp, rect.left, rect.top, m_paint);
						}
						else if(bmp instanceof Picture)
						{
							canvas.drawPicture((Picture)bmp, rect);
						}
					}
				}
			}
		}
	}

	protected void drawTexts(Canvas canvas)
	{
		if(m_textInfo != null)
		{
			ArrayList<FontInfo> fontInfo = m_textInfo.m_fontsInfo;
			if(fontInfo != null && fontInfo.size() > 0)
			{
				int size = fontInfo.size();
				String text;
				for(int i = 0; i < size; i ++)
				{
					FontInfo info = fontInfo.get(i);
					final String key = info.m_con;
					if("diy".equals(key))
					{
						text = getEditableText(info, i);
						if(info.m_typeSet.equals(MyTextInfo.HORIZONTAL))
						{
							drawHorizontalText(canvas, text, info);
						}
						else if(info.m_typeSet.equals(MyTextInfo.VERTICAL))
						{
							drawVerticalText(canvas, text, info);
						}
					}
				}
			}
		}
	}

	public void drawRect(Canvas canvas, int selText, Matrix matrix, boolean show)
	{
		m_curSelIndex = selText;
		if(m_textInfo != null && m_textRects != null)
		{
			ArrayList<FontInfo> info = m_textInfo.m_fontsInfo;
			if(info != null && info.size() > 0)
			{
				int size = info.size();
				for(int i = 0; i < size; i ++)
				{
					boolean isVer = info.get(i).m_typeSet.equals(MyTextInfo.VERTICAL);
					float[] dsts = getRectPoints(m_textRects.get(i), matrix);
					if((m_curSelIndex < 0 || m_curSelIndex >= size))
					{
						drawTextRect(canvas, dsts, isVer, show);
					}
					else if(m_curSelIndex == i)
					{
						drawTextRect(canvas, dsts, isVer, show);
						break;
					}
				}
			}
		}
	}

	public float[] getRectPoints(RectF rect, Matrix matrix)
	{
		if (rect != null && matrix != null)
		{
			float[] dsts = new float[8];
			float[] src = new float[8];
			//画选中框
			src[0] = rect.left;
			src[1] = rect.top;
			src[2] = rect.right;
			src[3] = rect.top;
			src[4] = rect.right;
			src[5] = rect.bottom;
			src[6] = rect.left;
			src[7] = rect.bottom;
			matrix.mapPoints(dsts, src);
			return dsts;
		}
		return null;
	}

	protected void drawTextRect(Canvas canvas, float[] temp_dst, boolean isVertical, boolean show)
	{
//		Log.v(TAG, "m_curAnimCount: " + curCount);
		if (temp_dst != null && show)
		{
			m_paint.reset();
			m_paint.setStyle(Paint.Style.STROKE);
			m_paint.setColor(0xffffc433);
			m_paint.setStrokeCap(Paint.Cap.SQUARE);
			m_paint.setStrokeJoin(Paint.Join.MITER);
			m_paint.setStrokeWidth(2);
			if (isVertical)
			{
				canvas.drawLine(temp_dst[4], temp_dst[5], temp_dst[6], temp_dst[7], m_paint);
			} else
			{
				canvas.drawLine(temp_dst[2], temp_dst[3], temp_dst[4], temp_dst[5], m_paint);
			}

			PathEffect effects = new DashPathEffect(new float[]{3, 5, 3, 5}, 1);
			m_paint.setPathEffect(effects);
			canvas.drawLine(temp_dst[0], temp_dst[1], temp_dst[2], temp_dst[3], m_paint);
			if (isVertical)
			{
				canvas.drawLine(temp_dst[2], temp_dst[3], temp_dst[4], temp_dst[5], m_paint);
				canvas.drawLine(temp_dst[4], temp_dst[5] + 4, temp_dst[6], temp_dst[7] + 4, m_paint);
				canvas.drawLine(temp_dst[6], temp_dst[7], temp_dst[0], temp_dst[1], m_paint);
			}
			else
			{
				canvas.drawLine(temp_dst[2] + 4, temp_dst[3], temp_dst[4] + 4, temp_dst[5], m_paint);
				canvas.drawLine(temp_dst[4], temp_dst[5], temp_dst[6], temp_dst[7], m_paint);
				canvas.drawLine(temp_dst[6], temp_dst[7], temp_dst[0], temp_dst[1], m_paint);
			}
		}
	}

	protected void drawHorizontalText(Canvas canvas, String text, FontInfo info)
	{
		info.m_showText = text;
		Painter.getFontPaint(m_context, info, m_paint);	//获取画笔
		float height = m_paint.descent() - m_paint.ascent();
		int offsetY = 0;
		int offsetX = 0;
		int newOffsetY = 0;
		int newOffsetX = 0;
		float spaceX = info.m_wordspace;
		float spaceY = info.m_verticalspacing;
		String lineString = "";
		float distanceY = 0.0f;
		float distanceX = 0.0f;
		String maxString = "";

		String[] tempStrings = text.split("[$]");	//回车
		ArrayList<String> stringsArr = new ArrayList<String>();

		for(int i = 0; i < tempStrings.length; i++)
		{
			if(tempStrings[i].length() <= info.m_maxNum)
				stringsArr.add(tempStrings[i]);
			else
			{
				String tempStr = tempStrings[i];
				while(tempStr.length() > info.m_maxNum)
				{
					stringsArr.add(tempStr.substring(0, info.m_maxNum));
					tempStr = tempStr.substring(info.m_maxNum, tempStr.length());
				}
				if(tempStr != null && ! tempStr.equals(""))
				{
					stringsArr.add(tempStr);
				}
			}
		}

		String[] strings = new String[stringsArr.size()];
		stringsArr.toArray(strings);
		int length = strings.length > info.m_maxLine ? info.m_maxLine : strings.length;
		for(int i = 0; i < length; i ++)
		{
			maxString = m_paint.measureText(maxString) > m_paint.measureText(strings[i]) ? maxString : strings[i];
		}
		offsetY = computeOffsetYForMultipleLine(info.m_pos, (int)info.m_offsetY, info.m_verticalspacing, length, m_paint);
		for(int i = 0; i < length; i ++)
		{
			newOffsetY = (int)(offsetY + distanceY);
			lineString = strings[i];
			offsetX = computeOffsetXForMutilpleLine(info.m_pos, info.m_align, maxString, lineString, (int)spaceX, (int)info.m_offsetX, m_paint);
			distanceX = 0.0f;
			int lastWidth = 0;
			int curWidth = 0;
			int strLen = strings[i].length();
			for(int j = 0; j < strLen; j ++)
			{
				String str = String.valueOf(lineString.charAt(j));
				curWidth = (int)(m_paint.measureText(str) + 0.5);
				distanceX += getFixDistance(info.m_pos, lastWidth, curWidth);
				newOffsetX = (int)(offsetX + distanceX);
				RectF src = ComputeRect(str, info.m_pos, newOffsetX, newOffsetY, m_paint, true);
				canvas.drawText(str, src.left, src.top, m_paint);
				lastWidth = curWidth;
				distanceX += spaceX;
			}
			distanceY += height + spaceY;
		}
	}

	protected void drawVerticalText(Canvas canvas, String text, FontInfo info)
	{
		info.m_showText = text;
		Painter.getFontPaint(m_context, info, m_paint);	//获取画笔
		float height = m_paint.descent() - m_paint.ascent();	//文字的高度
		int offsetY = 0;
		int offsetX = (int)info.m_offsetX;
		int newOffsetY = 0;
		int newOffsetX = offsetX;
		float distanceY = 0.0f;
		int len = text.length();
		int line = info.m_maxLine > len ? len : info.m_maxLine;

		offsetY = computeOffsetYForMultipleLine(info.m_pos, (int)info.m_offsetY, info.m_verticalspacing, line, m_paint);

		for(int j = 0; j < line; j ++)
		{
			String str = text.charAt(j) + "";
			if(str.equals("[$]"))
			{
				str = " ";
			}
			newOffsetY = (int)(offsetY + distanceY);
			RectF src = ComputeRect(str, info.m_pos, newOffsetX, newOffsetY, m_paint, true);
			canvas.drawText(str, src.left, src.top, m_paint);
			distanceY += height + info.m_verticalspacing;
		}
	}

	protected float getFixDistance(String align, int lastWidth, int curWidth)
	{
		float distanceX = 0f;
		if(align.equals(MyTextInfo.ALIGN_TOP_LEFT)
				|| align.equals(MyTextInfo.ALIGN_CENTER_LEFT)
				|| align.equals(MyTextInfo.ALIGN_BOTTOM_LEFT))
		{
			distanceX = lastWidth;
		}
		else if(align.equals(MyTextInfo.ALIGN_TOP_CENTER)
				|| align.equals(MyTextInfo.ALIGN_CENTER)
				|| align.equals(MyTextInfo.ALIGN_BOTTOM_CENTER))
		{
			distanceX = lastWidth / 2f + curWidth / 2f;
		}
		else
		{
			distanceX = curWidth;
		}
		return distanceX;
	}

	public void UpdateText(String text, int curText)
	{
		m_updateText = text;
		m_curSelIndex = curText;
		GetShapeInfo(m_textInfo);
	}

	public int GetCurColor()
	{
		if(m_color == -1)
		{
			if(m_textInfo.m_fontsInfo != null)
			{
				for(FontInfo info: m_textInfo.m_fontsInfo)
				{
					m_color = Painter.SetColorAlpha(info.baseAlpha, info.m_fontColor);
					return m_color;
				}
			}
			if(m_textInfo.m_imgInfo != null)
			{
				for(ImgInfo info: m_textInfo.m_imgInfo)
				{
					m_color = Painter.SetColorAlpha(info.baseAlpha, info.paint_color);
					return m_color;
				}
			}
		}
		return m_color;
	}

	public void resetColorAndAlpha()
	{
		m_shadowAlpha = -1;
		m_color = -1;
	}

	public int GetCurShadowAlpha()
	{
		if(m_shadowAlpha == -1)
		{
			if(m_textInfo.m_fontsInfo != null)
			{
				for(FontInfo info: m_textInfo.m_fontsInfo)
				{
					m_shadowAlpha = Painter.GetAlpha(info.m_shadowColor);
					return m_shadowAlpha;
				}
			}
			if(m_textInfo.m_imgInfo != null)
			{
				for(ImgInfo info: m_textInfo.m_imgInfo)
				{
					m_shadowAlpha = Painter.GetAlpha(info.m_shadowColor);
					return m_shadowAlpha;
				}
			}
		}
		return m_shadowAlpha;
	}

	public int UpdateColor(int color, boolean selOnly)
	{
		if(m_textInfo.m_fontsInfo != null)
		{
			for(FontInfo info: m_textInfo.m_fontsInfo)
			{
				if(!selOnly && info.m_ncColor == 1)
				{
					m_color = info.m_fontColor;
				}
				else
				{
					info.m_fontColor = Painter.GetColorWithAlphaUnchanged(info.m_fontColor, color);
					m_color = info.m_fontColor;
				}
			}
		}
		if(m_textInfo.m_imgInfo != null)
		{
			String zipPath = m_textInfo.image_zip;
			for(ImgInfo info: m_textInfo.m_imgInfo)
			{
				if(!selOnly && info.m_ncColor == 1)
				{
					m_color = info.paint_color;
				}
				else
				{
					info.paint_color = Painter.GetColorWithAlphaUnchanged(info.paint_color, color);
					m_color = info.paint_color;
					GetBmpByInfo(info, zipPath);
				}
			}
		}
		return m_color;
	}

	public int UpdateAlpha(int alpha, int parentAlpha, boolean selOnly)
	{
		if(m_textInfo.m_fontsInfo != null)
		{
			for(FontInfo info: m_textInfo.m_fontsInfo)
			{
				if(selOnly)
				{
					info.baseAlpha = alpha;
				}
				info.m_fontColor = Painter.SetColorAlpha(info.baseAlpha, parentAlpha, info.m_fontColor);
				int orgAlpha = Painter.GetAlpha(info.m_fontColor);
				info.m_shadowColor = Painter.SetColorAlpha(info.m_shadowAlpha, orgAlpha, info.m_shadowColor);
				m_color = info.m_fontColor;
			}
		}
		if(m_textInfo.m_imgInfo != null)
		{
			String zipPath = m_textInfo.image_zip;
			for(ImgInfo info: m_textInfo.m_imgInfo)
			{
				if(selOnly)
				{
					info.baseAlpha = alpha;
				}
				info.paint_color = Painter.SetColorAlpha(info.baseAlpha, parentAlpha, info.paint_color);
				int orgAlpha = Painter.GetAlpha(info.paint_color);
				info.m_shadowColor = Painter.SetColorAlpha(info.m_shadowAlpha, orgAlpha, info.m_shadowColor);
				GetBmpByInfo(info, zipPath);
				m_color = info.paint_color;
			}
		}
		return m_color;
	}

	public void UpdateShadowAlpha(int alpha)
	{
		m_shadowAlpha = alpha;
		if(m_textInfo.m_fontsInfo != null)
		{
			for(FontInfo info: m_textInfo.m_fontsInfo)
			{
				info.m_shadowAlpha = alpha;
				int orgAlpha = Painter.GetAlpha(info.m_fontColor);
				info.m_shadowColor = Painter.SetColorAlpha(alpha, orgAlpha, info.m_shadowColor);
			}
		}
		if(m_textInfo.m_imgInfo != null)
		{
			String zipPath = m_textInfo.image_zip;
			for(ImgInfo info: m_textInfo.m_imgInfo)
			{
				info.m_shadowAlpha = alpha;
				int orgAlpha = Painter.GetAlpha(info.paint_color);
				info.m_shadowColor = Painter.SetColorAlpha(alpha, orgAlpha, info.m_shadowColor);
				GetBmpByInfo(info, zipPath);
			}
		}
	}

	public void ClearFontPaint()
	{
		if(m_textInfo.m_fontsInfo != null)
		{
			for(FontInfo info: m_textInfo.m_fontsInfo)
			{
				info.m_paint = null;
			}
		}
	}

	/**
	 * 必须先调用这个接口计算出文字的宽高，才能调用draw()方法画文字
	 */
	public synchronized void GetShapeInfo(MyTextInfo info)
	{
		m_textInfo = info;
		m_bmpRect.set(0, 0, 0, 0);

		GetBmpRects();
		GetTextRects();

		for(Iterator it = m_textRects.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry e = (Map.Entry) it.next();
			RectF rect = (RectF)e.getValue();
			m_bmpRect.union(rect);
		}

		for(Iterator it = m_imageRects.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry e = (Map.Entry) it.next();
			RectF rect = (RectF)e.getValue();
			m_bmpRect.union(rect);
		}

		m_w = (int)m_bmpRect.width();
		m_h = (int)m_bmpRect.height();
		m_centerY = m_h / 2f;
		m_centerX = m_w / 2f;
	}

	protected void GetBmpRects()
	{
		if(m_textInfo != null)
		{
			m_imageRects.clear();
			ArrayList<ImgInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String zipPath = m_textInfo.image_zip;
				int size = imgInfos.size();
				for(int i = 0; i < size; i ++)
				{
					ImgInfo info = imgInfos.get(i);
					Object obj = GetBmpByInfo(info, zipPath);
					int w = 0, h = 0;
					if(obj instanceof Bitmap)
					{
						w = ((Bitmap)obj).getWidth();
						h = ((Bitmap)obj).getHeight();
					}
					else if(obj instanceof Picture)
					{
						w = ((Picture)obj).getWidth();
						h = ((Picture)obj).getHeight();
					}
					RectF rect = GetBmpRect(info, w, h);
					m_imageRects.put(i, rect);
				}
			}
		}
	}

	protected Object GetBmpByInfo(ImgInfo imgInfo, String zipPath)
	{
		Object out = null;
		if(imgInfo == null || zipPath == null || zipPath.length() == 0)
			return out;
		byte[] fileStr = Zip.GetFileStream(m_context, zipPath, imgInfo.m_imgFile);
		if(fileStr != null && fileStr.length > 0)
		{
			if(imgInfo.m_imgFile.endsWith(".svg"))
			{
				int replaceColor = imgInfo.paint_color;
				HashMap<String, Object> shadow = new HashMap<>();
				shadow.put("shadow_c", imgInfo.m_shadowColor);
				shadow.put("shadow_x", imgInfo.m_shadowX);
				shadow.put("shadow_y", imgInfo.m_shadowY);
				shadow.put("shadow_r", imgInfo.m_shadowRadius);
				SVG svg = SVGParser.getSVGFromString(new String(fileStr), null, replaceColor, shadow);
				out = svg.getPicture();
			}
			else
			{
				out = BitmapFactory.decodeByteArray(fileStr, 0, fileStr.length);
			}
			if(imgInfo.m_pic != null && imgInfo.m_pic instanceof Bitmap)
			{
				((Bitmap)imgInfo.m_pic).recycle();
				imgInfo.m_pic = null;
			}
			imgInfo.m_pic = out;
		}
		return out;
	}

	protected RectF GetBmpRect(ImgInfo imgInfo, int bmpW, int bmpH)
	{
		RectF rect = new RectF();
		if(imgInfo == null)
			return rect;
		float originX = 0;
		float originY = 0;
		int canvasW = 1024;
		int canvasH = 1024;

		if(MyTextInfo.ALIGN_TOP_LEFT.equals(imgInfo.m_pos))
		{
			originX = 0;
			originY = 0;
		}
		else if(MyTextInfo.ALIGN_TOP_CENTER.equals(imgInfo.m_pos))
		{
			originX = (canvasW - bmpW) / 2f;
			originY = 0;
		}
		else if(MyTextInfo.ALIGN_TOP_RIGHT.equals(imgInfo.m_pos))
		{
			originX = canvasW - bmpW;
			originY = 0;
		}
		else if(MyTextInfo.ALIGN_CENTER.equals(imgInfo.m_pos))
		{
			originX = (canvasW - bmpW) / 2f;
			originY = (canvasH - bmpH) / 2f;
		}
		else if(MyTextInfo.ALIGN_CENTER_LEFT.equals(imgInfo.m_pos))
		{
			originX = 0;
			originY = (canvasH - bmpH) / 2f;
		}
		else if(MyTextInfo.ALIGN_CENTER_RIGHT.equals(imgInfo.m_pos))
		{
			originX = canvasW - bmpW;
			originY = 0;
		}
		else if(MyTextInfo.ALIGN_BOTTOM_LEFT.equals(imgInfo.m_pos))
		{
			originX = 0;
			originY = canvasH - bmpH;
		}
		else if(MyTextInfo.ALIGN_BOTTOM_CENTER.equals(imgInfo.m_pos))
		{
			originX = (canvasW - bmpW) / 2f;
			originY = canvasH - bmpH;
		}
		else if(MyTextInfo.ALIGN_BOTTOM_RIGHT.equals(imgInfo.m_pos))
		{
			originX = canvasW - bmpW;
			originY = canvasH - bmpH;
		}
		rect.left = originX + imgInfo.m_offsetX;
		rect.top = originY + imgInfo.m_offsetY;
		rect.right = rect.left + bmpW;
		rect.bottom = rect.top + bmpH;
		return rect;
	}

	protected void GetTextRects()
	{
		if(m_textInfo != null)
		{
			m_textRects.clear();
			ArrayList<FontInfo> fontInfo = m_textInfo.m_fontsInfo;
			if(fontInfo != null && fontInfo.size() > 0)
			{
				int size = fontInfo.size();
				String text;
				for(int i = 0; i < size; i ++)
				{
					FontInfo info = fontInfo.get(i);
					final String key = info.m_con;
					if("diy".equals(key))
					{
						text = getEditableText(info, i);
						RectF rect = GetTextRect(text, info);
						m_textRects.put(i, rect);
					}
				}
			}
		}
	}

	protected String getEditableText(FontInfo info, int index)
	{
		String text = null;

		if(m_curSelIndex == index && m_updateText != null)
		{
			text = m_updateText;
			if(text.equals(""))
			{
				text = " ";
			}
			return text;
		}
		else if(info != null)
		{
			text = info.m_showText;
		}
		if(text == null)
			text = info.m_wenan.get(0 + "");
		return text;
	}

	protected RectF GetTextRect(String text, FontInfo info)
	{
		RectF rect = new RectF();
		if(info.m_typeSet.equals(MyTextInfo.HORIZONTAL))
		{
			rect = GetHorizontalRect(text, info);
		}
		else if(info.m_typeSet.equals(MyTextInfo.VERTICAL))
		{
			rect = GetVerticalRect(text, info);
		}
		return rect;
	}

	protected RectF GetHorizontalRect(String text, FontInfo info)
	{
		RectF rect = new RectF(1024, 1024, 0, 0);
		info.m_showText = text;
		Painter.getFontPaint(m_context, info, m_paint);	//获取画笔
		float height = m_paint.descent() - m_paint.ascent();
		int offsetY = 0;
		int offsetX = 0;
		int newOffsetY = offsetY;
		int newOffsetX = offsetX;
		float spaceX = info.m_wordspace;
		float spaceY = info.m_verticalspacing;
		String lineString = "";
		float distanceY = 0.0f;
		float distanceX = 0.0f;
		String maxString = "";

		String[] tempStrings = text.split("[$]");	//回车
		ArrayList<String> stringsArr = new ArrayList<String>();

		for(int i = 0; i < tempStrings.length; i++)
		{
			if(tempStrings[i].length() <= info.m_maxNum)
				stringsArr.add(tempStrings[i]);
			else
			{
				String tempStr = tempStrings[i];
				while(tempStr.length() > info.m_maxNum)
				{
					stringsArr.add(tempStr.substring(0, info.m_maxNum));
					tempStr = tempStr.substring(info.m_maxNum, tempStr.length());
				}
				if(tempStr != null && ! tempStr.equals(""))
				{
					stringsArr.add(tempStr);
				}
			}
		}

		String[] strings = new String[stringsArr.size()];
		stringsArr.toArray(strings);
		int length = strings.length > info.m_maxLine ? info.m_maxLine : strings.length;
		for(int i = 0; i < length; i ++)
		{
			maxString = m_paint.measureText(maxString) > m_paint.measureText(strings[i]) ? maxString : strings[i];
		}
		offsetY = computeOffsetYForMultipleLine(info.m_pos, (int)info.m_offsetY, info.m_verticalspacing, length, m_paint);
		for(int i = 0; i < length; i ++)
		{
			newOffsetY = (int)(offsetY + distanceY);
			lineString = strings[i];
			offsetX = computeOffsetXForMutilpleLine(info.m_pos, info.m_align, maxString, lineString, (int)spaceX, (int)info.m_offsetX, m_paint);
			distanceX = 0.0f;
			int lastWidth = 0;
			int curWidth = 0;
			int strLen = strings[i].length();
			for(int j = 0; j < strLen; j ++)
			{
				String str = String.valueOf(lineString.charAt(j));
				curWidth = (int)(m_paint.measureText(str) + 0.5);
				distanceX += getFixDistance(info.m_pos, lastWidth, curWidth);
				newOffsetX = (int)(offsetX + distanceX);
				RectF src = ComputeRect(str, info.m_pos, newOffsetX, newOffsetY, m_paint, false);
				rect.union(src);
				lastWidth = curWidth;
				distanceX += spaceX;
			}
			distanceY += height + spaceY;
		}

		return rect;
	}

	/**
	 * 计算X的偏移
	 * @param align 对齐方式
	 * @param pos 多行的对齐方式
	 * @param text	当前行显示的文字
	 * @param spaceX	文字间的间距
	 * @param offsetX	文字的偏移
	 * @param paint	画笔
	 * @return
	 */
	private int computeOffsetXForMutilpleLine(String pos, String align, String maxString, String text, int spaceX, int offsetX, Paint paint)
	{
		int len = text.length();
		float width = 0f;
		width = paint.measureText(text) + spaceX * (len - 1);

		float maxWidth = 0f;
		if(maxString == null || maxString.equals(""))
		{
			maxWidth = 0f;
		}
		else
		{
			int len1 = maxString.length();
			maxWidth += paint.measureText(maxString) + spaceX * (len1 - 1);
		}
		if(pos.equals(MyTextInfo.ALIGN_CENTER)
				|| pos.equals(MyTextInfo.ALIGN_TOP_CENTER)
				|| pos.equals(MyTextInfo.ALIGN_BOTTOM_CENTER))
		{
			offsetX = (int)(offsetX - width / 2f);
			if(maxWidth != 0f)
			{
				if(align.equals("left"))
				{
					offsetX = (int)(offsetX - (maxWidth - width) / 2f + 0.5f);
				}
				else if(align.equals("right"))
				{
					offsetX = (int)(offsetX + (maxWidth - width) / 2f + 0.5f);
				}
			}
		}
		else if(pos.equals(MyTextInfo.ALIGN_BOTTOM_RIGHT)
				|| pos.equals(MyTextInfo.ALIGN_CENTER_RIGHT)
				|| pos.equals(MyTextInfo.ALIGN_TOP_RIGHT))
		{
			offsetX = (int)(offsetX - width);
			if(maxWidth != 0f)
			{
				if(align.equals("left"))
				{
					offsetX = (int)(offsetX - (maxWidth - width) + 0.5f);
				}
				else if(align.equals("center"))
				{
					offsetX = (int)(offsetX - (maxWidth - width) / 2f + 0.5f);
				}
			}
		}
		else
		{
			if(maxWidth != 0f)
			{
				if(align.equals("center"))
				{
					offsetX = (int)(offsetX + (maxWidth - width) / 2f + 0.5f);
				}
				else if(align.equals("right"))
				{
					offsetX = (int)(offsetX + (maxWidth - width) + 0.5f);
				}
			}
		}
		return offsetX;
	}

	protected RectF GetVerticalRect(String text, FontInfo info)
	{
		RectF rect = new RectF(1024, 1024, 0, 0);
		info.m_showText = text;
		Painter.getFontPaint(m_context, info, m_paint);	//获取画笔
		float height = m_paint.descent() - m_paint.ascent();	//文字的高度
		int offsetY = 0;
		int offsetX = (int)info.m_offsetX;
		int newOffsetY = 0;
		int newOffsetX = offsetX;
		float distanceY = 0.0f;
		int len = text.length();
		int line = info.m_maxLine > len ? len : info.m_maxLine;

		offsetY = computeOffsetYForMultipleLine(info.m_pos, (int)info.m_offsetY, info.m_verticalspacing, line, m_paint);

		for(int j = 0; j < line; j ++)
		{
			String str = text.charAt(j) + "";
			if(str.equals("[$]"))
			{
				str = " ";
			}
			newOffsetY = (int)(offsetY + distanceY);
			RectF src = ComputeRect(str, info.m_pos, newOffsetX, newOffsetY, m_paint, false);
			rect.union(src);
			distanceY += height + info.m_verticalspacing;
		}

		return rect;
	}

	/**
	 * 计算多行时Y的偏移量
	 * @param align
	 * @param offsetY
	 * @param line
	 * @param paint
	 * @return
	 */
	private int computeOffsetYForMultipleLine (String align, int offsetY, int spaceY, int line, Paint paint)
	{
		float tempHeight = paint.descent() - paint.ascent();
		//第一个文字不做偏移
		float height = tempHeight * (line - 1) + spaceY * (line - 1);
		if(align.equals(MyTextInfo.ALIGN_CENTER) || align.equals(MyTextInfo.ALIGN_CENTER_LEFT) || align.equals(MyTextInfo.ALIGN_CENTER_RIGHT))
		{
			offsetY = (int)(offsetY - height / 2f);
		}
		else if(align.equals(MyTextInfo.ALIGN_BOTTOM_LEFT) || align.equals(MyTextInfo.ALIGN_BOTTOM_CENTER) ||
				align.equals(MyTextInfo.ALIGN_BOTTOM_RIGHT))
		{
			offsetY = (int)(offsetY - height);
		}
		return offsetY;
	}

	private RectF ComputeRect(String text, String align, int offsetX, int offsetY, Paint paint, boolean draw)
	{
		RectF rects = new RectF();
		if (text == null)
		{
			text = "-";
		}
		int space = 0;
		float originX = 0;
		float originY = 0;
		float startX = 0;
		float startY = 0;
		float endX = 0;
		float endY = 0;
		float textWidth = paint.measureText(text);
		float ascent = paint.ascent();
		float descent = paint.descent();
		float textHeight = descent-ascent;
		int canvasW = 1024;
		int canvasH = 1024;

		if (align.equals(MyTextInfo.ALIGN_TOP_LEFT))
		{
			originY = -ascent-space;
			startX = 0;
			startY = 0;
		}
		else if (align.equals(MyTextInfo.ALIGN_TOP_CENTER))
		{
			originX = canvasW / 2f;
			originY = -ascent - space;
			startX = originX - textWidth/2;
			startY = 0;
		}
		else if (align.equals(MyTextInfo.ALIGN_TOP_RIGHT))
		{
			originX = canvasW;
			originY = -ascent - space;
			startX = originX - textWidth;
			startY = 0;
		}
		else if (align.equals(MyTextInfo.ALIGN_CENTER_LEFT))
		{
			originY = (canvasH / 2f - (descent + ascent) / 2f);
			startX = 0;
			startY = originY  + ascent;
		}
		else if (align.equals(MyTextInfo.ALIGN_CENTER))
		{
			originX = canvasW / 2f;
			originY = (canvasH / 2f - (descent + ascent) / 2f);
			startX = originX - textWidth / 2;
			startY = originY + ascent;
		}
		else if (align.equals(MyTextInfo.ALIGN_CENTER_RIGHT))
		{
			originX = canvasW;
			originY = (canvasH / 2f - (descent + ascent) / 2f);
			startX = originX - textWidth;
			startY = originY  + ascent;
		}
		else if (align.equals(MyTextInfo.ALIGN_BOTTOM_LEFT))
		{
			originX = 0;
			float bottom = descent - space;
			originY = canvasH-bottom;
			startX = 0;
			startY = originY  + ascent - space;
		}
		else if (align.equals(MyTextInfo.ALIGN_BOTTOM_RIGHT))
		{
			originX = canvasW;
			float bottom = descent - space;
			originY = canvasH-bottom;
			startX =  originX - textWidth;
			startY = originY  + ascent - space;
		}
		else if (align.equals(MyTextInfo.ALIGN_BOTTOM_CENTER))
		{
			originX = canvasW / 2f;
			float bottom = descent - space;
			originY = canvasH - bottom;
			startX = originX - textWidth/2;
			startY = originY  + ascent - space;
		}
		Paint.FontMetrics fm = paint.getFontMetrics();

		startX = startX + offsetX;
		startY = startY + offsetY;
		endX = startX+textWidth;
		endY = startY+ textHeight;

		if(draw)
		{
			rects.left = originX + offsetX;
			rects.top = originY + offsetY;
			rects.right = endX;
			rects.bottom = endY;
		}
		else
		{
			rects.left = startX;
			rects.top = startY + (fm.top - fm.ascent);
			rects.right = endX;
			rects.bottom = endY - (fm.top - fm.ascent);
		}

		return rects;
	}
}
