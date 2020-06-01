package cn.poco.video.videoSpeed;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import cn.poco.tianutils.ShareData;


/**
 * Created by lgd on 2018/1/19.
 */

public class SpeedSeekBar extends View
{
    private int selectedBigR = ShareData.PxToDpi_xhdpi(24);
    private int selectedSmallR = ShareData.PxToDpi_xhdpi(8);
    private int normalBigR = ShareData.PxToDpi_xhdpi(16);
    private int normalSmallR = ShareData.PxToDpi_xhdpi(4);
    private Paint mPaint;
    private int mMax = 5;     //小圆点的数量
    private int mProgress = 0;  //当前选择的下表
    private int mCurCenterX = 0;   //黄色选择圆的中心点
    private int[] mDotCenterXs;

    public SpeedSeekBar(Context context)
    {
        super(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mDotCenterXs = new int[mMax];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0)
        {
            calculateDotsPosition();
        }
    }

    public void calculateDotsPosition()
    {
        for (int i = 0; i < mDotCenterXs.length; i++)
        {
            int startX = i * (getLineW()) / (mMax - 1);
            mDotCenterXs[i] = startX + selectedBigR;
        }
        mCurCenterX = getDotCenterX(mProgress);
//        setAndCheckCenterX(mCurCenterX);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        drawLine(canvas);
        for (int i = 0; i < mMax; i++)
        {
            drawDot(canvas, i);
        }
        drawSelectedDot(canvas);
    }

    private void drawSelectedDot(Canvas canvas)
    {
        int centerY = getHeight() / 2;
        mPaint.setColor(0xffffc433);
        canvas.drawCircle(mCurCenterX, centerY, selectedBigR, mPaint);
        mPaint.setColor(0xffffffff);
        canvas.drawCircle(mCurCenterX, centerY, selectedSmallR, mPaint);
    }

    private void setAndCheckCenterX(int position)
    {
        mCurCenterX = position;
        if (mCurCenterX < selectedBigR)
        {
            mCurCenterX = selectedBigR;
        }
        if (mCurCenterX > getWidth() - selectedBigR)
        {
            mCurCenterX = getWidth() - selectedBigR;
        }
    }

    private void drawLine(Canvas canvas)
    {
        mPaint.setColor(0xff333333);
        int r = ShareData.PxToDpi_xhdpi(1);
        int startX = selectedBigR;
        int startY = getHeight() / 2;
        int endX = getWidth() - selectedBigR;
        int endY = startY;
        canvas.drawRect(startX, startY - r, endX, endY + r, mPaint);
    }

    private void drawDot(Canvas canvas, int index)
    {
        mPaint.setColor(0xff333333);
        canvas.drawCircle(mDotCenterXs[index], getHeight() / 2, normalBigR, mPaint);
        mPaint.setColor(0x1fffffff);
        canvas.drawCircle(mDotCenterXs[index], getHeight() / 2, normalSmallR, mPaint);
    }

    private int getDotCenterX(int index)
    {
        if (index >= 0 && index < mDotCenterXs.length)
        {
            return mDotCenterXs[index];
        } else
        {
            return selectedBigR;
        }
    }

    /**
     * 放手时 恢复原位置
     *
     * @param touchPosition
     * @return
     */
    private int getRestorePosition(int touchPosition)
    {
        int index = getApproximateProgress(touchPosition);
        if (index >= 0)
        {
            return mDotCenterXs[index];
        } else
        {
            return mDotCenterXs[0];
        }
    }

    private int getApproximateProgress(int centerPosition)
    {
        return MathUtils.binarySearchApproximate(mDotCenterXs, centerPosition, 0, mDotCenterXs.length - 1);
    }


    private int getLineW()
    {
        return getWidth() - getPaddingLeft() - getRightPaddingOffset() - selectedBigR * 2;
    }

    private int getSuitableOffset(int start, int end)
    {
        int move = Math.abs(start - end) * DELAY_TIME / 50;    //每DELAY_TIME毫秒位移的距离   50毫秒移动完
        int minOffSet = ShareData.m_screenWidth * DELAY_TIME / 300; //每DELAY_TIME毫秒至少位移的距离
        if (move < minOffSet)
        {
            move = minOffSet;
        }
        return move;
    }

    private final int DELAY_TIME = 5; //5毫秒刷新一次；
    private int mCenterTargetX; //当前圆点需要去的位置
    private int mOffset; //每次post需要位移的距离
    private boolean isChecking = false;  //runnable是否在检查位置
    private boolean mIsDrag = false;
    boolean isDownTouchRight = false;  //判断滑动球ACTION_MOVE 是否到达手指下，然后取消动画检测

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                int touchX = (int) event.getX();
                mOffset = getSuitableOffset(touchX, mCurCenterX);
                if (touchX > mCurCenterX)
                {
                    isDownTouchRight = true;
                } else
                {
                    isDownTouchRight = false;
                }
                mIsDrag = true;
                mCenterTargetX = touchX;
//                if (mOnSeekBarChangeListener != null)
//                {
//                    mOnSeekBarChangeListener.onStartTrackingTouch(SpeedSeekBar.this);
//                }
                stopChecking();
                startChecking();
                break;
            case MotionEvent.ACTION_MOVE:
                mCenterTargetX = (int) event.getX();
                if (mCenterTargetX <= mCurCenterX && isDownTouchRight && isChecking)
                {
                    //滑动球到达手指时，要取消检测，滑动球随move， 不然滑动球跟不上手指
                    stopChecking();
                } else if (!isChecking)
                {
                    setAndCheckCenterX(mCenterTargetX);
                    checkPosition();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsDrag = false;
                mCenterTargetX = (int) event.getX();
                mCenterTargetX = getRestorePosition(mCenterTargetX);
                if (!isChecking)
                {
                    mOffset = getSuitableOffset(mCenterTargetX, mCurCenterX);
                    startChecking();
//                    if (Math.abs(mCenterTargetX - mCurCenterX) > mOffset)
//                    {
//                        startChecking();
//                    } else
//                    {
//                        mCurCenterX = mCenterTargetX;
//                        invalidate();
//                    }
                }
                break;
        }
        return true;
    }

    private void startChecking()
    {
        isChecking = true;
        postDelayed(mCheckPosition, DELAY_TIME);
    }

    private void stopChecking()
    {
        isChecking = false;
        removeCallbacks(mCheckPosition);
    }

    private Runnable mCheckPosition = new Runnable()
    {
        @Override
        public void run()
        {
//            if (Math.abs(mCurCenterX - mCenterTargetX) > mOffset)
//            {
//                mCurCenterX += mCenterTargetX > mCurCenterX ? mOffset : -mOffset;
//                startChecking();
//            } else
//            {
//                isChecking = false;
//                mCurCenterX = mCenterTargetX;
//            }
            if (Math.abs(mCurCenterX - mCenterTargetX) > 0)
            {
                if (Math.abs(mCurCenterX - mCenterTargetX) > mOffset)
                {
                    mCurCenterX += mCenterTargetX > mCurCenterX ? mOffset : -mOffset;
                }else{
                    mCurCenterX = mCenterTargetX;
                }
                startChecking();
            } else
            {
                isChecking = false;
            }
            checkPosition();
        }
    };

    private void checkPosition()
    {
        int progress = getApproximateProgress(mCurCenterX);
        if (progress != -1 && mProgress != progress)
        {
            mProgress = progress;
            if (mOnSeekBarChangeListener != null)
            {
                mOnSeekBarChangeListener.onProgressChanged(SpeedSeekBar.this, progress, true);
            }
        }
        if (mOnSeekBarChangeListener != null)
        {
            mOnSeekBarChangeListener.onCenterChanged(SpeedSeekBar.this, mCurCenterX);
        }
        if (!mIsDrag && !isChecking)
        {
            if (mOnSeekBarChangeListener != null)
            {
                mOnSeekBarChangeListener.onStopTrackingTouch(SpeedSeekBar.this);
            }
        }
        invalidate();
    }

    public void setMax(int max)
    {
        mMax = max;
        mDotCenterXs = new int[mMax];
        calculateDotsPosition();
        invalidate();
    }

    public void setProgress(int progress)
    {
        if (progress < 0)
        {
            progress = 0;
        }
        if (progress > mMax -1)
        {
            progress = mMax - 1;
        }
        mProgress = progress;
        mCurCenterX = getDotCenterX(mProgress);
        if (mOnSeekBarChangeListener != null)
        {
            mOnSeekBarChangeListener.onProgressChanged(SpeedSeekBar.this, progress, false);
            mOnSeekBarChangeListener.onCenterChanged(SpeedSeekBar.this, mCurCenterX);
        }
        invalidate();
    }

    public int getCenterX()
    {
        return mCurCenterX;
    }

    public int getProgress()
    {
        return mProgress;
    }

    public int getSelectedBigR()
    {
        return selectedBigR;
    }

    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener)
    {
        this.mOnSeekBarChangeListener = onSeekBarChangeListener;
    }

    public interface OnSeekBarChangeListener
    {
        //中心点的位置监听
        public void onCenterChanged(SpeedSeekBar seekBar, int centerX);

        public void onProgressChanged(SpeedSeekBar seekBar, int progress, boolean fromUser);

//        public void onStartTrackingTouch(SpeedSeekBar seekBar);

        public void onStopTrackingTouch(SpeedSeekBar seekBar);
    }
}
