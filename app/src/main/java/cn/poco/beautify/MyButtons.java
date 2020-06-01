package cn.poco.beautify;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

public class MyButtons extends RelativeLayout
{
	protected int def_res_out;
	protected int def_res_over;
	protected ImageView m_img;
	protected TextView m_text;
	protected ImageView m_newIcon;
	protected int m_defNewRes = R.drawable.beauty_new_tip_icon;
	protected boolean m_hasNew = true;

	private boolean mEnable = true;
	
	public MyButtons(Context context, int res_out, int res_over)
	{
		super(context);
		def_res_out = res_out;
		def_res_over = res_over;
		initUI(res_out);
	}
	
	protected void initUI(int res)
	{
		LinearLayout.LayoutParams ll;
		RelativeLayout.LayoutParams rl;

		LinearLayout Layout = new LinearLayout(getContext());
		Layout.setOrientation(LinearLayout.VERTICAL);
		Layout.setGravity(Gravity.CENTER);
		Layout.setId(R.id.beautify_img);
		rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.CENTER_IN_PARENT);
		Layout.setLayoutParams(rl);
		this.addView(Layout);
		
		m_img = new ImageView(getContext());
		m_img.setScaleType(ScaleType.CENTER);
		m_img.setImageResource(res);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		m_img.setLayoutParams(ll);
		Layout.addView(m_img);

		m_text = new TextView(getContext());
		m_text.setVisibility(View.GONE);
		m_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		m_text.setTextColor(0xffffffff);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.topMargin = ShareData.PxToDpi_xhdpi(15);
		m_text.setLayoutParams(ll);
		Layout.addView(m_text);
		
		m_newIcon = new ImageView(getContext());
		m_newIcon.setVisibility(View.GONE);
		m_newIcon.setImageResource(m_defNewRes);
		rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.ABOVE, R.id.beautify_img);
		rl.addRule(RelativeLayout.RIGHT_OF, R.id.beautify_img);
		rl.bottomMargin = -ShareData.PxToDpi_hdpi(6);
		rl.leftMargin = rl.topMargin;
		m_newIcon.setLayoutParams(rl);
		this.addView(m_newIcon);
	}
	
	public void SetNew(boolean isNew)
	{
		m_hasNew = isNew;
		if(m_hasNew && m_newIcon != null)
		{
			m_newIcon.setVisibility(View.VISIBLE);
			m_newIcon.setImageResource(m_defNewRes);
		}
		else if(m_newIcon != null)
		{
			m_newIcon.setVisibility(View.GONE);
		}
	}

	public void SetSelect(boolean select)
	{
		if(select)
		{
			m_img.setImageResource(def_res_over);
		}
		else
		{
			m_img.setImageResource(def_res_out);
		}
	}

	public void SetText(String text)
	{
		m_text.setVisibility(View.VISIBLE);
		m_text.setText(text);
	}

	public void SetEnable(boolean enable) {
		if (mEnable != enable) {
			mEnable = enable;
			setEnabled(enable);
			if (enable) {
				setAlpha(1f);
			} else {
				setAlpha(0.2f);
			}
		}
	}
}
