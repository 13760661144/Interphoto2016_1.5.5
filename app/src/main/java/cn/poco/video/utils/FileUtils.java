package cn.poco.video.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.StringDef;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

import cn.poco.tianutils.CommonUtils;

/**
 * Created by: fwc
 * Date: 2017/5/19
 */
public class FileUtils {

	public static final String AAC_FORMAT = ".aac";
	public static final String WAV_FORMAT = ".wav";
	public static final String PCM_FORMAT = ".pcm";
	public static final String MP3_FORMAT = ".mp3";
	public static final String MP4_FORMAT = ".mp4";
	public static final String H264_FORMAT = ".h264";

	public static String sVideoDir;
	private static String sTempDir;
	private static String sSrcDir;


	static {
		sVideoDir = Environment.getExternalStorageDirectory() + File.separator + "interphoto" + File.separator + ".video";

		sTempDir = sVideoDir + File.separator + "temp";
		sSrcDir = sVideoDir + File.separator + ".srcVideo";
	}

	@StringDef({AAC_FORMAT, WAV_FORMAT, PCM_FORMAT, MP3_FORMAT, MP4_FORMAT, H264_FORMAT})
	@Retention(RetentionPolicy.SOURCE)
	@interface Format {

	}

	private FileUtils() {

	}

	/**
	 * 判断所给文件是否存在
	 * @param file 文件；路径
	 * @return true: 文件存在
	 */
	public static boolean isFileExist(String file) {
		return !TextUtils.isEmpty(file) && new File(file).exists();
	}

	/**
	 * 删除指定文件，如果指定文件时目录，需要先删除该目录下的所有文件才能删除该目录
	 * @param path 文件路径
	 * @return 删除是否成功
	 */
	public static boolean delete(String path) {
		if (!TextUtils.isEmpty(path)) {
			File file = new File(path);
			if (!file.exists()) {
				return false;
			}

			if (file.isDirectory()) {
				String[] filePaths = file.list();
				if (filePaths != null) {
					for (String filePath : filePaths) {
						delete(path + "/" + filePath);
					}
				}
			}

			return file.delete();
		}

		return false;
	}

	/**
	 * 获取临时路径
	 *
	 * @param format 文件格式
	 * @return 临时路径
	 */
	public static String getTempPath(@Format String format) {
		// 确保文件夹存在
		CommonUtils.MakeFolder(sTempDir);
		return sTempDir + File.separator + UUID.randomUUID() + format;
	}

	public static String getVideoFrameDir(String videoPath) {

		int pos = videoPath.lastIndexOf('.');
		if (pos > -1) {
			// 去掉后缀名
			videoPath = videoPath.substring(0, pos);
		}
		String dir = sTempDir + File.separator + videoPath;
		CommonUtils.MakeFolder(dir);

		return dir;
	}

	/**
	 * 获取隐藏文件的路径
	 * @param format 文件格式
	 * @return 隐藏的视频源文件的路径
	 */
	public static String getSrcVideoPath(@Format String format) {
		CommonUtils.MakeFolder(sSrcDir);
		return sSrcDir + File.separator + System.currentTimeMillis() + format;
	}

	/**
	 * 关闭文件等资源
	 *
	 * @param closeable Closeable对象
	 */
	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 添加到媒体数据库
	 *
	 * @param context 上下文
	 */
	public static Uri fileScanVideo(Context context, String videoPath) {

		File file = new File(videoPath);
		if (file.exists()) {

			VideoUtils.VideoInfo info = VideoUtils.getVideoInfo(videoPath);

			Uri uri = null;

			if (info != null) {
				long size = file.length();
				String fileName = file.getName();
				long dateTaken = System.currentTimeMillis();

				ContentValues values = new ContentValues(11);
				values.put(MediaStore.Video.Media.DATA, videoPath); // 路径;
				values.put(MediaStore.Video.Media.TITLE, fileName); // 标题;
				values.put(MediaStore.Video.Media.DURATION, info.duration); // 时长
				values.put(MediaStore.Video.Media.WIDTH, info.width); // 视频宽
				values.put(MediaStore.Video.Media.HEIGHT, info.height); // 视频高
				values.put(MediaStore.Video.Media.SIZE, size); // 视频大小;
				values.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken); // 插入时间;
				values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);// 文件名;
				values.put(MediaStore.Video.Media.DATE_MODIFIED, dateTaken / 1000);// 修改时间;
				values.put(MediaStore.Video.Media.DATE_ADDED, dateTaken / 1000); // 添加时间;
				values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

				ContentResolver resolver = context.getContentResolver();

				if (resolver != null) {
					try {
						uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
					} catch (Exception e) {
						e.printStackTrace();
						uri = null;
					}
				}
			}



			if (uri == null) {
				MediaScannerConnection.scanFile(context, new String[] {videoPath}, new String[] {"video/*"}, new MediaScannerConnection.OnScanCompletedListener() {
					@Override
					public void onScanCompleted(String path, Uri uri) {

					}
				});
			}

			return uri;
		}

		return null;
	}

	/**
	 * 清除所有临时文件
	 */
	public static void clearTempFiles() {
		deleteFiles(FileUtils.sTempDir, false);
	}

	/**
	 * 清除录制视频临时文件
	 */
	public static void clearVideoFiles() {
		deleteFiles(FileUtils.sVideoDir, false);
	}

	/**
	 * 删除文件
	 * @param path 文件路径
	 * @param deleteSelf 是否删除自己
	 * @return 删除是否成功
	 */
	public static boolean deleteFiles(String path, boolean deleteSelf) {

		if (!TextUtils.isEmpty(path)) {
			File file = new File(path);
			if (!file.exists()) {
				return false;
			}

			boolean result = false;
			if (file.isDirectory()) {
				String[] filePaths = file.list();
				for (String filePath : filePaths) {
					result = delete(path + "/" + filePath);
				}
			}

			if (deleteSelf) {
				return file.delete();
			}

			return result;
		}

		return false;
	}

	public static void clearHiddenSrcVideoFile() {
		delete(sSrcDir);
	}

	public static void renameOrCopy(String from, String to) {

		boolean renameSuccess;
		try {
			renameSuccess = new File(from).renameTo(new File(to));
		} catch (Exception e) {
			e.printStackTrace();
			renameSuccess = false;
		}

		if (!renameSuccess) {
			cn.poco.album2.utils.FileUtils.copyFile(from, to);
		}
	}
}
