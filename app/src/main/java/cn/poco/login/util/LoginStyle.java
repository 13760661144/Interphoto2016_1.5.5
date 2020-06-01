//package cn.poco.login.util;
//
//import android.content.Context;
//import android.os.Handler;
//import android.view.View;
//import android.widget.Toast;
//
//import cn.poco.blogcore.SinaBlog;
//import cn.poco.blogcore.SinaBlog.BindSinaCallback;
//import cn.poco.blogcore.WeiXinBlog;
//import cn.poco.blogcore.WeiboInfo;
//import cn.poco.framework.IPage;
//import cn.poco.interphoto2.PocoCamera;
//import cn.poco.login.userinfo.TipsDialog;
//import cn.poco.loginlibs.LoginUtils;
//import cn.poco.loginlibs.info.LoginInfo;
//import cn.poco.loginlibs.info.TPLoginInfo;
//import cn.poco.system.AppInterface;
//
//public class LoginStyle {
//
//    private SinaBlog mSina;
//    private WeiXinBlog mWeiXin;
//    private Context mContext;
//    private IPage mPage;
//    private onLoginListener mOnloginLisener;
//    private Handler mHandler;
//    private boolean isClose = false;
//
//    LoginInfo mLoginInfo = new LoginInfo();
//
//    public LoginStyle(Context context, IPage page) {
//        this.mContext = context;
//        this.mPage = page;
//        mHandler = new Handler();
//        isClose = false;
//    }
//
//
//    public void setLoginLisener(onLoginListener lisener) {
//        if(mOnloginLisener != null)
//        {
//            mOnloginLisener = null;
//        }
//        this.mOnloginLisener = lisener;
//    }
//
//    public void bindSina() {
//        if (mSina == null) {
//            mSina = new SinaBlog(mContext);
//        }
//
//        mSina.bindSinaWithSSO(new BindSinaCallback() {
//            @Override
//            public void success(final String accessToken, final String expiresIn, final String uid, String userName,
//                                String nickName) {
//                LoginOtherUtil.showProgressDialog("登录中...");
//
//                new Thread(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        final TPLoginInfo info = LoginUtils.TPLogin(uid, accessToken, null, (int)Long.parseLong(expiresIn), LoginUtils.Partner.sina, AppInterface.GetInstance(PocoCamera.main));
//                        mHandler.post(new Runnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                if(!isClose)
//                                {
//                                    if(info != null)
//                                    {
//                                        if (info.mCode == 0) {
////                                            Credit.CreditIncome(mContext,mContext.getResources().getInteger(R.integer.积分_第三方登录)+"");
//                                            LoginOtherUtil.setSettingInfo(info);
//                                            //保存用户信息
//                                            LoginOtherUtil.saveUserInfoToLocal(info, new LoginOtherUtil.RunOnEnd() {
//                                                @Override
//                                                public void runOnfinally() {
//                                                    if(mOnloginLisener != null)
//                                                    {
//                                                        mOnloginLisener.onLoginSuccess(info.mUserId);
//                                                    }
//                                                }
//
//                                                @Override
//                                                public void loginFail() {
//                                                    saveUserInfoError();
//                                                }
//                                            });
//                                        }else if(info.mCode == 10001)
//                                        {
//                                            LoginOtherUtil.showToast("参数错误!");
//                                        }else if(info.mCode == 10003)
//                                        {
//                                            LoginOtherUtil.showToast("第三方授权失败!");
//                                        }
//                                        else if(info.mCode == 10004)
//                                        {
//                                            LoginOtherUtil.showToast("账号被禁!");
//                                        }else
//                                        {
//                                            LoginOtherUtil.showToast("绑定微博失败!");
//                                        }
//                                        LoginOtherUtil.dismissProgressDialog();
//                                    }
//                                    else {
//                                        LoginOtherUtil.showToast("网络异常!");
//                                        LoginOtherUtil.dismissProgressDialog();
//                                    }
//                                }
//                            }
//                        });
//                    }
//                }).start();
//            }
//
//            @Override
//            public void fail() {
//                switch (mSina.LAST_ERROR) {
//                    case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
//                        Toast.makeText(PocoCamera.main.getApplicationContext(), "还没有安装最新的新浪微博客户端，需要安装后才能绑定", Toast.LENGTH_SHORT).show();
//                        break;
//
//                    default:
//                        Toast.makeText(PocoCamera.main.getApplicationContext(), "绑定新浪微博失败", Toast.LENGTH_SHORT).show();
//                        break;
//                }
////				LoginOtherUtil.showToastVeritical(PocoCamera.main.getApplicationContext(), "登录失败！");
//            }
//        });
//    }
//
//    public void WeiXinLogin() {
//        if (mWeiXin == null)
//            mWeiXin = new WeiXinBlog(mContext);
//        if (mWeiXin.registerWeiXin())
//            mWeiXin.getCode();
//        else {
//            switch (mWeiXin.LAST_ERROR) {
//                case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
//                    Toast.makeText(PocoCamera.main.getApplicationContext(), "还没有安装微信客户端！", Toast.LENGTH_SHORT).show();
//                    break;
//                case WeiboInfo.BLOG_INFO_CLIENT_VERSION_LOW:
//                    Toast.makeText(PocoCamera.main.getApplicationContext(), "微信客户端版本过低，请升级你的微信客户端版本！", Toast.LENGTH_SHORT).show();
//                    break;
//                default:
//                    Toast.makeText(PocoCamera.main.getApplicationContext(), "微信绑定失败!", Toast.LENGTH_SHORT).show();
//                    break;
//            }
//        }
//
//    }
//
//    public void WeiXinLogin2(final String code) {
//        if (mWeiXin == null)
//            return;
//        LoginOtherUtil.showProgressDialog("绑定中...");
//
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mWeiXin.setCode(code);
//                if (mWeiXin.getAccessTokenAndOpenid()) {
//                    if (mWeiXin.getUserUnionid()) {
//                        final TPLoginInfo info = LoginUtils.weChatLogin(mWeiXin.getOpenid(), mWeiXin.getAccessToken(), mWeiXin.getRefreshToken(), (int)Long.parseLong(mWeiXin.getExpiresin()), mWeiXin.getUnionid(), AppInterface.GetInstance(PocoCamera.main));
//                        mHandler.post(new Runnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                if(!isClose)
//                                {
//                                    if (info != null) {
//                                        if (info.mCode == 0) {
////                                            Credit.CreditIncome(mContext,mContext.getResources().getInteger(R.integer.积分_第三方登录)+"");
//                                            LoginOtherUtil.dismissProgressDialog();
//                                            LoginOtherUtil.setSettingInfo(info);
//                                            LoginOtherUtil.saveUserInfoToLocal(info, new LoginOtherUtil.RunOnEnd() {
//                                                @Override
//                                                public void runOnfinally() {
//                                                    if(mOnloginLisener != null)
//                                                    {
//                                                        mOnloginLisener.onLoginSuccess(info.mUserId);
//                                                    }
//                                                }
//
//                                                @Override
//                                                public void loginFail() {
//                                                    saveUserInfoError();
//                                                }
//                                            });
//                                        }else if(info.mCode == 10001)
//                                        {
//                                            LoginOtherUtil.showToast("参数错误!");
//                                        }else if(info.mCode == 10003)
//                                        {
//                                            LoginOtherUtil.showToast("第三方授权失败!");
//                                        }
//                                        else if(info.mCode == 10004)
//                                        {
//                                            LoginOtherUtil.showToast("账号被禁!");
//                                        }
//                                        else {
//                                            WeiXinLoginFail();
//                                        }
//                                        LoginOtherUtil.dismissProgressDialog();
//                                    } else {
//                                        WeiXinLoginFail();
//                                    }
//                                }
//                            }
//                        });
//                    } else {
//                        WeiXinLoginFail();
//                    }
//                } else {
//                    WeiXinLoginFail();
//                }
//            }
//        }).start();
//    }
//
//
//    public void WeiXinLoginFail() {
//        ((View) mPage).post(new Runnable() {
//            @Override
//            public void run() {
//               LoginOtherUtil.dismissProgressDialog();
//                mWeiXin.backToSendStatus();
//              LoginOtherUtil.showToast("微信绑定失败!");
//            }
//        });
//    }
//
//    private void saveUserInfoError()
//    {
//        LoginOtherUtil.showToast("登陆失败!");
//        if(mOnloginLisener != null)
//        {
//            mOnloginLisener.onLoginFailed();
//        }
//        UserMgr.ExitLogin(mContext);
//    }
//
//    public SinaBlog getSinaBlog() {
//        return mSina;
//    }
//
//    private TipsDialog.Listener listener = new TipsDialog.Listener() {
//
//        @Override
//        public void ok() {
//        }
//
//        @Override
//        public void cancel() {
//        }
//    };
//
//    public void LoginByPhone(final String zoneNum, final String phone, final String psw) {
//        LoginOtherUtil.dismissProgressDialog();
//        if (mOnloginLisener != null) {
//            mOnloginLisener.onActionLogin();
//        }
//
//        new Thread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                final LoginInfo info = LoginUtils.userLogin(zoneNum, phone, psw, AppInterface.GetInstance(PocoCamera.main));
//                mHandler.post(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        if(!isClose)
//                        {
//                            if (info != null) {
//                                if (info.mCode == 0) {
//                                    LoginOtherUtil.setSettingInfo(info);
//                                    LoginOtherUtil.saveUserInfoToLocal(info, new LoginOtherUtil.RunOnEnd() {
//                                        @Override
//                                        public void runOnfinally() {
//                                            if (mOnloginLisener != null) {
//                                                mOnloginLisener.onLoginSuccess(info.mUserId);
//                                            }
////                                            Credit.CreditIncome(mContext,mContext.getResources().getInteger(R.integer.积分_每天使用)+"");
//                                        }
//
//                                        @Override
//                                        public void loginFail() {
//                                            saveUserInfoError();
//                                        }
//                                    });
//                                    mLoginInfo = info;
//                                } else if (info.mCode == 10001) {
//                                    showDailog("请输入手机号");
//                                } else if (info.mCode == 10002) {
//                                    showDailog("请输入密码");
//                                } else if (info.mCode == 10003) {
//                                    showDailog("用户不存在");
//                                } else if (info.mCode == 10004) {
//                                    showDailog("你的账号已被禁用");
//                                } else if (info.mCode == 10005) {
//                                    showDailog("密码错误");
//                                } else {
//                                    LoginOtherUtil.showToast("登录失败!");
//                                }
//                                if (info.mCode != 0) {
//                                    if (mOnloginLisener != null) {
//                                        mOnloginLisener.onLoginFailed();
//                                    }
//                                }
//                            }
//                            else
//                            {
//                                if(mOnloginLisener != null)
//                                {
//                                    mOnloginLisener.onLoginFailed();
//                                    LoginOtherUtil.showToast("网络异常,登录失败!");
//                                }
//                            }
//                        }
//                    }
//                });
//            }
//        }).start();
//    }
//
//    public void showDailog(String content)
//    {
//        if(!isClose)
//        {
//            TipsDialog tips = new TipsDialog(mContext, null, content, null, "确定", listener);
//            tips.showDialog();
//        }
//    }
//
//    public void close()
//    {
//        isClose = true;
//    }
//
//
//
//    public interface onLoginListener
//    {
//        public void onLoginSuccess(String id);
//
//        public void onLoginFailed();
//
//        public void onCancel();
//
//        public void onActionLogin();
//    }
//}
