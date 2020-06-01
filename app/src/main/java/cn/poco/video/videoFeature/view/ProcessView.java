package cn.poco.video.videoFeature.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import cn.poco.tianutils.ShareData;

/**
 * Created by Simon Meng on 2018/1/22.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class ProcessView extends View
{
    private static final String TAG = "ProcessView";
    private Paint mPaint;
    private float mProcess = 0;
    private int mLineW = ShareData.PxToDpi_xhdpi(6);
    //    private int mTouchW = ShareData.PxToDpi_xhdpi(40);  //拖动的view
    private int mTouchW = ShareData.m_screenWidth / 18;  //拖动的view

    private int endTime;
    private int startTime;

    public ProcessView(Context context)
    {
        super(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(0xffffc433);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if (endTime == 0)
        {
            endTime = w;
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        int start = (int) (mProcess * (getRangW()));
        if (start < startTime)
        {
            start = startTime;
        } else if (start > endTime)
        {
            start = endTime;
        }
        canvas.drawRect(start, 0, start + mLineW, getHeight(), mPaint);

//        canvas.drawRect(start, 0, endTime + mLineW, getHeight(), mPaint);
    }

    public void setRang(float startProgress,float endProgress)
    {
        startTime = (int) (startProgress * getRangW());
        endTime = (int) (endProgress * getRangW());
        setRangTime(startTime,endTime);
    }

    private void setRangTime(int start, int end)
    {
        startTime = start;
        if(startTime < 0){
            startTime = 0;
        }
//        endTime = end - 2 * VideoSelectView.SELECT_RECT_W;
        endTime = end;
        if(endTime < 0 || endTime > getRangW()){
            endTime = getRangW();
        }
        invalidate();
    }

    public void setProcess(float mProcess)
    {
        this.mProcess = mProcess;
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean isHandle = false;
        if (isClickable())
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    float downX = event.getX();
//                    float centerX = (mProcess * (getWidth() - 2 * mLineW) + mLineW / 2);
//                    int startX = (int) (centerX - mTouchW);
//                    int endX = (int) (centerX + mTouchW);
//                    if (startX < 0)
//                    {
//                        startX = 0;
//                        endX = mTouchW * 2;
//                    } else if (endX > getWidth())
//                    {
//                        endX = getWidth();
//                        startX = getWidth() - mTouchW * 2;
//                    }
//                    if (downX > startX && downX < endX)
//                    {
                    isHandle = true;
                    mProcess = getProcess(downX);
                    invalidate();
                    if (mProcessLister != null)
                    {
                        mProcessLister.onDown(downX);
                    }
//                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    float curX = event.getX();
                    mProcess = getProcess(curX);
                    invalidate();
                    isHandle = true;
                    if (mProcessLister != null)
                    {
                        mProcessLister.onProgress(mProcess);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    float upX = event.getX();
                    mProcess = getProcess(upX);
                    invalidate();
                    isHandle = true;
                    if (mProcessLister != null)
                    {
                        mProcessLister.onUp(mProcess);
                    }
                    break;
                default:
                    break;
            }
        }
        if (isHandle)
        {
            return true;
        } else
        {
            return super.onTouchEvent(event);
        }
    }

    private int getRangW()
    {
//        return endTime - startTime;
//        return getWidth() - getPaddingLeft() - getPaddingRight();
        return getWidth() - mLineW;
    }

    private float getProcess(float touchX)
    {
        float process;
        float curX = touchX;
        if (curX <= 0)
        {
            process = 0;
        } else if (curX >= getWidth())
        {
            process = 1;
        } else
        {
            process = curX / getWidth();
        }
        return process;
    }

    private ProcessLister mProcessLister;

    public void setProcessLister(ProcessLister processLister)
    {
        this.mProcessLister = processLister;
    }

    public interface ProcessLister
    {
        void onProgress(float process);

        void onDown(float process);

        void onUp(float process);
    }


}
