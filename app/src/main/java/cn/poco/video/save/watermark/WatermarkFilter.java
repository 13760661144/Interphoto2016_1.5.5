package cn.poco.video.save.watermark;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import cn.poco.interphoto2.R;
import cn.poco.video.render.draw.BaseDraw;
import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/26
 */
public class WatermarkFilter extends BaseDraw {

	private int mTextureId = GlUtil.NO_TEXTURE;

	private int uSourceImageLoc;

	private float[] mMVPMatrix = new float[16];
	private float[] mTexMatrix = new float[16];

	public WatermarkFilter(Context context) {
		super(context);

		Matrix.setIdentityM(mMVPMatrix, 0);
		Matrix.setIdentityM(mTexMatrix, 0);
		// 翻转纹理
		mTexMatrix[5] = -1;
		mTexMatrix[13] = 1;

		createProgram(R.raw.vertex_shader, R.raw.fragment_origin_shader);
	}

	/**
	 * 计算水印的大小和位置
	 *
	 * @param x 水印x位置
	 * @param y 水印y位置
	 * @param scaleX 水印x缩放比例
	 * @param scaleY 水印y缩放比例
	 */
	public void calculatePosition(float x, float y, float scaleX, float scaleY) {

		Matrix.setIdentityM(mMVPMatrix, 0);
		Matrix.translateM(mMVPMatrix, 0, x, y, 0.0f);
		Matrix.scaleM(mMVPMatrix, 0, scaleX, scaleY, 1.0f);
	}

	/**
	 * 更换Bitmap
	 *
	 * @param bitmap Bitmap对象
	 */
	public void changeBitmap(Bitmap bitmap) {
		mTextureId = GlUtil.setBitmapOnTexture(mTextureId, bitmap);
	}

	@Override
	protected void onGetUniformLocation(int program) {
		uSourceImageLoc = GLES20.glGetUniformLocation(program, "sourceImage");
	}

	@Override
	protected void onSetUniformData() {

	}

	@Override
	protected void onBindTexture(int textureId) {
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
		GLES20.glUniform1i(uSourceImageLoc, 1);
	}

	public void draw() {
		super.draw(0, mMVPMatrix, mTexMatrix);
	}

	@Override
	public void release() {
		super.release();

		if (mTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[mTextureId], 0);
			mTextureId = GlUtil.NO_TEXTURE;
		}
	}
}
