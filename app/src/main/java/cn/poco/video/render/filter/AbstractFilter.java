package cn.poco.video.render.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import cn.poco.video.render.draw.BaseDraw;
import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/15
 */
public abstract class AbstractFilter extends BaseDraw {

	private int uSourceImageLoc;

	public AbstractFilter(Context context) {
		super(context);
	}

	@Override
	protected void onGetUniformLocation(int program) {
		uSourceImageLoc = GLES20.glGetUniformLocation(program, "sourceImage");
	}

	@Override
	protected void onBindTexture(int textureId) {
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glUniform1i(uSourceImageLoc, 0);
	}

	public void draw(int textureId) {
		super.draw(textureId, GlUtil.IDENTITY_MATRIX, GlUtil.IDENTITY_MATRIX);
	}

	public abstract void setParams(Params params);

	public static class Params {

		public int type;

		Bitmap tableRes;
		boolean isBlackWhite;

		Bitmap[] blendRes;
		int[] blendComOps;
		float[] blendAlphas;

		public float alpha;

		public Params(FilterItem item) {

			type = item.type;

			tableRes = item.tableRes;
			isBlackWhite = item.isBlackWhite;

			blendRes = item.blendRes;
			blendComOps = item.blendComOps;
			blendAlphas = item.blendAlphas;

			alpha = item.resultAlpha;
		}
	}
}
