package cn.poco.statistics;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;
import java.util.ArrayList;

import cn.poco.system.SysConfig;
import cn.poco.tianutils.NetCore2;
import cn.poco.tianutils.NetState;
import cn.poco.tianutils.ShareData;

public class CountCoreV3
{
	private final Object DATABASE_LOCK = new Object();

	private static final String BASE_GET_URL = "http://phtj.poco.cn/phone_tj.php";
	private static final String BASE_POST_URL = "http://phtjp.poco.cn/phone_tj_post.php";
	private static final String TJ_VER = "2.0";
	private static final String CLIENT_ID = "129_3"; //不同应用不同,自行修改
	private static final String APP_OS = "android";

	private static final String DATABASE_NAME = "poco_count";
	private static final String TABLE_NAME_COUNT = "dyn_count";
	private static final String TABLE_ID = "_id";
	private static final String TABLE_TIME = "_time";
	private static final String TABLE_NAME_USE = "use_count";
	private static final String TABLE_NUM = "_num";

	protected Context m_appContext;

	protected SQLiteDatabase m_database;

	public CountCoreV3(Activity ac)
	{
		ShareData.InitData(ac);
		m_appContext = ac.getApplicationContext();

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized(DATABASE_LOCK)
				{
					try
					{
						m_database = m_appContext.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
						m_database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_COUNT + "(" + TABLE_ID + " TEXT NOT NULL, " + TABLE_TIME + " TEXT NOT NULL)");
						m_database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME_USE + "(" + TABLE_NUM + " INTEGER)");
					}
					catch(Throwable e)
					{
						m_database = null;
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * 报活和发送离线包
	 */
	public void IAmLive()
	{
		if(NetState.NET_NONE != NetState.GetConnectNet(m_appContext))
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						String database_get = null;
						ArrayList<NetCore2.FormData> arr_post = null;
						synchronized(DATABASE_LOCK)
						{
							if(m_database != null)
							{
								database_get = GetParams(m_appContext, m_database, true);

								String LINEND = "\r\n";
								String KEY1 = "event_id=";
								String KEY2 = "&event_time=";

								StringBuffer data = new StringBuffer(4096);
								data.append(GetParams(m_appContext, m_database, false));

								Cursor cur = m_database.query(TABLE_NAME_COUNT, null, null, null, null, null, null);
								if(cur != null && cur.getCount() > 0)
								{
									cur.moveToFirst();
									int idIndex = cur.getColumnIndex(TABLE_ID);
									int timeIndex = cur.getColumnIndex(TABLE_TIME);
									while(!cur.isAfterLast())
									{
										data.append(LINEND);
										data.append(KEY1);
										data.append(cur.getString(idIndex));
										data.append(KEY2);
										data.append(cur.getString(timeIndex));

										cur.moveToNext();
									}
									cur.close();
								}

								//System.out.println(data.toString());
								NetCore2.FormData formData = new NetCore2.FormData();
								formData.m_name = "tongji";
								formData.m_filename = "events-" + CLIENT_ID + "-" + System.currentTimeMillis() + "." + this.hashCode() + ".txt";
								//System.out.println(formData.m_filename);
								formData.m_data = data.toString().getBytes();
								arr_post = new ArrayList<NetCore2.FormData>();
								arr_post.add(formData);
							}
						}

						//发送报活包
						NetCore2 net = new NetCore2();
						if(database_get != null)
						{
							//System.out.println(BASE_GET_URL + "?" + database_get);
							//NetCore.NetMsg msg = 
							net.HttpGet(BASE_GET_URL + "?" + database_get);
							//System.out.println("get finish....." + msg.m_stateCode + ":" + msg.m_msg);
						}
						//发送离线包
						if(arr_post != null)
						{
							NetCore2.NetMsg msg = net.HttpPost(BASE_POST_URL, null, arr_post, true);
							if(msg != null && msg.m_data != null && new String(msg.m_data).contains("OK"))
							{
								//System.out.println("delete all data");
								synchronized(DATABASE_LOCK)
								{
									if(m_database != null)
									{
										m_database.execSQL("DELETE FROM " + TABLE_NAME_COUNT);
									}
								}
							}
							//System.out.println("post finish....." + msg.m_stateCode + ":" + (msg.m_msg == null ? "null" : msg.m_msg));
						}
						net.ClearAll();
					}
					catch(Throwable e)
					{
						e.printStackTrace();
					}
				}
			}).start();
		}
	}

	public void AddCount(final String id, final String time)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized(DATABASE_LOCK)
				{
					if(m_database != null)
					{
						try
						{
							m_database.execSQL("INSERT INTO " + TABLE_NAME_COUNT + "(" + TABLE_ID + ", " + TABLE_TIME + ") VALUES(\'" + id + "\', \'" + time + "\')");
						}
						catch(Throwable e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

	private Integer temp_runNum = null;

	/**
	 * 获取用户信息
	 *
	 * @param encode 是否使用url编码
	 * @return
	 */
	protected String GetParams(Context context, SQLiteDatabase database, boolean encode)
	{
		String client_ver = SysConfig.GetAppVer(context);
		String uid = ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		if(uid == null)
		{
			uid = "0";
		}
		String os_ver = android.os.Build.VERSION.RELEASE;
		if(os_ver == null)
		{
			os_ver = "";
		}
		if(temp_runNum == null)
		{
			temp_runNum = 1;
			Cursor cur = database.query(TABLE_NAME_USE, null, null, null, null, null, null);
			if(cur != null && cur.getCount() > 0)
			{
				cur.moveToFirst();
				int index = cur.getColumnIndex(TABLE_NUM);
				if(index >= 0)
				{
					temp_runNum = cur.getInt(index);
					temp_runNum++;
					database.execSQL("UPDATE " + TABLE_NAME_USE + " SET " + TABLE_NUM + " = " + temp_runNum);
				}
				cur.close();
			}
			else
			{
				//数据表空,插入数据
				database.execSQL("INSERT INTO " + TABLE_NAME_USE + "(" + TABLE_NUM + ") VALUES(" + temp_runNum + ")");
			}
		}
		String run_num = "" + temp_runNum;
		String run_interval = "0";
		String sub_type = "use";
		String phone_type = android.os.Build.MODEL;
		if(phone_type == null)
		{
			phone_type = "";
		}
		String screen = ShareData.m_screenWidth + "*" + ShareData.m_screenHeight;
		String memory = GetTotalMemory(context);

		StringBuffer params = new StringBuffer(512);
		params.append("uid=");
		params.append(encode ? URLEncoder.encode(uid) : uid);
		params.append("&screen=");
		params.append(encode ? URLEncoder.encode(screen) : screen);
		params.append("&run_num=");
		params.append(encode ? URLEncoder.encode(run_num) : run_num);
		params.append("&os=");
		params.append(encode ? URLEncoder.encode(APP_OS) : APP_OS);
		params.append("&tj_ver=");
		params.append(encode ? URLEncoder.encode(TJ_VER) : TJ_VER);
		params.append("&phone_type=");
		params.append(encode ? URLEncoder.encode(phone_type) : phone_type);
		params.append("&os_ver=");
		params.append(encode ? URLEncoder.encode(os_ver) : os_ver);
		params.append("&sub_type=");
		params.append(encode ? URLEncoder.encode(sub_type) : sub_type);
		params.append("&client_ver=");
		params.append(encode ? URLEncoder.encode(client_ver) : client_ver);
		params.append("&client_id=");
		params.append(encode ? URLEncoder.encode(CLIENT_ID) : CLIENT_ID);
		params.append("&run_interval=");
		params.append(encode ? URLEncoder.encode(run_interval) : run_interval);
		params.append("&memory=");
		params.append(encode ? URLEncoder.encode(memory) : memory);

		return params.toString();
	}

	protected String GetTotalMemory(Context context)
	{
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try
		{
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

			arrayOfString = str2.split("\\s+");

			initial_memory = Long.parseLong(arrayOfString[1]) * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
			localBufferedReader.close();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化
	}

	public void ClearAll()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized(DATABASE_LOCK)
				{
					if(m_database != null)
					{
						m_database.close();
						m_database = null;
					}
				}
			}
		}).start();
	}
}
