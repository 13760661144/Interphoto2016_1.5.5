package cn.poco.video.utils;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cn.poco.audio.AacEnDecoder;
import cn.poco.audio.AudioUtils;
import cn.poco.audio.CommonUtils;
import cn.poco.audio.GenerateMuteAudio;
import cn.poco.audio.Resample;
import cn.poco.interphoto2.BuildConfig;
import cn.poco.video.clip.ClipMediaCore;
import cn.poco.video.process.ClipMusicTask;
import cn.poco.video.save.audio.AudioInfo;

/**
 * Created by: fwc
 * Date: 2017/8/24
 */
public class AudioHandler {

	private static final int SAMPLE_RATE = 44100;

	/**
	 * 裁剪音频
	 *
	 * @param audioPath 音频路径
	 * @param start     音频开始时间，单位毫秒
	 * @param end       音频结束时间，单位毫秒
	 * @return          音频输出路径
	 */
	public static String clipAudio(String audioPath, long start, long end) {
//		String audioPath2 = newAudioFile(audioPath);

//		if (VideoUtils.mixVideoSegment(audioPath, audioPath2, start / 1000f, end / 1000f) >= 0) {
//			return audioPath2;
//		}
//
//		return audioPath;

		if (start >= end) {
			return audioPath;
		}

		String audioPath2 = newAudioFile(audioPath);
		ClipMediaCore clipMediaCore = new ClipMediaCore(audioPath, audioPath2, start, end);
		clipMediaCore.prepare();
		long result = clipMediaCore.start();
		if (result >= 0) {
			return audioPath2;
		}

		return audioPath;
	}

	/**
	 * 裁剪音频
	 * @param mp3Path 音频路径
	 * @param start   音频开始时间，单位毫秒
	 * @param end     音频结束时间，单位毫秒
	 * @return        音频输出路径
	 */
	public static String clipMp3(String mp3Path, long start, long end) {

//		if ("meizu".equalsIgnoreCase(Build.MANUFACTURER) && "m57ac".equalsIgnoreCase(Build.MODEL)) {
//			return null;
//		}

		String audioPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
		ClipMusicTask task = new ClipMusicTask(mp3Path, start, end, audioPath);
		try {
			task.run();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			audioPath = null;
		}

		return audioPath;
	}

	/**
	 * 调整音频音量
	 *
	 * @param audioPath 音频路径
	 * @param volume    音量大小，0~1
	 * @return 音频输出路径
	 */
	public static String volumeAdjust(String audioPath, float volume) {
		String audioPath2 = newAudioFile(audioPath);
		if (AudioUtils.volumeAdjust(audioPath, audioPath2, volume)) {
			return audioPath2;
		}

		return audioPath;
	}

	/**
	 * 音频淡出处理
	 *
	 * @param audioPath 音频路径
	 * @param duration  淡出时长，单位秒
	 * @return 音频输出路径
	 */
	public static String audioFade(String audioPath, int duration) {
		String audioPath2 = newAudioFile(audioPath);
		if (VideoUtils.audioFade(audioPath, audioPath2, duration)) {
			return audioPath2;
		}

		return audioPath;
	}

	/**
	 * 延长音频
	 * 如果音频时长不够，则需要延长音频，否则最终合成时导致视频时长和音频一样
	 *
	 * @param audioPath     音频路径
	 * @param videoDuration 视频时长
	 * @return 音频输出路径
	 */
	public static String expandAudioDuration(String audioPath, long videoDuration) {
		long audioDuration = VideoUtils.getDurationFromVideo2(audioPath);

		if (audioDuration < videoDuration) {
			String audioPath2 = newAudioFile(audioPath);

			if (AudioUtils.expandAudioDuration(audioPath, audioPath2, videoDuration / 1000f, 0, audioDuration / 1000f)) {
				return audioPath2;
			}
		}

		return audioPath;
	}

	/**
	 * 确保音频时长
	 *
	 * @param audioPath 音频路径
	 * @param duration  时长
	 * @return 音频路径
	 */
	public static String ensureDuration(String audioPath, long duration) {
		long audioDuration = VideoUtils.getDurationFromVideo2(audioPath);

		if (audioDuration < duration + 20) {
			String audioPath2 = newAudioFile(audioPath);
			if (AudioUtils.expandAudioDuration(audioPath, audioPath2, duration / 1000f, 0, audioDuration / 1000f)) {
				return audioPath2;
			}
		}

		return audioPath;
	}

	/**
	 * 混合音频
	 * @param audioPath1 音频路径
	 * @param volume1    音频音量大小
	 * @param audioPath2 音频路径
	 * @param volume2    音频音量大小
	 * @param isRepeat   音频是否重复
	 * @param startEnd   音频混合开始和结束为止
	 * @return 音频输出路径
	 */
	public static String mixAudio(String audioPath1, double volume1, String audioPath2, double volume2, boolean isRepeat, double... startEnd) {
		String outputPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
		List<String> paths = new ArrayList<>();
		paths.add(audioPath2);
		List<Double> volumes = new ArrayList<>();
		volumes.add(volume2);
		if (!AudioUtils.mixAudioNew(audioPath1, volume1, outputPath, paths, volumes, isRepeat, startEnd)) {
			outputPath = audioPath1;
		}

		return outputPath;
	}

	/**
	 * 音频拼接
	 *
	 * @param audioPaths 音频路径列表
	 * @return 拼接完成后的音频路径
	 */
	public static String jointAudio(List<AudioInfo> audioPaths) {

		if (audioPaths == null || audioPaths.isEmpty()) {
			return null;
		}

//		int targetSampleRate = SAMPLE_RATE;
//		int sampleRate;
//		for (AudioInfo info : audioPaths) {
//			sampleRate = CommonUtils.getAudioSampleRate(info.path);
//			if (sampleRate > 0 && sampleRate < targetSampleRate) {
//				targetSampleRate = sampleRate;
//			}
//		}

		String result = FileUtils.getTempPath(FileUtils.AAC_FORMAT);

		List<String> inputPaths = new ArrayList<>();
		String tempPath;
		for (AudioInfo info : audioPaths) {
			tempPath = ensureDuration(info.path, info.duration);
			inputPaths.add(tempPath);
		}
		boolean success = AudioUtils.jointAuido(inputPaths, result);
		if (!success) {
			FileUtils.delete(result);
			return null;
		}

		return result;
	}

	private static String changeToTargetAAC(String audioPath) {

		int sampleRate = CommonUtils.getAudioSampleRate(audioPath);
		int audioChannel = CommonUtils.getAudioChannels(audioPath);
		if (audioPath.endsWith(FileUtils.AAC_FORMAT) && sampleRate == SAMPLE_RATE) {
			return audioPath;
		}

		String pcmPath = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
		boolean result = VideoUtils.audioDecode2(audioPath, pcmPath, null);
		if (result) {

			if (sampleRate != SAMPLE_RATE) {
				String tempPcmPath = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
				if (Resample.doResample(pcmPath, tempPcmPath, sampleRate, SAMPLE_RATE) >= 0) {
					pcmPath = tempPcmPath;
				} else {
					FileUtils.delete(tempPcmPath);
				}
			}

			String outputPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
			result = AacEnDecoder.encodeAAC(sampleRate, audioChannel, 16, pcmPath, outputPath) >= 0;

			if (!result) {
				FileUtils.delete(outputPath);
			} else {
				return outputPath;
			}
		}

		return null;
	}

	/**
	 * 生成一定时长的aac音频文件
	 *
	 * @param duration 时长，单位毫秒
	 * @return aac音频路径
	 */
	@Nullable
	public static String generateAAC(long duration) {
		String pcmPath = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
		int result = GenerateMuteAudio.generteMutePcm(SAMPLE_RATE, 1, 16, duration / 1000f, pcmPath);

		if (result < 0) {
			if (BuildConfig.DEBUG) {
				throw new RuntimeException("call generteMutePcm() is failed");
			}

			return null;
		}

		String aacPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
		result = AacEnDecoder.encodeAAC(SAMPLE_RATE, 1, 16, pcmPath, aacPath);
		if (result < 0) {
			if (BuildConfig.DEBUG) {
				throw new RuntimeException("call encodeAAC() is failed");
			}
			return null;
		}

		return aacPath;
	}

	/**
	 * 根据格式创建一个音频文件
	 */
	private static String newAudioFile(String file) {
		String result = null;
		if (file.endsWith(FileUtils.AAC_FORMAT)) {
			result = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
		} else if (file.endsWith(FileUtils.WAV_FORMAT)) {
			result = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
		} else if (file.endsWith(FileUtils.MP3_FORMAT)) {
			result = FileUtils.getTempPath(FileUtils.MP3_FORMAT);
		}

		return result;
	}
}
