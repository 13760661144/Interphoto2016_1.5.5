package cn.poco.statistics;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.banner.BannerCore3;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.interphoto2.R;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.NetCore2;
import cn.poco.tianutils.ShareData;
import cn.poco.webview.site.WebViewPageSite3;

/**
 * 提示用户更新
 */
public class CheckUpdate
{
	public static String CHECK_URL_PROD = "http://open.adnonstop.com/interphoto/biz/prod/api/public/app_update.php?";
	public static String CHECK_URL_BETA = "http://tw.adnonstop.com/beauty/app/api/interphoto/biz/beta/api/public/app_update.php?";
	public static String CACHE_PATH = "";
	public static String APP_PATH = SysConfig.GetAppPath() + "/appdata/InterPhoto.apk";

	protected String code;
	protected String message;
	protected String type;
	protected String version;
	protected String download_url;
	protected String detail_url;
	protected ArrayList<String> details;
	protected Context m_context;
	protected String m_checkUrl;
	protected String m_showTag;
	protected ViewGroup m_parent;
	protected FrameLayout m_fr;

	public CheckUpdate(Context context, ViewGroup parent)
	{
		m_context = context;
		m_parent = parent;
		CommonUtils.MakeFolder(SysConfig.GetAppPath() + "/appdata/");
		CACHE_PATH = SysConfig.GetAppPath() + "/appdata/app_update.xxxx";
		if (SysConfig.IsDebug()) {
			m_checkUrl = CHECK_URL_BETA;
		} else {
			m_checkUrl = CHECK_URL_PROD;
		}
	}

	public void CheckForUpdate()
	{
		byte[] buffer = CommonUtils.ReadFile(CACHE_PATH);
		try
		{
			JSONObject object = new JSONObject(new String(buffer));
			code = object.getString("code");
			message = object.getString("message");
			JSONObject datas = object.getJSONObject("data");
			type = datas.getString("update_type");
			if(datas.has("version"))
			{
				version = datas.getString("version");
			}
			if(datas.has("download_url"))
			{
				download_url = datas.getString("download_url");
			}
			if(datas.has("details"))
			{
				details = new ArrayList<String>();
				JSONArray array = datas.getJSONArray("details");
				if(array != null && array.length() > 0)
				{
					int len = array.length();
					for(int i = 0; i < len; i ++)
					{
						details.add(array.getString(i));
					}
				}
			}
			if(datas.has("details_url"))
			{
				detail_url = datas.getString("details_url");
			}
			if(datas.has("pop_version"))
			{
				m_showTag = datas.getString("pop_version");
			}
			if("1".equals(type) || "2".equals(type))
			{
				if(!TextUtils.isEmpty(m_showTag) && TagMgr.CheckTag(m_context, Tags.PROMPT_UPDATE + m_showTag))
				{
					TongJi2.AddCountByRes(m_context, R.integer.POPUP);
					showDownloadDlg();
					TagMgr.SetTag(m_context, Tags.PROMPT_UPDATE + m_showTag);
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		ReadCloud();
	}

	public void ReadCloud()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				NetCore2 net = new NetCore2();
				FileOutputStream fos = null;
				try
				{
					StringBuffer out = new StringBuffer(256);

					out.append(m_checkUrl);
					out.append("os_type=android");
					out.append("&app_version=");
					out.append(CommonUtils.GetAppVer(m_context));
					NetCore2.NetMsg netMsg = net.HttpGet(out.toString());
					if(netMsg != null && netMsg.m_stateCode == 200)
					{
//						System.out.println("update data: " + new String(netMsg.m_data));
						fos = new FileOutputStream(CACHE_PATH);
						fos.write(netMsg.m_data);
						fos.flush();
					}

				}catch(Exception e)
				{}
				finally
				{
					if(fos != null)
					{
						try
						{
							fos.close();
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


	public void showDownloadDlg()
	{
		if(m_parent == null)
			return;

		m_fr = new FrameLayout(m_context);
		ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		m_fr.setLayoutParams(vl);
		m_parent.addView(m_fr);
		m_fr.setBackgroundColor(0x99000000);
		if(!"2".equals(type))
		{
			/*m_fr.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					dismiss();
				}
			});*/
		}

		FrameLayout frame = new FrameLayout(m_context);
		frame.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

			}
		});
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(560), FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		fl.bottomMargin = ShareData.PxToDpi_xhdpi(30);
		m_fr.addView(frame, fl);
		{
			LinearLayout lin = new LinearLayout(m_context);
			lin.setBackgroundColor(0xff333333);
			lin.setOrientation(LinearLayout.VERTICAL);
			fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(560), FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
			fl.topMargin = ShareData.PxToDpi_xhdpi(100);
			lin.setLayoutParams(fl);
			frame.addView(lin);
			{
				LinearLayout.LayoutParams ll;
				View line = new View(m_context);
				line.setBackgroundColor(0xff454545);
				ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(350), 1);
				ll.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
				ll.topMargin = ShareData.PxToDpi_xhdpi(85);
				line.setLayoutParams(ll);
				lin.addView(line);

				TextView text = new TextView(m_context);
				text.setText(message);
				text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
				text.setTextColor(Color.WHITE);
				TextPaint tp = text.getPaint();
				tp.setFakeBoldText(true);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				ll.topMargin = ShareData.PxToDpi_xhdpi(35);
				text.setLayoutParams(ll);
				lin.addView(text);

				FrameLayout temp = new FrameLayout(m_context);
				ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(350), LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
				ll.topMargin = ShareData.PxToDpi_xhdpi(20);
				temp.setLayoutParams(ll);
				lin.addView(temp);
				{
					line = new View(m_context);
					line.setBackgroundColor(0xff454545);
					fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(350), 1);
					fl.gravity = Gravity.CENTER;
					line.setLayoutParams(fl);
					temp.addView(line);

					text = new TextView(m_context);
					text.setBackgroundColor(0xff515151);
					text.setText(version);
					text.setGravity(Gravity.CENTER);
					text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
					text.setTextColor(Color.WHITE);
					fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(70), LinearLayout.LayoutParams.WRAP_CONTENT);
					fl.gravity = Gravity.CENTER;
					text.setLayoutParams(fl);
					temp.addView(text);
				}

				if(details != null && details.size() > 0)
				{
					String str = "";
					int size = details.size();
					for(int i = 0; i < size; i ++)
					{
						str += details.get(i) + "\n";
					}
					text = new TextView(m_context);
					text.setMaxLines(6);
					text.setLineSpacing(ShareData.PxToDpi_xhdpi(20), 1.0f);
					text.setEllipsize(TextUtils.TruncateAt.END);
					text.setText(str);
					text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
					text.setTextColor(0xffeeeeee);
					ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(500), LinearLayout.LayoutParams.WRAP_CONTENT);
					ll.gravity = Gravity.CENTER_HORIZONTAL;
					ll.topMargin = ShareData.PxToDpi_xhdpi(55);
					text.setLayoutParams(ll);
					lin.addView(text);
				}

				LinearLayout bottonBar = new LinearLayout(m_context);
				bottonBar.setOrientation(LinearLayout.HORIZONTAL);
				ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(70));
				ll.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
				ll.topMargin = ShareData.PxToDpi_xhdpi(60);
				ll.bottomMargin = ShareData.PxToDpi_xhdpi(40);
				bottonBar.setLayoutParams(ll);
				lin.addView(bottonBar);
				{
					final TextView btn1 = new TextView(m_context);
					btn1.setBackgroundResource(R.drawable.framework_updata_detail);
					btn1.setText(m_context.getResources().getString(R.string.MoreDetails));
					btn1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
					btn1.setTextColor(0xfffec533);
					btn1.setGravity(Gravity.CENTER);
					ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(235), ShareData.PxToDpi_xhdpi(70));
					btn1.setLayoutParams(ll);
					bottonBar.addView(btn1);
					btn1.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{

							TongJi2.AddCountByRes(m_context, R.integer.POPUP_了解更多);
//							m_dialog.dismiss();
							BannerCore3.ExecuteCommand(m_context, detail_url, new BannerCore3.CmdCallback()
							{
								@Override
								public void OpenPage(Context context, int code, String... args)
								{

								}

								@Override
								public void OpenWebPage(Context context, String... args)
								{
									String url;
									if(args != null && args.length > 0 && (url = args[0]) != null)
									{
										CommonUtils.OpenBrowser(context, Uri.decode(url));
									}
								}

								@Override
								public void OpenMyWebPage(Context context, String... args)
								{
									String url;
									if(args != null && args.length > 0 && (url = args[0]) != null)
									{

										System.out.println("url: " + url);
										HashMap<String, Object> params = new HashMap<String, Object>();
										params.put("url", Uri.decode(url));
										MyFramework.SITE_Popup(context, WebViewPageSite3.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
									}
								}

								@Override
								public void OpenPocoCamera(Context context, String... args)
								{

								}

								@Override
								public void OpenPocoMix(Context context, String... args)
								{

								}

								@Override
								public void OpenJane(Context context, String... args)
								{

								}

								@Override
								public void OpenBeautyCamera(Context context, String... args)
								{

								}

								@Override
								public void OpenBusinessPage(Context context, String... args)
								{

								}
							});
						}
					});
					btn1.setOnTouchListener(new View.OnTouchListener()
					{
						@Override
						public boolean onTouch(View v, MotionEvent event)
						{
							if(event.getAction() == MotionEvent.ACTION_DOWN)
							{
								btn1.setAlpha(0.6f);
							}
							else if(event.getAction() == MotionEvent.ACTION_UP)
							{
								btn1.setAlpha(1.0f);
							}
							return false;
						}
					});

					final TextView btn2 = new TextView(m_context);
					btn2.setBackgroundColor(0xfffec533);
					btn2.setText(m_context.getResources().getString(R.string.updateImmediately));
					btn2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
					btn2.setTextColor(0xff333333);
					btn2.setGravity(Gravity.CENTER);
					ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(235), ViewGroup.LayoutParams.MATCH_PARENT);
					ll.leftMargin = ShareData.PxToDpi_xhdpi(30);
					btn2.setLayoutParams(ll);
					bottonBar.addView(btn2);
					btn2.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							TongJi2.AddCountByRes(m_context, R.integer.POPUP_跳去下载);
							DownloadApp(download_url);
//							m_dialog.dismiss();
						}
					});
					btn2.setOnTouchListener(new View.OnTouchListener()
					{
						@Override
						public boolean onTouch(View v, MotionEvent event)
						{
							if(event.getAction() == MotionEvent.ACTION_DOWN)
							{
								btn2.setAlpha(0.6f);
							}
							else if(event.getAction() == MotionEvent.ACTION_UP)
							{
								btn2.setAlpha(1.0f);
							}
							return false;
						}
					});
				}

				final TextView btn3 = new TextView(m_context);
				if("2".equals(type))
				{
					btn3.setVisibility(View.GONE);
				}
				btn3.setText(m_context.getResources().getString(R.string.skip));
				btn3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				btn3.setTextColor(0xff999999);
				btn3.setGravity(Gravity.CENTER);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				ll.bottomMargin = ShareData.PxToDpi_xhdpi(40);
				btn3.setLayoutParams(ll);
				lin.addView(btn3);
				btn3.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						dismiss();
					}
				});
				btn3.setOnTouchListener(new View.OnTouchListener()
				{
					@Override
					public boolean onTouch(View v, MotionEvent event)
					{
						if(event.getAction() == MotionEvent.ACTION_DOWN)
						{
							btn3.setAlpha(0.6f);
						}
						else if(event.getAction() == MotionEvent.ACTION_UP)
						{
							btn3.setAlpha(1.0f);
						}
						return false;
					}
				});

			}

			ImageView img = new ImageView(m_context);
			img.setImageResource(R.drawable.framework_updata_logo);
			fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(560), FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
			img.setLayoutParams(fl);
			frame.addView(img);
		}

	}

	protected void dismiss()
	{
		if(m_parent != null)
		{
			m_parent.removeView(m_fr);
		}
	}

	public void DownloadApp(final String url)
	{
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		m_context.startActivity(intent);
	}

	//安装apk
	protected void installApk(File file)
	{
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		m_context.startActivity(intent);
	}
}
