package cn.poco.beautify;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ImageUtils;
import cn.poco.tianutils.ShareData;

/**
 * Created by admin on 2016/5/24.
 */
public class WaitDialog1 extends Dialog
{
	private TextView m_text;
	private String mMessage;

	public WaitDialog1(Context context)
	{
		super(context);
	}

	public WaitDialog1(Context context, int theme)
	{
		super(context, theme);
	}

	public void setMessage(String message) {
		mMessage = message;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		FrameLayout fr = new FrameLayout(getContext());
		fr.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		setContentView(fr);

		LinearLayout container = new LinearLayout(getContext());
		container.setGravity(Gravity.CENTER);
		Bitmap bmp = ImageUtils.MakeColorRoundBmp(0xb3000000, ShareData.PxToDpi_xhdpi(300), ShareData.PxToDpi_xhdpi(120), 10);
		container.setBackgroundDrawable(new BitmapDrawable(bmp));
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(300), ShareData.PxToDpi_xhdpi(120));
		fl.gravity = Gravity.CENTER;
		container.setLayoutParams(fl);
		fr.addView(container);

		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(40));
		ll.gravity = Gravity.CENTER;
		ProgressBar wait = new ProgressBar(getContext());
		wait.setIndeterminateDrawable(getContext().getResources().getDrawable(R.drawable.photofactory_progress));
		wait.setLayoutParams(ll);
		container.addView(wait);

		m_text = new TextView(getContext());
		if (mMessage == null) {
			m_text.setText(R.string.saving);
		} else {
			m_text.setText(mMessage);
		}
		m_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
		m_text.setTextColor(Color.WHITE);
		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		ll.leftMargin = ShareData.PxToDpi_xhdpi(25);
		m_text.setLayoutParams(ll);
		container.addView(m_text);

		this.setCancelable(false);
	}

	public void setText(String text)
	{
		m_text.setText(text);
	}
}
