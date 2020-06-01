package cn.poco.beautify.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.poco.interphoto2.R;

public class TextAnim1 extends FrameLayout {

	private ImageView mBgView;

	private ImageView mTextView;

	private Circle mCircle;

	private ImageView mBg2View;

	private TextView mSText;
	private TextView mAText;
	private TextView mMText;
	private TextView mVText;
	private TextView mIText;
	private TextView mS2Text;
	private TextView mI2Text;
	private TextView mOText;
	private TextView mNText;

	private ImageView mSaveView;

	private AnimatorSet mStartSet;

	private ObjectAnimator mTextShow;
	private ObjectAnimator mTextHide;

	private AnimatorSet mHideSet;

	private ObjectAnimator mSaveShow;
	private ObjectAnimator mSavaHide;

	private ObjectAnimator mCirclePress;
	private ObjectAnimator mCircleShow;
	private ObjectAnimator mCircleMoveTopX;
	private ObjectAnimator mCircleMoveTopY;
	private AnimatorSet mCircleMoveTopSet;

	private ObjectAnimator mCircleMoveBottomX;
	private ObjectAnimator mCircleMoveBottomY;
	private AnimatorSet mCircleMoveBottomSet;

	private ObjectAnimator mCircleMoveBackX;
	private ObjectAnimator mCircleMoveBackY;
	private AnimatorSet mCircleMoveBackSet;

	private ObjectAnimator mSTextShow;
	private ObjectAnimator mATextShow;
	private ObjectAnimator mMTextShow;
	private ObjectAnimator mVTextShow;
	private ObjectAnimator mITextShow;
	private ObjectAnimator mS2TextShow;
	private ObjectAnimator mI2TextShow;
	private ObjectAnimator mOTextShow;
	private ObjectAnimator mNTextShow;

	private Animation mSaveBgAnimation;

	private Context mContext;

	private float mMoveTopX;
	private float mMoveTopY;

	private float mMoveBottomX;
	private float mMoveBottomY;

	private boolean mStop = false;

	/**
	 * 标记是否隐藏了文字背景（即跳到了保存界面）
	 * 因为mCirclePress动画需要被执行两次，需要根据这个标记决定是否还有下一步动画
	 */
	private boolean mHide = false;

	public TextAnim1(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TextAnim1(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		LayoutInflater.from(mContext).inflate(R.layout.text_anim_view, this, true);

		initView();

		mMoveTopX = PxToDpi_xhdpi(130);
		mMoveTopY = PxToDpi_xhdpi(-190);

		mMoveBottomX = PxToDpi_xhdpi(90);
		mMoveBottomY = PxToDpi_xhdpi(85);

		initAnimation();

		startAnimation();
	}

	private void initView() {
		mBgView = (ImageView) findViewById(R.id.iv_bg);
		mTextView = (ImageView) findViewById(R.id.iv_text);
		mCircle = (Circle) findViewById(R.id.circle);
		mBg2View = (ImageView) findViewById(R.id.iv_bg2);

		mSText = (TextView) findViewById(R.id.tv_s);
		mAText = (TextView) findViewById(R.id.tv_a);
		mMText = (TextView) findViewById(R.id.tv_m);
		mVText = (TextView) findViewById(R.id.tv_v);
		mIText = (TextView) findViewById(R.id.tv_i);
		mS2Text = (TextView) findViewById(R.id.tv_s2);
		mI2Text = (TextView) findViewById(R.id.tv_i2);
		mOText = (TextView) findViewById(R.id.tv_o);
		mNText = (TextView) findViewById(R.id.tv_n);

		mSaveView = (ImageView) findViewById(R.id.iv_save);
	}

	private void initAnimation() {
		mSaveBgAnimation = AnimationUtils.loadAnimation(mContext, R.anim.bg_from_bottom);
		mTextShow = ObjectAnimator.ofFloat(mTextView, "alpha", 0, 1).setDuration(300);
		mTextHide = ObjectAnimator.ofFloat(mTextView, "alpha", 1, 0).setDuration(1000);

		mSavaHide = ObjectAnimator.ofFloat(mSaveView, "alpha", 1, 0).setDuration(1000);

		mHideSet = new AnimatorSet();
		mHideSet.play(mTextHide).with(mSavaHide);

		mCirclePress = ObjectAnimator.ofFloat(mCircle, "alpha", 0.8f, 1, 0.6f).setDuration(300);
		mCircleShow = ObjectAnimator.ofFloat(mCircle, "alpha", 0, 0.8f).setDuration(500);
		mCircleMoveTopX = ObjectAnimator.ofFloat(mCircle, "translationX", 0, mMoveTopX).setDuration(500);
		mCircleMoveTopY = ObjectAnimator.ofFloat(mCircle, "translationY", 0, mMoveTopY).setDuration(500);
		mCircleMoveBottomX = ObjectAnimator.ofFloat(mCircle, "translationX", mMoveTopX, mMoveBottomX).setDuration(500);
		mCircleMoveBottomY = ObjectAnimator.ofFloat(mCircle, "translationY", mMoveTopY, mMoveBottomY).setDuration(500);
		mCircleMoveBackX = ObjectAnimator.ofFloat(mCircle, "translationX", mMoveBottomX, 0).setDuration(300);
		mCircleMoveBackY = ObjectAnimator.ofFloat(mCircle, "translationY", mMoveBottomY, 0).setDuration(300);

		mSaveShow = ObjectAnimator.ofFloat(mSaveView, "alpha", 0, 1).setDuration(500);

		mCircleMoveTopSet = new AnimatorSet();
		mCircleMoveTopSet.play(mCircleMoveTopX).with(mCircleMoveTopY).with(mSaveShow)
				.after(mCircleShow).before(mCirclePress);

		mCircleMoveBottomSet = new AnimatorSet();
		mCircleMoveBottomSet.play(mCircleMoveBottomX).with(mCircleMoveBottomY).before(mCirclePress);

		mCircleMoveBackSet = new AnimatorSet();
		mCircleMoveBackSet.play(mCircleMoveBackX).with(mCircleMoveBackY);

		mSTextShow = ObjectAnimator.ofFloat(mSText, "translationX", 10, -16).setDuration(500);
		mATextShow = ObjectAnimator.ofFloat(mAText, "translationX", 10, -16).setDuration(500);
		mMTextShow = ObjectAnimator.ofFloat(mMText, "translationX", 10, -16).setDuration(500);
		mVTextShow = ObjectAnimator.ofFloat(mVText, "translationX", 10, -16).setDuration(500);
		mITextShow = ObjectAnimator.ofFloat(mIText, "translationX", 10, -16).setDuration(500);
		mS2TextShow = ObjectAnimator.ofFloat(mS2Text, "translationX", 10, -16).setDuration(500);
		mI2TextShow = ObjectAnimator.ofFloat(mI2Text, "translationX", 10, -16).setDuration(500);
		mOTextShow = ObjectAnimator.ofFloat(mOText, "translationX", 10, -16).setDuration(500);
		mNTextShow = ObjectAnimator.ofFloat(mNText, "translationX", 10, -16).setDuration(500);

		mStartSet = new AnimatorSet();
		mStartSet.play(mTextShow).before(mCircleMoveTopSet);
	}

	private void startAnimation() {
		mSaveBgAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mSText.setVisibility(View.VISIBLE);
				mSTextShow.start();

				mATextShow.setStartDelay(100);
				mATextShow.start();

				mMTextShow.setStartDelay(200);
				mMTextShow.start();

				mVTextShow.setStartDelay(300);
				mVTextShow.start();

				mITextShow.setStartDelay(400);
				mITextShow.start();

				mS2TextShow.setStartDelay(500);
				mS2TextShow.start();

				mI2TextShow.setStartDelay(600);
				mI2TextShow.start();

				mOTextShow.setStartDelay(700);
				mOTextShow.start();

				mNTextShow.setStartDelay(800);
				mNTextShow.start();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		mATextShow.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mAText.setVisibility(View.VISIBLE);
			}
		});

		mMTextShow.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mMText.setVisibility(View.VISIBLE);
			}
		});

		mVTextShow.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mVText.setVisibility(View.VISIBLE);
			}
		});

		mITextShow.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mIText.setVisibility(View.VISIBLE);
			}
		});

		mS2TextShow.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mS2Text.setVisibility(View.VISIBLE);
			}
		});

		mI2TextShow.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mI2Text.setVisibility(View.VISIBLE);
			}
		});

		mOTextShow.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mOText.setVisibility(View.VISIBLE);
			}
		});

		mNTextShow.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mNText.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mCircleMoveBottomSet.start();
			}
		});

		mCircleShow.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mCircle.setVisibility(View.VISIBLE);
			}
		});

		mSaveShow.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mSaveView.setVisibility(View.VISIBLE);
			}
		});

		mTextHide.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mBgView.setVisibility(View.GONE);
				mBg2View.setVisibility(View.VISIBLE);
				mBg2View.startAnimation(mSaveBgAnimation);
			}
		});

		mCirclePress.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if (!mHide) {
					mHideSet.start();
					mHide = true;
				} else {
					postDelayed(new Runnable() {
						@Override
						public void run() {
							mCircleMoveBackSet.start();
						}
					}, 700);
				}
			}
		});

		mCircleMoveBackSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if (!mStop) {
					reset();
				}
			}

			@Override
			public void onAnimationStart(Animator animation) {
				mCircle.setVisibility(View.GONE);
			}
		});

		mStartSet.start();
	}

	private void removeAnimation() {
		mBg2View.clearAnimation();
		mATextShow.removeAllListeners();
		mMTextShow.removeAllListeners();
		mVTextShow.removeAllListeners();
		mITextShow.removeAllListeners();
		mS2TextShow.removeAllListeners();
		mI2TextShow.removeAllListeners();
		mOTextShow.removeAllListeners();
		mNTextShow.removeAllListeners();

		mCircleShow.removeAllListeners();
		mSaveShow.removeAllListeners();
		mTextHide.removeAllListeners();
		mCirclePress.removeAllListeners();
		mCircleMoveBackSet.removeAllListeners();
	}
	/**
	 * 重置
	 */
	private void reset() {

		mHide = false;

		mStartSet.start();

		mBgView.setVisibility(View.VISIBLE);

		mCircle.setVisibility(View.GONE);

		mBg2View.setVisibility(View.GONE);
		mSText.setVisibility(View.GONE);
		mAText.setVisibility(View.GONE);
		mMText.setVisibility(View.GONE);
		mVText.setVisibility(View.GONE);
		mIText.setVisibility(View.GONE);
		mS2Text.setVisibility(View.GONE);
		mI2Text.setVisibility(View.GONE);
		mOText.setVisibility(View.GONE);
		mNText.setVisibility(View.GONE);

		mSaveView.setVisibility(View.GONE);
	}

	/**
	 * 停止动画并释放资源
	 */
	public void release() {
		mStop = true;
		mStartSet.cancel();
		removeAnimation();
	}

	public int PxToDpi_xhdpi(int size) {
		final float density = getContext().getResources().getDisplayMetrics().density;
		return (int)(size / 2f * density + 0.5f);
	}
}
