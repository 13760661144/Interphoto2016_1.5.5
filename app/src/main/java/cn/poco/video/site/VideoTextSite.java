package cn.poco.video.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.MaterialMgr2.site.ThemePageSite4;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * Created by lgd on 2018/1/2.
 */

public class VideoTextSite extends BaseSite
{
    /**
     * 派生类必须实现一个XXXSite()的构造函数
     *
     */
    public VideoTextSite()
    {
        super(-1);
    }

    @Override
    public IPage MakePage(Context context)
    {
        return null;
    }

    public void onBack(Context context)
    {

    }
    public void openTheme(Context context,HashMap<String, Object> data)
    {
        MyFramework.SITE_Popup(context, ThemePageSite4.class, data, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void openTextHelp(Context context, HashMap<String, Object> data)
    {
        MyFramework.SITE_Popup(context,VideoTextHelpSite.class,data,Framework2.ANIM_TRANSLATION_BOTTOM);
    }

}
