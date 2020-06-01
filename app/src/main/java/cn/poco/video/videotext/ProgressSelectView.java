package cn.poco.video.videotext;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import cn.poco.tianutils.ShareData;

/**
 * Created by Simon Meng on 2017/10/26.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class ProgressSelectView extends View {
    public interface ProgressChangeListener {
        void onProgressChange(float leftProgress, float rightProgress);
        void onProgressConfirm(float leftProgress, float rightProgress);
        void onDragHandlePositionChange(float leftPosition, float rightPosition);
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
    private int mSelectRectWidth;
    private int mSelectRectHeight;
    private int mBitmapListHeight;
    private int mLineTotalHeight;
    private int mLeftSideTouchPadding;
    private int mRightSideTouchPadding;
    private int mBitmapListWidth;

    private ProgressChangeListener mCallback;

    private WatermarkTimeLineView mParentView;

    // 最近的左边拖动游标位置， 最近的右边拖动游标位置
    private float mLastStartX, mLastEndX;


    // 最初的左边拖动游标
    private float mInitialStartX;
    // 最初的右边拖动游标
    private float mInitialEndX;

    // 两个拖动游标之间最小的距离
    private int mMinimalClipDistance;

    private float mPressedDistance;
    // 显示图片的recyclerview被滑动的距离
    private float mTotalOffset;

//    protected boolean mScrollByUser = true;


    public ProgressSelectView(WatermarkTimeLineView parent) {
        super(parent.getContext());
        mParentView = parent;
        initData();
        // 监听滑动
        mParentView.mBitmapListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dx != 0) {
                    mTotalOffset += (dx);
                        // 当剪辑类型是freeStyle，并且宽度大于屏幕时
                        if (mParentView.mNeedSplitScreen) {
                            // 滑动的时候也可以改变左右游标最近拖动的位置
                            mLastEndX += (-dx);
                            mLastStartX += (-dx);

                            float minimalLimit = mLastStartX + mSelectRectWidth * 2 + mMinimalClipDistance;
                            if (mLastEndX < minimalLimit) {
                                mLastEndX = minimalLimit;
                            }

                            if (mCallback != null) {
                                mCallback.onDragHandlePositionChange(mLastStartX, mLastEndX);
                            }
                            invalidate();
                        }
                }
            }

//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                if (newState == RecyclerView.SCROLL_STATE_IDLE && !mScrollByUser) {
//                    mScrollByUser = true;
//                }
//            }
        });
    }

    /**
     * 初始化相关数据
     */
    private void initData() {
        mSelectRectWidth = ShareData.PxToDpi_xhdpi(30);
        mSelectRectHeight = ShareData.PxToDpi_xhdpi(5);
        mLineTotalHeight = ShareData.PxToDpi_xhdpi(12);
        mLeftSideTouchPadding = ShareData.PxToDpi_xhdpi(35);
        mRightSideTouchPadding = ShareData.PxToDpi_xhdpi(35);
        mBitmapListHeight = mParentView.mBitmapFrameHeight;

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStyle(Paint.Style.FILL);

        mSelectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectedPaint.setColor(0xffffcf56);

        mUnselectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnselectedPaint.setColor(0x7f000000);
    }

    private void initDynamicData() {
        mMinimalClipDistance = mParentView.mMinimalRect;
        mBitmapListWidth = mParentView.mBitmapListWidth;
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
    }

    public void setTouchPointCoordination(int startX, int initialX) {
        mLastStartX = startX;
        mLastEndX = mLastStartX + mParentView.mSelectViewWidth;
        mInitialStartX = initialX;
        mInitialEndX = mLastEndX;
        initDynamicData();
        invalidate();
    }

    private static final int OFFSET_TO_SCROLL = ShareData.PxToDpi_xhdpi(120);
    private static final int SCROLL_DISTANCE = ShareData.PxToDpi_xhdpi(30);
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();

        if (action == MotionEvent.ACTION_DOWN) {
            if (mLastStartX -  mLeftSideTouchPadding <= x&& x <= mLastStartX + mRightSideTouchPadding + mSelectRectWidth && y >= 0 && y <= mHeight) {
                mPressedLeft = true;
                mPressedDistance = (x - mLastStartX);
                ViewParent viewParent = this.getParent();
                viewParent.requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            } else if (mLastEndX - mLeftSideTouchPadding - mSelectRectWidth <= x && x <= mLastEndX + mRightSideTouchPadding && y >= 0 && y <= mHeight) {
                mPressedRight = true;
                mPressedDistance = x - mLastEndX;
                ViewParent viewParent = this.getParent();
                viewParent.requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mPressedLeft && mLeftHandleCanMove) {
                float oldLastStart = mLastStartX;
                mLastStartX = x - mPressedDistance;

                // true为向左滑动，false为向右滑动
                boolean leftHandleDirection = mLastStartX - oldLastStart < 0 ? true : false;

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
                    mCallback.onProgressChange(mLeftProgress, mRightProgress);
                    mCallback.onDragHandlePositionChange(mLastStartX, mLastEndX);
                }
                invalidate();

                if (leftHandleDirection) {
                    if(mLastStartX <= OFFSET_TO_SCROLL && mParentView.mBitmapListView.canScrollHorizontally(-1)) {
                        mParentView.mBitmapListView.scrollBy(-SCROLL_DISTANCE, 0);
                    }
                } else {
                    if (mLastStartX >= mWidth - OFFSET_TO_SCROLL && mParentView.mBitmapListView.canScrollHorizontally(1)) {
                        mParentView.mBitmapListView.scrollBy(SCROLL_DISTANCE, 0);
                    }
                }
                return true;

            } else if (mPressedRight && mRightHandleCanMove) {
                float oldLastEndX = mLastEndX;
                mLastEndX = x - mPressedDistance;

                // false为向左拉动，true为向右拉动
                boolean rightHandleDirection = mLastEndX - oldLastEndX > 0;
                float minimalLimit = mLastStartX + mSelectRectWidth * 2 + mMinimalClipDistance;
                float maxLimit = mLastEndX > mWidth ? mWidth : mLastEndX;
                if (mLastEndX < minimalLimit) {
                    mLastEndX = minimalLimit;
                } else if (mLastEndX > maxLimit) {
                    mLastEndX = maxLimit;
                }

                if (rightHandleDirection) {
                    if(mLastEndX >= mWidth - OFFSET_TO_SCROLL && mParentView.mBitmapListView.canScrollHorizontally(1)) {
                        mParentView.mBitmapListView.scrollBy(SCROLL_DISTANCE, 0);
                    }
                } else {
                    if (mLastEndX <= OFFSET_TO_SCROLL && mParentView.mBitmapListView.canScrollHorizontally(-1)) {
                        mParentView.mBitmapListView.scrollBy(-SCROLL_DISTANCE, 0);
                    }
                }

                mRightProgress = (mLastEndX - mSelectRectWidth * 2 + mTotalOffset - mInitialStartX) / (mBitmapListWidth);
                mRightProgress = mRightProgress < 0 ? 0 : mRightProgress > 1 ? 1 : mRightProgress;

                if (mCallback != null) {
                    mCallback.onProgressChange(mLeftProgress, mRightProgress);
                    mCallback.onDragHandlePositionChange(mLastStartX, mLastEndX);
                }
                invalidate();

                return true;
            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (mPressedLeft) {
                mPressedLeft = false;

                if (mCallback != null) {
                    mCallback.onProgressConfirm(mLeftProgress, mRightProgress);
                }

                return true;
            } else if (mPressedRight) {
                mPressedRight = false;

                if (mCallback != null) {
                    mCallback.onProgressConfirm(mLeftProgress, mRightProgress);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 画出代表选择的矩形的左边;
        canvas.drawRect(mLastStartX , 0, mLastStartX + mSelectRectWidth, canvas.getHeight(), mSelectedPaint);
        // 画出代表选择的矩形的右边;
        canvas.drawRect(mLastEndX - mSelectRectWidth, 0, mLastEndX, canvas.getHeight(), mSelectedPaint);

        int rightPositionOfRect = (int) ((mLastEndX - mSelectRectWidth) + 0.5f);
        // 画出代表选择的矩形的上边
        canvas.drawRect(mLastStartX + mSelectRectWidth, 0, rightPositionOfRect, mSelectRectHeight, mSelectedPaint);
        // 画出代表选择的矩形的下边
        canvas.drawRect(mLastStartX + mSelectRectWidth, mBitmapListHeight + mSelectRectHeight, rightPositionOfRect, mBitmapListHeight + 2 * mSelectRectHeight, mSelectedPaint);

        int shortLineTop = (mHeight - mLineTotalHeight) / 2;
        int shortLineLeft = (int)(mLastStartX + ShareData.PxToDpi_xhdpi(10));


        // 画出左边的拖动游标
        if (!mLeftHandleCanMove) {
            mLinePaint.setColor(Color.GRAY);
        } else {
            mLinePaint.setColor(Color.WHITE);
        }
        canvas.drawRect(shortLineLeft, shortLineTop, shortLineLeft + ShareData.PxToDpi_xhdpi(8), shortLineTop + ShareData.PxToDpi_xhdpi(2), mLinePaint);
        canvas.drawRect(shortLineLeft, shortLineTop + ShareData.PxToDpi_xhdpi(5), shortLineLeft + ShareData.PxToDpi_xhdpi(8), shortLineTop + ShareData.PxToDpi_xhdpi(7), mLinePaint);
        canvas.drawRect(shortLineLeft, shortLineTop + ShareData.PxToDpi_xhdpi(10), shortLineLeft + ShareData.PxToDpi_xhdpi(8), shortLineTop + ShareData.PxToDpi_xhdpi(12), mLinePaint);

        // 画出右边的拖动游标
        int rightEndX = (int)(mLastEndX - ShareData.PxToDpi_xhdpi(20));
        int longLineLeft = rightEndX;
        int longLineTop = (mHeight - mLineTotalHeight) / 2;

        if (!mRightHandleCanMove) {
            mLinePaint.setColor(Color.GRAY);
        } else {
            mLinePaint.setColor(Color.WHITE);
        }
        canvas.drawRect(longLineLeft, longLineTop, longLineLeft + ShareData.PxToDpi_xhdpi(8), longLineTop + ShareData.PxToDpi_xhdpi(2), mLinePaint);
        canvas.drawRect(longLineLeft, longLineTop + ShareData.PxToDpi_xhdpi(5), longLineLeft + ShareData.PxToDpi_xhdpi(8), longLineTop + ShareData.PxToDpi_xhdpi(7), mLinePaint);
        canvas.drawRect(longLineLeft, longLineTop + ShareData.PxToDpi_xhdpi(10), longLineLeft + ShareData.PxToDpi_xhdpi(8), longLineTop + ShareData.PxToDpi_xhdpi(12), mLinePaint);

        // 分情况画出左边代表没有选中的矩形
        canvas.drawRect(mInitialStartX - mTotalOffset, mSelectRectHeight, mLastStartX, mSelectRectHeight + mBitmapListHeight, mUnselectedPaint);
        // 画出右边代表没有选中的矩形
        canvas.drawRect(mLastEndX, mSelectRectHeight, mWidth, mSelectRectHeight + mBitmapListHeight, mUnselectedPaint);
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


    protected void scrollListMannually(int offset) {
        mTotalOffset += offset;
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
