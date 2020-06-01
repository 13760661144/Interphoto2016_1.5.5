package cn.poco.camera.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.Location;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.view.View;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.poco.camera.CameraConfig;
import cn.poco.camera.LocationHelper;
import cn.poco.imagecore.ImageUtils;
import cn.poco.utils.CpuUtils;

/**
 * Created by: fwc
 * Date: 2017/5/17
 */
public class CameraUtils {

	/**
	 * 旋转并裁剪图片
	 * @param data 图片数据
	 * @param hMirror 水平镜像
	 * @param degree 旋转角度
	 * @param ratio 裁剪比例
	 * @param topScale top占预览比例
	 * @param maxSize 最大不能超过maxSize  -1:不限制
	 * @param isThumbnail 是否缩略图
	 * @return Bitmap对象
	 */
	public static Bitmap rotateAndCropPicture(byte[] data, boolean hMirror, int degree, float ratio, float topScale, int maxSize, boolean isThumbnail) {
		if (data == null) {
			return null;
		}
		float maxMem = 0.25f;
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, opt);
		opt.inJustDecodeBounds = false;
		int bigOne = opt.outWidth > opt.outHeight ? opt.outWidth : opt.outHeight;

		float srcRatio = opt.outHeight * 1.0f / opt.outWidth;
		if (srcRatio < 1) {
			srcRatio = 1 / srcRatio;
		}
		if (ratio > srcRatio) {
			bigOne = opt.outWidth > opt.outHeight ? opt.outWidth : opt.outHeight;
			if (maxSize == -1) {
				maxSize = bigOne;
			}
			int sampleSize = bigOne / maxSize;
			if (sampleSize <= 0) {
				sampleSize = 1;
			}
			int cw = opt.outWidth / sampleSize;
			int ch = opt.outHeight / sampleSize;
			int memUse = cw * ch * 4;
			if (memUse > Runtime.getRuntime().maxMemory() * maxMem) {
				bigOne = opt.outWidth > opt.outHeight ? opt.outWidth : opt.outHeight;
			}
		}
		if (maxSize == -1) {
			maxSize = bigOne;
		}
		if (bigOne > maxSize) {
			opt.inSampleSize = bigOne / maxSize;
		}
		if (opt.inSampleSize < 1) {
			opt.inSampleSize = 1;
		}

		//判断MTK CPU，画质减半
		CpuUtils.CpuInfo cpuInfo = CpuUtils.getCpuInfo();
		if (cpuInfo != null && cpuInfo.mHardware != null && cpuInfo.mHardware.contains("MT"))
		{
			opt.inSampleSize *= 2;
		}

		if (isThumbnail) {
			opt.inPreferredConfig = Bitmap.Config.RGB_565;
			opt.inSampleSize = 8;
		} else {
			opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
		}
		Bitmap bitmap = ImageUtils.DecodeJpg(data, opt.inSampleSize);
		if (bitmap == null || bitmap.isRecycled())
			return null;
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		if (hMirror) {
			matrix.postScale(-1, 1);
		}
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		if (bitmap == null || bitmap.isRecycled())
			return null;

		//裁剪
		srcRatio = 1.0f;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int x = 0, y = 0;

		if (height > width) {
			srcRatio = height * 1.0f / width;
			x = 0;
			y = (int) (height * topScale);
			height = (int) (width * ratio);

		} else if (height < width) {
			srcRatio = width * 1.0f / height;
			x = (int) (width * topScale);
			y = 0;
			width = (int) (height * ratio);
		}
		Bitmap target = null;
		if (srcRatio == ratio) {
			target = bitmap;
		} else {
			if (x > bitmap.getWidth()) {
				x = 0;
			}
			if (y > bitmap.getHeight()) {
				y = 0;
			}
			if (x + width > bitmap.getWidth()) {
				width = bitmap.getWidth() - x;
			}
			if (y + height > bitmap.getHeight()) {
				height = bitmap.getHeight() - y;
			}
			target = Bitmap.createBitmap(bitmap, x, y, width, height);
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}
		if (isThumbnail && target != null && !target.isRecycled()) {
			target = ThumbnailUtils.extractThumbnail(target, 96, 96, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		return target;
	}

	/**
	 * 保存拍照图片的exif信息
	 * @param path 图片路径
	 * @param parameters Camera相关参数
	 */
	@SuppressWarnings("all")
	public static void saveExifInfo(String path, Camera.Parameters parameters) {
		float focalLength = 0;//焦距
		int exposureCompensation = 0;//曝光补偿
		if (parameters != null) {
			try {
				focalLength = parameters.getFocalLength();
				exposureCompensation = parameters.getExposureCompensation();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
			exifInterface.setAttribute(ExifInterface.TAG_DATETIME, simpleDateFormat.format(new Date()));
			exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "" + ExifInterface.ORIENTATION_NORMAL);
			exifInterface.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL);//机型
			exifInterface.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, "");//快门速度
			exifInterface.setAttribute(ExifInterface.TAG_APERTURE, "");//光圈
			if (focalLength != 0) {
				exifInterface.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, "" + focalLength);//焦距
			}
			exifInterface.setAttribute(ExifInterface.TAG_ISO, "");//ISO感光度
//            exifInterface.setAttribute(ExifInterface.TAG_WHITE_BALANCE, ""+exposureCompensation);//曝光补偿
			Location mGPSLocation = LocationHelper.getInstance().getLocation();
			if (mGPSLocation != null) {
				double latitude = mGPSLocation.getLatitude();
				double longitude = mGPSLocation.getLongitude();
				exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, locationConvert(latitude));//拍摄地点 纬度
				exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude > 0 ? "N" : "S");
				exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, locationConvert(longitude));//拍摄地点 经度
				exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude > 0 ? "E" : "W");
			}
			exifInterface.saveAttributes();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将位置坐标转换成相应字符串格式
	 * @param info 位置左边
	 * @return 字符串格式
	 */
	private static String locationConvert(double info) {
		info = Math.abs(info);
		String dms = Location.convert(info, Location.FORMAT_SECONDS); // DDD:MM:SS.SSSSS
		String[] splits = dms.split(":");
		String[] seconds = (splits[2]).split("\\.");
		String second;
		if (seconds.length == 0) {
			second = splits[2];
		} else {
			second = seconds[0];
		}
		return splits[0] + "/1," + splits[1] + "/1," + second + "/1";
	}

	/**
	 * 镜头校对拍照
	 *
	 * @param view View对象
	 * @return Bitmap对象
	 */
	public static Bitmap takeScreenShot(View view) {
		if (view == null) {
			return null;
		}
		Bitmap bitmap = null;
		try {
			view.setDrawingCacheEnabled(true);
			view.buildDrawingCache();

			bitmap = view.getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
			if (bitmap != null) {
				int width = bitmap.getWidth() / 4;
				int height = bitmap.getHeight() / 4;
				Bitmap temp = Bitmap.createScaledBitmap(bitmap, width, height, true);
				if (!bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
				bitmap = temp;
			}

			view.setDrawingCacheEnabled(false);
			view.destroyDrawingCache();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 计算距离
	 */
	public static double getDistance(float x1, float y1, float x2, float y2) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * 预览需要旋转的角度
	 * @param cameraId Camera Id
	 */
	public static int getPreviewPatchDegree(int cameraId) {
		if (cameraId == 0) {
			return CameraConfig.getPreviewPatch0();

		} else if (cameraId == 1) {
			return CameraConfig.getPreviewPatch1();
		}
		return 0;
	}

	/**
	 * 镜头修正时保存拍照需要旋转的角度
	 * @param cameraId Camera Id
	 */
	public static int getPicturePatchDegree(int cameraId) {
		if (cameraId == 0) {
			return CameraConfig.getPicturePatch0();

		} else if (cameraId == 1) {
			return CameraConfig.getPicturePatch1();
		}
		return 0;
	}

	/**
	 * 保存照片后需要旋转的角度
	 * @param cameraId Camera Id
	 * @param picturePatchDegree 镜头修正时获取拍照的角度
	 */
	public static int getPictureDegree(int cameraId, int picturePatchDegree) {
		int degree = picturePatchDegree;
		int patchDegree = getPicturePatchDegree(cameraId);

		if (cameraId != 0) {
			degree = (360 - degree) % 360;
			if (patchDegree != 0) {
				degree = (degree - patchDegree + 360) % 360;
			}
			/*
			0   0 0     90
            90  0 270   0
            180 0 180   270
            270 0 90    180
             */
		} else {
			if (patchDegree != 0) {
				degree = (degree + patchDegree) % 360;
			}
		}
		return degree;
	}
}
