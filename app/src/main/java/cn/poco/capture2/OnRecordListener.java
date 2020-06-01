package cn.poco.capture2;


import cn.poco.capture2.encoder.MediaMuxerWrapper;

/**
 * Created by: fwc
 * Date: 2017/10/9
 */
public interface OnRecordListener {

    void onPrepare(MediaMuxerWrapper mediaMuxerWrapper);

    void onPrepared();

    void onStart(MediaMuxerWrapper mediaMuxerWrapper);

    void onResume();

    void onProgressChange(float progress);

    void onPause();

    void onStop(boolean isValid);

	void onCompleted(long duration, String filePath);
}
