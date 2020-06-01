package cn.poco.album2.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/7/31
 */
public class SelectEffectView extends LinearLayout {

	private static final int COLOR_TEXT_GRAY = 0xff444444;

	private Context mContext;

	private ImageView mImageView;
	private TextView mTextView;

	private boolean mEnable;

	public SelectEffectView(Context context) {
		super(context);

		mContext = context;

		initViews();
	}

	private void initViews() {

		setOrientation(HORIZONTAL);

		LayoutParams params;

		mImageView = new ImageView(mContext);
		mImageView.setImageResource(R.drawable.album_add_copy);
		mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(56), ShareData.PxToDpi_xhdpi(56));
		params.gravity = Gravity.CENTER_VERTICAL;
		addView(mImageView, params);

		mTextView = new TextView(mContext);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		mTextView.setTextColor(0xff444444);
		mTextView.setPadding(ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20), 0);
		mTextView.setGravity(Gravity.CENTER);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		addView(mTextView, params);
	}

	public void setText(@StringRes int textId) {
		mTextView.setText(textId);
	}

	public void setImage(@DrawableRes int imageId) {
		mImageView.setImageResource(imageId);
	}

	public void setMyEnable(boolean enable) {
		if (enable != mEnable) {
			mEnable = enable;

			if (enable) {
				mTextView.setTextColor(Color.WHITE);
			} else {
				mTextView.setTextColor(COLOR_TEXT_GRAY);
			}
		}
	}

	public ImageView getImageView() {
		return mImageView;
	}

	public TextView getTextView() {
		return mTextView;
	}

	public boolean canClick() {
		return mEnable;
	}

	public void setImageAlpha(float alpha) {
		mImageView.setAlpha(alpha);
	}

	public void setImageBitmap(Bitmap bitmap) {
		mImageView.setImageBitmap(bitmap);
	}

	public void setImageResource(@DrawableRes int resId) {
		mImageView.setImageResource(resId);
	}
}
