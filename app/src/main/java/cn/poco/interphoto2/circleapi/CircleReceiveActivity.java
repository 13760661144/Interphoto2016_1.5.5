package cn.poco.interphoto2.circleapi;

import android.app.Activity;
import android.os.Bundle;

import com.taotie.cn.circlesdk.CircleSDK;
import com.taotie.cn.circlesdk.ICIRCLEAPI;

/**
 * Created by pocouser on 2017/3/10.
 */

public class CircleReceiveActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ICIRCLEAPI circleApi = CircleSDK.createApi(this, 4);
		circleApi.handleCallBackMessage(getIntent(), this);
	}
}
