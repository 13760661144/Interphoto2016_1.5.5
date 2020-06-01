package cn.poco.audio;

/**
 * Created by: fwc
 * Date: 2017/7/7
 */
public class PcmAfade {

	static {
		System.loadLibrary("audiofactory");
	}

	/**
	 * 淡入
	 *
	 * @param time 持续时间
	 */
	public static native int afadein(String inputFilePath, String outputFile, double time, long sample_rate, int bit, int channel);

	/**
	 * 淡出
	 *
	 * @param time 持续时间
	 */
	public static native int afadeout(String inputFilePath, String outputFile, double time, long sample_rate, int bit, int channel);
}
