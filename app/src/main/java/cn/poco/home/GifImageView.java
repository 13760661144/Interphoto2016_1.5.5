package cn.poco.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.ImageView;

import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.utils.FileUtil;

/**
 * 支持图片、gif的ImageView,并且GIF只支持本地路径
 */

public class GifImageView extends ImageView
{
	/**
	 * 播放GIF动画的关键类
	 */
	private Movie mMovie;

	/**
	 * 记录动画开始的时间
	 */
	private long mMovieStart;

	/**
	 * 图片是否正在播放
	 */
	private boolean isPlaying;

	/**
	 * 是否允许自动播放
	 */
	private boolean isAutoPlay = true;

	/**
	 * 是否允许循环播放
	 */
	private boolean canLoop = true;

	private PaintFlagsDrawFilter temp_filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

	/**
	 * PowerImageView构造函数。
	 *
	 * @param context
	 */
	public GifImageView(Context context) {
		super(context);
	}

	/**
	 * PowerImageView构造函数。
	 *
	 * @param context
	 */
	public GifImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * PowerImageView构造函数，在这里完成所有必要的初始化操作。
	 *
	 * @param context
	 */
	public GifImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void SetImageRes(Object res)
	{
		ClearAll();
		if(res instanceof String)
		{
			String mine_type = FileUtil.getMimeType((String)res);
			if("image/gif".equals(mine_type))
			{
				setLayerType(LAYER_TYPE_SOFTWARE, null);
				try
				{
					mMovie = Movie.decodeFile((String)res);
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				CommonUtils.LaunchViewGPU(this);
				Bitmap bmp = MakeBmpV2.DecodeImage(getContext(), (String)res, 0, -1, -1, -1, Bitmap.Config.ARGB_8888);
				setImageBitmap(bmp);
			}
		}
		else if(res instanceof Bitmap)
		{
			setLayerType(LAYER_TYPE_HARDWARE, null);
			CommonUtils.LaunchViewGPU(this);
			setImageBitmap((Bitmap)res);
		}
		else if(res instanceof Integer)
		{
			setLayerType(LAYER_TYPE_HARDWARE, null);
			setImageResource((Integer)res);
		}
	}

	public void setAutoPlay(boolean autoPlay)
	{
		isAutoPlay = autoPlay;
	}

	public void setCanLoop(boolean canLoop)
	{
		this.canLoop = canLoop;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (mMovie == null)
		{
			super.onDraw(canvas);
		}
		else
		{
			float w = this.getWidth();
			float h = this.getHeight();
			if(w > 0 && h > 0)
			{
				canvas.save();
				canvas.setDrawFilter(temp_filter);
				ScaleType scaleType = getScaleType();

				switch(scaleType)
				{
					case FIT_XY:
					{
						float scale1 = w / (float)mMovie.width();
						float scale2 = h / (float)mMovie.height();
						canvas.scale(scale1, scale2, w / 2f, h / 2f);
						break;
					}
					case CENTER:
					case CENTER_CROP:
					case CENTER_INSIDE:
					{
						if(w < mMovie.width() || h < mMovie.height())
						{
							float scale1 = w / (float)mMovie.width();
							float scale2 = h / (float)mMovie.height();
							float scale = scale1 < scale2 ? scale1 : scale2;

							canvas.scale(scale, scale, w / 2f, h / 2f);
						}
						break;
					}
				}

				if (isAutoPlay)
				{
					if(canLoop)
					{
						playMovie(canvas);
						postInvalidate();
					}
					else
					{
						if(!playMovie(canvas))
						{
							postInvalidate();
						}
					}
				}
				else
				{
					if (isPlaying)
					{
						if (playMovie(canvas))
						{
							isPlaying = false;
						}
						postInvalidate();
					}
					else
					{
						mMovie.setTime(0);
						mMovie.draw(canvas, (w - mMovie.width()) / 2, (h - mMovie.height()) / 2);
					}
				}
				canvas.restore();
			}
		}
	}

	/**
	 * 开始播放GIF动画，播放完成返回true，未完成返回false。
	 *
	 * @param canvas
	 * @return 播放完成返回true，未完成返回false。
	 */
	private boolean playMovie(Canvas canvas)
	{
		int w = getWidth();
		int h = getHeight();
		long now = SystemClock.uptimeMillis();
		if (mMovieStart == 0)
		{
			mMovieStart = now;
		}
		int duration = mMovie.duration();
		if (duration == 0)
		{
			duration = 1000;
		}
		int relTime = (int) ((now - mMovieStart) % duration);

		if(!canLoop && (now - mMovieStart) >= duration)
		{
			relTime = duration - 1;
		}
		mMovie.setTime(relTime);

		mMovie.draw(canvas, w / 2 - mMovie.width() / 2, h / 2 - mMovie.height() / 2);
		if ((now - mMovieStart) >= duration)
		{
			mMovieStart = 0;
			return true;
		}
		return false;
	}

	public void ClearAll()
	{
		mMovie = null;
		mMovieStart = 0;
		setImageBitmap(null);
	}
}
