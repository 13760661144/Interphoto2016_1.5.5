package cn.poco.video.videoAlbum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by lgd on 2018/1/5.
 */

public class TutorialView extends FrameLayout
{
    private ImageView mArrow1;
    private ImageView mArrow2;
    private TextView mText1;
    private TextView mText2;
    private Paint mShadowPaint;
    public TutorialView(@NonNull Context context)
    {
        super(context);
        setWillNotDraw(false);
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(0x99000000);
        init();
    }

    private void init()
    {
        LayoutParams fl;
        LinearLayout.LayoutParams ll;
        LinearLayout parent1 = new LinearLayout(getContext());
        parent1.setOrientation(LinearLayout.HORIZONTAL);
        parent1.setGravity(Gravity.CENTER);
        parent1.setMinimumHeight(ShareData.PxToDpi_xhdpi(96));
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.topMargin = ShareData.PxToDpi_xhdpi(80);
        fl.leftMargin = ShareData.PxToDpi_xhdpi(76);
        addView(parent1,fl);
        {
            mArrow1 = new ImageView(getContext());
            mArrow1.setImageResource(R.drawable.video_tutorial_arrow);
            mArrow1.setRotation(-90);
            mArrow1.setMinimumWidth(ShareData.PxToDpi_xhdpi(100));
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.CENTER;
            parent1.addView(mArrow1,ll);

            mText1 = new TextView(getContext());
            mText1.setText(R.string.press_to_select);
            mText1.setTextColor(Color.WHITE);
            mText1.setTextSize(TypedValue.COMPLEX_UNIT_DIP,13f);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.CENTER;
            ll.leftMargin = ShareData.PxToDpi_xhdpi(10);
            parent1.addView(mText1,ll);
        }


        LinearLayout parent2 = new LinearLayout(getContext());
        parent2.setMinimumWidth(ShareData.m_screenWidth/3);
        parent2.setOrientation(LinearLayout.VERTICAL);
        parent2.setGravity(Gravity.CENTER);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.topMargin = ShareData.PxToDpi_xhdpi(80+20)+ShareData.m_screenWidth/6;
        addView(parent2,fl);
        {
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.CENTER;
            mArrow2 = new ImageView(getContext());
            mArrow2.setImageResource(R.drawable.video_tutorial_arrow);
            parent2.addView(mArrow2,ll);

            mText2 = new TextView(getContext());
            mText2.setText(R.string.press_to_preview);
            mText2.setTextColor(Color.WHITE);
            mText2.setTextSize(TypedValue.COMPLEX_UNIT_DIP,13f);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.topMargin = ShareData.PxToDpi_xhdpi(10);
            ll.gravity = Gravity.CENTER;
            parent2.addView(mText2,ll);
        }


    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, ShareData.m_screenWidth, ShareData.PxToDpi_xhdpi(80), mShadowPaint);
        canvas.drawRect(ShareData.m_screenWidth/3, ShareData.PxToDpi_xhdpi(80), ShareData.m_screenWidth, ShareData.PxToDpi_xhdpi(80) + ShareData.m_screenWidth/3, mShadowPaint);
        canvas.drawRect(0, ShareData.PxToDpi_xhdpi(80) + ShareData.m_screenWidth/3, ShareData.m_screenWidth, ShareData.m_screenHeight, mShadowPaint);

    }
}
