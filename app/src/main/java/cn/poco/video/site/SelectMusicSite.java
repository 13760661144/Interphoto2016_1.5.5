package cn.poco.video.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.video.videoMusic.SelectMusicPage;

/**
 * Created by admin on 2017/6/20.
 */

public class SelectMusicSite extends BaseSite {


    public SelectMusicSite() {
        super(SiteID.SELECTMUSIC);
    }


    @Override
    public IPage MakePage(Context context) {
        SelectMusicPage selectMusicFrame1 = new SelectMusicPage(context,this);

        return selectMusicFrame1;
    }
    public  void onBack(HashMap<String ,Object> data,Context context){
        MyFramework.SITE_Back(context, data, Framework2.ANIM_NONE);
        //MyFramework.SITE_BackAndOpen();
    }
}
