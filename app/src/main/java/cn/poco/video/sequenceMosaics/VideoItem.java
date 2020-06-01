package cn.poco.video.sequenceMosaics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.poco.tianutils.ShareData;

/**
 * Created by admin on 2017/10/19.
 */

public class VideoItem extends LinearLayout
{
	public ImageView mThumb;
	public ImageView mIcon;
	public VideoItem(@NonNull Context context)
	{
		super(context);
		setOrientation(HORIZONTAL);
		setPadding(ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(20), ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(20));
		LinearLayout.LayoutParams fl;

		mThumb = new ImageView(getContext());
		fl = new LayoutParams(ShareData.PxToDpi_xhdpi(128), ShareData.PxToDpi_xhdpi(128));
		fl.gravity = Gravity.CENTER_VERTICAL;
		mThumb.setLayoutParams(fl);
		addView(mThumb);

		mIcon = new ImageView(getContext());
		fl = new LayoutParams(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(40));
		fl.gravity = Gravity.CENTER_VERTICAL;
		fl.leftMargin = ShareData.PxToDpi_xhdpi(20);
		mIcon.setLayoutParams(fl);
		addView(mIcon);

	}

	public void setmIconVisible(boolean visible)
	{
		if(visible)
		{
			mIcon.setAlpha(1f);
		}
		else
		{
			mIcon.setAlpha(0f);
		}
	}
}
