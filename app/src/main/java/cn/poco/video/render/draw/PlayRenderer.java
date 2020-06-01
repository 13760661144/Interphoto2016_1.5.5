package cn.poco.video.render.draw;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Looper;
import android.util.SparseArray;

import cn.poco.video.render.adjust.AbstractAdjust;
import cn.poco.video.render.adjust.AdjustItem;
import cn.poco.video.render.adjust.DarkCornerAdjust;
import cn.poco.video.render.adjust.SharpenAdjust;
import cn.poco.video.render.adjust.VideoAdjust;
import cn.poco.video.render.base.GLTextureView;
import cn.poco.video.render.filter.AbstractFilter;
import cn.poco.video.render.filter.BlendFilter;
import cn.poco.video.render.filter.FilterItem;
import cn.poco.video.render.filter.LookupTableFilter;
import cn.poco.video.render.player.MultiSurface;
import cn.poco.video.render.player.VideoMonitor;
import cn.poco.video.render.transition.AbstractTransition;
import cn.poco.video.render.transition.BlendTransition;
import cn.poco.video.render.transition.LinearTransition;
import cn.poco.video.render.transition.NoneTransition;
import cn.poco.video.render.transition.TransitionItem;

/**
 * Created by: fwc
 * Date: 2017/12/13
 */
public class PlayRenderer implements GLTextureView.Renderer {

	private Context mContext;

	private OnRenderListener mOnRenderListener;

	private int mWidth;
	private int mHeight;

	private VideoMonitor mVideoMonitor;

	private MultiSurface mSurface;

	private RenderManager mRenderManager;

	/**
	 * 视频滤镜
	 */
	private SparseArray<AbstractFilter> mFilterArray = new SparseArray<>();
	private SparseArray<AbstractFilter.Params> mFilterParamArray = new SparseArray<>();

	/**
	 * 视频调整
	 */
	private SparseArray<AbstractAdjust> mAdjustArray = new SparseArray<>();
	private SparseArray<AdjustInfo> mAdjustInfoArray = new SparseArray<>();

	/**
	 * 曲线数据
	 */
	private SparseArray<byte[]> mCurveArray = new SparseArray<>();

	/**
	 * 转场动画
	 */
	private int mTransitionId;
	private AbstractTransition mTransition;
	private AbstractTransition mDefaultTransition;
	private SparseArray<AbstractTransition> mTransitionArray = new SparseArray<>();

	private int mCurTextureId;
	private int mNextTextureId;

	private float[] mCurrTexMatrix = new float[16];
	private float[] mNextTexMatrix = new float[16];

	private boolean isRelease;

	public PlayRenderer(Context context, OnRenderListener listener) {
		mContext = context;
		mOnRenderListener = listener;

		mVideoMonitor = new VideoMonitor(mOnMonitorListener);
	}

	@Override
	public void onSurfaceCreated() {
		GLES20.glClearColor(0, 0, 0, 1);

		mRenderManager = new RenderManager(mContext);

		mDefaultTransition = new NoneTransition(mContext, mVideoMonitor.getLooper());
		mDefaultTransition.setValue(null);
		mDefaultTransition.setOnTransitionListener(mOnTransitionListener);

		mTransition = mDefaultTransition;
		mTransitionId = TransitionItem.NONE;

		mSurface = new MultiSurface(mOnSurfaceChangeListener);
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		mWidth = width;
		mHeight = height;

		mRenderManager.setSurfaceSize(width, height);

		initFilters();
		initAdjusts();
		initTransitions();

		// 防止资源没初始化，放在这里回调
		if (mOnRenderListener != null) {
			mOnRenderListener.onCreateSurface(mSurface);
		}
	}

	private void initFilters() {
		mFilterArray.clear();
		mFilterArray.put(FilterItem.TYPE_LOOKUP_TABLE, new LookupTableFilter(mContext));
		mFilterArray.put(FilterItem.TYPE_BLEND, new BlendFilter(mContext));
	}

	private void initAdjusts() {
		mAdjustArray.clear();
		mAdjustArray.put(AbstractAdjust.TYPE_VIDEO_ADJUST, new VideoAdjust(mContext));
		mAdjustArray.put(AbstractAdjust.TYPE_SHARPEN_ADJUST, new SharpenAdjust(mContext, mWidth, mHeight));
		mAdjustArray.put(AbstractAdjust.TYPE_DARK_CORNER, new DarkCornerAdjust(mContext));
	}

	private void initTransitions() {
		mTransitionArray.clear();
		Looper looper = mVideoMonitor.getLooper();
		mTransitionArray.put(TransitionItem.TYPE_LINEAR, new LinearTransition(mContext, looper));
		mTransitionArray.put(TransitionItem.TYPE_BLEND, new BlendTransition(mContext, looper));
	}

	@Override
	public void onDrawFrame() {

		if (isRelease) {
			return;
		}

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		mSurface.updateTexImage();

		long timestamp = mOnRenderListener.getCurrentPosition();
		mVideoMonitor.update(timestamp);

		Matrix.multiplyMM(mCurrTexMatrix, 0, mOnRenderListener.getCurrentTexMatrix(), 0, mSurface.getCurrentSTMatrix(), 0);
		mCurTextureId = mRenderManager.drawFrame(mSurface.getCurrentTextureId(), mOnRenderListener.getCurrentMvpMatrix(), mCurrTexMatrix);
		int curIndex = mOnRenderListener.getCurrentIndex();
		mCurTextureId = drawFilter(curIndex, mCurTextureId);
		mCurTextureId = drawAdjust(curIndex, mCurTextureId);
		mCurTextureId = drawCurve(curIndex, mCurTextureId);

		if (mTransition.shouldRenderNext()) {
			Matrix.multiplyMM(mNextTexMatrix, 0, mOnRenderListener.getNextTexMatrix(), 0, mSurface.getNextSTMatrix(), 0);
			mNextTextureId = mRenderManager.drawFrame(mSurface.getNextTextureId(), mOnRenderListener.getNextMvpMatrix(), mNextTexMatrix);
			int nextIndex = mOnRenderListener.getNextIndex();
			mNextTextureId = drawFilter(nextIndex, mNextTextureId);
			mNextTextureId = drawAdjust(nextIndex, mNextTextureId);
			mNextTextureId = drawCurve(nextIndex, mNextTextureId);
		} else {
			mNextTextureId = -1;
		}

		mRenderManager.drawTransition(mTransition, mTransitionId, mCurTextureId, mNextTextureId);
	}

	private int drawFilter(int index, int textureId) {
		AbstractFilter.Params params = mFilterParamArray.get(index);
		if (params != null) {
			if (params.type == FilterItem.TYPE_BOTH) {
				AbstractFilter filter = mFilterArray.get(FilterItem.TYPE_LOOKUP_TABLE);
				filter.setParams(params);
				textureId = mRenderManager.drawFilter(filter, textureId);

				filter = mFilterArray.get(FilterItem.TYPE_BLEND);
				filter.setParams(params);
				return mRenderManager.drawFilter(filter, textureId);
			} else {
				AbstractFilter filter = mFilterArray.get(params.type);
				filter.setParams(params);
				return mRenderManager.drawFilter(filter, textureId);
			}
		}

		return textureId;
	}

	private int drawAdjust(int index, int textureId) {
		AdjustInfo info = mAdjustInfoArray.get(index);
		if (info == null) {
			return textureId;
		}

		int type = AbstractAdjust.TYPE_VIDEO_ADJUST;
		if (info.shouldDraw(type)) {
			textureId = mRenderManager.drawAdjust(mAdjustArray.get(type), info.params.get(type), textureId);
		}

		type = AbstractAdjust.TYPE_SHARPEN_ADJUST;
		if (info.shouldDraw(type)) {
			textureId = mRenderManager.drawAdjust(mAdjustArray.get(type), info.params.get(type), textureId);
		}

		type = AbstractAdjust.TYPE_DARK_CORNER;
		if (info.shouldDraw(type)) {
			textureId = mRenderManager.drawAdjust(mAdjustArray.get(type), info.params.get(type), textureId);
		}

		return textureId;
	}

	private int drawCurve(int index, int textureId) {
		byte[] data = mCurveArray.get(index);
		if (data != null) {
			return mRenderManager.drawCurve(data, textureId);
		}

		return textureId;
	}

	@Override
	public void onSurfaceDestroyed() {
		// GL线程
		mTransition.setOnTransitionListener(null);

		if (mSurface != null) {
			mSurface.release();
			mSurface = null;
		}

		releaseFilter();
		releaseAdjust();
		releaseTransition();

		if (mRenderManager != null) {
			mRenderManager.release();
			mRenderManager = null;
		}
	}

	/**
	 * 释放滤镜资源
	 */
	private void releaseFilter() {
		for (int i = 0; i < mFilterArray.size(); i++) {
			mFilterArray.get(mFilterArray.keyAt(i)).release();
		}
		mFilterArray.clear();
	}

	/**
	 * 释放转场动画资源
	 */
	private void releaseTransition() {

		for (int i = 0; i < mTransitionArray.size(); i++) {
			mTransitionArray.get(mTransitionArray.keyAt(i)).release();
		}

		mTransitionArray.clear();
	}

	/**
	 * 释放视频调整资源
	 */
	private void releaseAdjust() {
		for (int i = 0; i < mAdjustArray.size(); i++) {
			mAdjustArray.get(mAdjustArray.keyAt(i)).release();
		}

		mAdjustArray.clear();
	}

	public void onVideoStart(int index) {
		mVideoMonitor.start();
		mTransition.onVideoStart(index);
	}

	public void onVideoResume() {
		mVideoMonitor.start();
		mTransition.onVideoResume();
	}

	public void onVideoSeekTo(int index, long position) {
		long duration = mOnRenderListener.getCurrentDuration();
		mTransition.onVideoSeekTo(index, position, duration);
	}

	public void onVideoPause() {
		mVideoMonitor.reset();
		mTransition.onVideoPause();
	}

	public void onVideoFinish(int index) {
		if (!mTransition.isBlendTransition()) {
			mVideoMonitor.reset();
		}
	}

	/**
	 * 更改局部滤镜
	 *
	 * @param index 视频下标
	 * @param item  滤镜item
	 */
	public void changeFilter(int index, FilterItem item) {
		if (item == null) {
			mFilterParamArray.put(index, null);
		} else {
			mFilterParamArray.put(index, new AbstractFilter.Params(item));
		}
	}

	/**
	 * 更改局部滤镜透明度
	 *
	 * @param index 视频下标
	 * @param alpha 滤镜透明度，0~1
	 */
	public void changeFilterAlpha(int index, float alpha) {
		AbstractFilter.Params params = mFilterParamArray.get(index);
		if (params != null) {
			params.alpha = alpha;
		}
	}

	public SparseArray<AbstractFilter.Params> getFilterParamArray() {
		return mFilterParamArray;
	}

	public void addAdjust(int index, AdjustItem item) {
		if (item == null) {
			return;
		}

		AdjustInfo info = mAdjustInfoArray.get(index);
		if (info == null) {
			info = new AdjustInfo();
			mAdjustInfoArray.put(index, info);
		}

		int type = AdjustItem.getType(item.id);
		AbstractAdjust.Params params = info.params.get(type);
		if (params == null) {
			params = AdjustItem.getParams(item.id);
			info.params.put(type, params);
		}
		params.addItem(item);
	}

	public SparseArray<AdjustInfo> getAdjustInfoArray() {
		return mAdjustInfoArray;
	}

	public void changeCurve(int index, byte[] data) {
		if (data == null) {
			mCurveArray.remove(index);
		} else {
			mCurveArray.put(index, data);
		}
	}

	public SparseArray<byte[]> getCurveArray() {
		return mCurveArray;
	}

	public void changeTransition(int id) {

		if (mTransition == null) {
			return;
		}

		mTransition.reset();

		if (mTransitionId != id) {
			mTransitionId = id;
			mTransition.setOnTransitionListener(null);

			if (id == TransitionItem.NONE) {
				mTransition = mDefaultTransition;
			} else {
				mTransition = mTransitionArray.get(TransitionItem.getType(id));
			}
			if (mTransition == null) {
				throw new RuntimeException("mTransition is null");
			}
			TransitionItem.setValue(mTransition, id);
			mTransition.setOnTransitionListener(mOnTransitionListener);
		}

		mTransition.onUse();
	}

	public void deleteVideo(int index) {
		int key;
		mFilterParamArray.remove(index);
		SparseArray<AbstractFilter.Params> filterTemp = new SparseArray<>();
		for (int i = 0; i < mFilterParamArray.size(); i++) {
			key = mFilterParamArray.keyAt(i);
			if (key > index) {
				filterTemp.put(key - 1, mFilterParamArray.get(key));
			} else {
				filterTemp.put(key, mFilterParamArray.get(key));
			}
		}
		mFilterParamArray.clear();
		copyArray(filterTemp, mFilterParamArray);
		filterTemp.clear();

		mAdjustInfoArray.remove(index);
		SparseArray<AdjustInfo> adjustTemp = new SparseArray<>();
		for (int i = 0; i < mAdjustInfoArray.size(); i++) {
			key = mAdjustInfoArray.keyAt(i);
			if (key > index) {
				adjustTemp.put(key - 1, mAdjustInfoArray.get(key));
			} else {
				adjustTemp.put(key, mAdjustInfoArray.get(key));
			}
		}
		mAdjustInfoArray.clear();
		copyArray(adjustTemp, mAdjustInfoArray);
		adjustTemp.clear();

		mCurveArray.remove(index);
		SparseArray<byte[]> curveTemp = new SparseArray<>();
		for (int i = 0; i < mCurveArray.size(); i++) {
			key = mCurveArray.keyAt(i);
			if (key > index) {
				curveTemp.put(key - 1, mCurveArray.get(key));
			} else {
				curveTemp.put(key, mCurveArray.get(key));
			}
		}
		mCurveArray.clear();
		copyArray(curveTemp, mCurveArray);
		curveTemp.clear();
	}

	public void copyVideo(int index) {
		int key;
		int newIndex = index + 1;

		SparseArray<AbstractFilter.Params> filterTemp = new SparseArray<>();
		for (int i = 0; i < mFilterParamArray.size(); i++) {
			key = mFilterParamArray.keyAt(i);
			if (key >= newIndex) {
				filterTemp.put(key + 1, mFilterParamArray.get(key));
			} else {
				filterTemp.put(key, mFilterParamArray.get(key));
			}
		}
		filterTemp.put(newIndex, mFilterParamArray.get(index));
		mFilterParamArray.clear();
		copyArray(filterTemp, mFilterParamArray);
		filterTemp.clear();

		SparseArray<AdjustInfo> adjustTemp = new SparseArray<>();
		for (int i = 0; i < mAdjustInfoArray.size(); i++) {
			key = mAdjustInfoArray.keyAt(i);
			if (key >= newIndex) {
				adjustTemp.put(key + 1, mAdjustInfoArray.get(key));
			} else {
				adjustTemp.put(key, mAdjustInfoArray.get(key));
			}
		}
		adjustTemp.put(newIndex, mAdjustInfoArray.get(index));
		mAdjustInfoArray.clear();
		copyArray(adjustTemp, mAdjustInfoArray);
		adjustTemp.clear();

		SparseArray<byte[]> curveTemp = new SparseArray<>();
		for (int i = 0; i < mCurveArray.size(); i++) {
			key = mCurveArray.keyAt(i);
			if (key >= newIndex) {
				curveTemp.put(key + 1, mCurveArray.get(key));
			} else {
				curveTemp.put(key, mCurveArray.get(key));
			}
		}
		curveTemp.put(newIndex, mCurveArray.get(index));
		mCurveArray.clear();
		copyArray(curveTemp, mCurveArray);
		curveTemp.clear();

	}

	public void changeVideoOrder(int fromPosition, int toPosition) {
		int key;
		SparseArray<AbstractFilter.Params> filterTemp = new SparseArray<>();
		AbstractFilter.Params params = mFilterParamArray.get(fromPosition);
		mFilterParamArray.remove(fromPosition);
		for (int i = 0; i < mFilterParamArray.size(); i++) {
			key = mFilterParamArray.keyAt(i);
			if (key < fromPosition || key > toPosition) {
				filterTemp.put(key, mFilterParamArray.get(key));
			} else {
				filterTemp.put(key - 1, mFilterParamArray.get(key));
			}
		}
		filterTemp.put(toPosition, params);
		mFilterParamArray.clear();
		copyArray(filterTemp, mFilterParamArray);
		filterTemp.clear();

		SparseArray<AdjustInfo> adjustTemp = new SparseArray<>();
		AdjustInfo info = mAdjustInfoArray.get(fromPosition);
		mAdjustInfoArray.remove(fromPosition);
		for (int i = 0; i < mAdjustInfoArray.size(); i++) {
			key = mAdjustInfoArray.keyAt(i);
			if (key < fromPosition || key > toPosition) {
				adjustTemp.put(key, mAdjustInfoArray.get(key));
			} else {
				adjustTemp.put(key - 1, mAdjustInfoArray.get(key));
			}
		}
		adjustTemp.put(toPosition, info);
		mAdjustInfoArray.clear();
		copyArray(adjustTemp, mAdjustInfoArray);
		adjustTemp.clear();

		SparseArray<byte[]> curveTemp = new SparseArray<>();
		byte[] data = mCurveArray.get(fromPosition);
		mCurveArray.remove(fromPosition);
		for (int i = 0; i < mCurveArray.size(); i++) {
			key = mCurveArray.keyAt(i);
			if (key < fromPosition || key > toPosition) {
				curveTemp.put(key, mCurveArray.get(key));
			} else {
				curveTemp.put(key - 1, mCurveArray.get(key));
			}
		}
		curveTemp.put(toPosition, data);
		mCurveArray.clear();
		copyArray(curveTemp, mCurveArray);
		curveTemp.clear();
	}

	private <T> void copyArray(SparseArray<T> from, SparseArray<T> to) {
		int key;
		for (int i = 0; i < from.size(); i++) {
			key = from.keyAt(i);
			to.put(key, from.get(key));
		}
	}

	private VideoMonitor.OnMonitorListener mOnMonitorListener = new VideoMonitor.OnMonitorListener() {
		@Override
		public void onUpdate(long timestamp) {
			mTransition.update(timestamp, mOnRenderListener.getCurrentDuration());
		}
	};

	private MultiSurface.OnSurfaceChangeListener mOnSurfaceChangeListener = new MultiSurface.OnSurfaceChangeListener() {
		@Override
		public void onChangeSurface() {
			mTransition.onChangeSurface();
		}
	};


	private AbstractTransition.OnTransitionListener mOnTransitionListener = new AbstractTransition.OnTransitionListener() {
		@Override
		public void requestRender() {
			mSurface.requestRender();
		}

		@Override
		public void onVideoFinish() {
			if (mOnRenderListener != null) {
				mOnRenderListener.onVideoFinish();
			}
		}

		@Override
		public void onStartNextVideo() {
			if (mOnRenderListener != null) {
				mOnRenderListener.onStartNextVideo();
			}
		}

		@Override
		public void onTransitionFinish() {
			if (mOnRenderListener != null) {
				mOnRenderListener.onChangeTransition();
			}
		}
	};

	/**
	 * 释放，注意不要再这里释放GL资源
	 */
	public void release() {

		isRelease = true;

		mFilterParamArray.clear();
		mAdjustInfoArray.clear();
		mVideoMonitor.release();
	}

	public static class AdjustInfo {
		public SparseArray<AbstractAdjust.Params> params = new SparseArray<>();

		public boolean shouldDraw(int type) {
			AbstractAdjust.Params param = params.get(type);
			return param != null && !param.isDefault();
		}
	}

	public interface OnRenderListener {

		void onCreateSurface(MultiSurface surface);

		int getCurrentIndex();

		int getNextIndex();

		long getCurrentPosition();

		long getCurrentDuration();

		void onVideoFinish();

		void onStartNextVideo();

		float[] getCurrentMvpMatrix();

		float[] getNextMvpMatrix();

		float[] getCurrentTexMatrix();

		float[] getNextTexMatrix();

		void onChangeTransition();
	}
}
