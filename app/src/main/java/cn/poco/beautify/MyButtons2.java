package cn.poco.beautify;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.tianutils.ShareData;

/**
 * Created by admin on 2016/6/23.
 */
public class MyButtons2 extends FrameLayout
{
	protected ImageView m_img;
	protected TextView m_text;
	protected ImageView m_state;
	protected int m_textSize;

	public MyButtons2(Context context, int res, String text, int bottomMargin, int textSize)
	{
		super(context);
		m_textSize = textSize;
		InitUI(res, text, bottomMargin);
	}

	protected void InitUI(int res, String text, int bottomMargin)
	{
		LinearLayout.LayoutParams ll;
		FrameLayout.LayoutParams fl;

		LinearLayout lin = new LinearLayout(getContext());
		lin.setPadding(0, 0, 0, bottomMargin);
		lin.setGravity(Gravity.CENTER);
		fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		lin.setLayoutParams(fl);
		this.addView(lin);
		{
			if(res != 0)
			{
				m_img = new ImageView(getContext());
				m_img.setImageResource(res);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.rightMargin = ShareData.PxToDpi_xhdpi(15);
				m_img.setLayoutParams(ll);
				lin.addView(m_img);
			}

			m_text = new TextView(getContext());
			m_text.setMaxLines(1);
			m_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, m_textSize);
			m_text.setTextColor(0xffffffff);
			m_text.setText(text);
			ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			m_text.setLayoutParams(ll);
			lin.addView(m_text);
		}

		m_state = new ImageView(getContext());
		m_state.setBackgroundColor(0xffffc433);
		fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(5));
		fl.gravity = Gravity.BOTTOM;
//		ll.topMargin = ShareData.PxToDpi_xhdpi(12);
		m_state.setLayoutParams(fl);
		this.addView(m_state);
	}

	public void OnChoose(boolean choose)
	{
		if(choose)
		{
			m_state.setBackgroundColor(0xffffc433);
		}
		else
		{
			m_state.setBackgroundColor(0x00ffc433);
		}
	}

	public void OnChooseText(boolean choose)
	{
		if(choose)
		{
			m_text.setTextColor(0xffffc433);
		}
		else
		{
			m_text.setTextColor(0xffffffff);
		}
	}
}
