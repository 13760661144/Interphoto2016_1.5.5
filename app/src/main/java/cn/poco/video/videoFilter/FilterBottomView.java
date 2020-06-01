package cn.poco.video.videoFilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.MaterialMgr2.MgrUtils;
import cn.poco.MaterialMgr2.ThemeItemInfo;
import cn.poco.beautify.BeautyAdjustType;
import cn.poco.beautify.BeautyColorType;
import cn.poco.beautify.CurveView;
import cn.poco.beautify.CurveView2;
import cn.poco.beautify.MyButtons2;
import cn.poco.beautify.MySeekBar2;
import cn.poco.beautify.SimpleListItem;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.resource.AbsDownloadMgr;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.FilterRes;
import cn.poco.resource.FilterResMgr2;
import cn.poco.resource.IDownload;
import cn.poco.resource.ResType;
import cn.poco.resource.ResourceUtils;
import cn.poco.resource.ThemeRes;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.system.Tags;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv100.SimpleBtnList100;
import cn.poco.video.VideoResMgr;
import cn.poco.video.page.ProcessMode;
import cn.poco.video.utils.VideoUtils;
import cn.poco.video.view.BaseBottomView;

/**
 * Created by Shine on 2017/6/6.
 */

public class FilterBottomView extends BaseBottomView implements EventRouter.EventChain {
    public interface BottomViewCallbackAdapter {
        void onNeedDownItemClick(HashMap<String, Object> params);

        void onApplayFilter(FilterRes filterRes, int uri);

        void onMoreItemClick(View view, int index);

        void onItemAvatarClick(HashMap<String, Object> params);

        void onAdjustModeSelected(BeautyAdjustType type, String title);

        void onFilterTypeChange(int mode);

        void onAdjustbarSeek(BeautyColorType filterType, BeautyAdjustType type, float dataTransformed, float dataRaw);

        void onFilterAlphaBarVisibilityChange(boolean show);

        void doCurveEffect(int mode, List<Point> controlPointList, CurveView.ControlInfo red, CurveView.ControlInfo green, CurveView.ControlInfo blue, CurveView.ControlInfo rgb);

        void openHelpPage(HashMap<String, Object>params, Context context);

    }



    protected LinearLayout m_seekBarFr;
    protected MySeekBar2 m_colorSeekBar;
    protected TextView m_seekkBarTip;
    protected SimpleBtnList100 m_filterTypeList;    //颜色列表
    protected LinearLayout m_adjustBar;
    protected SimpleBtnList100 m_adjustList;    //调整列表
    protected RelativeLayout m_rgbFr;
    protected ArrayList<SimpleBtnList100.Item> m_adjustItems;
    protected ImageView m_adjustDownBtn;
    protected MyButtons2 m_btnR;
    protected MyButtons2 m_btnG;
    protected MyButtons2 m_btnB;
    protected MyButtons2 m_btnRGB;
    protected ImageView m_scanBtn;

    protected int m_curFilterUri = -1;    //颜色
    protected BeautyColorType m_filterType = BeautyColorType.FILTER;
    protected VideoResMgr.AdjustData m_curAdjustData;

    protected VideoResMgr.ColorMsg m_colorMsg;

    protected String m_adjustTip;
    protected boolean m_seekBarShow;

    protected MyBtnLst m_btnLst = new MyBtnLst();
    private Bitmap m_orgInfo;
    private BottomViewCallbackAdapter mAdapterCallback;
    private EventRouter.EventChain mNextChain;
    private boolean mShouldHandle = true;
    private FilterInnerInfo mFilterInfo;

    protected CurveView2 m_curveView;

    public static int s_selUri = -1;

    protected int m_curViewSize;

    public FilterBottomView(@NonNull Context context, FilterInnerInfo filterInfo) {
        super(context);
        mMode = ProcessMode.Fileter;
        mFilterInfo = filterInfo;
        initData();
        initView();
    }

    @Override
    protected void initData() {
        super.initData();
        DownloadMgr.getInstance().AddDownloadListener(m_downloadLst);

        m_adjustTip = getResources().getString(R.string.Adjust);
        m_colorMsg = new VideoResMgr.ColorMsg();
        m_filterType = BeautyColorType.FILTER;

        m_curAdjustData = new VideoResMgr.AdjustData(BeautyAdjustType.NONE, 0);
    }

    @Override
    protected void initView() {
        super.initView();
        m_resList.getRecyclerView().addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (s_selUri != -1) {
                    DragListItemInfo info;
                    int index = m_listAdapter.GetIndexByUri(s_selUri);
                    if (index != -1) {
                        info = m_listDatas.get(index);
                        onClickNormalItem(null, info, index);
                    }
                }
                m_resList.getRecyclerView().removeOnLayoutChangeListener(this);
            }
        });

        m_seekBarFr = new LinearLayout(getContext());
        m_seekBarFr.setVisibility(View.GONE);
        m_seekBarFr.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        fl.bottomMargin = m_resBarHeight;
        m_seekBarFr.setLayoutParams(fl);
        this.addView(m_seekBarFr);

        {
            LinearLayout.LayoutParams ll;
            m_seekkBarTip = new TextView(getContext());
            m_seekkBarTip.setMaxLines(1);
            m_seekkBarTip.setText("0");
            m_seekkBarTip.setTextColor(Color.WHITE);
            m_seekkBarTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.LEFT;
            ll.leftMargin = ShareData.PxToDpi_xhdpi(21);
            m_seekkBarTip.setLayoutParams(ll);
            m_seekBarFr.addView(m_seekkBarTip);

            m_colorSeekBar = new MySeekBar2(getContext());
            m_colorSeekBar.setMax(12);
            m_colorSeekBar.SetDotNum(13);
            m_colorSeekBar.setOnSeekBarChangeListener(m_seekBarListener);
            m_colorSeekBar.setProgress(0);
            ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40), LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.topMargin = ShareData.PxToDpi_xhdpi(10);
            m_colorSeekBar.setLayoutParams(ll);
            m_seekBarFr.addView(m_colorSeekBar);
        }


        m_rgbFr = new RelativeLayout(getContext());
        m_rgbFr.setVisibility(View.GONE);
        fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(120));
        fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        fl.bottomMargin = ShareData.PxToDpi_xhdpi(160);
        fl.leftMargin = fl.rightMargin = ShareData.PxToDpi_xhdpi(40);
        m_rgbFr.setLayoutParams(fl);
        this.addView(m_rgbFr);

        {
            RelativeLayout.LayoutParams rl;
            m_btnRGB = new MyButtons2(getContext(), R.drawable.beauty_curve_rgb, "RGB", ShareData.PxToDpi_xhdpi(17), 12);
            m_btnRGB.setId(R.id.btn_rgb);
            m_btnRGB.setOnClickListener(m_btnLst);
            m_btnRGB.OnChoose(true);
            rl= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rl.addRule(RelativeLayout.CENTER_VERTICAL);
            m_btnRGB.setLayoutParams(rl);
            m_rgbFr.addView(m_btnRGB);

            m_btnR = new MyButtons2(getContext(), R.drawable.beauty_curve_r, "R", ShareData.PxToDpi_xhdpi(17), 12);
            m_btnR.setId(R.id.btn_r);
            m_btnR.setOnClickListener(m_btnLst);
            m_btnR.OnChoose(false);
            rl= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rl.addRule(RelativeLayout.CENTER_VERTICAL);
            rl.addRule(RelativeLayout.RIGHT_OF, R.id.btn_rgb);
            rl.leftMargin = ShareData.PxToDpi_xhdpi(65);
            m_btnR.setLayoutParams(rl);
            m_rgbFr.addView(m_btnR);

            m_btnG = new MyButtons2(getContext(), R.drawable.beauty_curve_g, "G", ShareData.PxToDpi_xhdpi(17), 12);
            m_btnG.setId(R.id.btn_g);
            m_btnG.setOnClickListener(m_btnLst);
            m_btnG.OnChoose(false);
            rl= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rl.addRule(RelativeLayout.CENTER_VERTICAL);
            rl.addRule(RelativeLayout.RIGHT_OF, R.id.btn_r);
            rl.leftMargin = ShareData.PxToDpi_xhdpi(65);
            m_btnG.setLayoutParams(rl);
            m_rgbFr.addView(m_btnG);

            m_btnB = new MyButtons2(getContext(), R.drawable.beauty_curve_b, "B", ShareData.PxToDpi_xhdpi(17), 12);
            m_btnB.setId(R.id.btn_b);
            m_btnB.setOnClickListener(m_btnLst);
            m_btnB.OnChoose(false);
            rl= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rl.addRule(RelativeLayout.CENTER_VERTICAL);
            rl.addRule(RelativeLayout.RIGHT_OF, R.id.btn_g);
            rl.leftMargin = ShareData.PxToDpi_xhdpi(65);
            m_btnB.setLayoutParams(rl);
            m_rgbFr.addView(m_btnB);

            m_scanBtn = new ImageView(getContext());
            m_scanBtn.setOnClickListener(m_btnLst);
            m_scanBtn.setImageResource(R.drawable.beauty_curve_show);
            rl= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rl.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            rl.topMargin = ShareData.PxToDpi_xhdpi(15);
            m_scanBtn.setLayoutParams(rl);
            m_rgbFr.addView(m_scanBtn);
        }

        // 去掉底部的bar
        m_filterTypeList = new SimpleBtnList100(getContext());
        ArrayList<SimpleBtnList100.Item> items = VideoResMgr.getColorItems(getContext());
        m_filterTypeList.SetData(VideoResMgr.getColorItems(getContext()), m_filterListCB);
        int len = items.size();
        int index = -1;
        for (int i = 0; i < len; i++) {
            BeautyColorType type = (BeautyColorType) (((SimpleListItem) items.get(i)).m_ex);
            if (m_filterType == type) {
                index = i;
                break;
            }
        }
        m_filterTypeList.SetSelByIndex(index);
        fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_bottomBarHeight);
        fl.gravity = Gravity.BOTTOM;
        m_filterTypeList.setLayoutParams(fl);
        this.addView(m_filterTypeList);

        int adjustBtnsHeight = ShareData.PxToDpi_xhdpi(120);
        int downHeight = ShareData.PxToDpi_xhdpi(40);
        m_adjustBar = new LinearLayout(getContext());
        m_adjustBar.setVisibility(View.GONE);
        m_adjustBar.setBackgroundColor(0xff000000);
        m_adjustBar.setOrientation(LinearLayout.VERTICAL);
        fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.BOTTOM;
        m_adjustBar.setLayoutParams(fl);
        this.addView(m_adjustBar);
        {
            m_adjustList = new SimpleBtnList100(getContext());
            m_adjustItems = VideoResMgr.getColorAdjustItems(getContext());
            m_adjustList.SetData(m_adjustItems, m_adjustBtnListCB);
            LinearLayout.LayoutParams ll;
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, adjustBtnsHeight);
            m_adjustList.setLayoutParams(ll);
            m_adjustBar.addView(m_adjustList);

            ImageView Line = new ImageView(getContext());
            Line.setBackgroundColor(0xff272727);
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            Line.setLayoutParams(ll);
            m_adjustBar.addView(Line);

            m_adjustDownBtn = new ImageView(getContext());
            m_adjustDownBtn.setOnClickListener(m_btnLst);
			m_adjustDownBtn.setScaleType(ImageView.ScaleType.CENTER);
            m_adjustDownBtn.setImageResource(R.drawable.beauty_color_adjust_down);
            ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, downHeight);
            m_adjustDownBtn.setLayoutParams(ll);
            m_adjustBar.addView(m_adjustDownBtn);
        }

        m_curveView = new CurveView2(getContext());
        m_curveView.SetOnChangeListener(m_curveCB);
        m_curveView.setVisibility(View.GONE);
        fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        fl.bottomMargin = ShareData.PxToDpi_xhdpi(280) + ShareData.PxToDpi_xhdpi(40);
        m_curveView.setLayoutParams(fl);
        this.addView(m_curveView);

        m_curViewSize = m_curveView.GetCurveViewSize();
    }

    private boolean mIsUrlStillValid = true; // 用于标记滤镜的url是否还有效
    private List<DragListItemInfo> mDragListInfo;

    @Override
    protected List<DragListItemInfo> initItemList() {
        mDragListInfo = VideoResMgr.GetFilterRess(getContext());
        if (m_orgInfo != null && !m_orgInfo.isRecycled()) {
            mDragListInfo.get(1).m_logo = m_orgInfo;
        }

        return mDragListInfo;
    }

    @Override
    protected boolean canDelete(int position) {
        boolean flag = false;
        DragListItemInfo itemInfo = m_listDatas.get(position);
        if(itemInfo != null)
        {
            FilterRes res = (FilterRes)itemInfo.m_ex;
            if(res.m_type != BaseRes.TYPE_LOCAL_RES)
            {
                flag = true;
            }
        }
        return flag;
    }

    public void setAdjustData(List<VideoResMgr.AdjustData> data) {
        for (SimpleBtnList100.Item item : m_adjustList.GetDatas()) {
            if (item instanceof SimpleListItem) {
                SimpleListItem simpleItem = (SimpleListItem) item;
                int index = m_adjustList.GetDatas().indexOf(item);
                Object object = data.get(index);
                simpleItem.m_ex = data.get(index);
                if (object instanceof VideoResMgr.CurveData) {
                    VideoResMgr.CurveData curveData = (VideoResMgr.CurveData) object;
                    if (curveData.mRGB != null) {
                        m_curveView.setCurveInfo(curveData.transformBackToControlInfo(curveData.mRed), curveData.transformBackToControlInfo(curveData.mGreen), curveData.transformBackToControlInfo(curveData.mBlue), curveData.transformBackToControlInfo(curveData.mRGB));
                    }
                }
            }
        }
    }

    private boolean checkFiltersUriValidity (List<VideoResMgr.FilterData> filterDataList) {
        boolean result = true;
        for (VideoResMgr.FilterData filterData : filterDataList) {
            for (DragListItemInfo info : mDragListInfo)  {
                if (filterData.mFilterUrl == info.m_uri) {
                    filterData.mIsValid = true;
                    filterData.mFilterRes = (FilterRes) info.m_ex;
                    break;
                }
                filterData.mIsValid = false;
                result = false;
            }
        }
        return result;
    }



 public boolean isFilterUriValid (int url) {
        if (url == -1) {
            return false;
        }
        boolean filterResValid = false;
        for (DragListItemInfo info : mDragListInfo) {
            if (info.m_ex instanceof FilterRes) {
                if (info.m_uri == url) {
                    mIsUrlStillValid = true;
                    filterResValid = true;
                    break;
                }
            }
        }
        return filterResValid;
    }


    public void decodeAndUpdateCoverImage() {
        if (mDragListInfo.size() > 0) {
            final DragListItemInfo info = mDragListInfo.get(1);
            new Thread() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(mFilterInfo.mVideoMediaPath)) {
                        m_orgInfo = VideoUtils.decodeFrameByTimeAndroidApi(mFilterInfo.mVideoMediaPath, mFilterInfo.mClipStartTime);
//                        VideoUtils.VideoInfo videoInfo = VideoUtils.getVideoInfo(mFilterInfo.mVideoMediaPath);
//                        m_orgInfo = VideoUtils.rotateVideoBitmap(m_orgInfo, videoInfo.rotation);
                        if (mDragListInfo != null) {
                            if (mDragListInfo.size() > 0) {
                                info.m_logo = m_orgInfo;
                                FilterBottomView.this.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (m_listAdapter != null) {
                                            m_listAdapter.notifyItemChanged(1);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }.start();
        }
    }


    /**
     * 调整滤镜资源
     */
    public void adjustFilterResource() {
        for (DragListItemInfo info : mDragListInfo) {
            if (info.m_ex instanceof FilterRes) {
                FilterRes filterRes = (FilterRes) info.m_ex;
                if (mFilterInfo.mFilterDataList.size() > 0) {
                    if (info.m_uri == mFilterInfo.mFilterDataList.get(0).mFilterUrl) {
                        filterRes.m_alpha = (int)(mFilterInfo.mFilterDataList.get(0).mFilterAlpha / (12f) * 100);
                        break;
                    }
                }
            }
        }
    }

    public FilterRes getFilterResByUri(int uri) {
        FilterRes filterRes = null;
        for (DragListItemInfo info : mDragListInfo) {
            if (info.m_ex instanceof FilterRes) {
                if (info.m_uri == uri) {
                    filterRes = (FilterRes) info.m_ex;
                    break;
                }
            }
        }
        return filterRes;
    }


    @Override
    protected void OnHideItem(int position) {
        DragListItemInfo itemInfo = m_listDatas.remove(position);
        if (itemInfo != null) {
            TongJi2.AddCountByRes(getContext(), R.integer.美化_滤镜_隐藏);
            shenCeOnClickRes(R.string.视频滤镜_删除滤镜, R.string.单视频滤镜_删除滤镜);

            FilterRes res = (FilterRes) itemInfo.m_ex;
            res.m_isHide = !res.m_isHide;
            FilterResMgr2.getInstance().DeleteRes(getContext(), res);
//            m_themeCompleteFlags.remove(res.m_id);

            mIsUrlStillValid = checkFiltersUriValidity(mFilterInfo.mFilterDataList);
            if (m_curFilterUri == res.m_id || !mIsUrlStillValid) {
                SetSelItemByUri(DragListItemInfo.URI_ORIGIN);
                if (m_seekBarFr.getVisibility() == View.VISIBLE) {
                    LayoutColorList(FilterBottomView.this, true, true);
                }
            }
        }
        Object object = itemInfo.m_ex;
        if (object instanceof FilterRes) {
            FilterRes filterRes = (FilterRes) object;
            MyBeautyStat.onDeleteMaterial(String.valueOf(filterRes.m_tjId), R.string.视频滤镜);
        }
    }

    private void shenCeOnClickRes(int globalData, int partialData) {
        if (!mFilterInfo.mIsPartialFilter) {
            MyBeautyStat.onClickByRes(globalData);
        } else {
            MyBeautyStat.onClickByRes(partialData);
        }
    }



    @Override
    protected void OnChangeItem(int fromPosition, int toPosition) {
        DragListItemInfo itemInfo = m_listDatas.get(fromPosition);
        int fromPos = fromPosition;
        int toPos = toPosition;
        if (itemInfo != null) {
            fromPos = ResourceUtils.HasId(FilterResMgr2.getInstance().GetOrderArr(), itemInfo.m_uri);
        }
        itemInfo = m_listDatas.get(toPosition);
        if (itemInfo != null) {
            toPos = ResourceUtils.HasId(FilterResMgr2.getInstance().GetOrderArr(), itemInfo.m_uri);
        }
        ResourceUtils.ChangeOrderPosition(FilterResMgr2.getInstance().GetOrderArr(), fromPos, toPos);
        FilterResMgr2.getInstance().SaveOrderArr();
        if (m_listDatas != null && m_listDatas.size() > fromPosition && m_listDatas.size() > toPosition) {
            itemInfo = m_listDatas.remove(fromPosition);
            m_listDatas.add(toPosition, itemInfo);
        }
    }

    @Override
    protected void onClickNormalItem(View v, DragListItemInfo info, int index) {
        if (info.m_isLock) {
            if (m_curFilterUri != info.m_uri && m_curFilterUri != DragListItemInfo.URI_ORIGIN) {
                SetSelItemByUri(DragListItemInfo.URI_ORIGIN);
            }
            onHeadClick(v, info, index);
            return;
        }

        if (info.m_uri == DragListItemInfo.URI_MGR) {
            // 统计注释
            TongJi2.AddCountByRes(getContext(), R.integer.美化_滤镜_更多按钮);
            HashMap<String, Object> params = new HashMap<>();
            params.put("type", ResType.FILTER);
            params.put("typeOnly", true);

            if (mAdapterCallback != null) {
                mAdapterCallback.onMoreItemClick(v, index);
            }
            return;
        }

        switch (info.m_style) {

            case NORMAL: {

                if (m_filterType == BeautyColorType.FILTER) {
                    if (m_curFilterUri != info.m_uri) {
                        SetSelItemByUri(info.m_uri);
                        if (m_listAdapter != null) {
                            m_listAdapter.SetSelByUri(info.m_uri);
                            m_resList.ScrollToCenter(index);
                        }
                    } else if (info.m_uri != DragListItemInfo.URI_ORIGIN) {
                        boolean flag = true;
                        if (m_seekBarFr.getVisibility() == View.VISIBLE) {
                            flag = false;
                            LayoutColorList(this, true, true);
                            if (mAdapterCallback != null) {
                                mAdapterCallback.onFilterAlphaBarVisibilityChange(false);
                            }
                        }
                        if (flag && m_seekBarFr.getVisibility() == View.GONE) {
                            LayoutColorList(this, false, true);
                            if (mAdapterCallback != null) {
                                mAdapterCallback.onFilterAlphaBarVisibilityChange(true);
                            }
                        }
                    }
                    if (info.m_uri == DragListItemInfo.URI_ORIGIN) {
                        if (m_seekBarFr.getVisibility() == View.VISIBLE) {
                            LayoutColorList(this, true, true);
                        }
                    }
                    m_curFilterUri = info.m_uri;

                }
                break;
            }

            case NEED_DOWNLOAD: {
                if (info.m_ex instanceof ThemeRes) {
                    TongJi2.AddCountByRes(getContext(), R.integer.美化_滤镜_主题推荐位);
                    HashMap<String, Object> params = new HashMap<>();
                    ThemeItemInfo itemInfo = MgrUtils.GetThemeItemInfoByUri(getContext(), ((ThemeRes) info.m_ex).m_id, ResType.FILTER);
                    params.put("data", itemInfo);
                    if (v != null) {
                        int[] location = new int[2];
                        v.getLocationOnScreen(location);

                        params.put("hasAnim", true);
                        params.put("centerX", location[0]);
                        params.put("centerY", location[1]);
                        params.put("viewH", v.getHeight());
                        params.put("viewW", v.getWidth());
                    }
                    if (mAdapterCallback != null) {
                        mAdapterCallback.onNeedDownItemClick(params);
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onHeadClick(View v, DragListItemInfo info, int index) {
        if (!info.m_isLock) {
            if (m_curFilterUri != info.m_uri) {
                onClickNormalItem(v, info, index);
                return;
            }
        } else {
            if (m_listAdapter != null) {
                m_listAdapter.SetSelByUri(DragListItemInfo.URI_NONE);
            }
        }

        TongJi2.AddCountByRes(getContext(), R.integer.美化_滤镜_大师介绍);

        HashMap<String, Object> params = new HashMap<>();
        params.put("head_img", info.m_head);
        FilterRes data = (FilterRes) info.m_ex;
        params.put("top_img", data.m_coverImg);
        params.put("name", data.m_authorName);
        params.put("detail", data.m_authorInfo);
        params.put("intro", data.m_filterDetail);
        params.put("img_url", data.m_filterIntroUrl);
        if (info.m_isLock) {
            params.put("unlock_tag", Tags.UNLOCK_FILTER);
            params.put("lock", true);
            params.put("filter_id", info.m_uri);
        }
        params.put("share_url", data.m_shareUrl);
        params.put("share_title", data.m_shareTitle);

        if (v != null) {
            int[] location = new int[2];
            v.getLocationOnScreen(location);

            params.put("centerX", location[0]);
            params.put("centerY", location[1]);
            params.put("viewH", v.getHeight());
            params.put("viewW", v.getWidth());
        }
        OpenMasterPage(params);

        m_curFilterUri = info.m_uri;
    }

    @Override
    public void setNextChain(EventRouter.EventChain chain) {
        mNextChain = chain;
    }

    @Override
    public boolean handleEvent(EventRouter.Event event) {
        if (event == EventRouter.Event.OnBack) {
            if (mShouldHandle) {
                if (m_filterType == BeautyColorType.ADJUST) {
                    m_curveView.setVisibility(View.GONE);
                    m_rgbFr.setVisibility(View.GONE);
                    m_btnLst.onClick(m_adjustDownBtn);
                    return true;
                }
                if (m_filterType == BeautyColorType.FILTER && m_seekBarFr.getVisibility() == View.VISIBLE) {
                    LayoutColorList(this, true, true);
                    return true;
                }
                if (mNextChain != null) {
                    mNextChain.handleEvent(event);
                }
            } else {
                mShouldHandle = true;
                if (mNextChain != null) {
                    mNextChain.handleEvent(event);
                }
                return false;
            }
        } else {
            if (mNextChain != null) {
                mNextChain.handleEvent(event);
            }
        }
        return false;
    }

    public void setHandleEvent(boolean handleEvent) {
        this.mShouldHandle = handleEvent;
    }


    protected void OpenMasterPage(HashMap<String, Object> params) {
        if (mAdapterCallback != null) {
            mAdapterCallback.onItemAvatarClick(params);
        }
    }

    protected void SetSelItemByUri(int uri) {
        if (m_filterType == BeautyColorType.FILTER) {
            FilterRes filterRes = null;
            if (m_listDatas != null) {
                int size = m_listDatas.size();
                for (int i = 0; i < size; i++) {
                    if (uri == m_listDatas.get(i).m_uri) {
                        filterRes = (FilterRes) m_listDatas.get(i).m_ex;
                        break;
                    }
                }
                if (mAdapterCallback != null) {
                    mAdapterCallback.onApplayFilter(filterRes, uri);
                }
            }
            if (m_colorMsg != null) {
                m_colorMsg.m_filterData = filterRes;
                if (filterRes != null) {
                    m_colorMsg.m_filterAlpha = filterRes.m_alpha;
                }

                if (filterRes != null) {
                    int progress = (int) ((filterRes.m_alpha / 100f) * m_colorSeekBar.getMax() + 0.5);
                    ReLayoutSeekBarTip(progress, m_colorSeekBar.getMax());
                    m_colorSeekBar.setProgress(progress);
                }
            }
            m_curFilterUri = uri;
            for (VideoResMgr.FilterData item :mFilterInfo.mFilterDataList) {
                item.mFilterUrl = m_curFilterUri;
            }

        }
    }

    public DragListItemInfo getDramItemInfoByUri(int uri) {
        DragListItemInfo itemInfo = null;
        if (m_listDatas != null) {
            int size = m_listDatas.size();
            for (int i = 0; i < size; i++) {
                if (uri == m_listDatas.get(i).m_uri) {
                    itemInfo = m_listDatas.get(i);
                    break;
                }
            }
        }
        return itemInfo;
    }


    private boolean mIsUp;
    protected void LayoutColorList(View v, final boolean isUp, boolean hasAnim) {
        if (v == null)
            return;
        v.clearAnimation();
        final int margin1 = m_bottomBarHeight + ShareData.PxToDpi_xhdpi(82);
        int start;
        int end;
        mIsUp = isUp;
        if (isUp) {
            m_seekBarFr.setVisibility(View.GONE);
            FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            fl.bottomMargin = m_resBarHeight;
            m_seekBarFr.setLayoutParams(fl);

            fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            fl.gravity = Gravity.BOTTOM;
            this.setLayoutParams(fl);

            start = margin1;
            end = 0;
        } else {
            start = 0;
            end = margin1;
        }

        if (hasAnim) {
            AnimationSet as;
            TranslateAnimation ta;
            as = new AnimationSet(true);
            ta = new TranslateAnimation(0, 0, start, end);
            ta.setDuration(350);
            ta.setFillAfter(true);
            ta.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    FrameLayout.LayoutParams fl;
                    if (!isUp) {
                        fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                        fl.gravity = Gravity.BOTTOM;
                        fl.bottomMargin = -margin1;
                        FilterBottomView.this.clearAnimation();
                        FilterBottomView.this.setLayoutParams(fl);

                        fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                        fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                        fl.bottomMargin = margin1 + m_resBarHeight - ShareData.PxToDpi_xhdpi(20);
                        if (m_seekBarFr != null) {
                            m_seekBarFr.clearAnimation();
                            m_seekBarFr.setVisibility(View.VISIBLE);
                            m_seekBarFr.setLayoutParams(fl);
                        }
                    }
                }
            });
            as.addAnimation(ta);
            v.startAnimation(as);
        }
    }


    protected void ReLayoutSeekBarTip(int progress, int max) {
        int w = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40);
        int leftMargin = ShareData.PxToDpi_xhdpi(20);
        int seekBarThumbW = ShareData.PxToDpi_xhdpi(21);
        LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) m_seekkBarTip.getLayoutParams();
        ll.leftMargin = (int) ((w - (seekBarThumbW << 1)) * progress / (float) max + leftMargin + seekBarThumbW - ShareData.PxToDpi_xhdpi(35));
        m_seekkBarTip.setLayoutParams(ll);
        String tip;
        switch (m_filterType) {
            case FILTER: {
                if (progress > 0) {
                    tip = "+" + progress;
                } else if (progress < 0) {
                    tip = "" + progress;
                } else {
                    tip = " " + progress;
                }
                m_seekkBarTip.setText(tip);
                break;
            }
            case ADJUST: {
                switch (m_curAdjustData.m_type) {
                    case BRIGHTNESS:
                    case CONTRAST:
                    case SATURABILITY:
                    case HUE:
                    case TEMPERATURE:
                        int progress1 = progress - 6;
                        if (progress1 > 0) {
                            tip = "+" + progress1;
                        } else if (progress1 < 0) {
                            tip = "" + progress1;
                        } else {
                            tip = "  " + progress1;
                        }
                        m_seekkBarTip.setText(tip);
                        break;
                    case DARKCORNER:
                    case HIGHTLIGHT:
                    case SHADE:
                    case SHARPEN: {
                        {
                            if (progress > 0) {
                                tip = "+" + progress;
                            } else if (progress < 0) {
                                tip = "" + progress;
                            } else {
                                tip = " " + progress;
                            }
                            m_seekkBarTip.setText(tip);
                            break;
                        }
                    }
                    default:
                        break;
                }
                break;
            }
        }
    }


    protected void colorBtnClick(BeautyColorType type) {
        m_filterType = type;
        FrameLayout.LayoutParams fl;
        if (m_filterType == BeautyColorType.ADJUST) {
            TongJi2.AddCountByRes(getContext(), R.integer.滤镜微调);
            adjustBtnClick(m_curAdjustData);
            int len = m_adjustItems.size();
            for (int i = 0; i < len; i++) {
                if (m_curAdjustData.m_type.GetValue() == m_adjustItems.get(i).m_uri) {
                    m_adjustList.SetSelByIndex(i);
                    break;
                }
            }

            fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            fl.bottomMargin = m_resBarHeight + ShareData.PxToDpi_xhdpi(20);
            m_seekBarFr.setLayoutParams(fl);

            SetViewState(m_filterTypeList, false, false);
            SetViewState(m_adjustBar, true, true);
            SetViewState(m_resList, false, false);
        } else if (m_filterType == BeautyColorType.FILTER) {

            fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            fl.bottomMargin = m_resBarHeight;
            m_seekBarFr.setLayoutParams(fl);

            SetViewState(m_adjustBar, false, false);
            SetViewState(m_filterTypeList, true, false);
            SetViewState(m_resList, true, true);
            m_seekBarShow = false;
            m_seekBarFr.setVisibility(View.GONE);

            FilterRes colorData = m_colorMsg.m_filterData;
            if (colorData != null) {
                int progress = (int) ((colorData.m_alpha / 100f) * m_colorSeekBar.getMax() + 0.5);
                ReLayoutSeekBarTip(progress, m_colorSeekBar.getMax());
                m_colorSeekBar.setProgress(progress);
            }

            if (mAdapterCallback != null) {
                mAdapterCallback.onFilterTypeChange(0);
            }
        }
    }

    protected boolean m_helpFlag = true;
    protected boolean m_curveShow;
    protected void adjustBtnClick(VideoResMgr.AdjustData data) {
        m_curAdjustData = data;
        if (m_curAdjustData == null) {
            return;
        }
        InsertToAdjustList(data);

        if(m_curAdjustData.m_type == BeautyAdjustType.CURVE)
        {
            //不弹曲线教程
//            if(TagMgr.CheckTag(getContext(), "first_enter_curve"))
//            {
//                postDelayed(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        if(m_helpFlag)
//                        {
//                            m_helpFlag = false;
//                            HashMap<String, Object> params = new HashMap<>();
//                            InitBkImg();
//                            params.put("img", m_bkBmp);
//                            if (mAdapterCallback != null) {
//                                mAdapterCallback.openHelpPage(params, getContext());
//                            }
//                            TagMgr.SetTag(getContext(), "first_enter_curve");
//                        }
//                    }
//                }, 400);
//            }
            shenCeOnClickRes(R.string.视频滤镜_曲线, R.string.单视频细节调整_曲线);
//            m_btnLst.onClick(m_btnRGB);
            m_seekBarFr.setVisibility(View.GONE);
            m_curveShow = true;
            m_scanBtn.setImageResource(R.drawable.beauty_curve_show);
            m_curveView.setVisibility(View.VISIBLE);
            m_rgbFr.setVisibility(View.VISIBLE);
//            m_icon.setVisibility(View.VISIBLE);
            return;
        } else if (m_curAdjustData.m_type != BeautyAdjustType.NONE) {
            m_seekBarShow = true;
            m_curveView.setVisibility(View.GONE);
            m_rgbFr.setVisibility(View.GONE);
            m_seekBarFr.setVisibility(View.VISIBLE);
//            m_icon.setVisibility(View.GONE);
        } else {
            m_seekBarShow = false;
            m_curveView.setVisibility(View.GONE);
            m_rgbFr.setVisibility(View.GONE);
            m_seekBarFr.setVisibility(View.GONE);
//            m_icon.setVisibility(View.GONE);
        }
        m_colorSeekBar.setProgress(0);
        int progress = 0;
        if (m_seekBarFr != null && m_curAdjustData.m_type != BeautyAdjustType.NONE) {
            if (m_seekBarFr.getVisibility() == View.GONE) {
                m_seekBarFr.setVisibility(View.VISIBLE);
            }
            int max = m_colorSeekBar.getMax();
            switch (m_curAdjustData.m_type) {
                case BRIGHTNESS:
                    progress = (int) (((m_curAdjustData.m_value + 30) / 60f) * max + 0.5f);
                    shenCeOnClickRes(R.string.视频滤镜_亮度, R.string.单视频细节调整_亮度);
                    break;
                case CONTRAST:
                    progress = (int) (((m_curAdjustData.m_value - 0.9f) / 0.3f) * max + 0.5f);
                    shenCeOnClickRes(R.string.视频滤镜_对比度, R.string.单视频细节调整_对比度);
                    break;

                case SATURABILITY:
                    progress = (int) (((m_curAdjustData.m_value - 0.5f) / 1f) * max + 0.5f);
                    shenCeOnClickRes(R.string.视频滤镜_饱和度, R.string.单视频细节调整_饱和度);
                    break;
                case TEMPERATURE: {
                    progress = (int) (((m_curAdjustData.m_value + 0.35f) / 0.7f) * max + 0.5f);
                    shenCeOnClickRes(R.string.视频滤镜_色温, R.string.单视频细节调整_色温);
                    break;
                }
                case SHARPEN:
                    progress = (int) (m_curAdjustData.m_value * max + 0.5f);
                    shenCeOnClickRes(R.string.视频滤镜_锐度, R.string.单视频细节调整_锐度);
                    break;

                case HUE:
                    progress = (int) (((m_curAdjustData.m_value + 0.1f) / 0.2f) * max + 0.5f);
                    shenCeOnClickRes(R.string.视频滤镜_色调, R.string.单视频细节调整_色调);
                    break;

                case DARKCORNER:
                    progress = (int) (m_curAdjustData.m_value * max + 0.5f);
                    shenCeOnClickRes(R.string.视频滤镜_暗角, R.string.单视频细节调整_暗角);
                    break;

                case SHADE:
                    progress = (int) (m_curAdjustData.m_value * max + 0.5f);
                    shenCeOnClickRes(R.string.视频滤镜_阴影补偿, R.string.单视频细节调整_阴影补偿);
                    break;

                case HIGHTLIGHT:
                    progress = (int) (m_curAdjustData.m_value * max + 0.5f);
                    shenCeOnClickRes(R.string.视频滤镜_高光减淡, R.string.单视频细节调整_高光减淡);
                    break;

                default:
                    break;
            }
            int lastProgress = m_colorSeekBar.getProgress();
            int newProgress = progress;
            if (lastProgress == newProgress) {
                ReLayoutSeekBarTip(newProgress, max);
            }
            m_colorSeekBar.setProgress(progress);
        }
    }


    protected void SetViewState(View v, boolean isOpen, boolean hasAnimation) {
        if (v == null)
            return;
        v.clearAnimation();

        int start;
        int end;
        if (isOpen) {
            v.setVisibility(View.VISIBLE);

            start = 1;
            end = 0;
        } else {
            v.setVisibility(View.GONE);

            start = 0;
            end = 1;
        }

        if (hasAnimation) {
            AnimationSet as;
            TranslateAnimation ta;
            as = new AnimationSet(true);
            ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, start, Animation.RELATIVE_TO_SELF, end);
            ta.setDuration(200);
            as.addAnimation(ta);
            v.startAnimation(as);
        }
    }

    protected void InsertToAdjustList(VideoResMgr.AdjustData data) {
        if (m_colorMsg != null) {
            VideoResMgr.AdjustData tempData;

            //清理掉效果为0的数据
            for (int i = 0; i < m_colorMsg.m_adjustData.size(); i++) {
                tempData = m_colorMsg.m_adjustData.get(i);
                if (tempData.m_type != BeautyAdjustType.NONE && tempData.m_value == 0) {
                    m_colorMsg.m_adjustData.remove(i);
                    i--;
                }
                if (tempData.m_type == BeautyAdjustType.NONE && data.m_type == BeautyAdjustType.NONE) {
                    m_colorMsg.m_adjustData.remove(i);
                    i--;
                }
            }

            boolean flag = true;
            for (int i = 0; i < m_colorMsg.m_adjustData.size(); i++) {
                tempData = m_colorMsg.m_adjustData.get(i);
                if (data.m_type == tempData.m_type) {
                    m_colorMsg.m_adjustData.set(i, data);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                m_colorMsg.m_adjustData.add(data);
            }
        }
    }

    public void setBottomViewCallback(BottomViewCallbackAdapter callback) {
        this.mAdapterCallback = callback;
    }


    public void unlockMasterRes(int id) {
        if (m_listAdapter != null) {
            DragListItemInfo info;
            m_listAdapter.Unlock(id);
            int index = m_listAdapter.GetIndexByUri(id);
            if (index != -1) {
                info = m_listDatas.get(index);
                onClickNormalItem(null, info, index);
            }
        }
    }





    @Override
    public void releaseMemButKeepData() {
        super.releaseMemButKeepData();
        if (m_orgInfo != null) {
            m_orgInfo.recycle();
            m_orgInfo = null;
        }

        DownloadMgr.getInstance().RemoveDownloadListener(m_downloadLst);
        m_downloadLst = null;
    }

    public void onPageResult(int siteID, HashMap<String, Object> params) {
//        if(siteID == SiteID.BIGHT_HELP_ANIM || siteID == SiteID.FILTER_HELP_ANIM)
//        {
//            m_helpFlag = true;
        if (siteID == SiteID.THEME_INTRO_PAGE || siteID == SiteID.THEME_PAGE) {
            if (params != null) {
                int sel_uri = -1;
                if (params.containsKey("id")) {
                    sel_uri = (Integer) params.get("id");
                }
                boolean needRefresh = false;
                if (params.containsKey("need_refresh")) {
                    needRefresh = (Boolean) params.get("need_refresh");
                }
                if (needRefresh) {
                    UpdateListDatas();
                    boolean validity = checkFiltersUriValidity(mFilterInfo.mFilterDataList);
                    mIsUrlStillValid = validity;
                    if (!validity) {
                        m_listAdapter.SetSelByIndex(1);
                    }
                    if (!mIsUp) {
                        LayoutColorList(this, true, true);
                    }
                }
                if (sel_uri != -1) {
                    DragListItemInfo info;
                    int index = m_listAdapter.GetIndexByUri(sel_uri);
                    if (index != -1) {
                        info = m_listDatas.get(index);
                        onClickNormalItem(null, info, index);
                    }
                } else {
                    if (!mIsUrlStillValid) {
                        SetSelItemByUri(DragListItemInfo.URI_ORIGIN);
                    }
                }
            }
        }
    }


    protected SeekBar.OnSeekBarChangeListener m_seekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ReLayoutSeekBarTip(progress, seekBar.getMax());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            int maxP = seekBar.getMax();
            if (m_colorMsg != null) {
                switch (m_filterType) {
                    case FILTER: {
                        int filterAlpha = 0;
                        if (m_colorMsg.m_filterData != null) {
//                            m_colorMsg.m_filterAlpha = (int) ((progress / (float) maxP) * 100 + 0.5f);
                            filterAlpha = (int) ((progress / (float) maxP) * 100 + 0.5f);
                        }
                        if (mAdapterCallback != null) {
                            mAdapterCallback.onAdjustbarSeek(m_filterType, null, filterAlpha, progress);
                        }

                        int size = m_listDatas.size();
                        FilterRes filterRes = null;
                        for (int i = 0; i < size; i++) {
                            if (m_curFilterUri == m_listDatas.get(i).m_uri) {
                                filterRes = (FilterRes) m_listDatas.get(i).m_ex;
                                break;
                            }
                        }
                        if (filterRes != null) {
                            filterRes.m_alpha = (int)((progress / (float)maxP) * 100);
                        }
//                        synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
//                        {
//                            SQLiteDatabase db = FilterResMgr2.getInstance().GetDB();
//                            if (db != null) {
//                                FilterResMgr2.getInstance().SaveResByDB(db, filterRes);
//                                FilterResMgr2.getInstance().CloseDB();
//                            }
//                        }
                        break;
                    }
                    case ADJUST: {
                        switch (m_curAdjustData.m_type) {
                            case BRIGHTNESS: {
                                m_curAdjustData.m_value = -30 + (progress * 2 / (float) maxP) * 30;
                                break;
                            }
                            case CONTRAST: {
                                m_curAdjustData.m_value = 0.9f + (progress / (float) maxP) * 0.3f;
                                break;
                            }
                            case SATURABILITY: {
                                m_curAdjustData.m_value = 0.5f + ((progress / (float) maxP) * 1f);
                                break;
                            }
                            case TEMPERATURE: {
                                m_curAdjustData.m_value = -0.35f + (progress * 2 / (float) maxP) * 0.35f;
                                break;
                            }
                            case SHARPEN: {
                                m_curAdjustData.m_value = progress / (float) maxP;
                                break;
                            }

                            case HUE: {
                                m_curAdjustData.m_value = -0.1f + (progress * 2 / (float) maxP) * 0.1f;
                                break;
                            }

                            case HIGHTLIGHT: {
                                m_curAdjustData.m_value = ((progress / (float) maxP));
                                break;
                            }

                            case DARKCORNER: {
                                m_curAdjustData.m_value = ((progress / (float) maxP));
                                break;
                            }

                            case SHADE: {
                                m_curAdjustData.m_value = ((progress / (float) maxP));
                                break;
                            }
                            default:
                                break;
                        }
                        if (mAdapterCallback != null) {
                            mAdapterCallback.onAdjustbarSeek(m_filterType, m_curAdjustData.m_type, m_curAdjustData.m_value, progress);
                        }

                        break;
                    }
                    default:
                        break;
                }
            }
        }
    };


    protected SimpleBtnList100.Callback m_filterListCB = new SimpleBtnList100.Callback() {

        @Override
        public void OnClick(SimpleBtnList100.Item view, int index) {
            {
                BeautyColorType type = (BeautyColorType) (((SimpleListItem) view).m_ex);
                if (m_filterTypeList != null) {
                    m_filterTypeList.SetSelByIndex(index);
                }
                if (mAdapterCallback != null) {
                    mAdapterCallback.onFilterTypeChange(index);
                }

                if (m_filterType != type || m_filterType != BeautyColorType.FILTER) {
                    colorBtnClick(type);
                }
            }
        }

    };

    protected SimpleBtnList100.Callback m_adjustBtnListCB = new SimpleBtnList100.Callback() {

        @Override
        public void OnClick(SimpleBtnList100.Item view, int index) {
            VideoResMgr.AdjustData data = (VideoResMgr.AdjustData) (((SimpleListItem) view).m_ex);

            if (m_curAdjustData == null || m_curAdjustData.m_type != data.m_type) {
                TongJi2.AddCountByRes(getContext(), ((SimpleListItem) view).m_tjID);
                if (m_adjustList != null) {
                    m_adjustList.SetSelByIndex(index);
                    m_adjustList.ScrollToCenter(true);
                }
                m_adjustTip = ((SimpleListItem) view).m_title;
                adjustBtnClick(data);
                if (mAdapterCallback != null) {
                    mAdapterCallback.onAdjustModeSelected(m_curAdjustData.m_type, m_adjustTip);
                }
            } else {
                if (data.m_type != BeautyAdjustType.CURVE) {
                    if (m_seekBarShow == true) {
                        m_seekBarShow = false;
                        m_seekBarFr.setVisibility(View.GONE);
                        String text = getResources().getString(R.string.Adjust);
                        m_adjustTip = text;
//                        m_title.setText(text);
                        m_adjustList.SetSelByIndex(-1);
                    } else {
                        m_seekBarShow = true;
//                        m_titleFr.setVisibility(View.VISIBLE);
//                        m_title.setText(m_adjustTip);
                        m_seekBarFr.setVisibility(View.VISIBLE);
                        m_adjustList.SetSelByIndex(index);
                    }
                }
                if (mAdapterCallback != null) {
                    mAdapterCallback.onAdjustModeSelected(m_curAdjustData.m_type, m_adjustTip);
                }
            }
        }
    };


    protected class MyBtnLst implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v == m_adjustDownBtn) {
                SetViewState(m_seekBarFr, false, false);
                if (m_filterTypeList != null)
                {
                    m_filterTypeList.SetSelByIndex(0);
                }
                colorBtnClick(BeautyColorType.FILTER);
                m_curveView.setVisibility(View.GONE);
                m_rgbFr.setVisibility(View.GONE);
            }
            else if(v == m_btnRGB)
            {
                m_btnRGB.OnChoose(true);
                m_btnR.OnChoose(false);
                m_btnG.OnChoose(false);
                m_btnB.OnChoose(false);
                m_curveView.SetMode(CurveView.MODE_RGB);
            }
            else if(v == m_btnR)
            {
                m_btnRGB.OnChoose(false);
                m_btnR.OnChoose(true);
                m_btnG.OnChoose(false);
                m_btnB.OnChoose(false);
                m_curveView.SetMode(CurveView.MODE_RED);
            }
            else if(v == m_btnG)
            {
                m_btnRGB.OnChoose(false);
                m_btnR.OnChoose(false);
                m_btnG.OnChoose(true);
                m_btnB.OnChoose(false);
                m_curveView.SetMode(CurveView.MODE_GREEN);
            }
            else if(v == m_btnB)
            {
                m_btnRGB.OnChoose(false);
                m_btnR.OnChoose(false);
                m_btnG.OnChoose(false);
                m_btnB.OnChoose(true);
                m_curveView.SetMode(CurveView.MODE_BLUE);
            }
            else if(v == m_scanBtn)
            {
                if(m_curveShow)
                {
                    m_curveShow = false;
                    m_scanBtn.setImageResource(R.drawable.beauty_curve_hide);
                    m_curveView.setVisibility(View.GONE);
                }
                else
                {
                    m_curveShow = true;
                    m_scanBtn.setImageResource(R.drawable.beauty_curve_show);
                    m_curveView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

//    protected HashMap<Integer, Boolean> m_themeCompleteFlags = new HashMap<>();
    public AbsDownloadMgr.DownloadListener m_downloadLst = new AbsDownloadMgr.DownloadListener() {
        @Override
        public void OnDataChange(int resType, int downloadId, IDownload[] resArr) {
            if (resArr != null && resArr.length > 0) {
                int type = ((BaseRes) resArr[0]).m_resType;
                if (type == ResType.FILTER.GetValue()) {
                    boolean flag = true;
                    int theme_id = ((BaseRes) resArr[resArr.length - 1]).m_id;
                    for (int i = 0; i < resArr.length; i++) {
                        if (((BaseRes) resArr[i]).m_type != BaseRes.TYPE_LOCAL_PATH) {
                            flag = false;
                            break;
                        }
                    }
//                    boolean need_update = false;
//                    if (m_themeCompleteFlags.get(theme_id) == null || m_themeCompleteFlags.get(theme_id) == false) {
//                        need_update = true;
//                    }
//
//                    if (flag && need_update) {
//                        m_themeCompleteFlags.put(theme_id, true);
//                    }
                    if (flag) {
                        UpdateListDatas();
                    }
                }
            }
        }
    };

    protected CurveView.Callback m_curveCB = new CurveView.Callback()
    {

        @Override
        public void OnDown(PointF point, ArrayList<PointF> m_ctrlPoints)
        {
            m_curveView.ShowCoord(true);
            OnMove(point, m_ctrlPoints);
        }

        @Override
        public void OnMove(PointF point, ArrayList<PointF> m_ctrlPoints)
        {
            int x = 0, y = 0;

            if(point != null)
            {
                x = (int)(point.x / m_curViewSize * 255);
                y = (int)((m_curViewSize - point.y) / m_curViewSize * 255);
            }
            m_curveView.SetCoord(x, y);


            List<Point> controlPointList = new ArrayList<>();
            for (PointF item : m_ctrlPoints) {
                int[] result = makeValidPoint(item.x, item.y);
                controlPointList.add(new Point(result[0], result[1]));
            }

            if (mAdapterCallback != null) {
                mAdapterCallback.doCurveEffect(m_curveView.GetMode(), controlPointList, m_curveView.getRedControlInfo(), m_curveView.getGreenControlInfo(), m_curveView.getBlueControlInfo(), m_curveView.getRgbControlInfo());
            }
//            m_view.setImageBitmap(m_curveBmp);
        }

        @Override
        public void OnUp(PointF point, ArrayList<PointF> m_ctrlPoints)
        {
			m_curveView.ShowCoord(false);
            int x = 0, y = 0;

            if(point != null)
            {
                x = (int)(point.x / m_curViewSize * 255);
                y = (int)((m_curViewSize - point.y) / m_curViewSize * 255);
            }
            m_curveView.SetCoord(x, y);

            List<Point> controlPointList = new ArrayList<>();
            for (PointF item : m_ctrlPoints) {
                int[] result = makeValidPoint(item.x, item.y);
                controlPointList.add(new Point(result[0], result[1]));
            }
            if (mAdapterCallback != null) {
                mAdapterCallback.doCurveEffect(m_curveView.GetMode(), controlPointList, m_curveView.getRedControlInfo(), m_curveView.getGreenControlInfo(), m_curveView.getBlueControlInfo(), m_curveView.getRgbControlInfo());
            }
//            m_curveBmp = DoCurve();
//            m_view.setImageBitmap(m_curveBmp);
        }
    };


    private int[] makeValidPoint(float x, float y) {
        int[] result = new int[2];
        result[0] = (int)(x / m_curViewSize * 255);
        result[1] = (int)((m_curViewSize - y) * 1.0f / m_curViewSize * 255);
        result[0] = result[0] < 0 ? 0 : result[0] > 255 ? 255 : result[0];
        result[1] = result[1] < 0 ? 0 : result[1] > 255 ? 255 : result[1];
        return result;
    }



}

