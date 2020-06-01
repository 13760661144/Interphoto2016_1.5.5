package cn.poco.album2.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by: fwc
 * Date: 2017/3/7
 */
public class SDCardUtils {

	public static final long DEFAULT_SIZE = 250 * 1024 * 1024; // 250M

	/**
	 * 判断SDCard是否可用
	 *
	 * @return SD卡是否可用
	 */
	public static boolean checkSDCardAvailable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取SD卡大小
	 * @param context 上下文
	 * @return SD卡大小
	 */
	public static long getSDTotalSize(Context context) {

		if (!checkSDCardAvailable()) {
			return 0;
		}

		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		return getPathSize(path);
	}

	/**
	 * 获取SD卡的剩余大小
	 * @return SD卡的剩余大小
	 */
	public static long getSDCardAvailableSize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat;
		try {
			stat = new StatFs(path.getPath());
		} catch (Throwable e) {
			e.printStackTrace();
			return DEFAULT_SIZE;
		}

		long availableBlocks;
		long blockSize;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			blockSize = stat.getBlockSizeLong();
			availableBlocks = stat.getAvailableBlocksLong();
		} else {
			blockSize = stat.getBlockSize();
			availableBlocks = stat.getAvailableBlocks();
		}

		return availableBlocks * blockSize;
	}

//	/**
//	 * 获取文件的大小
//	 * @param path 路径
//	 * @return 文件的大小
//	 */
//	public static long getFileSize(String path) {
//		File file = new File(path);
//
//		if (!file.exists()) {
//			return 0;
//		}
//
//		long size = 0;
//		if (file.isDirectory() && file.list().length != 0) {
//			for (File file1 : file.listFiles()) {
//				size += file1.length();
//			}
//		} else if (file.isFile()) {
//			size = file.length();
//		}
//
//		return size;
//	}

	private static long getPathSize(String path) {
		if (TextUtils.isEmpty(path) || !new File(path).exists()) {
			return 0;
		}

		StatFs stat;
		try {
			stat = new StatFs(path);
		} catch (Exception e) {
			e.printStackTrace();
			return DEFAULT_SIZE;
		}

		long blockSize;
		long totalBlocks;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			blockSize = stat.getBlockSizeLong();
			totalBlocks = stat.getBlockCountLong();
		} else {
			blockSize = stat.getBlockSize();
			totalBlocks = stat.getBlockCount();
		}
		return blockSize * totalBlocks;
	}
}
