package cn.poco.camera.view;

import android.content.Context;
import android.graphics.Color;
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
public class BottomGuideView extends LinearLayout {

	private Context mContext;

	private TextView mTextView;

	public BottomGuideView(Context context) {
		super(context);

		mContext = context;

		initViews();
	}

	private void initViews() {
		setOrientation(VERTICAL);

		mTextView = new TextView(mContext);
		mTextView.setTextColor(Color.WHITE);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setIncludeFontPadding(false);
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		addView(mTextView, params);

		ImageView imageView = new ImageView(mContext);
		imageView.setImageResource(R.drawable.video_tutorial_arrow);
		imageView.setRotation(180);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.topMargin = ShareData.PxToDpi_xhdpi(20);
		addView(imageView, params);
	}

	public void setTip(@StringRes int res) {
		mTextView.setText(res);
	}
}
