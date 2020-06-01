package cn.poco.video.frames;

import android.content.Context;
import android.view.MotionEvent;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;

/**
 * 视频页面基类
 */

public abstract class VideoPage extends IPage
{

	private boolean mUiEnable = true;

	public VideoPage(Context context, BaseSite baseSite)
	{
		super(context, baseSite);
	}

	public int getBottomPartHeight() {
	    return 0;
	};

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		if(mUiEnable)
		{
			return super.onInterceptTouchEvent(ev);
		}else{
			return true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(mUiEnable)
		{
			return super.onTouchEvent(event);
		}else{
			return true;
		}
	}

	public void setUiEnable(boolean uiEnable)
	{
		this.mUiEnable = uiEnable;
	}
}
