package cn.poco.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.ui.AlertDialogV1;

/**
 * Created by zwq on 2016/01/14 17:05.<br/><br/>
 */
public class PatchDialog extends AlertDialogV1 implements View.OnClickListener{

    private static final String TAG = PatchDialog.class.getName();

    private int mType;
    private LinearLayout container;
    private LinearLayout.LayoutParams lParams;
    private TextView title;
    private ImageView pic;
    private TextView msg;
    private Button rotateBtn;
    private Button saveBtn;

    private int rotate;
    private boolean canQuit = true;

    public PatchDialog(Context context, int type) {
        super(context);
        mType = type;

        initView();

        initData();
    }

    private void initView() {
        lParams = new LinearLayout.LayoutParams((int) (ShareData.m_screenWidth * 0.8f), LinearLayout.LayoutParams.WRAP_CONTENT);
        container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        addContentView(container, lParams);

        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        title = new TextView(getContext());
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(10));
        container.addView(title, lParams);

        int width = (int) (ShareData.m_screenWidth * 0.4f);
        lParams = new LinearLayout.LayoutParams(width, (int) (width*4.0f/3));
        lParams.gravity = Gravity.CENTER_HORIZONTAL;
        pic = new ImageView(getContext());
        container.addView(pic, lParams);

        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        msg = new TextView(getContext());
        msg.setPadding(ShareData.PxToDpi_xhdpi(15), ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(15), ShareData.PxToDpi_xhdpi(15));
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        msg.setTextSize(16);
        msg.setTextColor(Color.BLACK);
        container.addView(msg, lParams);

        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout btnLayout = new LinearLayout(getContext());
        btnLayout.setPadding(0, 0, 0, ShareData.PxToDpi_xhdpi(15));
        container.addView(btnLayout, lParams);

        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        lParams.leftMargin = ShareData.PxToDpi_xhdpi(15);
        lParams.rightMargin = ShareData.PxToDpi_xhdpi(15);
        rotateBtn = new Button(getContext());
        rotateBtn.setTextSize(18);
        rotateBtn.setOnClickListener(this);
        rotateBtn.setBackgroundDrawable(getShapePressedDrawable(true, true, true, true, 0xff32bea0, 0xff32aea0));
        btnLayout.addView(rotateBtn, lParams);

        lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        lParams.leftMargin = ShareData.PxToDpi_xhdpi(15);
        lParams.rightMargin = ShareData.PxToDpi_xhdpi(15);
        saveBtn = new Button(getContext());
        saveBtn.setTextSize(18);
        saveBtn.setOnClickListener(this);
        saveBtn.setBackgroundDrawable(getShapePressedDrawable(true, true, true, true, 0xff32bea0, 0xff32aea0));
        btnLayout.addView(saveBtn, lParams);

    }

    private void initData() {
        /**
         * 0:
         *  镜头方向校正
         *  请保证手机处于垂直 竖屏状态, 点击开始校正
         *  开始校正
         * 1：
         *  请保持手机垂直
         *  现在看到的预览方向正确吗？
         *  旋转    正确
         * 2：
         *  照片方向校正
         *  pic
         *  现在看到的照片方向正确吗？
         *  旋转    正确
         * 3：
         *  校正完成
         *  关闭
         */
        if (mType == 0) {
            title.setText(getContext().getResources().getString(R.string.lensCorrectionTips));
            pic.setVisibility(View.GONE);
            msg.setText(getContext().getResources().getString(R.string.keepPhoneVerticalTips));
            rotateBtn.setVisibility(View.GONE);
            saveBtn.setText(getContext().getResources().getString(R.string.cameraAdjusting));

        } else if (mType == 1) {
            title.setText(getContext().getResources().getString(R.string.PleasekeepPhoneVericalTips));
            pic.setVisibility(View.GONE);
            msg.setText(getContext().getResources().getString(R.string.lensDirectionCorrectedTips));
            rotateBtn.setText(getContext().getResources().getString(cn.poco.interphoto2.R.string.Rotate));
            saveBtn.setText(getContext().getResources().getString(R.string.correct));

        } else if (mType == 2) {
            title.setText(getContext().getResources().getString(R.string.photocorrection));
            msg.setText(getContext().getResources().getString(R.string.isorientationcorrect));
            rotateBtn.setText(getContext().getResources().getString(R.string.Rotate));
            saveBtn.setText(getContext().getResources().getString(R.string.correct));

        } else if (mType == 3) {
            title.setText(getContext().getResources().getString(R.string.A));
            pic.setVisibility(View.GONE);
            msg.setText(" ");
            rotateBtn.setVisibility(View.GONE);
            saveBtn.setText(getContext().getResources().getString(R.string.Exit));
        }
    }

    public void setPicture(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled())
            return;
        if (pic != null) {
            pic.setImageDrawable(new BitmapDrawable(getContext().getResources(), bitmap));
        }
    }

    public int getRotate() {
        return rotate;
    }

    @Override
    public void onClick(View v) {
        canQuit = false;
        if (v == rotateBtn) {
            if (mType == 2) {
                rotate = (rotate + 90) % 360;
                pic.setRotation(rotate);
                canQuit = true;
            } else {
                if (mListener != null) {
                    mListener.onClick(this, 0);
                }
            }

        } else if (v == saveBtn) {
            if (mType == 3) {
                canQuit = true;
            }
            if (mListener != null) {
                mListener.onClick(this, 1);
            }
            this.dismiss();
        }
    }

    public void setCanQuitPatch(boolean quit) {
        canQuit = quit;
    }

    public boolean canQuitPatch() {
        return canQuit;
    }
}
