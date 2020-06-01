package cn.poco.video.render.player;

import android.view.Surface;

/**
 * Author: Comit
 * Date: 2017/12/2
 * Time: 11:21
 */
public interface IPlayer {

    int IDLE = 0;
    int PREPARING = 1;
    int PREPARED = 2;
    int START = 3;
    int PAUSE = 4;
    int STOP = 5;
    int RELEASE = 6;

    void setSurface(Surface surface);

    void setDataSource(String dataSource);

    void setVolume(float volume);

    float getVolume();

    void setLooping(boolean looping);

    boolean isStart();

    boolean isPause();

    long getCurrentPosition();

    long getDuration();

    void seekTo(long position);

    void setOnPlayListener(OnPlayListener listener);

    void prepare();

    void start();

    void restart();

    void pause();

    void stop();

    boolean shouldFinish(long duration);

    void reset();

    void release();

    interface OnPlayListener {

        void onStart();

        void onFinish();

        void onSeekComplete(IPlayer player);
    }
}
