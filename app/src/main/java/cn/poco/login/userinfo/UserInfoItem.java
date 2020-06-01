package cn.poco.login.userinfo;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

import static cn.poco.interphoto2.R.id.login_userinfo_item_arrow;
import static cn.poco.interphoto2.R.id.login_userinfo_item_title;

public class UserInfoItem extends RelativeLayout
{
	protected TextView m_title;
	protected TextView m_info;
	protected ImageView m_nextArrow;
	public UserInfoItem(Context context)
	{
		super(context);
		initUI();
	}

	protected void initUI()
	{
		this.setBackgroundColor(0x4d000000);
		this.setMinimumHeight(ShareData.PxToDpi_xhdpi(100));
		LayoutParams rl;
		m_title = new TextView(getContext());
		m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, UserInfoPage.TEXT_SIZE);
		m_title.setTextColor(0xffaaaaaa);
		m_title.setId(login_userinfo_item_title);
		rl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
		rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rl.leftMargin = ShareData.PxToDpi_xhdpi(40);
		m_title.setLayoutParams(rl);
		this.addView(m_title);

		m_nextArrow = new ImageView(getContext());
//			m_nextArrow.setImageResource(R.drawable.setting_arrow);
		m_nextArrow.setImageResource(R.drawable.framework_right_arrow);
		m_nextArrow.setId(login_userinfo_item_arrow);
		rl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
		rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rl.rightMargin = ShareData.PxToDpi_xhdpi(40);
		m_nextArrow.setLayoutParams(rl);
		this.addView(m_nextArrow);

		m_info = new TextView(getContext());
		m_info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//		m_info.setTextColor(0xffaaaaaa);
		m_info.setTextColor(Color.WHITE);
		m_info.setSingleLine();
		m_info.setEllipsize(TextUtils.TruncateAt.END);
		m_info.setGravity(Gravity.RIGHT);
		rl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
//		rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rl.addRule(RelativeLayout.LEFT_OF, login_userinfo_item_arrow);
		rl.addRule(RelativeLayout.RIGHT_OF, login_userinfo_item_title);
		rl.rightMargin = ShareData.PxToDpi_xhdpi(20);
		rl.leftMargin = ShareData.PxToDpi_xhdpi(50);
		m_info.setLayoutParams(rl);
		this.addView(m_info);
	}

	public void setTitle(String title)
	{
		m_title.setText(title);
	}

	public void setInfo(String info)
	{
		m_info.setText(info);
	}

	public void isShowArrow(boolean isShow)
	{
		if(isShow){
			m_nextArrow.setVisibility(View.VISIBLE);
		}else{
			m_nextArrow.setVisibility(View.GONE);
		}
	}

}