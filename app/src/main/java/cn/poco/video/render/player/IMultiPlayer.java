package cn.poco.video.render.player;

/**
 * Author: Comit
 * Date: 2017/12/2
 * Time: 14:54
 */
public interface IMultiPlayer {

    void setSurface(MultiSurface surface);

    void setDataSources(String... dataSources);

    void setCurrentIndex(int currentIndex);

    int getCurrentIndex();

    long getCurrentPlayPosition();

    void setVolume(float volume);

    float getVolume();

    void setLooping(boolean looping);

    void setOnMultiPlayListener(OnMultiPlayListener listener);

    void updateFrame();

    void prepare();

    void start();

    void startNext();

    void seekTo(int index, long position);

    void forceSeekTo(int index, long position);

    void changeVideoOrder(int index, long position, String... dataSources);

    void pause();

    void finishVideo();

    void reset();

    void release();

    void post(Runnable r);

    interface OnMultiPlayListener {

        void onStart(int index);

        void onFinish(int index);

        void onSeekComplete(int index, long position);

        long getDuration(int index);

        boolean isBlendTransition(int index);

        boolean isMute(int index);
    }
}
