package cn.poco.audio;

import java.io.File;
import java.util.List;
import java.util.UUID;

import cn.poco.utils.FileUtil;
import cn.poco.video.utils.FileUtils;

/**
 * Created by menghd on 2017/2/15 0015.
 * wav 文件拼接
 */

public class SoundJoint {
    static {
        System.loadLibrary("audiofactory");
    }

    /**
     * wav 文件拼接(注意：wave头信息必须相同)
     * @param inputFile1
     * @param inputFile2
     * @param outputFile
     * @return
     */
    public static native int joint(String inputFile1, String inputFile2, String outputFile);

    /**
     * 不会为空
     * @param inputFile
     * @return [samplerate][cahnnels][bit]
     */
    public static native int[] getWavHead(String inputFile);

    public static int joint(String outputPath , List<String> wavList){
        int result ;
        if(wavList == null || wavList.size() < 2 ){
            return -1;
        }

        if(wavList.size() == 2){
            result = joint(wavList.get(0),wavList.get(1),outputPath);
            if(result < 0){
                FileUtils.delete(outputPath);
                return -1;
            }
        }else {
            String tempOutputWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
            for (int i = 0 ; i < wavList.size() -1; i ++){
                if(i == 0){
                    result = joint(wavList.get(0), wavList.get(1),tempOutputWav);
                    if(result < 0){
                        FileUtils.delete(tempOutputWav);
                        return -1;
                    }
                } else if(i == wavList.size() - 2) {
                    result = joint(tempOutputWav , wavList.get(i + 1),outputPath);
                    FileUtils.delete(tempOutputWav);
                    if(result < 0){
                        FileUtils.delete(outputPath);
                        return -1;
                    }
                } else {
                    String tempWav = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
                    result = joint(tempOutputWav,wavList.get(i + 1),tempWav);
                    FileUtils.delete(tempOutputWav);
                    if(result < 0){
                        FileUtils.delete(tempWav);
                        return -1;
                    }
                    tempOutputWav = tempWav;
                }
            }
        }
        return 1;
    }
}
