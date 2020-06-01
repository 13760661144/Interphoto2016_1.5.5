package cn.poco.video;

import android.graphics.Bitmap;

public class NativeUtils {

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("SDL2main");
    }

    /**
     * 获取视频的旋转角度
     * @param videoin 输入视频的路径
     * @return 成功返回 >=0 的数
     */
    public static native int getRotateAngleFromFile(String videoin);

    /** 修改视频旋转角度信息，如果framerate为0，则按照原本MP4帧率
     *
     * @param MP4PATH 视频路径
     * @param AACPATH AACPATH AAC/mp3路径，  如果不想添加音频，可以直接用空字符""传入
     * @param OUTPUTMP4 输出的视频路径
     * @param degree 旋转角度
     * @param framerate 融合后视频的帧率，如果想依照原来视频的帧率，可以直接填0
     * @return 成功返回 >=0 的数
      */
    public static native int setMp4Rotation(String MP4PATH, String AACPATH, String OUTPUTMP4, String degree, int framerate);

    /**
     * 获取视频的帧率
     * @param video_in 输入的视频
     * @return 视频的帧率
     */
    public static native float getFPSFromFile(String video_in);

    /**
     * 获取视频的帧数(新方法 ，获取准确）
     * @param mp4in 输入的视频
     * @return 视频的帧数
     */
    public static native int getFrameNumFromFile2(String mp4in);

    /**
     * 根据视频获取Buffer大小
     * @param videopath 输入的视频
     * @return 视频Buffer大小
     */
    public static native int getVideoBufferSize(String videopath);

    /**
     * 获取一帧RGB数据，并存到到Buffer中
     * @param video_in  输入视频路径
     * @param videoindex  视频编号
     * @param width  Integer width = new Integer(-1)   函数运行后返回视频宽高存放的变量
     * @param height  Integer height = new Integer(-1)   函数运行后返回视频宽高存放的变量
     * @param buffer 返回的视频帧数据
     * @return 返回当前帧的时间，单位为秒
     */
    public static native float getNextFrameWithTimeFromFile(String video_in, int videoindex, Integer width, Integer height, byte[] buffer);

    /**
     * 获取一帧数据，返回Bitmap
     * @param video_in 输入的视频
     * @param seektime 某一位置，单位s
     * @param videoindex 视频下标
     * @return Bitmap对象
     */
    public static native Bitmap decodeFrameBySeekTime(String video_in, int seektime, int videoindex);

    /**
     * 关闭组内index视频的组件, 清理组内某个视频的资源
     * @param index 视频下标
     * @return 成功返回 >=0 的数
     */
    public static native int cleanVideoGroupByIndex(int index);

    /**
     * 关闭组内所有视频的组件
     * @return 成功返回 >=0 的数
     */
    public static native int endDecodeFrameBySeekTime();

    /**
     * 获取视频的时长
     * @param mp4in 输入的视频
     * @return 视频的长度，单位s
     */
    public static native float getDurationFromFile(String mp4in);

    /**
     * 截取视频/音频的某一段，追加到新文件中，当对新文件追加结束想保存为文件后，要调用endMixing()
     * @param in_video 输入视频路径
     * @param out_video 输出视频路径
     * @param StartTime 开始截取的时间，单位S
     * @param EndTime 结束截取的时间，单位S
     * @return 成功返回非负数 ，值为视频开始截取的真正时间
     */
    public static native float mixVideoSegment(String in_video, String out_video, float StartTime, float EndTime);

    /**
     * 结束截取视频，生成最终out_video，配合mixVideoSegment使用
     * @return 成功返回非负数
     */
    public static native int endMixing();

    /**
     * 获取视频文件的音频流保存为AAC
     * @param video_in 输入的视频
     * @param aac_out 输出的aac文件路径
     * @return 成功返回非负数
     */
    public static native int getAACFromVideo(String video_in, String aac_out);

    /**
     * 把H264/MP4文件与AAC/MP3融合
     * @param MP4PATH 输入的视频
     * @param AACPATH AAC/mp3路径，如果不想添加音频，可以直接用空字符""传入
     * @param OUTPUTMP4 输出的视频
     * @param framerate 融合后视频的帧率，如果想依照原来视频的帧率，可以直接填0
     * @return 成功返回非负数
     */
    public static native int muxerMp4(String MP4PATH, String AACPATH, String OUTPUTMP4, int framerate);   //把H264或者MP4文件与AAC融合,如果framerate为0，则按照原本MP4帧率

    /**
     * 获取一帧RGBA数据，并存到到buffer中
     * @param video_in  输入视频路径
     * @param videoindex  视频编号
     * @param width  Integer width = new Integer(-1)   函数运行后返回视频宽高存放的变量
     * @param height  Integer height = new Integer(-1)   函数运行后返回视频宽高存放的变量
     * @param buffer
     * @return 返回当前帧的时间，单位为秒
     */
    public static native float getNextFrameRGBAWithTimeFromFile(String video_in, int videoindex, Integer width, Integer height, byte[] buffer);

    /**
     * 根据视频获取RGBA格式的BUFFER大小
     * @param videopath 输入视频的路径
     * @return 返回视频一帧内容所需要的内存
     */
    public static native int getVideoRGBABufferSize(String videopath);

    /**
     * 获取视频流的H264文件
     * @param MP4PATH 输入的视频路径
     * @param OUTH264PATH 输出的H264文件路径
     * @return 成功返回非负数
     */
    public static native int getH264FromFile( String MP4PATH, String OUTH264PATH);

    /**
     * 融合两个H264文件
     */
    public static native int mixH264(String H264, String DSTH264);

    /**
     * 把H264或者MP4文件与AAC融合,如果framerate为0，则按照原本MP4帧率
     */
    public static native int muxerMp4WithRotation(String MP4PATH, String AACPATH, String OUTPUTMP4, int framerate, String degree);
}
