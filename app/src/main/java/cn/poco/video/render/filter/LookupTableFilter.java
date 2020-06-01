package cn.poco.video.render.filter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import cn.poco.interphoto2.R;
import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/15
 */
public class LookupTableFilter extends AbstractFilter {

	private int uLookupTextureLoc;
	private int uBasicBlackWhiteLoc;

	private int uAlphaLoc;
	private int uIsBlackWhiteLoc;

	private Params mParams;

	private int mLookupTextureId = GlUtil.NO_TEXTURE;
	private int mBlackWhiteTextureId = GlUtil.NO_TEXTURE;

	public LookupTableFilter(Context context) {
		super(context);

		createProgram(R.raw.vertex_shader, R.raw.fragment_lookup_table);
	}

	@Override
	public void setParams(Params params) {
		mParams = params;
		mLookupTextureId = GlUtil.setBitmapOnTexture(mLookupTextureId, mParams.tableRes);
		if (mParams.isBlackWhite && mBlackWhiteTextureId == GlUtil.NO_TEXTURE) {
			mBlackWhiteTextureId = GlUtil.createTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.filter_basic_black_white));
		}
	}

	@Override
	protected void onGetUniformLocation(int program) {
		super.onGetUniformLocation(program);
		uLookupTextureLoc = GLES20.glGetUniformLocation(program, "lookupTexture");
		uBasicBlackWhiteLoc = GLES20.glGetUniformLocation(program, "basicBlackWhite");
		uAlphaLoc = GLES20.glGetUniformLocation(program, "alpha");
		uIsBlackWhiteLoc = GLES20.glGetUniformLocation(program, "isBlackWhite");
	}

	@Override
	protected void onSetUniformData() {
		GLES20.glUniform1f(uAlphaLoc, mParams.alpha);
		GLES20.glUniform1i(uIsBlackWhiteLoc, mParams.isBlackWhite ? 1 : 0);
	}

	@Override
	protected void onBindTexture(int textureId) {
		super.onBindTexture(textureId);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLookupTextureId);
		GLES20.glUniform1i(uLookupTextureLoc, 1);

		if (mParams.isBlackWhite) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBlackWhiteTextureId);
			GLES20.glUniform1i(uBasicBlackWhiteLoc, 2);
		}
	}

	@Override
	public void release() {
		super.release();

		if (mLookupTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mLookupTextureId}, 0);
			mLookupTextureId = GlUtil.NO_TEXTURE;
		}

		if (mBlackWhiteTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mBlackWhiteTextureId}, 0);
			mBlackWhiteTextureId = GlUtil.NO_TEXTURE;
		}

		mParams = null;
	}
}
