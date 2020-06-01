package cn.poco.album2.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 *网页-选图
 */

public class AlbumSite5 extends AlbumSite {

    @Override
    public void onSelectPhoto(Context context, HashMap<String, Object> params) {
        MyFramework.SITE_ClosePopup(context, params, Framework2.ANIM_NONE);
    }
}
