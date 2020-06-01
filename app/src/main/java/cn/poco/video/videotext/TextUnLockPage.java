package cn.poco.video.videotext;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseResp;

import java.util.HashMap;

import cn.poco.beautify.RecomDisplayMgr;
import cn.poco.beautify.ScrollShareFr;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework2App;
import cn.poco.interphoto2.R;
import cn.poco.resource.LockRes;
import cn.poco.resource.VideoTextRes;
import cn.poco.share.SendWXAPI;
import cn.poco.share.SharePage;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.MyImageLoader;
import cn.poco.utils.ScaleEvaluator;
import cn.poco.utils.Utils;
import cn.poco.video.site.TextUnLockPageSite;

import static cn.poco.system.Tags.VIDEOTEXT_UNLOCK;

/**
 * Created by lgd on 2017/7/19.
 */
@Deprecated
public class TextUnLockPage extends IPage {
    private static final String TAG = "水印解锁";
    private TextUnLockPageSite m_site;
    private ScrollShareFr m_mainFr;
    protected ImageView m_stateImg;
    protected TextView m_stateText;
    private VideoTextRes m_res;
    private ImageView m_backBtn;
    private ImageView m_shareBtn;
    private int m_headW;
    private int m_headH;
    protected int m_shareFrHeight;

    protected boolean m_exit = false;
    private boolean m_hasAnim = false;
    private int m_centerX;
    private int m_centerY;
    private int m_viewH;
    private int m_viewW;
    private ImageView m_animView;
    private boolean m_uiEnabled = true;
    private LinearLayout m_stateBtn;
    private TextView m_text1;
    private TextView m_text2;
    private String m_unlockTag;
    private boolean m_lock = true;
    private boolean m_isUse = false;
    private ImageView m_logo;

    public TextUnLockPage(Context context, BaseSite site) {
        super(context, site);
        m_site = (TextUnLockPageSite) site;

        m_shareFrHeight = ShareData.PxToDpi_xhdpi(242);
        m_headW = ShareData.PxToDpi_xhdpi(120);
        m_headH = m_headW;
        InitUI();

        TongJiUtils.onPageStart(getContext(), TAG);
    }

    /**
     * data ThemeItemInfo
     *
     * @param params
     */
    @Override
    public void SetData(HashMap<String, Object> params) {
        if (params != null) {
            Object obj;
            obj = params.get("res");
            if (obj != null) {
                m_res = (VideoTextRes) obj;
                Bitmap bmp = MyImageLoader.MakeBmp2(getContext(), m_res.m_coverPic,
                        ShareData.m_screenWidth, ShareData.m_screenWidth);
                m_animView.setImageBitmap(bmp);
                m_logo.setImageBitmap(bmp);
                if(m_res.lockType == LockRes.SHARE_TYPE_MARKET){
                    m_stateText.setText(getResources().getString(R.string.unlockVideoTextTips));
                }else{
                    m_stateText.setText(getResources().getString(R.string.shareToUnlockVideoTextTips));
                }
                m_text1.setText(m_res.m_name);
                String detail = m_res.shareIntroduce.replaceAll("&lt;br rel=auto&gt;", "\n");
//                String detail = "  "+m_res.shareIntroduce;
                m_text2.setText(detail);
//                PocoCamera.s_downloader.DownloadRes(m_res, false, null);
            }

            obj = params.get("hasAnim");
            if (obj != null) {
                m_hasAnim = (boolean) obj;
            }
            obj = params.get("centerX");
            if (obj != null) {
                m_centerX = (Integer) obj;
            }
            obj = params.get("centerY");
            if (obj != null) {
                m_centerY = (Integer) obj;
            }
            obj = params.get("viewH");
            if (obj != null) {
                m_viewH = (Integer) obj;
            }
            m_viewW = ShareData.m_screenWidth;
            obj = params.get("viewW");
            if (obj != null) {
                m_viewW = (Integer) obj;
            }
        }
        if (m_hasAnim) {
            m_uiEnabled = true;
            startAnim();
        }
    }



    private void InitUI() {
        FrameLayout.LayoutParams fl;
        m_mainFr = new ScrollShareFr(this, m_shareFrHeight);
        m_mainFr.GetMainFr().setBackgroundColor(0xff0e0e0e);

        m_shareBtn = new ImageView(getContext());
        m_shareBtn.setVisibility(View.GONE);
        m_shareBtn.setImageResource(R.drawable.beauty_master_filter_tip_share_btn);
        m_shareBtn.setOnClickListener(m_btnListener);
        fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.TOP | Gravity.RIGHT;
        fl.topMargin = ShareData.PxToDpi_xhdpi(30);
        fl.rightMargin = fl.topMargin;
        m_shareBtn.setLayoutParams(fl);
        m_mainFr.AddMainChild(m_shareBtn);

        m_stateBtn = new LinearLayout(getContext());
//		m_stateBtn.setVisibility(View.GONE);
        m_stateBtn.setGravity(Gravity.CENTER);
//		m_stateBtn.setBackgroundColor(0xffffc433);
        fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(98));
        fl.gravity = Gravity.BOTTOM;
        m_stateBtn.setLayoutParams(fl);
        m_stateBtn.setOnClickListener(m_btnListener);
        m_stateBtn.setBackgroundColor(0xffffc433);
        m_mainFr.AddMainChild(m_stateBtn);
        {
            m_stateImg = new ImageView(getContext());
            m_stateImg.setImageResource(R.drawable.master_share_friend);
            LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            m_stateImg.setLayoutParams(ll);
            m_stateBtn.addView(m_stateImg);

            m_stateText = new TextView(getContext());
            m_stateText.setTextColor(0xffffffff);
            m_stateText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            TextPaint tp = m_stateText.getPaint();
            tp.setFakeBoldText(true);
            m_stateText.setText(getResources().getString(R.string.mgr_unlock));
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.leftMargin = ShareData.PxToDpi_xhdpi(20);
            m_stateText.setLayoutParams(ll);
            m_stateBtn.addView(m_stateText);
        }

        m_logo = new ImageView(getContext());
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.m_screenWidth);
        fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        m_logo.setLayoutParams(fl);
        m_mainFr.AddMainChild(m_logo);

        m_text1 = new TextView(getContext());
        m_text1.setTextColor(0xffffffff);
        m_text1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        fl.gravity = Gravity.CENTER_HORIZONTAL;
        fl.leftMargin = ShareData.PxToDpi_xhdpi(50);
        fl.topMargin = ShareData.PxToDpi_xhdpi(40)+ShareData.m_screenWidth;
        m_text1.setLayoutParams(fl);
        m_mainFr.AddMainChild(m_text1);

        m_text2 = new TextView(getContext());
        m_text2.setPadding(ShareData.PxToDpi_xhdpi(50), 0, ShareData.PxToDpi_xhdpi(50), 0);
        m_text2.setTextColor(0xffaaaaaa);
        m_text2.setLineSpacing(1.0f, 1.3f);
        m_text2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        fl.gravity = Gravity.CENTER_HORIZONTAL;
        fl.topMargin = ShareData.PxToDpi_xhdpi(120)+ShareData.m_screenWidth;
        m_text2.setLayoutParams(fl);
        m_mainFr.AddMainChild(m_text2);

        m_backBtn = new ImageView(getContext());
        m_backBtn.setImageResource(R.drawable.beauty_master_filter_tip_back_btn);
        m_backBtn.setOnClickListener(m_btnListener);
        fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.TOP | Gravity.LEFT;
        fl.topMargin = ShareData.PxToDpi_xhdpi(30);
        fl.leftMargin = fl.topMargin;
        m_backBtn.setLayoutParams(fl);
        m_mainFr.AddMainChild(m_backBtn);

        m_animView = new ImageView(getContext());
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.m_screenWidth);
        fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        m_animView.setLayoutParams(fl);
        this.addView(m_animView);
    }

    private void startAnim() {
        if (!m_uiEnabled)
            return;
        m_uiEnabled = false;
        m_animView.setScaleType(ImageView.ScaleType.CENTER);

        m_animView.setVisibility(VISIBLE);
        m_animView.clearAnimation();
        m_mainFr.GetMainFr().setAlpha(0);

        int w = ShareData.m_screenWidth;
        int endW = w;
        if (m_viewW < w) {
            endW = w + 50;
            m_animView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        ValueAnimator scale;
        if (endW > w) {
            scale = ValueAnimator.ofObject(new ScaleEvaluator(), new Point(m_viewW, m_viewH),
                    new Point(endW, endW), new Point(w, w));
            scale.setDuration(550);
        } else {
            scale = ValueAnimator.ofObject(new ScaleEvaluator(), new Point(m_viewW, m_viewH),
                    new Point(endW, endW));
            scale.setDuration(350);
        }
        scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) m_animView.getLayoutParams();
                params.height = point.y;
                params.width = point.x;
                m_animView.setLayoutParams(params);
            }
        });

        int centerX = m_centerX - (ShareData.m_screenWidth - m_viewW) / 2;
        ValueAnimator trans;
        if (endW > w) {
            trans = ValueAnimator.ofObject(new ScaleEvaluator(),
                    new Point(centerX, m_centerY), new Point(0, -50), new Point(0, 0));
        } else {
            trans = ValueAnimator.ofObject(new ScaleEvaluator(),
                    new Point(centerX, m_centerY), new Point(0, 0));
        }
        trans.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) m_animView.getLayoutParams();
                params.topMargin = point.y;
                params.leftMargin = point.x;
                m_animView.setLayoutParams(params);
            }
        });
        trans.setDuration(350);

        ValueAnimator alpha = ValueAnimator.ofFloat(0, 1);
        alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatorValue = Float.valueOf(animation.getAnimatedValue() + "");
                m_mainFr.GetMainFr().setAlpha(animatorValue);
            }
        });
        alpha.setStartDelay(200);
        alpha.setDuration(150);

        AnimatorSet as = new AnimatorSet();
        as.setInterpolator(new DecelerateInterpolator());
        as.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                m_animView.clearAnimation();
                m_animView.setVisibility(GONE);
                m_uiEnabled = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        as.play(scale).with(trans);
        as.play(trans).with(alpha);
        as.start();
    }
    private void closeAnim() {
        if (!m_uiEnabled)
            return;
        m_uiEnabled = false;
        m_animView.setVisibility(VISIBLE);
        m_animView.clearAnimation();
        m_mainFr.GetMainFr().setVisibility(GONE);

        ValueAnimator scale = ValueAnimator.ofObject(new ScaleEvaluator(), new Point(ShareData.m_screenWidth, ShareData.m_screenWidth), new Point(m_viewW, m_viewH));
        scale.setInterpolator(new DecelerateInterpolator());
        scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) m_animView.getLayoutParams();
                params.height = point.y;
                params.width = point.x;
                m_animView.setLayoutParams(params);
            }
        });
        scale.setDuration(350);

        int centerX = m_centerX - (ShareData.m_screenWidth - m_viewW) / 2;
        ValueAnimator trans = ValueAnimator.ofObject(new ScaleEvaluator(), new Point(0, 0),
                new Point(centerX, m_centerY));
        trans.setInterpolator(new DecelerateInterpolator());
        trans.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) m_animView.getLayoutParams();
                params.topMargin = point.y;
                params.leftMargin = point.x;
                m_animView.setLayoutParams(params);
            }
        });
        trans.setDuration(350);

        ValueAnimator alpha = ValueAnimator.ofFloat(1, 0);
        alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatorValue = Float.valueOf(animation.getAnimatedValue() + "");
                m_animView.setAlpha(animatorValue);
            }
        });
        alpha.setDuration(100);
        alpha.setStartDelay(250);

        AnimatorSet as = new AnimatorSet();
        as.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                m_animView.clearAnimation();
                onBack1();
                TextUnLockPage.this.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        as.play(scale).with(trans).with(alpha);
        as.start();
    }
    private View.OnClickListener m_btnListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == m_backBtn) {
                onBack();
            } else if (v == m_stateBtn) {
                if(m_lock) {
                    onUnLock();
                }else{
                    m_isUse = true;
                    onBack();
                }
            }
        }
    };

    private void onUnLock() {
        if(m_res.lockType == LockRes.SHARE_TYPE_MARKET) {
            OpenMarket();
            m_lock = false;
            TagMgr.SetTag(getContext(), VIDEOTEXT_UNLOCK + m_res.m_id);
            return;
        }else{
            WeixinUnLock();
        }
    }
    private void WeixinUnLock()
    {
        String shareContent = null;
        Object shareImg = null;
        String shareLink = null;
        shareContent = m_res.shareTitle;
        shareLink = m_res.m_shareLink;
        shareImg = m_res.shareImg;
        SharePage.unlockResourceByWeiXin(getContext(), shareContent, shareContent, shareLink, RecomDisplayMgr.MakeWXLogo(Utils.MakeLogo(getContext(), shareImg)), false, new SendWXAPI.WXCallListener() {
            @Override
            public void onCallFinish(int result) {
                if (result != BaseResp.ErrCode.ERR_USER_CANCEL) {
                    TagMgr.SetTag(MyFramework2App.getInstance().getApplicationContext(), VIDEOTEXT_UNLOCK + m_res.m_id);
                    m_lock = false;
//                    if (shareLink != null && shareLink.length() > 0) {
                        m_stateImg.setVisibility(View.GONE);
                        m_stateText.setText(R.string.use_now);
//                    }
                    Toast.makeText(getContext(), getResources().getString(R.string.UnlockSuccessful), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void OpenMarket()
    {
        try
        {
            Uri uri = Uri.parse("market://details?id=" + getContext().getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
        catch(Throwable e)
        {
            Toast.makeText(getContext(), "还没有安装安卓市场，请先安装", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }



    protected void AddItem(LinearLayout parent, View child, int width) {
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
        ll.gravity = Gravity.CENTER_VERTICAL;
        child.setLayoutParams(ll);
        parent.addView(child);
        child.setOnClickListener(m_btnListener);
    }

    @Override
    public void onBack() {
        if (m_mainFr != null && m_mainFr.IsTopBarShowing()) {
            m_mainFr.ShowTopBar(false);
            return;
        }
        if (m_hasAnim) {
            closeAnim();
        } else {
            onBack1();
        }
    }

    private void onBack1() {
//        HashMap<String, Object> params = new HashMap<>();
//        params.put("need_refresh", m_needRefresh);
//        if (m_pageCB != null) {
//            m_pageCB.onClose(params);
//        } else {
//            m_site.OnBack(params);
//        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", m_res.m_id);
        params.put("lock", m_lock);
        params.put("isUse", m_isUse);
        m_site.onBack(getContext(), params);
    }

    @Override
    public void onClose() {
        m_exit = true;
        TongJiUtils.onPageEnd(getContext(), TAG);
    }

    @Override
    public void onResume() {
        if(!m_lock){
            m_stateImg.setVisibility(View.GONE);
            m_stateText.setText(R.string.use_now);
        }
        TongJiUtils.onPageResume(getContext(), TAG);
        super.onResume();
    }

    @Override
    public void onPause() {
        TongJiUtils.onPagePause(getContext(), TAG);
        super.onPause();
    }
}
