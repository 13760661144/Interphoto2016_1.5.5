package cn.poco.camera.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.MaterialMgr2.site.ThemePageSite3;
import cn.poco.album2.site.AlbumSite;
import cn.poco.beautify.site.BeautifyPageSite;
import cn.poco.camera.CameraPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.webview.site.WebViewPageSite;

public class CameraPageSite extends BaseSite {

    public CameraPageSite() {
        super(SiteID.CAMERA);
    }

    @Override
    public IPage MakePage(Context context) {
        return new CameraPage(context, this);
    }

    public void OnBack(Context context) {
        MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
    }

    /**
     * @param params
     * imgs 数据保存对象(ImageFile2) <br/>
     */
    public void OnTakePicture(Context context, HashMap<String, Object> params) {
        MyFramework.SITE_Open(context, BeautifyPageSite.class, params, Framework2.ANIM_TRANSLATION_TOP);
    }

    /**
     * @param params
     * sel_img 图片地址
     */
    public void OnPickPhoto(Context context, HashMap<String, Object> params) {
        MyFramework.SITE_Open(context, AlbumSite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void openCompositionPage(Context context, HashMap<String, Object> params) {
        MyFramework.SITE_Popup(context, CompositionPageSite.class, params, Framework2.ANIM_TRANSLATION_TOP);
    }

    /**
     * @param params
     * url 网络地址
     */
    public void openCameraPermissionsHelper(Context context, HashMap<String, Object> params) {
        MyFramework.SITE_Popup(context, WebViewPageSite.class, params, Framework2.ANIM_TRANSLATION_TOP);
    }

    /**
     * @param params
     * imgs ImageFile2
     */
    public void openBeautyPage(Context context, HashMap<String, Object> params) {
        params.put("from_camera", true);
        MyFramework.SITE_Open(context, BeautifyPageSite.class, params, Framework2.ANIM_TRANSITION);
    }

    public void onDownloadMore(Context context, HashMap<String, Object> params) {
        MyFramework.SITE_Popup(context, ThemePageSite3.class, params, Framework2.ANIM_TRANSLATION_RIGHT);
    }
}
