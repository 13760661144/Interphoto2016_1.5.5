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

import cn.poco.appmarket.AppItemLayout;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by admin on 2016/8/1.
 */
public class FilterAnim1 extends View
{
	protected Timer m_timer;
	protected TimerTask m_task;
	protected int list_res = R.drawable.filter_anim1_list;
	protected int pic1 = R.drawable.filter_anim1_pic1;
	protected int pic2 = R.drawable.filter_anim1_pic2;
	protected int pic3 = R.drawable.filter_anim1_pic3;
	protected int pic4 = R.drawable.filter_anim1_pic4;
	protected int pic5 = R.drawable.filter_anim1_pic5;

	protected int thumb2 = R.drawable.filter_anim1_thumb2;
	protected int thumb3 = R.drawable.filter_anim1_thumb3;
	protected int thumb4 = R.drawable.filter_anim1_thumb4;
	protected int seekbar = R.drawable.filter_anim1_seekbar;
	protected int seekbar_thumb = R.drawable.filter_anim1_seekbar_thumb;

	protected Bitmap m_listBmp;
	protected Bitmap m_pic1Bmp;
	protected Bitmap m_pic2Bmp;
	protected Bitmap m_pic3Bmp;
	protected Bitmap m_pic4Bmp;
	protected Bitmap m_thumb2Bmp;
	protected Bitmap m_thumb3Bmp;
	protected Bitmap m_thumb4Bmp;
	protected Bitmap m_seekBar;
	protected Bitmap m_seekBarThumb;

	protected int m_curAnim;
	protected final int show_touch1 = 1;
	protected final int move_left = 2;
	protected final int move_right = 3;
	protected final int click1 = 4;
	protected final int click2 = 5;
	protected final int click3 = 6;
	protected final int click4 = 11;
	protected final int seekbar_show = 7;
	protected final int finger_move = 14;
	protected final int seekbar_wait = 15;
	protected final int seekbar_move1 = 8;
	protected final int seekbar_move2 = 9;
	protected final int sleep = 12;

	protected int show_touch_time = 30;
	protected int list_move_left_time = 80;
	protected int list_move_right_time = 80;
	protected int click_time = 55;
	protected int seekbar_show_time = 30;
	protected int seekbar_move_time = 60;
	protected int sleep_time = 20;
	protected int finger_move_time = 40;
	protected int seekbar_wait_time = 60;
	protected int m_curCount;

	protected int m_radius1 = ShareData.PxToDpi_xhdpi(16);
	protected int m_radius2 = m_radius1 * 2;
	protected int list_y;
	protected int list_move_max_x = ShareData.PxToDpi_xhdpi(876);
	protected int list_left_margin = 0;
	protected int touch_x = ShareData.PxToDpi_xhdpi(520);
	protected float text_size;
	protected float text_y;
	protected Matrix temp_matrix;
	protected Paint temp_paint;
	protected PaintFlagsDrawFilter temp_filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	public FilterAnim1(Context context)
	{
		super(context);
		m_listBmp = BitmapFactory.decodeResource(getResources(), list_res);
		m_pic1Bmp = BitmapFactory.decodeResource(getResources(), pic1);
		m_pic2Bmp = BitmapFactory.decodeResource(getResources(), pic2);
		m_pic3Bmp = BitmapFactory.decodeResource(getResources(), pic3);
		m_pic4Bmp = BitmapFactory.decodeResource(getResources(), pic4);
		m_thumb2Bmp = BitmapFactory.decodeResource(getResources(), thumb2);
		m_thumb3Bmp = BitmapFactory.decodeResource(getResources(), thumb3);
		m_thumb4Bmp = BitmapFactory.decodeResource(getResources(), thumb4);
		m_seekBar = BitmapFactory.decodeResource(getResources(), seekbar);
		m_seekBarThumb = BitmapFactory.decodeResource(getResources(), seekbar_thumb);
		list_y = m_pic1Bmp.getHeight() + ShareData.PxToDpi_xhdpi(41);
		text_y = m_pic1Bmp.getHeight() + ShareData.PxToDpi_xhdpi(20);

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
		m_curAnim = show_touch1;
		m_timer = new Timer();
		m_task = new TimerTask()
		{
			@Override
			public void run()
			{
				m_curCount++;
				switch(m_curAnim)
				{
					case show_touch1:
					{
						if(m_curCount > show_touch_time)
						{
							m_curCount = 0;
							m_curAnim = move_left;
						}
						break;
					}
					case move_left:
					{
						if(m_curCount > list_move_left_time)
						{
							m_curCount = 0;
							m_curAnim = move_right;
						}
						break;
					}
					case move_right:
					{
						if(m_curCount > list_move_right_time)
						{
							m_curCount = 0;
							m_curAnim = click1;
						}
						break;
					}
					case click1:
					{
						if(m_curCount > click_time)
						{
							m_curCount = 0;
							m_curAnim = click2;
						}
						break;
					}
					case click2:
					{
						if(m_curCount > click_time)
						{
							m_curCount = 0;
							m_curAnim = click3;
						}
						break;
					}
					case click3:
					{
						if(m_curCount > click_time)
						{
							m_curCount = 0;
							m_curAnim = click4;
						}
						break;
					}
					case click4:
					{
						if(m_curCount > click_time)
						{
							m_curCount = 0;
							m_curAnim = seekbar_show;
						}
						break;
					}
					case seekbar_show:
					{
						if(m_curCount > seekbar_show_time)
						{
							m_curCount = 0;
							m_curAnim = finger_move;
						}
						break;
					}
					case finger_move:
					{
						if(m_curCount > finger_move_time)
						{
							m_curCount = 0;
							m_curAnim = seekbar_wait;
						}
						break;
					}
					case seekbar_wait:
					{
						if(m_curCount > seekbar_wait_time)
						{
							m_curCount = 0;
							m_curAnim = seekbar_move1;
						}
						break;
					}
					case seekbar_move1:
					{
						if(m_curCount > seekbar_move_time)
						{
							m_curCount = 0;
							m_curAnim = seekbar_move2;
						}
						break;
					}
					case seekbar_move2:
					{
						if(m_curCount > seekbar_move_time)
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
							m_curAnim = show_touch1;
						}
						break;
					}
				}
				FilterAnim1.this.postInvalidate();
			}
		};
		m_timer.schedule(m_task, 200, 20);
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

		int viewW = getWidth();
		int picW = m_pic1Bmp.getWidth();
		temp_matrix.reset();
		temp_matrix.postTranslate((viewW - picW) / 2f, 0);
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		switch(m_curAnim)
		{
			case show_touch1:
			{
				DrawBmp(canvas, m_pic1Bmp, 255);
				drawList(canvas, m_listBmp, 0);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setColor(0xffffffff);
				temp_paint.setAlpha((int)(m_curCount / (float)show_touch_time * 255));
				canvas.drawCircle(ShareData.PxToDpi_xhdpi(520), list_y + ShareData.PxToDpi_xhdpi(70), m_radius1, temp_paint);

				break;
			}
			case move_left:
			{
				DrawBmp(canvas, m_pic1Bmp, 255);
				list_left_margin = - list_move_max_x * m_curCount / list_move_left_time;
				drawList(canvas, m_listBmp, list_left_margin);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setColor(0xffffffff);
				temp_paint.setAlpha(255);
				canvas.drawCircle(ShareData.PxToDpi_xhdpi(520) + list_left_margin, list_y + ShareData.PxToDpi_xhdpi(70), m_radius1, temp_paint);
				break;
			}
			case move_right:
			{
				DrawBmp(canvas, m_pic1Bmp, 255);
				list_left_margin = -list_move_max_x + list_move_max_x * m_curCount / list_move_right_time;
				drawList(canvas, m_listBmp, list_left_margin);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setColor(0xffffffff);
				temp_paint.setAlpha(255);
				canvas.drawCircle(ShareData.PxToDpi_xhdpi(520) + list_left_margin, list_y + ShareData.PxToDpi_xhdpi(70), m_radius1, temp_paint);
				break;
			}
			case click1:
			{
				drawClick(canvas, m_pic2Bmp, m_pic1Bmp, m_thumb2Bmp, null, ShareData.PxToDpi_xhdpi(180), 0);
				break;
			}
			case click2:
			{
				drawClick(canvas, m_pic3Bmp, m_pic2Bmp, m_thumb3Bmp, m_thumb2Bmp, ShareData.PxToDpi_xhdpi(340), ShareData.PxToDpi_xhdpi(180));
				break;
			}
			case click3:
			{
				drawClick(canvas, m_pic4Bmp, m_pic3Bmp, m_thumb4Bmp, m_thumb3Bmp, ShareData.PxToDpi_xhdpi(500), ShareData.PxToDpi_xhdpi(340));
				break;
			}
			case click4:
			{
				drawClick(canvas, m_pic4Bmp, m_pic4Bmp, m_thumb4Bmp, m_thumb4Bmp, ShareData.PxToDpi_xhdpi(500), ShareData.PxToDpi_xhdpi(500));
				break;
			}
			case seekbar_show:
			{
				float moveY = ShareData.PxToDpi_xhdpi(70);
				float listy = moveY * m_curCount / seekbar_show_time;
				DrawBmp(canvas, m_pic2Bmp, 255);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin, list_y + listy);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_listBmp, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin + ShareData.PxToDpi_xhdpi(500), list_y + listy - ShareData.PxToDpi_xhdpi(21));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_thumb4Bmp, temp_matrix, temp_paint);
				break;
			}
			case finger_move:
			{
				float listy = ShareData.PxToDpi_xhdpi(70);
				DrawBmp(canvas, m_pic2Bmp, 255);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin, list_y + listy);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_listBmp, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin + ShareData.PxToDpi_xhdpi(500), list_y + listy - ShareData.PxToDpi_xhdpi(21));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_thumb4Bmp, temp_matrix, temp_paint);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setTextSize(text_size);
				temp_paint.setColor(Color.WHITE);
				float textH = temp_paint.descent() - temp_paint.ascent() - ShareData.PxToDpi_xhdpi(10);
				float textW = temp_paint.getStrokeWidth();
				int cur_num = 10;
				temp_paint.measureText("+" + cur_num);
				int seek_x = (getWidth() - m_seekBar.getWidth()) / 2;
				float thumb_x = seek_x + m_seekBar.getWidth() - m_seekBarThumb.getWidth() / 2;
				canvas.drawText("+" + cur_num, thumb_x + (m_seekBarThumb.getWidth() - textW) / 2f, text_y, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(seek_x, text_y + textH + ShareData.PxToDpi_xhdpi(14));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_seekBar, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(thumb_x, text_y + textH);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_seekBarThumb, temp_matrix, temp_paint);

				float click_y = list_y + ShareData.PxToDpi_xhdpi(70);
				float click_x = ShareData.PxToDpi_xhdpi(570) + list_left_margin;
				float x = click_x + (thumb_x - click_x + m_seekBarThumb.getWidth() / 2f) * m_curCount / finger_move_time;
				float y = click_y - (click_y - text_y - textH - m_seekBarThumb.getHeight() / 2f) * m_curCount / finger_move_time;
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setColor(0xffffffff);
				temp_paint.setAlpha(255);
				canvas.drawCircle(x, y, m_radius1, temp_paint);
				break;
			}
			case seekbar_wait:
			{
				float listy = ShareData.PxToDpi_xhdpi(70);
				DrawBmp(canvas, m_pic2Bmp, 255);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin, list_y + listy);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_listBmp, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin + ShareData.PxToDpi_xhdpi(500), list_y + listy - ShareData.PxToDpi_xhdpi(21));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_thumb4Bmp, temp_matrix, temp_paint);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setTextSize(text_size);
				temp_paint.setColor(Color.WHITE);
				float textH = temp_paint.descent() - temp_paint.ascent() - ShareData.PxToDpi_xhdpi(10);
				float textW = temp_paint.getStrokeWidth();
				int cur_num = 10;
				temp_paint.measureText("+" + cur_num);
				int seek_x = (getWidth() - m_seekBar.getWidth()) / 2;
				float thumb_x = seek_x + m_seekBar.getWidth() - m_seekBarThumb.getWidth() / 2;
				canvas.drawText("+" + cur_num, thumb_x + (m_seekBarThumb.getWidth() - textW) / 2f, text_y, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(seek_x, text_y + textH + ShareData.PxToDpi_xhdpi(14));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_seekBar, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(thumb_x, text_y + textH);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_seekBarThumb, temp_matrix, temp_paint);
				break;
			}
			case seekbar_move1:
			{
				float moveY = ShareData.PxToDpi_xhdpi(70);
				int seek_x = (getWidth() - m_seekBar.getWidth()) / 2;
				int total_move = ShareData.PxToDpi_xhdpi(56 * 6);
				int first_num = 10;
				DrawBmp(canvas, m_pic1Bmp, 255);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin, list_y + moveY);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_listBmp, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin + ShareData.PxToDpi_xhdpi(500), list_y + ShareData.PxToDpi_xhdpi(70) - ShareData.PxToDpi_xhdpi(21));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_thumb4Bmp, temp_matrix, temp_paint);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setTextSize(text_size);
				temp_paint.setColor(Color.WHITE);
				float textH = temp_paint.descent() - temp_paint.ascent() - ShareData.PxToDpi_xhdpi(10);
				float textW = temp_paint.getStrokeWidth();
				float move_x;
				if(m_curCount <= seekbar_move_time - 5)
				{
					move_x = total_move * m_curCount / (seekbar_move_time - 5);
				}
				else
				{
					move_x = total_move;
					DrawBmp(canvas, m_pic2Bmp, 255 * 4 / 10);
				}
				int cur_num = first_num - (int)move_x / ShareData.PxToDpi_xhdpi(56);
				temp_paint.measureText("+" + cur_num);
				float thumb_x = seek_x + m_seekBar.getWidth() - m_seekBarThumb.getWidth() / 2 - move_x;
				canvas.drawText("+" + cur_num, thumb_x + (m_seekBarThumb.getWidth() - textW) / 2f, text_y, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(seek_x, text_y + textH + ShareData.PxToDpi_xhdpi(14));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_seekBar, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(thumb_x, text_y + textH);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_seekBarThumb, temp_matrix, temp_paint);
				break;
			}
			case seekbar_move2:
			{
				float moveY = ShareData.PxToDpi_xhdpi(70);
				int seek_x = (getWidth() - m_seekBar.getWidth()) / 2;
				int total_move = ShareData.PxToDpi_xhdpi(56 * 3);
				int first_num = 4;
				DrawBmp(canvas, m_pic2Bmp, 255);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin, list_y + moveY);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_listBmp, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin + ShareData.PxToDpi_xhdpi(500), list_y + ShareData.PxToDpi_xhdpi(70) - ShareData.PxToDpi_xhdpi(21));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_thumb4Bmp, temp_matrix, temp_paint);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setTextSize(text_size);
				temp_paint.setColor(Color.WHITE);
				float textH = temp_paint.descent() - temp_paint.ascent() - ShareData.PxToDpi_xhdpi(10);
				float textW = temp_paint.getStrokeWidth();
				float move_x;
				if(m_curCount <= seekbar_move_time - 5)
				{
					move_x = total_move * m_curCount / (seekbar_move_time - 5);
				}
				else
				{
					move_x = total_move;

					DrawBmp(canvas, m_pic2Bmp, 255 * 7 / 10);
				}
				int cur_num = first_num + (int)move_x / ShareData.PxToDpi_xhdpi(56);
				temp_paint.measureText("+" + cur_num);
				float thumb_x = seek_x + m_seekBar.getWidth() - ShareData.PxToDpi_xhdpi(56 * 6) - m_seekBarThumb.getWidth() / 2 + move_x;
				canvas.drawText("+" + cur_num, thumb_x + (m_seekBarThumb.getWidth() - textW) / 2f, text_y, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(seek_x, text_y + textH + ShareData.PxToDpi_xhdpi(14));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_seekBar, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(thumb_x, text_y + textH);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_seekBarThumb, temp_matrix, temp_paint);
				break;
			}
			case sleep:
			{
				float moveY = ShareData.PxToDpi_xhdpi(70);
				int seek_x = (getWidth() - m_seekBar.getWidth()) / 2;
				int total_move = ShareData.PxToDpi_xhdpi(56 * 3);
				int first_num = 4;
				DrawBmp(canvas, m_pic1Bmp, 255);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin, list_y + moveY);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_listBmp, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(list_left_margin + ShareData.PxToDpi_xhdpi(500), list_y + ShareData.PxToDpi_xhdpi(70) - ShareData.PxToDpi_xhdpi(21));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_thumb4Bmp, temp_matrix, temp_paint);

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setTextSize(text_size);
				temp_paint.setColor(Color.WHITE);
				float textH = temp_paint.descent() - temp_paint.ascent() - ShareData.PxToDpi_xhdpi(10);
				float textW = temp_paint.getStrokeWidth();
				float move_x = total_move;
				int cur_num = first_num + (int)move_x / ShareData.PxToDpi_xhdpi(56);
				temp_paint.measureText("+" + cur_num);
				float thumb_x = seek_x + m_seekBar.getWidth() - ShareData.PxToDpi_xhdpi(56 * 6) - m_seekBarThumb.getWidth() / 2 + move_x;
				canvas.drawText("+" + cur_num, thumb_x + (m_seekBarThumb.getWidth() - textW) / 2f, text_y, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(seek_x, text_y + textH + ShareData.PxToDpi_xhdpi(14));
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_seekBar, temp_matrix, temp_paint);

				temp_matrix.reset();
				temp_matrix.postTranslate(thumb_x, text_y + textH);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(m_seekBarThumb, temp_matrix, temp_paint);


				DrawBmp(canvas, m_pic2Bmp, 255 * cur_num / 10);
				break;
			}
		}
	}

	protected void DrawBmp(Canvas canvas, Bitmap bmp, int alpha)
	{
		int viewW = getWidth();
		int picW = m_pic1Bmp.getWidth();
		temp_matrix.reset();
		temp_matrix.postTranslate((viewW - picW) / 2f, 0);
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		temp_paint.setAlpha(alpha);
		canvas.drawBitmap(bmp, temp_matrix, temp_paint);
	}

	protected void drawList(Canvas canvas, Bitmap bmp, float movex)
	{
		temp_matrix.reset();
		temp_matrix.postTranslate(movex, list_y);
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		canvas.drawBitmap(bmp, temp_matrix, temp_paint);
	}

	protected void drawClick(Canvas canvas, Bitmap pic, Bitmap last_pic, Bitmap thumb, Bitmap last_thumb, float thumb_x, float last_x)
	{
		int click_show_time = 10;
		int click_time = 10;
		int click_hide_time = 10;
		float thumb_y = list_y - ShareData.PxToDpi_xhdpi(21);
		float click_y = list_y + ShareData.PxToDpi_xhdpi(70);
		float click_x = thumb_x + ShareData.PxToDpi_xhdpi(70) + list_left_margin;

		if(m_curCount <= click_show_time)
		{
			DrawBmp(canvas, last_pic, 255);
			drawList(canvas, m_listBmp, list_left_margin);

			if(last_thumb != null)
			{
				temp_matrix.reset();
				temp_matrix.postTranslate(last_x + list_left_margin, thumb_y);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(last_thumb, temp_matrix, temp_paint);
			}

			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setColor(0xffffffff);
			temp_paint.setAlpha((int)(m_curCount / (float)click_show_time * 255));
			canvas.drawCircle(click_x, click_y, m_radius1, temp_paint);
		}
		else if(m_curCount <= click_show_time + click_time)
		{
			DrawBmp(canvas, last_pic, 255);
			drawList(canvas, m_listBmp, list_left_margin);

			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setColor(0xffffffff);
			temp_paint.setAlpha(255);
			canvas.drawCircle(click_x, click_y, m_radius1, temp_paint);

			if(last_thumb != null)
			{
				temp_matrix.reset();
				temp_matrix.postTranslate(last_x + list_left_margin, thumb_y);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				canvas.drawBitmap(last_thumb, temp_matrix, temp_paint);
			}

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
			DrawBmp(canvas, pic, 255);
			drawList(canvas, m_listBmp, list_left_margin);

			temp_matrix.reset();
			temp_matrix.postTranslate(thumb_x + list_left_margin, thumb_y);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			canvas.drawBitmap(thumb, temp_matrix, temp_paint);

			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setColor(0xffffffff);
			int time = m_curCount - click_show_time - click_time;
			temp_paint.setAlpha(255 - (int)(time / (float)click_hide_time * 255));
			canvas.drawCircle(click_x, click_y, m_radius1, temp_paint);
		}
		else if(m_curCount <= this.click_time - 10 && thumb_x != last_x)
		{
			int time = m_curCount - click_show_time - click_time - click_hide_time;
			int totalTime = this.click_time - click_show_time - click_time - click_hide_time - 10;
			int flag = 1;
			if(thumb_x  - last_x > 0)
			{
				flag = -1;
			}
			if(thumb_x == ShareData.PxToDpi_xhdpi(180))
			{
				list_left_margin = 0;
			}
			else if(thumb_x == ShareData.PxToDpi_xhdpi(340))
			{
				int max = ShareData.PxToDpi_xhdpi(120) * flag;
				list_left_margin = time * max / (totalTime);
			}
			else if(thumb_x == ShareData.PxToDpi_xhdpi(500))
			{
				int max = -ShareData.PxToDpi_xhdpi(160);
				list_left_margin = time * max / (totalTime) - ShareData.PxToDpi_xhdpi(120);
			}
			DrawBmp(canvas, pic, 255);
			drawList(canvas, m_listBmp, list_left_margin);

			temp_matrix.reset();
			temp_matrix.postTranslate(thumb_x + list_left_margin, thumb_y);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			canvas.drawBitmap(thumb, temp_matrix, temp_paint);
		}
		else
		{
			DrawBmp(canvas, pic, 255);
			drawList(canvas, m_listBmp, list_left_margin);

			temp_matrix.reset();
			temp_matrix.postTranslate(thumb_x + list_left_margin, thumb_y);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			canvas.drawBitmap(thumb, temp_matrix, temp_paint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int heightSpec = MeasureSpec.makeMeasureSpec(ShareData.PxToDpi_xhdpi(444), MeasureSpec.EXACTLY);
		int widthSpec = MeasureSpec.makeMeasureSpec(ShareData.PxToDpi_xhdpi(580), MeasureSpec.EXACTLY);
		super.onMeasure(widthSpec, heightSpec);
	}
}
