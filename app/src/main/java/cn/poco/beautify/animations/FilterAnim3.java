package cn.poco.beautify.animations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by pengdh on 2016/10/18.
 */

public class FilterAnim3 extends View {

    private final int CICLE_SHOW = 1;
    private final int CICLE_EXTEND_1 = 2;
    private final int CICLE_EXTEND_2 = 3;
    private final int TRANSLATE_RIGHT = 4;
    private final int TRANSLATE_LEFT = 5;
    private final int CLCLE_HIDE2 = 6;
    private final int CICLE_SHOW2 = 7;
    private final int CICLE_EXTEND2_1 = 8;
    private final int CICLE_EXTEND2_2 = 9;
    private final int TRANSLATE_CENTER = 10;
    private final int HIDE_CENTER = 11;
    private final int FINISH_PAUSH = 12;


    private final float CICLE_SHOW_TIME = 1000;
    private final float CICLE_EXTEND_1_TIME = 600;
    private final float CICLE_EXTEND_2_TIME = 600;
    private final float TRANSLATE_RIGHT_TIME = 800;
    private final float TRANSLATE_LEFT_TIME = 800;
    private final float CLCLE_HIDE2_TIME = 1000;
    private final float CICLE_SHOW2_TIME = 1000;
    private final float CICLE_EXTEND2_1_TIME = 600;
    private final float CICLE_EXTEND2_2_TIME = 600;

    private final float TRANSLATE_CENTER_TIME = 1000;
    private final float HIDE_CENTER_TIME = 800;
    private final float FINISH_PAUSH_TIME = 1000;
    private int m_width;
    private int m_height;
    private Timer m_timer;
    private TimerTask m_task;
    private Paint m_paint;
    private int m_curStep = CICLE_SHOW;
    private int m_curTime;
    private Bitmap bmp_top;
    private Rect[] m_rects;
    private Rect m_hideRect;
    private int m_cicleCenterX;
    private int m_cicleCenterY;
    private int m_startTranslateX1;
    private int m_startTranslateX2;


    private Bitmap[] bmps = new Bitmap[5];
    private Bitmap bmp_select1;
    private Bitmap bmp_select2;
    private Bitmap bmp_hide;

    public FilterAnim3(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public FilterAnim3(Context context) {
        super(context);
        initialize();
    }

    public FilterAnim3(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize()
    {
        m_paint = new Paint();
        m_paint.setAntiAlias(true);
        bmp_top = BitmapFactory.decodeResource(getResources(),R.drawable.filter_anim1_pic2);
        bmps[0] = BitmapFactory.decodeResource(getResources(),R.drawable.clipanim_bmp1);
        bmps[1] = BitmapFactory.decodeResource(getResources(),R.drawable.clipanim_bmp2);
        bmps[2] = BitmapFactory.decodeResource(getResources(),R.drawable.clipanim_bmp3);
        bmps[3] = BitmapFactory.decodeResource(getResources(),R.drawable.clipanim_bmp4);
        bmps[4] = BitmapFactory.decodeResource(getResources(),R.drawable.clipanim_bmp5);

        bmp_select1 = BitmapFactory.decodeResource(getResources(),R.drawable.clipanim_select);
        bmp_select2 = BitmapFactory.decodeResource(getResources(),R.drawable.clipanim_select1);
        bmp_hide = BitmapFactory.decodeResource(getResources(),R.drawable.clipanim_hide);
        m_rects = new Rect[5];

        m_width = ShareData.PxToDpi_xhdpi(580);
        m_height = ShareData.PxToDpi_xhdpi(480);
        initRect();
        initCircleData();
        m_startTranslateX1 = ShareData.PxToDpi_xhdpi(140) + ShareData.PxToDpi_xhdpi(30);
        m_startTranslateX2 = ShareData.PxToDpi_xhdpi(140)*2 + ShareData.PxToDpi_xhdpi(50);

        distanX = (int) (ShareData.PxToDpi_xhdpi(140)*2 + ShareData.PxToDpi_xhdpi(50) - (m_width - ShareData.PxToDpi_xhdpi(140))/2f);
        distanY = (int) (ShareData.PxToDpi_xhdpi(300) -((ShareData.PxToDpi_xhdpi(263) - ShareData.PxToDpi_xhdpi(140))/2f));
        m_task = new TimerTask() {
            @Override
            public void run() {
                m_curTime = m_curTime + 17;
                switch (m_curStep)
                {
                    case CICLE_SHOW:
                    {
                        if(m_curTime >= CICLE_SHOW_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = CICLE_EXTEND_1;
                        }
                        break;
                    }
                    case CICLE_EXTEND_1:
                    {
                        if(m_curTime >= CICLE_EXTEND_1_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = CICLE_EXTEND_2;
                        }
                        break;
                    }
                    case CICLE_EXTEND_2:
                    {
                        if(m_curTime >= CICLE_EXTEND_2_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = TRANSLATE_RIGHT;
                        }
                        break;
                    }
                    case TRANSLATE_RIGHT:
                    {
                        if(m_curTime >= TRANSLATE_RIGHT_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = TRANSLATE_LEFT;
                        }
                        else
                        {
                                float  value = m_curTime/TRANSLATE_RIGHT_TIME;
                                int x2 = (int) (m_startTranslateX2 - ShareData.PxToDpi_xhdpi(160)*value);
                                m_rects[2].left = x2;
                                m_rects[2].right = x2 + ShareData.PxToDpi_xhdpi(140);
                        }
                        break;
                    }
                    case TRANSLATE_LEFT:
                    {
                        if(m_curTime >= TRANSLATE_LEFT_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = CLCLE_HIDE2;
                        }
                        else
                        {
                            float value = m_curTime/TRANSLATE_LEFT_TIME;
                            int x1 = (int) (m_startTranslateX1 + ShareData.PxToDpi_xhdpi(160)*value);
                            m_rects[1].left = x1;
                            m_rects[1].right = x1 + ShareData.PxToDpi_xhdpi(140);
                        }
                        break;
                    }
                    case CLCLE_HIDE2:
                    {
                        if(m_curTime >= CLCLE_HIDE2_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = CICLE_SHOW2;
                        }
                        break;
                    }
                    case CICLE_SHOW2:
                    {
                        if(m_curTime >= CICLE_SHOW2_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = CICLE_EXTEND2_1;
                        }
                        break;
                    }

                    case CICLE_EXTEND2_1:
                    {
                        if(m_curTime >= CICLE_EXTEND2_1_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = CICLE_EXTEND2_2;
                        }
                        break;
                    }

                    case CICLE_EXTEND2_2:
                    {
                        if(m_curTime >= CICLE_EXTEND2_2_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = TRANSLATE_CENTER;
                        }
                        break;
                    }

                    case TRANSLATE_CENTER:
                    {
                        if(m_curTime >= TRANSLATE_CENTER_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = HIDE_CENTER;
                        }
                        else
                        {
                            float value = m_curTime/TRANSLATE_CENTER_TIME;
                            int x = (int) (ShareData.PxToDpi_xhdpi(140)*2 + ShareData.PxToDpi_xhdpi(50) - distanX*value);
                            int y = (int) (ShareData.PxToDpi_xhdpi(300) - distanY*value);
                            m_rects[1] = new Rect(x,y,x + ShareData.PxToDpi_xhdpi(140),y + ShareData.PxToDpi_xhdpi(140));

                            if(m_curTime > TRANSLATE_CENTER_TIME/2)
                            {
                                int x1 = (int) (-ShareData.PxToDpi_xhdpi(160)*(value - 0.5)/0.5f + ShareData.PxToDpi_xhdpi(140)*3 + ShareData.PxToDpi_xhdpi(70));
                                int x2 = (int) (-ShareData.PxToDpi_xhdpi(160)*(value - 0.5)/0.5f + ShareData.PxToDpi_xhdpi(140)*4 + ShareData.PxToDpi_xhdpi(90));
                                m_rects[3] = new Rect(x1,m_rects[3].top,x1 + ShareData.PxToDpi_xhdpi(140),m_rects[3].bottom);
                                m_rects[4] = new Rect(x2,m_rects[4].top,x2 + ShareData.PxToDpi_xhdpi(140),m_rects[4].bottom);
                            }
                        }
                        break;
                    }
                    case HIDE_CENTER:
                    {
                        if(m_curTime >= HIDE_CENTER_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = FINISH_PAUSH;
                        }
                        break;
                    }
                    case FINISH_PAUSH:
                    {
                        if(m_curTime > FINISH_PAUSH_TIME)
                        {
                            m_curTime = 0;
                            m_curStep = CICLE_SHOW;
                            initRect();
                        }
                    }
                }
                postInvalidate();
            }
        };

        m_timer = new Timer();
    }


    private void initCircleData()
    {
        m_cicleCenterX = m_rects[2].left + ShareData.PxToDpi_xhdpi(70);
        m_cicleCenterY = m_rects[2].top + ShareData.PxToDpi_xhdpi(70);
    }

    private void initRect()
    {
        for(int i = 0; i < m_rects.length; i++)
        {
            int leftX = ShareData.PxToDpi_xhdpi(10) + ShareData.PxToDpi_xhdpi(140)*i;
            if(i != 0)
            {
                leftX = leftX + ShareData.PxToDpi_xhdpi(20)*i;
            }
            int leftY = ShareData.PxToDpi_xhdpi(300);
            int rightX = leftX + ShareData.PxToDpi_xhdpi(140);
            int rightY = leftY + ShareData.PxToDpi_xhdpi(140);
           m_rects[i] = new Rect(leftX,leftY,rightX,rightY);
        }

        int x1 = (int) ((m_width - ShareData.PxToDpi_xhdpi(180))/2f);
        int y1 = (int) ((ShareData.PxToDpi_xhdpi(263) - ShareData.PxToDpi_xhdpi(180))/2f);
        m_hideRect = new Rect(x1,y1,x1 + ShareData.PxToDpi_xhdpi(180),y1 + ShareData.PxToDpi_xhdpi(180));
    }

    public void startAnim()
    {
        if(m_timer != null && m_task != null)
        {
            m_timer.schedule(m_task,300,17);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(ShareData.PxToDpi_xhdpi(580),ShareData.PxToDpi_xhdpi(480));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawToCanvas(canvas);
    }

    private int distanX = 0;
    private int distanY = 0;
    private void drawToCanvas(Canvas canvas)
    {
        switch (m_curStep)
        {
            case CICLE_SHOW:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                drawBottomBmpSame(canvas);
                float value = m_curTime/CICLE_SHOW_TIME;
                m_paint.reset();
                m_paint.setColor(Color.WHITE);
                m_paint.setAlpha((int) (value*255));
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,ShareData.PxToDpi_xhdpi(15),m_paint);
                break;
            }
            case CICLE_EXTEND_1:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                float value = m_curTime/CICLE_EXTEND_1_TIME;

                drawBottomBmpSame(canvas);
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,ShareData.PxToDpi_xhdpi(15),m_paint);
                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setAlpha((int) (100*value));
                paint.setAntiAlias(true);
                Log.i("PPP","value == " + (int) (100*value));
                int radio = (int) (ShareData.PxToDpi_xhdpi(15) + ShareData.PxToDpi_xhdpi(20)*value);
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,radio,paint);
                break;
            }
            case CICLE_EXTEND_2:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                float value = m_curTime/CICLE_EXTEND_2_TIME;
                Paint tempPaint = new Paint();
                tempPaint.setColor(Color.BLACK);
                tempPaint.setAlpha((int) (200*value));
                canvas.drawRect(new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),tempPaint);
                canvas.drawBitmap(bmp_hide,null,m_hideRect,null);
                drawBottomBmpSame(canvas);
                m_paint.reset();
                m_paint.setColor(Color.WHITE);
                m_paint.setAlpha(255);
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,ShareData.PxToDpi_xhdpi(15),m_paint);
                drawOutCicle(canvas,value);

                break;
            }
            case TRANSLATE_RIGHT:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                Paint tempPaint = new Paint();
                tempPaint.setColor(Color.BLACK);
                tempPaint.setAlpha(200);
                canvas.drawRect(new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),tempPaint);
                canvas.drawBitmap(bmp_hide,null,m_hideRect,null);
                drawBottomBmpSame(canvas);
                break;
            }

            case TRANSLATE_LEFT:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                Paint tempPaint = new Paint();
                tempPaint.setColor(Color.BLACK);
                tempPaint.setAlpha(200);
                canvas.drawRect(new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),tempPaint);
                canvas.drawBitmap(bmp_hide,null,m_hideRect,null);
                drawBottomBmpSame(canvas);
                break;
            }
            case CLCLE_HIDE2:
            {

                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                drawBottomBmpSame(canvas);
                float value = m_curTime/CLCLE_HIDE2_TIME;
                Paint tempPaint = new Paint();
                tempPaint.setColor(Color.BLACK);
                tempPaint.setAlpha((int) (200*(1 - value)));
                canvas.drawRect(new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),tempPaint);
                m_paint.reset();
                m_paint.setColor(Color.WHITE);
                m_paint.setAlpha((int) ((1 - value)*255));
                canvas.drawCircle(m_rects[2].left + ShareData.PxToDpi_xhdpi(70),m_rects[2].top + ShareData.PxToDpi_xhdpi(70),ShareData.PxToDpi_xhdpi(15),m_paint);
                break;
            }
            case CICLE_SHOW2:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                float value = m_curTime/CICLE_SHOW2_TIME;

                drawBottomBmpSame(canvas);
                m_paint.reset();
                m_paint.setColor(Color.WHITE);
                m_paint.setAlpha((int) (value*255));
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,ShareData.PxToDpi_xhdpi(15),m_paint);
                break;
            }

            case CICLE_EXTEND2_1:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                float value = m_curTime/CICLE_EXTEND2_1_TIME;

                drawBottomBmpSame(canvas);
                m_paint.reset();
                m_paint.setColor(Color.WHITE);
//                m_paint.setAlpha((int) (value*255));
                m_paint.setAlpha(255);
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,ShareData.PxToDpi_xhdpi(15),m_paint);
                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setAlpha((int) (100*value));
                paint.setAntiAlias(true);
                int radio = (int) (ShareData.PxToDpi_xhdpi(15) + ShareData.PxToDpi_xhdpi(20)*value);
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,radio,paint);
                break;
            }

            case CICLE_EXTEND2_2:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                float value = m_curTime/CICLE_EXTEND2_2_TIME;
                Paint tempPaint = new Paint();
                tempPaint.setColor(Color.BLACK);
                tempPaint.setAlpha((int) (200*value));
                canvas.drawRect(new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),tempPaint);
                canvas.drawBitmap(bmp_hide,null,m_hideRect,null);

                drawBottomBmpSame(canvas);
                m_paint.reset();
                m_paint.setColor(Color.WHITE);
                m_paint.setAlpha(255);
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,ShareData.PxToDpi_xhdpi(15),m_paint);
                drawOutCicle(canvas,value);


//                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
//                float value = m_curTime/CICLE_EXTEND_2_TIME;
//                Paint tempPaint = new Paint();
//                tempPaint.setColor(Color.BLACK);
//                tempPaint.setAlpha((int) (200*value));
//                canvas.drawRect(new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),tempPaint);
//                canvas.drawBitmap(bmp_hide,null,m_hideRect,null);
//                drawBottomBmpSame(canvas);
//                m_paint.reset();
//                m_paint.setColor(Color.WHITE);
//                m_paint.setAlpha(255);
//                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,ShareData.PxToDpi_xhdpi(15),m_paint);
//                drawOutCicle(canvas,value);
                break;
            }
            case TRANSLATE_CENTER:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                Paint tempPaint = new Paint();
                tempPaint.setColor(Color.BLACK);
                tempPaint.setAlpha(200);
                canvas.drawRect(new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),tempPaint);
                canvas.drawBitmap(bmp_hide,null,m_hideRect,null);
                drawBottomBmpSame(canvas);
                break;
            }
            case HIDE_CENTER:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                float value = m_curTime/HIDE_CENTER_TIME;
                Paint tempPaint = new Paint();
                tempPaint.setColor(Color.BLACK);
                tempPaint.setAlpha((int) (200*(1 - value)));

                int scaleDistans = (int) (m_curTime/CICLE_SHOW2_TIME*ShareData.PxToDpi_xhdpi(50));

                int x1 = (int) ((m_width - ShareData.PxToDpi_xhdpi(180))/2f) - scaleDistans;
                int y1 = (int) ((ShareData.PxToDpi_xhdpi(263) - ShareData.PxToDpi_xhdpi(180))/2f) - scaleDistans;
                int width = ShareData.PxToDpi_xhdpi(180) + 2*scaleDistans;
                m_hideRect = new Rect(x1,y1,x1 + width,y1 + width);

                canvas.drawBitmap(bmp_hide,null,m_hideRect,tempPaint);
                canvas.drawRect(new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),tempPaint);
                drawBottomBmpSame(canvas);
                break;
            }
            case FINISH_PAUSH:
            {
                canvas.drawBitmap(bmp_top,null,new Rect(0,0,m_width, ShareData.PxToDpi_xhdpi(263)),null);
                drawBottomBmpSame(canvas);
                break;
            }

        }
    }

    private void drawOutCicle(Canvas canvas,float value)
    {
        Paint paint  = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        int radio = (int) (ShareData.PxToDpi_xhdpi(35) + ShareData.PxToDpi_xhdpi(3)*value);
        int oriAlpha = (int) (100*(1f - value));
        for(int i = 0 ; i < 10 ; i++)
        {
            paint.setAlpha((int) ((10- i)/10f * oriAlpha));
            if(i == 0)
            {
                paint.setAlpha((int) (oriAlpha*8/10f));
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,radio - i,paint);
            }
            else
            {
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,radio + i,paint);
                canvas.drawCircle(m_cicleCenterX,m_cicleCenterY,radio - i,paint);
            }

        }
//        paint.setAlpha((int) (255*(0.5f - value)));
    }

    private void drawBottomBmpSame(Canvas canvas)
    {
        canvas.drawBitmap(bmps[0],null,m_rects[0],null);

        if(m_curStep == TRANSLATE_CENTER)
        {
            drawBitmap3(canvas);
            drawBitmap2(canvas);
        }
        else
        {
            drawBitmap2(canvas);
            drawBitmap3(canvas);
        }

        canvas.drawBitmap(bmps[3],null,m_rects[3],null);
        canvas.drawBitmap(bmps[4],null,m_rects[4],null);
    }


    private void drawBitmap2(Canvas canvas)
    {
        if(m_curStep == HIDE_CENTER || m_curStep == TRANSLATE_CENTER || m_curStep == CICLE_EXTEND2_2)
        {
            Paint paint = new Paint();
            if(m_curStep == HIDE_CENTER)
            {
                paint.setAntiAlias(true);
                paint.setAlpha((int) (255*(HIDE_CENTER_TIME - m_curTime)/HIDE_CENTER_TIME));
            }
            canvas.drawBitmap(bmp_select1,null,m_rects[1],paint);
            if(m_curStep == TRANSLATE_CENTER)
            {
                int centerX = m_rects[1].left + ShareData.PxToDpi_xhdpi(70);
                int centerY = m_rects[1].top + ShareData.PxToDpi_xhdpi(70);
                m_paint.setAlpha(255);
                canvas.drawCircle(centerX,centerY,ShareData.PxToDpi_xhdpi(15),m_paint);
            }
        }
        else
        {
            if(m_curStep != FINISH_PAUSH)
                canvas.drawBitmap(bmps[1],null,m_rects[1],null);
        }
    }

    private void drawBitmap3(Canvas canvas)
    {
        if(m_curStep == CICLE_EXTEND_2 || m_curStep == TRANSLATE_LEFT || m_curStep == TRANSLATE_RIGHT)
        {
//            if(m_curStep == CICLE_EXTEND_2)
//            {
//                if(m_curTime > CICLE_EXTEND_2*2/3f)
//                {
//                    float value = m_curTime/CICLE_EXTEND_2;
//                    Paint tempPaint = new Paint();
//                    tempPaint.setAntiAlias(true);
//                    tempPaint.setAlpha((int) (255*value));
//                    canvas.drawBitmap(bmp_select2,null,m_rects[2],tempPaint);
//                }
//                else
//                {
//                    canvas.drawBitmap(bmps[2],null,m_rects[2],null);
//                }
//            }
//            else
//            {
                canvas.drawBitmap(bmp_select2,null,m_rects[2],null);
//            }
        }
        else
        {
            canvas.drawBitmap(bmps[2],null,m_rects[2],null);
        }
        if(m_curStep == TRANSLATE_RIGHT || m_curStep == TRANSLATE_LEFT)
        {
            int centerX = m_rects[2].left + ShareData.PxToDpi_xhdpi(70);
            int centerY = m_rects[2].top + ShareData.PxToDpi_xhdpi(70);
            m_paint.setAlpha(255);
            canvas.drawCircle(centerX,centerY,ShareData.PxToDpi_xhdpi(15),m_paint);
        }
    }

    private void stopAnim()
    {
        if(m_timer != null)
        {
            m_timer.cancel();
            m_timer = null;
        }

        if(m_task != null)
        {
            m_task.cancel();
            m_task = null;
        }
    }

    public void clearAll()
    {
        stopAnim();
        if(bmps != null && bmps.length > 0)
        {
            for(int i = 0; i < bmps.length ; i++)
            {
               if(bmps[i] != null)
               {
                   bmps[i].recycle();
                   bmps[i] = null;
               }
            }
        }

        if(bmp_hide != null)
        {
            bmp_hide.recycle();
            bmp_hide = null;
        }

        if(bmp_top != null)
        {
            bmp_top.recycle();
            bmp_top = null;
        }

        if(bmp_select1 != null)
        {
            bmp_select1.recycle();
            bmp_select1 = null;
        }

        if(bmp_select2 != null)
        {
            bmp_select2.recycle();
            bmp_select2 = null;
        }
    }
}
