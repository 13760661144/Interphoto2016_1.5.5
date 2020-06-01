package cn.poco.video.videoFeature;

import android.support.v7.widget.RecyclerView;

import cn.poco.tianutils.ShareData;

/**
 * Created by Simon Meng on 2018/1/18.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public abstract class SectionOnScrollListener extends RecyclerView.OnScrollListener{
    private int mLeftPadding;
    private int mTotalOffset;

    public int mContentWidth;
    private int mCurrentIndex = -1;
    private int mItemWidthIncludeSpace;
    public SectionOnScrollListener(int contentWidth , int leftPadding, int itemWidthIncludeSpace) {
        this.mContentWidth = contentWidth;
        this.mLeftPadding = leftPadding;
        this.mItemWidthIncludeSpace = itemWidthIncludeSpace;
    }

    private boolean mScrollByUser;
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        int videoCount = recyclerView.getAdapter().getItemCount() - 2;
        mTotalOffset += (dx);
        mTotalOffset = mTotalOffset < 0 ? 0 :mTotalOffset;
        if (mTotalOffset >= mLeftPadding) {
            int oldIndex = (mTotalOffset - mLeftPadding) / (mItemWidthIncludeSpace);
            if (mTotalOffset <= mLeftPadding + mContentWidth && oldIndex != mCurrentIndex) {
                mCurrentIndex = oldIndex;
                mCurrentIndex = mCurrentIndex < 0 ? 0 : mCurrentIndex >= videoCount - 1 ? videoCount - 1 : mCurrentIndex;
                onSectionSelected(mCurrentIndex + 1);
            }

            int count = recyclerView.getAdapter().getItemCount();
            int itemWidth = 0;
            if (mCurrentIndex == count - 3) {
                itemWidth = ShareData.PxToDpi_xhdpi(160);
            } else {
                itemWidth = ShareData.PxToDpi_xhdpi(180);
            }

            float progress = (mTotalOffset - mCurrentIndex * ShareData.PxToDpi_xhdpi(180)) / (itemWidth * 1.0f);
            progress = progress < 0 ? 0 : progress;
            progress = progress > 1 ? 1 : progress;
            if (mScrollByUser) {
                onScrollProgressByUser(mCurrentIndex, progress);
            }
        }
    }

    private int mLastState = -1;
    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (mLastState == -1 && newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            mScrollByUser = true;
            startScroll();
            mLastState = newState;
        } else if (mLastState == RecyclerView.SCROLL_STATE_DRAGGING && newState == RecyclerView.SCROLL_STATE_IDLE) {
            mScrollByUser = false;
            stopScroll();
            mLastState = -1;
        }
    }


    public int getLastScrollDistance() {
        return mTotalOffset;
    }


    public void clear(boolean keepPosition) {
        mCurrentIndex = -1;
        mScrollByUser = false;
        if (!keepPosition) {
            mTotalOffset = 0;
        }
    }


    protected abstract void startScroll();

    protected abstract void onSectionSelected(int index);

//    protected abstract void onScrollProgressByUser(float progress);
    protected abstract void onScrollProgressByUser(int index, float progress);

    protected abstract void stopScroll();
}
