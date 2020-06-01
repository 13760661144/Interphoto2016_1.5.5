package cn.poco.login.userinfo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import cn.poco.camera.RotationImg2;
import cn.poco.framework.BaseSite;
import cn.poco.framework.EventCenter;
import cn.poco.framework.EventID;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.login.LoginPage;
import cn.poco.login.LoginPageInfo;
import cn.poco.login.site.EditHeadIconImgPageSite;
import cn.poco.login.util.AliyunHeadUpload;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.login.util.UserMgr;
import cn.poco.loginlibs.LoginUtils;
import cn.poco.loginlibs.info.BaseInfo;
import cn.poco.loginlibs.info.UserInfo;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.AppInterface;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

public class EditHeadIconImgPage extends IPage
{
	private static final String TAG = "编辑头像";
	public static final String KEY_MODE = "key_mode";
	public static final String KEY_IMG_PATH = "key_img_path";
	public static final String KEY_HEAD_URL = "key_head_url";
	public static final String KEY_FILTER_VALUE = "key_filter_value";

	public static final int REGISTER = 1;
	public static final int OTHER = 2;
	private int mode;
	private LoginPageInfo mLoginPageInfo;

	private Context mContext;
	private String imgPath;

	private ProgressDialog mProgress;
	private Handler mHandler;
	private boolean isUploading;
	private EditHeadIconImgPageSite mSite;
//	private Handler m_uiHandler;
	private Bitmap bmp;
	private FrameLayout m_topFr;
	private FrameLayout m_centerFr;

	private ImageView m_backBtn;
	private TextView title;
	private ImageView m_submitBtn;

	private ClipView clipView;
	private int ww = ShareData.PxToDpi_xhdpi(510);
	private int wh = ShareData.PxToDpi_xhdpi(510);
	private int mTopHeight = -1;

	public EditHeadIconImgPage(Context context, BaseSite site)
	{
		super(context, site);
		mContext = context;
		mSite = (EditHeadIconImgPageSite)site;
		initView();
//		initData();

		TongJiUtils.onPageStart(getContext(), TAG);
	}


	/**
	 * @param params imgs[]:图片路径
	 *               mode:注册设置头像和编辑资料修改头像 REGISTER和OTHER
	 *               info: LoginPageInfo
	 *               LoginInfo:用到id和token
	 *               filterValue:拍照进入用到
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		if(params != null)
		{
			if(params.get(KEY_IMG_PATH) != null)
			{
				RotationImg2[] img = (RotationImg2[])params.get(EditHeadIconImgPage.KEY_IMG_PATH);
				if(img != null)
				{
					this.imgPath = (String)img[0].m_img;
					if(imgPath != null && imgPath.length() > 0) setImg(imgPath);
				}
			}
			if(params.get(KEY_MODE) != null)
			{
				this.mode = (int)params.get(KEY_MODE);
			}
			if(params.get(LoginPage.KEY_INFO) != null)
			{
				mLoginPageInfo = (LoginPageInfo)params.get(LoginPage.KEY_INFO);
			}
		}
	}
	private void initView()
	{
		ww = ShareData.PxToDpi_xhdpi(562);
		wh = ShareData.PxToDpi_xhdpi(562);
		mTopHeight = ShareData.PxToDpi_xhdpi(80);
		mHandler = new Handler();

		LayoutParams fl;


		fl = new LayoutParams(ww, wh);
		fl.gravity = Gravity.CENTER_HORIZONTAL;
		fl.topMargin = ShareData.PxToDpi_xhdpi(210) + mTopHeight;
		m_centerFr = new FrameLayout(getContext());
		addView(m_centerFr, fl);
		{
			fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			fl.gravity = Gravity.CENTER;
			clipView = new ClipView(mContext);
			m_centerFr.addView(clipView, fl);
		}

		fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTopHeight);
		fl.gravity = Gravity.LEFT | Gravity.TOP;
		m_topFr = new FrameLayout(getContext());
		m_topFr.setBackgroundColor(0x4d000000);
		addView(m_topFr, fl);
		{
			fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
			m_backBtn = new ImageView(mContext);
			m_backBtn.setImageResource(R.drawable.framework_back_btn);
			m_backBtn.setOnClickListener(mOnClickListener);
			m_topFr.addView(m_backBtn, fl);

			fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER;
			title = new TextView(mContext);
			title.setTextColor(Color.WHITE);
			title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			title.setText(getResources().getString(R.string.userinfo_avatar));
			m_topFr.addView(title, fl);
		}

		fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		fl.bottomMargin = ShareData.PxToDpi_xhdpi(200);
		m_submitBtn = new ImageView(mContext);
		m_submitBtn.setOnTouchListener(mOnTouchListener);
		m_submitBtn.setImageResource(R.drawable.userinfo_edit_ok_btn);
		addView(m_submitBtn, fl);
		m_submitBtn.setOnClickListener(mOnClickListener);

		if(imgPath != null && !imgPath.trim().equals(""))
		{
			setImg(imgPath);
		}
	}

	protected void setImg(String imgPath)
	{
		RotationImg2 img = Utils.Path2ImgObj(imgPath);
		Bitmap temp = Utils.DecodeImage(getContext(), img.m_img, img.m_degree, -1, -1, -1);
		bmp = MakeBmpV2.CreateBitmapV2(temp, img.m_degree, img.m_flip, -1, ShareData.m_screenWidth, ShareData.m_screenHeight, Config.ARGB_8888);
		temp = null;
		if(bmp != null)
		{
			clipView.setImage(bmp);
		}
	}

//	public void setFilterValue(int value)
//	{
//		this.filterValue = value;
//		if(filterValue != 0)
//		{
//			m_filterHanlder.sendEmptyMessage(0);
//		}
//	}

	private OnClickListener mOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{

			if(v == m_backBtn)
			{
				onBack();
			}
			else if(v == m_submitBtn)
			{
				if(isUploading)
				{
					LoginOtherUtil.showToast(getContext(),R.string.toast_submitting_avatar);
				}
				else
				{
					if(LoginOtherUtil.isNetConnected(mContext))
					{
						Bitmap temp = clipView.getClipBmp();
						File file = new File(UserMgr.HEAD_TEMP_PATH);
						if(file.exists())
						{
							file.delete();
						}
						FileOutputStream out = null;
						try
						{
							out = new FileOutputStream(file);
							Bitmap m_thumb = MakeBmp.CreateBitmap(temp, 180, 180, -1, 0, Config.ARGB_8888);
							m_thumb.compress(CompressFormat.PNG, 100, out);
						}
						catch(FileNotFoundException e)
						{
							e.printStackTrace();
						}
						finally
						{
							if(out != null)
							{
								try
								{
									out.close();
								}
								catch(IOException e)
								{
									e.printStackTrace();
								}
							}
						}
						uploadHeadBmp(mode);
					}
					else
					{
						LoginOtherUtil.showToast(getContext(),R.string.toast_network_net_connected);
					}

				}
			}

		}
	};

	@Override
	public void onBack()
	{

		mSite.onBack(getContext());
	}

	protected void uploadHeadBmp(final int style)
	{
		mProgress = new ProgressDialog(getContext());
		mProgress.setMessage(getResources().getString(R.string.uploading));
		mProgress.setCancelable(false);
		mProgress.show();
		isUploading = true;
		if(mLoginPageInfo != null && mLoginPageInfo.m_info != null )
		{
			final String userId = mLoginPageInfo.m_info.mUserId;
			final String accessToken = mLoginPageInfo.m_info.mAccessToken;
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					AliyunHeadUpload upload = new AliyunHeadUpload();
					final String m_headUrl = upload.uploadHeadThumb(getContext(), userId, accessToken, UserMgr.HEAD_TEMP_PATH, AppInterface.GetInstance(getContext()));
					if(m_headUrl != null && m_headUrl.length() > 0)
					{
						final UserInfo userInfo = LoginUtils.getUserInfo(userId, accessToken, AppInterface.GetInstance(mContext));
						if(userInfo != null)
						{
							userInfo.mUserIcon = m_headUrl;
						}
						final BaseInfo resultInfo = LoginUtils.updateUserInfo(userId, accessToken, userInfo, AppInterface.GetInstance(getContext()));
						if(resultInfo != null)
						{
							mHandler.post(new Runnable()
							{
								@Override
								public void run()
								{
									if(mProgress != null)
									{
										mProgress.dismiss();
										mProgress = null;
									}
									isUploading = false;
									if(resultInfo.mCode == 0)
									{
										LoginOtherUtil.showToast(getContext(),R.string.toast_submit_avatar_success);
										UserMgr.SaveCache(getContext(),userInfo);
										UserMgr.MoveFile(UserMgr.HEAD_TEMP_PATH, UserMgr.HEAD_PATH);
										isUploading = false;
										mSite.upLoadSuccess(getContext(),m_headUrl);
										EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
									}
									else
									{
										if(resultInfo.mMsg != null)
										{
											LoginOtherUtil.showToast(getContext(),resultInfo.mMsg);
										}
										else
										{
											LoginOtherUtil.showToast(getContext(),R.string.toast_submit_avatar_failure);
										}
									}
								}
							});
						}
						else
						{
							mHandler.post(new Runnable()
							{
								@Override
								public void run()
								{
									if(mProgress != null)
									{
										mProgress.dismiss();
										mProgress = null;
									}
									LoginOtherUtil.showToast(getContext(),R.string.toast_submit_avatar_failure);
									isUploading = false;
								}
							});
						}
					}
					else
					{
						mHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								if(mProgress != null)
								{
									mProgress.dismiss();
									mProgress = null;
								}
								LoginOtherUtil.showToast(getContext(),R.string.toast_submit_avatar_failure);
								isUploading = false;
							}
						});
					}
				}
			}).start();
		}
		else
		{
			if(mProgress != null)
			{
				mProgress.dismiss();
				mProgress = null;
			}
			isUploading = false;
		}
	}

	@Override
	public void onClose()
	{
		this.setBackgroundDrawable(null);
		if(mProgress != null)
		{
			mProgress.dismiss();
			mProgress = null;
		}
//		m_filterThread.quit();
		mHandler.removeCallbacksAndMessages(null);
//		m_filterHanlder.removeCallbacksAndMessages(null);
		TongJiUtils.onPageEnd(getContext(), TAG);
	}

	@Override
	public void onResume()
	{
		TongJiUtils.onPageResume(getContext(), TAG);
		super.onResume();
	}

	@Override
	public void onPause()
	{
		TongJiUtils.onPagePause(getContext(), TAG);
		super.onPause();
	}

	private OnTouchListener mOnTouchListener = new OnTouchListener()
	{
		@SuppressLint("NewApi")
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				if(v == m_submitBtn)
				{
					m_submitBtn.setAlpha(0.5f);
				}
			}
			else if(event.getAction() == MotionEvent.ACTION_UP)
			{
				if(v == m_submitBtn)
				{
					m_submitBtn.setAlpha(1.0f);
				}
			}
			return false;
		}
	};
}
