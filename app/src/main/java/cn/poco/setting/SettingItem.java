package cn.poco.setting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.time.format.TextStyle;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

public class SettingItem extends RelativeLayout{

	public SettingItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context);
	}

	public SettingItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	public SettingItem(Context context) {
		super(context);
		initialize(context);
	}
	
	public SettingItem(Context context, String text, View button) {
		super(context);
		initialize(context);
		setText(text);
		if(button != null)
		{
			setButton(button);
		}
	}
	
	TextView mTxTitle;
	RelativeLayout mButtonHolder;
	protected void initialize(Context context)
	{
		ShareData.InitData((Activity)context);
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		mTxTitle = new TextView(context);
		addView(mTxTitle, params);
		mTxTitle.setTextColor(Color.WHITE);
		mTxTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		mTxTitle.setPadding(ShareData.PxToDpi_xhdpi(30), 0, 0, 0);
		mTxTitle.setId(R.id.setting_title);
		
		params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.addRule(RIGHT_OF, R.id.setting_title);
		params.leftMargin = ShareData.PxToDpi_hdpi(18);
		params.rightMargin = ShareData.PxToDpi_hdpi(18);
		mButtonHolder = new RelativeLayout(context);
		addView(mButtonHolder, params);
	}
	public void setTextStyle(Typeface textStyle,int textSize){
		mTxTitle.setTypeface(textStyle);
		mTxTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,textSize);
	}
	
	public void setText(String strText)
	{
		mTxTitle.setText(strText);
	}
	
	public void setButton(View button)
	{
		mButtonHolder.removeAllViews();
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mButtonHolder.addView(button, params);
	}
}
