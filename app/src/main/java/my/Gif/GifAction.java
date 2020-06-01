package my.Gif;

public interface GifAction {

	/**
	 * gif瑙ｇ爜瑙傚療鑰?
	 * @param parseStatus 瑙ｇ爜鏄惁鎴愬姛锛屾垚鍔熶細涓簍rue
	 * @param frameIndex 褰撳墠瑙ｇ爜鐨勭鍑犲抚锛屽綋鍏ㄩ儴瑙ｇ爜鎴愬姛鍚庯紝杩欓噷涓?1
	 */
	public void parseOk(boolean parseStatus, int frameIndex);
}
