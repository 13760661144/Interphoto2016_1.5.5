package cn.poco.album2.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/7/31
 */
public class FolderItem extends RelativeLayout {

	private Context mContext;

	public ImageView decorateView;
	public ImageView imageView;
	public TextView folderName;
	public TextView photoNumber;
	public ImageView nextView;

	public FolderItem(@NonNull Context context) {
		super(context);

		mContext = context;

		initViews();
	}

	private void initViews() {
		setPadding(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(20),
				   ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(20));

		LayoutParams params;

		decorateView = new ImageView(mContext);
		decorateView.setImageResource(R.drawable.album_list_decorate);
		decorateView.setId(R.id.iv_decorate);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.leftMargin = ShareData.PxToDpi_xhdpi(5);
		addView(decorateView, params);

		imageView = new ImageView(mContext);
		imageView.setId(R.id.iv_image);
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(100), ShareData.PxToDpi_xhdpi(100));
		params.topMargin = ShareData.PxToDpi_xhdpi(2);
		params.addRule(RelativeLayout.BELOW, decorateView.getId());
		addView(imageView, params);

		RelativeLayout layout = new RelativeLayout(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.leftMargin = ShareData.PxToDpi_xhdpi(40);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.addRule(RelativeLayout.END_OF, imageView.getId());
		addView(layout, params);
		{
			folderName = new TextView(mContext);
			folderName.setId(R.id.tv_folder_name);
			folderName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			folderName.setTextColor(Color.WHITE);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			layout.addView(folderName, params);

			photoNumber = new TextView(mContext);
			photoNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			photoNumber.setTextColor(Color.WHITE);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.topMargin = ShareData.PxToDpi_xhdpi(12);
			params.addRule(RelativeLayout.BELOW, folderName.getId());
			layout.addView(photoNumber, params);
		}

		nextView = new ImageView(mContext);
		nextView.setImageResource(R.drawable.album_next_current);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_END);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		addView(nextView, params);
	}
}
