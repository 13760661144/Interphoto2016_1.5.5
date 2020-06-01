package cn.poco.watermarksync.model;

import java.io.File;

import cn.poco.resource.DownloadMgr;
import cn.poco.resource.MyLogoRes;

/**
 * Created by Shine on 2017/3/24.
 */

public class NotEditableWatermark extends Watermark{
    private String mDownloadSuffix;

    public NotEditableWatermark() {
        super();
        initLocalData();
        this.setEditable(false);
    }

    public NotEditableWatermark(MyLogoRes res) {
        super(res);
        initLocalData();
    }

    private void initLocalData() {
        mSuffix = ".png";
        mDownloadSuffix = ".img";
    }

    @Override
    protected void init(MyLogoRes res) {
        super.init(res);
        this.setEditable(false);
    }


    @Override
    public String generateNewPathAfterDownload() {
        final String newPath = DownloadMgr.getInstance().MY_LOGO_PATH + File.separator + "." + System.currentTimeMillis() + (int)(Math.random() * 10000) + mDownloadSuffix;
        return newPath;
    }
}
