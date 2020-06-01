package cn.poco.video.videoFeature.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.sequenceMosaics.TransitionDataInfo;
import cn.poco.video.videoFeature.cell.FeatureCell;

/**
 * Created by Simon Meng on 2018/1/4.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class TransitionTypeList extends FrameLayout{
    public interface TransitionTypeListCallback {
        void onClickTransitionType(TransitionDataInfo transitionDataInfo);
        void onBack();
        boolean canClick(int transitionID);
    }

    private Context mContext;
    private ImageView mBackBtn;
    private HorizontalScrollView mMenuScrollView;
    private LinearLayout mSubViewContainer;

    private FeatureCell mNoEffect, mCrossStack, mBlackSceneTransition, mWhiteSceneTransition, mBlurryTransition;

    private TransitionTypeListCallback mCallback;

    public TransitionTypeList(Context context) {
        super(context);
        mContext = context;
        initView();
        initListener();
    }

    private void initView() {
        LayoutParams params;

        int color1 = Color.BLACK;
        mBackBtn = new ImageView(mContext);
        mBackBtn.setBackgroundColor(color1);
        params = new LayoutParams(ShareData.PxToDpi_xhdpi(90), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_VERTICAL);
        mBackBtn.setImageResource(R.drawable.video_beautifypage_back);
        mBackBtn.setLayoutParams(params);
        this.addView(mBackBtn);
        mBackBtn.setOnClickListener(mOnClickListener);

        mMenuScrollView = new HorizontalScrollView(mContext);
        mMenuScrollView.setHorizontalScrollBarEnabled(false);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.leftMargin = ShareData.PxToDpi_xhdpi(90);
        params.gravity = Gravity.CENTER_VERTICAL;
        mMenuScrollView.setLayoutParams(params);
        this.addView(mMenuScrollView);

        mSubViewContainer = new LinearLayout(mContext);
        mSubViewContainer.setOrientation(LinearLayout.HORIZONTAL);
        mSubViewContainer.setGravity(Gravity.CENTER_VERTICAL);
        HorizontalScrollView.LayoutParams params1 = new HorizontalScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSubViewContainer.setLayoutParams(params1);
        mMenuScrollView.addView(mSubViewContainer);

        int textColorNormal = 0xff999999;
        int textColorSelected = 0xffffc433;

        mNoEffect = new FeatureCell(mContext);
        mNoEffect.setBackgroundColor(color1);
        mNoEffect.setIconRes(R.drawable.transition_none_out, R.drawable.transition_none_over);
        mNoEffect.setTextColor(textColorNormal, textColorSelected);
        mNoEffect.setFeatureName(getResources().getString(R.string.transition_none));
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(140), ViewGroup.LayoutParams.MATCH_PARENT);
        mNoEffect.setLayoutParams(params2);
        mSubViewContainer.addView(mNoEffect);
        mNoEffect.setSelectState(true);
        mLastClickView = mNoEffect;

        mCrossStack = new FeatureCell(mContext);
        mCrossStack.setBackgroundColor(color1);
        mCrossStack.setIconRes(R.drawable.transition_overlap_out, R.drawable.transition_overlap_over);
        mCrossStack.setTextColor(textColorNormal, textColorSelected);
        mCrossStack.setLayoutParams(params2);
        mCrossStack.setFeatureName(getResources().getString(R.string.transition_overlay));
        mSubViewContainer.addView(mCrossStack);

        mBlackSceneTransition = new FeatureCell(mContext);
        mBlackSceneTransition.setBackgroundColor(color1);
        mBlackSceneTransition.setIconRes(R.drawable.transition_black_out, R.drawable.transition_black_over);
        mBlackSceneTransition.setTextColor(textColorNormal, textColorSelected);
        mBlackSceneTransition.setFeatureName(getResources().getString(R.string.transition_black));
        mBlackSceneTransition.setLayoutParams(params2);
        mSubViewContainer.addView(mBlackSceneTransition);

        mWhiteSceneTransition = new FeatureCell(mContext);
        mWhiteSceneTransition.setBackgroundColor(color1);
        mWhiteSceneTransition.setIconRes(R.drawable.transition_white_out, R.drawable.transition_white_over);
        mWhiteSceneTransition.setTextColor(textColorNormal, textColorSelected);
        mWhiteSceneTransition.setFeatureName(getResources().getString(R.string.transition_white));
        mWhiteSceneTransition.setLayoutParams(params2);
        mSubViewContainer.addView(mWhiteSceneTransition);

        mBlurryTransition = new FeatureCell(mContext);
        mBlurryTransition.setBackgroundColor(color1);
        mBlurryTransition.setIconRes(R.drawable.transition_fuzzy_out, R.drawable.transition_fuzzy_over);
        mBlurryTransition.setTextColor(textColorNormal, textColorSelected);
        mBlurryTransition.setFeatureName(getResources().getString(R.string.transition_fuzzy));
        mBlurryTransition.setLayoutParams(params2);
        mSubViewContainer.addView(mBlurryTransition);
    }

    private void initListener() {
        mNoEffect.setOnClickListener(mOnClickListener);
        mCrossStack.setOnClickListener(mOnClickListener);
        mBlackSceneTransition.setOnClickListener(mOnClickListener);
        mWhiteSceneTransition.setOnClickListener(mOnClickListener);
        mBlurryTransition.setOnClickListener(mOnClickListener);
    }

    public void setTransitionTypeListCallback(TransitionTypeListCallback callback) {
       this.mCallback = callback;
    }

    private TransitionDataInfo mSelectedOne;
    public void setSelectedTransitionType(TransitionDataInfo transitionDataInfo) {
        reInitializeState();
        mSelectedOne = transitionDataInfo;
        switch (transitionDataInfo.mID) {
            case TransitionItem.NONE : {
                mNoEffect.setSelectState(true);
                mLastClickView = mNoEffect;
                break;
            }

            case TransitionItem.ALPHA : {
                mCrossStack.setSelectState(true);
                mLastClickView = mCrossStack;
                break;
            }

            case TransitionItem.BLACK : {
                mBlackSceneTransition.setSelectState(true);
                mLastClickView = mBlackSceneTransition;
                break;
            }

            case TransitionItem.WHITE : {
                mWhiteSceneTransition.setSelectState(true);
                mLastClickView = mWhiteSceneTransition;
                break;
            }

            case TransitionItem.BLUR: {
                mBlurryTransition.setSelectState(true);
                mLastClickView = mBlurryTransition;
                break;
            }
        }
    }

    private void reInitializeState() {
        mNoEffect.setSelectState(false);
        mCrossStack.setSelectState(false);
        mBlackSceneTransition.setSelectState(false);
        mWhiteSceneTransition.setSelectState(false);
        mBlurryTransition.setSelectState(false);
    }



    public TransitionDataInfo getSelectedTransitionEffect() {
        return mSelectedOne;
    }

    private int mSelectedTransitionId;
    private FeatureCell mLastClickView;
    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mBackBtn) {
                if (mCallback != null)  {
                    mCallback.onBack();
                }
            } else {
                TransitionDataInfo transitionDataInfo = new TransitionDataInfo();
                if (v == mNoEffect) {
                    transitionDataInfo.mID = TransitionItem.NONE;
                    transitionDataInfo.mResSelected = R.drawable.video_transition_none;
                    transitionDataInfo.mResDefault = R.drawable.video_transition_none_default;
                    mSelectedTransitionId = R.string.转场动画页_无转场;
                } else if (v == mCrossStack) {
                    transitionDataInfo.mID = TransitionItem.ALPHA;
                    transitionDataInfo.mResSelected = R.drawable.video_transition_crossstack;
                    transitionDataInfo.mResDefault = R.drawable.video_transition_cross_default;
                    mSelectedTransitionId = R.string.转场动画页_交叉叠化;
                } else if (v == mBlackSceneTransition) {
                    transitionDataInfo.mID = TransitionItem.BLACK;
                    transitionDataInfo.mResSelected = R.drawable.video_transition_black;
                    transitionDataInfo.mResDefault = R.drawable.video_transition_black_default;
                    mSelectedTransitionId = R.string.转场动画页_黑场过渡;
                } else if (v == mWhiteSceneTransition) {
                    transitionDataInfo.mID = TransitionItem.WHITE;
                    transitionDataInfo.mResSelected = R.drawable.video_white_transition;
                    transitionDataInfo.mResDefault = R.drawable.video_transition_while_default;
                    mSelectedTransitionId = R.string.转场动画页_白场过渡;
                } else if (v == mBlurryTransition) {
                    transitionDataInfo.mID = TransitionItem.BLUR;
                    transitionDataInfo.mResSelected = R.drawable.video_transition_blurry;
                    transitionDataInfo.mResDefault = R.drawable.video_transition_blurry_default;
                    mSelectedTransitionId = R.string.转场动画页_模糊;
                }

                if(mCallback != null && mCallback.canClick(transitionDataInfo.mID))
                {
                    mSelectedOne = transitionDataInfo;
                    if (mLastClickView != null) {
                        mLastClickView.setSelectState(false);
                    }
                    if (v instanceof FeatureCell) {
                        mLastClickView = (FeatureCell) v;
                        mLastClickView.setSelectState(true);
                    }
                    if (mCallback != null) {
                        mCallback.onClickTransitionType(transitionDataInfo);
                    }
                }
            }
        }
    };

    public int getSelectedTransitionId() {
        return mSelectedTransitionId;
    }



}
