package cn.poco.video.timeline2;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.poco.tianutils.ShareData;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by: fwc
 * Date: 2017/11/16
 */
public class TimelineLayout extends FrameLayout {

	private Context mContext;
	private MediaMetadataRetriever mRetriever;

	private ExecutorService mExecutor;
	private List<TimelineTask> mTimelineTasks;
	private List<Future> mFutureList;

	private int mSelectedIndex;
	private TimelineInfo mTimelineInfo;

	private RecyclerView mRecyclerView;
	private LinearLayoutManager mLayoutManager;
	private VideoFrameAdapter mFrameAdapter;

	private ThumbView mThumbView;

	private Handler mHandler;

	/**
	 * 10秒模式剩下的时间
	 */
	static long sTenLeftDuration;

	/**
	 * 3分钟剩下的时间
	 */
	static long s3MLeftDuration;

	private boolean isTenSecondMode = false;

	private OnDragListener mOnDragListener;

	private int mScrollDistance;
	private boolean isStartAutoScroll;

	public TimelineLayout(@NonNull Context context) {
		this(context, null);
	}

	public TimelineLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		init();
	}

	private void init() {

		mHandler = new Handler(Looper.getMainLooper());

		initExecutor();
		initViews();
	}

	private void initExecutor() {

//		int cpuCount = Runtime.getRuntime().availableProcessors();
//		int corePoolSize = Math.max(2, Math.min(cpuCount - 1, 4));
		mExecutor = Executors.newFixedThreadPool(2);
//		mExecutor = Executors.newSingleThreadExecutor();
	}

	public void releaseExecutor() {

		if (!mExecutor.isShutdown()) {
			mExecutor.shutdownNow();
		}
	}

	private void stopAllTask() {
		for (Future future : mFutureList) {
			if (!future.isDone()) {
				future.cancel(true);
			}
		}
		mFutureList.clear();

		for (TimelineTask task : mTimelineTasks) {
			task.release();
		}
		mTimelineTasks.clear();
	}

	private void initViews() {
		LayoutParams params;

		mRecyclerView = new RecyclerView(mContext);
		int thumbWidth = ShareData.PxToDpi_xhdpi(40);
		mRecyclerView.setPadding(thumbWidth, 0, thumbWidth, 0);
		mRecyclerView.setClipToPadding(false);
		mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.setHasFixedSize(true);
		((SimpleItemAnimator)mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
		mRecyclerView.addOnScrollListener(mOnScrollListener);

		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mRecyclerView, params);

		mThumbView = new ThumbView(mContext);
		mThumbView.setOnDragListener(mOnMyDragListener);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mThumbView, params);
	}

	public void setVideoInfos(List<VideoInfo> videoInfos, int index) {

		mThumbView.setTranslationX(0);
		mThumbView.setScrollX(0);
		mRecyclerView.setTranslationX(0);

		mSelectedIndex = index;
		final VideoInfo videoInfo = videoInfos.get(index);

		mRetriever = new MediaMetadataRetriever();
		mRetriever.setDataSource(videoInfo.mPath);

		mTimelineInfo = getTimelineInfo(videoInfo);

		mTimelineTasks = new ArrayList<>(mTimelineInfo.frameCount);
		mFutureList = new ArrayList<>(mTimelineInfo.frameCount);

		long step = (long)((float)mTimelineInfo.duration / mTimelineInfo.frameCount);

		TimelineTask task;
		for (int i = 0; i < mTimelineInfo.frameCount; i++) {
			task = new TimelineTask(mRetriever, videoInfo.mPath, i, i * step);
			task.setOnGetFrameListener(mOnGetFrameListener);
			mTimelineTasks.add(task);
		}

		mFrameAdapter = new VideoFrameAdapter(mContext, mTimelineInfo.frameList);
		mRecyclerView.setAdapter(mFrameAdapter);

		mThumbView.setDuration(mTimelineInfo.duration);
		mThumbView.setTenSecondMode(isTenSecondMode);
		mThumbView.updateProgress(mTimelineInfo.startProgress, mTimelineInfo.endProgress);
		mThumbView.setFrameCount(mTimelineInfo.frameCount);
		mThumbView.updateSize();
		mThumbView.requestLayout();
		mThumbView.initAnimator();

		for (int i = 0; i < mTimelineInfo.frameCount; i++) {
			mFutureList.add(mExecutor.submit(mTimelineTasks.get(i)));
		}

		calculateLeftDuration(videoInfos);

		int totalWidth = mTimelineInfo.frameCount * ShareData.PxToDpi_xhdpi(40);
		if (totalWidth < ShareData.m_screenWidth) {
			float offset = (ShareData.m_screenWidth - totalWidth) / 2f - mRecyclerView.getPaddingLeft();
			mRecyclerView.setTranslationX(offset);
			mThumbView.setTranslationX(offset);
		}
	}

	private void calculateLeftDuration(List<VideoInfo> videoInfos) {
		long totalDuration = 0;
		for (VideoInfo info : videoInfos) {
			totalDuration = totalDuration + (info.GetEndTime() - info.GetStartTime());
		}

		if (isTenSecondMode) {
			sTenLeftDuration = Math.max(0, 10000 - totalDuration);
		}

		s3MLeftDuration = 180000 - totalDuration;
	}

	public void restartVideo() {
		mThumbView.start();
	}

	public void resumeVideo() {
		mThumbView.resume();
	}

	public void pauseVideo() {
		mThumbView.pause();
	}

	public void setOnDragListener(OnDragListener listener) {
		mOnDragListener = listener;
	}

	/**
	 * 设置10秒模式
	 * @param isTenSecondMode true: 开启
	 */
	public void setTenSecondMode(boolean isTenSecondMode) {
		this.isTenSecondMode = isTenSecondMode;
	}

	private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			mThumbView.scrollBy(dx, 0);
			if (isStartAutoScroll) {
				mThumbView.updateProgress(dx);
			}
		}
	};

	private Runnable mScrollRunnable = new Runnable() {

		@Override
		public void run() {
			if (mRecyclerView != null && isStartAutoScroll) {
				mRecyclerView.scrollBy(mScrollDistance, 0);
				ViewCompat.postOnAnimation(mRecyclerView, mScrollRunnable);
			}
		}
	};

	private TimelineTask.OnGetFrameListener mOnGetFrameListener = new TimelineTask.OnGetFrameListener() {

		@Override
		public void onFrameGet(String videoPath, final int index, final String framePath) {

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (mFrameAdapter != null && mTimelineInfo != null) {
						mTimelineInfo.frameList.set(index, framePath);
						mFrameAdapter.notifyItemChanged(index);
					}
				}
			});
		}
	};

	private ThumbView.OnDragListener mOnMyDragListener = new ThumbView.OnDragListener() {
		@Override
		public void onDragLeft(long position, float distance) {
			if (mOnDragListener != null) {
				mOnDragListener.onDragLeft(mSelectedIndex, position);
			}
		}

		@Override
		public void onDragRight(long position, float distance) {
			if (mOnDragListener != null) {
				mOnDragListener.onDragRight(mSelectedIndex, position);
			}
		}

		@Override
		public void onDragOverall(long leftPosition, long rightPosition) {
			if (mOnDragListener != null) {
				mOnDragListener.onDragOverall(mSelectedIndex, leftPosition, rightPosition);
			}
		}

		@Override
		public void onDragStart(int move) {
			if (mOnDragListener != null) {
				mOnDragListener.onDragStart();
			}

			if (move == ThumbView.MOVE_OVERALL && mOnDragListener != null) {
				mOnDragListener.onStartMove();
			}
		}

		@Override
		public void onDragStop(int move) {
			if (mOnDragListener != null) {
				mOnDragListener.onDragStop();
			}

			if (move == ThumbView.MOVE_OVERALL && mOnDragListener != null) {
				mOnDragListener.onStopMove();
			}
		}

		@Override
		public void onStartScroll(int move) {
			if (!isStartAutoScroll) {
				if (move == ThumbView.MOVE_LEFT) {
					mScrollDistance = -16;
				} else if (move == ThumbView.MOVE_RIGHT) {
					mScrollDistance = 16;
				}
				isStartAutoScroll = true;
				ViewCompat.postOnAnimation(mRecyclerView, mScrollRunnable);
			}
		}

		@Override
		public void onStopScroll() {
			if (isStartAutoScroll) {
				mRecyclerView.removeCallbacks(mScrollRunnable);
				isStartAutoScroll = false;
			}
		}

		@Override
		public void onDispatchTouchEvent(MotionEvent event) {
			mRecyclerView.dispatchTouchEvent(event);
		}
	};

	private TimelineInfo getTimelineInfo(VideoInfo videoInfo) {
		TimelineInfo info = new TimelineInfo();
		info.videoPath = videoInfo.mPath;
		info.duration = VideoUtils.getDurationFromVideo2(videoInfo.mPath);
		info.startProgress = videoInfo.GetStartTime() * 1f / info.duration;
		if (videoInfo.GetEndTime() != 0) {
			info.endProgress = videoInfo.GetEndTime() * 1f / info.duration;
		}
		if (info.duration >= 5000) {
			int add = info.duration % 1000 == 0 ? 0 : 1;
			info.frameCount = (int)(info.duration / 1000 + add);
		} else {
			info.frameCount = 5;
		}
		info.frameList = new ArrayList<>(info.frameCount);
		for (int i = 0; i < info.frameCount; i++) {
			info.frameList.add(TimelineTask.EMPTY_PATH);
		}
		return info;
	}

	public void reset() {

		mThumbView.release();
		mHandler.removeCallbacksAndMessages(null);
		mTimelineInfo = null;
//		mRecyclerView.removeOnScrollListener(mOnScrollListener);

		stopAllTask();

		if (mRetriever != null) {
			mRetriever.release();
			mRetriever = null;
		}
	}

	public interface OnDragListener {

		void onDragLeft(int index, long position);

		void onDragRight(int index, long position);

		void onDragOverall(int index, long startPosition, long endPosition);

		void onDragStart();

		void onDragStop();

		void onStartMove();

		void onStopMove();
	}
}
