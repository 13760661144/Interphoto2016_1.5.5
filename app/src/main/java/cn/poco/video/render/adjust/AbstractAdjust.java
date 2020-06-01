package cn.poco.video.render.adjust;

import android.content.Context;
import android.opengl.GLES20;

import cn.poco.video.render.draw.BaseDraw;
import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/15
 */
public abstract class AbstractAdjust extends BaseDraw {

	public static final int TYPE_VIDEO_ADJUST = 1;
	public static final int TYPE_SHARPEN_ADJUST = 2;
	public static final int TYPE_DARK_CORNER = 3;

	private int uSourceImageLoc;

	private int uValueLoc;

	private Params mParams;

	public AbstractAdjust(Context context) {
		super(context);
	}

	@Override
	protected void onGetUniformLocation(int program) {
		uSourceImageLoc = GLES20.glGetUniformLocation(program, "sourceImage");
		uValueLoc = GLES20.glGetUniformLocation(program, "values");
	}

	@Override
	protected void onSetUniformData() {
		GLES20.glUniform1fv(uValueLoc, mParams.values.length, mParams.values, 0);
	}

	public void setParams(Params params) {
		checkParams(params);
		mParams = params;
	}

	protected abstract void checkParams(Params params);

	@Override
	protected void onBindTexture(int textureId) {
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glUniform1i(uSourceImageLoc, 0);
	}

	public void draw(int textureId) {
		super.draw(textureId, GlUtil.IDENTITY_MATRIX, GlUtil.IDENTITY_MATRIX);
	}

	@Override
	public void release() {
		super.release();
		mParams = null;
	}

	public static abstract class Params {

		protected float[] values;

		public Params(int size) {
			values = new float[size];

			initValues(values);
		}

		protected abstract void initValues(float[] values);

		public abstract boolean isDefault();

		public abstract void addItem(AdjustItem item);
	}
}
