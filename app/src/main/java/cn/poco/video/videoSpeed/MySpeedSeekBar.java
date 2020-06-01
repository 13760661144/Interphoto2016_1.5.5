package cn.poco.video.videoSpeed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import cn.poco.tianutils.ShareData;

public class MySpeedSeekBar extends SeekBar
{
	protected Bitmap m_dot;
	protected int m_dotNum = 5;
	private Paint mPaint;
	public MySpeedSeekBar(Context context)
	{
		super(context);

		Init();
	}

	public MySpeedSeekBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		Init();
	}

	public MySpeedSeekBar(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);

		Init();
	}

	@SuppressLint("NewApi")
	protected void Init()
	{
		mPaint = new Paint();
		mPaint.setColor(0xff333333);
		mPaint.setAntiAlias(true);

		m_dot = getDotBitmap();
		if(android.os.Build.VERSION.SDK_INT >= 21)
		{
			this.setSplitTrack(false);
		}
		setThumbOffset(0);
		this.setBackgroundColor(0);
		Drawable drawable = new BitmapDrawable(getResources(),getThumbBitmap());
		drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
		setThumb(drawable);
//		int seekbar_thumb_w2 = ShareData.PxToDpi_xhdpi(21);
//		this.setPadding(seekbar_thumb_w2, 0, seekbar_thumb_w2, 0);
		this.setProgressDrawable(new MyDrawable());
		this.setMinimumHeight(ShareData.PxToDpi_xhdpi(80));
	}

	public void SetDotNum(int num)
	{
		if(num > 0)
		{
			m_dotNum = num;
			this.invalidate();
		}
	}
	
	public int GetDotNum()
	{
		return m_dotNum;
	}

	protected class MyDrawable extends ColorDrawable
	{
		@Override
		public void draw(Canvas canvas)
		{
			Rect rect = getBounds();
			int x = rect.left;
			int y = (int)((rect.bottom + rect.top) / 2f + 0.5f);
			canvas.drawRect(rect.left,y-ShareData.PxToDpi_xhdpi(1),rect.right,y+ShareData.PxToDpi_xhdpi(1),mPaint);
			if(m_dotNum > 1)
			{
				float dx = (float)(rect.right - rect.left) / (float)(m_dotNum - 1);
				for(int i = 0; i < m_dotNum; i++)
				{
					DrawDot(canvas, (int)(x + dx * i + 0.5f), y);
				}
			}
			else
			{
				DrawDot(canvas, x, y);
			}
		}

		protected Matrix temp_matrix = new Matrix();

		protected void DrawDot(Canvas canvas, int cx, int cy)
		{
			if(m_dot != null && !m_dot.isRecycled())
			{
				temp_matrix.reset();
				temp_matrix.postTranslate(cx - (int)(m_dot.getWidth() / 2f + 0.5f), cy - (int)(m_dot.getHeight() / 2f + 0.5f));
				canvas.drawBitmap(m_dot, temp_matrix, null);
			}
		}
	}

	@Override
	protected synchronized void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}

	private Bitmap getDotBitmap()
	{
		Bitmap bitmap = Bitmap.createBitmap(ShareData.PxToDpi_xhdpi(32),ShareData.PxToDpi_xhdpi(32), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xff333333);
		canvas.drawCircle(bitmap.getWidth()/2,bitmap.getHeight()/2,bitmap.getWidth()/2,paint);
		paint.setColor(0x1affffff);
		canvas.drawCircle(bitmap.getWidth()/2,bitmap.getHeight()/2,ShareData.PxToDpi_xhdpi(8),paint);
		return bitmap;
	}
	private Bitmap getThumbBitmap()
	{
		Bitmap bitmap = Bitmap.createBitmap(ShareData.PxToDpi_xhdpi(48),ShareData.PxToDpi_xhdpi(48), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(0xffffc433);
		canvas.drawCircle(bitmap.getWidth()/2,bitmap.getHeight()/2,bitmap.getWidth()/2,paint);
		paint.setColor(0xffffffff);
		canvas.drawCircle(bitmap.getWidth()/2,bitmap.getHeight()/2,ShareData.PxToDpi_xhdpi(8),paint);
		return bitmap;
	}
}
