package cn.poco.beautify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.poco.tianutils.ShareData;

/**
 * Created by admin on 2016/6/30.
 */
public class MySimpleListItem extends MySimpleBtnList.Item
{
	public String m_uri;
	public String m_title;
	public int def_title_size;
	public int title_color_out;
	public int title_color_over;
	public int title_bg_res_out;
	public int title_bg_res_over;
	public int item_width;
	public int item_height;
	public int left_margin;
	public int right_margin;

	public int delete_res;

	public Object bk_out_res;
	public Object bk_over_res;
	public int bk_color_out;
	public int bk_color_over;

	public Object m_ex;
	public int m_tjID = 0;

	protected TextView m_text;

	public MySimpleListItem(Context context)
	{
		super(context);
	}

	public void InitDatas()
	{
		FrameLayout.LayoutParams fl;
		this.setMinimumWidth(item_width);
		this.setMinimumHeight(item_height);
		this.setPadding(left_margin, 0, right_margin, 0);
		if(bk_color_out != 0)
		{
			this.setBackgroundColor(bk_color_out);
		}

		m_text = new TextView(getContext());
		m_text.setGravity(Gravity.CENTER);
		m_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, def_title_size);
		m_text.setText(m_title);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.TOP | Gravity.RIGHT;
		fl.topMargin = fl.rightMargin = ShareData.PxToDpi_xhdpi(8);
		m_text.setLayoutParams(fl);
		this.addView(m_text);

		m_deleteBtn.setImageResource(delete_res);
		m_deleteBtn.setPadding(0, 0, 0, ShareData.PxToDpi_xhdpi(20));
		m_deleteBtn.setVisibility(View.GONE);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.RIGHT | Gravity.TOP;
		m_deleteBtn.setLayoutParams(fl);
		this.addView(m_deleteBtn);
	}

	@Override
	public void SetOut(int index)
	{
		super.SetOut(index);
		if(m_text != null)
		{
			m_text.setTextColor(title_color_out);
		}
		setRes(m_text, title_bg_res_out);
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
		if(m_text != null)
		{
			m_text.setTextColor(title_color_over);
		}
		setRes(m_text, title_bg_res_over);
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

	@Override
	public void OnLongSelect(boolean select)
	{
		super.OnLongSelect(select);
		if(select)
		{
			m_deleteBtn.setVisibility(View.VISIBLE);
		}
		else
		{
			m_deleteBtn.setVisibility(View.GONE);
		}
	}
}
