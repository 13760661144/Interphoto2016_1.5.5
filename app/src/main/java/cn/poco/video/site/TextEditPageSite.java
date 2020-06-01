package cn.poco.video.site;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.video.videotext.TextEditPage;

/**
 * Created by admin on 2017/6/5.
 */

public class TextEditPageSite extends BaseSite{

    /**
     * 派生类必须实现一个XXXSite()的构造函数
     *
     */
    public TextEditPageSite() {
        super(SiteID.TEXTEDIT);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public IPage MakePage(Context context) {
        TextEditPage textEditPage = new TextEditPage(context,this);
        return textEditPage;
    }
    public void onBack(Context context){
        MyFramework.SITE_Back(context, null, Framework2.ANIM_NONE);
    }

    public void onSave(HashMap<String ,Object> data,Context context)
    {
        MyFramework.SITE_Back(context, data, Framework2.ANIM_NONE);
    }

}
