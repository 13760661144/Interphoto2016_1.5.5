package cn.poco.album2.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.MaterialMgr2.site.ThemeIntroPageSite;
import cn.poco.beautify.site.BeautifyPageSite;
import cn.poco.beautify.site.BeautifyPageSite2;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 *素材中心-马上使用-美化
 */

public class AlbumSite3 extends AlbumSite {

    @Override
    public void onSelectPhoto(Context context, HashMap<String, Object> params) {

        params.put(BeautifyPageSite.DEF_OPEN_PAGE, m_inParams.get(ThemeIntroPageSite.TYPE));
        params.put(BeautifyPageSite.DEF_SEL_URI, m_inParams.get(ThemeIntroPageSite.ID));
        m_myParams.remove(ThemeIntroPageSite.TYPE);
        m_myParams.remove(ThemeIntroPageSite.ID);
        MyFramework.SITE_Open(context, BeautifyPageSite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
    }
}
