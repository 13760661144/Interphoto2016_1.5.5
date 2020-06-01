package cn.poco.album2.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.album2.PhotoStore;
import cn.poco.album2.model.FolderInfo;
import cn.poco.album2.model.PhotoInfo;
import cn.poco.interphoto2.R;

/**
 * Created by: fwc
 * Date: 2017/8/1
 */
public class PhotoUtils {

	private static final float RATIO_RESTRICT = 5.0f / 16.0f;

	private PhotoUtils() {

	}

	/**
	 * 验证图片是否符合要求
	 *
	 * @param context 上下文
	 * @param path    图片路径
	 * @return 是否符合要求
	 */
	public static boolean validatePhoto(Context context, String path) {

		if (TextUtils.isEmpty(path)) {
			return false;
		}

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);

		if (opts.outWidth > 0 && opts.outHeight > 0) {

			float r = (float)opts.outWidth / (float)opts.outHeight;
			if (r > 1) r = 1 / r;
			if (r < RATIO_RESTRICT) {
				Toast toast = Toast.makeText(context, context.getResources().getString(R.string.notSupportedTips), Toast.LENGTH_SHORT);
				toast.show();
				toast.setGravity(Gravity.CENTER, 0, 0);
				return false;
			}
		} else if (!new File(path).exists()) {
			Toast toast = Toast.makeText(context, context.getResources().getString(R.string.InvalidImgeTips), Toast.LENGTH_SHORT);
			toast.show();
			toast.setGravity(Gravity.CENTER, 0, 0);
			return false;
		}

		return true;
	}

	/**
	 * 删除图片
	 * @param context 上下文
	 * @param infos 图片信息列表
	 */
	public static void deletePhotos(Context context, List<ImageStore.ImageInfo> infos) {
		for (ImageStore.ImageInfo imageInfo : infos) {
			deletePhoto(context, imageInfo);
		}
	}

	/**
	 * 删除图片
	 * @param context 上下文
	 * @param info 图片信息
	 */
	public static void deletePhoto(Context context, ImageStore.ImageInfo info) {

		PhotoStore photoStore = PhotoStore.getInstance(context);

		String where = MediaStore.Images.Media.DATA + "='" + info.image + "'";
		ContentResolver cr = context.getContentResolver();
		if (cr != null) {
			try {
				cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);

				FolderInfo folderInfo = null;
				for (FolderInfo temp : photoStore.getFolderInfos()) {
					if (temp.getName().equals(info.folder)) {
						folderInfo = temp;
						break;
					}
				}

				if (folderInfo == null || folderInfo.getCount() <= 0) {
					return;
				}

				folderInfo.subCount();

				// 系统相册
				FolderInfo albums = photoStore.getFolderInfo(0);
				albums.subCount();
				if (albums.getCount() <= 0) {
					photoStore.clearCache();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static List<ImageStore.ImageInfo> change(List<PhotoInfo> infoList) {
		List<ImageStore.ImageInfo> result = new ArrayList<>();

		if (infoList == null || infoList.isEmpty()) {
			return result;
		}

		for (PhotoInfo info : infoList) {
			result.add(change(info));
		}

		return result;
	}

	public static ImageStore.ImageInfo change(PhotoInfo info) {
		ImageStore.ImageInfo imageInfo = new ImageStore.ImageInfo();
		imageInfo.id = info.getId();
		imageInfo.image = info.getImagePath();
		imageInfo.folder = info.getFolderName();
		imageInfo.fileSize = info.getSize();
		imageInfo.rotation = info.getRotation();
		imageInfo.lastModified = info.getLastModified();

		return imageInfo;
	}

	public static ImageStore.ImageInfo change(String imagePath) {

		ImageStore.ImageInfo imageInfo = new ImageStore.ImageInfo();
		imageInfo.id = -1;
		imageInfo.image = imagePath;

		return imageInfo;
	}
}
