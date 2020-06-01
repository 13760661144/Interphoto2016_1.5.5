package cn.poco.album2.view;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.Gravity;
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
public class EmptyView extends RelativeLayout {

	private Context mContext;

	private TextView mTextView;
	private ImageView mImageView;

	public EmptyView(Context context) {
		super(context);

		mContext = context;

		initViews();
	}

	private void initViews() {
		LayoutParams params;

		mTextView = new TextView(mContext);
		mTextView.setId(R.id.empty_text);
		mTextView.setTextColor(0xff666666);
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		mTextView.setGravity(Gravity.CENTER);
		mTextView.setLineSpacing(ShareData.PxToDpi_xhdpi(8), 1);

		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(mTextView, params);

		mImageView = new ImageView(mContext);
		mImageView.setImageResource(R.drawable.album_empty);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ABOVE, mTextView.getId());
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.bottomMargin = ShareData.PxToDpi_xhdpi(80);
		addView(mImageView, params);
	}

	public void setText(@StringRes int textId) {
		mTextView.setText(textId);
	}
}
