package cn.poco.video.videoSpeed;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import cn.poco.tianutils.ShareData;
import cn.poco.video.utils.AndroidUtil;

/**
 * Created by lgd on 2018/2/3.
 * <p>
 * 这个getViewWidth 和 SpeedSeekBar 宽度一样 ，setPadding改变
 */

public class SpeedTimeView extends View
{
    private String mTextInfos[];
    private int selectedIndex = 0;
    private Paint mPaint;
    private int normalBaseLine = ShareData.PxToDpi_xhdpi(23);
    private int baseLineSelected = ShareData.PxToDpi_xhdpi(35);
    private float normalTextSize = 12f;
    private int increaentBaseLine = ShareData.PxToDpi_xhdpi(35 - 23);
    private float incrementTextSize = 14 - 12f;
    private int mCenterX;
    private int mTextCenterXs[];

    public SpeedTimeView(Context context)
    {
        super(context);
        mTextInfos = new String[]{"1/4X", "1/2X", "正常", " 2X", "4X"};
        mTextCenterXs = new int[mTextInfos.length];
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDotsPosition();
    }

    public void calculateDotsPosition()
    {
        for (int i = 0; i < mTextCenterXs.length; i++)
        {
            int centerX = i * (
                    getViewWidth()) / (mTextCenterXs.length - 1);
            mTextCenterXs[i] = centerX + getPaddingLeft();
        }
        if (mCenterX == 0)
        {
            mCenterX = mTextCenterXs[0];
        }

    }

    private int getViewWidth()
    {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private static final String TAG = "SpeedTimeView";

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
//            int index = MathUtils.binarySearchApproximate(mTextCenterXs, mCenterX, 0, mTextCenterXs.length - 1);
        int average = getViewWidth() / (mTextCenterXs.length + 2);
        for (int i = 0; i < mTextInfos.length; i++)
        {
            float textSize = normalTextSize;
            float baseLine = normalBaseLine;
            if (Math.abs(mTextCenterXs[i] - mCenterX) <= average)
            {
                float f = Math.abs(mCenterX - mTextCenterXs[i]) * 1.0f / average;
                textSize += incrementTextSize * (1 - f);
                baseLine += increaentBaseLine * (1 - f);
            }
            mPaint.setTextSize(AndroidUtil.convertDpToPixel(getContext(), textSize));
            canvas.drawText(mTextInfos[i], mTextCenterXs[i], baseLine, mPaint);
        }


//        for (int i = 0; i < mTextInfos.length; i++)
//        {
//            int baseLine = normalBaseLine;
//            if (i == selectedIndex)
//            {
//                baseLine = baseLineSelected;
//                mPaint.setTextSize(AndroidUtil.convertDpToPixel(getContext(), 14));
//            } else
//            {
//                baseLine = normalBaseLine;
//                mPaint.setTextSize(AndroidUtil.convertDpToPixel(getContext(), 12));
//            }
//            int width = (int) mPaint.measureText(mTextInfos[i]);
//            mPaint.setTextAlign(Paint.Align.CENTER);
//            if (i == 0)
//            {
////                canvas.drawText(mTextInfos[i], (margin+width)/ 2, baseLine, mPaint);
//                canvas.drawText(mTextInfos[i], width / 2, baseLine, mPaint);
//            } else if (i == mTextInfos.length - 1)
//            {
////                canvas.drawText(mTextInfos[i], getWidth() - (width + margin)/2, baseLine, mPaint);
//                canvas.drawText(mTextInfos[i], getWidth() - width / 2 - margin, baseLine, mPaint);
//            } else if (i == 2)
//            {
//                canvas.drawText(mTextInfos[i], getWidth() / 2, baseLine, mPaint);
//            } else if (i < 2)
//            {
//                canvas.drawText(mTextInfos[i], getWidth() * i / (mTextInfos.length - 1) + margin, baseLine, mPaint);
//            } else if (i > 2)
//            {
//                canvas.drawText(mTextInfos[i], getWidth() * i / (mTextInfos.length - 1) - margin, baseLine, mPaint);
//            }
//        }
    }

//    private int getTextSize(int textIndex)
//    {
////        int position = mTextCenterXs[m]
////        if(index > -1){
////           mTextCenterXs
////        }
//        int average = getViewWidth() / (mTextCenterXs.length + 2);
//
//        int textSize = normalBaseLine;
//        int index = MathUtils.binarySearchApproximate(mTextCenterXs, mCenterX, 0, mTextCenterXs.length - 1);
//        int position = mTextCenterXs[index];
//        textSize += incrementTextSize * (1 - (mCenterX - mTextCenterXs[index]) / average);
//
////        int average = getViewWidth() / mTextCenterXs.length+2;
////        int textSize = normalBaseLine;
////        if (mCenterX < mTextCenterXs[0] + average)
////        {
////            textIndex += (mCenterX - mTextCenterXs[0]) *
////        }else if()
//
//    }

    public void setCenterX(int centerX)
    {
        mCenterX = centerX;
        if (mCenterX < getPaddingLeft())
        {
            mCenterX = getPaddingLeft();
        }
        if (mCenterX > getViewWidth()+getPaddingRight())
        {
            mCenterX = getViewWidth()+getPaddingRight();
        }
        invalidate();
    }
}
