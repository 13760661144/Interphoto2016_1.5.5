package cn.poco.capture2.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/12/29
 */
public class RecordTip extends LinearLayout {

	private Context mContext;

	private RedCircleView mRedCircleView;
	private TextView mTimeView;

	private ValueAnimator mValueAnimator;

	private boolean isRecord;

	private long mInitTime = 0;

	public RecordTip(Context context) {
		super(context);

		mContext = context;
		initViews();
	}

	private void initViews() {

		setOrientation(HORIZONTAL);

		LayoutParams params;

		mRedCircleView = new RedCircleView(mContext);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(16), ShareData.PxToDpi_xhdpi(16));
		params.gravity = Gravity.CENTER_VERTICAL;
		addView(mRedCircleView, params);

		mTimeView = new TextView(mContext);
		mTimeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		mTimeView.setIncludeFontPadding(false);
		mTimeView.setTextColor(Color.WHITE);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		params.leftMargin = ShareData.PxToDpi_xhdpi(10);
		addView(mTimeView, params);

		setVisibility(INVISIBLE);
	}

	public void startRecord() {
		if (isRecord) {
			return;
		}

		setVisibility(VISIBLE);
		isRecord = true;
		mTimeView.setText(formatTime(mInitTime));
		mValueAnimator = ValueAnimator.ofFloat(1f, 0.2f);
		mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float value = (float)animation.getAnimatedValue();
				mRedCircleView.setAlpha(value);
			}
		});
		mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
		mValueAnimator.setRepeatMode(ValueAnimator.REVERSE);
		mValueAnimator.setDuration(750);
		mValueAnimator.start();
	}

	public void setTime(long time) {
		if (isRecord) {
			mTimeView.setText(formatTime(time));
		}
	}

	public void setInitTime(long time) {
		mInitTime = time;
	}

	private String formatTime(long time) {
		long minute = (time / 60) % 60;
		long second = time % 60;
		return String.format(Locale.getDefault(), "00:%02d:%02d", minute, second);
	}

	public void stopRecord() {
		if (!isRecord) {
			return;
		}

		isRecord = false;
		setVisibility(GONE);
		release();
	}

	public void release() {
		if (mValueAnimator != null) {
			mValueAnimator.removeAllUpdateListeners();
			mValueAnimator.cancel();
			mValueAnimator = null;
		}
	}
}
