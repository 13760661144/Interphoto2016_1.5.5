package cn.poco.capture2.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.poco.camera.CameraConfig;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/12/28
 */
public class SettingsToastView extends FrameLayout {

	private Context mContext;

	private ImageView mSizeView;
	private TextView mDurationView;

	private int mSize = CameraConfig.SIZE_16_9;
	private int mDuration = CameraConfig.DURATION_10S;

	public SettingsToastView(Context context) {
		super(context);

		mContext = context;

		initViews();
	}

	private void initViews() {
		setBackgroundResource(R.drawable.camera_settings_toast_bg);

		LayoutParams params;

		mSizeView = new ImageView(mContext);
		mSizeView.setImageResource(R.drawable.camera_size_16_9_unselect);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(32), ShareData.PxToDpi_xhdpi(32));
		params.gravity = Gravity.CENTER_VERTICAL;
		params.leftMargin = ShareData.PxToDpi_xhdpi(24);
		addView(mSizeView, params);

		View divide = new View(mContext);
		divide.setBackgroundColor(0x1fffffff);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(2), ShareData.PxToDpi_xhdpi(30));
		params.gravity = Gravity.CENTER;
		addView(divide, params);

		mDurationView = new TextView(mContext);
		mDurationView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		mDurationView.setTextColor(Color.WHITE);
		mDurationView.setGravity(Gravity.CENTER);
		mDurationView.setText(R.string.camera_duration_10);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(79), ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
		addView(mDurationView, params);
	}

	public void setSize(int size) {
		if (mSize != size) {
			mSize = size;
			switch (mSize) {
				case CameraConfig.SIZE_9_16:
					mSizeView.setImageResource(R.drawable.camera_size_9_16_unselect);
					break;
				case CameraConfig.SIZE_16_9:
					mSizeView.setImageResource(R.drawable.camera_size_16_9_unselect);
					break;
				case CameraConfig.SIZE_235_1:
					mSizeView.setImageResource(R.drawable.camera_size_235_1_unselect);
					break;
				case CameraConfig.SIZE_1_1:
					mSizeView.setImageResource(R.drawable.camera_size_1_1_unselect);
					break;
			}
		}
	}

	public void setDuration(int duration) {
		if (mDuration != duration) {
			mDuration = duration;
			switch (mDuration) {
				case CameraConfig.DURATION_10S:
					mDurationView.setText(R.string.camera_duration_10);
					break;
				case CameraConfig.DURATION_30S:
					mDurationView.setText(R.string.camera_duration_30);
					break;
				case CameraConfig.DURATION_60S:
					mDurationView.setText(R.string.camera_duration_60);
					break;
				case CameraConfig.DURATION_180S:
					mDurationView.setText(R.string.camera_duration_180);
					break;
			}
		}
	}
}
