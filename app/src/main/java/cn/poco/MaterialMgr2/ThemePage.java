package cn.poco.MaterialMgr2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.MaterialMgr2.site.ThemeIntroPageSite;
import cn.poco.MaterialMgr2.site.ThemePageSite;
import cn.poco.MaterialMgr2.site.ThemePageSite2;
import cn.poco.MaterialMgr2.site.ThemePageSite3;
import cn.poco.MaterialMgr2.site.ThemePageSite4;
import cn.poco.beautify.MyButtons2;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.resource.ResType;
import cn.poco.resource.ResourceMgr;
import cn.poco.setting.LanguagePage;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.ShareData;

/**
 * 主题首页
 */
public class ThemePage extends IPage {
    private static final String TAG = "主题列表页";
    private ThemePageSite m_site;
    private FrameLayout m_topBar;
    private FrameLayout m_classifyBar;
    private ImageView m_backBtn;
    private ImageView m_manageBtn;
    private LinearLayout m_titleFr;
    private ArrayList<MyButtons2> m_titles;
    private ArrayList<GroupInfo> m_ress;
    private ImageView m_scrollLine;

    private ViewPager m_pager;
    private ArrayList<RecylerViewV1> m_viewList;
    private int m_curSel = 0;

    private int m_topBarHeight;
    private int m_curClickPosition = -1;
    private boolean m_isRefreshing = false;
    private int m_refreshingIndex = -1;
    private boolean m_needRefresh = false;;

    private String m_textClassify = null;
    private boolean m_typeOnly = false;
    private ThemeIntroPage m_introPage;
    private TextView titleView;
    private  FrameLayout.LayoutParams fl;
    protected ThemePagerAdapter m_pagerAdapter;

    private MgrUtils.MyRefreshCb m_refreshCB = new MgrUtils.MyRefreshCb(new MgrUtils.MyCB2() {
        @Override
        public void OnFinish() {
            m_isRefreshing = false;
            View tempView = GetChildViewByPosition(m_curSel);
            if (tempView != null) {
                RecylerViewV1 view = (RecylerViewV1) tempView;
                if (view != null && view.getRefreshState() == RecylerViewV1.REFRESH_ING && m_refreshingIndex == m_curSel) {
                    view.fininshAnim1(false);
                }
                ResType type = null;
                if (m_ress != null) {
                    if (m_ress.size() > 0 && m_typeOnly && m_ress.get(0) != null) {
                        type = m_ress.get(0).m_type;
                    }
                    m_ress.clear();
                }
                m_ress = MgrUtils.ClassifyThemeInfos(getContext(), type, m_textClassify);

                view.getRecylerView().stopScroll();

                ThemeListAdapter adapter = (ThemeListAdapter) view.getAdapter();
                adapter.SetDatas(m_ress.get(m_curSel).m_resArr);
                adapter.notifyDataSetChanged();
                //	Toast.makeText(getContext(),"刷新完成！", Toast.LENGTH_SHORT).show();
            }
        }
    });
    private OnClickListener m_btnLst = new OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v == m_backBtn) {
                onBack();
            } else if (v == m_manageBtn) {
                MyBeautyStat.onClickByRes(R.string.素材主题_打开素材管理);
                HashMap<String, Object> params = new HashMap<>();
                params.put("typeOnly", m_typeOnly);
                if (m_typeOnly && !m_ress.isEmpty()) {
                    ResType type = m_ress.get(0).m_type;
                    Log.i(TAG, "onClick: " + type);
                    if (type == ResType.TEXT) {
                        if ("water".equals(m_textClassify)) {
                            type = ResType.TEXT_WATERMARK;
                        } else if ("attitude".equals(m_textClassify)) {
                            type = ResType.TEXT_ATTITUTE;
                        }else if ("watermark".equals(m_textClassify)){
                            type = ResType.VIEDO_WATERMARK;
                        }else if ("originality".equals(m_textClassify)){
                            type = ResType.VIEDO_ORIGINALITY;
                        }
                    }
                    params.put("type", type);
                }
                m_site.OpenManagePage(params,mContext);
            } else {
                int size = m_titles.size();
                for (int i = 0; i < size; i++) {
                    if (m_titles.get(i) == v) {
                        m_pager.setCurrentItem(i);
                    }
                }
            }
        }
    };
    private RecylerViewV1.RefreshImp m_refreshImp = new RecylerViewV1.RefreshImp() {
        @Override
        public void refresh() {
            if (m_isRefreshing == false) {
                m_isRefreshing = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        m_refreshingIndex = m_curSel;
                        ResourceMgr mgr = new ResourceMgr();
                        mgr.ReloadCloudRes(getContext());
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (m_refreshCB != null) {
                                    m_refreshCB.OnFinish();
                                }
                            }
                        });
                    }
                }).start();
            }
        }

        @Override
        public void refreshfinish() {

        }
    };
    private ThemeIntroPageSite m_introSite = new ThemeIntroPageSite() {
        @Override
        public void OnBack(HashMap<String, Object> params,Context context) {
            if (m_introPage != null) {
                ThemePage.this.removeView(m_introPage);
                m_introPage.onClose();
                m_introPage = null;
            }

            onPageResult(SiteID.THEME_INTRO_PAGE, params);
        }

        @Override
        public void OnResourceUse(HashMap<String, Object> params,Context context) {
            if (m_site instanceof ThemePageSite2 || m_site instanceof ThemePageSite3 || m_site instanceof ThemePageSite4) {
                boolean need_refresh = false;
                if (params.containsKey("need_refresh")) {
                    need_refresh = (Boolean) params.get("need_refresh");
                }
                if (!need_refresh) {
                    need_refresh = m_needRefresh;
                }
                params.put("need_refresh", need_refresh);

                //关闭主题详情页
                if (m_introPage != null) {
                    ThemePage.this.removeView(m_introPage);
                    m_introPage.onClose();
                    m_introPage = null;
                }
                m_site.OnBack(params,getContext());
            } else {
                super.OnResourceUse(params,context);
            }
        }
    };
    private ThemeListAdapter.ItemClickListener m_listItemClickListener = new ThemeListAdapter.ItemClickListener() {
        @Override
        public void onItemClick(View v, ThemeItemInfo info, int position) {
            m_curClickPosition = position;
            HashMap<String, Object> params = new HashMap<>();
            params.put("data", info);
            params.put("need_refresh", m_needRefresh);
            if (v != null) {
                int[] location = new int[2];
                v.getLocationOnScreen(location);

                params.put("hasAnim", true);
                params.put("centerX", location[0]);
                params.put("centerY", location[1]);
                params.put("viewH", v.getHeight());
            }
            OpenIntroPage(params);
//			m_site.OpenIntroPage(params);
        }

        @Override
        public void OnStateClick(View v, ThemeItemInfo info, int position) {
            if (info != null) {
                if (info.m_state != ThemeItemInfo.COMPLETE) {
                    onItemClick(v, info, position);
                }
            }
        }
    };

    public class ThemePagerAdapter extends PagerAdapter{
        private boolean m_needRefresh = false;
        @Override
        public int getCount() {
            return m_ress == null ? 0 : m_ress.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof RecylerViewV1) {
                container.removeView((RecylerViewV1) object);
                if (m_viewList != null) {
                    ((RecylerViewV1) object).setTag(null);
                    m_viewList.add((RecylerViewV1) object);
                }
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            RecylerViewV1 view = null;
            if (m_viewList.size() > 0) {
                view = m_viewList.remove(0);
            }
            if (view == null) {
                view = new RecylerViewV1(getContext());
                view.setLayoutManager(new LinearLayoutManager(getContext()));
            }
            view.setTag(position);
            container.addView(view);//添加页卡
            return view;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object)
        {
            super.setPrimaryItem(container, position, object);
            if(m_needRefresh)
            {
                m_needRefresh = false;
                if(m_pageChangeListener != null)
                {
                    m_pageChangeListener.onPageSelected(position);
                }
            }
        }

        public void setNeedRefresh(boolean refresh)
        {
            m_needRefresh = refresh;
        }
    }

    private ViewPager.OnPageChangeListener m_pageChangeListener = new ViewPager.OnPageChangeListener() {
        private int m_leftMargin;
        private float m_rateLeft;
        private float m_rateRight;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams) m_scrollLine.getLayoutParams();
            if (position == m_curSel) {
                fl.leftMargin = m_leftMargin + (int) (positionOffsetPixels * m_rateRight);
            } else if (position < m_curSel) {
                fl.leftMargin = m_leftMargin - (int) ((ShareData.m_screenWidth - positionOffsetPixels) * m_rateLeft);
            }
            m_scrollLine.setLayoutParams(fl);
        }

        @Override
        public void onPageSelected(int position) {
            m_scrollLine.setVisibility(GONE);
            if (m_curSel >= 0 && m_curSel < m_titles.size()) {
                m_titles.get(m_curSel).OnChoose(false);
                m_titles.get(m_curSel).OnChooseText(false);
            }

            if (position >= 0 && position < m_titles.size() && !m_typeOnly) {
                m_titles.get(position).OnChoose(true);
                m_titles.get(position).OnChooseText(true);
            }
            View tempView = GetChildViewByPosition(position);
            if (tempView != null) {
                RecylerViewV1 view = (RecylerViewV1) tempView;

                ThemeListAdapter adapter = (ThemeListAdapter)view.getAdapter();
                if(adapter == null){
                    adapter = new ThemeListAdapter(getContext());
                    view.setAdapter(adapter);
                    view.setRefreshCB(m_refreshImp);
                    adapter.SetOnItemClickListener(m_listItemClickListener);
                    adapter.SetDatas(m_ress.get(position).m_resArr);
                    adapter.notifyDataSetChanged();
                }

                m_curSel = position;
            }

            switch (m_ress.get(position).m_type) {
                case FILTER:
                    TongJi2.AddCountByRes(getContext(), R.integer.素材商店_滤镜主题列表);
                    MyBeautyStat.onClickByRes(R.string.素材主题_切换到滤镜主题);
                    break;
                case TEXT:
                    TongJi2.AddCountByRes(getContext(), R.integer.素材商店_文字主题列表);
                    MyBeautyStat.onClickByRes(R.string.素材主题_切换到水印主题);
                    break;
                case LIGHT_EFFECT:
                    TongJi2.AddCountByRes(getContext(), R.integer.素材商店_光效主题列表);
                    MyBeautyStat.onClickByRes(R.string.素材主题_切换到光效主题);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (m_typeOnly) {
                return;
            }
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                m_scrollLine.setVisibility(VISIBLE);
                int width = m_titles.get(m_curSel).getWidth();
                int height = ShareData.PxToDpi_xhdpi(5);
                Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);
                canvas.drawColor(0xffffc433);
                m_scrollLine.setImageBitmap(bmp);

                m_rateLeft = 0;
                m_rateRight = 0;
                int leftIndex = m_curSel - 1;
                if (leftIndex >= 0 && leftIndex < m_titles.size()) {
                    m_rateLeft = (m_titles.get(m_curSel).getLeft() - m_titles.get(leftIndex).getLeft()) / (float) ShareData.m_screenWidth;
                }
                int rigthIndex = m_curSel + 1;
                if (rigthIndex >= 0 && rigthIndex < m_titles.size()) {
                    m_rateRight = (m_titles.get(rigthIndex).getLeft() - m_titles.get(m_curSel).getLeft()) / (float) ShareData.m_screenWidth;
                }

                m_leftMargin = m_titleFr.getLeft() + m_titles.get(m_curSel).getLeft();
                FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams) m_scrollLine.getLayoutParams();
                fl.leftMargin = m_leftMargin;
                m_scrollLine.setLayoutParams(fl);

                m_titles.get(m_curSel).OnChoose(false);
            } else {
                m_titles.get(m_curSel).OnChoose(true);
                m_scrollLine.setVisibility(GONE);
            }
        }
    };
    private Context mContext;

    public ThemePage(Context context, BaseSite site) {
        super(context, site);
        m_site = (ThemePageSite) site;
        mContext = context;
        ShareData.InitData(context);
        m_topBarHeight = ShareData.PxToDpi_xhdpi(80);

        m_viewList = new ArrayList<>();
        m_titles = new ArrayList<>();
        InitUI(context);
        TongJiUtils.onPageStart(getContext(), TAG);
    }

    private void InitUI(Context context) {
        this.setBackgroundColor(0xff000000);

        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, m_topBarHeight);

        m_topBar = new FrameLayout(context);
        fl.gravity = Gravity.TOP;
        m_topBar.setLayoutParams(fl);
        this.addView(m_topBar);

        {
            m_backBtn = new ImageView(getContext());
            m_backBtn.setImageResource(R.drawable.framework_back_btn);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            m_backBtn.setLayoutParams(fl);
            m_topBar.addView(m_backBtn);
            m_backBtn.setOnClickListener(m_btnLst);


            titleView = new TextView(context);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            titleView.setGravity(Gravity.CENTER);
            fl.gravity = Gravity.CENTER;
            titleView.setLayoutParams(fl);
            titleView.setTextColor(Color.parseColor("#ffffff"));
            titleView.setText(R.string.material_store);
            m_topBar.addView(titleView);

            m_manageBtn = new ImageView(getContext());
            m_manageBtn.setImageResource(R.drawable.mgr_manage_btn);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            m_manageBtn.setLayoutParams(fl);
            m_topBar.addView(m_manageBtn);
            m_manageBtn.setOnClickListener(m_btnLst);
        }

        {
            m_classifyBar = new FrameLayout(context);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_topBarHeight);
            fl.gravity = Gravity.TOP;
            m_classifyBar.setLayoutParams(fl);
            fl.topMargin = m_topBarHeight;
            this.addView(m_classifyBar);

            m_titleFr = new LinearLayout(context);
            fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            fl.gravity = Gravity.CENTER;
            m_titleFr.setLayoutParams(fl);
            m_classifyBar.addView(m_titleFr);

            m_scrollLine = new ImageView(getContext());
            m_scrollLine.setVisibility(GONE);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.LEFT | Gravity.BOTTOM;
            m_scrollLine.setLayoutParams(fl);
            m_classifyBar.addView(m_scrollLine);

        }

        m_pagerAdapter = new ThemePagerAdapter();
        m_pager = new ViewPager(context);
        m_pager.addOnPageChangeListener(m_pageChangeListener);
        m_pager.setAdapter(m_pagerAdapter);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.TOP;
        fl.topMargin = m_topBarHeight + m_topBarHeight;
        m_pager.setLayoutParams(fl);
        this.addView(m_pager);
    }

    private void AddTitles(String title, int leftMargin) {
        LinearLayout.LayoutParams ll;
        int size = 14;
        if (!TagMgr.CheckTag(getContext(), LanguagePage.ENGLISH_TAGVALUE)) {
            size = 12;
        }
        MyButtons2 btn = new MyButtons2(getContext(), 0, title, 0, size);
        int width = (int) (ShareData.getScreenW() / 3.5);
        ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ll.setMargins(width/3,0,width/3,0);
        btn.setLayoutParams(ll);
        m_titleFr.addView(btn);
        btn.setOnClickListener(m_btnLst);
        btn.OnChoose(false);
        m_titles.add(btn);
    }

    public View GetChildViewByPosition(int position) {
        View out = null;
        if (m_pager != null) {
            View temp;
            int len = m_pager.getChildCount();
            for (int i = 0; i < len; i++) {
                temp = m_pager.getChildAt(i);
                if ((Integer) temp.getTag() == position) {
                    out = temp;
                    break;
                }
            }
        }

        return out;
    }

    private void OpenIntroPage(HashMap<String, Object> params) {
        if (m_introPage != null) {
            removeView(m_introPage);
            m_introPage.onClose();
            m_introPage = null;
        }
        m_introPage = new ThemeIntroPage(getContext(), m_introSite);
        m_introPage.SetData(params);
        addView(m_introPage);
    }

    private ResType mInitType = null;
    private int mInitId = -1;
    /**
     * type ResType
     * id int	//选中主题的id
     * typeOnly Boolean //是否只显示当前分类的主题
     * textType String //针对文字（水印、态度）
     *
     * @param params
     */
    @Override
    public void SetData(HashMap<String, Object> params) {
        if (params != null) {
            Object o = params.get("textType");
            if (o != null) {
                m_textClassify = (String) o;
            }
            o = params.get("typeOnly");
            if (o != null) {
                m_typeOnly = (Boolean) o;
            }
            o = params.get("type");
            if (o != null) {
                mInitType = (ResType) o;
            }
            o = params.get("id");
            if (o != null) {
                mInitId = Integer.parseInt(o + "");
            }

        }

        TongJi2.AddCountByRes(getContext(), R.integer.素材商店);

        m_ress = MgrUtils.ClassifyThemeInfos(getContext(), m_typeOnly ? mInitType : null, m_textClassify);
        ThemeItemInfo itemInfo = null;
        if (mInitType != null) {
            int size = m_ress.size();

            for (int i = 0; i < size; i++) {
                if (mInitType == m_ress.get(i).m_type) {
                    m_curSel = i;
                    break;
                }
            }
        }
        if (mInitId != -1) {
            int size = m_ress.get(m_curSel).m_resArr.size();
            for (int i = 0; i < size; i++) {
                itemInfo = (ThemeItemInfo) m_ress.get(m_curSel).m_resArr.get(i);
                if (mInitId == itemInfo.m_uri) {
                    m_curClickPosition = i;
                    break;
                }
            }
        }

        m_pagerAdapter.setNeedRefresh(true);
        m_pagerAdapter.notifyDataSetChanged();
        m_pager.setCurrentItem(m_curSel, false);

        if (m_curClickPosition != -1 && itemInfo != null) {
            m_listItemClickListener.onItemClick(null, itemInfo, m_curClickPosition);
        }

        if (m_typeOnly) {

            if (mInitType == ResType.TEXT || mInitType  == ResType.AUDIO_TEXT
                    || mInitType == ResType.TEXT_ATTITUTE|| mInitType == ResType.TEXT_WATERMARK) {
                //  AddTitles(getResources().getString(R.string.Watermark), 0);
                m_classifyBar.setVisibility(GONE);
                titleView.setText(getResources().getString(R.string.Theme_watermark));
            } else if (mInitType == ResType.FILTER) {
                //  AddTitles(getResources().getString(R.string.Filters), 0);
                m_classifyBar.setVisibility(GONE);
                titleView.setText(getResources().getString(R.string.Filters));

            } else if (mInitType == ResType.LIGHT_EFFECT) {
                // AddTitles(getResources().getString(R.string.Light), 0);
                m_classifyBar.setVisibility(GONE);
                titleView.setText(getResources().getString(R.string.Light));
            }
            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, m_topBarHeight);
            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.TOP;
            fl.topMargin = m_topBarHeight ;
            m_pager.setLayoutParams(fl);
        } else {
            AddTitles(getResources().getString(R.string.Filters), ShareData.PxToDpi_xhdpi(140));
            AddTitles(getResources().getString(R.string.Theme_watermark), ShareData.PxToDpi_xhdpi(140));
            AddTitles(getResources().getString(R.string.Light), ShareData.PxToDpi_xhdpi(140));
        }

        if (m_curSel >= 0 && m_curSel < m_titles.size() && !m_typeOnly) {
            m_titles.get(m_curSel).OnChoose(true);
            m_titles.get(m_curSel).OnChooseText(true);
        }
    }

    @Override
    public void onPageResult(int siteID, HashMap<String, Object> params) {
        super.onPageResult(siteID, params);
        if (siteID == SiteID.THEME_INTRO_PAGE) {
            if (params != null) {
                if (params.containsKey("need_refresh")) {
                    boolean refresh = (Boolean) params.get("need_refresh");
                    m_needRefresh = refresh;
                    if (refresh) {
                        View tempView = GetChildViewByPosition(m_curSel);
                        if (tempView != null) {
                            ThemeListAdapter adapter = (ThemeListAdapter) ((RecylerViewV1) tempView).getAdapter();
                            if (adapter != null) {
                                adapter.notifyItemChanged(m_curClickPosition);
                            }
                        }
                    }
                }
            }
        }
        if (siteID == SiteID.MANAGE) {
            if (params != null) {
                if (params.containsKey("need_refresh")) {
                    m_needRefresh = (Boolean) params.get("need_refresh");
                    if(m_needRefresh){
                        m_refreshCB.OnFinish();
                    }
                }
            }
        }
    }

    @Override
    public void onBack() {
        if (m_introPage != null) {
            m_introPage.onBack();
            return;
        }
        HashMap<String, Object> params = new HashMap<>();
        params.put("need_refresh", m_needRefresh);
        MyBeautyStat.onClickByRes(R.string.素材主题_退出素材商店);
        m_site.OnBack(params,mContext);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (m_introPage != null) {
            m_introPage.onActivityResult(requestCode, resultCode, data);
        }
        return super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClose() {
        if (m_pager != null) {
            int len = m_pager.getChildCount();
            ThemeListAdapter adapter;
            RecylerViewV1 view;
            for (int i = 0; i < len; i++) {
                view = (RecylerViewV1) m_pager.getChildAt(i);
                adapter = (ThemeListAdapter) view.getAdapter();
                if (adapter != null) {
                    adapter.SetOnItemClickListener(null);
                    adapter.release();
                    adapter.notifyDataSetChanged();
                }
                view.setAdapter(null);
            }
            m_pager.setAdapter(null);
            m_pager.removeOnPageChangeListener(m_pageChangeListener);
            m_pager.removeAllViews();
            m_pager = null;
            m_pageChangeListener = null;
            m_pagerAdapter = null;
        }
        if (m_ress != null) {
            m_ress.clear();
            m_ress = null;
        }
        if (m_refreshCB != null) {
            m_refreshCB.ClearAll();
            m_refreshCB = null;
        }
        this.removeAllViews();
        this.clearFocus();
        super.onClose();
        TongJiUtils.onPageEnd(getContext(), TAG);
    }

    @Override
    public void onResume() {
        TongJiUtils.onPageResume(getContext(), TAG);
        super.onResume();
    }

    @Override
    public void onPause() {
        TongJiUtils.onPagePause(getContext(), TAG);
        super.onPause();
    }
}
