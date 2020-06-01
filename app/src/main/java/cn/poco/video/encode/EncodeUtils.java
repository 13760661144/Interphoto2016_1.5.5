package cn.poco.video.encode;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;

import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/9/21
 */
public class EncodeUtils {

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public static boolean isSupportHideMode() {

		return ShareData.m_screenWidth >= 1080 && ShareData.m_screenRealHeight >= 1920;
	}

	/**
	 * 限制保存视频的宽度和高度
	 * @param mimeType 视频的MIME类型
	 * @param width 视频原宽度
	 * @param heigth 视频原高度
	 * @param rotation 视频旋转角度
	 * @return 视频的宽度和高度
	 */
	public static int[] limitVideoWidthAndHeight(String mimeType, int width, int heigth, int rotation) {

		int size[] = new int[2];
		size[0] = width;
		size[1] = heigth;

		if (rotation % 180 != 0) {
			int temp = size[0];
			size[0] = size[1];
			size[1] = temp;
		}

		size = getVideoSupportSize(size[0], size[1],
				ShareData.m_screenWidth, ShareData.m_screenRealHeight >= 1280 ? ShareData.m_screenRealHeight : 1280);

		size[0] = checkEncodeSize(size[0]);
		size[1] = checkEncodeSize(size[1]);

//		if (!isEncoderSupport(mimeType, size[0], size[1])) {
//			size = getVideoSupportSize(size[0], size[1], 720, 1280);
//		}

		return size;
	}

	/**
	 * 获取视频编码支持的大小
	 * @param videoWidth 视频宽度
	 * @param videoHeight 视频高度
	 * @param maxWidth 限制最大宽度
	 * @param maxHeight 限制最大高度
	 * @return 最终大小
	 */
	public static int[] getVideoSupportSize(int videoWidth, int videoHeight, int maxWidth, int maxHeight) {

		int[] size = new int[2];

		if (videoWidth <= maxWidth && videoHeight <= maxHeight) {
			size[0] = videoWidth;
			size[1] = videoHeight;
		} else {
			float scaleX = videoWidth * 1f / maxWidth;
			float scaleY = videoHeight * 1f / maxHeight;

			if (scaleX > scaleY) {
				size[0] = maxWidth;
				size[1] = (int)(videoHeight / scaleX);
			} else {
				size[0] = (int)(videoWidth / scaleY);
				size[1] = maxHeight;
			}
		}

		return size;
	}

	/**
	 * 判断编码器是否支持该视频宽度和高度
	 * @param mimeType 视频的MIME类型
	 * @param width 视频宽度
	 * @param height 视频高度
	 * @return 是否支持
	 */
	public static boolean isEncoderSupport(String mimeType, int width, int height) {

		boolean result = true;

		MediaFormat format = MediaFormat.createVideoFormat(mimeType, width, height);

		format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
		format.setInteger(MediaFormat.KEY_BIT_RATE, 5 * 1024 * 1024);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

		MediaCodec mediaCodec = null;
		try {
			mediaCodec = MediaCodec.createEncoderByType(mimeType);
			mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
			mediaCodec.start();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			result = false;
		} finally {
			if (mediaCodec != null) {
				mediaCodec.stop();
				mediaCodec.release();
			}
		}

		return result;
	}

	public static int checkEncodeSize(int size) {
		return size / 16 * 16;
	}

	/**
	 * 选择支持mimeType的编码器信息
	 * @param mimeType 视频的MIME类型
	 * @return 编码器信息
	 */
	public static MediaCodecInfo selectCodec(String mimeType) {
		int numCodecs = MediaCodecList.getCodecCount();
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
			if (!codecInfo.isEncoder()) {
				continue;
			}
			String[] types = codecInfo.getSupportedTypes();

			for (String type : types) {
				if (type.equalsIgnoreCase(mimeType)) {
					return codecInfo;
				}
			}
		}
		return null;
	}

	public static MediaCodecInfo selectCodec(String mimeType, int colorFormat) {
		int numCodecs = MediaCodecList.getCodecCount();
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

			if (codecInfo.isEncoder()) {
				String[] types = codecInfo.getSupportedTypes();
				for (String type : types) {
					if (type.equalsIgnoreCase(mimeType)) {
						for (int format : codecInfo.getCapabilitiesForType(type).colorFormats) {
							if (format == colorFormat) {
								return codecInfo;
							}
						}
					}
				}
			}
		}
		return null;
	}
}
