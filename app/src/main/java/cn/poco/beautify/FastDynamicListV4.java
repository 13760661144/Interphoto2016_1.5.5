package cn.poco.beautify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.io.File;
import java.io.InputStream;

import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.ImageUtils;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv.AsynImgLoader;
import cn.poco.tsv.FastDynamicListV2;
import cn.poco.tsv.FastItemList;
import cn.poco.utils.Utils;

public class FastDynamicListV4 extends FastDynamicListV2
{
	public int def_head_res;
	public int def_head_x;
	public int def_head_y;
	public int def_head_w;
	public int def_head_h;
//	public boolean show_head = false;

	public int def_new_x;
	public int def_new_y;
	public int def_new_w;
	public int def_new_h;

	public boolean def_show_title_bg = false; //是否显示文字背景
	public int def_text_bg_x_out;
	public int def_text_bg_y_out;
	public int def_text_bg_w_out;
	public int def_text_bg_h_out;
	public int def_text_bg_h_out1;

	public int def_text_bg_x_over;
	public int def_text_bg_y_over;
	public int def_text_bg_w_over;
	public int def_text_bg_h_over;

	public int def_text_y_over;
	public int def_title_over_res;
	public int def_title_over_w;
	public int def_title_over_h;
	public int def_title_over_x;
	public int def_title_over_y;

	public int def_author_size;
	public int def_title_margin;

	protected AsynImgLoader m_headLoader;

	protected int m_max = 100;
	protected Path temp_path = new Path();

	public FastDynamicListV4(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}

	public FastDynamicListV4(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public FastDynamicListV4(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void InitData(cn.poco.tsv.FastItemList.ControlCallback cb)
	{
		m_cb = cb;
		if(def_img_res != 0)
		{
			m_defImgBmp = ImageUtils.MakeResRoundBmp(getContext(), def_img_res, def_img_w, def_img_h, def_img_round_size);
		}
		else if(def_img_color != 0)
		{
			m_defImgBmp = ImageUtils.MakeColorRoundBmp(def_img_color, def_img_w, def_img_h, def_img_round_size);
		}
		if(def_bk_out_res != 0)
		{
			m_outBkBmp = ImageUtils.MakeResRoundBmp(getContext(), def_bk_out_res, def_bk_w, def_bk_h, def_img_round_size);
		}
		else if(def_bk_out_color != 0)
		{
			m_outBkBmp = ImageUtils.MakeColorRoundBmp(def_bk_out_color, def_bk_w, def_bk_h, def_img_round_size);
		}
		if(def_bk_over_res != 0)
		{
			m_overBkBmp = ImageUtils.MakeResRoundBmp(getContext(), def_bk_over_res, def_bk_w, def_bk_h, def_img_round_size);
		}
		else if(def_bk_over_color != 0)
		{
			m_overBkBmp = ImageUtils.MakeColorRoundBmp(def_bk_over_color, def_bk_w, def_bk_h, def_img_round_size);
		}
		m_loader = new AsynImgLoader(new AsynImgLoader.ControlCallback()
		{
			@Override
			public void PopImg(int uri)
			{
				FastItemList.ItemInfo info = GetItemInfoByUri(uri);
				info.m_animTime = System.currentTimeMillis() + def_anim_time;
				FastDynamicListV4.this.invalidate();
			}

			@Override
			public Bitmap MakeBmp(AsynImgLoader.Item item)
			{
				Bitmap out = null;

				if(item != null)
				{
					if(item.m_res instanceof String)
					{
						File file = new File((String)item.m_res);
						if(file.exists())
						{
							int rotate = 0;
							if(item.m_uri == FastDynamicListV4.ItemInfo.URI_NONE)
							{
								rotate = CommonUtils.GetImgInfo((String)item.m_res)[0];
							}
							Bitmap temp = Utils.DecodeImage(getContext(), item.m_res, rotate, -1, def_img_w, def_img_h);
							out = MakeBmpV2.CreateBitmapV2(temp, rotate, 0, -1, def_img_w, def_img_h, Bitmap.Config.ARGB_8888);
							if(temp != null)
							{
								temp.recycle();
								temp = null;
							}
						}
						else {
							try
							{
								InputStream is = getContext().getAssets().open((String)item.m_res);
								out = BitmapFactory.decodeStream(is);
							}catch(Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else if(item.m_res instanceof Bitmap)
					{
						out = (Bitmap)item.m_res;
					}
					else
					{
						Bitmap temp = Utils.DecodeImage(getContext(), item.m_res, 0, -1, def_img_w, def_img_h);
						out = MakeBmpV2.CreateBitmapV2(temp, 0, 0, -1, def_img_w, def_img_h, Bitmap.Config.ARGB_8888);
						if(temp != null)
						{
							temp.recycle();
							temp = null;
						}
					}
					if(out != null)
					{
						Bitmap temp = out;
						out = ImageUtils.MakeRoundBmp(temp, def_img_w, def_img_h, def_img_round_size);
						if(out != temp)
						{
							temp.recycle();
							temp = null;
						}
					}
				}

				return out;
			}
		});
		m_loader.SetQueueSize(GetShowNum(ShareData.m_screenWidth, def_item_left + def_item_width + def_item_right));

		m_headLoader = new AsynImgLoader(new AsynImgLoader.ControlCallback()
		{
			@Override
			public void PopImg(int uri)
			{
				FastItemList.ItemInfo info = GetItemInfoByUri(uri);
				info.m_animTime = System.currentTimeMillis() + def_anim_time;
				FastDynamicListV4.this.invalidate();
			}

			@Override
			public Bitmap MakeBmp(AsynImgLoader.Item item)
			{
				Bitmap out = null;

				if(item != null)
				{
					Bitmap temp = MakeHeadBmp(item.m_res);
					out = cn.poco.utils.ImageUtil.makeCircleBmp(temp, 2, 0xffffffff);
					if(temp != null)
					{
						temp.recycle();
						temp = null;
					}
				}

				return out;
			}
		});
		m_headLoader.SetQueueSize(GetShowNum(ShareData.m_screenWidth, def_item_left + def_item_width + def_item_right));
	}

	@Override
	protected void DrawItem(Canvas canvas, int index, cn.poco.tsv.FastHSVCore.ItemInfo info)
	{
		canvas.setDrawFilter(temp_filter);

		//画背景
		Bitmap bmp = null;
		if(m_currentSel == index)
		{
			bmp = m_overBkBmp;
		}
		else
		{
			bmp = m_outBkBmp;
		}
		if(bmp != null)
		{
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			canvas.drawBitmap(bmp, def_bk_x, def_bk_y, temp_paint);
			bmp = null;
		}

		//画图片
		bmp = m_loader.GetImg(info.m_uri, true);
		if(bmp == null)
		{
			m_loader.PushImg(info.m_uri, ((FastItemList.ItemInfo)info).m_logo);
			bmp = m_defImgBmp;
		}
		if(bmp != null)
		{
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			canvas.drawBitmap(bmp, def_img_x, def_img_y, temp_paint);
			bmp = null;
		}

		//画文字背景
		if(def_show_title_bg)
		{
			if(m_currentSel == index && ((FastItemList.ItemInfo)info).m_uri != ItemInfo.URI_NONE)
			{
				bmp = ImageUtils.MakeColorRoundBmp(((FastDynamicListV4.ItemInfo)info).text_bg_color_over, def_text_bg_w_over, def_text_bg_h_over, def_img_round_size);
				if(bmp != null)
				{
					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setFilterBitmap(true);
					canvas.drawBitmap(bmp, def_text_bg_x_over, def_text_bg_y_over, temp_paint);
				}
			}
			else
			{
				float h = 0;
				if(((ItemInfo)info).m_author != null && ((ItemInfo)info).m_author.length() > 0)
				{
					h = def_text_bg_h_out1 - def_text_bg_h_out;
				}
				Bitmap temp = ImageUtils.MakeColorRoundBmp(((FastDynamicListV4.ItemInfo)info).text_bg_color_out, def_text_bg_w_out, (int)(def_text_bg_h_out + h), 0);
				if(temp != null)
				{
					bmp = cn.poco.utils.ImageUtil.MakeDiffCornerRoundBmp(temp, 0, 0, def_img_round_size, def_img_round_size);
				}
				if(temp != bmp)
				{
					temp.recycle();
					temp = null;
				}
				if(bmp != null)
				{
					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setFilterBitmap(true);
					canvas.drawBitmap(bmp, def_text_bg_x_out, def_text_bg_y_out - h, temp_paint);
				}
			}
			bmp = null;
		}
		//画文字
		if(def_show_title && ((FastItemList.ItemInfo)info).m_name != null)
		{
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			temp_paint.setTextSize(def_title_size);
			float w = temp_paint.measureText(((FastItemList.ItemInfo)info).m_name);
			float h = temp_paint.descent() - temp_paint.ascent();
			if(m_currentSel == index && ((FastItemList.ItemInfo)info).m_uri != ItemInfo.URI_NONE)
			{
				temp_paint.setColor(def_title_color_over);
				/*if(((ItemInfo)info).m_author != null && ((ItemInfo)info).m_author.length() > 0)
				{
					float w1 = temp_paint.measureText(((ItemInfo)info).m_author);
					canvas.drawText(((FastItemList.ItemInfo)info).m_name, def_img_x + (def_img_w - w) / 2, def_text_y_over, temp_paint);
					canvas.drawText(((ItemInfo)info).m_author, def_img_x + (def_img_w - w1) / 2, def_text_y_over + h, temp_paint);
				}
				else
				{*/
					canvas.drawText(((FastItemList.ItemInfo)info).m_name, def_img_x + (def_img_w - w) / 2, def_text_y_over, temp_paint);
//				}
				if(def_title_over_res > 0)
				{
					bmp = ImageUtils.MakeResRoundBmp(getContext(), def_title_over_res, def_title_over_w, def_title_over_h, 0);

					if(bmp != null)
					{
						temp_paint.reset();
						temp_paint.setAntiAlias(true);
						temp_paint.setFilterBitmap(true);
						canvas.drawBitmap(bmp, def_title_over_x, def_title_over_y, temp_paint);
						bmp = null;
					}
				}
			}
			else
			{
				if(((ItemInfo)info).m_author != null && ((ItemInfo)info).m_author.length() > 0)
				{
					temp_paint.setTextSize(def_author_size);
					float w1 = temp_paint.measureText(((ItemInfo)info).m_author);
					float h1 = temp_paint.descent() - temp_paint.ascent();

					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setFilterBitmap(true);
					temp_paint.setTextSize(def_title_size);
					temp_paint.setColor(def_title_color_out);
					float y = def_item_height - def_text_bg_h_out1 + (def_text_bg_h_out1 - (h + h1 + def_title_margin)) / 2f + h;
					canvas.drawText(((FastItemList.ItemInfo)info).m_name, def_img_x + (def_img_w - w) / 2, y, temp_paint);

					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setFilterBitmap(true);
					temp_paint.setTextSize(def_author_size);
					temp_paint.setColor(def_title_color_out);
					canvas.drawText(((ItemInfo)info).m_author, def_img_x + (def_img_w - w1) / 2, y + h - h1 + def_title_margin , temp_paint);
				}
				else
				{
					temp_paint.setColor(def_title_color_out);
					float y = def_item_height - def_title_bottom_margin;
					canvas.drawText(((FastItemList.ItemInfo)info).m_name, def_img_x + (def_img_w - w) / 2, y, temp_paint);
				}
			}
		}

		//画头像
		if(m_currentSel == index)
		{
//			show_head = false;
			bmp = m_headLoader.GetImg(info.m_uri, true);
			if(bmp == null)
			{
				m_headLoader.PushImg(info.m_uri, ((FastDynamicListV4.ItemInfo)info).m_head);
				bmp = null;
			}
			if(bmp != null)
			{
//				show_head = true;
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				canvas.drawBitmap(bmp, def_head_x, def_head_y, temp_paint);
				bmp = null;
			}
		}

		//画下载状态
		switch(((FastDynamicListV2.ItemInfo)info).m_style)
		{
			case NEED_DOWNLOAD:
			{
				if(m_readyBmp == null && def_ready_res != 0)
				{
					Bitmap temp = BitmapFactory.decodeResource(getResources(), def_ready_res);
					m_readyBmp = ImageUtils.MakeRoundBmp(temp, def_new_w, def_new_h, def_img_round_size);
					if(m_readyBmp != temp)
					{
						temp.recycle();
						temp = null;
					}
				}
				bmp = m_readyBmp;
				break;
			}
			case NEW:
			{
				if(m_newBmp == null && def_new_res != 0)
				{
					Bitmap temp = BitmapFactory.decodeResource(getResources(), def_new_res);
					m_newBmp = ImageUtils.MakeRoundBmp(temp, def_new_w, def_new_h, def_img_round_size);
					if(m_newBmp != temp)
					{
						temp.recycle();
						temp = null;
					}
				}
				break;
			}
			case LOADING:
			{
				/*if(m_loadingBmp == null && def_loading_res != 0)
				{
					Bitmap temp = BitmapFactory.decodeResource(getResources(), def_loading_res);
					m_loadingBmp = ImageUtils.MakeRoundBmp(temp, def_state_w, def_state_h, def_img_round_size);
					if(m_loadingBmp != temp)
					{
						temp.recycle();
						temp = null;
					}
				}
				bmp = m_loadingBmp;
				if(def_loading_anim && bmp != null)
				{
					if(def_loading_mask_color != 0)
					{
						temp_paint.reset();
						temp_paint.setColor(def_loading_mask_color);
						canvas.drawRect(def_img_x, def_img_y, def_img_x + def_img_w, def_img_y + def_img_h, temp_paint);
					}
					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setFilterBitmap(true);
					temp_matrix.reset();
					temp_matrix.postRotate((System.currentTimeMillis() % 1000) / 1000f * 360f, bmp.getWidth() / 2f, bmp.getHeight() / 2f);
					temp_matrix.postTranslate(def_state_x, def_state_y);
					canvas.drawBitmap(bmp, temp_matrix, temp_paint);

					this.invalidate();
					bmp = null;
				}*/
				if(def_loading_anim)
				{
					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setColor(0xffffc433);
					temp_paint.setStrokeWidth(2);
					temp_paint.setDither(true);
					temp_paint.setStrokeCap(Paint.Cap.ROUND);
					temp_paint.setStyle(Paint.Style.STROKE);
					temp_path.reset();
					int perimeter = 2 * def_img_w + 2 * def_img_h;
					int pro = perimeter * ((FastDynamicListV4.ItemInfo)info).m_progress / m_max;
					if(pro <= def_img_w)
					{
						if(pro <= def_img_round_size)
						{
							pro = (int)def_img_round_size;
						}
						temp_path.moveTo(def_img_x, def_img_y + def_img_round_size);
						temp_path.quadTo(def_img_x, def_img_y, def_img_x + def_img_round_size, def_img_y);
						temp_path.lineTo(def_img_x + pro, def_img_y);
					}
					else if(pro > def_img_w && pro <= def_img_w + def_img_h)
					{
						pro = pro - def_img_w;
						if(pro <= def_img_round_size)
						{
							pro = (int)def_img_round_size;
						}
						temp_path.moveTo(def_img_x, def_img_y + def_img_round_size);
						temp_path.quadTo(def_img_x, def_img_y, def_img_x + def_img_round_size, def_img_y);
						temp_path.lineTo(def_img_x + def_img_w - def_img_round_size, def_img_y);
						temp_path.quadTo(def_img_x + def_img_w, def_img_y, def_img_x + def_img_w, def_img_y + def_img_round_size);
						temp_path.lineTo(def_img_x + def_img_w, def_img_y + pro);
					}
					else if(pro > def_img_w + def_img_h && pro <= def_img_w * 2 + def_img_h)
					{
						pro = pro - def_img_w - def_img_h;
						if(pro <= def_img_round_size)
						{
							pro = (int)def_img_round_size;
						}
						temp_path.moveTo(def_img_x, def_img_y + def_img_round_size);
						temp_path.quadTo(def_img_x, def_img_y, def_img_x + def_img_round_size, def_img_y);
						temp_path.lineTo(def_img_x + def_img_w - def_img_round_size, def_img_y);
						temp_path.quadTo(def_img_x + def_img_w, def_img_y, def_img_x + def_img_w, def_img_y + def_img_round_size);
						temp_path.lineTo(def_img_x + def_img_w, def_img_y + def_img_h - def_img_round_size);
						temp_path.quadTo(def_img_x + def_img_w, def_img_y + def_img_h, def_img_x + def_img_w - def_img_round_size, def_img_y + def_img_h);
						temp_path.lineTo(def_img_x + def_img_w - pro, def_img_y + def_img_h);
					}
					else
					{
						pro = pro - 2 * def_img_w - def_img_h;
						if(pro <= def_img_round_size)
						{
							pro = (int)def_img_round_size;
						}
						if(pro > def_img_h - def_img_round_size)
						{
							pro = (int)(def_img_h - def_img_round_size);
						}
						temp_path.moveTo(def_img_x, def_img_y + def_img_round_size);
						temp_path.quadTo(def_img_x, def_img_y, def_img_x + def_img_round_size, def_img_y);
						temp_path.lineTo(def_img_x + def_img_w - def_img_round_size, def_img_y);
						temp_path.quadTo(def_img_x + def_img_w, def_img_y, def_img_x + def_img_w, def_img_y + def_img_round_size);
						temp_path.lineTo(def_img_x + def_img_w, def_img_y + def_img_h - def_img_round_size);
						temp_path.quadTo(def_img_x + def_img_w, def_img_y + def_img_h, def_img_x + def_img_w - def_img_round_size, def_img_y + def_img_h);
						temp_path.lineTo(def_img_x + def_img_round_size, def_img_y + def_img_h);
						temp_path.quadTo(def_img_x, def_img_y + def_img_h, def_img_x, def_img_y + def_img_h - def_img_round_size);
						temp_path.lineTo(def_img_x, def_img_y + def_img_h - pro);
					}
					canvas.drawPath(temp_path, temp_paint);
				}
				break;
			}
			case WAIT:
			{
				if(m_waitBmp == null && def_wait_res != 0)
				{
					Bitmap temp = BitmapFactory.decodeResource(getResources(), def_wait_res);
					m_waitBmp = ImageUtils.MakeRoundBmp(temp, def_state_w, def_state_h, def_img_round_size);
					if(m_waitBmp != temp)
					{
						temp.recycle();
						temp = null;
					}
				}
				bmp = m_waitBmp;
				break;
			}
			default:
				break;
		}
		if(bmp != null && !(((FastDynamicListV2.ItemInfo)info).m_isLock && def_lock_res != 0))
		{
			switch(((FastDynamicListV2.ItemInfo)info).m_style)
			{
				case NEW:
				case NEED_DOWNLOAD:
				{
					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setFilterBitmap(true);
					canvas.drawBitmap(bmp, def_new_x, def_new_y, temp_paint);
					bmp = null;
					break;
				}
				default:
				{
					temp_paint.reset();
					temp_paint.setAntiAlias(true);
					temp_paint.setFilterBitmap(true);
					canvas.drawBitmap(bmp, def_state_x, def_state_y, temp_paint);
					bmp = null;
					break;
				}
			}
		}

		//画new状态
		if(((FastDynamicListV2.ItemInfo)info).m_style == FastDynamicListV2.ItemInfo.Style.NEW)
		{

			bmp = m_newBmp;
			if(bmp != null)
			{
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				canvas.drawBitmap(bmp, def_new_x, def_new_y, temp_paint);
				bmp = null;
			}
		}

		//画锁
		if(((FastDynamicListV2.ItemInfo)info).m_isLock && def_lock_res != 0)
		{
			if(m_lockBmp == null)
			{
				m_lockBmp = BitmapFactory.decodeResource(getResources(), def_lock_res);
			}

			if(m_lockBmp != null)
			{
				canvas.drawBitmap(m_lockBmp, def_lock_x, def_lock_y, null);
				bmp = null;
			}
		}
	}

	public Bitmap MakeHeadBmp(Object res)
	{
		Bitmap out = null;

		if(res != null)
		{
			if(res instanceof Integer)
			{
				out = BitmapFactory.decodeResource(getResources(), (Integer)res);
			}
			else if(res instanceof String && ((String)res).length() > 0)
			{
				File file = new File((String)res);
				if(file.exists())
				{
					out = BitmapFactory.decodeFile((String)res);
				}
				else
				{
					try
					{
						InputStream is = getContext().getAssets().open((String)res);
						out = BitmapFactory.decodeStream(is);
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			else if(res instanceof Bitmap)
			{
				out = (Bitmap)res;
			}
			if(out != null)
			{
				if(out != null)
				{
					Bitmap temp = out;
					out = ImageUtils.MakeRoundBmp(temp, def_head_w, def_head_h, 0);
					if(out != temp)
					{
						temp.recycle();
						temp = null;
					}
				}
			}
		}

		return out;
	}

	@Override
	protected void OnUp(MotionEvent event)
	{
		if(event.getPointerCount() == 1)
		{
			m_isTouch = false;
		}
		if(down_index >= 0)
		{
			OnItemUp(down_index);
			if(m_isClick)
			{
				int touchIndex = GetTouchIndex((int)event.getX());
				if(down_index == touchIndex)
				{
					if(GetTouchHead(down_index, event.getX(), event.getY()))
					{
						if(m_cb != null)
						{
							((FastDynamicListV4.ControlCallback)m_cb).OnHeadClick(this, (FastItemList.ItemInfo)m_infoList.get(down_index), down_index);
						}
						m_isClick = false;
						down_index = -1;
						return;
					}
					OnItemClick(down_index);
				}
				m_isClick = false;
			}
			down_index = -1;
		}
	}

	protected boolean GetTouchHead(int index, float x, float y)
	{
		int itemW = def_item_left + def_item_width + def_item_right;
		float originX = itemW * index;
		Object head = ((FastDynamicListV4.ItemInfo)m_infoList.get(down_index)).m_head;
		boolean flag = false;
		if(head instanceof String)
		{
			if(((String)head).length() > 0)
			{
				flag = true;
			}
		}
		if(head instanceof Integer)
		{
			if((Integer)head > 0)
			{
				flag = true;
			}
		}
		if(def_head_w != 0 && def_head_h != 0 && flag)
		{
			if(x > (originX + def_head_x) && x < (originX + def_head_x + def_head_w)
					&& y > def_head_y && y < (def_head_y + def_head_h))
			{
				return true;
			}
		}
		return false;
	}

	public int SetProgressByUri(int uri, int progress)
	{
		int out = GetIndex(m_infoList, uri);
		if(out >= 0)
		{
			FastDynamicListV4.ItemInfo info = (FastDynamicListV4.ItemInfo)m_infoList.get(out);
			info.m_progress = progress;
			this.invalidate();
		}
		return out;
	}

	@Override
	public void ClearAll()
	{
		// TODO Auto-generated method stub
		super.ClearAll();
		m_headLoader.ClearAll();
	}

	public static interface ControlCallback extends FastItemList.ControlCallback
	{
		void OnHeadClick(FastItemList list, FastItemList.ItemInfo info, int index);
	}

	public static class ItemInfo extends FastDynamicListV2.ItemInfo
	{

		public Object m_head;	//头像
		public String m_author;
		public int text_bg_color_out;
		public int text_bg_color_over;
		public int m_progress = 0;
	}

}
