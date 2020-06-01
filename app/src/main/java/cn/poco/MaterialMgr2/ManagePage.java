package cn.poco.MaterialMgr2;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.MaterialMgr2.site.ManagePageSite;
import cn.poco.beautify.MyButtons2;
import cn.poco.beautify.VerticalImageSpan;
import cn.poco.draglistview.DragItemAdapter;
import cn.poco.draglistview.DragListView;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.resource.BaseRes;
import cn.poco.resource.FilterRes;
import cn.poco.resource.FilterResMgr2;
import cn.poco.resource.LightEffectRes;
import cn.poco.resource.LightEffectResMgr2;
import cn.poco.resource.MusicRes;
import cn.poco.resource.MusicResMgr2;
import cn.poco.resource.ResType;
import cn.poco.resource.ResourceUtils;
import cn.poco.resource.TextRes;
import cn.poco.resource.TextResMgr2;
import cn.poco.resource.VideoTextRes;
import cn.poco.resource.VideoTextResMgr2;
import cn.poco.resource.database.ResourseDatabase;
import cn.poco.setting.LanguagePage;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.ShareData;
import cn.poco.ui.ElasticHorizontalScrollView;
import cn.poco.utils.InterphotoDlg;

/**
 * Created by admin on 2016/9/9.
 */
public class ManagePage extends IPage {
    private static final String TAG = "素材管理页";
    private final int WATERMARK = 1;//水印
    private final int ORIGINALITY = 2;//创意 态度
    private final int VIDEO_WATERMARK = 2;//视频水印
    private final int PHOTT_WATERMARK = 1;//照片水印
    BaseRes m_deleteRes;
    int deletePosition;
    int m_deleteResId;
    private ManagePageSite m_site;
    private FrameLayout m_topBar;
    private FrameLayout m_classifyBar;
    private ImageView m_backBtn;
    private ImageView m_scrollLine;
    private LinearLayout m_titleFr;
    private FrameLayout m_watermarkClassify;
    private ArrayList<MyButtons2> m_titles;
    private TextView m_tip;
    private ArrayList<GroupInfo> m_ress;
    private int m_topBarHeight;
    private int m_curSel = 0;
    private ArrayList<DragListView> m_viewList;
    private TextView videoWatermarkBtn, videoOriginalityBtn;
    private FrameLayout.LayoutParams fl;
    private RoundColorDrawable roundColorDrawable = new RoundColorDrawable(0x80666666);
    private RoundColorDrawable roundColorDrawable1 = new RoundColorDrawable(Color.BLACK);
    private List<GroupInfo> list;
    private boolean isSel = false;
    private InterphotoDlg dialog;
    private boolean m_needRefresh = false;
    private ViewPager m_pager;
    private boolean m_typeOnly = false;
    private boolean isDrawListner = true;//是否拖动item
    private TextView titleView;
    private ManageListAdapter.ClickListener m_listItemClickLst = new ManageListAdapter.ClickListener() {
        @Override
        public void onHideBtn(View view, BaseRes res, int position) {

            m_deleteRes = res;
            deletePosition = position;
            m_deleteResId = res.m_id;
            deleteRes();
        }
    };

    private OnClickListener m_btnLst = new OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.i(TAG, "onClick: " + isDrawListner);
            if (v == m_backBtn) {
                onBack();
            } else if (v == videoOriginalityBtn) {
                if (isDrawListner) {
                    videoOriginalityBtn.setBackground(roundColorDrawable);
                    videoWatermarkBtn.setBackground(roundColorDrawable1);
                    Log.i(TAG, "onPageSelected: " + m_curSel);
                    if (m_curSel == VIDEO_WATERMARK) {
                        MyBeautyStat.onClickByRes(R.string.视频水印管理_选择创意);
                        selectLabel(m_curSel, ResType.VIEDO_ORIGINALITY, ORIGINALITY);
                        isSel = true;
                    } else if (m_curSel == PHOTT_WATERMARK) {
                        isSel = true;
                        MyBeautyStat.onClickByRes(R.string.照片水印管理_选择态度);
                        Log.i(TAG, "onPageSelected: " + m_curSel);
                        selectLabel(m_curSel, ResType.TEXT_ATTITUTE, ORIGINALITY);
                    }
                }

            } else if (v == videoWatermarkBtn) {
                if (isDrawListner) {
                    videoWatermarkBtn.setBackground(roundColorDrawable);
                    videoOriginalityBtn.setBackground(roundColorDrawable1);
                    isSel  = false;
                    if (m_curSel == VIDEO_WATERMARK) {
                        isSel = false;
                        MyBeautyStat.onClickByRes(R.string.视频水印管理_选择水印);
                        selectLabel(m_curSel, ResType.VIEDO_WATERMARK, WATERMARK);
                    } else if (m_curSel == PHOTT_WATERMARK) {
                        isSel = false;
                        MyBeautyStat.onClickByRes(R.string.照片水印管理_选择水印);
                        selectLabel(m_curSel, ResType.TEXT_WATERMARK, WATERMARK);
                    }
                }
            } else {
                if (isDrawListner) {
                    int size = m_titles.size();
                    for (int i = 0; i < size; i++) {
                        if (m_titles.get(i) == v) {
                            m_pager.setCurrentItem(i);
                        }
                    }
                }

            }
        }
    };
    private DragListView.DragListListener m_drawListener = new DragListView.DragListListener() {
        @Override
        public void onItemDragStarted(int position) {
            isDrawListner = false;
        }

        @Override
        public void onItemDragging(int itemPosition, float x, float y) {

        }

        @Override
        public void onItemDragEnded(int fromPosition, int toPosition) {

            if (m_curSel >= 0 && m_curSel < m_ress.size() && m_ress != null) {
                GroupInfo info;
                if (m_curSel == VIDEO_WATERMARK && isSel) {
                    info = list.get(0);
                } else if (m_curSel == PHOTT_WATERMARK && isSel) {

                    info = list.get(0);
                } else {
                    info = m_ress.get(m_curSel);
                }
                switch (info.m_type) {
                    case FILTER: {
                        m_needRefresh = true;
                        TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_滤镜管理_排序);
                        MyBeautyStat.onClickByRes(R.string.滤镜管理_排序);

                        FilterRes itemInfo = (FilterRes) info.m_resArr.get(fromPosition);
                        int fromPos = fromPosition;
                        int toPos = toPosition;
                        if (itemInfo != null) {
                            fromPos = ResourceUtils.HasId(FilterResMgr2.getInstance().GetOrderArr(), itemInfo.m_id);
                        }
                        itemInfo = (FilterRes) info.m_resArr.get(toPosition);
                        if (itemInfo != null) {
                            toPos = ResourceUtils.HasId(FilterResMgr2.getInstance().GetOrderArr(), itemInfo.m_id);
                        }
                        ResourceUtils.ChangeOrderPosition(FilterResMgr2.getInstance().GetOrderArr(), fromPos, toPos);
                        FilterResMgr2.getInstance().SaveOrderArr();
                        break;
                    }
                    case LIGHT_EFFECT: {
                        m_needRefresh = true;
                        MyBeautyStat.onClickByRes(R.string.光效管理_排序);
                        TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_光效管理_排序);
                        LightEffectRes itemInfo = (LightEffectRes) info.m_resArr.get(fromPosition);
                        int fromPos = fromPosition;
                        int toPos = toPosition;
                        if (itemInfo != null) {
                            fromPos = ResourceUtils.HasId(LightEffectResMgr2.getInstance().GetOrderArr(), itemInfo.m_id);
                        }
                        itemInfo = (LightEffectRes) info.m_resArr.get(toPosition);
                        if (itemInfo != null) {
                            toPos = ResourceUtils.HasId(LightEffectResMgr2.getInstance().GetOrderArr(), itemInfo.m_id);
                        }
                        ResourceUtils.ChangeOrderPosition(LightEffectResMgr2.getInstance().GetOrderArr(), fromPos, toPos);
                        LightEffectResMgr2.getInstance().SaveOrderArr();
                        break;
                    }
                    case TEXT_WATERMARK: {
                        m_needRefresh = true;
                        TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_水印管理_排序);
                        MyBeautyStat.onClickByRes(R.string.照片水印管理_排序);
                        ArrayList<Integer> orderArr = TextResMgr2.getInstance().GetOrderArr1().get(1);
                        TextRes itemInfo = (TextRes) info.m_resArr.get(fromPosition);
                        int fromPos = fromPosition;
                        int toPos = toPosition;
                        if (itemInfo != null) {
                            fromPos = ResourceUtils.HasId(orderArr, itemInfo.m_id);
                        }
                        itemInfo = (TextRes) info.m_resArr.get(toPosition);
                        if (itemInfo != null) {
                            toPos = ResourceUtils.HasId(orderArr, itemInfo.m_id);
                        }
                        ResourceUtils.ChangeOrderPosition(orderArr, fromPos, toPos);
                        TextResMgr2.getInstance().SaveOrderArr();
                        break;
                    }
                    case TEXT_ATTITUTE: {
                        MyBeautyStat.onClickByRes(R.string.照片水印管理_排序);
                        m_needRefresh = true;
                        TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_态度管理_排序);


                        ArrayList<Integer> orderArr = TextResMgr2.getInstance().GetOrderArr1().get(2);
                        TextRes itemInfo = (TextRes) info.m_resArr.get(fromPosition);
                        int fromPos = fromPosition;
                        int toPos = toPosition;
                        if (itemInfo != null) {
                            fromPos = ResourceUtils.HasId(orderArr, itemInfo.m_id);
                        }
                        itemInfo = (TextRes) info.m_resArr.get(toPosition);
                        if (itemInfo != null) {
                            toPos = ResourceUtils.HasId(orderArr, itemInfo.m_id);
                        }
                        ResourceUtils.ChangeOrderPosition(orderArr, fromPos, toPos);
                        TextResMgr2.getInstance().SaveOrderArr();
                        break;
                    }
                    case VIEDO_ORIGINALITY: {

                        m_needRefresh = true;
                        TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_态度管理_排序);
                        MyBeautyStat.onClickByRes(R.string.视频水印管理_排序);
                        ArrayList<Integer> orderArr = VideoTextResMgr2.getInstance().GetOrderArr1().get(2);

                        VideoTextRes itemInfo = (VideoTextRes) info.m_resArr.get(fromPosition);
                        int fromPos = fromPosition;
                        int toPos = toPosition;
                        if (itemInfo != null) {
                            fromPos = ResourceUtils.HasId(orderArr, itemInfo.m_id);
                        }

                        itemInfo = (VideoTextRes) info.m_resArr.get(toPosition);
                        if (itemInfo != null) {
                            toPos = ResourceUtils.HasId(orderArr, itemInfo.m_id);
                        }
                        ResourceUtils.ChangeOrderPosition(orderArr, fromPos, toPos);
                        VideoTextResMgr2.getInstance().SaveOrderArr();
                        break;

                    }
                    case VIEDO_WATERMARK: {
                        m_needRefresh = true;
                        TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_态度管理_排序);
                        MyBeautyStat.onClickByRes(R.string.视频水印管理_排序);
                        ArrayList<Integer> orderArr = VideoTextResMgr2.getInstance().GetOrderArr1().get(1);

                        VideoTextRes itemInfo = (VideoTextRes) info.m_resArr.get(fromPosition);
                        int fromPos = fromPosition;
                        int toPos = toPosition;
                        if (itemInfo != null) {
                            fromPos = ResourceUtils.HasId(orderArr, itemInfo.m_id);
                        }
                        itemInfo = (VideoTextRes) info.m_resArr.get(toPosition);
                        if (itemInfo != null) {
                            toPos = ResourceUtils.HasId(orderArr, itemInfo.m_id);
                        }
                        ResourceUtils.ChangeOrderPosition(orderArr, fromPos, toPos);
                        VideoTextResMgr2.getInstance().SaveOrderArr();
                        break;

                    }
                    case MUSIC: {
                        m_needRefresh = true;
                        TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_滤镜管理_排序);
                        MyBeautyStat.onClickByRes(R.string.音乐管理_排序);
                        MusicRes itemInfo = (MusicRes) info.m_resArr.get(fromPosition);


                        int fromPos = fromPosition;
                        int toPos = toPosition;
                        if (itemInfo != null) {
                            fromPos = ResourceUtils.HasId(MusicResMgr2.getInstance().GetOrderArr(), itemInfo.m_id);

                        }
                        itemInfo = (MusicRes) info.m_resArr.get(toPosition);
                        if (itemInfo != null) {
                            toPos = ResourceUtils.HasId(MusicResMgr2.getInstance().GetOrderArr(), itemInfo.m_id);
                        }
                        ResourceUtils.ChangeOrderPosition(MusicResMgr2.getInstance().GetOrderArr(), fromPos, toPos);
                        MusicResMgr2.getInstance().SaveOrderArr();
                        break;

                    }

                }

                ResourceUtils.ChangeArrayPosition(info.m_resArr, fromPosition, toPosition);
            }
            isDrawListner = true;
        }
    };
    private DragListView.DragListCallback m_dragCallback = new DragListView.DragListCallback() {
        @Override
        public boolean canDragItemAtPosition(int dragPosition) {
            return true;
        }

        @Override
        public boolean canDropItemAtPosition(int dropPosition) {
            return true;
        }
    };

    private PagerAdapter m_pagerAdapter = new PagerAdapter() {
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
            if (object instanceof DragListView) {
                container.removeView((DragListView) object);
                if (m_viewList != null) {
                    ((DragListView) object).setTag(null);
                    m_viewList.add((DragListView) object);
                }
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            DragListView view = null;
            if (m_viewList.size() > 0) {
                view = m_viewList.remove(0);
            }
            if (view == null) {
                view = new DragListView(getContext());
                view.setCustomDragItem(new ManageListAdapter.ManageDragItem(getContext()));
                view.setDragListCallback(m_dragCallback);
                view.setDragListListener(m_drawListener);
                view.setLayoutManager(new LinearLayoutManager(getContext()));
                view.getRecyclerView().addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL, 0.5f));
            }
            if (m_curSel == position) {
                DragItemAdapter adapter = view.getAdapter();
                if (adapter == null) {
                    adapter = new ManageListAdapter(getContext());
                    ((ManageListAdapter) adapter).SetClickListener(m_listItemClickLst);
                    view.setAdapter(adapter, true);
                }
                adapter.setItemList(m_ress.get(position).m_resArr);
                adapter.notifyDataSetChanged();
            }

            view.setTag(position);
            container.addView(view);//添加页卡
            return view;
        }
    };
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
            isSel = false;
            m_scrollLine.setVisibility(GONE);
            if (m_curSel >= 0 && m_curSel < m_titles.size()) {
                m_titles.get(m_curSel).OnChoose(false);
                m_titles.get(m_curSel).OnChooseText(false);
            }

            View tempView = GetChildViewByPosition(m_curSel);
            if (tempView != null) {
                DragListView view = (DragListView) tempView;
                view.getRecyclerView().stopScroll();

                ManageListAdapter adapter = (ManageListAdapter) view.getAdapter();
                if (adapter != null) {
                    adapter.SetClickListener(null);
                    adapter.setItemList(null);
                    adapter.ClearCache();
                    adapter.notifyDataSetChanged();
                }
                view.setAdapter(null, true);
            }

            if (position >= 0 && position < m_titles.size() && !m_typeOnly) {
                m_titles.get(position).OnChoose(true);
                m_titles.get(position).OnChooseText(true);
            }
            tempView = GetChildViewByPosition(position);
            if (tempView != null) {
                DragListView view = (DragListView) tempView;
                DragItemAdapter adapter = view.getAdapter();
                if (adapter == null) {
                    adapter = new ManageListAdapter(getContext());
                    ((ManageListAdapter) adapter).SetClickListener(m_listItemClickLst);
                    view.setAdapter(adapter, true);
                } else {
                    ((ManageListAdapter) adapter).SetClickListener(m_listItemClickLst);
                }

                Log.i(TAG, "onPageSelected: " + m_ress.get(position).m_type);
                adapter.setItemList(m_ress.get(position).m_resArr);
                adapter.notifyDataSetChanged();

                m_curSel = position;
            }
            switch (m_ress.get(position).m_type) {

                case FILTER: {
                    m_watermarkClassify.setVisibility(GONE);
                    isShowUI();
                    TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_滤镜管理);
                    MyBeautyStat.onClickByRes(R.string.素材管理页_切换至滤镜管理);
                    break;
                }
                case LIGHT_EFFECT: {
                    m_watermarkClassify.setVisibility(GONE);
                    isShowUI();
                    TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_光效管理);
                    MyBeautyStat.onClickByRes(R.string.素材管理页_切换至光效管理);
                    break;
                }
                case TEXT_WATERMARK: {
                    m_watermarkClassify.setVisibility(VISIBLE);
                    isShowUI();
                    videoOriginalityBtn.setText(R.string.managemnet_photo_attitude);
                    videoWatermarkBtn.setText(R.string.management_photo_watermark);
                    MyBeautyStat.onClickByRes(R.string.素材管理页_切换至照片水印管理);
                    TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_水印管理);
                    break;
                }
                case VIEDO_WATERMARK: {
                    m_watermarkClassify.setVisibility(VISIBLE);
                    isShowUI();
                    videoOriginalityBtn.setText(R.string.management_video_originality);
                    videoWatermarkBtn.setText(R.string.management_video_watermark);
                    MyBeautyStat.onClickByRes(R.string.素材管理页_切换至视频水印管理);
                    TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_态度管理);
                    break;
                }
                case MUSIC: {
                    MyBeautyStat.onClickByRes(R.string.素材管理页_切换至音乐管理);
                    m_watermarkClassify.setVisibility(GONE);
                    isShowUI();
                    break;
                }
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

    public ManagePage(Context context, BaseSite site) {
        super(context, site);

        m_site = (ManagePageSite) site;

        ShareData.InitData(context);
        m_topBarHeight = ShareData.PxToDpi_xhdpi(80);

        m_titles = new ArrayList<>();
        m_viewList = new ArrayList<>();

        InitUI(context);
        TongJiUtils.onPageStart(getContext(), TAG);
    }

    private void deleteRes() {
        if (dialog == null) {
            dialog = new InterphotoDlg((Activity) getContext(), R.style.waitDialog);
            dialog.SetTitle(R.string.manage_delet_title);
            dialog.SetMessage(R.string.manage_delet);
            dialog.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback() {
                @Override
                public void onOK() {
                    if (m_deleteRes instanceof TextRes) {

                        if ((m_ress.get(m_curSel).m_resArr.size())> 0 && (deletePosition)<m_ress.get(m_curSel).m_resArr.size()) {
                            m_needRefresh = true;
                            TextRes res = (TextRes) m_deleteRes;
                            TextResMgr2.getInstance().DeleteRes(getContext(), res);
                            MyBeautyStat.onClickByRes(R.string.照片水印管理_删除);
                            MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.素材管理页);
                            switch (((TextRes) m_deleteRes).m_resTypeID) {
                                case 1:

                                    TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_水印管理_隐藏);
                                    break;
                                case 2:

                                    TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_态度管理_隐藏);
                                    break;
                            }
                            removeItem(deletePosition);
                        }


                    } else if (m_deleteRes instanceof FilterRes) {
                        if ((m_ress.get(m_curSel).m_resArr.size())> 0 && (deletePosition)<m_ress.get(m_curSel).m_resArr.size()) {
                            m_needRefresh = true;
                            MyBeautyStat.onClickByRes(R.string.滤镜管理_删除);
                            FilterRes res = (FilterRes) m_deleteRes;
                            MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.素材管理页);
                            FilterResMgr2.getInstance().DeleteRes(getContext(), res);
                            removeItem(deletePosition);
                        }


                    } else if (m_deleteRes instanceof LightEffectRes) {
                        if ((m_ress.get(m_curSel).m_resArr.size())> 0 && (deletePosition)<m_ress.get(m_curSel).m_resArr.size()) {
                            TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理_光效管理_隐藏);
                            m_needRefresh = true;
                            MyBeautyStat.onClickByRes(R.string.光效管理_删除);
                            LightEffectRes res = (LightEffectRes) m_deleteRes;
                            MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.素材管理页);
                            LightEffectResMgr2.getInstance().DeleteRes(getContext(), res);
                            removeItem(deletePosition);

                        }


                    } else if (m_deleteRes instanceof MusicRes) {
                        if ((m_ress.get(m_curSel).m_resArr.size())> 0 && (deletePosition)<m_ress.get(m_curSel).m_resArr.size()) {
                            m_needRefresh = true;
                            MyBeautyStat.onClickByRes(R.string.音乐管理_删除);
                            MusicRes res = (MusicRes) m_deleteRes;
                            MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.素材管理页);
                            MusicResMgr2.getInstance().DeleteRes(getContext(), res);
                            removeItem(deletePosition);
                        }


                    } else if (m_deleteRes instanceof VideoTextRes) {
                        if ((m_ress.get(m_curSel).m_resArr.size())> 0 && (deletePosition)<m_ress.get(m_curSel).m_resArr.size()) {
                            m_needRefresh = true;
                            MyBeautyStat.onClickByRes(R.string.视频水印管理_删除);
                            VideoTextRes res = (VideoTextRes) m_deleteRes;
                            MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.素材管理页);
                            VideoTextResMgr2.getInstance().DeleteRes(getContext(), res);
                            removeItem(deletePosition);
                        }

                    }
                    dialog.dismiss();
                }

                @Override
                public void onCancel() {
                    dialog.dismiss();
                }
            });

        }

        if (dialog != null) {
            //    dialog.setData(res);
            dialog.show();
        }

    }

    private void removeItem(int position) {
        View tempView = GetChildViewByPosition(m_curSel);
        if (tempView != null) {
            DragListView dragListView = (DragListView) tempView;
            ManageListAdapter adapter = (ManageListAdapter) dragListView.getAdapter();
            adapter.removeItem(position);
            adapter.notifyItemChanged(position);
        }
        m_ress.get(m_curSel).m_resArr.remove(position);
    }

    private void selectLabel(int positoin, ResType tepy, int tag) {

        View tempView = GetChildViewByPosition(positoin);
        if (tempView != null) {
            DragListView view = (DragListView) tempView;
            view.getRecyclerView().stopScroll();

            ManageListAdapter adapter = (ManageListAdapter) view.getAdapter();
            if (adapter != null) {
                adapter.SetClickListener(m_listItemClickLst);
                adapter.setItemList(null);
                adapter.ClearCache();
                adapter.notifyDataSetChanged();
            }
            list = MgrUtils.GetManageInfos(tepy, tag);
            adapter.setItemList(list.get(0).m_resArr);
        }


    }

    private void InitUI(Context context) {
         this.setBackgroundColor(0xff0e0e0e);
       // this.setBackgroundColor(Color.BLACK);

        m_topBar = new FrameLayout(context);
        m_topBar.setBackgroundColor(Color.BLACK);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, m_topBarHeight);
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


            titleView= new TextView(context);
            titleView.setText(R.string.material_management);
            titleView.setGravity(Gravity.CENTER);
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            titleView.setTextColor(Color.parseColor("#ffffff"));
            m_topBar.addView(titleView);

        }
        {
         /*   titleScrollView = new ElasticHorizontalScrollView(context);
            titleScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            titleScrollView.setHorizontalScrollBarEnabled(false);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_topBarHeight);
            fl.gravity = Gravity.TOP;
            fl.topMargin = m_topBarHeight;
            titleScrollView.setLayoutParams(fl);
            this.addView(titleScrollView);*/


            m_classifyBar = new FrameLayout(context);
            m_classifyBar.setBackgroundColor(Color.BLACK);
            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_topBarHeight);
            fl.gravity = Gravity.TOP;
            m_classifyBar.setLayoutParams(fl);
            fl.topMargin = m_topBarHeight;
            this.addView(m_classifyBar);

            m_titleFr = new LinearLayout(context);
            fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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

        {
            m_watermarkClassify = new FrameLayout(context);
            fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(108));
            fl.topMargin = m_topBarHeight + m_topBarHeight;
            m_watermarkClassify.setBackgroundColor(Color.BLACK);
            m_watermarkClassify.setLayoutParams(fl);
            m_watermarkClassify.setVisibility(GONE);
            m_watermarkClassify.setBackgroundColor(0xff0e0e0e);
            addView(m_watermarkClassify);

            videoWatermarkBtn = new TextView(context);
            videoWatermarkBtn.setOnClickListener(m_btnLst);
            videoWatermarkBtn.setMinWidth(ShareData.PxToDpi_xhdpi(150));
            videoWatermarkBtn.setMinHeight(ShareData.PxToDpi_xhdpi(48));
            videoWatermarkBtn.setGravity(Gravity.CENTER);
            fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.LEFT;
            fl.topMargin = ShareData.PxToDpi_xhdpi(30);
            fl.leftMargin = ShareData.PxToDpi_xhdpi(160);
            videoWatermarkBtn.setText(R.string.management_video_watermark);
            videoWatermarkBtn.setTextColor(Color.parseColor("#666666"));
            videoWatermarkBtn.setBackground(roundColorDrawable);
            videoWatermarkBtn.setLayoutParams(fl);
            m_watermarkClassify.addView(videoWatermarkBtn);

            videoOriginalityBtn = new TextView(context);
            videoOriginalityBtn.setOnClickListener(m_btnLst);
            videoOriginalityBtn.setMinWidth(ShareData.PxToDpi_xhdpi(150));
            videoOriginalityBtn.setMinHeight(ShareData.PxToDpi_xhdpi(48));
            videoOriginalityBtn.setGravity(Gravity.CENTER);
            roundColorDrawable1 = new RoundColorDrawable(0xff0e0e0e);
            fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.RIGHT;
            fl.topMargin = ShareData.PxToDpi_xhdpi(30);
            fl.rightMargin = ShareData.PxToDpi_xhdpi(160);
            videoOriginalityBtn.setText(R.string.management_video_originality);
            videoOriginalityBtn.setTextColor(Color.parseColor("#666666"));
            videoOriginalityBtn.setBackground(roundColorDrawable1);
            videoOriginalityBtn.setLayoutParams(fl);
            m_watermarkClassify.addView(videoOriginalityBtn);

        }

        m_tip = new TextView(context);
        m_tip.setVisibility(GONE);
        m_tip.setBackgroundColor(0xffffc433);
        m_tip.setGravity(Gravity.CENTER);
        m_tip.setTextColor(0xffffffff);
        m_tip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.TOP;
        m_tip.setLayoutParams(fl);
        this.addView(m_tip);

        m_pager = new ViewPager(context);
//		m_pager.setPadding(ShareData.PxToDpi_xhdpi(40), 0, ShareData.PxToDpi_xhdpi(40), 0);
        m_pager.setAdapter(m_pagerAdapter);


        m_pager.addOnPageChangeListener(m_pageChangeListener);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fl.gravity = Gravity.TOP;
        fl.topMargin = m_topBarHeight +m_topBarHeight;
        m_pager.setLayoutParams(fl);
        this.addView(m_pager);

        String text = getResources().getString(R.string.clickHideOrDragOrder);
        Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.mgr_scan_delete);
        VerticalImageSpan imgSpan = new VerticalImageSpan(getContext(), bmp);
        Paint paint = m_tip.getPaint();
        imgSpan.getSize(paint, "", 0, 0, paint.getFontMetricsInt());
        bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.mgr_drag_btn);
        VerticalImageSpan imgSpan2 = new VerticalImageSpan(getContext(), bmp);
        imgSpan2.getSize(paint, "", 0, 0, paint.getFontMetricsInt());
        SpannableString spanString = new SpannableString(text);
        int index = text.indexOf("hideI");
        spanString.setSpan(imgSpan, index, index + 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        index = text.indexOf("dragI");
        spanString.setSpan(imgSpan2, index, index + 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        m_tip.setText(spanString);


    }

    private void isShowUI() {
        if (m_watermarkClassify.getVisibility() == VISIBLE) {
            fl.topMargin = m_topBarHeight * 2+ShareData.PxToDpi_xhdpi(108);
            videoWatermarkBtn.setBackground(roundColorDrawable);
            videoOriginalityBtn.setBackground(roundColorDrawable1);
            m_pager.setLayoutParams(fl);

        } else {
            fl.topMargin = m_topBarHeight * 2;
            m_pager.setLayoutParams(fl);
        }

    }


    private void AddTitles(String title, int leftMargin) {
        LinearLayout.LayoutParams ll;
        int size = 14;
        if (!TagMgr.CheckTag(getContext(), LanguagePage.ENGLISH_TAGVALUE)) {
            size = 12;
        }
        MyButtons2 btn = new MyButtons2(getContext(), 0, title, 0, size);
        ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ll.leftMargin = leftMargin;
        btn.setLayoutParams(ll);
        m_titleFr.addView(btn);
        btn.setOnClickListener(m_btnLst);
        btn.OnChoose(false);
        m_titles.add(btn);
    }

    /**
     * @param params typeOnly Boolean //是否只显示当前分类
     *               type ResType
     */
    @Override
    public void SetData(HashMap<String, Object> params) {
        ResType type = null;
        if (params != null) {
            Object o = params.get("type");
            if (o != null) {
                type = (ResType) o;
            }
            o = params.get("typeOnly");
            if (o != null) {
                m_typeOnly = (Boolean) o;
            }

        }


        if (type ==  ResType.VIEDO_WATERMARK || type == ResType.TEXT_WATERMARK){

            m_ress = MgrUtils.GetManageInfos(m_typeOnly ? type : null,WATERMARK);

        }else if (type ==  ResType.VIEDO_ORIGINALITY || type == ResType.TEXT_ATTITUTE){

            m_ress = MgrUtils.GetManageInfos(m_typeOnly ? type : null,ORIGINALITY);

        }else {

            m_ress = MgrUtils.GetManageInfos(m_typeOnly ? type : null,WATERMARK);

        }

        TongJi2.AddCountByRes(getContext(), R.integer.素材商店_素材管理);
        if (type != null) {
            int size = m_ress.size();

            for (int i = 0; i < size; i++) {
                if (type == m_ress.get(i).m_type) {
                    m_curSel = i;
                    break;
                }
            }
        }

        if (m_typeOnly) {
            Log.i(TAG, "SetData: " + type);

            switch (type){
                case VIEDO_WATERMARK :
                    m_classifyBar.setVisibility(GONE);
                    titleView.setText(getResources().getString(R.string.video_watermark));
                    break;
                case VIEDO_ORIGINALITY:
                    m_classifyBar.setVisibility(GONE);
                    titleView.setText(getResources().getString(R.string.video_originality));
                    break;
                case TEXT_WATERMARK:
                    m_classifyBar.setVisibility(GONE);
                    titleView.setText(getResources().getString(R.string.watermark));
                    break;
                case TEXT_ATTITUTE:
                    titleView.setText(getResources().getString(R.string.photo_attitude));
                    m_classifyBar.setVisibility(GONE);
                    break;
                case LIGHT_EFFECT:
                    m_classifyBar.setVisibility(GONE);
                    titleView.setText(getResources().getString(R.string.Light));
                    break;
                case FILTER:
                    m_classifyBar.setVisibility(GONE);
                    titleView.setText(getResources().getString(R.string.Filters));
                    break;
                case MUSIC:
                    m_classifyBar.setVisibility(GONE);
                    titleView.setText(getResources().getString(R.string.material_management_music));
                    break;

            }

            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, m_topBarHeight);
            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.TOP;
            fl.topMargin = m_topBarHeight ;
            m_pager.setLayoutParams(fl);

        } else {
            int padding = ShareData.PxToDpi_xhdpi(55);
            if (!TagMgr.CheckTag(getContext(), LanguagePage.ENGLISH_TAGVALUE)) {
                padding = ShareData.PxToDpi_xhdpi(60);
            }
            AddTitles(getResources().getString(R.string.Filters),padding);
            AddTitles(getResources().getString(R.string.watermark), padding);
            AddTitles(getResources().getString(R.string.video_watermark), padding);
            AddTitles(getResources().getString(R.string.Light), padding);
            AddTitles(getResources().getString(R.string.material_management_music), padding);

        }

        m_pagerAdapter.notifyDataSetChanged();
        m_pager.setCurrentItem(m_curSel);
        if (m_curSel >= 0 && m_curSel < m_titles.size() && !m_typeOnly) {
            m_titles.get(m_curSel).OnChoose(true);
            m_titles.get(m_curSel).OnChooseText(true);
        }


        if (TagMgr.CheckTag(getContext(), "theme_manage_first_tip")) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (m_tip != null) {
                        showTip(m_tip, true, true);
                    }
                }
            }, 350);

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (m_tip != null) {
                        showTip(m_tip, false, true);
                    }
                }
            }, 3350);
            TagMgr.SetTag(getContext(), "theme_manage_first_tip");
        }
    }

    private void showTip(View v, boolean isOpen, boolean hasAnimation) {
        if (v == null)
            return;
        v.clearAnimation();

        int start;
        int end;
        if (isOpen) {
            v.setVisibility(View.VISIBLE);

            start = -1;
            end = 0;
        } else {
            v.setVisibility(View.GONE);

            start = 0;
            end = -1;
        }

        if (hasAnimation) {
            AnimationSet as;
            TranslateAnimation ta;
            as = new AnimationSet(true);
            ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, start, Animation.RELATIVE_TO_SELF, end);
            ta.setDuration(350);
            as.addAnimation(ta);
            v.startAnimation(as);
        }
    }

    @Override
    public void onBack() {
        MyBeautyStat.onClickByRes(R.string.素材管理页_退出素材管理页);
        HashMap<String, Object> params = new HashMap<>();
        params.put("need_refresh", m_needRefresh);
        m_site.OnBack(params,getContext());
    }

    @Override
    public void onClose() {
        super.onClose();
        this.removeAllViews();
        this.clearFocus();
        if (m_pager != null) {
            int len = m_pager.getChildCount();
            ManageListAdapter adapter;
            DragListView view;
            for (int i = 0; i < len; i++) {
                view = (DragListView) m_pager.getChildAt(i);
                adapter = (ManageListAdapter) view.getAdapter();
                if (adapter != null) {
                    adapter.SetClickListener(null);
                    adapter.setItemList(null);
                    adapter.ClearCache();
                    adapter.notifyDataSetChanged();
                    m_listItemClickLst = null;
                }
                view.setAdapter(null, true);
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
        }
        m_tip = null;
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
}
