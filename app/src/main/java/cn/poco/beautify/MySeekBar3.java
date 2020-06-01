package cn.poco.beautify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * 裁剪界面用到的刻度
 */
public class MySeekBar3 extends SeekBar
{
	protected int m_dotNum;
	protected int m_moveDotNum;
	protected Bitmap m_dot1;
	protected Bitmap m_dot2;
	public MySeekBar3(Context context)
	{
		super(context);
		Init();
	}

	public MySeekBar3(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		Init();
	}

	public MySeekBar3(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		Init();
	}

	protected void Init()
	{
		m_dot1 = Bitmap.createBitmap(ShareData.PxToDpi_xhdpi(5), ShareData.PxToDpi_xhdpi(20), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(m_dot1);
		canvas.drawColor(0xff999999);
		m_dot2 = Bitmap.createBitmap(ShareData.PxToDpi_xhdpi(2), ShareData.PxToDpi_xhdpi(10), Bitmap.Config.ARGB_8888);
		canvas = new Canvas(m_dot2);
		canvas.drawColor(0xff999999);
		if(android.os.Build.VERSION.SDK_INT >= 21)
		{
			this.setSplitTrack(false);
		}
		this.setBackgroundColor(0);
		this.setThumb(getResources().getDrawable(R.drawable.clip_seekbar_thumb));
		int seekbar_thumb_w2 = ShareData.PxToDpi_xhdpi(2);
		this.setPadding(seekbar_thumb_w2, 0, seekbar_thumb_w2, 0);
		this.setProgressDrawable(new MyDrawable());
		this.setMinimumHeight(ShareData.PxToDpi_xhdpi(20));
		this.setMax(100);
	}

	/**
	 *
	 * @param num	>0 向左移
	 *              <0 向右移
	 */
	public void MoveDot(int num)
	{
		m_moveDotNum = num;
	}

	public void SetDotsNum(int num)
	{
		m_dotNum = num;
	}

	public int GetDotsNum()
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
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			if(m_dotNum > 1)
			{
				float dx = (float)(rect.right - rect.left) / (float)m_dotNum;
				int mid = m_dotNum / 2;
				for(int i = m_moveDotNum; i <= m_dotNum + m_moveDotNum; i++)
				{
					int index = i - m_moveDotNum;
					if(index <= mid)
					{
						int alpha = (int)(index / (float)mid * 255);
						temp_paint.setAlpha(alpha);
					}
					else
					{
						int alpha = (int)((m_dotNum - index) / (float)(m_dotNum - mid) * 255);
						temp_paint.setAlpha(alpha);
					}
					if(i % 5 == 0)
					{
						DrawDot(canvas, (int)(x + dx * (i - m_moveDotNum)  + 0.5f), y);
					}
					else
					{
						DrawDot2(canvas, (int)(x + dx * (i - m_moveDotNum) + 0.5f), y);
					}
				}
			}
			else
			{
				DrawDot(canvas, x, y);
			}
		}

		protected Matrix temp_matrix = new Matrix();
		protected Paint temp_paint = new Paint();

		protected void DrawDot(Canvas canvas, int cx, int cy)
		{
			if(m_dot1 != null && !m_dot1.isRecycled())
			{
				temp_matrix.reset();
				temp_matrix.postTranslate(cx - (int)(m_dot1.getWidth() / 2f + 0.5f), cy - (int)(m_dot1.getHeight() / 2f + 0.5f));
				canvas.drawBitmap(m_dot1, temp_matrix, temp_paint);
			}
		}

		protected void DrawDot2(Canvas canvas, int cx, int cy)
		{
			if(m_dot2 != null && !m_dot2.isRecycled())
			{
				temp_matrix.reset();
				temp_matrix.postTranslate(cx - (int)(m_dot2.getWidth() / 2f + 0.5f), cy - (int)(m_dot2.getHeight() / 2f + 0.5f));
				canvas.drawBitmap(m_dot2, temp_matrix, temp_paint);
			}
		}
	}
}
