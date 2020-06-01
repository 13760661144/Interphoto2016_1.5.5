package cn.poco.capture2.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.poco.capture2.model.Snippet;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2018/1/2
 */
public class SnippetView extends View {

	private static final int ANIM_DURATION = 300;

	private static final int COLOR_DOT = 0x99ffffff;
	private static final int COLOR_LINE = 0xffffc433;

	private static final int SIZE_MINI_DOT = 8;
	private static final int SIZE_DOT = 20;
	private static final int SIZE_LINE = 8;
	private static final int MINI_DOT_GAP = 6;

	private Context mContext;

	private Paint mPaint;

	private int mMiniDotSize;
	private int mDotSize;
	private int mLineSize;
	private int mDotGap;

	private List<Snippet> mSnippets = new ArrayList<>();

	private ValueAnimator mValueAnimator;

	private OnShowDeleteListener mOnShowDeleteListener;

	public SnippetView(Context context) {
		super(context);

		mContext = context;
		init();
	}

	private void init() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		mPaint.setStyle(Paint.Style.FILL);

		mMiniDotSize = ShareData.PxToDpi_xhdpi(SIZE_MINI_DOT);
		mDotSize = ShareData.PxToDpi_xhdpi(SIZE_DOT);
		mLineSize = ShareData.PxToDpi_xhdpi(SIZE_LINE);
		mDotGap = ShareData.PxToDpi_xhdpi(MINI_DOT_GAP);
	}

	public void addSnippet(Snippet snippet) {
		if (snippet != null) {
			mValueAnimator = ValueAnimator.ofFloat(0, snippet.ratio);
			mValueAnimator.addUpdateListener(mAnimatorUpdateListener);
			mValueAnimator.setDuration(ANIM_DURATION);
			mValueAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mSnippets.get(mSnippets.size()-1).finish = true;
				}
			});

			mSnippets.add(new Snippet());
			mValueAnimator.start();
		}
	}

	public void deleteSnippet(final Runnable runnable) {
		if (!mSnippets.isEmpty()) {
			Snippet snippet = mSnippets.get(mSnippets.size() - 1);
			snippet.finish = false;
			mValueAnimator = ValueAnimator.ofFloat(snippet.ratio, 0);
			mValueAnimator.addUpdateListener(mAnimatorUpdateListener);
			mValueAnimator.setDuration(ANIM_DURATION);
			mValueAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mSnippets.remove(mSnippets.size() - 1);
					if (runnable != null) {
						runnable.run();
					}
				}
			});
			mValueAnimator.start();
		}
	}

	public void setSnippets(List<Snippet> snippets) {
		if (snippets != null) {
			mSnippets.addAll(snippets);
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			mSnippets.get(mSnippets.size()-1).ratio = (float)animation.getAnimatedValue();
			ViewCompat.postInvalidateOnAnimation(SnippetView.this);
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {

		int width = getWidth();
		float centerY = getHeight() / 2f;

		// 画小圆点
		float startX = mDotGap + mMiniDotSize / 2f;
		mPaint.setColor(COLOR_DOT);
		while (startX < width) {
			canvas.drawCircle(startX, centerY, mMiniDotSize / 2f, mPaint);
			startX = startX + mMiniDotSize + mDotGap;
		}

		// 画分段
		Snippet snippet = null;
		float length;
		startX = 0;
		int size = mSnippets.size();
		boolean end = false;
		boolean showDelete = false;
		for (int i = 0; i < size; i++) {
			snippet = mSnippets.get(i);
			length = width * snippet.ratio;
			mPaint.setColor(COLOR_LINE);
			mPaint.setStrokeWidth(mLineSize);

			if (startX + length + mDotSize / 2f >= width) {
				length = width - startX;
				end = true;
			}
			canvas.drawLine(startX, centerY, startX + length, centerY, mPaint);

			if (i != 0) {
				mPaint.setColor(Color.WHITE);
				canvas.drawCircle(startX, centerY, mDotSize / 2f, mPaint);
			}

			startX += length;
		}

		if (snippet != null && snippet.finish && !end) {
			mPaint.setColor(Color.WHITE);
			canvas.drawCircle(startX, centerY, mDotSize / 2f, mPaint);
			showDelete = true;
		}

		if (mOnShowDeleteListener != null) {
			float right = showDelete ? startX + mDotSize / 2f : 0;
			if (end) {
				showDelete = true;
				right = width - ShareData.PxToDpi_xhdpi(56);
			}
			mOnShowDeleteListener.showDeleteIcon(showDelete, right);
		}
	}

	public void release() {
		if (mValueAnimator != null) {
			mValueAnimator.removeAllUpdateListeners();
			mValueAnimator.cancel();
			mValueAnimator = null;
		}
	}

	public void setOnShowDeleteListener(OnShowDeleteListener listener) {
		mOnShowDeleteListener = listener;
	}

	public interface OnShowDeleteListener {
		void showDeleteIcon(boolean show, float right);
	}
}
