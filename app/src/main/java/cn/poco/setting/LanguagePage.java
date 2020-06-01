package cn.poco.setting;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.home.site.HomePageSite;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.setting.site.LanguagePageSite;
import cn.poco.setting.site.SettingPageSite;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.ShareData;

/**
 * Created by pengdh on 2016/9/9.
 */
public class LanguagePage extends IPage {

    public static final int AUTO = 1;
    public static final int CHINA = 2;
    public static final int ENGLISH = 3;
    public static final String ENGLISH_TAGVALUE = "about_EnglishLanguage";
    public static final String CHINA_TAGVALUE = "about_ChinaLanguage";
    private LanguagePageSite m_site;
    private LinearLayout m_main;
    private FrameLayout m_autoItem;
    private FrameLayout m_chinaItem;
    private FrameLayout m_englishItem;
    private FrameLayout m_topBar;
    private ImageView mBtnCancel;
    private int m_curValue;

    public LanguagePage(Context context, BaseSite site) {
        super(context, site);
        m_site = (LanguagePageSite) site;
        checkTag();
        initUI();
    }

    @Override
    public void SetData(HashMap<String, Object> params) {

    }

    private void initUI() {
        setBackgroundColor(0xff0e0e0e);
        m_topBar = new FrameLayout(getContext());
        m_topBar.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
        fl.gravity = Gravity.TOP | Gravity.LEFT;
        m_topBar.setLayoutParams(fl);
        this.addView(m_topBar);

        mBtnCancel = new ImageView(getContext());
        mBtnCancel.setImageResource(R.drawable.framework_back_btn);
        fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        m_topBar.addView(mBtnCancel, fl);
        mBtnCancel.setOnClickListener(m_onclickLisener);

        TextView title = new TextView(getContext());
        fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.CENTER;
        title.setLayoutParams(fl);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        title.setTextColor(Color.WHITE);
        title.setText(getResources().getString(R.string.setting_language));
        m_topBar.addView(title);

        m_main = new LinearLayout(getContext());
        fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.LEFT | Gravity.TOP;
        fl.topMargin = ShareData.PxToDpi_xhdpi(80);
        m_main.setLayoutParams(fl);
        m_main.setOrientation(LinearLayout.VERTICAL);
        m_main.setBackgroundColor(0xff1a1a1a);
        this.addView(m_main);

        m_autoItem = addItem(getResources().getString(R.string.autoLanguage),1);
        m_autoItem.setOnClickListener(m_onclickLisener);
        addLine();
        m_chinaItem = addItem(getResources().getString(R.string.chinaLanguage),2);
        m_chinaItem.setOnClickListener(m_onclickLisener);
        addLine();
        m_englishItem = addItem(getResources().getString(R.string.englishLanguage),3);
        m_englishItem.setOnClickListener(m_onclickLisener);

    }

    private FrameLayout addItem(String str,int value) {
        FrameLayout out;
        out = new FrameLayout(getContext());
//        out.setBackgroundColor(0xff1a1a1a);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(108));
        out.setLayoutParams(ll);
        m_main.addView(out);
        TextView textView = new TextView(getContext());
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        fl.leftMargin = ShareData.PxToDpi_xhdpi(30);
        textView.setLayoutParams(fl);
        textView.setText(str);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        out.addView(textView);

        if(value == m_curValue)
        {
            ImageView m_okBtn = new ImageView(getContext());
            m_okBtn.setImageResource(R.drawable.framework_ok_btn);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            m_okBtn.setLayoutParams(fl);
            out.addView(m_okBtn);
        }
        return out;
    }

    private void addLine()
    {
        ImageView img = new ImageView(getContext());
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(20),ShareData.PxToDpi_xhdpi(1));
        ll.leftMargin = ShareData.PxToDpi_xhdpi(30);
        img.setLayoutParams(ll);
        img.setBackgroundColor(0xff333333);
        m_main.addView(img);
    }

    View.OnClickListener m_onclickLisener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == m_englishItem) {
                if(m_curValue != ENGLISH)
                {
                    changeLanguage(ENGLISH,getResources());
                    refresh(ENGLISH);
                }
            } else if (v == m_chinaItem) {
                if(m_curValue != CHINA)
                {
                    changeLanguage(CHINA,getResources());
                    refresh(CHINA);
                }
            } else if (v == m_autoItem) {
                if(m_curValue != AUTO)
                {
                    changeLanguage(AUTO,getResources());
                    refresh(AUTO);
                }
            }
            else if(v == mBtnCancel)
            {
                m_site.onBack(getContext());
            }
        }
    };



    private void refresh(int value)
    {
        save(value,getContext());
        MyFramework.SITE_ClosePopup(getContext(), null, Framework2.ANIM_NONE);

        HashMap<String,Object> params = new HashMap<>();
        params.put("isLanguageBack",true);
        MyFramework.SITE_Open(getContext(), HomePageSite.class, params, Framework2.ANIM_NONE);

        MyFramework.SITE_Popup(getContext(), SettingPageSite.class,null,Framework2.ANIM_NONE);
        MyFramework.SITE_Popup(getContext(),LanguagePageSite.class,null,Framework2.ANIM_NONE);
    }

    public static void changeLanguage(int language,Resources resources) {
                     // 获得res资源对象
        Configuration config = resources.getConfiguration();     // 获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();       // 获得屏幕参数：主要是分辨率，像素等。
        switch (language) {
            case 2:
                config.locale = Locale.CHINA;     // 中文
                break;
            case 3:
                config.locale = Locale.ENGLISH;   // 英文
                break;
            case 1:
                config.locale = Locale.getDefault();         // 系统默认语言
                break;
            default:
                break;
        }
        resources.updateConfiguration(config, dm);

//        m_site.onBack();
//        MyFramework.SITE_Back(PocoCamera.main,null,MyFramework.ANIM_NONE);
//        ((Activity) getContext()).finish();
//        Intent intent = new Intent();
//        intent.setClass(getContext(), PocoCamera.class);
//        getContext().startActivity(intent);
    }

    private static void save(int value,Context context)
    {
        switch (value)
        {
            case 1:
                resetTag(context);
                break;
            case 2:
                resetTag(context);
                TagMgr.SetTag(context,CHINA_TAGVALUE);
                break;
            case 3:
                resetTag(context);
                TagMgr.SetTag(context,ENGLISH_TAGVALUE);
                break;
        }
    }

    private void checkTag()
    {
        if(!TagMgr.CheckTag(getContext(),CHINA_TAGVALUE))
        {
            m_curValue = CHINA;
        }
        else if(!TagMgr.CheckTag(getContext(),ENGLISH_TAGVALUE))
        {
            m_curValue = ENGLISH;
        }
        else
        {
            m_curValue = AUTO;
        }

    }

    private static void resetTag(Context context)
    {
        if(!TagMgr.CheckTag(context,CHINA_TAGVALUE))
        {
            TagMgr.ResetTag(context,CHINA_TAGVALUE);
        }
        if(!TagMgr.CheckTag(context,ENGLISH_TAGVALUE))
        {
            TagMgr.ResetTag(context,ENGLISH_TAGVALUE);
        }
    }

    @Override
    public void onBack() {
        m_site.onBack(getContext());
    }
}
