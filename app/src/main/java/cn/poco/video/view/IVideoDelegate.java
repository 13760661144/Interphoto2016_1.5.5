package cn.poco.video.view;

import cn.poco.video.model.VideoEntry;

/**
 * Created by Shine on 2017/5/18.
 */

public interface IVideoDelegate {
    void seekTo(int msec);
    void setVideoModel(VideoEntry videoEntry);

    void playVideo();

    void pauseVideo();

    void onPause();

    void resumeVideo();

    void onResume();

    void stopPlay();

    void onStop();

    void clear();




}
