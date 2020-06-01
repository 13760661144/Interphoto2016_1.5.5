package cn.poco.video.render.curve;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;

import cn.poco.interphoto2.R;
import cn.poco.video.render.draw.BaseDraw;
import cn.poco.video.render.gles.GlUtil;

/**
 * Created by: fwc
 * Date: 2017/12/28
 */
public class CurveEffect extends BaseDraw {

	public static final int CURVE_R = 1;
	public static final int CURVE_B = 2;
	public static final int CURVE_G = 3;
	public static final int CURVE_RGB = 4;

	private int uSourceImageLoc;

	private int mCurveTextureId = GlUtil.NO_TEXTURE;
	private int uCurveImageLoc;

	private ByteBuffer mData;

	public CurveEffect(Context context) {
		super(context);

		mData = ByteBuffer.allocateDirect(256 * 3);

		mCurveTextureId = GlUtil.createTexture(GLES20.GL_TEXTURE_2D);

		createProgram(R.raw.vertex_shader, R.raw.fragment_curve);
	}

	@Override
	protected void onGetUniformLocation(int program) {
		uSourceImageLoc = GLES20.glGetUniformLocation(program, "sourceImage");
		uCurveImageLoc = GLES20.glGetUniformLocation(program, "curveImage");
	}

	@Override
	protected void onSetUniformData() {

	}

	public void setData(byte[] data) {
		mData.clear();
		mData.put(data);
		mData.flip();

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCurveTextureId);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, 256, 1, 0,
							GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, mData);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	@Override
	protected void onBindTexture(int textureId) {
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glUniform1i(uSourceImageLoc, 0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mCurveTextureId);
		GLES20.glUniform1i(uCurveImageLoc, 1);
	}

	public void draw(int textureId) {
		super.draw(textureId, GlUtil.IDENTITY_MATRIX, GlUtil.IDENTITY_MATRIX);
	}

	@Override
	public void release() {
		super.release();

		if (mCurveTextureId != GlUtil.NO_TEXTURE) {
			GLES20.glDeleteTextures(1, new int[] {mCurveTextureId}, 0);
			mCurveTextureId = GlUtil.NO_TEXTURE;
		}

		if (mData != null) {
			mData.clear();
			mData = null;
		}
	}

	public static class Params {
		public int[] rgb = new int[256];
		public int[] r = new int[256];
		public int[] g = new int[256];
		public int[] b = new int[256];

		public byte[] mDatas;

		private static final int[] IDENTITY_MAP = new int[256];

		static {
			for (int i = 0; i < 256; i++) {
				IDENTITY_MAP[i] = i;
			}
		}

		public Params() {
//			Arrays.fill(rgb, 0);
//			Arrays.fill(r, 0);
//			Arrays.fill(g, 0);
//			Arrays.fill(b, 0);

			mDatas = new byte[3 * 256];
			for (int i = 0; i < 256; i++) {
				mDatas[i * 3] = mDatas[i * 3 + 1] = mDatas[i * 3 + 2] = (byte)i;
			}
		}

		public void doCurve(int curveType, int[] mapPoints) {

			switch (curveType) {
				case CURVE_RGB:
					mapPoints(rgb, mapPoints);
					calculate(0, r);
					calculate(1, g);
					calculate(2, b);
					break;
				case CURVE_R:
					mapPoints(r, mapPoints);
					calculate(0, r);
					break;
				case CURVE_G:
					mapPoints(g, mapPoints);
					calculate(1, g);
					break;
				case CURVE_B:
					mapPoints(b, mapPoints);
					calculate(2, b);
					break;
				default:
					break;
			}
		}

		private void mapPoints(int[] data, int[] mapPoints) {
			for (int i = 0; i < 256; i++) {
				data[i] = mapPoints[i] - i;
			}
		}

		private void calculate(int offset, int[] colors) {
			int index;
			for (int i = 0; i < 256; i++) {
				index = checkRange(colors[i] + i, 0, 255);
				mDatas[i * 3 + offset] = (byte)checkRange(index + rgb[index], 0, 255);
			}
		}

		private int checkRange(int value, int min, int max) {
			if (value < min) {
				return min;
			} else if (value > max) {
				return max;
			}

			return value;
		}

		public void reset(int curveType) {
			doCurve(curveType, IDENTITY_MAP);
		}

		public Params Clone() {
			Params params = new Params();

			System.arraycopy(rgb, 0, params.rgb, 0, 256);
			System.arraycopy(r, 0, params.r, 0, 256);
			System.arraycopy(g, 0, params.g, 0, 256);
			System.arraycopy(b, 0, params.b, 0, 256);
			System.arraycopy(mDatas, 0, params.mDatas, 0, 256);

			return params;
		}
	}
}
