package cn.poco.video.videotext.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.poco.Text.MyTextInfo;
import cn.poco.Text.Painter;
import cn.poco.graphics.ShapeEx;

/**
 * 生成视频上显示的文字
 */

public class VideoText extends ShapeEx
{
	public static final int ANIM_NONE = 0;		//无动画
	public static final int ANIM1 = 20001;		//左向淡入
	public static final int ANIM2 = 20002;		//右向淡入
	/*public static final int ANIM3 = 20003;	//整体淡入
	public static final int ANIM4 = 20004;	//缩入
	public static final int ANIM5 = 20005;	//左向横向移出
	public static final int ANIM6 = 20006;	//右向横向移出
	public static final int ANIM7 = 20007;	//上升淡入
	public static final int ANIM8 = 20008;	//下移淡入
*/
	public static final int BASE_WIDTH = 1024;
	public static final int BASE_HEIGHT = 1024;
	public static final int DISMISS_TIME = 400;

	public float m_animScaleX = 1.0f;
	public float m_animScaleY = 1.0f;
	public float m_animRotate = 0;
	public float m_showAnimDx = 0;
	public float m_showAnimDy = 0;
	public int m_animAlpha = 255;//[0-255]
	public int m_animW;
	public int m_animStartTime = 0;		//整体动画开始时间
	public int m_animStayTime = 0;		//动画停留时间， 用户控制
	public boolean m_editable = true;

	protected Map<Integer, RectF> m_textRects = new HashMap<>();
	protected Map<Integer, RectF> m_imageRects = new HashMap<>();
	protected Paint m_paint;
	protected Context m_context;
	public RectF m_bmpRect = new RectF(0, 0, 0, 0);
	protected RectF m_textRect = new RectF(0, 0, 0, 0);        //文字所在的区域
	protected PorterDuffColorFilter temp_color_filter;
	protected WaterMarkInfo m_textInfo;
	protected Matrix temp_matrix;
	protected float[] temp_dst = new float[8];
	protected float[] temp_src = new float[8];

	protected int m_imageWidth;
	protected int m_imageHeight;
	protected float m_outScale = 1.0f;
	protected float m_outTextScale = 1.0f;

	protected AnimCallback m_animCB;

	protected PorterDuffXfermode m_textAnimMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

	/**
	 * @param context
	 * @param width   水印所在图片的宽
	 * @param height  水印所在图片的高
	 */
	public VideoText(Context context, WaterMarkInfo info, int width, int height)
	{
		m_context = context;
		m_textInfo = info;
		m_paint = new Paint();
		m_imageWidth = width;
		m_imageHeight = height;
		temp_matrix = new Matrix();
	}

	public void setAnimCallback(AnimCallback cb)
	{
		m_animCB = cb;
	}

	public synchronized void setCurTime(int time)
	{
		ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
		if(imgInfos != null && imgInfos.size() > 0)
		{
			String zipPath = m_textInfo.res_path;
			int size = imgInfos.size();
			for(int i = 0; i < size; i ++)
			{
				ImageInfo info = imgInfos.get(i);
				info.SetCurTime(time, zipPath, m_imageWidth, m_imageHeight);
			}
		}

		ArrayList<CharInfo> charInfo = m_textInfo.m_fontsInfo;
		if(charInfo != null && charInfo.size() > 0)
		{
			int size = charInfo.size();
			for(int i = 0; i < size; i ++)
			{
				CharInfo info = charInfo.get(i);
				info.SetCurTime(time, m_textRects.get(i).width(), m_imageWidth, m_imageHeight);
			}
		}
	}

	public void setStartTime(int time)
	{
		m_animStartTime = time;
		ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
		if(imgInfos != null && imgInfos.size() > 0)
		{
			for(ImageInfo info : imgInfos)
			{
				info.setStartTime(time);
			}
		}

		ArrayList<CharInfo> charInfo = m_textInfo.m_fontsInfo;
		if(charInfo != null && charInfo.size() > 0)
		{
			for(CharInfo info : charInfo)
			{
				info.setStartTime(time);
			}
		}
	}

	public void setStayTime(int time)
	{
		m_animStayTime = time;
		ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
		if(imgInfos != null && imgInfos.size() > 0)
		{
			for(ImageInfo info : imgInfos)
			{
				info.setStayTime(time);
			}
		}

		ArrayList<CharInfo> charInfo = m_textInfo.m_fontsInfo;
		if(charInfo != null && charInfo.size() > 0)
		{
			for(CharInfo info : charInfo)
			{
				info.setStayTime(time);
			}
		}
	}

	public Bitmap GetOutBmp(WaterMarkInfo info)
	{
		boolean isShow;
		GetShapeInfo(info);
		float scaleX = (float)m_imageWidth / BASE_WIDTH;
		float scaleY = (float)m_imageHeight / BASE_HEIGHT;
		float scale = scaleX < scaleY ? scaleX : scaleY;
		float rate = m_imageWidth / (float)m_imageHeight;
		float rate9_16 = 9 / 16f;
		if(Math.abs(rate9_16 - rate) < 0.01f || rate == 1)
		{
			scale = scale * 0.8f;
		}
		if(info.effect_id == WaterMarkInfo.EFFECT_6)
		{
			m_outScale = 1;
			m_outTextScale = scale;
		}
		else
		{
			m_outScale = scale;
			m_outTextScale = 1.0f;
		}
		int w = (int)(m_w * m_outScale);
		int h = (int)(m_h * m_outScale);
		if(w == 0 || h == 0)
		{
			return null;
		}
		m_w = w;
		m_h = h;
		m_centerY = m_h / 2f;
		m_centerX = m_w / 2f;

		if(m_animAlpha == 0)
		{
			isShow = false;
		}
		else
		{
			isShow = checkShow();
		}
		if(!isShow)
			return null;
//		System.out.println("scale: " + m_outScale);

		Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(out);
//		canvas.drawColor(0x99ff00ff);
		canvas.save();
		temp_matrix.reset();
		temp_matrix.postTranslate(-m_bmpRect.left, -m_bmpRect.top);
		temp_matrix.postScale(m_outScale, m_outScale);
		canvas.setMatrix(temp_matrix);
		draw(canvas);
		canvas.restore();
		return out;
	}

	public Bitmap UpdateText(String text, int index)
	{
		if(m_textInfo != null && m_textInfo.m_fontsInfo != null
				&& index >= 0 && index < m_textInfo.m_fontsInfo.size())
		{
			CharInfo info = m_textInfo.m_fontsInfo.get(index);
			if(info != null)
			{
				info.m_showText = text;
				return GetOutBmp(m_textInfo);
			}
		}
		return null;
	}

	private boolean checkShow()
	{
		ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
		if(imgInfos != null && imgInfos.size() > 0)
		{
			int size = imgInfos.size();
			for(int i = 0; i < size; i ++)
			{
				ImageInfo info = imgInfos.get(i);
				if(info.m_animAlpha != 0)
					return true;
			}
		}
		ArrayList<CharInfo> charInfo = m_textInfo.m_fontsInfo;
		if(charInfo != null && charInfo.size() > 0)
		{
			int size = charInfo.size();
			for(int i = 0; i < size; i ++)
			{
				CharInfo info = charInfo.get(i);
				if(info.m_animTextAlpha != 0)
					return true;
			}
		}
		return false;
	}

	public void ShowLastFrame(boolean show)
	{
		ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
		if(imgInfos != null && imgInfos.size() > 0)
		{
			int size = imgInfos.size();
			for(int i = 0; i < size; i ++)
			{
				ImageInfo info = imgInfos.get(i);
				info.ShowLastFrame(show);
			}
		}
		ArrayList<CharInfo> charInfo = m_textInfo.m_fontsInfo;
		if(charInfo != null && charInfo.size() > 0)
		{
			int size = charInfo.size();
			for(int i = 0; i < size; i ++)
			{
				CharInfo info = charInfo.get(i);
				info.ShowLastFrame(show);
			}
		}
	}

	public long GetTotalTime()
	{
		long time = 0;
		int temp_time;
		ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
		if(imgInfos != null)
		{
			for(ImageInfo info : imgInfos)
			{
				temp_time = info.GetAnimTime();
				time = temp_time > time ? temp_time : time;
			}
		}

		ArrayList<CharInfo> charInfo = m_textInfo.m_fontsInfo;
		if(charInfo != null && charInfo.size() > 0)
		{
			for(CharInfo info : charInfo)
			{
				temp_time = info.GetAnimTime();
				time = temp_time > time ? temp_time : time;
			}
		}
		return time;
	}

	public long GetTotalTimeNoStayTime()
	{
		long time = 0;
		int temp_time;
		ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
		if(imgInfos != null)
		{
			for(ImageInfo info : imgInfos)
			{
				temp_time = info.GetAnimTimeNoStaytime();
				time = temp_time > time ? temp_time : time;
			}
		}

		ArrayList<CharInfo> charInfo = m_textInfo.m_fontsInfo;
		if(charInfo != null && charInfo.size() > 0)
		{
			for(CharInfo info : charInfo)
			{
				temp_time = info.GetAnimTimeNoStayTime();
				time = temp_time > time ? temp_time : time;
			}
		}
		return time;
	}

	/**
	 * 当水印在视频结尾的时候添加的时候，计算水印延迟开始时间
	 * @param time	-1 不延迟
	 */
	public void SetVideoTime(int time)
	{
		ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
		if(imgInfos != null && imgInfos.size() > 0)
		{
			int size = imgInfos.size();
			for(int i = 0; i < size; i ++)
			{
				ImageInfo info = imgInfos.get(i);
				info.SetVideoTime(time);
			}
		}

		ArrayList<CharInfo> charInfo = m_textInfo.m_fontsInfo;
		if(charInfo != null && charInfo.size() > 0)
		{
			int size = charInfo.size();
			for(int i = 0; i < size; i ++)
			{
				CharInfo info = charInfo.get(i);
				info.SetVideoTime(time);
			}
		}
	}

	/**
	 * 释放图片内存
	 */
	public void ReleaseMem()
	{
		if(m_textInfo != null && m_textInfo.m_imgInfo != null)
		{
			int size = m_textInfo.m_imgInfo.size();
			ImageInfo info;
			for(int i = 0; i < size; i ++)
			{
				info = m_textInfo.m_imgInfo.get(i);
				if(info != null)
				{
					info.m_pic = null;
					if(info.m_animInfo != null)
					{
						for(int k = 0; k < info.m_animInfo.length; k ++)
						{
							info.m_animInfo[k].m_pic = null;
						}
					}
				}
			}
		}
	}

	public Bitmap GetOutBmp(int width, int height, float scale, WaterMarkInfo info)
	{
		m_imageWidth = width;
		m_imageHeight = height;
		return GetOutBmp(info);
	}

	/**
	 * 必须先调用这个接口计算出文字的宽高，才能调用draw()方法画文字
	 */
	public synchronized void GetShapeInfo(WaterMarkInfo info)
	{
		m_textInfo = info;
		m_bmpRect.set(0, 0, 0, 0);

		if(m_textInfo != null)
		{
			switch(m_textInfo.effect_id)
			{
				case WaterMarkInfo.EFFECT_NONE:
				case WaterMarkInfo.EFFECT_5:
				case WaterMarkInfo.EFFECT_6:
				case WaterMarkInfo.EFFECT_7:
				{
					GetTextRects();
					UnionTextRects();
					GetBmpRects();
					break;
				}
				case WaterMarkInfo.EFFECT_1:
				{
					GetTextRects();
					UnionTextRects();
					GetEffect1BmpRects();
					break;
				}
				case WaterMarkInfo.EFFECT_2:
				{
					GetTextRects();
					UnionTextRects();
					GetEffect2BmpRects();
					break;
				}
				case WaterMarkInfo.EFFECT_3:
				{
					GetTextRects();
					UnionTextRects();
					GetEffect3BmpRects();
					break;
				}
				case WaterMarkInfo.EFFECT_4:
				{
					GetTextRects();
					UnionTextRects();
					GetEffect4BmpRects();
					break;
				}
				default:
				{
					GetBmpRects();
					GetTextRects();
					UnionTextRects();
					break;
				}
			}
		}

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

		m_w = (int)(m_bmpRect.width() + 0.5f);
		m_h = (int)(m_bmpRect.height() + 0.5f);
		m_centerY = m_h / 2f;
		m_centerX = m_w / 2f;
	}

	/**
	 * 动画+图片固定长度的
	 * @param canvas
	 */
	public void draw(Canvas canvas)
	{
		if(m_textInfo != null)
		{
			switch(m_textInfo.effect_id)
			{
				case WaterMarkInfo.EFFECT_NONE:
				case WaterMarkInfo.EFFECT_5:
				case WaterMarkInfo.EFFECT_6:
				case WaterMarkInfo.EFFECT_7:
				{
					drawBmps(canvas);
					drawTexts(canvas);
					break;
				}
				case WaterMarkInfo.EFFECT_1:
				{
					drawTexts(canvas);
					drawEffect1Bmp(canvas);
					break;
				}
				case WaterMarkInfo.EFFECT_2:
				{
					drawTexts(canvas);
					drawEffect2Bmp(canvas);
					break;
				}
				case WaterMarkInfo.EFFECT_3:
				{
					drawEffect3Bmp(canvas);
					drawTexts(canvas);
					break;
				}
				case WaterMarkInfo.EFFECT_4:
				{
					drawTexts(canvas);
					drawEffect4Bmp(canvas);
					break;
				}
				default:
					drawBmps(canvas);
					drawTexts(canvas);
					break;
			}
		}
	}

	/**必须先调用GetTextRects()*/
	protected void UnionTextRects()
	{
		m_textRect.set(0, 0, 0, 0);

		for(Iterator it = m_textRects.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry e = (Map.Entry) it.next();
			RectF rect = (RectF)e.getValue();
			m_textRect.union(rect);
		}
	}

	protected void drawEffect1Bmp(Canvas canvas)
	{
		if(m_textInfo != null)
		{
			ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String zipPath = m_textInfo.res_path;
				ImageInfo info = imgInfos.get(0);
				Object bmp;
				if(info != null && info.m_pic != null)
				{
					bmp = info.m_pic;
				}
				else
				{
					bmp = info.GetBmpByInfo(m_context, zipPath);
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

				//左边
				RectF rectLeft = m_imageRects.get(0);

				RectF rectRight = m_imageRects.get(1);

				drawBmp(canvas, bmp, w, h, rectLeft, info);

				drawBmp(canvas, bmp, w, h, rectRight, info);
			}
		}
	}

	protected void drawEffect2Bmp(Canvas canvas)
	{
		if(m_textInfo != null)
		{
			ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String zipPath = m_textInfo.res_path;
				ImageInfo info = imgInfos.get(0);
				Object bmp;
				if(info != null && info.m_pic != null)
				{
					bmp = info.m_pic;
				}
				else
				{
					bmp = info.GetBmpByInfo(m_context, zipPath);
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

				RectF rectTop = m_imageRects.get(0);
				RectF rectBottom = m_imageRects.get(1);
				drawBmp(canvas, bmp, w, h, rectTop, info);
				drawBmp(canvas, bmp, w, h, rectBottom, info);
			}
		}
	}

	protected void drawEffect3Bmp(Canvas canvas)
	{
		drawBmps(canvas);
	}

	protected void drawEffect4Bmp(Canvas canvas)
	{
		if(m_textInfo != null)
		{
			ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String zipPath = m_textInfo.res_path;
				int size = imgInfos.size();
				for(int i = 0; i < size; i ++)
				{
					ImageInfo info = imgInfos.get(i);
					if(info.type_id == ImageInfo.STRETCH_HV)
					{
						RectF rect = m_imageRects.get(i);
						m_paint.reset();
						m_paint.setAntiAlias(true);
						m_paint.setFlags(Paint.ANTI_ALIAS_FLAG);
						m_paint.setDither(true);
						m_paint.setColor(info.paint_color);
						m_paint.setStyle(Paint.Style.STROKE);
						m_paint.setStrokeWidth(10);

						m_paint.setAlpha(info.m_animAlpha);

						temp_matrix.reset();
						temp_matrix.postTranslate(rect.left, rect.top);
						temp_src[0] = 0;
						temp_src[1] = 0;
						temp_src[2] = rect.width();
						temp_src[3] = 0;
						temp_src[4] = rect.width();
						temp_src[5] = rect.height();
						temp_src[6] = 0;
						temp_src[7] = rect.height();
						temp_matrix.mapPoints(temp_dst, temp_src);
						float dx = (temp_dst[0] + temp_dst[4]) / 2;
						float dy = (temp_dst[1] + temp_dst[5]) / 2;

						temp_matrix.reset();
						canvas.save();
						temp_matrix.postTranslate(info.m_showAnimDx, info.m_showAnimDy);
						temp_matrix.postScale(info.m_animScaleX, info.m_animScaleY, dx, dy);
						temp_matrix.postRotate(info.m_animRotate, dx, dy);

						canvas.concat(temp_matrix);

						canvas.drawRect(rect, m_paint);
						canvas.restore();
					}
					else
					{
						Object bmp = null;
						if(info != null && info.m_pic != null)
						{
							bmp = info.m_pic;
						}
						else
						{
							if(info.type_id != ImageInfo.ANIM)
							{
								bmp = info.GetBmpByInfo(m_context, zipPath);
							}
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
						RectF rect = m_imageRects.get(i);
						drawBmp(canvas, bmp, w, h, rect, info);
					}
				}
			}
		}
	}

	protected void drawBmps(Canvas canvas)
	{
		if(m_textInfo != null)
		{
			ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String zipPath = m_textInfo.res_path;
				int size = imgInfos.size();
				for(int i = 0; i < size; i ++)
				{
					ImageInfo info = imgInfos.get(i);
					Object bmp = null;
					if(info != null && info.m_pic != null)
					{
						bmp = info.m_pic;
					}
					else
					{
						if(info.type_id != ImageInfo.ANIM)
						{
							bmp = info.GetBmpByInfo(m_context, zipPath);
						}
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
					RectF rect = m_imageRects.get(i);
					drawBmp(canvas, bmp, w, h, rect, info);
				}
			}
		}
	}

	protected void drawBmp(Canvas canvas, Object bmp, int w, int h, RectF rect, ImageInfo info)
	{
		if(bmp != null)
		{
			canvas.save();
			/*if(bmp instanceof Picture)
			{
				int tempW = (int)(rect.width() + 0.5f);
				int tempH = (int)(rect.height() + 0.5f);
				Bitmap temp = Bitmap.createBitmap(tempW, tempH, Bitmap.Config.ARGB_8888);
				Canvas out = new Canvas(temp);
				RectF rect1 = new RectF(0, 0, rect.width(), rect.height());
				out.drawPicture((Picture)bmp, rect1);
				bmp = temp;
				info.m_pic = temp;
				w = tempW;
				h = tempH;
			}*/
			if(bmp instanceof Bitmap)
			{
				m_paint.reset();
				m_paint.setAntiAlias(true);
				m_paint.setFilterBitmap(true);
				m_paint.setAlpha(info.m_animAlpha);
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
				temp_matrix.reset();
				temp_matrix.postTranslate(rect.left, rect.top);
				float scaleX = rect.width() / (float)w;
				float scaleY = rect.height() / (float)h;
				temp_matrix.postScale(scaleX, scaleY, rect.left, rect.top);
				temp_src[0] = 0;
				temp_src[1] = 0;
				temp_src[2] = w;
				temp_src[3] = 0;
				temp_src[4] = w;
				temp_src[5] = h;
				temp_src[6] = 0;
				temp_src[7] = h;
				temp_matrix.mapPoints(temp_dst, temp_src);
				float dx = (temp_dst[0] + temp_dst[4]) / 2;
				float dy = (temp_dst[1] + temp_dst[5]) / 2;

				temp_matrix.postTranslate(info.m_showAnimDx, info.m_showAnimDy);
				temp_matrix.postScale(info.m_animScaleX, info.m_animScaleY, dx, dy);
				temp_matrix.postRotate(info.m_animRotate, dx, dy);
				canvas.drawBitmap((Bitmap)bmp, temp_matrix, m_paint);
			}
			else if(bmp instanceof Picture)
			{
				canvas.save();
				temp_matrix.reset();
				temp_matrix.postTranslate(rect.left, rect.top);
				float scaleX = rect.width() / (float)w;
				float scaleY = rect.height() / (float)h;
				temp_matrix.postScale(scaleX, scaleY, rect.left, rect.top);
				temp_src[0] = 0;
				temp_src[1] = 0;
				temp_src[2] = w;
				temp_src[3] = 0;
				temp_src[4] = w;
				temp_src[5] = h;
				temp_src[6] = 0;
				temp_src[7] = h;
				temp_matrix.mapPoints(temp_dst, temp_src);
				float dx = (temp_dst[0] + temp_dst[4]) / 2;
				float dy = (temp_dst[1] + temp_dst[5]) / 2;

				temp_matrix.reset();

				temp_matrix.postTranslate(info.m_showAnimDx, info.m_showAnimDy);
				temp_matrix.postScale(info.m_animScaleX, info.m_animScaleY, dx, dy);
				temp_matrix.postRotate(info.m_animRotate, dx, dy);
				canvas.concat(temp_matrix);

				//暂时有个bug，两张图片第二张svg图片的透明度无法拿到
				/*info.paint_color = Painter.SetColorAlpha(info.m_animAlpha, 255, info.paint_color);
				int shadowAlpha = Painter.GetAlpha(info.m_shadowColor);
				info.m_shadowColor = Painter.SetColorAlpha(info.m_animAlpha, shadowAlpha, info.m_shadowColor);
				String zipPath = m_textInfo.res_path;
				bmp = info.GetBmpByInfo(m_context, zipPath);*/

				canvas.drawPicture((Picture)bmp, rect);
				canvas.restore();
			}
			canvas.restore();
		}
	}

	protected void drawTexts(Canvas canvas)
	{
		if(m_textInfo != null)
		{
			ArrayList<CharInfo> fontInfo = m_textInfo.m_fontsInfo;
			if(fontInfo != null && fontInfo.size() > 0)
			{
				canvas.save();
				temp_matrix.reset();
				temp_matrix.postScale(m_outTextScale, m_outTextScale,
									  (m_textRect.left + m_textRect.right) / 2
						, (m_textRect.top + m_textRect.bottom) / 2);
				canvas.concat(temp_matrix);
				int size = fontInfo.size();
				String text;
				CharInfo info;
				RectF textRect;
				for(int i = 0; i < size; i ++)
				{
					info = fontInfo.get(i);
					final String key = info.m_con;
					textRect = m_textRects.get(i);
					Bitmap textBmp = CreateTextBmp(info.animation_id, textRect, info, i);
					GetTextAnimShowMatrix(info, temp_matrix);
					canvas.save();
					if(textBmp != null)
					{
						RectF rectF = new RectF();
						if(info.animation_id == ANIM1)
						{
							rectF.right = textRect.right;
							rectF.top = textRect.top;
							rectF.left = rectF.right - info.m_animTextW;
							rectF.bottom = textRect.bottom;
						}
						else if(info.animation_id == ANIM2)
						{
							rectF.left = textRect.left;
							rectF.top = textRect.top;
							rectF.right = rectF.left + info.m_animTextW;
							rectF.bottom = textRect.bottom;
						}

						canvas.concat(temp_matrix);
						m_paint.reset();
						m_paint.setColor(0xFFFFFFFF);
						m_paint.setAntiAlias(true);
						m_paint.setFilterBitmap(true);
						m_paint.setStyle(Paint.Style.FILL);
						canvas.drawRect(rectF, m_paint);

						m_paint.reset();
						m_paint.setAntiAlias(true);
						m_paint.setFilterBitmap(true);
						m_paint.setXfermode(m_textAnimMode);
						temp_matrix.reset();
						temp_matrix.postTranslate(m_textRect.left, m_textRect.top);
						canvas.drawBitmap(textBmp, temp_matrix, m_paint);
					}
					else
					{
						canvas.concat(temp_matrix);
						if("diy".equals(key))
						{
							text = getEditableText(info, i);

//							System.out.println("alpha: " + info.m_animTextAlpha);
//							System.out.println("x: " + info.m_showTextAnimDx);
//							System.out.println("y: " + info.m_showTextAnimDy);
//							System.out.println("m_showTextAnimScaleRate: " + info.m_showTextAnimScaleRate);
//							System.out.println("text: " + info.m_animTextAlpha);

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
					canvas.restore();
				}
				canvas.restore();
			}
		}
	}

	private void GetTextAnimShowMatrix(CharInfo info, Matrix matrix)
	{
		float[] dst = new float[]{(m_textRect.left + m_textRect.right) / 2,
				(m_textRect.top + m_textRect.bottom) / 2};

		matrix.reset();
		matrix.postTranslate(info.m_showTextAnimDx, info.m_showTextAnimDy);
		matrix.postScale(info.m_animScaleX, info.m_animScaleY, dst[0], dst[1]);
		matrix.postRotate(info.m_animRotate, dst[0], dst[1]);
	}

	private Bitmap CreateTextBmp(int animID, RectF rectF, CharInfo info, int index)
	{
		Bitmap out = null;
		if(rectF != null && info != null && rectF.width() > 0 && rectF.height() > 0
				&& (animID == ANIM1 || animID == ANIM2))
		{
			out = Bitmap.createBitmap((int)(rectF.width() + 0.5), (int)(rectF.height() + 0.5), Bitmap.Config.ARGB_8888);
			Canvas canvas1 = new Canvas(out);
			temp_matrix.reset();
			temp_matrix.postTranslate(-rectF.left, -rectF.top);
			canvas1.setMatrix(temp_matrix);
			final String key = info.m_con;
			if("diy".equals(key))
			{
				String text = getEditableText(info, index);

				if(info.m_typeSet.equals(MyTextInfo.HORIZONTAL))
				{
					drawHorizontalText(canvas1, text, info);
				}
				else if(info.m_typeSet.equals(MyTextInfo.VERTICAL))
				{
					drawVerticalText(canvas1, text, info);
				}
			}
			canvas1.restore();
		}
		return out;
	}

	protected void drawHorizontalText(Canvas canvas, String text, CharInfo info)
	{
//		info.m_showText = text;
		Painter.getFontPaint(m_context, info, m_paint);	//获取画笔
		/*if(info.effect_id == WaterMarkInfo.EFFECT_5)
		{
			m_paint.setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		}*/
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
			maxString = getTextWidth(m_paint, maxString) > getTextWidth(m_paint, strings[i]) ? maxString : strings[i];
		}
		offsetY = computeOffsetYForMultipleLine(info.m_pos, (int)info.m_offsetY, info.m_verticalspacing, length, m_paint);
		RectF tempRect;
		for(int i = 0; i < length; i ++)
		{
			tempRect = new RectF();
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
				curWidth = getTextWidth(m_paint, str);
				distanceX += getFixDistance(info.m_pos, lastWidth, curWidth);
				newOffsetX = (int)(offsetX + distanceX);
				RectF src = ComputeRect(str, info.m_pos, newOffsetX, newOffsetY, m_paint, true);
				//下划线
				if(info.effect_id == WaterMarkInfo.EFFECT_5)
				{
					RectF line = ComputeRect(str, info.m_pos, newOffsetX, newOffsetY, m_paint, false);
					tempRect.union(line);
				}
				canvas.drawText(str, src.left, src.top, m_paint);
				lastWidth = curWidth;
				distanceX += spaceX;
			}
			distanceY += height + spaceY;
			if(info.effect_id == WaterMarkInfo.EFFECT_5)
			{
				RectF rect = new RectF(tempRect.left, tempRect.bottom, tempRect.right, tempRect.bottom + 10);
				canvas.drawRect(rect, m_paint);
				distanceY += 10 + spaceY;
			}
		}
	}

	protected void drawVerticalText(Canvas canvas, String text, CharInfo info)
	{
//		info.m_showText = text;
		Painter.getFontPaint(m_context, info, m_paint);	//获取画笔
		/*if(info.effect_id == WaterMarkInfo.EFFECT_5)
		{
			m_paint.setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		}*/

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
			if(info.effect_id == WaterMarkInfo.EFFECT_5)
			{
				RectF line1 = ComputeRect(str, info.m_pos, newOffsetX, newOffsetY, m_paint, false);
				RectF rect = new RectF(line1.left, line1.bottom, line1.right, line1.bottom + 10);
				canvas.drawRect(rect, m_paint);
				distanceY += 10 + info.m_verticalspacing;
			}
		}
	}

	protected RectF GetBmpRect(ImageInfo imgInfo, int bmpW, int bmpH)
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

	protected void GetBmpRects()
	{
		if(m_textInfo != null)
		{
			m_imageRects.clear();
			ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String path = m_textInfo.res_path;
				int size = imgInfos.size();
				for(int i = 0; i < size; i ++)
				{
					ImageInfo info = imgInfos.get(i);
					int w = 0, h = 0;
					if(info.type_id == ImageInfo.ANIM)
					{
						if(info.m_animInfo != null
								&& info.m_animInfo.length > 0
								&& info.m_animInfo[0].m_frames != null
								&& info.m_animInfo[0].m_frames.length > 0)
						{
							w = info.m_animInfo[0].m_frames[0].w;
							h = info.m_animInfo[0].m_frames[0].h;
						}
					}
					else
					{
						Object obj = info.GetBmpByInfo(m_context, path);
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
					}
					RectF rect = GetBmpRect(info, w, h);

					if(info.type_id == ImageInfo.SCREEN_H){
//						float scale = m_imageWidth / rect.width();
						float dx = m_imageWidth - rect.width();
						float dy = rect.height() * (m_outTextScale - 1);

						rect.left = rect.left - dx / 2;
						rect.right = rect.right + dx / 2;
						rect.top = rect.top - dy / 2;
						rect.bottom = rect.bottom + dy / 2;
					}
					else if(info.type_id == ImageInfo.SCREEN_V){
						float scale = m_imageHeight / rect.height();
						float dx = rect.width() * (scale - 1);
						float dy = m_imageHeight - rect.height();

						rect.left = rect.left - dx / 2;
						rect.right = rect.right + dx / 2;
						rect.top = rect.top - dy / 2;
						rect.bottom = rect.bottom + dy / 2;
					}

					m_imageRects.put(i, rect);
				}
			}
		}
	}

	/**
	 * 必须先获取文字的矩形区域，对应{@link WaterMarkInfo#EFFECT_1}
	 */
	protected void GetEffect1BmpRects()
	{
		if(m_textInfo != null)
		{
			m_imageRects.clear();
			ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String path = m_textInfo.res_path;
				ImageInfo info = imgInfos.get(0);
				Object obj = info.GetBmpByInfo(m_context, path);
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
				//左边
				RectF rectLeft = new RectF();
				rectLeft.left = m_textRect.left - info.m_specialX - w;
				rectLeft.top = rect.top;
				rectLeft.right = rectLeft.left + w;
				rectLeft.bottom = rect.bottom;
				m_imageRects.put(0, rectLeft);

				RectF rectRight = new RectF();
				rectRight.left = m_textRect.right + info.m_specialX;
				rectRight.top = rect.top;
				rectRight.right = rectRight.left + w;
				rectRight.bottom = rect.bottom;
				m_imageRects.put(1, rectRight);
			}
		}
	}

	/**
	 * 必须先获取文字的矩形区域，对应{@link WaterMarkInfo#EFFECT_2}
	 */
	protected void GetEffect2BmpRects()
	{
		if(m_textInfo != null)
		{
			m_imageRects.clear();
			ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String path = m_textInfo.res_path;
				ImageInfo info = imgInfos.get(0);
				Object obj = info.GetBmpByInfo(m_context, path);
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
				//上边
				RectF rectTop = new RectF();
				rectTop.left = rect.left;
				rectTop.top = m_textRect.top - info.m_specialY - h;
				rectTop.right = rect.right;
				rectTop.bottom = rectTop.top + h;
				m_imageRects.put(0, rectTop);

				//下边
				RectF rectBottom = new RectF();
				rectBottom.left = rect.left;
				rectBottom.top = m_textRect.bottom + info.m_specialY;
				rectBottom.right = rect.right;
				rectBottom.bottom = rectBottom.top + h;
				m_imageRects.put(1, rectBottom);
			}
		}
	}

	/**
	 * 必须先获取文字的矩形区域，对应{@link WaterMarkInfo#EFFECT_3}
	 *
	 */
	protected void GetEffect3BmpRects()
	{
		if(m_textInfo != null)
		{
			m_imageRects.clear();
			ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String path = m_textInfo.res_path;
				int size = imgInfos.size();
				for(int i = 0; i < size; i ++)
				{
					ImageInfo info = imgInfos.get(i);
					int w = 0, h = 0;
					if(info.type_id == ImageInfo.STRETCH_HV)
					{
						RectF rect = new RectF();
						rect.set(m_textRect);
						rect.top -= info.m_specialY;
						rect.left -= info.m_specialX;
						rect.right += info.m_specialX;
						rect.bottom += info.m_specialY;
						m_imageRects.put(i, rect);
					}
					else
					{
						Object obj = info.GetBmpByInfo(m_context, path);
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
	}

	/**
	 * 必须先获取文字的矩形区域，对应{@link WaterMarkInfo#EFFECT_4}
	 *
	 */
	protected void GetEffect4BmpRects()
	{
		if(m_textInfo != null)
		{
			m_imageRects.clear();
			ArrayList<ImageInfo> imgInfos = m_textInfo.m_imgInfo;
			if(imgInfos != null && imgInfos.size() > 0)
			{
				String path = m_textInfo.res_path;
				int size = imgInfos.size();
				for(int i = 0; i < size; i ++)
				{
					ImageInfo info = imgInfos.get(i);
					int w = 0, h = 0;
					if(info.type_id == ImageInfo.STRETCH_HV)
					{
						RectF rect = new RectF();
						rect.set(m_textRect);
						rect.top -= info.m_specialY;
						rect.left -= info.m_specialX;
						rect.right += info.m_specialX;
						rect.bottom += info.m_specialY;
						m_imageRects.put(i, rect);
					}
					else
					{
						Object obj = info.GetBmpByInfo(m_context, path);
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
	}

	protected void GetTextRects()
	{
		if(m_textInfo != null)
		{
			m_textRects.clear();
			ArrayList<CharInfo> fontInfo = m_textInfo.m_fontsInfo;
			if(fontInfo != null && fontInfo.size() > 0)
			{
				int size = fontInfo.size();
				String text;
				for(int i = 0; i < size; i ++)
				{
					CharInfo info = fontInfo.get(i);
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

	public WaterMarkInfo getTextInfo()
	{
		return m_textInfo;
	}

	protected String getEditableText(CharInfo info, int index)
	{
		String text = info.m_showText;
		if(text == null)
			text = info.m_wenan.get(0 + "");
		if(text != null && text.length() > info.m_maxLine * info.m_maxNum)
		{
			text = text.substring(0, info.m_maxLine * info.m_maxNum - 3);
			text += "...";
		}
		if(text == null)
		{
			text = "";
		}
		return text;
	}

	protected RectF GetTextRect(String text, CharInfo info)
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

	protected RectF GetHorizontalRect(String text, CharInfo info)
	{
		RectF rect = new RectF(1024, 1024, 0, 0);
//		info.m_showText = text;
		Painter.getFontPaint(m_context, info, m_paint);	//获取画笔
		/*if(info.effect_id == WaterMarkInfo.EFFECT_5)
		{
			m_paint.setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		}*/
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
			maxString = getTextWidth(m_paint, maxString) > getTextWidth(m_paint, strings[i]) ? maxString : strings[i];
		}
		offsetY = computeOffsetYForMultipleLine(info.m_pos, (int)info.m_offsetY, info.m_verticalspacing, length, m_paint);
		RectF tempRect;
		for(int i = 0; i < length; i ++)
		{
			tempRect = new RectF();
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
				curWidth = getTextWidth(m_paint, str);
				distanceX += getFixDistance(info.m_pos, lastWidth, curWidth);
				newOffsetX = (int)(offsetX + distanceX);
				RectF src = ComputeRect(str, info.m_pos, newOffsetX, newOffsetY, m_paint, false);
				tempRect.union(src);
				rect.union(src);
				lastWidth = curWidth;
				distanceX += spaceX;
			}
			distanceY += height + spaceY;
			if(info.effect_id == WaterMarkInfo.EFFECT_5)
			{
				distanceY += 10 + spaceY;
				rect.bottom += 20;
			}
		}

		rect.top = rect.top - info.fontOffsetY;
		rect.bottom = rect.bottom + info.fontOffsetY;
		rect.left = rect.left - info.fontOffsetX;
		rect.right = rect.right + info.fontOffsetX;

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
		width = getTextWidth(paint, text) + spaceX * (len - 1);

		float maxWidth = 0f;
		if(maxString == null || maxString.equals(""))
		{
			maxWidth = 0f;
		}
		else
		{
			int len1 = maxString.length();
			maxWidth += getTextWidth(paint, maxString) + spaceX * (len1 - 1);
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

	public static int getTextWidth(Paint paint, String str) {
		int iRet = 0;
		if (str != null && str.length() > 0) {
			return (int)(paint.measureText(str) + 0.5f);
		}
		return iRet;
	}

	protected RectF GetVerticalRect(String text, CharInfo info)
	{
		RectF rect = new RectF(1024, 1024, 0, 0);
//		info.m_showText = text;
		Painter.getFontPaint(m_context, info, m_paint);	//获取画笔
		/*if(info.effect_id == WaterMarkInfo.EFFECT_5)
		{
			m_paint.setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		}*/
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
			if(info.effect_id == WaterMarkInfo.EFFECT_5)
			{
				distanceY += 10 + info.m_verticalspacing;
				rect.bottom += 20;
			}
		}

		rect.top = rect.top - info.fontOffsetY;
		rect.bottom = rect.bottom + info.fontOffsetY;
		rect.left = rect.left - info.fontOffsetX;
		rect.right = rect.right + info.fontOffsetX;

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
		float textWidth = getTextWidth(paint, text);
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
//			rects.left = startX;
//			rects.top = startY + (fm.top - fm.ascent);
//			rects.right = endX;
//			rects.bottom = endY;
//			rects.bottom = endY - (fm.top - fm.ascent);

			rects.left = startX;
			rects.top = startY;
			rects.right = endX;
			rects.bottom = endY;
		}

		return rects;
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



	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("VideoText(");
		sb.append("x: ");
		sb.append(m_x);
		sb.append(", y: ");
		sb.append(m_y);
		sb.append(", m_animScaleRate: ");
		sb.append(m_animScaleX);
		sb.append(", m_animDx: ");
		sb.append(m_showAnimDx);
		sb.append(", m_animDy: ");
		sb.append(m_showAnimDy);
		sb.append(", m_showAnimScaleRate: ");
		sb.append(m_animScaleY);
		sb.append(", m_showAnimDx: ");
		sb.append(m_showAnimDx);
		sb.append(", m_showAnimDy: ");
		sb.append(m_showAnimDy);
		sb.append(", m_animAlpha: ");
		sb.append(m_animAlpha);
		sb.append(", m_animW: ");
		sb.append(m_animW);
		sb.append(")");
		return sb.toString();
	}

	public static interface AnimCallback
	{
		public void onUpdate(VideoText video);

		public void onEnd(VideoText video);
	}

}
