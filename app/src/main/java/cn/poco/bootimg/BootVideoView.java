package cn.poco.bootimg;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * 开机播放视频，高度铺满，宽度
 */

public class BootVideoView extends VideoView
{
	private int mRatioWidth;
	private int mRatioHeight;
	public BootVideoView(Context context)
	{
		super(context);
	}

	public BootVideoView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public BootVideoView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	public void SetRatioSize(int width, int height){
		mRatioWidth = width;
		mRatioHeight = height;

		requestLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

		if(mRatioWidth > 0 && mRatioHeight > 0){
			int width;
			int height;
			float ratio1 = mRatioWidth * 1f / mRatioHeight;
			float ratio2 = widthSpecSize * 1f / heightSpecSize;
			if(ratio1 < ratio2)
			{
				width = widthSpecSize;
				height = width * mRatioHeight / mRatioWidth;
			}
			else{
				height = heightSpecSize;
				width = height * mRatioWidth / mRatioHeight;
			}
			setMeasuredDimension(width, height);
		}
		else {
			setMeasuredDimension(widthSpecSize, heightSpecSize);
		}
	}
}
