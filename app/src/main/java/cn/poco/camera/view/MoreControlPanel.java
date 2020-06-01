package cn.poco.camera.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.camera.CameraConfig;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/10/9
 */
public class MoreControlPanel extends LinearLayout {

	private static final int COLOR_TITLE = 0xff999999;
	private static final int COLOR_NORMAL = Color.WHITE;
	private static final int COLOR_SELECT = 0xffffc433;

	private Context mContext;

	private int mItemHeight;
	private int mChildItemSize;

	private FrameLayout mSizeLayout;
	private ImageView mSize9_16View;
	private ImageView mSize3_4View;
	private ImageView mSize1_1View;

	private FrameLayout mFlashLayout;
	private TextView mAutoView;
	private TextView mOpenView;
	private TextView mCloseView;
	private View mLine;

	private FrameLayout mTimerLayout;
	private TextView mTimer10SView;
	private TextView mTimer3SView;
	private TextView mTimerOffView;

	private FrameLayout mTouchCaptureLayout;
	private TextView mTouchOpenView;
	private TextView mTouchCloseView;

	private int mSize = CameraConfig.SIZE_9_16;
	private int mFlash = CameraConfig.FLASH_AUTO;
	private int mTimer = CameraConfig.TIMER_OFF;
	private boolean isTouchCapture = false;

	private boolean mFlashEnable = true;

	private OnMoreControlListener mControlListener;

	public MoreControlPanel(@NonNull Context context) {
		super(context);

		mContext = context;

		init();
	}

	private void init() {

		mItemHeight = ShareData.PxToDpi_xhdpi(110);
		mChildItemSize = ShareData.PxToDpi_xhdpi(80);

		initViews();
	}

	private void initViews() {
		setClickable(true);
		setBackgroundColor(Color.BLACK);
		setOrientation(VERTICAL);

		LayoutParams params;

		FrameLayout frameLayout = new FrameLayout(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(120));
		addView(frameLayout, params);
		{
			TextView moreText = generateTextView(R.string.camera_more, 16, Color.WHITE, true);
			FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.gravity = Gravity.CENTER_VERTICAL;
			params1.leftMargin = ShareData.PxToDpi_xhdpi(30);
			frameLayout.addView(moreText, params1);
		}

		addLine(0xff999999);

		mSizeLayout = new FrameLayout(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
		addView(mSizeLayout, params);
		{
			TextView title = generateTextView(R.string.camera_size, 12, COLOR_TITLE, false);
			FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.gravity = Gravity.CENTER_VERTICAL;
			params1.leftMargin = ShareData.PxToDpi_xhdpi(30);
			mSizeLayout.addView(title, params1);

			mSize9_16View = new ImageView(mContext);
			mSize9_16View.setImageResource(R.drawable.camera_size_9_16);
			mSize9_16View.setScaleType(ImageView.ScaleType.CENTER);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(250);
			mSizeLayout.addView(mSize9_16View, params1);
			mSize9_16View.setOnClickListener(mOnClickListener);

			mSize3_4View = new ImageView(mContext);
			mSize3_4View.setImageResource(R.drawable.camera_size_3_4_unselect);
			mSize3_4View.setScaleType(ImageView.ScaleType.CENTER);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(130);
			mSizeLayout.addView(mSize3_4View, params1);
			mSize3_4View.setOnClickListener(mOnClickListener);

			mSize1_1View = new ImageView(mContext);
			mSize1_1View.setImageResource(R.drawable.camera_size_1_1_unselect);
			mSize1_1View.setScaleType(ImageView.ScaleType.CENTER);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(10);
			mSizeLayout.addView(mSize1_1View, params1);
			mSize1_1View.setOnClickListener(mOnClickListener);
		}

		mLine = addLine(0xff555555);

		mFlashLayout = new FrameLayout(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
		addView(mFlashLayout, params);
		{
			TextView title = generateTextView(R.string.camera_flash, 12, COLOR_TITLE, false);
			FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.gravity = Gravity.CENTER_VERTICAL;
			params1.leftMargin = ShareData.PxToDpi_xhdpi(30);
			mFlashLayout.addView(title, params1);

			mAutoView = generateTextView(R.string.camera_flash_auto, 12, COLOR_SELECT, false);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(250);
			mFlashLayout.addView(mAutoView, params1);
			mAutoView.setOnClickListener(mOnClickListener);

			mOpenView = generateTextView(R.string.camera_open, 12, COLOR_NORMAL, false);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(130);
			mFlashLayout.addView(mOpenView, params1);
			mOpenView.setOnClickListener(mOnClickListener);

			mCloseView = generateTextView(R.string.camera_close, 12, COLOR_NORMAL, false);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			params1.rightMargin = ShareData.PxToDpi_xhdpi(10);
			mFlashLayout.addView(mCloseView, params1);
			mCloseView.setOnClickListener(mOnClickListener);
		}

		addLine(0xff555555);

		mTimerLayout = new FrameLayout(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
		addView(mTimerLayout, params);
		{
			FrameLayout.LayoutParams params1;

			TextView title = generateTextView(R.string.camera_timer, 12, COLOR_TITLE, false);
			params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.gravity = Gravity.CENTER_VERTICAL;
			params1.leftMargin = ShareData.PxToDpi_xhdpi(30);
			mTimerLayout.addView(title, params1);

			mTimer10SView = generateTextView(R.string.camera_timer_10, 12, COLOR_NORMAL, false);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.rightMargin = ShareData.PxToDpi_xhdpi(250);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			mTimerLayout.addView(mTimer10SView, params1);
			mTimer10SView.setOnClickListener(mOnClickListener);

			mTimer3SView = generateTextView(R.string.camera_timer_3, 12, COLOR_NORMAL, false);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.rightMargin = ShareData.PxToDpi_xhdpi(130);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			mTimerLayout.addView(mTimer3SView, params1);
			mTimer3SView.setOnClickListener(mOnClickListener);

			mTimerOffView = generateTextView(R.string.camera_close, 12, COLOR_SELECT, false);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.rightMargin = ShareData.PxToDpi_xhdpi(10);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			mTimerLayout.addView(mTimerOffView, params1);
			mTimerOffView.setOnClickListener(mOnClickListener);
		}

		addLine(0xff555555);
		mTouchCaptureLayout = new FrameLayout(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
		addView(mTouchCaptureLayout, params);
		{
			TextView title = generateTextView(R.string.camera_touch_to_capture, 12, COLOR_TITLE, false);
			FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.gravity = Gravity.CENTER_VERTICAL;
			params1.leftMargin = ShareData.PxToDpi_xhdpi(30);
			mTouchCaptureLayout.addView(title, params1);

			mTouchOpenView = generateTextView(R.string.camera_open, 12, COLOR_NORMAL, false);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.rightMargin = ShareData.PxToDpi_xhdpi(130);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			mTouchCaptureLayout.addView(mTouchOpenView, params1);
			mTouchOpenView.setOnClickListener(mOnClickListener);

			mTouchCloseView = generateTextView(R.string.camera_close, 12, COLOR_SELECT, false);
			params1 = new FrameLayout.LayoutParams(mChildItemSize, mChildItemSize);
			params1.rightMargin = ShareData.PxToDpi_xhdpi(10);
			params1.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			mTouchCaptureLayout.addView(mTouchCloseView, params1);
			mTouchCloseView.setOnClickListener(mOnClickListener);
		}
	}

	public boolean setSize(int size) {
		if (size != mSize) {
			mSize1_1View.setImageResource(R.drawable.camera_size_1_1_unselect);
			mSize3_4View.setImageResource(R.drawable.camera_size_3_4_unselect);
			mSize9_16View.setImageResource(R.drawable.camera_size_9_16_unselect);

			switch (size) {
				case CameraConfig.SIZE_1_1:
					mSize1_1View.setImageResource(R.drawable.camera_size_1_1);
					break;
				case CameraConfig.SIZE_3_4:
					mSize3_4View.setImageResource(R.drawable.camera_size_3_4);
					break;
				case CameraConfig.SIZE_9_16:
					mSize9_16View.setImageResource(R.drawable.camera_size_9_16);
					break;
			}

			mSize = size;

			return true;
		}

		return false;
	}

	public boolean setFlash(int flash) {
		if (flash != mFlash) {
			mAutoView.setTextColor(COLOR_NORMAL);
			mOpenView.setTextColor(COLOR_NORMAL);
			mCloseView.setTextColor(COLOR_NORMAL);

			switch (flash) {
				case CameraConfig.FLASH_AUTO:
					mAutoView.setTextColor(COLOR_SELECT);
					break;
				case CameraConfig.FLASH_ON:
					mOpenView.setTextColor(COLOR_SELECT);
					break;
				case CameraConfig.FLASH_OFF:
					mCloseView.setTextColor(COLOR_SELECT);
					break;
			}

			mFlash = flash;

			return true;
		}

		return false;
	}

	public boolean setTimer(int timer) {
		if (timer != mTimer) {
			mTimer10SView.setTextColor(COLOR_NORMAL);
			mTimer3SView.setTextColor(COLOR_NORMAL);
			mTimerOffView.setTextColor(COLOR_NORMAL);

			switch (timer) {
				case CameraConfig.TIMER_10S:
					mTimer10SView.setTextColor(COLOR_SELECT);
					break;
				case CameraConfig.TIMER_3S:
					mTimer3SView.setTextColor(COLOR_SELECT);
					break;
				case CameraConfig.TIMER_OFF:
					mTimerOffView.setTextColor(COLOR_SELECT);
					break;
			}

			mTimer = timer;

			return true;
		}

		return false;
	}

	public boolean setTouchCapture(boolean touchCapture) {

		if (touchCapture != isTouchCapture) {
			if (touchCapture) {
				mTouchOpenView.setTextColor(COLOR_SELECT);
				mTouchCloseView.setTextColor(COLOR_NORMAL);
			} else {
				mTouchCloseView.setTextColor(COLOR_SELECT);
				mTouchOpenView.setTextColor(COLOR_NORMAL);
			}

			isTouchCapture = touchCapture;

			return true;
		}

		return false;
	}

	private TextView generateTextView(@StringRes int resId, int textSize, int textColor, boolean bold) {
		TextView textView = new TextView(mContext);
		textView.setText(resId);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
		textView.setTextColor(textColor);
		textView.setGravity(Gravity.CENTER);
		if (bold) {
			textView.getPaint().setFakeBoldText(true);
		}

		return textView;
	}

	private View addLine(int color) {
		View view = new View(mContext);
		view.setBackgroundColor(color);
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
		params.leftMargin = params.rightMargin = ShareData.PxToDpi_xhdpi(30);
		addView(view, params);

		return view;
	}

	public void setOnMoreControlListener(OnMoreControlListener controlListener) {
		mControlListener = controlListener;
	}

	public void setFlashEnable(boolean flashEnable) {
		if (mFlashEnable != flashEnable) {
			mFlashEnable = flashEnable;

			if (mFlashEnable) {
				mLine.setVisibility(VISIBLE);
				mFlashLayout.setVisibility(VISIBLE);
			} else {
				mLine.setVisibility(GONE);
				mFlashLayout.setVisibility(GONE);
			}
		}
	}

	public boolean isFlashEnable() {
		return mFlashEnable;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mControlListener != null) {
				if (v == mSize1_1View) {
					if (setSize(CameraConfig.SIZE_1_1)) {
						mControlListener.changeSize(CameraConfig.SIZE_1_1);
					}
				} else if (v == mSize3_4View) {
					if (setSize(CameraConfig.SIZE_3_4)) {
						mControlListener.changeSize(CameraConfig.SIZE_3_4);
					}
				} else if (v == mSize9_16View) {
					if (setSize(CameraConfig.SIZE_9_16)) {
						mControlListener.changeSize(CameraConfig.SIZE_9_16);
					}
				} else if (v == mAutoView) {
					if (setFlash(CameraConfig.FLASH_AUTO)) {
						mControlListener.changeFlash(CameraConfig.FLASH_AUTO);
					}

				} else if (v == mOpenView) {
					if (setFlash(CameraConfig.FLASH_ON)) {
						mControlListener.changeFlash(CameraConfig.FLASH_ON);
					}

				} else if (v == mCloseView) {
					if (setFlash(CameraConfig.FLASH_OFF)) {
						mControlListener.changeFlash(CameraConfig.FLASH_OFF);
					}

				} else if (v == mTimerOffView) {
					if (setTimer(CameraConfig.TIMER_OFF)) {
						mControlListener.changeTimer(CameraConfig.TIMER_OFF);
					}
				} else if (v == mTimer3SView) {
					if (setTimer(CameraConfig.TIMER_3S)) {
						mControlListener.changeTimer(CameraConfig.TIMER_3S);
					}
				} else if (v == mTimer10SView) {
					if (setTimer(CameraConfig.TIMER_10S)) {
						mControlListener.changeTimer(CameraConfig.TIMER_10S);
					}
				} else if (v == mTouchOpenView) {
					if (setTouchCapture(true)) {
						mControlListener.changeTouchCapture(true);
					}
				} else if (v == mTouchCloseView) {
					if (setTouchCapture(false)) {
						mControlListener.changeTouchCapture(false);
					}
				}
			}
		}
	};

	public interface OnMoreControlListener {

		void changeSize(int size);

		void changeFlash(int flash);

		void changeTimer(int timer);

		void changeTouchCapture(boolean open);
	}
}
