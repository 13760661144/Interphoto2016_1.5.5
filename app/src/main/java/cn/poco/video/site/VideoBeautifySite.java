package cn.poco.video.site;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;

import java.util.HashMap;

import cn.poco.MaterialMgr2.site.ThemePageSite4;
import cn.poco.capture2.CapturePage;
import cn.poco.framework.AnimatorHolder;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.tianutils.ShareData;
import cn.poco.video.page.VideoBeautifyPage;
import cn.poco.video.videoAlbum.VideoAlbumUtils;

/**
 * Created by Shine on 2017/5/27.
 */

public class VideoBeautifySite extends BaseSite {

    public VideoBeautifySite() {
        super(SiteID.VIDEO_PROCESS);
    }

    @Override
    public IPage MakePage(Context context) {
        return new VideoBeautifyPage(context, this);
    }

    public void onBack(Context context, HashMap<String, Object> params) {
//        MyFramework.SITE_Back(context, params, Framework2.ANIM_TRANSLATION_LEFT);
        MyFramework.SITE_Back(context, params, new AnimatorHolder()
        {
            @Override
            public void doAnimation(View oldView, final View newView, final AnimatorHolder.AnimatorListener lst)
            {
                ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(oldView,View.TRANSLATION_X, 0 ,ShareData.m_screenWidth);
                objectAnimator1.setDuration(350);
                ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(newView,View.TRANSLATION_X, -ShareData.m_screenWidth, 0);
                objectAnimator1.setDuration(350);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(objectAnimator1,objectAnimator2);
                animatorSet.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        lst.OnAnimationEnd();
                        if(newView instanceof CapturePage){
                            VideoAlbumUtils.enterImmersiveMode(newView.getContext());
                        }
                    }


                    @Override
                    public void onAnimationStart(Animator animation)
                    {
                    }
                });
                animatorSet.start();

            }
        });
    }

    /**
     *
     * @param context
     * @param hasModify  返回VideoAlbumSite恢复现场，返回
     * @param need_restore 返回视频是否需要记住上次拍摄的
     */
    public void onBack(Context context,boolean hasModify, boolean need_restore) {

        if(hasModify)
        {
            MyFramework.SITE_BackTo(context, VideoAlbumSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
        }else{
            HashMap<String, Object> params = new HashMap<>();
            params.put("restore", need_restore);
            onBack(context, params);
        }
    }

    public void onNextStepClick() {

    }

//    public void onSelectMusic(Context context)
//    {
//        MyFramework.SITE_Popup(context,SelectMusicSite.class,null,Framework.ANIM_NONE);
//    }
//
//    public void onEditVideoText(Context context, HashMap<String, Object> data)
//    {
//        MyFramework.SITE_Popup(context,TextEditPageSite.class,data,Framework.ANIM_NONE);
//    }
//
//    public void onDownLoad(Context context, HashMap<String, Object> data)
//    {
//        MyFramework.SITE_Popup(context,TextUnLockPageSite.class,data,Framework.ANIM_TRANSLATION_BOTTOM);
//    }

    public void onVideoAlumb(Context context, HashMap<String, Object> data)
    {
        MyFramework.SITE_Popup(context,VideoAlbumSite3.class,data,Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void openMosaicHelp(Context context, HashMap<String, Object> data)
    {
        MyFramework.SITE_Popup(context,VideoMosaicHelpSite.class,data,Framework2.ANIM_NONE);
    }

    public void openTheme(Context context, HashMap<String, Object> data)
    {
        MyFramework.SITE_Popup(context, ThemePageSite4.class, data, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void onOpenShare(Context context, HashMap<String, Object> data) {
        MyFramework.SITE_Popup(context, SaveVideoSite.class, data, Framework2.ANIM_TRANSITION);
    }


}
