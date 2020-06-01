package cn.poco.home.site;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;

import cn.poco.MaterialMgr2.site.ThemePageSite;
import cn.poco.album2.site.AlbumSite;
import cn.poco.album2.site.AlbumSite4;
import cn.poco.appmarket.site.MarketPageSite;
import cn.poco.banner.BannerCore3;
import cn.poco.beautify.BeautifyModuleType;
import cn.poco.camera.site.CameraPageSite;
import cn.poco.capture2.site.CapturePageSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.home.HomePage;
import cn.poco.home.OpenApp;
import cn.poco.interphoto2.R;
import cn.poco.login.site.LoginPageSite;
import cn.poco.login.site.LoginPageSite5;
import cn.poco.login.site.UserInfoPageSite;
import cn.poco.login.util.UserMgr;
import cn.poco.resource.ResType;
import cn.poco.setting.site.SettingPageSite;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.CommonUtils;
import cn.poco.utils.Utils;
import cn.poco.video.site.VideoAlbumSite;
import cn.poco.webview.WebViewPage1;
import cn.poco.webview.site.WebViewPageSite3;

public class HomePageSite extends BaseSite {
    public CmdProc m_cmdProc;

    public HomePageSite() {
        super(SiteID.HOME);

        MakeCmdProc();
    }

    /**
     * 注意构造函数调用
     */
    protected void MakeCmdProc() {
        m_cmdProc = new CmdProc();
    }

    @Override
    public IPage MakePage(Context context) {
        return new HomePage(context, this);
    }

    public void OnCamera(Context context) {
        MyFramework.SITE_Open(context, CameraPageSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void OnCapture(Context context) {
        MyFramework.SITE_Open(context, CapturePageSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void OnBeautify(Context context) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("from_home", true);
        MyFramework.SITE_Open(context, AlbumSite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void onVideo(Context context) {
        MyFramework.SITE_Open(context, VideoAlbumSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);

    }

    public void OnImg(String url,Context context) {
        BannerCore3.ExecuteCommand(context, url, m_cmdProc);
    }

    public void OnBack(Context context) {
        MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void OnInterphoto(String url,Context context) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("url", url);
        MyFramework.SITE_Popup(context, WebViewPageSite3.class, params, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void OnMaterial(Context context) {
        MyBeautyStat.onClickByRes(R.string.素材主题);
        MyFramework.SITE_Popup(context, ThemePageSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void OnRecommendApp(Context context) {
        MyFramework.SITE_Popup(context, MarketPageSite.class, null, Framework2.ANIM_TRANSLATION_BOTTOM);
    }

    public void OnSetting(Context context) {
//		MyFramework.SITE_Popup(context, VideoTextSamplePageSite.class, null, Framework.ANIM_TRANSLATION_BOTTOM);
        MyFramework.SITE_Popup(context, SettingPageSite.class, null, Framework2.ANIM_TRANSLATION_BOTTOM);
    }

    public void OnLogin(String maskBmp,Context context) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("img", maskBmp);
        MyFramework.SITE_Popup(context, LoginPageSite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
//		MyFramework.SITE_Popup(context, UserInfoPageSite.class, params, MyFramework.ANIM_TRANSLATION_LEFT);
    }

    public void OnUserInfo(String id, String maskBmp,Context context) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("img", maskBmp);
        MyFramework.SITE_Popup(context, UserInfoPageSite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public static class CmdProc implements BannerCore3.CmdCallback {

        @Override
        public void OpenPage(Context context, int code, String... args) {
            HashMap<String, Object> params = new HashMap<>();
            if (code == 2) {
                params.put("def_page", BeautifyModuleType.FILTER.GetValue());
                MyFramework.SITE_Open(context, AlbumSite4.class, params, Framework2.ANIM_TRANSLATION_LEFT);
            } else if (code == 1) {
                params.put("def_page", BeautifyModuleType.TEXT.GetValue());
                MyFramework.SITE_Open(context, AlbumSite4.class, params, Framework2.ANIM_TRANSLATION_LEFT);
            } else if (code == 3) {
                params.put("def_page", BeautifyModuleType.EFFECT.GetValue());
                MyFramework.SITE_Open(context, AlbumSite4.class, params, Framework2.ANIM_TRANSLATION_LEFT);
            } else if (code == 4) {
                if (args != null) {
                    int len = args.length;
                    String[] pair;
                    for (int i = 0; i < len; i++) {
                        if (!TextUtils.isEmpty(args[i])) {
                            pair = args[i].split("=");
                            if (pair.length == 2) {
                                if (pair[0].equals("type")) {
                                    if (pair[1].equals("filter")) {
                                        params.put("type", ResType.FILTER);
                                    } else if (pair[1].equals("text")) {
                                        params.put("type", ResType.TEXT);
                                    } else if (pair[1].equals("light")) {
                                        params.put("type", ResType.LIGHT_EFFECT);
                                    }
                                }
                                if (pair[0].equals("id")) {
                                    params.put("id", pair[1]);
                                }
                            }
                        }
                    }
                }
                MyFramework.SITE_Open(context, ThemePageSite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
            } else if (code == -1) {
                if (args != null && args.length == 2) {
                    String url = args[1];
                    if (url != null) {
                        if (args[0].equals("month")) {
                            params.put("url", WebViewPage1.GetBeautyUrl(context, url));
                            params.put("title_by_url", true);
                            MyFramework.SITE_Popup(context, WebViewPageSite3.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
                        } else if (args[0].equals("login")) {
                            if (UserMgr.IsLogin(context, null)) {
                                params.put("url", WebViewPage1.GetBeautyUrl(context, url));
                                params.put("title_by_url", true);
                                MyFramework.SITE_Popup(context, WebViewPageSite3.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
                            } else {
                                params.put("url", WebViewPage1.GetBeautyUrl(context, url));
                                MyFramework.SITE_Popup(context, LoginPageSite5.class, params, Framework2.ANIM_NONE);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void OpenWebPage(Context context, String... args) {
            String url;
            if (args != null && args.length > 0 && (url = args[0]) != null) {
                String myUrl = Utils.PocoDecodeUrl(context, url);
                CommonUtils.OpenBrowser(context, myUrl);
            }
        }

        @Override
        public void OpenMyWebPage(Context context, String... args) {
            String url;
            if (args != null && args.length > 0 && (url = args[0]) != null) {
                HashMap<String, Object> params = new HashMap<String, Object>();
                String myUrl = Utils.PocoDecodeUrl(context, url);
                params.put("url", myUrl);
                String share_logo;
                if (args.length > 1 && (share_logo = args[1]) != null) {
                    params.put("share_app", 1);
                    params.put("share_logo", share_logo);
                }
                MyFramework.SITE_Popup(context, WebViewPageSite3.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
            }
        }

        @Override
        public void OpenPocoCamera(Context context, String... args) {
            OpenApp.openPoco(context);
        }

        @Override
        public void OpenPocoMix(Context context, String... args) {
            OpenApp.openPMix(context);
        }

        @Override
        public void OpenJane(Context context, String... args) {
            OpenApp.openJane(context);
        }

        @Override
        public void OpenBeautyCamera(Context context, String... args) {
            OpenApp.openBeauty(context);
        }

        @Override
        public void OpenBusinessPage(Context context, String... args) {

        }
    }
}
