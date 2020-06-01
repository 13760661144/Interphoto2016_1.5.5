package cn.poco.video.render.adjust;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import cn.poco.interphoto2.R;
import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/21
 * 暗角：0~1，默认0
 */
public class DarkCornerAdjust extends AbstractAdjust {

	private int mTextureId = GlUtil.NO_TEXTURE;
	private int mDarkenCornerImageLoc;

	public DarkCornerAdjust(Context context) {
		super(context);

		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.dark_corner);
		mTextureId = GlUtil.createTexture(bitmap);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}

		createProgram(R.raw.vertex_shader, R.raw.fragment_dark_corner);
	}

	@Override
	protected void checkParams(AbstractAdjust.Params params) {
		if (!(params instanceof Params)) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	protected void onGetUniformLocation(int program) {
		super.onGetUniformLocation(program);
		mDarkenCornerImageLoc = GLES20.glGetUniformLocation(program, "darkenCornerImage");
	}

	@Override
	protected void onBindTexture(int textureId) {
		super.onBindTexture(textureId);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
		GLES20.glUniform1i(mDarkenCornerImageLoc, 1);
	}

	@Override
	public void release() {

		if (mTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mTextureId}, 0);
			mTextureId = GlUtil.NO_TEXTURE;
		}

		super.release();
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
			if (item.id == AdjustItem.DARK_CORNER) {
				values[0] = item.value;
			} else {
				throw new RuntimeException();
			}
		}
	}
}
