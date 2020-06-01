package cn.poco.light;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.poco.graphics.ShapeEx;
import cn.poco.video.render.gles.OffscreenBuffer;
import cn.poco.video.view.base.GLTextureView;

/**
 * Created by admin on 2017/7/26.
 */

public class LightEffectRenderer implements GLTextureView.Renderer
{
	private int mWidth;
	private int mHeight;
	private Context mContext;

	private ShapeEx mImg;
	private LightEffectShapeEx mEffect;

	private ShapeEx mOutImg;
	private LightEffectShapeEx mOutEffect;

	private EffectGLBitmap mGLEffect;

	private OnCaptureCallback mSaveCB;
	private boolean  mCapture = false;	//获取当前显示的图片
	private boolean  mSave = false;		//得到最终保存的大图
	private boolean mClearFlag = false;
	private int mSaveFlag = -1; //0 表示传入底图，1 表示光效图也传入了 mSaveFlag = 1的时候才开始混合

	public LightEffectRenderer(Context context)
	{
		mContext = context.getApplicationContext();
	}

	public void setSaveCB(OnCaptureCallback cb)
	{
		mSaveCB = cb;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		GLES20.glClearColor(0, 0, 0, 1);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		if(height == 0)
		{
			height = 1;
		}
		mWidth = width;
		mHeight = height;

		if(!mSave)
		{
			GLES20.glViewport(0, 0, mWidth, mHeight);
//			onCreateTextureID();
		}
	}


	private boolean mNeedDelete = true;

	@Override
	public boolean onDrawFrame(GL10 gl)
	{
		GLES20.glClearColor(0, 0, 0, 0);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);

		if(!mSave)
		{
			if(mClearFlag)
			{
				mClearFlag = false;
				clear();
			}
			if(mNeedDelete)
			{
				delEffect();
				mNeedDelete = false;
			}
			onCreateTextureID();
			if(mGLEffect != null)
			{
				mGLEffect.draw(mImg, mEffect);
			}
		}
		if(mCapture)
		{
			mCapture = false;
			int width = mWidth;
			int height = mHeight;
			IntBuffer mFrameBuf;
			mFrameBuf = IntBuffer.allocate(width * height);
			GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mFrameBuf);
			mFrameBuf.rewind();
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bmp.copyPixelsFromBuffer(mFrameBuf);
			mFrameBuf.clear();
			if(mSaveCB != null)
			{
				mSaveCB.onCapture(bmp);
			}
			clear();
		}
		if(mSave)
		{
			mSave = false;
			int width = mOutImg.m_w;
			int height = mOutImg.m_h;
			GLES20.glViewport(0, 0, width, height);
			OffscreenBuffer buffer1 = new OffscreenBuffer(width, height);
			buffer1.bind();

			EffectGLBitmap effect = new EffectGLBitmap(mContext);
			effect.SetBufferID(buffer1.getFrameBufferId());
			effect.setViewWH(width, height);
			try
			{
				effect.onCreateOriginTextureID(mOutImg);
				if(mOutImg != null)
				{
					mOutImg.m_bmp = null;
				}
				effect.onCreateEffectTextureID(mOutEffect);
				if(mOutEffect != null)
				{
					mOutEffect.m_bmp = null;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			effect.draw(mOutImg, mOutEffect);

			IntBuffer mFrameBuf;
			mFrameBuf = IntBuffer.allocate(width * height);
			GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mFrameBuf);
			mFrameBuf.rewind();
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bmp.copyPixelsFromBuffer(mFrameBuf);
			mFrameBuf.clear();
			buffer1.unbind();
			buffer1.release();
			effect.onDeleteEffect();
			effect.onDeleteOrigin();
			if(mSaveCB != null)
			{
				mSaveCB.onSave(bmp);
			}
		}

		return true;
	}

	@Override
	public void onSurfaceDestroyed()
	{
		mClearFlag = true;
	}

	public void setCapture() {
		mCapture = true;
	}

	public void setFinalSave() {
		mSave = true;
	}

	public void setOutImg(ShapeEx shapeEx) {
		mOutImg = shapeEx;
	}

	public void setOutEffect(LightEffectShapeEx shapeEx)
	{
		mOutEffect = shapeEx;
	}

	public void addImg(ShapeEx img)
	{
		mImg = img;
	}

	public void addEffect(LightEffectShapeEx effect)
	{
//		delEffect();
		mNeedDelete = true;
		mEffect = effect;
	}

	/**
	 * 不会重复创建
	 */
	private void onCreateTextureID()
	{
		if(mGLEffect == null)
		{
			mGLEffect = new EffectGLBitmap(mContext);
		}
		mGLEffect.setViewWH(mWidth, mHeight);
		try
		{
			mGLEffect.onCreateOriginTextureID(mImg);
			mGLEffect.onCreateEffectTextureID(mEffect);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void delEffect()
	{
		if(mGLEffect != null)
		{
			mGLEffect.onDeleteEffect();
		}
	}

	private void clear()
	{
		if(mGLEffect != null)
		{
			mGLEffect.onDeleteEffect();
			mGLEffect.onDeleteOrigin();
			mGLEffect.releaseBuffer();
		}
	}

	public static interface OnCaptureCallback
	{
		public void onCapture(Bitmap bitmap);
		public void onSave(Bitmap bmp);
	}
}
