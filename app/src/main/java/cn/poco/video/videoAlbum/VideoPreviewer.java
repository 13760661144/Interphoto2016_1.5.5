package cn.poco.video.videoAlbum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import cn.poco.video.model.VideoEntry;

/**
 * Created by Shine on 2017/5/17.
 */

public class VideoPreviewer {
    private Context mParentContext;
    private ViewGroup mParent;
    private VideoPlayFrame mContainerView;

    private static volatile VideoPreviewer sInstance = null;
    private float mLastLayerValue;
    private VideoEntry mVideoEntry;

    private boolean mPreviewStart;

    public static VideoPreviewer getInstance() {
        VideoPreviewer localInstance = sInstance;
        if (localInstance == null) {
            synchronized (VideoPreviewer.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new VideoPreviewer();
                }
            }
        }
        return localInstance;
    }

    private VideoPreviewer() {
        initData();
    }

    private int mAnimationTime;
    private void initData() {
        mAnimationTime = 200;
    }

    public void setParentContext(ViewGroup parent) {
        mParentContext = parent.getContext();
        mParent = parent;
        mContainerView = new VideoPlayFrame(mParentContext);
        if (mContainerView.getParent() != null) {
            ViewParent viewParent = mContainerView.getParent();
            ViewGroup parentView = (ViewGroup) viewParent;
            parentView.removeView(mContainerView);
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainerView.setLayoutParams(params);
        mParent.addView(mContainerView);
    }

    public void previewSelectedVideo(VideoEntry videoEntry) {
        mPreviewStart = true;
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 0.6f);
        if (mContainerView.getVisibility() != View.VISIBLE) {
            mContainerView.setVisibility(View.VISIBLE);
        }
        mVideoEntry = videoEntry;
        mContainerView.setVideoModel(mVideoEntry);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentValue = (Float)animation.getAnimatedValue();
                mLastLayerValue = currentValue;
                mContainerView.setProgress(currentValue);
            }
        });

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mContainerView.mVideoView, "alpha", 0f, 1f);
        animatorSet.playTogether(valueAnimator, animator2);
        animatorSet.setDuration(mAnimationTime);
        animatorSet.start();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                mContainerView.stopPlay();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mContainerView.playVideo();
            }
        });
    }


    public boolean isVideoPreviewing() {
        return mPreviewStart;
    }


    public void closePreview() {
        if (mPreviewStart) {
            AnimatorSet animatorSet = new AnimatorSet();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(mLastLayerValue, 0f);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float currentValue = (Float)animation.getAnimatedValue();
                    mContainerView.setProgress(currentValue);
                }
            });

            ObjectAnimator animator2 = ObjectAnimator.ofFloat(mContainerView.mVideoView, "alpha", mContainerView.mVideoView.getAlpha(), 0f);
            animatorSet.playTogether(valueAnimator, animator2);
            animatorSet.setDuration(mAnimationTime);
            animatorSet.start();
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    mContainerView.stopPlay();
                    mPreviewStart = false;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mContainerView.stopPlay();
                    mPreviewStart = false;
                }
            });
        }
    }

    public void clear() {
        mContainerView.clear();
        sInstance = null;
    }


}
