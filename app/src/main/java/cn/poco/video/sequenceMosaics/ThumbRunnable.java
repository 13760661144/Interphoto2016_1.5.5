package cn.poco.video.sequenceMosaics;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.util.concurrent.Future;

import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

/**
 * Created by admin on 2017/11/22.

 */
public class ThumbRunnable implements Runnable
{
	private String mVideoPath;
	private int mPosition;
	private long time;
	private String savePath;
	private MediaMetadataRetriever mMetadataRetriever;
	private ThumbCallback mCB;
	private Context mContext;
	public ThumbRunnable(Context context, MediaMetadataRetriever mMetadataRetriever, String videoPath, int position, long curTime, String savePath, ThumbCallback cb)
	{
		this.mContext = context;
		this.mMetadataRetriever = mMetadataRetriever;
		this.mVideoPath = videoPath;
		this.mPosition = position;
		this.time = curTime;
		this.savePath = savePath;
		this.mCB = cb;
	}


	@Override
	public void run()
	{
		if(!Thread.currentThread().isInterrupted())
		{
			if(mMetadataRetriever != null){
				mMetadataRetriever.setDataSource(mVideoPath);
				Bitmap bmp = mMetadataRetriever.getFrameAtTime(time * 1000);
				bmp = MakeBmpV2.CreateBitmapV2(bmp, 0, 0, -1, ShareData.PxToDpi_xhdpi(160), ShareData.PxToDpi_xhdpi(160), Bitmap.Config.ARGB_8888);
				Utils.SaveImg(mContext, bmp, savePath, 100, false);
			}
			if(mCB != null){
				mCB.onComplete(mPosition);
			}
		}
	}

	public static interface ThumbCallback{
		public void onComplete(int positon);
	}
}
