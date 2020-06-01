package cn.poco.beautify;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

public class MySeekBar2 extends SeekBar
{
	protected Bitmap m_dot;
	protected int m_dotNum = 11;

	public MySeekBar2(Context context)
	{
		super(context);

		Init();
	}

	public MySeekBar2(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		Init();
	}

	public MySeekBar2(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);

		Init();
	}

	@SuppressLint("NewApi")
	protected void Init()
	{
		m_dot = BitmapFactory.decodeResource(getResources(), R.drawable.beauty_seekbar_bk_dot);
		if(android.os.Build.VERSION.SDK_INT >= 21)
		{
			this.setSplitTrack(false);
		}
		this.setBackgroundColor(0);
		this.setThumb(getResources().getDrawable(R.drawable.beauty_seekbar_thumb_out));
		int seekbar_thumb_w2 = ShareData.PxToDpi_xhdpi(21);
		this.setPadding(seekbar_thumb_w2, 0, seekbar_thumb_w2, 0);
		this.setProgressDrawable(new MyDrawable());
		this.setMinimumHeight(ShareData.PxToDpi_xhdpi(80));
		this.setMax(100);
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
}
