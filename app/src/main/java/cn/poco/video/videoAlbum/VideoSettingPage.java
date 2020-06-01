package cn.poco.video.videoAlbum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.ShareData;
import cn.poco.video.model.VideoEntry;
import cn.poco.video.render.PlayRatio;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.site.VideoSettingSite;
import cn.poco.video.view.ActionBar;

/**
 * Created by lgd on 2018/1/5.
 */

public class VideoSettingPage extends IPage
{
    private LinearLayout mLinearLayout;
//    private Bitmap mMaskBp;
    private boolean mIs10sMode = false;
    private int mCurRatio = PlayRatio.RATIO_1_1;
    private LinearLayout mode10sParent;
    private LinearLayout modeFreeParent;
    private TextView frame9_16;
    private TextView frame16_9;
    private TextView frame235_1;
    private TextView frame1_1;
    private TextView mConfirm;
    private VideoSettingSite mSite;
    public VideoSettingPage(@NonNull Context context, BaseSite site)
    {
        super(context,site);
        mSite = (VideoSettingSite) site;
        init();
    }

    private void init()
    {
        setBackgroundColor(0xff0e0e0e);

        LayoutParams fl;
        LinearLayout.LayoutParams ll;
        ActionBar mActionBar = new ActionBar(getContext());

        mActionBar.setBackgroundColor(Color.BLACK);
        mActionBar.setUpLeftImageBtn(R.drawable.framework_back_btn);
        mActionBar.setUpActionbarTitle(getResources().getString(R.string.video_setting), Color.WHITE,16f);
        mActionBar.getLeftImageBtn().setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBack();
            }
        });
        ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
        addView(mActionBar,ll);

        mLinearLayout = new LinearLayout(getContext());
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setPadding(ShareData.PxToDpi_xhdpi(39),0,ShareData.PxToDpi_xhdpi(39),0);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.topMargin = ShareData.PxToDpi_xhdpi(80);
        addView(mLinearLayout,fl);
        {

            TextView modeTip = new TextView(getContext());
            modeTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f);
            modeTip.setTextColor(0xffaaaaaa);
            modeTip.setText(R.string.video_setting_edition);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.topMargin = ShareData.PxToDpi_xhdpi(60);
            mLinearLayout.addView(modeTip,ll);

            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.topMargin = ShareData.PxToDpi_xhdpi(20);
            LinearLayout modeParent = new LinearLayout(getContext());
            modeParent.setGravity(Gravity.CENTER_HORIZONTAL);
            modeParent.setOrientation(LinearLayout.HORIZONTAL);
            mLinearLayout.addView(modeParent,ll);
            {
                mode10sParent = new ModeItem(getContext(),R.drawable.video_setting_mode_10,R.string.video_setting_10s_mode,R.string.video_setting_10s_mode_tip);
                mode10sParent.setOnClickListener(mOnClickListener);
                ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(300), ViewGroup.LayoutParams.WRAP_CONTENT);
                ll.gravity = Gravity.CENTER;
                modeParent.addView(mode10sParent,ll);

                modeFreeParent = new ModeItem(getContext(),R.drawable.video_setting_mode_free,R.string.video_setting_free_mode,R.string.video_setting_free_mode_tip);
                modeFreeParent.setOnClickListener(mOnClickListener);
                ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(300), ViewGroup.LayoutParams.WRAP_CONTENT);
                ll.leftMargin = ShareData.PxToDpi_xhdpi(40);
                ll.gravity = Gravity.CENTER;
                modeParent.addView(modeFreeParent,ll);
            }
            TextView frameTip = new TextView(getContext());
            frameTip.setText(R.string.video_setting_frame_tip);
            frameTip.setTextColor(0xffaaaaaa);
            frameTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.topMargin = ShareData.PxToDpi_xhdpi(60);
            mLinearLayout.addView(frameTip,ll);

            LinearLayout frameParent = new LinearLayout(getContext());
//            ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
//            shapeDrawable.getPaint().setColor(0xff333333);
//            shapeDrawable.setIntrinsicHeight(1);
//            frameParent.setDividerDrawable(getResources().getDrawable(R.drawable.video_setting_dividers));
//            frameParent.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
//            frameParent.setPadding(ShareData.PxToDpi_xhdpi(33),ShareData.PxToDpi_xhdpi(48),ShareData.PxToDpi_xhdpi(33),ShareData.PxToDpi_xhdpi(48));

//            frameParent.setPadding(ShareData.PxToDpi_xhdpi(30),ShareData.PxToDpi_xhdpi(48),ShareData.PxToDpi_xhdpi(30),ShareData.PxToDpi_xhdpi(48));
//            frameParent.setPadding(ShareData.PxToDpi_xhdpi(30),ShareData.PxToDpi_xhdpi(48),ShareData.PxToDpi_xhdpi(30),ShareData.PxToDpi_xhdpi(48));
//            frameParent.setMinimumWidth(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(80));
            frameParent.setMinimumHeight(ShareData.PxToDpi_xhdpi(180));
            frameParent.setBackground(getDrawable(0xff454545,ShareData.PxToDpi_xhdpi(1)));
            frameParent.setOrientation(LinearLayout.HORIZONTAL);
            frameParent.setGravity(Gravity.CENTER);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.topMargin = ShareData.PxToDpi_xhdpi(20);
            mLinearLayout.addView(frameParent,ll);
            {
                int[] selectedResId = new int[]{R.drawable.video_setting_square_selected,R.drawable.video_setting_cinemascrope_selected,R.drawable.video_setting_wide_selected,R.drawable.video_setting_vertical_selected};
                int[] unSelectedResId = new int[]{R.drawable.video_setting_square_default,R.drawable.video_setting_cinemascrope_default,R.drawable.video_setting_wide_default,R.drawable.video_setting_vertical_default};
                int[] textResId = new int[]{R.string.video_setting_frame_square,R.string.video_setting_frame_cinemascrope,R.string.video_setting_frame_widescreen,R.string.video_setting_frame_vertical_screen};
                frame1_1 = addFrameItem(frameParent,selectedResId[0],unSelectedResId[0],textResId[0]);
                addDividerLine(frameParent);
                frame235_1 = addFrameItem(frameParent,selectedResId[1],unSelectedResId[1],textResId[1]);
                addDividerLine(frameParent);
                frame16_9 = addFrameItem(frameParent,selectedResId[2],unSelectedResId[2],textResId[2]);
                addDividerLine(frameParent);
                frame9_16 = addFrameItem(frameParent,selectedResId[3],unSelectedResId[3],textResId[3]);
            }
        }

        mConfirm = new TextView(getContext());
        mConfirm.setText(R.string.video_setting_confirm);
        mConfirm.setTextColor(0xff272727);
        mConfirm.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f);
        mConfirm.setGravity(Gravity.CENTER);
        mConfirm.setBackgroundColor(0xffffc433);
        fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ShareData.PxToDpi_xhdpi(100));
        fl.gravity = Gravity.BOTTOM;
        fl.leftMargin = ShareData.PxToDpi_xhdpi(40);
        fl.rightMargin = ShareData.PxToDpi_xhdpi(40);
        fl.bottomMargin = ShareData.PxToDpi_xhdpi(40);
        addView(mConfirm,fl);
        mConfirm.setOnClickListener(mOnClickListener);
    }

    public TextView addFrameItem(LinearLayout frameParent,int selectedResId, int unSelectedResId, int textResId)
    {
        int minW = (ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(80)- 6)/4;
        FrameTextView item = new FrameTextView(getContext(),selectedResId,unSelectedResId,textResId);
        item.setMinWidth(minW);
        item.setOnClickListener(mOnClickListener);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        ll.weight = 1;
        item.setLayoutParams(ll);
        frameParent.addView(item);
        return item;
    }
    public void addDividerLine(LinearLayout parent)
    {
        View line = new View(getContext());
        line.setBackgroundColor(0xff333333);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(1), ShareData.PxToDpi_xhdpi(80));
        parent.addView(line,ll);
    }

    private View.OnClickListener mOnClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == mConfirm){
                MyBeautyStat.onClickByRes(R.string.视频相册页_下一步);
                onNext();
            }else if(v == frame1_1){
                MyBeautyStat.onClickByRes(R.string.视频相册页_1_1尺寸);
                setFrame(PlayRatio.RATIO_1_1);
            }else if(v == frame235_1){
                setFrame(PlayRatio.RATIO_235_1);
                MyBeautyStat.onClickByRes(R.string.视频相册页_2_35_1尺寸);
            }else if(v == frame16_9){
                setFrame(PlayRatio.RATIO_16_9);
                MyBeautyStat.onClickByRes(R.string.视频相册页_16_9横尺寸);
            }else if(v == frame9_16){
                setFrame(PlayRatio.RATIO_9_16);
                MyBeautyStat.onClickByRes(R.string.视频相册页_16_9竖尺寸);
            }else if(v == mode10sParent){
                MyBeautyStat.onClickByRes(R.string.视频设置页_选择10秒模式);
                setMode(true);
            }else if(v == modeFreeParent){
                MyBeautyStat.onClickByRes(R.string.视频设置页_选择自由模式);
                setMode(false);
            }
        }
    };

    private void onNext()
    {
        MyBeautyStat.PictureSize size = MyBeautyStat.PictureSize.PictureSize1_1;
        MyBeautyStat.ClipMode clipMode = mIs10sMode ? MyBeautyStat.ClipMode.mode10S : MyBeautyStat.ClipMode.modeFree;
        switch (mCurRatio){
            case PlayRatio.RATIO_9_16:
                size = MyBeautyStat.PictureSize.PictureSize16_9竖;
                break;
            case PlayRatio.RATIO_16_9:
                size = MyBeautyStat.PictureSize.PictureSize16_9横;
                break;
            case PlayRatio.RATIO_235_1:
                size = MyBeautyStat.PictureSize.PictureSize235_1;
                break;
            case PlayRatio.RATIO_1_1:
                size = MyBeautyStat.PictureSize.PictureSize1_1;
                break;
            default:
                break;
        }
        MyBeautyStat.onVideoSetting(size,clipMode);

        MyBeautyStat.onClickByRes(R.string.视频设置页_进入美化);
        final HashMap<String,Object> parmas = new HashMap<>();
        if(mSite.m_inParams != null){
            parmas.putAll(mSite.m_inParams);
        }
        parmas.put("ratio", mCurRatio);
        parmas.put("10s_mode",mIs10sMode);
        mSite.m_myParams.put("ratio",mCurRatio);
        mSite.m_myParams.put("10s_mode",mIs10sMode);
        List<VideoEntry> videoEntries = (List<VideoEntry>) mSite.m_inParams.get("videos");
        VideoAlbumUtils.transformVideoInfo(getContext(), videoEntries, mIs10sMode, new VideoAlbumUtils.VideoTransformCallBack()
        {
            @Override
            public void onFinish(List<VideoInfo> videoInfos)
            {
                parmas.put("videos",videoInfos);
                mSite.onVideoBeautify(getContext(), parmas);
            }
        });
    }


    @Override
    public void SetData(HashMap<String, Object> params)
    {
        if(params != null){
//            if(params.containsKey("bk")){
//                mMaskBp = (Bitmap) params.get("bk");
//            }
            if(params.containsKey("ratio")){
                mCurRatio = (int) params.get("ratio");
            }
            if(params.containsKey("10s_mode")){
                mIs10sMode = (boolean) params.get("10s_mode");
            }
        }
        if(mSite.m_myParams.containsKey("ratio")){
            mCurRatio = (int) mSite.m_myParams.get("ratio");
        }
        if(mSite.m_myParams.containsKey("10s_mode")){
            mIs10sMode = (boolean) mSite.m_myParams.get("10s_mode");
        }
        setMode(mIs10sMode);
        setFrame(mCurRatio);
//        setBk(mMaskBp);
    }
    private void setMode(boolean is10sMode)
    {
        this.mIs10sMode = is10sMode;
        mode10sParent.setSelected(is10sMode);
        modeFreeParent.setSelected(!is10sMode);
    }

    private void setFrame(int ratio)
    {
        frame9_16.setSelected(false);
        frame16_9.setSelected(false);
        frame235_1.setSelected(false);
        frame1_1.setSelected(false);
        mCurRatio = ratio;
        switch (ratio){
            case PlayRatio.RATIO_9_16:
                frame9_16.setSelected(true);
                break;
            case PlayRatio.RATIO_16_9:
                frame16_9.setSelected(true);
                break;
            case PlayRatio.RATIO_235_1:
                frame235_1.setSelected(true);
                break;
            case PlayRatio.RATIO_1_1:
                frame1_1.setSelected(true);
                break;
            default:
                break;
        }
    }

    protected void setBk(Bitmap bitmap)
    {
        if(bitmap == null){
            setBackgroundColor(Color.BLACK);
        }else{
            setBackground(new BitmapDrawable(bitmap));
        }
    }

    @Override
    public void onBack()
    {
        MyBeautyStat.onClickByRes(R.string.视频设置页_退出视频设置页);

        HashMap<String,Object> parmas = new HashMap<>();
        parmas.put("ratio", mCurRatio);
        parmas.put("10s_mode",mIs10sMode);
        mSite.onBack(getContext(),parmas);
    }

    /**
     * 方形边框
     * @param color
     * @return
     */
    private Drawable getDrawable(int color,int strokeW)
    {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
        Paint paint = shapeDrawable.getPaint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeW);
        paint.setColor(color);
        return shapeDrawable;
    }

    class ModeItem extends LinearLayout
    {
        private final Drawable unselectedDrawable;
        private final Drawable selectedDrawable;
        public ModeItem(Context context,int logoResId,int titleTextId, int tipTextId)
        {
            super(context);
//            setPadding(ShareData.PxToDpi_xhdpi(32),0,ShareData.PxToDpi_xhdpi(32),ShareData.PxToDpi_xhdpi(42));
            setPadding(ShareData.PxToDpi_xhdpi(30),0,ShareData.PxToDpi_xhdpi(30),ShareData.PxToDpi_xhdpi(42));
            setOrientation(LinearLayout.VERTICAL);
            setGravity(Gravity.CENTER);
            unselectedDrawable = getDrawable(0xff454545, ShareData.PxToDpi_xhdpi(1));
            selectedDrawable = getDrawable(0xffffc433, ShareData.PxToDpi_xhdpi(2));

            LinearLayout.LayoutParams ll;
            ImageView logo = new ImageView(getContext());
            logo.setMinimumWidth(ShareData.PxToDpi_xhdpi(194));
            logo.setMinimumHeight(ShareData.PxToDpi_xhdpi(136));
            logo.setScaleType(ImageView.ScaleType.CENTER);
            logo.setImageResource(logoResId);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.topMargin = ShareData.PxToDpi_xhdpi(25);
            addView(logo,ll);

            TextView title = new TextView(getContext());
            title.setText(titleTextId);
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.topMargin = ShareData.PxToDpi_xhdpi(29);
            addView(title,ll);

            View line = new View(getContext());
            line.setBackgroundColor(0xff454545);
            ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(60),2);
            ll.topMargin = ShareData.PxToDpi_xhdpi(30);
            addView(line,ll);

            TextView tip = new TextView(getContext());
            tip.setLineSpacing(ShareData.PxToDpi_xhdpi(7),1f);
            if(LoginOtherUtil.isChineseLanguage(getContext())){
                tip.setMinLines(3);
                tip.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12f);
            }else
            {
                tip.setTextSize(TypedValue.COMPLEX_UNIT_DIP,11f);
                tip.setMinLines(4);
            }
            tip.setTextColor(0xff666666);
            tip.setText(tipTextId);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.topMargin = ShareData.PxToDpi_xhdpi(30);
            addView(tip,ll);

            setSelected(false);
        }

        @Override
        public void setSelected(boolean selected)
        {
            super.setSelected(selected);
            if(selected){
               setBackgroundDrawable(selectedDrawable);
            }else{
                setBackgroundDrawable(unselectedDrawable);
            }
        }
    }

    class FrameTextView extends TextView{
        private Drawable unSelectedDrawable;
        private Drawable selectedDrawable;
        private int selectedColor = 0xffffc433;
        private int unSelectedColor = 0xff666666;
        public FrameTextView(Context context,int selectedImageId,int unSelectImageId,int textResId)
        {
            super(context);
            if(LoginOtherUtil.isChineseLanguage(getContext())){
                setTextSize(TypedValue.COMPLEX_UNIT_DIP,12f);
            }else
            {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f);
            }
            setText(textResId);
            setGravity(Gravity.CENTER);
            setTextColor(unSelectedColor);
            setCompoundDrawablePadding(ShareData.PxToDpi_xhdpi(20));
            selectedDrawable = getResDrawable(selectedImageId);
            unSelectedDrawable = getResDrawable(unSelectImageId);
            setSelected(false);
        }

        public Drawable getResDrawable(int resId)
        {
            Drawable drawable = getResources().getDrawable(resId);
            drawable.setBounds(0,0,drawable.getMinimumWidth(),drawable.getMinimumHeight());
            return drawable;
        }


        @Override
        public void setSelected(boolean selected)
        {
            if(selected){
                setCompoundDrawables(null,selectedDrawable,null,null);
                setTextColor(selectedColor);
            }else{
                setCompoundDrawables(null,unSelectedDrawable,null,null);
                setTextColor(unSelectedColor);
            }
        }
    }
}
