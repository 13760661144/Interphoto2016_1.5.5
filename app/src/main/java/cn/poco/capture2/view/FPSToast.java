//package cn.poco.capture2.view;
//
//import android.app.Activity;
//import android.graphics.Color;
//import android.util.TypedValue;
//import android.view.Choreographer;
//import android.view.Gravity;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import java.util.Locale;
//
//import cn.poco.tianutils.ShareData;
//
///**
// * Created by: fwc
// * Date: 2018/1/16
// */
//
//public class FPSToast {
//
//	private Activity mContext;
//	private Choreographer mChoreographer;
//	private long mLastTime;
//
//	private LinearLayout mLayout;
//	private TextView mFrameText;
//
//	private boolean isStart;
//
//	public FPSToast(Activity context) {
//
//		mContext = context;
//
//		init();
//	}
//
//	private void init() {
//		mChoreographer = Choreographer.getInstance();
//
//		mLayout = new LinearLayout(mContext);
//		mLayout.setOrientation(LinearLayout.HORIZONTAL);
//		mLayout.setPadding(ShareData.PxToDpi_xhdpi(16), 0, ShareData.PxToDpi_xhdpi(16), 0);
//		mLayout.setBackgroundColor(0xa5000000);
//		{
//			LinearLayout.LayoutParams params;
//
//			TextView tip = new TextView(mContext);
//			tip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//			tip.setTextColor(Color.WHITE);
//			tip.setText("FPS:");
//			params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//			params.gravity = Gravity.CENTER_VERTICAL;
//			mLayout.addView(tip, params);
//
//			mFrameText = new TextView(mContext);
//			mFrameText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//			mFrameText.setTextColor(Color.RED);
//			mFrameText.setText("0");
//			params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//			params.gravity = Gravity.CENTER_VERTICAL;
//			params.leftMargin = ShareData.PxToDpi_xhdpi(16);
//			mLayout.addView(mFrameText, params);
//		}
//	}
//
//	public void start() {
//		mLastTime = 0;
//		mChoreographer.postFrameCallback(mFrameCallback);
//		isStart = true;
//
//		ViewGroup parent = (ViewGroup) mContext.getWindow().getDecorView();
//		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//		params.gravity = Gravity.END;
//		params.topMargin = ShareData.PxToDpi_xhdpi(32);
//		params.rightMargin = ShareData.PxToDpi_xhdpi(32);
//		parent.addView(mLayout, params);
//	}
//
//	public void stop() {
//		isStart = false;
//		mChoreographer.removeFrameCallback(mFrameCallback);
//
//		ViewGroup parent = (ViewGroup) mContext.getWindow().getDecorView();
//		parent.removeView(mLayout);
//	}
//
//	private Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
//		@Override
//		public void doFrame(long frameTimeNanos) {
//			if (mLastTime != 0) {
//				float frameTime = (frameTimeNanos - mLastTime) / 1000000f;
//				float frame = 1000 / frameTime;
//				mFrameText.setText(String.format(Locale.getDefault(), "%.2f", frame));
//			}
//
//			mLastTime = frameTimeNanos;
//
//			if (isStart) {
//				mChoreographer.postFrameCallback(this);
//			}
//		}
//	};
//}
