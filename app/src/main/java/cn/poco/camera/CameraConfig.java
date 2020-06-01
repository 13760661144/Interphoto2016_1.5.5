package cn.poco.camera;

import android.content.Context;
import android.content.SharedPreferences;

import cn.poco.draglistview.DragListItemInfo;

/**
 * Created by: fwc
 * Date: 2017/6/7
 */
public class CameraConfig {

	public static final int SIZE_9_16 = 1;
	public static final int SIZE_16_9 = 2;
	public static final int SIZE_235_1 = 3;
	public static final int SIZE_3_4 = 4;
	public static final int SIZE_1_1 = 5;

	public static final int FLASH_AUTO = 1;
	public static final int FLASH_ON = 2;
	public static final int FLASH_OFF = 3;

	public static final int TIMER_OFF = 1;
	public static final int TIMER_3S = 2;
	public static final int TIMER_10S = 3;

	public static final int DURATION_10S = 1;
	public static final int DURATION_30S = 2;
	public static final int DURATION_60S = 3;
	public static final int DURATION_180S = 4;

	private static final String CONFIG_NAME = "camera_config";

	private static final String KEY_IMAGE_SIZE = "key_image_size";
	private static final String KEY_TIMER = "key_timer";
	private static final String KEY_TOUCH_CAPTURE = "key_touch_capture";

	private static final String KEY_FILTER_URI = "key_filter_uri";

	private static final String KEY_PREVIEW_PATCH_0 = "key_preview_patch_0";
	private static final String KEY_PREVIEW_PATCH_1 = "key_preview_patch_1";
	private static final String KEY_PICTURE_PATCH_0 = "key_picture_patch_0";
	private static final String KEY_PICTURE_PATCH_1 = "key_picture_patch_1";

	private static final String KEY_VIDEO_SIZE = "key_video_size";
	private static final String KEY_VIDEO_DURATION = "key_video_duration";

	private static SharedPreferences sSharedPreferences;
	private static SharedPreferences.Editor sEditor;

	private static int sImageSize;
	private static int sTimer;
	private static boolean sTouchCapture;

	private static int sVideoSize;
	private static int sVideoDuration;

	private static int sFilterUri;

	private static int sPreviewPatch0;
	private static int sPreviewPatch1;
	private static int sPicturePatch0;
	private static int sPicturePatch1;

	/**
	 * 初始化配置
	 *
	 * @param context 上下文
	 */
	public static void initConfig(Context context) {
		sSharedPreferences = context.getSharedPreferences(CONFIG_NAME, Context.MODE_PRIVATE);

		sImageSize = sSharedPreferences.getInt(KEY_IMAGE_SIZE, SIZE_9_16);
		sTimer = sSharedPreferences.getInt(KEY_TIMER, TIMER_OFF);
		sTouchCapture = sSharedPreferences.getBoolean(KEY_TOUCH_CAPTURE, false);

		sPreviewPatch0 = sSharedPreferences.getInt(KEY_PREVIEW_PATCH_0, 90);
		sPreviewPatch1 = sSharedPreferences.getInt(KEY_PREVIEW_PATCH_1, 90);
		sPicturePatch0 = sSharedPreferences.getInt(KEY_PICTURE_PATCH_0, 0);
		sPicturePatch1 = sSharedPreferences.getInt(KEY_PICTURE_PATCH_1, 0);

		sVideoSize = sSharedPreferences.getInt(KEY_VIDEO_SIZE, SIZE_16_9);
		sVideoDuration = sSharedPreferences.getInt(KEY_VIDEO_DURATION, DURATION_10S);

		sFilterUri = sSharedPreferences.getInt(KEY_FILTER_URI, DragListItemInfo.URI_ORIGIN);
	}

	public static int getImageSize() {
		return sImageSize;
	}

	public static void setImageSize(int imageSize) {
		sImageSize = imageSize;

		checkEditor();

		sEditor.putInt(KEY_IMAGE_SIZE, sImageSize);
		sEditor.apply();
	}

	public static int getVideoSize() {
		return sVideoSize;
	}

	public static void setVideoSize(int videoSize) {
		sVideoSize = videoSize;

		checkEditor();

		sEditor.putInt(KEY_VIDEO_SIZE, sVideoSize);
		sEditor.apply();
	}

	public static String getFlashStr(int flashMode) {
		String s = "auto";

		if (flashMode == FLASH_ON) {
			s = "torch";
		} else if (flashMode == FLASH_OFF) {
			s = "off";
		}

		return s;
	}

	public static int getTimer() {
		return sTimer;
	}

	public static void setTimer(int timer) {
		sTimer = timer;

		checkEditor();

		sEditor.putInt(KEY_TIMER, sTimer);
		sEditor.apply();
	}

	public static boolean isTouchCapture() {
		return sTouchCapture;
	}

	public static void setTouchCapture(boolean touchCapture) {
		sTouchCapture = touchCapture;

		checkEditor();

		sEditor.putBoolean(KEY_TOUCH_CAPTURE, sTouchCapture);
		sEditor.apply();
	}

	public static int getDuration() {
		return sVideoDuration;
	}

	public static void setDuration(int duration) {
		sVideoDuration = duration;

		checkEditor();

		sEditor.putInt(KEY_VIDEO_DURATION, sVideoDuration);
		sEditor.apply();
	}

	public static int getPreviewPatch0() {
		return sPreviewPatch0;
	}

	public static void setPreviewPatch0(int previewPatch0) {
		sPreviewPatch0 = previewPatch0;

		checkEditor();

		sEditor.putInt(KEY_PREVIEW_PATCH_0, sPreviewPatch0);
		sEditor.apply();
	}

	public static int getPreviewPatch1() {
		return sPreviewPatch1;
	}

	public static void setPreviewPatch1(int previewPatch1) {
		sPreviewPatch1 = previewPatch1;

		checkEditor();

		sEditor.putInt(KEY_PREVIEW_PATCH_1, sPreviewPatch1);
		sEditor.apply();
	}

	public static int getPicturePatch0() {
		return sPicturePatch0;
	}

	public static void setPicturePatch0(int picturePatch0) {
		sPicturePatch0 = picturePatch0;

		checkEditor();

		sEditor.putInt(KEY_PICTURE_PATCH_0, sPicturePatch0);
		sEditor.apply();
	}

	public static int getPicturePatch1() {
		return sPicturePatch1;
	}

	public static void setPicturePatch1(int picturePatch1) {
		sPicturePatch1 = picturePatch1;

		checkEditor();

		sEditor.putInt(KEY_PICTURE_PATCH_1, sPicturePatch1);
		sEditor.apply();
	}

	public static int getFilterUri() {
		return sFilterUri;
	}

	public static void setFilterUri(int filterUri) {
		sFilterUri = filterUri;
		checkEditor();

		sEditor.putInt(KEY_FILTER_URI, sFilterUri);
		sEditor.apply();
	}

	@SuppressWarnings("all")
	private static void checkEditor() {
		if (sEditor == null) {
			sEditor = sSharedPreferences.edit();
		}
	}
}
