package cn.poco.video.save.watermark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

/**
 * Created by: fwc
 * Date: 2017/5/26
 */
public class BitmapLoader {

	private final Object mLock = new Object();

	private Context mContext;
	private int[] mDrawableResIds;
	private int mCurrentIndex;

	private Bitmap mCurrentBitmap;
	private Bitmap mLastBitmap;

	private boolean mQuit = false;

	private boolean mAvailable = false;

	private boolean mRunning = false;

	private int mGetCount = 0;

	public BitmapLoader(Context context) {
		mContext = context;
	}

	/**
	 * 设置数据
	 *
	 * @param drawableResIds 资源id
	 */
	public void setData(int... drawableResIds) {
		mDrawableResIds = drawableResIds;
		reset();
	}

	/**
	 * 开始加载
	 */
	public void start() {

		if (mDrawableResIds == null) {
			throw new IllegalStateException("please invoke setData() before.");
		}
		if (mRunning) {
			synchronized (mLock) {
				reset();
				mLock.notifyAll();
			}
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					mRunning = true;
					while (!mQuit) {
						synchronized (mLock) {
							if (mAvailable) {
								try {
									mLock.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}

						if (mCurrentIndex < mDrawableResIds.length) {
							BitmapFactory.Options options = new BitmapFactory.Options();
							options.inJustDecodeBounds = true;
							options.inMutable = true;
							options.inSampleSize = 1;
							BitmapFactory.decodeResource(mContext.getResources(), mDrawableResIds[mCurrentIndex], options);

							if (mLastBitmap != null && canUseForInBitmap(mLastBitmap, options)) {
								options.inBitmap = mLastBitmap;
							}

							options.inJustDecodeBounds = false;

							synchronized (mLock) {
								mLastBitmap = mCurrentBitmap;
								mCurrentBitmap = BitmapFactory.decodeResource(mContext.getResources(), mDrawableResIds[mCurrentIndex], options);
								mCurrentIndex++;
								mAvailable = true;
								mLock.notify();
							}
						}
					}
					mRunning = false;
				}
			}).start();
		}
	}

	/**
	 * 重置状态
	 */
	private void reset() {
		mGetCount = 0;
		mCurrentIndex = 0;
		mAvailable = false;
	}

	/**
	 * 判断是否结束
	 *
	 * @return true: 获取结束
	 */
	public boolean isFinish() {
		return mGetCount == mDrawableResIds.length;
	}

	/**
	 * 获取下一张Bitmap
	 *
	 * @return Bitmap对象
	 */
	public Bitmap getNext() {
		synchronized (mLock) {

			if (!mAvailable) {
				try {
					mLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		Bitmap bitmap;
		synchronized (mLock) {
			bitmap = mCurrentBitmap;
			mAvailable = false;
			mLock.notify();
			mGetCount++;
		}

		return bitmap;
	}

	/**
	 * 释放资源
	 */
	public void release() {
		if (!mQuit) {
			synchronized (mLock) {
				mQuit = true;
				mLock.notifyAll();
			}

			reset();
		}
	}

	private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int width = targetOptions.outWidth / targetOptions.inSampleSize;
			int height = targetOptions.outHeight / targetOptions.inSampleSize;
			int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
			return byteCount <= candidate.getAllocationByteCount();
		}
		return candidate.getWidth() == targetOptions.outWidth && candidate.getHeight() == targetOptions.outHeight && targetOptions.inSampleSize == 1;
	}

	private static int getBytesPerPixel(Bitmap.Config config) {
		if (config == Bitmap.Config.ARGB_8888) {
			return 4;
		} else if (config == Bitmap.Config.RGB_565) {
			return 2;
		} else if (config == Bitmap.Config.ARGB_4444) {
			return 2;
		} else if (config == Bitmap.Config.ALPHA_8) {
			return 1;
		}
		return 1;
	}
}
