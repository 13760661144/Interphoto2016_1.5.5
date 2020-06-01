package cn.poco.setting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;

import cn.poco.tianutils.ShareData;

public class SettingGroup extends LinearLayout
{

	public SettingGroup(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize(context);
	}

	public SettingGroup(Context context)
	{
		super(context);
		initialize(context);
	}

	protected ArrayList<SettingItem> mItems = new ArrayList<SettingItem>();

	protected void initialize(Context context)
	{
		setOrientation(LinearLayout.VERTICAL);
	}

	public SettingItem addItem(String title, View button)
	{
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, ShareData.PxToDpi_xhdpi(108));
		SettingItem item = new SettingItem(getContext(), title, button);
		addView(item, params);
		mItems.add(item);
		return item;
	}

	public SettingItem addItem(String title, View button,int textSize)
	{
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, ShareData.PxToDpi_xhdpi(108));
		SettingItem item = new SettingItem(getContext(), title, button);
		//item.setTextStyle(typeface,textSize);
		addView(item, params);
		mItems.add(item);
		return item;
	}


	protected void updateBackground()
	{
		/*int sw = getWidth();
		int sh = getHeight();
		int l = getPaddingLeft();
		int r = getPaddingRight();
		int t = getPaddingTop();
		int b = getPaddingBottom();
		int len = mItems.size();
		int vspace = Utils.getRealPixel(60);
		if(sw > 0 && sh > 0)
		{
			Bitmap bmBack = Bitmap.createBitmap(sw, sh, Config.ARGB_8888);
			Canvas canvas = new Canvas(bmBack);
			Paint paint = new Paint();
			paint.setColor(0xffcdcbcb);
			paint.setStyle(Style.STROKE);
			canvas.drawRoundRect(new RectF(l, t, sw-r-1, sh-b-1), 10, 10, paint);
			for(int i = 1; i < len; i++)
			{
				canvas.drawLine(l, t+i*vspace, sw-r-1, t+i*vspace, paint);
			}
			BitmapDrawable backDrawable = new BitmapDrawable(getResources(), bmBack);
			setBackgroundDrawable(backDrawable);
		}*/

		ShareData.InitData((Activity)getContext());
		int sw = getWidth();
		int sh = getHeight();
		//int l = getPaddingLeft();
		//int r = getPaddingRight();
		//int t = getPaddingTop();
		//int b = getPaddingBottom();
		int len = mItems.size();
		int vspace = ShareData.PxToDpi_xhdpi(108);
		int line = 1;
		if(sw > 0 && sh > 0)
		{
			Bitmap bmBack = Bitmap.createBitmap(sw, sh, Config.ARGB_8888);
			Canvas canvas = new Canvas(bmBack);
			Paint paint = new Paint();
			paint.setStrokeWidth(1);
			for(int i = 0; i < len; i++)
			{
				paint.setColor(0xff1a1a1a);
				paint.setStyle(Style.FILL);
				canvas.drawRect(new RectF(0, (vspace + line)*i, sw, vspace + (vspace+line)*i), paint);

				if(i < len - 1)
				{
					canvas.drawLine(0, vspace + (vspace+line)*i, ShareData.PxToDpi_xhdpi(30), vspace + (vspace+line)*i, paint);
					paint.setColor(0xff333333);
					canvas.drawLine(ShareData.PxToDpi_xhdpi(30), vspace + (vspace+line)*i, sw, vspace + (vspace+line)*i, paint);
				}
			}
			BitmapDrawable backDrawable = new BitmapDrawable(getResources(), bmBack);
			setBackgroundDrawable(backDrawable);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		updateBackground();
	}
}
