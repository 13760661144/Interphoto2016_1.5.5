package cn.poco.video.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by admin on 2018/1/24.
 */

public class LogoTextView extends View {
    private Paint mPaint;
    private int d = 2;//间距
    private int ii = 0;//头间距
    private String mText;
    private int h;

    public LogoTextView(Context context) {
        super(context);
        initPaint();

    }

    public LogoTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public LogoTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }
    private void initPaint(){
   //     mTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/PingFangRegular.ttf");
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(0xfffefefe);
        mPaint.setShadowLayer(2, (float) 1.75, 1, 0x33221e1f);
      //  mPaint.setTypeface(mTypeface);
        mPaint.setTextSize(22);

    }
    public String getText(){
        return mText;
    }
    public void setText(String s){
        this.mText = s;
        this.requestLayout();
    }
    @Override
    protected void onDraw(Canvas canvas) {

        String str = String.valueOf(getText());

        char chr = 0;
        for (int i = 0; i < str.length(); i++) {
            chr= str.charAt(i);
            canvas.drawText(String.valueOf(chr),ii ,getFontLeading2(mPaint), mPaint);
           // canvas.drawText(String.valueOf(chr),ii ,getFontLeading(mPaint)-10, mPaint);
            ii += getFontlength(mPaint, String.valueOf(chr))+d;
        }
        ii = 0;

    }
    public float getFontlength(Paint paint, String str) {
        return paint.measureText(str);
    }

    public float getFontLeading(Paint paint) {
        return paint.descent() - paint.ascent();
    }
    public float getFontLeading2(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        float textY = h/2 + (fm.descent - fm.ascent) / 2 - fm.descent;
        return textY;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        String str = getText();
        h = (int) getFontLeading(mPaint)+9  ;
        if (str != null){
            int size = str.length();
            int w =0;
            for (int i = 0; i <size ; i++) {
                char c = str.charAt(i);
                w =  w +(int) (getFontlength(mPaint, String.valueOf(c))+2);
            }
            setMeasuredDimension(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
        }

    }
}
