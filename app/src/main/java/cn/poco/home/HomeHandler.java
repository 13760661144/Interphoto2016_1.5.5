package cn.poco.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import cn.poco.image.filter;

public class HomeHandler extends Handler
{
	public static final int MSG_CYC_QUEUE = 0x00000008; // 处理消息队列
	public static final int MSG_UPDATE_UI = 0x00000010; //更新界面

	protected Context m_context;
	protected Handler m_UIHandler;
	protected QueueItem m_queue;

	public HomeHandler(Looper looper, Context context, Handler ui)
	{
		super(looper);

		m_context = context;
		m_UIHandler = ui;
	}

	@Override
	public void handleMessage(Message msg)
	{
		switch(msg.what)
		{
			case MSG_CYC_QUEUE:
			{
				QueueItem item = GetItem();
				if(item != null)
				{
					MakeGlassBk(item.m_bmp);

					Message ui_msg = m_UIHandler.obtainMessage();
					ui_msg.obj = item;
					ui_msg.what = MSG_UPDATE_UI;
					m_UIHandler.sendMessage(ui_msg);
				}
				break;
			}

			default:
				break;
		}
	}

	protected synchronized QueueItem GetItem()
	{
		QueueItem out = m_queue;
		m_queue = null;
		return out;
	}

	public synchronized void AddItem(QueueItem item)
	{
		if(m_queue != null)
		{
			if(m_queue.m_bmp != null)
			{
				m_queue.m_bmp.recycle();
				m_queue.m_bmp = null;
			}
		}
		m_queue = item;
	}

	public static void MakeGlassBk(Bitmap bmp)
	{
		filter.fakeGlass(bmp, 0x26000000);
	}

	public static class QueueItem
	{
		//in out
		public int m_index;
		public Bitmap m_bmp;
	}
}
