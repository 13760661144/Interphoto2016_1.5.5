package cn.poco.Text;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * 包含吸取工具的颜色切换工具UI
 */

public class ColorChangeLayout1 extends LinearLayout
{
	private ArrayList<ItemInfo> m_ress;
	private ArrayList<ImageView> m_views;
	private int m_rowCount = 6;  	//每行有多少个元素
	private int m_curSelIndex = -1;

	private ImageView m_obsorbBtn;	//吸取工具
	private boolean m_obsorbing = false;
	private LinearLayout m_colorFr;
	private ItemOnClickListener m_cb;

	public ColorChangeLayout1(Context context)
	{
		super(context);
	}

	public void SetDatas(ArrayList<ItemInfo> ress, int rowCount)
	{
		m_ress = ress;
		if(rowCount > 0)
		{
			m_rowCount = rowCount;
		}
		m_views = new ArrayList<>();

		InitUI();
	}

	private void InitUI()
	{
		this.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		LinearLayout.LayoutParams ll;
		m_colorFr = new LinearLayout(getContext());
		m_colorFr.setOrientation(VERTICAL);
		m_colorFr.setGravity(Gravity.CENTER_VERTICAL);
		ll = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(170));
		ll.weight = 1;
		ll.leftMargin = ShareData.PxToDpi_xhdpi(25);
		m_colorFr.setLayoutParams(ll);
		addView(m_colorFr);
		if(m_ress != null && m_ress.size() > 0)
		{
			int row = (int)Math.ceil(m_ress.size() / m_rowCount);
			for(int i = 0; i < row; i ++)
			{
				LinearLayout container = new LinearLayout(getContext());
				ll = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				if(i > 0)
				{
					ll.topMargin = ShareData.PxToDpi_xhdpi(4);
				}
				container.setLayoutParams(ll);
				m_colorFr.addView(container);
				{
					ImageView view;
					int rowNum = m_rowCount;
					if(i == row - 1)
					{
						rowNum = m_ress.size() - m_rowCount * i;
					}
					for(int k = 0; k < rowNum; k ++)
					{
						view = new ImageView(getContext());
						view.setImageResource(R.drawable.beautify_text_base_color_out);
						ll = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
						ll.weight = 1;
						view.setLayoutParams(ll);
						container.addView(view);
						view.setOnClickListener(m_btnLst);
						m_views.add(view);

						view.setColorFilter(m_ress.get(m_views.size() - 1).m_color,	PorterDuff.Mode.SRC_IN);
					}
				}

			}
		}

		ImageView line = new ImageView(getContext());
		line.setBackgroundColor(0xff333333);
		ll = new LayoutParams(1, ShareData.PxToDpi_xhdpi(140));
		ll.leftMargin = ShareData.PxToDpi_xhdpi(20);
		line.setLayoutParams(ll);
		addView(line);

		m_obsorbBtn = new ImageView(getContext());
		m_obsorbBtn.setOnClickListener(m_btnLst);
		m_obsorbBtn.setImageResource(R.drawable.beautify_text_color_absorb_out);
		ll = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		ll.leftMargin = ShareData.PxToDpi_xhdpi(20);
		ll.rightMargin = ll.leftMargin;
		m_obsorbBtn.setLayoutParams(ll);
		addView(m_obsorbBtn);
	}

	public void setSelectedItemByColor(int color)
	{
		boolean flag = false;
		if(m_ress != null)
		{
			int size = m_ress.size();
			for(int i = 0; i < size; i ++)
			{
				int color1 = Painter.GetColorWithAlphaUnchanged(m_ress.get(i).m_color, color);
				if(m_ress.get(i).m_color == color1)
				{
					flag = true;
					setSelecteItemByIndex(i);
					break;
				}
			}
		}
		if(!flag)
		{
			setSelecteItemByIndex(-1);
		}
	}

	public void setSelecteItemByIndex(int index)
	{
		if(m_views != null)
		{
			if(m_curSelIndex >= 0 && m_curSelIndex < m_views.size())
			{
				m_views.get(m_curSelIndex).setImageResource(R.drawable.beautify_text_base_color_out);
			}

			if(index >= 0 && index < m_views.size())
			{
				m_views.get(index).setImageResource(R.drawable.beautify_text_base_color_over);
				m_curSelIndex = index;
			}
		}
	}

	public void cancelAbsorbing()
	{
		if(m_obsorbing)
		{
			m_btnLst.onClick(m_obsorbBtn);
		}
	}

	private View.OnClickListener m_btnLst = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v == m_obsorbBtn)
			{
				m_obsorbing = !m_obsorbing;
				if(m_obsorbing)
				{
					m_obsorbBtn.setImageResource(R.drawable.beautify_text_color_absorb_over);
				}
				else
				{
					m_obsorbBtn.setImageResource(R.drawable.beautify_text_color_absorb_out);
				}
				if(m_cb != null)
				{
					m_cb.onObsorbClick(m_obsorbing);
				}
				if(m_obsorbing)
				{
					setSelecteItemByIndex(-1);
				}
			}
			else if(m_views != null)
			{
				int len = m_views.size();
				for(int i = 0; i < len; i ++)
				{
					if(v == m_views.get(i))
					{
						setSelecteItemByIndex(i);
						if(m_cb != null)
						{
							m_cb.onColorItemClick(m_ress.get(i).m_color, i);
						}
						if(m_obsorbing)
						{
							onClick(m_obsorbBtn);
						}
					}
				}
			}
		}
	};

	public void setItemOnClickListener(ItemOnClickListener listener)
	{
		m_cb = listener;
	}

	public interface ItemOnClickListener{
		public void onObsorbClick(boolean absorb);
		public void onColorItemClick(int color, int index);
	}

	public static class ItemInfo
	{
		public int m_uri;
		public int m_color;
	}
}
