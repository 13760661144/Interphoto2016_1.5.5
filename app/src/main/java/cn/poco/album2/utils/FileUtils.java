package cn.poco.album2.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Environment;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.beautify.BeautifyHandler;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.tianutils.CommonUtils;
import cn.poco.utils.Utils;

/**
 * Created by: fwc
 * Date: 2016/9/30
 */
public class FileUtils {

    /**
     * 图片保存的目录
     */
    public static String PHOTO_DIR;

    public static void init(Context context) {

        if (TextUtils.isEmpty(PHOTO_DIR)) {
            PHOTO_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                    ".POCO" + File.separator + "localAlbum" + File.separator +
                    context.getApplicationContext().getPackageName() + File.separator + "photo";
        }
    }

    /**
     * 上传文件并且复制文件到指定目录
     * @param dir 目录
     * @param sourceFile 源文件
     * @param filename 文件名
     * @param extension 默认扩展名
     * @param copy 当存在相同文件时是否强制复制一份
     * @return 文件路径
     */
    public static String copyFile(String dir, File sourceFile, String filename, String extension, boolean copy) {
        // 确保目录存在（如果在应用相机运行期间删除导致目录不存在）
        CommonUtils.MakeFolder(dir);

        long size = sourceFile.length();
        File file = new File(dir, filename);
        String newName;
        int index = 1;

        int pos = filename.lastIndexOf('.');
        String name;
        if (pos > -1 && pos < (filename.length() - 1)) {
            name = filename.substring(0, pos);
            extension = filename.substring(pos + 1);
        } else {
            name = filename;
        }

        while (file.exists()) {
            if (file.length() == size && !copy) {
                return file.getAbsolutePath();
            }

            newName = name + "(" + index++ + ")." + extension;
            file = new File(dir, newName);
        }

        FileUtils.fileChannelCopy(sourceFile, file);

        return file.getAbsolutePath();
    }

    /**
     * 使用文件通道的方式复制文件
     * @param s 源文件
     * @param t 复制到的新文件
     */
    public static void fileChannelCopy(File s, File t) {
        FileChannel in = null;
        FileChannel out = null;
        try {
            in = new FileInputStream(s).getChannel();
            out = new FileOutputStream(t).getChannel();
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制文件
     *
     * @param in 源文件路径
     * @param out 目标文件路径
     */
    public static void copyFile(String in, String out) {
        fileChannelCopy(new File(in), new File(out));
    }

    /**
     * 保存Bitmap为jpg图片，并返回相应的图片路径
     * @param context 上下文
     * @param bitmap Bitmap对象
     * @return 图片路径
     */
    public static String saveBitmap(Context context, Bitmap bitmap) {

        if (bitmap == null) {
            return null;
        }

        String imageName = getUUID() + ".jpg";
        init(context);
        int quality = 100;
        if(bitmap.getWidth() > 2048 && bitmap.getHeight() > 2048){
            quality = 96;
        }

        CommonUtils.MakeFolder(PHOTO_DIR);
        String path = PHOTO_DIR + File.separator + imageName;
        return Utils.SaveImg(context, bitmap, path, quality, false);
    }

    /**
     * 关闭资源
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 复制到系统相册中
     * @param context 上下文
     * @param imageInfos ImageInfo对象列表
     */
    public static void copyToSystem(Context context, List<ImageStore.ImageInfo> imageInfos) {
        if (imageInfos == null || imageInfos.isEmpty()) {
            return;
        }
        String savePhotoPath = SettingInfoMgr.GetSettingInfo(context).GetPhotoSavePath();
        String filename, path;
        for (ImageStore.ImageInfo imageInfo : imageInfos) {
            filename = getUUID() + "." + getExtension(imageInfo.image);
            path = copyFile(savePhotoPath, new File(imageInfo.image), filename, "jpg", false);
            imageInfo.image = path;
            updateModifyDate(path);
            Utils.FileScan(context, path);
        }
    }

    /**
     * 更新图片保存的日期
     * @param imagePath 图片路径
     */
    public static void updateModifyDate(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
            exif.setAttribute(ExifInterface.TAG_DATETIME, sdf.format(new Date()));
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取扩展名
     * @param path 路径或文件名
     * @return 扩展名，默认jpg
     */
    public static String getExtension(String path) {
        String extension = "jpg";
        int pos = path.lastIndexOf('.');
        if (pos > -1 && pos < (path.length() - 1)) {
            extension = path.substring(pos + 1);
        }

        return extension;
    }

    /**
     * 删除指定文件，如果指定文件时目录，需要先删除该目录下的所有文件才能删除该目录
     * @param path 文件路径
     */
    public static void delete(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (!file.exists()) {
                return;
            }

            if (file.isDirectory()) {
                String[] filePaths = file.list();
                for (String filePath : filePaths) {
                    delete(path + "/" + filePath);
                }
            }

            file.delete();
        }
    }

    /**
     * 根据图片信息列表来获取Bitmap对象列表
     * @param imageInfos 图片信息列表
     * @return Bitmap对象列表
     */
    public static List<Bitmap> getBitmaps(Context context, List<ImageStore.ImageInfo> imageInfos) {
        List<Bitmap> bitmaps = new ArrayList<>();
        Bitmap bitmap;
        for (ImageStore.ImageInfo imageInfo : imageInfos) {
            bitmap = BeautifyHandler.MakeBmp(context, imageInfo.image, -1, -1);
            bitmaps.add(bitmap);
        }

        return bitmaps;
    }

    public static List<ImageStore.ImageInfo> getRestoreImageInfo(Context context, List<ImageStore.ImageInfo> imageInfos) {
        List<ImageStore.ImageInfo> results = new ArrayList<>();
        for (ImageStore.ImageInfo imageInfo : imageInfos) {
            if (!imageInfo.isSaved && AlbumUtils.canRestore(context, imageInfo.id)) {
                AlbumUtils.restore(context, imageInfo.id);
                results.add(imageInfo);
            } else if (imageInfo.isSaved) {
                results.add(imageInfo);
            }
        }
        return results;
    }

    /**
     * 获取去掉'-'符号的UUID
     * @return 去掉'-'符号的UUID
     */
    public static String getUUID(){
        String s = UUID.randomUUID().toString();
        //去掉“-”符号
        return s.substring(0, 8)+s.substring(9, 13)+s.substring(14, 18)+s.substring(19, 23)+s.substring(24);
    }
}
