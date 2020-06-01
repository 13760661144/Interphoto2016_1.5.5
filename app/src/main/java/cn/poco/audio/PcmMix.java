package cn.poco.audio;

public class PcmMix {

    static {
        System.loadLibrary("audiofactory");
    }

    /**
     * 两个pcm文件混音
     * @param inputFilePath1 第一个文件
     * @param intputFilePath2 第二个文件
     * @param outputFile 输出文件
     * @param startPosPercent 第二个文件在第一个文件的百分比位置开始混合
     * @return -1 失败
     */
    public static native int mixPcm(String inputFilePath1 , String intputFilePath2, String outputFile,double startPosPercent,double endPosPercent);

    /**
     * 两个pcm文件混音，音量范围0~1，1为原始音量
     * @param inputFilePath1 第一个文件
     * @param intputFilePath2 第二个文件
     * @param outputFile 输出文件
     * @param startPosPercent 第二个文件在第一个文件的百分比位置开始混合
     * @param vol1 第一个文件的音量
     * @param vol2 第二个文件的音量
     * @return -1 失败
     */
    public static native int mixPcmVloAdjust(String inputFilePath1 , String intputFilePath2, String outputFile,double startPosPercent,double endPosPercent,double vol1,double vol2);

    /**
     * 两个pcm文件混音，音量范围0~1，1为原始音量(第二个音乐重复)
     * @param inputFilePath1 第一个文件
     * @param intputFilePath2 第二个文件
     * @param outputFile 输出文件
     * @param startPosPercent 第二个文件在第一个文件的百分比位置开始混合
     * @param vol1 第一个文件的音量
     * @param vol2 第二个文件的音量
     * @return -1 失败
     */
    public static native int mixPcmVloAdjustRepeat(String inputFilePath1 , String intputFilePath2, String outputFile,double startPosPercent,double endPosPercent,double vol1,double vol2);

    public static native void clipPcm(String inputpath,String outputpath,long samplerate, int channels , int bit ,double start,double end);

    public static native int jointPcm(String inputFilePath1 , String intputFilePath2, String outputFile);

    public static native int volAdjust(String inputFilePath , String outputFile,double vol);
}
