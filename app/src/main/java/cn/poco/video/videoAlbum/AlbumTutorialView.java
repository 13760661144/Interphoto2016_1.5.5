package cn.poco.video.videoAlbum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.video.utils.AndroidUtil;

/**
 * Created by Shine on 2017/7/11.
 */

public class AlbumTutorialView extends View {
    private Drawable mDrawable;
    private int mDrawableX = -1, mDrawableY = -1;
    private StaticLayout mTextLayout;
    private Paint mShadowPaint;
    private TextPaint mTextPaint;

    public AlbumTutorialView(Context context) {
        super(context);
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(0x99000000);

        mDrawable = context.getResources().getDrawable(R.drawable.video_tutorial_arrow);
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(AndroidUtil.convertDpToPixel(context, 13));

        String text = context.getString(R.string.press_to_preview);
        mTextLayout = new StaticLayout(text, mTextPaint, ShareData.PxToDpi_xhdpi(336), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    }

    public void show() {
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (AlbumTutorialView.this.getVisibility() != View.VISIBLE) {
                    AlbumTutorialView.this.setVisibility(View.VISIBLE);
                }
            }
        }, 200);
    }


    public void disapear() {
        if (this.getVisibility() != GONE) {
            this.setVisibility(View.GONE);
        }
    }

    public void setPosition(int x, int y) {
        mDrawableX = x;
        mDrawableY = y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawableX != -1 && mDrawableY != -1) {
            canvas.drawRect(0, 0, ShareData.m_screenWidth, ShareData.PxToDpi_xhdpi(80), mShadowPaint);
            canvas.drawRect(2 * mDrawableX, ShareData.PxToDpi_xhdpi(80), ShareData.m_screenWidth, ShareData.PxToDpi_xhdpi(80) + 2 * mDrawableX, mShadowPaint);
            canvas.drawRect(0, ShareData.PxToDpi_xhdpi(80) + 2 * mDrawableX, ShareData.m_screenWidth, ShareData.m_screenHeight, mShadowPaint);
            mDrawable.setBounds(mDrawableX, mDrawableY, mDrawableX + mDrawable.getIntrinsicWidth(), mDrawableY + mDrawable.getIntrinsicHeight());
            mDrawable.draw(canvas);

            int textX = mDrawableX + ShareData.PxToDpi_xhdpi(25);
            int textY = mDrawable.getBounds().bottom - ShareData.PxToDpi_xhdpi(30);
            canvas.save();
            canvas.translate(textX, textY);
            mTextLayout.draw(canvas);
            canvas.restore();
        }
    }
}
