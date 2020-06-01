package cn.poco.album2.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;

/**
 * Created by: fwc
 * Date: 2017/7/31
 */
public class MyViewPager extends ViewPager {

	private boolean isScroll = true;

	public MyViewPager(Context context) {
		super(context);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return isScroll && super.onInterceptTouchEvent(ev);
	}

	public void setCanScroll(boolean scroll) {
		isScroll = scroll;
	}
}
