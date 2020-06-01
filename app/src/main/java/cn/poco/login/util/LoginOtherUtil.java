package cn.poco.login.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.widget.Toast;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import cn.poco.exception.MyApplication;
import cn.poco.loginlibs.info.LoginInfo;
import cn.poco.loginlibs.info.UserInfo;
import cn.poco.setting.SettingInfo;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.system.AppInterface;

public class LoginOtherUtil {

    /**
     * 正则表达式是否含是在该字符集中
     *
     * @param strCheck 检验的字符串
     * @param regEx    正则表达式
     * @throws PatternSyntaxException
     */
    public static boolean isInGather(String strCheck, String regEx) throws PatternSyntaxException {
        //生成Pattern对象并且编译一个正则表达式regEx
        Pattern p = Pattern.compile(regEx);
        //用Pattern类的matcher()方法生成一个Matcher对象
        return p.matcher(strCheck).matches();
    }


    /**
     * 检查网络状态
     *
     * @param context
     * @return
     */
    public static boolean isNetConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            return info.isAvailable();
        }
        return false;
    }


    public static void setSettingInfo(Context context,LoginInfo info) {
        final Context mContext = context.getApplicationContext();
        SettingInfo settingInfo = SettingInfoMgr.GetSettingInfo(mContext);
        settingInfo.SetPoco2Id(info.mUserId);
        settingInfo.SetPoco2ExpiresIn(info.mExpireTime + "");
        settingInfo.SetPoco2Token(info.mAccessToken);
        settingInfo.SetPoco2RefreshToken(info.mRefreshToken);
        SettingInfoMgr.Save(mContext);
        MyBeautyStat.checkLogin(MyApplication.getInstance());//统计
    }
//
//    public static void showToast(String text) {
//        showToast(text, false);
//    }
//
//    public static void showToast(String text, boolean isInCenter) {
//        if (text != null && text.length() > 0) {
//            Toast toast = Toast.makeText(PocoCamera.main, text, Toast.LENGTH_SHORT);
//            if (isInCenter) {
//                toast.setGravity(Gravity.CLIP_VERTICAL, 0, 0);
//            }
//            toast.show();
//        }
//    }
//
//    public static void showToast(int id, boolean isInCenter) {
//        String text = PocoCamera.main.getResources().getString(id);
//        showToast(text, isInCenter);
//    }
//
//    public static void showToast(int id) {
//        String text = PocoCamera.main.getResources().getString(id);
//        showToast(text, false);
//    }

    public static void showToast(Context context, String text) {
        showToast(context, text, false);
    }

    public static void showToast(Context context, String text, boolean isInCenter) {
        if (text != null && text.length() > 0) {
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            if (isInCenter) {
                toast.setGravity(Gravity.CLIP_VERTICAL, 0, 0);
            }
            toast.show();
        }
    }

    public static void showToast(Context context, int id, boolean isInCenter) {
        String text = context.getResources().getString(id);
        showToast(context, text, isInCenter);
    }

    public static void showToast(Context context, int id) {
        String text = context.getResources().getString(id);
        showToast(context, text, false);
    }

//    public static void LoginByPhone(final String zoneNum, final String phone, final String psw, final LoginCallBack callBack) {
//        final Handler handler = new Handler();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final LoginInfo info = cn.poco.loginlibs.LoginUtils.userLogin(zoneNum, phone, psw, AppInterface.GetInstance(PocoCamera.main));
//                if (info != null && info.mCode == 0) {
//                    UserInfo userInfo = cn.poco.loginlibs.LoginUtils.getUserInfo(info.mUserId, info.mAccessToken, AppInterface.GetInstance(PocoCamera.main));
//                    if (info != null && info.mCode == 0) {
//                        String defaultName = "interphoto用户";
//                        String defaultUrl = "http://mir19-bt-aimg.poco.cn/interphoto/interphoto/20170317/10/15000206220170317102436746.png";
//                        if (userInfo.mNickname.equals(defaultName) && userInfo.mUserIcon.equals(defaultUrl)) {
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (callBack != null) {
//                                        callBack.fillInfo(info);
//                                    }
//                                }
//                            });
//                        } else {
//                            UserMgr.DownloadHeadImg(PocoCamera.main, userInfo.mUserIcon);
//                            UserMgr.SaveCache(userInfo);
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    LoginOtherUtil.setSettingInfo(info);
//                                    LoginOtherUtil.showToast(R.string.toast_login_success);
//                                    EventCenter.sendEvent(EventID.UPDATE_USER_INFO);
//                                    if (callBack != null) {
//                                        callBack.succeed(info);
//                                    }
//                                }
//                            });
//                        }
//                    } else {
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                loginFail(info);
//                            }
//                        });
//                    }
//                } else {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            loginFail(info);
//                        }
//                    });
//                }
//            }
//        }).start();
//    }

    /**
     * 请在线程中调用
     *
     * @param userId
     * @param token
     * @return
     */
    public static boolean saveUserInfoToLocalBase(Context context,String userId, String token) {
        boolean isSuccess = false;
        UserInfo info = cn.poco.loginlibs.LoginUtils.getUserInfo(userId, token, AppInterface.GetInstance(context));
        if (info != null) {
            if (info.mCode == 0) {
                UserMgr.DownloadHeadImg(context, info.mUserIcon);
                UserMgr.SaveCache(context,info);
                isSuccess = true;
            }
        }
        return isSuccess;
    }

    public static boolean isChineseLanguage(Context context) {
        return context.getResources().getConfiguration().locale.getCountry().equals("CN");
    }
}
