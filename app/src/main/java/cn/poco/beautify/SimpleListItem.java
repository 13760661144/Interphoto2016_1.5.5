package cn.poco.beautify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.tianutils.ShareData;
import cn.poco.tsv100.SimpleBtnList100;

/**
 * Created by admin on 2016/5/11.
 */
public class SimpleListItem extends SimpleBtnList100.Item
{
	public Object img_res_out;
	public Object img_res_over;

	public String m_title;
	public boolean m_showTitle = false;
	public int def_title_size;
	public int title_color_out;
	public int title_color_over;
	public int title_bg_res_out;
	public int title_bg_res_over;

	public Object bk_out_res;
	public Object bk_over_res;

	public int bk_color_out;
	public int bk_color_over;

	public int item_width;
	public int item_height;

	public int left_margin;
	public int right_margin;
	public int top_margin;
	public boolean isVertical;	//（上图片下文字/左图片右文字）
	public int img_text_margin = ShareData.PxToDpi_xhdpi(10);

	public Object m_ex;
	public int m_tjID = 0;
	public int m_shenceTjID;
	public String m_shenceTjStr;

	protected TextView m_text;
	protected ImageView m_img;
	private LinearLayout m_contentFr;

	public SimpleListItem(Context context)
	{
		super(context);
	}

	public void InitDatas()
	{
		FrameLayout.LayoutParams fl;
		this.setMinimumWidth(item_width);
		this.setMinimumHeight(item_height);
		this.setPadding(left_margin, top_margin, right_margin, 0);
		if(bk_color_out != 0)
		{
			this.setBackgroundColor(bk_color_out);
		}

		m_contentFr = new LinearLayout(getContext());
		if(isVertical)
		{
			m_contentFr.setOrientation(LinearLayout.VERTICAL);
		}
		m_contentFr.setGravity(Gravity.CENTER);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		m_contentFr.setLayoutParams(fl);
		this.addView(m_contentFr);
		{
			m_img = new ImageView(getContext());
			LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			m_img.setLayoutParams(ll);
			m_contentFr.addView(m_img);

			m_text = new TextView(getContext());
			if(!m_showTitle)
			{
				m_text.setVisibility(View.GONE);
			}
			m_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, def_title_size);
			m_text.setText(m_title);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			if(img_res_out != null && img_res_over != null)
			{
				if(isVertical)
				{
					ll.topMargin = img_text_margin;
				}
				else
				{
					ll.leftMargin = img_text_margin;
				}
			}
			m_text.setLayoutParams(ll);
			m_contentFr.addView(m_text);
		}
	}

	@Override
	public void SetOut(int index)
	{
		super.SetOut(index);
		setRes(m_img, img_res_out);
		if(m_text != null)
		{
			m_text.setTextColor(title_color_out);
		}
		setRes(m_contentFr, title_bg_res_out);
		if(bk_color_out != 0)
		{
			this.setBackgroundColor(bk_color_out);
		}
		else
		{
			setRes(this, bk_out_res);
		}
	}

	@Override
	public void SetOver(int index)
	{
		super.SetOver(index);
		setRes(m_img, img_res_over);
		if(m_text != null)
		{
			m_text.setTextColor(title_color_over);
		}
		setRes(m_contentFr, title_bg_res_over);
		if(bk_color_over != 0)
		{
			this.setBackgroundColor(bk_color_over);
		}
		else
		{
			setRes(this, bk_over_res);
		}
	}

	protected void setRes(View v, Object res)
	{
		if(v != null && res != null)
		{
			if(res instanceof Integer)
			{
				v.setBackgroundResource((Integer)res);
			}
			else if(res instanceof String)
			{
				Bitmap bmp = BitmapFactory.decodeFile((String)res);
				v.setBackgroundDrawable(new BitmapDrawable(bmp));
			}
		}
	}
}
