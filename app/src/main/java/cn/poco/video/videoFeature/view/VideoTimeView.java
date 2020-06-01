package cn.poco.video.videoFeature.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import cn.poco.tianutils.ShareData;

/**
 * Created by Simon Meng on 2018/1/16.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoTimeView extends View {
    private float mDotRadius = ShareData.PxToDpi_xhdpi(3);

    private long mTimeDuration;
    private float mTimeInterval;
    private int mCount;
    private Paint mPaint;

    public VideoTimeView(Context context, long timeDuration) {
        super(context);
        mTimeDuration = timeDuration;
        mTimeInterval = (long)((float)mTimeDuration / 3);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setColor(0xff666666);
        mPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,14, getResources().getDisplayMetrics()));
        mPaint.setTextAlign(Paint.Align.LEFT);
    }

    private int mWidth, mHeight;
    private float mTextDistance;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != 0 && h != 0) {
            mWidth = w;
            mHeight = h;
            mTextDistance = mWidth / 6;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Rect bounds = new Rect();
        String text = "0S";
        mPaint.getTextBounds("0S", 0, text.length(), bounds);
//        Paint.FontMetrics metric = mPaint.getFontMetrics();
//        int textHeight = (int) Math.ceil(metric.descent - metric.ascent);
//        int y = (int)(textHeight - metric.descent);
        int y = bounds.height();
        float distanceToLeftEdge = mTextDistance;
        float distanceToRightRightEdge = mTextDistance - bounds.width();

        canvas.save();
        canvas.drawText("0S", 0, y, mPaint);
        canvas.translate(distanceToLeftEdge, 0);
        canvas.drawCircle(mDotRadius, y / 2, mDotRadius, mPaint);
        canvas.translate(distanceToRightRightEdge, 0);
        canvas.drawText("1S", 0, y, mPaint);
        canvas.translate(distanceToLeftEdge, 0);
        canvas.drawCircle(mDotRadius, y / 2, mDotRadius, mPaint);
        canvas.translate(distanceToRightRightEdge, 0);
        canvas.drawText("2S", 0, y, mPaint);
        canvas.translate(distanceToLeftEdge, 0);
        canvas.drawCircle(mDotRadius, y / 2, mDotRadius, mPaint);
        canvas.translate(distanceToRightRightEdge, 0);
        canvas.drawText("3S", 0, y, mPaint);
        canvas.restore();
    }
}
