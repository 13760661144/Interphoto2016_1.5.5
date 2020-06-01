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
import cn.poco.framework2.Framework2;
import cn.poco.video.videoAlbum.VideoAlbumPage;

/**
 * Created by Shine on 2017/5/27.
 */

public class VideoAlbumSite extends BaseSite{

    public VideoAlbumSite() {
        super(SiteID.VIDEO_ALBUM);
    }

    @Override
    public IPage MakePage(Context context) {
        VideoAlbumPage videoAlbumPage = new VideoAlbumPage(context, this);
        return videoAlbumPage;
    }
/*
    public void onClickVideo(Context context, MediaEntry mediaEntry) {
       *//* HashMap<String, Object> params = new HashMap<>();
        params.put("data", mediaEntry);
        MyFramework.SITE_Open(context, VideoBeautifySite.class, params, Framework2.ANIM_TRANSLATION_LEFT);*//*

    }*/
    public void onClickVideo(Context context,HashMap<String, Object> params) {
//        MyFramework.SITE_Open(context, VideoBeautifySite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
//        MyFramework.SITE_Popup(context, VideoPreviewSite.class, params, Framework2.ANIM_TRANSITION);
        MyFramework.SITE_Popup(context, VideoPreviewSite.class, params, new AnimatorHolder()
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


    public void onVideoSetting(Context context,HashMap<String, Object> params)
    {
//        if(m_inParams != null)
//        {
//            if (m_inParams.containsKey("watermark_type"))
//            {
//                params.put("watermark_type", m_inParams.get("watermark_type"));
//                m_inParams.remove("watermark_type");
//            }
//            if (m_inParams.containsKey("watermark_id"))
//            {
//                params.put("watermark_id", m_inParams.get("watermark_id"));
//                m_inParams.remove("watermark_id");
//            }
//        }
        if(m_inParams != null){
            params.putAll(m_inParams);
        }
        MyFramework.SITE_Open(context, VideoSettingSite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
//        MyFramework.SITE_Open(context, VideoBeautifySite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
    }


//    public void onVideoBeautify(Context context, ArrayList<VideoEntry> videos,int ratio) {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("videos",videos);
//        params.put("ratio",ratio);
//        if(m_inParams != null)
//        {
//            if (m_inParams.containsKey("watermark_type"))
//            {
//                params.put("watermark_type", m_inParams.get("watermark_type"));
//                m_inParams.remove("watermark_type");
//            }
//            if (m_inParams.containsKey("watermark_id"))
//            {
//                params.put("watermark_id", m_inParams.get("watermark_id"));
//                m_inParams.remove("watermark_id");
//            }
//        }
//        MyFramework.SITE_Open(context, VideoBeautifySite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
//    }


    public void onBack(Context context) {
        MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
    }



}
