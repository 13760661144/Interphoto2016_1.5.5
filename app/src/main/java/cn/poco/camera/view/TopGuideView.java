package cn.poco.camera.view;

import android.content.Context;
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
 * Date: 2017/11/24
 */
public class TopGuideView extends LinearLayout {

	private Context mContext;

	private ImageView mImageView;
	private TextView mTextView;

	public TopGuideView(Context context) {
		super(context);

		mContext = context;

		initViews();
	}

	private void initViews() {
		setOrientation(VERTICAL);

		mImageView = new ImageView(mContext);
		mImageView.setImageResource(R.drawable.video_tutorial_arrow);
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		addView(mImageView, params);

		mTextView = new TextView(mContext);
		mTextView.setTextColor(Color.WHITE);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setIncludeFontPadding(false);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.topMargin = ShareData.PxToDpi_xhdpi(20);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		addView(mTextView, params);
	}

	public void setTip(@StringRes int res) {
		mTextView.setText(res);
	}

	public void setImage(@DrawableRes int res) {
		mImageView.setImageResource(res);
	}
}
