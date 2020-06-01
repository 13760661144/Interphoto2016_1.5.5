package cn.poco.video.videoFeature.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import cn.poco.tianutils.ShareData;

/**
 * Created by Simon Meng on 2018/2/2.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class SelectedDrawable extends Drawable{
    private int mWidth, mHeight;

    public SelectedDrawable(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        initPaint();
    }

    private Paint mBorderPaint;
    private void initPaint() {
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(0xffffc433);
        mBorderPaint.setDither(true);
        mBorderPaint.setStrokeWidth(ShareData.PxToDpi_xhdpi(6));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect background = new Rect();
        background.left = 0;
        background.top = 0;
        background.right = background.left + mWidth;
        background.bottom = background.top + mHeight;
        canvas.drawRect(background, mBorderPaint);

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
