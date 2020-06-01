package cn.poco.share;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

import cn.poco.blogcore.WeiXinBlog;


public class SendWXAPI 
{
	public interface WXCallListener
	{
		void onCallFinish(int result);
	}
	
	private static ArrayList<WXCallListener> sWXCallListeners = new ArrayList<WXCallListener>();
	
	/**
     * 发送url到微信
     * @param context 
     * @param url url地址
     * @param title 标题内容，可以为null，但内容一旦为null会被微信自动填上不相干内容，建议不为null
     * @param description 介绍内容，可以为null，只在发送好友里显示，朋友圈中不显示
     * @param thumb 缩略图（最大边长150以下）
     * @param WXSceneSession true为发送到微信好友，false为发送到微信朋友圈
     */
    public static boolean sendUrlToWeiXin(Context context, String url, String title, String description, Bitmap thumb, boolean WXSceneSession)
    {
    	if(context != null && url != null && url.length() > 0)
    	{	
    		WeiXinBlog mWeiXin = new WeiXinBlog(context);
			if(mWeiXin.sendUrlToWeiXin(url, title, description, thumb, WXSceneSession)) return true;
    		else WeiXinBlog.showErrorMessageToast(context, mWeiXin.LAST_ERROR, WXSceneSession);
    	}
    	return false;
    }
	
	
    public static void dispatchResult(final int result)
	{
		synchronized(sWXCallListeners)
		{
			if(sWXCallListeners.size() > 0)
			{
				Handler handler = new Handler(Looper.getMainLooper());
				for(WXCallListener l : sWXCallListeners)
				{
					if(l != null)
					{
						final WXCallListener listener = l;
						handler.post(new Runnable()
						{
							@Override
							public void run() {
								listener.onCallFinish(result);
							}
						});
					}
				}
			}
		}
	}
	
	public static void addListener(WXCallListener l)
	{
		sWXCallListeners.add(l);
	}
	
	public static void removeAllListener()
	{
		synchronized(sWXCallListeners)
		{
			sWXCallListeners.clear();
		}
	}
	
	public static void removeListener(WXCallListener l)
	{
		if(sWXCallListeners.size() > 0)
		{
			for(int i = 0; i < sWXCallListeners.size(); i++)
			{
				if(l == sWXCallListeners.get(i))
				{
					sWXCallListeners.remove(i);
					i--;
				}
			}
		}
	}
}
