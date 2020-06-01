package cn.poco.video.render.filter;

import android.content.Context;
import android.opengl.GLES20;

import java.util.Arrays;

import cn.poco.interphoto2.R;
import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/15
 */
public class BlendFilter extends AbstractFilter {

	private int uComOpLoc;
	private int uAlphaLoc;
	private int uResultAlphaLoc;

	private int[] mTextureLocs;
	private int[] mTextureIds;

	private Params mParams;

	public BlendFilter(Context context) {
		super(context);

		mTextureLocs = new int[3];
		Arrays.fill(mTextureLocs, -1);
		mTextureIds = new int[3];
		Arrays.fill(mTextureIds, GlUtil.NO_TEXTURE);

		createProgram(R.raw.vertex_shader, R.raw.fragment_blend);
	}

	@Override
	public void setParams(Params params) {

		mParams = params;

		for (int i = 0; i < mParams.blendRes.length; i++) {
			mTextureIds[i] = GlUtil.setBitmapOnTexture(mTextureIds[i], mParams.blendRes[i]);
		}
	}

	@Override
	protected void onGetUniformLocation(int program) {
		super.onGetUniformLocation(program);
		String name = "maskImage";

		for (int i = 0; i < mTextureLocs.length; i++) {
			mTextureLocs[i] = GLES20.glGetUniformLocation(program, name + (i+1));
		}

		uComOpLoc = GLES20.glGetUniformLocation(program, "comOp");
		uAlphaLoc = GLES20.glGetUniformLocation(program, "alpha");
		uResultAlphaLoc = GLES20.glGetUniformLocation(program, "resultAlpha");
	}

	@Override
	protected void onSetUniformData() {
		int[] comOpTemp = mParams.blendComOps;
		float[] alphaTemp = mParams.blendAlphas;

		int size = 3;
		int[] comOp = new int[size];
		float[] alpha = new float[size];
		for (int i = 0; i < mParams.blendRes.length; i++) {
			comOp[i] = comOpTemp[i];
			alpha[i] = alphaTemp[i];
		}

		GLES20.glUniform1iv(uComOpLoc, size, comOp, 0);
		GLES20.glUniform1fv(uAlphaLoc, size, alpha, 0);

		GLES20.glUniform1f(uResultAlphaLoc, mParams.alpha);
	}

	@Override
	protected void onBindTexture(int textureId) {
		super.onBindTexture(textureId);

		for (int i = 0; i < mParams.blendRes.length; i++) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE1 + i);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[i]);
			GLES20.glUniform1i(mTextureLocs[i], i + 1);
		}
	}

	@Override
	public void release() {
		super.release();

		int textureSize = 0;
		for (int textureId : mTextureIds) {
			if (textureId != GlUtil.NO_TEXTURE) {
				textureSize++;
			}
		}

		if (textureSize > 0) {
			int[] temp = new int[textureSize];
			System.arraycopy(mTextureIds, 0, temp, 0, textureSize);

			GLES20.glDeleteTextures(textureSize, temp, 0);

			Arrays.fill(mTextureIds, GlUtil.NO_TEXTURE);
		}

		mParams = null;
	}

}
