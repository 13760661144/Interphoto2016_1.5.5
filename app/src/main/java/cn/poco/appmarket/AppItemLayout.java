package cn.poco.appmarket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.resource.AppMarketRes;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.tianutils.ImageUtils;
import cn.poco.tianutils.ShareData;

public class AppItemLayout extends RelativeLayout
{
	private static final int ID_IMAGEAPPICON = R.id.app_market_icon;
	private static final int ID_RIGHT_LAYOUT = R.id.app_market_layout;
	private static final int ID_TXT_APP_NAME = R.id.app_market_tex_app_name;

	private ImageView m_ImgAppIcon;
	private TextView m_TxtAppName;
	private TextView m_TxtAppDescribe;
	private AppMarketRes m_curItem;
	private MyDownloadCB m_DownloadCB;

	public AppItemLayout(Context context)
	{
		super(context);
		InitLayout(context);
		ShareData.InitData(context);

	}

	private void InitLayout(Context context)
	{
		RelativeLayout mainLayout = new RelativeLayout(context);
		int mainLayoutH = ShareData.PxToDpi_xhdpi(180);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mainLayoutH);
		addView(mainLayout, params);
		{

			params = new LayoutParams(ShareData.PxToDpi_xhdpi(106), ShareData.PxToDpi_xhdpi(106));
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(CENTER_IN_PARENT);
			params.setMargins(ShareData.PxToDpi_xhdpi(30), 0, ShareData.PxToDpi_xhdpi(30), 0);
			m_ImgAppIcon = new ImageView(context);
			m_ImgAppIcon.setId(ID_IMAGEAPPICON);
			mainLayout.addView(m_ImgAppIcon, params);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.RIGHT_OF, ID_IMAGEAPPICON);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			RelativeLayout rightLayout = new RelativeLayout(context);
			rightLayout.setId(ID_RIGHT_LAYOUT);
			mainLayout.addView(rightLayout, params);
			{
				params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				m_TxtAppName = new TextView(context);
				m_TxtAppName.setTextColor(0xFFFFFFFF);
				m_TxtAppName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				m_TxtAppName.setId(ID_TXT_APP_NAME);
				rightLayout.addView(m_TxtAppName, params);

				params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.ALIGN_LEFT, ID_TXT_APP_NAME);
				params.addRule(RelativeLayout.BELOW, ID_TXT_APP_NAME);
				params.setMargins(0, ShareData.PxToDpi_xhdpi(30), 0, 0);
				m_TxtAppDescribe = new TextView(context);
				m_TxtAppDescribe.setTextColor(0xFF999999);
//				m_TxtAppDescribe.setSingleLine();
				m_TxtAppDescribe.setEllipsize(TruncateAt.END);
				m_TxtAppDescribe.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				rightLayout.addView(m_TxtAppDescribe, params);
			}
		}
	}

	public void initItem(AppMarketRes item)
	{
		m_curItem = item;
		m_TxtAppName.setText(m_curItem.m_name);
		m_TxtAppDescribe.setText(m_curItem.m_info);
		if(m_DownloadCB != null)
		{
			m_DownloadCB.m_layout = null;
		}
		m_DownloadCB = new MyDownloadCB(this);
		DownloadMgr.getInstance().DownloadRes(m_curItem, m_DownloadCB);

		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				try
				{
					Uri uri = Uri.parse(m_curItem.m_downloadUrl);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					getContext().startActivity(Intent.createChooser(intent, ""));
				}
				catch(Throwable e)
				{
					Toast.makeText(getContext().getApplicationContext(), getResources().getString(R.string.openBrowserFailed), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});
	}

	public static class MyDownloadCB implements DownloadMgr.Callback
	{
		public AppItemLayout m_layout;

		public MyDownloadCB(View view)
		{
			m_layout = (AppItemLayout)view;
		}

		@Override
		public void OnProgress(int downloadId, IDownload res, int progress)
		{

		}

		@Override
		public void OnComplete(int downloadId, IDownload res)
		{
			if(m_layout != null)
			{
				Bitmap temp = BitmapFactory.decodeFile((String)((BaseRes)res).m_thumb);
				m_layout.m_ImgAppIcon.setImageBitmap(ImageUtils.MakeRoundBmp(temp, 25));
			}
		}

		@Override
		public void OnFail(int downloadId, IDownload res)
		{

		}

	}
}
