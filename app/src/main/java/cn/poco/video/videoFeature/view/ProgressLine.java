package cn.poco.video.videoFeature.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Simon Meng on 2018/1/9.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class ProgressLine extends View {
    private Paint mPaint;

    public ProgressLine(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setDither(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, this.getWidth(), getHeight(), mPaint);
    }
}
