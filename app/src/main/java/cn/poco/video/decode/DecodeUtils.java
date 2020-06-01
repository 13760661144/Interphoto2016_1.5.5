package cn.poco.video.decode;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.Matrix;
import android.support.annotation.Nullable;

import java.io.FileInputStream;
import java.io.IOException;

import cn.poco.video.NativeUtils;
import cn.poco.video.encode.VideoEncoderCore;
import cn.poco.video.render.PlayVideoInfo;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by: fwc
 * Date: 2017/9/20
 */
public class DecodeUtils {

	@Nullable
	public static VideoInfo getVideoInfo(String videoPath) {

		if (!FileUtils.isFileExist(videoPath)) {
			throw new IllegalArgumentException("the video is not exist");
		}

		MediaExtractor extractor = null;
		String mimeType;
		int frameRate;
		int bitRate;
		int width;
		int height;
		long duration;
		try {
			extractor = new MediaExtractor();
			extractor.setDataSource(videoPath);
			int numTracks = extractor.getTrackCount();
			int trackIndex = -1;
			for (int i = 0; i < numTracks; i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				if (mime.startsWith("video/")) {
					trackIndex = i;
					break;
				}
			}
			if (trackIndex < 0) {
//				throw new RuntimeException("No video track found in " + videoPath);
				return null;
			}
			extractor.selectTrack(trackIndex);
			MediaFormat format = extractor.getTrackFormat(trackIndex);
			mimeType = format.getString(MediaFormat.KEY_MIME);

			width = format.getInteger(MediaFormat.KEY_WIDTH);
			if (format.containsKey("crop-left") && format.containsKey("crop-right")) {
				width = format.getInteger("crop-right") + 1 - format.getInteger("crop-left");
			}

			height = format.getInteger(MediaFormat.KEY_HEIGHT);
			if (format.containsKey("crop-top") && format.containsKey("crop-bottom")) {
				height = format.getInteger("crop-bottom") + 1 - format.getInteger("crop-top");
			}

//			if (format.containsKey(MediaFormat.KEY_DURATION)) {
//				duration = format.getLong(MediaFormat.KEY_DURATION) / 1000;
//			} else {
//				duration = (long)(NativeUtils.getDurationFromFile(videoPath) * 1000);
//			}

			duration = VideoUtils.getDurationFromVideo2(videoPath);

			frameRate = Math.round(NativeUtils.getFPSFromFile(videoPath));
			if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
				bitRate = format.getInteger(MediaFormat.KEY_BIT_RATE);
			} else {
				bitRate = (int)(0.25f * VideoEncoderCore.EncodeConfig.FRAME_RATE * width * height);
			}


			VideoInfo config = new VideoInfo();
			config.mimeType = mimeType;
			config.width = width;
			config.height = height;
			config.frameRate = frameRate;

			config.bitRate = bitRate;
			config.duration = duration;
			config.rotation = NativeUtils.getRotateAngleFromFile(videoPath);
			return config;

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (extractor != null) {
				extractor.release();
			}
		}

		return null;
	}

	@Nullable
	public static PlayVideoInfo getPlayVideoInfo(String videoPath) {

		VideoInfo videoInfo = getVideoInfo(videoPath);
		if (videoInfo == null) {
			return null;
		}

		PlayVideoInfo info = new PlayVideoInfo();
		info.path = videoPath;
		info.data = videoInfo;
		Matrix.setIdentityM(info.mvpMatrix, 0);

		return info;
	}

	/**
	 * 检查视频合法性
	 * @param videoPath 视频路径
	 */
	public static boolean checkVideo(String videoPath) {
		MediaExtractor extractor = null;
		FileInputStream fis = null;
		try {
			extractor = new MediaExtractor();
			fis = new FileInputStream(videoPath);
			extractor.setDataSource(fis.getFD());
			int numTracks = extractor.getTrackCount();

			int trackIndex = -1;
			for (int i = 0; i < numTracks; i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(MediaFormat.KEY_MIME);
				if (mime.startsWith("video/")) {
					trackIndex = i;
					break;
				}
			}

			return trackIndex != -1;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (extractor != null) {
				extractor.release();
				extractor = null;
			}

			FileUtils.close(fis);
		}

		return false;
	}

	public static boolean checkAudio(String videoPath) {
//		if ("meizu".equalsIgnoreCase(Build.MANUFACTURER) && "m57ac".equalsIgnoreCase(Build.MODEL)) {
//			MediaExtractor extractor = null;
//			FileInputStream fis = null;
//			try {
//				extractor = new MediaExtractor();
//				fis = new FileInputStream(videoPath);
//				extractor.setDataSource(fis.getFD());
//				int numTracks = extractor.getTrackCount();
//
//				int trackIndex = -1;
//				for (int i = 0; i < numTracks; i++) {
//					MediaFormat format = extractor.getTrackFormat(i);
//					String mime = format.getString(MediaFormat.KEY_MIME);
//					if (mime.startsWith("audio/")) {
//						trackIndex = i;
//						break;
//					}
//				}
//
//				return trackIndex != -1;
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//				if (extractor != null) {
//					extractor.release();
//					extractor = null;
//				}
//
//				FileUtils.close(fis);
//			}
//		}

		return true;
	}

	/**
	 * 选择支持mimeType的编码器信息
	 * @param mimeType 视频的MIME类型
	 * @return 编码器信息
	 */
	@Nullable
	public static MediaCodecInfo selectCodec(String mimeType) {
		int numCodecs = MediaCodecList.getCodecCount();
		for (int i = 0; i < numCodecs; i++) {
			MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
			if (codecInfo.isEncoder()) {
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
}
