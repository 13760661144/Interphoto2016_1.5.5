package cn.poco.beautify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.view.MotionEvent;

import cn.poco.display.CoreViewV3;
import cn.poco.graphics.ShapeEx;

/**
 * 1.增加点击图片查看原图的回调
 * 2.加边框时重置图片位置
 * 3.保存图片时如果图片小于输出尺寸则按图片尺寸生成
 * 4.限制最多添加装饰个数
 * 
 * @author POCO
 *
 */
public class BeautifyViewV3 extends CoreViewV3
{

	public BeautifyViewV3(Context context, int frW, int frH)
	{
		super(context, frW, frH);
	}

	@Override
	protected void OddDown(MotionEvent event)
	{
		m_isTouch = true;

		if(m_operateMode == MODE_FRAME)
		{
			if(m_frame == null)
			{
				m_target = null;
				return;
			}
		}

		super.OddDown(event);

		if(m_operateMode == MODE_IMAGE)
		{
			((ControlCallback)m_cb).TouchImage(true);
		}
	}

	@Override
	protected void OddUp(MotionEvent event)
	{
		super.OddUp(event);

		if(m_operateMode == MODE_IMAGE)
		{
			((ControlCallback)m_cb).TouchImage(false);
		}
	}

	@Override
	protected void EvenDown(MotionEvent event)
	{
		m_isTouch = true;
		m_isOddCtrl = false;

		if(m_operateMode == MODE_FRAME)
		{
			if(m_frame == null)
			{
				m_target = null;
				return;
			}
		}

		super.EvenDown(event);
	}

	@Override
	public void SetFrame(Object info, Bitmap bmp)
	{
		super.SetFrame(info, bmp);

		if(m_img != null && m_frame != null)
		{
			m_img.m_x = m_origin.m_centerX - m_img.m_centerX;
			m_img.m_y = m_origin.m_centerY - m_img.m_centerY;
			{
				float scale1 = m_viewport.m_w * m_viewport.m_scaleX / m_img.m_w;
				float scale2 = m_viewport.m_h * m_viewport.m_scaleY / m_img.m_h;
				m_img.m_scaleX = (scale1 > scale2) ? scale1 : scale2;
				m_img.m_scaleY = m_img.m_scaleX;
			}
			m_img.m_degree = 0;
		}
	}

	@Override
	public void SetFrame2(ShapeEx item)
	{
		super.SetFrame2(item);

		if(m_img != null)
		{
			//重置图片位置
			if(item == null)
			{
				m_img.m_x = m_viewport.m_x;
				m_img.m_y = m_viewport.m_y;
				m_img.m_scaleX = m_img.DEF_SCALE;
				m_img.m_scaleY = m_img.DEF_SCALE;
				m_img.m_degree = 0;
			}
			else
			{
				m_img.m_x = m_origin.m_centerX - m_img.m_centerX;
				m_img.m_y = m_origin.m_centerY - m_img.m_centerY;
				{
					float scale1 = m_viewport.m_w * m_viewport.m_scaleX / m_img.m_w;
					float scale2 = m_viewport.m_h * m_viewport.m_scaleY / m_img.m_h;
					m_img.m_scaleX = (scale1 > scale2) ? scale1 : scale2;
					m_img.m_scaleY = m_img.m_scaleX;
				}
				m_img.m_degree = 0;
			}
		}
	}

	public static interface ControlCallback extends CoreViewV3.ControlCallback
	{
		public void TouchImage(boolean isTouch);
	}

	@Override
	public Bitmap GetOutputBmp(int size)
	{
		float whscale = (float)m_viewport.m_w / (float)m_viewport.m_h;
		float outW = size;
		float outH = outW / whscale;
		if(outH > size)
		{
			outH = size;
			outW = outH * whscale;
		}
		ShapeEx backup = (ShapeEx)m_origin.Clone();

		//设置输出位置
		m_origin.m_scaleX = outW / (float)m_viewport.m_w / m_viewport.m_scaleX;
		m_origin.m_scaleY = m_origin.m_scaleX;
		m_origin.m_x = (int)outW / 2f - (m_viewport.m_x + m_viewport.m_centerX - m_origin.m_centerX) * m_origin.m_scaleX - m_origin.m_centerX;
		m_origin.m_y = (int)outH / 2f - (m_viewport.m_y + m_viewport.m_centerY - m_origin.m_centerY) * m_origin.m_scaleY - m_origin.m_centerY;

		float tempW;
		float tempH;
		Bitmap imgBmp = null;
		if(m_img != null)
		{
			tempW = m_origin.m_scaleX * m_img.m_scaleX * m_img.m_w;
			tempH = m_origin.m_scaleY * m_img.m_scaleY * m_img.m_h;
			imgBmp = m_cb.MakeOutputImg(m_img.m_ex, (int)(tempW + 0.5), (int)(tempH + 0.5));
			if(imgBmp != null && imgBmp.getWidth() > 0 && imgBmp.getHeight() > 0)
			{
				if(Math.max(tempW, tempH) > Math.max(imgBmp.getWidth(), imgBmp.getHeight()))
				{
					//修正(图片小于输出size)
					m_origin.m_scaleX = (float)imgBmp.getWidth() / (m_img.m_scaleX * m_img.m_w);
					m_origin.m_scaleY = m_origin.m_scaleX;
					outW = m_origin.m_scaleX * (float)m_viewport.m_w * m_viewport.m_scaleX;
					outH = outW / whscale;
					m_origin.m_x = (int)outW / 2f - (m_viewport.m_x + m_viewport.m_centerX - m_origin.m_centerX) * m_origin.m_scaleX - m_origin.m_centerX;
					m_origin.m_y = (int)outH / 2f - (m_viewport.m_y + m_viewport.m_centerY - m_origin.m_centerY) * m_origin.m_scaleY - m_origin.m_centerY;
				}
			}
			else
			{
				imgBmp = null;
			}
		}

		Bitmap outBmp = Bitmap.createBitmap((int)outW, (int)outH, Config.ARGB_8888);
		Canvas canvas = new Canvas(outBmp);
		canvas.setDrawFilter(temp_filter);

		Bitmap tempBmp;
		canvas.drawColor(m_bkColor);
		if(m_bk != null)
		{
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			tempBmp = m_cb.MakeOutputBK(m_bk.m_ex, (int)outW, (int)outH);
			BitmapShader shader = new BitmapShader(tempBmp, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
			temp_paint.setShader(shader);
			canvas.drawRect(0, 0, outW, outH, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		if(m_img != null && imgBmp != null)
		{
			tempBmp = imgBmp;
			imgBmp = null;
			GetOutputMatrix(temp_matrix, m_img, tempBmp);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		if(m_frame != null)
		{
			tempW = m_origin.m_scaleX * m_frame.m_scaleX * m_frame.m_w;
			tempH = m_origin.m_scaleY * m_frame.m_scaleY * m_frame.m_h;
			tempBmp = m_cb.MakeOutputFrame(m_frame.m_ex, (int)(tempW + 0.5), (int)(tempH + 0.5));
			GetOutputMatrix(temp_matrix, m_frame, tempBmp);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		int len = m_pendantArr.size();
		ShapeEx temp;
		for(int i = 0; i < len; i++)
		{
			temp = m_pendantArr.get(i);
			tempW = m_origin.m_scaleX * temp.m_scaleX * temp.m_w;
			tempH = m_origin.m_scaleY * temp.m_scaleY * temp.m_h;
			tempBmp = m_cb.MakeOutputPendant(temp.m_ex, (int)(tempW + 0.5), (int)(tempH + 0.5));
			GetOutputMatrix(temp_matrix, temp, tempBmp);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		m_origin.Set(backup);

		return outBmp;
	}

	public Bitmap GetOutputBmp()
	{
		int size = m_img.m_w > m_img.m_h ? m_img.m_w : m_img.m_h;

		float whscale = (float)m_viewport.m_w / (float)m_viewport.m_h;
		float outW = size;
		float outH = outW / whscale;
		if(outH > size)
		{
			outH = size;
			outW = outH * whscale;
		}
		ShapeEx backup = (ShapeEx)m_origin.Clone();

		//设置输出位置
		m_origin.m_scaleX = outW / (float)m_viewport.m_w / m_viewport.m_scaleX;
		m_origin.m_scaleY = m_origin.m_scaleX;
		m_origin.m_x = (int)outW / 2f - (m_viewport.m_x + m_viewport.m_centerX - m_origin.m_centerX) * m_origin.m_scaleX - m_origin.m_centerX;
		m_origin.m_y = (int)outH / 2f - (m_viewport.m_y + m_viewport.m_centerY - m_origin.m_centerY) * m_origin.m_scaleY - m_origin.m_centerY;

		Bitmap outBmp = Bitmap.createBitmap((int)outW, (int)outH, Config.ARGB_8888);
		Canvas canvas = new Canvas(outBmp);
		canvas.setDrawFilter(temp_filter);

		Bitmap tempBmp;
		canvas.drawColor(m_bkColor);
		if(m_bk != null)
		{
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			tempBmp = m_bk.m_bmp;
			BitmapShader shader = new BitmapShader(tempBmp, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
			temp_paint.setShader(shader);
			canvas.drawRect(0, 0, outW, outH, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		if(m_img != null)
		{
			tempBmp = m_img.m_bmp;
			GetOutputMatrix(temp_matrix, m_img, tempBmp);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		if(m_frame != null)
		{
			tempBmp = m_frame.m_bmp;
			GetOutputMatrix(temp_matrix, m_frame, tempBmp);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		int len = m_pendantArr.size();
		ShapeEx temp;
		for(int i = 0; i < len; i++)
		{
			temp = m_pendantArr.get(i);
			tempBmp = temp.m_bmp;
			GetOutputMatrix(temp_matrix, temp, tempBmp);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		m_origin.Set(backup);

		return outBmp;
	}

	@Override
	public int GetPendantMaxNum()
	{
		long mem = Runtime.getRuntime().maxMemory() / 1048576;
		int max;
		if(mem >= 96)
		{
			max = 32;
		}
		else if(mem >= 64)
		{
			max = 24;
		}
		else if(mem >= 32)
		{
			max = 12;
		}
		else if(mem >= 24)
		{
			max = 8;
		}
		else
		{
			max = 6;
		}

		return max;
	}
}
