package cn.poco.login.userinfo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

import cn.poco.framework.BaseSite;
import cn.poco.framework.EventCenter;
import cn.poco.framework.EventID;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.login.LoginPageInfo;
import cn.poco.login.area.AreaList;
import cn.poco.login.area.AreaList.AreaInfo2;
import cn.poco.login.area.ChooseItem;
import cn.poco.login.area.CitiesPicker;
import cn.poco.login.site.UserInfoPageSite;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.login.util.MyTextUtils;
import cn.poco.login.util.UserMgr;
import cn.poco.login.widget.EditTextWithDel;
import cn.poco.login.widget.TipDialog;
import cn.poco.loginlibs.LoginUtils;
import cn.poco.loginlibs.info.BaseInfo;
import cn.poco.loginlibs.info.LoginInfo;
import cn.poco.loginlibs.info.UserInfo;
import cn.poco.setting.SettingInfo;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.AppInterface;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.ImageUtil;
import cn.poco.utils.PermissionUtils;
import cn.poco.utils.Utils;

import static cn.poco.tianutils.ShareData.PxToDpi_xhdpi;

public class UserInfoPage extends IPage
{
	private static final String TAG = "用户信息";

	public static final int NONE = 1;
	protected static final int NICKNAME = 2;
	protected static final int PASSWORD = 3;
	protected static final int SEX = 4;
	protected static final int BIRTH = 5;
	protected static final int AREA = 6;

	protected static final int UPDATE_PSW = 10;
	protected static final int UPDATE_USERINFO = 11;
	protected static final int UPDATE_WATERMARK = 12;
	protected static final int CHECK = 13;
	protected static final int RELOGIN = 20;

	public static final int TEXT_SIZE = 16;

	protected FrameLayout m_mainFr;
	protected LinearLayout m_linearFr;
	protected FrameLayout m_topFr;
	protected ImageView m_backBtn;
	protected FrameLayout m_headFr;
	protected ImageView m_cameraBtn;
	protected ImageView m_headImg;
	protected int m_topBarHeight;
	protected int m_headWH;
	protected boolean m_editable = true;

	protected UserInfoItem m_nickname; //昵称
//	protected UserInfoItem m_watermark; //水印
	protected UserInfoItem m_password; //密码
	protected UserInfoItem m_sex; //性别
	protected UserInfoItem m_birth;    //生日
	protected UserInfoItem m_area;    //地区

	protected TextView m_exitLogin;

	protected FrameLayout m_editFr;
	protected FrameLayout m_editTopBar;
	protected ImageView m_editBack;
	protected TextView m_editTitle;
	protected TextView m_editComplete;
	protected EditText m_editName;
	protected WheelDatePicker m_datePicker;
	private TextView m_cancel;
	private TextView m_ok;

	protected EditTextWithDel m_oldPsw;
	protected EditTextWithDel m_newPsw;
	protected String m_newPassword;
	protected String m_oldPassword;

	protected LinearLayout m_sexGroup;
	protected ChooseItem m_man;
	protected ChooseItem m_woman;
	protected boolean m_chooseMan = true;
	//	protected boolean m_hasUserInfoChanged = false;
	protected boolean m_uiEnabled = true;

	protected CitiesPicker m_citiesPicker;
	protected AreaInfo2[] m_allAreaInfos;

	protected int m_editTopBarHeight;

	protected int m_mode = NONE;

	protected UserInfo m_localInfo;
	protected UserInfo m_netInfo;
	protected Handler m_threadHandler;
	protected HandlerThread m_thread;
	protected Handler m_handler;

	private ProgressDialog mProgressDialog;
	private TipDialog mTipDialog;
	//	private WaitDialog1 m_dlg;
	private UserInfoPageSite mSite;

	private SettingInfo m_settingInfo;

	private boolean isClose = false;

	public UserInfoPage(Context context, BaseSite site)
	{
		super(context, site);
		InitData();
		InitUI();
		mSite = (UserInfoPageSite)site;

		TongJiUtils.onPageStart(getContext(), TAG);
		MyBeautyStat.onPageStartByRes(R.string.个人资料页);
	}

	/**
	 * @param params String id:  用户id<br/>
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		if(params != null)
		{
			if(params.get("id") != null)
			{
				setDatas((String)params.get("id"));
			}
			if(params.get("img") != null)
			{
				SetBackground(Utils.DecodeFile((String)params.get("img"), null));
			}
		}

	}

	protected void SetBackground(Bitmap bk)
	{
		if(bk != null)
		{
			this.setBackgroundDrawable(new BitmapDrawable(bk));
		}
		else
		{
			//黑色 透明度75%
			setBackgroundColor(0x40000000);
		}
	}

	protected void InitData()
	{
		ShareData.InitData(getContext());
		m_topBarHeight = PxToDpi_xhdpi(410);
		m_editTopBarHeight = PxToDpi_xhdpi(80);
		m_thread = new HandlerThread("UserInfoPage");
		m_thread.start();

		m_threadHandler = new Handler(m_thread.getLooper())
		{
			@Override
			public void dispatchMessage(final Message msg)
			{
				switch(msg.what)
				{
					case CHECK:
					{
						boolean updateHead = false;
						m_netInfo = LoginUtils.getUserInfo((String)msg.obj, m_settingInfo.GetPoco2Token(false), AppInterface.GetInstance(getContext()));
						if(m_netInfo != null)
						{
							if(m_netInfo.mProtocolCode == 205)
							{
								Message reLoginMsg = Message.obtain();
								reLoginMsg.what = RELOGIN;
								m_handler.sendMessage(reLoginMsg);
							}else if(m_netInfo.mProtocolCode == 200 && m_netInfo.mCode == 0)
							{
								if(m_netInfo.mUserIcon != null)
								{
									if(m_localInfo == null || !m_netInfo.mUserIcon.equals(m_localInfo.mUserIcon))
									{
										updateHead = true;
										UserMgr.DownloadHeadImg(getContext(), m_netInfo.mUserIcon);
									}
								}
								Message uimsg = m_handler.obtainMessage();
								uimsg.what = CHECK;
								uimsg.obj = updateHead;
								m_handler.sendMessage(uimsg);
							}

						}
						break;
					}
					case UPDATE_PSW:
					{
						BaseInfo data = LoginUtils.changePassWord(m_netInfo.mUserId, m_settingInfo.GetPoco2Token(false), m_oldPassword, m_newPassword, AppInterface.GetInstance(getContext()));
						if(data != null && data.mProtocolCode == 205)
						{
							Message reLoginMsg = Message.obtain();
							reLoginMsg.what = RELOGIN;
							m_handler.sendMessage(reLoginMsg);
						}else
						{
							Message uimsg = m_handler.obtainMessage();
							uimsg.what = UPDATE_PSW;
							uimsg.obj = data;
							m_handler.sendMessage(uimsg);
						}
						break;
					}
					case UPDATE_USERINFO:
					{
						BaseInfo data = null;
						if(m_netInfo != null)
						{
							if(m_newPassword != null && m_newPassword.length() == 0)
							{
								m_newPassword = null;
							}
							data = LoginUtils.updateUserInfo(m_netInfo.mUserId, m_settingInfo.GetPoco2Token(false), m_netInfo, AppInterface.GetInstance(getContext()));
						}
						if(data != null && data.mProtocolCode == 205)
						{
							Message reLoginMsg = Message.obtain();
							reLoginMsg.what = RELOGIN;
							m_handler.sendMessage(reLoginMsg);
						}else
						{
							Message uimsg = m_handler.obtainMessage();
							uimsg.what = UPDATE_USERINFO;
							uimsg.obj = data;
							m_handler.sendMessage(uimsg);
						}
						break;
					}
				}
			}
		};

		m_handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				switch(msg.what)
				{
					case CHECK:
					{
						if(m_netInfo != null)
						{
							m_editable = true;
							m_localInfo = (UserInfo)m_netInfo.clone();
							UserMgr.SaveCache(getContext(),m_netInfo);
							EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
							UpdateDataToUI(m_netInfo, (Boolean)msg.obj);
						}
						break;
					}
					case UPDATE_PSW:
					{
						BaseInfo data = (BaseInfo)msg.obj;

                        if (data != null) {
                            if(data.mCode == 0)
                            {
                                Toast.makeText(getContext(), getResources().getString(R.string.toast_modify_success), Toast.LENGTH_SHORT).show();
                                onBackBtn(false);
                            }
                            else if(data.mCode == 10001)
                            {
                                Toast.makeText(getContext(), getResources().getString(R.string.toast_param_error), Toast.LENGTH_SHORT).show();
                            }
                            else if(data.mCode == 10002)
                            {
                                Toast.makeText(getContext(), getResources().getString(R.string.toast_original_password_error), Toast.LENGTH_SHORT).show();
                            }
                            else if(data.mCode == 10003)
                            {
                                Toast.makeText(getContext(), getResources().getString(R.string.toast_operate_error), Toast.LENGTH_SHORT).show();
                            }
                            else if(data.mMsg != null)
                            {
                                Toast.makeText(getContext(), data.mMsg, Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getContext(), getResources().getString(R.string.toast_modify_password_failure), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(getContext(), getResources().getString(R.string.toast_modify_password_failure), Toast.LENGTH_SHORT).show();
                        }
                        break;
					}
					case UPDATE_USERINFO:
					{
						BaseInfo data = (BaseInfo)msg.obj;
						if(data != null && data.mCode == 0)
						{
							m_localInfo = (UserInfo)m_netInfo.clone();
							UserMgr.SaveCache(getContext(),m_netInfo);
							EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
							Toast.makeText(getContext(), getResources().getString(R.string.toast_modify_success), Toast.LENGTH_SHORT).show();
						}
						else
						{
							UpdateDataToUI(m_localInfo, false);
							Toast.makeText(getContext(), getResources().getString(R.string.toast_modify_failure), Toast.LENGTH_SHORT).show();
						}
						m_uiEnabled = true;
						isShowDlg(false);
						break;
					}
					case RELOGIN:
					{
						if(!isClose)
						{
							final TipDialog tipDialog = new TipDialog((Activity)getContext());
							tipDialog.setCancelable(false);
							tipDialog.setMessage(getResources().getString(R.string.toast_relogin));
							tipDialog.setBtnText(getResources().getString(R.string.ok), new TipDialog.OnCallBack()
							{
								@Override
								public void onBtnClick()
								{
									tipDialog.dismiss();
									UserMgr.ExitLogin(getContext());
									EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
									mSite.toLoginPage(getContext());
								}
							});
							tipDialog.show();
						}
						break;
					}
				}
			}
		};
		m_settingInfo = SettingInfoMgr.GetSettingInfo(getContext());
	}

	protected void InitUI()
	{
		this.setBackgroundColor(0xcc000000);

		LayoutParams fl;
		m_headWH = PxToDpi_xhdpi(180);

		m_mainFr = new FrameLayout(getContext());
		fl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		m_mainFr.setLayoutParams(fl);
		addView(m_mainFr);
		{
			m_topFr = new FrameLayout(getContext());
			m_topFr.setBackgroundColor(0x4d000000);
			fl = new LayoutParams(LayoutParams.MATCH_PARENT, m_topBarHeight);
			m_topFr.setLayoutParams(fl);
			m_mainFr.addView(m_topFr);
			{
				m_headFr = new FrameLayout(getContext());
				fl = new LayoutParams(m_headWH, m_headWH);
				fl.gravity = Gravity.CENTER;
				m_headFr.setLayoutParams(fl);
				m_topFr.addView(m_headFr);
				{
					m_headImg = new ImageView(getContext());
					m_headImg.setScaleType(ScaleType.FIT_XY);
					m_headImg.setImageResource(R.drawable.login_head_logo);
					fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
					fl.gravity = Gravity.CENTER;
					m_headImg.setLayoutParams(fl);
					m_headFr.addView(m_headImg);
					m_headImg.setOnClickListener(m_btnListener);
//					m_headImg.setOnTouchListener(m_onTouchListener);

					m_cameraBtn = new ImageView(getContext());
					m_cameraBtn.setScaleType(ScaleType.CENTER);
					m_cameraBtn.setImageResource(R.drawable.userinfo_camera_btn);
					m_cameraBtn.setPadding(ShareData.PxToDpi_xhdpi(20), ShareData.PxToDpi_xhdpi(20), 0, 0);
					fl = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					fl.gravity = Gravity.BOTTOM | Gravity.RIGHT;
					m_cameraBtn.setLayoutParams(fl);
					m_headFr.addView(m_cameraBtn);
					m_cameraBtn.setOnClickListener(m_btnListener);
//					m_cameraBtn.setOnTouchListener(m_onTouchListener);
				}

				m_backBtn = new ImageView(getContext());
				m_backBtn.setImageResource(R.drawable.framework_back_btn);
				fl = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,PxToDpi_xhdpi(80));
				fl.gravity = Gravity.LEFT;
				m_backBtn.setLayoutParams(fl);
				m_topFr.addView(m_backBtn);
				m_backBtn.setOnClickListener(m_btnListener);
//				m_backBtn.setOnTouchListener(m_onTouchListener);
			}

			m_linearFr = new LinearLayout(getContext());
			m_linearFr.setOrientation(LinearLayout.VERTICAL);
			fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			fl.topMargin = m_topBarHeight + ShareData.PxToDpi_xhdpi(50);
			m_linearFr.setLayoutParams(fl);
			m_mainFr.addView(m_linearFr);
			{
				m_nickname = new UserInfoItem(getContext());
				m_nickname.setTitle(getResources().getString(R.string.userinfo_nickname));
				AddView(m_nickname);
				m_sex = new UserInfoItem(getContext());
				m_sex.setTitle(getResources().getString(R.string.userinfo_sex));
				AddView(m_sex);
				m_birth = new UserInfoItem(getContext());
				m_birth.setTitle(getResources().getString(R.string.userinfo_birth));
				AddView(m_birth);
				m_area = new UserInfoItem(getContext());
				m_area.setTitle(getResources().getString(R.string.userinfo_regin));
				AddView(m_area);
				m_password = new UserInfoItem(getContext());
				m_password.setTitle(getResources().getString(R.string.userinfo_modify_password));
				AddView(m_password);

//				m_watermark = new UserInfoItem(getContext());
//				m_watermark.setVisibility(View.GONE);
//				m_watermark.isShowArrow(false);
//				m_watermark.setTitle(getResources().getString(R.string.userinfo_synchronized_watermark));
//				AddView(m_watermark);
			}

			m_exitLogin = new TextView(getContext());
			m_exitLogin.setBackgroundColor(0x4d000000);
			m_exitLogin.setText(getResources().getString(R.string.userinfo_log_out));
			m_exitLogin.setGravity(Gravity.CENTER);
			m_exitLogin.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			m_exitLogin.setTextColor(0xffFFC433);
			fl = new LayoutParams(LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(100));
			fl.gravity = Gravity.BOTTOM;
			m_exitLogin.setLayoutParams(fl);
			m_mainFr.addView(m_exitLogin);
			m_exitLogin.setOnClickListener(m_btnListener);
//			m_exitLogin.setOnTouchListener(m_onTouchListener);
		}
		m_editFr = new FrameLayout(getContext());
		m_editFr.setVisibility(View.GONE);
		fl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		m_editFr.setLayoutParams(fl);
		this.addView(m_editFr);

//		m_dlg = new WaitDialog1((Activity)getContext());
//		m_dlg.setText(getResources().getString(R.string.updating));
//		m_dlg.setCancelable(false);
		mProgressDialog = new ProgressDialog(getContext());
		mProgressDialog.setCancelable(false);
		mProgressDialog.setMessage(getResources().getString(R.string.updating));

		mTipDialog = new TipDialog((Activity)getContext());
	}

	protected void AddView(View view)
	{
		int viewH = PxToDpi_xhdpi(100);
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, viewH);
		ll.bottomMargin = PxToDpi_xhdpi(1);
		view.setLayoutParams(ll);
		m_linearFr.addView(view);
		view.setOnClickListener(m_btnListener);
//		view.setOnTouchListener(m_onTouchListener);
	}

	protected OnClickListener m_btnListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			if(v == m_backBtn )
			{
				MyBeautyStat.onPageStartByRes(R.string.个人资料页_退出个人资料页);
				mSite.onBack(getContext());
			}
			else if(v == m_editBack)
			{
				onBackBtn(false);
			}
			else if(v == m_exitLogin)
			{
				MyBeautyStat.onClickByRes(R.string.个人资料页_退出登录);
				UserMgr.ExitLogin(getContext());
				EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
				mSite.onExit(getContext());
			}
			else if(v == m_editComplete)
			{
				if(m_mode == NICKNAME)
				{
					if(m_netInfo != null)
					{
						String text = String.valueOf(m_editName.getEditableText());
						if(text.length() == 0)
						{
							Toast.makeText(getContext(), getResources().getString(R.string.toast_nickname_null), Toast.LENGTH_SHORT).show();
							return;
						}
						if(!m_netInfo.mNickname.equals(text))
						{
							m_netInfo.mNickname = text;

							m_uiEnabled = false;

							isShowDlg(true);
							Message msg = m_threadHandler.obtainMessage();
							msg.what = UPDATE_USERINFO;
							msg.obj = -1;
							m_threadHandler.sendMessage(msg);
						}
					}
					onBackBtn(false);
				}
				else if(m_mode == PASSWORD)
				{
					m_oldPassword = String.valueOf(m_oldPsw.getEditableText());
					m_newPassword = String.valueOf(m_newPsw.getEditableText());
					if(m_oldPassword.equals(""))
					{
						Toast.makeText(getContext(), getResources().getString(R.string.toast_enter_original_password), Toast.LENGTH_SHORT).show();
					}
					else if(m_newPassword.length() < 8 || m_newPassword.length() > 20 || Pattern.compile("[\u4e00-\u9fa5]").matcher(m_newPassword).find())
					{
						Toast.makeText(getContext(), getResources().getString(R.string.toast_password_rule_error), Toast.LENGTH_SHORT).show();
					}
					else
					{
						Message msg = m_threadHandler.obtainMessage();
						msg.what = UPDATE_PSW;
						m_threadHandler.sendMessage(msg);
					}

				}
			}
			else if(v == m_man)
			{
				if(m_netInfo != null)
				{
//					m_netInfo.mSex = getResources().getString(Rstring.userinfo_male);
					m_netInfo.mSex = "男";
				}
				m_chooseMan = true;
				m_man.onChoose(m_chooseMan);
				m_woman.onChoose(!m_chooseMan);

			}
			else if(v == m_woman)
			{
				if(m_netInfo != null)
				{
//					m_netInfo.mSex = getResources().getString(R.string.userinfo_female);
					m_netInfo.mSex = "女";
				}
				m_chooseMan = false;
				m_man.onChoose(m_chooseMan);
				m_woman.onChoose(!m_chooseMan);
			}
			else if(v == m_cancel || v == m_editFr)
			{
				onBackBtn(false);
			}
			else if(v == m_ok)
			{
				if(!m_localInfo.mBirthdayYear.equals(m_netInfo.mBirthdayYear) || !m_localInfo.mBirthdayMonth.equals(m_netInfo.mBirthdayMonth) || !m_localInfo.mBirthdayDay.equals(m_netInfo.mBirthdayDay))
				{
					m_uiEnabled = false;
					isShowDlg(true);
					Message msg = m_threadHandler.obtainMessage();
					msg.what = UPDATE_USERINFO;
					m_threadHandler.sendMessage(msg);
				}
				showEditView(m_mode, NONE);
				m_mode = NONE;
				UpdateDataToUI(m_netInfo, false);
			}
			else if(m_editable)
			{
				if(v == m_nickname)
				{
					MyBeautyStat.onClickByRes(R.string.个人资料页_昵称);
					m_mode = NICKNAME;

					showEditView(NONE, m_mode);
				}
				else if(v == m_password)
				{
					MyBeautyStat.onClickByRes(R.string.个人资料页_修改密码);
					m_mode = PASSWORD;

					showEditView(NONE, m_mode);
				}
				else if(v == m_sex)
				{
					MyBeautyStat.onClickByRes(R.string.个人资料页_性别);
					m_mode = SEX;
					showEditView(NONE, m_mode);
				}
				else if(v == m_birth)
				{
					MyBeautyStat.onClickByRes(R.string.个人资料页_生日);
					m_mode = BIRTH;

					showEditView(NONE, m_mode);
				}
				else if(v == m_area)
				{
					MyBeautyStat.onClickByRes(R.string.个人资料页_地区);
					m_mode = AREA;

					showEditView(NONE, m_mode);
				}
				else if(v == m_cameraBtn)
				{
					boolean flag = true;
					if(Build.VERSION.SDK_INT >= 23)
					{
						flag = PermissionUtils.checkCameraPermission(getContext());
					}
					if(flag)
					{
						OnCamera();
					}
					else
					{
						PermissionUtils.requestPermissions(getContext(), Manifest.permission.CAMERA);
					}
				}
				else if(v == m_headImg)
				{
					MyBeautyStat.onClickByRes(R.string.个人资料页_设置头像);
//					mSite.onChooseHeadBmp(m_netInfo.mUserId, m_settingInfo.GetPoco2Token(false));
					LoginPageInfo info = new LoginPageInfo();
					info.m_info = new LoginInfo();
					info.m_info.mAccessToken = m_settingInfo.GetPoco2Token(false);
					info.m_info.mUserId = m_netInfo.mUserId;
					mSite.onChooseHeadBmp(getContext(),info);
					return;
				}
			}
			else if(!m_editable)
			{
				LoginOtherUtil.showToast(getContext(),R.string.networkError);
			}

		}
	};

	private void OnCamera()
	{
		LoginPageInfo info = new LoginPageInfo();
		info.m_info = new LoginInfo();
		info.m_info.mAccessToken = m_settingInfo.GetPoco2Token(false);
		info.m_info.mUserId = m_netInfo.mUserId;
		mSite.onCamera(getContext(),info);
	}

	@Override
	public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		String str;
		for(int i = 0; i < permissions.length; i ++)
		{
			str = permissions[i];
			if(Manifest.permission.CAMERA.equals(str))
			{
				if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
				{
					m_btnListener.onClick(m_cameraBtn);
				}
				else
				{
					if(!ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(), str))
					{
						Toast.makeText(getContext(), "请到应用程序管理里面打开相机权限", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			}
		}
		return super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	protected boolean onBackBtn(boolean systemBackBtn)
	{
		if(m_mode == NONE && m_uiEnabled == false)
		{
			return false;
		}
		if(m_mode != NONE)
		{
			if(m_mode == AREA)
			{
				if(m_citiesPicker != null && m_citiesPicker.onBack())
				{
					return false;
				}
			}
			if(m_mode == BIRTH)
			{
				if(m_netInfo != null)
				{
					m_netInfo.mBirthdayYear = m_localInfo.mBirthdayYear;
					m_netInfo.mBirthdayMonth = m_localInfo.mBirthdayMonth;
					m_netInfo.mBirthdayDay = m_localInfo.mBirthdayDay;
				}
			}
			else if(m_mode == SEX)
			{
				if(m_netInfo != null && m_netInfo.mSex != null && m_netInfo.mSex.length() > 0 && !m_netInfo.mSex.equals(m_localInfo.mSex))
				{
					m_uiEnabled = false;
					isShowDlg(true);
					Message msg = m_threadHandler.obtainMessage();
					msg.what = UPDATE_USERINFO;
					m_threadHandler.sendMessage(msg);
				}
			}
			showEditView(m_mode, NONE);

			m_mode = NONE;

			UpdateDataToUI(m_netInfo, false);
			return false;
		}
		else if(!systemBackBtn)
		{
			mSite.onBack(getContext());
		}

		return true;
	}

	protected void showEditView(int lastMode, int mode)
	{
		clearUI(lastMode);
		m_editFr.setVisibility(View.VISIBLE);
		switch(mode)
		{
			case NICKNAME:
			{
				initNickPage();
				break;
			}
			case PASSWORD:
			{
				initPswPage();
				break;
			}
			case BIRTH:
			{
				m_mainFr.setVisibility(View.VISIBLE);
				initBirthPage();
				break;
			}
			case AREA:
			{
				initAreaPage();
				break;
			}
			case SEX:
			{
				initSexPage();
				break;
			}
			case NONE:
			{
				m_mainFr.setVisibility(View.VISIBLE);
				m_editFr.setVisibility(View.GONE);
			}
			default:
				break;
		}
	}

	protected void clearUI(int mode)
	{
		switch(mode)
		{
			case NICKNAME:
			{
				hideSoftInput(m_editName);
				break;
			}
			case PASSWORD:
			{
				hideSoftInput(m_oldPsw);
				break;
			}
			case BIRTH:
				m_datePicker.SetOnFocusChangeListener(null);
				break;
			case NONE:
				m_mainFr.setVisibility(View.GONE);
				break;
		}
		m_editFr.setOnClickListener(null);
		m_editFr.removeAllViews();
		m_editFr.setVisibility(View.GONE);
	}

	protected void initNickPage()
	{
		InitEditTopBar(getResources().getString(R.string.userinfo_nickname), true);

//		m_editName = new EditTextWithDel(getContext(), -1, R.drawable.login_delete_logo);
		m_editName = new EditText(getContext());
		m_editName.setBackgroundColor(0x4d000000);
		m_editName.setHintTextColor(0xff666666);
		m_editName.setTextColor(Color.WHITE);
		m_editName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
		m_editName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		m_editName.setSingleLine();
		m_editName.setInputType(InputType.TYPE_CLASS_TEXT);
		m_editName.setHint(getResources().getString(R.string.userinfo_nickname));
		m_editName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
		m_editName.setGravity(Gravity.CENTER_VERTICAL);
		MyTextUtils.setupLengthFilter(m_editName, getContext(), 16, true);
		m_editName.setPadding(ShareData.PxToDpi_xhdpi(40), 0, 0, 0);
		LayoutParams fl = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
		fl.gravity = Gravity.TOP;
		fl.topMargin = ShareData.PxToDpi_xhdpi(50) + m_editTopBarHeight;
		m_editName.requestFocus();
		showSoftInput(m_editName);
		m_editFr.addView(m_editName, fl);
		if(m_netInfo != null)
		{
			m_editName.setText(m_netInfo.mNickname);
			m_editName.setSelection(m_editName.getText().toString().length());
		}
	}


	protected void initPswPage()
	{
		InitEditTopBar(getResources().getString(R.string.userinfo_modify_password), true);
		LayoutParams fl;

		LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.topMargin = PxToDpi_xhdpi(50) + m_editTopBarHeight;
		m_editFr.addView(linearLayout, fl);
		{
			LinearLayout.LayoutParams ll;
			PwdEditItem pwdEditItem;

			pwdEditItem = new PwdEditItem(getContext());
//			pwdEditItem.getPswShowIcon().setVisibility(View.GONE);
			m_oldPsw = pwdEditItem.getEditText();
			m_oldPsw.setHint(R.string.userinfo_original_password);
			m_oldPsw.requestFocus();
			showSoftInput(m_oldPsw);
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(100));
			linearLayout.addView(pwdEditItem, ll);

			pwdEditItem = new PwdEditItem(getContext());
			m_newPsw = pwdEditItem.getEditText();
			m_newPsw.setHint(R.string.login_new_password);
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(100));
			ll.topMargin = PxToDpi_xhdpi(1);
			linearLayout.addView(pwdEditItem, ll);
		}
	}

	protected void initBirthPage()
	{
//		m_editFr.setBackgroundColor(0x99000000);
		FrameLayout fr = new FrameLayout(getContext());
		fr.setClickable(true);
		fr.setBackgroundColor(Color.WHITE);
		LayoutParams fl = new LayoutParams(LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(480));
		fl.gravity = Gravity.BOTTOM;
		fr.setLayoutParams(fl);
		m_editFr.addView(fr);
		{
			FrameLayout m_toolFr = new FrameLayout(getContext());
			m_toolFr.setBackgroundColor(0xfff7f7f7);
			fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(80));
			fr.addView(m_toolFr, fl);
			{
				fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
//				fl.leftMargin = ShareData.PxToDpi_xhdpi(8);
				m_cancel = new TextView(getContext());
				m_cancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				m_cancel.setPadding(PxToDpi_xhdpi(20), 0, 0, 0);
				m_cancel.setText(getResources().getString(R.string.Cancel));
				m_cancel.setOnClickListener(m_btnListener);
				m_cancel.setTextColor(0xff1188ff);
				m_toolFr.addView(m_cancel, fl);

				fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
//				fl.rightMargin = ShareData.PxToDpi_xhdpi(8);
				m_ok = new TextView(getContext());
				m_ok.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				m_ok.setPadding(0, 0, PxToDpi_xhdpi(20), 0);
				m_ok.setText(getResources().getString(R.string.Done));
				m_ok.setOnClickListener(m_btnListener);
				m_ok.setTextColor(0xff1188ff);
				m_toolFr.addView(m_ok, fl);
			}

			int year;
			int monthOfYear;
			int dayOfMonth;
			if(m_netInfo != null)
			{
				year = Integer.valueOf(m_netInfo.mBirthdayYear);
				monthOfYear = Integer.valueOf(m_netInfo.mBirthdayMonth);
				dayOfMonth = Integer.valueOf(m_netInfo.mBirthdayDay);
			}
			else
			{
				Calendar calendar = Calendar.getInstance();
				year = calendar.get(Calendar.YEAR);
				monthOfYear = calendar.get(Calendar.MONTH);
				dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
			}
			m_datePicker = new WheelDatePicker(getContext());
			m_datePicker.setBackgroundColor(0xffffffff);
			m_datePicker.SetOnFocusChangeListener(m_OnDateChangedListener);
			m_datePicker.InitDate(year, monthOfYear, dayOfMonth);
			fl = new LayoutParams(LayoutParams.WRAP_CONTENT, PxToDpi_xhdpi(400));
			fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			m_datePicker.setLayoutParams(fl);
			fr.addView(m_datePicker);
		}
		m_editFr.setOnClickListener(m_btnListener);
	}

	protected WheelDatePicker.OnFocusChangeListener m_OnDateChangedListener = new WheelDatePicker.OnFocusChangeListener()
	{

		@Override
		public void onChange(int year, int month, int day)
		{
			if(m_netInfo != null)
			{
				m_netInfo.mBirthdayYear = String.valueOf(year);
				m_netInfo.mBirthdayMonth = String.valueOf(month);
				m_netInfo.mBirthdayDay = String.valueOf(day);
			}
//			UpdateDataToUI(m_netInfo, false);
		}

	};

	protected void initAreaPage()
	{
		InitEditTopBar(getResources().getString(R.string.userinfo_regin), false);
//		m_editFr.setBackgroundDrawable(new BitmapDrawable(m_bg));
		if(m_allAreaInfos == null)
		{
			m_allAreaInfos = AreaList.GetLocationLists(getContext());
		}

		m_citiesPicker = new CitiesPicker(getContext(), m_citiesCB);
		m_citiesPicker.initData(m_allAreaInfos);
//		LayoutParams fl = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.m_screenHeight - (ShareData.PxToDpi_xhdpi(40) + m_editTopBarHeight));
		LayoutParams fl = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		fl.gravity = Gravity.TOP;
		fl.topMargin = m_editTopBarHeight;
		m_citiesPicker.setLayoutParams(fl);
		m_editFr.addView(m_citiesPicker);
	}

	protected CitiesPicker.OnChooseCallback m_citiesCB = new CitiesPicker.OnChooseCallback()
	{

		@Override
		public void onChoose(long id)
		{
			if(m_netInfo != null)
			{
				m_netInfo.mLocationId = String.valueOf(id);
				m_uiEnabled = false;

				isShowDlg(true);
				Message msg = m_threadHandler.obtainMessage();
				msg.what = UPDATE_USERINFO;
				m_threadHandler.sendMessage(msg);

				showEditView(m_mode, NONE);

				m_mode = NONE;

				UpdateDataToUI(m_netInfo, false);
			}

		}
	};

	protected void initSexPage()
	{
		InitEditTopBar(getResources().getString(R.string.userinfo_sex), false);
//		m_editFr.setBackgroundDrawable(new BitmapDrawable(m_bg));
		m_chooseMan = false;
		if(m_netInfo != null && m_netInfo.mSex.equals("男"))
		{
			m_chooseMan = true;
		}
		else if(m_netInfo != null)
		{
			m_netInfo.mSex = "女";
		}

		m_sexGroup = new LinearLayout(getContext());
		m_sexGroup.setOrientation(LinearLayout.VERTICAL);
		LayoutParams fl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.TOP;
		fl.topMargin = PxToDpi_xhdpi(50) + m_editTopBarHeight;
		m_sexGroup.setLayoutParams(fl);
		m_editFr.addView(m_sexGroup);
		{
			m_man = new ChooseItem(getContext());
			m_man.setBackgroundColor(0x4d000000);
			LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(100));
			m_man.setText(getResources().getString(R.string.userinfo_male));
			m_man.showArrow(false);
			m_man.onChoose(m_chooseMan);
			m_man.setOnClickListener(m_btnListener);
//			m_man.setOnTouchListener(m_onTouchListener);
			m_man.setLayoutParams(ll);
			m_sexGroup.addView(m_man);

			m_woman = new ChooseItem(getContext());
			m_woman.setBackgroundColor(0x4d000000);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(100));
			ll.topMargin = ShareData.PxToDpi_xhdpi(1);
			m_woman.setText(getResources().getString(R.string.userinfo_female));
			m_woman.showArrow(false);
			m_woman.onChoose(!m_chooseMan);
			m_woman.setOnClickListener(m_btnListener);
//			m_woman.setOnTouchListener(m_onTouchListener);
			m_woman.setLayoutParams(ll);
			m_sexGroup.addView(m_woman);
		}
	}

	protected void InitEditTopBar(String title, boolean hasCompleteBtn)
	{
		m_editTopBar = new FrameLayout(getContext());
		m_editTopBar.setBackgroundColor(0x4d000000);
		LayoutParams fl = new LayoutParams(LayoutParams.MATCH_PARENT, m_editTopBarHeight);
		fl.gravity = Gravity.TOP;
		m_editTopBar.setLayoutParams(fl);
		m_editFr.addView(m_editTopBar);
		{
			m_editBack = new ImageView(getContext());
			m_editBack.setImageResource(R.drawable.framework_back_btn);
			fl = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
			m_editBack.setLayoutParams(fl);
			m_editTopBar.addView(m_editBack);
			m_editBack.setOnClickListener(m_btnListener);
//			m_editBack.setOnTouchListener(m_onTouchListener);

			m_editTitle = new TextView(getContext());
			m_editTitle.setText(title);
			m_editTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
			m_editTitle.setTextColor(Color.WHITE);
			fl = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER;
			m_editTitle.setLayoutParams(fl);
			m_editTopBar.addView(m_editTitle);

			if(hasCompleteBtn)
			{
				m_editComplete = new TextView(getContext());
				m_editComplete.setText(getResources().getString(R.string.Save));
				m_editComplete.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
				m_editComplete.setTextColor(0xffFFC433);
				fl = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
				fl.rightMargin = PxToDpi_xhdpi(36);
				m_editComplete.setLayoutParams(fl);
				m_editTopBar.addView(m_editComplete);
				m_editComplete.setOnClickListener(m_btnListener);
//				m_editComplete.setOnTouchListener(m_onTouchListener);
			}
		}
	}

	public void isShowDlg(boolean isShow)
	{
		if(mProgressDialog != null)
		{
			if(isShow)
			{
				mProgressDialog.setCancelable(false);
				mProgressDialog.setMessage(getResources().getString(R.string.updating));
				mProgressDialog.show();
			}
			else
			{
				mProgressDialog.dismiss();
			}

		}
	}

	public void showSoftInput(View v)
	{
		InputMethodManager manager = (InputMethodManager)getContext().
				getSystemService(Context.INPUT_METHOD_SERVICE);
		if(manager != null)
		{
			manager.toggleSoftInputFromWindow(v.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);
		}
	}

	public void hideSoftInput(View v)
	{
		InputMethodManager manager = (InputMethodManager)getContext().
				getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public void setDatas(final String id)
	{
		m_localInfo = UserMgr.ReadCache(getContext());
		UpdateDataToUI(m_localInfo, true);
		m_editable = false;
		Message msg = m_threadHandler.obtainMessage();
		msg.what = CHECK;
		msg.obj = id;
		m_threadHandler.sendMessage(msg);
	}

	public void UpdateDataToUI(UserInfo userInfo, boolean headUpdate)
	{
		if(headUpdate)
		{
			Bitmap head = BitmapFactory.decodeFile(UserMgr.HEAD_PATH);
			head = ImageUtil.makeCircleBmp(head, 0, 0);
			m_headImg.setImageBitmap(head);
		}
		if(userInfo != null)
		{
			m_nickname.setInfo(userInfo.mNickname);
//			String sex = "未填写";
			String sex = "";
			if(userInfo.mSex != null && userInfo.mSex.length() > 0)
			{
				sex = userInfo.mSex;
			}
			if(sex.equals("男"))
			{
				sex = getResources().getString(R.string.userinfo_male);
			}
			if(sex.equals("女"))
			{
				sex = getResources().getString(R.string.userinfo_female);
			}
			m_sex.setInfo(sex);
//			String date = "未填写";
			String date = "";
			if(userInfo.mBirthdayYear != null && userInfo.mBirthdayYear.length() > 0)
			{
				date = userInfo.mBirthdayYear + "-" + userInfo.mBirthdayMonth + "-" + userInfo.mBirthdayDay;
			}
			m_birth.setInfo(date);
			if(m_allAreaInfos == null)
			{
				m_allAreaInfos = AreaList.GetLocationLists(getContext());
			}
//			String name = "未填写";
			String name = "";
			String temp = null;
			if(userInfo.mLocationId != null && userInfo.mLocationId.length() > 0)
			{
				temp = AreaList.GetLocationStr(m_allAreaInfos, Long.valueOf(userInfo.mLocationId), " ");
			}
			if(temp != null && temp.length() > 0)
			{
				name = temp;
			}
			m_area.setInfo(name);
		}
	}

	@Override
	public void onBack()
	{
		if(m_uiEnabled && onBackBtn(true))
		{
			if(m_netInfo != null)
			{
				if(m_netInfo.mCode == 0)
				{
					UserMgr.SaveCache(getContext(),m_netInfo);
					EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
				}
			}
			isClose = true;
			MyBeautyStat.onPageStartByRes(R.string.个人资料页_退出个人资料页);
			mSite.onBack(getContext());
		}
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		return false;
	}


	@Override
	public void onClose()
	{
		m_thread.quit();
		isClose = true;

		if(mProgressDialog != null)
		{
			mProgressDialog.dismiss();
		}

		TongJiUtils.onPageEnd(getContext(), TAG);
		MyBeautyStat.onPageEndByRes(R.string.个人资料页);
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

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params)
	{
		super.onPageResult(siteID, params);
		if(siteID == SiteID.CLIPHEAD)
		{
			if(params != null)
			{
				String headUrl = (String)params.get(EditHeadIconImgPage.KEY_HEAD_URL);
				if(headUrl != null)
				{
					if(m_netInfo != null)
					{
						m_netInfo.mUserIcon = headUrl;
						UpdateDataToUI(m_netInfo, true);
					}
					EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
					showEditView(m_mode, NONE);
					m_mode = NONE;
				}
			}
		}
	}

//	private OnTouchListener m_onTouchListener = new OnTouchListener()
//	{
//		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//		@Override
//		public boolean onTouch(View v, MotionEvent event)
//		{
//			if(event.getAction() == MotionEvent.ACTION_DOWN)
//			{
//				if(v == m_nickname)
//				{
//					m_nickname.setAlpha(0.5f);
//				}
//				else if(v == m_password)
//				{
//					m_password.setAlpha(0.5f);
//				}
//				else if(v == m_sex)
//				{
//					m_sex.setAlpha(0.5f);
//				}
//				else if(v == m_birth)
//				{
//					m_birth.setAlpha(0.5f);
//				}
//				else if(v == m_area)
//				{
//					m_area.setAlpha(0.5f);
//				}
//				else if(v == m_exitLogin)
//				{
//					m_exitLogin.setAlpha(0.5f);
//				}
//				else if(v == m_man)
//				{
//					m_man.setAlpha(0.5f);
//				}
//				else if(v == m_woman)
//				{
//					m_woman.setAlpha(0.5f);
//				}
//				else if(v == m_headImg)
//				{
//					m_headImg.setAlpha(0.5f);
//				}
//				else if(v == m_cameraBtn)
//				{
//					m_cameraBtn.setAlpha(0.5f);
//				}
//				else if(v == m_editBack)
//				{
//					m_editBack.setAlpha(0.5f);
//				}
//				else if(v == m_editComplete)
//				{
//					m_editComplete.setAlpha(0.5f);
//				}
//			}
//			else if(event.getAction() == MotionEvent.ACTION_UP)
//			{
//				if(v == m_nickname)
//				{
//					m_nickname.setAlpha(1f);
//				}
//				else if(v == m_password)
//				{
//					m_password.setAlpha(1f);
//				}
//				else if(v == m_sex)
//				{
//					m_sex.setAlpha(1f);
//				}
//				else if(v == m_birth)
//				{
//					m_birth.setAlpha(1f);
//				}
//				else if(v == m_area)
//				{
//					m_area.setAlpha(1f);
//				}
//				else if(v == m_exitLogin)
//				{
//					m_exitLogin.setAlpha(1f);
//				}
//				else if(v == m_man)
//				{
//					m_man.setAlpha(1f);
//				}
//				else if(v == m_woman)
//				{
//					m_woman.setAlpha(1f);
//				}
//				else if(v == m_headImg)
//				{
//					m_headImg.setAlpha(1f);
//				}
//				else if(v == m_cameraBtn)
//				{
//					m_cameraBtn.setAlpha(1f);
//				}
//				else if(v == m_editBack)
//				{
//					m_editBack.setAlpha(1f);
//				}
//				else if(v == m_editComplete)
//				{
//					m_editComplete.setAlpha(1f);
//				}
//			}
//			return false;
//		}
//	};
}
