package cn.poco.audio;

import java.util.UUID;

import cn.poco.album2.utils.FileUtils;

/**
 * Created by menghd on 2017/6/21 0021.
 * 重采样
 */

public class Resample {

    static {
        System.loadLibrary("audiofactory");
    }

    /**
     * 执行重采样
     */
    public static native int doResample(String inputFilePath, String outputFilePath, int inSample , int outSample);

    /**
     * 立体声转单声道
     */
    public static native int stereoToMono(String inputFilePath, String outputFilePath);

    /**
     * 单声道转双声道（假立体声）
     */
    public static native int monoToStereo(String inputFilePath, String outputFilePath);

    public static int doReChannels(String inputFilePath, String outputFilePath , int inChannels , int outChannels){
        if(inChannels ==1 && outChannels ==2){
            return monoToStereo(inputFilePath,outputFilePath);
        }

        if(inChannels == 2 && outChannels == 1){
            return stereoToMono(inputFilePath,outputFilePath);
        }
        return -1;
    }

    public static boolean reSamplePateChannels(String inputFilePath, String outputFilePath, int inSample , int outSample, int inChannels , int outChannels){
        if(inSample == outSample){
            if(inChannels == outChannels){
                FileUtils.copyFile(inputFilePath, outputFilePath);
                return true;
            } else {
                return doReChannels(inputFilePath,outputFilePath,inChannels,outChannels) > -1;
            }
        } else {
            if(inChannels == outChannels){
                return doResample(inputFilePath,outputFilePath,inSample,outSample) > -1;
            } else {
                String temp = cn.poco.video.utils.FileUtils.getTempPath(cn.poco.video.utils.FileUtils.PCM_FORMAT);
                doResample(inputFilePath, temp, inSample,outSample);
                return doReChannels(temp, outputFilePath, inChannels, outChannels) > -1;
            }
        }
    }
}
