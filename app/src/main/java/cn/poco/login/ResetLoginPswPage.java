package cn.poco.login;

import android.content.Context;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.regex.Pattern;

import cn.poco.framework.BaseSite;
import cn.poco.interphoto2.R;
import cn.poco.login.site.ResetLoginPswPageSite;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.login.widget.PwdItem;
import cn.poco.loginlibs.LoginUtils;
import cn.poco.loginlibs.info.LoginInfo;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJiUtils;

public class ResetLoginPswPage extends BaseLoginPage {
    private static final String TAG = "重置密码";
    private ResetLoginPswPageSite mSite;
    private boolean mUiEnabled = true;

    public ResetLoginPswPage(Context context, BaseSite site) {
        super(context, site);
        mSite = (ResetLoginPswPageSite) site;
        initUI();
        TongJiUtils.onPageStart(getContext(), TAG);
        MyBeautyStat.onPageStartByRes(R.string.进入忘记密码);
    }

    public void initUI() {
        mVerifyType = LoginUtils.VerifyCodeType.find;


//        mCodeInput.setOnFocusChangeListener(new OnFocusChangeListener()
//        {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus)
//            {
//                if(hasFocus){
//                    MyBeautyStat.onClickByRes(R.string.进入忘记密码_验证码填写);
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
//                    MyBeautyStat.onClickByRes(R.string.进入忘记密码_手机号码填写);
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
//                    MyBeautyStat.onClickByRes(R.string.进入忘记密码_密码填写);
//                }
//            }
//        });

        mPwdItem.setPwdTypeChange(new PwdItem.PwdTypeChange()
        {
            @Override
            public void onPwdHide()
            {
                MyBeautyStat.onClickByRes(R.string.进入忘记密码_密码加密);
            }

            @Override
            public void onPwdShow()
            {
                MyBeautyStat.onClickByRes(R.string.进入忘记密码_密码明文);
            }
        });
    }


    @Override
    void onViewClick(View v) {
        if (v == mCancelBtn) {
            hideKeyboard();
           onBack();
        }
        if (mUiEnabled) {
            if (v == mPhoneAreaRl) {
                MyBeautyStat.onClickByRes(R.string.进入忘记密码_修改号码地区);
                hideKeyboard();
                mSite.chooseCountry(getContext());
            } else if (v == mOkBtn) {
                MyBeautyStat.onClickByRes(R.string.进入忘记密码_点击确定);
                hideKeyboard();
                String phone = mPhoneInput.getText().toString();
                phone = phone.trim();
                phone.replace(" ", "");
                if (phone.length() == 0) {
                    LoginOtherUtil.showToast(getContext(),R.string.toast_enter_mobile_number);
                    return;
                }
                String vcode = mCodeInput.getText().toString();
                if (vcode.length() == 0) {
                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_enter_code));
                    return;
                }
                String pws = mPwdInput.getText().toString();

                if (pws.length() < 8 || pws.length() > 20 || Pattern.compile("[\u4e00-\u9fa5]").matcher(pws).find()) {
                    LoginOtherUtil.showToast(getContext(),getResources().getString(R.string.toast_password_rule_error));
                    return;
                }
                mUiEnabled = false;
                mOkBtn.setLoadingState();
                Message msg = mThreadHandler.obtainMessage();
                msg.what = CHANGE_PASSWORD;
                mThreadHandler.sendMessage(msg);
            } else if (v == mCodeTip) {
                hideKeyboard();
                if (isTimerDone) {
                    MyBeautyStat.onClickByRes(R.string.进入忘记密码_点击获取验证码);
                    String phone = mPhoneInput.getText().toString();
                    phone = phone.trim();
                    phone.replace(" ", "");
                    if (phone.length() == 0) {
                        LoginOtherUtil.showToast(getContext(),R.string.toast_enter_mobile_number);
                        return;
                    }
                    mCodeTip.setTextColor(0xff666666);
                    mCodeTip.setText(getResources().getString(R.string.getting));
                    isTimerDone = false;
                    mCodeTip.setFocusable(false);
                    Message msg = mThreadHandler.obtainMessage();
                    msg.what = GET_VERIFY_CODE;
                    mThreadHandler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    protected void fillRegisterInfo(LoginPageInfo info) {
        super.fillRegisterInfo(info);
        mUiEnabled = true;
        info.m_country = mCountry;
        info.m_areaCodeNum = mAreaCodeNum;
        info.m_phoneNum = mPhoneInput.getText().toString();
        HashMap<String, Object> data = new HashMap<>();
        data.put(KEY_INFO, info);
        mSite.fillRegisterInfo(getContext(),data);
    }

    @Override
    protected void resetPwdFailed(LoginInfo info) {
        super.resetPwdFailed(info);
        mUiEnabled = true;
    }

    @Override
    protected void loginSucceed(LoginInfo info) {
        super.loginSucceed(info);
        mUiEnabled = true;
        mSite.loginSucceed(getContext());
    }

    @Override
    protected void loginFailed(LoginInfo info) {
        super.loginFailed(info);
        mUiEnabled = true;
        LoginPageInfo pageInfo = new LoginPageInfo();
        pageInfo.m_info = info;
        pageInfo.m_areaCodeNum = mAreaCodeNum;
        pageInfo.m_phoneNum = mPhoneInput.getText().toString();
        HashMap<String, Object> data = new HashMap<>();
        data.put(KEY_INFO, pageInfo);
        mSite.onBack(getContext(),data);
    }

    @Override
    protected void getCodeFinish() {
        mUiEnabled = true;
    }

    @Override
    boolean onViewTouch(View v, MotionEvent event) {

        return false;
    }


    @Override
    public void onBack() {
        super.onBack();
        mSite.onBack(getContext());
        MyBeautyStat.onClickByRes(R.string.进入忘记密码_退出忘记密码);
    }

    @Override
    public void onClose() {
        super.onClose();
        TongJiUtils.onPageEnd(getContext(), TAG);
        MyBeautyStat.onPageEndByRes(R.string.进入忘记密码);
    }

    @Override
    public void onResume() {
        TongJiUtils.onPageResume(getContext(), TAG);
        super.onResume();
    }

    @Override
    public void onPause() {
        TongJiUtils.onPagePause(getContext(), TAG);
        super.onPause();
    }
}
