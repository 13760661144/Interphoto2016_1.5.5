package cn.poco.video.site;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;

import java.util.HashMap;

import cn.poco.framework.AnimatorHolder;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.video.page.VideoPreviewPage;

/**
 * Created by admin on 2018/1/8.
 */

public class VideoPreviewSite extends BaseSite
{
    /**
     * 派生类必须实现一个XXXSite()的构造函数
     */
    public VideoPreviewSite()
    {
        super(SiteID.VIDEO_PREVIEW);
    }

    @Override
    public IPage MakePage(Context context)
    {
        VideoPreviewPage videoPreviewPage = new VideoPreviewPage(context, this);
        return videoPreviewPage;
    }

    public void onBack(Context context)
    {
        onBack(context, null);
    }

    public void onBack(Context context, HashMap<String, Object> params)
    {

//        MyFramework.SITE_Back(context, params, Framework2.ANIM_TRANSITION);
        MyFramework.SITE_Back(context, params, new AnimatorHolder()
        {
            @Override
            public void doAnimation(View oldView, View newView, final AnimatorHolder.AnimatorListener lst)
            {
//                Framework2.AddAnim(oldView, Framework2.MakeTAL(Framework2.ANIM_T_O | Framework2.ANIM_O_R, 1000), newView, Framework2.MakeTAL(Framework2.ANIM_T_N | Framework2.ANIM_O_R, 1000), lst);
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(oldView,"alpha",1f,0f);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(newView,"alpha",0,1f);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(fadeOut,fadeIn);
                animatorSet.setDuration(350);
                animatorSet.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        lst.OnAnimationEnd();
                    }

                    @Override
                    public void onAnimationStart(Animator animation)
                    {
                        lst.OnAnimationStart();
                    }
                });
                animatorSet.start();
            }
        });
    }
}
