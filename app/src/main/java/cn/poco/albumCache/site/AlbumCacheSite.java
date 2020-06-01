package cn.poco.albumCache.site;

import android.content.Context;

import cn.poco.albumCache.AlbumCachePage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * Created by: fwc
 * Date: 2017/3/14
 */
public class AlbumCacheSite extends BaseSite {

	public AlbumCacheSite() {
		super(SiteID.ALBUM_CACHE);
	}

	@Override
	public IPage MakePage(Context context) {
		return new AlbumCachePage(context, this);
	}

	public void onBack(Context context) {
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
