package cn.poco.video.videoMusic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by lgd on 2017/8/23.
 */

public class SelectVolumeView extends FrameLayout {
    private ClipHorizontalScrollView mScrollView;  //滚动区域的父布局
    private int emptyViewW;
    private VolumeLineView mVolumeLineView;   //线段的view
    private int bkColor;
    private int mVolume = 60;          // 使用整形方便计算，  0到100
    public final static int Thick_LINE_W = ShareData.PxToDpi_xhdpi(4);    //粗线段的宽
    public final static int Thick_LINE_H = ShareData.PxToDpi_xhdpi(20);     //粗线段的高
    private TextView mVolumeTip;       //音量提示
    private ImageView mStrengthenBtn;   //+图标
    private ImageView mWeakenBtn;      //-图标

    public SelectVolumeView(@NonNull Context context, int bkColor, float volume) {
        super(context);
        this.bkColor = bkColor;
        this.mVolume = (int) (volume * 100);
        init();
    }

    private void init() {
        LayoutParams params;
        emptyViewW = (ShareData.m_screenWidth - Thick_LINE_W) / 2;
        int bottomMargin = ShareData.PxToDpi_xhdpi(50);

        mScrollView = new ClipHorizontalScrollView(getContext());
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(100));
        params.gravity = Gravity.BOTTOM;
        params.bottomMargin = bottomMargin - ShareData.PxToDpi_xhdpi(100) / 2 + Thick_LINE_H / 2;
        addView(mScrollView, params);
        {
            LinearLayout parent = new LinearLayout(getContext());
            parent.setOrientation(LinearLayout.HORIZONTAL);
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mScrollView.addView(parent, params);
            {
                LinearLayout.LayoutParams params1;
                View leftView = new View(getContext());
                params1 = new LinearLayout.LayoutParams(emptyViewW, ViewGroup.LayoutParams.MATCH_PARENT);
                parent.addView(leftView, params1);

                mVolumeLineView = new VolumeLineView(getContext(), 50);
                LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mVolumeLineView.setLayoutParams(layoutParams);
                parent.addView(mVolumeLineView);

                View rightView = new View(getContext());
                params1 = new LinearLayout.LayoutParams(emptyViewW, ViewGroup.LayoutParams.MATCH_PARENT);
                parent.addView(rightView, params1);
            }
        }
        mScrollView.setScrollViewListener(mScrollViewListener);
        mScrollView.setHorizontalScrollBarEnabled(false);

        //中间黄色的线段
        View centerLine = new View(getContext());
        centerLine.setBackgroundColor(0xffffc433);
        params = new LayoutParams(Thick_LINE_W, Thick_LINE_H);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = bottomMargin;
        addView(centerLine, params);

        // 渐变遮罩层
        GradientView leftMask = new GradientView(getContext(), bkColor, getAlphaColor(bkColor, 0.1f));
        params = new LayoutParams(emptyViewW, Thick_LINE_H);
        params.gravity = Gravity.BOTTOM | Gravity.LEFT;
        params.bottomMargin = bottomMargin;
        addView(leftMask, params);

        // 渐变遮罩层
        GradientView rightMask = new GradientView(getContext(), getAlphaColor(bkColor, 0.1f), bkColor);
        params = new LayoutParams(emptyViewW, Thick_LINE_H);
        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        params.bottomMargin = bottomMargin;
        addView(rightMask, params);

        FrameLayout parent = new FrameLayout(getContext());
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        params.bottomMargin = bottomMargin + ShareData.PxToDpi_xhdpi(30);
        addView(parent, params);
        {
            int padding = ShareData.PxToDpi_xhdpi(20);
            mWeakenBtn = new ImageView(getContext());
            mWeakenBtn.setPadding(padding, padding, padding, padding);
            mWeakenBtn.setImageResource(R.drawable.video_music_edit_weaken);
            mWeakenBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mVolume > 0) {
                        mVolume -= 5;
                        if (mVolume < 0) {
                            mVolume = 0;
                        }
                        updateVolume();
                        if (mOnCallBack != null) {
                            mOnCallBack.onClick(mVolume * 1.0f / 100);
                        }
                    }
                }
            });
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
            params.leftMargin = ShareData.PxToDpi_xhdpi(40);
            parent.addView(mWeakenBtn, params);

            mStrengthenBtn = new ImageView(getContext());
            mStrengthenBtn.setPadding(padding, padding, padding, padding);
            mStrengthenBtn.setImageResource(R.drawable.video_music_edit_strengthen);
            mStrengthenBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mVolume < 100) {
                        mVolume += 5;
                        if (mVolume > 100) {
                            mVolume = 100;
                        }
                        updateVolume();
                        if (mOnCallBack != null) {
                            mOnCallBack.onClick(mVolume * 1.0f / 100);
                        }
                    }
                }
            });
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            params.rightMargin = ShareData.PxToDpi_xhdpi(40);
            parent.addView(mStrengthenBtn, params);

            mVolumeTip = new TextView(getContext());
            mVolumeTip.setText(String.valueOf(mVolume));
            mVolumeTip.setTextColor(0xffffc433);
            mVolumeTip.getPaint().setFakeBoldText(true);
            mVolumeTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            mVolumeTip.setMinHeight(ShareData.PxToDpi_xhdpi(25));
            mVolumeTip.setGravity(Gravity.CENTER);
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            parent.addView(mVolumeTip, params);
        }

        mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                updateVolume(false);
            }
        });
    }

    /**
     * 获取透明度后的颜色
     *
     * @param color
     * @param alpha
     * @return
     */
    public int getAlphaColor(int color, float alpha) {
        if (alpha == 1.0f) {
            return color;
        } else {
            return (color & 0xffffff) | (((int) (255 * alpha)) << 24);
        }
    }

    public void updateVolume() {
        updateVolume(true);
    }

    /**
     * 更新音量和滚动位置
     *
     * @param isSmooth
     */
    private void updateVolume(boolean isSmooth) {
        int startX = mVolumeLineView.getStartX(mVolume);
        mVolumeTip.setText(String.valueOf(mVolume + "%"));
        if (isSmooth) {
            mScrollView.smoothScrollBy(startX - mScrollView.getScrollX(), 0);
        } else {
            mScrollView.scrollTo(startX, 0);
        }
        if (mVolume == 0) {
            mWeakenBtn.setVisibility(View.GONE);
            mStrengthenBtn.setVisibility(View.VISIBLE);
        } else if (mVolume == 100) {
            mStrengthenBtn.setVisibility(View.GONE);
            mWeakenBtn.setVisibility(View.VISIBLE);
        } else {
            mStrengthenBtn.setVisibility(View.VISIBLE);
            mWeakenBtn.setVisibility(View.VISIBLE);
        }
    }

    private static final String TAG = "SelectVolumeView";
    private ClipHorizontalScrollView.ScrollViewListener mScrollViewListener = new ClipHorizontalScrollView.ScrollViewListener() {

        @Override
        public void onScrollChanged(ClipHorizontalScrollView.ScrollType scrollType, int scrollX) {

            //根据滚动的位置计算音量
            mVolume = mVolumeLineView.getIndex(scrollX);
            if (scrollType == ClipHorizontalScrollView.ScrollType.IDLE) {
                updateVolume();
                if (mOnCallBack != null) {
                    mOnCallBack.onStop(mVolume * 1.0f / 100);
                }
            } else {
                if (mOnCallBack != null) {
                    mOnCallBack.onScroll(mVolume * 1.0f / 100);
                }
                mVolumeTip.setText(String.valueOf(mVolume + "%"));
            }
        }
    };

    private class GradientView extends View {
        private Paint paint;
        private int color1;
        private int color2;

        public GradientView(Context context, int color1, int color2) {
            super(context);
            this.color1 = color1;
            this.color2 = color2;
            paint = new Paint();
            paint.setAntiAlias(true);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            paint.setShader(new LinearGradient(0, 0, w, h, color1, color2, Shader.TileMode.CLAMP));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        }
    }


    public class VolumeLineView extends View {

        private Paint mThickPaint;   //粗线段画笔
        private Paint mFinePaint;   //细线段画笔
        private int mSpan;        //线段间隔
        private int mThickH;
        private int mThickW;
        private int mFineH;
        private int mFineW;
        private int mPeriod = 5;    //周期是5
        private final int MAX_NUM = 100;  //最大音量为100

        public VolumeLineView(Context context, int span) {
            super(context);
            this.mSpan = span;
            init();
        }

        private void init() {
            mThickW = Thick_LINE_W;
            mThickH = Thick_LINE_H;
            mFineH = ShareData.PxToDpi_xhdpi(10);
            mFineW = ShareData.PxToDpi_xhdpi(2);

            mThickPaint = new Paint();
            mThickPaint.setColor(Color.WHITE);
            mThickPaint.setAntiAlias(true);

            mFinePaint = new Paint();
            mFinePaint.setAntiAlias(true);
            mFinePaint.setColor(0xff999999);
            mSpan = ShareData.PxToDpi_xhdpi(20);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

            int spanW = MAX_NUM * mSpan;
            int fineLineW = (MAX_NUM - MAX_NUM / mPeriod) * mFineW;
            int thickLineW = (MAX_NUM / mPeriod + 1) * mThickW;
            int w = +spanW + fineLineW + thickLineW;

            int specMode = MeasureSpec.getMode(widthMeasureSpec);
            int specSize = MeasureSpec.getSize(widthMeasureSpec);
            switch (specMode) {
                case MeasureSpec.AT_MOST:
                    break;
                case MeasureSpec.EXACTLY:
                    w = specSize;
                    break;
                case MeasureSpec.UNSPECIFIED:
                    w = Math.max(w, specSize);
                    break;
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), heightMeasureSpec);

        }

        /**
         * 画线段 ，  1 ，5 ，10 。。 画粗线段， 其他画细线段
         * @param canvas
         */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int curIndex = 0;
            int startX = 0;
            while (curIndex <= MAX_NUM) {
                if (curIndex % mPeriod == 0) {
                    canvas.drawRect(startX, (getHeight() - mThickH) / 2, startX + mThickW, (getHeight() + mThickH) / 2, mThickPaint);
                    startX += mSpan + mThickW;
                } else {
                    canvas.drawRect(startX, (getHeight() - mFineH) / 2, startX + mFineW, (getHeight() + mFineH) / 2, mFinePaint);
                    startX += mSpan + mFineW;
                }
                curIndex++;
            }
        }

        /**
         * ]
         *
         * @param startX 对应的线段的左上角坐标
         * @return 音量的大小
         */
        public int getIndex(int startX) {
            int index = 0;
            int spanW = mPeriod * mSpan;
            int fineLineW = (mPeriod - 1) * mFineW;
            int periodW = spanW + fineLineW + mThickW;

            int period = startX / periodW;
            int offsetX = startX % periodW;

            if (offsetX >= mSpan / 2 + mThickW && offsetX <= periodW - mSpan / 2) {
                index += (offsetX - mSpan / 2 - mThickW) / (mFineW + mSpan) + 1;
            } else if (offsetX > periodW - (mSpan + mThickW / 2)) {
                index += mPeriod;
            }
            index += period * mPeriod;

            return index;
        }

        /**
         * @param index 音量的大小
         * @return 计算对应的线段的左上角坐标
         */
        public int getStartX(int index) {
            int w = 0;
            int spanW = index * mSpan;
            int fineLineW = 0;
            if (index > 0) {
                fineLineW = (index - (index - 1) / mPeriod - 1) * mFineW;
            }
            int thickLineW = ((index + mPeriod - 1) / mPeriod) * mThickW;
            w += spanW + fineLineW + thickLineW;
            return w;
        }
    }

    private OnCallBack mOnCallBack;

    public void setOnCallBack(OnCallBack onCallBack) {
        this.mOnCallBack = onCallBack;
    }

    public interface OnCallBack {
        void onStop(float volume);

        void onScroll(float volume);

        void onClick(float volume);
    }

    public void setVolume(float volume) {
        mVolume = (int) (volume * 100);
        updateVolume(false);
    }
}
