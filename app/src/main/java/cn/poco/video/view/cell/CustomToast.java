package cn.poco.video.view.cell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

import cn.poco.tianutils.ShareData;
import cn.poco.video.utils.AndroidUtil;

/**
 * Created by Shine on 2017/6/23.
 */

public class CustomToast extends View {
    private Paint mBgPaint;
    private TextPaint mTextPaint;
    private StaticLayout mTextLayout;
    private String mText;
    private float mTextWidth;

    private Context mContext;

    private int mEdgePadding;

    public CustomToast(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(0x4d000000);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(AndroidUtil.convertDpToPixel(mContext, 12));
        mTextPaint.setColor(0xffffffff);

        mEdgePadding = ShareData.PxToDpi_xhdpi(20);
    }


    public void setToastText(String text) {
        mText = text;
        mTextWidth = mTextPaint.measureText(text);
        requestLayout();
        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int)(mTextWidth + mEdgePadding * 2);
        if (!TextUtils.isEmpty(mText)) {
            mTextLayout = new StaticLayout(mText, mTextPaint, (int)mTextWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        }
        setMeasuredDimension(width, MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTextLayout != null) {
            canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), mBgPaint);
            canvas.save();

            int textX = mEdgePadding;
            int textY = (int)((this.getHeight() - mTextLayout.getHeight()) / 2f);
            canvas.translate(textX, textY);
            mTextLayout.draw(canvas);
            canvas.restore();
        }
    }
}
