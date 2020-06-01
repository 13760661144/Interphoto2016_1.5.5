package cn.poco.capture2.site;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.MaterialMgr2.site.ThemePageSite3;
import cn.poco.capture2.CapturePage;
import cn.poco.capture2.model.Snippet;
import cn.poco.framework.AnimatorHolder;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.tianutils.ShareData;
import cn.poco.video.page.VideoBeautifyPage;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.site.VideoBeautifySite2;
import cn.poco.video.utils.VideoUtils;
import cn.poco.video.videoAlbum.VideoAlbumUtils;
import cn.poco.webview.site.WebViewPageSite;

/**
 * Created by: fwc
 * Date: 2017/10/9
 */
public class CapturePageSite extends BaseSite {

	public CapturePageSite() {
		super(SiteID.CAPTURE_VIDEO);
	}

	@Override
	public IPage MakePage(Context context) {
		return new CapturePage(context, this);
	}

	public void onBack(Context context) {
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	/**
	 * @param params
	 * url 网络地址
	 */
	public void openCameraPermissionsHelper(Context context, HashMap<String, Object> params) {
		MyFramework.SITE_Popup(context, WebViewPageSite.class, params, Framework2.ANIM_TRANSLATION_TOP);
	}

	/**
	 * 拍完视频后处理
	 * @param params 参数
	 * data 视频路径 String
	 */
	public void openProcessVideo(Context context, HashMap<String, Object> params) {
		@SuppressWarnings("unchecked")
		List<Snippet> snippets = (List<Snippet>)params.get("snippet_list");
		params.remove("snippet_list");

		List<VideoInfo> videoInfos = new ArrayList<>();
		long duration;
		int filterUri;
		int filterAlpha;
		for (Snippet snippet : snippets) {
			duration = VideoUtils.getDurationFromVideo2(snippet.path);
			if (snippets.size() == 1) {
				duration = duration / 1000 * 1000;
			}
			if (snippet.filterItem != null) {
				filterUri = snippet.filterItem.filterUri;
				filterAlpha = (int)(snippet.alpha * 12);
			} else {
				filterUri = -1;
				filterAlpha = 1;
			}

			videoInfos.add(new VideoInfo(snippet.path, duration, filterUri, filterAlpha));
		}
		params.put("videos", videoInfos);
		MyFramework.SITE_Open(context,false, VideoBeautifySite2.class, params, new AnimatorHolder()
		{
			@Override
			public void doAnimation(View oldView, final View newView, final AnimatorListener lst)
			{
				ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(oldView,View.TRANSLATION_X,0, -ShareData.m_screenWidth);
				objectAnimator1.setDuration(350);
				ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(newView,View.TRANSLATION_X, ShareData.m_screenWidth, 0);
				objectAnimator1.setDuration(350);
				AnimatorSet animatorSet = new AnimatorSet();
				animatorSet.playTogether(objectAnimator1,objectAnimator2);
				animatorSet.addListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationEnd(Animator animation)
					{
						lst.OnAnimationEnd();
						if(newView instanceof VideoBeautifyPage){
							VideoAlbumUtils.enterImmersiveMode(newView.getContext());
						}
					}


					@Override
					public void onAnimationStart(Animator animation)
					{
					}
				});
				animatorSet.start();

			}
		});

//		List<VideoEntry> videos = new ArrayList<>();
//		long duration;
//		VideoEntry.VideoNormal videoNormal;
//		VideoEntry.VideoFilterRes filterRes;
//		for (Snippet snippet : snippets) {
//			duration = VideoUtils.getDurationFromVideo2(snippet.path);
//			if (snippets.size() == 1) {
//				duration = duration / 1000 * 1000;
//			}
//			videoNormal = new VideoEntry.VideoNormal(snippet.path, duration);
//			filterRes = new VideoEntry.VideoFilterRes();
//			if (snippet.filterItem != null) {
//				filterRes.mFilterRes = snippet.filterItem.filterRes;
//				filterRes.mFilterRes.m_alpha = (int)(snippet.alpha * 12);
//				filterRes.mUri = snippet.filterItem.filterUri;
//			} else {
//				filterRes.mFilterRes = null;
//				filterRes.mUri = DragListItemInfo.URI_ORIGIN;
//			}
//
//			videoNormal.mVideoFilter = filterRes;
//			videos.add(videoNormal);
//		}
//
//		params.put("videos", VideoAlbumUtils.transformVideoInfo(videos));
//		MyFramework.SITE_Open(context, VideoBeautifySite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void onDownloadMore(Context context, HashMap<String, Object> params) {
		MyFramework.SITE_Popup(context, ThemePageSite3.class, params, Framework2.ANIM_TRANSLATION_RIGHT);
	}
}
