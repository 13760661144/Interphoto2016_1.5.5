package cn.poco.video.videotext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.HashMap;

import cn.poco.beautify.BeautifyResMgr;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.video.site.VideoTextHelpSite;

/**
 * Created by lgd on 2018/1/18.
 */

public class VideoTextHelpPage extends IPage
{
    private String animPath;
    private VideoTextHelpSite mSite;
    protected VideoView m_anim1;
    private boolean mCanBack = true;
    private ImageView mMask;
    public VideoTextHelpPage(Context context, BaseSite site)
    {
        super(context, site);
        mSite = (VideoTextHelpSite) site;
        init();
    }

    private void init()
    {
        LayoutParams fl;
//        this.setBackgroundColor(0xff333333);
        this.setBackgroundColor(0xff0e0e0e);

        String packageName = getContext().getPackageName();
        animPath = "android.resource://" + packageName + "/" + R.raw.video_text_edit_help;

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.filter_anim1_pic1);
        Bitmap bk1 = BeautifyResMgr.MakeBkBmp(bmp, ShareData.PxToDpi_xhdpi(580), ShareData.PxToDpi_xhdpi(100), 0x99000000, 0xaa000000);
        if(bmp != null)
        {
            bmp.recycle();
        }


        ImageView back = new ImageView(getContext());
        back.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBack();
            }
        });
        back.setImageResource(R.drawable.help_anim_exit_btn);
        fl = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        fl.bottomMargin = ShareData.PxToDpi_xhdpi(50);
        back.setLayoutParams(fl);
        addView(back);

        FrameLayout m_topBar = new FrameLayout(getContext());
        m_topBar.setBackgroundDrawable(new BitmapDrawable(bk1));
//        m_topBar.setBackgroundColor(Color.BLACK);
        fl = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
        fl.gravity = Gravity.TOP;
        m_topBar.setLayoutParams(fl);
        this.addView(m_topBar);
        {
            LinearLayout m_titleFr = new LinearLayout(getContext());
            m_titleFr.setGravity(Gravity.CENTER);
            fl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            fl.gravity = Gravity.CENTER;
            m_titleFr.setLayoutParams(fl);
            m_topBar.addView(m_titleFr);
            {
                LinearLayout.LayoutParams ll;

                TextView  m_title = new TextView(getContext());
                m_title.setTextColor(0xffffffff);
                m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                m_title.setText(getResources().getString(R.string.video_text));
                ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ll.gravity = Gravity.CENTER_VERTICAL;
                m_title.setLayoutParams(ll);
                m_titleFr.addView(m_title);

                ImageView img = new ImageView(getContext());
                img.setImageResource(R.drawable.beautify_effect_help_up);
                ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ll.gravity = Gravity.CENTER_VERTICAL;
                ll.leftMargin = ShareData.PxToDpi_xhdpi(6);
                img.setLayoutParams(ll);
                m_titleFr.addView(img);
            }
        }

        LinearLayout lin = new LinearLayout(getContext());
        lin.setMinimumHeight(ShareData.PxToDpi_xhdpi(580));
        lin.setBackgroundColor(Color.BLACK);
        lin.setOrientation(LinearLayout.VERTICAL);
        fl = new LayoutParams(ShareData.PxToDpi_xhdpi(580),ShareData.PxToDpi_xhdpi(580));
        fl.topMargin = ShareData.PxToDpi_xhdpi(140);
        fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lin.setLayoutParams(fl);
        addView(lin);
        {
            LinearLayout.LayoutParams ll;
            m_anim1 = new VideoView(getContext())
            {
                @Override
                public boolean onTouchEvent(MotionEvent ev)
                {
                    return true;
                }
            };
            ll = new  LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            ll.gravity = Gravity.TOP;
            m_anim1.setLayoutParams(ll);
            lin.addView(m_anim1);

            setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onBack();
                }
            });
            m_anim1.setVideoURI(Uri.parse(animPath));
            m_anim1.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    m_anim1.start();
                }
            });

            TextView text = new TextView(getContext());
            text.setPadding(ShareData.PxToDpi_xhdpi(30), 0, 0, 0);
            text.setGravity(Gravity.CENTER_VERTICAL);
            text.setBackgroundDrawable(new BitmapDrawable(bk1));
            text.setText(getResources().getString(R.string.video_text_help));
            text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            text.setTextColor(Color.WHITE);
            ll = new  LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
            text.setLayoutParams(ll);
            lin.addView(text);
        }
    }

    @Override
    public void SetData(HashMap<String, Object> params)
    {
//        DoAnim();
        postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                m_anim1.start();
            }
        },350);
    }


    @Override
    public void onResume()
    {
        m_anim1.start();
    }

    private void DoAnim(){
        setTranslationY(-ShareData.m_screenHeight);
        this.animate().translationY(0).setDuration(350).setListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                mCanBack = true;
                m_anim1.setVideoURI(Uri.parse(animPath));
                m_anim1.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        m_anim1.start();
                    }
                });
                m_anim1.start();
            }

            @Override
            public void onAnimationStart(Animator animation)
            {
                mCanBack = false;
            }
        });
    }

    private void closeAnim()
    {
        this.animate().translationY(-ShareData.m_screenHeight).setDuration(350).setListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                mCanBack = true;
                mSite.onBack(getContext());
            }

            @Override
            public void onAnimationStart(Animator animation)
            {
                mCanBack = false;
            }
        });
    }


    @Override
    public void onBack()
    {
        if(mCanBack)
        {
//            closeAnim();
            mSite.onBack(getContext());
        }
    }

    @Override
    public void onClose()
    {
        if(m_anim1 != null)
        {
            m_anim1.stopPlayback();
            m_anim1.setVisibility(GONE);
            m_anim1 = null;
        }
    }
}
