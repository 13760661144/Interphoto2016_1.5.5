package cn.poco.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.camera.site.CompositionPageSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.home.PageNumComponent;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.tianutils.ShareData;
import cn.poco.ui.ElasticHorizontalScrollView;
import cn.poco.widget.PressedButton;

/**
 * Created by zwq on 2016/05/26 17:24.<br/><br/>
 * 拍照--构图页
 */
public class CompositionPage extends IPage implements View.OnClickListener {

    private static final String TAG = "bbb";//CompositionPage.class.getName();
    private CompositionPageSite mPageSite;
    private RelativeLayout rootView;

    private PressedButton mBackBtn;
    private ElasticHorizontalScrollView mThemeContainer;
    private LinearLayout themeItemLayout;
    private TextView mUseBtn;
    private LinearLayout mThemeLayout;

    private String[] themeTexts = new String[]{getResources().getString(R.string.Basic), getResources().getString(R.string.Food), getResources().getString(R.string.Landscape), getResources().getString(R.string.Group)};
    private HashMap<String, ArrayList<TeachInfo>> themeInfoList;
    private ArrayList<TeachInfo> baseList;

    private int themeSelected = 0;
    private int currentThemeCourseId = 0;
    private ThemeItem selectedItem;
    private ViewPager mViewPager;
    private PageNumComponent pageNumComponent;

    private Thread mThread;
    private int mBgResId = -1;
    private Bitmap mBgBitmap;


    private boolean isLeftToRight = false;  //从左向右滑动

    private boolean isSelectTheme = false;
    private int currentThemeItemCount = 0;
    private int isSelectItem = 0;
    private int lastSelected = 0;
    private boolean isBack = false;

    public CompositionPage(Context context, BaseSite site) {
        super(context, site);
        mPageSite = (CompositionPageSite) site;
        rootView = new RelativeLayout(context);
        addView(rootView);

        initView();
    }

    /**
     * 按压切换颜色
     *
     * @param normal
     * @param pressed
     * @return
     */
    public static StateListDrawable colorPressedDrawable(int normal, int pressed) {
        StateListDrawable selector = new StateListDrawable();
        selector.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressed));
        selector.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(normal));
        return selector;
    }

    private void initView() {
        rootView.setBackgroundColor(0xff000000);
        rootView.setOnClickListener(this);

        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(100), ShareData.PxToDpi_xhdpi(100));
        mBackBtn = new PressedButton(getContext());
        mBackBtn.setId(R.id.camera_back_btn);
        mBackBtn.setButtonImage(cn.poco.interphoto2.R.drawable.framework_back_btn, cn.poco.interphoto2.R.drawable.framework_back_btn);
        mBackBtn.setOnClickListener(this);
        rootView.addView(mBackBtn, rParams);

        rParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(100));
        rParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rParams.setMargins(ShareData.PxToDpi_xhdpi(100), 0, ShareData.PxToDpi_xhdpi(100), 0);
        mThemeContainer = new ElasticHorizontalScrollView(getContext());
        mThemeContainer.setId(R.id.camera_theme_container);
        mThemeContainer.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mThemeContainer.setHorizontalScrollBarEnabled(false);
        rootView.addView(mThemeContainer, rParams);

        rParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        themeItemLayout = new LinearLayout(getContext());
        themeItemLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mThemeContainer.addView(themeItemLayout, rParams);
        mThemeContainer.onFinishAddView(themeItemLayout);
        initThemeItem(themeItemLayout);

        rParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
        rParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mUseBtn = new TextView(getContext());
        mUseBtn.setId(R.id.camera_use_btn);
        mUseBtn.setGravity(Gravity.CENTER);
        mUseBtn.setTextColor(Color.WHITE);
        mUseBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        mUseBtn.setText(getResources().getString(R.string.usetutorial));
        mUseBtn.setBackgroundDrawable(colorPressedDrawable(0xffffc433, 0xffffd433));
        mUseBtn.setOnClickListener(this);
        rootView.addView(mUseBtn, rParams);

        rParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        rParams.addRule(RelativeLayout.BELOW, R.id.camera_back_btn);
        rParams.addRule(RelativeLayout.ABOVE, R.id.camera_use_btn);
        mThemeLayout = new LinearLayout(getContext());
        mThemeLayout.setOrientation(LinearLayout.VERTICAL);
        mThemeLayout.setBackgroundColor(0xff393939);
        mThemeLayout.setOnClickListener(this);
        rootView.addView(mThemeLayout, rParams);

        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        mViewPager = new ViewPager(getContext());
//        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mViewPager.setAdapter(new MyPagerAdapter());
        mViewPager.addOnPageChangeListener(new MyPagerChangeListener());
        mViewPager.setOnTouchListener(new MyTouchListener());
        mViewPager.setCurrentItem(1);
        mThemeLayout.addView(mViewPager, lParams);

        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lParams.topMargin = ShareData.PxToDpi_xhdpi(30);
        lParams.bottomMargin = ShareData.PxToDpi_xhdpi(30);
        lParams.gravity = Gravity.CENTER_HORIZONTAL;
        pageNumComponent = new PageNumComponent(getContext());
        pageNumComponent.page_num_out = cn.poco.interphoto2.R.drawable.homepage_dot_out;
        pageNumComponent.page_num_over = cn.poco.interphoto2.R.drawable.homepage_dot_over;
        pageNumComponent.page_num_margin = ShareData.PxToDpi_xhdpi(8);
        mThemeLayout.addView(pageNumComponent, lParams);

    }

    private void initData() {
        if (baseList == null) {
            baseList = new ArrayList<TeachInfo>();
        }
        if (baseList.isEmpty()) {
            //new String[]{"基础", "美食", "风景", "合影"};
            baseList.add(new TeachInfo("0", cn.poco.interphoto2.R.drawable.teach_base_1_preview, cn.poco.interphoto2.R.drawable.teach_base_1_mask, cn.poco.interphoto2.R.integer.基础1, true, false, 0));
            baseList.add(new TeachInfo("1", cn.poco.interphoto2.R.drawable.teach_base_2_preview, cn.poco.interphoto2.R.drawable.teach_base_2_mask, cn.poco.interphoto2.R.integer.基础2));
            baseList.add(new TeachInfo("2", cn.poco.interphoto2.R.drawable.teach_base_3_preview, cn.poco.interphoto2.R.drawable.teach_base_3_mask, cn.poco.interphoto2.R.integer.基础3, false, false, 0));
            baseList.add(new TeachInfo("3", cn.poco.interphoto2.R.drawable.teach_base_4_preview, cn.poco.interphoto2.R.drawable.teach_base_4_mask, cn.poco.interphoto2.R.integer.基础4, false, false, 0));
            baseList.add(new TeachInfo("4", cn.poco.interphoto2.R.drawable.teach_base_5_preview, cn.poco.interphoto2.R.drawable.teach_base_5_mask, cn.poco.interphoto2.R.integer.基础5, false, true, 0));

            baseList.add(new TeachInfo("5", cn.poco.interphoto2.R.drawable.teach_cate_1_preview, cn.poco.interphoto2.R.drawable.teach_cate_1_mask, cn.poco.interphoto2.R.integer.美食1, true, false, 1));
            baseList.add(new TeachInfo("6", cn.poco.interphoto2.R.drawable.teach_cate_2_preview, cn.poco.interphoto2.R.drawable.teach_cate_2_mask, cn.poco.interphoto2.R.integer.美食2, false, false, 1));
            baseList.add(new TeachInfo("7", cn.poco.interphoto2.R.drawable.teach_cate_3_preview, cn.poco.interphoto2.R.drawable.teach_cate_3_mask, cn.poco.interphoto2.R.integer.美食3, false, false, 1));
            baseList.add(new TeachInfo("8", cn.poco.interphoto2.R.drawable.teach_cate_4_preview, cn.poco.interphoto2.R.drawable.teach_cate_4_mask, cn.poco.interphoto2.R.integer.美食4, false, false, 1));
            baseList.add(new TeachInfo("9", cn.poco.interphoto2.R.drawable.teach_cate_5_preview, cn.poco.interphoto2.R.drawable.teach_cate_5_mask, cn.poco.interphoto2.R.integer.美食5, false, false, 1));
            baseList.add(new TeachInfo("10", cn.poco.interphoto2.R.drawable.teach_cate_6_preview, cn.poco.interphoto2.R.drawable.teach_cate_6_mask, cn.poco.interphoto2.R.integer.美食6, false, true, 1));


            baseList.add(new TeachInfo("11", cn.poco.interphoto2.R.drawable.teach_scenery_1_preview, cn.poco.interphoto2.R.drawable.teach_scenery_1_mask, cn.poco.interphoto2.R.integer.风光1, true, false, 2));
            baseList.add(new TeachInfo("12", cn.poco.interphoto2.R.drawable.teach_scenery_2_preview, cn.poco.interphoto2.R.drawable.teach_scenery_2_mask, cn.poco.interphoto2.R.integer.风光2, false, false, 2));
            baseList.add(new TeachInfo("13", cn.poco.interphoto2.R.drawable.teach_scenery_3_preview, cn.poco.interphoto2.R.drawable.teach_scenery_3_mask, cn.poco.interphoto2.R.integer.风光3, false, true, 2));

            baseList.add(new TeachInfo("14", cn.poco.interphoto2.R.drawable.teach_groupphoto_1_preview, cn.poco.interphoto2.R.drawable.teach_groupphoto_1_mask, cn.poco.interphoto2.R.integer.合照1, true, false, 3));
            baseList.add(new TeachInfo("15", cn.poco.interphoto2.R.drawable.teach_groupphoto_2_preview, cn.poco.interphoto2.R.drawable.teach_groupphoto_2_mask, cn.poco.interphoto2.R.integer.合照2, false, false, 3));
            baseList.add(new TeachInfo("16", cn.poco.interphoto2.R.drawable.teach_groupphoto_3_preview, cn.poco.interphoto2.R.drawable.teach_groupphoto_3_mask, cn.poco.interphoto2.R.integer.合照3, false, true, 3));

        }
    }

    @Override
    public void SetData(HashMap<String, Object> params) {

        isBack = false;
        Log.v("themeCourseId", "" + params.get("themeCourseId"));
        if (params != null) {
            if (params.containsKey("bgBmp")) {
                mBgBitmap = (Bitmap) params.get("bgBmp");
                mThemeLayout.setBackgroundDrawable(new BitmapDrawable(mBgBitmap));
            }
            if (params.containsKey("themeSelected")) {
                themeSelected = (Integer) params.get("themeSelected");
            }
            if (params.containsKey("themeCourseId")) {
                currentThemeCourseId = (Integer) params.get("themeCourseId");
            }
            if (params.containsKey("isSelectItem")) {
                isSelectItem = (Integer) params.get("isSelectItem");
            }
        }
        //初始化资源
        initData();
        ((MyPagerAdapter) mViewPager.getAdapter()).notifyDataSetChanged(false);
        if (themeItemLayout != null && themeSelected < themeItemLayout.getChildCount()) {
            //  themeItemLayout.getChildAt(themeSelected).performClick()
            selectedItem = (ThemeItem) themeItemLayout.getChildAt(themeSelected);
            selectedItem.setSelected(true);
            Log.v("isSelectItem", "isSelectItem" + isSelectItem);
            mViewPager.setCurrentItem(isSelectItem);

            for (int i = 0; i < baseList.size(); i++) {

                if (baseList.get(i).getTheme() == themeSelected) {
                    currentThemeItemCount++;
                }

            }
        }

        Log.v("pageCount", "" + currentThemeItemCount);
        if (pageNumComponent != null) {
            pageNumComponent.UpdatePageNum(currentThemeCourseId, currentThemeItemCount);
        }


    }

    @Override
    public void onBack() {
        //销毁资源
        if (mBgBitmap != null && !mBgBitmap.isRecycled()) {
            mBgBitmap.recycle();
            mBgBitmap = null;
        }
        mThemeLayout.setBackgroundColor(0xff393939);
        if (mPageSite != null) {
            mPageSite.onBack(getContext(), null);
        }
        MyBeautyStat.onClickByRes(R.string.选择构图_退出选择构图);

    }

    @Override
    public void onClose() {
        super.onClose();
        if (mViewPager != null) {
            mViewPager.removeAllViews();
            mViewPager = null;
        }
        System.gc();
    }

    @Override
    public void onClick(View v) {
        if (v == mBackBtn) {
            onBack();

        } else if (v == mUseBtn) {
            // ArrayList<TeachInfo> list = getSelectedList();
            ArrayList<TeachInfo> list = baseList;
            if (list != null) {
                TeachInfo teachInfo = list.get(isSelectItem);
                if (teachInfo != null) {
                    Log.v("TeachInfo", "" + teachInfo.getId());

                    TongJi2.AddCountByRes(getContext(), teachInfo.getTongjiId());
                    MyBeautyStat.onClickByRes(R.string.选择构图_使用该构图);
                    Object res = teachInfo.getMaskPic();
                    if (res != null && res instanceof Integer) {
                        int resId = Integer.parseInt(res.toString());
                        if (mPageSite != null) {
                            HashMap<String, Object> params = new HashMap<String, Object>();
                            params.put("themeName", themeTexts[teachInfo.getTheme()]);
                            params.put("themeSelected", teachInfo.getTheme());
                            params.put("themeCourseId", currentThemeCourseId);
                            params.put("isSelectItem", isSelectItem);
                            params.put("themeBmp", getScaleBitmap(resId));
                            mPageSite.onBack(getContext(), params);
                        }
                    }
                }
            }

        } else {
            isSelectTheme = true;

            currentThemeItemCount = 0;
            if (v.getTag() != null) {

                String tag = v.getTag().toString();
                if (tag != null && !tag.trim().equals("")) {
                    int index = -1;
                    try {
                        index = Integer.parseInt(tag);
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }

                    if (index != -1) {
                        String theme = themeTexts[index];

                        if (theme != null) {
                            if (theme.equals(getResources().getString(R.string.Basic))) {
                                TongJi2.AddCountByRes(getContext(), cn.poco.interphoto2.R.integer.拍照_构图线);
                                MyBeautyStat.onClickByRes(R.string.选择构图_基础);
                            } else if (theme.equals(getResources().getString(R.string.Landscape))) {
                                TongJi2.AddCountByRes(getContext(), cn.poco.interphoto2.R.integer.拍照_构图线_风光);
                                MyBeautyStat.onClickByRes(R.string.选择构图_风光);
                            } else if (theme.equals(getResources().getString(R.string.Group))) {
                                MyBeautyStat.onClickByRes(R.string.选择构图_合照);
                                TongJi2.AddCountByRes(getContext(), cn.poco.interphoto2.R.integer.拍照_构图线_合影);
                            } else if (theme.equals(getResources().getString(R.string.Food))) {
                                MyBeautyStat.onClickByRes(R.string.选择构图_美食);
                                TongJi2.AddCountByRes(getContext(), R.integer.拍照_构图线_美食);
                            }
                        }
                        selectedItem = (ThemeItem) ((LinearLayout) v.getParent()).getChildAt(themeSelected);
                        selectedItem.setSelected(false);

                        selectedItem = (ThemeItem) v;
                        selectedItem.setSelected(true);

                        themeSelected = index;
                        Log.v("index", "" + index);


                        for (int i = 0; i < baseList.size(); i++) {

                            if (baseList.get(i).getTheme() == themeSelected) {
                                currentThemeItemCount++;
                            }
                            if (baseList.get(i).getTheme() == themeSelected) {
                                if (baseList.get(i).isFistItemId()) {
                                    mViewPager.setCurrentItem(i, false);
                                }
                            }
                        }

                        mViewPager.getAdapter().notifyDataSetChanged();
                        if (pageNumComponent != null) {
                            currentThemeCourseId = 0;
                            pageNumComponent.UpdatePageNum(currentThemeCourseId, currentThemeItemCount);
                        }

//                        scrollToCenter();
                    }

                }
            }
        }
    }

    public void initThemeItem(LinearLayout view) {
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < themeTexts.length; i++) {
            ThemeItem item = new ThemeItem(getContext());
            item.setTag("" + i);
            item.setItemName(themeTexts[i]);
        /*    if (i == themeSelected) {// 选中状态
                item.setSelected(true);

            } else {// 未选中
                item.setSelected(false);
            }*/
            item.setOnClickListener(this);
            view.addView(item, lParams);
        }
    }

    private Bitmap getScaleBitmap(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resId, options);
        int width = options.outWidth;
        int height = options.outHeight;
        float whRatio = height * 1.0f / width;
        if (width > ShareData.getScreenW()) {
            if (ShareData.getScreenH() > 960) {//960x540
                width = ShareData.getScreenW();
                height = (int) (width * whRatio);
            } else {
                width = (int) (ShareData.getScreenW() * 1.0f / 4 * 3);
                height = (int) (width * whRatio);
            }
        }
        options.inJustDecodeBounds = false;
//        options.inSampleSize = 2;
        Bitmap temp = BitmapFactory.decodeResource(getResources(), resId, options);
        Bitmap src = null;
        if (temp != null && !temp.isRecycled()) {
            src = Bitmap.createScaledBitmap(temp, width, height, true);
            temp = null;
        }
        return src;
    }

    class ThemeItem extends RelativeLayout {

        private TextView mItemName;
        private TextView mIndicator;

        public ThemeItem(Context context) {
            super(context);
            initItemView();
        }

        private void initItemView() {
            LayoutParams rParams = new LayoutParams(LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(100));
            rParams.setMargins(ShareData.PxToDpi_xhdpi(35), 0, ShareData.PxToDpi_xhdpi(35), 0);
            mItemName = new TextView(getContext());
            mItemName.setId(R.id.camera_theme_item);
            mItemName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            mItemName.setTextColor(Color.WHITE);
            mItemName.setGravity(Gravity.CENTER_VERTICAL);
            addView(mItemName, rParams);

            rParams = new LayoutParams(LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(6));
            rParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.camera_theme_item);
            rParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.camera_theme_item);
            rParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.camera_theme_item);
            mIndicator = new TextView(getContext());
            addView(mIndicator, rParams);
        }

        public void setItemName(String name) {
            if (mItemName != null) {
                mItemName.setText(name);
            }
        }

        public void setSelected(boolean selected) {
            if (selected) {
                mIndicator.setBackgroundColor(Color.WHITE);


            } else {
                mIndicator.setBackgroundColor(0x00000000);


            }
        }

    }

    class MyPagerAdapter extends PagerAdapter {

        private boolean recycle = true;

        @Override
        public int getCount() {
            int count = 0;
            if (baseList == null) {
                count = 0;
            } else {
                ArrayList<TeachInfo> item = baseList;
                if (item == null) {
                    count = 0;
                } else {
                    count = item.size();
                }
            }
//            Log.i(TAG, ""+themeSelected+" "+currentThemeCourseId+" "+count);
            return count;
        }
        public void notifyDataSetChanged(boolean needRecycle) {

            recycle = needRecycle;
            super.notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if (getCount() > 0) {
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            Log.i(TAG, "destroyItem  "+position+" "+recycle);
            if (object != null) {
                if (object instanceof ImageView) {
                    ((ImageView) object).setImageBitmap(null);
                }
                container.removeView((View) object);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            recycle = true;
            ImageView image = new ImageView(getContext());
            int resId = Integer.parseInt(baseList.get(position).getPreviewPic().toString());
            image.setImageBitmap(getScaleBitmap(resId));
            container.addView(image);
            return image;
        }
    }

    class MyTouchListener implements ViewPager.OnTouchListener {
        float x1 = 0;
        float x2 = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //当手指按下的时候
                x1 = event.getX();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //当手指离开的时候
                x2 = event.getX();
                if (x1 - x2 > 50) {
                    //   向左滑
                    isLeftToRight = false;

                } else if (x2 - x1 > 50) {
                    //   向右滑

                    isLeftToRight = true;
                }
            }
            return onTouchEvent(event);
        }
    }

    class MyPagerChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            isSelectTheme = true;
            isBack = true;
        }

        @Override
        public void onPageSelected(final int position) {
            int fist = 0, end = 0;
            if (baseList.get(position).getTheme() != themeSelected) {
                int lastThemeSelected = themeSelected;
                lastSelected = lastThemeSelected;
                themeSelected = baseList.get(position).getTheme();
                selectedItem = (ThemeItem) themeItemLayout.getChildAt(lastThemeSelected);
                selectedItem.setSelected(false);
                selectedItem = (ThemeItem) themeItemLayout.getChildAt(themeSelected);
                selectedItem.setSelected(true);

                for (int i = 0; i < baseList.size(); i++) {
                    if (baseList.get(i).getTheme() == themeSelected) {

                        if (baseList.get(i).isFistItemId()) {
                            fist = Integer.parseInt(baseList.get(i).getId());
                        }
                    }
                    if (baseList.get(i).getTheme() == themeSelected) {
                        if (baseList.get(i).isEndItemId()) {
                            end = Integer.parseInt(baseList.get(i).getId());
                            currentThemeItemCount = (end - fist) + 1;
                        }
                    }
                }
                currentThemeCourseId = -1;
                if (lastThemeSelected > themeSelected) {
                    currentThemeCourseId = currentThemeItemCount;
                }
            }

            if (isLeftToRight) {
                currentThemeCourseId--;
            } else {
                if (isBack) {
                    currentThemeCourseId++;
                }
            }
            Log.v("currentThemeCourseId", "ssssssss" + currentThemeCourseId);
            if (pageNumComponent != null) {
                pageNumComponent.UpdatePageNum(currentThemeCourseId, currentThemeItemCount);
            }
            isSelectItem = position;
        }


        @Override
        public void onPageScrollStateChanged(int state) {

        }

    }


}