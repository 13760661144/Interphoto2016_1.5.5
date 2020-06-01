package cn.poco.video.render.draw;

import android.content.Context;
import android.opengl.GLES20;
import android.support.annotation.RawRes;

import cn.poco.video.render.gles.Drawable2d;
import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/15
 */
public abstract class BaseDraw {

	protected Context mContext;

	private int mProgram = 0;
	private Drawable2d mDrawable2d = new Drawable2d();

	private int aPositionLoc;
	private int aTextureCoordLoc;

	private int uMVPMatrixLoc;
	private int uTexMatrixLoc;

	public BaseDraw(Context context) {
		mContext = context;
	}

	protected void createProgram(@RawRes int vertexShader, @RawRes int fragmentShader) {
		mProgram = GlUtil.createProgram(mContext, vertexShader, fragmentShader);

		aPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition");
		aTextureCoordLoc = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
		uMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		uTexMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uTexMatrix");

		onGetUniformLocation(mProgram);
	}

	public final void draw(int textureId, float[] mvpMatrix, float[] texMatrix) {
		onUseProgram();
		onSetUniformData();
		onBindTexture(textureId);
		onDraw(mvpMatrix, texMatrix);
	}

	protected void onUseProgram(){
		GLES20.glUseProgram(mProgram);
	}

	/**
	 * 获取Uniform变量位置
	 */
	protected abstract void onGetUniformLocation(int program);

	/**
	 * 设置Uniform变量数据
	 */
	protected abstract void onSetUniformData();

	/**
	 * 绑定纹理
	 */
	protected abstract void onBindTexture(int textureId);

	/**
	 * 绘制
	 */
	protected void onDraw(float[] mvpMatrix, float[] texMatrix) {

		GLES20.glUniformMatrix4fv(uMVPMatrixLoc, 1, false, mvpMatrix, 0);
		GLES20.glUniformMatrix4fv(uTexMatrixLoc, 1, false, texMatrix, 0);

		GLES20.glEnableVertexAttribArray(aPositionLoc);
		GLES20.glVertexAttribPointer(aPositionLoc, mDrawable2d.getCoordsPerVertex(),
									 GLES20.GL_FLOAT, false, mDrawable2d.getVertexStride(), mDrawable2d.getVertexArray());

		GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
		GLES20.glVertexAttribPointer(aTextureCoordLoc, 2,
									 GLES20.GL_FLOAT, false, mDrawable2d.getTexCoordStride(), mDrawable2d.getTexCoordArray());

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mDrawable2d.getVertexCount());

		GLES20.glDisableVertexAttribArray(aPositionLoc);
		GLES20.glDisableVertexAttribArray(aTextureCoordLoc);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES20.glUseProgram(0);
	}

	public void release() {
		if (mProgram != 0) {
			GLES20.glDeleteProgram(mProgram);
			mProgram = 0;
		}
	}
}
