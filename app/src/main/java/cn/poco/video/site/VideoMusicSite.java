package cn.poco.video.site;

import android.content.Context;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;

/**
 * Created by lgd on 2018/1/2.
 */

public class VideoMusicSite extends BaseSite
{
    /**
     * 派生类必须实现一个XXXSite()的构造函数
     *
     */
    public VideoMusicSite()
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
}
