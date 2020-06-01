package cn.poco.albumCache;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

import cn.poco.album2.model.MemoryInfo;
import cn.poco.album2.utils.FileUtils;
import cn.poco.album2.utils.SDCardUtils;
import cn.poco.album2.utils.T;
import cn.poco.album2.utils.AlbumUtils;
import cn.poco.albumCache.site.AlbumCacheSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/3/14
 */
public class AlbumCachePage extends IPage {

	private static final String TAG = "清除缓存页";

	private Context mContext;

	private AlbumCacheSite mSite;

	private RelativeLayout mTopLayout;
	private ImageView mBackView;

	private LinearLayout mBottomLayout;

	private TextView mCacheView;
	private TextView mPercentView;

	private TextView mClearView;

	private boolean mUiEnable = true;

	private ProgressDialog mProgressDialog;

	private GetCacheSizeTask mGetCacheSizeTask;

	public AlbumCachePage(Context context, BaseSite site) {
		super(context, site);

		mContext = context;
		mSite = (AlbumCacheSite) site;

		initViews();

		MyBeautyStat.onPageStartByRes(R.string.清除缓存页);
	}

	@Override
	public void SetData(HashMap<String, Object> params) {
		if (MemoryInfo.sdCardSize == -1 || MemoryInfo.sdAvailableSize == -1 || MemoryInfo.interPhotoSize == -1) {
			mGetCacheSizeTask = new GetCacheSizeTask();
			mGetCacheSizeTask.execute();
		} else {
			initMemoryInfo(MemoryInfo.sdCardSize, MemoryInfo.sdAvailableSize, MemoryInfo.interPhotoSize);
		}
	}

	private void initViews() {

		setClickable(true);
		setBackgroundColor(0xff0e0e0e);

		LayoutParams params;

		mTopLayout = new RelativeLayout(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
		addView(mTopLayout, params);
		{
			mBackView = new ImageView(mContext);
			mBackView.setImageResource(R.drawable.framework_back_btn);
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			mTopLayout.addView(mBackView, params1);
			mBackView.setOnClickListener(mOnClickListener);
		}

		TextView interPhoto = new TextView(mContext);
		interPhoto.setIncludeFontPadding(false);
		interPhoto.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		interPhoto.setTextColor(Color.WHITE);
		interPhoto.setText(R.string.interphoto_cache);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.topMargin = ShareData.PxToDpi_xhdpi(312);
		addView(interPhoto, params);

		View line = new View(mContext);
		line.setBackgroundColor(Color.WHITE);
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(50), 1);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.topMargin = ShareData.PxToDpi_xhdpi(393);
		addView(line, params);

		mCacheView = new TextView(mContext);
		mCacheView.setIncludeFontPadding(false);
		mCacheView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 45);
		mCacheView.setTextColor(0xffffc433);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.topMargin = ShareData.PxToDpi_xhdpi(422);
		addView(mCacheView, params);
		mCacheView.setText("0MB");

		mPercentView = new TextView(mContext);
		mPercentView.setIncludeFontPadding(false);
		mPercentView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		mPercentView.setTextColor(0xffaaaaaa);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.topMargin = ShareData.PxToDpi_xhdpi(522);
		addView(mPercentView, params);
		mPercentView.setText(getResources().getString(R.string.interphoto_cache_percent, 0f));

		mClearView = new TextView(mContext);
		mClearView.setIncludeFontPadding(false);
		mClearView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		mClearView.setTextColor(Color.WHITE);
		mClearView.setText(R.string.clear_cache);
		mClearView.setGravity(Gravity.CENTER);
		mClearView.setBackgroundDrawable(getRectShapeDrawable(0xffffc433));
		params = new LayoutParams(ShareData.PxToDpi_xhdpi(400), ShareData.PxToDpi_xhdpi(78));
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.topMargin = ShareData.PxToDpi_xhdpi(646);
		addView(mClearView, params);
		mClearView.setAlpha(0.4f);
		mClearView.setOnClickListener(mOnClickListener);

		mBottomLayout = new LinearLayout(mContext);
		mBottomLayout.setOrientation(LinearLayout.VERTICAL);
		mBottomLayout.setPadding(ShareData.PxToDpi_xhdpi(40), 0, ShareData.PxToDpi_xhdpi(40), 0);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(80);
		addView(mBottomLayout, params);
		{
			LinearLayout.LayoutParams params1;
			TextView tip1 = new TextView(mContext);
			tip1.setIncludeFontPadding(false);
			tip1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			tip1.setTextColor(0xffaaaaaa);
			tip1.setText(R.string.clear_cache_page_tip1);
			params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			mBottomLayout.addView(tip1, params1);

			TextView tip2 = new TextView(mContext);
			tip2.setIncludeFontPadding(false);
			tip2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			tip2.setTextColor(0xffaaaaaa);
			tip2.setText(R.string.clear_cache_page_tip2);
			params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.topMargin = ShareData.PxToDpi_xhdpi(32);
			mBottomLayout.addView(tip2, params1);

			TextView tip3 = new TextView(mContext);
			tip3.setIncludeFontPadding(false);
			tip3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			tip3.setTextColor(0xffaaaaaa);
			tip3.setText(R.string.clear_cache_page_tip3);
			params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.topMargin = ShareData.PxToDpi_xhdpi(32);
			mBottomLayout.addView(tip3, params1);
		}

		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setCancelable(false);

		mUiEnable = false;
	}

	@Override
	public void onBack() {

		MyBeautyStat.onClickByRes(R.string.清除缓存页_退出清除缓存页);
		mSite.onBack(mContext);
	}

	@Override
	public void onClose() {
		if (mGetCacheSizeTask != null && !mGetCacheSizeTask.isCancelled()) {
			mGetCacheSizeTask.cancel(true);
			mGetCacheSizeTask = null;
		}

		MyBeautyStat.onPageEndByRes(R.string.清除缓存页);
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mUiEnable) {
				if (v == mBackView) {
					onBack();
				} else if (v == mClearView){

					if (mClearView.getAlpha() <= 0.99f) {
						return;
					}

					mProgressDialog.setMessage(mContext.getResources().getString(R.string.clearing_cache));
					mProgressDialog.show();
					AlbumUtils.clearHistory(mContext, true, new AlbumUtils.Callback() {
						@Override
						public void onCompleted() {
							((Activity)mContext).runOnUiThread(new Runnable() {
								@Override
								public void run() {
//									long size = SDCardUtils.getFileSize(FileUtils.PHOTO_DIR);
									long sdCardAvailableSize = SDCardUtils.getSDCardAvailableSize();
									long sdCardSize = SDCardUtils.getSDTotalSize(mContext);
									MemoryInfo.sdAvailableSize = sdCardAvailableSize;
									MemoryInfo.interPhotoSize = 0;

									initMemoryInfo(sdCardSize, sdCardAvailableSize, 0);

									MemoryInfo.notifyChange();

									mProgressDialog.dismiss();

									T.showShort(mContext, R.string.clear_cache_completed);
								}
							});
						}
					});

					MyBeautyStat.onClickByRes(R.string.清除缓存页_清除缓存);
				}
			}
		}
	};

	private class GetCacheSizeTask extends AsyncTask<Void, Void, Long> {

		@Override
		protected void onPreExecute() {

			mProgressDialog.setMessage("");
			mProgressDialog.show();

			FileUtils.init(mContext);
		}

		@Override
		protected Long doInBackground(Void... params) {

			if (AlbumUtils.isDatabaseEmpty(mContext)) {
				FileUtils.delete(FileUtils.PHOTO_DIR);
				return 0L;
			}

			return AlbumUtils.getCacheSize(mContext);
		}

		@Override
		protected void onPostExecute(Long aLong) {

			long sdCardSize = SDCardUtils.getSDTotalSize(mContext);
			long sdCardAvailableSize = SDCardUtils.getSDCardAvailableSize();

			MemoryInfo.sdCardSize = sdCardSize;
			MemoryInfo.sdAvailableSize = sdCardAvailableSize;
			MemoryInfo.interPhotoSize = aLong;

			initMemoryInfo(sdCardSize, sdCardAvailableSize, aLong);

			mProgressDialog.dismiss();
		}
	}

	private void initMemoryInfo(long sdCardSize, long sdCardAvailableSize, long interPhotoSize) {
		float percent = (interPhotoSize * 1f / sdCardSize) * 100f;
		if (percent > 100) {
			percent = 100;
		}

		mCacheView.setText(Formatter.formatFileSize(mContext, interPhotoSize));
		mPercentView.setText(getResources().getString(R.string.interphoto_cache_percent, percent));

		if (interPhotoSize > 0) {
			mClearView.setAlpha(1f);
		} else {
			mClearView.setAlpha(0.4f);
		}

		mUiEnable = true;
	}

	private ShapeDrawable getRectShapeDrawable(int color) {
		ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
		shapeDrawable.getPaint().setColor(color);
		return shapeDrawable;
	}
}
