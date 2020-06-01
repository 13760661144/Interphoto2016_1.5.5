package cn.poco.PhotoPicker;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import cn.poco.interphoto2.R;
import cn.poco.system.SysConfig;
import cn.poco.tianutils.CommonUtils;
import cn.poco.utils.JniUtils;
import cn.poco.utils.Utils;

public class ImageStore {
	public static class ImageInfo{
		public ImageInfo() {
		}

		public String image;
		public String thumb;
		public int rotation;
		public long fileSize;
		public String folder;
		public byte[] bytes;
		public int id;
		public long lastModified;
		public int selCount = 0;
		public double longitude;
		public double latitude;
		public boolean selected;
		public boolean deleted = false;
		public boolean isVideo = false;
		public ExtraInfo extraInfo = new ExtraInfo();
		public CloudInfo cloudInfo = new CloudInfo();

		/**
		 * 标记是否已保存
		 */
		public boolean isSaved;

		/**
		 * 保存的效果
		 */
		public String effect;

		/**
		 * 标记是否为本地相册的
		 */
		public boolean localAlbum = false;

		public ImageInfo clone() {
			ImageInfo img = new ImageInfo();
			img.image = image;
			img.thumb = thumb;
			img.rotation = rotation;
			img.fileSize = fileSize;
			img.folder = folder;
			img.bytes = bytes;
			img.id = id;
			img.deleted = deleted;
			img.lastModified = lastModified;
			img.selCount = selCount;
			img.longitude = longitude;
			img.latitude = latitude;
			img.selected = selected;
			img.isVideo = isVideo;
			img.extraInfo = extraInfo;
			img.isSaved = isSaved;
			img.effect = effect;
			img.localAlbum = localAlbum;
			return img;
		}
	}
	
	public static class CloudInfo
	{
		public String infoXml;
		public String infoXmlId;
		public String imgId;
		public String img;
		public int server;
		public long uptime;
	}

	public static class ExtraInfo {
		public boolean isRead;
		public boolean favorite;
		public String address;
		public String describe;
		public String sound;
		public String[] tags;
	}

	public static class FolderInfo {
		public String folder;
		public int totalImg;
		public int totalEnc;
		public ArrayList<ImageInfo> imgs = new ArrayList<ImageInfo>();
		public long lastModified;
	};

	public static class SiteInfo {
		public String site;
		public ArrayList<ImageInfo> imgs = new ArrayList<ImageInfo>();
		public long lastModified;
	};

	private static class ThumbInfo {
		public int id;
		public String thumb;
	};

	public interface ProgressListener {
		void onProgress(int size, int index);
	}

	private static ArrayList<ImageInfo> sImgs;
	private static ArrayList<ImageInfo> sCachedImgs = new ArrayList<ImageInfo>();
	private static ArrayList<ThumbInfo> sThumbs = new ArrayList<ThumbInfo>();
	private static ArrayList<FolderInfo> sFolders;
	private static int sThumbSize = 150;
	private static long sMemoryLimit = Runtime.getRuntime().maxMemory() / 4;
	private static long sCacheUsedMemory = 0;
	private static final String CACHE_PATH = "/.POCO/Thumbs";
	private static String sCachePath;
	private static int sQuality = 85;
	private static boolean sCheckedRotate = false;
	private static boolean sNeedRotateThumb = false;
	public static boolean sUpdated = false;

	public static ArrayList<ImageInfo> getImages(Context context) {
		if (sImgs != null && sUpdated == true)
			return sImgs;

		String columns[] = { Media.DATA, Media.BUCKET_DISPLAY_NAME, Media.DATE_MODIFIED, Media.ORIENTATION, Media.LATITUDE, Media.LONGITUDE, Media.SIZE, Media._ID };
		ContentResolver contentResolver = null;
		Cursor cursor = null;
		try {
			contentResolver = context.getContentResolver();
			if (contentResolver != null) {
				cursor = contentResolver.query(Media.EXTERNAL_CONTENT_URI, columns, null, null, null);
			}
		} catch (SQLiteException e) {
            e.printStackTrace();
		} catch (Exception e) {
            e.printStackTrace();
        }
		if (cursor != null) {
			int pos1 = -1, pos2 = -1;
			ArrayList<ImageInfo> imgs = new ArrayList<ImageInfo>();
			if (cursor.moveToFirst()) {
				int columnsIndex[] = new int[columns.length];
				for (int i = 0; i < columns.length; i++) {
					columnsIndex[i] = cursor.getColumnIndex(columns[i]);
				}
				int c0 = columnsIndex[0];
				int c1 = columnsIndex[1];
				int c2 = columnsIndex[2];
				int c3 = columnsIndex[3];
				int c4 = columnsIndex[4];
				int c5 = columnsIndex[5];
				int c6 = columnsIndex[6];
				int c7 = columnsIndex[7];

				ImageInfo imgInfo = null;
				do {
					imgInfo = new ImageInfo();
					imgInfo.image = cursor.getString(c0);
					imgInfo.folder = cursor.getString(c1);
					imgInfo.lastModified = cursor.getLong(c2);
					imgInfo.rotation = cursor.getInt(c3);
					imgInfo.latitude = cursor.getDouble(c4);
					imgInfo.longitude = cursor.getDouble(c5);
					imgInfo.fileSize = cursor.getLong(c6);
					imgInfo.id = cursor.getInt(c7);
					if (imgInfo.folder == null) {
						if (imgInfo.image != null) {
							pos2 = imgInfo.image.lastIndexOf('/');
							if (pos2 != -1) {
								pos1 = imgInfo.image.lastIndexOf('/', pos2 - 1);
							}
							if (pos1 != -1 && pos2 != -1 && pos1 < pos2) {
								imgInfo.folder = imgInfo.image.substring(pos1 + 1, pos2);
							}
						}
					}
					
					if (imgInfo.folder != null 
							&& imgInfo.image != null 
							&& JniUtils.isFileExist(imgInfo.image))
					{
						imgs.add(imgInfo);
					}
				} while (cursor.moveToNext());
				cursor.close();

				/*// 视频
				columns = new String[] { Video.Media.DATA, Video.Media.BUCKET_DISPLAY_NAME, Video.Media.DATE_MODIFIED, Video.Media.ALBUM, Video.Media.LATITUDE, Video.Media.LONGITUDE, Video.Media.SIZE, Video.Media._ID };
				cursor = null;
				try {
					if (cr != null) {
						cursor = cr.query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null, null, null);
					}
				} catch (SQLiteException e) {
				}
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						columnsIndex = new int[columns.length];
						for (int i = 0; i < columns.length; i++) {
							columnsIndex[i] = cursor.getColumnIndex(columns[i]);
						}

						do {
							imgInfo = new ImageInfo();
							imgInfo.isVideo = true;
							imgInfo.image = cursor.getString(columnsIndex[0]);
							imgInfo.folder = cursor.getString(columnsIndex[1]);
							imgInfo.lastModified = cursor.getLong(columnsIndex[2]);
							imgInfo.latitude = cursor.getDouble(columnsIndex[4]);
							imgInfo.longitude = cursor.getDouble(columnsIndex[5]);
							imgInfo.fileSize = cursor.getLong(columnsIndex[6]);
							imgInfo.id = cursor.getInt(columnsIndex[7]);
							if (imgInfo.folder == null) {
								if (imgInfo.image != null) {
									pos2 = imgInfo.image.lastIndexOf('/');
									if (pos2 != -1) {
										pos1 = imgInfo.image.lastIndexOf('/', pos2 - 1);
									}
									if (pos1 != -1 && pos2 != -1 && pos1 < pos2) {
										imgInfo.folder = imgInfo.image.substring(pos1 + 1, pos2);
									}
								}
							}
							if (imgInfo.folder != null && imgInfo.image != null) {
								imgs.add(imgInfo);
							}
						} while (cursor.moveToNext());
					}
					cursor.close();
				}*/

				ImageInfo[] aimgs = imgs.toArray(new ImageInfo[imgs.size()]);
				int size = aimgs.length;
				Arrays.sort(aimgs, new Comparator<ImageInfo>() {
					@Override
					public int compare(ImageInfo object1, ImageInfo object2) {
						if (object1.lastModified == object2.lastModified)
							return 0;
						return object1.lastModified < object2.lastModified ? 1 : -1;
					}
				});
				imgs.clear();
				for (int i = 0; i < aimgs.length; i++) {
					imgs.add(aimgs[i]);
				}
				
				ArrayList<FolderInfo> folders = new ArrayList<FolderInfo>();
				FolderInfo folderInfo = null;
				HashMap<String, FolderInfo> groups = new HashMap<String, FolderInfo>();
				String curFolder = null;
				int indexOfCamera = 0;
				for (int i = 0; i < size; i++) {
					imgInfo = aimgs[i];
					if (curFolder == null || curFolder.equals(imgInfo.folder) == false) {
						folderInfo = groups.get(imgInfo.folder);
						if (folderInfo == null) {
							folderInfo = new FolderInfo();
							folderInfo.folder = imgInfo.folder;
							folderInfo.totalEnc = 0;
							folderInfo.totalImg = 0;
							folderInfo.lastModified = imgInfo.lastModified;
							groups.put(imgInfo.folder, folderInfo);
							folders.add(folderInfo);
							if (folderInfo.folder.equals("Camera")) {
								indexOfCamera = folders.size() - 1;
							}
						}
						curFolder = imgInfo.folder;
					}
					folderInfo.totalImg++;
					folderInfo.imgs.add(imgInfo);
				}
				if (indexOfCamera > 0) {
					folderInfo = folders.get(indexOfCamera);
					folders.remove(indexOfCamera);
					folders.add(0, folderInfo);
				}
				sImgs = imgs;
				sFolders = folders;
				new Thread(new Runnable() {
					@Override
					public void run() {
						clearUnavaliableThumbs();
					}
				}).start();
			}

		}
		columns = new String[] { Images.Thumbnails.DATA, Images.Thumbnails.IMAGE_ID };
		cursor = null;
		try {
			contentResolver = context.getContentResolver();
			if (contentResolver != null) {
				cursor = contentResolver.query(Images.Thumbnails.EXTERNAL_CONTENT_URI, columns, null, null, null);
			}
		} catch (SQLiteException e) {
		}
		ThumbInfo thumb;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int columnsIndex[] = new int[columns.length];
				for (int i = 0; i < columns.length; i++) {
					columnsIndex[i] = cursor.getColumnIndex(columns[i]);
				}

				do {
					thumb = new ThumbInfo();
					thumb.thumb = cursor.getString(columnsIndex[0]);
					thumb.id = cursor.getInt(columnsIndex[1]);
					sThumbs.add(thumb);
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		if(sImgs != null)
		{
			sUpdated = true;
		}
		return sImgs;
	}


	public static ArrayList<ImageInfo> getImages(Context context, String folder) {
		return getImages(context, folder, 0);
	}

	public static ArrayList<ImageInfo> getImages(Context context, String folder,int sizeLimit) {
		ImageInfo imgInfo = null;
		ArrayList<ImageInfo> imgs = getImages(context);
		if (imgs != null && folder == null) {
			ArrayList<ImageInfo> outImgs = new ArrayList<ImageInfo>();
			int length = imgs.size();
			for (int j = 0; j < length; j++) {
				imgInfo = imgs.get(j);
				if (imgInfo != null && (imgInfo.fileSize > sizeLimit || imgInfo.fileSize == 0)) {
					outImgs.add(imgInfo);
				}
			}
			return outImgs;
		}
		if (null != imgs && sFolders != null) {
			FolderInfo info = null;
			int size = sFolders.size();
			for (int i = 0; i < size; i++) {
				info = sFolders.get(i);
				if (info != null && info.folder.equals(folder)) {
					ArrayList<ImageInfo> outImgs = new ArrayList<ImageInfo>();
					int length = info.imgs.size();
					for (int j = 0; j < length; j++) {
						imgInfo = info.imgs.get(j);
						if (imgInfo != null && (imgInfo.fileSize > sizeLimit || imgInfo.fileSize == 0)) {
							outImgs.add(imgInfo);
						}
					}
					return outImgs;
				}
			}
		}
		return null;
	}

	public static ArrayList<ImageInfo> getImages(Context context, String[] folders) {
		return getImages(context, folders, 0);
	}

	public static ArrayList<ImageInfo> getImages(Context context, String[] folders, int sizeLimit) {
		if (folders != null && folders.length == 1) {
			return getImages(context, folders[0]);
		}
		ImageInfo imgInfo = null;
		ArrayList<ImageInfo> imgs = getImages(context);
		if (imgs != null && folders == null) {
			int size = imgs.size();
			ArrayList<ImageInfo> outImgs = new ArrayList<ImageInfo>();
			for (int i = 0; i < size; i++) {
				imgInfo = imgs.get(i);
				if ((imgInfo.fileSize > sizeLimit || imgInfo.fileSize == 0)) {
					outImgs.add(imgInfo);
				}
			}
			return outImgs;
		}
		if (null != imgs && sFolders != null) {
			ArrayList<ImageInfo> outImgs = new ArrayList<ImageInfo>();
			FolderInfo info = null;
			int size = sFolders.size();
			for (int i = 0; i < size; i++) {
				info = sFolders.get(i);
				if (info != null) {
					for (String folder : folders) {
						if (info.folder.equals(folder)) {
							int length = info.imgs.size();
							for (int j = 0; j < length; j++) {
								imgInfo = info.imgs.get(j);
								if (imgInfo != null && (imgInfo.fileSize > sizeLimit || imgInfo.fileSize == 0)) {
									outImgs.add(imgInfo);
								}
							}
						}
					}
				}
			}
			return outImgs;
		}
		return null;
	}
	
	public static boolean isCached()
	{
		return sImgs != null;
	}

	public static ImageInfo getImage(String img) {
		ImageInfo info = null;
		if (sImgs != null) {
			int size = sImgs.size();
			for (int i = 0; i < size; i++) {
				info = sImgs.get(i);
				if (info.image.equals(img))
					return info;
			}
		}
		return null;
	}

	public static ImageInfo getImage(int id) {
		ImageInfo info = null;
		if (sImgs != null) {
			int size = sImgs.size();
			for (int i = 0; i < size; i++) {
				info = sImgs.get(i);
				if (info.id == id)
					return info;
			}
		}
		return null;
	}

	public static int getSelCount() {
		int count = 0;
		ImageInfo img = null;
		if (sImgs != null) {
			int size = sImgs.size();
			for (int i = 0; i < size; i++) {
				img = sImgs.get(i);
				if (img.selected == true) {
					count++;
				}
			}
		}
		return count;
	}

	public static ImageInfo[] getSelImgs() {
		ImageInfo[] imgs;
		ArrayList<ImageInfo> o = new ArrayList<ImageInfo>();
		ImageInfo img;
		ArrayList<ImageInfo> a = sImgs;
		if (a != null) {
			imgs = a.toArray(new ImageInfo[a.size()]);
			for (int i = 0; i < imgs.length; i++) {
				img = imgs[i];
				if (img.selected == true) {
					o.add(img);
				}
			}
		}
		if (o.size() > 0) {
			imgs = o.toArray(new ImageInfo[o.size()]);
			return imgs;
		}
		return null;
	}

	public static void deleteSelImgs(Context context, ProgressListener progressCb) {
		FolderInfo info;
		ImageInfo img = null;
		int count = getSelCount();
		int index = 0;
		int size = sImgs.size();
		for (int i = 0; i < size; i++) {
			img = sImgs.get(i);
			if (img.selected == true) {
				img.selected = false;
				new File(img.image).delete();
				if (img.extraInfo.sound != null && img.extraInfo.sound.length() > 0) {
					new File(img.extraInfo.sound).delete();
				}
				img.deleted = true;
				synchronized (sCachedImgs) {
					sCachedImgs.remove(img);
				}
				int folderSize = sFolders.size();
				for (int j = 0; j < folderSize; j++) {
					info = sFolders.get(j);
					if (info.folder != null && info.folder.equals(img.folder)) {
						info.imgs.remove(img);
						info.totalImg --;
						break;
					}
				}
				if(img.isVideo == false)
				{
					String where = MediaStore.Images.Media.DATA + "='" + img.image + "'";
					ContentResolver cr = context.getContentResolver();
					if (cr != null) {
						try
						{
							cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
						}
						catch(Exception e)
						{}
					}
				}
				else
				{
					String where = MediaStore.Video.Media.DATA + "='" + img.image + "'";
					ContentResolver cr = context.getContentResolver();
					if (cr != null) {
						try
						{
							cr.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, where, null);
						}
						catch(Exception e)
						{}
					}
				}
				sImgs.remove(i);

				i--;
				size--;
				index++;
				if (progressCb != null) {
					progressCb.onProgress(count, index);
				}
			}
		}
	}

	public static void deleteImage(Context context, String img) {
		if (sImgs != null) {
			for (ImageInfo imgInfo : sImgs) {
				if (imgInfo.image == img) {
					deleteImage(context, imgInfo);
					break;
				}
			}
		}
	}

	public static void deleteImage(Context context, ImageInfo img) {
		if (context != null && img != null) {
			new File(img.image).delete();
			if (img.extraInfo.sound != null && img.extraInfo.sound.length() > 0) {
				new File(img.extraInfo.sound).delete();
			}
			img.deleted = true;
			synchronized (sCachedImgs) {
				sCachedImgs.remove(img);
			}
			int folderSize = sFolders.size();
			FolderInfo info;
			String albums = context.getResources().getString(R.string.albums);
			for (int j = 0; j < folderSize; j++) {
				info = sFolders.get(j);
				if (info.folder != null && info.folder.equals(img.folder)) {
					info.imgs.remove(img);
					info.totalImg--;
					if (info.totalImg == 0 && !info.folder.equals(albums)) {
						sFolders.remove(info);
					}
					break;
				}
			}
			sImgs.remove(img);
			if(img.isVideo == false)
			{
				String where = MediaStore.Images.Media.DATA + "='" + img.image + "'";
				ContentResolver cr = context.getContentResolver();
				if (cr != null) {
					try
					{
						cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
					}
					catch(Exception e)
					{}
				}
			}
			else
			{
				String where = MediaStore.Video.Media.DATA + "='" + img.image + "'";
				ContentResolver cr = context.getContentResolver();
				if (cr != null) {
					try
					{
						cr.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, where, null);
					}
					catch(Exception e)
					{}
				}
			}
		}
	}

	public static boolean moveImage(Context context, ImageInfo img, String folder) {
		if (img != null && img.image != null && folder != null) {
			String srcFolder = img.image.substring(0, img.image.lastIndexOf("/"));
			String srcName = img.image.substring(img.image.lastIndexOf("/")+1);
			if (folder.charAt(folder.length() - 1) == '/') {
				folder = folder.substring(0, folder.length() - 1);
			}
			if (srcFolder.equals(folder)) {
				return true;
			}
			String dstImage = folder+"/"+srcName;
			int dot = srcName.lastIndexOf('.');
			int index = 1;
			while(new File(dstImage).exists())
			{
				if(dot != -1)
				{
					dstImage = folder+"/"+srcName.replace(".", "("+index+").");
				}
				else
				{
					dstImage = folder+"/"+srcName + "("+index+")";
				}
				if(index > 20)
					return false;
				index++;
			}
			try {
				FileUtils.copyFile(new File(img.image), new File(dstImage));
				//dstImage = Utils.copyFileTo(img.image, dstImage);
				if (dstImage != null) {
					// 修改时间
					if(new File(dstImage).setLastModified(img.lastModified * 1000) == false)
					{
						img.lastModified = new Date().getTime()/1000;
					}
					// 从源文件夹图片列表移除该图,并添加到新文件夹图片列表中
					String srcFolderName = srcFolder.substring(srcFolder.lastIndexOf("/") + 1);
					String dstFolderName = folder.substring(folder.lastIndexOf("/") + 1);
					FolderInfo folderInfo = null;
					int size = sFolders.size();
					for (int i = 0; i < size; i++) {
						folderInfo = sFolders.get(i);
						if (folderInfo.folder.equals(srcFolderName)) {
							folderInfo.imgs.remove(img);
							folderInfo.totalImg--;
						} else if (folderInfo.folder.equals(dstFolderName)) {
							folderInfo.imgs.add(img);
							folderInfo.totalImg++;
						}
					}
					// 删除源图片
					new File(img.image).delete();
					// 从系统图库删除
					if(img.isVideo == false)
					{
						String where = MediaStore.Images.Media.DATA + "='" + img.image + "'";
						ContentResolver cr = context.getContentResolver();
						if (cr != null) {
							try
							{
								cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
							}
							catch(Exception e)
							{}
						}
					}
					else
					{
						String where = MediaStore.Video.Media.DATA + "='" + img.image + "'";
						ContentResolver cr = context.getContentResolver();
						if (cr != null) {
							try
							{
								cr.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, where, null);
							}
							catch(Exception e)
							{}
						}
					}
					// 更新图片
					img.image = dstImage;
					img.folder = dstFolderName;
					// 添加到系统图库
					insertToSys(context, img);
					return true;
				}
			} catch (Exception e) {
			}
		}
		return false;
	}

	public static boolean copyImage(Context context, ImageInfo img, String folder) {
		if (img != null && img.image != null && folder != null) {
			String srcName = img.image.substring(img.image.lastIndexOf("/")+1);
			if (folder.charAt(folder.length() - 1) == '/') {
				folder = folder.substring(0, folder.length() - 1);
			}
			String dstImage = folder+"/"+srcName;
			int dot = srcName.lastIndexOf('.');
			int index = 1;
			while(new File(dstImage).exists())
			{
				if(dot != -1)
				{
					dstImage = folder+"/"+srcName.replace(".", "("+index+").");
				}
				else
				{
					dstImage = folder+"/"+srcName + "("+index+")";
				}
				if(index > 20)
					return false;
				index++;
			}
			try {
				//dstImage = Utils.copyFileTo(img.image, dstImage);
				FileUtils.copyFile(new File(img.image), new File(dstImage));
				if (dstImage != null) {
					String dstFolderName = folder.substring(folder.lastIndexOf("/") + 1);
					// 拷贝原图属性
					img = img.clone();
					// 更新图片
					img.selected = false;
					img.image = dstImage;
					img.folder = dstFolderName;
					// 修改时间
					if(new File(dstImage).setLastModified(img.lastModified * 1000) == false)
					{
						img.lastModified = new Date().getTime()/1000;
					}
//					PLog.out(img.image);
//					PLog.out(img.folder);
					// 添加到新文件夹图片列表中
					sImgs.add(img);
					FolderInfo folderInfo = null;
					int size = sFolders.size();
					for (int i = 0; i < size; i++) {
						folderInfo = sFolders.get(i);
						if (folderInfo.folder.equals(dstFolderName)) {
							folderInfo.imgs.add(img);
							folderInfo.totalImg++;
							break;
						}
					}

					// 添加到系统图库
					insertToSys(context, img);
					return true;
				}
			} catch (Exception e) {
			}
		}
		return false;
	}

	public static boolean existFolder(String folder) {
		FolderInfo folderInfo = null;
		int size = sFolders.size();
		for (int i = 0; i < size; i++) {
			folderInfo = sFolders.get(i);
			if (folderInfo.folder.equals(folder)) {
				return true;
			}
		}
		return false;
	}

	public static void addFolder(String folder) {
		if (existFolder(folder) == false) {
			FolderInfo folderInfo = new FolderInfo();
			folderInfo.folder = folder;
			sFolders.add(folderInfo);
		}
	}

	public static void updateImage(ImageInfo img) {
		if (img == null) {
			return;
		}
		img.bytes = null;
		synchronized (sCachedImgs) {
			sCachedImgs.remove(img);
		}
		img.bytes = null;
		String thumb = getThumbFile(img);
		if (thumb != null) {
			File file = new File(thumb);
			if (file.exists() == true) {
				file.delete();
			}
		}
		ThumbInfo thumbInfo;
		int size = sThumbs.size();
		for (int i = 0; i < size; i++) {
			thumbInfo = sThumbs.get(i);
			if (thumbInfo.id == img.id) {
				sThumbs.remove(i);
				break;
			}
		}
	}

	public static ContentValues getContentValues(Context context, ImageInfo img) {
		ContentResolver cr = context.getContentResolver();
		ContentValues values = new ContentValues();
		String where = MediaStore.Images.Media.DATA + "='" + img.image + "'";
//		PLog.out(where);
		if (cr != null) {
			String[] columns = new String[] { Media.BUCKET_DISPLAY_NAME, Media.ORIENTATION, Media.SIZE, Media.TITLE, Media.BUCKET_ID, Media.IS_PRIVATE, Media.LATITUDE, Media.LONGITUDE, Media.MIME_TYPE, Media.MINI_THUMB_MAGIC, Media.PICASA_ID, Media.DATA, Media.DATE_MODIFIED, Media.DATE_ADDED, Media.DATE_TAKEN, Media.DESCRIPTION, Media.DISPLAY_NAME };
			if (cr != null) {
				Cursor cursor = null;
				try {
					cursor = cr.query(Media.EXTERNAL_CONTENT_URI, columns, where, null, null);
				} catch (Exception e) {
//					PLog.out(e.getMessage());
				}
				if (cursor != null && cursor.moveToFirst()) {
					int[] columnsIndex = new int[columns.length];
					for (int i = 0; i < columns.length; i++) {
						columnsIndex[i] = cursor.getColumnIndex(columns[i]);
					}
					values.put(Media.BUCKET_DISPLAY_NAME, cursor.getString(columnsIndex[0]));
					values.put(Media.ORIENTATION, cursor.getString(columnsIndex[1]));
					values.put(Media.SIZE, cursor.getString(columnsIndex[2]));
					values.put(Media.TITLE, cursor.getString(columnsIndex[3]));
					values.put(Media.BUCKET_ID, cursor.getString(columnsIndex[4]));
					values.put(Media.IS_PRIVATE, cursor.getString(columnsIndex[5]));
					values.put(Media.LATITUDE, cursor.getString(columnsIndex[6]));
					values.put(Media.LONGITUDE, cursor.getString(columnsIndex[7]));
					values.put(Media.MIME_TYPE, cursor.getString(columnsIndex[8]));
					values.put(Media.MINI_THUMB_MAGIC, cursor.getString(columnsIndex[9]));
					values.put(Media.PICASA_ID, cursor.getString(columnsIndex[10]));
					values.put(Media.DATA, cursor.getString(columnsIndex[11]));
					values.put(Media.DATE_MODIFIED, cursor.getString(columnsIndex[12]));
					values.put(Media.DATE_ADDED, cursor.getString(columnsIndex[13]));
					values.put(Media.DATE_TAKEN, cursor.getString(columnsIndex[14]));
					values.put(Media.DESCRIPTION, cursor.getString(columnsIndex[15]));
					values.put(Media.DISPLAY_NAME, cursor.getString(columnsIndex[16]));
					return values;
				}
				if(cursor != null)
				{
					cursor.close();
				}
			}
		}
//		PLog.out("getContentValues false");
		return null;
	}

	public static ArrayList<FolderInfo> getFolders(Context context) {
		if (null != getImages(context) && sFolders != null) {
			return sFolders;
		}
		return null;
	}

	public static void setThumbSize(int size) {
		sThumbSize = size;
	}

	public static void setQuality(int quality) {
		sQuality = quality;
	}

	public static ImageInfo getImageByName(Context context, String name) {
		ImageInfo imgInfo = null;
		ArrayList<ImageInfo> imgs = getImages(context);
		if (imgs != null) {
			for (int i = 0; i < imgs.size(); i++) {
				imgInfo = imgs.get(i);
				if (imgInfo.image != null && imgInfo.image.endsWith(name)) {
					return imgInfo;
				}
			}
		}
		return null;
	}

	public static String getThumbFile(Context context, String image) {
		ImageInfo imgInfo = null;
		ArrayList<ImageInfo> imgs = getImages(context);
		if (imgs != null) {
			for (int i = 0; i < imgs.size(); i++) {
				imgInfo = imgs.get(i);
				if (imgInfo.image != null && imgInfo.image.equals(image)) {
					String thumb = getThumbFile(imgInfo);
					if (new File(thumb).exists()) {
						return thumb;
					}
					break;
				}
			}
		}
		return null;
	}

	public static String getThumbFile(ImageInfo img) {
		if (img == null) {
			return null;
		}
		String name = getFileName(img.image);
		if (name != null && name.length() > 0) {
			String thumb = sCachePath + "/" + name + "_thumb_" + img.fileSize + ".thumb";
			return thumb;
		}
		return null;
	}

	public static Bitmap getThumbnail(Context context, ImageInfo imgInfo) {
		if (imgInfo.bytes != null) {
			Bitmap bmp = null;
			// 打补丁，修复旧版本缓存长条缩略图过大导致内存问题
			if (imgInfo.bytes.length > 30 * 1024) {
				Options opts = new Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeByteArray(imgInfo.bytes, 0, imgInfo.bytes.length, opts);
				opts.inJustDecodeBounds = false;
				if (opts.outWidth > 0 && opts.outHeight > 0 && sThumbSize > 0) {
					int big = opts.outWidth > opts.outHeight ? opts.outWidth : opts.outHeight;
					opts.inSampleSize = big / sThumbSize;
				}
				bmp = BitmapFactory.decodeByteArray(imgInfo.bytes, 0, imgInfo.bytes.length, opts);
			} else {
				bmp = BitmapFactory.decodeByteArray(imgInfo.bytes, 0, imgInfo.bytes.length);
			}
			return bmp;
		}
		Bitmap bmp = null;
		bmp = loadThumb(imgInfo);
		if (bmp != null)
			return bmp;
		if (context != null) {
			bmp = decodeThumb(context, imgInfo);
			if (sThumbSize > 0 && bmp != null) {
				int size = sThumbSize;
				int w = bmp.getWidth();
				int h = bmp.getHeight();
				Rect rcSrc = new Rect();
				if (w < h) {
					rcSrc.set(0, (h - w) / 2, w, (h - w) / 2 + w);
				} else {
					rcSrc.set((w - h) / 2, 0, (w - h) / 2 + h, h);
				}
				if(size > rcSrc.width())
				{
					size = rcSrc.width();
				}
				Bitmap thumb = Bitmap.createBitmap(size, size, Config.RGB_565);
				Canvas canvas = new Canvas(thumb);
				canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
				canvas.drawBitmap(bmp, rcSrc, new Rect(0, 0, size, size), null);
				bmp = thumb;
			}
			if (bmp != null) {
				cacheBitmap(bmp, imgInfo);
				synchronized (sSaveQueue) {
					sSaveQueue.add(imgInfo);
				}
				startSaveThumb();
			}
			return bmp;
		}
		return null;
	}

	private static ArrayList<ImageInfo> sSaveQueue = new ArrayList<ImageInfo>();
	private static boolean sSaveStarted = false;

	private static void startSaveThumb() {
		if (sSaveStarted == false) {
			sSaveStarted = true;
			Thread loader = new Thread(sSaveThumbRunnable);
			loader.start();
		}
	}

	private static Runnable sSaveThumbRunnable = new Runnable() {

		@Override
		public void run() {
			while (true) {
				ImageInfo img = null;
				synchronized (sSaveQueue) {
					if (sSaveQueue.size() == 0)
						break;
					img = sSaveQueue.get(0);
					sSaveQueue.remove(0);
				}
				if (img != null && img.bytes != null) {
					saveThumb(img);
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					break;
				}
			}
			sSaveStarted = false;
		}

	};

	public static void makeCacheBitmap(Context context, ImageInfo imgInfo) {
		if (sCachePath == null || imgInfo == null || imgInfo.image == null)
			return;

		String thumb = getThumbFile(imgInfo);
		if (thumb != null) {
			File file = new File(thumb);
			if (file.exists() == true) {
				int length = (int) file.length();
				if (length > 0) {
					if (sMemoryLimit > 0) {
						while (sCacheUsedMemory > sMemoryLimit) {
							synchronized (sCachedImgs) {
								ImageInfo cacheInfo = sCachedImgs.get(0);
								if (cacheInfo.bytes != null) {
									sCacheUsedMemory -= cacheInfo.bytes.length;
									cacheInfo.bytes = null;
								}
								sCachedImgs.remove(0);
							}
						}
					}
					try {
						byte[] bytes = new byte[length];
						FileInputStream fis = new FileInputStream(file);
						fis.read(bytes);
						fis.close();
						sCacheUsedMemory += bytes.length;
						imgInfo.bytes = bytes;
						synchronized (sCachedImgs) {
							sCachedImgs.add(imgInfo);
						}
					} catch (Exception e) {
					}
				}
				return;
			}
		}
		if (context != null) {
			ContentResolver cr = context.getContentResolver();
			if (cr != null) {
				Bitmap bmp = decodeThumb(context, imgInfo);
				if (sThumbSize > 0 && bmp != null) {
					int size = sThumbSize;
					int w = bmp.getWidth();
					int h = bmp.getHeight();
					Rect rcSrc = new Rect();
					if (w < h) {
						rcSrc.set(0, (h - w) / 2, w, (h - w) / 2 + w);
					} else {
						rcSrc.set((w - h) / 2, 0, (w - h) / 2 + h, h);
					}
					if(size > rcSrc.width())
					{
						size = rcSrc.width();
					}
					Bitmap thumbBmp = Bitmap.createBitmap(size, size, Config.RGB_565);
					Canvas canvas = new Canvas(thumbBmp);
					canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
					canvas.drawBitmap(bmp, rcSrc, new Rect(0, 0, size, size), null);
					bmp = thumbBmp;
				}
				if (bmp != null) {
					cacheBitmap(bmp, imgInfo);
					synchronized (sSaveQueue) {
						sSaveQueue.add(imgInfo);
					}
					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable() {
						@Override
						public void run() {
							startSaveThumb();
						}
					});
					bmp.recycle();
				}
			}
		}
	}

	public static void setMemoryLimit(long limit) {
		if (limit < 1024 * 500) {
			limit = 1024 * 500;
		}
		sMemoryLimit = limit;
	}

	public static void clearCache() {
		sUpdated = false;
		sCacheUsedMemory = 0;
		synchronized (sCachedImgs) {
			sCachedImgs.clear();
		}
	}

	public static void clear(boolean clearSel) {
		if (sImgs != null) {
			ImageInfo info;
			int count = sImgs.size();
			if (clearSel == true) {
				for (int i = 0; i < count; i++) {
					info = sImgs.get(i);
					if (info != null) {
						info.bytes = null;
						info.selected = false;
					}
				}
			} else {
				for (int i = 0; i < count; i++) {
					info = sImgs.get(i);
					if (info != null) {
						info.bytes = null;
					}
				}
				synchronized(THREAD_LOCK)
				{
					if(sImgs != null)
					{
						sImgs.clear();
					}
					if(sThumbs != null)
					{
						sThumbs.clear();
					}
					if(sFolders != null)
					{
						sFolders.clear();
					}
				}
			}
		}
		sCacheUsedMemory = 0;
		synchronized (sCachedImgs) {
			sCachedImgs.clear();
		}
	}

	public static void clearSel() {
		if (sImgs != null) {
			ImageInfo info;
			int count = sImgs.size();
			for (int i = 0; i < count; i++) {
				info = sImgs.get(i);
				if (info != null) {
					info.selected = false;
				}
			}
		}
	}

	private static Bitmap decodeThumb(Context context, ImageInfo imgInfo) {
		Bitmap bmp = null;
		if (imgInfo.isVideo) {
			bmp = ThumbnailUtils.createVideoThumbnail(imgInfo.image, Video.Thumbnails.MICRO_KIND);
			return bmp;
		}
		Bitmap thumb = getExifThumb(imgInfo);
		if(thumb != null)
		{
			return thumb;
		}
		String thumbImg = getThumbFile(imgInfo.id);
		int rotation = CommonUtils.GetImgInfo(imgInfo.image)[0];
		if (thumbImg != null) {
			// 检查缩略图是否有读取Exif的旋转值，只要有一个是没有读的，可以判断其它都是没有读的
			if (sCheckedRotate == false) {
				if (rotation % 360 != 0) {
					Options optsThumb = new Options();
					optsThumb.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(thumbImg, optsThumb);
					Options optsImage = new Options();
					optsImage.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(thumbImg, optsImage);
					if (optsThumb.outWidth > 0 && optsThumb.outHeight > 0 && optsImage.outHeight > 0 && optsImage.outWidth > 0) {
						float r1 = (float) optsThumb.outWidth / (float) optsThumb.outHeight;
						float r2 = (float) optsImage.outHeight / (float) optsImage.outWidth;
						if (Math.abs(r2 - 1.0f) > 0.05f) {
							if (Math.abs(r1 - r2) > 0.05f) {
								sNeedRotateThumb = true;
							}
							sCheckedRotate = true;
						}
					}
				}
			}
		}
		if (thumbImg == null || (sCheckedRotate == false && rotation % 360 != 0)) {
			bmp = decodeFile(imgInfo, rotation);
		} else {
			//bmp = Utils.decodeFile(thumbImg, sThumbSize*2);
			bmp = Utils.DecodeImage(context, thumbImg, rotation, -1, sThumbSize, sThumbSize);
			if (bmp != null && sNeedRotateThumb == true && rotation % 360 != 0) {
				Matrix m = new Matrix();
				m.setRotate(rotation);
				Bitmap temp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
				if(temp != bmp)
				{
					bmp.recycle();
					bmp = null;
				}
				bmp = temp;
			}
			if (bmp == null) {
				bmp = decodeFile(imgInfo, rotation);
			}
		}
		return bmp;
	}
	
	private static Bitmap getExifThumb(ImageInfo img)
	{
		if(img == null)
		{
			return null;
		}
		ExifInterface exif;
		try {
			exif = new ExifInterface(img.image);
			byte[] bytes = exif.getThumbnail();
			if(bytes != null)
			{
				Options opts = new Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
				opts.inJustDecodeBounds = false;
				int small = opts.outWidth<opts.outHeight?opts.outWidth:opts.outHeight;
				//过滤掉劣质缩略图
				if(small < sThumbSize)
				{
					return null;
				}
				
				Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				if(bmp != null)
				{
					int rotation = 0;
					int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
					switch(ori)
					{
					case ExifInterface.ORIENTATION_ROTATE_90:
						rotation = 90;
						break;
					case ExifInterface.ORIENTATION_ROTATE_180:
						rotation = 180;
						break;
					case ExifInterface.ORIENTATION_ROTATE_270:
						rotation = 270;
						break;
					}
					if(rotation != 0) {
						Matrix m = new Matrix();
						m.setRotate(rotation);
						bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
					}
					return bmp;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	private static Bitmap decodeFile(ImageInfo imgInfo, int rotation) {
		Bitmap bmp = null;
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imgInfo.image, opts);
		opts.inJustDecodeBounds = false;
		if (opts.outWidth > 0 && opts.outHeight > 0) {
			int ref = opts.outWidth < opts.outHeight ? opts.outWidth : opts.outHeight;
			float r = (float) opts.outWidth / (float) opts.outHeight;
			if (r > 1) {
				r = 1 / r;
			}
			int big = opts.outWidth > opts.outHeight ? opts.outWidth : opts.outHeight;
			if (big > 1024 && r < 0.3) {
				ref = (int) ((float) big / (r * 20));
			}
			opts.inSampleSize = ref / sThumbSize;
			bmp = BitmapFactory.decodeFile(imgInfo.image, opts);
			if (bmp != null && rotation % 360 != 0) {
				Matrix m = new Matrix();
				m.setRotate(rotation);
				bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
			}
		}
		return bmp;
	}

	private static final Object THREAD_LOCK = new Object();
	private static String getThumbFile(int imgId) {
		synchronized(THREAD_LOCK)
		{
			ThumbInfo thumb;
			int size = sThumbs.size();
			for (int i = 0; i < size; i++) {
				thumb = sThumbs.get(i);
				if (thumb != null && thumb.id == imgId) {
					return thumb.thumb;
				}
			}
			return null;
		}
	}

	private static void cacheBitmap(Bitmap bmp, ImageInfo info) {
		if (bmp == null || info.bytes != null)
			return;
		try {
			if (sMemoryLimit > 0) {
				while (sCacheUsedMemory > sMemoryLimit) {
					synchronized (sCachedImgs) {
						ImageInfo cacheInfo = sCachedImgs.get(0);
						if (cacheInfo != null && cacheInfo.bytes != null) {
							sCacheUsedMemory -= cacheInfo.bytes.length;
							cacheInfo.bytes = null;
						}
						sCachedImgs.remove(0);
					}
				}
			}
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			bmp.compress(CompressFormat.JPEG, sQuality, os);
			info.bytes = os.toByteArray();
			sCacheUsedMemory += info.bytes.length;
			os.close();
			synchronized (sCachedImgs) {
				sCachedImgs.add(info);
			}
		} catch (Exception e) {
		}
	}

	private static void saveThumb(ImageInfo info) {
		if (info.bytes != null && info.image != null) {
			makeCachePath();
			if (sCachePath == null)
				return;

			String thumb = getThumbFile(info);
			if (thumb != null) {
				try {
					FileOutputStream fos = new FileOutputStream(thumb);
					fos.write(info.bytes);
					fos.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private static Bitmap loadThumb(ImageInfo info) {
		makeCachePath();
		if (sCachePath == null)
			return null;

		String thumb = getThumbFile(info);
		if (thumb != null) {
			try {
				File file = new File(thumb);
				if (file.exists() == true) {
					int length = (int) file.length();
					if (length > 0) {
						byte[] bytes = new byte[length];
						FileInputStream fis = new FileInputStream(file);
						fis.read(bytes);
						fis.close();
						Bitmap bmp = null;
						// 打补丁，修复旧版本缓存长条缩略图过大导致内存问题
						if (bytes.length > 30 * 1024) {
							Options opts = new Options();
							opts.inJustDecodeBounds = true;
							BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
							opts.inJustDecodeBounds = false;
							if (opts.outWidth > 0 && opts.outHeight > 0 && sThumbSize > 0) {
								int big = opts.outWidth > opts.outHeight ? opts.outWidth : opts.outHeight;
								opts.inSampleSize = big / sThumbSize;
							}
							bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
						} else {
							bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
						}
						info.bytes = bytes;
						return bmp;
					}
				}
			} catch (Exception e) {
			}
		}
		return null;
	}

	private static void clearUnavaliableThumbs() {
		makeCachePath();
		ArrayList<ImageInfo> imgs = sImgs;
		String path = sCachePath;
		ImageInfo info = null;
		if (path != null && imgs != null) {
			File[] files = new File(path).listFiles();
			if (files != null && imgs.size() < files.length) {
				int deleteCount = 0;
				StringBuffer strThumbs = new StringBuffer();
				ImageInfo[] aimgs = imgs.toArray(new ImageInfo[imgs.size()]);
				for (int i = 0; i < aimgs.length; i++) {
					info = aimgs[i];
					String thumb = getThumbFile(info);
					if (thumb != null) {
						strThumbs.append(thumb);
						strThumbs.append(",");
					}
				}
				for (int i = 0; i < files.length; i++) {
					String thumb = files[i].getAbsolutePath();
					// 最多删30张，没删完留着下次删，节省点时间
					if (strThumbs.indexOf(thumb) == -1 && deleteCount < 30) {
						files[i].delete();
						deleteCount++;
					}
				}
			}
		}
	}

	private static String getFileName(String img) {
		String image = img;
		int pos = img.lastIndexOf('/');
		int dot = img.lastIndexOf('.');
		if (pos != -1 && dot != -1 && dot > pos) {
			image = img.substring(pos + 1, dot);
		}
		return image;
	}

	private static String makeCachePath() {
		if (sCachePath == null) {
			String sdcard = SysConfig.GetSDCardPath();
			if (sdcard != null) {
				sCachePath = SysConfig.GetSDCardPath() + CACHE_PATH;
				File directory = new File(sCachePath);
				if (directory.exists() == false) {
					if (false == directory.mkdirs())
						sCachePath = null;
				}
			}
		}
		return sCachePath;
	}

	private static void insertToSys(Context context, ImageInfo img) {
		if (img != null && img.image != null) {
			if(img.isVideo == false)
			{
				int degree = CommonUtils.GetImgInfo(img.image)[0];
				ContentValues values = new ContentValues();
				values.put(Media.DATA, img.image);
				values.put(Media.DATE_MODIFIED, img.lastModified);
				values.put(Media.DATE_ADDED, img.lastModified);
				values.put(Media.DATE_TAKEN, img.lastModified * 1000);
				values.put(Media.LATITUDE, img.latitude);
				values.put(Media.LONGITUDE, img.longitude);
				values.put(Media.DISPLAY_NAME, new File(img.image).getName());
				values.put(Media.ORIENTATION, degree);
				values.put(Media.SIZE, img.fileSize);
				ContentResolver cr = context.getContentResolver();
				if (cr != null) {
					cr.insert(Media.EXTERNAL_CONTENT_URI, values);
				}
				
				String where = MediaStore.Images.Media.DATA + "='" + img.image + "'";
				String[] columns = new String[] { Media._ID};
				Cursor cursor = null;
				try {
					cursor = cr.query(Media.EXTERNAL_CONTENT_URI, columns, where, null, null);
					if(cursor != null)
					{
						cursor.moveToFirst();
						img.id = cursor.getInt(0);
						cursor.close();
					}
				} catch (Exception e) {
//					PLog.out(e.getMessage());
				}
			}
			else
			{
				ContentValues values = new ContentValues();
				values.put(Video.Media.DATA, img.image);
				values.put(Video.Media.DATE_MODIFIED, img.lastModified);
				values.put(Video.Media.DATE_ADDED, img.lastModified);
				values.put(Video.Media.DATE_TAKEN, img.lastModified * 1000);
				values.put(Video.Media.LATITUDE, img.latitude);
				values.put(Video.Media.LONGITUDE, img.longitude);
				values.put(Video.Media.DISPLAY_NAME, new File(img.image).getName());
				values.put(Video.Media.SIZE, img.fileSize);
				ContentResolver cr = context.getContentResolver();
				if (cr != null) {
					cr.insert(Video.Media.EXTERNAL_CONTENT_URI, values);
				}
				
				String where = Video.Media.DATA + "='" + img.image + "'";
				String[] columns = new String[] { Video.Media._ID};
				Cursor cursor = null;
				try {
					cursor = cr.query(Video.Media.EXTERNAL_CONTENT_URI, columns, where, null, null);
					if(cursor != null)
					{
						cursor.moveToFirst();
						img.id = cursor.getInt(0);
						cursor.close();
					}
				} catch (Exception e) {
//					PLog.out(e.getMessage());
				}
			}
		}
	}
}
