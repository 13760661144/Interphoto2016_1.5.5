package cn.poco.beautify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import cn.poco.graphics.Shape;
import cn.poco.graphics.ShapeEx;
import cn.poco.resource.LightEffectRes;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

public class LightEffectView extends BeautifyViewV3
{
	protected boolean m_isCompare = false; //对比
	protected PorterDuff.Mode m_mode = null;
	public LightEffectView(Context context, int frW, int frH)
	{
		super(context, frW, frH);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
//		super.onDraw(canvas);
		if(m_drawable && m_viewport.m_w > 0 && m_viewport.m_h > 0)
		{
			//if(m_isTouch)
			//{
			//	DrawToCanvas(canvas, m_operateMode);
			//}
			//else
			{
				if(m_drawBuffer == null && m_origin.m_w > 0 && m_origin.m_h > 0)
				{
					m_drawBuffer = Bitmap.createBitmap(m_origin.m_w, m_origin.m_h, Config.ARGB_8888);
				}

				if(m_drawBuffer != null)
				{
//					if(m_invalidate)
					{
						Canvas tempCanvas = new Canvas(m_drawBuffer);
						tempCanvas.drawColor(0, Mode.CLEAR);
						DrawToCanvas(tempCanvas, m_operateMode);
						m_invalidate = false;
					}

					canvas.save();
					//canvas.setDrawFilter(temp_filter);
					temp_paint.reset();
					//temp_paint.setAntiAlias(true);
					//temp_paint.setFilterBitmap(true);
					canvas.drawBitmap(m_drawBuffer, 0, 0, temp_paint);
					canvas.restore();
				}
			}
		}
	}

	@Override
	protected void DrawToCanvas(Canvas canvas, int mode)
	{
		canvas.save();

		canvas.setDrawFilter(temp_filter);

		//控制渲染矩形
		ClipStage(canvas);

		//画背景
		DrawBK(canvas, m_bk, m_bkColor);

		//画图片
		DrawItem(canvas, m_img);
		if (!m_isCompare)
		{
			//画边框
			DrawItem(canvas, m_frame);

			//画装饰
			int len = m_pendantArr.size();
			for(int i = 0; i < len; i++)
			{
				DrawItem2(canvas, m_pendantArr.get(i));
			}
		}
		canvas.restore();
	}

	protected void DrawItem2(Canvas canvas, ShapeEx item)
	{
		if(item != null && item.m_bmp != null)
		{
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			if(item instanceof ShapeEx2)
			{
				temp_paint.setAlpha((int)(((ShapeEx2)item).m_alpha / 120f * 255));
				PorterDuff.Mode mode = ((ShapeEx2)item).m_mode;
				if(m_mode != null)
				{
					mode = m_mode;
				}
				PorterDuffXfermode temp_mode = new PorterDuffXfermode(mode);
				temp_paint.setXfermode(temp_mode);
			}
			GetShowMatrix(temp_matrix, item);
			canvas.drawBitmap(item.m_bmp, temp_matrix, temp_paint);
		}
	}

	public void setPoterDuffMode(PorterDuff.Mode mode)
	{
		m_mode = mode;
	}

	@Override
	protected void OddDown(MotionEvent event)
	{
		super.OddDown(event);
		if(m_operateMode != MODE_IMAGE)
		{
			((ControlCallback)m_cb).TouchImage(true);
		}
	}

	@Override
	protected void OddUp(MotionEvent event)
	{
		super.OddUp(event);
		if(m_operateMode != MODE_IMAGE)
		{
			((ControlCallback)m_cb).TouchImage(false);
		}
	}

	public void Flip(boolean isHorizontal)
	{
		if(m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size())
		{
			ShapeEx item = m_pendantArr.get(m_pendantCurSel);
			if(item != null)
			{
				if(isHorizontal)
				{
					if(item.m_flip == Shape.Flip.NONE|| item.m_flip == Shape.Flip.VERTICAL)
					{
						item.m_flip = Shape.Flip.HORIZONTAL;
					}
					else
					{
						item.m_flip = Shape.Flip.NONE;
					}
				}
				else
				{
					if(item.m_flip == Shape.Flip.NONE || item.m_flip == Shape.Flip.HORIZONTAL)
					{
						item.m_flip = Shape.Flip.VERTICAL;
					}
					else
					{
						item.m_flip = Shape.Flip.NONE;
					}
				}
			}
		}
	}

	public void SetAlpha(int alpha)
	{
		if(m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size())
		{
			ShapeEx item = m_pendantArr.get(m_pendantCurSel);
			if(item != null && item instanceof ShapeEx2)
			{
				((ShapeEx2)item).m_alpha = alpha;
			}
		}
	}

	public ShapeEx GetPendantByIndex(int index)
	{
		ShapeEx out = null;
		if(index >= 0 && index < m_pendantArr.size())
		{
			out = m_pendantArr.get(index);
		}

		return out;
	}

	public void SetCompareFlag(boolean flag)
	{
		m_isCompare = flag;
	}

	/**
	 * 光效用到
	 * @param info
	 * @param bmp
	 *            可以为null,为null时调用默认回调生成图片
	 * @return index值,失败-1
	 */
	public int AddPendant2(Object info, Bitmap bmp)
	{
		if(GetPendantIdleNum() > 0)
		{
			ShapeEx2 item = new ShapeEx2();
			if(bmp != null)
			{
				item.m_bmp = bmp;
			}
			else
			{
				item.m_bmp = m_cb.MakeShowPendant(info, -1, -1);
			}
			item.m_w = item.m_bmp.getWidth();
			item.m_h = item.m_bmp.getHeight();
			item.m_centerX = (float)item.m_w / 2f;
			item.m_centerY = (float)item.m_h / 2f;
			item.m_ex = info;

//			System.out.println("item.m_w: " + item.m_w);
//			System.out.println("item.m_h: " + item.m_h);

			//初始化光效位置信息
			if(info != null && info instanceof LightEffectRes)
			{
				Object res = ((LightEffectRes)info).m_res;
				File file = new File((String)res);
				if(!file.exists())
				{
					try
					{
						InputStream is = getContext().getAssets().open((String)res);
						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						byte[] bytes = new byte[1024];
						while(is.read(bytes) != -1)
						{
							bout.write(bytes, 0, bytes.length);
						}
						res = bout.toByteArray();
						bout.close();
						is.close();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				if(res instanceof String)
				{
					Utils.DecodeFile((String)res, opts);
				}
				else if(res instanceof byte[])
				{
					BitmapFactory.decodeByteArray((byte[])res, 0, ((byte[])res).length, opts);
				}
				int orgW = opts.outWidth;
				float bmpScale = (float)orgW / item.m_w;
//				System.out.println("bmpScale: " + ((LightEffectRes)info).m_scale / 100f * bmpScale);
//				System.out.println("item.m_w: " + item.m_w);
//				System.out.println("orgW: " + orgW);
				item.m_scaleX = item.m_scaleY = ((LightEffectRes)info).m_scale / 100f * bmpScale;
				item.MIN_SCALE = ((LightEffectRes)info).m_minScale / 100f * bmpScale;
				item.MAX_SCALE = ((LightEffectRes)info).m_maxScale / 100f * bmpScale;
				if(ShareData.m_screenWidth > 720)
				{
					item.m_scaleX = item.m_scaleY = item.m_scaleX * ShareData.m_screenWidth / 720f * 1.3f;
					item.MIN_SCALE = item.MIN_SCALE * ShareData.m_screenWidth / 720f * 1.3f;
					item.MAX_SCALE = item.MAX_SCALE * ShareData.m_screenWidth / 720f * 1.3f;
				}
				if(((LightEffectRes)info).m_compose == 45)
				{
					item.m_mode = PorterDuff.Mode.SCREEN;
				}
				else if(((LightEffectRes)info).m_compose == 33)
				{
					item.m_mode = PorterDuff.Mode.LIGHTEN;
				}
				String[] infos = ((LightEffectRes)info).m_location.split("\\+");
				if(infos != null && infos.length == 5)
				{
					String align = infos[0];
					float x = Float.parseFloat(infos[1]);
					float y = Float.parseFloat(infos[2]);
					int degree = Integer.parseInt(infos[3]);
					int alpha = Integer.parseInt(infos[4]);
					item.m_alpha = alpha;
					item.m_degree = degree;
					PointF pointF = computeXYWithAlign(item, align, x, y);
					item.m_x = pointF.x;
					item.m_y = pointF.y;
				}
			}

			m_pendantArr.add(item);

			return m_pendantArr.size() - 1;
		}

		return -1;
	}

	@Override
	public int AddPendant2(ShapeEx item)
	{
		if(GetPendantIdleNum() > 0)
		{
			if(item.m_bmp == null || item.m_bmp.isRecycled())
			{
				item.m_bmp = m_cb.MakeShowPendant(item.m_ex, -1, -1);
			}
			m_pendantArr.add(item);

			return m_pendantArr.size() - 1;
		}

		return -1;
	}

	@Override
	public ShapeEx DelPendant()
	{
		ShapeEx out = null;

		if(m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size())
		{
			out = m_pendantArr.remove(m_pendantCurSel);
			if(out.m_bmp != null)
			{
				out.m_bmp.recycle();
				out.m_bmp = null;
			}

			m_pendantCurSel = m_pendantArr.size() - 1;
			m_cb.SelectPendant(m_pendantCurSel);
		}

		return out;
	}

	@Override
	public void DelAllPendant()
	{
		int len = m_pendantArr.size();
		ShapeEx temp;
		for(int i = 0; i < len; i++)
		{
			temp = m_pendantArr.get(i);
			if(temp.m_bmp != null)
			{
				temp.m_bmp.recycle();
				temp.m_bmp = null;
			}
		}
		super.DelAllPendant();
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
					outW = imgBmp.getWidth();
					outH = imgBmp.getHeight();
					/*outW = m_origin.m_scaleX * (float)m_viewport.m_w * m_viewport.m_scaleX;
					outH = outW / whscale;*/
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
			if(temp instanceof ShapeEx2)
			{
				temp_paint.setAlpha((int)(((ShapeEx2)temp).m_alpha / 120f * 255));
				PorterDuffXfermode temp_mode = new PorterDuffXfermode(((ShapeEx2)temp).m_mode);
				temp_paint.setXfermode(temp_mode);
			}
			canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		m_origin.Set(backup);

		return outBmp;
	}

	protected PointF computeXYWithAlign(ShapeEx2 item, String align, float x, float y)
	{
		PointF pointF = new PointF();
		float viewWidth = m_viewport.m_w * m_viewport.m_scaleX;
		float viewHeight = m_viewport.m_h * m_viewport.m_scaleY;
		float width = item.m_w * item.m_scaleX;
		float height = item.m_h * item.m_scaleY;
		float dx = item.m_centerX * item.m_scaleX - item.m_centerX;
		float dy = item.m_centerY * item.m_scaleY - item.m_centerY;
		float originX = m_viewport.m_x + m_viewport.m_centerX - m_viewport.m_centerX * m_viewport.m_scaleX;
		float originY = m_viewport.m_y + m_viewport.m_centerY - m_viewport.m_centerY * m_viewport.m_scaleY;
		if(align != null && align.length() > 0)
		{
			int flag = Integer.parseInt(align);
			//九宫格位置，1-9
			switch(flag)
			{
				case 2:
				{
					originX += (viewWidth - width) / 2f;
					break;
				}
				case 3:
				{
					originX += viewWidth - width;
					break;
				}
				case 4:
				{
					originY += (viewHeight - height) / 2f;
					break;
				}
				case 5:
				{
					originX += (viewWidth - width) / 2f;
					originY += (viewHeight - height) / 2f;
					break;
				}
				case 6:
				{
					originX += viewWidth - width;
					originY += (viewHeight - height) / 2f;
					break;
				}
				case 7:
				{
					originY += viewHeight - height;
					break;
				}
				case 8:
				{
					originX += (viewWidth - width) / 2f;
					originY += viewHeight - height;
					break;
				}
				case 9:
				{
					originX += viewWidth - width;
					originY += viewHeight - height;
					break;
				}

			}
		}
		pointF.x = originX + x + dx;
		pointF.y = originY + y + dy;
		return pointF;
	}


	@Override
	public void SetSelPendant(int index)
	{
		if(index >= 0 && index < m_pendantArr.size())
		{
			m_pendantCurSel = index;
		}
		else
		{
			m_pendantCurSel = -1;
		}

		m_isOddCtrl = false;
		m_isTouch = false;
		m_target = null;
	}

}
