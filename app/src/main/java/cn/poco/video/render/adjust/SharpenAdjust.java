package cn.poco.video.render.adjust;

import android.content.Context;
import android.opengl.GLES20;

import cn.poco.interphoto2.R;

/**
 * Created by: fwc
 * Date: 2017/12/21
 * 锐度：0 ~ 1，默认0
 */
public class SharpenAdjust extends AbstractAdjust {

	private final int mWidth;
	private final int mHeight;

	private int mImageWidthFactorLoc;
	private int mImageHeightFactorLoc;

	public SharpenAdjust(Context context, int width, int height) {
		super(context);

		mWidth = width;
		mHeight = height;

		createProgram(R.raw.vertex_sharpen, R.raw.fragment_sharpen);
	}

	@Override
	protected void onGetUniformLocation(int program) {
		super.onGetUniformLocation(program);
		mImageWidthFactorLoc = GLES20.glGetUniformLocation(program, "imageWidthFactor");
		mImageHeightFactorLoc = GLES20.glGetUniformLocation(program, "imageHeightFactor");
	}

	@Override
	protected void onSetUniformData() {
		super.onSetUniformData();
		GLES20.glUniform1f(mImageWidthFactorLoc, 1f / mWidth);
		GLES20.glUniform1f(mImageHeightFactorLoc, 1f / mHeight);
	}

	@Override
	protected void checkParams(AbstractAdjust.Params params) {
		if (!(params instanceof Params)) {
			throw new IllegalArgumentException();
		}
	}

	public static class Params extends AbstractAdjust.Params {
		public Params() {
			super(1);
		}

		@Override
		protected void initValues(float[] values) {
			values[0] = 0;
		}

		@Override
		public boolean isDefault() {
			return values[0] == 0;
		}

		@Override
		public void addItem(AdjustItem item) {
			if (item.id == AdjustItem.SHARPEN) {
				values[0] = item.value;
			} else {
				throw new RuntimeException();
			}
		}
	}
}
