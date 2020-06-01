package cn.poco.framework;

import android.content.Context;

import com.baidu.mobstat.StatService;

import cn.poco.framework2.BaseActivitySite;
import cn.poco.framework2.BaseFrameworkActivity;

/**
 * Created by Raining on 2017/11/30.
 */

public abstract class BaseFwActivity<T extends BaseActivitySite> extends BaseFrameworkActivity<T>
{
	@Override
	protected String getAppPackName(Context context)
	{
		return "cn.poco.interphoto2";
	}

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
