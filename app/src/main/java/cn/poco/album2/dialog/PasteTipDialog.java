package cn.poco.album2.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.album2.adapter.GridAdapter;
import cn.poco.album2.utils.PhotoGridDivide;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/8/2
 */
public class PasteTipDialog extends Dialog implements View.OnClickListener {

	private Context mContext;

	private LinearLayout mContainer;

	private TextView mNumberTip;

	private RecyclerView mRecyclerView;

	private TextView mOkText;
	private TextView mCancelText;

	private OnBtnClickListener mOnBtnClickListener;

	public PasteTipDialog(@NonNull Context context) {
		super(context);

		mContext = context;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(false);

		mContainer = new LinearLayout(mContext);
		mContainer.setOrientation(LinearLayout.VERTICAL);
		mContainer.setBackgroundColor(0xff404040);
		int paddingTop = ShareData.PxToDpi_xhdpi(26);
		mContainer.setPadding(0, paddingTop, 0, 0);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ShareData.PxToDpi_xhdpi(530), ViewGroup.LayoutParams.WRAP_CONTENT);
		setContentView(mContainer, params);

		initViews();
	}

	private void initViews() {
		LinearLayout.LayoutParams params;

		mNumberTip = new TextView(mContext);
		mNumberTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		mNumberTip.setTextColor(Color.WHITE);
		params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		mContainer.addView(mNumberTip, params);

		TextView continueText = new TextView(mContext);
		continueText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		continueText.setTextColor(Color.WHITE);
		continueText.setText(R.string.tip_continue);
		params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.topMargin = ShareData.PxToDpi_xhdpi(2);
		mContainer.addView(continueText, params);

		mRecyclerView = new RecyclerView(mContext);
		mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		mRecyclerView.addItemDecoration(new PhotoGridDivide(ShareData.PxToDpi_xhdpi(30), ShareData.PxToDpi_xhdpi(24), false));
		params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(360));
		params.topMargin = ShareData.PxToDpi_xhdpi(26);
		params.leftMargin = params.rightMargin = ShareData.PxToDpi_xhdpi(30);
		mContainer.addView(mRecyclerView, params);

		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setBackgroundColor(0xff272727);
		params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(90));
		params.topMargin = ShareData.PxToDpi_xhdpi(40);
		mContainer.addView(layout, params);
		{
			mCancelText = new TextView(mContext);
			mCancelText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			mCancelText.setText(R.string.Cancel);
			mCancelText.setTextColor(Color.WHITE);
			mCancelText.setGravity(Gravity.CENTER);
			mCancelText.setOnClickListener(this);
			params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
			layout.addView(mCancelText, params);

			View view = new View(mContext);
			view.setBackgroundColor(0xff3b3b3b);
			params = new LinearLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);
			layout.addView(view, params);

			mOkText = new TextView(mContext);
			mOkText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			mOkText.setText(R.string.Continue);
			mOkText.setTextColor(0xffffcf56);
			mOkText.setGravity(Gravity.CENTER);
			mOkText.setOnClickListener(this);
			params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
			layout.addView(mOkText, params);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mCancelText) {
			if (mOnBtnClickListener != null) {
				mOnBtnClickListener.onCancel();
			}
		} else if (v == mOkText) {
			if (mOnBtnClickListener != null) {
				mOnBtnClickListener.onConfirm();
			}
		}
	}

	public void setOnBtnClickListener(OnBtnClickListener listener) {
		mOnBtnClickListener = listener;
	}

	public void setData(int photoNum, List<ImageStore.ImageInfo> photoInfs) {
		if (photoNum > 0) {
			String title = mContext.getResources().getQuantityString(R.plurals.photo_unsaved, photoNum, photoNum);
			mNumberTip.setText(title);
		} else {
			mNumberTip.setVisibility(View.GONE);
		}
		mRecyclerView.setAdapter(new GridAdapter(mContext, photoInfs));
	}

	public interface OnBtnClickListener {
		void onCancel();
		void onConfirm();
	}
}
