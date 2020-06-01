package cn.poco.audio;

import java.util.List;

import cn.poco.album2.utils.FileUtils;

/**
 * Created by menghd on 2017/6/26 0026.
 */
public class CommonUtils {

	/**
	 * 解码音频文件(文件名必须包含正确的后缀)  WAV MP3 AAC
	 *
	 * @param inputFile
	 * @param outputFile
	 * @return
	 */
	public static boolean getAudioPcm(String inputFile, String outputFile) {
		String[] fomponents = inputFile.toLowerCase().split("\\.");
		if (fomponents.length < 2) {
			return false;
		}

		int result = -1;

		if (fomponents[fomponents.length - 1].equals("aac")) {
			result = AacEnDecoder.decodeAAC1(inputFile, outputFile);
		}
		if (fomponents[fomponents.length - 1].equals("wav")) {
			result = PcmWav.wavToPcm(inputFile, outputFile);
		}
		if (fomponents[fomponents.length - 1].equals("mp3")) {
			result = MP3DeEncode.decode(inputFile, outputFile);
		}

		return result > -1;
	}

	public static int getAudioSampleRate(String inputFile) {
		String[] fomponents = inputFile.toLowerCase().split("\\.");
		if (fomponents.length < 2) {
			return 0;
		}

		int result = -1;

		if (fomponents[fomponents.length - 1].equals("aac")) {
			result = (int)AacEnDecoder.getSamplerate(inputFile);
		}
		if (fomponents[fomponents.length - 1].equals("wav")) {
			int[] ret = SoundJoint.getWavHead(inputFile);
			if (ret != null && ret.length > 0) {
				result = (int)ret[0];
			} else {
				return 0;
			}
		}
		if (fomponents[fomponents.length - 1].equals("mp3")) {
			result = MP3DeEncode.getSamplerate(inputFile);
		}

		return result;
	}

	public static int getAudioChannels(String inputFile) {
		String[] fomponents = inputFile.toLowerCase().split("\\.");
		if (fomponents.length < 2) {
			return 0;
		}

		int result = -1;

		if (fomponents[fomponents.length - 1].equals("aac")) {
			result = AacEnDecoder.getChannels(inputFile);
		}
		if (fomponents[fomponents.length - 1].equals("wav")) {
			int[] ret = SoundJoint.getWavHead(inputFile);
			if (ret != null && ret.length > 1) {
				result = (int)ret[1];
			} else {
				return 0;
			}
		}
		if (fomponents[fomponents.length - 1].equals("mp3")) {
			result = MP3DeEncode.getChannels(inputFile);
		}

		return result;
	}

	/**
	 * 编码音频文件(文件名必须包含正确的后缀)  WAV MP3 AAC
	 *
	 * @param inputFile
	 * @param outputFile
	 * @param sampleRate
	 * @param channels
	 * @return
	 */
	public static boolean encodeAudio(String inputFile, String outputFile, int sampleRate, int channels) {
		String[] fomponents = outputFile.toLowerCase().split("\\.");
		if (fomponents.length < 2) {
			return false;
		}

		int result = -1;

		if (fomponents[fomponents.length - 1].equals("aac")) {
			result = AacEnDecoder.encodeAAC(sampleRate, channels, 16, inputFile, outputFile);
		}
		if (fomponents[fomponents.length - 1].equals("wav")) {
			return PcmToWav.pcmToWav(inputFile, outputFile, sampleRate, channels);
		}
		if (fomponents[fomponents.length - 1].equals("mp3")) {
			result = MP3DeEncode.encode(inputFile, outputFile, sampleRate, channels);
		}

		return result >= 0;
	}

	/**
	 * 将音频文件转成WAV格式(文件名必须包含正确的后缀)  WAV MP3 AAC
	 *
	 * @param inputFile  WAV MP3 AAC格式
	 * @param outputFile WAV格式
	 * @return
	 */
	public static boolean audioToWav(String inputFile, String outputFile) {
		String[] fomponents = inputFile.toLowerCase().split("\\.");
		if (fomponents.length < 2) {
			return false;
		}

		if (fomponents[fomponents.length - 1].equals("aac")) {
			int result = AudioUtils.aacToWav(inputFile, outputFile);
			return result > -1;
		}

		if (fomponents[fomponents.length - 1].equals("mp3")) {
			return AudioUtils.mp3ToWav(inputFile, outputFile);
		}

		if (fomponents[fomponents.length - 1].equals("wav")) {
			FileUtils.copyFile(inputFile, outputFile);
			return true;
		}

		return false;
	}

	/**
	 * 将音频文件转成WAV格式(文件名必须包含正确的后缀)  WAV MP3 AAC
	 *
	 * @param inputFile        WAV MP3 AAC格式
	 * @param outputFile       WAV格式
	 * @param targetSampleRate 输出的采样率
	 * @param targetChannels   输出的声道数
	 * @return
	 */
	public static boolean audioToWav(String inputFile, String outputFile, int targetSampleRate, int targetChannels) {
		String[] fomponents = inputFile.toLowerCase().split("\\.");
		if (fomponents.length < 2) {
			return false;
		}
		int sampleRate = getAudioSampleRate(inputFile);
		int channels = getAudioChannels(inputFile);

		if (sampleRate < 1 || channels < 1) {
			return false;
		}
		String tempInPcm = cn.poco.video.utils.FileUtils.getTempPath(cn.poco.video.utils.FileUtils.PCM_FORMAT);
		String tempOutPcm = cn.poco.video.utils.FileUtils.getTempPath(cn.poco.video.utils.FileUtils.PCM_FORMAT);

		int result = -1;
		if (fomponents[fomponents.length - 1].equals("aac")) {
			result = AacEnDecoder.decodeAAC1(inputFile, tempInPcm);
		}

		if (fomponents[fomponents.length - 1].equals("mp3")) {
			result = MP3DeEncode.decode(inputFile, tempInPcm);
		}

		if (fomponents[fomponents.length - 1].equals("wav")) {
			if (sampleRate == targetSampleRate && channels == targetChannels) {
				FileUtils.copyFile(inputFile, outputFile);
				return true;
			}
			result = PcmWav.wavToPcm(inputFile, tempInPcm);
		}

		if (result < 0) {
			return false;
		}
		boolean ret = Resample.reSamplePateChannels(tempInPcm, tempOutPcm, sampleRate, targetSampleRate, channels, targetChannels);
		if (!ret) {
			return false;
		}
		ret = PcmToWav.pcmToWav(tempOutPcm, outputFile, targetSampleRate, targetChannels);
		return ret;
	}

	public static boolean wavToAudio(String inputFile, String outputFile) {
		String[] fomponents = outputFile.toLowerCase().split("\\.");
		if (fomponents.length < 2) {
			return false;
		}
		int result = -1;

		if (fomponents[fomponents.length - 1].equals("aac")) {
			result = AudioUtils.wavToAac(inputFile, outputFile);
		}

		if (fomponents[fomponents.length - 1].equals("mp3")) {
			return AudioUtils.wavToMp3(inputFile, outputFile);
		}

		if (fomponents[fomponents.length - 1].equals("wav")) {
			FileUtils.copyFile(inputFile, outputFile);
			return true;
		}

		return result > -1;
	}

	/**
	 * wav音频拼接
	 * @param inputFiles 输入音频路径列表(wav格式)
	 * @param outputFile 输出音频路径(wac格式)
	 * @return 是否成功
	 */
	public static boolean jointWav(List<String> inputFiles, String outputFile) {

		if (inputFiles.size() == 1) {
			FileUtils.copyFile(inputFiles.get(0), outputFile);
			return true;
		}

		return SoundJoint.joint(outputFile, inputFiles) >= 0;
	}
}
