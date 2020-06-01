package cn.poco.video.render.view;

import android.content.Context;
import android.util.AttributeSet;

import cn.poco.video.render.base.GLTextureView;

/**
 * Created by: fwc
 * Date: 2017/6/9
 */
public class AutoFitTextureView extends GLTextureView {

	private float mAspectRatio = 0;

	public AutoFitTextureView(Context context) {
		super(context);
	}

	public AutoFitTextureView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setAspectRatio(float aspectRatio) {
		mAspectRatio = aspectRatio;
		requestLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mAspectRatio != 0) {
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = MeasureSpec.getSize(heightMeasureSpec);

			float ratio = width * 1f / height;
			if (mAspectRatio > ratio) {
				setMeasuredDimension(width, (int)(width / mAspectRatio));
			} else {
				setMeasuredDimension((int)(height * mAspectRatio), height);
			}
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
