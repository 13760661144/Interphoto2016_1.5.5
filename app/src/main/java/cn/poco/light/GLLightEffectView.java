package cn.poco.light;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import cn.poco.graphics.Shape;
import cn.poco.graphics.ShapeEx;
import cn.poco.resource.LightEffectRes;
import cn.poco.tianutils.ImageUtils;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;
import cn.poco.video.render.gles.GlUtil;
import cn.poco.video.view.base.GLTextureView;

/**
 * 用OpenGL实现光效
 */

public class GLLightEffectView extends GLTextureView implements GLTextureView.SurfaceTextureListener
{

	protected LightEffectRenderer mRenderer;
	protected Paint temp_paint = new Paint();

	private ShapeEx mImg;
	private int mFrW;
	private int mFrH;
	private LightEffectShapeEx mEffect;
	private float mDefMinScale = 0.5f;
	private float mDefMaxScale = 2f;

	private ControlCallback mCb;

	private boolean mUIEnable = true;

	protected boolean m_isTouch = false;

	public GLLightEffectView(Context context, int frW, int frH)
	{
		super(context);
		Init(frW, frH);
	}

	protected void Init(int frW, int frH)
	{
		mFrW = frW;
		mFrH = frH;
		setFocusableInTouchMode(true);
		setEGLContextClientVersion(2);

		mRenderer = new LightEffectRenderer(getContext());
		setRenderer(mRenderer);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}

	public void setControlCallback(ControlCallback cb)
	{
		this.mCb = cb;
	}

	public void SetUIEnabled(boolean state)
	{
		mUIEnable = state;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(mUIEnable)
		{
			switch(event.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_MOVE:
				{
					if(event.getPointerCount() > 1)
					{
						EvenMove(event);
					}
					else
					{
						OddMove(event);
					}

					break;
				}

				case MotionEvent.ACTION_DOWN:
				{
					//System.out.println("ACTION_DOWN");
					//System.out.println(event.getX() + "-" + event.getY());
					m_downX = event.getX();
					m_downY = event.getY();
					OddDown(event);
					break;
				}

				case MotionEvent.ACTION_OUTSIDE:
				case MotionEvent.ACTION_UP:
				{
					//System.out.println("ACTION_UP");
					//System.out.println(event.getX() + "-" + event.getY());
					OddUp(event);
					break;
				}

				case MotionEvent.ACTION_POINTER_DOWN:
				{
					//System.out.println("ACTION_POINTER_DOWN");
					//System.out.println(event.getX(event.getActionIndex()) + "-" + event.getY(event.getActionIndex()));
					m_downX1 = event.getX(0);
					m_downY1 = event.getY(0);
					m_downX2 = event.getX(1);
					m_downY2 = event.getY(1);
					EvenDown(event);
					break;
				}

				case MotionEvent.ACTION_POINTER_UP:
				{
					//System.out.println("ACTION_POINTER_UP");
					//System.out.println(event.getX(event.getActionIndex()) + "-" + event.getY(event.getActionIndex()));
					EvenUp(event);
					break;
				}
			}
		}
		return true;
	}

	protected void OddDown(MotionEvent event)
	{
		m_isTouch = true;
		if(mEffect != null)
		{
			Init_M_Data(mEffect, m_downX, m_downY);
		}
		if(mCb != null)
		{
			mCb.onTouch(true);
		}
	}

	protected void OddMove(MotionEvent event)
	{
		if(m_isTouch)
		{
			if(mEffect != null)
			{
				Run_M(mEffect, event.getX(), event.getY());
			}
			UpdateUI();
		}
	}

	protected void OddUp(MotionEvent event)
	{
		m_isTouch = false;
		if(mCb != null)
		{
			mCb.onTouch(false);
		}
	}

	protected void EvenDown(MotionEvent event)
	{
		m_isTouch = true;
		if(mEffect != null)
		{
			Init_MRZ_Data(mEffect, m_downX1, m_downY1, m_downX2, m_downY2);
		}
	}

	protected void EvenMove(MotionEvent event)
	{
		if(mEffect != null)
		{
			Run_MRZ(mEffect, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
		}
		UpdateUI();
	}

	protected void EvenUp(MotionEvent event)
	{
		OddUp(event);
	}

	public void SetImg(Object info, Bitmap bmp)
	{
		mImg = new ShapeEx();
		if(bmp != null)
		{
			mImg.m_bmp = bmp;
		}
		else
		{
			mImg.m_bmp = mCb.MakeShowImg(info, mFrW, mFrH);
		}
		mImg.m_w = mImg.m_bmp.getWidth();
		mImg.m_h = mImg.m_bmp.getHeight();
		float scale1 = (float)mFrW / (float)mImg.m_w;
		float scale2 = (float)mFrH / (float)mImg.m_h;
		float scale = (scale1 > scale2) ? scale2 : scale1;
		mImg.m_w = (int)(scale * mImg.m_w);
		mImg.m_h = (int)(scale * mImg.m_h);
		mImg.m_centerX = mImg.m_w / 2f;
		mImg.m_centerY = mImg.m_h / 2f;
		mImg.m_ex = info;

		if(mRenderer != null)
		{
			mRenderer.addImg(mImg);
			mRenderer.addEffect(null);
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width = mFrW;
		int height = mFrH;
		if(mImg != null)
		{
			width = mImg.m_w;
			height = mImg.m_h;
		}
		int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		;
		super.onMeasure(widthSpec, heightSpec);
	}

	public void Flip(boolean isHorizontal)
	{
		if(mEffect != null)
		{
			if(isHorizontal)
			{
				if(mEffect.m_flip == Shape.Flip.NONE || mEffect.m_flip == Shape.Flip.VERTICAL)
				{
					mEffect.m_flip = Shape.Flip.HORIZONTAL;
				}
				else
				{
					mEffect.m_flip = Shape.Flip.NONE;
				}
			}
			else
			{
				if(mEffect.m_flip == Shape.Flip.NONE || mEffect.m_flip == Shape.Flip.HORIZONTAL)
				{
					mEffect.m_flip = Shape.Flip.VERTICAL;
				}
				else
				{
					mEffect.m_flip = Shape.Flip.NONE;
				}
			}
			UpdateUI();
		}
	}

	public void changeMixMode(int mode)
	{
		if(mEffect != null)
		{
			mEffect.m_mode = mode;
			UpdateUI();
		}
	}

	public void UpdateUI()
	{
//		this.invalidate();
		requestRender();
	}

	public ShapeEx GetCurEffect()
	{
		return mEffect;
	}

	public int AddEffect(ShapeEx shapeEx)
	{
		if(shapeEx instanceof LightEffectShapeEx)
		{
			if(shapeEx.m_bmp == null || shapeEx.m_bmp.isRecycled())
			{
				shapeEx.m_bmp = mCb.MakeShowEffect(shapeEx.m_ex, -1, -1);
			}
			mEffect = (LightEffectShapeEx)shapeEx;

			SetRendererEffect();
			return 0;
		}
		return -1;
	}

	public void onChangeEffectBmp()
	{
		SetRendererEffect();
	}

	public int AddEffect(Object info, Bitmap bmp)
	{
		mEffect = new LightEffectShapeEx();
		if(bmp != null)
		{
			mEffect.m_bmp = bmp;
		}
		else
		{
			int size = GlUtil.getMaxTextureSize();
			mEffect.m_bmp = mCb.MakeShowEffect(info, size, size);
		}
		mEffect.m_w = mEffect.m_bmp.getWidth();
		mEffect.m_h = mEffect.m_bmp.getHeight();
		mEffect.m_centerX = (float)mEffect.m_w / 2f;
		mEffect.m_centerY = (float)mEffect.m_h / 2f;
		mEffect.m_ex = info;

//			System.out.println("item.m_w: " + item.m_w);
//			System.out.println("item.m_h: " + item.m_h);

		//初始化光效位置信息
		if(info != null && info instanceof LightEffectRes)
		{
			Object res = ((LightEffectRes)info).m_res;
			File file = new File((String)res);
			if(!file.exists())
			{
				try
				{
					InputStream is = getContext().getAssets().open((String)res);
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					byte[] bytes = new byte[1024];
					while(is.read(bytes) != -1)
					{
						bout.write(bytes, 0, bytes.length);
					}
					res = bout.toByteArray();
					bout.close();
					is.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			if(res instanceof String)
			{
				Utils.DecodeFile((String)res, opts);
			}
			else if(res instanceof byte[])
			{
				BitmapFactory.decodeByteArray((byte[])res, 0, ((byte[])res).length, opts);
			}
			int orgW = opts.outWidth;
			float bmpScale = (float)orgW / mEffect.m_w;
//				System.out.println("bmpScale: " + ((LightEffectRes)info).m_scale / 100f * bmpScale);
//				System.out.println("item.m_w: " + item.m_w);
//				System.out.println("orgW: " + orgW);
			float tempScale = Math.max(ShareData.m_screenWidth / 750f, ShareData.m_screenHeight / 1334f) * 2f;
			mEffect.m_scaleX = mEffect.m_scaleY = ((LightEffectRes)info).m_scale / 100f * bmpScale * tempScale;
			mEffect.MIN_SCALE = ((LightEffectRes)info).m_minScale / 100f * bmpScale * tempScale;
			mEffect.MAX_SCALE = ((LightEffectRes)info).m_maxScale / 100f * bmpScale * tempScale;
			mEffect.m_mode = ((LightEffectRes)info).m_compose;
			String[] infos = ((LightEffectRes)info).m_location.split("\\+");
			if(infos != null && infos.length == 5)
			{
				String align = infos[0];
				float x = Float.parseFloat(infos[1]);
				float y = Float.parseFloat(infos[2]);
				int degree = Integer.parseInt(infos[3]);
				int alpha = Integer.parseInt(infos[4]);
				mEffect.m_alpha = alpha;
				mEffect.m_degree = degree;
				PointF pointF = computeXYWithAlign(mEffect, align, x, y);
				mEffect.m_x = pointF.x;
				mEffect.m_y = pointF.y;
			}
		}
		SetRendererEffect();
		return 0;
	}

	protected void SetRendererEffect()
	{
		if(mRenderer != null)
		{
			mRenderer.addEffect(mEffect);
		}
	}

	protected PointF computeXYWithAlign(LightEffectShapeEx item, String align, float x, float y)
	{
		PointF pointF = new PointF();
		float viewWidth = mImg.m_w * mImg.m_scaleX;
		float viewHeight = mImg.m_h * mImg.m_scaleY;
		//九宫格中心点
		float aveX = viewWidth / 3f;
		float aveY = viewHeight / 3f;
		float originX = mImg.m_x + mImg.m_centerX - mImg.m_centerX * mImg.m_scaleX;
		float originY = mImg.m_y + mImg.m_centerY - mImg.m_centerY * mImg.m_scaleY;
		if(align != null && align.length() > 0)
		{
			int flag = Integer.parseInt(align);
			//九宫格位置，1-9
			switch(flag)
			{
				case 1:
				{
					originX += aveX / 2 - item.m_centerX;
					originY += aveY / 2 - item.m_centerY;
					break;
				}
				case 2:
				{
					originX += aveX + aveX / 2 - item.m_centerX;
					originY += aveY / 2 - item.m_centerY;
					break;
				}
				case 3:
				{
					originX += aveX * 2 + aveX / 2 - item.m_centerX;
					originY += aveY / 2 - item.m_centerY;
					break;
				}
				case 4:
				{
					originX += aveX / 2 - item.m_centerX;
					originY += aveY + aveY / 2f - item.m_centerY;
					break;
				}
				case 5:
				{
					originX += aveX + aveX / 2 - item.m_centerX;
					originY += aveY + aveY / 2f - item.m_centerY;
					break;
				}
				case 6:
				{
					originX += aveX * 2 + aveX / 2 - item.m_centerX;
					originY += aveY + aveY / 2f - item.m_centerY;
					break;
				}
				case 7:
				{
					originX += aveX / 2 - item.m_centerX;
					originY += aveY * 2 + aveY / 2f - item.m_centerY;
					break;
				}
				case 8:
				{
					originX += aveX + aveX / 2 - item.m_centerX;
					originY += aveY * 2 + aveY / 2f - item.m_centerY;
					break;
				}
				case 9:
				{
					originX += aveX * 2 + aveX / 2 - item.m_centerX;
					originY += aveY * 2 + aveY / 2f - item.m_centerY;
					break;
				}

			}
		}
		pointF.x = originX + x;
		pointF.y = originY + y;
		return pointF;
	}

	public ShapeEx DelEffect()
	{
		LightEffectShapeEx out = mEffect;
		if(mEffect != null)
		{
			mEffect.m_bmp = null;
		}
		if(mRenderer != null) {
			mRenderer.addEffect(null);
		}
		mEffect = null;
		return out;
	}

	public void SetAlpha(int alpha)
	{
		if(mEffect != null)
		{
			mEffect.m_alpha = alpha;
		}
		UpdateUI();
	}

	private LightEffectRenderer.OnCaptureCallback mCaptureCB = new LightEffectRenderer.OnCaptureCallback()
	{
		@Override
		public void onCapture(Bitmap bitmap)
		{
			//必须放到主线程里面
			final Bitmap bmp = MakeBmpV2.CreateBitmapV2(bitmap, 0, MakeBmpV2.FLIP_V, -1, -1, -1, Bitmap.Config.ARGB_8888);
			((Activity)getContext()).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(mCb != null)
					{
						mCb.onGetOutputBmp(bmp, false);
					}
				}
			});
		}

		@Override
		public void onSave(Bitmap bmp)
		{
			//垂直翻转
			bmp = MakeBmpV2.CreateBitmapV2(bmp, 0, MakeBmpV2.FLIP_V, -1, -1, -1, Bitmap.Config.ARGB_8888);

			if(mCb != null)
			{
				mCb.onGetOutputBmp(bmp, true);
			}

		}
	};

	public void GetMirror()
	{
		if(mRenderer != null)
		{
			mRenderer.setSaveCB(mCaptureCB);
			mRenderer.setCapture();
			requestRender();
		}
	}

	public void GetOutputBmp(int size)
	{
		if(mImg != null)
		{
			mImg.m_bmp = null;
			if(mEffect != null)
			{
				mEffect.m_bmp = null;
			}
			ShapeEx mOutImg = new ShapeEx();
			mOutImg.Set(mImg);

			LightEffectShapeEx outEffect = null;

			float whscale = (float)mImg.m_w / (float)mImg.m_h;
			float outW = size;
			float outH = outW / whscale;
			if(outH > size)
			{
				outH = size;
				outW = outH * whscale;
			}

			Bitmap imgBmp = null;
			float scale = outW / (mImg.m_w * mImg.m_scaleX);
			float dx = outW / 2f - mImg.m_centerX;
			float dy = outH / 2f- mImg.m_centerY;

			int textureSize = GlUtil.getMaxTextureSize();
			if(outW > textureSize || outH > textureSize)
			{
				outW = outH = textureSize;
			}
			imgBmp = mCb.MakeOutputImg(mImg.m_ex, (int)(outW + 0.5), (int)(outH + 0.5));
			if(imgBmp != null && imgBmp.getWidth() > 0 && imgBmp.getHeight() > 0)
			{
				mOutImg.m_bmp = imgBmp;
				//修正(图片小于输出size)
				outW = imgBmp.getWidth();
				outH = imgBmp.getHeight();
				scale = outW / (mImg.m_w * mImg.m_scaleX);
				dx = outW / 2f - mImg.m_centerX;
				dy = outH / 2f- mImg.m_centerY;
			}

			mOutImg.m_w = (int)(outW + 0.5);
			mOutImg.m_h = (int)(outH + 0.5);
			mOutImg.m_centerX = mOutImg.m_w / 2f;
			mOutImg.m_centerY = mOutImg.m_h / 2f;

			if(mEffect != null)
			{
				mEffect.m_bmp = null;
				outEffect = new LightEffectShapeEx();
				outEffect.Set(mEffect);
				float tempW = scale * mEffect.m_scaleX * mEffect.m_w;
				float tempH = scale * mEffect.m_scaleY * mEffect.m_h;
				if(tempW > textureSize || tempH > textureSize)
				{
					tempW = tempH = textureSize;
				}

				Bitmap tempBmp = mCb.MakeOutputEffect(mEffect.m_ex, (int)(tempW + 0.5), (int)(tempH + 0.5));

				float[] src = {mEffect.m_x + mEffect.m_centerX, mEffect.m_y + mEffect.m_centerY};
				float[] dst = new float[2];

				int len = src.length / 2 * 2;
				for(int i = 0; i < len; i += 2)
				{
					dst[i] = (src[i] - mImg.m_centerX) * scale + dx + mImg.m_centerX;
					dst[i + 1] = (src[i + 1] - mImg.m_centerY) * scale + dy + mImg.m_centerY;
				}
				outEffect.m_x = dst[0] - tempBmp.getWidth() / 2f;
				outEffect.m_y = dst[1] - tempBmp.getHeight() / 2f;
				outEffect.m_w = tempBmp.getWidth();
				outEffect.m_h = tempBmp.getHeight();
				outEffect.m_centerX = outEffect.m_w / 2f;
				outEffect.m_centerY = outEffect.m_h / 2f;
				outEffect.m_scaleX = scale * mEffect.m_scaleX * mEffect.m_w / (float)tempBmp.getWidth();
				outEffect.m_scaleY = scale * mEffect.m_scaleY * mEffect.m_h / (float)tempBmp.getHeight();
				outEffect.m_bmp = tempBmp;
			}

			if(mRenderer != null)
			{
				mRenderer.setSaveCB(mCaptureCB);
				mRenderer.setOutImg(mOutImg);
				mRenderer.setOutEffect(outEffect);
				mRenderer.setFinalSave();
				requestRender();
			}
		}
	}

	public void releaseMem()
	{
		DelEffect();
		if(mCb != null)
		{
			mCb = null;
		}
		if(mImg != null)
		{
			mImg.m_bmp = null;
		}
		mImg = null;
		mEffect = null;
	}

	protected float m_centerX;    //旋转、缩放中心点
	protected float m_centerY;

	protected float m_downX;
	protected float m_downY;

	protected float m_downX1;
	protected float m_downY1;
	protected float m_downX2;
	protected float m_downY2;

	protected float m_gammaX; //移动
	protected float m_gammaY;
	protected float m_delta; //放大
	protected float m_beta; //旋转

	protected float m_oldX;
	protected float m_oldY;
	protected float m_oldScaleX;
	protected float m_oldScaleY;
	protected float m_oldDegree;

	/**
	 * 初始化移动
	 */
	protected void Init_M_Data(ShapeEx target, float x, float y)
	{
		m_gammaX = x;
		m_gammaY = y;
		m_oldX = target.m_x;
		m_oldY = target.m_y;
	}

	/**
	 * 子元件移动
	 *
	 * @param target
	 * @param x
	 * @param y
	 */
	protected void Run_M(ShapeEx target, float x, float y)
	{
		target.m_x = x - m_gammaX + m_oldX;
		target.m_y = y - m_gammaY + m_oldY;
	}

	/**
	 * 初始化旋转
	 *
	 * @param target
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	protected void Init_R_Data(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		if(x1 - x2 == 0)
		{
			if(y1 >= y2)
			{
				m_beta = 90;
			}
			else
			{
				m_beta = -90;
			}
		}
		else if(y1 - y2 != 0)
		{
			m_beta = (float)Math.toDegrees(Math.atan(((double)(y1 - y2)) / (x1 - x2)));
			if(x1 < x2)
			{
				m_beta += 180;
			}
		}
		else
		{
			if(x1 >= x2)
			{
				m_beta = 0;
			}
			else
			{
				m_beta = 180;
			}
		}
		m_oldDegree = target.m_degree;
	}

	protected void Run_R(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		float tempAngle;
		if(x1 - x2 == 0)
		{
			if(y1 >= y2)
			{
				tempAngle = 90;
			}
			else
			{
				tempAngle = -90;
			}
		}
		else if(y1 - y2 != 0)
		{
			tempAngle = (float)Math.toDegrees(Math.atan(((double)(y1 - y2)) / (x1 - x2)));
			if(x1 < x2)
			{
				tempAngle += 180;
			}
		}
		else
		{
			if(x1 >= x2)
			{
				tempAngle = 0;
			}
			else
			{
				tempAngle = 180;
			}
		}
		target.m_degree = m_oldDegree + tempAngle - m_beta;
	}

	/**
	 * 初始化缩放
	 *
	 * @param target
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	protected void Init_Z_Data(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		m_delta = ImageUtils.Spacing(x1 - x2, y1 - y2);
		m_oldScaleX = target.m_scaleX;
		m_oldScaleY = target.m_scaleY;
	}

	protected void Run_Z(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		float tempDist = ImageUtils.Spacing(x1 - x2, y1 - y2);
		if(tempDist > 10)
		{
			float scale = tempDist / m_delta;
			float scaleX = m_oldScaleX * scale;
			float scaleY = m_oldScaleY * scale;
			if(scaleX > target.MAX_SCALE)
			{
				scaleX = target.MAX_SCALE;
				scaleY = scaleX / m_oldScaleX * m_oldScaleY;
			}
			if(scaleY > target.MAX_SCALE)
			{
				scaleY = target.MAX_SCALE;
				scaleX = scaleY / m_oldScaleY * m_oldScaleX;
			}
			if(scaleX < target.MIN_SCALE)
			{
				scaleX = target.MIN_SCALE;
				scaleY = scaleX / m_oldScaleX * m_oldScaleY;
			}
			if(scaleY < target.MIN_SCALE)
			{
				scaleY = target.MIN_SCALE;
				scaleX = scaleY / m_oldScaleY * m_oldScaleX;
			}
			target.SetScaleXY(scaleX, scaleY);
		}
	}

	protected void Init_RZ_Data(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		Init_R_Data(target, x1, y1, x2, y2);
		Init_Z_Data(target, x1, y1, x2, y2);
	}

	protected void Init_MRZ_Data(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		Init_M_Data(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
		Init_R_Data(target, x1, y1, x2, y2);
		Init_Z_Data(target, x1, y1, x2, y2);
	}

	protected void Run_RZ(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		Run_R(target, x1, y1, x2, y2);
		Run_Z(target, x1, y1, x2, y2);
	}

	protected void Run_MRZ(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		Run_M(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
		Run_R(target, x1, y1, x2, y2);
		Run_Z(target, x1, y1, x2, y2);
	}

	public static interface ControlCallback
	{
		public Bitmap MakeShowImg(Object info, int frW, int frH);

		public Bitmap MakeOutputImg(Object info, int outW, int outH);

		public Bitmap MakeShowEffect(Object info, int frW, int frH);

		public Bitmap MakeOutputEffect(Object info, int outW, int outH);

		public void SelectPendant(int index);

		public void onTouch(boolean isTouch);

		public void onGetOutputBmp(Bitmap bmp, boolean save);
	}
}
