package cn.poco.video.render.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import cn.poco.interphoto2.R;
import cn.poco.video.render.draw.BaseDraw;

/**
 * Created by: fwc
 * Date: 2017/12/20
 */
public class BlurFilter extends BaseDraw {

	private final int mWidth;
	private final int mHeight;
	private int uSourceImageLoc;

	private int uOffsetCoordinateLoc;

	private float[] mMvpMatrix = new float[16];
	private float[] mTexMatrix = new float[16];

	private boolean isNext;

	private float mRadius = 1.2f;

	public BlurFilter(Context context, int width, int height) {
		super(context);

		mWidth = width;
		mHeight = height;
		createProgram(R.raw.vertex_blur, R.raw.fragment_blur);
	}

	@Override
	protected void onGetUniformLocation(int program) {
		uSourceImageLoc = GLES20.glGetUniformLocation(program, "sourceImage");
		uOffsetCoordinateLoc = GLES20.glGetUniformLocation(program, "offsetCoordinate");
	}

	@Override
	protected void onSetUniformData() {
		if (!isNext) {
			GLES20.glUniform2f(uOffsetCoordinateLoc, mRadius * 5.0f / mWidth, 0.f);
		} else {
			GLES20.glUniform2f(uOffsetCoordinateLoc, 0.f, mRadius * 2.0f / mHeight);
		}
	}

	public void setRadius(float radius) {
		if (radius < 0) {
			radius = 0;
		} else if (radius > 1.2f) {
			radius = 1.2f;
		}

		mRadius = radius;
	}

	@Override
	protected void onBindTexture(int textureId) {
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glUniform1i(uSourceImageLoc, 0);
	}

	/**
	 * 需要画两次
	 * @param textureId 纹理id
	 * @param first 是否是第一次画
	 */
	public void draw(int textureId, boolean first) {
		isNext = !first;
		initMatrix();
		draw(textureId, mMvpMatrix, mTexMatrix);
	}

	private void initMatrix() {
		Matrix.setIdentityM(mMvpMatrix, 0);
		Matrix.setIdentityM(mTexMatrix, 0);
		if (!isNext) {
			Matrix.scaleM(mMvpMatrix, 0, 0.4f, 0.4f, 1);
			Matrix.translateM(mMvpMatrix, 0, -0.6f / 0.4f, -0.6f / 0.4f, 0);

			// 注意矩阵操作是反过来执行的
//			Matrix.translateM(mMvpMatrix, 0, -0.6f, -0.6f, 0);
//			Matrix.scaleM(mMvpMatrix, 0, 0.4f, 0.4f, 1);

		} else {
			Matrix.scaleM(mTexMatrix, 0, 0.4f, 0.4f, 1);
		}
	}
}
