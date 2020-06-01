package cn.poco.login;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.regex.Pattern;

import cn.poco.framework.BaseSite;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.login.site.LoginPageSite;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.login.widget.PwdItem;
import cn.poco.loginlibs.info.LoginInfo;
import cn.poco.share.SharePage;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJiUtils;
import cn.poco.tianutils.ShareData;

/**
 * 登录页面
 */
public class LoginPage extends BaseLoginPage {
    private static final String TAG = "登录";
    private boolean isPhoneUnRegister = false;
    private boolean isForgotPwd = false;
    private boolean mUiEnabled = true;
    //忘记密码,创建账号
    private FrameLayout mBottomFr;
    private TextView mLosePswBtn;
    private TextView mCreateAccountBtn;
    private LoginPageSite mSite;

    public LoginPage(Context context, BaseSite site) {
        super(context, site);
        mSite = (LoginPageSite) site;
        TongJiUtils.onPageStart(getContext(), TAG);
        MyBeautyStat.onPageStartByRes(R.string.登录页);
        initData();
        initUi();
    }

    protected void initData() {
        SharePage.initBlogConfig();
    }

    protected void initUi() {
        mOkBtn.setText(R.string.login_log_in);
        mCodeParent.setVisibility(View.GONE);
        mCodeLine.setVisibility(View.GONE);

        LayoutParams fParams;
        LinearLayout.LayoutParams llParams;
        llParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        llParams.topMargin = ShareData.PxToDpi_xhdpi(21);
        mBottomFr = new FrameLayout(getContext());
        mCenterLl.addView(mBottomFr, llParams);
        {
            fParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            fParams.gravity = Gravity.LEFT;
            mCreateAccountBtn = new TextView(getContext());
            mCreateAccountBtn.setPadding(ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(10));
            mCreateAccountBtn.setGravity(Gravity.CENTER);
            mCreateAccountBtn.setTextColor(0xffffc433);
//			mCreateAccountBtn.setTextColor(0xffff4c33);
            mCreateAccountBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            mCreateAccountBtn.setText(R.string.login_sign_up);
            mCreateAccountBtn.setOnClickListener(mOnClickListener);
            mCreateAccountBtn.setOnTouchListener(mOnTouchListener);
            mBottomFr.addView(mCreateAccountBtn, fParams);

            fParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            fParams.gravity = Gravity.RIGHT;
            mLosePswBtn = new TextView(getContext());
            mLosePswBtn.setPadding(ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(10));
            mLosePswBtn.setGravity(Gravity.CENTER);
            mLosePswBtn.setTextColor(0xffcccccc);
            mLosePswBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            mLosePswBtn.setText(getResources().getString(R.string.login_password_forgotten));
            mLosePswBtn.setOnClickListener(mOnClickListener);
            mLosePswBtn.setOnTouchListener(mOnTouchListener);
            mBottomFr.addView(mLosePswBtn, fParams);
        }

//        mPhoneInput.setOnFocusChangeListener(new OnFocusChangeListener()
//        {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus)
//            {
//                if(hasFocus){
//                    MyBeautyStat.onClickByRes(R.string.登录页_手机号码填写);
//                }
//            }
//        });
//
//        mPhoneInput.setOnFocusChangeListener(new OnFocusChangeListener()
//        {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus)
//            {
//                if(hasFocus){
//                    MyBeautyStat.onClickByRes(R.string.登录页_密码填写);
//                }
//            }
//        });

        mPwdItem.setPwdTypeChange(new PwdItem.PwdTypeChange()
        {
            @Override
            public void onPwdHide()
            {
                MyBeautyStat.onClickByRes(R.string.登录页_密码加密);
            }

            @Override
            public void onPwdShow()
            {
                MyBeautyStat.onClickByRes(R.string.登录页_密码明文);
            }
        });
    }

    @Override
    protected void checkOkBtnEnable() {
        super.checkOkBtnEnable();
        isPhoneUnRegister = false;
        isForgotPwd = false;
    }

    @Override
    protected void fillRegisterInfo(LoginPageInfo info) {
        super.fillRegisterInfo(info);
        mUiEnabled = true;
        info.m_country = mCountry;
        info.m_areaCodeNum = mAreaCodeNum;
        info.m_phoneNum = mPhoneInput.getText().toString();
        HashMap<String, Object> params = new HashMap<>();
        params.put(KEY_INFO,info);
        mSite.fillRegisterInfo(getContext(),params);
    }

    @Override
    protected void loginSucceed(LoginInfo info) {
        super.loginSucceed(info);
        mUiEnabled = true;
        mSite.loginSuccess(getContext());
    }

    @Override
    protected void loginFailed(LoginInfo info) {
        super.loginFailed(info);
        mUiEnabled = true;
        if (info != null) {
            if (info.mCode == 10003) {
                isPhoneUnRegister = true;
            } else if (info.mCode == 10005) {
                isForgotPwd = true;
            }
        }
    }

    @Override
    void onViewClick(View v) {
        if (v == mCancelBtn) {
            hideKeyboard();
            onBack();
        }else if(mUiEnabled) {
            if (v == mPhoneAreaRl) {
                MyBeautyStat.onClickByRes(R.string.登录页_修改号码地区);
                hideKeyboard();
                mSite.ChooseCountry(getContext());
            } else if (v == mOkBtn) {
                MyBeautyStat.onClickByRes(R.string.登录页_登录按钮);
                hideKeyboard();
                String phone = mPhoneInput.getText().toString();
                phone = phone.trim();
                phone = phone.replace(" ", "");
                if (phone.length() == 0) {
                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_enter_mobile_number));
                    return;
                }
                String pwd = mPwdInput.getText().toString();
                if (pwd.length() == 0) {
                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_enter_password));
                    return;
                }
                if (pwd.length() < 8 || pwd.length() > 20 || Pattern.compile("[\u4e00-\u9fa5]").matcher(pwd).find()) {
                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_password_rule_error));
                    return;
                }
                if (!LoginOtherUtil.isNetConnected(getContext())) {
                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_network_net_connected));
                    return;
                }
                mUiEnabled = false;
                mOkBtn.setLoadingState();
                Message message = mThreadHandler.obtainMessage();
                message.what = LOGIN;
                mThreadHandler.sendMessage(message);
            } else if (v == mLosePswBtn) {
                MyBeautyStat.onClickByRes(R.string.登录页_进入忘记密码);
                hideKeyboard();
                HashMap<String, Object> params = new HashMap<>();
                LoginPageInfo info = new LoginPageInfo();
                info.m_country = mCountry;
                info.m_areaCodeNum = mAreaCodeNum;
                if (isForgotPwd) {
                    info.m_phoneNum = mPhoneInput.getText().toString();
                }
                params.put(KEY_INFO, info);
                mSite.LosePsw(getContext(),params);
            } else if (v == mCreateAccountBtn) {
                MyBeautyStat.onClickByRes(R.string.登录页_进入注册账号);
                hideKeyboard();
                HashMap<String, Object> params = new HashMap<>();
                LoginPageInfo info = new LoginPageInfo();
                info.m_country = mCountry;
                info.m_areaCodeNum = mAreaCodeNum;
                if (isPhoneUnRegister) {
                    info.m_phoneNum = mPhoneInput.getText().toString();
                }
                params.put(KEY_INFO, info);
                mSite.createAccount(getContext(),params);
            }
        }
    }

    @Override
    boolean onViewTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (v == mCancelBtn) {
                mCancelBtn.setAlpha(0.5f);
            } else if (v == mLosePswBtn) {
                mLosePswBtn.setAlpha(0.5f);
            } else if (v == mCreateAccountBtn) {
                mCreateAccountBtn.setAlpha(0.5f);
            } else if (v == mPhoneAreaRl) {
                mPhoneAreaRl.setAlpha(0.5f);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (v == mCancelBtn) {
                mCancelBtn.setAlpha(1.0f);
            } else if (v == mLosePswBtn) {
                mLosePswBtn.setAlpha(1.0f);
            } else if (v == mCreateAccountBtn) {
                mCreateAccountBtn.setAlpha(1.0f);
            } else if (v == mPhoneAreaRl) {
                mPhoneAreaRl.setAlpha(1.0f);
            }
        }
        return false;
    }


    @Override
    public void onBack() {
        super.onBack();
        mSite.onBack(getContext());
        MyBeautyStat.onClickByRes(R.string.登录页_退出登录页);
    }

    @Override
    public void onResume() {
        hideKeyboard();
        TongJiUtils.onPageResume(getContext(), TAG);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }



    @Override
    public void onClose() {
        this.setBackgroundDrawable(null);
        mHandler.removeCallbacksAndMessages(null);
        TongJiUtils.onPageEnd(getContext(), TAG);
        MyBeautyStat.onPageEndByRes(R.string.登录页);
    }

    @Override
    public void onPause() {
        TongJiUtils.onPagePause(getContext(), TAG);
        super.onPause();
    }

    @Override
    public void onPageResult(int siteID, HashMap<String, Object> params) {
        super.onPageResult(siteID, params);
        if (siteID == SiteID.RESETPSW) {
            if (params != null) {
                if (params.get(KEY_INFO) != null) {
                    final LoginPageInfo info = (LoginPageInfo) params.get(KEY_INFO);
                    if (info != null) {
                        mCountry = info.m_country;
                        mAreaCodeNum = info.m_areaCodeNum;
                        mPhoneAreaCode.setText("+" + mAreaCodeNum);
                        mPhoneInput.setText(info.m_phoneNum);
                    }
                }
            }
        }
    }
}
