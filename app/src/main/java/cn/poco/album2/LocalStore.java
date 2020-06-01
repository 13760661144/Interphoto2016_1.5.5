package cn.poco.album2;

import android.content.Context;

import java.util.List;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.album2.utils.AlbumUtils;
import poco.photedatabaselib2016.info.InterPhoto;

/**
 * Created by: fwc
 * Date: 2017/8/1
 */
public class LocalStore {

	private static List<ImageStore.ImageInfo> sLocalAlbumList;

	public static void init(Context context) {
		sLocalAlbumList = AlbumUtils.getAllLocalImage(context);
	}

	public static List<ImageStore.ImageInfo> getLocalAlbumList() {
		return sLocalAlbumList;
	}

	public static void deleteInterPhoto(ImageStore.ImageInfo imageInfo) {

		if (imageInfo == null) {
			return;
		}

		if (sLocalAlbumList.contains(imageInfo)) {
			sLocalAlbumList.remove(imageInfo);
		} else {
			ImageStore.ImageInfo info;
			for (int i = 0; i < sLocalAlbumList.size(); i++) {
				info = sLocalAlbumList.get(i);
				if (info.id == imageInfo.id) {
					sLocalAlbumList.remove(info);
					break;
				}
			}
		}
	}

	public static ImageStore.ImageInfo updateInterPhoto(InterPhoto photo) {
		int id = photo.getId();

		for (ImageStore.ImageInfo imageInfo : sLocalAlbumList) {
			if (imageInfo.id == id) {
				imageInfo.image = photo.getFinalUri();
				imageInfo.effect = photo.getPhotoEffect();
				imageInfo.isSaved = photo.getSaved();
				imageInfo.localAlbum = true;
				imageInfo.lastModified = photo.getUpdateDate();
				return imageInfo;
			}
		}
		return null;
	}

	public static ImageStore.ImageInfo changeInterPhoto(InterPhoto photo) {
		ImageStore.ImageInfo imageInfo = new ImageStore.ImageInfo();
		imageInfo.id = photo.getId();
		imageInfo.image = photo.getFinalUri();
		imageInfo.effect = photo.getPhotoEffect();
		imageInfo.isSaved = photo.getSaved();
		imageInfo.localAlbum = true;
		imageInfo.lastModified = photo.getUpdateDate();
		return imageInfo;
	}

	public static ImageStore.ImageInfo addInterPhoto(Context context, InterPhoto photo) {

		ImageStore.ImageInfo imageInfo = changeInterPhoto(photo);

		if (sLocalAlbumList == null) {
			init(context);
		}

		if (sLocalAlbumList != null) {
			sLocalAlbumList.add(0, imageInfo);
		}

		return imageInfo;
	}
}
