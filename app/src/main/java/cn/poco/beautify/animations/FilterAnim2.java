package cn.poco.beautify.animations;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.TypedValue;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by admin on 2016/8/1.
 */
public class FilterAnim2 extends View
{
	protected Timer m_timer;
	protected TimerTask m_task;
	protected int page_res = R.drawable.filter_anim2_page;
	protected int list_res = R.drawable.filter_anim1_list;
	protected int pic2 = R.drawable.filter_anim1_pic2;
	protected int select_res2 = R.drawable.filter_anim1_thumb2;
	protected int seekbar = R.drawable.filter_anim1_seekbar;
	protected int seekbar_thumb = R.drawable.filter_anim1_seekbar_thumb;

	protected Bitmap m_listBmp;
	protected Bitmap m_pic2Bmp;
	protected Bitmap m_selectRes2Bmp;
	protected Bitmap m_pageBmp;
	protected Bitmap m_seekBar;
	protected Bitmap m_seekBarThumb;

	protected int m_curAnim;
	protected final int click1 = 4;
	protected final int show_intro_page = 10;
	protected final int page_up = 11;
	protected final int page_down = 7;
	protected final int page_down_alpha = 8;
	protected final int page_down_move = 9;
	protected final int sleep = 12;
	protected int list_left_margin = 0;

	protected int click1_time = 30;
	protected int page_time = 40;
	protected int page_move_time = 100;
	protected int page_alpha_time = 20;
	protected int sleep_time = 40;

	protected int m_radius1 = ShareData.PxToDpi_xhdpi(16);
	protected int m_radius2 = m_radius1 * 2;
	protected int list_y;
	protected int m_removeY;
	protected int m_curPageY;
	protected int m_curCount;

	protected Matrix temp_matrix;
	protected Paint temp_paint;
	protected float text_size;
	protected float text_y;
	protected PaintFlagsDrawFilter temp_filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	public FilterAnim2(Context context)
	{
		super(context);
		m_listBmp = BitmapFactory.decodeResource(getResources(), list_res);
		m_pic2Bmp = BitmapFactory.decodeResource(getResources(), pic2);
		m_selectRes2Bmp = BitmapFactory.decodeResource(getResources(), select_res2);
		m_pageBmp = BitmapFactory.decodeResource(getResources(), page_res);
		m_seekBar = BitmapFactory.decodeResource(getResources(), seekbar);
		m_seekBarThumb = BitmapFactory.decodeResource(getResources(), seekbar_thumb);
		list_y = m_pic2Bmp.getHeight() + ShareData.PxToDpi_xhdpi(41);
		text_y = m_pic2Bmp.getHeight() + ShareData.PxToDpi_xhdpi(5);
		m_removeY = ShareData.PxToDpi_xhdpi(1750);

		Context c = getContext();
		Resources r;
		if (c == null)
			r = Resources.getSystem();
		else
			r = c.getResources();
		text_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());

		temp_matrix = new Matrix();
		temp_paint = new Paint();
	}


	public void startAnim()
	{
		cancelAnim();
		m_curCount = 0;
		m_curAnim = click1;
		m_timer = new Timer();
		m_task = new TimerTask()
		{
			@Override
			public void run()
			{
				m_curCount++;
				switch(m_curAnim)
				{
					case click1:
					{
						if(m_curCount > click1_time)
						{
							m_curCount = 0;
							m_curAnim = show_intro_page;
						}
						break;
					}
					case show_intro_page:
					{
						if(m_curCount > page_time)
						{
							m_curCount = 0;
							m_curAnim = page_up;
						}
						break;
					}
					case page_up:
					{
						if(m_curCount > page_move_time)
						{
							m_curCount = 0;
							m_curAnim = page_down;
						}
						break;
					}
					case page_down:
					{
						if(m_curCount > page_move_time)
						{
							m_curCount = 0;
							m_curAnim = page_down_alpha;
						}
						break;
					}
					case page_down_alpha:
					{
						if(m_curCount > page_alpha_time)
						{
							m_curCount = 0;
							m_curAnim = page_down_move;
						}
						break;
					}
					case page_down_move:
					{
						if(m_curCount > page_time)
						{
							m_curCount = 0;
							m_curAnim = sleep;
						}
						break;
					}
					case sleep:
					{
						if(m_curCount > sleep_time)
						{
							m_curCount = 0;
							m_curAnim = click1;
						}
						break;
					}
				}
				FilterAnim2.this.postInvalidate();
			}
		};
		m_timer.schedule(m_task, 1200, 20);
	}

	public void cancelAnim()
	{
		if (m_timer != null)
		{
			m_timer.cancel();
			m_timer = null;
		}
		if(m_task != null)
		{
			m_task.cancel();
			m_task = null;
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		canvas.setDrawFilter(temp_filter);

		switch(m_curAnim)
		{
			case click1:
			{
				drawUnchangeInfo(canvas);
				int click_show_time = 5;
				int click_time = 5;
				int click_hide_time = 5;
				float click_y = list_y;
				float click_x = ShareData.PxToDpi_xhdpi(250) + list_left_margin;
				if(m_curCount <= click_show_time)
				{
					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setColor(0xffffffff);
					temp_paint.setAlpha((int)(m_curCount / (float)click_show_time * 255));
					canvas.drawCircle(click_x, click_y, m_radius1, temp_paint);
				}
				else if(m_curCount <= click_show_time + click_time)
				{
					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setColor(0xffffffff);
					temp_paint.setAlpha(255);
					canvas.drawCircle(click_x, click_y, m_radius1, temp_paint);

					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setColor(0xffffffff);
					temp_paint.setAlpha(125);
					int time = m_curCount - click_show_time;
					int radius = m_radius2 * time / click_time;
					canvas.drawCircle(click_x, click_y, radius, temp_paint);
				}
				else if(m_curCount <= click_show_time + click_time + click_hide_time)
				{
					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setColor(0xffffffff);
					int time = m_curCount - click_show_time - click_time;
					temp_paint.setAlpha(255 - (int)(time / (float)click_hide_time * 255));
					canvas.drawCircle(click_x, click_y, m_radius1, temp_paint);
				}
				break;
			}
			case show_intro_page:
			{
				drawUnchangeInfo(canvas);
				m_curPageY = ShareData.PxToDpi_xhdpi(444) - ShareData.PxToDpi_xhdpi(444) * m_curCount / page_time;

				temp_matrix.reset();
				temp_matrix.postTranslate(0, m_curPageY);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_pageBmp, temp_matrix, temp_paint);
				break;
			}
			case page_up:
			{
				int movey = m_curCount * m_removeY / page_move_time;
				m_curPageY = - movey;
				int viewW = getWidth();
				int viewH = getHeight();

				temp_matrix.reset();
				temp_matrix.postTranslate(0, m_curPageY);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_pageBmp, temp_matrix, temp_paint);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setColor(0xffffffff);
				temp_paint.setAlpha(255);
				canvas.drawCircle(viewW * 3 / 4f, viewH * 3 / 4f -  movey, m_radius1, temp_paint);
				break;
			}
			case page_down:
			{
				int movey = m_curCount * m_removeY / page_move_time;
				m_curPageY = -m_removeY +  movey;
				int viewW = getWidth();
				int viewH = getHeight();

				temp_matrix.reset();
				temp_matrix.postTranslate(0, m_curPageY);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_pageBmp, temp_matrix, temp_paint);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setColor(0xffffffff);
				temp_paint.setAlpha(255);
				canvas.drawCircle(viewW * 3 / 4f, viewH * 3 / 4f -  m_removeY + movey, m_radius1, temp_paint);
				break;
			}
			case page_down_alpha:
			{
				drawUnchangeInfo(canvas);
				int viewW = getWidth();
				int viewH = getHeight();

				int alpha = 255 - 125 * m_curCount / page_alpha_time;

				temp_matrix.reset();
				temp_matrix.postTranslate(0, 0);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setAlpha(alpha);
				canvas.drawBitmap(m_pageBmp, temp_matrix, temp_paint);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setColor(0xffffffff);
				temp_paint.setAlpha(255);
				canvas.drawCircle(viewW * 3 / 4f, viewH * 3 / 4f, m_radius1, temp_paint);
				break;
			}
			case page_down_move:
			{
				drawUnchangeInfo(canvas);

				m_curPageY = ShareData.PxToDpi_xhdpi(444) * m_curCount / page_time;

				temp_matrix.reset();
				temp_matrix.postTranslate(0, m_curPageY);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setAlpha(125);
				canvas.drawBitmap(m_pageBmp, temp_matrix, temp_paint);
				break;
			}
			case sleep:
			{
				drawUnchangeInfo(canvas);
				break;
			}
		}
	}

	protected void drawUnchangeInfo(Canvas canvas)
	{
		int viewW = getWidth();
		int picW = m_pic2Bmp.getWidth();
		temp_matrix.reset();
		temp_matrix.postTranslate((viewW - picW) / 2f, 0);
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		canvas.drawBitmap(m_pic2Bmp, temp_matrix, temp_paint);

		temp_matrix.reset();
		temp_matrix.postTranslate(list_left_margin, list_y);
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		canvas.drawBitmap(m_listBmp, temp_matrix, temp_paint);

		temp_matrix.reset();
		temp_matrix.postTranslate(list_left_margin + ShareData.PxToDpi_xhdpi(180), list_y - ShareData.PxToDpi_xhdpi(21));
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		canvas.drawBitmap(m_selectRes2Bmp, temp_matrix, temp_paint);

//		int total_move = ShareData.PxToDpi_xhdpi(56 * 3);
//		int seek_x = (getWidth() - m_seekBar.getWidth()) / 2;
//		temp_paint.reset();
//		temp_paint.setAntiAlias(true);
//		temp_paint.setTextSize(text_size);
//		temp_paint.setColor(Color.WHITE);
//		float textH = temp_paint.descent() - temp_paint.ascent();
//		float textW = temp_paint.getStrokeWidth();
//		float move_x = total_move;
//		temp_paint.measureText("+" + 7);
//		float thumb_x = seek_x + m_seekBar.getWidth() - ShareData.PxToDpi_xhdpi(56 * 6) - m_seekBarThumb.getWidth() / 2 + move_x;
//		canvas.drawText("+" + 7, thumb_x + (m_seekBarThumb.getWidth() - textW) / 2f, text_y, temp_paint);
//
//		temp_matrix.reset();
//		temp_matrix.postTranslate(seek_x, text_y + textH + ShareData.PxToDpi_xhdpi(14));
//		temp_paint.reset();
//		temp_paint.setAntiAlias(true);
//		canvas.drawBitmap(m_seekBar, temp_matrix, temp_paint);
//
//		temp_matrix.reset();
//		temp_matrix.postTranslate(thumb_x, text_y + textH);
//		temp_paint.reset();
//		temp_paint.setAntiAlias(true);
//		canvas.drawBitmap(m_seekBarThumb, temp_matrix, temp_paint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int heightSpec = MeasureSpec.makeMeasureSpec(ShareData.PxToDpi_xhdpi(444), MeasureSpec.EXACTLY);
		int widthSpec = MeasureSpec.makeMeasureSpec(ShareData.PxToDpi_xhdpi(580), MeasureSpec.EXACTLY);
		super.onMeasure(widthSpec, heightSpec);
	}
}
