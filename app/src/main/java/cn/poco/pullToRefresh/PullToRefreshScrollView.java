package cn.poco.pullToRefresh;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import cn.poco.tianutils.ShareData;

public class PullToRefreshScrollView extends PullToRefreshBase
{
	private ScrollView m_refreshView;

	public PullToRefreshScrollView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
		
		setPullToRefreshEnabled(true);
		ShareData.InitData((Activity)context);
	}

	@Override
	public View createRefreshableView(Context context, AttributeSet attrs)
	{
		// TODO Auto-generated method stub
		m_refreshView = new ScrollView(context);
		
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, ShareData.m_screenHeight - ShareData.PxToDpi_xhdpi(122));
		m_refreshView.setLayoutParams(ll);
		
		return m_refreshView;
	}

	@Override
	protected boolean isReadyToPullDown()
	{
		// TODO Auto-generated method stub
		if(m_refreshView.getScrollY() == 0)
		{
			return true;
		}
		return false;
	}
	
	public void addChildView(View view)
	{
		if(null == m_refreshView)
		{
			throw new NullPointerException("refreshView can not be null.");
		}
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		m_refreshView.addView(view, fl);
	}
	
	public void setRefreshViewHeight(int height)
	{
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, height);
		m_refreshView.setLayoutParams(ll);
	}
}
