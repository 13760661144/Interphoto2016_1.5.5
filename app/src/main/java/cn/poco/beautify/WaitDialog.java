package cn.poco.beautify;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

public class WaitDialog extends Dialog
{
	public WaitDialog(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}
	public WaitDialog(Context context, int theme)
	{
		super(context, theme);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		FrameLayout fr = new FrameLayout(getContext());
		fr.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));
		setContentView(fr);
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(70), ShareData.PxToDpi_xhdpi(70));
		fl.gravity = Gravity.CENTER;
		int padding = ShareData.PxToDpi_xhdpi(10);
		ProgressBar wait = new ProgressBar(getContext());
		wait.setPadding(padding, padding, padding, padding);
		wait.setIndeterminateDrawable(getContext().getResources().getDrawable(R.drawable.photofactory_progress));
		wait.setLayoutParams(fl);
		fr.addView(wait);
		
		this.setCancelable(false);
	}
}
