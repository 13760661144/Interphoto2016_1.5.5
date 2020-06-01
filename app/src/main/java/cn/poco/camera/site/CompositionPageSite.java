package cn.poco.camera.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.camera.CompositionPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * Created by zwq on 2016/05/26 17:21.<br/><br/>
 * 拍照--构图页
 */
public class CompositionPageSite extends BaseSite {

    private static final String TAG = CompositionPageSite.class.getName();

    public CompositionPageSite() {
        super(SiteID.CAMERA_COMPOSITION_PAGE);
    }

    @Override
    public IPage MakePage(Context context) {
        return new CompositionPage(context, this);
    }

    public void onBack(Context context, HashMap<String, Object> params) {
        MyFramework.SITE_Back(context, params, Framework2.ANIM_NONE);
    }

}
