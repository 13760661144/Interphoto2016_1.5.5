package cn.poco.video.view.cell;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import cn.poco.interphoto2.R;

/**
 * Created by Shine on 2017/6/22.
 */

public class LoadingCell extends View {
    private Bitmap mLoadingBitmap;
    private Paint mDrawPaint;

    private int mRotateDegrees;

    private ValueAnimator mAnimator;

    public LoadingCell(Context context) {
        this(context, R.drawable.login_loading_logo);
    }

    public LoadingCell(Context context, int loadingIcon) {
        super(context);
        mLoadingBitmap = BitmapFactory.decodeResource(context.getResources(), loadingIcon);
        mDrawPaint = new Paint();
        mDrawPaint.setAntiAlias(true);

        startAnimation(400);
    }

    public void startAnimation(int duration) {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }

        mAnimator = ValueAnimator.ofInt(0, 360);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setDuration(duration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int)animation.getAnimatedValue();
                mRotateDegrees = value;
                invalidate();
            }
        });
        mAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mLoadingBitmap.getWidth(), mLoadingBitmap.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.rotate(mRotateDegrees, this.getWidth() / 2, this.getHeight() / 2);
        canvas.drawBitmap(mLoadingBitmap, 0, 0, mDrawPaint);
        canvas.restore();
    }

}
