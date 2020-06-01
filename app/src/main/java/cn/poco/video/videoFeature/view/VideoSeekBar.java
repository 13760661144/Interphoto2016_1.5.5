package cn.poco.video.videoFeature.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.poco.interphoto2.R;
import cn.poco.video.VideoConfig;


/**
 * Created by admin on 2018/1/18.
 *
 */

public class VideoSeekBar extends FrameLayout {
    public VideoSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    public VideoSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public VideoSeekBar(Context context) {
        super(context);
        initialize(context);
    }

    public interface OnSeekBarChangeListener {
        /**
         *
         * @param seekBar
         * @param progress  0到1
         * @param fromUser
         */
        void onProgressChanged(VideoSeekBar seekBar, float progress, boolean fromUser);

        void onStartTrackingTouch(VideoSeekBar seekBar);

        void onStopTrackingTouch(VideoSeekBar seekBar);
    }

    private float mMinProcess;
    private float mMaxProcess;
    public void setTotalTime(int totalTime)
    {
        int offset = 4; //误差值
        mMinProcess = (VideoConfig.DURATION_LIMIT+offset) * 1.0f  /totalTime;
        mMaxProcess = (totalTime - VideoConfig.DURATION_LIMIT-offset ) * 1.0f/totalTime;
        setProgress(mMinProcess,false);
    }

    private LinearLayout mSliderLayout;
    private float mProgress = 0;
    private float mMax = 1f;
    private int mSliderWidth = 80;
    private boolean mTrackingTouch = false;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private ValueAnimator mRestoreAnimator;  //回弹
    private void initialize(Context context) {

        mRestoreAnimator = new ValueAnimator();
        mRestoreAnimator.setDuration(150);
        mRestoreAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                float f = (float) animation.getAnimatedValue();
                setProgress(f,true);
            }
        });

        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        FrameLayout c = new FrameLayout(context);
        addView(c, lParams);

        LayoutParams params = new LayoutParams((int) (mSliderWidth * 1.5), LayoutParams.WRAP_CONTENT);
        mSliderLayout = new LinearLayout(getContext());
        mSliderLayout.setOrientation(LinearLayout.VERTICAL);
        c.addView(mSliderLayout, params);

        lParams = new LinearLayout.LayoutParams(mSliderWidth, LayoutParams.WRAP_CONTENT);
        lParams.leftMargin = mSliderWidth / 2;
        ImageView mSliderTop = new ImageView(context);
        mSliderTop.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mSliderLayout.addView(mSliderTop, lParams);
        mSliderTop.setBackgroundResource(R.drawable.video_previewsetting_slider_top);
        mSliderTop.setVisibility(VISIBLE);

        lParams = new LinearLayout.LayoutParams(mSliderWidth, LayoutParams.MATCH_PARENT);
        View mSlider = new View(context);
        mSliderLayout.addView(mSlider, lParams);
        mSlider.setBackgroundResource(R.drawable.video_previewsetting_slider);
        mSlider.setVisibility(VISIBLE);
        setClickable(true);
    }

    private float getCorrectProgress(float progress)
    {
        if (progress < mMinProcess)
        {
            progress = mMinProcess;
        }
        if (progress > mMaxProcess)
        {
            progress = mMaxProcess;
        }
        return progress;
    }

    public float getProgress() {
        return getCorrectProgress(mProgress);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mOnSeekBarChangeListener = listener;
    }

    private void setProgress(float progress, boolean fromUser) {
        if(!fromUser)
        {
            progress = getCorrectProgress(progress);
        }


//        double r = (double) progress / (double) mMax;
//        if (r > 0.98) {
//            progress = mMax;
//        }
//        if (r < 0.02) {
//            progress = 0;
//        }

        if (mProgress != progress) {
            mProgress = progress;
            updateProgress(fromUser);
        }
    }

    private void updateProgress(boolean fromUser) {
//        int paddingLeft = getPaddingLeft() + mSliderWidth;
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int w = getWidth() - paddingLeft - paddingRight - mSliderWidth;
        if (w > 0 && mMax > 0) {
            LayoutParams params = (LayoutParams) mSliderLayout.getLayoutParams();
//            params.leftMargin = (int) ((w - mSliderWidth / 2) *mProgress);
            params.leftMargin = (int) (w *mProgress);
            mSliderLayout.setLayoutParams(params);
        }
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, getCorrectProgress(mProgress), fromUser);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        int x = (int) ev.getX();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int w = getWidth() - paddingLeft - paddingRight - mSliderWidth;
        if (x < paddingLeft) {
            x = paddingLeft;
        }
        if (x > w + paddingLeft) {
            x = w + paddingLeft;
        }
        x -= paddingLeft;
        if (action == MotionEvent.ACTION_DOWN) {
            mRestoreAnimator.cancel();
            float progress = mMax * ((float) x / (float) w);
            if (progress != mProgress) {
                if (!mTrackingTouch) {
                    mTrackingTouch = true;
                    if (mOnSeekBarChangeListener != null) {
                        mOnSeekBarChangeListener.onStartTrackingTouch(this);
                    }
                }
                setProgress(progress, true);
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            float progress = mMax * ((float) x / (float) w);
            if (progress != mProgress) {
                if (!mTrackingTouch) {
                    mTrackingTouch = true;
                    if (mOnSeekBarChangeListener != null) {
                        mOnSeekBarChangeListener.onStartTrackingTouch(this);
                    }
                }
                setProgress(progress, true);
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if(mProgress != getCorrectProgress(mProgress)){
                mRestoreAnimator.setFloatValues(mProgress,getCorrectProgress(mProgress));
                mRestoreAnimator.start();
            }
            mTrackingTouch = false;
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onStopTrackingTouch(this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateProgress(false);
        super.onSizeChanged(w, h, oldw, oldh);
    }
}

//public class VideoSeekBar extends FrameLayout {
//    public VideoSeekBar(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        initialize(context);
//    }
//
//    public VideoSeekBar(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        initialize(context);
//    }
//
//    public VideoSeekBar(Context context) {
//        super(context);
//        initialize(context);
//    }
//
//    public interface OnSeekBarChangeListener {
//        /**
//         *
//         * @param seekBar
//         * @param progress  0到1
//         * @param fromUser
//         */
//        void onProgressChanged(VideoSeekBar seekBar, float progress, boolean fromUser);
//
//        void onStartTrackingTouch(VideoSeekBar seekBar);
//
//        void onStopTrackingTouch(VideoSeekBar seekBar);
//    }
//
//    private float mMinProcess;
//    private float mMaxProcess;
//    public void setTotalTime(int totalTime)
//    {
//        int offset = 4; //误差值
//        mMinProcess = (VideoConfig.DURATION_LIMIT+offset) * 1.0f  /totalTime;
//        mMaxProcess = (totalTime - VideoConfig.DURATION_LIMIT-offset ) * 1.0f/totalTime;
//        setProgress(mMinProcess,false);
//    }
//
//    private FrameLayout mSliderLayout;
//    private float mProgress = 0;
//    private float mMax = 1f;
//    private int mSliderWidth = 80;
//    private boolean mTrackingTouch = false;
//    private OnSeekBarChangeListener mOnSeekBarChangeListener;
//
//    private void initialize(Context context) {
//
//        LayoutParams lParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        FrameLayout c = new FrameLayout(context);
//        addView(c, lParams);
//
//        LayoutParams params = new LayoutParams((int) (mSliderWidth * 1.5), LayoutParams.WRAP_CONTENT);
//        mSliderLayout = new FrameLayout(getContext());
////        mSliderLayout.setOrientation(VERTICAL);
//        c.addView(mSliderLayout, params);
//
//        lParams = new LayoutParams(mSliderWidth, LayoutParams.WRAP_CONTENT);
//        lParams.leftMargin = mSliderWidth / 2;
//        ImageView mSliderTop = new ImageView(context);
//        mSliderTop.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//        mSliderLayout.addView(mSliderTop, lParams);
//        mSliderTop.setImageResource(R.drawable.video_previewsetting_slider_top);
//        mSliderTop.setVisibility(VISIBLE);
//
//        lParams = new LayoutParams(mSliderWidth, LayoutParams.MATCH_PARENT);
//        lParams.topMargin = ShareData.PxToDpi_xhdpi(40);
//        ImageView mSlider = new ImageView(context);
//        mSliderLayout.addView(mSlider, lParams);
//        mSlider.setImageResource(R.drawable.video_previewsetting_slider);
//        mSlider.setVisibility(VISIBLE);
//        setClickable(true);
//    }
//
//    public float getProgress() {
//        return mProgress;
//    }
//
//    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
//        mOnSeekBarChangeListener = listener;
//    }
//
//    private void setProgress(float progress, boolean fromUser) {
//        if (progress < mMinProcess)
//        {
//            progress = mMinProcess;
//        }
//        if (progress > mMaxProcess)
//        {
//            progress = mMaxProcess;
//        }
//
//
////        double r = (double) progress / (double) mMax;
////        if (r > 0.98) {
////            progress = mMax;
////        }
////        if (r < 0.02) {
////            progress = 0;
////        }
//
//        if (mProgress != progress) {
//            mProgress = progress;
//            updateProgress(fromUser);
//        }
//    }
//
//    private void updateProgress(boolean fromUser) {
//        int paddingLeft = getPaddingLeft() + mSliderWidth;
//        int paddingRight = getPaddingRight();
//        int w = getWidth() - paddingLeft - paddingRight;
//        if (w > 0 && mMax > 0) {
//            LayoutParams params = (LayoutParams) mSliderLayout.getLayoutParams();
//            params.leftMargin = (int) ((w - mSliderWidth / 2) *mProgress);
//            mSliderLayout.setLayoutParams(params);
//        }
//        if (mOnSeekBarChangeListener != null) {
//            mOnSeekBarChangeListener.onProgressChanged(this, mProgress, fromUser);
//        }
//    }
//
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        int action = ev.getAction();
//        int x = (int) ev.getX();
//        int paddingLeft = getPaddingLeft();
//        int paddingRight = getPaddingRight();
//        int w = getWidth() - paddingLeft - paddingRight;
//        if (x < paddingLeft) {
//            x = paddingLeft;
//        }
//        if (x > w + paddingLeft) {
//            x = w + paddingLeft;
//        }
//        x -= paddingLeft;
//        if (action == MotionEvent.ACTION_DOWN) {
//            float progress = mMax * ((float) x / (float) w);
//            if (progress != mProgress) {
//                if (!mTrackingTouch) {
//                    mTrackingTouch = true;
//                    if (mOnSeekBarChangeListener != null) {
//                        mOnSeekBarChangeListener.onStartTrackingTouch(this);
//                    }
//                }
//                setProgress(progress, true);
//            }
//        } else if (action == MotionEvent.ACTION_MOVE) {
//            float progress = mMax * ((float) x / (float) w);
//            if (progress != mProgress) {
//                if (!mTrackingTouch) {
//                    mTrackingTouch = true;
//                    if (mOnSeekBarChangeListener != null) {
//                        mOnSeekBarChangeListener.onStartTrackingTouch(this);
//                    }
//                }
//                setProgress(progress, true);
//            }
//        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
//            mTrackingTouch = false;
//            if (mOnSeekBarChangeListener != null) {
//                mOnSeekBarChangeListener.onStopTrackingTouch(this);
//            }
//        }
//        return super.dispatchTouchEvent(ev);
//    }
//
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        updateProgress(false);
//        super.onSizeChanged(w, h, oldw, oldh);
//    }
//}
//

