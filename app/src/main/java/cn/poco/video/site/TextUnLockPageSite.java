package cn.poco.video.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.video.videotext.DownloadResPage;

/**
 * Created by admin on 2017/6/7.
 */
@Deprecated
public class TextUnLockPageSite extends BaseSite {
    public TextUnLockPageSite() {
        super(SiteID.TEXT_UNLOCK);
    }

    @Override
    public IPage MakePage(Context context) {

        DownloadResPage downloadPage = new DownloadResPage(context, this);
        return downloadPage;
    }

    public void onBack(Context context) {
        MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_TOP);
    }

    public void onFinish(Context context, HashMap<String, Object> data) {
        MyFramework.SITE_Back(context, data, Framework2.ANIM_TRANSLATION_TOP);
    }

    public void onBack(Context context, HashMap<String, Object> data) {
        MyFramework.SITE_Back(context, data, Framework2.ANIM_TRANSLATION_TOP);
    }

}
