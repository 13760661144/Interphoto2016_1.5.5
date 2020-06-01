package cn.poco.video.videoFeature.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by admin on 2018/1/18.
 */

public class TimeBar extends LinearLayout {
    private int mDuration;
    private int mWidth;
    private int mTotalItems = 6;

    public TimeBar(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        init(context);
    }

    private void init(Context context) {
        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mWidth = getWidth();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (mDuration > 0) {
                    initTimeItems();
                }
            }
        });
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
        if (mWidth > 0) {
            initTimeItems();
        }
    }

    private String getIndexTime(int index) {
        if (index % 2 == 0) {
            float time = (float) ((float)mDuration /(float) mTotalItems * (float)index / 1000.0);
            int intTime = (int) time;
            if (time > intTime) {
                DecimalFormat df = new DecimalFormat("0.#");
                return df.format(time) + "S";
            } else {
                return intTime + "S";
            }
        } else {
            return "·";
        }
    }

    private void initTimeItems() {
        int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i <= mTotalItems; i++) {
                if (i < childCount && getChildAt(i) instanceof TextView) {
                    ((TextView) getChildAt(i)).setText(getIndexTime(i));
                }
            }
        } else {
            int itemW = mWidth / mTotalItems;
            for (int i = 0; i <= mTotalItems; i++) {
                int w = (i == 0 || i == mTotalItems - 1) ? itemW / 2 : itemW;
                int gravity = (i == 0) ? (Gravity.LEFT | Gravity.CENTER_VERTICAL) : (i == mTotalItems || i == mTotalItems - 1) ? (Gravity.RIGHT | Gravity.CENTER_VERTICAL) : Gravity.CENTER;
                LayoutParams params = new LayoutParams(w, LayoutParams.WRAP_CONTENT);
                TextView textView = new TextView(getContext());
                textView.setText(getIndexTime(i));
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                textView.setGravity(gravity);
                addView(textView, params);
            }
        }
    }
}
