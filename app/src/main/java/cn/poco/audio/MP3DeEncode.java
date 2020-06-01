package cn.poco.audio;

/**
 *MP3编解码
 * Created by menghd on 2017/6/9 0009.
 */

public class MP3DeEncode {
    static {
        System.loadLibrary("audiofactory");
    }

    /**
     * 获取mp3文件采样率
     * @return -1代表失败
     */
    public static native int getSamplerate(String path );

    /**
     * 获取mp3文件通道数
     * @return -1代表失败
     */
    public static native int getChannels(String path );

    /**
     * 解码mp3成pcm
     * @return -1代表失败
     */
    public static native int decode(String inPath ,String outPath);

    /**
     * 编码pcm成MP3（注意：sample、channel必须要跟pcm的远格式的一样）
     * @param inPath pcm文件输入路径
     * @param outPath MP3文件输出路径
     * @param sample 采样率
     * @param channel 通道
     * @return -1代表失败
     */
    public static native int encode(String inPath ,String outPath , int sample, int channel);
}
