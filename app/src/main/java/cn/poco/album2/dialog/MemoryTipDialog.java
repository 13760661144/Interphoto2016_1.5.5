package cn.poco.album2.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/3/7
 */
public class MemoryTipDialog extends Dialog implements View.OnClickListener {

	private TextView mCheckView;
	private TextView mClearView;

	private OnClickListener mOnClickListener;

	public MemoryTipDialog(@NonNull Context context) {
		super(context);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setCancelable(false);

		initViews();
	}

	private void initViews() {
		LinearLayout layout = new LinearLayout(getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setBackgroundColor(0xff404040);
		setContentView(layout);

		FrameLayout center = new FrameLayout(getContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(178));
		layout.addView(center, params);
		{
			TextView messageView = new TextView(getContext());
			messageView.setPadding(ShareData.PxToDpi_xhdpi(32), 0, ShareData.PxToDpi_xhdpi(32), 0);
			messageView.setIncludeFontPadding(false);
			messageView.setTextColor(Color.WHITE);
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			messageView.setLineSpacing(0, 1.3f);
			messageView.setText(R.string.no_enough_memory);
			FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.gravity = Gravity.CENTER;
			center.addView(messageView, params1);
		}

		View line = new View(getContext());
		line.setBackgroundColor(0xff3b3b3b);
		params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
		layout.addView(line, params);

		LinearLayout horizonLayout = new LinearLayout(getContext());
		horizonLayout.setOrientation(LinearLayout.HORIZONTAL);
		horizonLayout.setBackgroundColor(0xff272727);
		params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(90));
		layout.addView(horizonLayout, params);
		{
			mCheckView = new TextView(getContext());
			mCheckView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			mCheckView.setTextColor(0xffffcf56);
			mCheckView.setText(R.string.check_memory);
			mCheckView.setGravity(Gravity.CENTER);
			params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
			params.weight = 1;
			horizonLayout.addView(mCheckView, params);
			mCheckView.setOnClickListener(this);

			View line2 = new View(getContext());
			line2.setBackgroundColor(0xff3b3b3b);
			params = new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);
			horizonLayout.addView(line2, params);

			mClearView = new TextView(getContext());
			mClearView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			mClearView.setTextColor(0xffffcf56);
			mClearView.setText(R.string.clear_interphoto);
			mClearView.setGravity(Gravity.CENTER);
			params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
			params.weight = 1;
			horizonLayout.addView(mClearView, params);
			mClearView.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mCheckView) {
			if (mOnClickListener != null) {
				mOnClickListener.onCheck();
			}
		} else if (v == mClearView) {
			if (mOnClickListener != null) {
				mOnClickListener.onClear();
			}
		}
	}

	public void setOnClickListener(OnClickListener listener) {
		mOnClickListener = listener;
	}

	public interface OnClickListener {
		void onCheck();
		void onClear();
	}
}
