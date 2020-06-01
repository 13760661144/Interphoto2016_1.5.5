package cn.poco.watermarksync.model;

import java.io.File;

import cn.poco.resource.DownloadMgr;
import cn.poco.resource.MyLogoRes;

/**
 * Created by Shine on 2017/3/24.
 */

public class EditableWatermark extends Watermark{

    public EditableWatermark() {
        super();
        mSuffix = ".zip";
        this.setEditable(true);
    }

    public EditableWatermark(MyLogoRes res) {
        super(res);
        mSuffix = ".zip";
    }

    @Override
    protected void init(MyLogoRes res) {
        super.init(res);
        this.setEditable(true);
    }


    @Override
    public String generateNewPathAfterDownload() {
        final String newPath = DownloadMgr.getInstance().MY_LOGO_PATH + File.separator + "." + System.currentTimeMillis() + (int)(Math.random() * 10000) + mSuffix;
        return newPath;
    }
}
