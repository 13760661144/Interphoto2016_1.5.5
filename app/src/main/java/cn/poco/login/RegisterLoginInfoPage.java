package cn.poco.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.EventCenter;
import cn.poco.framework.EventID;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.login.site.RegisterLoginInfoPageSite;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.login.util.MyTextUtils;
import cn.poco.login.util.NoDoubleClickListener;
import cn.poco.login.util.UserMgr;
import cn.poco.login.widget.CommonBtn;
import cn.poco.login.widget.EditTextWithDel;
import cn.poco.login.widget.TipDialog;
import cn.poco.loginlibs.info.BaseInfo;
import cn.poco.loginlibs.info.LoginInfo;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.AppInterface;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.ImageUtil;
import cn.poco.utils.PermissionUtils;
import cn.poco.utils.Utils;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.RIGHT_OF;
import static cn.poco.login.userinfo.EditHeadIconImgPage.KEY_HEAD_URL;
import static cn.poco.tianutils.ShareData.PxToDpi_xhdpi;


public class RegisterLoginInfoPage extends IPage
{
	public static String DEF_HEAD_URL = "http://avatar.adnonstop.com/interphoto/20170904/15/15098654920170904152807486.png";
	private static final String TAG = "注册填写资料";
	private int topBarHeight = PxToDpi_xhdpi(80);
	private int userHeadIconWidth = PxToDpi_xhdpi(180);

	private FrameLayout m_topTabFr;
	private ImageView m_cancelBtn;
	private FrameLayout m_MainLayout;

	//圆形头像
	private FrameLayout userHeadFrame;
	private ImageView cameraImageView;
	private ImageView userHeadImg;

	//输入昵称
	private EditTextWithDel centerNickInput;
	private View centerNickBottomLine;
	//完成按钮
	private CommonBtn okBtn;

	//头像地址
	private String iconUrl = null;
	private boolean iconUpload = false;
	private LoginPageInfo mLoginPageInfo;

	//匹配非表情符号的正则表达式
//	private final String reg = "^([a-z]|[A-Z]|[0-9]|[\u2E80-\u9FFF]){3,}|@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?|[wap.]{4}|[www.]{4}|[blog.]{5}|[bbs.]{4}|[.com]{4}|[.cn]{3}|[.net]{4}|[.org]{4}|[http://]{7}|[ftp://]{6}$";
//	private Pattern pattern = Pattern.compile(reg);

	protected RegisterLoginInfoPageSite mSite;
	private TipDialog dialog;
	private boolean isClose = false;

	public RegisterLoginInfoPage(Context context, BaseSite site)
	{
		super(context, site);
		mSite = (RegisterLoginInfoPageSite)site;
		init();

		TongJiUtils.onPageStart(getContext(), TAG);
	}

	/**
	 * @param params img：背景图片路径
	 *               info:LoginPageInfo
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		if(params != null)
		{
			if(params.get("img") != null)
			{
				SetBk(Utils.DecodeFile((String)params.get("img"), null));
			}
			if(params.get(LoginPage.KEY_INFO) != null)
			{
				mLoginPageInfo = (LoginPageInfo)params.get(LoginPage.KEY_INFO);
				iconUrl = mLoginPageInfo.m_userIcon;
				if(iconUrl != null) {
					Glide.with(getContext()).load(iconUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
						@Override
						public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
							if (resource != null) {
								userHeadImg.setImageBitmap(ImageUtil.makeCircleBmp(resource, 0, 0));
							}
						}
					});
					if(!DEF_HEAD_URL.equals(iconUrl))
					{
						iconUpload = true;
					}
				}
			}
		}

	}

	@Override
	public void onBack()
	{
		if(dialog != null)
		{
			if(!dialog.isShowing())
			{
				dialog.show();
			}
			else
			{
				dialog.dismiss();
			}
		}
		else
		{
			mSite.onBack(getContext());
		}
	}

	public void init()
	{
		setBackgroundColor(0xbf000000);

		LinearLayout.LayoutParams llParams;
		RelativeLayout.LayoutParams rlParams;
		LayoutParams flParams;
		m_MainLayout = new FrameLayout(getContext());
		flParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(m_MainLayout, flParams);
		{
			flParams = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_topTabFr = new FrameLayout(getContext());
			m_topTabFr.setMinimumHeight(topBarHeight);
			m_topTabFr.setBackgroundColor(0x4b000000);
			m_topTabFr.setId(R.id.login_registerinfopage_m_topbar);
			addView(m_topTabFr, flParams);
			{
				// 返回到上一层按钮
				m_cancelBtn = new ImageView(getContext());
				m_cancelBtn.setImageResource(R.drawable.framework_back_btn);
				flParams = new LayoutParams(LayoutParams.WRAP_CONTENT, PxToDpi_xhdpi(80));
				flParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
				m_cancelBtn.setOnClickListener(mOnClickListener);
				m_cancelBtn.setOnTouchListener(mOnTouchListener);
				m_topTabFr.addView(m_cancelBtn, flParams);

				TextView title = new TextView(getContext());
				title.setText(getResources().getString(R.string.login_basic_information));
				title.setTextColor(Color.WHITE);
				title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
				flParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				flParams.gravity = Gravity.CENTER;
				m_topTabFr.addView(title, flParams);
			}

			flParams = new LayoutParams(userHeadIconWidth, userHeadIconWidth);
			flParams.topMargin = PxToDpi_xhdpi(140) + topBarHeight;
			flParams.gravity = Gravity.CENTER_HORIZONTAL;
			userHeadFrame = new FrameLayout(getContext());
			userHeadFrame.setId(R.id.login_registerinfopage_userheadcon);
			addView(userHeadFrame, flParams);
			{
				flParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				userHeadImg = new ImageView(getContext());
				userHeadImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
				userHeadImg.setImageResource(R.drawable.login_head_logo);
				userHeadImg.setOnClickListener(mOnClickListener);
				userHeadImg.setOnTouchListener(mOnTouchListener);
				userHeadFrame.addView(userHeadImg, flParams);

				flParams = new LayoutParams(PxToDpi_xhdpi(50), PxToDpi_xhdpi(50));
				flParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
				cameraImageView = new ImageView(getContext());
				cameraImageView.setImageResource(R.drawable.userinfo_camera_btn);
				cameraImageView.setOnClickListener(mOnClickListener);
				userHeadFrame.addView(cameraImageView, flParams);
			}

			//昵称
			flParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(100));
			flParams.topMargin = PxToDpi_xhdpi(90) + PxToDpi_xhdpi(140) + topBarHeight + userHeadIconWidth;
			RelativeLayout rlNickLayout = new RelativeLayout(getContext());
			rlNickLayout.setMinimumHeight(PxToDpi_xhdpi(100));
			rlNickLayout.setPadding(PxToDpi_xhdpi(50), 0, PxToDpi_xhdpi(50), 0);
			rlNickLayout.setId(R.id.login_registerinfopage_nickname);
			addView(rlNickLayout, flParams);
			{
				rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				rlParams.addRule(RelativeLayout.CENTER_VERTICAL);
				rlParams.addRule(ALIGN_PARENT_LEFT);
				rlParams.leftMargin = PxToDpi_xhdpi(10);
				TextView tip = new TextView(getContext());
				tip.setText(getResources().getString(R.string.userinfo_nickname));
				tip.setTextColor(Color.GRAY);
				tip.setId(R.id.login_registerinfopage_centernickicon);
				tip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				rlNickLayout.addView(tip, rlParams);

				rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
				rlParams.addRule(RIGHT_OF, R.id.login_registerinfopage_centernickicon);
				rlParams.addRule(RelativeLayout.CENTER_VERTICAL);
				rlParams.leftMargin = PxToDpi_xhdpi(60);
				centerNickInput = new EditTextWithDel(getContext(), -1, R.drawable.login_delete_logo);
				centerNickInput.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
				centerNickInput.setPadding(0, 0, PxToDpi_xhdpi(5), 0);
				centerNickInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.5f);
				centerNickInput.setTextColor(Color.WHITE);
				centerNickInput.setHintTextColor(0xffb2b2b2);
				centerNickInput.setBackgroundColor(0x00000000);
				centerNickInput.setHint(getResources().getString(R.string.userinfo_nickname));
//					centerNickInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
				centerNickInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
				centerNickInput.setSingleLine();
				MyTextUtils.setupLengthFilter(centerNickInput, getContext(), 16, true);
				centerNickInput.setInputType(InputType.TYPE_CLASS_TEXT);
				rlNickLayout.addView(centerNickInput, rlParams);

				centerNickInput.setOnFocusChangeListener(new OnFocusChangeListener()
				{
					@Override
					public void onFocusChange(View v, boolean hasFocus)
					{
						if(!hasFocus)
						{
							if(v instanceof EditTextWithDel)
							{
								EditTextWithDel editText = (EditTextWithDel)v;
								editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
							}
						}
						else
						{
							if(v instanceof EditTextWithDel)
							{
								EditTextWithDel editText = (EditTextWithDel)v;
								editText.setDrawable();
							}
						}
					}
				});
				centerNickInput.addTextChangedListener(new TextWatcher()
				{
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after)
					{

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count)
					{
						if(s.toString().length() > 0)
						{
							okBtn.setEnabled(true);
						}
						else
						{
							okBtn.setEnabled(false);
						}
					}

					@Override
					public void afterTextChanged(Editable s)
					{
						if(s.toString().length() > 0)
						{
							okBtn.setEnabled(true);
						}
						else
						{
							okBtn.setEnabled(false);
						}
					}
				});
				rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(1));
				rlParams.addRule(ALIGN_PARENT_BOTTOM);
				centerNickBottomLine = new View(getContext());
				centerNickBottomLine.setBackgroundColor(0xff666666);
				rlNickLayout.addView(centerNickBottomLine, rlParams);
			}
			//完成按钮
			flParams = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(608), ShareData.PxToDpi_xhdpi(96));
			flParams.topMargin = PxToDpi_xhdpi(130) + PxToDpi_xhdpi(90) + PxToDpi_xhdpi(140) + topBarHeight + userHeadIconWidth + PxToDpi_xhdpi(100);
			flParams.gravity = Gravity.CENTER_HORIZONTAL;
			okBtn = new CommonBtn(getContext());
			okBtn.setEnabled(false);
			okBtn.setText(getResources().getString(R.string.Done));
			okBtn.setOnClickListener(mOnClickListener);
			addView(okBtn, flParams);

			setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					hideKeyboard();
				}
			});
		}
		initDialog();
	}

	private void initDialog()
	{
		dialog = new TipDialog((Activity)getContext());
		dialog.setMessage(getResources().getString(R.string.login_register_tip));
		dialog.setTwoBtnText(getResources().getString(R.string.yes), getResources().getString(R.string.no), new TipDialog.OnCallBack2()
		{
			@Override
			public void onLeftClick()
			{
				dialog.dismiss();
			}

			@Override
			public void onRightClick()
			{
				dialog.dismiss();
				MyBeautyStat.onClickByRes(R.string.注册账号页_退出注册);
				mSite.onBack(getContext());
			}
		});
	}

	protected void SetBk(Bitmap bmp)
	{
		if(bmp != null)
		{
			setBackgroundDrawable(new BitmapDrawable(bmp));
		}
		else
		{
			setBackgroundColor(0xbf000000);
		}
	}

	private NoDoubleClickListener mOnClickListener = new NoDoubleClickListener()
	{
		@Override
		public void onNoDoubleClick(View v)
		{
			if(v == userHeadImg)
			{
				MyBeautyStat.onClickByRes(R.string.注册账号页_设置头像);
				hideKeyboard();
				mSite.uploadHeadImg(getContext());
			}
			else if(v == okBtn)
			{
				MyBeautyStat.onClickByRes(R.string.注册账号页_完成资料填写);
				isFitRule();
			}
			else if(v == m_cancelBtn)
			{
				hideKeyboard();
				onBack();
			}
			else if(v == cameraImageView)
			{
				boolean flag = true;
				if(Build.VERSION.SDK_INT >= 23)
				{
					flag = PermissionUtils.checkCameraPermission(getContext());
				}
				if(flag)
				{
					hideKeyboard();
					mSite.onCamera(getContext());
				}
				else
				{
					PermissionUtils.requestPermissions(getContext(), Manifest.permission.CAMERA);
				}
			}
		}
	};

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
					mOnClickListener.onClick(cameraImageView);
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

	private void hideKeyboard()
	{
		InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
	}

	/**
	 * 规则判断
	 */
	private boolean isFitRule()
	{
		String nickName = centerNickInput.getText().toString();
		nickName = nickName.trim();
		nickName = nickName.replace(" ", "");
		//昵称为空
		if(nickName.length() == 0)
		{
			LoginOtherUtil.showToast(getContext(),R.string.toast_enter_nickname);
			return false;
		}
		if(!iconUpload)
		{
			LoginOtherUtil.showToast(getContext(),R.string.toast_upload_avatar);
			return false;
		}
		if(iconUpload && nickName.length() > 0 && mLoginPageInfo != null && mLoginPageInfo.m_info != null)
		{
			hideKeyboard();
			fillRegisterInfo(mLoginPageInfo.m_info.mUserId, mLoginPageInfo.m_info.mAccessToken, iconUrl, nickName);
			return true;
		}
		return false;
	}

	protected void fillRegisterInfo(final String userId, final String token, final String iconUrl, final String nickName)
	{
		okBtn.setLoadingState();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				final BaseInfo info = cn.poco.loginlibs.LoginUtils.fillUserRegisterInfo(userId, token, iconUrl, nickName, null, AppInterface.GetInstance(getContext()));
				if(info != null && info.mCode == 0) {
					LoginInfo loginInfo = mLoginPageInfo.m_info;
					final boolean isSuccess = LoginOtherUtil.saveUserInfoToLocalBase(getContext(),loginInfo.mUserId, loginInfo.mAccessToken);
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if(!isClose) {
								okBtn.setNormalState();
								if (isSuccess) {
									LoginOtherUtil.setSettingInfo(getContext(),mLoginPageInfo.m_info);
									SettingInfoMgr.GetSettingInfo(getContext()).SetPoco2HeadUrl(iconUrl);
									SettingInfoMgr.GetSettingInfo(getContext()).SetPocoNick(nickName);
									EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
									LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_login_success));
									mSite.fillInfoSuccess(getContext());
								} else {
									LoginOtherUtil.showToast(getContext(),R.string.toast_network_net_connected);
								}
							}
						}
					});
				}else{
					mHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							if(!isClose)
							{
								okBtn.setNormalState();
								if(info != null)
								{
									if(info.mCode == 10002)
									{
										LoginOtherUtil.showToast(getContext(),R.string.toast_upload_avatar);
									}
									else if(info.mCode == 10003)
									{
										LoginOtherUtil.showToast(getContext(),R.string.toast_enter_nickname);
									}
									else
									{
										if(info.mMsg != null)
										{
											LoginOtherUtil.showToast(getContext(),info.mMsg);
										}
										{
											LoginOtherUtil.showToast(getContext(),R.string.toast_submit_info_failure);
										}
									}
								}
								else
								{
									LoginOtherUtil.showToast(getContext(),R.string.toast_network_net_connected);
								}
							}
						}
					});
				}
			}
		}).start();
	}

	private OnTouchListener mOnTouchListener = new OnTouchListener()
	{
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@SuppressLint("NewApi")
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				if(v == m_cancelBtn)
				{
					m_cancelBtn.setAlpha(0.5f);
				}
				else if(v == userHeadImg)
				{
					userHeadImg.setAlpha(0.5f);
				}
			}
			else if(event.getAction() == MotionEvent.ACTION_UP)
			{
				if(v == m_cancelBtn)
				{
					m_cancelBtn.setAlpha(1.0f);
				}
				else if(v == userHeadImg)
				{
					userHeadImg.setAlpha(1.0f);
				}
			}
			return false;
		}
	};


	private Handler mHandler = new Handler(Looper.getMainLooper());

	@Override
	public void onClose()
	{
		isClose = true;
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

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params)
	{
		super.onPageResult(siteID, params);
		if(siteID == SiteID.CLIPHEAD)
		{
			File file = new File(UserMgr.HEAD_PATH);
			if(file.exists())
			{
				Bitmap temp = BitmapFactory.decodeFile(UserMgr.HEAD_PATH);
				temp = ImageUtil.makeCircleBmp(temp, 0, 0);
				userHeadImg.setImageBitmap(temp);
			}
			if(params != null)
			{
				if(params.get(KEY_HEAD_URL) != null && ((String)params.get(KEY_HEAD_URL)).length() > 0)
				{
					iconUrl = (String)params.get(KEY_HEAD_URL);
					iconUpload = true;
				}
			}
		}
	}
}
