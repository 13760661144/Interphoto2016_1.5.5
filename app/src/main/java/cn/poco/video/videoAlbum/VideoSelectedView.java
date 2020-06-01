package cn.poco.video.videoAlbum;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by lgd on 2018/1/5.
 */

public class VideoSelectedView extends FrameLayout
{
    private TextView mNext;
    private TextView mNum;
    private TextView mTime;
//    private Paint mPaint;
    public VideoSelectedView(@NonNull Context context)
    {
        super(context);
        init();
    }

    private void init()
    {
        setBackgroundColor(0xf21c1c1c);


//        setWillNotDraw(false);
//        mPaint = new Paint();
//        mPaint.setAntiAlias(true);
//        mPaint.setColor(0xf21c1c1c);
//        mPaint.setShadowLayer(30,20,20,0x66000000);
//        setLayerType(LAYER_TYPE_SOFTWARE, null);
//        setBackgroundColor(0x99111111);
//        setBackgroundColor(0xff1c1c1c);
//        getBackground().setAlpha((int) (0.95f*255));

        LayoutParams fl;
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        fl.rightMargin = ShareData.PxToDpi_xhdpi(40);
        mNext = new TextView(getContext());
        mNext.setTextColor(Color.BLACK);
        mNext.setGravity(Gravity.CENTER);
        mNext.setBackgroundColor(0xffffc433);
        mNext.setMinWidth(ShareData.PxToDpi_xhdpi(150));
        mNext.setMinHeight(ShareData.PxToDpi_xhdpi(64));
        mNext.setText(R.string.video_next);
        addView(mNext,fl);

        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.topMargin = ShareData.PxToDpi_xhdpi(26);
        fl.leftMargin = ShareData.PxToDpi_xhdpi(31);
        mTime = new TextView(getContext());
        mTime.setText(getResources().getText(R.string.video_selected_time,"0"));
        mTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
        mTime.setTextColor(Color.WHITE);
        addView(mTime,fl);

        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.BOTTOM);
        fl.bottomMargin = ShareData.PxToDpi_xhdpi(26);
        fl.leftMargin = ShareData.PxToDpi_xhdpi(31);
        mNum = new TextView(getContext());
        mNum.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12);
        mNum.setText(getResources().getString(R.string.video_selected_num,0));
        mNum.setTextColor(0xffaaaaaa);
        addView(mNum,fl);
    }

    public void setDate(int num,int time)
    {
//        long minute = (time / 60000) % 60;
//        long second = time/1000 % 60;
//        long tenthSecond= (time/100)%10;
//        String s = String.format(Locale.getDefault(), "%02d:%02d.%1d", minute, second,tenthSecond);
        mNum.setText(getResources().getString(R.string.video_selected_num,num));
        mTime.setText(getResources().getString(R.string.video_selected_time,VideoAlbumUtils.getData(time)));
    }

    public void setOnNextClickListener(OnClickListener clickListener)
    {
        mNext.setOnClickListener(clickListener);
    }



    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
//        canvas.drawRect(0,0,getWidth(),getHeight(),mPaint);
    }
    public void setNextBtnClickable(boolean isShow)
    {
        if(isShow){
            mNext.setClickable(true);
        }else{
            mNext.setClickable(false);
        }
    }

}
