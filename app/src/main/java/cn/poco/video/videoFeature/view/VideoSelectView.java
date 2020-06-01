package cn.poco.video.videoFeature.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import cn.poco.tianutils.ShareData;


/**
 * Created by Simon Meng on 2017/10/26.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoSelectView extends View {
    public interface ProgressChangeListener {
        void onLeftProgressChange(float leftProgress, float rightProgress);
        void onRightProgressChange(float leftProgress, float rightProgress);
        void onConfirmProgress();
        void onSizeChange();
        boolean canMoveLeft();
        boolean canMoveRight();
    }

    private static final String TAG = "ProgressSelectView";
    private Paint mLinePaint;
    private Paint mSelectedPaint;
    private Paint mUnselectedPaint;
    private float mLeftProgress, mRightProgress = 1.0f;
    private boolean mPressedLeft, mPressedRight;
    private int mWidth; // 控件的宽度
    private int mHeight; // 控件的高度

    // UI相关数据
    public final static int SELECT_RECT_W = ShareData.PxToDpi_xhdpi(40);
    private int mSelectRectWidth;
    private int mSelectRectHeight;
    private int mBitmapListHeight;
    private int mShortLineHeight;
    private int mLongLineHeight;
    private int mLeftSideTouchPadding;
    private int mRightSideTouchPadding;
    private int mBitmapListWidth;

    private ProgressChangeListener mCallback;

    private ClipTimeLineView mParentView;

    // 最近的左边拖动游标位置， 最近的右边拖动游标位置
    private float mLastStartX = -1, mLastEndX = -1;


    // 最初的左边拖动游标
    private float mInitialStartX;
    // 最初的右边拖动游标
    private float mInitialEndX;

    // 两个拖动游标之间最小的距离
    private int mMinimalClipDistance;

    private float mPressedDistance;
    // 显示图片的recyclerview被滑动的距离
    private float mTotalOffset;



    public VideoSelectView(ClipTimeLineView parent) {
        super(parent.getContext());
        mParentView = parent;
        initData();
    }

    /**
     * 初始化相关数据
     */
    private void initData() {
        mSelectRectWidth = SELECT_RECT_W;
        mSelectRectHeight = ShareData.PxToDpi_xhdpi(5);

        mShortLineHeight = ShareData.PxToDpi_xhdpi(20);
        mLongLineHeight = ShareData.PxToDpi_xhdpi(30);

        mLeftSideTouchPadding = ShareData.PxToDpi_xhdpi(20);
        mRightSideTouchPadding = ShareData.PxToDpi_xhdpi(20);
        mBitmapListHeight = ShareData.PxToDpi_xhdpi(162);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStyle(Paint.Style.FILL);

        mSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectedPaint.setColor(0xfff7f8f9);

        mUnselectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnselectedPaint.setColor(0x7f000000);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mBitmapListHeight + mSelectRectHeight * 2);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        if (mLastStartX == -1) {
            mLastStartX = 0;
        }
        if (mLastEndX == -1) {
            mLastEndX = mWidth;
        }
        mMinimalClipDistance = mParentView.mMinimalRect;
        mBitmapListWidth = mParentView.mBitmapListVisibleWidth;

        mRectF.left = mLastStartX;
        mRectF.top = 0;
        mRectF.right = mLastEndX;
        mRectF.bottom = mHeight;

        if (mCallback != null) {
            mCallback.onSizeChange();
        }
    }

    public void refreshData() {
        mMinimalClipDistance = mParentView.mMinimalRect;
        mBitmapListWidth = mParentView.mBitmapListVisibleWidth;
        mRectF.left = mLastStartX;
        mRectF.top = 0;
        mRectF.right = mLastEndX;
        mRectF.bottom = mHeight;
    }

    private static final int OFFSET_TO_SCROLL = ShareData.PxToDpi_xhdpi(120);
    private static final int SCROLL_DISTANCE = ShareData.PxToDpi_xhdpi(30);

    private float mLastTouchX;
    private boolean mIsDragBySpace;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        if (action == MotionEvent.ACTION_DOWN) {
            if (mLastStartX -  mLeftSideTouchPadding <= x && x <= mLastStartX + mSelectRectWidth && y >= 0 && y <= mHeight) {
                mPressedLeft = true;
                mPressedDistance = (x - mLastStartX);
                ViewParent viewParent = this.getParent();
                viewParent.requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            } else if (mLastEndX - mSelectRectWidth <= x && x <= mLastEndX + mRightSideTouchPadding && y >= 0 && y <= mHeight) {
                mPressedRight = true;
                mPressedDistance = x - mLastEndX;
                ViewParent viewParent = this.getParent();
                viewParent.requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            } else if (mLastStartX + mSelectRectWidth < x && x < mLastEndX - mSelectRectWidth){
                mIsDragBySpace = true;
                mLastTouchX = x;
                return true;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mPressedLeft && mLeftHandleCanMove) {
                float oldLastStart = mLastStartX;
                float lastStartX = x - mPressedDistance;

                // true为向左滑动，false为向右滑动
                boolean leftHandleDirection = lastStartX - oldLastStart < 0 ? true : false;

                if(leftHandleDirection)
                {
                    if(mCallback != null && mCallback.canMoveLeft())
                    {
                        moveToLeft(x);

                        if(mLastStartX <= OFFSET_TO_SCROLL && mParentView.mBitmapListView.canScrollHorizontally(-1)) {
                            mParentView.mBitmapListView.scrollBy(-SCROLL_DISTANCE, 0);
                        }
                    }
                }
                else
                {
                    moveToLeft(x);
                    if (mLastStartX >= mWidth - OFFSET_TO_SCROLL && mParentView.mBitmapListView.canScrollHorizontally(1)) {
                        mParentView.mBitmapListView.scrollBy(SCROLL_DISTANCE, 0);
                    }
                }
                return true;

            } else if (mPressedRight && mRightHandleCanMove) {
                float oldLastEndX = mLastEndX;
                float laseEndX = x - mPressedDistance;

                // false为向左拉动，true为向右拉动
                boolean rightHandleDirection = laseEndX - oldLastEndX > 0;

                if(rightHandleDirection)
                {
                    if(mCallback != null && mCallback.canMoveRight())
                    {
                        moveToRight(x);

                        if(mLastEndX >= mWidth - OFFSET_TO_SCROLL && mParentView.mBitmapListView.canScrollHorizontally(1)) {
                            mParentView.mBitmapListView.scrollBy(SCROLL_DISTANCE, 0);
                        }
                    }
                }
                else
                {
                    moveToRight(x);
                    if (mLastEndX <= OFFSET_TO_SCROLL && mParentView.mBitmapListView.canScrollHorizontally(-1)) {
                        mParentView.mBitmapListView.scrollBy(-SCROLL_DISTANCE, 0);
                    }
                }

                return true;
            } else if(mIsDragBySpace){
                float touchX = event.getX();
                float touchDiffer = touchX - mLastTouchX;
                float handleWidth = mLastEndX - mLastStartX;
                mLastTouchX = touchX;
                mLastStartX += touchDiffer;
                mLastEndX += touchDiffer;

                mLastStartX = mLastStartX <= 0 ? 0 : mLastStartX > mWidth - handleWidth ? mWidth - handleWidth : mLastStartX;
                mLastEndX = mLastEndX >= mWidth ? mWidth : mLastEndX < handleWidth ? handleWidth : mLastEndX;

                mLeftProgress = (mLastStartX + mTotalOffset - mInitialStartX) / (mBitmapListWidth);
                mLeftProgress = (mLeftProgress < 0 ? 0 : mLeftProgress > 1 ? 1 : mLeftProgress);
                if (mCallback != null) {
                    mCallback.onLeftProgressChange(mLeftProgress, mRightProgress);
                }
                mRightProgress = (mLastEndX - mSelectRectWidth * 2 + mTotalOffset - mInitialStartX) / (mBitmapListWidth);
                mRightProgress = mRightProgress < 0 ? 0 : mRightProgress > 1 ? 1 : mRightProgress;

                mParentView.mProcessView.setVisibility(View.GONE);
                invalidate();
            }
        } else if (action == MotionEvent.ACTION_UP) {
            mParentView.mProcessView.setVisibility(View.VISIBLE);
            mParentView.mProcessView.setRang(getLeftProgress(),getRightProgress());
            if (mPressedLeft) {
                mPressedLeft = false;
                if (mCallback != null) {
                    mCallback.onConfirmProgress();
                }
                return true;
            } else if (mPressedRight)
            {
                mPressedRight = false;

                if (mCallback != null)
                {
                    mCallback.onConfirmProgress();
                }
                return true;
            }else if(mIsDragBySpace){
                if (mCallback != null)
                {
                    mCallback.onConfirmProgress();
                }
                return true;
            }
        }
        return false;
    }

    private void moveToRight(float x)
    {
        mLastEndX = x - mPressedDistance;
        float minimalLimit = mLastStartX + mSelectRectWidth * 2 + mMinimalClipDistance;
        float maxLimit = mLastEndX > mWidth ? mWidth : mLastEndX;
        if (mLastEndX < minimalLimit) {
            mLastEndX = minimalLimit;
        } else if (mLastEndX > maxLimit) {
            mLastEndX = maxLimit;
        }

        mRightProgress = (mLastEndX - mSelectRectWidth * 2 + mTotalOffset - mInitialStartX) / (mBitmapListWidth);
        mRightProgress = mRightProgress < 0 ? 0 : mRightProgress > 1 ? 1 : mRightProgress;

        if (mCallback != null) {
            mCallback.onRightProgressChange(mLeftProgress, mRightProgress);
        }
        mParentView.mProcessView.setVisibility(View.GONE);
        invalidate();
    }

    private void moveToLeft(float x)
    {
        mLastStartX = x - mPressedDistance;

        float maxPosition = mLastEndX - mMinimalClipDistance - mSelectRectWidth * 2;
        float minimalPosition = mInitialStartX;
        if (mLastStartX < minimalPosition) {
            mLastStartX = minimalPosition;
        } else if (mLastStartX > maxPosition) {
            mLastStartX = maxPosition;
        }

        mLeftProgress = (mLastStartX + mTotalOffset - mInitialStartX) / (mBitmapListWidth);
        mLeftProgress = (mLeftProgress < 0 ? 0 : mLeftProgress > 1 ? 1 : mLeftProgress);

        if (mCallback != null) {
            mCallback.onLeftProgressChange(mLeftProgress, mRightProgress);
        }
        mParentView.mProcessView.setVisibility(View.GONE);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 画出代表选择的矩形的左边;
        canvas.drawRect(mLastStartX , 0, mLastStartX + mSelectRectWidth, mHeight, mSelectedPaint);
        // 画出代表选择的矩形的右边;
        canvas.drawRect(mLastEndX - mSelectRectWidth, 0, mLastEndX, mHeight, mSelectedPaint);
        mRectF.left = mLastStartX;
        mRectF.top = 0;
        mRectF.right = mLastEndX;
        mRectF.bottom = canvas.getHeight();

        int rightPositionOfRect = (int) ((mLastEndX - mSelectRectWidth) + 0.5f);
        // 画出代表选择的矩形的上边
        canvas.drawRect(mLastStartX + mSelectRectWidth, 0, rightPositionOfRect, mSelectRectHeight, mSelectedPaint);
        // 画出代表选择的矩形的下边
        canvas.drawRect(mLastStartX + mSelectRectWidth, mBitmapListHeight + mSelectRectHeight, rightPositionOfRect, mBitmapListHeight + 2 * mSelectRectHeight, mSelectedPaint);

        int lineWidth = ShareData.PxToDpi_xhdpi(4);

        int shortLineTop = (mHeight - mShortLineHeight) / 2;
        int shortLineLeft = (int)(mLastStartX + ShareData.PxToDpi_xhdpi(14));
        int shortLineHeight = ShareData.PxToDpi_xhdpi(20);

        int longLineTop = (mHeight - mLongLineHeight) / 2;
        int longLineLeft = (int)(mLastStartX + ShareData.PxToDpi_xhdpi(21));
        int longLineHeight = ShareData.PxToDpi_xhdpi(30);

        // 画出左边的拖动游标
        if (!mLeftHandleCanMove) {
            mLinePaint.setColor(Color.GRAY);
        } else {
            mLinePaint.setColor(Color.BLACK);
        }

        canvas.drawRect(shortLineLeft, shortLineTop, shortLineLeft + lineWidth, shortLineTop + shortLineHeight, mLinePaint);
        canvas.drawRect(longLineLeft, longLineTop, longLineLeft + lineWidth, longLineTop + longLineHeight, mLinePaint);


        shortLineTop = (mHeight - mShortLineHeight) / 2;
        int shortLineRight = (int)(mLastEndX - ShareData.PxToDpi_xhdpi(14));
        shortLineHeight = ShareData.PxToDpi_xhdpi(20);

        longLineTop = (mHeight - mLongLineHeight) / 2;
        int longlineRight = (int)(mLastEndX - ShareData.PxToDpi_xhdpi(21));
        longLineHeight = ShareData.PxToDpi_xhdpi(30);

        // 画出右边的拖动游标
        if (!mRightHandleCanMove) {
            mLinePaint.setColor(Color.GRAY);
        } else {
            mLinePaint.setColor(Color.BLACK);
        }

        canvas.drawRect(shortLineRight - lineWidth, shortLineTop, shortLineRight, shortLineTop + shortLineHeight, mLinePaint);
        canvas.drawRect(longlineRight - lineWidth, longLineTop, longlineRight, longLineTop + longLineHeight, mLinePaint);

        // 分情况画出左边代表没有选中的矩形
//        canvas.drawRect(mSelectRectWidth, mSelectRectHeight, mLastStartX, mSelectRectHeight + mBitmapListHeight, mUnselectedPaint);
        canvas.drawRect(0, mSelectRectHeight, mLastStartX, mSelectRectHeight + mBitmapListHeight, mUnselectedPaint);
        // 画出右边代表没有选中的矩形
//        canvas.drawRect(mLastEndX, mSelectRectHeight, mWidth - mSelectRectWidth, mSelectRectHeight + mBitmapListHeight, mUnselectedPaint);
        canvas.drawRect(mLastEndX, mSelectRectHeight, mWidth, mSelectRectHeight + mBitmapListHeight, mUnselectedPaint);
    }

    public void setLeftHandlePosition(float startX, float leftProgress) {
        this.mLastStartX = startX;
        setLeftProgress(leftProgress);
    }

    public void setRightHandlePosition(float endX, float rightProgress) {
        this.mLastEndX = endX;
        setRightProgress(rightProgress);
    }

    /**
     * 设置右边的进度,范围为0-1
     * @param rightProgress 右边的进度
     */
    public void setRightProgress(float rightProgress) {
        assertProgressRange(rightProgress);
        this.mRightProgress = rightProgress;
    }

    private boolean mRightHandleCanMove = true;
    public void setRightHandle(boolean canMove) {
        this.mRightHandleCanMove = canMove;
    }

    /**
     * 获取右边的进度,范围为1-0
     * @return 右边的进度
     */
    public float getRightProgress() {
        return mRightProgress;
    }

    public float getRightHandleRightPosition() {
        return mLastEndX;
    }

    /**
     * 设置左边的进度，范围为0-1
     * @param leftProgress 左边的进度
     */
    public void setLeftProgress(float leftProgress) {
        assertProgressRange(leftProgress);
        this.mLeftProgress = leftProgress;
    }

    private boolean mLeftHandleCanMove = true;
    public void setLeftHandle(boolean canMove) {
        this.mLeftHandleCanMove = canMove;
    }

    /**
     * 获取左边的进度，范围为0-1
     * @return 左边的进度
     */
    public float getLeftProgress() {
        return mLeftProgress;
    }

    public float getLeftHandleLeftPosition() {
        return mLastStartX;
    }


    private void assertProgressRange(float progress) {
        if (progress < 0) {
            Log.i(TAG,"the progress can not less than zero");
        }

        if (progress > 1) {
            Log.i(TAG, "the progress can not large than one");
        }
    }


    /**
     * 获取整个控件的宽度
     * @return 控件的宽度
     */
    public int getViewWidth() {
        return (int)(mLastEndX - mLastStartX);
    }

    /**
     * 获取代表选中区域的宽度
     * @return 选中区域的宽度
     */
    public int getSelectZoneWidth() {
        int viewWidth = getViewWidth();
        int handle = mSelectRectWidth * 2;
        return viewWidth - handle;
    }


    public void setTimeLineCallback(ProgressChangeListener callback) {
        this.mCallback = callback;
    }



    private RectF mRectF = new RectF();
    public RectF getSelectRectF() {
        return mRectF;
    }


    /**
     * 重置所有数据到默认状态;
     */
    public void reset() {
        mTotalOffset = 0;
        mLeftProgress = 0;
        mRightProgress = 1.0f;
        mTotalOffset = 0;
        mPressedLeft = false;
        mPressedRight = false;
        mLastStartX = 0;
        mLastEndX = 0;
        mInitialStartX = 0;
        mInitialEndX = 0;
        mPressedDistance = 0;
        mLeftHandleCanMove = true;
        mRightHandleCanMove = true;
    }
}
