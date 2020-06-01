package cn.poco.video.videotext;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.poco.framework.FileCacheMgr;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.FileUtil;
import cn.poco.video.helper.CacheManager;
import cn.poco.video.helper.TimeFormatter;
import cn.poco.video.helper.VideoMediator;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.threadPool.DecodeVideoThreadPool;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by Simon Meng on 2017/10/25.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class WatermarkTimeLineView extends FrameLayout{
    public enum TimeLineMode{
        BEGINNING,
        WHOLE,
        END;
    }

    public interface WaterTimeLineListener {
        void onWaterMarkTimeChange(long startTime, long endTime);

        void onBack();
    }

    private static final String TAG = "WatermarkTimeLineView";
    protected static final int FRAME_OFFSET_TIME = 1000;
    private Context mContext;
    // UI布局
    protected RecyclerView mBitmapListView;
    private BitmapAdater mBitmapAdapter;;
    private ProgressSelectView mSelectView;
    private FrameLayout mTimeTextContainer;

    //UI布局数据
    protected int mBitmapFrameWidth, mBitmapFrameHeight;
    protected int mFramesToLoad; // 总共需要加载的幁数

    private int mTopPadding, mBottomPadding, mLeftPadding;
    private int mHeaderWidth, mFooterWidth;

    private List<Object> mBitmapFrames = new ArrayList<>();
    protected boolean mIsLoadingBitmap;

    private long mFrameTimeOffset;  //单位为毫秒
    protected int mBitmapListWidth; //截取的图片的长度

    protected int mTimeLineViewWidth; // 整个控件的长度, 包括左右两个用于拖动的边框,包含未显示出来的可拖拉的部分
    protected int mSelectRectWidth; // 被选中的区域的长度，不包括左右两个边框
    protected int mSelectViewWidth; // 被选中的区域的长度，包括左右两个用于拖动的边框
    protected int mMinimalRect;
    protected boolean mNeedSplitScreen; //视频剪辑的类型

    private WaterTimeLineListener mListener;
    private CacheManager mCacheManager;

    public WatermarkTimeLineView(Context context) {
        super(context);
        mContext = context;
        initData();
        initHelperClass();
    }

    /**
     * 初始化布局数据
     */
    private void initData() {
        mTopPadding = ShareData.PxToDpi_xhdpi(5);
        mBottomPadding = ShareData.PxToDpi_xhdpi(5);
        mLeftPadding = ShareData.PxToDpi_xhdpi(30);
        mBitmapFrameHeight = ShareData.PxToDpi_xhdpi(100);
        mFrameTimeOffset = FRAME_OFFSET_TIME;
    }

    private void initHelperClass() {
        mCacheManager = new CacheManager();
        mCacheManager.setCacheCallback(mCacheCallback);
    }


    private List<VideoInfo> mVideoList = new ArrayList<>();
    protected long mVideoTotalLength; // 单位为毫秒
    public void setVideoInfoList(List<VideoInfo> videoList) {
        mVideoList.clear();
        mVideoList.addAll(videoList);
        mVideoTotalLength = 0;
        for (VideoInfo videoInfo : mVideoList) {
            long duration = videoInfo.mSelectEndTime - videoInfo.mSelectStartTime;
            mVideoTotalLength += duration;
            int framesToLoadForThisVideo = (int)(duration / mFrameTimeOffset) + 1;
            mFramesToLoad += framesToLoadForThisVideo;
        }
        preSetListViewData(mFramesToLoad);
        buildLayout();
    }

    private void preSetListViewData(int itemCount) {
        for (int i = 0; i < itemCount + 1; i++) {
            Object placeHolder = new Object();
            mBitmapFrames.add(placeHolder);
        }
    }

    /**
     * 调整裁剪freeStyle的正确模式
     */
    private void adjustFreeStyleType() {
        // 单张图片的宽度
        mBitmapFrameWidth = mScreenLimitWidth / 15;
        int totalLength = mFramesToLoad * mBitmapFrameWidth;
        if (totalLength >= mScreenLimitWidth - BACK_IMAGE_WIDTH - mLeftPadding * 2) {
            // 需要分屏
            mNeedSplitScreen = true;
        } else {
            // 不需要分屏
            mNeedSplitScreen = false;
        }
        // 所有图片的长度
        mBitmapListWidth = totalLength;
    }



    private TextView mTimeStartText, mTimeEndText;
    private void buildLayout() {
        mBitmapListView = new RecyclerView(mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mBitmapListView.setLayoutManager(layoutManager);
        mBitmapListView.setHorizontalScrollBarEnabled(false);
        mBitmapListView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        mHeaderWidth = mLeftPadding;
        mFooterWidth = mLeftPadding;
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mBitmapListView.setLayoutParams(params1);
        params1.topMargin = mTopPadding;
        params1.bottomMargin = mBottomPadding;
        this.addView(mBitmapListView);

        mBitmapAdapter = new BitmapAdater(mContext, mBitmapFrames);

        mSelectView = new ProgressSelectView(this);
        params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mSelectView.setLayoutParams(params1);
        this.addView(mSelectView);

        mTimeTextContainer = new FrameLayout(mContext);
        params1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params1.topMargin = mBitmapFrameHeight + mTopPadding + mBottomPadding;
        mTimeTextContainer.setLayoutParams(params1);
        this.addView(mTimeTextContainer);

        mTimeStartText = new TextView(mContext);
        mTimeStartText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        mTimeStartText.setSingleLine(true);
        mTimeStartText.setLines(1);
        mTimeStartText.setText(mCurrentLeft);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
        mTimeStartText.setLayoutParams(params);
        mTimeTextContainer.addView(mTimeStartText);

        mTimeEndText = new TextView(mContext);
        mTimeEndText.setSingleLine(true);
        mTimeEndText.setLines(1);
        mTimeEndText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        mTimeEndText.setText(mCurrentRight);
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT);
        mTimeEndText.setLayoutParams(params);
        mTimeTextContainer.addView(mTimeEndText);
        mSelectView.setTimeLineCallback(mTimeLineListener);
    }


    private DecodeVideoThreadPool mThreadPool;
    private Map<String, DecodeVideoThreadPool.VideoTask> mPathToIndexMap = new HashMap<>();
    private void loadFrame(List<DecodeVideoThreadPool.VideoTask> videoTaskList) {
        if (mThreadPool == null) {
            mThreadPool = new DecodeVideoThreadPool(5, videoTaskList, new DecodeVideoThreadPool.ThreadPoolCallback() {
                @Override
                public void getDecodedFrame(final DecodeVideoThreadPool.VideoTask videoTask, final Bitmap bitmap, final boolean isFinished) {
                    mBitmapListView.post(new Runnable() {
                        @Override
                        public void run() {
                            mIsLoadingBitmap = !isFinished;
                            if (bitmap != null) {
                                Bitmap dstBitmap = VideoUtils.scaleVideoFrameBitmap(bitmap, mBitmapFrameWidth, mBitmapFrameHeight);
                                String bitmapFileName = FileCacheMgr.GetLinePath().concat(".img");
                                videoTask.mImgCachePath = bitmapFileName;
                                mCacheManager.saveBitmapToSdCard(bitmapFileName, dstBitmap);
                                mPathToIndexMap.put(bitmapFileName, videoTask);
                            }
                        }
                    });
                }
            });
        } else {
            mThreadPool.setVideoTaskList(videoTaskList);
        }
        boolean isReverse = mMode == TimeLineMode.END ? true : false;
        mThreadPool.startDecode(isReverse);
        mLoadedFrame = true;
    }

    private int mScreenLimitWidth;
    private boolean mGetSize;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != 0) {
            mGetSize = true;
            mScreenLimitWidth = w;
            adjustFreeStyleType();
            layoutForDifferentTime();
        }
    }

    /**
     *
     * @return 查询是否正在加载图片
     */
    public boolean isLoadingBitmap() {
        return mIsLoadingBitmap;
    }

    /**
     * 获取进度条左边的进度
     * @return 左边的进度
     */
    public float getLeftProgress() {
        return mSelectView.getLeftProgress();
    }

    /**
     * 获取进度条右边的进度
     * @return 右边的进度
     */
    public float getRightProgress() {
        return mSelectView.getRightProgress();
    }



    private String mCurrentLeft, mCurrentRight;
    private long mStartTime, mEndTime, mMinimalTime, mElapsedTime;
    private float mLeftProgress, mRightProgress;
    private TimeLineMode mMode;
    private boolean mLoadedFrame;

    public void setTime(long startTime, long endTime, long minimalLimit, TimeLineMode mode) {
        mMode = mode;
        mStartTime = startTime;
        mEndTime = endTime;
        this.mMinimalTime = minimalLimit;
        if (mStartTime > mEndTime) {
            throw new RuntimeException("how can startTime later than end time?");
        }
        mElapsedTime = Math.abs(mStartTime - mEndTime);
        mElapsedTime = mElapsedTime > mVideoTotalLength ? mVideoTotalLength : mElapsedTime;

        mEndTime = mEndTime > mVideoTotalLength ? mVideoTotalLength : mEndTime;
        long tempStartTime = mEndTime - mElapsedTime;
        mStartTime = mStartTime > tempStartTime ? tempStartTime : mStartTime;

        mMinimalTime = mMinimalTime > mVideoTotalLength ? mVideoTotalLength : mMinimalTime;
        mLeftProgress = mStartTime * 1.0f / mVideoTotalLength;
        mRightProgress = mEndTime * 1.0f / mVideoTotalLength;

        if (!mLoadedFrame) {
            List<DecodeVideoThreadPool.VideoTask> videoTaskList = new ArrayList<>();

            int indexOfList = 0;
            for (VideoInfo videoInfo : mVideoList) {
                long duration = videoInfo.mSelectEndTime - videoInfo.mSelectStartTime;
                int framesToLoadForThisVideo = (int)(duration / mFrameTimeOffset) + 1;
                int localIndex = 0;
                for (int i = 0; i < framesToLoadForThisVideo; i++) {
                    DecodeVideoThreadPool.VideoTask videoTask = new DecodeVideoThreadPool.VideoTask();
                    videoTask.mDurationOfSelectedVideo = duration;
                    videoTask.mVideoPath = videoInfo.mPath;
                    videoTask.mIndexOfGroup = videoTaskList.size();
                    videoTask.mLayoutPosition = indexOfList + 2;
                    long decodeFrameTime = videoInfo.mSelectStartTime + (localIndex * mFrameTimeOffset);
                    decodeFrameTime = decodeFrameTime > videoInfo.mSelectEndTime ? videoInfo.mSelectEndTime : decodeFrameTime;
                    videoTask.mFrameDecodedTime = decodeFrameTime;
                    videoTaskList.add(videoTask);
                    localIndex++;
                    indexOfList++;
                }
            }
            loadFrame(videoTaskList);
        }
        layoutForDifferentTime();
    }

    private void calculateData() {
        mMinimalRect = (int)(mMinimalTime * 1.0f / mVideoTotalLength * mBitmapListWidth);
        float timeRate = mElapsedTime * 1.0f / mVideoTotalLength;
        mSelectRectWidth = (int)(timeRate * mBitmapListWidth);
        mSelectViewWidth = mSelectRectWidth + mLeftPadding * 2;
        mTimeLineViewWidth = mBitmapListWidth + mLeftPadding * 2 + BACK_IMAGE_WIDTH;
    }

    private void layoutForDifferentTime() {
        if (mGetSize) {
            clearModeData();
            calculateData();
            mBitmapListView.setAdapter(mBitmapAdapter);

            if (!mNeedSplitScreen) {
                FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)mSelectView.getLayoutParams();
                params.width = mTimeLineViewWidth;
            } else {
                FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)mSelectView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            int endPosition = mTimeLineViewWidth;

            int startX = 0;
            if (mMode == TimeLineMode.END) {
                mSelectView.setLeftHandle(true);
                mSelectView.setRightHandle(false);
                startX = endPosition - mSelectViewWidth;
                startX = startX < BACK_IMAGE_WIDTH ? BACK_IMAGE_WIDTH : startX;
            } else if (mMode == TimeLineMode.BEGINNING) {
                mSelectView.setLeftHandle(false);
                mSelectView.setRightHandle(true);
                startX = 0 + BACK_IMAGE_WIDTH;
            } else if (mMode == TimeLineMode.WHOLE) {
                mSelectViewWidth = mSelectRectWidth + mLeftPadding * 2;
                startX = (int)(mLeftProgress * mBitmapListWidth) + BACK_IMAGE_WIDTH;
                startX = startX < BACK_IMAGE_WIDTH ? BACK_IMAGE_WIDTH : startX;
                mSelectView.setLeftHandle(true);
                mSelectView.setRightHandle(true);
            }
            mSelectView.setTouchPointCoordination(startX, BACK_IMAGE_WIDTH);
            mSelectView.setLeftProgress(mLeftProgress);
            mSelectView.setRightProgress(mRightProgress);

            mCurrentLeft = makeTimeInFormat(mSelectView.getLeftProgress());
            mCurrentRight = makeTimeInFormat(mSelectView.getRightProgress());
            mTimeStartText.setText(mCurrentLeft);
            mTimeEndText.setText(mCurrentRight);
            reLayoutTimeText();
            this.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
    }

    private void clearModeData() {
        mSelectView.reset();
    }


    /**
     * 清理资源
     */
    public void clear() {
        mThreadPool.clear();
        mCacheManager.clear();
        mListener = null;
        Glide.get(mContext).clearMemory();
    }

    private static final int BACK_IMAGE_WIDTH = ShareData.PxToDpi_xxhdpi(120) + ShareData.PxToDpi_xxhdpi(60);
    private List<DecodeVideoThreadPool.VideoTask> mRecoverVideoTaskList = new ArrayList<>();
    private class BitmapAdater extends HeadFooterEnableAdapter<Object> {

        public BitmapAdater(Context context, List<Object> bitmapList) {
            super(context, bitmapList);
        }

        @Override
        protected void initHeaderAndFooter() {
            mHeaderW = mHeaderWidth;
            Object head = new Object();
            mDataList.add(1, head);

            mFooterW = mFooterWidth;
            Object footer = new Object();
            mDataList.add(footer);
            mHeight = ShareData.PxToDpi_xhdpi(110);
        }

        @Override
        protected HeadFooterEnableAdapter.ViewHolder onCreateViewHolder(int viewType) {
            RecyclerView.LayoutParams params = null;
            HeadFooterEnableAdapter.ViewHolder viewHolder;
            ImageView imageView = new ImageView(mContext);
            if (viewType == 0) {
                params = new RecyclerView.LayoutParams(BACK_IMAGE_WIDTH, mBitmapFrameHeight);
            } else {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                params = new RecyclerView.LayoutParams(mBitmapFrameWidth, mBitmapFrameHeight);
            }
            imageView.setLayoutParams(params);
            viewHolder = new HeadFooterEnableAdapter.ViewHolder(imageView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ImageView imageView = (ImageView) holder.itemView;
            if (position == 0) {
                imageView.setImageResource(R.drawable.video_text_back);
                imageView.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (mListener != null) {
                            mListener.onBack();
                        }
                    }
                });
            } else {
                Object item = mDataList.get(position);
                if (item instanceof DecodeVideoThreadPool.VideoTask) {
                    DecodeVideoThreadPool.VideoTask videoTask = (DecodeVideoThreadPool.VideoTask)item;
                    boolean result = FileUtil.isFileExists(videoTask.mImgCachePath);
                    if (result) {
                        Glide.with(mContext).load(videoTask.mImgCachePath).into(imageView);
                    } else {
                        imageView.setImageBitmap(null);
                        if (mRecoverVideoTaskList.indexOf(videoTask) == -1) {
                            mRecoverVideoTaskList.add(videoTask);
                            loadFrame(mRecoverVideoTaskList);
                            mRecoverVideoTaskList.remove(videoTask);
                        }
                    }
                } else {
                    imageView.setImageBitmap(null);
                }
            }
        }

    }



    private ProgressSelectView.ProgressChangeListener mTimeLineListener = new ProgressSelectView.ProgressChangeListener() {
        @Override
        public void onProgressChange(float leftProgress, float rightProgress) {
            mTimeStartText.setText(makeTimeInFormat(leftProgress));
            mTimeEndText.setText(makeTimeInFormat(rightProgress));
        }

        @Override
        public void onProgressConfirm(float leftProgress, float rightProgress) {
            if (mListener != null) {
                mListener.onWaterMarkTimeChange(VideoMediator.transferProgressToVideoFrameTime(leftProgress, mVideoTotalLength), VideoMediator.transferProgressToVideoFrameTime(rightProgress, mVideoTotalLength));
            }
        }

        @Override
        public void onDragHandlePositionChange(float leftPosition, float rightPosition) {
            reLayoutTimeText();
        }
    };

    private String makeTimeInFormat(float progress) {
        long timeMoment = VideoMediator.transferProgressToVideoFrameTime(progress, mVideoTotalLength);
        String timeText = TimeFormatter.toVideoDurationFormat(timeMoment);
        return timeText;
    }

    private void reLayoutTimeText() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mTimeStartText.getLayoutParams();
        params.leftMargin = (int)mSelectView.getLeftHandleLeftPosition();

        params = (FrameLayout.LayoutParams)mTimeEndText.getLayoutParams();
        int position = (int)mSelectView.getRightHandleRightPosition();
        int rightMargin = mScreenLimitWidth - position;
        params.rightMargin = rightMargin;
        mTimeTextContainer.requestLayout();
    }


    public void setWatermarkTimeLineListener(WaterTimeLineListener listener) {
        this.mListener = listener;
    }



    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener =  new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (mMode == TimeLineMode.END) {
                mBitmapListView.getLayoutManager().smoothScrollToPosition(mBitmapListView, null, mBitmapAdapter.getItemCount() - 1);
            } else if (mMode == TimeLineMode.BEGINNING) {
                mBitmapListView.getLayoutManager().smoothScrollToPosition(mBitmapListView, null, 0);
            } else if (mMode == TimeLineMode.WHOLE) {
                int resuslt = (int)(mLeftProgress * mBitmapListWidth) + BACK_IMAGE_WIDTH;
                // 根据传入的秒数滑动到可以展示的位置
                if (resuslt > mScreenLimitWidth) {
                    int scrollOffset = mBitmapListWidth - resuslt > mScreenLimitWidth ? mScreenLimitWidth : mTimeLineViewWidth - mScreenLimitWidth;
                    mBitmapListView.scrollBy(scrollOffset, 0);
                } else {
                    mBitmapListView.getLayoutManager().smoothScrollToPosition(mBitmapListView, null, 0);
                }
            }
            WatermarkTimeLineView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    };

    private CacheManager.CacheManagerCallback mCacheCallback = new CacheManager.CacheManagerCallback() {
        @Override
        public void onSaveImgSuccessfully(String path) {
            if (mPathToIndexMap.get(path) != null) {
                DecodeVideoThreadPool.VideoTask videoTask = mPathToIndexMap.get(path);
                mBitmapFrames.set(videoTask.mLayoutPosition, videoTask);
                mBitmapAdapter.notifyItemChanged(videoTask.mLayoutPosition);
            }
        }
        @Override
        public void failureToLoadImg(String path) {
            Log.i(TAG, "fail to load" + path);
        }
    };

}