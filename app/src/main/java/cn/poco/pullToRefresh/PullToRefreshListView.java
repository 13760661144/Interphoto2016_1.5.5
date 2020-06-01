package cn.poco.pullToRefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

public class PullToRefreshListView extends PullToRefreshBase
{
	private static final int STATE_TOP = 1;
	private static final int STATE_BOTTOM = 2;
	private static final int STATE_OTHER = 3;
	
	private ListView m_refreshView;
	private int state = STATE_TOP;

	public PullToRefreshListView(Context context)
	{
		super(context);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected View createRefreshableView(Context context, AttributeSet attrs)
	{
		m_refreshView = new ListView(context);
		m_refreshView.setOnScrollListener(m_scrollListener);
		return m_refreshView;
	}
	
	public ListView getRefreshView()
	{
		return m_refreshView;
	}

	@Override
	protected boolean isReadyToPullDown()
	{
		if(state == STATE_TOP)
			return true;
		return false;
	}
	
	private AbsListView.OnScrollListener m_scrollListener = new AbsListView.OnScrollListener()
	{
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
		{
			if(firstVisibleItem == 0)
			{
				state = STATE_TOP;
			}
			else if(firstVisibleItem + visibleItemCount == totalItemCount)
			{
				state = STATE_BOTTOM;
			}	
			else
			{
				state = STATE_OTHER;
			}
		}
	};
}
