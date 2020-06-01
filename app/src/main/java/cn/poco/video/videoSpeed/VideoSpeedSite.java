package cn.poco.video.videoSpeed;

import android.content.Context;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;

/**
 * Created by lgd on 2018/1/18.
 */

public class VideoSpeedSite extends BaseSite
{
    public VideoSpeedSite()
    {
        super(-1);
    }

    @Override
    public IPage MakePage(Context context)
    {
        return new VideoSpeedPage(context,this,null);
    }

    public void onBack(Context context,boolean isRefresh)
    {

    }
}
