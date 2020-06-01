package cn.poco.watermarksync.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import cn.poco.watermarksync.model.Watermark;

/**
 * Created by Shine on 2017/3/2.
 */

public class FileUtil {
    private static final String LOG = "FileUtil";

    public static void modifyJsonFileContent(String path, Watermark watermark, boolean isDownload) {
        if (!TextUtils.isEmpty(path)) {
            String fileContent = readSpecificFileContent(path);
            String result;
            if (!isDownload) {
                result = JsonHelper.getInstacne().updateWholeWatermarkValueFromLocal(fileContent, watermark);
            } else {
                result = JsonHelper.getInstacne().updateWholeWatermarkValueFromServer(fileContent, watermark);
            }
            if (!TextUtils.isEmpty(result)) {
                writeDataToFile(path, result);
            }
        }
    }





    // 将数据写入文件
    public static void writeDataToFile(String path, String content) {
        boolean sdCardState = isSdCardStateValid();
        if (!sdCardState) {
            return;
        }
        BufferedWriter bufWriter = null;
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (!file.exists()) {
                if (file.isDirectory()) {
                    Log.i(LOG, "the input path is directory, can not save in it");
                } else {
                    File parentFile = file.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    try {
                        file.createNewFile();
                        bufWriter = appendDataToFile(false, content, path);
                    } catch (IOException e) {

                    } finally {
                        if (bufWriter != null) {
                            try {
                                bufWriter.flush();
                                bufWriter.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            } else {
                if (file.isDirectory()) {
                    Log.i(LOG, "the input path is directory, can not save in it");
                } else if (file.isFile()) {
                    try {
                        bufWriter = appendDataToFile(false, content, path);
                    } catch (IOException e) {

                    } finally {
                        if (bufWriter != null) {
                            try {
                                bufWriter.flush();
                                bufWriter.close();
                            } catch (IOException e1) {

                            }
                        }
                    }
                }
            }
        }
    }

    public static String readSpecificFileContent(String path) {
        String content = null;
        if (isSdCardStateValid()) {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream stream = null;
                FileChannel fc = null;
                try {
                    stream = new FileInputStream(path);
                    fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                    content = Charset.defaultCharset().decode(bb).toString();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (stream != null) {
                            stream.close();
                        }
                        if (fc != null) {
                            fc.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                ensureFileExist(path);
            }
        }
        return content;
    }



    public static BufferedWriter appendDataToFile(boolean isAppend, String content, String path) throws IOException{
            BufferedWriter bufWriter = new BufferedWriter(new FileWriter(path, isAppend));
            bufWriter.write(content);
        return bufWriter;
    }


    public static void deleteFile(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }


    public static void createNewFile(File file) throws IOException{
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null) {
                parentFile.mkdirs();
                file.createNewFile();
            }
        }
    }



    public static void renameFile(String path, String newNameFilePath) {
        if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(newNameFilePath)) {
            File file = new File(path);
            File file2 = new File(newNameFilePath);
            if (file.exists()) {
                file.renameTo(file2);
            }
        }
    }

    public static String changeFilePathSuffix(String path, String suffix) {
        int index = path.lastIndexOf(".");
        String newPath = null;
        if (index > 0 && index < path.length() - 1) {
            newPath = path.substring(0, index + 1).concat(suffix);
        } else if (index == -1){
            newPath = path.concat(".").concat(suffix);
        }
        return newPath;
    }



    public static String getPathSuffix(String path) {
        int index = path.lastIndexOf(".");
        String suffix = null;
        if (index > 0 && index < path.length()) {
            suffix = path.substring(index + 1);
        }
        return suffix;
    }

    public static long getFileSize(String path) {
        long size = 0;
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                size = file.length();
            }
        }
        return size;
    }






    private static boolean isSdCardStateValid (){
        String sdCardState = Environment.getExternalStorageState();
        if (sdCardState != null && sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    private static void ensureFileExist(String filePath) {
        File resFile = new File(filePath);
        if (!resFile.exists()) {
            try {
                FileUtil.createNewFile(resFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
