package cn.poco.video.videoFeature.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.view.View;

import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.ImageUtil;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.sequenceMosaics.TransitionDataInfo;

/**
 * Created by Simon Meng on 2018/1/10.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class ImageStitchingView extends View {
    private int mBitmapTotalWidth;

    private Bitmap mLeftBitmap, mRightBitmap;
    private Path mPath;

    private Rect mLeftBitmapRect = new Rect();
    private Rect mRightBitmapRect = new Rect();

    private Rect mLeftBitmapDstRect = new Rect();
    private Rect mRightBitmapDstRect = new Rect();

    private Paint mPaint, mBackgroundPaint, mBorderPaint;

    private int mBitmapOffset, mStretchWidth;

    public ImageStitchingView(Context context) {
        super(context);
    }

    private void initData() {
        mBitmapOffset = ShareData.PxToDpi_xhdpi(10);
        mStretchWidth = ShareData.PxToDpi_xhdpi(20);
        mBitmapTotalWidth = ShareData.PxToDpi_xhdpi(160) * 2 + mBitmapOffset;

        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(Color.WHITE);
        mBackgroundPaint.setDither(true);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(0xffffc433);
        mBorderPaint.setDither(true);
        mBorderPaint.setStrokeWidth(ShareData.PxToDpi_xhdpi(3));
    }



    private TransitionDataInfo mTransitionInfo;
    public void setTransitionInfo(TransitionDataInfo transitionDataInfo, String leftBitmapPath, String rightBitmapPath) {
        this.mTransitionInfo = transitionDataInfo;
        Bitmap tempLeft = BitmapFactory.decodeFile(leftBitmapPath);
        Bitmap tempRight = BitmapFactory.decodeFile(rightBitmapPath);
        int size = ShareData.PxToDpi_xhdpi(160);
        mLeftBitmap = MakeBmpV2.CreateFixBitmapV2(tempLeft, 0, 0, MakeBmpV2.POS_V_CENTER, size, size, Bitmap.Config.ARGB_8888);
        mRightBitmap = MakeBmpV2.CreateFixBitmapV2(tempRight, 0, 0, MakeBmpV2.POS_V_CENTER, size, size, Bitmap.Config.ARGB_8888);
        if(mLeftBitmap == null)
        {
            mLeftBitmap = ImageUtil.createBitmapByColor(0xff333333, size, size);
        }
        if(mRightBitmap == null)
        {
            mRightBitmap = ImageUtil.createBitmapByColor(0xff333333, size, size);
        }
        initData();
        invalidate();
    }

    public void setTransitionInfo(TransitionDataInfo transitionDataInfo) {
        this.mTransitionInfo = transitionDataInfo;
        invalidate();
    }

    private int mWidth, mHeight;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != 0 && h != 0 && mLeftBitmap != null && mRightBitmap != null) {
            mWidth = w;
            mHeight = h;

            mLeftBitmapRect.left = (mWidth - mBitmapTotalWidth) / 2;
            mLeftBitmapRect.top = (mHeight - mLeftBitmap.getHeight()) / 2;
            mLeftBitmapRect.right = mLeftBitmapRect.left + mLeftBitmap.getWidth();
            mLeftBitmapRect.bottom = mLeftBitmapRect.top + mLeftBitmap.getHeight();

            mLeftBitmapDstRect = new Rect(mLeftBitmapRect.left, mLeftBitmapRect.top, mLeftBitmapRect.right + mStretchWidth / 2, mLeftBitmapRect.bottom);

            mRightBitmapRect.right = (mWidth - mBitmapTotalWidth) / 2 + mBitmapTotalWidth;
            mRightBitmapRect.top = (mHeight - mRightBitmap.getHeight()) / 2;
            mRightBitmapRect.left = mRightBitmapRect.right - mRightBitmap.getWidth();
            mRightBitmapRect.bottom = mRightBitmapRect.top + mRightBitmap.getHeight();

            mRightBitmapDstRect = new Rect(mRightBitmapRect.left - mStretchWidth / 2, mRightBitmapRect.top, mRightBitmapRect.right, mRightBitmapRect.bottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mLeftBitmap == null || mRightBitmap == null)
            return;
        mPath.reset();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mTransitionInfo.mResSelected);

        int itemWidth = ShareData.PxToDpi_xhdpi(60);
        int itemHeight = ShareData.PxToDpi_xhdpi(80);
        int itemleft = (mWidth - itemWidth) / 2;
        int itemtop = (mHeight - itemHeight) / 2;
        int bitmapLeft = (mWidth - bitmap.getWidth()) / 2;
        int bitmapTop = (mHeight - bitmap.getHeight()) / 2;


        int rectLeft = itemleft;
        int rectTop = itemtop;
        int rectRight = rectLeft + itemWidth;
        int rectBottom = rectTop + itemHeight;
        Rect imageBackgroundRect = new Rect(rectLeft, rectTop, rectRight, rectBottom);

        if (mTransitionInfo.mID == TransitionItem.NONE) {
            canvas.drawBitmap(mLeftBitmap, mLeftBitmapRect.left, mLeftBitmapRect.top, mPaint);
            canvas.drawBitmap(mRightBitmap, mRightBitmapRect.left, mRightBitmapRect.top, mPaint);
        } else {
            canvas.save();
            mPath.moveTo(mLeftBitmapDstRect.right, mLeftBitmapDstRect.top);
            mPath.lineTo(mLeftBitmapDstRect.right - mStretchWidth, mLeftBitmapDstRect.bottom);
            mPath.lineTo(mLeftBitmapDstRect.right, mLeftBitmapDstRect.bottom);
            mPath.close();
            canvas.clipPath(mPath, Region.Op.DIFFERENCE);
            canvas.drawBitmap(mLeftBitmap, null, mLeftBitmapDstRect, mPaint);
            canvas.restore();

            canvas.save();
            mPath.reset();
            mPath.moveTo(mRightBitmapDstRect.left, mRightBitmapDstRect.top);
            mPath.lineTo(mRightBitmapDstRect.left, mRightBitmapDstRect.bottom);
            mPath.lineTo(mRightBitmapDstRect.left + mStretchWidth, mRightBitmapDstRect.top);
            mPath.close();
            canvas.clipPath(mPath, Region.Op.DIFFERENCE);
            canvas.drawBitmap(mRightBitmap, null, mRightBitmapDstRect, mPaint);
            canvas.restore();
        }
        // 填充
        canvas.drawRect(imageBackgroundRect, mBackgroundPaint);
        // 描边
        canvas.drawRect(imageBackgroundRect, mBorderPaint);
        // 画图
        canvas.drawBitmap(bitmap, bitmapLeft, bitmapTop, mPaint);
    }
}
