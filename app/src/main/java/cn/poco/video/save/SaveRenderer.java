package cn.poco.video.save;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.SparseArray;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import cn.poco.video.render.adjust.AbstractAdjust;
import cn.poco.video.render.adjust.DarkCornerAdjust;
import cn.poco.video.render.adjust.SharpenAdjust;
import cn.poco.video.render.adjust.VideoAdjust;
import cn.poco.video.render.draw.PlayRenderer;
import cn.poco.video.render.filter.AbstractFilter;
import cn.poco.video.render.filter.BlendFilter;
import cn.poco.video.render.filter.FilterItem;
import cn.poco.video.render.filter.LookupTableFilter;
import cn.poco.video.render.gles.GlUtil;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.save.player.SoftTexture;
import cn.poco.video.save.transition.AbstractTransition;
import cn.poco.video.save.transition.BlendTransition;
import cn.poco.video.save.transition.LinearTransition;
import cn.poco.video.save.transition.NoneTransition;
import cn.poco.video.save.watermark.BitmapInfo;
import cn.poco.video.save.watermark.WatermarkFilter;

import static cn.poco.video.render.transition.TransitionItem.WHITE;

/**
 * Created by: fwc
 * Date: 2017/12/26
 */
public class SaveRenderer implements EGLHelper.Renderer {

	private Context mContext;

	private SaveParams mSaveParams;

	private OnRenderListener mOnRenderListener;

	private int mWidth;
	private int mHeight;

	private RenderManager mRenderManager;

	private int mTexture1 = GlUtil.NO_TEXTURE;
	private SoftTexture mSoftTexture1;
	private float[] mSTMatrix1;

	private int mTexture2 = GlUtil.NO_TEXTURE;
	private SoftTexture mSoftTexture2;
	private float[] mSTMatrix2;

	private int mCurTextureId;
	private int mNextTextureId;

	private float[] mCurrTexMatrix = new float[16];
	private float[] mNextTexMatrix = new float[16];

	private int mCurIndex;

	/**
	 * 视频滤镜
	 */
	private SparseArray<AbstractFilter> mFilterArray = new SparseArray<>();

	/**
	 * 视频调整
	 */
	private SparseArray<AbstractAdjust> mAdjustArray = new SparseArray<>();

	/**
	 * 转场动画
	 */
	private int mTransitionId;
	private AbstractTransition mTransition;
	private AbstractTransition.OnTransitionListener mOnTransitionListener;
	private SparseArray<AbstractTransition> mTransitionArray = new SparseArray<>();

	/**
	 * 水印
	 */
	private WatermarkFilter mWatermarkFilter;
	private Paint mPaint;
	private List<SoftReference<Bitmap>> mReuseList;

	/**
	 * 视频logo
	 */
	private WatermarkFilter mVideoLogo;

	/**
	 * 每帧的间隔，按照每秒25帧来算
	 */
	private static final long FRAME_INTERVAL = 1000000 / 25;

	private long mLastDrawTimestamp = -FRAME_INTERVAL;
	private long mLastTimestamp = 0;
	private boolean mResetTimestamp = false;

	public SaveRenderer(Context context, SaveParams saveParams, OnRenderListener listener) {
		mContext = context;
		mSaveParams = saveParams;

		mOnRenderListener = listener;

		init();
	}

	private void init() {

		mSTMatrix1 = new float[16];
		mSTMatrix2 = new float[16];

		Matrix.setIdentityM(mSTMatrix1, 0);
		Matrix.setIdentityM(mSTMatrix2, 0);

		mReuseList = new ArrayList<>();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setFilterBitmap(true);
	}

	public void setOnTransitionListener(AbstractTransition.OnTransitionListener listener) {
		mOnTransitionListener = listener;
	}

	@Override
	public void onSurfaceCreated() {
		GLES20.glClearColor(0, 0, 0, 1);

		mRenderManager = new RenderManager(mContext);

		mTexture1 = GlUtil.createTexture(GLES20.GL_TEXTURE_2D);
		mSoftTexture1 = new SoftTexture(mTexture1);

		mTexture2 = GlUtil.createTexture(GLES20.GL_TEXTURE_2D);
		mSoftTexture2 = new SoftTexture(mTexture2);

		if (mOnRenderListener != null) {
			mOnRenderListener.onCreateSurface(mSoftTexture1, mSoftTexture2);
		}
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		mWidth = width;
		mHeight = height;

		mRenderManager.setSurfaceSize(width, height);

		if (mSaveParams.videoText != null) {
			mWatermarkFilter = new WatermarkFilter(mContext);
		}

		if (mSaveParams.logoPath != null || mSaveParams.videoLogo != 0) {
			Bitmap bitmap;
			if (mSaveParams.logoPath != null) {
				bitmap = BitmapFactory.decodeFile(mSaveParams.logoPath);
			} else {
				bitmap = BitmapFactory.decodeResource(mContext.getResources(), mSaveParams.videoLogo);
			}

			if (bitmap != null) {
				mVideoLogo = new WatermarkFilter(mContext);
				float scaleRatio = 1f;
				if ((float)mWidth / mHeight == 9f / 16) {
					scaleRatio = 0.8f;
				} else if (mWidth == mHeight) {
					scaleRatio = 1.4f;
				}
				int size = Math.max(mWidth, mHeight);
				float scale = size / 1920f;
				float scaleX = (float)bitmap.getWidth() / mWidth * scaleRatio * scale;
				float scaleY = (float)bitmap.getHeight() / mHeight * scaleRatio * scale;
				float x = 1 - scaleX - 0.0145f;
				float y = scaleY - 1 + 0.018f * mWidth / mHeight;
				mVideoLogo.calculatePosition(x, y, scaleX, scaleY);
				mVideoLogo.changeBitmap(bitmap);
			}
		}

		initFilters();
		initAdjusts();
		initTransitions();
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

		if (mSaveParams.transitions == null) {
			mTransitionId = TransitionItem.NONE;
			mTransition = new NoneTransition(mContext);
			mTransitionArray.put(TransitionItem.TYPE_NONE, mTransition);

			mTransition.setValue(null);

		} else {
			mTransitionArray.put(TransitionItem.TYPE_NONE, new NoneTransition(mContext));
			mTransitionArray.put(TransitionItem.TYPE_LINEAR, new LinearTransition(mContext));
			mTransitionArray.put(TransitionItem.TYPE_BLEND, new BlendTransition(mContext));

			mTransitionId = getTransitionId(0);
			mTransition = mTransitionArray.get(TransitionItem.getType(mTransitionId));
			setValue(mTransition, mTransitionId);
		}

		mTransition.setOnTransitionListener(mOnTransitionListener);
	}

	@Override
	public void onDrawFrame() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		mCurIndex = mOnRenderListener.getCurrentIndex();

		mSoftTexture1.updateTexImage();
		mSoftTexture1.getTransformMatrix(mSTMatrix1);
		Matrix.multiplyMM(mCurrTexMatrix, 0, getTexMatrix(mCurIndex), 0, mSTMatrix1, 0);
		mCurTextureId = mRenderManager.drawFrame(mTexture1, getMvpMatrix(mCurIndex), mCurrTexMatrix);

		if (mCurIndex + 1 < mSaveParams.videoInfos.size()) {
			mSoftTexture2.updateTexImage();
			mSoftTexture2.getTransformMatrix(mSTMatrix2);
			Matrix.multiplyMM(mNextTexMatrix, 0, getTexMatrix(mCurIndex + 1), 0, mSTMatrix2, 0);
			mNextTextureId = mRenderManager.drawFrame(mTexture2, getMvpMatrix(mCurIndex + 1), mNextTexMatrix);
		} else {
			mNextTextureId = -1;
		}

		long timestamp = mSoftTexture1.getTimestamp();

		if (mResetTimestamp) {
			timestamp = (timestamp / FRAME_INTERVAL - 1) * FRAME_INTERVAL;
			mLastDrawTimestamp = timestamp;
			mLastTimestamp = mLastTimestamp - mLastDrawTimestamp - FRAME_INTERVAL;
		}

		if (timestamp >= mLastDrawTimestamp + FRAME_INTERVAL) {
			long lastTimestamp = mLastDrawTimestamp + FRAME_INTERVAL;
			if (lastTimestamp <= timestamp) {
				mCurTextureId = drawFilter(mCurIndex, mCurTextureId);
				mCurTextureId = drawAdjust(mCurIndex, mCurTextureId);
				mCurTextureId = drawCurve(mCurIndex, mCurTextureId);

				if (mNextTextureId != -1 && mTransition.shouldRenderNext()) {
					mNextTextureId = drawFilter(mCurIndex + 1, mNextTextureId);
					mNextTextureId = drawAdjust(mCurIndex + 1, mNextTextureId);
					mNextTextureId = drawCurve(mCurIndex + 1, mNextTextureId);
				}
			}
			while (lastTimestamp <= timestamp) {
				onDraw(lastTimestamp);
				lastTimestamp += FRAME_INTERVAL;
			}
		}

		mRenderManager.resetBuffer(mCurTextureId);
		mRenderManager.resetBuffer(mNextTextureId);

		mResetTimestamp = false;
	}

	public void onVideoStart(int index) {

	}

	public void onVideoFinish(int index) {

		mTransition.onVideoFinish(index);

		mLastTimestamp += (mLastDrawTimestamp + FRAME_INTERVAL);
		if (mTransition.isBlendTransition()) {
			mResetTimestamp = true;
			onChangeTransition(index + 1);
		} else {
			mLastDrawTimestamp = -FRAME_INTERVAL;
		}

		exchangeTextureId();
	}

	private void exchangeTextureId() {
		SoftTexture softTexture = mSoftTexture1;
		mSoftTexture1 = mSoftTexture2;
		mSoftTexture2 = softTexture;

		int textureId = mTexture1;
		mTexture1 = mTexture2;
		mTexture2 = textureId;
	}

	/**
	 * 具体绘制逻辑
	 */
	private void onDraw(long timestamp) {
		long duration = mSaveParams.videoInfos.get(mCurIndex).data.duration;
		boolean isFinish = mTransition.update(timestamp / 1000, duration);

		mRenderManager.drawTransition(mTransition, mTransitionId, mCurTextureId, mNextTextureId);

		if (mOnRenderListener != null && mWatermarkFilter != null) {
			BitmapInfo bitmapInfo = mOnRenderListener.getWatermarkInfo(timestamp + mLastTimestamp);

			if (bitmapInfo != null && bitmapInfo.bitmap != null) {
				Bitmap bitmap = getWatermark(bitmapInfo);
				changeWatermark(bitmap, bitmapInfo.x, bitmapInfo.y, bitmapInfo.scaleX, bitmapInfo.scaleY);
				mRenderManager.drawWatermark(mWatermarkFilter);
				mReuseList.add(new SoftReference<>(bitmap));
			}
		}

		if (mVideoLogo != null) {
			mRenderManager.drawWatermark(mVideoLogo);
		}

		if (isFinish) {
			onChangeTransition(mCurIndex + 1);
		}

		mLastDrawTimestamp = timestamp;
		onDrawFinish(timestamp + mLastTimestamp);
	}

	private int drawFilter(int index, int textureId) {
		AbstractFilter.Params params = mSaveParams.filterParamArray.get(index);
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
		PlayRenderer.AdjustInfo info = mSaveParams.adjustInfoArray.get(index);
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
		byte[] data = mSaveParams.curveArray.get(index);
		if (data != null) {
			return mRenderManager.drawCurve(data, textureId);
		}

		return textureId;
	}

	private void onDrawFinish(long timestamp) {
		if (mOnRenderListener != null) {
			mOnRenderListener.onDrawFrame(timestamp);
		}
	}

	private float[] getMvpMatrix(int videoIndex) {
		if (videoIndex >= 0 && videoIndex < mSaveParams.videoInfos.size()) {
			return mSaveParams.videoInfos.get(videoIndex).saveMatrix;
		}

		return GlUtil.IDENTITY_MATRIX;
	}

	private float[] getTexMatrix(int videoIndex) {
		if (videoIndex >= 0 && videoIndex < mSaveParams.videoInfos.size()) {
			return mSaveParams.videoInfos.get(videoIndex).texMatrix;
		}

		return GlUtil.IDENTITY_MATRIX;
	}

	private int getTransitionId(int videoIndex) {
		if (videoIndex >= 0 && videoIndex < mSaveParams.transitions.length) {
			return mSaveParams.transitions[videoIndex];
		}

		return TransitionItem.NONE;
	}

	/**
	 * 更换转场动画
	 */
	private void onChangeTransition(int videoIndex) {
		mTransition.reset();

		if (mSaveParams.transitions == null) {
			return;
		}

		int id = getTransitionId(videoIndex);
		if (mTransitionId != id) {
			mTransitionId = id;
			mTransition.setOnTransitionListener(null);

			mTransition = mTransitionArray.get(TransitionItem.getType(id));
			if (mTransition == null) {
				throw new RuntimeException("mTransition is null");
			}
			setValue(mTransition, id);
			mTransition.setOnTransitionListener(mOnTransitionListener);
		}
	}

	private static void setValue(AbstractTransition transition, int id) {
		switch (id) {
			case TransitionItem.BLACK:
				transition.setValue(cn.poco.video.render.transition.LinearTransition.MASK_BLACK);
				break;
			case WHITE:
				transition.setValue(cn.poco.video.render.transition.LinearTransition.MASK_WHITE);
				break;
		}
	}

	@Override
	public void onSurfaceDestroy() {
		release();
	}

	private void release() {

		releaseFilter();
		releaseAdjust();
		releaseTransition();
		releaseWatermark();
		releaseVideoLogo();

		if (mRenderManager != null) {
			mRenderManager.release();
			mRenderManager = null;
		}

		if (mSoftTexture1 != null) {
			mSoftTexture1.release();
			mSoftTexture1 = null;
		}

		if (mTexture1 != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mTexture1}, 0);
			mTexture1 = GlUtil.NO_TEXTURE;
		}

		if (mSoftTexture2 != null) {
			mSoftTexture2.release();
			mSoftTexture2 = null;
		}

		if (mTexture2 != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mTexture2}, 0);
			mTexture2 = GlUtil.NO_TEXTURE;
		}

		mSaveParams = null;
	}

	private void releaseFilter() {
		for (int i = 0; i < mFilterArray.size(); i++) {
			mFilterArray.get(mFilterArray.keyAt(i)).release();
		}
		mFilterArray.clear();
	}

	private void releaseAdjust() {
		for (int i = 0; i < mAdjustArray.size(); i++) {
			mAdjustArray.get(mAdjustArray.keyAt(i)).release();
		}

		mAdjustArray.clear();
	}

	private void releaseTransition() {
		for (int i = 0; i < mTransitionArray.size(); i++) {
			mTransitionArray.get(mTransitionArray.keyAt(i)).release();
		}

		mTransitionArray.clear();
		mTransition = null;
	}

	private void releaseWatermark() {
		if (mWatermarkFilter != null) {
			mWatermarkFilter.release();
			mWatermarkFilter = null;
		}

		mReuseList.clear();
	}

	private void releaseVideoLogo() {
		if (mVideoLogo != null) {
			mVideoLogo.release();
			mVideoLogo = null;
		}
	}

	private void changeWatermark(Bitmap bitmap, float x, float y, float scaleX, float scaleY) {
		if (mWatermarkFilter != null && bitmap != null) {
			mWatermarkFilter.calculatePosition(x, y, scaleX, scaleY);
			mWatermarkFilter.changeBitmap(bitmap);
		}
	}

	private Bitmap getWatermark(BitmapInfo info) {

		int width = info.bitmap.getWidth();
		int height = info.bitmap.getHeight();

		Bitmap result = getReuseBitmap(width, height);

		if (result == null) {
			result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		}

		mPaint.setAlpha(info.alpha);
		Canvas canvas = new Canvas(result);
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		canvas.drawBitmap(info.bitmap, 0, 0, mPaint);

		return result;
	}

	private Bitmap getReuseBitmap(int width, int height) {

		Bitmap result = null;
		List<SoftReference<Bitmap>> removeList = new ArrayList<>();
		if (!mReuseList.isEmpty()) {
			Bitmap bitmap;
			for (SoftReference<Bitmap> reference : mReuseList) {
				bitmap = reference.get();
				if (bitmap == null) {
					removeList.add(reference);
				} else if (bitmap.getWidth() == width && bitmap.getHeight() == height) {
					result = bitmap;
					removeList.add(reference);
					break;
				}
			}

			if (!removeList.isEmpty()) {
				mReuseList.removeAll(removeList);
			}
		}

		return result;
	}

	public interface OnRenderListener {

		void onCreateSurface(SoftTexture softTexture1, SoftTexture softTexture2);

		int getCurrentIndex();

		void onDrawFrame(long timestamp);

		BitmapInfo getWatermarkInfo(long timestamp);
	}
}
