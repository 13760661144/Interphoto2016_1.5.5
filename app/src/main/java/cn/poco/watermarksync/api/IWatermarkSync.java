package cn.poco.watermarksync.api;

import cn.poco.pocointerfacelibs.IPOCO;

/**
 * Created by Shine on 2017/2/27.
 */

public interface IWatermarkSync extends IPOCO{

    String GetUpdateMyWebUrl();

    String getWatermarkSyncData();

    String getUserWatermarkList();

    String deleteWatermark();

    String modifyWatermark();

}
