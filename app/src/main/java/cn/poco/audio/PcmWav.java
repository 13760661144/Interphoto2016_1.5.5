package cn.poco.audio;

/**
 * Created by menghd on 2017/2/9 0009.
 */

public class PcmWav {
    static {
        System.loadLibrary("audiofactory");
    }

    /**
     * PCM转WAV格式
     */
//    public static native int pcmToWav(String inputFile, String outputFile,long samplerate,int channels,int bit);

    /**
     * WAV格式转PCM
     */
    public static native int wavToPcm(String inputFile, String outputFile);
}
