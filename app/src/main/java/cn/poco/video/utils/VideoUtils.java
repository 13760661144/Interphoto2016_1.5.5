package cn.poco.video.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.poco.audio.AacEnDecoder;
import cn.poco.audio.AudioUtils;
import cn.poco.audio.CommonUtils;
import cn.poco.audio.MP3DeEncode;
import cn.poco.audio.PcmAfade;
import cn.poco.audio.PcmWav;
import cn.poco.interphoto2.BuildConfig;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.FileUtil;
import cn.poco.video.NativeUtils;
import cn.poco.video.decode.AudioDecoderCore;
import cn.poco.video.helper.controller.MediaController;
import cn.poco.video.render.PlayRatio;

/**
 * Created by: fwc
 * Date: 2017/5/18
 * 封装一个视频工具类，方便调用底层函数处理
 */
public class VideoUtils {

	public static boolean DEBUG = BuildConfig.DEBUG;
	public static final float SMAXRATIO = 2.4f;

	private VideoUtils() {

	}

	/**
	 * 获取视频时长
	 *
	 * @param videoPath 视频路径
	 * @return 视频时长，单位毫秒
	 */
	public static long getDurationFromVideo2(String videoPath) {
		if (BuildConfig.DEBUG && !FileUtils.isFileExist(videoPath)) {
			shouldThrowException("the video path is not correct.");
			return 0;
		}

		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		String duration = null;
		try {
			mmr.setDataSource(videoPath);
			duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			return Long.valueOf(duration);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			return (long)(NativeUtils.getDurationFromFile(videoPath) * 1000);
		} finally {
			mmr.release();
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Nullable
	public static VideoInfo getVideoInfo(String videoPath) {
		VideoInfo info = null;
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		String width, height, duration, rotation;
		try {
			mmr.setDataSource(videoPath);
			width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
			height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
			duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			rotation = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

			info = new VideoInfo();
			info.width = Integer.valueOf(width);
			info.height = Integer.valueOf(height);
			info.duration = Long.valueOf(duration);
			info.rotation = Integer.valueOf(rotation);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mmr.release();
		}

		return info;
	}

	/**
	 * 修改视频旋转角度
	 *
	 * @param videoPath  视频路径
	 * @param outputPath 视频输出路径
	 * @param rotation   旋转角度
	 * @return 是否成功
	 */
	public static boolean changeVideoRotation(String videoPath, String outputPath, int rotation) {
		if (!FileUtils.isFileExist(videoPath) || outputPath == null) {
			shouldThrowException("the video path is not correct.");
			return false;
		}
		int result;
		String aacTempPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
		result = NativeUtils.getAACFromVideo(videoPath, aacTempPath);
		if (result < 0) {
			FileUtils.delete(aacTempPath);
			return false;
		}
		result = NativeUtils.setMp4Rotation(videoPath, aacTempPath, outputPath, String.valueOf(rotation), 0);
		FileUtils.delete(aacTempPath);
		if (result < 0) {
			FileUtils.delete(outputPath);
			return false;
		}
		return true;
	}

	/**
	 * 获取时间点的视频的一帧
	 *
	 * @param videoPath 视频路径
	 * @param time      时间点，单位秒
	 * @return Bitmap对象，可能为null
	 */
	@Nullable
	public static Bitmap decodeFrameByTime(String videoPath, int time) {
		return NativeUtils.decodeFrameBySeekTime(videoPath, time, 0);
	}

	public static Bitmap decodeFrameByTimeAndroidApi(String videoPath, long timeInMills) {
		MediaMetadataRetriever mediaMetadataRetriever = null;
		Bitmap bitmap = null;
		try {
			mediaMetadataRetriever = new MediaMetadataRetriever();
			if (FileUtil.isFileExists(videoPath)) {
				mediaMetadataRetriever.setDataSource(videoPath);
				bitmap = mediaMetadataRetriever.getFrameAtTime(timeInMills * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (mediaMetadataRetriever != null)
					mediaMetadataRetriever.release();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}

	/**
	 * 释放视频相关资源
	 *
	 * @return 是否成功
	 */
	public static boolean releaseVideoResource() {
		return NativeUtils.cleanVideoGroupByIndex(0) >= 0;
	}

	public static String clipVideo(String videoPath, long endTime) {
		String outputPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
		float result;
		result = mixVideoSegment(videoPath, outputPath, 0,endTime / 1000f);
//		NativeUtils.endMixing();
		if (result < 0) {
			if (BuildConfig.DEBUG) {
				throw new RuntimeException("clip video fail");
			}
			outputPath = videoPath;
		}

		return outputPath;
	}

	/**
	 * 裁剪音视频
	 * @param videoPath 视频路径
	 * @param outputPath 输出路径
	 * @param startTime 裁剪开始时间，单位秒
	 * @param endTime 裁剪结束时间，单位秒
	 * @return 成功返回非负数，数值代表实际裁剪开始时间
	 */
	public static float mixVideoSegment(String videoPath, String outputPath, float startTime, float endTime) {

		float result = NativeUtils.mixVideoSegment(videoPath, outputPath, startTime, endTime);
		NativeUtils.endMixing();
		return result;

//		float result;
//		try {
//			long start = (long)(startTime * 1000 + 0.5f);
//			long end = (long)(endTime * 1000 + 0.5f);
//			ClipMediaCore clipMediaCore = new ClipMediaCore(videoPath, outputPath, start, end);
//			clipMediaCore.prepare();
//			result = clipMediaCore.start();
//			if (result != -1f) {
//				result /= 1000f;
//			}
//		} catch (Throwable e) {
//			e.printStackTrace();
//			result = -1;
//		}
//
//		return result;
	}

	/**
	 * 视频声音和音频混合
	 *
	 * @param videoPath       视频路径
	 * @param volume          视频音量，音量范围[0,1], 1是原始音量大小，小于1则使音量变小，0为无声
	 * @param bgMusicPath     背景音频路径
	 * @param bgVolume        背景音频音量，音量范围[0,1], 1是原始音量大小，小于1则使音量变小，0为无声
	 * @param isRepeat        背景音乐是否重复播放
	 * @param outputPath      音频输出路径
	 * @param bgMusicStartEnd 背景音频列表对应的起始、终止位置在主文件的百分比，范围：0~1
	 * @return 是否成功
	 */
	public static boolean mixBgAudioNew(String videoPath, double volume, String bgMusicPath, double bgVolume, boolean isRepeat, String outputPath, double... bgMusicStartEnd) {

		if (!FileUtils.isFileExist(videoPath) || outputPath == null) {
			shouldThrowException("the video path is not correct.");
			return false;
		}

		if (bgMusicStartEnd == null || bgMusicStartEnd.length < 2) {
			shouldThrowException("the times is not correct.");
			return false;
		}

		List<String> bgMusicList = new ArrayList<>();
		bgMusicList.add(bgMusicPath);

		List<Double> volumeList = new ArrayList<>();
		volumeList.add(bgVolume);

		String audioInputFile = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
		int result = NativeUtils.getAACFromVideo(videoPath, audioInputFile);
		File file = new File(audioInputFile);
		if (result < 0 || !file.exists() || file.length() == 0) {
			// 获取视频文件的音频流失败
			FileUtils.delete(audioInputFile);
			return false;
		}

		if (!AudioUtils.mixAudioNew(audioInputFile, volume, outputPath, bgMusicList, volumeList, isRepeat, bgMusicStartEnd)) {
			return false;
		}

		return true;
	}

	/**
	 * 替换视频声音
	 *
	 * @param videoPath  视频路径
	 * @param aacPath    aac文件路径
	 * @param outputPath 视频输出路径
	 * @return 是否成功
	 */
	public static boolean replaceAudio(String videoPath, String aacPath, String outputPath) {

		if (BuildConfig.DEBUG && !FileUtils.isFileExist(videoPath) || outputPath == null) {
			shouldThrowException("the video path is not correct.");
			return false;
		}

		String aacTempPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);

		if (TextUtils.isEmpty(aacPath)) {
			// 去掉音频信息
			aacTempPath = "";
		} else {
			if (!changeToAac(aacPath, aacTempPath)) {
				FileUtils.delete(aacTempPath);
				aacTempPath = "";
			}
		}

		int result = NativeUtils.muxerMp4(videoPath, aacTempPath, outputPath, 0);
		if (result < 0) {
			FileUtils.delete(outputPath);
			return false;
		}

		if (!TextUtils.isEmpty(aacTempPath)) {
			FileUtils.delete(aacTempPath);
		}

		return true;
	}

	/**
	 * 把音频解码为pcm数据，软解码
	 *
	 * @param audioPath    音频文件路径
	 * @param outputPath   pcm文件输出路径
	 * @param outputParams 可选输出参数，大小为2，第一个是采样率，第二个是声道数
	 * @return 是否成功
	 */
	public static boolean audioDecode2(String audioPath, String outputPath, @Nullable int[] outputParams) {

		if (audioPath.endsWith(FileUtils.PCM_FORMAT)) {
			cn.poco.album2.utils.FileUtils.copyFile(audioPath, outputPath);
			return true;
		}

		if (audioPath.endsWith(FileUtils.AAC_FORMAT)) {
			int result = AacEnDecoder.decodeAAC1(audioPath, outputPath);
			if (result < 0) {
				FileUtils.delete(outputPath);
				return false;
			}

			return true;

		} else if (audioPath.endsWith(FileUtils.MP3_FORMAT)) {
			if (outputParams != null) {
				outputParams[0] = MP3DeEncode.getSamplerate(audioPath);
				outputParams[1] = MP3DeEncode.getChannels(audioPath);
			}
			int result = MP3DeEncode.decode(audioPath, outputPath);
			if (result < 0) {
				FileUtils.delete(outputPath);
				return false;
			}

			return true;
		} else if (audioPath.endsWith(FileUtils.WAV_FORMAT)) {
			if (PcmWav.wavToPcm(audioPath, outputPath) < 0) {
				FileUtils.delete(outputPath);
				return false;
			}

			return true;
		}

		return audioDecode(audioPath, outputPath, outputParams);
	}

	/**
	 * 把音频解码为pcm数据，硬解码
	 *
	 * @param audioPath    音频文件路径
	 * @param outputPath   pcm文件输出路径
	 * @param outputParams 可选输出参数，大小为2，第一个是采样率，第二个是声道数
	 * @return 是否成功
	 */
	public static boolean audioDecode(String audioPath, String outputPath, @Nullable int[] outputParams) {
		if (!FileUtils.isFileExist(audioPath) || outputPath == null) {
			shouldThrowException("the audio path is not correct.");
			return false;
		}

		if (audioPath.endsWith(FileUtils.PCM_FORMAT)) {
			cn.poco.album2.utils.FileUtils.copyFile(audioPath, outputPath);
			return true;
		}

		boolean result = false;
		AudioDecoderCore audioDecoder = new AudioDecoderCore(audioPath, outputPath, outputParams);
		try {
			audioDecoder.prepare();
			audioDecoder.start();

			result = true;
		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			audioDecoder.release();
		}

		return result;
	}

	/**
	 * 将音频文件转换为aac格式
	 *
	 * @param audioPath  音频文件路径
	 * @param outputPath aac文件输出路径
	 * @return 是否成功
	 */
	public static boolean changeToAac(String audioPath, String outputPath) {
		if (!FileUtils.isFileExist(audioPath) || outputPath == null) {
			shouldThrowException("the audio path is not correct.");
			return false;
		}

		if (audioPath.endsWith(FileUtils.AAC_FORMAT)) {
			cn.poco.album2.utils.FileUtils.copyFile(audioPath, outputPath);
			return true;
		}

		String pcmTempPath = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
		int[] params = new int[2];
		boolean result = audioDecode2(audioPath, pcmTempPath, params);
		if (result) {
			result = AacEnDecoder.encodeAAC(params[0], params[1], 16, pcmTempPath, outputPath) >= 0;

			if (!result) {
				FileUtils.delete(outputPath);
			}
		}

		FileUtils.delete(pcmTempPath);

		return result;
	}

	/**
	 * 添加视频到媒体库
	 *
	 * @param context   上下文
	 * @param videoPath 视频路径
	 * @return 保存的视频路径Uri
	 */
	public static Uri addVideoToMedia(Context context, String videoPath) {
		MediaController.getInstance(context).addVideoInfo(videoPath);
		return FileUtils.fileScanVideo(context, videoPath);
	}

	/**
	 * 获取视频保存路径
	 * @param context 上下文
	 * @return 视频路径
	 */
	public static String getVideoSavePath(Context context) {
		String dir;
		if ("vivo".equalsIgnoreCase(Build.MANUFACTURER)) {
			dir = Environment.getExternalStorageDirectory().toString() + File.separator + "相机";
		} else {
			dir = SettingInfoMgr.GetSettingInfo(context).GetPhotoSavePath();
		}

		cn.poco.tianutils.CommonUtils.MakeFolder(dir);
		return dir + File.separator + "InterPhoto_" + new Date().getTime() + ".mp4";
	}

	private static void shouldThrowException(String message) {

		if (DEBUG) {
			throw new RuntimeException(message);
		}
	}

	/**
	 * 判断视频的比例是否符合要求
	 *
	 * @param width  视频宽
	 * @param height 视频高
	 * @return true: 符合要求
	 */
	public static boolean isVideoRatioValid(int width, int height) {
		if (width < 0 || height < 0) {
			return false;
		}
		int max = Math.max(width, height);
		int min = Math.min(width, height);
		float videoRatio = max / (float)min;
		return videoRatio <= SMAXRATIO;
	}

	/**
	 * 先调用 {@link #isVideoRatioValid(int,int)}
	 * @param width
	 * @param height
	 * @return
	 */
	public static int getVideoRatio(int width, int height)
	{
		int r = PlayRatio.RATIO_1_1;
		float f = width * 1.0f / height;
		float ratio1 = 9 * 1.0f /16;
		float ratio2 = 1;
		float ratio3 = 16 * 1.0f /9;
		float ratio4 = 2.35f /1;
		if(f < (ratio1+ratio2)/2){
			r = PlayRatio.RATIO_9_16;
		}else if(f < (ratio2 + ratio3)/2){
			r = PlayRatio.RATIO_1_1;
		}else if(f < (ratio3 + ratio4)/2){
			r = PlayRatio.RATIO_16_9;
		}else{
			r = PlayRatio.RATIO_235_1;
		}
		return r;
	}

	/**
	 * 音频淡出处理
	 * @param audioPath 音频文件路径
	 * @param outputPath 音频输出路径
	 * @param duration 持续时间，单位s
	 * @return 是否成功
	 */
	public static boolean audioFade(String audioPath, String outputPath, int duration) {

		if (!FileUtils.isFileExist(audioPath) || outputPath == null) {
			shouldThrowException("the audio path is not correct.");
			return false;
		}

		String tempPcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
		String tempOutPcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);

		int sampleRate = CommonUtils.getAudioSampleRate(audioPath);
		int channels = CommonUtils.getAudioChannels(audioPath);

		boolean result = CommonUtils.getAudioPcm(audioPath, tempPcm);
		if (!result || sampleRate < 1 || channels < 1) {
			FileUtils.delete(tempPcm);
			return false;
		}

		if (PcmAfade.afadeout(tempPcm, tempOutPcm, duration, sampleRate, 16, channels) < 0) {
			FileUtils.delete(tempPcm);
			FileUtils.delete(tempOutPcm);
			return false;
		}

		FileUtils.delete(tempPcm);
		result = CommonUtils.encodeAudio(tempOutPcm, outputPath, sampleRate, channels);
		if (!result) {
			FileUtils.delete(tempOutPcm);
			FileUtils.delete(outputPath);
			return false;
		}

		FileUtils.delete(tempOutPcm);
		return true;
	}

	public static boolean isVideoFormatValid(String path) {

		if ("OPPO A57".equalsIgnoreCase(Build.MODEL)) {
			MediaExtractor extractor = null;
			try {
				extractor = new MediaExtractor();
				extractor.setDataSource(path);
				int numTracks = extractor.getTrackCount();
				for (int i = 0; i < numTracks; i++) {
					String mime = extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME);
					if (mime.startsWith("video/")) {
						if (mime.equalsIgnoreCase("video/mp4v-es")) {
							return false;
						} else {
							break;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (extractor != null) {
					extractor.release();
				}
			}
		}

		int dotIndex = path.lastIndexOf(".");
		if (dotIndex + 1 < path.length()) {
			String suffix = path.substring(dotIndex + 1);
			String suffixUpperCase = suffix.toUpperCase();
			return suffixUpperCase.equals("MP4") || suffixUpperCase.equals("3GP");
		}
		return false;
	}

	/**
	 *
	 * @param srcBitmap 输入的图片
	 * @param currentRotation 图片的旋转角度
	 * @return 返回已经旋转过的图片
	 */
	public static Bitmap rotateVideoBitmap(Bitmap srcBitmap, int currentRotation) {
		Bitmap dstBitmap = null;
		if (srcBitmap != null) {
			Matrix matrix = new Matrix();
			if (currentRotation == 0) {
                matrix.postRotate(0);
			} else if (currentRotation == 90) {
				matrix.postRotate(90);
			} else if (currentRotation == 180) {
				matrix.postRotate(180);
			} else if (currentRotation == 270) {
				matrix.postRotate(270);
			}
			dstBitmap = Bitmap.createBitmap(srcBitmap , 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
		}
		return dstBitmap;
	}

	/**
	 *
	 * @param srcBitmap 输入的图片
	 * @param currentRotation 旋转的角度
	 * @param viewWidth view容器的宽度
	 * @param viewHeight view容器的高度
	 * @return 返回已经缩放已经旋转过的图片
	 */
	public static Bitmap scaleAndRotateVideoBitmap(Bitmap srcBitmap, int currentRotation, int viewWidth, int viewHeight) {
		Bitmap dstBitmap = null;
		if (srcBitmap != null) {
			Matrix matrix = new Matrix();
			boolean needRotate = false;

			int srcBitmapWidth = srcBitmap.getWidth();
			int srcBitmapHeight = srcBitmap.getHeight();
			if (currentRotation == 0) {
				needRotate = false;
			} else if (currentRotation == 90) {
				needRotate = true;
				matrix.postRotate(90);
			} else if (currentRotation == 180) {
				needRotate = false;
				matrix.postRotate(180);
			} else if (currentRotation == 270) {
				needRotate = true;
				matrix.postRotate(270);
			}
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(srcBitmap, srcBitmapWidth, srcBitmapHeight,true);
			float scaleX, scaleY;
			if (needRotate) {
				scaleX = (float)viewHeight / (float)srcBitmapWidth;
				scaleY = (float)viewWidth / (float)srcBitmapHeight;
			} else {
				scaleX = (float)viewWidth / (float)srcBitmapWidth;
				scaleY = (float)viewHeight / (float)srcBitmapHeight;
			}
			float scale = Math.max(scaleX, scaleY);
			srcBitmap.recycle();
			srcBitmap = null;

			matrix.postScale(scale, scale);
			dstBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
		}
		return dstBitmap;
	}

	public static Bitmap scaleVideoFrameBitmap(Bitmap srcBitmap, int dstWidth, int dstHeight) {
        Bitmap dstBitmap = null;
		if (srcBitmap != null) {
			Matrix matrix = new Matrix();

			int srcBitmapWidth = srcBitmap.getWidth();
			int srcBitmapHeight = srcBitmap.getHeight();
			Bitmap scaledBitmap = Bitmap.createScaledBitmap(srcBitmap, srcBitmapWidth, srcBitmapHeight,true);
			float scaleX = (float)dstWidth / (float)srcBitmapWidth;
			float scaleY = (float)dstHeight / (float)srcBitmapHeight;
			float scale = Math.max(scaleX, scaleY);
			srcBitmap.recycle();
			srcBitmap = null;

			matrix.postScale(scale, scale);
			dstBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
		}
		return dstBitmap;


	}

	/**
	 * 是否支持该视频播放
	 * @param width 视频的宽度
	 * @param height 视频的高度
	 * @return true: 支持视频播放
	 */
	public static boolean isSupport(int width, int height) {

		boolean isSamsung = "samsung".equalsIgnoreCase(Build.MANUFACTURER);
		if (isSamsung && ShareData.m_screenWidth <= 720) {
			int w = Math.min(width, height);
			int h = Math.max(width, height);
			return w <= ShareData.m_screenRealWidth || h <= ShareData.m_screenRealHeight;
		}

		int maxSize = (int)(Math.max(ShareData.m_screenRealWidth, ShareData.m_screenRealHeight) * 1.8f);

		return width <= maxSize && height <= maxSize;
	}

	public static class VideoInfo {
		public int width;
		public int height;
		public long duration;
		public int rotation;
	}
}
