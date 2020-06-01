package cn.poco.video.videoMusic;

import cn.poco.resource.MusicRes;

/**
 * Created by lgd on 2017/11/14.
 */

public class MusicSaveInfo
{
    public float mMaxVolume = 0.6f;
    public float mCurVolume = mMaxVolume;
    public String mMusicPath;
    public int mMusicUri;      // 缩略图音乐的uri，本地音乐没有
    public int mMusicStartTime;  //毫秒单位
    public String mMusicName;
    public MusicRes mMusicRes;
}
