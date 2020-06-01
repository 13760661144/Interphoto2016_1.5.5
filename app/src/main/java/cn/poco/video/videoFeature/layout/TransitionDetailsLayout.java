package cn.poco.video.videoFeature.layout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.ShareData;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.sequenceMosaics.TransitionDataInfo;
import cn.poco.video.videoFeature.VideoGroupInfo;
import cn.poco.video.videoFeature.view.ImageStitchingView;
import cn.poco.video.videoFeature.view.TransitionTypeList;

/**
 * Created by Simon Meng on 2018/1/9.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class TransitionDetailsLayout extends LinearLayout{
   public interface TransitionDetailsLayoutCallback {
       void onApplyEffetToAllTransition(TransitionDataInfo transitionDataInfo);
       void onBack();
       void onTransitionEffectSelected(VideoGroupInfo videoGroupInfo);
       void onDispear();
       boolean canAddTransition(int index);  //针对叠加，这个需要前后视频都是2S以上
   }

    private Context mContext;

    private LinearLayout mBtnContainer;
    private ImageView mApplyToAllBtn;
    private TextView mTextView;

    private ImageStitchingView mStitchingView;
    private Toast mToast;
    private TransitionTypeList mTransitionTypeList;
    private TransitionDetailsLayoutCallback mCallback;

    public TransitionDetailsLayout(@NonNull Context context) {
        super(context);
        this.setBackgroundColor(0xff0e0e0e);
        this.setOrientation(VERTICAL);
        this.setClickable(true);
//        setGravity(Gravity.CENTER);
        mContext = context;
        initView();
        MyBeautyStat.onPageStartByRes(R.string.转场动画页);
    }

    private void initView() {
        LinearLayout.LayoutParams params;

        mBtnContainer = new LinearLayout(mContext);
        mBtnContainer.setOrientation(LinearLayout.HORIZONTAL);
        mBtnContainer.setGravity(Gravity.CENTER_VERTICAL);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = ShareData.PxToDpi_xhdpi(16);
        params.topMargin = ShareData.PxToDpi_xhdpi(16);
        mBtnContainer.setLayoutParams(params);
        this.addView(mBtnContainer);
        mBtnContainer.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MyBeautyStat.onClickByRes(R.string.转场动画页_应用至所有转场);
                if (mTransitionTypeList.getSelectedTransitionId() != 0) {
                    MyBeautyStat.onClickByRes(mTransitionTypeList.getSelectedTransitionId());
                }

                if (mCallback != null) {
                    mCallback.onApplyEffetToAllTransition(mTransitionTypeList.getSelectedTransitionEffect());
                }
                closeHorizontalTransition(mTransitionTypeList, ShareData.m_screenWidth, 0);
                startAlphaAnimation(mBtnContainer, 1, 0, null);
                startAlphaAnimation(mStitchingView, 1, 0, null);
            }
        });
        {
            LinearLayout.LayoutParams params1;
            mApplyToAllBtn = new ImageView(mContext);
            mApplyToAllBtn.setImageResource(R.drawable.video_beautifypage_apply);
            params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params1.leftMargin = ShareData.PxToDpi_xhdpi(12);
            mApplyToAllBtn.setLayoutParams(params1);
            mBtnContainer.addView(mApplyToAllBtn);

            mTextView = new TextView(mContext);
            mTextView.setText(R.string.applyToAllTransition);
            mTextView.setTextColor(Color.WHITE);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            mTextView.setLayoutParams(params1);
            mBtnContainer.addView(mTextView);
        }

        FrameLayout frameLayout = new FrameLayout(getContext());
//        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,Gravity.BOTTOM);
//        params.bottomMargin = ShareData.PxToDpi_xhdpi(180);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        addView(frameLayout,params);
        {
            FrameLayout.LayoutParams fl;
            mStitchingView = new ImageStitchingView(mContext);
            fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(210),Gravity.CENTER);
            mStitchingView.setLayoutParams(fl);
            frameLayout.addView(mStitchingView);
        }

        mTransitionTypeList = new TransitionTypeList(mContext);
//        params = new LinearLayout.LayoutParams(ShareData.m_screenWidth, ShareData.PxToDpi_xhdpi(180),Gravity.BOTTOM);
        params = new LinearLayout.LayoutParams(ShareData.m_screenWidth, ShareData.PxToDpi_xhdpi(180));
        params.leftMargin = -ShareData.m_screenWidth;
        mTransitionTypeList.setLayoutParams(params);
        this.addView(mTransitionTypeList);
        mTransitionTypeList.setTransitionTypeListCallback(new TransitionTypeList.TransitionTypeListCallback() {
            @Override
            public boolean canClick(int transitionID)
            {
                boolean flag = false;
                switch(transitionID)
                {
                    case TransitionItem.NONE:
                    case TransitionItem.BLACK:
                    case TransitionItem.WHITE:
                        flag = true;
                        break;
                    case TransitionItem.BLUR:
                    case TransitionItem.ALPHA:
                        if(mCallback != null && mCallback.canAddTransition(mVideoGroupInfo.mGroupIndex))
                        {
                            flag = true;
                        }
                        else{
                            flag = false;
                            mToast.show();
                        }
                        break;
                }
                return flag;
            }

            @Override
            public void onClickTransitionType(TransitionDataInfo transitionDataInfo) {
                mVideoGroupInfo.mTransitionDataInfo = transitionDataInfo;
                mStitchingView.setTransitionInfo(transitionDataInfo);
                if (mCallback != null) {
                    mCallback.onTransitionEffectSelected(mVideoGroupInfo);
                }

            }

            @Override
            public void onBack() {
                closeHorizontalTransition(mTransitionTypeList, ShareData.m_screenWidth, 0);
                startAlphaAnimation(mBtnContainer, 1, 0, null);
                startAlphaAnimation(mStitchingView, 1, 0, null);
                if (mCallback != null) {
                    mCallback.onBack();
                }
            }
        });

        mToast = Toast.makeText(getContext(), R.string.video_tran_unavailable, Toast.LENGTH_SHORT);
    }

    private VideoGroupInfo mVideoGroupInfo;
    public void show(VideoGroupInfo videoGroupInfo) {
        mVideoGroupInfo = videoGroupInfo;
        mTransitionTypeList.setSelectedTransitionType(videoGroupInfo.mTransitionDataInfo);
        mStitchingView.setTransitionInfo(videoGroupInfo.mTransitionDataInfo, videoGroupInfo.mLeftBitmapPath, videoGroupInfo.mRightBitmapPath);
        startHorizontalTransition(mTransitionTypeList, 0, ShareData.m_screenWidth);
        startAlphaAnimation(mBtnContainer, 0, 1, null);
        startAlphaAnimation(mStitchingView, 0, 1, null);
    }

    public void disapear() {
        closeHorizontalTransition(mTransitionTypeList, ShareData.m_screenWidth, 0);
        startAlphaAnimation(mBtnContainer, 1, 0, null);
        startAlphaAnimation(mStitchingView, 1, 0, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mCallback != null) {
                    mCallback.onDispear();
                    mCallback = null;
                }
            }
        });
        if (mTransitionTypeList.getSelectedTransitionId() != 0) {
            MyBeautyStat.onClickByRes(mTransitionTypeList.getSelectedTransitionId());
        }
        MyBeautyStat.onPageEndByRes(R.string.转场动画页);
    }


    public void setTransitionDetailsLayout(TransitionDetailsLayoutCallback callback) {
        this.mCallback = callback;
    }

    // 过渡动画函数
    private void startHorizontalTransition(View v, float startPosition, float endPosition) {
        if (v.getVisibility() != VISIBLE) {
            v.setVisibility(View.VISIBLE);
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.TRANSLATION_X, startPosition, endPosition);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    private Animator startAlphaAnimation(View v, float startAlpha, float endAlpha, final Animator.AnimatorListener listener) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.ALPHA, startAlpha, endAlpha);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
        if (listener != null) {
            animator.addListener(listener);
        }
        return animator;
    }

    private void closeHorizontalTransition (final View v, float startPosition, float endPosition) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, View.TRANSLATION_X, startPosition, endPosition);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
            }
        });
    }

}
