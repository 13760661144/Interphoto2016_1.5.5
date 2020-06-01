package cn.poco.video.videoAlbum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import cn.poco.tianutils.ShareData;
import cn.poco.video.helper.TimeFormatter;
import cn.poco.video.model.VideoEntry;


/**
 * Created by Shine on 2017/5/15.
 */

public class VideoCell extends FrameLayout{
    private ImageView mCoverImage;
    private TextView mDuration;
    private Context mContext;
    private VideoEntry mVideoData;
    private TextView mOrder;

    private int mDurationRightPadding, mDurationBottomPadding;
    private boolean mDrawDisableLayer;
    private FrameLayout mOrderParent;

    public VideoCell(Context context) {
        super(context);
        mContext = context;
        initData();
        initView();
        setWillNotDraw(false);
    }


    private void initData() {
        mDurationRightPadding = 10;
        mDurationBottomPadding = 10;
    }

    private void initView() {
        mCoverImage = new ImageView(mContext) {

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                int size = getMeasuredWidth();
                setMeasuredDimension(size, size);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (this.getDrawable() != null) {
                    if (mDrawDisableLayer) {
                        canvas.drawColor(0x80000000);
                    } else {
                        canvas.drawColor(Color.TRANSPARENT);
                    }
                }
            }
        };

        mCoverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mCoverImage.setLayoutParams(params);
        this.addView(mCoverImage);

        mDuration = new TextView(mContext);
        mDuration.setSingleLine(true);
        mDuration.setMaxLines(1);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.BOTTOM);
        params.rightMargin = mDurationRightPadding;
        params.bottomMargin = mDurationBottomPadding;
        mDuration.setLayoutParams(params);
        this.addView(mDuration);


        mOrderParent = new FrameLayout(getContext());
        params = new LayoutParams(ShareData.PxToDpi_xhdpi(90),ShareData.PxToDpi_xhdpi(90));
        addView(mOrderParent,params);
        {
            mOrder = new TextView(getContext());
            mOrder.setBackgroundDrawable(new UnSelectedDrawable());
            mOrder.setGravity(Gravity.CENTER);
            mOrder.setMinWidth(ShareData.PxToDpi_xhdpi(44));
            mOrder.setMinHeight(ShareData.PxToDpi_xhdpi(44));
            mOrder.setTextColor(Color.WHITE);
            mOrder.setTextSize(TypedValue.COMPLEX_UNIT_DIP,10f);
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP);
            params.leftMargin = ShareData.PxToDpi_xhdpi(16);
            params.topMargin = ShareData.PxToDpi_xhdpi(26);
            mOrderParent.addView(mOrder,params);
        }



//        post(new Runnable() {
//            @Override
//            public void run() {
//                Rect bounds = new Rect();
//                mOrder.setEnabled(true);
//                mOrder.getHitRect(bounds);
//                bounds.top = 0;
//                bounds.bottom = ShareData.PxToDpi_xhdpi(90);
//                bounds.left = 0;
//                bounds.right = ShareData.PxToDpi_xhdpi(90);
//                TouchDelegate touchDelegate = new TouchDelegate(bounds, mOrder);
//                VideoCell.this.setTouchDelegate(touchDelegate);
//            }
//        });

    }


    public void setVideoData(final VideoEntry video) {
        if (video != null) {
            this.mVideoData = video;
            if (mVideoData.mDuration >= 1000 * 181)
            {
                mDrawDisableLayer = true;
            } else {
                mDrawDisableLayer = false;
            }
            mCoverImage.invalidate();
            Glide.with(mContext).load(mVideoData.mMediaPath).dontAnimate().diskCacheStrategy(DiskCacheStrategy.RESULT).into(mCoverImage);
            mDuration.setText(TimeFormatter.toVideoDurationFormat(mVideoData.mDuration));
        }
    }

    public VideoEntry getVideoData() {
        return mVideoData;
    }

    public void setJointOrder(int order)
    {
        if(order == 0){
            mOrder.setBackgroundDrawable(new UnSelectedDrawable());
            mOrder.setText("");
        }else{
            mOrder.setBackgroundDrawable(new SelectedDrawable());
            mOrder.setText(String.valueOf(order));
        }
    }

    class SelectedDrawable extends Drawable{
        private Paint mPaintCircle;
        private int centerX;
        private int centerY;
        private int radius;
        public SelectedDrawable()
        {
            super();
            mPaintCircle = new Paint();
            mPaintCircle.setColor(0xffFFC433);
            mPaintCircle.setAntiAlias(true);
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom)
        {
            super.setBounds(left, top, right, bottom);
            centerX = (right + left)/2;
            centerY = (bottom + top)/2;
            radius = Math.min((right - left)/2,(bottom - top)/2);
        }

        @Override
        public void draw(@NonNull Canvas canvas)
        {
            canvas.drawCircle(centerX,centerY,radius,mPaintCircle);
        }

        @Override
        public void setAlpha(@IntRange(from = 0, to = 255) int alpha)
        {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter)
        {

        }

        @Override
        public int getOpacity()
        {
            return 0;
        }
    }

    class UnSelectedDrawable extends Drawable{
        private Paint mPaintRing;
        private Paint mPaintCircle;
        private int centerX;
        private int centerY;
        private int radius;
        private int strokeW;
        public UnSelectedDrawable()
        {
            super();
            mPaintRing = new Paint();
            mPaintRing.setColor(0x99ffffff);
            mPaintRing.setAntiAlias(true);
            mPaintRing.setStyle(Paint.Style.STROKE);
            strokeW = ShareData.PxToDpi_xhdpi(1);
            mPaintRing.setStrokeWidth(strokeW);
            mPaintCircle = new Paint();
            mPaintCircle.setColor(0x66000000);
            mPaintCircle.setAntiAlias(true);
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom)
        {
            super.setBounds(left, top, right, bottom);
            centerX = (right + left)/2;
            centerY = (bottom + top)/2;
            radius = Math.min((right - left)/2,(bottom - top)/2);
        }

        @Override
        public void draw(@NonNull Canvas canvas)
        {
            canvas.drawCircle(centerX,centerY,radius-strokeW,mPaintCircle);
            canvas.drawCircle(centerX,centerY,radius-(strokeW*1f/2+0.5f),mPaintRing);
        }

        @Override
        public void setAlpha(@IntRange(from = 0, to = 255) int alpha)
        {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter)
        {

        }

        @Override
        public int getOpacity()
        {
            return 0;
        }
    }

    public View getOrderView()
    {
        return mOrderParent;
    }
}
