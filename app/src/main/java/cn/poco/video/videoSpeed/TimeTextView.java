package cn.poco.video.videoSpeed;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import cn.poco.tianutils.ShareData;
import cn.poco.video.utils.AndroidUtil;

/**
 * Created by lgd on 2018/1/18.
 */

public class TimeTextView extends View
{
    private String textInfos[] = new String[]{"1/4X", "1/2X", "正常", " 2X", "4X"};
    private int selectedIndex = 0;
    private Paint mPaint;
    private int baseLineNormal = ShareData.PxToDpi_xhdpi(23);
    private int baseLineSelected = ShareData.PxToDpi_xhdpi(35);

    public TimeTextView(Context context)
    {
        super(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        int margin = ShareData.PxToDpi_xhdpi(16);
        for (int i = 0; i < textInfos.length; i++)
        {
            int baseLine = baseLineNormal;
            if (i == selectedIndex)
            {
                baseLine = baseLineSelected;
                mPaint.setTextSize(AndroidUtil.convertDpToPixel(getContext(), 14));
            } else
            {
                baseLine = baseLineNormal;
                mPaint.setTextSize(AndroidUtil.convertDpToPixel(getContext(), 12));
            }
            int width = (int) mPaint.measureText(textInfos[i]);
            mPaint.setTextAlign(Paint.Align.CENTER);
            if (i == 0)
            {
//                canvas.drawText(textInfos[i], (margin+width)/ 2, baseLine, mPaint);
                canvas.drawText(textInfos[i], width/ 2, baseLine, mPaint);
            } else if (i == textInfos.length - 1)
            {
//                canvas.drawText(textInfos[i], getWidth() - (width + margin)/2, baseLine, mPaint);
                canvas.drawText(textInfos[i], getWidth() - width/2-margin, baseLine, mPaint);
            } else if (i == 2)
            {
                canvas.drawText(textInfos[i], getWidth() / 2, baseLine, mPaint);
            } else if (i < 2)
            {
                canvas.drawText(textInfos[i], getWidth() * i / (textInfos.length - 1) + margin, baseLine, mPaint);
            } else if (i > 2)
            {
                canvas.drawText(textInfos[i], getWidth() * i / (textInfos.length - 1) - margin, baseLine, mPaint);
            }
        }
    }

    public void setSelectedIndex(int selectedIndex)
    {
        this.selectedIndex = selectedIndex;
        invalidate();
    }
}
