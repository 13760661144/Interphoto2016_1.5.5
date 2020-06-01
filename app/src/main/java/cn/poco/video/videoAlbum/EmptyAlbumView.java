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

public class EmptyAlbumView extends View {
    private Drawable mEmptyDrawable;
    private StaticLayout mTextLayout;

    private String mText;
    private TextPaint mTextPaint;
    private int mViewOffset;

    public EmptyAlbumView(Context context) {
        super(context);
        mViewOffset = ShareData.PxToDpi_xhdpi(20);
        mText = context.getResources().getString(R.string.video_album_empty_text);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(AndroidUtil.convertDpToPixel(context, 16));
        mTextPaint.setColor(0xff666666);
        mEmptyDrawable = context.getResources().getDrawable(R.drawable.album_empty);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTextLayout = new StaticLayout(mText, mTextPaint, this.getMeasuredWidth(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int centerY = (this.getHeight() - mTextLayout.getHeight() - mEmptyDrawable.getIntrinsicHeight() - mViewOffset) / 2;
        int drawableLeft = (this.getWidth() - mEmptyDrawable.getIntrinsicWidth()) / 2;
        int drawableTop = centerY;
        mEmptyDrawable.setBounds(drawableLeft, drawableTop, drawableLeft + mEmptyDrawable.getIntrinsicWidth(), drawableTop + mEmptyDrawable.getIntrinsicHeight());
        mEmptyDrawable.draw(canvas);
        canvas.save();

        int textX = (this.getWidth() - mTextLayout.getWidth()) / 2;
        canvas.translate(textX, centerY + mEmptyDrawable.getIntrinsicHeight() + mViewOffset);
        mTextLayout.draw(canvas);
        canvas.restore();
    }
}
