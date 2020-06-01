package cn.poco.video.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by admin on 2018/1/23.
 */

public class WatermarkScrollView extends FrameLayout implements View.OnTouchListener, GestureDetector.OnGestureListener {


    //构建手势探测器
    GestureDetector mygesture = new GestureDetector(this);
    private String TAG = "sssssd";
    private float mPosX, mPosY, mCurPosX, mCurPosY;

    public WatermarkScrollView(Context context) {
        this(context, null);
    }

    public WatermarkScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatermarkScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //设置Touch监听
        this.setOnTouchListener(this);

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        return mygesture.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {

        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {


    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {

        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {


        if (distanceY > 0) {//上移

            Log.i(TAG, "onScroll: " +  this.getTranslationY());
        } else {
        //    this.scrollBy(0, (int) distanceY);
           // Log.i(TAG, "onScroll: " + getScrollY());
            Log.i(TAG, "onScroll: " +  this.getTranslationY());
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {


        return false;
    }


}
