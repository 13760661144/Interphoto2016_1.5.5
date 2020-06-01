package cn.poco.video.helper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Shine on 2017/5/12.
 */

public class RecyclerOnItemClickListener implements RecyclerView.OnItemTouchListener{


    public static class OnItemClickListener {
        public void onItemClick(View view, int position) {

        }

        public void onItemLongClick(View view, int position) {

        }

        public void onLongPressRelease(View view, int position) {

        }

        public void onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

        };

        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

    }


    private Context mContext;
    private GestureDetector mGestureDetector;
    private OnItemClickListener mListener;
    private boolean mIsLongPressed;

    public RecyclerOnItemClickListener(Context context, final RecyclerView rv, OnItemClickListener callBack) {
        this.mContext = context;
        mListener = callBack;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null) {
                    mIsLongPressed = true;
                    mListener.onItemLongClick(childView, rv.getChildAdapterPosition(childView));
                }
            }
        });
    }


    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (mListener != null) {
            mListener.onInterceptTouchEvent(rv ,e);
        }

        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, rv.getChildAdapterPosition(childView));
        } else {
            if (e.getAction() == MotionEvent.ACTION_UP && mIsLongPressed) {
                mListener.onLongPressRelease(childView, rv.getChildAdapterPosition(childView));
                mIsLongPressed = false;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        if (mListener != null) {
            mListener.onTouchEvent(rv, e);
        }

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
