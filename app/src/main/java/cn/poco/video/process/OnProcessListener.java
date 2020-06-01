package cn.poco.video.process;

/**
 * Created by: fwc
 * Date: 2017/9/29
 */
public interface OnProcessListener {

	void onStart();
	void onFinish();
	void onError(String message);
}
