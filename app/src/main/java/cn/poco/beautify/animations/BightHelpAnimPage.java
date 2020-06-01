package cn.poco.beautify.animations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.site.BightHelpAnimPageSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.setting.LanguagePage;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.ShareData;

/**
 * Created by pengdh on 2016/7/13.
 */
public class BightHelpAnimPage extends IPage{
    private BightHelpAnimPageSite mSite;
    private BightAnim mBightAnim;
    private LinearLayout m_titleFr;
    private FrameLayout m_topBar;
    private ImageView m_exitBtn;
    private int m_textSize = 16;
    public BightHelpAnimPage(Context context, BaseSite site) {
        super(context, site);
        if(site != null)
        {
            mSite = (BightHelpAnimPageSite) site;
        }
        initUI();
    }

    /**
     *
     * @param params
     * Bitmap img 背景图片
     */
    @Override
    public void SetData(HashMap<String, Object> params) {
        if(params != null)
        {
            Object o = params.get("img");
            if(o != null)
            {
                Bitmap img = (Bitmap)o;
                this.setBackgroundDrawable(new BitmapDrawable(img));
            }
        }
    }


    public void initUI()
    {
        if(!TagMgr.CheckTag(getContext(), LanguagePage.ENGLISH_TAGVALUE))
        {
            m_textSize = 10;
        }
        this.setBackgroundColor(0xff333333);
//        Bitmap bk4 = BeautifyResMgr.MakeBkBmp(bmp, ShareData.PxToDpi_xhdpi(580), ShareData.PxToDpi_xhdpi(100), 0x99000000, 0xaa000000);
        m_topBar = new FrameLayout(getContext());
//        m_topBar.setBackgroundDrawable(new BitmapDrawable(bk3));
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.light_move_bg);
        Bitmap bk4 = BeautifyResMgr.MakeBkBmp(bmp, ShareData.PxToDpi_xhdpi(580), ShareData.PxToDpi_xhdpi(100), 0x99000000, 0xaa000000);
        if(bmp != null)
        {
            bmp.recycle();
        }

        Bitmap bmp1 = BitmapFactory.decodeResource(getResources(), R.drawable.light_move_bg);
        Bitmap bk1 = BeautifyResMgr.MakeBkBmp(bmp1, ShareData.PxToDpi_xhdpi(580), ShareData.PxToDpi_xhdpi(100), 0xcc383838, 0x1e383838);
        if(bmp != null)
        {
            bmp.recycle();
        }
        m_topBar.setBackgroundDrawable(new BitmapDrawable(bk4));
        FrameLayout.LayoutParams fl = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
        fl.gravity = Gravity.TOP;
        m_topBar.setLayoutParams(fl);
        this.addView(m_topBar);
        {
			/*m_backBtn = new ImageView(getContext());
			m_backBtn.setImageResource(R.drawable.framework_back_btn);
			fl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
			m_backBtn.setLayoutParams(fl);
			m_topBar.addView(m_backBtn);
			m_backBtn.setOnClickListener(m_btnLst);*/

            m_titleFr = new LinearLayout(getContext());
//            m_titleFr.setOnClickListener(m_btnLst);
            m_titleFr.setGravity(Gravity.CENTER);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            fl.gravity = Gravity.CENTER;
            m_titleFr.setLayoutParams(fl);
            m_titleFr.setOnClickListener(m_onclickLisener);
            m_topBar.addView(m_titleFr);
            {
                LinearLayout.LayoutParams ll;
                TextView text = new TextView(getContext());
                text.setTextColor(0xffffffff);
                text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                text.setText(getResources().getString(R.string.Curve));
                ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ll.gravity = Gravity.CENTER_VERTICAL;
                text.setLayoutParams(ll);
                m_titleFr.addView(text);

                ImageView img = new ImageView(getContext());
                img.setImageResource(R.drawable.beautify_effect_help_up);
                ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ll.gravity = Gravity.CENTER_VERTICAL;
                ll.leftMargin = ShareData.PxToDpi_xhdpi(6);
                img.setLayoutParams(ll);
                m_titleFr.addView(img);
            }
        }

        LinearLayout centerLayout = new LinearLayout(getContext());
         fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(540),ShareData.PxToDpi_xhdpi(540));
        centerLayout.setOrientation(LinearLayout.VERTICAL);
        centerLayout.setLayoutParams(fl);
        fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        fl.topMargin = ShareData.PxToDpi_xhdpi(80 + 60);
        BightHelpAnimPage.this.addView(centerLayout);

        mBightAnim = new BightAnim(getContext());
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(580),ShareData.PxToDpi_xhdpi(480));
        mBightAnim.setLayoutParams(ll);
        centerLayout.addView(mBightAnim);

//        FrameLayout textFrame = new FrameLayout(getContext());
//        ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(500),ShareData.PxToDpi_xhdpi(100));
//        textFrame.setLayoutParams(ll);
//        centerLayout.addView(textFrame);
//        textFrame.setBackgroundColor(Color.GRAY);
//
//        TextView text = new TextView(getContext());
//        fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        fl.gravity = Gravity.CENTER_VERTICAL;
//        fl.leftMargin = ShareData.PxToDpi_xhdpi(30);
//        text.setLayoutParams(fl);
//        text.setText("轻点可添加控制点，长按可将它移除");
//        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        textFrame.addView(text);


        TextView text = new TextView(getContext());
        text.setPadding(ShareData.PxToDpi_xhdpi(30), 0, 0, 0);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setBackgroundDrawable(new BitmapDrawable(bk1));
        text.setText(getResources().getString(R.string.tipscontrolPoint));
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, m_textSize);
        text.setTextColor(Color.WHITE);
        ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(580), ShareData.PxToDpi_xhdpi(100));
        text.setLayoutParams(ll);
        centerLayout.addView(text);

        m_exitBtn = new ImageView(getContext());
        m_exitBtn.setImageResource(R.drawable.help_anim_exit_btn);
        fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        fl.bottomMargin = ShareData.PxToDpi_xhdpi(50);
        m_exitBtn.setLayoutParams(fl);
        this.addView(m_exitBtn);
        m_exitBtn.setOnClickListener(m_onclickLisener);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void onBack() {
        mSite.onBack(getContext());
    }


    View.OnClickListener m_onclickLisener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v == m_titleFr || v == m_exitBtn)
            {
                mSite.onBack(getContext());
            }
        }
    };

    @Override
    public void onClose() {
        if(mBightAnim != null)
        {
            mBightAnim.ClearAll();
        }
        this.setBackgroundDrawable(null);
    }
}
