package cn.poco.beautify.animations;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import cn.poco.image.PocoImageInfo;
import cn.poco.image.filter;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by pengdh on 2016/7/12.
 */

public class BightAnim extends FrameLayout
{

    private BethelAnim mBethelAnim;
    private BightCircle mCircle;
    private PointF mStartPoint = new PointF();
    private PointF mEndPoint = new PointF();
    private int mWidth;
    private int mHeight;
    private Bitmap m_bkBmp;
    public BightAnim(Context context) {
        super(context);
        Init();
    }

    public BightAnim(Context context, AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public BightAnim(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init();
    }


    public void Init()
    {
        m_bkBmp = BitmapFactory.decodeResource(getResources(), R.drawable.interphoto_bight_anim_ori);
        BightAnim.this.setBackgroundDrawable(new BitmapDrawable(m_bkBmp.copy(Bitmap.Config.ARGB_8888,true)));
        mBethelAnim = new BethelAnim(this.getContext());
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mBethelAnim.setLayoutParams(fl);
        this.addView(mBethelAnim);
//        this.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mCircle.initFirstAnimData(mStartPoint,mEndPoint);
//                startAnim();
//            }
//        },500);

        mCircle = new BightCircle(this.getContext());
        fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(62),ShareData.PxToDpi_xhdpi(62));
        mCircle.setLayoutParams(fl);
        this.addView(mCircle);

        mCircle.setUpdateCallBack(new UpdateCallBack() {
            @Override
            public void updateView(float x, float y,boolean isShow) {
                mBethelAnim.updateUI(x,y,isShow);
            }

            @Override
            public void updateView1(float x, float y, boolean isShowControlPoint, float value) {
                mBethelAnim.updateUI2(x,y,isShowControlPoint,value);
            }

            @Override
            public void updataeViewBack(float x, float y, boolean isShowControlPoint, float value) {

            }

            @Override
            public void showOnce(boolean isshow,float x, float y) {
                mBethelAnim.ShowOnce(isshow,x,y);
            }

            @Override
            public void setControlPoint(boolean isshow) {
                mBethelAnim.setControlPointShow(isshow);
            }

        });
    }

    boolean test = true;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(changed)
        {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
            mStartPoint = new PointF();
            mStartPoint.x = mWidth/2 - ShareData.PxToDpi_xhdpi(62)/2f;
            mStartPoint.y = mHeight*3/4f - ShareData.PxToDpi_xhdpi(62)/2f;

            mEndPoint.x = mWidth/4f + mWidth/8f;
            mEndPoint.y = mHeight/2f + mHeight/8f- ShareData.PxToDpi_xhdpi(62)/2f;
//            this.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mCircle.startFirstAnim(mStartPoint,mEndPoint);
//                }
//            },300);
            mCircle.initFirstAnimData(mStartPoint,mEndPoint);
            mCircle.startFirstAnim(mStartPoint,mEndPoint);
        }


    }

    public void startAnim()
    {
        if(mCircle != null)
        {
            mCircle.startFirstAnim(mStartPoint,mEndPoint);
        }
    }

    public void ClearAll()
    {
        if(mCircle != null)
        {
            mCircle.ClearAll();
            mCircle.clearAnimation();
        }
        if(mBethelAnim != null)
        {
            mBethelAnim.ClearAll();
        }
        this.setBackgroundDrawable(null);
    }

     class BethelAnim extends View {

        private Paint mPaint;
        private Paint mCiclePaint;
        private Paint mBlackPaint;
         private Paint mWhitePaint;
         private Paint mWhitePaint1;
        private int mWidth;
        private int mHeight;
        private int m_arr[];
        private float m_contolArr[];
         private float m_contolArrF[];
        private Path mPath ;
        private int mRadio = ShareData.PxToDpi_xhdpi(12);
        private int mRadio2 = ShareData.PxToDpi_xhdpi(7);
        private boolean isShowControlPoint = false;
         private int mControlX;
         private int mControlY;

        private boolean showOnce = false;

        public BethelAnim(Context context) {
            super(context);
            init();
        }

         public BethelAnim(Context context, AttributeSet attrs) {
             super(context, attrs);
             Init();
         }

         public BethelAnim(Context context, AttributeSet attrs, int defStyleAttr) {
             super(context, attrs, defStyleAttr);
             Init();
         }


        public void init()
        {
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(ShareData.PxToDpi_xhdpi(4));
            mPaint.setColor(Color.WHITE);
//            mPaint.setStrokeWidth(ShareData.PxToDpi_xhdpi(10));
            mPaint.setAntiAlias(true);
            m_contolArr = new float[8];
            m_contolArrF = new float[6];
            mPath = new Path();

            mCiclePaint = new Paint();
//            mCiclePaint.setStyle(Paint.Style.STROKE);
//            mCiclePaint.setStrokeWidth(3);
            mCiclePaint.setColor(Color.WHITE);
            mCiclePaint.setAntiAlias(true);

            mBlackPaint = new Paint();
            mBlackPaint.setColor(Color.BLACK);
            mBlackPaint.setAntiAlias(true);
            mBlackPaint.setStyle(Paint.Style.STROKE);
            mBlackPaint.setStrokeWidth(ShareData.PxToDpi_xhdpi(4));
//            this.setBackgroundColor(Color.BLACK);




            mWhitePaint = new Paint();
            mWhitePaint.setColor(Color.WHITE);
            mWhitePaint.setAntiAlias(true);
//            mWhitePaint.setStrokeWidth(ShareData.PxToDpi_xhdpi(4));

            mWhitePaint1 = new Paint();
            mWhitePaint1.setColor(Color.WHITE);
            mWhitePaint1.setAntiAlias(true);
            mWhitePaint1.setStyle(Paint.Style.STROKE);
            mWhitePaint1.setStrokeWidth(ShareData.PxToDpi_xhdpi(2));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            if(changed)
            {
                mWidth = getMeasuredWidth();
                mHeight = getMeasuredHeight();
                m_arr = new int[mWidth*2];
                m_contolArr[0] = 0f + mRadio;
                m_contolArr[1] = mHeight - 0.5f - mRadio;
                m_contolArr[4] = mWidth/2f;
                m_contolArr[5] = mHeight/2f;
                m_contolArr[6] = mWidth - 0.5f - mRadio;
                m_contolArr[7] = 0f + mRadio;
                m_contolArr[2] = m_contolArr[4]/2f;
                m_contolArr[3] = mHeight*3/4f;
                transformToRatio();
            }
        }

        public void transformToRatio()
         {
             int width = getMeasuredWidth();
             int  height = getMeasuredHeight() + ShareData.PxToDpi_xhdpi(100);
             m_arr = new int[mWidth*2];
             m_contolArrF[0] = 0f + mRadio;
             m_contolArrF[1] = height - 0.5f - mRadio;
//             m_contolArrF[4] = width/2f;
//             m_contolArrF[5] = height/2f;
             m_contolArrF[4] = width - 0.5f - mRadio;
             m_contolArrF[5] = 0f + mRadio;
             m_contolArrF[2] = m_contolArr[4]/2f;
             m_contolArrF[3] = height*3/4f;

             //转换成比例
//             m_contolArrF[0] = m_contolArrF[0]/(width + 0.5f);
//             m_contolArrF[1] = m_contolArrF[1]/(height + 0.5f);
//             m_contolArrF[2] = m_contolArrF[2]/(width + 0.5f);
//             m_contolArrF[3] = m_contolArrF[3]/(height + 0.5f);
////             m_contolArrF[4] = m_contolArrF[4]/(width + 0.5f);
////             m_contolArrF[5] = m_contolArrF[5]/(height + 0.5f);
//             m_contolArrF[4] = m_contolArrF[4]/(width + 0.5f);
//             m_contolArrF[5] = m_contolArrF[5]/(height + 0.5f);

             m_contolArrF[0] = 0f;
             m_contolArrF[1] = 0f;
            m_contolArrF[2] = 0.5f;
             m_contolArrF[3] = 0.5f;
//             m_contolArrF[4] = m_contolArrF[4]/(width + 0.5f);
//             m_contolArrF[5] = m_contolArrF[5]/(height + 0.5f);
             m_contolArrF[4] = 1f;
             m_contolArrF[5] = 1f;
         }

        public void updateUI(float x, float y,boolean isshow)
        {
            isShowControlPoint = isshow;
            m_contolArr[2] = x * BightAnim.this.mWidth;
            m_contolArr[3] = y * BightAnim.this.mHeight;
            this.invalidate();
        }

         Bitmap temBmp = null;
         Bitmap temp = null;

         public void updateUI2(float x, float y,boolean isshow,float value)
         {
             m_contolArrF[2] = 0.5f - (0.2f*value);
             m_contolArrF[3] = 0.5f + (0.2f*value);
             if(value == 1)
             {
                 isShowControlPoint = false;
             }

             isShowControlPoint = isshow;
             mControlX = (int) (x * BightAnim.this.mWidth);
             mControlY = (int) (y * BightAnim.this.mHeight);
             m_contolArr[2] = x * BightAnim.this.mWidth - ShareData.PxToDpi_xhdpi(60)*value;
             m_contolArr[3] = y * BightAnim.this.mHeight - ShareData.PxToDpi_xhdpi(100)*value;
             this.invalidate();
             if(temBmp != null)
             {
                 temBmp.recycle();
                 temBmp = null;
             }

             if(temp != null)
             {
                 temp.recycle();
                 temp = null;
             }
             BightAnim.this.setBackgroundDrawable(null);
             temBmp = m_bkBmp.copy(Bitmap.Config.ARGB_8888,true);
             temp = filter.AdjustCurve(temBmp,PocoImageInfo.ChannelType.AllChannels,m_contolArrF,3);
             BightAnim.this.setBackgroundDrawable(new BitmapDrawable(temp));
//             m_bkBmp = temp;
         }


        public void ShowOnce(boolean isShowonce,float x,float y)
        {
            if(isShowonce)
            {
                showOnce = true;
                m_contolArr[2] = x;
                m_contolArr[3] = y;
                BethelAnim.this.invalidate();
                this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showOnce = false;
                        BethelAnim.this.invalidate();
                    }
                },300);
            }
        }

         public void setControlPointShow(boolean flag)
         {
             if(flag != isShowControlPoint)
             {
                 isShowControlPoint = flag;
             }

         }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

//            filter.CurvesCreate(m_contolArr, 4, m_arr, mWidth, mHeight);
//            if(m_arr != null)
//            {
//                mPath.reset();
//                for (int i = 0; i < m_arr.length/2 ;)
//                {
//                    if (i == 0)
//                    {
//                        mPath.moveTo(m_arr[i],m_arr[i + 1]);
//                    }
//                    else
//                    {
//                        mPath.lineTo(m_arr[i*2],m_arr[i*2 + 1]);
//                    }
//                    i = i + 3;
//                }
//                canvas.drawPath(mPath,mPaint);
//            }

            if(m_contolArr != null && m_contolArr.length > 0)
            {
                mPath.reset();
                mPath.moveTo(m_contolArr[0],m_contolArr[1]);
                mPath.quadTo(m_contolArr[2],m_contolArr[3],m_contolArr[4],m_contolArr[5]);
                mPath.quadTo(BightAnim.this.mWidth - m_contolArr[2],BightAnim.this.mHeight - m_contolArr[3],m_contolArr[6],m_contolArr[7]);

                canvas.drawPath(mPath,mPaint);
            }


            canvas.drawCircle(mRadio,BightAnim.this.mHeight - mRadio,mRadio,mCiclePaint);
            canvas.drawCircle(BightAnim.this.mWidth/2,BightAnim.this.mHeight/2,mRadio,mCiclePaint);
            canvas.drawCircle(BightAnim.this.mWidth - mRadio,mRadio,mRadio,mCiclePaint);

//            canvas.drawCircle(mRadio,BightAnim.this.mHeight - mRadio,mRadio2,mBlackPaint);
//            canvas.drawCircle(BightAnim.this.mWidth/2,BightAnim.this.mHeight/2,mRadio2,mBlackPaint);
//            canvas.drawCircle(BightAnim.this.mWidth - mRadio,mRadio,mRadio2,mBlackPaint);


            if(isShowControlPoint)
            {
                canvas.drawCircle(mControlX,mControlY,mRadio,mWhitePaint1);
                canvas.drawCircle(mControlX,mControlY,mRadio2,mWhitePaint);
            }

//            if(showOnce)
//            {
//                canvas.drawCircle(m_contolArr[2]*mWidth,m_contolArr[3]*mHeight,mRadio,mWhitePaint1);
//                canvas.drawCircle(m_contolArr[2]*mWidth,m_contolArr[3]*mHeight,mRadio2,mWhitePaint);
//                return;
//            }

            if(isShowControlPoint)
            canvas.drawCircle(m_contolArr[2]*mWidth,m_contolArr[3]*mHeight,mRadio,mCiclePaint);


        }

         public void ClearAll()
         {
             if(temBmp != null)
             {
                 temBmp.recycle();
             }
         }

    }

     class BightCircle extends View {
        private Paint mPaint;
        private Paint mOutPaint;
        private int mWidth;
        private int mHeight;
        private Handler mHandler;
        private int mDegree;
        private UpdateCallBack m_cb;
        private final int ORICIRCLE_RADIUS = ShareData.PxToDpi_xhdpi(18);
        private final int OUTCIRCLE_RADIUS = ShareData.PxToDpi_xhdpi(13);

        private AnimatorSet firstAnimSet;
        private AnimatorSet secondAnimSet;
        private AnimatorSet finishAnimSet;

         private boolean isClose = false;

        public BightCircle(Context context) {
            super(context);
            Init();
        }

        public BightCircle(Context context, AttributeSet attrs) {
            super(context, attrs);
            Init();
        }


        public BightCircle(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            Init();
        }



        public void Init()
        {
            mDegree = 0;
            mPaint = new Paint();
//            mPaint.setStrokeWidth(ShareData.PxToDpi_xhdpi(10));
//            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.WHITE);
            mPaint.setAntiAlias(true);

            mOutPaint = new Paint();
//            mOutPaint.setStrokeWidth(ShareData.PxToDpi_xhdpi(10));
//            mOutPaint.setStyle(Paint.Style.FILL);
            mOutPaint.setColor(Color.WHITE);
            mOutPaint.setAntiAlias(true);

            mHandler = new Handler();
//            this.setBackgroundColor(0xff333333);
        }

        public void setUpdateCallBack(UpdateCallBack callBack)
        {
         this.m_cb = callBack;
        }

        public void showTransparency()
        {
            this.setAlpha(0.2f);
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BightCircle.this.setAlpha(1f);
//                    showOutCircle();
//                    if(m_cb != null)
//                    {
////                        updateX = ((float)animation.getAnimatedValue())/(BightAnim.this.mWidth + 0.5f);
////                        System.out.println("=========== x = " + animation.getAnimatedValue());
////                        updataY = getRario((float)animation.getAnimatedValue(),secondpoint1,secondpoint2,animation);
////                        if(m_cb != null)
////                        {
////                            m_cb.updateView(updateX,updataY,true);
////                        }
//                        updateX = nextUsePoint.x/(BightAnim.this.mWidth + 0.5f);
//                        PointF endPoint = new PointF();
//                        endPoint.x = BightAnim.this.mWidth/4f - ShareData.PxToDpi_xhdpi(25);
//                        endPoint.y = BightAnim.this.mHeight/2f - ShareData.PxToDpi_xhdpi(62)/2f;
//                        updataY = getRario(updateX,nextUsePoint,endPoint);
//                        m_cb.showOnce(true,updateX,updataY);
//                    }
                        if (mHandler != null) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    PointF endPoint = new PointF();
                                    endPoint.x = BightAnim.this.mWidth/4f - ShareData.PxToDpi_xhdpi(25);
                                    endPoint.y = BightAnim.this.mHeight/2f - ShareData.PxToDpi_xhdpi(62)/2f;
                                    showSecondAnim(nextUsePoint,endPoint);
                                }
                            },300);
                        }
                    }
                },800);
            }
        }

        public void showTransparency2()
        {
            mDegree = 360;
            BightCircle.this.invalidate();
            this.setAlpha(0.4f);
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDegree = 0;
                        BightCircle.this.setAlpha(1f);
//                    showOutCircle();
                        showOutCircle();

                    }
                },800);
            }
        }


        private PointF nextUsePoint;
         private ObjectAnimator mFirstObjectAnimator1;
         private ObjectAnimator mFirstObjectAnimator2;

         public void initFirstAnimData(PointF point1, PointF point2)
         {
             mFirstObjectAnimator1 = ObjectAnimator.ofFloat(this, "translationX", point1.x, point2.x);
             mFirstObjectAnimator2 = ObjectAnimator.ofFloat(this, "translationY", point1.y, point2.y);
             firstAnimSet = new AnimatorSet();
             firstAnimSet.play(mFirstObjectAnimator1).with(mFirstObjectAnimator2);
             firstAnimSet.setDuration(500);
         }

        public void startFirstAnim(PointF point1, PointF point2)
        {

            nextUsePoint = point2;
            if(mFirstObjectAnimator1 != null && mFirstObjectAnimator2 != null && firstAnimSet != null)
            {
               firstAnimSet.start();
            }
            else
            {
                initFirstAnimData(point1,point2);
                firstAnimSet.start();
            }
            this.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCircle.showTransparency();
                }
            },500);
        }

         float updateX;
         float updataY;
        boolean showOnce = true;
         private ObjectAnimator mSecondAnimtor1;
         private ObjectAnimator mSecondAnimtor2;
//         boolean onceY = true;

         public void initSecondAnimData(final PointF secondpoint1, final PointF secondpoint2)
         {
             nextUsePoint = secondpoint2;
             mSecondAnimtor1 = ObjectAnimator.ofFloat(this, "translationX", secondpoint1.x, secondpoint2.x);
             mSecondAnimtor1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                 @Override
                 public void onAnimationUpdate(ValueAnimator animation) {
                     if(m_cb != null)
                     {

                         updateX = ((float)animation.getAnimatedValue())/(BightAnim.this.mWidth + 0.5f);
                         updataY = getRario((float)animation.getAnimatedValue(),secondpoint1,secondpoint2);
                         if(m_cb != null)
                         {
//                             m_cb.updateView(updateX,updataY,true);
                             m_cb.updateView1(updateX,updataY,true,(secondpoint1.x - (float)animation.getAnimatedValue())/(secondpoint1.x - secondpoint2.x));
                         }

                         if(showOnce)
                         {
                             m_cb.showOnce(false,updateX,updataY);
                             showOnce = false;
//                             secondAnimSet.pause();
//                             mSecondAnimtor1.pause();
//                             mSecondAnimtor2.pause();
//                             BightCircle.this.postDelayed(new Runnable() {
//                                 @Override
//                                 public void run() {
//                                     mSecondAnimtor1.resume();
//                                     mSecondAnimtor2.resume();
//                                     secondAnimSet.resume();
//                                 }
//                             },500);
                         }
                     }
                 }
             });
             mSecondAnimtor2 = ObjectAnimator.ofFloat(this, "translationY", secondpoint1.y, secondpoint2.y);
             secondAnimSet = new AnimatorSet();
             secondAnimSet.play(mSecondAnimtor1).with(mSecondAnimtor2);
             secondAnimSet.setDuration(1000);
             secondAnimSet.addListener(new Animator.AnimatorListener() {
                 @Override
                 public void onAnimationStart(Animator animation) {

                 }

                 @Override
                 public void onAnimationEnd(Animator animation) {
                     mSecondAnimtor1.removeAllListeners();
                     mSecondAnimtor2.removeAllListeners();
//                     BightAnim.this.setBackgroundDrawable(null);
//                     BightAnim.this.setBackgroundDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(),R.drawable.interphoto_bight_anim_effect)));
                     showTransparency2();
                     BightAnim.this.postDelayed(new Runnable() {
                         @Override
                         public void run() {
                             PointF endPoint = new PointF();
                             endPoint.x = BightAnim.this.mWidth/2f - ShareData.PxToDpi_xhdpi(62)/2;
                             endPoint.y = BightAnim.this.mHeight*3/4f - ShareData.PxToDpi_xhdpi(62)/2f;
                             showFinishAnim(nextUsePoint,endPoint);
                         }
                     },1500);
                 }

                 @Override
                 public void onAnimationCancel(Animator animation) {

                 }

                 @Override
                 public void onAnimationRepeat(Animator animation) {

                 }
             });
         }

        protected void showSecondAnim(final PointF secondpoint1, final PointF secondpoint2)
        {
            if(mSecondAnimtor1 != null && mSecondAnimtor2 != null && secondAnimSet != null)
            {
                secondAnimSet.start();
            }
            else
            {
                initSecondAnimData(secondpoint1,secondpoint2);
                secondAnimSet.start();
            }
        }

         float disX;
         float disY;
         private ValueAnimator mFinishAnimator;
         private ObjectAnimator mFinishObjectAnimator1;
         private ObjectAnimator mFinishObjectAnimator2;
         private ObjectAnimator mAlphaObjectAnimator;

         private void initFinishAnimData(final PointF startPoint, PointF endPoint)
         {
             mAlphaObjectAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
             mFinishObjectAnimator1 = ObjectAnimator.ofFloat(this, "translationX", startPoint.x, endPoint.x);
             mFinishObjectAnimator2 = ObjectAnimator.ofFloat(this, "translationY", startPoint.y, endPoint.y);
             finishAnimSet = new AnimatorSet();
             finishAnimSet.play(mFinishObjectAnimator1).after(mAlphaObjectAnimator).with(mFinishObjectAnimator2);
             finishAnimSet.setDuration(500);
             finishAnimSet.addListener(new Animator.AnimatorListener() {
                 @Override
                 public void onAnimationStart(Animator animation) {
                     BightCircle.this.setAlpha(0f);
                 }

                 @Override
                 public void onAnimationEnd(Animator animation) {
                     BightCircle.this.setAlpha(1f);
                     mDegree = 0;
                     showOnce = true;
                     BightCircle.this.invalidate();
                     reStartAnim();
                 }

                 @Override
                 public void onAnimationCancel(Animator animation) {

                 }

                 @Override
                 public void onAnimationRepeat(Animator animation) {

                 }
             });


             mFinishAnimator = ValueAnimator.ofFloat(0f, 1f);
             mFinishAnimator.setDuration(400);
             mFinishAnimator.start();
             mFinishAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                 @Override
                 public void onAnimationUpdate(ValueAnimator animation) {
                     if(m_cb != null)
                     {
//                        updateX = (startPoint.x / (BightAnim.this.mWidth + 0.5f)) + (0.25f * (float)animation.getAnimatedValue());
//                        updataY = startPoint.y/(BightAnim.this.mHeight + 0.5f) + (0.25f * (float)animation.getAnimatedValue());
                         disX = Math.abs(startPoint.x - BightAnim.this.mEndPoint.x);
                         disY = Math.abs(startPoint.y - BightAnim.this.mEndPoint.y);
                         updateX = (startPoint.x + disX*(float)animation.getAnimatedValue())/(BightAnim.this.mWidth + 0.5f);
                         updataY = (startPoint.y + disY*(float)animation.getAnimatedValue() + ShareData.PxToDpi_xhdpi(62)/2f)/(BightAnim.this.mHeight + 0.5f);
                         if(m_cb != null)
                         {
//                             m_cb.updateView(updateX,updataY,false);
                             m_cb.updateView1(updateX,updataY,false,(1 - (float)animation.getAnimatedValue()));
//                             if((float)animation.getAnimatedValue() == 1f)
//                             {
//                                 m_cb.setControlPoint(false);
//                             }
                         }
                     }
                 }
             });

         }

        protected void showFinishAnim(final PointF startPoint, PointF endPoint)
        {

            if(mFinishObjectAnimator1 != null && mFinishObjectAnimator2 != null && finishAnimSet != null)
            {
                finishAnimSet.start();
            }
            else
            {
                initFinishAnimData(startPoint, endPoint);
                finishAnimSet.start();
            }

            if(mFinishAnimator != null)
            {
                mFinishAnimator.start();
            }
            else
            {
                initFinishAnimData(startPoint, endPoint);
                mFinishAnimator.start();

            }

//            if(finishAnimSet != null)
//            {
//                finishAnimSet.start();
//            }

        }

        private void reStartAnim() {
            if(!isClose)
            {
//                BightAnim.this.setBackgroundDrawable(null);
//                BightAnim.this.setBackgroundDrawable(new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.interphoto_bight_anim_ori)));
                this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startFirstAnim(BightAnim.this.mStartPoint,BightAnim.this.mEndPoint);
                    }
                },300);
            }
//            else
//            {
//                ClearAll();
//            }
        }

        private float getRario(float x,PointF point1,PointF point2)
        {
            float y;
            float ratio = (point1.x - x)/(point1.x - point2.x);
            y = point1.y - Math.abs(point1.y - point2.y)*ratio;
//            System.out.println("==========================ratio = " + ratio);
//            System.out.println("====================update y = " + y);
            y =  (y + ShareData.PxToDpi_xhdpi(62)/2f)/(BightAnim.this.mHeight + 0.5f);
            return y;

        }

         private ValueAnimator OutCircleAnim;
         private void initOutCircleAnimData()
         {
             OutCircleAnim = ValueAnimator.ofFloat(0f, 1f);
             OutCircleAnim.setDuration(500);
             OutCircleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                 @Override
                 public void onAnimationUpdate(ValueAnimator animation) {
                     float currentValue = (float) animation.getAnimatedValue();
                     mDegree = (int) (currentValue*360);
//                     if((float)animation.getAnimatedValue() == 1f)
//                     {
//                         m_cb.setControlPoint(false);
//                     }
                     BightCircle.this.invalidate();
                 }
             });
         }
        public void showOutCircle()
        {
            if(OutCircleAnim != null)
            {
                OutCircleAnim.start();
            }
            else
            {
                initOutCircleAnimData();
                OutCircleAnim.start();
            }
        }

        private boolean once = true;
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//            if(once)
//            {
//                mWidth = this.getMeasuredWidth();
//                mHeight = this.getMeasuredHeight();
//                mPaint.setStrokeWidth(mWidth - OUTCIRCLE_RADIUS);
//                mOutPaint.setStrokeWidth(OUTCIRCLE_RADIUS);
//                once = false;
//            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            if(changed)
            {
                mWidth = this.getMeasuredWidth();
                mHeight = this.getMeasuredHeight();
//                mPaint.setStrokeWidth(mWidth - OUTCIRCLE_RADIUS);
//                mOutPaint.setStrokeWidth(OUTCIRCLE_RADIUS);
//                once = false;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
            if(mDegree != 0)
            {
//                canvas.drawCircle(mWidth/2,mHeight/2,mWidth,mOutPaint);
//                RectF oval = new RectF(mWidth/2 - ORICIRCLE_RADIUS - OUTCIRCLE_RADIUS,mHeight/2 - ORICIRCLE_RADIUS - OUTCIRCLE_RADIUS,mWidth/2 + ORICIRCLE_RADIUS + OUTCIRCLE_RADIUS,mHeight/2 + ORICIRCLE_RADIUS + OUTCIRCLE_RADIUS);
               RectF oval = new RectF(0,0,mWidth,mHeight);
                canvas.drawArc(oval,0,mDegree,true,mOutPaint);
            }
            canvas.drawCircle(mWidth/2,mHeight/2,ORICIRCLE_RADIUS,mPaint);
        }

         public void ClearAll()
         {
             if(firstAnimSet != null)
             {
                 firstAnimSet.cancel();
                 firstAnimSet.removeAllListeners();
                 firstAnimSet = null;
             }

             if(secondAnimSet != null)
             {
                 secondAnimSet.cancel();
                 secondAnimSet.removeAllListeners();
                 secondAnimSet = null;
             }

             if(finishAnimSet != null)
             {
                 finishAnimSet.cancel();
                 finishAnimSet.removeAllListeners();
                 finishAnimSet = null;
             }

             if(mSecondAnimtor1 != null)
             {
                 mSecondAnimtor1.removeAllListeners();
                 mSecondAnimtor1.cancel();
                 mSecondAnimtor1 = null;
             }

             if(mSecondAnimtor2 != null)
             {
                 mSecondAnimtor2.removeAllListeners();
                 mSecondAnimtor2.cancel();
                 mSecondAnimtor2 = null;
             }
             if(mFirstObjectAnimator1 != null)
             {
                 mFirstObjectAnimator1.removeAllListeners();
                 mFirstObjectAnimator1.removeAllUpdateListeners();
                 mFirstObjectAnimator1.cancel();
                 mFirstObjectAnimator1 = null;
             }

             if(mFirstObjectAnimator2 != null)
             {
                 mFirstObjectAnimator2.removeAllListeners();
                 mFirstObjectAnimator2.removeAllUpdateListeners();
                 mFirstObjectAnimator2.cancel();
                 mFirstObjectAnimator2 = null;
             }

             if(mFinishObjectAnimator1 != null)
             {
                 mFinishObjectAnimator1.removeAllListeners();
                 mFinishObjectAnimator1.removeAllUpdateListeners();
                 mFinishObjectAnimator1.cancel();
                 mFinishObjectAnimator1 = null;
             }
             if(mFinishObjectAnimator2 != null)
             {
                 mFinishObjectAnimator2.removeAllListeners();
                 mFinishObjectAnimator2.removeAllUpdateListeners();
                 mFinishObjectAnimator2.cancel();
                 mFinishObjectAnimator2 = null;
             }
             if(OutCircleAnim != null)
             {
                 OutCircleAnim.removeAllListeners();
                 OutCircleAnim.removeAllUpdateListeners();
                 OutCircleAnim.cancel();
                 OutCircleAnim = null;
             }

             if(mFinishAnimator != null)
             {
                 mFinishAnimator.removeAllListeners();
                 mFinishAnimator.removeAllUpdateListeners();
                 mFinishAnimator.cancel();
                 mFinishAnimator = null;
             }
             m_cb = null;
             mHandler = null;
             isClose = true;
         }
    }

    interface UpdateCallBack
    {
        public void updateView(float x,float y,boolean isShowControlPoint);

        public void updateView1(float x,float y,boolean isShowControlPoint,float value);

        public void updataeViewBack(float x,float y,boolean isShowControlPoint,float value);

        public void showOnce(boolean isshow,float x,float y);

        public void setControlPoint(boolean isshow);
    }

}
