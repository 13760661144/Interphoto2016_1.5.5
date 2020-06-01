package cn.poco.utils;

import android.graphics.Bitmap;

public class JniUtils {
	public static native void conversePixels(byte[] data, int w, int h);
	public static native void reversePixels(int[] data, int w, int h);
	public static native int[] byteArrayToIntArray(byte[] data);
	public static native void yuv2rgb(int w,int h,int sz,byte[] arIn,int[] arOut);
	public static native boolean saveAlphaBitmap(Bitmap bitmap, byte[] file);
	public static native Bitmap readAlphaBitmap(byte[] file, int sampleSize);
	public static native int[] getAlphaArea(Bitmap bitmap);
	public static native boolean getMaskedBitmap(Bitmap bitmap, Bitmap mask);
	public static native boolean imgFilter(String img);
	public static native boolean isFileExist(String img);
	static
	{
		System.loadLibrary("utilitys");
	}
}
