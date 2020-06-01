package cn.poco.audio;

public final class SoundFactory {

	static {
		System.loadLibrary("audiofactory");
	}

	public native static String getVersionString();

	private native void setTempo(long handle, float tempo);

	private native void setPitchSemiTones(long handle, float pitch);

	private native void setSpeed(long handle, float speed);

	private native int processFile(long handle, String inputFile, String outputFile);

	public native static String getErrorString();

	private native static long newInstance();

	private native void deleteInstance(long handle);

	private long handle = 0;

	/**
	 * 支持wav格式
	 */
	public SoundFactory() {
		handle = newInstance();
	}

	public void close() {
		deleteInstance(handle);
		handle = 0;
	}

	/**
	 * @param tempo 1不改变，例如1.5加速不变调
	 */
	public void setTempo(float tempo) {
		setTempo(handle, tempo);
	}

	/**
	 * @param pitch 0不改变，例如1或-1变调不变速
	 */
	public void setPitchSemiTones(float pitch) {
		setPitchSemiTones(handle, pitch);
	}

	/**
	 * @param speed 1不改变，例如2变速又变调
	 */
	public void setSpeed(float speed) {
		setSpeed(handle, speed);
	}

	public int processFile(String inputFile, String outputFile) {
		return processFile(handle, inputFile, outputFile);
	}
}
