package cn.poco.beautify.animations;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.poco.interphoto2.R;


/**
 * Created by admin on 2017/2/23.
 */

public class TextAnim4 extends FrameLayout {

    ObjectAnimator isDown1;
    ObjectAnimator moveCrularX;
    ObjectAnimator moveCrularY;
    AnimatorSet translationAniS;
    ObjectAnimator fadeRectAni;
    AnimatorSet startAniSet;

    //第二次出现点击
    ObjectAnimator isDown2;
    ObjectAnimator moveCrularX1;
    ObjectAnimator moveCrularY1;
    AnimatorSet st;
    AnimatorSet moveCrular2Center;
    ObjectAnimator fadeRectAni1;
    ObjectAnimator twinkleRectAni;
    AnimatorSet twinkleAniS;

    //第三次出现点击
    ObjectAnimator isDown3;
    ObjectAnimator moveCrularX2;
    ObjectAnimator moveCrularY2;
    AnimatorSet animatorSet;
    AnimatorSet startTranslationSet;
    ObjectAnimator translationRectBm;
    ObjectAnimator translationtextIv;
    ObjectAnimator translationCrular;
    AnimatorSet animatorSet1;
    //第四次出现点击
    ObjectAnimator isDown4;
    AnimatorSet move2Left;
    AnimatorSet isDownAni;

    //第四次出现点击后 小圆归为
    ObjectAnimator moveCrularX3;
    ObjectAnimator moveCrularY3;
    AnimatorSet moveCrular2Center2;
    //文字归位
    ObjectAnimator translationtextIv1;



    LayoutParams params;
    ImageView photoIv;
    ImageView bmRect;
    ImageView criularIv;
    ImageView textIv;
    Crular crular;
    LayoutParams paramsRect;
    RectView rectView;
    RectBm rectBm;
    SmallCrular smallCrular;
    LayoutParams rectBmlParams;
    RectBmBig rectBm1;

    private boolean isStop = false;


    public TextAnim4(Context context) {
        super(context);
        initView(context);
    }

    public TextAnim4(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public TextAnim4(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    private void initView(final Context context) {

        params = new LayoutParams(dp2px(580), dp2px(480));
//        params.height = px2dp(1000);
        params.gravity = Gravity.CENTER;
        photoIv = new ImageView(context);
        photoIv.setImageResource(R.drawable.bg);
        photoIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addView(photoIv, params);


        LayoutParams bmRectParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bmRect = new ImageView(context);
        bmRect.setImageResource(R.drawable.anim_text_is_selected);
        bmRectParams.gravity = Gravity.CENTER;
        bmRectParams.setMargins(0,0,0,dp2px(40));
        bmRect.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        bmRect.setVisibility(INVISIBLE);
        addView(bmRect, bmRectParams);

        criularIv = new ImageView(context);
        criularIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        criularIv.setImageResource(R.drawable.anim_text_criular);
        addView(criularIv, bmRectParams);

        textIv = new ImageView(context);
        textIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        textIv.setImageResource(R.drawable.anim_text_interphoto);
        addView(textIv, bmRectParams);

        LayoutParams crularParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        crularParams.gravity  = Gravity.CENTER_HORIZONTAL;
        crularParams.setMargins(0,0,0,dp2px(80));
        crular = new Crular(context);
        addView(crular,crularParams);;
        LayoutParams spreadCrularParams = new LayoutParams(300,300);
        spreadCrularParams.gravity  = Gravity.CENTER;
        SpreadCrular spreadCrular = new SpreadCrular(context);
        spreadCrular.setY(crular.getY()-120);
        spreadCrular.setX(crular.getX());
        addView(spreadCrular,spreadCrularParams);


        paramsRect = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rectView = new RectView(context);
        paramsRect.gravity = Gravity.CENTER;
        paramsRect.setMargins(0,0,0,dp2px(40));
        paramsRect.height = dp2px(180);
        paramsRect.width = dp2px(200);
        rectView.setVisibility(INVISIBLE);
        addView(rectView, paramsRect);

        rectBm = new RectBm(context);
        rectBm.setY(this.getHeight()/2+(this.getHeight()/6));
        rectBm.setLayoutParams(paramsRect);
        rectBm.setVisibility(INVISIBLE);
        addView(rectBm, paramsRect);

        smallCrular = new SmallCrular(context);
        smallCrular.setVisibility(INVISIBLE);
        addView(smallCrular,paramsRect);

        rectBmlParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rectBm1 = new RectBmBig(context);
        rectBmlParams.gravity = Gravity.CENTER;
        rectBmlParams.setMargins(0,0,0,dp2px(35));
        rectBmlParams.height = dp2px(700);
        rectBmlParams.width = dp2px(200);
        rectBm1.setVisibility(INVISIBLE);
        addView(rectBm1, rectBmlParams);




        isDown1 = ObjectAnimator.ofFloat(crular, "Alpha",  1, 0).setDuration(2200);
        moveCrularX = ObjectAnimator.ofFloat(crular, "translationX", 0f, -190f).setDuration(100);
        moveCrularY = ObjectAnimator.ofFloat(crular, "translationY", 0f, 170f).setDuration(100);
        translationAniS = new AnimatorSet();
        translationAniS.play(moveCrularX).with(moveCrularY);

        fadeRectAni = ObjectAnimator.ofFloat(bmRect, "Alpha", 0, 1).setDuration(300);
        startAniSet = new AnimatorSet();
        (startAniSet.play(fadeRectAni).after(isDown1)).before(translationAniS);

        //第二次出现点击
        isDown2 = ObjectAnimator.ofFloat(crular, "Alpha",  1, 0).setDuration(2200);
        moveCrularX1 = ObjectAnimator.ofFloat(crular, "translationX", 210f, 0).setDuration(100);
        moveCrularY1 = ObjectAnimator.ofFloat(crular, "translationY", 170f, 0).setDuration(100);
        st = new AnimatorSet();
        moveCrular2Center = new AnimatorSet();
        moveCrular2Center.play(moveCrularX1).with(moveCrularY1);
        ;
       /* (st.play(startAniSet).before(moveCrular2Center)).after(isDown2);*/
        st.play(startAniSet).before(isDown2);
        //st.start();

        fadeRectAni1 = ObjectAnimator.ofFloat(bmRect, "Alpha", 1, 0).setDuration(300);
        twinkleRectAni = ObjectAnimator.ofFloat(rectView, "Alpha", 0, 1, 0, 1, 0).setDuration(900);

        twinkleAniS = new AnimatorSet();
        (twinkleAniS.play(fadeRectAni1).before(twinkleRectAni)).after(st);
        twinkleAniS.start();

        //第三次出现点击
        isDown3 = ObjectAnimator.ofFloat(crular, "Alpha",  1, 0).setDuration(2200);
        moveCrularX2 = ObjectAnimator.ofFloat(crular, "translationX", 0, -200).setDuration(100);
        moveCrularY2 = ObjectAnimator.ofFloat(crular, "translationY", 0, 280).setDuration(100);
        animatorSet = new AnimatorSet();
        animatorSet.play(isDown3).after(moveCrular2Center);
        startTranslationSet = new AnimatorSet();
        translationRectBm = ObjectAnimator.ofFloat(rectBm, "translationY", 0f, 180f).setDuration(300);
        translationtextIv = ObjectAnimator.ofFloat(textIv, "translationY", 0f, 180f).setDuration(300);
        translationCrular = ObjectAnimator.ofFloat(smallCrular, "translationY", 0f, 180f).setDuration(300);
        (startTranslationSet.play(translationRectBm).with(translationtextIv).with(translationCrular)).after(animatorSet);
        animatorSet1 = new AnimatorSet();
        animatorSet1.play(startTranslationSet).after(twinkleAniS);


        //第四次出现点击
        isDown4 = ObjectAnimator.ofFloat(crular, "Alpha", 1, 0).setDuration(2200);
        moveCrularX3 = ObjectAnimator.ofFloat(crular, "translationX", 200, 0).setDuration(1000);
        moveCrularY3 = ObjectAnimator.ofFloat(crular, "translationY", 300, 0).setDuration(1000);
        translationtextIv1 = ObjectAnimator.ofFloat(textIv, "translationY", 200, 0).setDuration(300);
        translationRectBm = ObjectAnimator.ofFloat(rectBm, "translationY", 200f, 0f).setDuration(300);
        moveCrular2Center2 = new AnimatorSet();
        translationtextIv1.setStartDelay(900);
        moveCrular2Center2.play(moveCrularX3).with(moveCrularY3).with(translationtextIv1).with(translationRectBm);


        move2Left = new AnimatorSet();
        move2Left.play(moveCrularX2).with(moveCrularY2).before(isDown4);
        isDownAni = new AnimatorSet();
        isDownAni.play(move2Left).after(animatorSet1);
        isDownAni.start();



        isDown1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                LayoutParams crularParams = new LayoutParams(300,300);
                crularParams.gravity  = Gravity.CENTER;
                SpreadCrular spreadCrular = new SpreadCrular(context);
                spreadCrular.setY(crular.getY()-120);
                spreadCrular.setX(crular.getX());
                addView(spreadCrular,crularParams);
                crular.setVisibility(VISIBLE);

            }
        });
        isDown2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                crular.setVisibility(VISIBLE);
                LayoutParams crularParams = new LayoutParams(300,300);
                crularParams.gravity  = Gravity.CENTER;
                crularParams.setMargins(0,0,0,40);
                SpreadCrular spreadCrular = new SpreadCrular(context);
                spreadCrular.setY(crular.getY()-80);
                spreadCrular.setX(crular.getX());
                addView(spreadCrular,crularParams);
            }
        });
        isDown3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                crular.setVisibility(VISIBLE);
                LayoutParams crularParams = new LayoutParams(300,300);
                crularParams.gravity  = Gravity.CENTER;
                crularParams.setMargins(0,0,0,40);
                SpreadCrular spreadCrular = new SpreadCrular(context);
                spreadCrular.setY(crular.getY()-80);
                spreadCrular.setX(crular.getX());
                addView(spreadCrular,crularParams);
            }
        });
        isDown4.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                crular.setVisibility(VISIBLE);
                LayoutParams crularParams = new LayoutParams(300,300);
                crularParams.gravity  = Gravity.CENTER;
                crularParams.setMargins(0,0,0,40);
                SpreadCrular spreadCrular = new SpreadCrular(context);
                spreadCrular.setY(crular.getY()-80);
                spreadCrular.setX(crular.getX());
                addView(spreadCrular,crularParams);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                rectBm.setVisibility(INVISIBLE);
                rectBm1.setVisibility(VISIBLE);
                reSet();
            }
        });
        fadeRectAni.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                bmRect.setVisibility(VISIBLE);
            }
        });
        twinkleRectAni.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                rectView.setVisibility(VISIBLE);

            }
        });
        twinkleRectAni.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rectBm.setVisibility(VISIBLE);
            }
        });
        moveCrular2Center.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                crular.setVisibility(INVISIBLE);
            }
        });


        translationCrular.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                smallCrular.setVisibility(VISIBLE);
            }
        });
        translationCrular.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                smallCrular.setVisibility(INVISIBLE);
            }
        });
        moveCrularX2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                crular.setVisibility(INVISIBLE);
            }
        });

        translationtextIv1.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                textIv.setVisibility(INVISIBLE);
                rectBm1.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                textIv.setVisibility(VISIBLE);
            }
        });

      /*  isDown4.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rectBm.setVisibility(INVISIBLE);
                rectBm1.setVisibility(VISIBLE);
                reSet();
            }
        });*/
        moveCrular2Center2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                crular.setVisibility(INVISIBLE);
                //textIv.setVisibility(INVISIBLE);
                rectBm.setVisibility(INVISIBLE);
            }
        });



    }
    private void reSet() {
        moveCrular2Center2.start();
        isDownAni.setStartDelay(3000);
        isDownAni.start();
    }

    public void removeAnimation(){
        moveCrularX.removeAllListeners();
        isDown2.removeAllListeners();
        fadeRectAni.removeAllListeners();
        twinkleRectAni.removeAllListeners();
        moveCrular2Center.removeAllListeners();
        isDown3.removeAllListeners();
        translationCrular.removeAllListeners();
        moveCrularX2.removeAllListeners();
        isDown4.removeAllListeners();
        translationtextIv1.removeAllListeners();
        moveCrular2Center2.removeAllListeners();
        isDownAni.removeAllListeners();
    }
    //停止动画
    public void release() {

        moveCrular2Center2.cancel();
        isDownAni.cancel();
        removeAnimation();
    }

    private int dp2px(float pxValue) {

        float scale = getContext().getResources().getDisplayMetrics().density;
        int dp = (int) (pxValue * scale + 0.5);
        return dp;
    }

    //模仿点击的小圆
    public class Crular extends View {

        Paint mPaint = new Paint();
        Paint bigPaint = new Paint();
        Paint crularPaint = new Paint();

        public Crular(Context context) {
            super(context);
            initCirclePaint(mPaint, bigPaint);
            initRectPaint(crularPaint);
        }

        public Crular(Context context, AttributeSet attrs) {
            super(context, attrs);

        }

        public Crular(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, 21, mPaint);
           // canvas.drawCircle(getWidth() / 2, getHeight() / 2, 22, bigPaint);
           // canvas.drawCircle(getWidth() / 2, getHeight() / 2, 35, bigPaint);
        }
    }

    public class SmallCrular extends View {

        Paint mPaint = new Paint();

        public SmallCrular(Context context) {
            super(context);
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }

        public SmallCrular(Context context, AttributeSet attrs) {
            super(context, attrs);


        }

        public SmallCrular(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, 20, mPaint);
        }

    }

    public class RectView extends View {

        Paint rect, alpRect;

        public RectView(Context context) {
            super(context);
            rect = new Paint();
            alpRect = new Paint();
            initRectPaint(rect);
            initRectPaint(rect);
            alpRect = new Paint();
            alpRect.setAntiAlias(true);
            alpRect.setColor(Color.WHITE);
            alpRect.setStrokeWidth(3);
            alpRect.setAlpha(80);
            alpRect.setStyle(Paint.Style.FILL_AND_STROKE);
        }

        public RectView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RectView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int bLeft = getWidth() - (getWidth() - getWidth() / 5);
            int bTop = getHeight() - (getHeight() - getHeight() / 5);
            int bRight = getWidth() - getWidth() / 5;
            int bBotton = getHeight() - getHeight() / 5;
            //float left, float top, float right, float bottom, @NonNull Paint paint
            canvas.drawRect(bLeft, bTop + 10, bRight, bBotton, rect);
            canvas.drawRect(bLeft, bTop + 10, bRight, bBotton, alpRect);

            //小矩形
            int sLeft = getWidth() - (getWidth() - getWidth() / 10);
            int sTop = (int) (getHeight() - (getHeight() - getHeight() / 2.5f));
            int sRight = getWidth() - getWidth() / 10;
            int sBotton = (int) (getHeight() - getHeight() / 2.5);
            canvas.drawRect(sLeft, sTop + 20, sRight, sBotton + 50, rect);
            canvas.drawRect(sLeft, sTop + 20, sRight, sBotton + 50, alpRect);
        }
    }

    //扩散的小圆环
    public class SpreadCrular extends View{

        //最大透明度
        private List<Bean> list;
        private int maxAlpha = 170;

        public SpreadCrular(Context context) {
            super(context);

            Bean bean = new Bean();
            bean.radius = 0;
            bean.alpha = maxAlpha;
            bean.width = 30;
            bean.paint = initPaint(bean.alpha,bean.width);
            list = new ArrayList<>();
            list.add(bean);
            handler.sendEmptyMessage(0);
        }

        public SpreadCrular(Context context, AttributeSet attrs) {
            super(context, attrs);
/*
            Bean bean = new Bean();
            bean.radius = 0;
            bean.alpha = maxAlpha;
            bean.width = 30;
            bean.paint = initPaint(bean.alpha,bean.width);
            list = new ArrayList<>();
            list.add(bean);
            handler.sendEmptyMessage(0);*/
        }

        public SpreadCrular(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        private Paint initPaint(int alpha, float width) {
            Paint paint = new Paint();
            //paint.setAntiAlias(true);
            paint.setStrokeWidth(width);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAlpha(alpha);
            paint.setColor(Color.WHITE);
            return paint;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < list.size(); i++) {
                Bean circle = list.get(i);
                canvas.drawCircle(getWidth()/2,getHeight()/2,circle.radius, circle.paint);
            }

        }

        private Handler handler =  new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0 :
                        Refresh();
                        invalidate();
                        if (list != null && list.size() != 0){
                            handler.sendEmptyMessageDelayed(0,100);// 每150毫秒发送
                        }
                }
            }
        };

        private void Refresh() {
            for (int i = 0 ; i < list.size();i++){
                Bean bean = list.get(i);
                if (bean.alpha < 0 ){
                    list.remove(i);
                    bean.paint = null;
                    continue;
                }
                bean.radius += 5;
                bean.alpha -= 15;
                if (bean.alpha <0 ){
                    bean.alpha = 0;
                }
                bean.width = bean.radius /4; // 描边宽度设置为半径的1/4
                bean.paint.setAlpha(bean.alpha);
                bean.paint.setStrokeWidth(bean.width);

            }

        }
        class Bean{
            int alpha;
            float width;
            float radius;
            Paint paint;
        }
    }

    public class RectBm extends View {
        Paint mPaint;
        Bitmap bitmap, bitmap1, bitmap2, bitmap3;

        public RectBm(Context context) {
            super(context);
            mPaint = new Paint();
            initRectPaint(mPaint);
            Resources resources = getResources();
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.photofactory_pendant_del_btn);
            bitmap1 = BitmapFactory.decodeResource(resources, R.drawable.photofactory_pendant_merge_btn);
            bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.photofactory_pendant_save_btn);
            bitmap3 = BitmapFactory.decodeResource(resources, R.drawable.photofactory_pendant_scale_btn);
        }

        public RectBm(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RectBm(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int sLeft = getWidth() - (getWidth() - getWidth() / 10);
            int sTop = (int) (getHeight() - (getHeight() - getHeight() / 2.5f));
            int sRight = getWidth() - getWidth() / 10;
            int sBotton = (int) (getHeight() - getHeight() / 2.5);
            canvas.drawRect(sLeft, sTop + 20, sRight, sBotton + 50, mPaint);

            //(@NonNull Bitmap bitmap, float left, float top, @Nullable Paint paint
            canvas.drawBitmap(bitmap, sLeft - (bitmap.getWidth() / 2 - bitmap.getWidth() / 6), sTop - bitmap.getHeight() / 4, mPaint);
            canvas.drawBitmap(bitmap2, sRight - bitmap.getWidth() + bitmap.getWidth() / 2 - bitmap.getWidth() / 6, sTop - bitmap.getHeight() / 4, mPaint);
            canvas.drawBitmap(bitmap1, sLeft - (bitmap.getWidth() / 2 - bitmap.getWidth() / 6), sBotton - bitmap.getHeight() / 4 + 30, mPaint);
            canvas.drawBitmap(bitmap3, sRight - bitmap.getWidth() / 2, sBotton - bitmap.getHeight() / 4 + 30, mPaint);

        }
    }
    public class RectBmBig extends View {
        Paint mPaint;
        Bitmap bitmap, bitmap1, bitmap2, bitmap3;

        public RectBmBig(Context context) {
            super(context);
            mPaint = new Paint();
            initRectPaint(mPaint);
            Resources resources = getResources();
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.photofactory_pendant_del_btn);
            bitmap1 = BitmapFactory.decodeResource(resources, R.drawable.photofactory_pendant_divide_btn);
            bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.photofactory_pendant_save_btn);
            bitmap3 = BitmapFactory.decodeResource(resources, R.drawable.photofactory_pendant_scale_btn);
        }

        public RectBmBig(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public RectBmBig(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int sLeft = getWidth() - (getWidth() - getWidth() / 10);
            int sTop = (int) (getHeight() - (getHeight() - getHeight() / 2.5f));
            int sRight = getWidth() - getWidth() / 10;
            int sBotton = (int) (getHeight() - getHeight() / 2.5);
            canvas.drawRect(sLeft, sTop + 20, sRight, sBotton + 50, mPaint);

            //(@NonNull Bitmap bitmap, float left, float top, @Nullable Paint paint
            canvas.drawBitmap(bitmap, sLeft - (bitmap.getWidth() / 2 - bitmap.getWidth() / 6), sTop - bitmap.getHeight() / 4, mPaint);
            canvas.drawBitmap(bitmap2, sRight - bitmap.getWidth() + bitmap.getWidth() / 2 - bitmap.getWidth() / 6, sTop - bitmap.getHeight() / 4, mPaint);
            canvas.drawBitmap(bitmap1, sLeft - (bitmap.getWidth() / 2 - bitmap.getWidth() / 6), sBotton - bitmap.getHeight() / 4 + 30, mPaint);
            canvas.drawBitmap(bitmap3, sRight - bitmap.getWidth() / 2, sBotton - bitmap.getHeight() / 4 + 30, mPaint);

        }
    }

    private void initCirclePaint(Paint mPaint, Paint mBigPaint) {
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setAntiAlias(true);
        mBigPaint.setColor(Color.WHITE);
        mBigPaint.setStrokeWidth(20);
        mBigPaint.setAntiAlias(true);
       // mBigPaint.setAlpha(220);
        mBigPaint.setStyle(Paint.Style.STROKE);

    }

    private void initRectPaint(Paint mPaint) {
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
    }


}
