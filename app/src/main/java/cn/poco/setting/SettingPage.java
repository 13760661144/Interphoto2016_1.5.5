package cn.poco.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.HashMap;

import cn.poco.blogcore.QzoneBlog2;
import cn.poco.blogcore.SinaBlog;
import cn.poco.blogcore.SinaBlog.BindSinaCallback;
import cn.poco.blogcore.Tools;
import cn.poco.blogcore.WeiXinBlog;
import cn.poco.blogcore.WeiboInfo;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.login.util.UserMgr;
import cn.poco.loginlibs.info.UserInfo;
import cn.poco.setting.site.SettingPageSite;
import cn.poco.share.SharePage;
import cn.poco.share.ShareTools;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.InterphotoDlg;
import cn.poco.watermarksync.manager.WatermarkSyncManager;


public class SettingPage extends IPage {
    private static final String TAG = "设置";
    protected SettingPageSite m_site;
    private Context m_context;

    private ImageView mBtnCancel;
    private TextView mSettingText;
    private ScrollView mScrollView;
    private LinearLayout mContainer;
    private TextView mTxCamera;
    private SettingGroup mSettingCamera;

    private TextView mTxBeautify;
    private SettingGroup mSettingBeautify;

    private TextView mTxAbout;
    private SettingGroup mSettingAbout;
    private TextView mTxWeibo;
    private SettingGroup mSettingWeibo;
    private SettingArrowBtn mABtnClearBuffer;
    private SettingArrowBtn mABtnFeedback;
    private SettingArrowBtn mABtnReset;
    private SettingArrowBtn mABtnSinaAccount;
    private SettingArrowBtn mABtnQzoneAccount;
    private SettingArrowBtn mABtnComment;
    private SettingArrowBtn mABtnAbout;
    private SettingArrowBtn mLanguageMoreBtn;
    private SettingItem mABtnWaterMark;

    private SettingItem mClearBuffer;
    private SettingItem mAItemFeedback;
    private SettingItem mAItemSinaAccount;
    private SettingItem mAItemQzoneAccount;
    private SettingItem mAItemAbout;
    private SettingItem mAItemReset;
    private SettingItem mLanguageMore;

    private TextView mVidoe;
    private SettingGroup mVideoBeautify;
    private SettingItem mVideoLogo;
    private SettingItem mVideoVideoHD;
    private SettingSliderBtn mVideoHDPhoto;
    private SettingArrowBtn mABtnVideoLogo;


    //	private SettingSliderBtn mSBtnAutoSaveSD;
    private SettingSliderBtn mSBtnNoSound;

    private SettingSliderBtn mSBtnAttachDate;
    //	private SettingSliderBtn mSBtnAutoOpenCamera;
    private SettingSliderBtn mSBtnHDPhoto;
    protected SettingSliderBtn.OnSwitchListener mSwitchListener = new SettingSliderBtn.OnSwitchListener() {
        @Override
        public void onSwitch(View v, boolean on) {
            if (v == mSBtnAttachDate) {
                SettingInfoMgr.GetSettingInfo(getContext()).SetAddDateState(on);
                if (mSBtnAttachDate.getSwitchStatus()){
                    MyBeautyStat.onClickByRes(R.string.设置页_图片加日期);
                }else {
                    MyBeautyStat.onClickByRes(R.string.设置页_关闭照片加日期);
                }
            }
//			else if(v == mSBtnAutoOpenCamera)
//			{
//				SettingInfoMgr.GetSettingInfo(getContext()).SetOpenCameraState(on);
//			}
//			else if(v == mSBtnAutoSaveSD)
//			{
//				SettingInfoMgr.GetSettingInfo(getContext()).SetAutoSaveCameraPhotoState(on);
//			}
            else if (v == mSBtnNoSound) {
                SettingInfoMgr.GetSettingInfo(getContext()).SetCameraSoundState(on);
                if (mSBtnNoSound.getSwitchStatus()){
                    MyBeautyStat.onClickByRes(R.string.设置页_打开无声拍照);
                }else {
                    MyBeautyStat.onClickByRes(R.string.设置页_关闭无声拍照);
                }
            } else if (v == mSBtnHDPhoto) {
                SettingInfoMgr.GetSettingInfo(getContext()).SetQualityState(on);
                if(mSBtnHDPhoto.getSwitchStatus()){
                    MyBeautyStat.onClickByRes(R.string.设置页_打开照片高清模式);
                }else {
                    MyBeautyStat.onClickByRes(R.string.设置页_关闭照片高清模式);
                }
            } else if (v == mVideoHDPhoto) {
                SettingInfoMgr.GetSettingInfo(getContext()).setVideoQualityState(on);
                TongJi2.AddCountByRes(getContext(), R.integer.视频高清模式);
                if (mVideoHDPhoto.getSwitchStatus()){
                    MyBeautyStat.onClickByRes(R.string.设置页_打开视频高清模式);
                }else {
                    MyBeautyStat.onClickByRes(R.string.设置页_关闭视频高清模式);
                }
            }
        }
    };
    private SettingGroup mLanguageGrop;
    private SinaBlog mSina;
    private QzoneBlog2 mQzone;
    private TextView mTxLanguage;
    private ProgressDialog m_progressDialog;
    private ShareTools m_shareTools;
    private InterphotoDlg m_loginTip;
    private ShareTools shareTools;
    private ImageView qZoneView, qqView, microblogView, weChatView, momentView;
    private String share_url = "http://www.adnonstop.com/interphoto/";
    protected OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mBtnCancel) {
                onBack();
                MyBeautyStat.onClickByRes(R.string.设置页_退出设置页);
            } else if (v == mABtnQzoneAccount || v == mAItemQzoneAccount) {
                if (checkQzoneBindingStatus(getContext())) {
                    AlertDialog alert = new AlertDialog.Builder(getContext()).create();
                    alert.setTitle(getResources().getString(R.string.tip));
                    alert.setMessage(getResources().getString(R.string.alreadyBoundTips));
                    alert.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearQzoneConfigure(getContext());
                            mABtnQzoneAccount.setText("");
                        }
                    });
                    alert.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.no), (DialogInterface.OnClickListener) null);
                    alert.show();
                } else {
                    MyBeautyStat.onClickByRes(R.string.设置页_qq空间账号);
                    bindQzone();

                }
            } else if (v == mABtnSinaAccount || v == mAItemSinaAccount) {
                if (checkSinaBindingStatus(getContext())) {
                    AlertDialog alert = new AlertDialog.Builder(getContext()).create();
                    alert.setTitle(getResources().getString(R.string.tip));
                    alert.setMessage(getResources().getString(R.string.alreadyBoundTips));
                    alert.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearSinaConfigure(getContext());
                            mABtnSinaAccount.setText("");
                        }
                    });
                    alert.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.no), (DialogInterface.OnClickListener) null);
                    alert.show();
                } else {
                    bindSina();
                    MyBeautyStat.onClickByRes(R.string.设置页_微博账号);
                }
            } else if (v == mABtnAbout || v == mAItemAbout) {
                if (m_site != null) m_site.OnAbout(getContext());
            } else if (v == mABtnComment) {
                try {
                    Uri uri = Uri.parse("market://details?id=" + getContext().getApplicationContext().getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                } catch (Throwable e) {
                    Toast.makeText(getContext(), getResources().getString(R.string.installplayStoreTips), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else if (v == mABtnFeedback || v == mAItemFeedback) {
                try {
                    MyBeautyStat.onClickByRes(R.string.设置页_打开问题反馈);
                    StringBuffer buf = new StringBuffer(2046);
                    String checkUrl;
                    if (SysConfig.IsDebug()) {
                        checkUrl = "http://tw.adnonstop.com/beauty/app/wap/interphoto/beta/public/index.php?r=Feedback/list";
                    } else {
                        checkUrl = "http://wap.adnonstop.com/interphoto/prod/public/index.php?r=Feedback/List";
                    }
                    buf.append(checkUrl);
                    buf.append("&");
                    buf.append("appname=");
                    buf.append("interphoto_app_android");
                    buf.append("&");

                    buf.append("client_ver=");
                    buf.append(SysConfig.GetAppVer(getContext()).trim());
                    buf.append("&");

                    buf.append("os_ver=");
                    buf.append(Build.VERSION.RELEASE);
                    buf.append("&");

                    buf.append("memory=");
                    buf.append(Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB");
                    buf.append("&");

                    buf.append("phone_type=");
                    String model = Build.MODEL;
                    PhoneTools info = new PhoneTools();
                    String fp = PhoneTools.replaceX(info.getFingerprint());
                    if (!TextUtils.isEmpty(fp)) {
                        try {
                            fp = Build.MODEL + "," + fp;
                            model = URLEncoder.encode(fp, "UTF-8");
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    buf.append(model);
                    buf.append("&");

                    buf.append("user_id=");
                    String userid = "0";

                    if(UserMgr.IsLogin(getContext(), null))
                    {
                        UserInfo info1 = UserMgr.ReadCache(getContext());
                        if(info1 != null)
                        {
                            userid = info1.mUserId;
                        }
                    }
                    buf.append(userid);

                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("url", buf.toString());
                    params.put("show_home", false);
                    params.put("title_by_url", true);
                    m_site.OnFeedback(getContext(), params);
                    /**
                     * 默认浏览器
                     */
//					CommonUtils.OpenBrowser(getContext(), buf.toString());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else if (v == mABtnReset || v == mAItemReset) {
                if (m_site != null) m_site.OnFixCamera(getContext());
                MyBeautyStat.onClickByRes(R.string.设置页_摄像头矫正);
            } else if (v == mLanguageMoreBtn || v == mLanguageMore) {
                m_site.OnLanguage(getContext());
            } else if (v == mABtnClearBuffer || v == mClearBuffer) {
                m_site.onAlbumCache(getContext());
                MyBeautyStat.onClickByRes(R.string.设置页_打开清除缓存);
            } else if (v == mABtnWaterMark) {

                if (UserMgr.IsLogin(getContext(),null)) {
                    if (WatermarkSyncManager.getInstacne(getContext()).isWatermarkBeingSynchronized()) {
                        LoginOtherUtil.showToast(getContext(),R.string.toast_synchronizing_background);
                    } else {
                        SynchronizedWatermark();
                        MyBeautyStat.onClickByRes(R.string.设置页_点击同步水印);
                    }
                } else {
                    if (m_loginTip == null) {
                        m_loginTip = new InterphotoDlg((Activity) getContext(), R.style.waitDialog);
                        m_loginTip.SetTitle(R.string.watermark_sync_tip_title);
                        m_loginTip.SetMessage(R.string.watermark_sync_tip_content);
                        m_loginTip.SetNegativeBtnText(R.string.watermark_sync_tip_cancel);
                        m_loginTip.SetPositiveBtnText(R.string.watermark_sync_tip_login);
                        m_loginTip.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback() {
                            @Override
                            public void onOK() {
                                m_loginTip.dismiss();
                                m_site.onLogin(getContext());
                            }

                            @Override
                            public void onCancel() {
                                m_loginTip.dismiss();
                            }
                        });

                    }
                    m_loginTip.show();
                }
            } else if (v == mVideoLogo || v == mABtnVideoLogo) {
                m_site.onSelectWatermark(getContext());
                TongJi2.AddCountByRes(getContext(), R.integer.视频LOGO);
                MyBeautyStat.onClickByRes(R.string.设置页_打开水印logo选择);
            } else if (v == momentView) {
                if (!Tools.checkApkExist(m_context, WeiXinBlog.WX_PACKAGE_NAME)) {
                    AlertDialog dlg = new AlertDialog.Builder(m_context).create();
                    dlg.setTitle(m_context.getResources().getString(R.string.tip));
                    dlg.setMessage(getResources().getString(R.string.setting_remind_wechat));
                    dlg.setButton(AlertDialog.BUTTON_POSITIVE, m_context.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
                    dlg.show();

                } else {
                    TongJi2.AddCountByRes(getContext(), R.integer.分享_微信朋友圈);
                    MyBeautyStat.onClickByRes(R.string.设置页_分享APP);
                    MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.朋友圈, R.string.设置页);
                    shareTools.sendUrlToWeiXin( R.drawable.login_default_head, share_url, getResources().getString(R.string.share_title), " ", false, new ShareTools.SendCompletedListener() {
                        @Override
                        public void result(int result) {
                            if (result == ShareTools.SUCCESS) {
                                ShareTools.ToastSuccess(getContext());
                            }
                        }
                    });
                }

            } else if (v == weChatView) {

                if (!Tools.checkApkExist(m_context, WeiXinBlog.WX_PACKAGE_NAME)) {
                    AlertDialog dlg = new AlertDialog.Builder(m_context).create();
                    dlg.setTitle(m_context.getResources().getString(R.string.tip));
                    dlg.setMessage(getResources().getString(R.string.setting_remind_wechat));
                    dlg.setButton(AlertDialog.BUTTON_POSITIVE, m_context.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
                    dlg.show();

                } else {
                    TongJi2.AddCountByRes(getContext(), R.integer.分享_微信好友);
                    MyBeautyStat.onClickByRes(R.string.设置页_分享APP);
                    MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微信好友, R.string.设置页);
                    shareTools.sendUrlToWeiXin( R.drawable.login_default_head, share_url,  getResources().getString(R.string.share_title), " ", true, new ShareTools.SendCompletedListener() {
                        @Override
                        public void result(int result) {
                            if (result == ShareTools.SUCCESS) {
                                ShareTools.ToastSuccess(getContext());
                            }
                        }
                    });
                }

            } else if (v == qqView) {


                if (Tools.checkApkExist(m_context, QzoneBlog2.QQ_PACKAGE_NAME)) {
                    MyBeautyStat.onClickByRes(R.string.设置页_分享APP);
                    TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ好友);
                    MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ好友, R.string.设置页);
                    shareTools.sendUrlToQQ( getResources().getString(R.string.share_title), "", R.drawable.setting_share_icon, share_url, new ShareTools.SendCompletedListener() {
                        @Override
                        public void result(int result) {
                            if (result == ShareTools.SUCCESS) {
                                ShareTools.ToastSuccess(getContext());
                            }
                        }
                    });
                }else {
                    AlertDialog dlg = new AlertDialog.Builder(m_context).create();
                    dlg.setTitle(m_context.getResources().getString(R.string.tip));
                    dlg.setMessage(getResources().getString(R.string.setting_remind_qq));
                    dlg.setButton(AlertDialog.BUTTON_POSITIVE, m_context.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
                    dlg.show();

                }

            } else if (v == qZoneView) {

                if (SettingPage.checkQzoneBindingStatus(getContext())) {
                    TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ空间);
                    MyBeautyStat.onClickByRes(R.string.设置页_分享APP);
                    MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.QQ空间, R.string.设置页);
                    m_shareTools.sendUrlToQzone("", R.drawable.setting_share_icon,  getResources().getString(R.string.share_title), share_url, new ShareTools.SendCompletedListener() {
                        @Override
                        public void result(int result) {
                            if(result ==  shareTools.SUCCESS){
                                  ShareTools.ToastSuccess(getContext());
                            }

                        }
                    });
                } else {
                    m_shareTools.bindQzone(false, new SharePage.BindCompleteListener() {
                        @Override
                        public void success() {
                            Toast.makeText(getContext(), getResources().getString(R.string.Linked), Toast.LENGTH_SHORT).show();
                            mABtnQzoneAccount.setText(SettingInfoMgr.GetSettingInfo(getContext()).GetQzoneUserName());
                            m_shareTools.sendUrlToQzone("", R.drawable.setting_share_icon,  getResources().getString(R.string.share_title), share_url, new ShareTools.SendCompletedListener() {
                                @Override
                                public void result(int result) {
                                    if(result ==  shareTools.SUCCESS){
                                          ShareTools.ToastSuccess(getContext());
                                    }

                                }
                            });
                        }

                        @Override
                        public void fail() {

                        }
                    });
                }
            } else if (v == microblogView) {
                TongJi2.AddCountByRes(getContext(), R.integer.分享_新浪微博);
                MyBeautyStat.onClickByRes(R.string.设置页_分享APP);
                MyBeautyStat.onShareCompleteByRes(MyBeautyStat.BlogType.微博, R.string.设置页);
                if (checkSinaBindingStatus(getContext())) {
                    shareTools.sendToSina( R.drawable.setting_share_icon, getResources().getString(R.string.share_title_weibo)+share_url, new ShareTools.SendCompletedListener() {
                        @Override
                        public void result(int result) {
                            if (result == ShareTools.SUCCESS) {
                                ShareTools.ToastSuccess(getContext());
                            }
                        }
                    });
                } else {
                    if (mSina == null) mSina = new SinaBlog(getContext());

                    mSina.bindSinaWithSSO(new BindSinaCallback() {
                        @Override
                        public void success(String accessToken, String expiresIn, String uid, String userName, String nickName) {

                            SettingInfoMgr.GetSettingInfo(getContext()).SetSinaAccessToken(accessToken);
                            SettingInfoMgr.GetSettingInfo(getContext()).SetSinaUid(uid);
                            SettingInfoMgr.GetSettingInfo(getContext()).SetSinaExpiresIn(expiresIn);
                            SettingInfoMgr.GetSettingInfo(getContext()).SetSinaSaveTime(String.valueOf(System.currentTimeMillis() / 1000));
                            SettingInfoMgr.GetSettingInfo(getContext()).SetSinaUserName(userName);
                            SettingInfoMgr.GetSettingInfo(getContext()).SetSinaUserNick(nickName);
                            if (nickName != null) mABtnSinaAccount.setText(nickName);

                            Toast.makeText(getContext(), getResources().getString(R.string.Linked), Toast.LENGTH_SHORT).show();

                            m_shareTools.sendToSina( R.drawable.setting_share_icon, getResources().getString(R.string.share_title_weibo)+share_url, new ShareTools.SendCompletedListener() {
                                @Override
                                public void result(int result) {
                                    if (result == ShareTools.SUCCESS) {
                                        ShareTools.ToastSuccess(getContext());
                                    }
                                }
                            });
                        }

                        @Override
                        public void fail() {

                        }
                    });


                }
            }
        }
    };

    public SettingPage(Context context, BaseSite site) {
        super(context, site);

        m_site = (SettingPageSite) site;
        m_context = context;
        initData();
        initUI();
        setConfigInfo();
        shareTools = new ShareTools(context);
        TongJiUtils.onPageStart(getContext(), TAG);
    }

    /**
     * 检测新浪微博绑定状态
     *
     * @param context
     * @return true为已绑定，false为未绑定
     */
    public static boolean checkSinaBindingStatus(Context context) {
        if (SettingInfoMgr.GetSettingInfo(context).GetSinaAccessToken() != null && SettingInfoMgr.GetSettingInfo(context).GetSinaAccessToken().length() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 检测QQ空间绑定状态
     *
     * @param context
     * @return true为已绑定，false为未绑定
     */
    public static boolean checkQzoneBindingStatus(Context context) {
        if (SettingInfoMgr.GetSettingInfo(context).GetQzoneAccessToken() != null && SettingInfoMgr.GetSettingInfo(context).GetQzoneAccessToken().length() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 解除新浪微博绑定
     *
     * @param context
     */
    public static void clearSinaConfigure(Context context) {
        SettingInfoMgr.GetSettingInfo(context).SetSinaAccessToken(null);
        SettingInfoMgr.GetSettingInfo(context).SetSinaUid(null);
        SettingInfoMgr.GetSettingInfo(context).SetSinaExpiresIn(null);
        SettingInfoMgr.GetSettingInfo(context).SetSinaSaveTime(null);
        SettingInfoMgr.GetSettingInfo(context).SetSinaUserName(null);
        SettingInfoMgr.GetSettingInfo(context).SetSinaUserNick(null);
    }

    /**
     * 解除QQ空间绑定
     *
     * @param context
     */
    public static void clearQzoneConfigure(Context context) {
        SettingInfoMgr.GetSettingInfo(context).SetQzoneAccessToken(null);
        SettingInfoMgr.GetSettingInfo(context).SetQzoneOpenid(null);
        SettingInfoMgr.GetSettingInfo(context).SetQzoneExpiresIn(null);
        SettingInfoMgr.GetSettingInfo(context).SetQzoneSaveTime(null);
        SettingInfoMgr.GetSettingInfo(context).SetQzoneUserName(null);
    }

    /**
     * 显示对话框
     *
     * @param title   标题资源
     * @param message 内容资源
     */
    private void showDialog(@StringRes int title, @StringRes int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null);
        builder.create().show();
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSina != null) mSina.onActivityResult(requestCode, resultCode, data, -10086);
		if(mQzone != null) mQzone.onActivityResult(requestCode, resultCode, data);
        if (m_shareTools != null) {
            m_shareTools.onActivityResult(requestCode, resultCode, data);
        }
        return super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void SetData(HashMap<String, Object> params) {
    }

    @Override
    public void onBack() {
        SettingInfoMgr.Save(getContext());
        if (m_site != null) m_site.OnBack(getContext());
        TongJiUtils.onPageEnd(getContext(), TAG);
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

    private void initData() {
        ShareData.InitData((Activity) m_context);
        SharePage.initBlogConfig();
        m_shareTools = new ShareTools(getContext());
    }

    private void initUI() {
        setBackgroundColor(0xff0e0e0e);

        FrameLayout.LayoutParams fl;
        LinearLayout.LayoutParams ll;

        FrameLayout topBar = new FrameLayout(m_context);
        topBar.setBackgroundColor(Color.BLACK);
        fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
        fl.gravity = Gravity.LEFT | Gravity.TOP;
        addView(topBar, fl);
        {
            mBtnCancel = new ImageView(m_context);
            mBtnCancel.setImageResource(R.drawable.framework_back_btn);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            topBar.addView(mBtnCancel, fl);
            mBtnCancel.setOnClickListener(mClickListener);

            fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER;
            mSettingText = new TextView(m_context);
            mSettingText.setTextColor(Color.WHITE);
            mSettingText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            mSettingText.setText(getResources().getString(R.string.homepage_setting));
            topBar.addView(mSettingText, fl);
        }

        mScrollView = new ScrollView(m_context);
        fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        fl.gravity = Gravity.LEFT | Gravity.TOP;
        fl.topMargin = ShareData.PxToDpi_xhdpi(80);
        addView(mScrollView, 0, fl);

        mABtnSinaAccount = new SettingArrowBtn(m_context);
        mABtnSinaAccount.setOnClickListener(mClickListener);
        mABtnQzoneAccount = new SettingArrowBtn(m_context);
        mABtnQzoneAccount.setOnClickListener(mClickListener);


        mABtnComment = new SettingArrowBtn(m_context);
        mABtnComment.setOnClickListener(mClickListener);
        mABtnAbout = new SettingArrowBtn(m_context);
        mABtnAbout.setOnClickListener(mClickListener);
        mABtnFeedback = new SettingArrowBtn(m_context);
        mABtnFeedback.setOnClickListener(mClickListener);
        mABtnClearBuffer = new SettingArrowBtn(m_context);
        mABtnClearBuffer.setOnClickListener(mClickListener);
        mLanguageMoreBtn = new SettingArrowBtn(m_context);
        mLanguageMoreBtn.setOnClickListener(mClickListener);

        mSBtnAttachDate = new SettingSliderBtn(m_context);
        mSBtnAttachDate.setOnSwitchListener(mSwitchListener);
//		mSBtnAutoOpenCamera = new SettingSliderBtn(m_context);
//		mSBtnAutoOpenCamera.setOnSwitchListener(mSwitchListener);
//		mSBtnAutoSaveSD = new SettingSliderBtn(m_context);
//		mSBtnAutoSaveSD.setOnSwitchListener(mSwitchListener);
        mSBtnNoSound = new SettingSliderBtn(m_context);
        mSBtnNoSound.setOnSwitchListener(mSwitchListener);
        mSBtnHDPhoto = new SettingSliderBtn(m_context);
        mSBtnHDPhoto.setOnSwitchListener(mSwitchListener);


        mVideoHDPhoto = new SettingSliderBtn(m_context);
        mVideoHDPhoto.setOnSwitchListener(mSwitchListener);


        mABtnVideoLogo = new SettingArrowBtn(m_context);
        mABtnVideoLogo.setOnClickListener(mClickListener);


        mABtnReset = new SettingArrowBtn(m_context);
        mABtnReset.setOnClickListener(mClickListener);

        mContainer = new LinearLayout(m_context);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.LEFT | Gravity.TOP;
        mScrollView.addView(mContainer, fl);
        {
            int left_padding = ShareData.PxToDpi_xhdpi(30);
            int top_padding = ShareData.PxToDpi_xhdpi(50);
            int bottom_padding = ShareData.PxToDpi_xhdpi(30);

            mTxLanguage = new TextView(m_context);
            mTxLanguage.setPadding(left_padding, top_padding, 0, bottom_padding);
            mTxLanguage.setText(getResources().getString(R.string.setting_language));
            mTxLanguage.setTextColor(0xffaaaaaa);
            mTxLanguage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mTxLanguage, ll);

            mLanguageGrop = new SettingGroup(m_context);
            mLanguageMore = mLanguageGrop.addItem(getCurLanguage(), mLanguageMoreBtn);
            mLanguageMore.setOnClickListener(mClickListener);
            ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mLanguageGrop, ll);

            mTxCamera = new TextView(m_context);
            mTxCamera.setPadding(left_padding, top_padding, 0, bottom_padding);
            mTxCamera.setText(getResources().getString(R.string.homepage_jingtou));
            mTxCamera.setTextColor(0xffaaaaaa);
            mTxCamera.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mTxCamera, ll);

            mSettingCamera = new SettingGroup(m_context);
            mSettingCamera.addItem(getResources().getString(R.string.shuttertext), mSBtnNoSound);
//			mSettingCamera.addItem("保存原图", mSBtnAutoSaveSD);
//			mSettingCamera.addItem("进入直接开启镜头", mSBtnAutoOpenCamera);
            mAItemReset = mSettingCamera.addItem(getResources().getString(R.string.lensCorrection), mABtnReset);
            mAItemReset.setOnClickListener(mClickListener);
            ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mSettingCamera, ll);

            mTxBeautify = new TextView(m_context);
            mTxBeautify.setPadding(left_padding, top_padding, 0, bottom_padding);
            mTxBeautify.setText(getResources().getString(R.string.homepage_qualityData));
            mTxBeautify.setTextColor(0xffaaaaaa);
            mTxBeautify.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mTxBeautify, ll);

            mSettingBeautify = new SettingGroup(m_context);
            mSettingBeautify.addItem(getResources().getString(R.string.homepage_addData), mSBtnAttachDate);
            mSettingBeautify.addItem(getResources().getString(R.string.homepage_hightqulity), mSBtnHDPhoto);
            mABtnWaterMark = mSettingBeautify.addItem(getResources().getString(R.string.userinfo_synchronized_watermark), mABtnWaterMark);
            mABtnWaterMark.setOnClickListener(mClickListener);
            ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mSettingBeautify, ll);

            mVidoe = new TextView(m_context);
            mVidoe.setPadding(left_padding, top_padding, 0, bottom_padding);
            mVidoe.setText(getResources().getString(R.string.video));
            mVidoe.setTextColor(0xffaaaaaa);
            mVidoe.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mVidoe, ll);

            mVideoBeautify = new SettingGroup(m_context);
            mVideoVideoHD = mVideoBeautify.addItem(getResources().getString(R.string.vidoe_mode), mVideoHDPhoto);
            mVideoLogo = mVideoBeautify.addItem(getResources().getString(R.string.video_watermark), mABtnVideoLogo);
            mVideoLogo.setOnClickListener(mClickListener);
            ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mVideoBeautify, ll);

            mTxWeibo = new TextView(m_context);
            mTxWeibo.setPadding(left_padding, top_padding, 0, bottom_padding);
            mTxWeibo.setText(getResources().getString(R.string.account));
            mTxWeibo.setTextColor(0xffaaaaaa);
            mTxWeibo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mTxWeibo, ll);

            mSettingWeibo = new SettingGroup(m_context);
            mAItemSinaAccount = mSettingWeibo.addItem(getResources().getString(R.string.Sina), mABtnSinaAccount);
            mAItemSinaAccount.setOnClickListener(mClickListener);
            mAItemQzoneAccount = mSettingWeibo.addItem(getResources().getString(R.string.QQZone), mABtnQzoneAccount);
            mAItemQzoneAccount.setOnClickListener(mClickListener);
            ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mSettingWeibo, ll);


            mTxAbout = new TextView(m_context);
            mTxAbout.setPadding(left_padding, top_padding, 0, bottom_padding);
            mTxAbout.setText(getResources().getString(R.string.homepage_System));
            mTxAbout.setTextColor(0xffaaaaaa);
            mTxAbout.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mTxAbout, ll);

            mSettingAbout = new SettingGroup(m_context);
            mSettingAbout.addItem(getResources().getString(R.string.homepage_rating), mABtnComment);
            mClearBuffer = mSettingAbout.addItem(getResources().getString(R.string.homepage_clear_sys_buffer), mABtnClearBuffer);
            mABtnClearBuffer.setOnClickListener(mClickListener);
            mClearBuffer.setOnClickListener(mClickListener);
            mAItemFeedback = mSettingAbout.addItem(getResources().getString(R.string.homepage_Feedback), mABtnFeedback);
            mAItemFeedback.setOnClickListener(mClickListener);
            mAItemAbout = mSettingAbout.addItem(getResources().getString(R.string.homepage_About), mABtnAbout);
            mAItemAbout.setOnClickListener(mClickListener);
//			mLanguageMore = mSettingAbout.addItem(getResources().getString(R.string.setting_language),mLanguageMoreBtn);
//			mLanguageMore.setOnClickListener(mClickListener);
            ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT | Gravity.TOP;
            mContainer.addView(mSettingAbout, ll);
        }

        //底部
        {
            RelativeLayout bottonLayout = new RelativeLayout(m_context);
            bottonLayout.setBackgroundColor(Color.BLACK);
            bottonLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mContainer.addView(bottonLayout);

            RelativeLayout.LayoutParams botPatams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView botText = new TextView(m_context);
            botText.setText(getContext().getString(R.string.setting_botText));
            botText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            botText.setId(R.id.botText);
            botText.setTextColor(Color.parseColor("#666666"));
            botPatams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            botPatams.topMargin = ShareData.PxToDpi_xhdpi(100);
            botPatams.bottomMargin = ShareData.PxToDpi_xhdpi(99);
            bottonLayout.addView(botText, botPatams);

            {
                botPatams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                LinearLayout botLinearLayout = new LinearLayout(m_context);
                botLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                botLinearLayout.setId(R.id.botlinearLyouat);
                botLinearLayout.setWeightSum(5);
                botPatams.addRule(RelativeLayout.BELOW, R.id.botText);
                botPatams.leftMargin = ShareData.PxToDpi_xhdpi(30);
                botPatams.rightMargin = ShareData.PxToDpi_xhdpi(30);
                bottonLayout.addView(botLinearLayout, botPatams);

                int pad = ShareData.PxToDpi_xhdpi(32);
                LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                momentView = new ImageView(m_context);
                momentView.setPadding(0,pad,0,pad);
                momentView.setOnClickListener(mClickListener);
                momentView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                momentView.setImageResource(R.drawable.setting_moment);
                botLinearLayout.addView(momentView, lParams);

                lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                weChatView = new ImageView(m_context);
                weChatView.setPadding(0,pad,0,pad);
                weChatView.setOnClickListener(mClickListener);
                weChatView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                weChatView.setImageResource(R.drawable.setting_wechat);
                botLinearLayout.addView(weChatView, lParams);

                lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                microblogView = new ImageView(m_context);
                microblogView.setPadding(0,pad,0,pad);
                microblogView.setOnClickListener(mClickListener);
                microblogView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                microblogView.setImageResource(R.drawable.setting_microblog);
                botLinearLayout.addView(microblogView, lParams);

                lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                qZoneView = new ImageView(m_context);
                qZoneView.setPadding(0,pad,0,pad);
                qZoneView.setOnClickListener(mClickListener);
                qZoneView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                qZoneView.setImageResource(R.drawable.setting_qzone);
                botLinearLayout.addView(qZoneView, lParams);

                lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                qqView = new ImageView(m_context);
                qqView.setPadding(0,pad,0,pad);
                qqView.setOnClickListener(mClickListener);
                qqView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                qqView.setImageResource(R.drawable.setting_qq);
                botLinearLayout.addView(qqView, lParams);
            }

            botPatams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView interPhotoText = new TextView(m_context);
            interPhotoText.setTextColor(Color.parseColor("#666666"));
            interPhotoText.setId(R.id.interPhotoText);
            interPhotoText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
            interPhotoText.setText(getContext().getString(R.string.setting_interphotp));
            botPatams.topMargin = ShareData.PxToDpi_xhdpi(80);
            botPatams.bottomMargin = ShareData.PxToDpi_xhdpi(10);
            botPatams.addRule(RelativeLayout.BELOW, R.id.botlinearLyouat);
            botPatams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            bottonLayout.addView(interPhotoText, botPatams);

            botPatams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ImageView beautyImage = new ImageView(m_context);
            beautyImage.setImageResource(R.drawable.setting_copyright);
            botPatams.addRule(RelativeLayout.BELOW, R.id.interPhotoText);
            botPatams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            botPatams.bottomMargin = ShareData.PxToDpi_xhdpi(100);
            bottonLayout.addView(beautyImage, botPatams);

        }

    }

    public void SynchronizedWatermark() {
//		if(WatermarkSyncManager.getInstacne(getContext()).isWatermarkBeingSynchronized())
//		{
//			LoginOtherUtil.showToast(getContext(),R.string.toast_synchronizing_background);
//		}
//		else
//		{
        m_progressDialog = new ProgressDialog(getContext());
        m_progressDialog.setCancelable(true);
        m_progressDialog.setCanceledOnTouchOutside(false);
        m_progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (m_progressDialog != null) {
                    m_progressDialog.dismiss();
                    m_progressDialog = null;
                }
                LoginOtherUtil.showToast(getContext(),R.string.toast_synchronizing_background);
            }
        });
        WatermarkSyncManager.getInstacne(getContext()).setSyncCallback(new WatermarkSyncManager.SyncManagerCallback() {
            @Override
            public void onFinishSuccessful() {
                if (m_progressDialog != null) {
                    m_progressDialog.dismiss();
                    m_progressDialog = null;
                }
                LoginOtherUtil.showToast(getContext(),R.string.toast_synchronize_success);
            }

            @Override
            public void onFail() {
                if (m_progressDialog != null) {
                    m_progressDialog.dismiss();
                    m_progressDialog = null;
                }
                LoginOtherUtil.showToast(getContext(),R.string.toast_synchronize_failure);
            }

            @Override
            public void onTimeOut() {
                if (m_progressDialog != null) {
                    m_progressDialog.dismiss();
                    m_progressDialog = null;
                }
                LoginOtherUtil.showToast(getContext(),R.string.toast_time_out);
            }

            @Override
            public void noNeedToSync() {
                if (m_progressDialog != null) {
                    m_progressDialog.dismiss();
                    m_progressDialog = null;
                }
                LoginOtherUtil.showToast(getContext(),R.string.toast_no_watermark);
            }
        });
        WatermarkSyncManager.getInstacne(getContext()).syncWatermarkInfoWithServer();
//		}
    }

    private String getCurLanguage() {
        String out = getResources().getString(R.string.autoLanguage);
        if (!TagMgr.CheckTag(getContext(), LanguagePage.ENGLISH_TAGVALUE)) {
            out = getResources().getString(R.string.englishLanguage);
            return out;
        } else if (!TagMgr.CheckTag(getContext(), LanguagePage.CHINA_TAGVALUE)) {
            out = getResources().getString(R.string.chinaLanguage);
            return out;
        }
        return out;
    }

    private void setConfigInfo() {
        if (SettingInfoMgr.GetSettingInfo(getContext()).GetQzoneUserName() != null && SettingInfoMgr.GetSettingInfo(getContext()).GetQzoneUserName().length() != 0) {
            mABtnQzoneAccount.setText(SettingInfoMgr.GetSettingInfo(getContext()).GetQzoneUserName());
        }
        if (SettingInfoMgr.GetSettingInfo(getContext()).GetSinaUserNick() != null && SettingInfoMgr.GetSettingInfo(getContext()).GetSinaUserNick().length() != 0) {
            mABtnSinaAccount.setText(SettingInfoMgr.GetSettingInfo(getContext()).GetSinaUserNick());
        }
        mSBtnHDPhoto.setSwitchStatus(SettingInfoMgr.GetSettingInfo(getContext()).GetQualityState());
//		mSBtnAutoSaveSD.setSwitchStatus(SettingInfoMgr.GetSettingInfo(getContext()).GetAutoSaveCameraPhotoState());
        mSBtnAttachDate.setSwitchStatus(SettingInfoMgr.GetSettingInfo(getContext()).GetAddDateState());
//		mSBtnAutoOpenCamera.setSwitchStatus(SettingInfoMgr.GetSettingInfo(getContext()).GetOpenCameraState());
        mSBtnNoSound.setSwitchStatus(SettingInfoMgr.GetSettingInfo(getContext()).GetCameraSoundState());
        mVideoHDPhoto.setSwitchStatus(SettingInfoMgr.GetSettingInfo(getContext()).getVideoQualityState());
    }

    public void bindSina() {
        if (mSina == null) mSina = new SinaBlog(getContext());

        mSina.bindSinaWithSSO(new BindSinaCallback() {
            @Override
            public void success(String accessToken, String expiresIn, String uid, String userName, String nickName) {
                SettingInfoMgr.GetSettingInfo(getContext()).SetSinaAccessToken(accessToken);
                SettingInfoMgr.GetSettingInfo(getContext()).SetSinaUid(uid);
                SettingInfoMgr.GetSettingInfo(getContext()).SetSinaExpiresIn(expiresIn);
                SettingInfoMgr.GetSettingInfo(getContext()).SetSinaSaveTime(String.valueOf(System.currentTimeMillis() / 1000));
                SettingInfoMgr.GetSettingInfo(getContext()).SetSinaUserName(userName);
                SettingInfoMgr.GetSettingInfo(getContext()).SetSinaUserNick(nickName);
                if (nickName != null) mABtnSinaAccount.setText(nickName);

//				new Thread(new Runnable()
//				{
//					@Override
//					public void run()
//					{
//						mSina.flowerCameraSinaWeibo(Constant.sinaUserId, accessToken);
//					}
//				}).start();
            }

            @Override
            public void fail() {
                switch (mSina.LAST_ERROR) {
                    case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
                        AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
                        dlg.setTitle(getResources().getString(R.string.tip));
                        dlg.setMessage(getResources().getString(R.string.installSinaWeiboTips));
                        dlg.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok), (DialogInterface.OnClickListener) null);
                        dlg.show();
                        break;

                    default:
                        Toast.makeText(getContext(), getResources().getString(R.string.LinkSinaWeiboFailed), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    private void bindQzone() {
//		if(mQzone == null) mQzone = new QzoneBlog2(getContext());

        m_progressDialog = ProgressDialog.show(getContext(), "", getContext().getResources().getString(R.string.Linking));
        m_progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_shareTools.bindQzone(false, new SharePage.BindCompleteListener() {
            @Override
            public void success() {
                if (m_progressDialog != null) {
                    m_progressDialog.dismiss();
                    m_progressDialog = null;
                }
                if (SettingInfoMgr.GetSettingInfo(getContext()).GetQzoneUserName() != null)
                    mABtnQzoneAccount.setText(SettingInfoMgr.GetSettingInfo(getContext()).GetQzoneUserName());

            }

            @Override
            public void fail() {
                if (m_progressDialog != null) {
                    m_progressDialog.dismiss();
                    m_progressDialog = null;
                }
            }
        });
//		mQzone.bindQzoneWithSDK(new BindQzoneCallback()
//		{
//			@Override
//			public void success(String accessToken, String expiresIn, String openId, String nickName)
//			{
//				SettingInfoMgr.GetSettingInfo(getContext()).SetQzoneAccessToken(accessToken);
//				SettingInfoMgr.GetSettingInfo(getContext()).SetQzoneOpenid(openId);
//				SettingInfoMgr.GetSettingInfo(getContext()).SetQzoneExpiresIn(expiresIn);
//				SettingInfoMgr.GetSettingInfo(getContext()).SetQzoneSaveTime(String.valueOf(System.currentTimeMillis() / 1000));
//				SettingInfoMgr.GetSettingInfo(getContext()).SetQzoneUserName(nickName);
//				if(nickName != null) mABtnQzoneAccount.setText(nickName);
//			}
//
//			@Override
//			public void fail()
//			{
//				switch(mQzone.LAST_ERROR)
//				{
//					case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
//						AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
//						dlg.setTitle(getResources().getString(R.string.tip));
//						dlg.setMessage(getResources().getString(R.string.installQQTips));
//						dlg.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
//						dlg.show();
//						break;
//
//					default:
//						Toast.makeText(getContext(), getResources().getString(R.string.boundQQZoneFailed), Toast.LENGTH_LONG).show();
//						break;
//				}
//			}
//		});
    }

    @Override
    public void onPageResult(int siteID, HashMap<String, Object> params) {
        super.onPageResult(siteID, params);
        if (siteID == SiteID.LOGIN || siteID == SiteID.REGISTER_DETAIL | siteID == SiteID.RESETPSW) {
            if (UserMgr.IsLogin(getContext(),null)) {
                SynchronizedWatermark();
            }
        }
    }
}
