package cn.poco.beautify;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

import cn.poco.tsv100.SimpleBtnList100;

/**
 * 在SimpleBtnList100上加了长按监听, name 唯一
 */
public class MySimpleBtnList extends SimpleBtnList100
{
	protected boolean showDelete = false;
	protected Callback m_cb;
	public MySimpleBtnList(Context context)
	{
		super(context);
	}

	public void SetData(ArrayList<SimpleBtnList100.Item> datas, Callback cb)
	{
		m_cb = cb;

		m_fr.removeAllViews();
		m_datas.clear();

		m_datas = datas;
		if(m_datas != null)
		{
			SimpleBtnList100.Item item;
			int len = m_datas.size();
			for(int i = 0; i < len; i++)
			{
				item = m_datas.get(i);
				m_fr.addView(item);
				item.setOnClickListener(m_clickLst);
				if(item instanceof Item)
				{
					((Item)item).m_deleteBtn.setOnClickListener(m_clickLst);
				}
				item.setOnLongClickListener(m_longClickLst);
				item.SetOut(i);
			}
		}

		if(showDelete)
		{
			ShowDeleteBtn();
		}
	}

	protected View.OnClickListener m_clickLst = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(m_datas != null)
			{
				SimpleBtnList100.Item item;
				int len = m_datas.size();
				for(int i = 0; i < len; i++)
				{
					item = m_datas.get(i);
					if(item == v)
					{
						if(m_cb != null)
						{
							m_cb.OnClick(item, i, showDelete);
						}
						break;
					}
					else if(item instanceof Item && ((Item)item).m_deleteBtn == v)
					{
						if(m_cb != null)
						{
							m_cb.OnDeleteBtn(item, i);
						}
						break;
					}
				}
			}
		}
	};

	public void HideDeleteBtn(String uri)
	{
		if(m_datas != null && showDelete)
		{
			int size = m_datas.size();

			for(int i = 0; i < size; i ++)
			{
				if(m_datas.get(i) instanceof  Item)
				{
					((Item)m_datas.get(i)).OnLongSelect(false);
				}
				if(m_datas.get(i) instanceof MySimpleListItem)
				{
					if(((MySimpleListItem)m_datas.get(i)).m_uri.equals(uri))
					{
						m_selIndex = i;
					}
				}

			}
		}
		SetSelByIndex(m_selIndex);
		showDelete = false;
	}

	public void SetSelByUri(String uri)
	{

	}

	public void ShowDeleteBtn()
	{
		showDelete = true;
		if(m_datas != null)
		{
			int size = m_datas.size();

			for(int i = 0; i < size; i ++)
			{
				if(m_datas.get(i) instanceof Item)
				{
					((Item)m_datas.get(i)).OnLongSelect(true);
					m_datas.get(i).SetOut(i);
				}
			}
		}
	}

	@Override
	public void ClearAll()
	{
		super.ClearAll();
		m_cb = null;
	}

	protected View.OnLongClickListener m_longClickLst = new OnLongClickListener()
	{
		@Override
		public boolean onLongClick(View v)
		{
			if(m_cb != null)
			{
				m_cb.OnLongClick(true);
			}
			return true;
		}
	};

	public interface Callback
	{
		public void OnDeleteBtn(SimpleBtnList100.Item view, int index);

		public  void OnLongClick(boolean select);

		public void OnClick(SimpleBtnList100.Item view, int index, boolean showDelete);
	}

	public static class Item extends SimpleBtnList100.Item
	{
		public ImageView m_deleteBtn;
		public Item(Context context)
		{
			super(context);
			m_deleteBtn = new ImageView(context);
		}

		public Item(Context context, AttributeSet attrs)
		{
			super(context, attrs);
			m_deleteBtn = new ImageView(context);
		}

		public Item(Context context, AttributeSet attrs, int defStyleAttr)
		{
			super(context, attrs, defStyleAttr);
			m_deleteBtn = new ImageView(context);
		}

		public void OnLongSelect(boolean select)
		{

		}
	}
}
