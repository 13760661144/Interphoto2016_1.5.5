package cn.poco.watermarksync.delegate;

import cn.poco.storagesystemlibs.IStorage;
import cn.poco.storagesystemlibs.StorageStruct;
import cn.poco.storagesystemlibs.UpdateInfo;
import cn.poco.storagesystemlibs.UploadInfo;

/**
 * Created by Shine on 2017/3/1.
 */

public interface WaterStorage extends IStorage{

    /**
     * 获取阿里token
     */
    String GetTokenUrl();


    String MakeUpdateMyWebData(UpdateInfo info);

    /**
     * 默认调用{@link cn.poco.storagesystemlibs.StorageUtils#GetTokenInfo(String, String, int, boolean, IStorage)}
     */
    UploadInfo GetUploadInfo(StorageStruct str, int num);

}
