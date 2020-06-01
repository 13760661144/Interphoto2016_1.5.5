package cn.poco.interphoto2;

import android.app.Activity;

import com.baidu.mobstat.StatService;

/**
 * 无框架，只适合接受数据用
 */
public abstract class BaseActivity extends Activity
{
	@Override
	protected void onResume()
	{
		super.onResume();

		try
		{
			StatService.onResume(this);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause()
	{
		try
		{
			StatService.onPause(this);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

		super.onPause();
	}
}
