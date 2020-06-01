package cn.poco.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import cn.poco.beautify.BeautifyHandler;
import cn.poco.camera.utils.CameraUtils;
import cn.poco.imagecore.ImageUtils;
import cn.poco.resource.FilterRes;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.utils.Utils;

/**
 * Created by: fwc
 * Date: 2017/6/9
 */
public class Camera2Handler extends Handler {

	public static final int MSG_SAVE_IMAGE = 10;
	public static final int MSG_IMAGE_THUMB = 11; // 缩略图

	private Handler mUiHandler;

	public Camera2Handler(Looper looper, Handler uiHandler) {
		super(looper);

		mUiHandler = uiHandler;
	}

	@Override
	public void handleMessage(Message msg) {

		switch (msg.what) {
			case MSG_SAVE_IMAGE: {
				Params params = (Params)msg.obj;
				msg.obj = null;

				if (!params.hideThumb) {
					// 生成缩略图
					Bitmap thumb = CameraUtils.rotateAndCropPicture(params.data, params.cameraWrapper.isFront(),
																	params.degree, params.ratio, params.topScale, 1024, true);
					if (params.filterRes != null) {
						thumb = BeautifyHandler.AddFilter(params.context, thumb, params.filterRes, params.alpha);
					}
					params.thumb = thumb;
					Message message = mUiHandler.obtainMessage();
					message.what = MSG_IMAGE_THUMB;
					message.obj = params;
					mUiHandler.sendMessage(message);
				}

				Bitmap bitmap = rotateAndCropPicture(params.data, params.cameraWrapper.isFront(), params.degree,
														  params.ratio, params.topScale, params.maxSize);
				params.data = null;
				if (bitmap != null) {

					if (params.filterRes != null) {
						bitmap = BeautifyHandler.AddFilter(params.context, bitmap, params.filterRes, params.alpha);
					}

					boolean addDate = SettingInfoMgr.GetSettingInfo(params.context).GetAddDateState();
					if (addDate) {
						Utils.attachDate(bitmap);
					}

					byte[] pic = ImageUtils.JpgEncode(bitmap, params.quality);

					//保存到相册
					final ImageFile2 imageFile2 = new ImageFile2();
					imageFile2.SetData(params.context, pic, 0, 0, -1);
					if (params.isSave) {
						imageFile2.SaveImg2(params.context);
						if (imageFile2.m_finalOrgPath != null) {
							saveExifInfo(params.cameraWrapper, imageFile2.m_finalOrgPath);
							params.imageFile = imageFile2;
						}
					} else {
						params.imageFile = imageFile2;
					}
				}

				Message message = mUiHandler.obtainMessage();
				message.what = MSG_SAVE_IMAGE;
				message.obj = params;
				mUiHandler.sendMessage(message);
				break;
			}
		}
	}

//	private static byte[] rotateAndCropPicture2byteArr(byte[] data, boolean hMirror, int degree, float ratio, float topScale, int maxSize, int quality) {
//		Bitmap target = rotateAndCropPicture(data, hMirror, degree, ratio, topScale, maxSize);
//		if (target == null || target.isRecycled()) return null;
//		return ImageUtils.JpgEncode(target, quality);
//	}

	/**
	 * 旋转并裁剪图片
	 */
	public static Bitmap rotateAndCropPicture(byte[] data, boolean hMirror, int degree, float ratio, float topScale, int maxSize) {
		return CameraUtils.rotateAndCropPicture(data, hMirror, degree, ratio, topScale, maxSize, false);
	}

	/**
	 * 保存拍照图片的exif信息
	 *
	 * @param path 图片路径
	 */
	private void saveExifInfo(CameraWrapper cameraWrapper, String path) {
		if (cameraWrapper == null) return;
		Camera.Parameters parameters = cameraWrapper.getCameraParameters();
		CameraUtils.saveExifInfo(path, parameters);
	}

	public static class Params {

		// in
		public Context context;
		public CameraWrapper cameraWrapper;
		public byte[] data;
		public int degree;
		public float ratio;
		public float topScale;
		public int maxSize;
		public int quality;
		public boolean isSave;
		public FilterRes filterRes;
		public int alpha;
		public boolean hideThumb;

		// out
		public ImageFile2 imageFile;
		public Bitmap thumb;
	}
}
