package cn.poco.audio;

import java.util.UUID;

import cn.poco.video.utils.FileUtils;

/**
 * Created by menghd on 2017/3/7 0007.
 */

public class GenerateMuteAudio {
	static {
		System.loadLibrary("audiofactory");
	}

	public static native int generteMutePcm(long sampleRate, int channels, int bit, double duration, String outputPath);

	public static boolean generteMuteWav(long sampleRate, int channels, int bit, double duration, String outputPath) {
		String tempMutePcm = FileUtils.getTempPath(FileUtils.PCM_FORMAT);
		int ret = generteMutePcm(sampleRate, channels, bit, duration, tempMutePcm);
		if (ret < 0) {
			return false;
		}

		return PcmToWav.pcmToWav(tempMutePcm, outputPath, sampleRate, channels);
	}
}
