package cn.poco.audio;

import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2017/5/19
 */
public class AacWavUtils {

	/**
	 * 将aac音频转换成wav音频
	 *
	 * @param aacFiltPath    aac音频文件路径
	 * @param outputFilePath wav输出文件路径
	 * @return 大于0表示转换成功
	 */
	public static int aacToWav(String aacFiltPath, String outputFilePath) {
		int result;

		String pcmTempFile = FileUtils.getTempPath(FileUtils.PCM_FORMAT);

		result = AacEnDecoder.decodeAAC1(aacFiltPath, pcmTempFile);
		if (result < 0) {
			FileUtils.delete(pcmTempFile);
			return result;
		}

		int sampleRate = (int)AacEnDecoder.getSamplerate(aacFiltPath);
		int channels = AacEnDecoder.getChannels(aacFiltPath);

		if (sampleRate < 0) {
			FileUtils.delete(pcmTempFile);
			return -1;
		}

		boolean resultPW = PcmToWav.pcmToWav(pcmTempFile, outputFilePath, sampleRate, channels);

		if (!resultPW) {
			FileUtils.delete(pcmTempFile);
			FileUtils.delete(outputFilePath);
			return -1;
		}

		FileUtils.delete(pcmTempFile);

		return result;
	}

	/**
	 * 将wav音频转换成aac音频
	 * @param wavFilePath wav音频文件路径
	 * @param outputFilePath aac输出文件路径
	 * @return 大于0表示转换成功
	 */
	public static int wavToAac(String wavFilePath, String outputFilePath) {

		int result;
		String pcmTempFile = FileUtils.getTempPath(FileUtils.PCM_FORMAT);

		result = PcmWav.wavToPcm(wavFilePath, pcmTempFile);
		if (result < 0) {
			FileUtils.delete(pcmTempFile);
			return result;
		}

		int[] wavInfo = SoundJoint.getWavHead(wavFilePath);
		if (wavInfo == null || wavInfo.length == 0) {
			return -1;
		}
		long sampleRate = wavInfo[0];
		int channels = (int)wavInfo[1];
		int bit = (int)wavInfo[2];
		if (sampleRate < 0 || channels < 0) {
			FileUtils.delete(pcmTempFile);
			return -1;
		}

		result = AacEnDecoder.encodeAAC(sampleRate, channels, bit, pcmTempFile, outputFilePath);
		FileUtils.delete(pcmTempFile);
		if (result < 0) {
			FileUtils.delete(outputFilePath);
			return result;
		}

		return result;
	}
}
