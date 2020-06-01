package cn.poco.video.view;

import android.content.Context;
import android.graphics.Color;
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


/**
 * Created by admin on 2016/7/27.
 */
public class ActionBar extends FrameLayout{
    public static class onActionbarMenuItemClick {
        public void onItemClick(int id){
        };

    }
    private TextView actionbarTitle;
    private ImageView leftImageBtn;
    private ImageView rightImageBtn;
    private TextView leftTextBtn;
    private TextView rightTextBtn;

    private Context mContext;
    private onActionbarMenuItemClick mListener;

    public final static int LEFT_MENU_ITEM_CLICK = 0;
    public final static int RIGHT_MENU_ITEM_CLICK = 1;

    private static int DEFAULT_COLOR;

    private int mBgColor;

    public ActionBar(Context context) {
        this(context, DEFAULT_COLOR);
    }

    public ActionBar(Context context, int backgroundColor) {
        super(context);
        mBgColor = backgroundColor;
        initView(context);
    }

    private LinearLayout mTitleContainer;
    private void initView(Context context) {
        mContext = context;
        this.setBackgroundColor(mBgColor);

        mTitleContainer = new LinearLayout(context);
        mTitleContainer.setGravity(Gravity.CENTER);
        mTitleContainer.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mTitleContainer.setLayoutParams(params);
        this.addView(mTitleContainer);

        createActionbarTitle(Color.BLACK, -1.0f);
        createTitleIcon();
    }

    public View getTitleView() {
        return mTitleContainer;
    }

    // 设置actionbar的标题.
    public void setUpActionbarTitle(CharSequence title) {
        setUpActionbarTitle(title, Color.BLACK, -1.0f);
    }

    // 设置actionbar的标题，字体大小和颜色.
    public void setUpActionbarTitle(CharSequence title, int textColor, float textSize) {
        if (actionbarTitle == null) {
            createActionbarTitle(textColor, textSize);
        } else {
            actionbarTitle.setTextColor(textColor);
            actionbarTitle.setTextSize(textSize);
        }
        if (actionbarTitle.getVisibility() != VISIBLE) {
            actionbarTitle.setVisibility(VISIBLE);
        }
        mTitleContainer.setAlpha(1);
        actionbarTitle.setAlpha(1);
        actionbarTitle.setText(title);
    }

    public TextView getActionBarTitleView() {
        return actionbarTitle;
    }



    private ImageView mIconView;
    public void setActionbarTitleIconVisibility(int visibility) {
        if (mIconView == null) {
            createTitleIcon();
        }
        mIconView.setVisibility(visibility);
    }

    private void createTitleIcon() {
        mIconView = new ImageView(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        params.leftMargin = ShareData.PxToDpi_xhdpi(5);
        mIconView.setLayoutParams(params);
        mIconView.setPadding(ShareData.PxToDpi_xhdpi(5), ShareData.PxToDpi_xhdpi(5), ShareData.PxToDpi_xhdpi(5), ShareData.PxToDpi_xhdpi(5));
        mIconView.setImageResource(R.drawable.beautify_effect_help_down);
        mIconView.setVisibility(View.GONE);
        mTitleContainer.addView(mIconView);
    }

    public void setActionbarTitleClickListener(OnClickListener onClickListener) {
        if (mTitleContainer != null) {
            mTitleContainer.setOnClickListener(onClickListener);
        }
    }

    // 设置actionbar左边按钮的图片.
    public void setUpLeftImageBtn(int resId) {
        if (leftImageBtn == null) {
            createLeftImageBtn();
        }
        if (leftImageBtn.getVisibility() != VISIBLE) {
            leftImageBtn.setVisibility(View.VISIBLE);
        }

        leftImageBtn.setImageResource(resId);
        leftImageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(LEFT_MENU_ITEM_CLICK);
                }
            }
        });
    }

    // 获取左边Icon按钮的引用
    public ImageView getLeftImageBtn() {
        return leftImageBtn;
    }

    public void setLeftImageBtnVisibility(int visibility) {
        if (leftImageBtn != null) {
            leftImageBtn.setVisibility(visibility);
        }
    }

    // 设置actionbar右边按钮的图片
    public void setUpRightImageBtn(int resId) {
        if (rightImageBtn == null) {
            createRightImageBtn();
        }
        if (rightImageBtn.getVisibility() != VISIBLE) {
            rightImageBtn.setVisibility(View.VISIBLE);
        }

        rightImageBtn.setImageResource(resId);
        rightImageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(RIGHT_MENU_ITEM_CLICK);
                }
            }
        });
    }

    // 获取右边icon按钮的引用
    public ImageView getRightImageBtn() {
        return rightImageBtn;
    }

    public void setRightImageBtnVisibility(int visibility) {
        if (rightImageBtn != null) {
            rightImageBtn.setVisibility(visibility);
        }
    }


    // 设置actionbar左边的文字按钮
    public void setLeftTextBtn(CharSequence content) {
        setLeftTextBtn(content, DEFAULT_COLOR, -1.0f);
    }

    // 设置actionbar左边的文字按钮，并设置颜色和字体大小
    public void setLeftTextBtn(CharSequence content, int color, float size) {
        setLeftTextBtn(content, color, size, 0);
    }

    // 设置actionbar左边的文字按钮，颜色和字体大小,以及距离左边的距离
    public void setLeftTextBtn(CharSequence content, int color, float size, int leftMargin) {
        if (leftTextBtn == null) {
            createLeftTextTBtn(color, size, leftMargin);
        } else {
            leftTextBtn.setTextColor(color);
            leftTextBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)leftTextBtn.getLayoutParams();
            params.leftMargin = leftMargin;
        }
        leftTextBtn.setText(content);
        leftTextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(LEFT_MENU_ITEM_CLICK);
                }
            }
        });
    }

    // 设置actionbar右边的文字按钮.
    public void setRightTextBtn(CharSequence content) {
        setRightTextBtn(content, DEFAULT_COLOR, -1.0f);
    }
    

    // 设置actionbar右边的文字按钮，并设置颜色和字体大小.
    public void setRightTextBtn(CharSequence content, int color, float size) {
        setRightTextBtn(content, color, -1.0f, 0);
    }

    // 设置actionbar右边的文字按钮，设置颜色和字体大小以及距离右边的距离
    public void setRightTextBtn(CharSequence content, int color, float size, int rightMargin) {
        if (rightTextBtn == null) {
            createRightTextBtn(color, size, rightMargin);
        } else {
            rightTextBtn.setTextColor(color);
            rightTextBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)rightTextBtn.getLayoutParams();
            params.rightMargin = rightMargin;
        }
        rightTextBtn.setText(content);
        rightTextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(RIGHT_MENU_ITEM_CLICK);
                }
            }
        });
    }


    private void createActionbarTitle(int textColor, float textSize) {
        actionbarTitle = new TextView(mContext);
        actionbarTitle.setGravity(Gravity.CENTER);
        actionbarTitle.setTextColor(textColor);

        if (textSize != -1) {
            actionbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        } else {
            actionbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        actionbarTitle.setLayoutParams(layoutParams);
        actionbarTitle.setVisibility(View.GONE);
        mTitleContainer.addView(actionbarTitle);
    }

    private void createLeftImageBtn() {
        leftImageBtn = new ImageView(mContext);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        this.addView(leftImageBtn, layoutParams);
    }

    private void createRightImageBtn() {
        rightImageBtn = new ImageView(mContext);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        this.addView(rightImageBtn, layoutParams);
    }

    private void createLeftTextTBtn(int textColor, float textSize, int leftMargin) {
        leftTextBtn = new TextView(mContext);
        leftTextBtn.setGravity(Gravity.CENTER);
        leftTextBtn.setTextColor(textColor);

        if (textSize != -1.0f) {
        	leftTextBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        } else {
        	leftTextBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        }

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        layoutParams.leftMargin = leftMargin;
        this.addView(leftTextBtn, layoutParams);
    }

    private void createRightTextBtn(int textColor, float textSize, int rightMargin) {
        rightTextBtn = new TextView(mContext);
        rightTextBtn.setPadding(ShareData.PxToDpi_xhdpi(20),ShareData.PxToDpi_xhdpi(20),0,ShareData.PxToDpi_xhdpi(20));
        rightTextBtn.setGravity(Gravity.CENTER);
        rightTextBtn.setTextColor(textColor);

        if (textSize != -1.0f) {
        	rightTextBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        } else {
        	rightTextBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        }

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        layoutParams.rightMargin = rightMargin;
        this.addView(rightTextBtn, layoutParams);
    }

    // 获取actionbar的标题文字.
    public CharSequence getActionbarTitle() {
        return actionbarTitle.getText().toString();
    }

    // 获取actionbar右边文字按钮的引用.
    public TextView getRightTextBtn() {
    	if (rightTextBtn != null) {
    		return rightTextBtn;
    	}
    	return null;
    }


    // 设置actionbar菜单按钮的监听器
    public void setOnActionbarMenuItemClick(onActionbarMenuItemClick listener) {
        this.mListener = listener;
    }

}
