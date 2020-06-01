package cn.poco.album2.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by: fwc
 * Date: 2016/9/22
 */

public class ProgressView extends View {

    private static final int COLOR_GRAY = 0xff595959;
    private static final int COLOR_YELLOW = 0xffffcf56;

    private Paint mPaint;

    private float mProgress;

    private int mWidth;

    private int mHeight;

    private int mNormalColor = COLOR_GRAY;
    private int mProgressColor = COLOR_YELLOW;

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void setProgress(float progress) {
        if (progress < 0) {
            progress = 0;
        } else if (progress > 100) {
            progress = 100;
        }
        mProgress = progress;
        invalidate();
    }

    public void setNormalColor(int color) {
        mNormalColor = color;
    }

    public void setProgressColor(int color) {
        mProgressColor = color;
    }

    public float getProgress() {
        return mProgress;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mNormalColor);
        canvas.drawRect(0, 0, mWidth, mHeight, mPaint);
        mPaint.setColor(mProgressColor);
        canvas.drawRect(0, 0, mWidth * mProgress * 1.0f / 100, mHeight, mPaint);
    }
}
