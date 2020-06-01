package cn.poco.video.videotext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;
import cn.poco.video.site.TextEditPageSite;
import cn.poco.video.videotext.text.WaterMarkInfo;

/**
 * Created by admin on 2017/6/12.
 */

public class TextEditPage extends IPage implements View.OnClickListener {
    private static final String TAG = "视频水印编辑";

    private RelativeLayout.LayoutParams layoutParams;
    private LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private RecyclerView mRecyclerView;
    private RelativeLayout root;
    private TextEditPageSite mSite;
    private RelativeLayout tiemFrameLayout;
    private RelativeLayout textLayout;

    private Context mContext;
    //头部
    private FrameLayout m_topBar;
    private ImageView m_backBtn;
    private ImageView m_okBtn;
    private FrameLayout.LayoutParams fl;
    private ImageButton videoStart, videoAll, videoEnd;
    private TextAdapter textAdapter;
    private WaterMarkInfo mWaterMarkInfo;
    private int mShowType = VideoTextPage.TYPE_START;
    private TextView modifyText;
    private String img;
    private Bitmap mBmp;
    private boolean isDoingAnim = false;
    private boolean isPackUp = false;
    private AnimatorSet packUpAnimSet;
    private AnimatorSet unfoAnimSet;
    private int keyBoardType;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public TextEditPage(Context context, TextEditPageSite textEditSite) {
        super(context, textEditSite);
        TongJiUtils.onPageStart(getContext(), TAG);
        this.mSite = textEditSite;
        this.mContext = context;
        keyBoardType = ((Activity)getContext()).getWindow().getAttributes().softInputMode;
        ((Activity)getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        initView();
        initAnimation();
    }

    public void initView() {
        LayoutParams fl;
        int topH = ShareData.PxToDpi_xhdpi(80);

        root = new RelativeLayout(mContext);
//        root.setOnClickListener(this);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fl.topMargin = topH;
        root.setLayoutParams(fl);
        this.addView(root);

        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
        m_topBar = new FrameLayout(mContext);
        m_topBar.setFocusableInTouchMode(true);
        m_topBar.requestFocus();
        addView(m_topBar, fl);
        {
            m_backBtn = new ImageView(getContext());
            m_backBtn.setOnClickListener(this);
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
                m_titleView.setText(R.string.modifyText);
                m_titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                fl.gravity = Gravity.CENTER;
                m_titleView.setLayoutParams(fl);
                m_topBar.addView(m_titleView);

                m_okBtn = new ImageView(getContext());
                m_okBtn.setOnClickListener(this);
                m_okBtn.setTag(1);
                m_okBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                m_okBtn.setImageResource(R.drawable.framework_ok_btn);
                fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                m_okBtn.setLayoutParams(fl);
                m_topBar.addView(m_okBtn);
            }
        }


        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tiemFrameLayout = new RelativeLayout(mContext);
        tiemFrameLayout.setId(R.id.tiemFrameLayout);
        tiemFrameLayout.setVisibility(View.GONE);
//        layoutParams.addRule(RelativeLayout.BELOW,R.id.m_topBar);
        root.addView(tiemFrameLayout, layoutParams);


        {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView modifyText = new TextView(mContext);
            modifyText.setId(R.id.modeText);
            modifyText.setText(getContext().getResources().getString(R.string.display_mode));
            modifyText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            layoutParams.setMargins(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(100), 0, 0);
            //   layoutParams.addRule(RelativeLayout.BELOW, R.id.m_topBar);
            tiemFrameLayout.addView(modifyText, layoutParams);
        }
        {
            //显示时段
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(120));
            LinearLayout m_DisplayLayout = new LinearLayout(mContext);
            m_DisplayLayout.setWeightSum(16);
            m_DisplayLayout.setBackgroundColor(Color.parseColor("#33000000"));
            m_DisplayLayout.setId(R.id.modeLayout);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.modeText);
            m_DisplayLayout.setOrientation(LinearLayout.HORIZONTAL);
            layoutParams.setMargins(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(60), ShareData.PxToDpi_xhdpi(40), 0);
            tiemFrameLayout.addView(m_DisplayLayout, layoutParams);

            LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 5.4f);
            videoStart = new ImageButton(mContext);
            videoStart.setOnClickListener(this);
            videoStart.setBackgroundColor(Color.BLACK);
            videoStart.setBackgroundColor(Color.parseColor("#00000000"));
            //  ll.setMargins(ShareData.PxToDpi_xhdpi(50), 0, ShareData.PxToDpi_xhdpi(64), 0);
            ll.gravity = Gravity.CENTER;
            videoStart.setImageResource(R.drawable.video_text_start_current);
            m_DisplayLayout.addView(videoStart, ll);

            ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(1), ShareData.PxToDpi_xhdpi(40), 0.05f);
            View view1 = new View(mContext);
            view1.setAlpha(0.2f);
            ll.gravity = Gravity.CENTER;
            view1.setBackgroundColor(Color.WHITE);
            m_DisplayLayout.addView(view1, ll);

            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 5.4f);
            videoAll = new ImageButton(mContext);
            videoAll.setOnClickListener(this);
            videoAll.setBackgroundColor(Color.BLACK);
            videoAll.setBackgroundColor(Color.parseColor("#00000000"));
            ll.gravity = Gravity.CENTER;
            // ll.setMargins(ShareData.PxToDpi_xhdpi(64), 0, ShareData.PxToDpi_xhdpi(64), 0);
            videoAll.setImageResource(R.drawable.video_text_all_default);
            m_DisplayLayout.addView(videoAll, ll);

            ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(1), ShareData.PxToDpi_xhdpi(40), 0.05f);
            View view2 = new View(mContext);
            view2.setAlpha(0.2f);
            ll.gravity = Gravity.CENTER;
            view2.setBackgroundColor(Color.WHITE);
            m_DisplayLayout.addView(view2, ll);

            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 5.4f);
            videoEnd = new ImageButton(mContext);
            videoEnd.setOnClickListener(this);
            videoEnd.setBackgroundColor(Color.BLACK);
            videoEnd.setBackgroundColor(Color.parseColor("#00000000"));
            //  ll.setMargins(ShareData.PxToDpi_xhdpi(64), 0, ShareData.PxToDpi_xhdpi(50), 0);
            ll.gravity = Gravity.CENTER;
            videoEnd.setImageResource(R.drawable.video_text_end_default);
            m_DisplayLayout.addView(videoEnd, ll);
        }


        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.tiemFrameLayout);
        textLayout = new RelativeLayout(mContext);
        root.addView(textLayout, layoutParams);

        {
            //多行文本
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            modifyText = new TextView(mContext);
            modifyText.setId(R.id.modifyText);
            modifyText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            // layoutParams.addRule(RelativeLayout.BELOW, R.id.modeLayout);
            layoutParams.setMargins(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(100), ShareData.PxToDpi_xhdpi(40), 0);
            modifyText.setText(getContext().getResources().getString(R.string.modifyText));
            modifyText.setVisibility(View.GONE);
            textLayout.addView(modifyText, layoutParams);


            mRecyclerView = new RecyclerView(mContext);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.modifyText);
            textLayout.addView(mRecyclerView, layoutParams);
        }
    }

    private void initAnimation() {
        int y = ShareData.PxToDpi_xhdpi(310);
        ObjectAnimator packUp1 = ObjectAnimator.ofFloat(root, "translationY", 0, -y);
        packUp1.setDuration(200);
        ObjectAnimator packUp2 = ObjectAnimator.ofFloat(tiemFrameLayout, "alpha", 1, 0);
        packUp2.setDuration(200);

        packUpAnimSet = new AnimatorSet();
        packUpAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isDoingAnim = false;
                tiemFrameLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                isDoingAnim = true;
            }
        });
        packUpAnimSet.playTogether(packUp1, packUp2);

        ObjectAnimator unfo1 = ObjectAnimator.ofFloat(root, "translationY", -y, 0);
        unfo1.setDuration(200);
        ObjectAnimator unfo2 = ObjectAnimator.ofFloat(tiemFrameLayout, "alpha", 0, 1);
        unfo2.setDuration(200);

        unfoAnimSet = new AnimatorSet();
        unfoAnimSet.playTogether(unfo1, unfo2);
        unfoAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isDoingAnim = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                isDoingAnim = true;
                tiemFrameLayout.setVisibility(View.VISIBLE);
            }
        });

    }

    private void doPackUpAmn() {
        if (!isPackUp) {
            isPackUp = true;
            packUpAnimSet.start();
        }
    }

    private void doUnFoAmn() {
        if (isPackUp) {
            isPackUp = false;
            unfoAnimSet.start();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isDoingAnim) {
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public void SetData(HashMap<String, Object> params) {
        if (params != null) {

            mWaterMarkInfo = (WaterMarkInfo) params.get("videoText");
            // int tepy = (int) params.get("resType");
            ArrayList<TextInfo> data = new ArrayList<>();
            if (mWaterMarkInfo.m_fontsInfo != null && mWaterMarkInfo.m_fontsInfo.size() > 0) {
                for (int i = 0; i < mWaterMarkInfo.m_fontsInfo.size(); i++) {
                    TextInfo info = new TextInfo();
                    String text = mWaterMarkInfo.m_fontsInfo.get(i).m_showText;
                    if (TextUtils.isEmpty(text)) {
                        info.text = "";
                    } else {
                        text = text.replace('$', '\n');
                        info.text = text;
                    }
                    info.maxLine = mWaterMarkInfo.m_fontsInfo.get(i).m_maxLine;
                    info.maxNum = mWaterMarkInfo.m_fontsInfo.get(i).m_maxNum;
                    data.add(info);
                }
            } else {
                modifyText.setVisibility(View.GONE);
            }
            if (data.size() == 0) {
                textLayout.setVisibility(GONE);
            }
            textAdapter = new TextAdapter(data);
            mRecyclerView.setAdapter(textAdapter);

            int type = (int) params.get("showType");
            if (type != mShowType) {
                mShowType = type;
                if (type == VideoTextPage.TYPE_ALL) {
                    videoAll.performClick();
                } else if (type == VideoTextPage.TYPE_END) {
                    videoEnd.performClick();
                } else {
                    videoStart.performClick();
                }
            }
            img = (String) params.get("img");
            if (img != null) {
                mBmp = Utils.DecodeFile(img, null);
                setBackgroundDrawable(new BitmapDrawable(mBmp));
            }
        }
    }

    @Override
    public void onClose() {
        TongJiUtils.onPageEnd(getContext(), TAG);
        ((Activity)getContext()).getWindow().setSoftInputMode(keyBoardType);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        TongJiUtils.onPageResume(getContext(), TAG);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        TongJiUtils.onPagePause(getContext(), TAG);
    }

    @Override
    public void onBack() {
        if (!isDoingAnim) {
            MyBeautyStat.onClickByRes(R.string.选中视频水印_退出修改文本);
            hideKeyboard();
            mSite.onBack(mContext);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == m_backBtn) {
            onBack();
        } else if (v == m_okBtn) {
            MyBeautyStat.onClickByRes(R.string.选中视频水印_保存文本);
            hideKeyboard();
            HashMap<String, Object> dataMap = new HashMap<>();
            if (textAdapter != null) {
                if (mWaterMarkInfo != null) {
                    ArrayList<TextInfo> data = textAdapter.getData();
                    if (mWaterMarkInfo.m_fontsInfo != null && mWaterMarkInfo.m_fontsInfo.size() > 0) {
                        for (int i = 0; i < mWaterMarkInfo.m_fontsInfo.size(); i++) {
                            String text = data.get(i).text;
                            text = text.replace('\n', '$');
                            text = text.replace('\r', '$');
//                            Log.v("hahah "," " + text);
                            mWaterMarkInfo.m_fontsInfo.get(i).m_showText = text;
                        }
                    }
                    textAdapter = new TextAdapter(data);
                    mRecyclerView.setAdapter(textAdapter);
                }
                dataMap.put("videoText", mWaterMarkInfo);
                dataMap.put("showType", mShowType);
            }
            mSite.onSave(dataMap,mContext);

        } else if (v == videoAll) {
            TongJi2.AddCountByRes(getContext(), R.integer.全场);
            mShowType = VideoTextPage.TYPE_ALL;
            videoAll.setImageResource(R.drawable.video_text_all_current);
            videoEnd.setImageResource(R.drawable.video_text_end_default);
            videoStart.setImageResource(R.drawable.video_text_start_default);

        } else if (v == videoEnd) {
            TongJi2.AddCountByRes(getContext(), R.integer.片尾);
            mShowType = VideoTextPage.TYPE_END;
            videoAll.setImageResource(R.drawable.video_text_all_default);
            videoEnd.setImageResource(R.drawable.video_text_end_current);
            videoStart.setImageResource(R.drawable.video_text_start_default);

        } else if (v == videoStart) {
            TongJi2.AddCountByRes(getContext(), R.integer.片头);
            mShowType = VideoTextPage.TYPE_START;
            videoAll.setImageResource(R.drawable.video_text_all_default);
            videoEnd.setImageResource(R.drawable.video_text_end_default);
            videoStart.setImageResource(R.drawable.video_text_start_current);
        } else if (v == root) {
            //root监听已移除
    /*        hideKeyboard();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    tiemFrameLayout.setVisibility(VISIBLE);
                }
            },300);
*/

        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
    }

    private void showKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }


    class TextAdapter extends RecyclerView.Adapter {
        private ArrayList<TextInfo> data;
        private int packUpEditIndex = -1;
        private View.OnClickListener mOnClearClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                MyBeautyStat.onClickByRes(R.string.选中视频水印_清空所有文本);
                int pos = (int) v.getTag();
                data.get(pos).text = "";
                ViewGroup parent = (ViewGroup) v.getParent();
                if (parent != null) {
                    MyViewHolder viewHolder = (MyViewHolder) mRecyclerView.getChildViewHolder(parent);
                    viewHolder.unfoEditText.setText("");
                    viewHolder.packUpEditText.setText("");
                }
            }
        };
        private View.OnClickListener mOnUnfoClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag();
                ViewGroup parent = (ViewGroup) v.getParent();
                if (parent != null) {
                    if (packUpEditIndex == -1) {
                        //展开
                        packUpEditIndex = pos;
                        openEditView((View) v.getParent());
                    } else if (packUpEditIndex == pos) {
                        //收起当前，复位
                        packUpEditIndex = -1;
                        hideKeyboard();
                        closeEditView((View) v.getParent());
//                        doUnFoAmn();
                        m_topBar.requestFocus();
                    }else {
                        //收起上一个
                        int closeIndex = -1;
                        for (int i = 0; i < mRecyclerView.getChildCount() ; i++) {
                            View view = mRecyclerView.getChildAt(i);
                            if(((int)view.getTag()) == packUpEditIndex){
                                closeIndex = packUpEditIndex;
                            }
                        }
                        if(closeIndex != -1) {
                            closeEditView(mRecyclerView.getChildAt(closeIndex));
                        }
                        packUpEditIndex = pos;
                        openEditView((View) v.getParent());

                    }
                }
            }
        };
        private View.OnClickListener mPackUpOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag();
                ViewGroup parent = (ViewGroup) v.getParent();
                if (parent != null) {
                    if (packUpEditIndex == -1) {
                        //展开
                        packUpEditIndex = pos;
                        openEditView((View) v.getParent());
                    }else {
                        //收起上一个
                        int closeIndex = -1;
                        for (int i = 0; i < mRecyclerView.getChildCount() ; i++) {
                            View view = mRecyclerView.getChildAt(i);
                            if(((int)view.getTag()) == packUpEditIndex){
                                closeIndex = packUpEditIndex;
                            }
                        }
                        if(closeIndex != -1) {
                            closeEditView(mRecyclerView.getChildAt(closeIndex));
                        }
                        packUpEditIndex = pos;
                        openEditView((View) v.getParent());

                    }
                }
            }
        };


        private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final int pos = (int) v.getTag();
                if (hasFocus) {
                    if (packUpEditIndex == pos) {
//                        doPackUpAmn();
                    }
                }
            }
        };

        private VideoEditView.OnTextChange mOnTextChange = new VideoEditView.OnTextChange() {
            @Override
            public void onChange(String text, int tag) {
                if (tag >= 0 && tag < data.size()) {
//                    data.set(tag, text);
                    data.get(tag).text = text;
                }
            }
        };

        public TextAdapter(ArrayList<TextInfo> data) {
            super();
            this.data = data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutParams fl;

            FrameLayout mainFr = new FrameLayout(mContext);
            mainFr.setBackgroundColor(0x33000000);
            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(110));
            fl.setMargins(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(40), 0);
            mainFr.setLayoutParams(fl);
            VideoEditView packUpEditText = new VideoEditView(mContext);
            packUpEditText.setPadding(0,ShareData.PxToDpi_xhdpi(30),0,0);
            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(280));
            fl.rightMargin = ShareData.PxToDpi_xhdpi(100);
            fl.leftMargin = ShareData.PxToDpi_xhdpi(20);
            packUpEditText.setGravity(Gravity.TOP);
            packUpEditText.setLines(5);
            packUpEditText.setSingleLine(false);
            packUpEditText.setVisibility(View.GONE);
            packUpEditText.setLineSpacing(1.0f, 1.3f);
            packUpEditText.setEllipsize(TextUtils.TruncateAt.END);
            packUpEditText.setBackgroundDrawable(null);
            packUpEditText.setHorizontallyScrolling(false);
            packUpEditText.setLayoutParams(fl);
            mainFr.addView(packUpEditText);


            VideoEditView unfoEditText = new VideoEditView(mContext);
            unfoEditText.setPadding(0,ShareData.PxToDpi_xhdpi(30),0,0);
            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(110));
            fl.rightMargin = ShareData.PxToDpi_xhdpi(100);
            fl.leftMargin = ShareData.PxToDpi_xhdpi(20);
            unfoEditText.setGravity(Gravity.TOP);
            unfoEditText.setFocusableInTouchMode(false);
            unfoEditText.setFocusable(false);
            unfoEditText.setEllipsize(TextUtils.TruncateAt.END);
            unfoEditText.setBackgroundDrawable(null);
            unfoEditText.setHorizontallyScrolling(false);
            unfoEditText.setLayoutParams(fl);
            mainFr.addView(unfoEditText);
            //http://blog.csdn.net/xcookies/article/details/41277955
            unfoEditText.setSingleLine(true);
//            unfoEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT);


            View packUpView = new View(getContext());
            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(110));
            packUpView.setLayoutParams(fl);
            mainFr.addView(packUpView, fl);


            fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.RIGHT | Gravity.BOTTOM;
//            fl.bottomMargin = ShareData.PxToDpi_xhdpi(40);
            ImageView unfolImage = new ImageView(mContext);
            unfolImage.setPadding(0, 0, ShareData.PxToDpi_xhdpi(20), ShareData.PxToDpi_xhdpi(40));
            unfolImage.setId(R.id.unfolImage);
            unfolImage.setImageResource(R.drawable.edit_text_unfold);
            mainFr.addView(unfolImage, fl);

            fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            fl.rightMargin = ShareData.PxToDpi_xhdpi(80);
            ImageView clearImage = new ImageView(mContext);
            clearImage.setPadding(ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20), ShareData.PxToDpi_xhdpi(40));
            clearImage.setImageResource(R.drawable.edit_text_clear);
            clearImage.setVisibility(GONE);
            mainFr.addView(clearImage, fl);

            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(2));
            fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            fl.bottomMargin = ShareData.PxToDpi_xhdpi(100);
            fl.leftMargin = ShareData.PxToDpi_xhdpi(40);
            fl.rightMargin = ShareData.PxToDpi_xhdpi(40);
            View mView = new View(mContext);
            mView.setVisibility(GONE);
            mView.setId(R.id.linsView);
            mView.setBackgroundColor(Color.parseColor("#19ffffff"));
            mainFr.addView(mView, fl);

            MyViewHolder viewHolder = new MyViewHolder(mainFr);
            viewHolder.packUpEditText = packUpEditText;
            viewHolder.unfoEditText = unfoEditText;
            viewHolder.clearImage = clearImage;
            viewHolder.mView = mView;
            viewHolder.unfolImage = unfolImage;
            viewHolder.packUpView = packUpView;

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            MyViewHolder viewHolder = (MyViewHolder) holder;
            viewHolder.packUpEditText.setOnTextChange(mOnTextChange);
            viewHolder.packUpEditText.setTag(position);
            viewHolder.packUpEditText.setOnFocusChangeListener(mOnFocusChangeListener);
            viewHolder.unfoEditText.setTag(position);
            viewHolder.clearImage.setTag(position);
            viewHolder.mView.setTag(position);
            viewHolder.unfolImage.setTag(position);
            viewHolder.packUpView.setTag(position);
            viewHolder.itemView.setTag(position);

            viewHolder.packUpView.setOnClickListener(mPackUpOnClickListener);
            viewHolder.clearImage.setOnClickListener(mOnClearClickListener);
            viewHolder.unfolImage.setOnClickListener(mOnUnfoClickListener);
//            String text[] = data.get(position).text.split('\n');
            viewHolder.packUpEditText.setText(data.get(position).text);

            if (packUpEditIndex == position) {
                packUpViewHolder(viewHolder);
            } else {
                unfoViewHolder(viewHolder);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private void packUpViewHolder(final MyViewHolder viewHolder) {
            if (viewHolder != null) {
                viewHolder.unfoEditText.setVisibility(View.GONE);
                viewHolder.packUpEditText.setVisibility(View.VISIBLE);
//                viewHolder.packUpEditText.setSingleLine(false);
                viewHolder.packUpView.setClickable(false);
                viewHolder.mView.setVisibility(VISIBLE);
                viewHolder.unfolImage.setImageResource(R.drawable.edit_text_pack_up);
                viewHolder.clearImage.setVisibility(VISIBLE);
            }
        }


        private void unfoViewHolder(final MyViewHolder viewHolder) {
            if (viewHolder != null) {
                viewHolder.unfoEditText.setVisibility(View.VISIBLE);
                viewHolder.packUpEditText.setVisibility(View.GONE);
                setUnFoEditText(viewHolder.unfoEditText,data.get(viewHolder.getAdapterPosition()).text);
//                viewHolder.packUpEditText.setSingleLine(true);
                viewHolder.packUpEditText.setEllipsize(TextUtils.TruncateAt.END);
                viewHolder.unfolImage.setImageResource(R.drawable.edit_text_unfold);
                viewHolder.mView.setVisibility(GONE);
                viewHolder.clearImage.setVisibility(GONE);
                viewHolder.packUpView.setClickable(true);
            }
        }
        private void setUnFoEditText(EditText editText,String text)
        {
            int index = text.indexOf("\n");
            if(index == -1){
                editText.setText(text);
            }else if (index >= 0 && index < text.length()) {
                text = text.substring(0, index);
                editText.setText(text);
            }
        }

        public ArrayList<TextInfo> getData() {
            return data;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            public VideoEditView packUpEditText;
            public VideoEditView unfoEditText;
            public ImageView clearImage;
            public View mView;
            public ImageView unfolImage;
            public View packUpView;

            public MyViewHolder(View itemView) {
                super(itemView);
            }
        }

        private void openEditView(final View view)
        {
            ValueAnimator animator = ValueAnimator.ofInt(ShareData.PxToDpi_xhdpi(110), ShareData.PxToDpi_xhdpi(430));
            animator.setDuration(200);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int i = (int) animation.getAnimatedValue();
                    ViewGroup.LayoutParams lp = view.getLayoutParams();
                    lp.height = i;
                    view.setLayoutParams(lp);
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isDoingAnim = false;
                    MyViewHolder viewHolder = (MyViewHolder) mRecyclerView.getChildViewHolder(view);
                    viewHolder.mView.setVisibility(VISIBLE);
                    viewHolder.unfolImage.setImageResource(R.drawable.edit_text_pack_up);
                    viewHolder.clearImage.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    isDoingAnim = true;
                    MyViewHolder viewHolder = (MyViewHolder) mRecyclerView.getChildViewHolder(view);
//                    packUpViewHolder(viewHolder);
                    viewHolder.unfoEditText.setVisibility(View.GONE);
                    viewHolder.packUpEditText.setVisibility(View.VISIBLE);
                    viewHolder.packUpView.setClickable(false);

                }
            });
            animator.start();
        }

        private void closeEditView(final View view)
        {
            ValueAnimator animator = ValueAnimator.ofInt(ShareData.PxToDpi_xhdpi(430), ShareData.PxToDpi_xhdpi(110));
            animator.setDuration(200);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int i = (int) animation.getAnimatedValue();
                    ViewGroup.LayoutParams lp = view.getLayoutParams();
                    lp.height = i;
                    view.setLayoutParams(lp);
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isDoingAnim = false;
                    MyViewHolder viewHolder = (MyViewHolder) mRecyclerView.getChildViewHolder(view);
                    viewHolder.unfoEditText.setVisibility(VISIBLE);
                    setUnFoEditText(viewHolder.unfoEditText,data.get(viewHolder.getAdapterPosition()).text);
                    viewHolder.packUpEditText.setVisibility(GONE);
                    viewHolder.packUpView.setClickable(true);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    isDoingAnim = true;
                    MyViewHolder viewHolder = (MyViewHolder) mRecyclerView.getChildViewHolder(view);
                    viewHolder.unfolImage.setImageResource(R.drawable.edit_text_unfold);
                    viewHolder.mView.setVisibility(GONE);
                    viewHolder.clearImage.setVisibility(GONE);
                    viewHolder.packUpView.setClickable(true);
                    viewHolder.packUpEditText.setSelection(0);
                    m_topBar.requestFocus();
                }
            });
            animator.start();
        }
    }

    class TextInfo {
        String text;
        int maxLine;
        int maxNum;
    }

}
