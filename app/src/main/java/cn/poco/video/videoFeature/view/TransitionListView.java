package cn.poco.video.videoFeature.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.poco.tianutils.ShareData;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.sequenceMosaics.TransitionDataInfo;

/**
 * Created by Simon Meng on 2018/1/9.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class TransitionListView extends View
{
    public interface TransitionViewCallback
    {
        void onClickItem(int index, TransitionDataInfo transitionDataInfo);
        boolean canAddTransition(int index);
    }

    private List<TransitionDataInfo> mInfoList = new ArrayList<>();
    private List<ClickInfo> mClickInfoList = new ArrayList<>();
    private boolean mNeedCalculateClickRect = true;
    private int mCount;
    private TransitionViewCallback mCallback;


    public TransitionListView(Context context, int count)
    {
        super(context);
        mCount = count;
        initDefaultData();
        initPaint();
    }

    private void initDefaultData()
    {
        for(int i = 0; i < mCount; i++)
        {
            TransitionDataInfo info = new TransitionDataInfo();
            mInfoList.add(info);
        }
    }

    private Paint mPaint, mBackgroundPaint, mBorderPaint;

    private void initPaint()
    {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(Color.WHITE);
        mBackgroundPaint.setDither(true);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(0xffffc433);
        mBorderPaint.setDither(true);
        mBorderPaint.setStrokeWidth(ShareData.PxToDpi_xhdpi(3));
    }


    public int mLeftMostPosition;
    private int mItemDiff, mTopOffset;

    public void setPositionData(int leftMostPosition, int topOffset, int itemDiff)
    {
        mLeftMostPosition = leftMostPosition;
        mTopOffset = topOffset;
        mItemDiff = itemDiff;
        invalidate();
    }

    public void setCoordinateByIndex(int index, float x, float y, int distance)
    {
        mClickInfoList.clear();
        ClickInfo clickInfo;
        if(mInfoList != null)
        {
            int index1 = 0;
            for(TransitionDataInfo info : mInfoList)
            {
                clickInfo = new ClickInfo();
                clickInfo.mTrnasitionInfo = info;
                clickInfo.mIndex = index1;
                float left = x - (index - index1 - 1) * (getBackgroundRectWidth() + distance);
                float top = y;
                float bottom = top + getBackgroundRectHeight();
                float right = left + getBackgroundRectWidth();
                clickInfo.mRectF.set(left, top, right, bottom);
                mClickInfoList.add(clickInfo);
                if(index1 == 0)
                {
                    mLeftMostPosition = (int)(left);
                    mTopOffset = (int)top;
                    mItemDiff = ShareData.PxToDpi_xhdpi(180);
                }
                index1 ++;
            }
        }
        mNeedCalculateClickRect = false;

        invalidate();
    }


    public void onScroll(int dx)
    {
        mLeftMostPosition += (-dx);
        for(ClickInfo clickInfo : mClickInfoList)
        {
            clickInfo.mRectF.offset(-dx, 0);
        }
        invalidate();
    }


    public void insertPositionTransition(int position, TransitionDataInfo transitionDataInfo)
    {
        if(position <= 0)
        {
            position = 0;
        }
        if(position >= mInfoList.size())
        {
            position = mInfoList.size();
        }
        mInfoList.add(position, transitionDataInfo);
        mNeedCalculateClickRect = true;
        mClickInfoList.clear();
    }

    public void replacePositionTransition(int position, TransitionDataInfo transitionDataInfo)
    {
        TransitionDataInfo info = mInfoList.get(position);
        if(info != null)
        {
            mInfoList.set(position, transitionDataInfo);
            mNeedCalculateClickRect = true;
            mClickInfoList.clear();
            invalidate();
        }
    }

    public void removePositionTransition(int position)
    {
        if(position < mInfoList.size())
        {
            mInfoList.remove(position);
            mNeedCalculateClickRect = true;
            mClickInfoList.clear();
            invalidate();
        }
    }

    public void changeTransitionPosition(int fromPos, int toPos)
    {
        if(mInfoList != null && fromPos != toPos)
        {
            int size = mInfoList.size();
            if(toPos >= size && fromPos < size)
            {
                TransitionDataInfo transitionDataInfo = new TransitionDataInfo();
                mInfoList.remove(fromPos);
                mInfoList.add(transitionDataInfo);
            }
            else if(toPos < size && fromPos >= size)
            {
                TransitionDataInfo transitionDataInfo = new TransitionDataInfo();
                mInfoList.set(toPos, transitionDataInfo);
            }
            else if(toPos < size && fromPos < size)
            {
                TransitionDataInfo info = mInfoList.remove(fromPos);
                mInfoList.add(toPos, info);
            }
        }
    }

    public void applyAllTransitionEffectToVideos(TransitionDataInfo transitionDataInfo) {
        int size = mInfoList.size();
        TransitionDataInfo item;
        for(int i = 0; i < size; i ++)
        {
            if(transitionDataInfo.mID == TransitionItem.ALPHA || transitionDataInfo.mID == TransitionItem.BLUR)
            {
                if(mCallback != null && mCallback.canAddTransition(i))
                {
                    item = new TransitionDataInfo(transitionDataInfo.mID);
                    mInfoList.set(i, item);
                }
            }
            else
            {
                item = new TransitionDataInfo(transitionDataInfo.mID);
                mInfoList.set(i, item);
            }
        }
        invalidate();
    }

    public TransitionDataInfo getTransitionDataInfoByIndex(int index)
    {
        if(mInfoList != null && index < mInfoList.size())
        {
           return mInfoList.get(index);
        }
        return null;
    }

    public void setCallback(TransitionViewCallback callback) {
        this.mCallback = callback;
    }


    private int mWidth;
    private int mHeight;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w !=0 && h != 0) {
            mWidth = w;
            mHeight = h;
        }
    }

    private float mDownX;
    private float mDownY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        boolean flag = super.onTouchEvent(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN : {
                mDownX = x;
                mDownY = y;
                mClickRectIndex = rectCheck(x, y);
                if(mClickRectIndex >= 0)
                {
                    flag = true;
                }
            }
            case MotionEvent.ACTION_MOVE:
            {
                if(mClickRectIndex != -1)
                {
                    if(Math.abs(x - mDownX) > 30 || Math.abs(y - mDownY) > 30)
                    {
                        mClickRectIndex = -1;
                    }
                }
                if(mClickRectIndex >= 0)
                {
                    flag = true;
                }
                break;
            }

            case MotionEvent.ACTION_UP : {
                if (mCallback != null && mClickRectIndex != -1) {
                    if(mClickInfoList.get(mClickRectIndex).mTrnasitionInfo== null){
                        mClickInfoList.get(mClickRectIndex).mTrnasitionInfo = new TransitionDataInfo();
                    }
                    mCallback.onClickItem(mClickRectIndex, mClickInfoList.get(mClickRectIndex).mTrnasitionInfo);
                    mClickRectIndex = -1;
                    flag = true;
                }
            }
        }
        return flag;
    }

    private int mClickRectIndex;
    private int rectCheck(float x, float y) {
        int isInRect = -1;
        int index = 0;
        for (ClickInfo info : mClickInfoList) {
            if (info.mRectF.contains(x, y)) {
                isInRect = index;
                return isInRect;
//                isInRect = mClickInfoList.indexOf(info);
            }
            index ++;
        }
        return isInRect;
    }


    private RectF mImageBackgroundRect;
    @Override
    protected void onDraw(Canvas canvas) {
        int left = mLeftMostPosition;
        int top = mTopOffset;
        for (TransitionDataInfo item : mInfoList) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), item.mResDefault);
            int itemWidth = ShareData.PxToDpi_xhdpi(60);
            int itemHeight = ShareData.PxToDpi_xhdpi(80);
            int rectLeft = left;
            int rectTop = top;
            int rectRight = rectLeft + itemWidth;
            int rectBottom = rectTop + itemHeight;
            mImageBackgroundRect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
            // 填充
            canvas.drawRect(mImageBackgroundRect, mBackgroundPaint);
//            // 描边
//            canvas.drawRect(mImageBackgroundRect, mBorderPaint);

            // 画图
            int bitmapLeftPosition = left + (itemWidth - bitmap.getWidth()) / 2;
            int bitmapTopPosition = top + (itemHeight - bitmap.getHeight()) / 2;
            canvas.drawBitmap(bitmap, bitmapLeftPosition, bitmapTopPosition, mPaint);

            if (mNeedCalculateClickRect) {
                ClickInfo clickInfo = new ClickInfo();
                clickInfo.mRectF.left = mImageBackgroundRect.left;
                clickInfo.mRectF.top = mImageBackgroundRect.top;
                clickInfo.mRectF.right = mImageBackgroundRect.right;
                clickInfo.mRectF.bottom = mImageBackgroundRect.bottom;
                clickInfo.mIndex = mInfoList.indexOf(item);
                mClickInfoList.add(clickInfo);
            }
            left += mItemDiff;

            int index = mInfoList.indexOf(item);
            if (mClickInfoList.get(index) != null) {
                mClickInfoList.get(index).mTrnasitionInfo = item;
            }
        }
        mNeedCalculateClickRect = false;
    }

    public int getBackgroundRectWidth() {
        return ShareData.PxToDpi_xhdpi(60);
    }

    public int getBackgroundRectHeight() {
        return ShareData.PxToDpi_xhdpi(80);
    }


    private static class ClickInfo {
        private TransitionDataInfo mTrnasitionInfo;
        private RectF mRectF = new RectF();
        private int mIndex;
    }


}
