package cn.poco.video.videoFeature.cell;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.video.utils.UiUtil;

/**
 * Created by Simon Meng on 2018/1/4.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class FeatureCell extends FrameLayout{
    private int mResId;
    private String mName;

    private Context mContext;

    private ImageView mIcon;
    private TextView mNameView;


    public FeatureCell(@NonNull Context context) {
        super(context);
        mContext = context;
        initView();
    }

    private void initView() {

        LinearLayout Layout = new LinearLayout(getContext());
        Layout.setOrientation(LinearLayout.VERTICAL);
        Layout.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.CENTER;
        Layout.setLayoutParams(fl);
        this.addView(Layout);

        mIcon = new ImageView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mIcon.setLayoutParams(params);
        Layout.addView(mIcon);

        mNameView = new TextView(mContext);
        mNameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,10);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = ShareData.PxToDpi_xhdpi(15);
        mNameView.setLayoutParams(params);
        Layout.addView(mNameView);
    }

    public void setIconRes(int normal, int selected) {
        StateListDrawable stateListDrawable = UiUtil.makeSelector(mContext, normal, selected);
        mIcon.setImageDrawable(stateListDrawable);
    }

    public void setTextColor(int normal, int selected) {
        mNameView.setTextColor(UiUtil.makeColorSelector(mContext, normal, selected));
    }

    public void setFeatureName(String name) {
        mNameView.setText(name);
    }

    public void setSelectState(boolean isSelected) {
        mIcon.setSelected(isSelected);
        mNameView.setSelected(isSelected);
    }




}
