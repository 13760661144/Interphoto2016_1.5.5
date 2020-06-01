package cn.poco.album2.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.beautify.site.BeautifyPageSite2;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 *首页-文字、滤镜、光效
 */

public class AlbumSite4 extends AlbumSite {

    @Override
    public void onSelectPhoto(Context context, HashMap<String, Object> params) {

        params.putAll(m_inParams);
        if(m_inParams != null)
        {
            m_inParams.put("is_back", true);
        }
        MyFramework.SITE_Open(context, BeautifyPageSite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
    }
}
