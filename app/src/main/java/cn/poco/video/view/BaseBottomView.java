package cn.poco.video.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.poco.beautify.BeautifyResMgr;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.draglistview.DragListView;
import cn.poco.draglistview.MyDragItemAdapter;
import cn.poco.interphoto2.R;
import cn.poco.statistics.TongJi2;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.ImageUtil;
import cn.poco.utils.Utils;
import cn.poco.video.VideoDragAdapter;
import cn.poco.video.page.ProcessMode;

/**
 * Created by Shine on 2017/6/6.
 */

public abstract class BaseBottomView extends FrameLayout{
    // UI布局数据
    protected int m_resBarHeight;
    protected int m_bottomBarHeight;

    protected FrameLayout m_resListFr;

    protected List<DragListItemInfo> m_listDatas;
    public VideoDragAdapter m_listAdapter;
    protected VideoDragAdapter.MyDragItem m_dragItem;	//拖动的item
    protected DragListView m_resList;    //资源列
    protected DefaultItemAnimator m_recycleAnim;
    protected LinearLayout m_hideFr;
    protected ImageView m_hideIcon;
    protected TextView m_hideText;

    protected boolean m_isChooseHideFr = false;


    protected ProcessMode mMode;

    public BaseBottomView(@NonNull Context context) {
        super(context);
    }


    protected void initData() {
        m_resBarHeight = ShareData.PxToDpi_xhdpi(160);
        m_bottomBarHeight = ShareData.PxToDpi_xhdpi(80);


    }

    protected void initView() {
        m_resListFr = new FrameLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.BOTTOM;
        m_resListFr.setLayoutParams(params);
        this.addView(m_resListFr);
        setUpResList();
    }

    /**
     * 继承之后，可以根据需要重写此函数，但首先需要通过super调用此函数
     */
    public void releaseMem()
    {
        this.clearAnimation();
        this.removeAllViews();
        if(m_listDatas != null)
        {
            m_listDatas.clear();
            m_listDatas = null;
            m_listAdapter.notifyDataSetChanged();
        }
        if(m_listAdapter != null)
        {
            m_listAdapter.ClearAll();
            m_listAdapter = null;
        }
        if(m_resList != null)
        {
            m_resList.Clear();
            m_resList.setAdapter(null, true);
            m_listCallback = null;
            m_resList.removeAllViews();
            m_resList = null;
        }
        m_dragControlCB = null;
        m_dragListener = null;

    }

    public void releaseMemButKeepData() {
        this.clearAnimation();
        this.removeAllViews();
        if(m_listAdapter != null)
        {
            m_listAdapter.ClearAll();
            m_listAdapter = null;
        }
        if(m_resList != null)
        {
            m_resList.Clear();
            m_resList.setAdapter(null, true);
            m_listCallback = null;
            m_resList.removeAllViews();
            m_resList = null;
        }
        m_dragControlCB = null;
        m_dragListener = null;

    }

    private void setUpResList() {
        if (m_listDatas != null)
        {
            m_listDatas.clear();
            m_listDatas = null;
        }
        if(m_listAdapter != null)
        {
            m_listAdapter.ClearAll();
            m_listAdapter = null;
        }

        m_listDatas = initItemList();
        m_dragItem = new MyDragItemAdapter.MyDragItem(getContext(), new ImageView(getContext()));

        m_resList = new DragListView(getContext());
        m_resList.setCustomDragItem(m_dragItem);
        LinearLayoutManager lin = new LinearLayoutManager(getContext());
        lin.setOrientation(LinearLayoutManager.HORIZONTAL);
        m_resList.setLayoutManager(lin);
        m_resList.setCanDragHorizontally(true);
        m_resList.setDragListCallback(m_dragControlCB);
        m_resList.setDragListListener(m_dragListener);

        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        fl.gravity = Gravity.BOTTOM;
        m_resList.setLayoutParams(fl);
        if(m_resListFr != null)
            m_resListFr.addView(m_resList);
        else{
            this.addView(m_resList);
        }

        fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.BOTTOM;
        fl.bottomMargin = m_bottomBarHeight + ShareData.PxToDpi_xhdpi(20);
        m_resList.getRecyclerView().setLayoutParams(fl);
        m_resList.getRecyclerView().setItemAnimator(null);
        m_recycleAnim = new DefaultItemAnimator();

        m_listAdapter = new VideoDragAdapter(getContext(), true);
        // // TODO: 2017/6/6 showTitle argument;
        m_listAdapter.showTitle(true);
        m_listAdapter.setItemList(m_listDatas);
        m_listAdapter.SetOnClickCallback(m_listCallback);
        m_resList.setAdapter(m_listAdapter, true);

        m_hideFr = new LinearLayout(getContext());
        m_hideFr.setGravity(Gravity.CENTER);
        m_hideFr.setOrientation(LinearLayout.VERTICAL);
        m_hideFr.setVisibility(GONE);
        m_hideFr.setBackgroundResource(R.drawable.framework_hide_bg_out);
        fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.CENTER;
        m_hideFr.setLayoutParams(fl);
        m_resList.addView(m_hideFr, 0);
        {
            LinearLayout.LayoutParams ll;
            m_hideIcon = new ImageView(getContext());
            m_hideIcon.setImageResource(R.drawable.framework_delete_icon_out);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            m_hideIcon.setLayoutParams(ll);
            m_hideFr.addView(m_hideIcon);

            m_hideText = new TextView(getContext());
            m_hideText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            m_hideText.setTextColor(Color.WHITE);
            m_hideText.setText(R.string.delete_resource);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.topMargin = ShareData.PxToDpi_xhdpi(20);
            m_hideText.setLayoutParams(ll);
            m_hideFr.addView(m_hideText);
        }

    }




    public void UpdateListDatas()
    {
        if (m_listDatas != null)
        {
            m_listDatas.clear();
            m_listDatas.addAll(initItemList());
            if(m_listAdapter != null)
            {
                m_listAdapter.ReleaseMem();
                m_listAdapter.setItemList(m_listDatas);
                m_listAdapter.SelCurUri();
                m_listAdapter.notifyDataSetChanged();
            }
        }
    }


    protected DragListView.DragListCallback m_dragControlCB = new DragListView.DragListCallback()
    {
        @Override
        public boolean canDragItemAtPosition(int dragPosition)
        {
            if(m_listDatas != null && dragPosition >= 0 && dragPosition < m_listDatas.size())
            {
                return m_listDatas.get(dragPosition).m_canDrag;
            }
            return false;
        }

        @Override
        public boolean canDropItemAtPosition(int dropPosition)
        {
            if(m_listDatas != null && dropPosition >= 0 && dropPosition < m_listDatas.size())
            {
                return m_listDatas.get(dropPosition).m_canDrop;
            }
            return false;
        }
    };


    protected DragListView.DragListListener m_dragListener = new DragListView.DragListListener()
    {
        @Override
        public void onItemDragStarted(int position)
        {
            if (mMode == ProcessMode.Fileter) {
                TongJi2.AddCountByRes(getContext(), R.integer.滤镜_长按隐藏);
            }
            m_isChooseHideFr = false;
            m_resList.getRecyclerView().setItemAnimator(m_recycleAnim);
            m_resList.setBackgroundColor(0xb2000000);
//			m_hideFr.setVisibility(VISIBLE);
            if(canDelete(position)) {
                Utils.AlphaAnim(m_hideFr, true, 400);
            }
        }

        @Override
        public void onItemDragging(int itemPosition, float x, float y)
        {
            boolean laseChoose = m_isChooseHideFr;
            RectF rectF = new RectF(m_hideFr.getLeft(), m_hideFr.getTop(), m_hideFr.getRight(), m_hideFr.getBottom());
            if(x >= rectF.left && x <= rectF.right && y >= rectF.top && y <= rectF.bottom)
            {
                m_isChooseHideFr = true;
                if(m_dragItem != null)
                {
                    m_dragItem.DoAlphaAnim(true);
                }
            }
            else
            {
                m_isChooseHideFr = false;
                if(m_dragItem != null)
                {
                    m_dragItem.DoAlphaAnim(false);
                }
            }
            if(laseChoose == !m_isChooseHideFr)
            {
                if(m_isChooseHideFr)
                {
                    m_hideFr.setBackgroundResource(R.drawable.framework_hide_bg_over);
                    m_hideIcon.setImageResource(R.drawable.framework_delete_icon_over);
                    m_hideText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                }
                else
                {
                    m_hideFr.setBackgroundResource(R.drawable.framework_hide_bg_out);
                    m_hideIcon.setImageResource(R.drawable.framework_delete_icon_out);
                    m_hideText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                }
            }
        }

        @Override
        public void onItemDragEnded(int fromPosition, int toPosition)
        {
            m_resList.getRecyclerView().setItemAnimator(null);
            m_resList.setBackgroundColor(0x00000000);
//			m_hideFr.setVisibility(GONE);
            if (m_hideFr.getVisibility() != GONE) {
                Utils.AlphaAnim(m_hideFr, false, 400);
            }
            if(m_isChooseHideFr && canDelete(fromPosition))
            {
                OnHideItem(fromPosition);

                m_listAdapter.removeItem(toPosition);
            }
            else if(fromPosition != toPosition)
            {
                OnChangeItem(fromPosition, toPosition);
            }
        }
    };

    protected MyDragItemAdapter.OnItemClickListener m_listCallback = new MyDragItemAdapter.OnItemClickListener()
    {
        @Override
        public void OnItemClick(View view, DragListItemInfo info, int index)
        {
            onClickNormalItem(view, info, index);
        }

        @Override
        public void OnHeadClick(View view, DragListItemInfo info, int index)
        {
            onHeadClick(view, info, index);
        }
    };

    public void setData(List<DragListItemInfo> listDatas)
    {
        this.m_listDatas = listDatas;
        m_listAdapter.ReleaseMem();
        m_listAdapter.setItemList(listDatas);
    }


    protected Bitmap m_bkBmp;
    protected synchronized void InitBkImg()
    {
        if((m_bkBmp == null || m_bkBmp.isRecycled()))
        {
            Bitmap bmp = ImageUtil.GetScreenBmp((Activity)getContext(), ShareData.m_screenWidth / 6, ShareData.m_screenHeight / 6);

            m_bkBmp = BeautifyResMgr.MakeBkBmp(bmp, ShareData.m_screenWidth, ShareData.m_screenHeight, 0xcc000000, 0x26000000);
        }
    }

    protected abstract List<DragListItemInfo> initItemList();

    protected abstract void OnHideItem(int position);
    /**交换item的顺序*/
    protected abstract void OnChangeItem(int fromPosition, int toPosition);

    protected abstract void onClickNormalItem(View v, DragListItemInfo info, int index);

    protected void onHeadClick(View v, DragListItemInfo info, int index) {

    };

    protected abstract boolean canDelete(int position);
}
