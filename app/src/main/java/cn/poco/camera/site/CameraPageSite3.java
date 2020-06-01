package cn.poco.camera.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * 网页-拍照
 */
public class CameraPageSite3 extends CameraPageSite {

    @Override
    public void OnTakePicture(Context context, HashMap<String, Object> params)
    {
        MyFramework.SITE_ClosePopup(context, params, Framework2.ANIM_NONE);
    }
}
