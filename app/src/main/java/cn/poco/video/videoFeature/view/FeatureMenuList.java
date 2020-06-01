package cn.poco.video.videoFeature.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.video.page.ProcessMode;
import cn.poco.video.videoFeature.cell.FeatureCell;

/**
 * Created by Simon Meng on 2018/1/8.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class FeatureMenuList extends LinearLayout{
    public interface FeatureMenuListCallback {
        void onClickMenuFeature(ProcessMode videoFeature);
        void onBack();
    }


    private Context mContext;
    private ImageView mBackBtn;
    private HorizontalScrollView mMenuScrollView;
    private LinearLayout mSubViewContainer;

    private FeatureCell mClip, mCanvasAdjust, mFilter, mSpeedRate, mSegmentation, mCopy, mDelete;
    private ProcessMode mSelectedFeature;

    public FeatureMenuList(Context context) {
        super(context);
        mContext = context;
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        initView();
        initListener();
    }


    private void initView() {
        LinearLayout.LayoutParams params;

        int color1 = Color.BLACK;
        mBackBtn = new ImageView(mContext);
        mBackBtn.setBackgroundColor(color1);
        params = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(90), ViewGroup.LayoutParams.MATCH_PARENT);
        mBackBtn.setImageResource(R.drawable.video_beautifypage_back);
        mBackBtn.setLayoutParams(params);
        this.addView(mBackBtn);
        mBackBtn.setOnClickListener(mOnClickListener);

        mMenuScrollView = new HorizontalScrollView(mContext);
        mMenuScrollView.setHorizontalScrollBarEnabled(false);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMenuScrollView.setLayoutParams(params);
        this.addView(mMenuScrollView);

        mSubViewContainer = new LinearLayout(mContext);
        mSubViewContainer.setOrientation(HORIZONTAL);
        mSubViewContainer.setGravity(Gravity.CENTER_VERTICAL);
        HorizontalScrollView.LayoutParams params1 = new HorizontalScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mSubViewContainer.setLayoutParams(params1);
        mMenuScrollView.addView(mSubViewContainer);

        int textColorNormal = 0xff999999;
        int textColorSelected = textColorNormal;

        mClip = new FeatureCell(mContext);
        mClip.setBackgroundColor(color1);
        mClip.setIconRes(R.drawable.video_feature_clip_icon, R.drawable.video_feature_clip_icon);
        mClip.setTextColor(textColorNormal, textColorSelected);
        mClip.setFeatureName(getResources().getString(R.string.clipVideo));
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(140), ViewGroup.LayoutParams.MATCH_PARENT);
        mClip.setLayoutParams(params2);
        mSubViewContainer.addView(mClip);
        mClip.setSelectState(true);
        mLastClickView = mClip;

        mCanvasAdjust = new FeatureCell(mContext);
        mCanvasAdjust.setBackgroundColor(color1);
        mCanvasAdjust.setIconRes(R.drawable.video_feature_canvas_icon, R.drawable.video_feature_canvas_icon);
        mCanvasAdjust.setTextColor(textColorNormal, textColorSelected);
        mCanvasAdjust.setLayoutParams(params2);
        mCanvasAdjust.setFeatureName(getResources().getString(R.string.canvasAdjust));
        mSubViewContainer.addView(mCanvasAdjust);


        mFilter = new FeatureCell(mContext);
        mFilter.setBackgroundColor(color1);
        mFilter.setIconRes(R.drawable.beauty_color_btn, R.drawable.beauty_color_btn);
        mFilter.setTextColor(textColorNormal, textColorSelected);
        mFilter.setLayoutParams(params2);
        mFilter.setFeatureName(getResources().getString(R.string.filter));
        mSubViewContainer.addView(mFilter);


        mSpeedRate = new FeatureCell(mContext);
        mSpeedRate.setBackgroundColor(color1);
        mSpeedRate.setIconRes(R.drawable.video_feature_speed_icon, R.drawable.video_feature_speed_icon);
        mSpeedRate.setTextColor(textColorNormal, textColorSelected);
        mSpeedRate.setFeatureName(getResources().getString(R.string.videoSpeed));
        mSpeedRate.setLayoutParams(params2);
        mSubViewContainer.addView(mSpeedRate);

        mSegmentation = new FeatureCell(mContext);
        mSegmentation.setBackgroundColor(color1);
        mSegmentation.setIconRes(R.drawable.video_feature_segmentation_icon, R.drawable.video_feature_segmentation_icon);
        mSegmentation.setTextColor(textColorNormal, textColorSelected);
        mSegmentation.setFeatureName(getResources().getString(R.string.videoSegmentation));
        mSegmentation.setLayoutParams(params2);
        mSubViewContainer.addView(mSegmentation);

        mCopy = new FeatureCell(mContext);
        mCopy.setBackgroundColor(color1);
        mCopy.setIconRes(R.drawable.video_feature_copy_icon, R.drawable.video_feature_copy_icon);
        mCopy.setTextColor(textColorNormal, textColorSelected);
        mCopy.setFeatureName(getResources().getString(R.string.videoCopy));
        mCopy.setLayoutParams(params2);
        mSubViewContainer.addView(mCopy);

        mDelete = new FeatureCell(mContext);
        mDelete.setBackgroundColor(color1);
        mDelete.setIconRes(R.drawable.video_feature_delete_icon, R.drawable.video_feature_delete_icon);
        mDelete.setTextColor(textColorNormal, textColorSelected);
        mDelete.setLayoutParams(params2);
        mDelete.setFeatureName(getResources().getString(R.string.videoDelete));
        mSubViewContainer.addView(mDelete);
    }


    private void initListener() {
        mBackBtn.setOnClickListener(mOnClickListener);
        mClip.setOnClickListener(mOnClickListener);
        mCanvasAdjust.setOnClickListener(mOnClickListener);
        mFilter.setOnClickListener(mOnClickListener);
        mSpeedRate.setOnClickListener(mOnClickListener);
        mSegmentation.setOnClickListener(mOnClickListener);
        mCopy.setOnClickListener(mOnClickListener);
        mDelete.setOnClickListener(mOnClickListener);
    }

    private FeatureMenuListCallback mCallback;
    public void setFeatureMenuCallback(FeatureMenuListCallback callback) {
        this.mCallback = callback;
    }

    public void setCopyFeatureVisibility(boolean isShow) {
        if(!isShow){
            mCopy.setVisibility(View.GONE);
        }else{
            mCopy.setVisibility(View.VISIBLE);
        }
    }

    public void setSplitFeatureVisibility(boolean isShow) {
        if(!isShow){
            mSegmentation.setVisibility(View.GONE);
        }else{
            mSegmentation.setVisibility(View.VISIBLE);
        }
    }

    public void setDeleteFeatureVisibility(boolean isShow) {
        if (!isShow) {
            mDelete.setVisibility(View.GONE);
        } else {
            mDelete.setVisibility(View.VISIBLE);
        }
    }


    private FeatureCell mLastClickView;
    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mBackBtn) {
                if (mCallback != null)  {
                    mCallback.onBack();
                }
            } else {
                if (mLastClickView != null) {
                    mLastClickView.setSelectState(false);
                }
                if (v instanceof FeatureCell) {
                    mLastClickView = (FeatureCell) v;
                    mLastClickView.setSelectState(true);
                }

                if (v == mClip) {
                    mSelectedFeature = ProcessMode.CLIP;
                } else if (v == mCanvasAdjust) {
                    mSelectedFeature = ProcessMode.CANVASADJUST;
                } else if (v == mFilter) {
                    mSelectedFeature = ProcessMode.FILTER;
                } else if (v == mSpeedRate) {
                    mSelectedFeature = ProcessMode.SPEEDRATE;
                } else if (v == mSegmentation) {
                    mSelectedFeature = ProcessMode.SEGENTATION;
                } else if (v == mCopy) {
                    mSelectedFeature = ProcessMode.COPY;
                } else if (v == mDelete) {
                    mSelectedFeature = ProcessMode.DELETE;
                }

                if (mCallback != null) {
                    mCallback.onClickMenuFeature(mSelectedFeature);
                }
            }

        }
    };





}
