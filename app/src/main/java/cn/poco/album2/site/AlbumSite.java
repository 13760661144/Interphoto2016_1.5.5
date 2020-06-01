package cn.poco.album2.site;

import android.content.Context;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;

import cn.poco.album2.AlbumPage;
import cn.poco.albumCache.site.AlbumCacheSite;
import cn.poco.beautify.site.BeautifyPageSite;
import cn.poco.camera.RotationImg2;
import cn.poco.framework.BaseSite;
import cn.poco.framework.FileCacheMgr;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.utils.Utils;

/**
 * Created by: fwc
 * Date: 2016/9/21
 */

public class AlbumSite extends BaseSite {

    public AlbumSite() {
        super(SiteID.ALBUM);
    }

    @Override
    public IPage MakePage(Context context) {
        return new AlbumPage(context, this);
    }

    protected void onSelectPhoto(Context context, HashMap<String, Object> params) {
        MyFramework.SITE_Open(context, BeautifyPageSite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
    }

    /**
     * 在选图页面时点击图片跳转调用
     * 重写请参考{@link #onSelectPhoto(Context, HashMap)}
     * @param folderName 文件夹名字
     * @param path 图片路径
     * @param localAlbum 标记是否为本地相册跳转
     */
    public final void onSelectPhoto(Context context, String folderName, String path, boolean localAlbum) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("imgs", MakeRotationImg(new String[] {path}));
        params.put("is_local", localAlbum);
        params.put("from_album", true);
        if (folderName != null) {
            params.put("folder_name", folderName);
        }
        onSelectPhoto(context, params);
    }

    public void OnBack(Context context) {
        MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public void openAlbumCachePage(Context context) {
        MyFramework.SITE_Popup(context, AlbumCacheSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
    }

    public static RotationImg2[] MakeRotationImg(String[] arr) {

        RotationImg2[] out = null;

        if (arr != null && arr.length > 0) {
            out = new RotationImg2[arr.length];
            for (int i = 0; i < arr.length; i++) {
                RotationImg2 temp = Utils.Path2ImgObj(arr[i]);
                temp.m_img = FileCacheMgr.GetLinePath();
                try {
                    File destFile = new File((String)temp.m_img);
                    File tempFile = destFile.getParentFile();
                    if (tempFile != null && !tempFile.exists()) {
                        tempFile.mkdirs();
                    }
                    FileUtils.copyFile(new File(arr[i]), destFile);
                } catch(Throwable e) {
                    e.printStackTrace();
                }
                out[i] = temp;
            }
        }
        return out;
    }
}
