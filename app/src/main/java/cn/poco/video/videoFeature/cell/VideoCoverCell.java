package cn.poco.video.videoFeature.cell;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.video.videoFeature.view.SelectedDrawable;

/**
 * Created by Simon Meng on 2018/1/12.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoCoverCell extends FrameLayout{
    private Context mContext;
    private ImageView mCover;
    private TextView mDurationView;

    private LinearLayout mViewContainer;
    private ImageView mAddBtn;
    private boolean mAddBtnShow; //是否需要显示添加按钮

    private boolean mHideAdd;

    public VideoCoverCell(@NonNull Context context) {
        super(context);
        mContext = context;
        initView();
        this.setWillNotDraw(false);
        this.setClickable(true);
    }


    private void initView() {
        FrameLayout.LayoutParams params;
        LinearLayout.LayoutParams paramsLinear;

        mViewContainer = new LinearLayout(mContext);
        mViewContainer.setOrientation(LinearLayout.HORIZONTAL);
//        mViewContainer.setGravity(Gravity.CENTER_VERTICAL);
        params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mViewContainer.setLayoutParams(params);
//        mViewContainer.setPadding(ShareData.PxToDpi_xhdpi(1), ShareData.PxToDpi_xhdpi(1), ShareData.PxToDpi_xhdpi(1) ,ShareData.PxToDpi_xhdpi(1));
        this.addView(mViewContainer);

        LinearLayout container = new LinearLayout(mContext);
        paramsLinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsLinear.gravity = Gravity.CENTER_VERTICAL;
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setLayoutParams(paramsLinear);
        mViewContainer.addView(container);
        {
            mCover = new ImageView(mContext);
            mCover.setBackgroundColor(0x00ffc433);
            mCover.setPadding(ShareData.PxToDpi_xhdpi(4), ShareData.PxToDpi_xhdpi(4), ShareData.PxToDpi_xhdpi(4) ,ShareData.PxToDpi_xhdpi(4));
            paramsLinear = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(160), ShareData.PxToDpi_xhdpi(160));
            mCover.setLayoutParams(paramsLinear);
            container.addView(mCover);

            mDurationView = new TextView(mContext);
            mDurationView.setGravity(Gravity.CENTER);
            mDurationView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            mDurationView.setTextColor(0xff666666);
            paramsLinear = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mDurationView.setLayoutParams(paramsLinear);
            container.addView(mDurationView);
        }

        mAddBtn = new ImageView(mContext);
        mAddBtn.setImageResource(R.drawable.video_transition_addvideo_btn);
        int padding = ShareData.PxToDpi_xhdpi(8);
        mAddBtn.setPadding(padding, padding, padding, padding);
        paramsLinear = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(80), ShareData.PxToDpi_xhdpi(80));
        paramsLinear.gravity = Gravity.TOP;
        paramsLinear.topMargin = ShareData.PxToDpi_xhdpi(40);
        mAddBtn.setLayoutParams(paramsLinear);
        mViewContainer.addView(mAddBtn);
        mAddBtn.setVisibility(View.GONE);

    }

    public ImageView getCover() {
        return mCover;
    }

    public View getViewContainer() {
        return mViewContainer;
    }


    public ImageView getAddBtn() {
        return mAddBtn;
    }

    public TextView getDurationView() {
        return mDurationView;
    }

    public void setVideoDuration(String text) {
        mDurationView.setText(text);
    }

    public void setmIconVisible(boolean visible)
    {
        mAddBtnShow = visible;
        if(visible)
        {
            if(!mHideAdd)
                mAddBtn.setVisibility(View.VISIBLE);
        }
        else
        {

            mAddBtn.setVisibility(View.GONE);
        }
    }

    public void hideAddIcon(boolean hide)
    {
        mHideAdd = hide;
        //这个方法主要用于添加按钮动画，只有当添加按钮可以显示的时候才会有动画效果
        if(mAddBtnShow)
        {
            if(hide)
            {
                mAddBtn.setVisibility(INVISIBLE);
            }
            else
            {
                mAddBtn.setVisibility(VISIBLE);
            }

        }
    }

    public boolean getAddBtnShow()
    {
        return mAddBtnShow;
    }

    public float getAddBtnX()
    {
        return mAddBtn.getX()+ mViewContainer.getX();
    }

    private boolean mIsSelected;
    public void setSelectedEffect(boolean isSelected) {
        this.mIsSelected = isSelected;
        if(mIsSelected)
        {
            mCover.setBackgroundDrawable(new SelectedDrawable(ShareData.PxToDpi_xhdpi(160), ShareData.PxToDpi_xhdpi(160)));
            mDurationView.setTextColor(Color.WHITE);
        }
        else
        {
            mCover.setBackgroundColor(0x00ffc433);
            mDurationView.setTextColor(0xff666666);
        }
    }

}
