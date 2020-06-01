package cn.poco.audio;

public class AacEnDecoder {

    static {
        System.loadLibrary("audiofactory");
    }

    public static native long getSamplerate(String inputFilePath);

    /**
     * 解码AAC音频为PCM
     */
//    public static native int decodeAAC(String inputFilePath , String outputFilePath);
    public static native int decodeAAC1(String inputFilePath , String outputFilePath);

    public static native int getChannels(String inputFilePath);


    @Deprecated
    public static native int AACEncoderOpen(String outputFilePath, int sampleRate, int channels);
    @Deprecated
    public static native int AacWrite(byte[] Buffer, int BufferSize);
    @Deprecated
    public static native int AACEncoderClose();


    /**
     * 编码PCM为AAC
     * @param sampleRate 44100;  // 采样率
     * @param channels nChannels = 2;         // 声道数
     * @param bit nBit = 16;             // 单样本位数
     * @param pcmInput
     * @param aacOutput
     * @return
     */
    public static native int encodeAAC(long sampleRate , int channels , int bit , String pcmInput, String aacOutput);
}
