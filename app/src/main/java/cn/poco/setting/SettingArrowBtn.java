package cn.poco.setting;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

public class SettingArrowBtn extends RelativeLayout
{
	public SettingArrowBtn(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	public SettingArrowBtn(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public SettingArrowBtn(Context context) {
		super(context);
		initialize(context);
	}
	
	ImageView mArrowIcon;
	TextView  mTxTitle;
	
	protected void initialize(Context context)
	{
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		mArrowIcon = new ImageView(context);
		mArrowIcon.setImageResource(R.drawable.framework_right_arrow);
		addView(mArrowIcon, params);
		mArrowIcon.setId(R.id.setting_arrow);
		mArrowIcon.setPadding(0, 0, 0, 0);
		
		params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.LEFT_OF, R.id.setting_arrow);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		mTxTitle = new TextView(context);
		mTxTitle.setSingleLine();
		mTxTitle.setGravity(Gravity.RIGHT);
		mTxTitle.setEllipsize(TextUtils.TruncateAt.END);
		addView(mTxTitle, params);
		mTxTitle.setTextColor(0xffffc433);
		mTxTitle.setPadding(0, 0, ShareData.PxToDpi_hdpi(24), 0);
	}
	
//	public void setText(String text)
//	{
//		mTxTitle.setText(text);
//	}
	
	public void setEllipsize(TextUtils.TruncateAt truncate)
	{
		mTxTitle.setEllipsize(truncate);
	}
	
	public void setText(String text)
	{
		if(text != null)
		{
			mTxTitle.setText(text);
		}
	}

	public ImageView getmArrowIcon()
	{
		return mArrowIcon;
	}
}