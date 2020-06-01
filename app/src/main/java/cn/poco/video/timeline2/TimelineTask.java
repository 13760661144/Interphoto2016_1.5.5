package cn.poco.video.timeline2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.poco.tianutils.ShareData;
import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2017/11/15
 */
public class TimelineTask implements Runnable {

	public static final String EMPTY_PATH = "empty";

	private MediaMetadataRetriever mRetriever;

	private String mVideoPath;
	private int mIndex;
	private long mPosition;

	private OnGetFrameListener mOnGetFrameListener;

	public TimelineTask(MediaMetadataRetriever retriever, String videoPath, int index, long position) {
		mRetriever = retriever;
		mVideoPath = videoPath;
		mIndex = index;
		mPosition = position;
	}

	@Override
	public void run() {
		if (!Thread.currentThread().isInterrupted()) {
			String dir = FileUtils.getVideoFrameDir(mVideoPath);
			String framePath = dir + File.separator + "video_" + mIndex + ".img";
			File file = new File(framePath);
			if (!file.exists()) {
				Bitmap bitmap = mRetriever.getFrameAtTime(mPosition * 1000);
//				bitmap = scaleBitmap(bitmap);
				saveBitmap(bitmap, file);
			}

			if (mOnGetFrameListener != null) {
				mOnGetFrameListener.onFrameGet(mVideoPath, mIndex, file.getAbsolutePath());
			}
		}
	}

	private void saveBitmap(Bitmap bitmap, File file) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.close(fos);
		}
	}

	public void setOnGetFrameListener(OnGetFrameListener listener) {
		mOnGetFrameListener = listener;
	}

	public void release() {
		mRetriever = null;
		mOnGetFrameListener = null;
	}

	private Bitmap scaleBitmap(Bitmap bitmap) {

		final int frameWidth = ShareData.PxToDpi_xhdpi(40);
		final int frameHeight = ShareData.PxToDpi_xhdpi(112);

		Bitmap result = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Matrix matrix = new Matrix();
		final float width = bitmap.getWidth();
		final float height = bitmap.getHeight();

		final float ratioW = frameWidth / width;
		final float ratioH = frameHeight / height;

		if (ratioW < ratioH) {
			matrix.setScale(ratioH, ratioH);
			final float dx = (frameWidth - width * ratioH) / 2f;
			matrix.postTranslate(dx, 0);
		} else {
			matrix.setScale(ratioW, ratioW);
			final float dy = (frameHeight - height * ratioW) / 2f;
			matrix.postTranslate(0, dy);
		}

		canvas.drawBitmap(bitmap, matrix, null);
		bitmap.recycle();

		return result;
	}

	public interface OnGetFrameListener {
		void onFrameGet(String videoPath, int index, String framePath);
	}
}
