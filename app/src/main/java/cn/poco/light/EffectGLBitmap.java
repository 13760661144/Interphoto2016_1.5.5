package cn.poco.light;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import cn.poco.graphics.Shape;
import cn.poco.graphics.ShapeEx;
import cn.poco.interphoto2.R;
import cn.poco.video.render.gles.GlUtil;
import cn.poco.video.render.gles.OffscreenBuffer;

/**
 * Created by admin on 2017/7/28.
 */

public class EffectGLBitmap
{
	private Context mContext;
	private int mWidth;
	private int mHeight;

	private int mOriginTextureID = -1;
	private int mTextureID = -1;
	private boolean mOrigin = false;	//是否只显示原图

	public FloatBuffer vertexBuffer; 	//顶点坐标
	private float[] vertices = new float[]{
			-1f, -1f, // V1 - bottom left
			-1f, 1f, // V2 - top left
			1f, -1f, // V3 - bottom right
			1f, 1f // V4 - top right
	};
	public FloatBuffer aTextureBuffer;	//原图纹理坐标
	private float texture[] = {
			0.0f, 1.0f, // top left (V2)
			0.0f, 0.0f, // bottom left (V1)
			1.0f, 1.0f, // top right (V4)
			1.0f, 0.0f // bottom right (V3)
	};

	public FloatBuffer bTextureBuffer;	//混合纹理坐标
	private float bTexture[] = {
			0.0f, 0.0f, // top left (V2)
			0.0f, 1.0f, // bottom left (V1)
			1.0f, 0.0f, // top right (V4)
			1.0f, 1.0f // bottom right (V3)
	};

	private int mProgram = -1;		//合成通道
	private int mProgram0 = -1;		//光效图通道
	private int mProgram1 = -1;		//底图通道
	private int mProgramType;
	private int aPositionLoc;
	private int aTextureCoordLoc;
	private int bTextureCoordLoc;
	private int mAlpha;
	private int mBlendType;
	private int mSourceImg;
	private int mSecondSourceImage;
	private int mUMVPMatrixLoc;
	private int mUTexMatrixLoc;

	private float[] mMVPMatrix = new float[16];
	private float[] mTexMatrix = new float[16];

	private android.graphics.Matrix mMatrix;
	private float[] mSrc = new float[8];
	private float[] mDst = new float[8];

	public EffectGLBitmap(Context context)
	{
		mContext = context;
		mMatrix = new android.graphics.Matrix();
		loadVertex();
	}

	public void setViewWH(int width, int height)
	{
		mWidth = width;
		mHeight = height;
	}

	private void loadVertex()
	{
		vertexBuffer = GlUtil.createFloatBuffer(vertices);
		aTextureBuffer = GlUtil.createFloatBuffer(texture);
		bTextureBuffer = GlUtil.createFloatBuffer(bTexture);
		Matrix.setIdentityM(mMVPMatrix, 0);
		Matrix.setIdentityM(mTexMatrix, 0);
	}

	//创建纹理id
	public void onCreateOriginTextureID(ShapeEx shape)
	{
		if(mOriginTextureID != -1)
		{
			return;
		}

		GlUtil.getMaxTextureSize();
		if(shape != null && shape.m_bmp != null && !shape.m_bmp.isRecycled())
		{
			mOriginTextureID = loadGLTexture(shape.m_bmp);
//			onGetUniformLocation1(mProgram1);
		}
	}

	public void onCreateEffectTextureID(ShapeEx shape)
	{
		if(mTextureID != -1)
		{
			return;
		}
		if(shape == null){
			mTextureID = GlUtil.createTexture(GLES20.GL_TEXTURE_2D);
			if(mTextureID > 0)
			{
				mOrigin = true;
				if(!checkProgramExist(mProgram0))
				{
					mProgram1 = GlUtil.createProgram(mContext, R.raw.vertex_shader, R.raw.fragment_origin_shader);
				}
			}
			else
			{
				mTextureID = -1;
			}
			return;
		}
		LightEffectShapeEx item = null;
		if(shape instanceof LightEffectShapeEx)
		{
			item = (LightEffectShapeEx)shape;
		}
		if(item != null && item.m_bmp != null)
		{
			mTextureID = loadGLTexture(shape.m_bmp);
			if(mTextureID > 0)
			{
				mOrigin = false;
				if(!checkProgramExist(mProgram0))
				{
					mProgram0 = GlUtil.createProgram(mContext, R.raw.vertex_shader, R.raw.fragment_origin_shader);
				}
				if(item.m_mode != mProgramType)
				{
					deleteProgram(mProgram);
					createLightEffectProgram(item.m_mode);
				}
				else if(!checkProgramExist(mProgram))
				{
					createLightEffectProgram(item.m_mode);
				}
			}
		}
	}

	private void createLightEffectProgram(int type)
	{
		mProgramType = type;
		switch(type)
		{
			case 1:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_normal);
				break;
			case 33:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_lighten);
				break;
			case 34:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_linear_lighten);
				break;
			case 38:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_multiply);
				break;
			case 41:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_overlay);
				break;
			case 45:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_screen);
				break;
			case 46:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_soft_light);
				break;
			case 59:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_vividlight);
				break;
			case 61:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_linear_dodge);
				break;
			case 8:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_color_burn);
				break;
			case 9:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_color_dodge);
				break;
			case 20:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_darken);
				break;
			case 26:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_difference);
				break;
			case 29:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_exclusion);
				break;
			case 30:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_hard_light);
				break;
			default:
				mProgram = GlUtil.createProgram(mContext, R.raw.vertex_lighteffect, R.raw.fragment_lighteffect_screen);
				break;
		}
	}

	public int loadGLTexture(Bitmap bmp)
	{
		if(bmp != null && !bmp.isRecycled())
		{
			int[] textures = new int[1];
			GLES20.glGenTextures(1, textures, 0);
//			GlUtil.checkGlError("glGenTextures");
			if (textures[0] != 0)
			{
				// Bind to the texture in OpenGL
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
				// Set filtering
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
				GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
				return textures[0];
			}
			else
			{
				//创建失败删除纹理id
				GLES20.glDeleteTextures(1, textures, 0);
			}
		}
		return -1;
	}

	protected void onGetUniformLocation(int program)
	{
		aPositionLoc = GLES20.glGetAttribLocation(program, "aPosition");
		aTextureCoordLoc = GLES20.glGetAttribLocation(program, "aTextureCoord");
		mSourceImg = GLES20.glGetUniformLocation(program, "sourceImage");

		bTextureCoordLoc = GLES20.glGetAttribLocation(program, "bTextureCoord");
		mSecondSourceImage = GLES20.glGetUniformLocation(program, "secondSourceImage");
		mAlpha = GLES20.glGetUniformLocation(program, "alpha");
		mBlendType = GLES20.glGetUniformLocation(program, "blendType");
	}

	//原图的时候
	protected void onGetUniformLocation1(int program)
	{
		aPositionLoc = GLES20.glGetAttribLocation(program, "aPosition");
		aTextureCoordLoc = GLES20.glGetAttribLocation(program, "aTextureCoord");
		mSourceImg = GLES20.glGetUniformLocation(program, "sourceImage");

		mUMVPMatrixLoc = GLES20.glGetUniformLocation(program, "uMVPMatrix");
		mUTexMatrixLoc = GLES20.glGetUniformLocation(program, "uTexMatrix");
	}

	public void calculatePosition(ShapeEx shape, int viewW, int viewH)
	{
		if(shape != null)
		{
			float[] dst = {shape.m_x + shape.m_centerX, shape.m_y + shape.m_centerY};
//			float[] dst = new float[2];
//			GetShowPos(dst, src);

			mMatrix.reset();
			if(shape.m_flip == Shape.Flip.VERTICAL)
			{
				float[] values = {1, 0, 0, 0, -1, shape.m_h, 0, 0, 1};
				mMatrix.setValues(values);
			}
			else if(shape.m_flip == Shape.Flip.HORIZONTAL)
			{
				float[] values = {-1, 0, shape.m_w, 0, 1, 0, 0, 0, 1};
				mMatrix.setValues(values);
			}

			mMatrix.postTranslate(dst[0] - shape.m_centerX, dst[1] - shape.m_centerY);
			mMatrix.postScale(shape.m_scaleX, shape.m_scaleY, dst[0], dst[1]);
			mMatrix.postRotate(shape.m_degree, dst[0], dst[1]);
			mSrc[0] = 0;
			mSrc[1] = 0;
			mSrc[2] = shape.m_w;
			mSrc[3] = 0;
			mSrc[4] = shape.m_w;
			mSrc[5] = shape.m_h;
			mSrc[6] = 0;
			mSrc[7] = shape.m_h;
			mMatrix.mapPoints(mDst, mSrc);

			/*vertices = new float[]{
					-1f, -1f, // V1 - bottom left
					-1f, 1f, // V2 - top left
					1f, -1f, // V3 - bottom right
					1f, 1f // V4 - top right
			};*/
			vertices[0] = mDst[6] / viewW * 2 - 1;
			vertices[1] = 1 - mDst[7] / viewH * 2;
			vertices[2] = mDst[0] / viewW * 2 - 1;
			vertices[3] = 1 - mDst[1] / viewH * 2;
			vertices[4] = mDst[4] / viewW * 2 - 1;
			vertices[5] = 1 - mDst[5] / viewH * 2;
			vertices[6] = mDst[2] / viewW * 2 - 1;
			vertices[7] = 1 - mDst[3] / viewH * 2;
			vertexBuffer = GlUtil.createFloatBuffer(vertices);

			/*Matrix.setIdentityM(mMVPMatrix, 0);
			float realW = shape.m_w * shape.m_scaleX;
			float realH = shape.m_h * shape.m_scaleY;
			float scaleX = realW / viewW;
			float scaleY = realH / viewH;

			float centerX = shape.m_x + shape.m_centerX;
			float centerY = shape.m_y + shape.m_centerY;
			float orgCenterX = viewW / 2f;
			float orgCenterY = viewH / 2f;
			float dx = centerX - orgCenterX;
			float dy = centerY - orgCenterY;

			float glDx = dx / viewW * 2;
			float glDy = -dy / viewH * 2;

			System.out.println("glDx: " + glDx + "glDy: " + glDy);
			System.out.println("scaleX: " + scaleX + "scaleY: " + scaleY);
			System.out.println("width: " + viewW + "height: " + viewH);

			Matrix.translateM(mMVPMatrix, 0, glDx, glDy, 0.0f);
			Matrix.scaleM(mMVPMatrix, 0, scaleX, scaleY, 1.0f);
			Matrix.rotateM(mMVPMatrix, 0, shape.m_degree, 0.0f, 0.0f, -1.0f);*/
		}
	}

	public void onDeleteEffect()
	{
		GLES20.glUseProgram(0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		if(mTextureID != -1)
		{
			GLES20.glDeleteTextures(1, new int[]{mTextureID}, 0);
			mTextureID = -1;
		}
	}

	private boolean checkProgramExist(int program)
	{
		if(program > 0)
		{
			int[] linkStatus = new int[1];
			boolean isProgram = GLES20.glIsProgram(program);
			if(isProgram)
			{
				GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
				if (linkStatus[0] == GLES20.GL_TRUE) {
					return true;
				}
			}
		}
		return false;
	}

	private void deleteProgram(int program)
	{
		if(program > 0)
		{
			int[] linkStatus = new int[1];
			boolean isProgram = GLES20.glIsProgram(program);
			if(isProgram)
			{
				GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
				if (linkStatus[0] == GLES20.GL_TRUE) {
					GLES20.glDeleteProgram(program);
				}
			}
		}
	}

	public void onDeleteOrigin()
	{
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		if(mOriginTextureID != -1)
		{
			GLES20.glDeleteTextures(1, new int[]{mOriginTextureID}, 0);
			mOriginTextureID = -1;
		}
		/*deleteProgram(mProgram1);
		mProgram1 = -1;*/
	}

	protected void onFlip(ShapeEx shape)
	{
		if(shape != null)
		{
			if(shape.m_flip == Shape.Flip.VERTICAL)
			{
				vertices[0] = -1f;
				vertices[1] = 1f;
				vertices[2] = -1f;
				vertices[3] = -1f;
				vertices[4] = 1f;
				vertices[5] = 1f;
				vertices[6] = 1f;
				vertices[7] = -1f;
			}
			else if(shape.m_flip == Shape.Flip.HORIZONTAL)
			{
				vertices[0] = 1f;
				vertices[1] = -1f;
				vertices[2] = 1f;
				vertices[3] = 1f;
				vertices[4] = -1f;
				vertices[5] = -1f;
				vertices[6] = -1f;
				vertices[7] = 1f;
			}
			else
			{
				vertices[0] = -1f;
				vertices[1] = -1f;
				vertices[2] = -1f;
				vertices[3] = 1f;
				vertices[4] = 1f;
				vertices[5] = -1f;
				vertices[6] = 1f;
				vertices[7] = 1f;
			}
			vertexBuffer = GlUtil.createFloatBuffer(vertices);
		}
	}

	protected void onSetUniformData(ShapeEx shape)
	{
		LightEffectShapeEx item = null;
		if(shape instanceof LightEffectShapeEx)
		{
			item = (LightEffectShapeEx)shape;
		}
		if(item != null)
		{
			float alpha = item.m_alpha / 120f;
			GLES20.glUniform1f(mAlpha, alpha);
		}
	}

	protected void onSetUniformData1()
	{
		GLES20.glUniformMatrix4fv(mUMVPMatrixLoc, 1, false, mMVPMatrix, 0);

		GLES20.glUniformMatrix4fv(mUTexMatrixLoc, 1, false, mTexMatrix, 0);
	}

	/**
	 * 只画1张图
	 * @param shape
	 * @param program
	 */
	private void drawTexture(ShapeEx shape, int program, int textureID)
	{
		//先计算坐标
		if(!mSave)
		{
			calculatePosition(shape, mWidth, mHeight);
//			onFlip(shape);
		}

		GLES20.glClearColor(0, 0, 0, 0);
		GLES20.glUseProgram(program);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
		GLES20.glUniform1i(mSourceImg, 1);

		onSetUniformData1();

		GLES20.glEnableVertexAttribArray(aPositionLoc);
		GLES20.glVertexAttribPointer(aPositionLoc, 2,
									 GLES20.GL_FLOAT, false, 8, vertexBuffer);

		GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
		GLES20.glVertexAttribPointer(aTextureCoordLoc, 2,
									 GLES20.GL_FLOAT, false, 8, aTextureBuffer);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertices.length / 2);

		GLES20.glDisableVertexAttribArray(aPositionLoc);
		GLES20.glDisableVertexAttribArray(aTextureCoordLoc);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES20.glUseProgram(0);
	}

	/**
	 *
	 * @param shape
	 * @param program
	 * @param textureID1	底图的纹理id
	 * @param textureID2	光效的纹理id
	 */
	private void drawTexture1(ShapeEx org, ShapeEx shape, int program, int textureID1, int textureID2)
	{
		//先计算坐标
		calculatePosition(org, mWidth, mHeight);
//		onFlip(org);
		GLES20.glUseProgram(program);
		onSetUniformData(shape);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID2);
		GLES20.glUniform1i(mSecondSourceImage, 1);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID1);
		GLES20.glUniform1i(mSourceImg, 2);

		GLES20.glEnableVertexAttribArray(aPositionLoc);
		GLES20.glVertexAttribPointer(aPositionLoc, 2,
									 GLES20.GL_FLOAT, false, 8, vertexBuffer);

		GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
		GLES20.glVertexAttribPointer(aTextureCoordLoc, 2,
									 GLES20.GL_FLOAT, false, 8, aTextureBuffer);

		GLES20.glEnableVertexAttribArray(bTextureCoordLoc);
		GLES20.glVertexAttribPointer(bTextureCoordLoc, 2,
									 GLES20.GL_FLOAT, false, 8, bTextureBuffer);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertices.length / 2);

		GLES20.glDisableVertexAttribArray(aPositionLoc);
		GLES20.glDisableVertexAttribArray(aTextureCoordLoc);
		GLES20.glDisableVertexAttribArray(bTextureCoordLoc);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES20.glUseProgram(0);
	}

	private boolean  mSave = false;
	public void setFinalSave() {
		mSave = true;
	}

	private int mBufferID = 0;
	private OffscreenBuffer mBuffer;

	public void SetBufferID(int bufferID)
	{
		this.mBufferID = bufferID;
	}

	public void releaseBuffer()
	{
		if(mBuffer != null)
		{
			mBuffer.release();
			mBuffer = null;
		}
	}

	public void draw(ShapeEx org, ShapeEx effect)
	{
		if(mOrigin)
		{
			onGetUniformLocation1(mProgram1);
			drawTexture(org, mProgram1, mOriginTextureID);
		}
		else
		{
//			long time = System.currentTimeMillis();
			if(mBuffer == null)
			{
				mBuffer = new OffscreenBuffer(mWidth, mHeight);
			}
			else
			{
				if(!GLES20.glIsTexture(mBuffer.getTextureId()))
				{
					mBuffer.release();
					mBuffer = new OffscreenBuffer(mWidth, mHeight);
				}
			}
			mBuffer.bind();
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);

			onGetUniformLocation1(mProgram0);
			drawTexture(effect, mProgram0, mTextureID);

			mBuffer.unbind(mBufferID);

			onGetUniformLocation(mProgram);
			drawTexture1(org, effect, mProgram, mOriginTextureID, mBuffer.getTextureId());
			GLES20.glFinish();
//			long time1 = System.currentTimeMillis();
//			System.out.println("time: " + (time1 - time));
		}
	}
}
