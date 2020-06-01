package cn.poco.video.videoFeature.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.poco.tianutils.ShareData;
import cn.poco.video.helper.CacheManager;
import cn.poco.video.page.ProcessMode;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.threadPool.DecodeVideoThreadPool;
import cn.poco.video.utils.VideoUtils;
import cn.poco.video.videoFeature.adapter.HeadFooterPlaceHolderAdapter;

/**
 * Created by Simon Meng on 2017/12/7.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class ClipTimeLineView extends FrameLayout {
    private static final String TAG = "ClipTimeLineView";

    private Context mContext;
    public ClipTimeLineView(@NonNull Context context) {
        super(context);
        mContext = context;
        initData();
        initUi();
//        initHelperClass();
    }

    private int mBitmapFrameWidth, mBitmapFrameHeight;
    private int mDragHandleWidth;
    private int mTopPadding, mBottomPadding;
    protected int mMinimalRect;
    private void initData() {
        mBitmapFrameWidth = ShareData.PxToDpi_xhdpi(80);
        mDragHandleWidth = ShareData.PxToDpi_xhdpi(40);
        mTopPadding = ShareData.PxToDpi_xhdpi(5);
        mBottomPadding = ShareData.PxToDpi_xhdpi(5);
        mBitmapFrameHeight = ShareData.PxToDpi_xhdpi(162);

        mHeaderWidth = mDragHandleWidth;
        mFooterWidth = 0;
    }

    protected RecyclerView mBitmapListView;
    private BitmapAdater mBitmapAdapter;

    private int mHeaderWidth, mFooterWidth;
    private List<Object> mBitmapFrames = new ArrayList<>();
    private VideoSelectView mSelectView;
    protected VideoSeekBar mVideoSeekBar;
    protected ProcessView mProcessView;

    private CacheManager mCacheManager;

    private void initUi() {

        mBitmapListView = new RecyclerView(mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };


        mBitmapListView.setLayoutManager(layoutManager);
        mBitmapListView.setItemAnimator(null);
        mBitmapListView.setHorizontalScrollBarEnabled(false);
        mBitmapListView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBitmapFrameHeight, Gravity.BOTTOM);
//        params1.topMargin = mTopPadding;
        params1.rightMargin = mDragHandleWidth;
        params1.bottomMargin = mBottomPadding;
        mBitmapListView.setLayoutParams(params1);
        this.addView(mBitmapListView);

        mBitmapAdapter = new BitmapAdater(mContext, mBitmapFrames);
        mBitmapListView.setAdapter(mBitmapAdapter);

        mSelectView = new VideoSelectView(this);
        params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBitmapFrameHeight + mTopPadding + mBottomPadding, Gravity.BOTTOM);
        mSelectView.setLayoutParams(params1);
        this.addView(mSelectView);

        mVideoSeekBar = new VideoSeekBar(mContext);
        params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ShareData.PxToDpi_xhdpi(203));
        params1.leftMargin = mDragHandleWidth - ShareData.PxToDpi_xhdpi(20); //VideoSeekBar内部图片宽度的一半
        params1.rightMargin = mDragHandleWidth - ShareData.PxToDpi_xhdpi(20);
        params1.bottomMargin = mBottomPadding;
        mVideoSeekBar.setLayoutParams(params1);
        this.addView(mVideoSeekBar);

        mProcessView = new ProcessView(getContext());
        mProcessView.setPadding(mDragHandleWidth,0,mDragHandleWidth,0);
        params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBitmapFrameHeight, Gravity.BOTTOM);
        params1.bottomMargin = mBottomPadding;
        params1.leftMargin = mDragHandleWidth;
        params1.rightMargin = mDragHandleWidth;
        mProcessView.setProcess(0);
        mProcessView.setClickable(true);
        this.addView(mProcessView, params1);
        mProcessView.setVisibility(View.GONE);
    }

    private void initHelperClass() {
        mCacheManager = new CacheManager();
//        mCacheManager.setCacheCallback(mCacheCallback);
    }


    private List<VideoInfo> mVideoInfoList = new ArrayList<>();
    private int mFrameCountToClip;
    private long mTotalTime;
    public void setVideoList(List<VideoInfo> videoInfoList) {
        mVideoInfoList.clear();
        mVideoInfoList.addAll(videoInfoList);
        mTotalTime = 0;
        for (VideoInfo item : videoInfoList) {
            mTotalTime += item.getVideoTime(mVideoFeature);
        }
    }

    private int mBitmapListWidth;
    protected int mBitmapListVisibleWidth;
    private int mMaxWidth;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != 0 && h!= 0 && !mIsBitmapLoadedFinish) {
            mMaxWidth = w;
            adjustProperDurationToClip(w - mDragHandleWidth * 2);
            presetList(mFrameCountToClip);
            loadFrame(makeVideoTaskList(mVideoInfoList));
        }
    }

    private long mDurationToClip;
    private void adjustProperDurationToClip(int width) {
        int tempCount = (int)Math.ceil((double)width / mBitmapFrameWidth);
        mFrameCountToClip = tempCount;
        mDurationToClip = mTotalTime / tempCount;
        int count = 0;
        for (int i = 0; i < mVideoInfoList.size(); i++) {
            VideoInfo current = mVideoInfoList.get(i);
            if (i == mVideoInfoList.size() - 1) {
                current.mFramesToLoad = mFrameCountToClip - count;
            } else {
                current.mFramesToLoad = mFrameCountToClip / mVideoInfoList.size();
                count += current.mFramesToLoad;
            }
        }

        mBitmapListVisibleWidth = mFrameCountToClip * mBitmapFrameWidth;
        mBitmapListVisibleWidth = mBitmapListVisibleWidth > width ? width : mBitmapListVisibleWidth;
        mBitmapListWidth = mBitmapListVisibleWidth + mDragHandleWidth * 2;
        float mininalPart = mTotalTime / (mMinimalLimitTime * 1.0f);
        mMinimalRect = (int)(mBitmapListVisibleWidth / mininalPart);
    }

    private void presetList(int count) {
        for (int i = 0; i < count; i++) {
            Object placeHolder = new Object();
            mBitmapFrames.add(placeHolder);
        }
        mBitmapAdapter.notifyDataSetChanged();
    }


    private DecodeVideoThreadPool mThreadPool;
    private boolean mIsBitmapLoadedFinish;
    private void loadFrame(List<DecodeVideoThreadPool.VideoTask> videoTaskList) {
        if (mThreadPool == null) {
            mThreadPool = new DecodeVideoThreadPool(5, videoTaskList, new DecodeVideoThreadPool.ThreadPoolCallback() {
                @Override
                public void getDecodedFrame(final DecodeVideoThreadPool.VideoTask videoTask, final Bitmap bitmap, final boolean isFinished) {
                    mBitmapListView.post(new Runnable() {
                        @Override
                        public void run() {
                            mIsBitmapLoadedFinish = isFinished;
                            if (bitmap != null) {
                                Bitmap dstBitmap = VideoUtils.scaleVideoFrameBitmap(bitmap, mBitmapFrameWidth, mBitmapFrameHeight);
                                mBitmapFrames.set(videoTask.mLayoutPosition, dstBitmap);
                                mBitmapAdapter.notifyItemChanged(videoTask.mLayoutPosition);
                            }
                        }
                    });
                }
            });
        } else {
            mThreadPool.setVideoTaskList(videoTaskList);
        }
        mThreadPool.startDecode(false);
    }

    private ProcessMode mVideoFeature = ProcessMode.Edit;
    public void refreshBitmapList(boolean refreshFrame, List<VideoInfo> videoInfoList, ProcessMode videoFeature) {
        mVideoFeature = videoFeature;
        setVideoList(videoInfoList);
        adjustProperDurationToClip(mMaxWidth - mDragHandleWidth * 2);
        if (refreshFrame) {
            loadFrame(makeVideoTaskList(videoInfoList));
        }
        VideoInfo videoInfo = videoInfoList.get(0);
        float leftProgress = videoInfo.GetStartTime() / (videoInfo.getVideoTime(mVideoFeature) * 1.0f);
        float rightProgress = videoInfo.GetEndTime() / (videoInfo.getVideoTime(mVideoFeature) * 1.0f);
        float leftPosition = leftProgress * (mMaxWidth - 2 * mDragHandleWidth);
        float rightPosition = rightProgress * (mMaxWidth - 2 * mDragHandleWidth) + 2* mDragHandleWidth;
        float minimalRightPosition = leftPosition + mMinimalRect + mDragHandleWidth * 2;
        float minimalRightProgress = minimalRightPosition / mMaxWidth;
        if(minimalRightProgress > 1){
            minimalRightProgress = 1;
        }
        if (rightPosition < minimalRightPosition) {
            rightPosition = minimalRightPosition;
            rightProgress = minimalRightProgress;
        }
        mSelectView.setLeftHandlePosition(leftPosition, leftProgress);
        mSelectView.setRightHandlePosition(rightPosition, rightProgress);
        mSelectView.refreshData();
    }

    private long mMinimalLimitTime = 1000; //最小可裁剪时长,毫秒为单位
    public void setMinClipTime(long minClipTime) {
        this.mMinimalLimitTime = minClipTime;
    }

    private List<DecodeVideoThreadPool.VideoTask> makeVideoTaskList(List<VideoInfo> videoInfoList) {
        List<DecodeVideoThreadPool.VideoTask> videoTaskList = new ArrayList<>();
        int indexOfList = 0;
        for (VideoInfo videoInfo : videoInfoList) {
            long duration = videoInfo.mDuration;
            int framesToLoadForThisVideo = videoInfo.mFramesToLoad;
            int localIndex = 0;
            for (int i = 0; i < framesToLoadForThisVideo; i++) {
                DecodeVideoThreadPool.VideoTask videoTask = new DecodeVideoThreadPool.VideoTask();
                videoTask.mDurationOfSelectedVideo = duration;
                videoTask.mVideoPath = videoInfo.getVideoPath(mVideoFeature);
                videoTask.mIndexOfGroup = videoTaskList.size();
                videoTask.mLayoutPosition = indexOfList + 1;
                long decodeFrameTime =  (localIndex * mDurationToClip);
                decodeFrameTime = decodeFrameTime > videoInfo.mDuration ? videoInfo.mDuration : decodeFrameTime;
                videoTask.mFrameDecodedTime = decodeFrameTime;
                videoTaskList.add(videoTask);
                localIndex++;
                indexOfList++;
            }
        }
        return videoTaskList;
    }


    public int getViewVisibleWidth() {
        return mSelectView.getWidth();
    }

    public RectF getSelectRectF() {
        return mSelectView.getSelectRectF();
    }




    public float getLeftProgress() {
        float progress = 0;
        if (mSelectView != null) {
            progress = mSelectView.getLeftProgress();
        }
        return progress;
    }

    public float getRightProgress() {
        float progress = 0;
        if (mSelectView != null) {
            progress = mSelectView.getRightProgress();
        }
        return progress;
    }

    public void setHandleDragVisibility(int visiblity) {
        mSelectView.setVisibility(visiblity);
    }

    public void setDragSeekbarVisibility(int visibility) {
        mVideoSeekBar.setVisibility(visibility);
        if(visibility == View.VISIBLE)
        {
            mVideoSeekBar.setTotalTime((int) mTotalTime);
        }
    }

    public void setProcessClickable(boolean isClickable)
    {
        if(mProcessView != null){
            mProcessView.setClickable(isClickable);
        }
    }

    public void setProgressLineVisibility(int visibility) {
        mProcessView.setVisibility(visibility);
    }

    public void setProgressLineRangeProgress(float start, float end) {
        mProcessView.setRang(start, end);
    }

    public void setProgressLineRangeProgress() {
        mProcessView.setRang(getLeftProgress(),getRightProgress());
    }




    public void setProgress(float process) {
        if (mProcessView != null) {
            mProcessView.setProcess(process);
        }
    }


    public void clear() {
        if (mThreadPool != null) {
            mThreadPool.clear();
        }
    }


    public void setTimeLineProgressCallback(VideoSelectView.ProgressChangeListener listener) {
        if (mSelectView != null) {
            mSelectView.setTimeLineCallback(listener);
        }
    }

    public void setProcessListener(ProcessView.ProcessLister listener)
    {
        if(mProcessView != null){
            mProcessView.setProcessLister(listener);
        }
    }

    public void setOnSeekBarProgressListener(VideoSeekBar.OnSeekBarChangeListener listener) {
        if (mVideoSeekBar != null) {
            mVideoSeekBar.setOnSeekBarChangeListener(listener);
        }
    }


    private class BitmapAdater extends HeadFooterPlaceHolderAdapter<Object> {

        public BitmapAdater(Context context, List<Object> bitmapList) {
            super(context, bitmapList);
        }

        @Override
        protected void initHeaderAndFooter() {
            mHeaderW = mHeaderWidth;
            Object head = new Object();
            mDataList.add(0, head);

            mFooterW = mFooterWidth;
            Object footer = new Object();
            mDataList.add(footer);
            mHeight = ShareData.PxToDpi_xhdpi(110);
        }

        @Override
        protected HeadFooterPlaceHolderAdapter.ViewHolder onCreateViewHolder(int viewType) {
            RecyclerView.LayoutParams params = null;
            HeadFooterPlaceHolderAdapter.ViewHolder viewHolder;
            ImageView imageView = new ImageView(mContext);
            if (viewType == 2) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                params = new RecyclerView.LayoutParams(mBitmapFrameWidth, mBitmapFrameHeight);
            }
            imageView.setLayoutParams(params);
            viewHolder = new ViewHolder(imageView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ImageView imageView = (ImageView) holder.itemView;
            Object item = mDataList.get(position);
            if (item instanceof Bitmap) {
                Bitmap bitmap = (Bitmap) item;
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageBitmap(null);
            }
        }
    }

}
