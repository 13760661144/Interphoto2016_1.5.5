package cn.poco.home;

import android.content.Context;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import java.util.ArrayList;

import cn.poco.tianutils.ShareData;
import cn.poco.tianutils.StatusButton;

public class PageNumComponent extends LinearLayout
{
	public int page_num_out;
	public int page_num_over;
	public int page_num_margin = ShareData.PxToDpi_hdpi(5);

	protected ArrayList<StatusButton> m_pageNumArr = new ArrayList<StatusButton>();

	public PageNumComponent(Context context)
	{
		super(context);

		this.setOrientation(LinearLayout.HORIZONTAL);
	}

	public void UpdatePageNum(int index, int max)
	{
		if(max < 2)
		{
			int len = m_pageNumArr.size();
			for(int i = 0; i < len; i++)
			{
				this.removeView(m_pageNumArr.remove(0));
			}
			return;
		}

		int len = m_pageNumArr.size();
		if(len > max)
		{
			len = len - max;
			for(int i = 0; i < len; i++)
			{
				this.removeView(m_pageNumArr.remove(0));
			}
		}
		else if(max > len)
		{
			len = max - len;
			LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.leftMargin = page_num_margin;
			ll.rightMargin = ll.leftMargin;
			for(int i = 0; i < len; i++)
			{
				StatusButton status = new StatusButton(getContext());
				status.SetData(page_num_out, page_num_over, ScaleType.CENTER_INSIDE);
				status.setLayoutParams(ll);
				m_pageNumArr.add(status);
				this.addView(status);
			}
		}

		len = m_pageNumArr.size();
		for(int i = 0; i < len; i++)
		{
			if(i == index)
			{
				m_pageNumArr.get(i).SetOver();
			}
			else
			{
				m_pageNumArr.get(i).SetOut();
			}
		}

	}
}
