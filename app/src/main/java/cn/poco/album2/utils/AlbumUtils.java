package cn.poco.album2.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.album2.LocalStore;
import cn.poco.album2.PhotoStore;
import cn.poco.beautify.BeautifyHandler;
import poco.photedatabaselib2016.info.InterHistory;
import poco.photedatabaselib2016.info.InterPhoto;
import poco.photedatabaselib2016.util.InterPhotoDBManger;

/**
 * Created by: fwc
 * Date: 2016/10/8
 */
public class AlbumUtils {

    /**
     * 在系统相册选中一张图片美化时调用
     * 将源图片和美化后的信息保存到数据库中
     * 会将Bitmap对象保存为JPG格式图片
     * 同时将源图片复制到相应目录中
     * @param context 上下文
     * @param bitmap Bitmap对象
     * @param orgPath 源图片路径
     * @param effect 图片效果
     * @return ImageInfo对象，已经添加到了本地相册数据列表
     */
    public static ImageStore.ImageInfo insertImage(Context context, Bitmap bitmap, String orgPath, String effect) {
        String finalPath = FileUtils.saveBitmap(context, bitmap);
        File image = new File(orgPath);
        String srcPath = FileUtils.copyFile(FileUtils.PHOTO_DIR, image, image.getName(), "jpg", true);
        BeautifyHandler.SaveExifInfoToImg(srcPath, finalPath);
        ImageStore.ImageInfo imageInfo = null;
        if (finalPath != null) {
            InterPhotoDBManger.connectDB(context);
            InterPhoto interPhoto = new InterPhoto();
            interPhoto.setFinalUri(finalPath);
            interPhoto.setOriginalUri(srcPath);
            interPhoto.setPhotoEffect(effect);
            interPhoto.setSaved(false);
            interPhoto.setCreateDate(System.currentTimeMillis());
            interPhoto.setUpdateDate(System.currentTimeMillis());
            int id = InterPhotoDBManger.insertPhoto(context, interPhoto);
            if (id >= 0) {
                InterHistory interHistory = new InterHistory();
                interHistory.setResUri(srcPath);
                interHistory.setPhotoId(id);
                interHistory.setPhotoEffect(null);
                interHistory.setCreateDate(System.currentTimeMillis());

                InterPhotoDBManger.insertHistory(context, interHistory);
                interPhoto.setId(id);
                imageInfo = LocalStore.addInterPhoto(context, interPhoto);
            }
            InterPhotoDBManger.closeDB(context);
        }

        return imageInfo;
    }

    /**
     * 美化完成后调用，用于保存相应美化效果
     * 会将Bitmap对象保存为JPG格式图片
     * 同时保存相应的历史记录
     * @param context 上下文
     * @param photoId 图片id
     * @param bitmap Bitmap对象
     * @param orgPath 源图片路径
     * @param effect 图片效果
     * @return ImageInfo对象，已在本地相册数据列表中修改
     */
    public static ImageStore.ImageInfo saveEffect(Context context, int photoId, Bitmap bitmap, String orgPath, String effect) {
        InterPhotoDBManger.connectDB(context);
        InterPhoto interPhoto = InterPhotoDBManger.getPhoto(context, photoId);
        ImageStore.ImageInfo imageInfo = null;
        String finalPath = FileUtils.saveBitmap(context, bitmap);
        if (interPhoto != null && finalPath != null) {
            String lastImagePath = interPhoto.getFinalUri();
            BeautifyHandler.SaveExifInfoToImg(orgPath, finalPath);

            InterHistory interHistory = new InterHistory();
            interHistory.setResUri(lastImagePath);
            if (interPhoto.getSaved()) {
//                interHistory.setResUri(interPhoto.getOriginalUri());
                interPhoto.setOriginalUri(lastImagePath);
            }

            interPhoto.setFinalUri(finalPath);

            interHistory.setPhotoId(photoId);
            interHistory.setPhotoEffect(interPhoto.getPhotoEffect());
            interHistory.setCreateDate(System.currentTimeMillis());

            interPhoto.setPhotoEffect(effect);
            interPhoto.setSaved(false);
            interPhoto.setUpdateDate(System.currentTimeMillis());

            InterPhotoDBManger.updatePhoto(context, interPhoto);
            InterPhotoDBManger.insertHistory(context, interHistory);
            LocalStore.updateInterPhoto(interPhoto);

            imageInfo = LocalStore.changeInterPhoto(interPhoto);
        }
        InterPhotoDBManger.closeDB(context);
        return imageInfo;
    }

    /**
     * 图片保存
     * 修改图片相应信息，更新数据库
     * 删除相关联的所有历史记录，并且删除相应的图片
     * @param context 上下文
     * @param photoId 图片id
     * @return ImageInfo对象
     */
    public static ImageStore.ImageInfo save(Context context, int photoId) {
        ImageStore.ImageInfo imageInfo = null;
        InterPhotoDBManger.connectDB(context);
        InterPhoto interPhoto = InterPhotoDBManger.getPhoto(context, photoId);
        if (interPhoto != null) {

            // 还没保存过的图片先更新数据库并删除所有历史记录
            if (!interPhoto.getSaved()) {
                String destPath = interPhoto.getFinalUri();
                interPhoto.setOriginalUri(null);
                interPhoto.setSaved(true);
                interPhoto.setUpdateDate(System.currentTimeMillis());

                // 更新数据库
                InterPhotoDBManger.updatePhoto(context, interPhoto);

                // 删除所有历史记录以及相应的图片
                List<InterHistory> list = InterPhotoDBManger.getHistorys(context, photoId);
                String imagePath;
                for (InterHistory history : list) {
                    imagePath = history.getResUri();
                    if (!destPath.equals(imagePath)) {
                        FileUtils.delete(imagePath);
                    }
                }
                InterPhotoDBManger.deleteHistorys(context, photoId);

                // 更新PhotoStore数据
                LocalStore.updateInterPhoto(interPhoto);
            }

            // 将InterPhoto对象转换成ImageInfo对象
            imageInfo = LocalStore.changeInterPhoto(interPhoto);

            // 复制图片到相应目录
            List<ImageStore.ImageInfo> imageInfos = new ArrayList<>();
            imageInfos.add(imageInfo);
            FileUtils.copyToSystem(context, imageInfos);
            // 更新PhotoStore中的相册数据
            PhotoStore.getInstance(context).addPhotoToAlbums(imageInfo.image);
        }
        InterPhotoDBManger.closeDB(context);

        return imageInfo;
    }

    /**
     * 判断当前图片是否可以撤销到上一步操作
     * 若当前图片为已保存状态，不可撤销
     * 若当前图片为未保存状态，根据其历史记录是否为空判断
     * @param context 上下文
     * @param photoId 图片id
     * @return true: 可以撤销到上一步操作
     */
    public static boolean canUndo(Context context, int photoId) {
        InterPhotoDBManger.connectDB(context);
        boolean b = false;
        InterPhoto interPhoto = InterPhotoDBManger.getPhoto(context, photoId);
        if (interPhoto != null) {
            List<InterHistory> list = InterPhotoDBManger.getHistorys(context, photoId);
            if (list != null && !list.isEmpty()) {
                b = true;
            }
        }
        InterPhotoDBManger.closeDB(context);
        return b;
    }

    /**
     * 撤销操作，进行此操作前先调用canUndo方法判断是否可撤销
     * 若当前图片为已保存状态，不能撤销
     * 若当前图片为未保存状态，根据历史记录来进行撤销，此时需注意撤销到上一状态是否为保存状态情况
     * @param context 上下文
     * @param photoId 图片id
     * @return ImageInfo对象
     */
    public static ImageStore.ImageInfo undo(Context context, int photoId) {
        InterPhotoDBManger.connectDB(context);
        InterPhoto interPhoto = InterPhotoDBManger.getPhoto(context, photoId);
        if (interPhoto == null) {
            InterPhotoDBManger.closeDB(context);
//            throw new RuntimeException("根据photoId获取InterPhoto对象为null");
            return null;
        }

        List<InterHistory> list = InterPhotoDBManger.getHistorys(context, photoId);

        if (list == null || list.isEmpty()) {
            InterPhotoDBManger.closeDB(context);
//            throw new RuntimeException("根据photoId获取InterHistory列表为空");
            return null;
        }

        ImageStore.ImageInfo imageInfo;

        if (!interPhoto.getSaved()) {
            InterHistory interHistory = list.get(0);
            String deletePath = interPhoto.getFinalUri();

            String srcPath = interPhoto.getOriginalUri();

            if (srcPath.equals(interHistory.getResUri())) {
                // 上一个保存点
                interPhoto.setSaved(false);
                interPhoto.setOriginalUri(null);
            }

            interPhoto.setFinalUri(interHistory.getResUri());
            interPhoto.setPhotoEffect(interHistory.getPhotoEffect());
            interPhoto.setUpdateDate(System.currentTimeMillis());

            if (!TextUtils.isEmpty(deletePath) && !deletePath.equals(srcPath)) {
                FileUtils.delete(deletePath);
            }

            InterPhotoDBManger.updatePhoto(context, interPhoto);
            InterPhotoDBManger.deleteHistory(context, interHistory.getId());
            LocalStore.updateInterPhoto(interPhoto);

            imageInfo = LocalStore.changeInterPhoto(interPhoto);
        } else {
            imageInfo = restore(context, photoId);
        }

        InterPhotoDBManger.closeDB(context);

        return imageInfo;
    }

    /**
     * 判断当前图片是否可以恢复上一步操作
     * 根据OriginalUri字段是否为空即可
     * @param context 上下文
     * @param photoId 图片id
     * @return true: 可以恢复上一步操作
     */
    public static boolean canRestore(Context context, int photoId) {
        InterPhotoDBManger.connectDB(context);
        boolean b = false;
        InterPhoto interPhoto = InterPhotoDBManger.getPhoto(context, photoId);
        List<InterHistory> list = InterPhotoDBManger.getHistorys(context, photoId);
        if (interPhoto != null && list != null && !list.isEmpty()) {
            b = true;
        }
        InterPhotoDBManger.closeDB(context);
        return b;
    }

    /**
     * 恢复操作，进行此操作前先调用canRestore方法判断是否可恢复
     * 若图片为已保存状态，根据OriginalUri字段恢复到上一张图片，并根据历史记录更新图片效果信息
     * 若图片为未保存状态，根据历史记录来进行恢复，此时需注意恢复到上一状态是否为保存状态情况
     * @param context 上下文
     * @param photoId 图片id
     * @return ImageInfo对象
     */
    public static ImageStore.ImageInfo restore(Context context, int photoId) {
        InterPhotoDBManger.connectDB(context);
        InterPhoto interPhoto = InterPhotoDBManger.getPhoto(context, photoId);
        if (interPhoto == null) {
            InterPhotoDBManger.closeDB(context);
//            throw new RuntimeException("根据photoId获取InterPhoto对象为null");
            return null;
        }

        List<InterHistory> list = InterPhotoDBManger.getHistorys(context, photoId);

        if (list == null || list.isEmpty()) {
            InterPhotoDBManger.closeDB(context);
//            throw new RuntimeException("根据photoId获取InterHistory列表为空");
            return null;
        }

        ImageStore.ImageInfo imageInfo = null;
        String orgPath = interPhoto.getOriginalUri();
        if (!TextUtils.isEmpty(orgPath) && new File(orgPath).exists()) {
            String deletePath = interPhoto.getFinalUri();

            InterHistory interHistory;
            int index;
            for (index = 0; index < list.size(); index++) {
                interHistory = list.get(index);
                InterPhotoDBManger.deleteHistory(context, interHistory.getId());
                if (interHistory.getResUri().equals(orgPath)) {
                    break;
                } else {
                    FileUtils.delete(interHistory.getResUri());
                }
            }

            interPhoto.setFinalUri(orgPath);
            interPhoto.setSaved(false);
            interPhoto.setUpdateDate(System.currentTimeMillis());
            if (index + 1 >= list.size()) {
                // 没有历史记录了，恢复到原图
                interPhoto.setOriginalUri(null);
                interPhoto.setPhotoEffect(null);
            } else {
                interPhoto.setOriginalUri(list.get(index).getResUri());
                interPhoto.setPhotoEffect(list.get(index).getPhotoEffect());
            }

            if (!TextUtils.isEmpty(deletePath) && !deletePath.equals(orgPath)) {
                FileUtils.delete(deletePath);
            }

            InterPhotoDBManger.updatePhoto(context, interPhoto);
            InterPhotoDBManger.closeDB(context);

            LocalStore.updateInterPhoto(interPhoto);
            imageInfo = LocalStore.changeInterPhoto(interPhoto);
        } else {
            imageInfo = undo(context, photoId);
//            InterPhotoDBManger.closeDB(context);
        }

        return imageInfo;
    }

    /**
     * 删除本地相册图片时调用
     * @param context 上下文
     * @param imageInfo ImageInfo对象
     */
    public static void deleteImage(Context context, ImageStore.ImageInfo imageInfo) {

        if (imageInfo != null && imageInfo.localAlbum) {
            List<ImageStore.ImageInfo> imageInfos = new ArrayList<>();
            imageInfos.add(imageInfo);
            deleteImages(context, imageInfos);
        }
    }

    /**
     * 删除本地相册图片时调用
     * @param context 上下文
     * @param imageInfos ImageInfo对象列表
     */
    public static void deleteImages(Context context, List<ImageStore.ImageInfo> imageInfos) {
        if (imageInfos == null || imageInfos.isEmpty()) {
            return;
        }

        InterPhotoDBManger.connectDB(context);
        InterPhoto interPhoto;
        List<InterHistory> histories;
        for (ImageStore.ImageInfo imageInfo : imageInfos) {
            interPhoto = InterPhotoDBManger.getPhoto(context, imageInfo.id);
            histories = InterPhotoDBManger.getHistorys(context, imageInfo.id);
            // 这里删除InterPhoto会自动把相关联的历史记录删除
            InterPhotoDBManger.deletePhoto(context, imageInfo.id);

            if (interPhoto != null) {
                FileUtils.delete(interPhoto.getFinalUri());
            }

            if (histories != null) {
                for (InterHistory interHistory : histories) {
                    FileUtils.delete(interHistory.getResUri());
                }
            }
            LocalStore.deleteInterPhoto(imageInfo);
        }

        InterPhotoDBManger.closeDB(context);
    }

    /**
     * 获取本地相册的所有图片
     * @param context 上下文
     */
    public static List<ImageStore.ImageInfo> getAllLocalImage(Context context) {

        List<ImageStore.ImageInfo> localImageList = new ArrayList<>();

        InterPhotoDBManger.connectDB(context);
        List<InterPhoto> list = InterPhotoDBManger.getAllPhotos(context);

        ImageStore.ImageInfo imageInfo;

        ArrayList<Integer> idList = new ArrayList<>();

        if (list != null) {
            for (InterPhoto photo : list) {

                if (new File(photo.getFinalUri()).exists()) {
                    imageInfo = new ImageStore.ImageInfo();
                    imageInfo.id = photo.getId();
                    imageInfo.image = photo.getFinalUri();
                    imageInfo.effect = photo.getPhotoEffect();
                    imageInfo.isSaved = photo.getSaved();
                    imageInfo.localAlbum = true;
                    imageInfo.lastModified = photo.getUpdateDate();

                    localImageList.add(imageInfo);
                } else {
                    idList.add(photo.getId());
                }
            }
        }

        if (!idList.isEmpty()) {
            int[] ids = new int[idList.size()];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = idList.get(i);
            }
            InterPhotoDBManger.deletePhotos(context, ids);
        }

        InterPhotoDBManger.closeDB(context);

        return localImageList;
    }

    /**
     * 清除历史记录
     * @param context 上下文
     * @param clearOrigin 是否清除原图
     * @param callback 清除完成回调（可空），注意回调所在线程不是主线程
     */
    public static void clearHistory(final Context context, final boolean clearOrigin,
                                    @Nullable final Callback callback) {
        new Thread() {
            @Override
            public void run() {
                InterPhotoDBManger.connectDB(context);

                List<InterPhoto> interPhotos = InterPhotoDBManger.getAllPhotos(context);
                if (interPhotos != null && !interPhotos.isEmpty()) {
                    String finalPath;
                    String originalPath;
                    for (InterPhoto interPhoto : interPhotos) {
                        if (!interPhoto.getSaved()) {
                            finalPath = interPhoto.getFinalUri();
                            originalPath = interPhoto.getOriginalUri();

                            List<InterHistory> list = InterPhotoDBManger.getHistorys(context, interPhoto.getId());
                            String imagePath;

                            if (list != null) {
                                for (InterHistory history : list) {
                                    imagePath = history.getResUri();
                                    if (imagePath != null && !imagePath.equals(finalPath) && !imagePath.equals(originalPath)) {
                                        FileUtils.delete(imagePath);
                                    }
                                }
                            }

                            InterHistory interHistory = null;

                            if (!clearOrigin && new File(originalPath).exists()) {
                                interHistory = new InterHistory();
                                interHistory.setResUri(originalPath);
                                interHistory.setPhotoId(interPhoto.getId());
                                interHistory.setPhotoEffect(interPhoto.getPhotoEffect());
                                interHistory.setCreateDate(System.currentTimeMillis());
                            } else {
                                FileUtils.delete(originalPath);
                            }

                            InterPhotoDBManger.deleteHistorys(context, interPhoto.getId());

                            if (interHistory != null) {
                                InterPhotoDBManger.insertHistory(context, interHistory);
                            }
                        }
                    }
                }

                InterPhotoDBManger.closeDB(context);

                Glide.get(context).clearDiskCache();

                if (callback != null) {
                    callback.onCompleted();
                }
            }
        }.start();
    }

    @WorkerThread
    public static long getCacheSize(Context context) {

        long cacheSize = 0;

        InterPhotoDBManger.connectDB(context);

        List<InterPhoto> interPhotos = InterPhotoDBManger.getAllPhotos(context);
        if (interPhotos != null && !interPhotos.isEmpty()) {
            String finalPath;
            for (InterPhoto interPhoto : interPhotos) {
                if (!interPhoto.getSaved()) {
                    finalPath = interPhoto.getFinalUri();

                    List<InterHistory> list = InterPhotoDBManger.getHistorys(context, interPhoto.getId());
                    String imagePath;

                    if (list != null) {
                        for (InterHistory history : list) {
                            imagePath = history.getResUri();
                            if (imagePath != null && !imagePath.equals(finalPath)) {
                                cacheSize += new File(imagePath).length();
                            }
                        }
                    }
                }
            }
        }

        InterPhotoDBManger.closeDB(context);

        return cacheSize;
    }

    public interface Callback {
        void onCompleted();
    }

    /**
     * 判断数据库是否为空，即InterPhoto相册为空
     * @param context 上下文
     * @return InterPhoto相册是否为空
     */
    public static boolean isDatabaseEmpty(Context context) {

        List<InterPhoto> interPhotos;

        try {
            InterPhotoDBManger.connectDB(context);
            interPhotos = InterPhotoDBManger.getAllPhotos(context);
        } finally {
            InterPhotoDBManger.closeDB(context);
        }

        return interPhotos == null || interPhotos.isEmpty();
    }
}
