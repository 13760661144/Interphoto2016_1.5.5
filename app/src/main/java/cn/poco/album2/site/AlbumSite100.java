package cn.poco.album2.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.beautify.site.BeautifyPageSite100;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 *第三方调用
 */

public class AlbumSite100 extends AlbumSite {

    @Override
    public void onSelectPhoto(Context context, HashMap<String, Object> params) {

        params.put("other_call", true);
        MyFramework.CopyExternalCallParams(m_inParams, params);
        MyFramework.SITE_Open(context, BeautifyPageSite100.class, params, Framework2.ANIM_TRANSLATION_LEFT);
    }
}
