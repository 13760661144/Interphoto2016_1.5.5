package cn.poco.login.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.net.HttpURLConnection;

import cn.poco.exception.MyApplication;
import cn.poco.framework.EventCenter;
import cn.poco.framework.EventID;
import cn.poco.interphoto2.R;
import cn.poco.loginlibs.LoginUtils;
import cn.poco.loginlibs.info.LoginInfo;
import cn.poco.loginlibs.info.UserInfo;
import cn.poco.setting.SettingInfo;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.system.AppInterface;
import cn.poco.system.FolderMgr;
import cn.poco.tianutils.NetCore2;
import cn.poco.utils.MyNetCore;

public class UserMgr
{
	public static final String HEAD_PATH = FolderMgr.getInstance().USER_INFO + File.separator + "head2.img"; //显示头像的真实路径
	public static final String HEAD_TEMP_PATH = FolderMgr.getInstance().USER_INFO_TEMP + File.separator + "head2.img"; //裁剪临时文件
	public static final String TEMP_IMG_PATH = FolderMgr.getInstance().USER_INFO_TEMP + File.separator + "temp2.img"; //下载的临时文件

	static
	{
		new File(FolderMgr.getInstance().USER_INFO).mkdirs();
		new File(FolderMgr.getInstance().USER_INFO_TEMP).mkdirs();
	}

	public UserMgr()
	{
	}

	public synchronized static UserInfo ReadCache(Context context)
	{
		context = context.getApplicationContext();
		UserInfo out = null;

		SettingInfo settingInfo = SettingInfoMgr.GetSettingInfo(context);
		if(settingInfo != null)
		{
			String id = settingInfo.GetPoco2Id(true);
			String token = settingInfo.GetPoco2Token(true);
			if(id != null && id.length() > 0 & token != null && token.length() > 0)
			{
				out = new UserInfo();
				out.mZoneNum = settingInfo.GetPoco2AreaCode();
				out.mUserIcon = settingInfo.GetPoco2HeadUrl();
				out.mUserId = settingInfo.GetPoco2Id(false);
				out.mMobile = settingInfo.GetPoco2Phone();
				out.mBirthdayYear = settingInfo.GetPoco2BirthdayYear();
				out.mBirthdayMonth = settingInfo.GetPoco2BirthdayMonth();
				out.mBirthdayDay = settingInfo.GetPoco2BirthdayDay();
				try
				{
					out.mFreeCredit = Integer.parseInt(settingInfo.GetPoco2Credit());
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
				out.mNickname = settingInfo.GetPocoNick();
				out.mSex = settingInfo.GetPoco2Sex();
				out.mLocationId = settingInfo.GetPoco2LocationId();
			}
		}

		return out;
	}

	public synchronized static void SaveCache(Context context,UserInfo info)
	{
		if(info != null)
		{
			context = context.getApplicationContext();
			SettingInfo settingInfo = SettingInfoMgr.GetSettingInfo(context);
			settingInfo.SetPoco2Id(info.mUserId);
			settingInfo.SetPoco2Phone(info.mMobile);
			settingInfo.SetPoco2HeadUrl(info.mUserIcon);
			settingInfo.SetPoco2Credit(Integer.toString(info.mFreeCredit));
			settingInfo.SetPoco2AreaCode(info.mZoneNum);
			settingInfo.SetPoco2BirthdayYear(info.mBirthdayYear);
			settingInfo.SetPoco2BirthdayMonth(info.mBirthdayMonth);
			settingInfo.SetPoco2BirthdayDay(info.mBirthdayDay);
			settingInfo.SetPocoNick(info.mNickname);
			settingInfo.SetPoco2Sex(info.mSex);
			settingInfo.SetPoco2LocationId(info.mLocationId);
			settingInfo.SetPoco2RegisterTime(info.mUserRegisterTime);
			SettingInfoMgr.Save(context);

			MyBeautyStat.checkLogin(MyApplication.getInstance());//统计
		}
	}

	public interface LoginCallback
	{
		public void OnLogin(LoginInfo loginInfo);

		public void ExitLogin();
	}

	/**
	 * 系统读取CONFIG后才能调用
	 *
	 * @return
	 */
	public static boolean IsLogin(final Context context,final LoginCallback cb)
	{
		boolean out = false;
		context.getApplicationContext();
		SettingInfo info = SettingInfoMgr.GetSettingInfo(context);
		final String id = info.GetPoco2Id(true);
		final String accessToken = info.GetPoco2Token(true);
		if(id != null && id.length() > 0 && accessToken != null && accessToken.length() > 0)
		{
			out = true;
		}
		else
		{
			if(cb != null)
			{
				final String id2 = info.GetPoco2Id(false);
				final String accessToken2 = info.GetPoco2Token(false);
				final String refreshToken = info.GetPoco2RefreshToken();
				if(id2 != null && id2.length() > 0 && accessToken2 != null && accessToken2.length() > 0 && refreshToken != null && refreshToken.length() > 0)
				{
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							final LoginInfo info = LoginUtils.refreshToken(id2, refreshToken, AppInterface.GetInstance(context));
							Handler handler = new Handler(Looper.getMainLooper());
							handler.post(new Runnable()
							{
								@Override
								public void run()
								{
									if(info != null)
									{
										if(info.mCode == 0)
										{
											LoginOtherUtil.setSettingInfo(context,info);
											cb.OnLogin(info);
										}
										else if(info.mCode == 216)
										{
											ExitLogin(context);
											cb.ExitLogin();
										}
									}
									else
									{
										ExitLogin(context);
										cb.ExitLogin();
									}
								}
							});
						}
					}).start();
				}
				else
				{
					ExitLogin(context);
					cb.ExitLogin();
				}
			}
		}

		return out;
	}


	static public void reFreshToken(final Context context)
	{
		SettingInfo info = SettingInfoMgr.GetSettingInfo(context);
		final String id2 = info.GetPoco2Id(false);
		final String accessToken2 = info.GetPoco2Token(false);
		final String refreshToken = info.GetPoco2RefreshToken();
		if(id2 != null && id2.length() > 0 && accessToken2 != null && accessToken2.length() > 0 && refreshToken != null && refreshToken.length() > 0)
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					final LoginInfo info = LoginUtils.refreshToken(id2, refreshToken, AppInterface.GetInstance(context));
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable()
					{
						@Override
						public void run()
						{
							if(info != null)
							{
								if(info.mProtocolCode == 205 || info.mProtocolCode == 216)
								{
									ExitLogin(context);
									EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
									Toast.makeText(context, R.string.toast_relogin,Toast.LENGTH_SHORT).show();
								}else if(info.mCode == 0)
								{
									LoginOtherUtil.setSettingInfo(context,info);
								}
							}
						}
					});
				}
			}).start();
		}
	}

//	static public void updateUserInfo(final Context context)
//	{
//		final SettingInfo info = SettingInfoMgr.GetSettingInfo(PocoCamera.main);
//		final String id2 = info.GetPoco2Id(false);
//		final String accessToken2 = info.GetPoco2Token(false);
//		if(id2 != null && id2.length() > 0 && accessToken2 != null && accessToken2.length() > 0 )
//		{
//			new Thread(new Runnable()
//			{
//				@Override
//				public void run()
//				{
//					final UserInfo info = LoginUtils.getUserInfo(id2, accessToken2, AppInterface.GetInstance(PocoCamera.main));
//					Handler handler = new Handler(Looper.getMainLooper());
//					handler.post(new Runnable()
//					{
//						@Override
//						public void run()
//						{
//							if(info != null)
//							{
//								if(info.mProtocolCode == 205)
//								{
//									ExitLogin(context);
//									EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
//									Toast.makeText(context, R.string.toast_relogin,Toast.LENGTH_SHORT).show();
//								}else if(info.mCode == 0)
//								{
//									SaveCache(info);
//									EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
//								}
//							}
//						}
//					});
//				}
//			}).start();
//		}
//	}



	/**
	 * 下载图片
	 *
	 * @param url
	 * @return
	 */
	public static String DownloadHeadImg(Context context, String url)
	{
		String out = null;

		if(url != null && url.length() > 0)
		{
			try
			{
				NetCore2 net = new MyNetCore(context);
				NetCore2.NetMsg msg = net.HttpGet(url, null, TEMP_IMG_PATH, null);
				if(msg != null && msg.m_stateCode == HttpURLConnection.HTTP_OK)
				{
					File file = new File(HEAD_PATH);
					if(file != null && file.exists())
					{
						file.delete();
					}
					file = new File(TEMP_IMG_PATH);
					if(file != null && file.exists())
					{
						file.renameTo(new File(HEAD_PATH));
					}
					out = HEAD_PATH;
				}
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}

		return out;
	}

	/**
	 * 移动文件
	 *
	 * @param src 原始路径
	 * @param dst 目标路径
	 * @return
	 */
	public static boolean MoveFile(String src, String dst)
	{
		boolean out = false;

		if(src != null && dst != null)
		{
			File file2 = new File(dst);
			if(file2.exists())
			{
				file2.delete();
			}
			File file1 = new File(src);
			if(file1.exists())
			{
				out = file1.renameTo(file2);
			}
		}

		return out;
	}

	public static void ExitLogin(Context context)
	{
		context = context.getApplicationContext();
		SettingInfoMgr.GetSettingInfo(context).ClearPoco2();
		SettingInfoMgr.Save(context);
		File file = new File(HEAD_PATH);
		if(file != null && file.exists())
		{
			file.delete();
		}
		file = new File(HEAD_TEMP_PATH);
		if(file != null && file.exists())
		{
			file.delete();
		}

//		// 清除上传的记录
//		TransportImgs.getInstance(context).clear();
	}
}
