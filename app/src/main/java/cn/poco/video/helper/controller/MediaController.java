package cn.poco.video.helper.controller;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.poco.album2.model.FolderInfo;
import cn.poco.interphoto2.R;
import cn.poco.utils.FileUtil;
import cn.poco.video.model.VideoEntry;
import cn.poco.video.utils.AndroidUtil;
import cn.poco.video.utils.VideoUtils;


/**
 * Created by Shine on 2017/5/16.
 */

public class MediaController {
    public static abstract class MediaControllerCallback {
        public abstract void loadMediaSuccessfully(Object result);
        public abstract void failToLoadMedia(Object error);
        public void onAllMediaLoaded() {

        };
        public void getTotalCount(int count) {};
    }

    private MediaController(Context context)
    {
        this.context = context.getApplicationContext();
        mLoading = new AtomicBoolean(false);
    }


    private static volatile MediaController sInstance;
    private Context context;
    private static final String[] mProjectionVideo = {
//            MediaStore.Video.Media._ID,
//            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
//            MediaStore.Video.Media.DATE_TAKEN,
//            MediaStore.Video.VideoColumns.DURATION
    };



    public static MediaController getInstance(Context context) {
        MediaController localInstance = sInstance;
        if (localInstance == null) {
            synchronized (MediaController.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new MediaController(context);
                }
            }
        }
        return localInstance;
    }

    private List<IVideoLoadComplete> mILoadCompletes = new ArrayList<>();
    private boolean mLoadCompeletd = false;
    private List<FolderInfo> mFolderInfos = new ArrayList<>();
    private List<VideoEntry> mVideoInfos = new ArrayList<>();
    private AtomicBoolean mLoading;

    public boolean isLoading()
    {
        return mLoading.get();
    }

    public void addVideoInfo(String path)
    {
        if(!TextUtils.isEmpty(path))
        {
            final VideoUtils.VideoInfo videoInfo = VideoUtils.getVideoInfo(path);
            if (videoInfo != null)
            {
                VideoEntry videoEntry = new VideoEntry.VideoNormal(path, videoInfo.duration);
                String folderName = new File(path).getParent();
                int start = folderName.lastIndexOf("/");
                if (start >= 0 && start < folderName.length())
                {
                    folderName = folderName.substring(start + 1, folderName.length());
                }

                videoEntry.mBucketName = folderName;
                if (videoEntry.mRotation == 90 || videoEntry.mRotation == 270)
                {
                    videoEntry.mWidth = videoInfo.height;
                    videoEntry.mHeight = videoInfo.width;
                } else
                {
                    videoEntry.mWidth = videoInfo.width;
                    videoEntry.mHeight = videoInfo.height;
                }
                int index = -1;
                for (int i = 0; i < mFolderInfos.size(); i++)
                {
                    if (mFolderInfos.get(i).equals(folderName))
                    {
                        mFolderInfos.get(i).addCount();
                        mFolderInfos.get(i).setCover(videoEntry.mMediaPath);
                        index = i;
                        break;
                    }
                }
                if (index == -1)
                {
                    FolderInfo info = new FolderInfo();
                    info.setCount(1);
                    info.setCover(videoEntry.mMediaPath);
                    info.setName(folderName);
                    mFolderInfos.add(info);
                }
                mFolderInfos.get(0).addCount();
                mFolderInfos.get(0).setCover(videoEntry.mMediaPath);
                mVideoInfos.add(0, videoEntry);
            }
        }
    }

    public void initVideoInfo(boolean isReload)
    {
        if(isReload)
        {
            mLoadCompeletd = false;
        }
        initVideoInfo();
    }

    public void initVideoInfo()
    {
        if(!mLoadCompeletd)
        {
            mLoading.set(true);
            ArrayMap<String, FolderInfo> groups = new ArrayMap<>();
            List<FolderInfo> folderInfos = new ArrayList<>();
            List<VideoEntry> videoInfos = new ArrayList<>();
            String albumCover = null;

            Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mProjectionVideo, null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC");
            if (cursor != null)
            {
//            int imageIdIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
//            int bucketIdIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
                int bucketNameIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
//            int dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
                final int count = cursor.getCount();

                if (count > 0)
                {
                    String curFolder = null;
                    FolderInfo folderInfo = null;
                    while (cursor.moveToNext())
                    {
//                int imageId = cursor.getInt(imageIdIndex);
//                int bucketId = cursor.getInt(bucketIdIndex);
                        String bucketName = cursor.getString(bucketNameIndex);
                        String path = cursor.getString(dataColumn);
//                long dataToken = cursor.getLong(dateColumn);

                        boolean isFileExist = FileUtil.isFileExists(path);
                        if (!isFileExist)
                        {
                            continue;
                        }
                        if (albumCover == null)
                        {
                            albumCover = path;
                        }

                        final VideoUtils.VideoInfo videoInfo = VideoUtils.getVideoInfo(path);

                        if (videoInfo != null && videoInfo.duration >= 1000 && !TextUtils.isEmpty(bucketName))
                        {
                            final VideoEntry mediaEntry;
                            if (videoInfo.duration >= 61 * 1000)
                            {
                                mediaEntry = new VideoEntry.VideoExceedLimit(path, videoInfo.duration);
                            } else
                            {
                                mediaEntry = new VideoEntry.VideoNormal(path, videoInfo.duration);
                            }
                            mediaEntry.mRotation = videoInfo.rotation;
                            if (mediaEntry.mRotation == 90 || mediaEntry.mRotation == 270)
                            {
                                mediaEntry.mWidth = videoInfo.height;
                                mediaEntry.mHeight = videoInfo.width;
                            } else
                            {
                                mediaEntry.mWidth = videoInfo.width;
                                mediaEntry.mHeight = videoInfo.height;
                            }
                            mediaEntry.mBucketName = bucketName;
                            videoInfos.add(mediaEntry);

                            if (curFolder == null || !curFolder.equals(bucketName))
                            {
                                folderInfo = groups.get(bucketName);
                                if (folderInfo == null)
                                {
                                    folderInfo = new FolderInfo();
                                    folderInfo.setName(bucketName);
                                    folderInfo.setCover(path);
                                    groups.put(bucketName, folderInfo);
                                    folderInfos.add(folderInfo);

                                }
                            }
                            folderInfo.addCount();
                            curFolder = bucketName;
                        }
                    }
                    cursor.close();
                }
            }

            int totalCount = 0;
            for (FolderInfo info : folderInfos)
            {
                totalCount += info.getCount();
            }

            // 新增系统相册文件夹，包含所有的图片数据
            String album = context.getResources().getString(R.string.video_album);
            FolderInfo folderInfo = new FolderInfo();
            folderInfo.setName(album);
            folderInfo.setCover(albumCover);
            folderInfo.setCount(totalCount);
            folderInfos.add(0, folderInfo);

            synchronized (this)
            {
                mLoadCompeletd = true;
                boolean update = true;
                mVideoInfos.clear();
                mVideoInfos.addAll(videoInfos);
//                if (mFolderInfos.isEmpty())
//                {
//                    update = false;
//                    mFolderInfos.addAll(folderInfos);
//                    folderInfos.clear();
//                }
//                notify(folderInfos, update);
                mFolderInfos.clear();
                mFolderInfos.addAll(folderInfos);
                AndroidUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (IVideoLoadComplete iLoadComplete : mILoadCompletes) {
                            iLoadComplete.onCompleted(mFolderInfos, true);
                        }
                    }
                });
            }
            mLoading.set(false);
        }
    }

    public List<VideoEntry> getVideoEntrys(int folderIndex)
    {
        if(folderIndex >= 0 && folderIndex < mFolderInfos.size()){
            String name = mFolderInfos.get(folderIndex).getName();
            return getVideoEntrys(name);
        }else{
            return null;
        }
    }

    public List<VideoEntry> getVideoEntrys(String folderName)
    {
        List<VideoEntry> infos = new ArrayList<>();
        if(folderName.equals(context.getResources().getString(R.string.video_album))){
//        if(folderName.equals(mAllFolderName)){
            infos.addAll(mVideoInfos);
        }else
        {
            for (int i = 0; i < mVideoInfos.size(); i++)
            {
                if (mVideoInfos.get(i).mBucketName.equals(folderName))
                {
                    infos.add(mVideoInfos.get(i));
                }
            }
        }
        return infos;
    }

    public FolderInfo getFolderInfo(int index)
    {
        FolderInfo info = null;
        if(index >=0 && index < mFolderInfos.size() )
        {
           info = mFolderInfos.get(index);
        }
        return info;
    }

    public List<VideoEntry> getAllVideoEntrys()
    {
        List<VideoEntry> infos = new ArrayList<>();
        infos.addAll(mVideoInfos);
        return infos;
    }

//    private void notify(final List<FolderInfo> folderInfos, final boolean update) {
//        if (mILoadCompletes.isEmpty()) {
//            setFolderInfos(folderInfos);
//            return;
//        }
//        AndroidUtil.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                for (IVideoLoadComplete iLoadComplete : mILoadCompletes) {
//                    iLoadComplete.onCompleted(folderInfos, update);
//                }
//            }
//        });
//
//    }


    public synchronized void setFolderInfos(List<FolderInfo> folderInfos) {
        if (folderInfos.isEmpty() || mFolderInfos == folderInfos) {
            return;
        }

        mFolderInfos.clear();
        mFolderInfos.addAll(folderInfos);
        folderInfos.clear();
    }
    public void addLoadCompleteListener(final IVideoLoadComplete iLoadComplete) {
        if (iLoadComplete != null) {
            synchronized (this) {
                final boolean update = !mFolderInfos.isEmpty();
                mILoadCompletes.add(iLoadComplete);
                if (mLoadCompeletd) {
                    if (!mFolderInfos.isEmpty()) {
                        FolderInfo folderInfo = mFolderInfos.get(0);
                        String album = context.getResources().getString(R.string.video_album);
                        folderInfo.setName(album);
                    }
                    AndroidUtil.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iLoadComplete.onCompleted(mFolderInfos, update);
                        }
                    });
                }
            }
        }
    }

    public void removeLoadCompleteListener(IVideoLoadComplete iLoadComplete) {
        if (iLoadComplete != null) {
            mILoadCompletes.remove(iLoadComplete);
        }
    }



    public void loadVideo(final MediaControllerCallback callback) {
        if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Thread queryVideo = new Thread() {
                @Override
                public void run() {
                    Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mProjectionVideo, null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC");
                    if (cursor != null) {
//                        int imageIdIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
//                        int bucketIdIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
                        int bucketNameIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                        int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
//                        int dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
                        final int count = cursor.getCount();

                        if (callback != null) {
                            AndroidUtil.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                   callback.getTotalCount(count);
                                }
                            });
                        }

                        if (count == 0) {
                            return;
                        }

                        while (cursor.moveToNext()) {
//                            int imageId = cursor.getInt(imageIdIndex);
//                            int bucketId = cursor.getInt(bucketIdIndex);
                            String bucketName = cursor.getString(bucketNameIndex);
                            String path = cursor.getString(dataColumn);
//                            long dataToken = cursor.getLong(dateColumn);

                            boolean isFileExist = FileUtil.isFileExists(path);
                            if (!isFileExist) {
                                continue;
                            }

                            final VideoUtils.VideoInfo videoInfo = VideoUtils.getVideoInfo(path);

                            if (videoInfo != null && videoInfo.duration >= 1000) {
                                final VideoEntry mediaEntry;
                                if (videoInfo.duration >= 61 * 1000) {
                                    mediaEntry = new VideoEntry.VideoExceedLimit(path, videoInfo.duration);
                                } else {
                                    mediaEntry = new VideoEntry.VideoNormal(path, videoInfo.duration);
                                }
                                mediaEntry.mRotation = videoInfo.rotation;
                                if (mediaEntry.mRotation == 90 || mediaEntry.mRotation == 270) {
                                    mediaEntry.mWidth = videoInfo.height;
                                    mediaEntry.mHeight = videoInfo.width;
                                } else {
                                    mediaEntry.mWidth = videoInfo.width;
                                    mediaEntry.mHeight = videoInfo.height;
                                }

                                if (callback != null) {
                                    callback.loadMediaSuccessfully(mediaEntry);
                                }
                            }
                        }

                        if (callback != null) {
                            AndroidUtil.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onAllMediaLoaded();
                                }
                            });
                        }
                        cursor.close();
                    } else {
                        if (callback != null) {
                            AndroidUtil.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.failToLoadMedia(null);
                                }
                            });
                        }
                    }
                }
            };
            queryVideo.start();
        }
    }

    public void loadLastestVideo(final MediaControllerCallback callback) {
        if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Thread queryVideo = new Thread() {
                @Override
                public void run() {
                    Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mProjectionVideo, null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC");
                    if (cursor != null) {
                        int imageIdIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                        int bucketIdIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
                        int bucketNameIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                        int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                        int dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);
//                        int durationIndex = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION);

                        if (cursor.moveToNext()) {
                            int imageId = cursor.getInt(imageIdIndex);
                            int bucketId = cursor.getInt(bucketIdIndex);
                            String bucketName = cursor.getString(bucketNameIndex);
                            final String path = cursor.getString(dataColumn);
                            long dataToken = cursor.getLong(dateColumn);

                            createVideoEntry(path, callback);

                        } else {
                            if (callback != null) {
                                AndroidUtil.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.loadMediaSuccessfully(null);
                                    }
                                });
                            }
                        }
                        cursor.close();
                    } else {
                        if (callback != null) {
                            AndroidUtil.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    callback.failToLoadMedia(null);
                                }
                            });
                        }
                    }
                }
            };
            queryVideo.start();
        }
    }

//    public void scanVideoFile(final MediaControllerCallback callback) {
//        final MediaScannerConnection scannerConnection = new MediaScannerConnection(context, new MediaScannerConnection.MediaScannerConnectionClient() {
//            @Override
//            public void onMediaScannerConnected() {
//
//            }
//
//            @Override
//            public void onScanCompleted(String path, Uri uri) {
//                createVideoEntry(path, callback);
//            }
//        });
//        scannerConnection.scanFile(,null);
//    }


    private void createVideoEntry(final String path, final MediaControllerCallback callback) {
        boolean isFileExist = FileUtil.isFileExists(path);
        if (isFileExist) {
            if (callback != null) {
                AndroidUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final VideoUtils.VideoInfo videoInfo = VideoUtils.getVideoInfo(path);
                        final long durationInMillis = videoInfo.duration;
                        VideoEntry videoEntry;
                        if (durationInMillis >= 1000) {
                            if (durationInMillis >= 61 * 1000) {
                                videoEntry = new VideoEntry.VideoExceedLimit(path, durationInMillis);
                            } else {
                                videoEntry = new VideoEntry.VideoNormal(path, durationInMillis);
                            }
                            if (videoEntry.mRotation == 90 || videoEntry.mRotation == 270) {
                                videoEntry.mWidth = videoInfo.height;
                                videoEntry.mHeight = videoInfo.width;
                            } else {
                                videoEntry.mWidth = videoInfo.width;
                                videoEntry.mHeight = videoInfo.height;
                            }
                            callback.loadMediaSuccessfully(videoEntry);
                        } else {
                            callback.failToLoadMedia(null);
                        }
                    }
                });
            }
        } else {
            if (callback != null) {
                AndroidUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.failToLoadMedia(null);
                    }
                });
            }
        }
    }



//    public void clear() {
//        context = null;
//        sInstance = null;
//    }

    public interface IVideoLoadComplete
    {
        void onCompleted(List<FolderInfo> folderInfos, boolean update);
    }

}

