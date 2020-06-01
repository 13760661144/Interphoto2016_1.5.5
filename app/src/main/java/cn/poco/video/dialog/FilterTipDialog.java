package cn.poco.video.dialog;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.FullScreenDlg;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2018/2/3
 */
public class FilterTipDialog extends FullScreenDlg {

	private Context mContext;
	private LinearLayout mRootLayout;

	private View.OnClickListener mOnClickListener;

	private boolean hasChecked = false;

	public FilterTipDialog(Activity activity) {
		this(activity, R.style.backDialog);
	}

	public FilterTipDialog(Activity activity, int theme) {
		super(activity, theme);
		mContext = activity;

		setCancelable(false);

		FrameLayout.LayoutParams fl;
		m_fr.setBackgroundColor(0x77000000);

		int viewW = ShareData.PxToDpi_xhdpi(620);
		mRootLayout = new LinearLayout(mContext);
		mRootLayout.setBackgroundColor(0xff404040);
		mRootLayout.setOrientation(LinearLayout.VERTICAL);
		mRootLayout.setClickable(true);
		fl = new FrameLayout.LayoutParams(viewW, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		AddView(mRootLayout, fl);

		initViews();
	}

	private void initViews() {

		LinearLayout.LayoutParams params;
		FrameLayout center = new FrameLayout(mContext);
		params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(200));
		mRootLayout.addView(center, params);
		{
			TextView message = new TextView(mContext);
			message.setPadding(ShareData.PxToDpi_xhdpi(48), 0, ShareData.PxToDpi_xhdpi(48), 0);
			message.setText(R.string.filter_tip);
			message.setTextColor(0xffa6a6a6);
			message.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

			FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.gravity = Gravity.CENTER;
			center.addView(message, params1);
		}

		CheckBox checkBox = new CheckBox(mContext);
		checkBox.setText(R.string.filter_tip_close);
		checkBox.setTextColor(0xffa6a6a6);
		params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.leftMargin = ShareData.PxToDpi_xhdpi(50);
		mRootLayout.addView(checkBox, params);
		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				hasChecked = isChecked;
			}
		});

		TextView okView = new TextView(mContext);
		okView.setTextColor(0xffa6a6a6);
		okView.setText(R.string.ok2);
		okView.setGravity(Gravity.CENTER);
		okView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		okView.setBackgroundColor(0xff272727);
		params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(90));
		params.topMargin = ShareData.PxToDpi_xhdpi(32);
		mRootLayout.addView(okView, params);
		okView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (hasChecked) {
					TagMgr.SetTag(mContext, Tags.VIDEO_FILTER_TIP);
				}
				if (mOnClickListener != null) {
					mOnClickListener.onClick(v);
				}
			}
		});
	}

	public void setOnClickListener(View.OnClickListener listener) {
		mOnClickListener = listener;
	}
}
