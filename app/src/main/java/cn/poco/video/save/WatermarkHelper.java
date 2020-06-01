package cn.poco.video.save;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import cn.poco.display.CoreViewV3;
import cn.poco.graphics.ShapeEx;
import cn.poco.video.save.watermark.BitmapInfo;
import cn.poco.video.videotext.text.VideoText;
import cn.poco.video.videotext.text.VideoTextView;

/**
 * Created by: fwc
 * Date: 2017/9/22
 */
public class WatermarkHelper {

	private Context mContext;
	private VideoText mVideoText;
	private ShapeEx mShapeEx;

	private int mStartTime;
	private int mStayTime;

	private VideoTextView mVideoTextView;

	private Handler mMainHandler;

	public WatermarkHelper(Context context, VideoText videoText, ShapeEx shapeEx, int startTime, int stayTime) {
		mContext = context;
		mVideoText = videoText;
		mShapeEx = shapeEx;
		mStartTime = startTime;
		mStayTime = stayTime;

		mMainHandler = new Handler(Looper.getMainLooper());
	}

	public void prepareWatermark(final int width, final int height) {
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				prepareWatermarkInner(width, height);
			}
		});
	}

	/**
	 * 准备水印操作
	 *
	 * @param width  视频宽度
	 * @param height 视频高度
	 */
	private void prepareWatermarkInner(int width, int height) {

		mVideoTextView = new VideoTextView(mContext, width, height);
		mVideoTextView.InitData(mControlCallback);
		mVideoTextView.SetOperateMode(VideoTextView.MODE_IMAGE);

		mVideoTextView.DelAllPendant();
		mVideoTextView.SetOutTextInfo(mVideoText, width, height, mShapeEx);
		mVideoTextView.setStartTime(mStartTime);
		mVideoTextView.setStayTime(mStayTime);
	}

	public BitmapInfo getWatermarkInfo(int width, int height, long timestamp) {

		VideoText videoText = mVideoTextView.GetOutTextBmp((int)(timestamp / 1000f));

		if (videoText != null && videoText.m_bmp != null) {
			BitmapInfo info = new BitmapInfo();
			Bitmap bmp = mVideoTextView.GetShowBmp(videoText);
			if (bmp == null) {
				return null;
			}

			info.bitmap = bmp;
			float bgWidth = bmp.getWidth();
			float bgHeight = bmp.getHeight();
			info.scaleX = videoText.m_animScaleX * bgWidth / width;
			info.scaleY = videoText.m_animScaleY * bgHeight / height;
			info.x = (videoText.m_x - (width - bgWidth) / 2 + videoText.m_showAnimDx) / (width / 2);
			info.y = -(videoText.m_y - (height - bgHeight) / 2 + videoText.m_showAnimDy) / (height / 2);
			info.alpha = videoText.m_animAlpha;

			return info;
		}

		return null;
	}

	private CoreViewV3.ControlCallback mControlCallback = new CoreViewV3.ControlCallback() {

		@Override
		public Bitmap MakeShowImg(Object info, int frW, int frH) {
			return null;
		}

		@Override
		public Bitmap MakeOutputImg(Object info, int outW, int outH) {
			return null;
		}

		@Override
		public Bitmap MakeShowFrame(Object info, int frW, int frH) {
			return null;
		}

		@Override
		public Bitmap MakeOutputFrame(Object info, int outW, int outH) {
			return null;
		}

		@Override
		public Bitmap MakeShowBK(Object info, int frW, int frH) {
			return null;
		}

		@Override
		public Bitmap MakeOutputBK(Object info, int outW, int outH) {
			return null;
		}

		@Override
		public Bitmap MakeShowPendant(Object info, int frW, int frH) {
			return null;
		}

		@Override
		public Bitmap MakeOutputPendant(Object info, int outW, int outH) {
			return null;
		}

		@Override
		public void SelectPendant(int index) {

		}
	};
}
