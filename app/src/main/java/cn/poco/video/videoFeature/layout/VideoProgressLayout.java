package cn.poco.video.videoFeature.layout;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.poco.draglistview.DragItemRecyclerView;
import cn.poco.draglistview.DragListView;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.video.render.GLVideoView;
import cn.poco.video.sequenceMosaics.AdapterDataInfo;
import cn.poco.video.sequenceMosaics.TransitionDataInfo;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.videoFeature.SectionOnScrollListener;
import cn.poco.video.videoFeature.VideoGroupInfo;
import cn.poco.video.videoFeature.adapter.VideoListAdapter3;
import cn.poco.video.videoFeature.cell.VideoCoverCell;
import cn.poco.video.videoFeature.view.ProgressLine;
import cn.poco.video.videoFeature.view.TransitionListView;

/**
 * Created by Simon Meng on 2018/1/9.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoProgressLayout extends FrameLayout{

    public interface VideoProgressLayoutCallback {
        void onTransitionSelected(int transitionIndex, VideoGroupInfo videoGroupInfo);
        void onClickVideo(int index);
        void onClickAddVideoBtn();
        void onScrolledByUser(int index, float progress);
        void onStartScroll();
        void onStopScroll();
        void startDragVideo(int position);
        void changeVideoOrder(int fromPosition, int toPosition, int index, float progress);
        boolean canAddTransition(int index);
    }


    private Context mContext;
    private DragListView mVideoListView;
    private VideoListAdapter3.VideoDragItem mDragItem;
    private VideoListAdapter3 mVideoAdapter;
    private ImageView mAddBtn;
    private Rect mAddRect;
    private float mMinScale = 0.8f;
    private boolean mShowAddBtn = true;
    private boolean mExit = false;

    private TransitionListView mTransitionListView;
    private ProgressLine mProgressLine;
    private RecyclerView.ItemAnimator mItemAnimator;
//    private View mShadowLayer;

    private VideoProgressLayoutCallback mCallback;

    public VideoProgressLayout(@NonNull Context context) {
        super(context);
        mContext = context;
        initView();
    }


    private RecyclerView mRecyclerView;
    private int mCurSelectedPosition = -1;
    private void initView() {
        FrameLayout.LayoutParams fl;

        mVideoListView = new DragListView(getContext());
        mDragItem = new VideoListAdapter3.VideoDragItem(getContext());
        mVideoListView.setCustomDragItem(mDragItem);
        final LinearLayoutManager lin = new LinearLayoutManager(getContext());
        lin.setOrientation(LinearLayoutManager.HORIZONTAL);
        mVideoListView.setLayoutManager(lin);
        mVideoListView.setCanDragHorizontally(true);
        ((DragItemRecyclerView)mVideoListView.getRecyclerView()).canFling(false);
        mVideoListView.setDragListCallback(m_dragControlCB);
        mVideoListView.setDragListListener(mDragListListener);
        mRecyclerView = mVideoListView.getRecyclerView();

        mItemAnimator = new DefaultItemAnimator();
        mRecyclerView.setItemAnimator(null);
        LayoutParams ll = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        ll.gravity = Gravity.CENTER;
        mVideoListView.setLayoutParams(ll);
        this.addView(mVideoListView);
        fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP);
        fl.topMargin = ShareData.PxToDpi_xhdpi(70);
        mRecyclerView.setLayoutParams(fl);
        mVideoAdapter = new VideoListAdapter3(getContext());
    }

//    private int mTopMargin = ShareData.PxToDpi_xhdpi(18); //图片居中， 设计图文字大小22，下移一半
    private ArrayList<VideoInfo> mVideoList = new ArrayList<>();
    public void setData(ArrayList<VideoInfo> videoInfoList) {
        mVideoList.clear();
        mVideoList.addAll(videoInfoList);
        mVideoAdapter.SetItemDatas(mVideoList);
        mVideoListView.setAdapter(mVideoAdapter, true);

        mTransitionListView = new TransitionListView(mContext, mVideoList.size() - 1);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
//        params.topMargin = mTopMargin;
        mTransitionListView.setLayoutParams(params);

        this.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        this.addView(mTransitionListView);

        mProgressLine = new ProgressLine(mContext);
        params = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(6), FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        mProgressLine.setLayoutParams(params);
        this.addView(mProgressLine);

//        mShadowLayer = new View(mContext);
//        mShadowLayer.setBackgroundColor(0x7f000000);
//        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL);
//        params.leftMargin = (ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(6)) / 2 + ShareData.PxToDpi_xhdpi(6);
//        mShadowLayer.setLayoutParams(params);
        initListener();

        mAddBtn = new ImageView(getContext());
        mAddBtn.setVisibility(GONE);
//        mAddBtn.setBackgroundColor(Color.WHITE);
        int padding = ShareData.PxToDpi_xhdpi(8);
        mAddBtn.setPadding(padding, padding, padding, padding);
        params = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(80), ShareData.PxToDpi_xhdpi(80));
        params.gravity = Gravity.CENTER | Gravity.RIGHT;
//        params.topMargin = mTopMargin;
        mAddBtn.setImageResource(R.drawable.video_transition_addvideo_btn);
        mAddBtn.setLayoutParams(params);
        this.addView(mAddBtn);
        scaleAddBtn(mMinScale);
        mAddBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mCallback != null) {
                    mCallback.onClickAddVideoBtn();
                }
            }
        });
        mAddRect = new Rect(0, 0, ShareData.PxToDpi_xhdpi(80), ShareData.PxToDpi_xhdpi(80));
    }


    private SectionOnScrollListener mScrollListener;
    private void initListener() {
        mVideoAdapter.SetOnItemClickListener(new VideoListAdapter3.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, AdapterDataInfo info, int index) {

                if (view instanceof VideoCoverCell) {
                    mCurSelectedPosition = index - 1;
                    setSelectedVideo(mCurSelectedPosition);

                    if (mCallback != null) {
                        mCallback.onClickVideo(mCurSelectedPosition);
                    }
                }
            }

            @Override
            public void OnIconClick(View view, AdapterDataInfo info, int index) {
                if (mCallback != null) {
                    mCallback.onClickAddVideoBtn();
                }

            }
        });


        mTransitionListView.setCallback(new TransitionListView.TransitionViewCallback() {
            @Override
            public void onClickItem(int index, TransitionDataInfo transitionDataInfo) {
                if (mCallback != null) {
                    VideoGroupInfo videoGroupInfo = new VideoGroupInfo();
                    videoGroupInfo.mTransitionDataInfo = transitionDataInfo;
                    String leftPath = mVideoAdapter.mMapIndexToBitmapPath.get(index);
                    String rightPath = mVideoAdapter.mMapIndexToBitmapPath.get(index + 1);
                    videoGroupInfo.mLeftBitmapPath = leftPath;
                    videoGroupInfo.mRightBitmapPath = rightPath;
                    videoGroupInfo.mGroupIndex = index;
                    mCallback.onTransitionSelected(index, videoGroupInfo);
                }
            }

            @Override
            public boolean canAddTransition(int index)
            {
                if(mCallback != null)
                {
                    return mCallback.canAddTransition(index);
                }
                return false;
            }
        });
        int contentWidth = mVideoAdapter.getVisibleWidth();
        int leftPadding = ShareData.PxToDpi_xhdpi(0);
        int itemWidthIncludeSpace = ShareData.PxToDpi_xhdpi(160) + ShareData.PxToDpi_xhdpi(20);
        mScrollListener = new SectionOnScrollListener(contentWidth, leftPadding, itemWidthIncludeSpace) {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!mIsVideoItemDragging) {
                    mTransitionListView.onScroll(dx);
                    changeAddBtn(recyclerView);
                }

            }

            @Override
            protected void onScrollProgressByUser(int index, float progress) {
                if (mCallback != null) {
                    mCallback.onScrolledByUser(index, progress);
                }
            }

            @Override
            protected void startScroll() {
                if (mCallback != null) {
                    mCallback.onStartScroll();
                }
            }

            @Override
            protected void stopScroll() {
                if (mCallback != null) {
                    mCallback.onStopScroll();
                }

            }

            @Override
            protected void onSectionSelected(int index) {
                if (!mIsVideoItemDragging) {
                    setSelectedVideo(index);
                }
            }
        };
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    private void showAddBtn(boolean show)
    {
        if(show && mShowAddBtn)
        {
            scaleAddBtn(mMinScale);
            mAddBtn.setVisibility(VISIBLE);
        }
        else
        {
            mAddBtn.setVisibility(INVISIBLE);
        }
    }

    private void scaleAddBtn(float scale)
    {
        mAddBtn.setScaleX(scale);
        mAddBtn.setScaleY(scale);
    }

    public boolean changeAddBtn(RecyclerView recyclerView)
    {
        boolean change = false;
        int itemCount = mVideoAdapter.getItemCount();
        int count = mRecyclerView.getChildCount();
        View view;
        for(int i = count - 1; i >= 0; i --)
        {
            view = mRecyclerView.getChildAt(i);
            if(view instanceof VideoCoverCell)
            {
                int index = mRecyclerView.getChildLayoutPosition(view);
                if(index == itemCount - 2)
                {
                    change = true;
                    float right = recyclerView.getX() + view.getX() + view.getWidth();
                    if(right >= mVideoListView.getWidth())
                    {
                        ((VideoCoverCell)view).hideAddIcon(true);
                        if(mVideoAdapter != null && mVideoAdapter.checkShowAdd())
                        {
                            showAddBtn(true);
                        }
                        else
                        {
                            showAddBtn(false);
                        }
                    }
                    else
                    {
                        if(mShowAddBtn)
                        {
                            ((VideoCoverCell)view).hideAddIcon(false);
                        }
                        showAddBtn(false);
                    }
                    if(right >= mVideoListView.getWidth())
                    {
                        if(right - mVideoListView.getWidth() <= mAddRect.width())
                        {
                            float scale = 1 - 0.2f * Math.abs(mVideoListView.getWidth() - right) / mAddRect.width();
                            scaleAddBtn(scale);
                        }
                    }
                }
                return change;
            }
        }
//        View view = recyclerView.findViewHolderForAdapterPosition(itemCount - 2);

        return change;
    }


    public void setVideoProgressLayoutCallback(VideoProgressLayoutCallback callback)  {
        this.mCallback = callback;
    }

    public void setPositionTransitionType(VideoGroupInfo videoGroupInfo) {
        mTransitionListView.replacePositionTransition(videoGroupInfo.mGroupIndex, videoGroupInfo.mTransitionDataInfo);
    }

    public void applyAllTransitionEffectToVideos(TransitionDataInfo transitionDataInfo) {
        mTransitionListView.applyAllTransitionEffectToVideos(transitionDataInfo);
    }

    public TransitionDataInfo getTransitionDataInfoByIndex(int index)
    {
        return mTransitionListView.getTransitionDataInfoByIndex(index);
    }

    public void refreshSelf(ArrayList<VideoInfo> videoInfoList) {
        if (mVideoAdapter != null) {
            mVideoList.clear();
            mVideoList.addAll(videoInfoList);

            mVideoAdapter.SetItemDatas(mVideoList);
            mVideoAdapter.notifyDataSetChanged();
            checkAddBtn();
            mScrollListener.mContentWidth = mVideoAdapter.getVisibleWidth();
        }
    }

    public void updateVideo(int index,VideoInfo videoInfo){
        mVideoList.set(index,videoInfo);
        if (mVideoAdapter != null) {
            mVideoAdapter.SetItemDatas(mVideoList);
            mVideoAdapter.notifyDataSetChanged();
            checkAddBtn();
            mScrollListener.mContentWidth = mVideoAdapter.getVisibleWidth();
        }
    }

    public void addVideo(VideoInfo videoInfo) {
        mVideoList.add(videoInfo);
        mVideoAdapter.SetItemDatas(mVideoList);
        mVideoAdapter.notifyDataSetChanged();
        mTransitionListView.insertPositionTransition(mVideoList.indexOf(videoInfo) - 1, new TransitionDataInfo());
        checkAddBtn();
        calculateTransitionLayout();
        mScrollListener.mContentWidth = mVideoAdapter.getVisibleWidth();
    }

    public void copyVideo(int videoIndex,VideoInfo dstInfo) {
        mVideoList.add(videoIndex + 1, dstInfo);
        mVideoAdapter.SetItemDatas(mVideoList);
        mTransitionListView.insertPositionTransition(mCurSelectedPosition, new TransitionDataInfo());
        mVideoAdapter.notifyDataSetChanged();
        mScrollListener.mContentWidth = mVideoAdapter.getVisibleWidth();
        checkAddBtn();
        calculateTransitionLayout();
    }

    public void deleteVideo(int videoIndex) {
        mTransitionListView.removePositionTransition(getRemoveTransitionItemIndex(videoIndex));
        mVideoList.remove(videoIndex);
        mVideoAdapter.SetItemDatas(mVideoList);
        mVideoAdapter.notifyDataSetChanged();
        mScrollListener.mContentWidth = mVideoAdapter.getVisibleWidth();
        checkAddBtn();
    }

    public void splitVideo(VideoInfo splitLeftOne, VideoInfo splitRightOne) {
        int originIndex = mVideoList.indexOf(splitLeftOne);
        if (originIndex != -1) {
            int splitedIndex = originIndex + 1;
            mVideoList.add(splitedIndex, splitRightOne);
            mVideoAdapter.SetItemDatas(mVideoList);
            mTransitionListView.insertPositionTransition(originIndex, new TransitionDataInfo());
            mVideoAdapter.notifyDataSetChanged();
            mScrollListener.mContentWidth = mVideoAdapter.getVisibleWidth();
            checkAddBtn();
            calculateTransitionLayout();
        }
    }

    public void changeVideo(int fromIndex, int toIndex) {
        if (fromIndex < mVideoList.size() && toIndex < mVideoList.size() && fromIndex != toIndex) {
            VideoInfo videoInfo = mVideoList.remove(fromIndex);
            mVideoList.add(toIndex, videoInfo);
        }
    }


    private int getRemoveTransitionItemIndex(int videoIndex) {
        if (videoIndex == 0) {
            return 0;
        } else if (videoIndex == mVideoList.size() - 1) {
            return videoIndex - 1;
        } else {
            return videoIndex;
        }
    }

    private void checkAddBtn()
    {
        if(!changeAddBtn(mRecyclerView))
        {
            if(mVideoAdapter != null && mVideoAdapter.checkShowAdd())
            {
                showAddBtn(true);
            }
            else
            {
                showAddBtn(false);
            }
        }
    }


    public void setSelectedVideo(int position) {
        if(mVideoAdapter != null)
        {
            List<AdapterDataInfo> lists = mVideoAdapter.getItemList();
            int index = 0;
            for(AdapterDataInfo info : lists)
            {
                if(index == position)
                {
                    info.mSelected = true;
                }
                else
                {
                    info.mSelected = false;
                }
                index ++;
            }

            int count = mRecyclerView.getChildCount();
            View view;
            for(int i = 0; i < count; i ++)
            {
                view = mRecyclerView.getChildAt(i);
                if(view instanceof VideoCoverCell)
                {
                    index = mRecyclerView.getChildLayoutPosition(view);
                    if(index == position)
                    {
                        ((VideoCoverCell)view).setSelectedEffect(true);
                    }
                    else
                    {
                        ((VideoCoverCell)view).setSelectedEffect(false);
                    }
                }
            }
            checkAddBtn();
        }
    }

    public void pauseScroll() {
        if(mVideoAdapter != null)
        {
            List<AdapterDataInfo> lists = mVideoAdapter.getItemList();
            for(AdapterDataInfo info : lists)
            {
                info.mSelected = false;
            }
            mVideoAdapter.notifyDataSetChanged();
            checkAddBtn();
        }
    }

    public void resumeScroll(boolean resetState) {
        mScrollListener.clear(true);
        if (resetState) {
            if(mVideoAdapter != null)
            {
                List<AdapterDataInfo> lists = mVideoAdapter.getItemList();
                for(AdapterDataInfo info : lists)
                {
                    info.mSelected = false;
                }
                mVideoAdapter.notifyDataSetChanged();
                checkAddBtn();
            }
        }
    }

    public void restart() {
        mRecyclerView.scrollBy(-mScrollListener.getLastScrollDistance(), 0);
        mScrollListener.clear(false);
        if(mVideoAdapter != null)
        {
            List<AdapterDataInfo> lists = mVideoAdapter.getItemList();
            for(AdapterDataInfo info : lists)
            {
                info.mSelected = false;
            }
            mVideoAdapter.notifyDataSetChanged();
            checkAddBtn();
        }
    }

    public int getSelectVideoIndex() {
        return mCurSelectedPosition;
    }

    private DragListView.DragListCallback m_dragControlCB = new DragListView.DragListCallback()
    {
        @Override
        public boolean canDragItemAtPosition(int dragPosition)
        {
            if (dragPosition == 0 || dragPosition == mVideoList.size() + 1) {
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean canDropItemAtPosition(int dropPosition) {
            if (dropPosition == 0 || dropPosition == mVideoList.size() + 1) {
                return false;
            } else {
                return true;
            }
        }
    };




    private boolean mIsVideoItemDragging;
    private DragListView.DragListListener mDragListListener = new DragListView.DragListListener() {
        @Override
        public void onItemDragStarted(int position) {
            mRecyclerView.setItemAnimator(mItemAnimator);
            mTransitionListView.setVisibility(INVISIBLE);
            mShowAddBtn = false;
            mVideoAdapter.showAddIcon(false);
            showAddBtn(false);

            position = position - 1;  //因为有头部
            if (mCallback != null && position >= 0) {
                mCallback.startDragVideo(position);
            }
        }

        @Override
        public void onItemDragging(int itemPosition, float x, float y) {
            mIsVideoItemDragging = true;
        }

        @Override
        public void onItemDragEnded(int fromPosition, final int toPosition) {
            mRecyclerView.setItemAnimator(null);
            mIsVideoItemDragging = false;
            mShowAddBtn = true;
            mVideoAdapter.showAddIcon(true);
            checkAddBtn();
            if (mCallback != null) {
                int realFromPos = fromPosition - 1;
                int realToPos = toPosition - 1;
                if(realFromPos < 0)
                {
                    realFromPos = 0;
                }
                if(realToPos < 0)
                {
                    realToPos = 0;
                }

				calculateTransitionLayout();
				mTransitionListView.setVisibility(VISIBLE);
				float[] offsetX = new float[1];
				int index = getSelectIndex(offsetX);
				setSelectedVideo(index);

				float finalProgress;
				index --;
                finalProgress = (offsetX[0] - ShareData.PxToDpi_xhdpi(10)) / ShareData.PxToDpi_xhdpi(180);

				changeVideo(realFromPos, realToPos);
				mCallback.changeVideoOrder(realFromPos, realToPos, index, finalProgress);
				mTransitionListView.changeTransitionPosition(realFromPos, realToPos);
			}
        }
    };

    private int mOriginX = -1;
    private int mOriginY = -1;
    private int mRecycleViewY;
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            VideoProgressLayout.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            int viewH = getHeight();
            FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP);
            fl.topMargin = (viewH - ShareData.PxToDpi_xhdpi(160)) / 2;
            mRecyclerView.setLayoutParams(fl);
            mRecycleViewY = fl.topMargin;

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                           mTransitionListView.getBackgroundRectHeight(), Gravity.TOP);
            params.topMargin = (int)(mVideoListView.getY() + mRecycleViewY + mRecyclerView.getPaddingTop() +
                    mTransitionListView.getBackgroundRectHeight() / 2);
            mTransitionListView.setLayoutParams(params);

            calculateTransitionLayout();

            params = (FrameLayout.LayoutParams)mAddBtn.getLayoutParams();
            params.gravity = Gravity.TOP | Gravity.RIGHT;
            params.topMargin = (int)(mVideoListView.getY() + mRecycleViewY + mRecyclerView.getPaddingTop() + ShareData.PxToDpi_xhdpi(41));
            mAddBtn.setLayoutParams(params);
            mAddRect.offset((int)(mVideoListView.getX() + mVideoListView.getWidth() - mAddBtn.getWidth()), params.topMargin);
            checkAddBtn();
        }
    };

    protected int getSelectIndex(float[] offsetX)
    {
        int index = 0;
        int count = mRecyclerView.getChildCount();
        View view;
        for(int i = 0; i < count; i ++)
        {
            view = mRecyclerView.getChildAt(i);
            if(view instanceof VideoCoverCell)
            {
                index = mRecyclerView.getChildLayoutPosition(view);

                int width = mRecyclerView.getWidth();
                float itemX = view.getX();

                int k = (int)Math.floor((width / 2 - itemX) / ShareData.PxToDpi_xhdpi(180));
                offsetX[0] = width / 2 - (itemX + k * ShareData.PxToDpi_xhdpi(180));
                return index + k;
            }
        }
        return index;
    }

    public void calculateTransitionLayout() {

        mVideoAdapter.notifyDataSetChanged();
        int count = mRecyclerView.getChildCount();
        View view;
        for(int i = 0; i < count; i ++)
        {
            view = mRecyclerView.getChildAt(i);
            if(view instanceof VideoCoverCell)
            {
                int index = mRecyclerView.getChildLayoutPosition(view);

                float itemX = view.getX();
                int leftPosition = (int)(mVideoListView.getX() + mRecyclerView.getX() +
                        itemX + ShareData.PxToDpi_xhdpi(180) - mTransitionListView.getBackgroundRectWidth() / 2);
                mOriginX = mOriginX == -1 ? leftPosition : mOriginX;

                /*int topPosition = (int)(mVideoListView.getY() + mRecycleViewY +
                        ((ShareData.PxToDpi_xhdpi(160) - mTransitionListView.getBackgroundRectHeight()) / 2));*/
                int topPosition = 0;
                        mOriginY = mOriginY == -1 ? topPosition : mOriginY;
                mTransitionListView.setCoordinateByIndex(index, leftPosition, 0,
                                                         (ShareData.PxToDpi_xhdpi(180) - mTransitionListView.getBackgroundRectWidth()));
                return;
            }
        }
    }

    public GLVideoView.OnItemProgressListener mVideoItemProgressListener = new GLVideoView.OnItemProgressListener() {
        @Override
        public void onItemChanged(int index, float progress, boolean isSeekTo) {
            if (!isSeekTo) {
                scrollToVideoByProgress(index, progress);
            }
        }
    };

    private int mLastScrollDistance;
    private void scrollToVideoByProgress(int index, float progress) {
        int videoItemWidth = ShareData.PxToDpi_xhdpi(160) + mVideoAdapter.mNormalPad * 2;
        int scrollDistance = (int)((index * videoItemWidth) + (progress * videoItemWidth));
//        System.out.println("progress: " + progress + "index: " + index);
        mLastScrollDistance = mScrollListener.getLastScrollDistance();
        int moveDistance = scrollDistance - mLastScrollDistance;
        mRecyclerView.scrollBy(moveDistance, 0);
    }


    public void clear() {
        if (mVideoListView != null) {
            mVideoListView.Clear();
        }
    }

}
