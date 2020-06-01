package cn.poco.video.videoFrame;

import android.content.Context;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;

/**
 * Created by lgd on 2018/1/18.
 */

public class VideoFrameAdjustSite extends BaseSite
{
    public VideoFrameAdjustSite()
    {
        super(-1);
    }

    @Override
    public IPage MakePage(Context context)
    {
        return new VideoFrameAdjustPage(context,this,null);
    }

    public void onBack(Context context)
    {

    }
}
