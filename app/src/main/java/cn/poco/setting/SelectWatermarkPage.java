package cn.poco.setting;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ScaleXSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import cn.poco.album2.utils.RoundCornerTransformation;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.interphoto2.R;
import cn.poco.setting.site.SelectWatermarkSite;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;
import cn.poco.video.utils.SoftKeyBoardListener;
import cn.poco.video.videotext.VideoEditView;
import cn.poco.video.view.LogoTextView;


/**
 * Created by admin on 2017/6/13.
 */

public class SelectWatermarkPage extends IPage {

    private static final String TAG = "SelectWatermarkPage";
    Bitmap bitmap1;

    private SelectWatermarkSite mSite;
    private Context mContext;
    //头部
    private FrameLayout m_topBar;
    private ImageView m_backBtn;
    private ImageView m_okBtn;
    private FrameLayout.LayoutParams fl;
    private FrameLayout.LayoutParams layoutParams;
    private LinearLayout.LayoutParams rlp;
    private int imgHeight;
    private ImageView mImageView;
    private SettingSliderBtn sliderBtn;
    private ClipViewPager mViewPager;
    private View shaderView;
    private SettingInfo settingInfo;
    private RelativeLayout relativeLayout;
    private int logoID;
    private boolean isOpen = true;
    private View editextUnderline;
    protected SettingSliderBtn.OnSwitchListener mSwitchListener = new SettingSliderBtn.OnSwitchListener() {
        @Override
        public void onSwitch(View v, boolean on) {
            if (v == sliderBtn) {
                isOpen = on;
                if (on) {
                    shaderView.setVisibility(GONE);
                } else {
                    shaderView.setVisibility(VISIBLE);

                }
            }
        }
    };
    private boolean isShowKeyBoard = false;
    private TextView inputBtn;
    private SettingGroup watermarkSwitch;
    private ImageView interPhoto;
    private LogoTextView logoView;
    private FrameLayout bottonLayout;
    private int[] imageItem = new int[]{R.drawable.logo_watermark_1, R.drawable.logo_watermark_2, R.drawable.logo_watermark_3,
            R.drawable.logo_watermark_4, R.drawable.logo_watermark_5, R.drawable.logo_watermark_6, R.drawable.logo_watermark_7,};
    private int[] image = new int[]{R.drawable.logo_watermark_image1, R.drawable.logo_watermark_image2, R.drawable.logo_watermark_image3,
            R.drawable.logo_watermark_image4, R.drawable.logo_watermark_image5, R.drawable.logo_watermark_image6,
            R.drawable.logo_watermark_image7,};
    private int[] logo = new int[]{R.drawable.interphpoto_logo_1, R.drawable.interphpoto_logo_2, R.drawable.interphpoto_logo_3, R.drawable.interphpoto_logo_4,
            R.drawable.interphpoto_logo_5, R.drawable.interphpoto_logo_6, R.drawable.interphpoto_logo_7};
    private FrameLayout rootLayout;
    private ObjectAnimator objectAnimator;
    private ScrollView mScrollView;
    private VideoEditView mEditText;
    private LinearLayout logoContainer;
    private boolean isPress = false;
    private String videoPath = "";
    private int imagW = 0, imagH = 0;
    private float ratio = 0.6f;
    private MyAdapter mAdapter;
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == m_okBtn) {
                if (isShowKeyBoard) {
                    hideKeyboard();
                } else {
                    if (sliderBtn.getSwitchStatus()) {
                        sevalogoText();
                        Bitmap bitmap = getLogo(logoContainer);
                        saveBitmapToSD(bitmap);
                        settingInfo.setVideoLogo(logoID, videoPath);
                        String mText = String.valueOf(mEditText.getText());
                        MyBeautyStat.onClickByRes(R.string.水印logo页_开启水印logo);
                        if (mText == null) {
                            settingInfo.setLogoText(mText);
                        }
                        switch (logoID) {
                            case 0:
                                TongJi2.AddCountByRes(getContext(), R.integer.视频LOGO1);
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0000, isOpen);
                                MyBeautyStat.onClickByRes(R.string.水印logo页_保存水印logo);
                                break;
                            case 1:
                                TongJi2.AddCountByRes(getContext(), R.integer.视频LOGO2);
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0001, isOpen);
                                MyBeautyStat.onClickByRes(R.string.水印logo页_保存水印logo);
                                break;
                            case 2:
                                TongJi2.AddCountByRes(getContext(), R.integer.视频LOGO3);
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0002, isOpen);
                                MyBeautyStat.onClickByRes(R.string.水印logo页_保存水印logo);
                                break;
                            case 3:
                                TongJi2.AddCountByRes(getContext(), R.integer.视频LOGO4);
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0003, isOpen);
                                MyBeautyStat.onClickByRes(R.string.水印logo页_保存水印logo);
                                break;
                            case 4:
                                TongJi2.AddCountByRes(getContext(), R.integer.视频LOGO5);
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0004, isOpen);
                                MyBeautyStat.onClickByRes(R.string.水印logo页_保存水印logo);
                                break;
                            case 5:
                                TongJi2.AddCountByRes(getContext(), R.integer.视频LOGO6);
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0005, isOpen);
                                MyBeautyStat.onClickByRes(R.string.水印logo页_保存水印logo);
                                break;
                            case 6:
                                TongJi2.AddCountByRes(getContext(), R.integer.视频LOGO7);
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0006, isOpen);
                                MyBeautyStat.onClickByRes(R.string.水印logo页_保存水印logo);
                                break;
                        }

                    } else {
                        settingInfo.setCloseLogoIndex(logoID);
                        settingInfo.setVideoLogo(-1, null);
                        String mText = String.valueOf(mEditText.getText());
                        settingInfo.setLogoText(mText);
                        MyBeautyStat.onClickByRes(R.string.水印logo页_关闭水印logo);
                        switch (logoID) {
                            case 0:
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0000, isOpen);
                                break;
                            case 1:
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0001, isOpen);
                                break;
                            case 2:
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0002, isOpen);
                                break;
                            case 3:
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0003, isOpen);
                                break;
                            case 4:
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0004, isOpen);
                                break;
                            case 5:
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0005, isOpen);
                                break;
                            case 6:
                                MyBeautyStat.onSaveWatermarkLogo(MyBeautyStat.WatermarkLogoType.videologo_0006, isOpen);
                                break;
                        }

                    }
                    mSite.onBack(mContext);
                }

            } else if (v == m_backBtn) {
                MyBeautyStat.onClickByRes(R.string.水印logo页_退出水印logo页);
                if (isShowKeyBoard) {
                    hideKeyboard();
                } else {
                    mSite.onBack(mContext);
                }

            } else if (v == sliderBtn){
                if (sliderBtn.getSwitchStatus()){
                    MyBeautyStat.onClickByRes(R.string.水印logo页_开启水印logo);
                }else {
                    MyBeautyStat.onClickByRes(R.string.水印logo页_关闭水印logo);
                }

            }else if (v == inputBtn) {
                doAnimMoveUp(mScrollView);
                MyBeautyStat.onClickByRes(R.string.水印logo页_输入名字);
            }else if (v == rootLayout) {

            /*  if (isShowKeyBoard){
                    isPress = true;
                    hideKeyboard();
                    doAnimMoveDown(mScrollView);
                }
*/
            }

        }
    };
    public SelectWatermarkPage(Context context, BaseSite site) {
        super(context, site);
        this.mSite = (SelectWatermarkSite) site;
        this.mContext = context;
        settingInfo = SettingInfoMgr.GetSettingInfo(mContext);
        initView();
    }

    private void initView() {
        this.setBackgroundColor(0xff1a1a1a);
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(80);
        mScrollView =  new ScrollView(getContext());
        mScrollView.setVerticalScrollBarEnabled(false);
        mScrollView.setOverScrollMode(OVER_SCROLL_NEVER);
        this.addView(mScrollView,layoutParams);

     //   mTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/PingFangRegular.ttf");
        rootLayout = new FrameLayout(mContext);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rootLayout.setBackgroundColor(0xff1a1a1a);
        rootLayout.setOnClickListener(onClickListener);
        mScrollView.addView(rootLayout);
        SoftKeyBoardListener.setListener(rootLayout, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                isShowKeyBoard = true;
            }

            @Override
            public void keyBoardHide(int height) {
                isShowKeyBoard = false;
                if (!isPress) {
                    doAnimMoveDown(rootLayout);
                }

            }
        });
       // this.addView(rootLayout, layoutParams);


        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FrameLayout imageLayout = new FrameLayout(mContext);
        rootLayout.addView(imageLayout, layoutParams);
        mImageView = new ImageView(mContext);
        mImageView.setId(R.id.watermarkPic);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mImageView.setImageResource(image[0]);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), image[0]);
        final int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        imgHeight = (int) (height * ShareData.getScreenW() * 1f / width);
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imgHeight);
        layoutParams.gravity = Gravity.TOP;
        imageLayout.addView(mImageView, layoutParams);


        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        logoContainer = new LinearLayout(mContext);
        logoContainer.setOrientation(LinearLayout.VERTICAL);
        layoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        layoutParams.rightMargin = ShareData.PxToDpi_xhdpi(12);
        layoutParams.bottomMargin = ShareData.PxToDpi_xhdpi(8);
        imageLayout.addView(logoContainer, layoutParams);

        bitmap1 = BitmapFactory.decodeResource(getResources(), logo[0]);
        imagW = (int) (bitmap1.getWidth() * ratio);
        imagH = (int) (bitmap1.getHeight() * ratio);
        rlp = new LinearLayout.LayoutParams(imagW, imagH);
        interPhoto = new ImageView(mContext);
        interPhoto.setId(R.id.interPhotoView);
        interPhoto.setImageResource(logo[0]);
        logoContainer.addView(interPhoto, rlp);

        rlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        logoView = new LogoTextView(mContext);
        logoView.setVisibility(GONE);
        logoView.setId(R.id.logoView);
        rlp.topMargin = ShareData.PxToDpi_xhdpi(5);
        logoContainer.addView(logoView, rlp);


        watermarkSwitch = new SettingGroup(mContext);
        watermarkSwitch.setBackgroundColor(0xff1a1a1a);
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(110));
        watermarkSwitch.setId(R.id.watermarkSwitch);
        layoutParams.topMargin = imgHeight;
        sliderBtn = new SettingSliderBtn(mContext);
        sliderBtn.setOnSwitchListener(mSwitchListener);
        sliderBtn.setSwitchStatus(true);
        sliderBtn.setOnClickListener(onClickListener);
        watermarkSwitch.addItem(getResources().getString(R.string.videologo_add_watermark), sliderBtn, 15);
        rootLayout.addView(watermarkSwitch, layoutParams);

        View  view = new View(getContext());
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ShareData.PxToDpi_xhdpi(1));
        view.setBackgroundColor(Color.BLACK);
        layoutParams.topMargin = imgHeight + ShareData.PxToDpi_xhdpi(110);
        rootLayout.addView(view,layoutParams);



        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bottonLayout = new FrameLayout(mContext);
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(110) + imgHeight;
        rootLayout.addView(bottonLayout, layoutParams);

        //layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(280));
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView watermarkStyle = new TextView(mContext);
        watermarkStyle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        watermarkStyle.setTextColor(0xff666666);
    //    watermarkStyle.setTypeface(mTypeface);
        watermarkStyle.setText(getResources().getString(R.string.videologo_logo_style));
        layoutParams.leftMargin = ShareData.PxToDpi_xhdpi(30);
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(54);
        bottonLayout.addView(watermarkStyle, layoutParams);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        relativeLayout = new RelativeLayout(mContext);
        relativeLayout.setBackgroundColor(Color.parseColor("#1A1A1A"));
        relativeLayout.setClipChildren(false);
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(126);
        bottonLayout.addView(relativeLayout, layoutParams);

        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(180), ShareData.PxToDpi_xhdpi(180));
        mViewPager = new ClipViewPager(mContext);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        mViewPager.setOffscreenPageLimit(6);
        mViewPager.setPageMargin(ShareData.PxToDpi_hdpi(50));
        mViewPager.setClipChildren(false);
        mViewPager.setPageTransformer(false, new MyTransformation());
        mAdapter = new MyAdapter(imageItem, mContext);

        mViewPager.setAdapter(mAdapter);
        relativeLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mViewPager.dispatchTouchEvent(event);
            }
        });
        relativeLayout.addView(mViewPager, lp);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(240));
        shaderView = new View(mContext);
        shaderView.setId(R.id.shaderView);
        shaderView.setBackgroundColor(Color.parseColor("#0e0e0e"));
        shaderView.setAlpha(0.8f);
        shaderView.setVisibility(GONE);
        layoutParams.gravity = Gravity.TOP;
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(100);
        bottonLayout.addView(shaderView, layoutParams);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView textView = new TextView(mContext);
        textView.setId(R.id.nameInput);
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(349);
        layoutParams.leftMargin = ShareData.PxToDpi_xhdpi(30);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        textView.setTextColor(0xff666666);
    //    textView.setTypeface(mTypeface);
        textView.setText(getResources().getString(R.string.videologo_hint_input));
        bottonLayout.addView(textView, layoutParams);

        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(90));
        inputBtn = new TextView(mContext);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        inputBtn.setOnClickListener(onClickListener);
        inputBtn.setGravity(Gravity.CENTER);
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(402);
        layoutParams.leftMargin = ShareData.PxToDpi_xhdpi(40);
        layoutParams.rightMargin = ShareData.PxToDpi_xhdpi(40);
        inputBtn.setAllCaps(false);
        inputBtn.setText(getResources().getString(R.string.videologo_input_btn));
        inputBtn.setTextColor(0xffaaaaaa);
        bottonLayout.addView(inputBtn, layoutParams);

        mEditText = new VideoEditView(mContext);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mEditText.setOnClickListener(onClickListener);
        mEditText.setGravity(Gravity.CENTER);
        mEditText.setSingleLine(true);
        mEditText.setOnKeyListener(onKeyListener);
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
        mEditText.addTextChangedListener(new LogoNameTextWatcher());
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(402);
        layoutParams.leftMargin = ShareData.PxToDpi_xhdpi(40);
        layoutParams.rightMargin = ShareData.PxToDpi_xhdpi(40);
        mEditText.setBackground(null);
        Utils.modifyEditTextCursor(mEditText,0xffffc433);
        mEditText.setVisibility(GONE);
        bottonLayout.addView(mEditText, layoutParams);

        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(1));
        editextUnderline =  new View(getContext());
        editextUnderline.setBackgroundColor(0xff393939);
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(482);
        layoutParams.leftMargin = ShareData.PxToDpi_xhdpi(40);
        layoutParams.rightMargin = ShareData.PxToDpi_xhdpi(40);
        layoutParams.bottomMargin = ShareData.PxToDpi_xhdpi(20);
        bottonLayout.addView(editextUnderline,layoutParams);


        //头部
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
        layoutParams.gravity = Gravity.TOP;
        m_topBar = new FrameLayout(mContext);
        m_topBar.setBackgroundColor(Color.BLACK);
        this.addView(m_topBar, layoutParams);

        m_backBtn = new ImageView(getContext());
        m_backBtn.setOnClickListener(onClickListener);
        m_backBtn.setTag(1);
        m_backBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        m_backBtn.setImageResource(R.drawable.framework_back_btn);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        m_backBtn.setLayoutParams(fl);
        m_topBar.setId(R.id.m_topBar);
        m_topBar.addView(m_backBtn);

        {
            TextView m_titleView = new TextView(mContext);
            m_titleView.setText(getContext().getResources().getString(R.string.video_watermark));
            m_titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER;
            m_titleView.setLayoutParams(fl);
            m_topBar.addView(m_titleView);

            m_okBtn = new ImageView(getContext());
            m_okBtn.setOnClickListener(onClickListener);
            m_okBtn.setTag(1);
            m_okBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            m_okBtn.setImageResource(R.drawable.framework_ok_btn);
            fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            m_okBtn.setLayoutParams(fl);
            m_topBar.addView(m_okBtn);
        }

        getCurrPosition();
    }
    private OnKeyListener onKeyListener  = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_ENTER){
                if (isShowKeyBoard){
                    isShowKeyBoard = false;
                    hideKeyboard();
                }
            }
            return false;
        }
    };

    private void saveBitmapToSD(Bitmap bt) {
        File path = mContext.getFilesDir();
        //File path = Environment.getExternalStorageDirectory();
        File file = new File(path, "logo.png");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bt.compress(Bitmap.CompressFormat.PNG, 100, out);

            videoPath = file.getPath();
            Log.i(TAG, "saveBitmapToSD: " + videoPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (out != null) {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Bitmap getLogo(View view) {

        int w = (int) (view.getWidth() / ratio);
        int h = (int) (view.getHeight() / ratio);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        view.setBackgroundColor(0x00000000);
        Matrix matrix = new Matrix();
        matrix.postScale(1 / ratio, 1 / ratio);
        Canvas c = new Canvas(bmp);
        c.save();
        c.concat(matrix);
        view.draw(c);
        c.restore();
        return bmp;

    }

    private String selectLogoStyle(String str) {

        StringBuilder builder = new StringBuilder(str);
        String mText = null;
        switch (logoID) {
            case 0:
                mText = builder.toString();
                break;
            case 1:
                mText = builder.toString();
                break;
            case 2:
                builder.insert(0, "@");
                mText = builder.toString();
                break;
            case 3:
                mText = builder.toString();
                break;
            case 4:
                builder.insert(0, "©");
                mText = builder.toString();
                break;
            case 5:
                builder.insert(0, "©");
                mText = builder.toString();
                break;
            case 6:
                builder.insert(0, "-");
                builder.append("-");
                mText = builder.toString();
                break;
        }

        return mText;
    }

    @Override
    public void SetData(HashMap<String, Object> params) {
        if (settingInfo.m_data.containsKey("LOGE_TEXT")) {
            if (settingInfo.getLogoText() != null) {
                String mText = settingInfo.getLogoText();
                mEditText.setText(mText);
                logoView.setVisibility(VISIBLE);

                if (sliderBtn.getSwitchStatus()) {
                    logoView.setText(mText);
                    if (mText.isEmpty()) {
                        inputBtn.setText(getResources().getString(R.string.videologo_input_btn));

                    } else {
                        inputBtn.setText(mText);
                    }

                } else {
                    logoView.setText("");
                    logoView.setVisibility(GONE);
                    inputBtn.setText(getResources().getString(R.string.videologo_input_btn));
                }
            }

        }
        String text = String.valueOf(mEditText.getText());
        if (!text.isEmpty()) {
            logoView.setVisibility(VISIBLE);
            String mText = String.valueOf(mEditText.getText());
            logoView.setText(mText);
            inputBtn.setText(mText);
        }

    }

    private void setlogoLayoutParams() {

        if (logoID == -1) {
            logoID = 0;
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), logo[logoID]);
        int imagW = (int) (bitmap.getWidth() * ratio);
        int imagH = (int) (bitmap.getHeight() * ratio);

        if (logoView.getWidth() > imagW) {
            rlp = new LinearLayout.LayoutParams(imagW, imagH);
            rlp.gravity = Gravity.RIGHT;
            interPhoto.setLayoutParams(rlp);
            rlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.gravity = Gravity.RIGHT;
            rlp.topMargin = ShareData.PxToDpi_xhdpi(5);
            logoView.setLayoutParams(rlp);

        } else if (logoView.getWidth() < imagW || logoView.getWidth() == imagW) {
            rlp = new LinearLayout.LayoutParams(imagW, imagH);
            rlp.gravity = Gravity.CENTER_HORIZONTAL;
            interPhoto.setLayoutParams(rlp);

            rlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.gravity = Gravity.CENTER_HORIZONTAL;
            rlp.topMargin = ShareData.PxToDpi_xhdpi(5);
            ;
            logoView.setLayoutParams(rlp);

        }

        String text = String.valueOf(mEditText.getText());
        if (text.isEmpty()) {
            logoView.setVisibility(GONE);
        }
    }

    private void logoText() {
        String inputText = String.valueOf(mEditText.getText());
        if (inputText.isEmpty()) {
            logoView.setVisibility(GONE);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), logo[logoID]);
            int imagW = (int) (bitmap.getWidth() * ratio);
            int imagH = (int) (bitmap.getHeight() * ratio);
            rlp = new LinearLayout.LayoutParams(imagW, imagH);
            rlp.gravity = Gravity.CENTER_HORIZONTAL;
            interPhoto.setLayoutParams(rlp);

        } else {

            StringBuilder builder = new StringBuilder(inputText);
            String mText;
            switch (logoID) {
                case 0:
                    mText = builder.toString();
                    logoView.setText(mText);
                    setlogoLayoutParams();
                    break;
                case 1:
                    mText = builder.toString();
                    logoView.setText(mText);
                    setlogoLayoutParams();
                    break;
                case 2:
                    builder.insert(0, "@");
                    mText = builder.toString();
                    logoView.setText(mText);
                    setlogoLayoutParams();
                    break;
                case 3:
                    mText = builder.toString();
                    logoView.setText(mText);
                    setlogoLayoutParams();

                    break;
                case 4:
                    builder.insert(0, "©");
                    mText = builder.toString();
                    logoView.setText(mText);
                    ;
                    setlogoLayoutParams();
                    break;
                case 5:
                    builder.insert(0, "©");
                    mText = builder.toString();
                    logoView.setText(mText);
                    setlogoLayoutParams();
                    break;
                case 6:
                    builder.insert(0, "-");
                    builder.append("-");
                    mText = builder.toString();
                    logoView.setText(mText);
                    setlogoLayoutParams();
                    break;
            }
        }
    }

    @Override
    public void onBack() {

        mSite.onBack(mContext);
        MyBeautyStat.onClickByRes(R.string.水印logo页_退出水印logo页);
    }

    private void getCurrPosition() {

        logoID = settingInfo.getVideoLogo(0);
        if (logoID == -1) {
            sliderBtn.setSwitchStatus(false);
            shaderView.setVisibility(VISIBLE);
            logoID = settingInfo.getCloseLogoIndex();

        }
        if (logoID < logo.length) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(logoID);
                    setlogoLayoutParams();
                }
            }, 300);

        }

    }

    private void sevalogoText() {
        String text = String.valueOf(mEditText.getText());
        settingInfo.setLogoText(text);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);

    }

    private void showKeyboard(EditText editText) {
        editText.requestFocus();
        editText.setSelection(editText.getText().length());
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    private void doAnimMoveUp(View View) {

        final int translationY = ShareData.PxToDpi_xhdpi(sizi);
        objectAnimator = ObjectAnimator.ofFloat(View, "translationY", 0, -translationY);
        objectAnimator.setDuration(200);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                inputBtn.setVisibility(GONE);
                mEditText.setVisibility(VISIBLE);
                editextUnderline.setVisibility(VISIBLE);
                //int h = ShareData.getScreenH() - translationY;
                layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(720));
                layoutParams.topMargin = ShareData.PxToDpi_xhdpi(80);
                mScrollView.setLayoutParams(layoutParams);

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showKeyboard(mEditText);
                mScrollView.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });

        objectAnimator.start();
    }
    private int sizi = 50;
    private void doAnimMoveDown(final View View) {

        int translationY = ShareData.PxToDpi_xhdpi(sizi);
        objectAnimator = ObjectAnimator.ofFloat(View, "translationY", -translationY, 0);
        objectAnimator.setDuration(200);

        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = ShareData.PxToDpi_xhdpi(80);
                mScrollView.setLayoutParams(layoutParams);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                isPress = false;
                inputBtn.setVisibility(VISIBLE);
                mEditText.setVisibility(GONE);
                String text = String.valueOf(mEditText.getText());
                if (logoView.getVisibility() == GONE) {
                    inputBtn.setText(getResources().getString(R.string.videologo_input_btn));
                } else {
                    inputBtn.setText(text);
                }

            }
        });
        objectAnimator.start();
    }

    class LogoNameTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            logoView.setVisibility(VISIBLE);
            String text = String.valueOf(mEditText.getText());
            String mText = selectLogoStyle(text);
            logoView.setText(mText);
            setlogoLayoutParams();
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mImageView.setImageResource(image[position]);
            logoID = position;
            interPhoto.setImageResource(logo[position]);
            relativeLayout.requestLayout();
            logoText();


        }

        @Override
        public void onPageScrollStateChanged(int state) {


        }
    }


    public class MyAdapter extends PagerAdapter {

        private int[] imags;
        private Context context;
        private View mCurrentView;

        public MyAdapter(int[] imag, Context context) {
            this.imags = imag;
            this.context = context;
        }

        @Override
        public int getCount() {
            return imags.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.i(TAG, "destroyItem: " + position);
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            ImageView imageView = new ImageView(context);
           imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Glide.with(context)
                    .load(imags[position]).bitmapTransform(new CenterCrop(context),
                    new RoundCornerTransformation(context, ShareData.PxToDpi_xhdpi(20), 0))
                    .into(imageView);
            container.addView(imageView);
            return imageView;
        }


    }



    public class ClipViewPager extends ViewPager {
        public ClipViewPager(Context context) {
            super(context);

        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {

            if (ev.getAction() == MotionEvent.ACTION_UP) {
                View view = viewOfClick(ev);
                if (view != null) {
                    int index = indexOfChild(view);
                    if (getCurrentItem() != index) {
                        setCurrentItem(indexOfChild(view));

                    }
                }
            }
            return super.dispatchTouchEvent(ev);
        }


        private View viewOfClick(MotionEvent ev) {
            int childCount = getChildCount();
            int[] location = new int[2];
            for (int i = 0; i < childCount; i++) {
                View v = getChildAt(i);
                v.getLocationOnScreen(location);

                int minX = location[0];
                int minY = location[1];

                int maxX = location[0] + v.getWidth();
                int maxY = minY + v.getHeight();

                float x = ev.getRawX();
                float y = ev.getRawY();

                if ((x > minX && x < maxX) && (y > minY && y < maxY)) {
                    return v;
                }
            }
            return null;
        }
    }

    public class MyTransformation implements ViewPager.PageTransformer {

        private static final float MIN_SCALE = 0.85f;

        @Override
        public void transformPage(View page, float position) {

            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));

            if (position < -1) {
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
            } else if (position < 0) {
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);

            } else if (position >= 0 && position < 1) {
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);

            } else if (position >= 1) {
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);
            }

        }
    }
}
