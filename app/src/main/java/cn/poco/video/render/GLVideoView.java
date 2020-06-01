package cn.poco.video.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.poco.album2.utils.T;
import cn.poco.beautify.WaitDialog1;
import cn.poco.graphics.ShapeEx;
import cn.poco.image.PocoNativeFilter;
import cn.poco.interphoto2.R;
import cn.poco.resource.FilterRes;
import cn.poco.tianutils.ShareData;
import cn.poco.video.decode.DecodeUtils;
import cn.poco.video.render.adjust.AdjustItem;
import cn.poco.video.render.draw.PlayRenderer;
import cn.poco.video.render.filter.FilterItem;
import cn.poco.video.render.gles.GlUtil;
import cn.poco.video.render.player.IMultiPlayer;
import cn.poco.video.render.player.IPlayer;
import cn.poco.video.render.player.MultiPlayer;
import cn.poco.video.render.player.MultiSurface;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.render.transition.UpdateAnimator;
import cn.poco.video.render.view.AutoFitTextureView;
import cn.poco.video.save.SaveParams;
import cn.poco.video.videotext.text.VideoText;

/**
 * Created by: fwc
 * Date: 2017/12/13
 */
public class GLVideoView extends FrameLayout implements IVideoPlayer, IVideoView, MultiSurface.OnFrameAvailableListener {

	private Context mContext;

	private int mState = IPlayer.IDLE;

	private IMultiPlayer mVideoPlayer;
	private MultiSurface mMultiSurface;

	private PlayRenderer mPlayRenderer;

	private List<PlayVideoInfo> mOriginInfos = new ArrayList<>();
	private List<PlayVideoInfo> mVideoInfos = new ArrayList<>();
	private @PlayRatio
	int mPlayRatio = 0;
	private int[] mTransitionIds;

	private boolean isSingleVideo;

	/**
	 * 视频显示的比例，宽/高
	 */
	private float mShowRatio;

	private AutoFitTextureView mGLTextureView;

	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private int mSurfaceTop;
	private int mSurfaceLeft;

	private float mTopRatio;
	private float mLeftRatio;

	private View mBlackView1;
	private View mBlackView2;

	/**
	 * 视频播放总时长
	 */
	private long mTotalDuration;

	private UpdateAnimator mUpdateAnimator;

	private UpdateAnimator mItemAnimator;

	private Handler mHandler;

	/**
	 * 自动播放
	 */
	private boolean isAutoPlay = true;

	/**
	 * 循环播放
	 */
	private boolean isLooping = true;

	/**
	 * 视频播放音量
	 */
	private float mVolume = 1f;

	private ListenerHelper mListenerHelper;

	/**
	 * 上一次暂停的进度
	 */
	private float mLastProgress;

	private SaveParams mSaveParams = new SaveParams();

	/**
	 * 进入单视频播放模式
	 */
	private boolean isEnterSingleVideoPlay;
	private int mEnterVideoIndex = -1;
	private boolean hasChangeVideo; // 是否更换了播放的视频

	/**
	 * 转场模式
	 */
	private boolean isTransitionMode;
	private int mTransitionIndex = -1;
	private long mStartPosition;
	private long mTransitionDuration;
	private boolean hasChanged = false; // 用于解决转场期间第二个视频有第一个视频的滤镜等
	private boolean mPostReset = false;

	/**
	 * 标记是否删除了视频
	 */
	private boolean isDeleteVideo;

	private float[] mLastMvpMatrix = new float[16];

	private WaitDialog1 mWaitDialog;

	public GLVideoView(Context context) {
		super(context);

		mContext = context;

		init();
	}

	private void init() {

		mHandler = new Handler(Looper.getMainLooper());
		mListenerHelper = new ListenerHelper();

		initPlayer(mContext);
		initPlayRenderer(mContext);
		initViews();
		initAnimator();
	}

	private void initPlayer(Context context) {
		mVideoPlayer = new MultiPlayer(context);
		mVideoPlayer.setLooping(true);
		mVideoPlayer.setOnMultiPlayListener(mOnMultiPlayListener);
	}

	private void initPlayRenderer(Context context) {
		mPlayRenderer = new PlayRenderer(context, mOnRenderListener);
	}

	private void initViews() {
		LayoutParams params;

		mGLTextureView = new AutoFitTextureView(mContext);
		mGLTextureView.setRenderer(mPlayRenderer);

		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		addView(mGLTextureView, params);

		mWaitDialog = new WaitDialog1(mContext, R.style.waitDialog);
		mWaitDialog.setMessage(getResources().getString(R.string.processing));
	}

	private void initAnimator() {
		mUpdateAnimator = new UpdateAnimator(null);
		mUpdateAnimator.setOnUpdateListener(mOnUpdateListener);

		mItemAnimator = new UpdateAnimator(null);
		mItemAnimator.setOnUpdateListener(mOnItemUpdateListener);
	}

	@Override
	public void setVideoPaths(String... paths) {
		if (mState != IPlayer.IDLE) {
			throw new IllegalStateException();
		}

		if (paths == null || paths.length == 0) {
			throw new IllegalArgumentException("the paths are empty.");
		}

		if (mPlayRatio == 0) {
			throw new RuntimeException("please call setPlayRatio() before.");
		}

		mVideoInfos.clear();
		for (String path : paths) {
			mVideoInfos.add(DecodeUtils.getPlayVideoInfo(path));
		}

		calculateVideoMatrix();

		isSingleVideo = mVideoInfos.size() == 1;
		if (!isSingleVideo) {
			mTransitionIds = new int[mVideoInfos.size() - 1];
			Arrays.fill(mTransitionIds, TransitionItem.NONE);
		}

		mTotalDuration = calculateDuration(mVideoInfos.size());
	}

	@Override
	public void addVideos(String... paths) {

		if (paths == null || paths.length == 0) {
			return;
		}

		PlayVideoInfo info;
		List<PlayVideoInfo> temp = new ArrayList<>();
		for (String path : paths) {
			info = DecodeUtils.getPlayVideoInfo(path);
			if (info != null) {
				temp.add(info);
				calculateVideoMatrix(info);
			}
		}

		if (temp.isEmpty()) {
			T.showShort(mContext, "添加视频失败");
			return;
		}

		mVideoInfos.addAll(temp);
		temp.clear();

		isSingleVideo = false;

		int[] transitionIds = new int[mVideoInfos.size() - 1];
		Arrays.fill(transitionIds, TransitionItem.NONE);

		if (mTransitionIds != null) {
			System.arraycopy(mTransitionIds, 0, transitionIds, 0, mTransitionIds.length);
		}

		mTransitionIds = transitionIds;
		mTotalDuration = calculateDuration(mVideoInfos.size());
	}

	@Override
	@SuppressWarnings("all")
	public void deleteVideo(int index) {

		if (index < 0) {
			index = 0;
		} else if (index >= mOriginInfos.size()) {
			index = mOriginInfos.size() - 1;
		}

		mOriginInfos.remove(index);
		isSingleVideo = mOriginInfos.size() == 1;

		if (isSingleVideo) {
			mTransitionIds = null;
		} else {
			int[] transitionIds = new int[mTransitionIds.length - 1];
			for (int i = 0; i < index && i < transitionIds.length; i++) {
				transitionIds[i] = mTransitionIds[i];
			}
			for (int i = index; i < transitionIds.length; i++) {
				transitionIds[i] = mTransitionIds[i + 1];
			}
			mTransitionIds = transitionIds;
		}

		isDeleteVideo = true;

		mTotalDuration = calculateDuration(mVideoInfos.size());
		mPlayRenderer.deleteVideo(index);
	}

	@Override
	public void copyVideo(int index, String path) {
		//在单个视频播放模式下复制 isEnterSingleVideoPlay
		if (index < 0) {
			index = 0;
		} else if (index >= mOriginInfos.size()) {
			index = mOriginInfos.size() - 1;
		}

		PlayVideoInfo info = mOriginInfos.get(index);
		PlayVideoInfo newInfo = info.Clone();
		newInfo.path = path;
		mOriginInfos.add(index + 1, newInfo);
		mPlayRenderer.copyVideo(index);

		isSingleVideo = false;

		if (mTransitionIds == null) {
			mTransitionIds = new int[1];
			mTransitionIds[0] = TransitionItem.NONE;
		} else {
			int[] transitionIds = new int[mTransitionIds.length + 1];
			if (mEnterVideoIndex == 0) {
				transitionIds[0] = TransitionItem.NONE;
				System.arraycopy(mTransitionIds, 0, transitionIds, 1, mTransitionIds.length);
			} else {
				System.arraycopy(mTransitionIds, 0, transitionIds, 0, mEnterVideoIndex);
				transitionIds[mEnterVideoIndex] = TransitionItem.NONE;
				if (mTransitionIds.length > mEnterVideoIndex) {
					System.arraycopy(mTransitionIds, mEnterVideoIndex, transitionIds, mEnterVideoIndex + 1, mTransitionIds.length - mEnterVideoIndex);
				}
			}

			mTransitionIds = transitionIds;
		}

		mTotalDuration = calculateDuration(mVideoInfos.size());
	}

	@Override
	@SuppressWarnings("all")
	public void setPlayRatio(int playRatio) {
		if (mState != IPlayer.IDLE) {
			throw new IllegalStateException();
		}

		if (mPlayRatio != playRatio) {
			mPlayRatio = playRatio;

			switch (mPlayRatio) {
				case PlayRatio.RATIO_1_1:
					mShowRatio = 1;
					mSurfaceWidth = mSurfaceHeight = ShareData.m_screenWidth;
					mSurfaceLeft = mSurfaceTop = 0;
					break;
				case PlayRatio.RATIO_9_16:
					mShowRatio = 9f / 16;
					mSurfaceHeight = ShareData.m_screenWidth;
					mSurfaceWidth = (int)(mSurfaceHeight * mShowRatio + 0.5f);
					mSurfaceTop = 0;
					mSurfaceLeft = (ShareData.m_screenWidth - mSurfaceWidth) / 2 + 2;
					break;
				case PlayRatio.RATIO_16_9:
					mShowRatio = 16f / 9;
					mSurfaceWidth = ShareData.m_screenWidth;
					mSurfaceHeight = (int)(mSurfaceWidth / mShowRatio + 0.5f);
					mSurfaceLeft = 0;
					mSurfaceTop = (ShareData.m_screenWidth - mSurfaceHeight) / 2 + 2;
					break;
				case PlayRatio.RATIO_235_1:
					mShowRatio = 2.35f;
					mSurfaceWidth = ShareData.m_screenWidth;
					mSurfaceHeight = (int)(mSurfaceWidth / mShowRatio + 0.5f);
					mSurfaceLeft = 0;
					mSurfaceTop = (ShareData.m_screenWidth - mSurfaceHeight) / 2 + 2;
					break;
			}

			if (mSurfaceTop != 0) {
				mBlackView1 = new View(mContext);
				mBlackView1.setBackgroundColor(Color.BLACK);
				LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mSurfaceTop);
				addView(mBlackView1, params);

				mBlackView2 = new View(mContext);
				mBlackView2.setBackgroundColor(Color.BLACK);
				params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mSurfaceTop);
				params.gravity = Gravity.BOTTOM;
				addView(mBlackView2, params);
			} else if (mSurfaceLeft != 0) {
				mBlackView1 = new View(mContext);
				mBlackView1.setBackgroundColor(Color.BLACK);
				LayoutParams params = new LayoutParams(mSurfaceLeft, ViewGroup.LayoutParams.MATCH_PARENT);
				addView(mBlackView1, params);

				mBlackView2 = new View(mContext);
				mBlackView2.setBackgroundColor(Color.BLACK);
				params = new LayoutParams(mSurfaceLeft, ViewGroup.LayoutParams.MATCH_PARENT);
				params.gravity = Gravity.END;
				addView(mBlackView2, params);
			}

			int halfSize = ShareData.m_screenWidth / 2;
			mTopRatio = (halfSize - mSurfaceTop) / (float)halfSize;
			mLeftRatio = (mSurfaceLeft - halfSize) / (float)halfSize;
		}
	}

	private void calculateVideoMatrix() {
		float videoRatio;
		for (PlayVideoInfo info : mVideoInfos) {
			videoRatio = info.getVideoRatio();
			if (videoRatio >= mShowRatio) {
				if (mShowRatio > 1) {
					info.initMvpMatrix(videoRatio / mShowRatio, 1 / mShowRatio);
				} else {
					info.initMvpMatrix(videoRatio, 1);
				}
			} else {
				if (mShowRatio > 1) {
					info.initMvpMatrix(1, 1 / videoRatio);
				} else {
					info.initMvpMatrix(mShowRatio, mShowRatio / videoRatio);
				}
			}
		}
	}

	private void calculateVideoMatrix(PlayVideoInfo info) {
		float videoRatio = info.getVideoRatio();
		if (videoRatio >= mShowRatio) {
			if (mShowRatio > 1) {
				info.initMvpMatrix(videoRatio / mShowRatio, 1 / mShowRatio);
			} else {
				info.initMvpMatrix(videoRatio, 1);
			}
		} else {
			if (mShowRatio > 1) {
				info.initMvpMatrix(1, 1 / videoRatio);
			} else {
				info.initMvpMatrix(mShowRatio, mShowRatio / videoRatio);
			}
		}
	}

	/**
	 * 计算从0到endIndex（不包括）的视频总时长
	 *
	 * @param endIndex 视频下标
	 * @return 视频时长
	 */
	private long calculateDuration(int endIndex) {
		long result = 0;
		for (int i = 0; i < endIndex; i++) {
			result += mVideoInfos.get(i).data.duration;
		}

		if (mTransitionIds != null) {
			for (int i = 0; i < endIndex - 1; i++) {
				if (TransitionItem.isBlendTransition(mTransitionIds[i])) {
					result -= TransitionItem.DEFAULT_TIME;
				}
			}
		}

		return result;
	}

	@Override
	public void prepare() {
		if (mState != IPlayer.IDLE) {
			throw new IllegalStateException();
		}

		if (mVideoInfos.isEmpty()) {
			throw new RuntimeException("please call setVideoPaths() before.");
		}

		if (mMultiSurface == null) {
			throw new RuntimeException("the mMultiSurface is null.");
		}

		String[] videoPaths = new String[mVideoInfos.size()];
		for (int i = 0; i < videoPaths.length; i++) {
			videoPaths[i] = mVideoInfos.get(i).path;
		}
		mVideoPlayer.setDataSources(videoPaths);
		mVideoPlayer.setSurface(mMultiSurface);
		mVideoPlayer.prepare();
		mVideoPlayer.post(new Runnable() {
			@Override
			public void run() {
				if (mTransitionIds != null) {
					mPlayRenderer.changeTransition(mTransitionIds[0]);
				}
			}
		});

		mState = IPlayer.PREPARED;
		mLastProgress = 0;

		if (isAutoPlay) {
			start();
		}
	}

	@Override
	public void start() {

		if (mState == IPlayer.PAUSE) {
			resume();
			return;
		}

		if (mState != IPlayer.PREPARED) {
			throw new IllegalStateException();
		}

		mVideoPlayer.start();
		mState = IPlayer.START;
	}

	@Override
	public void enterSingleVideoPlay(int index) {

		if (isEnterSingleVideoPlay) {
			return;
		}

		index = checkIndex(index);

		if (isSingleVideo) {
			restart();
			mEnterVideoIndex = 0;
			isEnterSingleVideoPlay = true;
			mOriginInfos.addAll(mVideoInfos);
			return;
		}

		if (mState != IPlayer.START && mState != IPlayer.PAUSE) {
			return;
		}

		isEnterSingleVideoPlay = isSingleVideo = true;
		mEnterVideoIndex = index;

		reset();

		mOriginInfos.clear();

		PlayVideoInfo info = mVideoInfos.get(index);
		mOriginInfos.addAll(mVideoInfos);
		mVideoInfos.clear();
		mVideoInfos.add(info);

		if (mMultiSurface.getCurrentSurface() == null) {
			return;
		}

		mVideoPlayer.setDataSources(info.path);
		mVideoPlayer.setSurface(mMultiSurface);
		mVideoPlayer.setCurrentIndex(0);
		mVideoPlayer.prepare();

		mPlayRenderer.changeTransition(TransitionItem.NONE);
		mTotalDuration = info.data.duration;

		mVideoPlayer.start();
		mState = IPlayer.START;
	}

	@Override
	public void exitSingleVideoPlay() {
		if (isEnterSingleVideoPlay) {
			isEnterSingleVideoPlay = false;
			mEnterVideoIndex = -1;
			mVideoInfos.clear();
			mVideoInfos.addAll(mOriginInfos);
			mOriginInfos.clear();

			isSingleVideo = mVideoInfos.size() == 1;
			mTotalDuration = calculateDuration(mVideoInfos.size());
		}
	}

	private void resume() {
		if (mState == IPlayer.PAUSE) {

			if (mLastProgress == 1) {
				restart();
			} else {
				final float startProgress = mLastProgress;
				long duration = (long)(mTotalDuration * (1 - startProgress));
				mVideoPlayer.start();
				mUpdateAnimator.start(startProgress, duration);

				long position = mVideoPlayer.getCurrentPlayPosition();
				long itemDuration = mVideoInfos.get(mVideoPlayer.getCurrentIndex()).data.duration;
				float itemProgress = position / (float)itemDuration;
				duration = (long)(itemDuration * (1 - itemProgress));
				mItemAnimator.start(itemProgress, duration);

				mState = IPlayer.START;

				mPlayRenderer.onVideoResume();
				mListenerHelper.onVideoResume();
			}
		}
	}

	@Override
	public void pause() {
		if (mState == IPlayer.START) {
			mUpdateAnimator.reset();
			mItemAnimator.reset();
			mVideoPlayer.pause();

			mState = IPlayer.PAUSE;

			mPlayRenderer.onVideoPause();
			mListenerHelper.onVideoPause();
		}
	}

	@Override
	public boolean isPlaying() {
		return mState == IPlayer.START;
	}

	@Override
	public void seekTo(long position) {
		final long[] data = getIndexAndPosition(position);
		seekTo((int)data[0], data[1]);
	}

	@Override
	public void seekTo(final int index, final long position) {
		mVideoPlayer.seekTo(index, position);
	}

	@Override
	public void forceSeekTo(int index, long position) {
		mVideoPlayer.forceSeekTo(index, position);
	}

	private void notifyProgressChanged(int index, long position, boolean isSeekTo) {
		long time = calculateDuration(index);
		time += position;
		mLastProgress = checkRange(time * 1f / mTotalDuration, 0, 1);
		mListenerHelper.onProgressChanged(mLastProgress, isSeekTo);

		mListenerHelper.onItemProgressChanged(index, position / (float)mVideoInfos.get(index).data.duration, isSeekTo);
	}

	private long[] getIndexAndPosition(long timestamp) {

		long[] result = new long[2];

		if (isSingleVideo) {
			result[0] = 0;
			result[1] = timestamp;
		} else {
			int index = 0;
			for (PlayVideoInfo info : mVideoInfos) {
				if (info.data.duration <= timestamp) {

					if (index > 0 && TransitionItem.isBlendTransition(mTransitionIds[index - 1])) {
						timestamp += TransitionItem.DEFAULT_TIME;
					}
					index++;
					timestamp -= info.data.duration;
				} else {
					result[0] = index;
					result[1] = timestamp;
					break;
				}
			}
		}

		return result;
	}

	@Override
	public void onResume() {
//		float startProgress = mProgressView.getProgress();
//		long duration = (long)(mTotalDuration * (1 - startProgress));
//		mVideoPlayer.start();
//		mUpdateAnimator.start(startProgress, duration);
//		mPlayRenderer.onVideoResume();
	}

	@Override
	public void onPause() {
		mVideoPlayer.pause();
		mUpdateAnimator.reset();
		mItemAnimator.reset();

		mPlayRenderer.onVideoPause();

		mState = IPlayer.PAUSE;
		mListenerHelper.onVideoPause();
	}

	@Override
	public void reset() {
		mPlayRenderer.changeTransition(TransitionItem.NONE);
		mUpdateAnimator.reset();
		mItemAnimator.reset();
		mVideoPlayer.pause();
		mVideoPlayer.reset();
		mLastProgress = 0;
		isDeleteVideo = false;
		mState = IPlayer.IDLE;
	}

	@Override
	public void release() {

		resetListener();
		mHandler.removeCallbacksAndMessages(null);

		mPlayRenderer.release();
		mUpdateAnimator.reset();
		mItemAnimator.reset();
		mVideoPlayer.release();
		mListenerHelper.clear();

		mVideoInfos.clear();

		mState = IPlayer.RELEASE;
	}

	private void resetListener() {
		mVideoPlayer.setOnMultiPlayListener(null);
		mUpdateAnimator.setOnUpdateListener(null);
		mItemAnimator.setOnUpdateListener(null);
	}

	@Override
	public void setLooping(boolean looping) {
		if (isLooping != looping) {
			isLooping = looping;

			mVideoPlayer.setLooping(isLooping);
		}
	}

	@Override
	public void setAutoPlay(boolean isAutoPlay) {
		if (mState != IPlayer.IDLE) {
			throw new IllegalStateException();
		}

		this.isAutoPlay = isAutoPlay;
	}

	@Override
	public void setVolume(@FloatRange(from = 0, to = 1) float volume) {
		if (mVolume != volume) {
			mVolume = volume;
			mVideoPlayer.setVolume(volume);
		}
	}

	@Override
	public float getVolume() {
		return mVolume;
	}

	@Override
	public void openVolume() {
		if (isEnterSingleVideoPlay) {
			PlayVideoInfo info = mVideoInfos.get(0);
			info.isMute = false;
			mVideoPlayer.setVolume(mVolume);
		}
	}

	@Override
	public void closeVolume() {
		if (isEnterSingleVideoPlay) {
			PlayVideoInfo info = mVideoInfos.get(0);
			info.isMute = true;
			mVideoPlayer.setVolume(0);
		}
	}

	@Override
	public void setTransition(int index, int transitionId) {

		if (mTransitionIds == null) {
			throw new IllegalStateException();
		}
		if (index < 0 || index >= mTransitionIds.length) {
			throw new IllegalArgumentException();
		}

		mHandler.removeCallbacks(mResetChangedRunnable);
		hasChanged = false;

		if (index != mTransitionIndex) {
			isTransitionMode = false;
		}

		if (!isTransitionMode) {
			isTransitionMode = true;
			mTransitionIndex = index;
			mTransitionIds[index] = transitionId;

			reset();

			PlayVideoInfo info1 = mVideoInfos.get(index);
			PlayVideoInfo info2 = mVideoInfos.get(index + 1);
			mOriginInfos.addAll(mVideoInfos);
			mVideoInfos.clear();
			mVideoInfos.add(info1);
			mVideoInfos.add(info2);

			mVideoPlayer.setDataSources(info1.path, info2.path);
			mVideoPlayer.setSurface(mMultiSurface);
			mVideoPlayer.prepare();

			setTransitionInner(transitionId, info1, info2);
		} else if (mTransitionIds[index] != transitionId) {
			mVideoPlayer.pause();

			mTransitionIds[index] = transitionId;
			PlayVideoInfo info1 = mVideoInfos.get(0);
			PlayVideoInfo info2 = mVideoInfos.get(1);
			setTransitionInner(transitionId, info1, info2);
		}
	}

	private void setTransitionInner(int transitionId, PlayVideoInfo info1, PlayVideoInfo info2) {
		mPlayRenderer.changeTransition(transitionId);
		mTotalDuration = info1.data.duration + info2.data.duration;
		mStartPosition = info1.data.duration - TransitionItem.DEFAULT_TIME;
		mTransitionDuration = TransitionItem.DEFAULT_TIME * 2;
		if (TransitionItem.isBlendTransition(transitionId)) {
			mStartPosition -= TransitionItem.DEFAULT_TIME / 2;
			mTransitionDuration += TransitionItem.DEFAULT_TIME;
			mTotalDuration -= TransitionItem.DEFAULT_TIME;
		}

		mVideoPlayer.forceSeekTo(0, mStartPosition);
		mVideoPlayer.start();
		mState = IPlayer.START;
	}

	@Override
	public void setTransitions(int... transitionIds) {
		mHandler.removeCallbacks(mResetChangedRunnable);
		hasChanged = false;

		if (transitionIds != null) {
			if (mTransitionIds == null) {
				mTransitionIds = new int[transitionIds.length];
			}

			System.arraycopy(transitionIds, 0, mTransitionIds, 0, mTransitionIds.length);

			exitTransition(false);
		}
	}

	@Override
	public void exitTransition() {
		mHandler.removeCallbacks(mResetChangedRunnable);
		hasChanged = false;
		exitTransition(true);
	}

	@Override
	public long checkTransition() {

		if (mTransitionIds == null || !isEnterSingleVideoPlay) {
			return 0;
		}

		long result = 0;
		if (mEnterVideoIndex == 0) {
			if (TransitionItem.isBlendTransition(mTransitionIds[0])) {
				result = 1000;
			}
		} else if (mEnterVideoIndex >= mTransitionIds.length) {
			if (TransitionItem.isBlendTransition(mTransitionIds[mEnterVideoIndex - 1])) {
				result = 1000;
			}
		} else {
			int id1 = mTransitionIds[mEnterVideoIndex - 1];
			int id2 = mTransitionIds[mEnterVideoIndex];
			if (TransitionItem.isBlendTransition(id1) || TransitionItem.isBlendTransition(id2)) {
				result = 1000;
			}
		}

		return result;
	}

	private void exitTransition(boolean shouldRestart) {

		if (isTransitionMode) {

			isTransitionMode = false;
			mTransitionIndex = -1;

			mVideoInfos.clear();
			mVideoInfos.addAll(mOriginInfos);
			mOriginInfos.clear();

			mTotalDuration = calculateDuration(mVideoInfos.size());

			if (shouldRestart) {
				restart();
			}
		}
	}

	@Override
	public void changeVideoOrder(int fromPosition, int toPosition, final int index, final float progress) {
		fromPosition = checkIndex(fromPosition);
		toPosition = checkIndex(toPosition);

		if (fromPosition == toPosition) {
			return;
		}

		pause();

		mWaitDialog.show();

		final int finalFromPosition = fromPosition;
		final int finalToPosition = toPosition;

		mVideoPlayer.post(new Runnable() {
			@Override
			public void run() {
				PlayVideoInfo info = mVideoInfos.remove(finalFromPosition);
				mVideoInfos.add(finalToPosition, info);

				int[] transitions = new int[mTransitionIds.length];

				if (finalToPosition >= mTransitionIds.length) {
					if (finalFromPosition > 0) {
						System.arraycopy(mTransitionIds, 0, transitions, 0, finalFromPosition);
					}
					if (finalFromPosition + 1 < mTransitionIds.length) {
						System.arraycopy(mTransitionIds, finalFromPosition + 1, transitions, finalFromPosition, mTransitionIds.length - (finalFromPosition + 1));
					}
					transitions[transitions.length - 1] = TransitionItem.NONE;
					mTransitionIds = transitions;
				} else if (finalFromPosition >= mTransitionIds.length) {
					if (finalToPosition > 0) {
						System.arraycopy(mTransitionIds, 0, transitions, 0, finalToPosition);
					}
					if (finalFromPosition > finalToPosition + 1) {
						System.arraycopy(mTransitionIds, finalToPosition, transitions, finalToPosition + 1, finalFromPosition - (finalToPosition + 1));
					}
					transitions[finalToPosition] = TransitionItem.NONE;
					mTransitionIds = transitions;
				} else {
					int tempId = mTransitionIds[finalFromPosition];
					mTransitionIds[finalFromPosition] = mTransitionIds[finalToPosition];
					mTransitionIds[finalToPosition] = tempId;
				}
				mPlayRenderer.changeVideoOrder(finalFromPosition, finalToPosition);
				String[] videoPaths = new String[mVideoInfos.size()];
				for (int i = 0; i < videoPaths.length; i++) {
					videoPaths[i] = mVideoInfos.get(i).path;
				}
				final long position = (long)(mVideoInfos.get(index).data.duration * progress);
				mVideoPlayer.changeVideoOrder(index, position, videoPaths);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mWaitDialog.dismiss();
					}
				});
			}
		});

	}

	@Override
	public void restart() {

		if (mMultiSurface.getCurrentSurface() == null) {
			return;
		}

		if (isSingleVideo && !isDeleteVideo) {

			if (hasChangeVideo) {
				hasChangeVideo = false;
				return;
			}

			mVideoPlayer.forceSeekTo(0, 0);
			mLastProgress = 0;
			mUpdateAnimator.start(0, mTotalDuration);
			mItemAnimator.start(0, mVideoInfos.get(0).data.duration);
			mVideoPlayer.start();

			mState = IPlayer.START;

			mPlayRenderer.onVideoResume();
//			mListenerHelper.onVideoResume();
			mListenerHelper.onVideoStart();
		} else {
			mVideoPlayer.pause();
			mPlayRenderer.onVideoSeekTo(0, 0);
			reset();
			prepare();
		}
	}

	@Override
	public long getTotalDuration() {
		return mTotalDuration;
	}

	@Override
	public long getBlendTime(int endIndex) {

		long blendTime = 0;
		if (mTransitionIds != null) {

			if (endIndex > mTransitionIds.length) {
				endIndex = mTransitionIds.length;
			}
			for (int i = 0; i < endIndex; i++) {
				if (TransitionItem.isBlendTransition(mTransitionIds[i])) {
					blendTime += TransitionItem.DEFAULT_TIME;
				}
			}
		}
		return blendTime;
	}

	@Override
	public long getCurrentPosition() {
		return (long)(mTotalDuration * mLastProgress);
	}

	@Override
	public void changeVideoPath(String videoPath) {
		if (isEnterSingleVideoPlay) {

			boolean isPause = mState == IPlayer.PAUSE;

			reset();

			PlayVideoInfo info = mVideoInfos.get(0);
			info = info.changeVideoPath(videoPath);

			mVideoInfos.clear();
			mVideoInfos.add(info);

			mOriginInfos.set(mEnterVideoIndex, info);

			mVideoPlayer.setDataSources(info.path);
			mVideoPlayer.setSurface(mMultiSurface);
			mVideoPlayer.prepare();

			mTotalDuration = info.data.duration;

			mLastProgress = 0;

			hasChangeVideo = true;

			if (isPause) {
				mState = IPlayer.PREPARED;
			} else {
				mVideoPlayer.start();
				mState = IPlayer.START;
			}

		}
	}

	@Override
	public void splitVideoPath(String videoPath1, String videoPath2) {
		if (isEnterSingleVideoPlay) {
			reset();

			PlayVideoInfo info = mVideoInfos.get(0);
			info = info.changeVideoPath(videoPath1);

			mOriginInfos.set(mEnterVideoIndex, info);
			info = info.changeVideoPath(videoPath2);
			mOriginInfos.add(mEnterVideoIndex + 1, info);
			mPlayRenderer.copyVideo(mEnterVideoIndex);

			if (mTransitionIds == null) {
				mTransitionIds = new int[1];
				mTransitionIds[0] = TransitionItem.NONE;
			} else {
				int[] transitionIds = new int[mTransitionIds.length + 1];
				if (mEnterVideoIndex == 0) {
					transitionIds[0] = TransitionItem.NONE;
					System.arraycopy(mTransitionIds, 0, transitionIds, 1, mTransitionIds.length);
				} else {
					System.arraycopy(mTransitionIds, 0, transitionIds, 0, mEnterVideoIndex);
					transitionIds[mEnterVideoIndex] = TransitionItem.NONE;
					if (mTransitionIds.length > mEnterVideoIndex) {
						System.arraycopy(mTransitionIds, mEnterVideoIndex, transitionIds, mEnterVideoIndex + 1, mTransitionIds.length - mEnterVideoIndex);
					}
				}

				mTransitionIds = transitionIds;
			}

			exitSingleVideoPlay();
		}
	}

	@Override
	public void changeFilter(FilterRes filterRes) {

		for (int i = 0; i < mVideoInfos.size(); i++) {
			changeFilter(i, filterRes);
		}
	}

	@Override
	public void changeFilterAlpha(float alpha) {

		for (int i = 0; i < mVideoInfos.size(); i++) {
			changeFilterAlpha(i, alpha);
		}
	}

	@Override
	public void changeFilter(int index, FilterRes filterRes) {
		FilterItem item = FilterItem.wrap(mContext, filterRes, 0);
		mPlayRenderer.changeFilter(index, item);
		shouldRequestRender();
	}

	@Override
	public void changeFilterAlpha(int index, float alpha) {
		mPlayRenderer.changeFilterAlpha(index, alpha);
		shouldRequestRender();
	}

	@Override
	public void doCurve(int curveType, List<Point> controlPoints) {
		for (int i = 0; i < mVideoInfos.size(); i++) {
			doCurve(i, curveType, controlPoints);
		}
	}

	@Override
	public void doCurve(int index, int curveType, List<Point> controlPoints) {
		PlayVideoInfo info;
		if (isEnterSingleVideoPlay) {
			info = mVideoInfos.get(0);
		} else {
			info = mVideoInfos.get(index);
		}

		if (controlPoints == null) {
			info.curve.reset(curveType);
			mPlayRenderer.changeCurve(index, info.curve.mDatas);
			return;
		}

		if (controlPoints.size() < 2) {
			throw new IllegalArgumentException();
		}

		int[] result = new int[256];

		int count = controlPoints.size();
		int[] ctrPoints = new int[count * 2];
		Point point;
		for (int i = 0; i < count; i++) {
			point = controlPoints.get(i);
			ctrPoints[i * 2] = point.x;
			ctrPoints[i * 2 + 1] = point.y;
		}
		PocoNativeFilter.CreateCurves(result, 256, ctrPoints, count);

		info.curve.doCurve(curveType, result);
		mPlayRenderer.changeCurve(index, info.curve.mDatas);

		shouldRequestRender();
	}

	@Override
	public void addAdjust(AdjustItem item) {
		for (int i = 0; i < mVideoInfos.size(); i++) {
			addAdjust(i, item);
		}
	}

	@Override
	public void addAdjust(int index, AdjustItem item) {
		mPlayRenderer.addAdjust(index, item);
		shouldRequestRender();
	}

	@Override
	public void setVideoText(int textId, VideoText videoText, ShapeEx shapeEx, int startTime, int stayTime) {
		if (textId == 0) {
			mSaveParams.textId = "0000";
		} else {
			mSaveParams.textId = String.valueOf(textId);
		}
		mSaveParams.videoText = videoText;
		mSaveParams.shapeEx = shapeEx;
		mSaveParams.startTime = startTime;
		mSaveParams.stayTime = stayTime;
	}

	@Nullable
	@Override
	public VideoText getVideoText() {
		return mSaveParams.videoText;
	}

	@Override
	public void setMusicPath(int musicId, String musicPath, int musicStart, float musicVolume) {
		if (musicId == 0) {
			mSaveParams.musicId = "0000";
		} else {
			mSaveParams.musicId = String.valueOf(musicId);
		}
		mSaveParams.musicPath = musicPath;
		mSaveParams.musicStart = musicStart;
		mSaveParams.musicVolume = musicVolume;
	}

	@Override
	public SaveParams getOutputParams() {
		mSaveParams.videoInfos = new ArrayList<>();
		for (PlayVideoInfo info : mVideoInfos) {
			info.calculateSaveMatrix(mLeftRatio, mTopRatio);
			mSaveParams.videoInfos.add(info);
		}

		if (mTransitionIds != null) {
			mSaveParams.transitions = new int[mTransitionIds.length];
			System.arraycopy(mTransitionIds, 0, mSaveParams.transitions, 0, mTransitionIds.length);
		}

		mSaveParams.filterParamArray = mPlayRenderer.getFilterParamArray();
		mSaveParams.adjustInfoArray = mPlayRenderer.getAdjustInfoArray();
		mSaveParams.curveArray = mPlayRenderer.getCurveArray();
		mSaveParams.videoVolume = mVolume;

		mSaveParams.playRatio = mPlayRatio;
		mSaveParams.duration = mTotalDuration;

		return mSaveParams;
	}

	@Override
	public void addOnPlayListener(OnPlayListener listener) {
		mListenerHelper.addOnPlayListener(listener);
	}

	@Override
	public void removeOnPlayListener(OnPlayListener listener) {
		mListenerHelper.removeOnPlayListener(listener);
	}

	@Override
	public void addOnProgressListener(OnProgressListener listener) {
		mListenerHelper.addOnProgressListener(listener);
	}

	@Override
	public void removeOnProgressListener(OnProgressListener listener) {
		mListenerHelper.removeOnProgressListener(listener);
	}

	@Override
	public void addOnItemProgressListener(OnItemProgressListener listener) {
		mListenerHelper.addOnItemProgressListener(listener);
	}

	@Override
	public void removeOnItemProgressListener(OnItemProgressListener listener) {
		mListenerHelper.removeOnItemProgressListener(listener);
	}

	private int checkIndex(int index) {
//		if (index < 0 || index >= mVideoInfos.size()) {
//			throw new IllegalArgumentException("index: " + index);
//		}
		if (index < 0) {
			return 0;
		} else if (index >= mVideoInfos.size()) {
			return mVideoInfos.size() - 1;
		}

		return index;
	}

	@SuppressWarnings("all")
	private float checkRange(float value, float min, float max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		}

		return value;
	}

	// ---------------------- IVideoView ---------------------------

	@Override
	public Bitmap getFrame() {
		Bitmap bitmap = mGLTextureView.getBitmap();
		Bitmap frame = Bitmap.createBitmap(mSurfaceWidth, mSurfaceHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(frame);
		canvas.drawBitmap(bitmap, -mSurfaceLeft, -mSurfaceTop, null);
		return frame;
	}

	@Override
	public int getSurfaceWidth() {
		return mSurfaceWidth;
	}

	@Override
	public int getSurfaceHeight() {
		return mSurfaceHeight;
	}

	@Override
	public int getSurfaceLeft() {
		return mSurfaceLeft;
	}

	@Override
	public int getSurfaceTop() {
		return mSurfaceTop;
	}

	@Override
	public void enterFrameAdjust() {
		PlayVideoInfo info = mVideoInfos.get(0);
		info.enterFrameAdjust();
		if (mBlackView1 != null) {
			mBlackView1.setAlpha(0.6f);
		}
		if (mBlackView2 != null) {
			mBlackView2.setAlpha(0.6f);
		}
	}

	@Override
	public void exitFrameAdjust() {
		if (mBlackView1 != null) {
			mBlackView1.setAlpha(1f);
		}
		if (mBlackView2 != null) {
			mBlackView2.setAlpha(1f);
		}
	}

	@Override
	public void translateFrame(float dx, float dy) {
		PlayVideoInfo info = mVideoInfos.get(0);
		info.translate(dx / mSurfaceWidth, dy / mSurfaceHeight, mLeftRatio, mTopRatio);
		shouldRequestRender();
	}

	@Override
	public void scaleFrame(float px, float py, float scale) {
		PlayVideoInfo info = mVideoInfos.get(0);
		info.scale(mSurfaceWidth, mSurfaceHeight, px, py, scale, mLeftRatio, mTopRatio);
		shouldRequestRender();
	}

	@Override
	public void doubleScaleFrame(float px, float py) {
		PlayVideoInfo info = mVideoInfos.get(0);
		info.doubleScale(mSurfaceWidth, mSurfaceHeight, px, py, mLeftRatio, mTopRatio, mRefreshRunnable);
	}

	@Override
	public void rotateFrame(boolean right) {
		PlayVideoInfo info = mVideoInfos.get(0);
		info.rotate(right, mShowRatio, mRefreshRunnable);
	}

	private Runnable mRefreshRunnable = new Runnable() {
		@Override
		public void run() {
			shouldRequestRender();
		}
	};

	@Override
	public void resetFrameAdjust() {
		PlayVideoInfo info = mVideoInfos.get(0);
		info.resetFrameAdjust();
		shouldRequestRender();
	}

	private void shouldRequestRender() {
		if (mState == IPlayer.PAUSE) {
			mGLTextureView.requestRender();
		}
	}

	// ------------------------------ MultiSurface.OnFrameAvailableListener ----------------------------

	@Override
	public void onFrameAvailable(MultiSurface multiSurface) {
//		mVideoPlayer.updateFrame();
		if (isTransitionMode) {
			long position = mVideoPlayer.getCurrentPlayPosition();
			int index = mVideoPlayer.getCurrentIndex();
			if (hasChanged && index == 0 && !mPostReset) {
				mPostReset = true;
				mHandler.post(mResetChangedRunnable);
			} else if (index == 1 && position > mTransitionDuration / 2) {
				hasChanged = true;
				mPostReset = false;
				mVideoPlayer.pause();

				mVideoPlayer.forceSeekTo(0, mStartPosition);
				mPlayRenderer.changeTransition(mTransitionIds[mTransitionIndex]);
				mVideoPlayer.start();
			}
		}

		mGLTextureView.requestRender();
	}

	private Runnable mResetChangedRunnable = new Runnable() {
		@Override
		public void run() {
			hasChanged = false;
		}
	};

	private PlayRenderer.OnRenderListener mOnRenderListener = new PlayRenderer.OnRenderListener() {
		@Override
		public void onCreateSurface(MultiSurface surface) {
			mMultiSurface = surface;
			mMultiSurface.setOnFrameAvailableListener(GLVideoView.this);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (isEnterSingleVideoPlay) {
						reset();

						PlayVideoInfo info = mVideoInfos.get(0);
						mVideoInfos.clear();
						mVideoInfos.add(info);

						mVideoPlayer.setDataSources(info.path);
						mVideoPlayer.setSurface(mMultiSurface);
						mVideoPlayer.prepare();

						mPlayRenderer.changeTransition(TransitionItem.NONE);
						mTotalDuration = info.data.duration;

						mVideoPlayer.start();
						mState = IPlayer.START;
					} else {
						reset();
						prepare();
					}
				}
			});
		}

		@Override
		public int getCurrentIndex() {
			if (isEnterSingleVideoPlay) {
				return mEnterVideoIndex;
			}

			if (isTransitionMode) {

				if (hasChanged) {
					return mTransitionIndex + 1;
				}
				return mVideoPlayer.getCurrentIndex() + mTransitionIndex;
			}

			return mVideoPlayer.getCurrentIndex();
		}

		@Override
		public int getNextIndex() {
			int next = mVideoPlayer.getCurrentIndex() + 1;
			if (isLooping && !mVideoInfos.isEmpty()) {
				next = next % mVideoInfos.size();
			}

			return next;
		}

		@Override
		public long getCurrentPosition() {
			return mVideoPlayer.getCurrentPlayPosition();
		}

		@Override
		public long getCurrentDuration() {
			if (mVideoInfos.isEmpty()) {
				return 0;
			}

			int index = mVideoPlayer.getCurrentIndex();
			if (index >= mVideoInfos.size()) {
				index = mVideoInfos.size() - 1;
			}

			return mVideoInfos.get(index).data.duration;
		}

		@Override
		public void onVideoFinish() {

			if (!isEnterSingleVideoPlay && mState == IPlayer.START) {
				mVideoPlayer.finishVideo();
			}
		}

		@Override
		public void onStartNextVideo() {
			mVideoPlayer.startNext();
		}

		@Override
		public float[] getCurrentMvpMatrix() {
			if (mVideoInfos.isEmpty()) {
				return GlUtil.IDENTITY_MATRIX;
			}

			mHandler.post(mGetMatrixRunnable);
			return mLastMvpMatrix;
		}

		@Override
		public float[] getNextMvpMatrix() {
			int next = mVideoPlayer.getCurrentIndex() + 1;
			if (isLooping && !mVideoInfos.isEmpty()) {
				next = next % mVideoInfos.size();
			}

			if (next >= mVideoInfos.size()) {
				return GlUtil.IDENTITY_MATRIX;
			}

			return mVideoInfos.get(next).mvpMatrix;
		}

		@Override
		public float[] getCurrentTexMatrix() {
			if (mVideoInfos.isEmpty()) {
				return GlUtil.IDENTITY_MATRIX;
			}
			return mVideoInfos.get(mVideoPlayer.getCurrentIndex()).texMatrix;
		}

		@Override
		public float[] getNextTexMatrix() {
			int next = mVideoPlayer.getCurrentIndex() + 1;
			if (isLooping && !mVideoInfos.isEmpty()) {
				next = next % mVideoInfos.size();
			}

			if (next >= mVideoInfos.size()) {
				return GlUtil.IDENTITY_MATRIX;
			}

			return mVideoInfos.get(next).texMatrix;
		}

		@Override
		public void onChangeTransition() {

			if (isSingleVideo) {
				return;
			}

			final int index = mVideoPlayer.getCurrentIndex();
			mGLTextureView.queueEvent(new Runnable() {
				@Override
				public void run() {
					mPlayRenderer.changeTransition(getTransitionId(index));
				}
			});
		}
	};

	private int getTransitionId(int index) {
		int result = TransitionItem.NONE;
		if (mTransitionIds != null && index < mTransitionIds.length) {
			result = mTransitionIds[index];
		}

		return result;
	}

	private Runnable mGetMatrixRunnable = new Runnable() {
		@Override
		public void run() {
			int index = mVideoPlayer.getCurrentIndex();
			if (index < mVideoInfos.size()) {
				System.arraycopy(mVideoInfos.get(index).mvpMatrix, 0, mLastMvpMatrix, 0, 16);
			}
		}
	};

	private IMultiPlayer.OnMultiPlayListener mOnMultiPlayListener = new IMultiPlayer.OnMultiPlayListener() {

		@Override
		public void onStart(int index) {
			hasChangeVideo = false;

			if (index == 0) {
				mUpdateAnimator.start(0, mTotalDuration);
				mLastProgress = 0;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mListenerHelper.onVideoStart();
					}
				});
			}

			System.arraycopy(mVideoInfos.get(index).mvpMatrix, 0, mLastMvpMatrix, 0, 16);

			mPlayRenderer.onVideoStart(index);
			long duration = mVideoInfos.get(index).data.duration;
			if (mTransitionIds != null && index >= 1) {
				if (isBlendTransition(index - 1)) {
					duration -= TransitionItem.DEFAULT_TIME;
				}
			}
			mItemAnimator.start(0, duration);
		}

		@Override
		public void onFinish(int index) {
			if (index == mVideoInfos.size() - 1) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mOnUpdateListener.onAnimatorEnd();
					}
				});

				mUpdateAnimator.reset();

				if (!isLooping) {
					mVideoPlayer.forceSeekTo(0, 0);
					mState = IPlayer.PAUSE;
					mVideoPlayer.pause();
					mPlayRenderer.onVideoPause();
					mListenerHelper.onVideoPause();
				}
			}
			mPlayRenderer.onVideoFinish(index);
			mItemAnimator.reset();
		}

		@Override
		public void onSeekComplete(int index, long position) {

			notifyProgressChanged(index, position, true);
			if (!isSingleVideo && !isTransitionMode) {
				if (index > 0 && position < 500) {
					mPlayRenderer.changeTransition(getTransitionId(index - 1));
				} else {
					mPlayRenderer.changeTransition(getTransitionId(index));
				}

				mPlayRenderer.onVideoSeekTo(index, position);
			}

			if (mState == IPlayer.START) {
				final float startProgress = mLastProgress;
				long duration = (long)(mTotalDuration * (1 - startProgress));
				mVideoPlayer.start();
				mUpdateAnimator.start(startProgress, duration);

				long itemDuration = mVideoInfos.get(index).data.duration;
				float itemProgress = position / (float)itemDuration;
				duration = (long)(itemDuration * (1 - itemProgress));
				mItemAnimator.start(itemProgress, duration);

				mPlayRenderer.onVideoResume();
				mListenerHelper.onVideoResume();
			}
		}

		@Override
		public long getDuration(int index) {
			return mVideoInfos.get(index).data.duration;
		}

		@Override
		public boolean isBlendTransition(int index) {
			return index < mTransitionIds.length && TransitionItem.isBlendTransition(mTransitionIds[index]);
		}

		@Override
		public boolean isMute(int index) {
			return index >= 0 && index < mVideoInfos.size() && mVideoInfos.get(index).isMute;
		}
	};

	private UpdateAnimator.OnUpdateListener mOnUpdateListener = new UpdateAnimator.OnUpdateListener() {
		@Override
		public void onUpdate(float progress) {
			mLastProgress = progress;
			mListenerHelper.onProgressChanged(progress, false);
		}

		@Override
		public void onAnimatorEnd() {
			mLastProgress = 1;
			mListenerHelper.onProgressChanged(1, false);
		}
	};

	private UpdateAnimator.OnUpdateListener mOnItemUpdateListener = new UpdateAnimator.OnUpdateListener() {

		@Override
		public void onUpdate(float progress) {
			mListenerHelper.onItemProgressChanged(mVideoPlayer.getCurrentIndex(), progress, false);
		}

		@Override
		public void onAnimatorEnd() {
			mListenerHelper.onItemProgressChanged(mVideoPlayer.getCurrentIndex(), 1, false);
		}
	};
}
