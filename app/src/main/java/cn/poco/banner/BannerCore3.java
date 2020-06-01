package cn.poco.banner;

import android.content.Context;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import cn.poco.framework.EventCenter;
import cn.poco.framework.EventID;
import cn.poco.framework.MyFramework2App;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.statistics.TongJi2;
import cn.poco.utils.Utils;

public class BannerCore3
{
	public String m_pos;
	public String m_defCmdStr;
//	public BannerRes m_res;
	public Callback m_cb;

	protected EventCenter.OnEventListener m_xmlCB;
//	protected DownloadResCallback m_resCB;

	protected boolean m_showTj = false; //UI显示调用后为true
	protected boolean m_sendShowTj = false; //只发送一次

	public BannerCore3(String pos, Callback cb)
	{
		m_pos = pos;
		m_cb = cb;
//		m_resCB = new DownloadResCallback();
//		m_resCB.m_banner = this;
	}


	public static class CmdStruct
	{
		public String m_cmd;
		public String[] m_params;
	}

	public static CmdStruct GetCmdStruct(String cmdStr)
	{
		CmdStruct out = null;

		if(cmdStr != null)
		{
			String cmd = null;
			String[] params = null;

			int pos = cmdStr.indexOf("://");
			if(pos > -1)
			{
				int pos2 = cmdStr.indexOf("/?", pos + 3);
				if(pos2 > -1)
				{
					cmd = cmdStr.substring(pos + 3, pos2);
					pos = pos2 + 1;
				}
				else
				{
					cmd = cmdStr.substring(0, pos);
					pos += 3;
				}
			}
			/*int pos3 = cmdStr.indexOf("?", pos);
			if(pos3 > -1)
			{
				pos = pos3 + 1;
			}*/
			if(pos > -1 && pos < cmdStr.length())
			{
				String temp = cmdStr.substring(pos);
				if(temp != null)
				{
					params = temp.split("&");
				}
			}

			out = new CmdStruct();
			out.m_cmd = cmd;
			out.m_params = params;
		}

		return out;
	}

	/*protected void OnDownloadResComplete(BannerRes res, boolean isFinal)
	{
		m_res = res;

		if(m_cb != null)
		{
			m_cb.ShowBanner(this);
		}
	}*/

	public void SetDefCmdStr(String cmdStr)
	{
		m_defCmdStr = cmdStr;
	}

	/*public void OnClick(Context context, CmdCallback cb)
	{
		String cmdStr = m_defCmdStr;
		if(m_res != null)
		{
			if(m_res.m_tjClickUrl != null)
			{
				Utils.UrlTrigger(context, m_res.m_tjClickUrl);
			}
			cmdStr = m_res.m_cmdStr;
		}
		if(cmdStr != null && cmdStr.length() > 0)
		{
			ExecuteCommand(context, cmdStr, cb);
		}
	}*/

	public void ShowTj()
	{
		m_showTj = true;
//		SendShowTj(m_res);
	}

	/*protected void SendShowTj(BannerRes res)
	{
		if(!m_sendShowTj && res != null && res.m_tjShowUrl != null && res.m_tjShowUrl.length() > 0)
		{
			m_sendShowTj = true;
			if(res.m_tjShowUrl.startsWith("http"))
			{
				Utils.UrlTrigger(MyFramework2App.getInstance().getApplicationContext(), res.m_tjShowUrl);
			}
			else
			{
				TongJi2.AddCountById(res.m_tjShowUrl);
			}
		}
	}*/

	/**
	 * 必须调用,否则有内存泄漏
	 */
	public void ClearAll()
	{
		if(m_xmlCB != null)
		{
			EventCenter.removeListener(m_xmlCB);
			m_xmlCB = null;
		}
		/*if(m_resCB != null)
		{
			m_resCB.m_banner = null;
			m_resCB = null;
		}*/
		m_cb = null;
	}

	public static HashMap<String, String> DecodeParams(String[] params)
	{
		HashMap<String, String> out = new HashMap<String, String>();

		if(params != null)
		{
			String[] pair;
			for(int i = 0; i < params.length; i++)
			{
				pair = params[i].split("=");
				if(pair != null && pair.length == 2)
				{
					out.put(pair[0], pair[1]);
				}
			}
		}

		return out;
	}

	public static void ExecuteCommand(Context context, String cmdStr, CmdCallback cb)
	{
		try
		{
			if(cmdStr != null && cb != null)
			{
				CmdStruct struct = GetCmdStruct(cmdStr);
				if(struct != null)
				{
					if(struct.m_cmd != null)
					{
						String cmd = struct.m_cmd.toLowerCase(Locale.ENGLISH);
						if(cmd.equals("jane"))
						{
							cb.OpenJane(context);
						}
						else if(cmd.equals("pococamera"))
						{
							cb.OpenPocoCamera(context);
						}
						else if(cmd.equals("artcamera20140919"))
						{
							cb.OpenPocoMix(context);
						}
						else if(cmd.equals("beautycamera"))
						{
							cb.OpenBeautyCamera(context);
						}
						else if(cmd.equals("interphoto"))
						{
							if(struct.m_params != null && struct.m_params.length > 0)
							{
								String[] pair = struct.m_params[0].split("=");
								if(pair.length == 2)
								{
									if(pair[0].equals("open"))
									{
										if(pair[1].equals("month") || pair[1].equals("login"))
										{
											String[] datas = null;
											if(struct.m_params.length >= 2)
											{
												datas = struct.m_params[1].split("=");
											}
											cb.OpenPage(context, -1, pair[1], datas != null ? URLDecoder.decode(datas[1]):null);
										}
										else
										{
											cb.OpenPage(context, Integer.parseInt(URLDecoder.decode(pair[1])), struct.m_params);
										}
									}
									else if(pair[0].equals("openmyweb"))
									{
										String[] datas = null;
										if(struct.m_params.length >= 2)
										{
											datas = struct.m_params[1].split("=");
										}
										cb.OpenMyWebPage(context, URLDecoder.decode(pair[1]), datas != null ? datas[1]:null);
									}
									else if(pair[0].equals("openweb"))
									{
										cb.OpenWebPage(context, URLDecoder.decode(pair[1]));
									}
								}
							}
						}
						else
						{
							cb.OpenWebPage(context, cmdStr);
						}
					}
					else
					{
						cb.OpenWebPage(context, cmdStr);
					}
				}
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public static boolean IsOutsideCmd(String cmdStr)
	{
		boolean out = false;
		CmdStruct struct = GetCmdStruct(cmdStr);
		if(struct != null && struct.m_cmd != null)
		{
			if(struct.m_cmd.equals("jane") || struct.m_cmd.equals("pococamera") || struct.m_cmd.equals("ArtCamera20140919") || struct.m_cmd.equals("beautycamera"))
			{
				out = true;
			}
		}
		return out;
	}

	public interface CmdCallback
	{
		/**
		 * @param code
		 * @param args
		 */
		public void OpenPage(Context context, int code, String... args);

		/**
		 * @param context
		 * @param args    [0]URL<br/>
		 */
		public void OpenWebPage(Context context, String... args);

		/**
		 * @param context
		 * @param args    [0]URL<br/>
		 */
		public void OpenMyWebPage(Context context, String... args);

		public void OpenPocoCamera(Context context, String... args);

		public void OpenPocoMix(Context context, String... args);

		public void OpenJane(Context context, String... args);

		public void OpenBeautyCamera(Context context, String... args);

		/**
		 * @param args [0]channel_value
		 */
		public void OpenBusinessPage(Context context, String... args);
	}

	/*protected static class DownloadResCallback implements DownloadMgr.Callback
	{
		public BannerCore3 m_banner;

		@Override
		public void OnProgress(int downloadId, IDownload res, int progress)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void OnComplete(int downloadId, IDownload res)
		{
			if(m_banner != null)
			{
				m_banner.OnDownloadResComplete((BannerRes)res, true);
			}
		}

		@Override
		public void OnFail(int downloadId, IDownload res)
		{
			// TODO Auto-generated method stub
		}
	}*/

	public interface Callback
	{
		public void ShowBanner(BannerCore3 banner);
	}
}
