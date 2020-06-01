package cn.poco.login;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.regex.Pattern;

import cn.poco.framework.BaseSite;
import cn.poco.interphoto2.R;
import cn.poco.login.site.RegisterLoginPageSite;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.login.widget.PwdItem;
import cn.poco.loginlibs.info.RegisterLoginInfo;
import cn.poco.setting.LanguagePage;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.CommonUtils;

import static cn.poco.tianutils.ShareData.PxToDpi_xhdpi;

public class RegisterLoginPage extends BaseLoginPage {
    private static final String TAG = "注册填验证码";
    private RegisterLoginPageSite mSite;
    private TextView mUserAgress;
    //注册协议
    public static final String AGREE_URL = "http://www.adnonstop.com/interphoto/wap/user_agreement.php";
    public static final String AGREE_URL_EN = "http://www.adnonstop.com/interphoto/wap/user_english_agreement.php";
    private TextView mTipstext;
    private LinearLayout mTipsLinear;

    private final static int GET_VERIFY_CODE = 1;
    private final static int REGISTER = 4;

    private boolean mUiEnabled = true;

    public RegisterLoginPage(Context context, BaseSite site) {
        super(context, site);
        mSite = (RegisterLoginPageSite) site;
        TongJiUtils.onPageStart(getContext(), TAG);
        MyBeautyStat.onPageStartByRes(R.string.注册账号页);
        initUI();
    }

    public void initUI() {

        mOkBtn.setText(R.string.login_next);

        LinearLayout.LayoutParams ll;
        LayoutParams fl;

        ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ll.gravity = Gravity.CENTER;
        ll.topMargin = PxToDpi_xhdpi(40);
        mUserAgress = new TextView(getContext());
        String str = getResources().getString(R.string.login_beauty_terms);
        SpannableString span = new SpannableString(str);
        int index = 0;
        if (LoginOtherUtil.isChineseLanguage(getContext())) {
            index = 7;
        } else {
            index = 24;
        }
        if (index < span.length()) {
            span.setSpan(new ForegroundColorSpan(0xffcccccc), 0, index, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(0xffFFC433), index, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new AbsoluteSizeSpan(10, true), 0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new StyleSpan(Typeface.BOLD), index, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mUserAgress.setText(span);
        } else {
            mUserAgress.setText(str);
        }
        mUserAgress.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
        mUserAgress.setPadding(PxToDpi_xhdpi(5), PxToDpi_xhdpi(5), PxToDpi_xhdpi(5), PxToDpi_xhdpi(5));
        mUserAgress.setOnClickListener(mOnClickListener);
        mCenterLl.addView(mUserAgress, ll);

        mTipstext = new TextView(getContext());
        mTipstext.setText(R.string.login_beauty_account);
        mTipstext.setSingleLine();
        mTipstext.setEllipsize(TextUtils.TruncateAt.END);
        mTipstext.setTextColor(lineColor);
        mTipstext.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
        mTipstext.setVisibility(VISIBLE);
        fl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        fl.bottomMargin = PxToDpi_xhdpi(25+65+52);
        mTipstext.setLayoutParams(fl);
        this.addView(mTipstext);

        mTipsLinear = new LinearLayout(getContext());
        mTipsLinear.setId(R.id.login_logo_icons_ll);
        fl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        fl.bottomMargin = PxToDpi_xhdpi(65);
        mTipsLinear.setVisibility(VISIBLE);
        mTipsLinear.setLayoutParams(fl);
        this.addView(mTipsLinear);
        {
            ImageView beauty = new ImageView(getContext());
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            beauty.setImageResource(R.drawable.login_icon_meiren);
            mTipsLinear.addView(beauty, ll);

            ImageView jianping = new ImageView(getContext());
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.leftMargin = PxToDpi_xhdpi(20);
            jianping.setImageResource(R.drawable.login_icon_jianping);
            mTipsLinear.addView(jianping, ll);

            ImageView jianke = new ImageView(getContext());
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.leftMargin = PxToDpi_xhdpi(20);
            jianke.setImageResource(R.drawable.login_icon_jianke);
            mTipsLinear.addView(jianke, ll);

            ImageView zaiyiqi = new ImageView(getContext());
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.leftMargin = PxToDpi_xhdpi(20);
            zaiyiqi.setImageResource(R.drawable.login_icon_zaiyiqi);
            mTipsLinear.addView(zaiyiqi, ll);

            ImageView poco = new ImageView(getContext());
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.leftMargin = PxToDpi_xhdpi(20);
            poco.setImageResource(R.drawable.login_icon_qinzi);
            mTipsLinear.addView(poco, ll);

            ImageView hechengqi = new ImageView(getContext());
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.leftMargin = PxToDpi_xhdpi(20);
            hechengqi.setImageResource(R.drawable.login_icon_hechengqi);
            mTipsLinear.addView(hechengqi, ll);
        }
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });


//        mCodeInput.setOnFocusChangeListener(new OnFocusChangeListener()
//        {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus)
//            {
//                if(hasFocus){
//                    MyBeautyStat.onClickByRes(R.string.注册账号页_验证码填写);
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
//                    MyBeautyStat.onClickByRes(R.string.注册账号页_手机号码填写);
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
//                    MyBeautyStat.onClickByRes(R.string.注册账号页_密码填写);
//                }
//            }
//        });

        mPwdItem.setPwdTypeChange(new PwdItem.PwdTypeChange()
        {
            @Override
            public void onPwdHide()
            {
                MyBeautyStat.onClickByRes(R.string.注册账号页_密码加密);
            }

            @Override
            public void onPwdShow()
            {
                MyBeautyStat.onClickByRes(R.string.注册账号页_密码明文);
            }
        });
    }

    @Override
    protected void registerSucceed(RegisterLoginInfo info) {
        mUiEnabled = true;
        LoginPageInfo pageInfo = new LoginPageInfo();
        pageInfo.m_info = info;
        pageInfo.m_areaCodeNum = mAreaCodeNum;
        pageInfo.m_phoneNum = mPhoneInput.getText().toString();
        HashMap<String,Object> data = new HashMap<>();
        data.put(KEY_INFO,pageInfo);
        mSite.fillRegisterInfo(getContext(),data);
    }

    @Override
    protected void registerFailed(RegisterLoginInfo info) {
        super.registerFailed(info);
        mUiEnabled = true;
    }

    @Override
    protected void getCodeFinish() {
        mUiEnabled = true;
    }

    @Override
    void onViewClick(View v) {
        if(v == mCancelBtn)
        {
            hideKeyboard();
            onBack();
        }else if(mUiEnabled) {
            if (v == mPhoneAreaRl) {
                MyBeautyStat.onClickByRes(R.string.注册账号页_修改号码地区);
                hideKeyboard();
                mSite.chooseCountry(getContext());
            } else if (v == mOkBtn) {
                MyBeautyStat.onClickByRes(R.string.注册账号页_点击下一步);
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
                msg.what = REGISTER;
                mThreadHandler.sendMessage(msg);
            } else if (v == mCodeTip) {
                hideKeyboard();
                if (isTimerDone) {
                    MyBeautyStat.onClickByRes(R.string.注册账号页_点击获取验证码);
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
            } else if (v == mUserAgress) {
                MyBeautyStat.onClickByRes(R.string.注册账号页_查看美人协议);
                String url = AGREE_URL;
                if(!TagMgr.CheckTag(getContext(), LanguagePage.ENGLISH_TAGVALUE))
                {
                    url = AGREE_URL_EN;
                }
                CommonUtils.OpenBrowser(getContext(), url);
            }
        }
    }

    @Override
    boolean onViewTouch(View v, MotionEvent event) {
        return false;
    }


    @Override
    public void onBack() {
        super.onBack();
        mSite.onBack(getContext());
        MyBeautyStat.onClickByRes(R.string.注册账号页_退出注册);
    }


    @Override
    public void onClose() {
        super.onClose();
        TongJiUtils.onPageEnd(getContext(), TAG);
        MyBeautyStat.onPageEndByRes(R.string.注册账号页);
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
