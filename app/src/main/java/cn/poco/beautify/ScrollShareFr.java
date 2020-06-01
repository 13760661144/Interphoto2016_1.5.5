package cn.poco.beautify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * 点击可见分享list
 */

public class ScrollShareFr {
	private FrameLayout m_parent;
	private Context m_context;
	private FrameLayout m_mainFr;
	private FrameLayout m_container;
	private LinearLayout m_topFr;
	private FrameLayout m_mask;

	private int m_topFrHeight;
	protected boolean m_isShareFrShow = false;
	private Bitmap m_maskBk;
	private int m_animTime = 150;

	private OnCloseListener mOnCloseListener;

	private AnimatorSet mAnimatorSet;

	public ScrollShareFr(FrameLayout parent, int topFrH) {
		m_parent = parent;
		m_context = m_parent.getContext();
		m_topFrHeight = topFrH;

		InitUI();
	}

	public void setOnCloseListener(OnCloseListener onCloseListener) {
		mOnCloseListener = onCloseListener;
	}

	public void setAnimTime(int time) {
		m_animTime = time;
	}

	private void InitUI() {
		FrameLayout.LayoutParams fl;
		m_mainFr = new FrameLayout(m_context);
		fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		m_mainFr.setLayoutParams(fl);
		m_parent.addView(m_mainFr);

		m_container = new FrameLayout(m_context);
		fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		m_container.setLayoutParams(fl);
		m_mainFr.addView(m_container, 0);

		m_mask = new FrameLayout(m_context);
		/*{
			private float mLastY;
			private float mDistanceY;

			@Override
			public boolean dispatchTouchEvent(MotionEvent ev)
			{
				super.dispatchTouchEvent(ev);
				switch (ev.getAction())
				{
					case MotionEvent.ACTION_DOWN:
					{
						mDistanceY = 0;
						mLastY = ev.getY();
						break;
					}
					case MotionEvent.ACTION_MOVE:
					{
						float x = ev.getX();
						float y = ev.getY();
						float disY = y - mLastY;
						mDistanceY += disY;
						FrameLayout.LayoutParams shareFl = (FrameLayout.LayoutParams) m_topFr.getLayoutParams();
						FrameLayout.LayoutParams beautyFl = (FrameLayout.LayoutParams) m_mainFr.getLayoutParams();
						int shareTopMargin = shareFl.topMargin;
						int beautyTopMargin = beautyFl.topMargin;
						shareFl.topMargin = shareTopMargin + (int) mDistanceY;
						beautyFl.topMargin = beautyTopMargin + (int) mDistanceY;
						if (shareFl.topMargin > 0)
						{
							shareFl.topMargin = 0;
						}
						if (shareFl.topMargin < -m_topFrHeight)
						{
							shareFl.topMargin = -m_topFrHeight;
						}
						if (beautyFl.topMargin < 0)
						{
							beautyFl.topMargin = 0;
						}
						if (beautyFl.topMargin > m_topFrHeight)
						{
							beautyFl.topMargin = m_topFrHeight;
						}
						m_topFr.setLayoutParams(shareFl);
						m_mainFr.setLayoutParams(beautyFl);
//						m_mask.setAlpha(0.5f + 0.5f * beautyFl.topMargin / (float) m_topFrHeight);
						mLastY = y;
						break;
					}
					case MotionEvent.ACTION_UP:
					{
						FrameLayout.LayoutParams beautyFl = (FrameLayout.LayoutParams) m_mainFr.getLayoutParams();
						FrameLayout.LayoutParams shareFl = (FrameLayout.LayoutParams) m_topFr.getLayoutParams();
						int beautyTopMargin = beautyFl.topMargin;

						if (Math.abs(mDistanceY) < 1 && beautyTopMargin == m_topFrHeight)
						{
							SetShareFrState(false, true, -1, -1);
							return true;
						}
						if (beautyTopMargin >= m_topFrHeight / 2 && beautyTopMargin <= m_topFrHeight)
						{
							SetShareFrState(true, true, shareFl.topMargin, 0);
						}
						else
						{
							SetShareFrState(false, true, beautyFl.topMargin, 0);
						}
						break;
					}
				}
				return true;
			}
		};*/
		m_mask.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnCloseListener != null) {
					mOnCloseListener.onClose();
				}
			}
		});
		m_mask.setVisibility(View.GONE);
		m_mask.setBackgroundColor(0x99000000);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		m_mask.setLayoutParams(fl);
		m_mainFr.addView(m_mask);

		m_topFr = new LinearLayout(m_context);
		m_topFr.setBackgroundColor(0xff222222);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_topFrHeight);
		fl.gravity = Gravity.TOP;
		fl.topMargin = -m_topFrHeight;
		m_topFr.setLayoutParams(fl);
		m_topFr.setOrientation(LinearLayout.VERTICAL);
		m_parent.addView(m_topFr);
	}

	public LinearLayout GetTopFr() {
		return m_topFr;
	}

	public FrameLayout GetMainFr() {
		return m_mainFr;
	}

	public void AddTopChild(View child) {
		m_topFr.addView(child);
	}

	public void AddMainChild(View child) {
		m_container.addView(child);
	}

	public void AddMainChild(View child, int index) {
		m_container.addView(child, index);
	}

	public View getContainer() {
		return m_container;
	}

	public void SetMaskBk(Bitmap bk) {
		if (bk == null) {
			m_mask.setBackgroundColor(Color.BLACK);
		}
		m_maskBk = bk;
	}

	public void ShowTopBar(boolean isShow) {
		SetShareFrState(isShow, true, -1, -1);
	}

	public boolean IsTopBarShowing() {
		return m_isShareFrShow;
	}

	private void SetShareFrState(final boolean isOpen, boolean hasAnim, int start1, int end1) {

		if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
			return;
		}

		int start, end;
		float alpha;
		if (isOpen) {
			m_isShareFrShow = true;
			start = 0;
			end = m_topFrHeight;
			m_mask.setBackgroundDrawable(new BitmapDrawable(m_maskBk));
			m_mask.setAlpha(0);
			m_mask.setVisibility(View.VISIBLE);
			alpha = 1;
		} else {
			m_isShareFrShow = false;
			start = m_topFrHeight;
			end = 0;
			m_mask.setAlpha(1);
			alpha = 0;
		}

		if (start1 != -1) {
			start = start1;
		}
		if (end1 != -1) {
			end = end1;
		}

		if (hasAnim) {
			mAnimatorSet = new AnimatorSet();
			ObjectAnimator animator1 = ObjectAnimator.ofFloat(m_topFr, "translationY", start, end);
			ObjectAnimator animator2 = ObjectAnimator.ofFloat(m_mainFr, "translationY", start, end);
			ObjectAnimator animator3 = ObjectAnimator.ofFloat(m_mask, "alpha", m_mask.getAlpha(), alpha);
			mAnimatorSet.playTogether(animator1, animator2, animator3);
			mAnimatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if (!isOpen) {
						m_mask.setVisibility(View.GONE);
					}
				}
			});
			mAnimatorSet.setDuration(m_animTime);
			mAnimatorSet.start();
		}
	}

	public interface OnCloseListener {
		void onClose();
	}
}
