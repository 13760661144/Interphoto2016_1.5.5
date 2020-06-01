package cn.poco.login;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.EventCenter;
import cn.poco.framework.EventID;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.login.util.UserMgr;
import cn.poco.login.widget.CommonBtn;
import cn.poco.login.widget.EditTextWithDel;
import cn.poco.login.widget.PwdItem;
import cn.poco.loginlibs.LoginUtils;
import cn.poco.loginlibs.info.LoginInfo;
import cn.poco.loginlibs.info.RegisterLoginInfo;
import cn.poco.loginlibs.info.UserInfo;
import cn.poco.loginlibs.info.VerifyInfo;
import cn.poco.system.AppInterface;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;
import cn.poco.watermarksync.manager.WatermarkSyncManager;

import static cn.poco.tianutils.ShareData.PxToDpi_xhdpi;

/**
 * Created by lgd on 2017/8/25.
 */

abstract public class BaseLoginPage extends IPage {

//    protected final static int PAGE_LOGIN = 1;
//    protected final static int PAGE_RESET_PSW = 2;
//    protected final static int PAGE_REGISETER = 3;
//    private int mCurPageType = PAGE_LOGIN;

    private final static int MAX_MOBILE_NUM = 7;
    private final static int MAX_PWD_NUM = 1;
    private final static int MAX_CODE_NUM = 6;
    public final static String KEY_INFO = "key_info";

    protected FrameLayout mTopFr;
    protected ImageView mCancelBtn;
    protected ImageView mLogo;
    protected int mTopHeight;

    protected LinearLayout mCenterLl;

    //输入手机号
    protected RelativeLayout mPhoneParent;
    protected RelativeLayout mPhoneAreaRl;
    protected ImageView mPhoneAreaLogo;
    protected TextView mPhoneAreaCode;
    protected EditTextWithDel mPhoneInput;
    protected String mAreaCodeNum = "86";
    protected String mCountry = "中国";
    protected View mPhoneLine;

    //输入验证码
    protected RelativeLayout mCodeParent;
    protected ImageView mCodeLogo;
    protected EditTextWithDel mCodeInput;
    public TextView mCodeTip;
    protected View mCodeLine;
    protected boolean isTimerDone = true;

    //输入密码
    protected EditTextWithDel mPwdInput;
    protected View mPwdLine;

    //重设密码和注册按钮
    protected CommonBtn mOkBtn;

    protected boolean isClose = false;
    protected int bkColor = 0xbf000000;
    protected int lineColor = 0xff666666;

    protected Handler mHandler;
    protected HandlerThread mThread;
    protected Handler mThreadHandler;
    protected final static int GET_VERIFY_CODE = 1;
    protected final static int CHECK_VERIFY_CODE = 2;
    protected final static int CHANGE_PASSWORD = 3;
    protected final static int REGISTER = 4;
    protected final static int CHECK_LOGIN = 5;
    protected final static int LOGIN = 10;
    protected final static int LOGIN_SUCCEED = 6;
    protected final static int LOGIN_FAIL = 7;
    protected final static int FILL_REGISTER_INFO = 8;
    protected LoginUtils.VerifyCodeType mVerifyType = LoginUtils.VerifyCodeType.register;
    protected PwdItem mPwdItem;

    public BaseLoginPage(Context context, BaseSite site) {
        super(context, site);
        initData();
        initUi();
    }

    private void initData() {
        mThread = new HandlerThread("BaseLoginPage");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper()) {
            @Override
            public void dispatchMessage(Message msg) {
                switch (msg.what) {
                    case LOGIN: {
                        final LoginInfo info = LoginUtils.userLogin(mAreaCodeNum, mPhoneInput.getText().toString(), mPwdInput.getText().toString(), AppInterface.GetInstance(getContext()));
                        Message message = mHandler.obtainMessage();
                        message.what = msg.what;
                        message.obj = info;
                        mHandler.sendMessage(message);
                        break;
                    }
                    case CHANGE_PASSWORD: {
                        LoginInfo info = LoginUtils.forgetPassWord(mAreaCodeNum, mPhoneInput.getText().toString(), mCodeInput.getText().toString(), mPwdInput.getText().toString(), AppInterface.GetInstance(getContext()));
                        Message message = mHandler.obtainMessage();
                        message.what = msg.what;
                        message.obj = info;
                        mHandler.sendMessage(message);
                        break;
                    }
                    case GET_VERIFY_CODE: {
                        VerifyInfo info = LoginUtils.getVerifyCode(mAreaCodeNum, mPhoneInput.getText().toString(), mVerifyType, AppInterface.GetInstance(getContext()));
                        Message message = mHandler.obtainMessage();
                        message.what = msg.what;
                        message.obj = info;
                        mHandler.sendMessage(message);
                        break;
                    }
                    case REGISTER: {
                        final RegisterLoginInfo info = LoginUtils.register(mAreaCodeNum, mPhoneInput.getText().toString(), mCodeInput.getText().toString(), mPwdInput.getText().toString(), AppInterface.GetInstance(getContext()));
                        Message message = mHandler.obtainMessage();
                        message.what = msg.what;
                        message.obj = info;
                        mHandler.sendMessage(message);
//						if(info != null && info.mCode == 0)
//						{
//							final boolean isSuccess = LoginOtherUtil.saveUserInfoToLocalBase(info.mUserId, info.mAccessToken);
//							mHandler.post(new Runnable()
//							{
//								@Override
//								public void run()
//								{
//									if(!isClose)
//									{
//										if(isSuccess)
//										{
//											LoginOtherUtil.setSettingInfo(info);
//											EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
//											LoginPageInfo pageInfo = new LoginPageInfo();
//											pageInfo.m_info = info;
//											pageInfo.m_areaCodeNum = mAreaCodeNum;
//											pageInfo.m_phoneNum = mPhoneInput.getText().toString();
//											mCallBack.registerSuccess(pageInfo);
//										}
//										else
//										{
//											LoginOtherUtil.showToast(getContext(),R.string.toast_register_failure);
//										}
//									}
//								}
//							});
//						}
//						else
//						{
//							Message message = mHandler.obtainMessage();
//							message.what = msg.what;
//							message.obj = info;
//							mHandler.sendMessage(message);
//						}
                        break;
                    }
                    case CHECK_LOGIN:
                        Message message = mHandler.obtainMessage();
                        LoginInfo info = (LoginInfo) msg.obj;
                        UserInfo userInfo = LoginUtils.getUserInfo(info.mUserId, info.mAccessToken, AppInterface.GetInstance(getContext()));
                        if (userInfo != null && userInfo.mCode == 0 && userInfo.mProtocolCode == 200) {
                            String defaultName = "interphoto用户";
                            String defaultUrl = "http://avatar.adnonstop.com/interphoto/20170904/15/15098654920170904152807486.png";
                            if (userInfo.mNickname.equals(defaultName) || userInfo.mUserIcon.equals(defaultUrl)) {
                                LoginPageInfo pageInfo = new LoginPageInfo();
                                pageInfo.m_info = info;
                                if(!userInfo.mUserIcon.equals(defaultUrl)){
                                    pageInfo.m_userIcon = userInfo.mUserIcon;
                                }
                                message.what = FILL_REGISTER_INFO;
                                message.obj = pageInfo;
                            } else {
                                UserMgr.DownloadHeadImg(getContext(), userInfo.mUserIcon);
                                UserMgr.SaveCache(getContext(),userInfo);
                                message.obj = info;
                                message.what = LOGIN_SUCCEED;
                            }
                        } else {
                            if(BaseLoginPage.this instanceof LoginPage) {
                                message.obj = info;
                            }
                            message.what = LOGIN_FAIL;
                        }
                        mHandler.sendMessage(message);
                        break;
                    default:
                        super.dispatchMessage(msg);
                        break;
                }
            }
        };
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (!isClose) {
                    switch (msg.what) {
                        case LOGIN: {
                            LoginInfo info = (LoginInfo) msg.obj;
                            if (info != null && info.mCode == 0) {
                                Message message = mThreadHandler.obtainMessage();
                                message.what = CHECK_LOGIN;
                                message.obj = info;
                                mThreadHandler.sendMessage(message);
                            } else {
                                mOkBtn.setNormalState();
                                Message message = mHandler.obtainMessage();
                                message.obj = info;
                                message.what = LOGIN_FAIL;
                                mHandler.sendMessage(message);
                            }
                            break;
                        }
                        case CHANGE_PASSWORD: {
                            mOkBtn.setNormalState();
                            LoginInfo info = (LoginInfo) msg.obj;
                            if (info != null && info.mCode == 0) {
                                mOkBtn.setLoadingState();
                                LoginOtherUtil.showToast(getContext(),R.string.toast_modify_password_success);
                                Message message = mThreadHandler.obtainMessage();
                                message.what = CHECK_LOGIN;
                                message.obj = info;
                                mThreadHandler.sendMessage(message);
                            } else {
                                resetPwdFailed(info);
                                if (info != null) {
                                    if (info.mCode == 10001 || info.mCode == 10004) {
                                        LoginOtherUtil.showToast(getContext(),R.string.toast_param_error);
                                    } else if (info.mCode == 10002) {
                                        LoginOtherUtil.showToast(getContext(),R.string.toast_code_error);
                                        mCodeInput.setText("");
                                        checkOkBtnEnable();
                                        showKeyboard(mCodeInput);
                                    } else if (info.mCode == 10005) {
                                        LoginOtherUtil.showToast(getContext(),R.string.toast_password_error);
                                    } else if (info.mCode == 10006 || info.mCode == 10003) {
                                        LoginOtherUtil.showToast(getContext(),R.string.toast_user_not_exist);
                                    } else if (info.mCode == 10007) {
                                        LoginOtherUtil.showToast(getContext(),R.string.toast_operate_error);
                                    } else if (info.mCode == 10008) {
                                        LoginOtherUtil.showToast(getContext(),R.string.toast_unknown_error);
                                    } else {
                                        if (info != null && info.mMsg != null) {
                                            LoginOtherUtil.showToast(getContext(),info.mMsg);
                                        } else {
                                            LoginOtherUtil.showToast(getContext(),R.string.toast_modify_password_failure);
                                        }
                                    }
                                } else {
                                    LoginOtherUtil.showToast(getContext(),R.string.toast_modify_password_failure);
                                }
                            }
                            break;
                        }
                        case GET_VERIFY_CODE: {
                            VerifyInfo info = (VerifyInfo) msg.obj;
                            if (info != null && info.mCode == 0) {
                                mTimer.start();
                            } else {
                                isTimerDone = true;
                                mCodeTip.setTextColor(0xffFFC433);
                                mCodeTip.setText(R.string.login_get_code_again);
                            }
                            if (info != null) {
                                if (info.mCode == 10101 && mVerifyType == LoginUtils.VerifyCodeType.find) {
                                    LoginOtherUtil.showToast(getContext(),R.string.toast_phone_unregistered);
                                } else if (info.mCode == 10100 && mVerifyType == LoginUtils.VerifyCodeType.register) {
                                    LoginOtherUtil.showToast(getContext(),R.string.toast_phone_registered);
                                } else if (info.mCode == 0) {
                                    LoginOtherUtil.showToast(getContext(),R.string.toast_send_code_success);
                                } else if (info.mCode == 10102) {
                                    LoginOtherUtil.showToast(getContext(),R.string.toast_enter_zone);
                                } else if (info.mCode == 10001) {
                                    LoginOtherUtil.showToast(getContext(),R.string.toast_enter_correct_phone);
                                } else if (info.mCode == 10002) {
                                    LoginOtherUtil.showToast(getContext(),R.string.toast_too_much_try_again);
                                } else {
                                    if (info != null && info.mMsg != null) {
                                        LoginOtherUtil.showToast(getContext(),info.mMsg);
                                    } else {
                                        LoginOtherUtil.showToast(getContext(),R.string.toast_get_code_failure);
                                    }
                                }
                            } else {
                                LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_network_net_connected));
                            }
                            getCodeFinish();
                            break;
                        }
                        case CHECK_VERIFY_CODE: {
//						CheckVerifyInfo verifyInfo = (CheckVerifyInfo)msg.obj;
//						if(verifyInfo != null)
//						{
//							if(verifyInfo.mCode == 0)
//							{
//								if(verifyInfo.mCheckResult)
//								{
//									Message message = mThreadHandler.obtainMessage();
//									message.setData(msg.getData());
//									if(mState == STATE_RESETPSW)
//									{
//										message.what = CHANGE_PASSWORD;
//									}
//									else
//									{
//										message.what = REGISTER;
//									}
//									mThreadHandler.sendMessage(message);
//								}
//								else
//								{
//									LoginOtherUtil.showToast(getContext(),"验证码不正确");
//								}
//							}
//							else if(verifyInfo.mCode == 10002 || verifyInfo.mCode == 10003)
//							{
//								LoginOtherUtil.showToast(getContext(),"验证码错误");
//							}
//							else
//							{
//								LoginOtherUtil.showToast(getContext(),"验证失败");
//							}
//
//						}
//						else
//						{
//							LoginOtherUtil.showToast(getContext(),"网络异常");
//						}
                            break;
                        }
                        case REGISTER: {
                            mOkBtn.setNormalState();
                            final RegisterLoginInfo info = (RegisterLoginInfo) msg.obj;
                            if (info != null && info.mCode == 0) {
                                registerSucceed(info);
                            } else {
                                registerFailed(info);
                                if (info != null) {
                                    if (info.mCode == 10003) {
                                        LoginOtherUtil.showToast(getContext(),R.string.toast_phone_rule_error);
                                    } else if (info.mCode == 10004) {
                                        LoginOtherUtil.showToast(getContext(),R.string.toast_code_error);
                                        mCodeInput.setText("");
                                        checkOkBtnEnable();
                                        showKeyboard(mCodeInput);
                                    } else if (info.mCode == 10009) {
                                        LoginOtherUtil.showToast(getContext(),R.string.toast_phone_registered);
                                    } else {
                                        if (info != null && info.mMsg != null) {
                                            LoginOtherUtil.showToast(getContext(),info.mMsg);
                                        } else {
                                            LoginOtherUtil.showToast(getContext(),R.string.toast_register_failure);
                                        }
                                    }
                                }
                            }
                            break;
                        }

                        case LOGIN_SUCCEED: {
                            LoginInfo info = (LoginInfo) msg.obj;
                            mOkBtn.setNormalState();
                            LoginOtherUtil.setSettingInfo(getContext(),info);
                            LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_login_success));
                            EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
                            WatermarkSyncManager.getInstacne(getContext()).startUpSyncDependOnSituation();
                            loginSucceed(info);
                            break;
                        }
                        case LOGIN_FAIL: {
                            mOkBtn.setNormalState();
                            LoginInfo info = (LoginInfo) msg.obj;
                            loginFailed(info);
                            if (info != null) {
                                if (info.mCode == 10001) {
                                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_enter_mobile_number));
                                } else if (info.mCode == 10002) {
                                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_enter_password));
                                } else if (info.mCode == 10003) {
                                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_user_not_exist));
                                } else if (info.mCode == 10004) {
                                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_account_forbid));
                                } else if (info.mCode == 10005) {
                                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_password_error));
                                } else {
                                    if (info.mMsg != null) {
                                        LoginOtherUtil.showToast(getContext(),info.mMsg);
                                    } else {
                                        LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_login_failure));
                                    }
                                }
                            } else {
                                LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_login_failure));
                            }
                            break;
                        }
                        case FILL_REGISTER_INFO:
                            mOkBtn.setNormalState();
                            LoginPageInfo info = (LoginPageInfo) msg.obj;
                            fillRegisterInfo(info);
                            break;
                        default:
                            super.dispatchMessage(msg);
                            break;
                    }
                }
            }
        };
    }

    private void initUi() {

        this.setBackgroundColor(bkColor);

        mTopHeight = PxToDpi_xhdpi(300);
        LayoutParams fl;
        LinearLayout.LayoutParams llParams;
        RelativeLayout.LayoutParams rlParams;

        mTopFr = new FrameLayout(getContext());
        mTopFr.setBackgroundColor(0x40000000);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTopHeight);
        addView(mTopFr, fl);
        {
            // 返回到上一层按钮
            mCancelBtn = new ImageView(getContext());
            mCancelBtn.setImageResource(R.drawable.framework_back_btn);
            fl = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mCancelBtn.setOnClickListener(mOnClickListener);
            mCancelBtn.setOnTouchListener(mOnTouchListener);
            mCancelBtn.setLayoutParams(fl);
            mTopFr.addView(mCancelBtn);

            mLogo = new ImageView(getContext());
            if (LoginOtherUtil.isChineseLanguage(getContext())) {
                mLogo.setImageResource(R.drawable.login_pass_logo);
            } else {
                mLogo.setImageResource(R.drawable.login_pass_logo_en);
            }
            fl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER;
            mLogo.setLayoutParams(fl);
            mTopFr.addView(mLogo);
        }

        fl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        fl.leftMargin = PxToDpi_xhdpi(50);
        fl.rightMargin = PxToDpi_xhdpi(50);
        fl.topMargin = mTopHeight;
        fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        mCenterLl = new LinearLayout(getContext());
        mCenterLl.setGravity(Gravity.CENTER_HORIZONTAL);
        mCenterLl.setOrientation(LinearLayout.VERTICAL);
        this.addView(mCenterLl, fl);

        //手机号码
        llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(100));
        mPhoneParent = new RelativeLayout(getContext());
        mCenterLl.addView(mPhoneParent, llParams);
        {
            mPhoneAreaRl = new RelativeLayout(getContext());
            mPhoneAreaRl.setId(R.id.login_phone_area_rl);
            mPhoneAreaRl.setOnClickListener(mOnClickListener);
            mPhoneAreaRl.setOnTouchListener(mOnTouchListener);
            rlParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            mPhoneParent.addView(mPhoneAreaRl, rlParams);
            {
                rlParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                rlParams.addRule(RelativeLayout.CENTER_VERTICAL);
                rlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rlParams.leftMargin = PxToDpi_xhdpi(20);
                mPhoneAreaLogo = new ImageView(getContext());
                mPhoneAreaLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                mPhoneAreaLogo.setImageResource(R.drawable.login_del_logo);
                mPhoneAreaLogo.setId(R.id.login_phone_area_rl_icon);
                mPhoneAreaRl.addView(mPhoneAreaLogo, rlParams);

                rlParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                rlParams.addRule(RelativeLayout.RIGHT_OF, R.id.login_phone_area_rl_icon);
                rlParams.leftMargin = PxToDpi_xhdpi(10);
                rlParams.addRule(RelativeLayout.CENTER_VERTICAL);
                mPhoneAreaCode = new TextView(getContext());
                mPhoneAreaCode.setText("+" + mAreaCodeNum);
                mPhoneAreaCode.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
                mPhoneAreaCode.setTextColor(Color.WHITE);
                mPhoneAreaCode.setId(R.id.login_phone_area_rl_num);
                mPhoneAreaRl.addView(mPhoneAreaCode, rlParams);
            }
            rlParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            rlParams.leftMargin = PxToDpi_xhdpi(50);
            rlParams.addRule(RelativeLayout.RIGHT_OF, R.id.login_phone_area_rl);
            mPhoneInput = new EditTextWithDel(getContext(), -1, R.drawable.login_delete_logo);
            mPhoneInput.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            mPhoneInput.setBackgroundColor(0x00000000);
            mPhoneInput.setPadding(0, 0, PxToDpi_xhdpi(5), 0);
            mPhoneInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
            mPhoneInput.setTextColor(Color.WHITE);
            mPhoneInput.setHintTextColor(lineColor);
            mPhoneInput.setHint(getResources().getString(R.string.login_mobile_number));
            mPhoneInput.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            mPhoneInput.setSingleLine();
            mPhoneInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            mPhoneParent.addView(mPhoneInput, rlParams);
            mPhoneInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    checkOkBtnEnable();
                }
            });
        }
        mPhoneLine = new View(getContext());
        mPhoneLine.setBackgroundColor(lineColor);
        llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        mCenterLl.addView(mPhoneLine, llParams);

        //输入验证码
        llParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(100));
        mCodeParent = new RelativeLayout(getContext());
        mCenterLl.addView(mCodeParent, llParams);
        {
            rlParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            rlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rlParams.addRule(RelativeLayout.CENTER_VERTICAL);
            rlParams.leftMargin = PxToDpi_xhdpi(20);
            mCodeLogo = new ImageView(getContext());
            mCodeLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mCodeLogo.setImageResource(R.drawable.login_verificationcode);
            mCodeLogo.setId(R.id.login_code_rl_icon);
            mCodeParent.addView(mCodeLogo, rlParams);

            rlParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            rlParams.addRule(RelativeLayout.RIGHT_OF, R.id.login_code_rl_icon);
            rlParams.leftMargin = PxToDpi_xhdpi(110);
            mCodeInput = new EditTextWithDel(getContext(), -1, -1);
            mCodeInput.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            mCodeInput.setBackgroundColor(0x00000000);
            mCodeInput.setPadding(0, 0, PxToDpi_xhdpi(5), 0);
            mCodeInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
            mCodeInput.setTextColor(Color.WHITE);
            mCodeInput.setHintTextColor(lineColor);
            mCodeInput.setHint(getResources().getString(R.string.login_verification_code));
            mCodeInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
            mCodeInput.setSingleLine();
            mCodeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            mCodeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
            mCodeInput.setTypeface(Typeface.MONOSPACE, 0);
            mCodeInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    checkOkBtnEnable();
                }
            });

            mCodeParent.addView(mCodeInput, rlParams);
            rlParams = new RelativeLayout.LayoutParams(1, PxToDpi_xhdpi(70));
            rlParams.addRule(RelativeLayout.LEFT_OF, R.id.login_code_rl_tip);
            rlParams.addRule(RelativeLayout.CENTER_VERTICAL);
            View line = new View(getContext());
            line.setBackgroundColor(lineColor);
            mCodeParent.addView(line, rlParams);

            rlParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rlParams.addRule(RelativeLayout.CENTER_VERTICAL);
            mCodeTip = new TextView(getContext());
            mCodeTip.setMinWidth(PxToDpi_xhdpi(180));
            mCodeTip.setGravity(Gravity.CENTER);
            mCodeTip.setTextColor(0xffFFC433);
            mCodeTip.getPaint().setFakeBoldText(true);
            mCodeTip.setText(getResources().getString(R.string.login_get_code));
            mCodeTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
            mCodeTip.setId(R.id.login_code_rl_tip);
            mCodeTip.setOnClickListener(mOnClickListener);
            mCodeTip.setOnTouchListener(mOnTouchListener);
            mCodeParent.addView(mCodeTip, rlParams);
        }
        mCodeLine = new View(getContext());
        mCodeLine.setBackgroundColor(lineColor);
        llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        mCenterLl.addView(mCodeLine, llParams);

        //输入密码框
        llParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, PxToDpi_xhdpi(100));
        mPwdItem = new PwdItem(getContext());
        mPwdInput = mPwdItem.getEditText();
        mCenterLl.addView(mPwdItem, llParams);
        mPwdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkOkBtnEnable();
            }
        });

        mPwdLine = new View(getContext());
        mPwdLine.setBackgroundColor(lineColor);
        llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        mCenterLl.addView(mPwdLine, llParams);

        llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llParams.gravity = Gravity.CENTER_HORIZONTAL;
        llParams.topMargin = PxToDpi_xhdpi(70);

        mOkBtn = new CommonBtn(getContext());
        llParams = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(608), ShareData.PxToDpi_xhdpi(96));
        llParams.topMargin = ShareData.PxToDpi_xhdpi(70);
        mOkBtn.setBackgroundResource(R.drawable.login_comfir_btn_bg);
        mOkBtn.setEnabled(false);
        mOkBtn.setOnClickListener(mOnClickListener);
        mCenterLl.addView(mOkBtn, llParams);

    }

    /**
     * @param params phoneNum:手机号码
     *               img:页面背景图片
     */
    @Override
    public void SetData(HashMap<String, Object> params) {
        if (params != null) {
            if (params.get(KEY_INFO) != null) {
                LoginPageInfo info = (LoginPageInfo) params.get(KEY_INFO);
                mPhoneInput.setText(info.m_phoneNum);
                mPhoneAreaCode.setText("+" + info.m_areaCodeNum);
            }
            if (params.get("img") != null) {
                SetBackground(Utils.DecodeFile((String) params.get("img"), null));
            }
        }
    }

    protected void SetBackground(Bitmap bk) {
        if (bk != null) {
            this.setBackgroundDrawable(new BitmapDrawable(bk));
        } else {
            //黑色 不透明度75%
            this.setBackgroundColor(bkColor);
        }
    }


    protected CountDownTimer mTimer = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            isTimerDone = false;
            mCodeTip.setText(millisUntilFinished / 1000 + "s");
        }

        @Override
        public void onFinish() {
            isTimerDone = true;
            mCodeTip.setEnabled(true);
            mCodeTip.setTextColor(0xffFFC433);
            mCodeTip.setText(R.string.login_get_code_again);
        }
    };


    protected void checkOkBtnEnable() {
        if (mOkBtn != null && mPwdInput != null && mPhoneInput != null && mCodeInput != null) {
            if (mPhoneInput.toString().length() >= MAX_MOBILE_NUM && mPwdInput.getText().toString().length() >= MAX_PWD_NUM && (mCodeParent.getVisibility() == View.VISIBLE ? mCodeInput.getText().toString().length() == MAX_CODE_NUM : true)) {
                mOkBtn.setEnabled(true);
            } else {
                mOkBtn.setEnabled(false);
            }
        }
    }

    protected View.OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onViewClick(v);
        }
    };

    protected View.OnTouchListener mOnTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return onViewTouch(v, event);
        }
    };

    abstract void onViewClick(View v);

    abstract boolean onViewTouch(View v, MotionEvent event);


    protected void resetPwdFailed(LoginInfo info) {

    }


    protected void registerSucceed(RegisterLoginInfo info) {

    }

    protected void registerFailed(RegisterLoginInfo info) {

    }

    protected void loginSucceed(LoginInfo info) {

    }

    protected void loginFailed(LoginInfo info) {

    }

    protected void fillRegisterInfo(LoginPageInfo info) {

    }

    protected void getCodeFinish() {

    }

    @Override
    public void onPageResult(int siteID, HashMap<String, Object> params) {
        if (siteID == SiteID.CHOOSE_COUNTRY) {
            if (params != null) {
                if (params.get(KEY_INFO) != null) {
                    final LoginPageInfo info = (LoginPageInfo) params.get(KEY_INFO);
                    if (info != null) {
                        mCountry = info.m_country;
                        mAreaCodeNum = info.m_areaCodeNum;
                        mPhoneAreaCode.setText("+" + mAreaCodeNum);
                    }
                }
            }
        }
    }

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
    }

    protected void showKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public void onBack() {
        isClose = true;
    }

    @Override
    public void onClose() {
        super.onClose();
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = null;
        mThread.quit();
        mThreadHandler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
    }
}
