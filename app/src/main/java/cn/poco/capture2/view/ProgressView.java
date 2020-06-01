package cn.poco.capture2.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

import cn.poco.capture2.AnimatorUtils;
import cn.poco.capture2.model.Snippet;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.OnAnimationClickListener;

/**
 * Created by: fwc
 * Date: 2018/1/2
 */
public class ProgressView extends FrameLayout {

	private Context mContext;

	private SnippetView mSnippetView;

	private ImageView mDeleteIcon;
	private int mDeleteIconPadding;

	private OnClickDeleteListener mOnClickDeleteListener;

	public ProgressView(@NonNull Context context) {
		super(context);
		mContext = context;

		initViews();
	}

	@SuppressWarnings("all")
	private void initViews() {
		LayoutParams params;

		mSnippetView = new SnippetView(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(20));
		params.gravity = Gravity.BOTTOM;
		addView(mSnippetView, params);
		mSnippetView.setOnShowDeleteListener(new SnippetView.OnShowDeleteListener() {
			@Override
			public void showDeleteIcon(boolean show, float right) {
				if (show) {
					mDeleteIcon.setTranslationX(right-mDeleteIconPadding);
					AnimatorUtils.showView(mDeleteIcon, 200);
				} else {
					AnimatorUtils.hideView(mDeleteIcon, 200);
				}
			}
		});

		mDeleteIconPadding = ShareData.PxToDpi_xhdpi(32);
		mDeleteIcon = new ImageView(mContext);
		mDeleteIcon.setImageResource(R.drawable.camera_snippet_delete);
		mDeleteIcon.setPadding(mDeleteIconPadding, 0, mDeleteIconPadding, 0);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mDeleteIcon, params);
		mDeleteIcon.setOnTouchListener(mOnTouchListener);
	}

	private OnTouchListener mOnTouchListener = new OnAnimationClickListener() {
		@Override
		public void onAnimationClick(View v) {
			if (v == mDeleteIcon && mOnClickDeleteListener != null) {
				mSnippetView.deleteSnippet(new Runnable() {
					@Override
					public void run() {
						mOnClickDeleteListener.onClickDelete();
					}
				});
			}
		}
	};

	public void hideDeleteIcon() {
		if (mDeleteIcon.getVisibility() == VISIBLE) {
			mDeleteIcon.setVisibility(GONE);
		}
	}

	public void addSnippet(Snippet snippet) {
		mSnippetView.addSnippet(snippet);
	}

	public void setSnippets(List<Snippet> snippets) {
		mSnippetView.setSnippets(snippets);
		setAlpha(1);
		setVisibility(VISIBLE);
	}

	public void release() {
		mSnippetView.release();
	}

	public void setOnClickDeleteListener(OnClickDeleteListener listener) {
		mOnClickDeleteListener = listener;
	}

	public interface OnClickDeleteListener {
		void onClickDelete();
	}
}
