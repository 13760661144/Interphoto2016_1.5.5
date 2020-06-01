package cn.poco.album2.view;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;

import cn.poco.album2.model.MemoryInfo;
import cn.poco.album2.utils.SDCardUtils;
import cn.poco.album2.site.AlbumSite;
import cn.poco.album2.utils.AlbumUtils;
import cn.poco.album2.utils.FileUtils;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/3/6
 */
public class CacheView extends FrameLayout implements MemoryInfo.OnInfoChangeListener {

	private Context mContext;

	private CircleProgressView mProgressView;
	private TextView mTotalVolumeView;
	private TextView mScanView;
	private TextView mOptimizeMemoryView;

	private ItemView mInterPhoto;
	private ItemView mLeftMemory;

	private TextView mCalculatingView;

	private GetInterPhotoSizeTask mGetInterPhotoSizeTask;

	private boolean mUiEnable = true;

	private ProgressDialog mProgressDialog;

	private AlbumSite mAlbumSite;

	public CacheView(@NonNull Context context, AlbumSite albumSite) {
		super(context);

		mContext = context;
		mAlbumSite = albumSite;

		initViews();
	}

	private void initViews() {
		LayoutParams params;

		FrameLayout progressLayout = new FrameLayout(mContext);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(220), ShareData.PxToDpi_xhdpi(220));
		params.gravity = Gravity.CENTER_VERTICAL;
		params.leftMargin = ShareData.PxToDpi_xhdpi(104);
		addView(progressLayout, params);
		{
			mTotalVolumeView = new TextView(mContext);
			mTotalVolumeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			mTotalVolumeView.setTextColor(Color.WHITE);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			progressLayout.addView(mTotalVolumeView, params);
			mTotalVolumeView.setVisibility(INVISIBLE);

			mProgressView = new CircleProgressView(mContext);
			params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			progressLayout.addView(mProgressView, params);

			mScanView = new TextView(mContext);
			mScanView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			mScanView.setTextColor(Color.WHITE);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			progressLayout.addView(mScanView, params);
			mScanView.setVisibility(INVISIBLE);
		}

		mOptimizeMemoryView = new TextView(mContext);
		mOptimizeMemoryView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		mOptimizeMemoryView.setTextColor(Color.WHITE);
		mOptimizeMemoryView.setGravity(Gravity.CENTER);
		mOptimizeMemoryView.setBackgroundDrawable(getRectShapeDrawable(0xffffc433));
		mOptimizeMemoryView.setText(R.string.optimize_memory);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(260), ShareData.PxToDpi_xhdpi(54));
		params.gravity = Gravity.BOTTOM;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(93);
		params.leftMargin = ShareData.PxToDpi_xhdpi(394);
		addView(mOptimizeMemoryView, params);
		mOptimizeMemoryView.setAlpha(0.4f);
		mOptimizeMemoryView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mUiEnable) {
					MyBeautyStat.onClickByRes(R.string.照片相册页_进入优化缓存);
					TongJi2.AddCountByRes(mContext, R.integer.优化内存);
					mAlbumSite.openAlbumCachePage(mContext);
				}
			}
		});

		mInterPhoto = new ItemView(mContext);
		mInterPhoto.square.setBackgroundDrawable(getRectShapeDrawable(0xffffc433));
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM;
		params.leftMargin = ShareData.PxToDpi_xhdpi(394);
		params.bottomMargin = ShareData.PxToDpi_xhdpi(234);
		addView(mInterPhoto, params);
		mInterPhoto.setVisibility(INVISIBLE);

		mLeftMemory = new ItemView(mContext);
		mLeftMemory.square.setBackgroundDrawable(getRectShapeDrawable(0xff4d4d4d));
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM;
		params.leftMargin = ShareData.PxToDpi_xhdpi(394);
		params.bottomMargin = ShareData.PxToDpi_xhdpi(187);
		addView(mLeftMemory, params);
		mLeftMemory.setVisibility(INVISIBLE);

		mCalculatingView = new TextView(mContext);
		mCalculatingView.setTextColor(0xffaaaaaa);
		mCalculatingView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
		mCalculatingView.setText(R.string.calculating_cache);
		mCalculatingView.setIncludeFontPadding(false);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(202);
		params.leftMargin = ShareData.PxToDpi_xhdpi(415);
		addView(mCalculatingView, params);
		mCalculatingView.setVisibility(INVISIBLE);

		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setCancelable(false);

		mUiEnable = false;

		MemoryInfo.addOnInfoChangeListener(this);
	}

	private ShapeDrawable getRectShapeDrawable(int color) {
		ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
		shapeDrawable.getPaint().setColor(color);
		return shapeDrawable;
	}

	public void startScan() {
		if (MemoryInfo.sdCardSize == -1 || MemoryInfo.sdAvailableSize == -1 || MemoryInfo.interPhotoSize == -1) {
			mProgressView.startScan();
			mScanView.setVisibility(VISIBLE);
			mScanView.setText(getResources().getString(R.string.has_scan, 0));
			mCalculatingView.setVisibility(VISIBLE);
			mGetInterPhotoSizeTask = new GetInterPhotoSizeTask();
			mGetInterPhotoSizeTask.execute();
		} else {
			mProgressView.stopScan();
			initMemoryInfo(MemoryInfo.sdCardSize, MemoryInfo.sdAvailableSize, MemoryInfo.interPhotoSize);
		}
	}

	private class GetInterPhotoSizeTask extends AsyncTask<Void, Integer, Long> {

		private File mFile;
		private int mTotalFiles = 0;

		@Override
		protected void onPreExecute() {
			FileUtils.init(mContext);
			mFile = new File(FileUtils.PHOTO_DIR);
			String[] paths = mFile.list();
			if (mFile.exists() && paths != null) {
				mTotalFiles = paths.length;
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			int percent = (int)(values[0] * 1f / mTotalFiles);
			mScanView.setText(getResources().getString(R.string.has_scan, percent));
		}

		@Override
		protected Long doInBackground(Void... params) {

			if (AlbumUtils.isDatabaseEmpty(mContext)) {
				FileUtils.delete(FileUtils.PHOTO_DIR);
				mTotalFiles = 0;
			}
			if (mTotalFiles == 0) {
				return 0L;
			}

			for (int i = 0; i < mTotalFiles; i++) {
				publishProgress(i);
			}
			return AlbumUtils.getCacheSize(mContext);
		}

		@Override
		protected void onPostExecute(Long aLong) {

			mProgressView.stopScan();
			long sdCardSize = SDCardUtils.getSDTotalSize(mContext);
			long sdCardAvailableSize = SDCardUtils.getSDCardAvailableSize();

			MemoryInfo.sdCardSize = sdCardSize;
			MemoryInfo.sdAvailableSize = sdCardAvailableSize;
			MemoryInfo.interPhotoSize = aLong;

			initMemoryInfo(sdCardSize, sdCardAvailableSize, aLong);
		}
	}

	private void initMemoryInfo(long sdCardSize, long sdCardAvailableSize, long interPhotoSize) {

		float progress = (interPhotoSize * 1f / sdCardSize) * 100f;
		mInterPhoto.text.setText(getResources().getString(R.string.interphoto_memory,
														  Formatter.formatFileSize(mContext, interPhotoSize)));
		mLeftMemory.text.setText(getResources().getString(R.string.left_memory,
														  Formatter.formatFileSize(mContext, sdCardAvailableSize)));
		mTotalVolumeView.setText(getResources().getString(R.string.total_volume,
														  Formatter.formatFileSize(mContext, sdCardSize)));
		mCalculatingView.setVisibility(INVISIBLE);
		mScanView.setVisibility(INVISIBLE);
		mInterPhoto.setVisibility(VISIBLE);
		mLeftMemory.setVisibility(VISIBLE);
		mTotalVolumeView.setVisibility(VISIBLE);
		if (interPhotoSize > 0) {
			mOptimizeMemoryView.setAlpha(1);
			mUiEnable = true;
		} else {
			mOptimizeMemoryView.setAlpha(0.4f);
			mUiEnable = false;
		}

		ObjectAnimator animator = ObjectAnimator.ofFloat(mProgressView, "progress", 0, progress);
		animator.setDuration(300);
		animator.start();
	}

	@Override
	public void onChange() {
		initMemoryInfo(MemoryInfo.sdCardSize, MemoryInfo.sdAvailableSize, MemoryInfo.interPhotoSize);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mGetInterPhotoSizeTask != null && !mGetInterPhotoSizeTask.isCancelled()) {
			mGetInterPhotoSizeTask.cancel(true);
			mGetInterPhotoSizeTask = null;
		}
	}

	public void onClose() {
		MemoryInfo.removeOnInfoChangeListener(this);
	}
}
